package com.example.feature.citychallenge

import android.content.Context
import android.location.Geocoder
import com.example.feature.communitymasjid.data.remote.awaitCm
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.min

/**
 * City challenge: everyone in the same city competes on one weekly board, and each area's
 * members add up to an area score. Scores come from the app's own logs, never typed in.
 *
 * Firestore: cc_cities/{cityId}/cc_weeks/{weekId}/cc_members/{uid}. The point formula and
 * every cap below are mirrored in firestore.rules, so change both together.
 */
enum class CcGoal(val label: String, val unit: String, val field: String, val weeklyCap: Int) {
    SALAH("Salah", "prayers", "salah", 35),
    QURAN("Quran", "pages", "quran", 140),
    DHIKR("Dhikr", "counts", "dhikr", 7000),
    HIFZ("Hifz", "ayahs", "hifz", 70);

    companion object {
        const val SALAH_POINTS = 10
        const val QURAN_POINTS = 5
        const val HIFZ_POINTS = 4
        /** One point for every ten dhikr. */
        const val DHIKR_PER_POINT = 10
        const val QURAN_DAILY_CAP = 20
        const val DHIKR_DAILY_CAP = 1000
    }
}

data class CcScores(val salah: Int = 0, val quran: Int = 0, val dhikr: Int = 0, val hifz: Int = 0) {
    val points: Int get() = salah * CcGoal.SALAH_POINTS + quran * CcGoal.QURAN_POINTS +
        hifz * CcGoal.HIFZ_POINTS + dhikr / CcGoal.DHIKR_PER_POINT

    fun of(goal: CcGoal?): Int = when (goal) {
        null -> points
        CcGoal.SALAH -> salah
        CcGoal.QURAN -> quran
        CcGoal.DHIKR -> dhikr
        CcGoal.HIFZ -> hifz
    }

    fun pointsOf(goal: CcGoal): Int = when (goal) {
        CcGoal.SALAH -> salah * CcGoal.SALAH_POINTS
        CcGoal.QURAN -> quran * CcGoal.QURAN_POINTS
        CcGoal.DHIKR -> dhikr / CcGoal.DHIKR_PER_POINT
        CcGoal.HIFZ -> hifz * CcGoal.HIFZ_POINTS
    }

    operator fun plus(o: CcScores) = CcScores(salah + o.salah, quran + o.quran, dhikr + o.dhikr, hifz + o.hifz)
}

/** Where the user competes. [cityId] and [areaKey] are stable slugs; the names are for display. */
data class CcProfile(
    val displayName: String,
    val cityName: String,
    val countryCode: String,
    val areaName: String
) {
    val cityId: String get() = "${countryCode.lowercase(Locale.ROOT).ifBlank { "xx" }}-${slug(cityName)}"
    val areaKey: String get() = slug(areaName.ifBlank { OTHER_AREA })
}

data class CcMember(val uid: String, val name: String, val area: String, val areaKey: String, val scores: CcScores)

data class CcArea(val key: String, val name: String, val members: Int, val scores: CcScores)

data class CcBoard(
    val weekId: String,
    val members: List<CcMember>,
    val totalMembers: Int,
    val myUid: String,
    /** 1-based city rank by points; null when this user has no document yet. */
    val myRank: Int?
) {
    val areas: List<CcArea>
        get() = members.groupBy { it.areaKey }.map { (key, list) ->
            CcArea(
                key = key,
                name = list.groupingBy { it.area }.eachCount().maxByOrNull { it.value }?.key ?: key,
                members = list.size,
                scores = list.fold(CcScores()) { acc, m -> acc + m.scores }
            )
        }
}

class CcUnavailableException(message: String) : Exception(message)

internal const val OTHER_AREA = "Other areas"

internal fun slug(text: String): String {
    val s = text.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]+"), "-").trim('-').take(40).trim('-')
    // City names typed in a non-Latin script still need a stable, rule-safe id.
    return if (s.length >= 2) s else "u" + text.trim().lowercase(Locale.ROOT).hashCode().absoluteValue.toString(36)
}

internal fun weekStart(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

internal fun weekId(date: LocalDate): String =
    "%d-w%02d".format(Locale.ROOT, date.get(IsoFields.WEEK_BASED_YEAR), date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR))

/** Display names: no line breaks or control characters, collapsed spaces, at most [max] chars. */
internal fun ccCleanName(text: String, max: Int): String =
    text.replace(Regex("\\p{Cntrl}"), " ").replace(Regex("\\s+"), " ").trim().take(max).trim()

