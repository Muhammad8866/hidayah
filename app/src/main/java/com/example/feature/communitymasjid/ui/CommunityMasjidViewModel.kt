package com.example.feature.communitymasjid.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.communitymasjid.data.CmRepository
import com.example.feature.communitymasjid.data.remote.CmCommunityUnavailableException
import com.example.feature.communitymasjid.data.remote.CmWriteOutcome
import com.example.feature.communitymasjid.domain.CmTimingRules
import com.example.feature.communitymasjid.location.CmLocationProvider
import com.example.feature.communitymasjid.model.CmCommunityRecord
import com.example.feature.communitymasjid.model.CmDownReason
import com.example.feature.communitymasjid.model.CmHistoryEntry
import com.example.feature.communitymasjid.model.CmMosque
import com.example.feature.communitymasjid.model.CmMyVotes
import com.example.feature.communitymasjid.model.CmPresence
import com.example.feature.communitymasjid.model.CmTimings
import com.example.feature.communitymasjid.model.CmUserLocation
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class CmDetailState(
    val mosque: CmMosque,
    val record: CmCommunityRecord? = null,
    val myVotes: CmMyVotes = CmMyVotes(),
    val myUid: String? = null,
    val history: List<CmHistoryEntry>? = null,
    val isLoading: Boolean = false,
    val isHistoryLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null
) {
    val isOwnMosque: Boolean get() = myUid != null && mosque.addedByUid == myUid
    val isOwnTiming: Boolean get() = myUid != null && record?.updatedByUid == myUid
}

data class CmUiState(
    val hasLocationPermission: Boolean = false,
    val isLocating: Boolean = false,
    val location: CmUserLocation? = null,
    val mosques: List<CmMosque> = emptyList(),
    val records: Map<String, CmCommunityRecord> = emptyMap(),
    val isLoadingList: Boolean = false,
    val listError: String? = null,
    val showingCachedList: Boolean = false,
    val communityConfigured: Boolean = true,
    val communityError: String? = null,
    val detail: CmDetailState? = null,
    val isAddingMosque: Boolean = false,
    val guidelinesAccepted: Boolean = false,
    val isDeletingData: Boolean = false,
    val message: String? = null
) {
    /** Masjids voted down by the community are left out of the list. */
    val visibleMosques: List<CmMosque>
        get() = mosques.filter { mosque ->
            records[mosque.docId]?.let { CmTimingRules.isVotedDown(it.upVotes, it.downVotes) } != true
        }
}

/** Presence of the user relative to one masjid, derived from the latest fix. */
data class CmProximity(val distanceMetres: Int, val accuracyMetres: Int, val isWithinRadius: Boolean, val isAccurateEnough: Boolean) {
    val canAct: Boolean get() = isWithinRadius && isAccurateEnough
}

class CommunityMasjidViewModel(application: Application) : AndroidViewModel(application) {
    private val repository by lazy { CmRepository(application) }
    private val locationProvider = CmLocationProvider(application)

