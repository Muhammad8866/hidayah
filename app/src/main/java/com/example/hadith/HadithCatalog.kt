package com.example.hadith

import java.util.Locale

data class HadithChapter(val title: String, val page: Int)

/** One untouched publisher PDF belonging to a complete, curated Hadith collection. */
data class HadithBook(
    val id: String,
    val collectionId: String,
    val collectionName: String,
    val romanTitle: String,
    val nativeTitle: String? = null,
    val languageCode: String = "ar",
    val languageName: String = "Arabic",
    val pageCount: Int,
    val publicationYear: Int = 2014,
    val fileId: Int,
    val slug: String,
    val volume: Int,
    val volumeCount: Int,
    val remoteUrl: String? = null,
    val sourceUrl: String? = null,
    val chapters: List<HadithChapter> = emptyList(),
    /** Shown instead of "Volume n of m" when a file is not simply a numbered volume. */
    val volumeTitle: String? = null,
    /** Release of the mirror repository that holds this book's byte-identical copy. */
    val mirrorTag: String = "hadith-pdfs-v1",
    /** False for books that exist only at their source, so removing them there removes them here. */
    val mirrored: Boolean = true
) {
    val downloadUrl: String
        get() = remoteUrl ?: "https://data2.dawateislami.net/Data/Books/Download/$languageCode/pdf/$publicationYear/$fileId-1.pdf"
    val mirrorAssetName: String
        get() = "$languageCode-${collectionId.replace('_', '-')}-v$volume.pdf"
    val mirrorUrl: String
        get() = "https://github.com/Muhammad8866/hidayah/releases/download/$mirrorTag/$mirrorAssetName"
    val downloadUrls: List<String>
        get() = if (mirrored) listOf(mirrorUrl, downloadUrl).distinct() else listOf(downloadUrl)
    val sourcePageUrl: String
        get() = sourceUrl ?: "https://www.dawateislami.net/bookslibrary/$languageCode/$slug"
    fun displayTitle(appLanguage: String): String = nativeTitle?.takeIf { appLanguage == languageCode } ?: romanTitle
}

enum class HadithSort(val label: String) {
    COLLECTION("Collection order"), TITLE_ASC("Title A-Z"), TITLE_DESC("Title Z-A"),
    VOLUME_ASC("Volume 1-last"), VOLUME_DESC("Volume last-1"),
    CHAPTERS_FIRST("Chapter-indexed first"),
    PAGES_DESC("Most pages"), PAGES_ASC("Fewest pages")
}

object HadithCatalog {
    const val ALL_LANGUAGES = "all"
    const val OFFICIAL_LIBRARY_URL = "https://www.dawateislami.net/bookslibrary/"
    const val SOURCE_NAME = "Dawat-e-Islami Books Library · Kutub al-Sittah"

    private val collectionSearchAliases = mapOf(
        "bukhari" to "صحيح البخاري صحیح بخاری",
        "muslim" to "صحيح مسلم صحیح مسلم",
        "abu_dawud" to "سنن أبي داود سنن ابی داؤد",
        "tirmidhi" to "جامع الترمذي جامع ترمذی",
        "nasai" to "سنن النسائي سنن نسائی",
        "ibn_majah" to "سنن ابن ماجه سنن ابن ماجہ",
        "nematul_bari" to "نعمۃ الباری نعمت الباری نعم الباری Nematul Bari Naimat ul Bari Nemat ul Bari Nimatul Bari Niam ul Bari",
        "sharh_muslim" to "شرح صحیح مسلم شرح مسلم",
        "nematul_wadood" to "نعمۃ الودود نعمت الودود Nematul Wadood Naimat ul Wadood Nemat ul Wadood Nimatul Wadood",
        "sharh_tirmidhi" to "شرح جامع ترمذی شرح ترمذی Hashim Khan Attari"
    )

    /** Other spellings of a collection's name, which the library's search also reads. */
    internal fun searchAliases(collectionId: String): String = collectionSearchAliases[collectionId].orEmpty()