/** Prayer marks that count as offered; "Late" is still a prayer prayed. */
internal val CC_PRAYED_MARKS = setOf("Jamaat", "Prayed", "Late")

/** Local profile and the week's scores, read from the logs other screens already keep. */
class CcLocalStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("city_challenge", Context.MODE_PRIVATE)
    private val tools = context.applicationContext.getSharedPreferences("daily_tools", Context.MODE_PRIVATE)

    var profile: CcProfile?
        get() {
            val city = prefs.getString("city", null) ?: return null
            return CcProfile(
                displayName = prefs.getString("name", "") ?: "",
                cityName = city,
                countryCode = prefs.getString("country", "") ?: "",
                areaName = prefs.getString("area", "") ?: ""
            )
        }
        set(value) {
            val e = prefs.edit()
            if (value == null) e.remove("city").remove("country").remove("area")
            else e.putString("name", value.displayName).putString("city", value.cityName)
                .putString("country", value.countryCode).putString("area", value.areaName)
            e.apply()
        }

    /** Name from the old Family challenge screen, so returning users do not retype it. */
    fun suggestedName(context: Context): String =
        prefs.getString("name", null)
            ?: context.getSharedPreferences("family_challenges", Context.MODE_PRIVATE).getString("name", null)
                ?.takeUnless { it == "You" } ?: ""

    var lastSyncMs: Long
        get() = prefs.getLong("last_sync", 0L)
        set(value) = prefs.edit().putLong("last_sync", value).apply()

    /**
     * This week's scores. Salah and Quran come from their daily logs, dhikr from the tasbeeh's
     * daily count; Hifz counts ayahs that became confident since the week's first check.
     */
    fun weekScores(today: LocalDate, confidentHifzAyahs: Int): CcScores {
        val days = generateSequence(weekStart(today)) { it.plusDays(1) }.takeWhile { !it.isAfter(today) }.toList()
        val salah = days.sumOf { d -> PRAYERS.count { tools.getString("prayer_${d}_$it", null) in CC_PRAYED_MARKS } }
        val quran = days.sumOf { min(tools.getInt("quran_$it", 0), CcGoal.QURAN_DAILY_CAP) }
        val dhikr = days.sumOf { min(tools.getInt("dhikr_$it", 0), CcGoal.DHIKR_DAILY_CAP) }
        val baseKey = "hifz_base_${weekId(today)}"
        if (!prefs.contains(baseKey)) prefs.edit().putInt(baseKey, confidentHifzAyahs).apply()
        val hifz = (confidentHifzAyahs - prefs.getInt(baseKey, confidentHifzAyahs)).coerceAtLeast(0)
        return CcScores(
            salah = salah.coerceAtMost(CcGoal.SALAH.weeklyCap),
            quran = quran.coerceAtMost(CcGoal.QURAN.weeklyCap),
            dhikr = dhikr.coerceAtMost(CcGoal.DHIKR.weeklyCap),
            hifz = hifz.coerceAtMost(CcGoal.HIFZ.weeklyCap)
        )
    }

    /** Days of this week with any salah, Quran or dhikr logged. */
    fun activeDays(today: LocalDate): Int = activeDayIndices(today).size

    /** Monday-based indices (0..6) of this week's days that have any salah, Quran or dhikr logged. */
    fun activeDayIndices(today: LocalDate): Set<Int> =
        generateSequence(weekStart(today)) { it.plusDays(1) }.takeWhile { !it.isAfter(today) }.filter { d ->
            PRAYERS.any { tools.getString("prayer_${d}_$it", null) in CC_PRAYED_MARKS } ||
                tools.getInt("quran_$d", 0) > 0 || tools.getInt("dhikr_$d", 0) > 0
        }.map { it.dayOfWeek.value - 1 }.toSet()

    /**
     * True when any upload happened in the last [MIN_PUBLISH_GAP_MS] (the rules reject faster
     * writes anyway), or these exact totals were already uploaded recently.
     */
    fun shouldSkipPublish(signature: String, now: Long = System.currentTimeMillis()): Boolean {
        val age = now - prefs.getLong("pub_at", 0L)
        if (prefs.getString("pub_sig", null) == null) return false
        if (age in 0 until MIN_PUBLISH_GAP_MS) return true
        return prefs.getString("pub_sig", null) == signature && age in 0 until SAME_SCORE_REPUBLISH_MS
    }

    fun markPublished(signature: String, now: Long = System.currentTimeMillis()) {
        prefs.edit().putString("pub_sig", signature).putLong("pub_at", now).apply()
    }

    fun clearPublished() { prefs.edit().remove("pub_sig").remove("pub_at").apply() }

    /** A leave that could not reach the server yet; retried the next time the screen opens. */
    var pendingLeave: Pair<String, String>?
        get() {
            val city = prefs.getString("pending_leave_city", null) ?: return null
            val week = prefs.getString("pending_leave_week", null) ?: return null
            return city to week
        }
        set(value) {
            val e = prefs.edit()
            if (value == null) e.remove("pending_leave_city").remove("pending_leave_week")
            else e.putString("pending_leave_city", value.first).putString("pending_leave_week", value.second)
            e.apply()
        }

    /** Keeps the last loaded board so it can be shown offline, with the time it was fetched. */
    fun cacheBoard(cityId: String, board: CcBoard, now: Long = System.currentTimeMillis()) {
        val json = JSONObject()
            .put("city", cityId).put("week", board.weekId).put("total", board.totalMembers)
            .put("uid", board.myUid).put("rank", board.myRank ?: -1).put("at", now)
            .put("members", JSONArray().apply {
                board.members.take(BOARD_CACHE_LIMIT).forEach { m ->
                    put(JSONObject().put("u", m.uid).put("n", m.name).put("a", m.area).put("k", m.areaKey)
                        .put("s", m.scores.salah).put("q", m.scores.quran).put("d", m.scores.dhikr).put("h", m.scores.hifz))
                }
            })
        prefs.edit().putString("board_cache", json.toString()).apply()
    }

    /** The cached board for [cityId] and [weekId] with its fetch time, or null when none matches. */
    fun cachedBoard(cityId: String, weekId: String): Pair<CcBoard, Long>? = runCatching {
        val raw = prefs.getString("board_cache", null) ?: return null
        val json = JSONObject(raw)
        if (json.getString("city") != cityId || json.getString("week") != weekId) return null
        val arr = json.getJSONArray("members")
        val members = (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            CcMember(o.getString("u"), o.getString("n"), o.getString("a"), o.getString("k"),
                CcScores(o.optInt("s"), o.optInt("q"), o.optInt("d"), o.optInt("h")))
        }
        CcBoard(weekId, members, json.optInt("total", members.size), json.getString("uid"),
            json.optInt("rank", -1).takeIf { it > 0 }) to json.getLong("at")
    }.getOrNull()

    fun clearBoardCache() { prefs.edit().remove("board_cache").apply() }

    companion object {
        private val PRAYERS = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
        private const val MIN_PUBLISH_GAP_MS = 30_000L
        private const val SAME_SCORE_REPUBLISH_MS = 10 * 60_000L
        private const val BOARD_CACHE_LIMIT = 300
        private val dhikrLock = Any()

        /**
         * Called by the tasbeeh on every count so dhikr reaches the city board. Synchronized so
         * fast taps never lose a count between read and write; a day's total is held under a
         * sane ceiling far above the board's daily cap.
         */
        fun addDhikr(context: Context, count: Int = 1) {
            if (count <= 0) return
            val tools = context.applicationContext.getSharedPreferences("daily_tools", Context.MODE_PRIVATE)
            synchronized(dhikrLock) {
                val key = "dhikr_${LocalDate.now()}"
                val next = (tools.getInt(key, 0).toLong() + count).coerceIn(0L, 1_000_000L).toInt()
                tools.edit().putInt(key, next).apply()
            }
        }
    }
}

