package com.example.azan

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.model.PrayerType
import com.example.prayer.PrayerSettingsStore
import org.json.JSONArray
import java.io.File

data class AzanTrack(
    val id: String,
    val title: String,
    val muezzin: String,
    val region: String,
    /** A Fajr azan carries "As-salatu khayrun minan-nawm", so it is only offered for the Fajr alert. */
    val fajr: Boolean,
    val url: String,
    val sizeBytes: Long,
    val seconds: Int
) {
    /** Short stable file name used for the GitHub mirror and the bundled copy. */
    val fileName: String get() = AzanCatalog.fileName(id)

    /** Ships inside the APK, so it plays and downloads with no internet at all. */
    val bundled: Boolean get() = id in AzanCatalog.BUNDLED_IDS

    val mirrorUrl: String get() = AzanCatalog.MIRROR_BASE + fileName

    /** Faster GitHub copy first, the Internet Archive original as the fallback. */
    val sources: List<String> get() = listOf(mirrorUrl, url)
}

/** Sunni azans from around the world, listed in assets/azan_catalog.json (tools/azan/generate_catalog.py). */
object AzanCatalog {
    private const val ASSET = "azan_catalog.json"
    private const val TRUSTED_PREFIX = "https://archive.org/download/"
    const val MIRROR_BASE = "https://github.com/Muhammad8866/deen-companion-quran/releases/download/azan-v1/"
    const val BUNDLED_DIR = "azan_bundled"

    /** Popular azans kept inside the APK (tools/azan/bundle_assets.py copies them into assets). */
    val BUNDLED_IDS: Set<String> = setOf(
        "athan-220-ali-mulla-01",
        "athan-786-ali-mulla-02",
        "makkahfajradhan6913sheikhalimullah-makkah-fajr-adhan-6-9-13-sheikh-ali-mullah",
        "adhan-202303-adhan-alharam-almakaa",
        "adhan-202303-alfajr-alharam-almakiya",
        "adhan-202303-alharam-almudnaa",
        "adzan-202208-madinah",
        "adhan-202303-naji-qazaaz",
        "adhan-202303-eabd-albasit-eabdalsamad",
        "azan-alafasy-azan-hejaz-2014-al-fajer"
    )

    fun fileName(id: String): String =
        java.security.MessageDigest.getInstance("MD5").digest(id.toByteArray())
            .joinToString("") { "%02x".format(it) }.take(12) + ".mp3"

    @Volatile private var tracks: List<AzanTrack>? = null

    fun load(context: Context): List<AzanTrack> = tracks ?: synchronized(this) {
        tracks ?: runCatching {
            parse(context.assets.open(ASSET).bufferedReader().use { it.readText() })
        }.getOrDefault(emptyList()).also { tracks = it }
    }

    private fun parse(text: String): List<AzanTrack> {
        val json = JSONArray(text)
        return (0 until json.length()).map { index ->
            val item = json.getJSONObject(index)
            val url = item.getString("url")
            require(url.startsWith(TRUSTED_PREFIX)) { "Untrusted URL" }
            AzanTrack(
                id = item.getString("id"), title = item.getString("title"), muezzin = item.getString("muezzin"),
                region = item.getString("region"), fajr = item.optBoolean("fajr"), url = url,
                sizeBytes = item.optLong("size", 0), seconds = item.optInt("seconds", 0)
            )
        }
    }
}

/**
 * Which downloaded azan plays with the prayer alert. Read by the alarm receiver without the app
 * open, so it lives in the same preferences as the other prayer settings.
 */
object AzanAlertStore {
    private const val FAJR_KEY = "azan_alert_fajr"
    private const val OTHER_KEY = "azan_alert_other"

    fun selectedId(prefs: SharedPreferences, fajr: Boolean): String? =
        prefs.getString(if (fajr) FAJR_KEY else OTHER_KEY, null)

    fun select(prefs: SharedPreferences, fajr: Boolean, id: String?) {
        prefs.edit().putString(if (fajr) FAJR_KEY else OTHER_KEY, id).apply()
    }

    fun audioDir(context: Context) = File(context.filesDir, "azan_audio")

    fun audioFile(context: Context, id: String) = File(audioDir(context), "$id.mp3")

    /** The azan to play for [prayerName], or null when the alert should keep its normal sound. */
    fun fileFor(context: Context, prayerName: String): File? {
        val fajr = prayerName == PrayerType.FAJR.displayName
        val id = selectedId(PrayerSettingsStore.prefs(context), fajr) ?: return null
        return audioFile(context, id).takeIf(File::isFile)
    }
}
