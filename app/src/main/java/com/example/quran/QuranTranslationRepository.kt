package com.example.quran

import android.content.Context
import com.example.core.net.FastNet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

/** One ayah with its translation; [globalNumber] (1..6236) addresses its audio. */
data class TranslatedAyah(val surah: Int, val number: Int, val globalNumber: Int, val arabic: String, val translation: String)

/** What the player speaks for each ayah. */
enum class TranslationPlayMode { ARABIC_AND_TRANSLATION, TRANSLATION_ONLY, ARABIC_ONLY }

/** Reader preferences that survive app restarts. */
data class TranslationReaderSettings(
    val arabicSize: Int = 24,
    val translationSize: Int = 16,
    val showArabic: Boolean = true,
    val playMode: TranslationPlayMode = TranslationPlayMode.ARABIC_AND_TRANSLATION,
    val speed: Float = 1f,
    val repeatAyah: Boolean = false,
    val continueNextSurah: Boolean = true
) {
    companion object {
        val ARABIC_SIZES = 18..36
        val TRANSLATION_SIZES = 12..26
        val SPEEDS = listOf(0.75f, 1f, 1.25f, 1.5f)
    }
}

/** Fetches Arabic paired with the chosen translation, verified and kept on disk for offline reading. */
object QuranTranslationRepository {
    private const val PREFS = "quran_translation"
    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun selectedId(context: Context): String = QuranTranslationCatalog.byId(prefs(context).getString("text", null)).id
    fun saveSelectedId(context: Context, id: String) =
        prefs(context).edit().putString("text", QuranTranslationCatalog.byId(id).id).apply()
    fun selectedAudioId(context: Context): String =
        prefs(context).getString("audio", null)
            ?.takeIf { QuranTranslationCatalog.audioById(it) != null } ?: QuranTranslationCatalog.audio.first().id
    fun saveSelectedAudioId(context: Context, id: String) {
        require(QuranTranslationCatalog.audioById(id) != null)
        prefs(context).edit().putString("audio", id).apply()
    }

    /** Last read position as (surah, ayah); defaults to Al-Fatihah 1. */
    fun lastRead(context: Context): Pair<Int, Int> {
        val p = prefs(context)
        val surah = p.getInt("last_surah", 1).coerceIn(1, 114)
        val ayah = p.getInt("last_ayah", 1).coerceIn(1, FullQuranCatalog.surahs[surah - 1].totalAyat)
        return surah to ayah
    }
    fun saveLastRead(context: Context, surah: Int, ayah: Int) {
        if (surah !in 1..114 || ayah !in 1..FullQuranCatalog.surahs[surah - 1].totalAyat) return
        prefs(context).edit().putInt("last_surah", surah).putInt("last_ayah", ayah).apply()
    }

    /** Bookmarks stored as "surah:ayah", ordered by mushaf position. */
    fun bookmarks(context: Context): List<Pair<Int, Int>> =
        prefs(context).getStringSet("bookmarks", emptySet()).orEmpty().mapNotNull(::parseKey)
            .sortedWith(compareBy({ it.first }, { it.second }))
    fun toggleBookmark(context: Context, surah: Int, ayah: Int): Boolean {
        val key = "$surah:$ayah"
        val set = prefs(context).getStringSet("bookmarks", emptySet()).orEmpty().toMutableSet()
        val added = set.add(key) || run { set.remove(key); false }
        prefs(context).edit().putStringSet("bookmarks", set).apply()
        return added
    }
    internal fun parseKey(key: String): Pair<Int, Int>? {
        val (s, a) = key.split(':').takeIf { it.size == 2 } ?: return null
        val surah = s.toIntOrNull()?.takeIf { it in 1..114 } ?: return null
        val ayah = a.toIntOrNull()?.takeIf { it in 1..FullQuranCatalog.surahs[surah - 1].totalAyat } ?: return null
        return surah to ayah
    }

