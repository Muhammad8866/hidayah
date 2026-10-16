package com.example.quran

import android.content.Context
import com.example.core.net.FastNet
import com.example.domain.model.Ayah
import com.example.domain.model.MushafLine
import com.example.util.toHinglish
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

/** Reads the complete Quran on demand; the Arabic source and translation stay paired by ayah. */
object RemoteQuranRepository {
    private const val TAJ_ASSET = "taj_13_line_pages.json"
    internal const val TAJ_13_LINE_SHA256 =
        "83657726a0ed66b17948d2a84484bb82ea8d3d8b4b98691493428ec3819f7d41"

    @Volatile private var tajPages: org.json.JSONArray? = null
    @Volatile private var tajSurahStartPages: Map<Int, Int>? = null
    private val surahLocks = Array(114) { Mutex() }
    private val surahCache = object : LinkedHashMap<Int, List<Ayah>>(8, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, List<Ayah>>): Boolean = size > 8
    }

    /**
     * Loads and verifies the complete bundled Mushaf exactly once per process. A modified or
     * partially packaged Quran asset is rejected before any of its text can be displayed.
     */
    private fun verifiedTajPages(context: Context): org.json.JSONArray {
        tajPages?.let { return it }
        return synchronized(this) {
            tajPages ?: context.assets.open(TAJ_ASSET).use { stream ->
                val bytes = stream.readBytes()
                verifyTajAsset(bytes)
                JSONObject(bytes.toString(Charsets.UTF_8)).getJSONArray("pages").also { pages ->
                    check(pages.length() == 847) { "Invalid Taj Mushaf page count" }
                    tajPages = pages
                }
            }
        }
    }

    internal fun verifyTajAsset(bytes: ByteArray) {
        val actual = MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }
        check(actual == TAJ_13_LINE_SHA256) {
            "Bundled Quran asset checksum mismatch"
        }
    }

    /** Exact offline Taj Company 13-line layout, generated from the canonical QUL export. */
    suspend fun loadMushafPage(context: Context, page: Int): List<MushafLine> = withContext(Dispatchers.IO) {
        require(page in 1..847)
        val lines = verifiedTajPages(context).getJSONArray(page - 1)
        check(lines.length() in 1..13) { "Invalid Taj Mushaf page structure" }
        List(lines.length()) { index ->
            val line = lines.getJSONObject(index)
            val type = line.getString("t")
            check(type == "ayah" || type == "basmallah" || type == "surah_name") {
                "Invalid Taj Mushaf line type"
            }
            val text = line.optString("x")
            check(type != "ayah" || text.isNotBlank()) { "Empty Quran text line" }
            MushafLine(
                type = type,
                isCentered = line.getInt("c") == 1,
                surahNumber = if (line.has("s")) line.getInt("s") else null,
                text = text
            )
        }
    }

    suspend fun loadSurahStartPages(context: Context): Map<Int, Int> = withContext(Dispatchers.IO) {
        tajSurahStartPages?.let { return@withContext it }
        synchronized(this@RemoteQuranRepository) {
            tajSurahStartPages ?: buildMap {
                val pages = verifiedTajPages(context)
                for (pageIndex in 0 until pages.length()) {
                    val lines = pages.getJSONArray(pageIndex)
                    for (lineIndex in 0 until lines.length()) {
                        val line = lines.getJSONObject(lineIndex)
                        if (line.getString("t") == "surah_name" && line.has("s")) {
                            putIfAbsent(line.getInt("s"), pageIndex + 1)
                        }
                    }
                }
                check(size == 114) { "Incomplete Taj Mushaf surah index" }
            }.also { tajSurahStartPages = it }
        }
    }

    /** Loads one of the canonical 604 Mushaf pages using Quran Foundation's Indo-Pak word text. */
    suspend fun loadPage(page: Int): List<Ayah> = withContext(Dispatchers.IO) {
        runCatching {
            require(page in 1..604)
            val url = "https://api.quran.com/api/v4/verses/by_page/$page?words=true&word_fields=text_indopak&per_page=50"
            val root = JSONObject(FastNet.text(url))
            val verses = root.getJSONArray("verses")
            check(verses.length() > 0) { "Empty Mushaf page" }
            List(verses.length()) { index ->
                val verse = verses.getJSONObject(index)
                val key = verse.getString("verse_key").split(':')
                check(key.size == 2) { "Invalid Quran verse reference" }
                val words = verse.getJSONArray("words")
                val arabic = buildString {
                    for (wordIndex in 0 until words.length()) {
                        val text = words.getJSONObject(wordIndex).optString("text_indopak").trim()
                        if (text.isNotEmpty()) {
                            if (isNotEmpty()) append(' ')
                            append(text)
                        }
                    }
                }
                check(arabic.isNotBlank()) { "Missing Indo-Pak Quran text for ${verse.getString("verse_key")}" }
                Ayah(
                    surahNumber = key[0].toInt(),
                    ayahNumber = key[1].toInt(),
                    arabicText = arabic,
                    transliteration = "",
                    englishTranslation = "",
                    urduTranslation = "",
                    tafsirSummary = "",
                    tafsirSource = "Quran Foundation • Indo-Pak script"
                )
            }
        }.getOrDefault(emptyList())
    }

    suspend fun loadSurah(number: Int, expectedAyahCount: Int): List<Ayah> = withContext(Dispatchers.IO) {
        require(number in 1..114 && expectedAyahCount > 0)
        synchronized(surahCache) { surahCache[number]?.takeIf { it.size == expectedAyahCount } }
            ?.let { return@withContext it }
        surahLocks[number - 1].withLock {
            synchronized(surahCache) { surahCache[number]?.takeIf { it.size == expectedAyahCount } }
                ?.let { return@withLock it }
            // A verified surah is kept on disk across launches and in memory for fast revisits.
            val stored = FastNet.storedFile("quran_surah_$number.json")
            val saved = stored?.takeIf { it.isFile }?.let { runCatching { it.readText() }.getOrNull() }
            runCatching {
                val text = saved ?: FastNet.text("https://api.alquran.cloud/v1/surah/$number/editions/quran-uthmani,hi.hindi,ur.kanzuliman")
                val root = JSONObject(text)
                val editions = root.getJSONArray("data")
                val arabic = editions.getJSONObject(0).getJSONArray("ayahs")
                val hindi = editions.getJSONObject(1).getJSONArray("ayahs")
                val kanzulIman = editions.getJSONObject(2).getJSONArray("ayahs")
                check(arabic.length() == expectedAyahCount) { "Incomplete Arabic Quran response" }
                check(hindi.length() == expectedAyahCount && kanzulIman.length() == expectedAyahCount) {
                    "Quran editions are not aligned"
                }
                List(arabic.length()) { index ->
                    val ar = arabic.getJSONObject(index)
                    check(ar.getInt("numberInSurah") == index + 1 && ar.getString("text").isNotBlank()) {
                        "Invalid Quran ayah sequence"
                    }
                    Ayah(number, ar.getInt("numberInSurah"), ar.getString("text"), "", hindi.getJSONObject(index).getString("text").toHinglish(), kanzulIman.getJSONObject(index).getString("text"), tafsirSummary = "Kanzul Iman Urdu translation by Imam Ahmed Raza Khan", tafsirSource = "Arabic: Uthmani • Urdu: Kanzul Iman", juzNumber = ar.optInt("juz", 1))
                }.also { ayahs ->
                    synchronized(surahCache) { surahCache[number] = ayahs }
                    if (saved == null && stored != null) runCatching {
                        val temp = File(stored.path + ".part")
                        temp.writeText(text)
                        if (!temp.renameTo(stored)) temp.delete()
                    }
                }
            }.onFailure {
                if (it is CancellationException) throw it
                if (saved != null) stored?.delete()
            }.getOrDefault(emptyList())
        }
    }
}