    private data class SetDef(
        val id: String, val name: String, val baseSlug: String,
        val firstFileId: Int, val pages: List<Int>
    )

    /**
     * The six books (Kutub al-Sittah) in the original Arabic from Dawat-e-Islami's library. Every
     * other Hadith book of that library is read in the World Library.
     */
    private val arabicSets = listOf(
        SetDef("bukhari", "Sahih al-Bukhari", "sahih-al-bukhari-jild", 922, listOf(1035, 950)),
        SetDef("muslim", "Sahih Muslim", "sahih-muslim-shareef-jild", 973, listOf(693, 730)),
        SetDef("abu_dawud", "Sunan Abi Dawud", "sunan-abi-daud-jild", 896, listOf(406, 620)),
        SetDef("tirmidhi", "Jami at-Tirmidhi", "sunan-al-tarmizi-jild", 914, listOf(83, 122, 242, 354, 383)),
        SetDef("nasai", "Sunan an-Nasa'i", "sunan-al-nasai-jild", 908, listOf(505, 416, 384, 413, 418, 465)),
        SetDef("ibn_majah", "Sunan Ibn Majah", "sunan-ibn-e-maja-jild", 898, listOf(350, 381))
    )

    /** Release that holds the Urdu commentaries, byte-for-byte as scanned; they have no other source. */
    private const val COMMENTARY_RELEASE = "hadith-sharh-ur-v1"

    private data class CommentaryDef(
        val id: String, val name: String, val urduTitle: String,
        /** Pages of each volume, by the volume number printed on its title page. */
        val pages: List<Int>,
        /** False while the work is still being published, so no volume is called the last. */
        val complete: Boolean,
        /** Volumes printed under another title. */
        val retitled: Map<Int, String> = emptyMap()
    )

    /**
     * Urdu commentaries (shuruh) on four of the six books. Nimat al-Bari was renamed Ni'am al-Bari
     * from its eighth volume by its author; Nimat al-Wadood and the Tirmidhi commentary are not
     * complete yet.
     */
    private val urduCommentaries = listOf(
        CommentaryDef(
            "nematul_bari", "Nimat al-Bari · Sharh al-Bukhari", "نعمۃ الباری فی شرح صحیح البخاری",
            listOf(900, 883, 891, 954, 1003, 1015, 1086, 1019, 908, 897, 980, 908, 966, 993, 1083, 1050),
            complete = true, retitled = (8..16).associateWith { "نعم الباری فی شرح صحیح البخاری" }
        ),
        CommentaryDef(
            "sharh_muslim", "Sharh Sahih Muslim", "شرح صحیح مسلم",
            listOf(1340, 1037, 1194, 911, 982, 1245, 1103), complete = true
        ),
        CommentaryDef(
            "nematul_wadood", "Nimat al-Wadood · Sharh Abi Dawud", "نعمۃ الودود فی شرح سنن ابی داؤد",
            listOf(858, 842, 850, 866, 850), complete = false
        ),
        CommentaryDef(
            "sharh_tirmidhi", "Sharh Jami at-Tirmidhi", "شرح جامع ترمذی",
            listOf(1044, 1059, 1058, 1042), complete = false
        )
    )

    /** Ids are "sharh-set-volume", apart from every other library's ids; the files come only from the release. */
    private val commentaryBooks: List<HadithBook> = urduCommentaries.flatMap { set ->
        set.pages.mapIndexed { index, pages ->
            val volume = index + 1
            val label = if (set.complete) "Volume $volume of ${set.pages.size}" else "Volume $volume"
            val url = "https://github.com/Muhammad8866/hidayah/releases/download/$COMMENTARY_RELEASE/" +
                "ur-${set.id.replace('_', '-')}-v$volume.pdf"
            HadithBook(
                id = "sharh-${set.id}-$volume",
                collectionId = set.id,
                collectionName = set.name,
                romanTitle = "${set.name} — Urdu · $label",
                nativeTitle = set.retitled[volume] ?: set.urduTitle,
                languageCode = "ur",
                languageName = "Urdu",
                pageCount = pages,
                fileId = 0,
                slug = "${set.id.replace('_', '-')}-$volume",
                volume = volume,
                volumeCount = set.pages.size,
                remoteUrl = url,
                sourceUrl = url,
                volumeTitle = if (set.complete) null else label,
                mirrorTag = COMMENTARY_RELEASE,
                mirrored = false
            )
        }
    }