    fun settings(context: Context): TranslationReaderSettings {
        val p = prefs(context)
        val d = TranslationReaderSettings()
        return TranslationReaderSettings(
            arabicSize = p.getInt("arabic_size", d.arabicSize).coerceIn(TranslationReaderSettings.ARABIC_SIZES),
            translationSize = p.getInt("translation_size", d.translationSize).coerceIn(TranslationReaderSettings.TRANSLATION_SIZES),
            showArabic = p.getBoolean("show_arabic", d.showArabic),
            playMode = TranslationPlayMode.entries.firstOrNull { it.name == p.getString("play_mode", null) } ?: d.playMode,
            speed = p.getFloat("speed", d.speed).takeIf { it in TranslationReaderSettings.SPEEDS } ?: d.speed,
            repeatAyah = p.getBoolean("repeat_ayah", d.repeatAyah),
            continueNextSurah = p.getBoolean("continue_next", d.continueNextSurah)
        )
    }
    fun saveSettings(context: Context, s: TranslationReaderSettings) {
        prefs(context).edit()
            .putInt("arabic_size", s.arabicSize.coerceIn(TranslationReaderSettings.ARABIC_SIZES))
            .putInt("translation_size", s.translationSize.coerceIn(TranslationReaderSettings.TRANSLATION_SIZES))
            .putBoolean("show_arabic", s.showArabic)
            .putString("play_mode", s.playMode.name)
            .putFloat("speed", s.speed)
            .putBoolean("repeat_ayah", s.repeatAyah)
            .putBoolean("continue_next", s.continueNextSurah)
            .apply()
    }

    suspend fun loadSurah(surah: Int, expectedAyahCount: Int, translationId: String): List<TranslatedAyah> = withContext(Dispatchers.IO) {
        require(surah in 1..114 && expectedAyahCount > 0)
        val edition = QuranTranslationCatalog.byId(translationId).id
        val stored = FastNet.storedFile("quran_tr_${edition}_$surah.json")
        val saved = stored?.takeIf { it.isFile }?.let { runCatching { it.readText() }.getOrNull() }
        saved?.let { text -> runCatching { parse(text, surah, expectedAyahCount, edition) }.getOrNull() }
            ?.let { return@withContext it }
        stored?.delete()
        val text = FastNet.text("https://api.alquran.cloud/v1/surah/$surah/editions/quran-uthmani,$edition")
        parse(text, surah, expectedAyahCount, edition).also {
            if (stored != null) runCatching {
                val temp = File(stored.path + ".part")
                temp.writeText(text)
                if (!temp.renameTo(stored)) temp.delete()
            }
        }
    }

    /** Warms the offline cache for the next surah; failures are silent. */
    suspend fun prefetch(surah: Int, translationId: String) {
        if (surah !in 1..114) return
        runCatching { loadSurah(surah, FullQuranCatalog.surahs[surah - 1].totalAyat, translationId) }
    }

    internal fun parse(text: String, surah: Int, expectedAyahCount: Int, edition: String): List<TranslatedAyah> {
        val editions = JSONObject(text).getJSONArray("data")
        check(editions.length() == 2) { "Missing Quran edition" }
        check(editions.getJSONObject(1).getJSONObject("edition").getString("identifier") == edition) { "Wrong translation edition" }
        val arabic = editions.getJSONObject(0).getJSONArray("ayahs")
        val meaning = editions.getJSONObject(1).getJSONArray("ayahs")
        check(arabic.length() == expectedAyahCount && meaning.length() == expectedAyahCount) { "Quran editions are not aligned" }
        return List(expectedAyahCount) { index ->
            val ar = arabic.getJSONObject(index)
            val tr = meaning.getJSONObject(index)
            val global = ar.getInt("number")
            check(ar.getInt("numberInSurah") == index + 1 && tr.getInt("number") == global && global in 1..6236) {
                "Invalid Quran ayah sequence"
            }
            val arabicText = ar.getString("text").removePrefix("﻿")
            check(arabicText.isNotBlank()) { "Empty Quran ayah" }
            TranslatedAyah(surah, index + 1, global, arabicText, tr.getString("text"))
        }
    }
}
