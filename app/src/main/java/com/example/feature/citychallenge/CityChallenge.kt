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
import kotlinx.coroutines.Dispatchers
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
        val prayed = setOf("Jamaat", "Prayed")
        val salah = days.sumOf { d -> PRAYERS.count { tools.getString("prayer_${d}_$it", null) in prayed } }
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
    fun activeDays(today: LocalDate): Int =
        generateSequence(weekStart(today)) { it.plusDays(1) }.takeWhile { !it.isAfter(today) }.count { d ->
            PRAYERS.any { tools.getString("prayer_${d}_$it", null) in setOf("Jamaat", "Prayed") } ||
                tools.getInt("quran_$d", 0) > 0 || tools.getInt("dhikr_$d", 0) > 0
        }

    companion object {
        private val PRAYERS = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")

        /** Called by the tasbeeh on every count so dhikr reaches the city board. */
        fun addDhikr(context: Context, count: Int = 1) {
            val tools = context.applicationContext.getSharedPreferences("daily_tools", Context.MODE_PRIVATE)
            val key = "dhikr_${LocalDate.now()}"
            tools.edit().putInt(key, tools.getInt(key, 0) + count).apply()
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
        val data = mapOf(
            "name" to profile.displayName.trim().take(24),
            "area" to profile.areaName.trim().ifBlank { OTHER_AREA }.take(40),
            "areaKey" to profile.areaKey,
            "salah" to scores.salah, "quran" to scores.quran, "dhikr" to scores.dhikr, "hifz" to scores.hifz,
            "points" to scores.points,
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

    suspend fun leave(cityId: String, weekId: String) {
        runCatching { members(cityId, weekId).document(uid()).delete().awaitCm() }
    }

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
        private const val DELETE_WEEKS = 53
    }
}