/** City and area for a coordinate, in English so every phone derives the same city id. */
suspend fun ccDetectPlace(context: Context, latitude: Double, longitude: Double): Triple<String, String, String>? =
    withContext(Dispatchers.IO) {
        runCatching {
            @Suppress("DEPRECATION")
            val a = Geocoder(context, Locale.ENGLISH).getFromLocation(latitude, longitude, 1)?.firstOrNull()
                ?: return@runCatching null
            val city = a.locality ?: a.subAdminArea ?: a.adminArea ?: return@runCatching null
            val area = a.subLocality ?: a.thoroughfare?.takeUnless { it.equals(city, true) } ?: ""
            Triple(city, area, a.countryCode ?: "")
        }.getOrNull()
    }

class CcRemote(private val context: Context) {
    private val app: FirebaseApp? by lazy {
        com.example.core.firebase.FirebaseSetup.app(context)
    }
    private val firestore by lazy { FirebaseFirestore.getInstance(requireApp()) }
    private val auth by lazy { FirebaseAuth.getInstance(requireApp()) }

    private fun requireApp() = app ?: throw CcUnavailableException("City challenge is not available on this build")

    private suspend fun uid(): String {
        requireApp()
        auth.currentUser?.let { return it.uid }
        return auth.signInAnonymously().awaitCm().user?.uid ?: throw CcUnavailableException("Sign-in failed")
    }

