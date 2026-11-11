package com.example.quran

import android.content.Context
import android.util.Log
import org.json.JSONArray

data class TafsirAudioTrack(
    val surah: Int,
    val title: String,
    val audioUrl: String,
    val sourceUrl: String,
    val collection: String = "Sirat ul Jinan"
) {
    val id: String get() = if (audioUrl.isNotEmpty()) audioUrl.substringAfterLast('/').substringBefore('.') else "youtube_83"
    val isStreamable: Boolean get() = audioUrl.isNotEmpty()
}

/** Official Dawat-e-Islami recordings; no audio is bundled in the APK. */
object TafsirAudioCatalog {
    private const val TAG = "TafsirAudioCatalog"
    @Volatile private var cached: List<TafsirAudioTrack>? = null

    /** Already-loaded tracks, or null if [tracks] has not completed yet. Never touches assets. */
    fun cachedTracks(): List<TafsirAudioTrack>? = cached

    fun tracks(context: Context): List<TafsirAudioTrack> = cached ?: synchronized(this) {
        cached ?: run {
            val json = try {
                context.assets.open("quran_tafsir_sirat_ul_jinan.json").bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                Log.w(TAG, "Tafsir audio catalog asset unreadable", e)
                return@run emptyList()
            }
            parse(json).also { result -> if (result.isNotEmpty()) cached = result }
        }
    }

    /** Parses and validates the catalog; any malformed/incomplete data yields an empty list (logged). */
    internal fun parse(json: String): List<TafsirAudioTrack> = try {
        val data = JSONArray(json)
        val urdu = List(data.length()) { index ->
            val item = data.getJSONObject(index)
            TafsirAudioTrack(
                item.getInt("surah"), item.getString("title"),
                item.getString("url"), item.getString("page")
            )
        }
        require(urdu.map { it.surah }.toSet() == (1..114).toSet()) { "Incomplete Sirat ul Jinan audio catalog" }
        require(urdu.map { it.id }.distinct().size == urdu.size) { "Duplicate tafseer audio ID" }
        urdu
    } catch (e: Exception) {
        Log.w(TAG, "Tafsir audio catalog malformed", e)
        emptyList()
    }

    fun forSurah(context: Context, surah: Int): List<TafsirAudioTrack> =
        tracks(context).filter { it.surah == surah }

}
