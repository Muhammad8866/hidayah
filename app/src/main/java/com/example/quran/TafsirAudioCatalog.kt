package com.example.quran

import android.content.Context
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
    private var cached: List<TafsirAudioTrack>? = null

    fun tracks(context: Context): List<TafsirAudioTrack> = cached ?: synchronized(this) {
        cached ?: run {
            val data = context.assets.open("quran_tafsir_sirat_ul_jinan.json")
                .bufferedReader().use { JSONArray(it.readText()) }
            val urdu = List(data.length()) { index ->
                val item = data.getJSONObject(index)
                TafsirAudioTrack(
                    item.getInt("surah"), item.getString("title"),
                    item.getString("url"), item.getString("page")
                )
            }
            require(urdu.map { it.surah }.toSet() == (1..114).toSet()) { "Incomplete Sirat ul Jinan audio catalog" }
            urdu.also { result ->
                require(result.map { it.id }.distinct().size == result.size) { "Duplicate tafseer audio ID" }
                cached = result
            }
        }
    }

    fun forSurah(context: Context, surah: Int): List<TafsirAudioTrack> =
        tracks(context).filter { it.surah == surah }

}