    private fun members(cityId: String, weekId: String) =
        firestore.collection("cc_cities").document(cityId).collection("cc_weeks").document(weekId).collection("cc_members")

    /** Uploads this week's totals. The rules allow one write per 30 s, so a throttled write is skipped. */
    suspend fun publish(profile: CcProfile, weekId: String, scores: CcScores): Boolean {
        val uid = uid()
        val capped = capped(scores)
        val data = mapOf(
            "name" to ccCleanName(profile.displayName, 24).ifBlank { "?" },
            "area" to ccCleanName(profile.areaName, 40).ifBlank { OTHER_AREA },
            "areaKey" to profile.areaKey,
            "salah" to capped.salah, "quran" to capped.quran, "dhikr" to capped.dhikr, "hifz" to capped.hifz,
            "points" to capped.points,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return runCatching { members(profile.cityId, weekId).document(uid).set(data).awaitCm() }.isSuccess
    }

    /** The top of the city board plus this user's exact rank, even when outside the top. */
    suspend fun board(profile: CcProfile, weekId: String, myPoints: Int): CcBoard {
        val uid = uid()
        val col = members(profile.cityId, weekId)
        val top = col.orderBy("points", Query.Direction.DESCENDING).limit(TOP_LIMIT).get().awaitCm()
            .documents.mapNotNull { it.toMember() }
        val total = col.count().get(AggregateSource.SERVER).awaitCm().count.toInt()
        val mine = top.indexOfFirst { it.uid == uid }
        val rank = if (mine >= 0) mine + 1 else runCatching {
            col.whereGreaterThan("points", myPoints).count().get(AggregateSource.SERVER).awaitCm().count.toInt() + 1
        }.getOrNull()
        return CcBoard(weekId, top, total, uid, rank)
    }

    /** Removes this user's row for [weekId]. Returns false when the server could not be reached. */
    suspend fun leave(cityId: String, weekId: String): Boolean =
        runCatching { members(cityId, weekId).document(uid()).delete().awaitCm() }.isSuccess

    /**
     * Runs a leave outside any screen scope so closing the screen right after tapping Leave
     * cannot cancel it. A failed leave is remembered in [store] and retried later.
     */
    fun leaveInBackground(store: CcLocalStore, cityId: String, weekId: String) {
        store.pendingLeave = cityId to weekId
        backgroundScope.launch {
            if (leave(cityId, weekId) && store.pendingLeave == (cityId to weekId)) store.pendingLeave = null
        }
    }

    private fun capped(s: CcScores) = CcScores(
        s.salah.coerceIn(0, CcGoal.SALAH.weeklyCap), s.quran.coerceIn(0, CcGoal.QURAN.weeklyCap),
        s.dhikr.coerceIn(0, CcGoal.DHIKR.weeklyCap), s.hifz.coerceIn(0, CcGoal.HIFZ.weeklyCap)
    )

    /**
     * Removes this user's board entries for the last year of weeks in [cityId]. Deleting a missing
     * document succeeds, so weeks the user never joined cost nothing but a write.
     */
    suspend fun deleteMine(cityId: String, today: LocalDate) {
        val current = auth.currentUser ?: return
        val batch = firestore.batch()
        repeat(DELETE_WEEKS) { i ->
            batch.delete(members(cityId, weekId(today.minusWeeks(i.toLong()))).document(current.uid))
        }
        batch.commit().awaitCm()
    }

    private fun DocumentSnapshot.toMember(): CcMember? {
        val name = getString("name") ?: return null
        return CcMember(
            uid = id, name = name,
            area = getString("area") ?: OTHER_AREA, areaKey = getString("areaKey") ?: slug(OTHER_AREA),
            scores = CcScores(
                salah = (getLong("salah") ?: 0).toInt(), quran = (getLong("quran") ?: 0).toInt(),
                dhikr = (getLong("dhikr") ?: 0).toInt(), hifz = (getLong("hifz") ?: 0).toInt()
            )
        )
    }

    companion object {
        const val TOP_LIMIT = 300L
        private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private const val DELETE_WEEKS = 53
    }
}
