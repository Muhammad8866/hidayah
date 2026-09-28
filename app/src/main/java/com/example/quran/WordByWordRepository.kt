package com.example.quran

import android.content.Context
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Offline word-by-word Quran bundled in `assets/wbw` (built by `scripts/build.py`).
 *
 * - Arabic: IndoPak script, word segmentation and word audio from Quran.com.
 * - Word meanings: Quran.com word-by-word glosses (English, Urdu, Hindi, Bengali).
 * - Roots and lemmas: Quranic Arabic Corpus morphology.
 * - Ayah translation: Kanz-ul-Iman (Urdu), Imam Ahmad Raza Khan.
 * - Recitation: Mishary Rashid Alafasy (murattal), one file per surah, with per-word timings so
 *   words can be highlighted and replayed from the downloaded or streamed surah audio.
 */
enum class WbwLanguage(val label: String) {
    English("English"), Urdu("اردو"), Hindi("हिन्दी"), Bengali("বাংলা");

    companion object {
        fun forAppLanguage(language: String) = when (language) {
            "ur", "ks", "sd" -> Urdu
            "hi", "mr", "ne", "mai", "brx", "doi", "kok", "sa" -> Hindi
            "bn", "as" -> Bengali
            else -> English
        }
    }
}

data class WbwWord(
    val surah: Int,
    val ayah: Int,
    val position: Int,
    val arabic: String,
    val transliteration: String,
    private val glosses: List<String>,
    val root: String,
    val lemma: String,
    /** Position of this word in the surah recitation, or -1 when the recitation has no timing for it. */
    val startMs: Int = -1,
    val endMs: Int = -1
) {
    val isTimed: Boolean get() = startMs >= 0 && endMs > startMs
    val id: String get() = "$surah:$ayah:$position"
    /** Vocabulary identity: the dictionary form, so a word learned once is known everywhere it recurs. */
    val key: String get() = lemma.ifEmpty { arabic }
    /**
     * Single-word recitation from Quran.com. The app's GitHub mirror (release wbw-audio-v1) holds only
     * the words the surah timings don't cover, so only those try it first.
     */
    val audioUrls: List<String> get() = "%03d_%03d_%03d.mp3".format(surah, ayah, position).let { name ->
        listOfNotNull(
            "https://github.com/Muhammad8866/deen-companion-quran/releases/download/wbw-audio-v1/wbw_$name".takeUnless { isTimed },
            "https://audio.qurancdn.com/wbw/$name"
        )
    }
    fun gloss(language: WbwLanguage): String = glosses[language.ordinal]
}

data class WbwAyah(val surah: Int, val number: Int, val translation: String, val words: List<WbwWord>, val startMs: Int, val endMs: Int) {
    val key: String get() = "$surah:$number"
}

/** Recitation file of one surah: the app's own GitHub mirror first, then the original host. */
data class SurahAudio(val urls: List<String>, val bytes: Long)

data class RootExample(val arabic: String, private val glosses: List<String>, val verseKey: String) {
    fun gloss(language: WbwLanguage): String = glosses[language.ordinal]
}

data class RootInfo(val root: String, val occurrences: Int, val examples: List<RootExample>)

class WbwIndex(
    val audio: List<SurahAudio>,
    val juzStarts: List<Pair<Int, Int>>,
    val roots: Map<String, RootInfo>,
    val lemmaCounts: Map<String, Int>
) {
    val totalOccurrences: Int = lemmaCounts.values.sum()
    val vocabularySize: Int get() = lemmaCounts.size

    /** Surahs whose recitation is needed to play juz [juz] (1-based); partly covered surahs are included whole. */
    fun surahsInJuz(juz: Int): List<Int> {
        val (fromSurah, _) = juzStarts[juz - 1]
        val next = juzStarts.getOrNull(juz)
        val toSurah = when {
            next == null -> 114
            next.second == 1 -> next.first - 1
            else -> next.first
        }
        return (fromSurah..toSurah).toList()
    }
}

object WordByWordRepository {
    private val surahCache = LruCache<Int, List<WbwAyah>>(4)
    private val lock = Mutex()
    @Volatile private var index: WbwIndex? = null

    suspend fun index(context: Context): WbwIndex = index ?: lock.withLock {
        index ?: withContext(Dispatchers.IO) { parseIndex(readAsset(context, "wbw/index.json")) }.also { index = it }
    }

    suspend fun surah(context: Context, number: Int): List<WbwAyah> {
        require(number in 1..114) { "Surah $number is out of range" }
        surahCache.get(number)?.let { return it }
        return withContext(Dispatchers.IO) {
            parseSurah(number, readAsset(context, "wbw/%03d.json".format(number))).also { surahCache.put(number, it) }
        }
    }

    private fun readAsset(context: Context, path: String) =
        context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }

    private fun JSONArray.strings(from: Int, to: Int) = (from until to).map { getString(it) }

    private fun parseSurah(number: Int, json: String): List<WbwAyah> {
        val root = JSONObject(json)
        val texts = root.getJSONArray("t")
        val ayahs = root.getJSONArray("w")
        val timings = root.getJSONArray("a")
        return (0 until ayahs.length()).map { a ->
            val words = ayahs.getJSONArray(a)
            val timing = timings.getJSONArray(a)
            val segments = timing.getJSONArray(2)
            val parsed = (0 until words.length()).map { w ->
                val row = words.getJSONArray(w)
                WbwWord(
                    number, a + 1, w + 1, row.getString(0), row.getString(1), row.strings(2, 6), row.getString(6), row.getString(7),
                    segments.optInt(w * 2, -1), segments.optInt(w * 2 + 1, -1)
                )
            }
            // Word segments can begin a few milliseconds before the ayah's own timestamp.
            val start = (parsed.filter { it.isTimed }.minOfOrNull { it.startMs } ?: timing.getInt(0)).coerceAtMost(timing.getInt(0))
            WbwAyah(number, a + 1, texts.getString(a), parsed, start, timing.getInt(1))
        }
    }

    private fun parseIndex(json: String): WbwIndex {
        val root = JSONObject(json)
        val juz = root.getJSONArray("juz").let { arr -> (0 until arr.length()).map { arr.getJSONArray(it).let { p -> p.getInt(0) to p.getInt(1) } } }
        val rootsJson = root.getJSONObject("roots")
        val roots = rootsJson.keys().asSequence().associateWith { key ->
            val entry = rootsJson.getJSONArray(key)
            val examples = entry.getJSONArray(1)
            RootInfo(key, entry.getInt(0), (0 until examples.length()).map { i ->
                val e = examples.getJSONArray(i)
                RootExample(e.getString(0), e.strings(1, 5), e.getString(5))
            })
        }
        val lemmasJson = root.getJSONObject("lemmas")
        val lemmas = lemmasJson.keys().asSequence().associateWith { lemmasJson.getInt(it) }
        val audioJson = root.getJSONArray("audio")
        val audio = (0 until audioJson.length()).map { audioJson.getJSONArray(it).let { e -> SurahAudio(e.getJSONArray(0).let { u -> (0 until u.length()).map(u::getString) }, e.getLong(1)) } }
        return WbwIndex(audio, juz, roots, lemmas)
    }
}