    /**
     * Ids stay "%02d-set-volume" so saved books, reading positions and mirrors keep working. Each
     * volume ships its PDF's bookmarks, so the chapter list needs no download.
     */
    val arabicBooks: List<HadithBook> = arabicSets.flatMapIndexed { collectionIndex, set ->
        set.pages.mapIndexed { index, pages ->
            val volume = index + 1
            HadithBook(
                id = "%02d-%s-%d".format(collectionIndex, set.id, volume),
                collectionId = set.id,
                collectionName = set.name,
                romanTitle = "${set.name} — Arabic · Volume $volume of ${set.pages.size}",
                pageCount = pages,
                fileId = set.firstFileId + index,
                slug = "${set.baseSlug}-$volume",
                volume = volume,
                volumeCount = set.pages.size,
                // Looked up with ASCII digits, whatever the device locale formats the id with.
                chapters = HadithOutlines.chapters("%02d-%s-%d".format(Locale.ROOT, collectionIndex, set.id, volume))
            )
        }
    }

    /** The Hadith library: the six books in Arabic, then the Urdu commentaries. */
    val books: List<HadithBook> = arabicBooks + commentaryBooks

    /**
     * Hindi (Ekummat) and English (Emaan Library/OpenMaktaba) editions that earlier versions listed.
     * They were not Dawat-e-Islami prints and were removed; their saved files are deleted once.
     */
    internal val removedEditions: List<Pair<String, String>> =
        listOf("bukhari" to 8, "muslim" to 8, "tirmidhi" to 4, "abu_dawud" to 6, "nasai" to 7, "ibn_majah" to 1)
            .flatMapIndexed { index, (id, volumes) -> (1..volumes).map { "hi" to "h%02d-%s-%d".format(index, id, it) } } +
            listOf("e-bukhari", "e-muslim", "e-tirmidhi", "e-abu-dawud", "e-nasai-1", "e-nasai-2", "e-ibn-majah").map { "en" to it }

    fun visibleBooks(books: List<HadithBook>, query: String, languageCode: String, sort: HadithSort): List<HadithBook> {
        val needle = query.trim()
        val filtered = books.filter { book ->
            (languageCode == ALL_LANGUAGES || book.languageCode == languageCode) &&
                (needle.isBlank() || listOfNotNull(
                    book.romanTitle, book.nativeTitle, book.collectionName, book.languageName
                ).any { it.contains(needle, true) } || collectionSearchAliases[book.collectionId]?.contains(needle, true) == true)
        }
        val title = compareBy<HadithBook> { it.collectionName.lowercase(Locale.ROOT) }.thenBy { it.volume }
        return when (sort) {
            HadithSort.COLLECTION -> filtered
            HadithSort.TITLE_ASC -> filtered.sortedWith(title)
            HadithSort.TITLE_DESC -> filtered.sortedWith(title.reversed())
            HadithSort.VOLUME_ASC -> filtered.sortedWith(compareBy<HadithBook> { it.volume }.thenBy { it.id })
            HadithSort.VOLUME_DESC -> filtered.sortedWith(compareByDescending<HadithBook> { it.volume }.thenBy { it.id })
            HadithSort.CHAPTERS_FIRST -> filtered.sortedWith(compareByDescending<HadithBook> { it.chapters.size }.thenBy { it.id })
            HadithSort.PAGES_DESC -> filtered.sortedByDescending { it.pageCount }
            HadithSort.PAGES_ASC -> filtered.sortedBy { it.pageCount }
        }
    }
}