    private val prefs = application.getSharedPreferences("community_masjid", android.content.Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(CmUiState(guidelinesAccepted = prefs.getBoolean(KEY_GUIDELINES, false)))
    val state: StateFlow<CmUiState> = _state.asStateFlow()

    // Any unexpected failure is surfaced inside this tab and never propagates to the app.
    private val guard = CoroutineExceptionHandler { _, error ->
        _state.update {
            it.copy(
                isLocating = false,
                isLoadingList = false,
                isAddingMosque = false,
                detail = it.detail?.copy(isLoading = false, isSubmitting = false, isHistoryLoading = false),
                message = friendly(error)
            )
        }
    }
    private var listJob: Job? = null
    private var detailJob: Job? = null
    private var started = false

    fun onScreenShown() {
        val granted = locationProvider.hasPermission()
        _state.update { it.copy(hasLocationPermission = granted, communityConfigured = runCatching { repository.isCommunityConfigured }.getOrDefault(false)) }
        if (!started && granted) {
            started = true
            refreshList(force = false)
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _state.update { it.copy(hasLocationPermission = granted) }
        if (granted) {
            started = true
            refreshList(force = false)
        }
    }

    /**
     * Speed path: (1) the saved list appears instantly, (2) a quick Wi-Fi/cell or recent cached fix
     * is used instead of waiting for GPS, (3) OSM and community lists load in parallel, and
     * (4) a precise GPS fix arriving later only triggers a reload if the user is far from the quick one.
     */
    fun refreshList(force: Boolean) {
        // A manual refresh replaces a job that may only be waiting for the precise fix.
        if (listJob?.isActive == true && !force) return
        listJob?.cancel()
        listJob = viewModelScope.launch(guard) {
            _state.update { it.copy(isLoadingList = true, listError = null) }
            if (_state.value.mosques.isEmpty()) {
                val saved = runCatching { repository.savedMosques(_state.value.location) }.getOrDefault(emptyList())
                if (saved.isNotEmpty()) {
                    val ids = saved.map { it.docId }
                    _state.update {
                        it.copy(mosques = saved, records = runCatching { repository.cachedRecords(ids) }.getOrDefault(it.records))
                    }
                }
            }

            _state.update { it.copy(isLocating = true) }
            val location = locationProvider.quickLocation() ?: _state.value.location
            _state.update { it.copy(isLocating = false, location = location ?: it.location) }
            if (location == null) {
                _state.update { it.copy(isLoadingList = false, listError = "Could not get your location. Turn on device location and try again.") }
                return@launch
            }
            loadNearby(location, force)

            // Refine in the background; the precise fix is also reused for presence checks.
            val precise = locationProvider.currentLocation(timeoutMs = 15_000) ?: return@launch
            _state.update { it.copy(location = precise) }
            val moved = CmTimingRules.distanceMetres(location.latitude, location.longitude, precise.latitude, precise.longitude)
            if (moved > RELOAD_AFTER_MOVE_METRES) loadNearby(precise, force = false)
        }
    }

    private suspend fun loadNearby(location: CmUserLocation, force: Boolean) {
        _state.update { it.copy(isLoadingList = true) }
        val result = repository.nearbyMosques(location, force)
        val ids = result.mosques.map { it.docId }
        _state.update {
            it.copy(
                mosques = result.mosques,
                isLoadingList = false,
                showingCachedList = result.fromCache && result.error != null,
                listError = if (result.error != null && result.mosques.isEmpty()) "Could not load nearby masjids. Check your internet connection." else null,
                records = it.records + runCatching { repository.cachedRecords(ids) }.getOrDefault(emptyMap())
            )
        }
        // Community data is optional: its failure leaves the OSM list fully usable.
        try {
            if (repository.isCommunityConfigured && ids.isNotEmpty()) {
                val records = repository.refreshRecords(ids, force)
                _state.update { it.copy(records = it.records + records, communityError = null) }
            }
        } catch (error: Exception) {
            _state.update { it.copy(communityError = friendly(error)) }
        }
    }

    fun openMosque(mosque: CmMosque) {
        _state.update { it.copy(detail = CmDetailState(mosque = mosque, record = it.records[mosque.docId], isLoading = true)) }
        loadDetail(force = false)
    }

    fun closeMosque() {
        detailJob?.cancel()
        _state.update { it.copy(detail = null) }
    }

    fun refreshDetail() = loadDetail(force = true)

    private fun loadDetail(force: Boolean) {
        val mosque = _state.value.detail?.mosque ?: return
        detailJob?.cancel()
        detailJob = viewModelScope.launch(guard) {
            updateDetail(mosque.docId) { it.copy(isLoading = true, error = null) }
            // A stale fix would show the wrong distance; refresh it alongside the network load.
            if (_state.value.location == null || System.currentTimeMillis() - (_state.value.location?.fixedAtMs ?: 0) > LOCATION_MAX_AGE_MS) {
                refreshLocation()
            }
            runCatching { repository.cachedRecords(listOf(mosque.docId))[mosque.docId] }.getOrNull()?.let { cached ->
                updateDetail(mosque.docId) { it.copy(record = cached) }
            }
            try {
                val record = repository.record(mosque.docId, force)
                val uid = repository.currentUid()
                val votes = repository.myVotes(mosque.docId, record.currentVersion)
                updateDetail(mosque.docId) { it.copy(record = record, myUid = uid, myVotes = votes, isLoading = false) }
                _state.update { it.copy(records = it.records + (mosque.docId to record)) }
            } catch (error: Exception) {
                updateDetail(mosque.docId) { it.copy(isLoading = false, error = friendly(error)) }
            }
        }
    }

    fun loadHistory() {
        val mosque = _state.value.detail?.mosque ?: return
        viewModelScope.launch(guard) {
            updateDetail(mosque.docId) { it.copy(isHistoryLoading = true) }
            try {
                val history = repository.history(mosque.docId)
                updateDetail(mosque.docId) { it.copy(history = history, isHistoryLoading = false) }
            } catch (error: Exception) {
                updateDetail(mosque.docId) { it.copy(isHistoryLoading = false) }
                _state.update { it.copy(message = friendly(error)) }
            }
        }
    }

    fun refreshLocation() {
        if (_state.value.isLocating) return
        viewModelScope.launch(guard) {
            _state.update { it.copy(isLocating = true) }
            val location = locationProvider.currentLocation()
            _state.update { it.copy(isLocating = false, location = location ?: it.location) }
            if (location == null) _state.update { it.copy(message = "Could not get a location fix. Turn on GPS and try again.") }
        }
    }

    fun proximityTo(mosque: CmMosque, location: CmUserLocation?): CmProximity? {
        location ?: return null
        val distance = CmTimingRules.distanceMetres(location.latitude, location.longitude, mosque.latitude, mosque.longitude)
        return CmProximity(
            distanceMetres = distance.roundToInt(),
            accuracyMetres = location.accuracyMetres.roundToInt(),
            isWithinRadius = distance <= CmTimingRules.PRESENCE_RADIUS_METRES,
            isAccurateEnough = location.accuracyMetres <= CmTimingRules.MAX_LOCATION_ACCURACY_METRES
        )
    }

    /** Returns validation problems synchronously so the editor can show them inline. */
    fun validate(mosque: CmMosque, timings: CmTimings): List<String> {
        val problems = CmTimingRules.validate(timings, mosque.latitude, mosque.longitude).toMutableList()
        if (CmTimingRules.isSameAs(timings, _state.value.detail?.record?.current)) {
            problems += "These timings are the same as the current timings"
        }
        return problems
    }

    /** New timings become current immediately; the community can vote them down afterwards. */
    fun submitTiming(timings: CmTimings, onDone: (Boolean) -> Unit) {
        val detail = _state.value.detail ?: return
        if (detail.isSubmitting) return
        val mosque = detail.mosque
        updateDetail(mosque.docId) { it.copy(isSubmitting = true) }
        viewModelScope.launch(guard) {
            try {
                val problems = validate(mosque, timings)
                if (problems.isNotEmpty()) {
                    _state.update { it.copy(message = problems.first()) }
                    onDone(false); return@launch
                }
                repository.localRateLimitProblem(CmRepository.KIND_SET_TIMING)?.let { problem ->
                    _state.update { it.copy(message = problem) }
                    onDone(false); return@launch
                }
                val presence = freshPresence(mosque) ?: run { onDone(false); return@launch }
                val message = when (repository.setTiming(mosque, timings, presence)) {
                    is CmWriteOutcome.TimingSet -> "Timings updated. Others at the masjid can vote on them."
                    CmWriteOutcome.Unchanged -> "These timings are already current"
                    else -> "This masjid changed meanwhile. Please try again."
                }
                _state.update { it.copy(message = message) }
                onDone(true)
            } catch (error: Exception) {
                _state.update { it.copy(message = friendly(error)) }
                onDone(false)
                return@launch
            } finally {
                updateDetail(mosque.docId) { it.copy(isSubmitting = false) }
            }
            loadDetail(force = true)
        }
    }

    /** 👍 / 👎 on the current timing. Enough 👎 revert it to the previous timing. */
    fun voteTiming(vote: Int) {
        val detail = _state.value.detail ?: return
        val record = detail.record ?: return
        if (detail.isSubmitting) return
        val mosque = detail.mosque
        runVote(mosque) { presence ->
            when (val outcome = repository.voteTiming(mosque.docId, record.currentVersion, vote, presence)) {
                is CmWriteOutcome.Voted -> if (outcome.votedDown) {
                    "Enough people voted these timings wrong — the previous timings are restored."
                } else {
                    "Vote saved (👍 ${outcome.up} · 👎 ${outcome.down})."
                }
                CmWriteOutcome.Unchanged -> "You already voted this way"
                CmWriteOutcome.OwnContent -> "You cannot vote on timings you set"
                else -> "The timings changed meanwhile. Please check them again."
            }
        }
    }

    /** 👍 / 👎 on the masjid itself. Enough 👎 remove it from the list. */
    fun voteMosque(vote: Int, reason: CmDownReason?) {
        val detail = _state.value.detail ?: return
        if (detail.isSubmitting) return
        val mosque = detail.mosque
        runVote(mosque) { presence ->
            when (val outcome = repository.voteMosque(mosque, vote, reason, presence)) {
                is CmWriteOutcome.Voted -> if (outcome.votedDown) {
                    removeMosque(mosque.docId)
                    "Enough people reported this masjid — it has been removed from the list."
                } else {
                    "Vote saved (👍 ${outcome.up} · 👎 ${outcome.down})."
                }
                CmWriteOutcome.Unchanged -> "You already voted this way"
                CmWriteOutcome.OwnContent -> "You cannot vote on a masjid you added"
                else -> "This masjid no longer exists"
            }
        }
    }

    private fun runVote(mosque: CmMosque, action: suspend (CmPresence) -> String) {
        updateDetail(mosque.docId) { it.copy(isSubmitting = true) }
        viewModelScope.launch(guard) {
            try {
                repository.localRateLimitProblem(CmRepository.KIND_VOTE)?.let { problem ->
                    _state.update { it.copy(message = problem) }
                    return@launch
                }
                val presence = freshPresence(mosque) ?: return@launch
                val message = action(presence)
                _state.update { it.copy(message = message) }
            } catch (error: Exception) {
                _state.update { it.copy(message = friendly(error)) }
                return@launch
            } finally {
                updateDetail(mosque.docId) { it.copy(isSubmitting = false) }
            }
            loadDetail(force = true)
        }
    }

    private fun removeMosque(docId: String) {
        detailJob?.cancel()
        _state.update { state ->
            state.copy(
                mosques = state.mosques.filterNot { it.docId == docId },
                detail = state.detail?.takeUnless { it.mosque.docId == docId }
            )
        }
    }

    /** Problems with the add-masjid form; empty means it can be submitted. */
    fun validateNewMosque(name: String, address: String): List<String> = buildList {
        val trimmed = name.trim()
        if (trimmed.length < CmTimingRules.MOSQUE_NAME_MIN) add("Enter the masjid name (at least ${CmTimingRules.MOSQUE_NAME_MIN} letters)")
        if (trimmed.length > CmTimingRules.MOSQUE_NAME_MAX) add("Name is too long")
        if (address.trim().length > CmTimingRules.MOSQUE_ADDRESS_MAX) add("Address is too long")
    }

    /** Adds a masjid at the user's current GPS position, which must be precise and not next to a listed masjid. */
    fun addMosque(name: String, address: String, onDone: (Boolean) -> Unit) {
        if (_state.value.isAddingMosque) return
        _state.update { it.copy(isAddingMosque = true) }
        viewModelScope.launch(guard) {
            try {
                validateNewMosque(name, address).firstOrNull()?.let { problem ->
                    _state.update { it.copy(message = problem) }
                    onDone(false); return@launch
                }
                repository.localRateLimitProblem(CmRepository.KIND_ADD_MOSQUE)?.let { problem ->
                    _state.update { it.copy(message = problem) }
                    onDone(false); return@launch
                }
                _state.update { it.copy(isLocating = true) }
                val location = locationProvider.currentLocation()
                _state.update { it.copy(isLocating = false, location = location ?: it.location) }
                if (location == null) {
                    _state.update { it.copy(message = "Could not get your location. Turn on GPS and try again.") }
                    onDone(false); return@launch
                }
                val accuracy = location.accuracyMetres.roundToInt()
                if (accuracy > CmTimingRules.ADD_MOSQUE_MAX_ACCURACY_METRES) {
                    _state.update { it.copy(message = "Location is not precise enough (±$accuracy m, need ±${CmTimingRules.ADD_MOSQUE_MAX_ACCURACY_METRES} m). Move to open sky near the masjid and retry.") }
                    onDone(false); return@launch
                }
                val nearest = _state.value.visibleMosques.minByOrNull {
                    CmTimingRules.distanceMetres(location.latitude, location.longitude, it.latitude, it.longitude)
                }
                val nearestDistance = nearest?.let { CmTimingRules.distanceMetres(location.latitude, location.longitude, it.latitude, it.longitude) }
                if (nearest != null && nearestDistance != null && nearestDistance <= CmTimingRules.DUPLICATE_MOSQUE_RADIUS_METRES) {
                    _state.update { it.copy(message = "\"${nearest.name}\" is already listed ${nearestDistance.roundToInt()} m from here. Open it from the list instead.") }
                    onDone(false); return@launch
                }
                val mosque = repository.addMosque(name.trim(), address.trim(), location, accuracy)
                _state.update {
                    it.copy(
                        mosques = (it.mosques + mosque).sortedBy { m -> m.distanceMetres ?: Double.MAX_VALUE },
                        message = "Masjid added. Everyone nearby can see it now."
                    )
                }
                onDone(true)
                openMosque(mosque)
            } catch (error: Exception) {
                _state.update { it.copy(message = friendly(error)) }
                onDone(false)
            } finally {
                _state.update { it.copy(isAddingMosque = false, isLocating = false) }
            }
        }
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }

    /** Shown once before the first contribution (Play UGC policy: users accept the rules first). */
    fun acceptGuidelines() {
        prefs.edit().putBoolean(KEY_GUIDELINES, true).apply()
        _state.update { it.copy(guidelinesAccepted = true) }
    }

    /** Sends a moderation report; needs no presence at the masjid. */
    fun report(target: String, reason: String) {
        val mosque = _state.value.detail?.mosque ?: return
        viewModelScope.launch(guard) {
            val message = runCatching { repository.report(mosque.docId, target, reason) }
                .fold({ "Thank you. The report was sent for review." }, { friendly(it) })
            _state.update { it.copy(message = message) }
        }
    }

    /**
     * Deletes this user's City challenge board entries and the anonymous account. Votes, timings and
     * masjids already shared stay as anonymous community data that no longer links to this device.
     */
    fun deleteMyCommunityData() {
        if (_state.value.isDeletingData) return
        _state.update { it.copy(isDeletingData = true) }
        viewModelScope.launch(guard) {
            val app = getApplication<Application>()
            val result = runCatching {
                val local = com.example.feature.citychallenge.CcLocalStore(app)
                local.profile?.let { profile ->
                    if (repository.hasAccount) {
                        com.example.feature.citychallenge.CcRemote(app).deleteMine(profile.cityId, java.time.LocalDate.now())
                    }
                }
                local.profile = null
                repository.deleteAccount()
                prefs.edit().remove(KEY_GUIDELINES).apply()
            }
            _state.update {
                it.copy(
                    isDeletingData = false,
                    detail = null,
                    guidelinesAccepted = if (result.isSuccess) false else it.guidelinesAccepted,
                    message = result.fold({ "Your community data and anonymous account were deleted." }, { e -> friendly(e) })
                )
            }
        }
    }

    /** Takes a fresh fix and enforces the 200 m presence rule before any write. */
    private suspend fun freshPresence(mosque: CmMosque): CmPresence? {
        _state.update { it.copy(isLocating = true) }
        val location = locationProvider.currentLocation()
        _state.update { it.copy(isLocating = false, location = location ?: it.location) }
        if (location == null) {
            _state.update { it.copy(message = "Could not verify your location. Turn on GPS and try again.") }
            return null
        }
        val proximity = proximityTo(mosque, location)!!
        return when {
            !proximity.isAccurateEnough -> {
                _state.update { it.copy(message = "Location is not precise enough (±${proximity.accuracyMetres} m). Move to open sky and retry.") }
                null
            }
            !proximity.isWithinRadius -> {
                _state.update { it.copy(message = "You are ${proximity.distanceMetres} m away. You must be within ${CmTimingRules.PRESENCE_RADIUS_METRES} m of the masjid.") }
                null
            }
            else -> CmPresence(proximity.distanceMetres, proximity.accuracyMetres)
        }
    }

    private fun updateDetail(docId: String, transform: (CmDetailState) -> CmDetailState) {
        _state.update { state ->
            val detail = state.detail
            if (detail == null || detail.mosque.docId != docId) state else state.copy(detail = transform(detail))
        }
    }

    private fun friendly(error: Throwable): String = when {
        error is CmCommunityUnavailableException -> error.message ?: "Community timings are unavailable"
        error is FirebaseNetworkException -> "Network problem. Please check your internet connection."
        error is FirebaseAuthException || error.message?.contains("ADMIN_ONLY_OPERATION") == true ->
            "Community sign-in is not enabled on the server yet. Please try again later."
        error is FirebaseFirestoreException && error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "Rejected by server: you may be submitting too often, or the data was invalid"
        error is FirebaseFirestoreException && error.code == FirebaseFirestoreException.Code.UNAVAILABLE ->
            "You are offline. Showing saved data."
        error is FirebaseFirestoreException && error.code == FirebaseFirestoreException.Code.NOT_FOUND ->
            "Community server is not set up yet. Please try again later."
        error is FirebaseFirestoreException && error.code == FirebaseFirestoreException.Code.ABORTED ->
            "Someone else updated this masjid at the same moment. Please try again."
        error is java.io.IOException -> "Network problem. Please check your internet connection."
        else -> "Something went wrong: ${error.message ?: error.javaClass.simpleName}"
    }

    companion object {
        private const val LOCATION_MAX_AGE_MS = 2 * 60 * 1000L
        private const val RELOAD_AFTER_MOVE_METRES = 500.0
        private const val KEY_GUIDELINES = "guidelines_accepted"
    }
}
