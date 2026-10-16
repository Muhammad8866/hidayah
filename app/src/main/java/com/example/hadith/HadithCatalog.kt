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
        get() = when (id) {
            "e-bukhari" -> "en-bukhari-complete.pdf"
            "e-muslim" -> "en-muslim-complete.pdf"
            "e-tirmidhi" -> "en-tirmidhi-complete.pdf"
            "e-abu-dawud" -> "en-abu-dawud-complete.pdf"
            "e-nasai-1" -> "en-nasai-v1-5.pdf"
            "e-nasai-2" -> "en-nasai-v6.pdf"
            "e-ibn-majah" -> "en-ibn-majah-complete.pdf"
            else -> "$languageCode-${collectionId.replace('_', '-')}-v$volume.pdf"
        }
    val mirrorUrl: String
        get() = "https://github.com/Muhammad8866/deen-companion-hadith-pdfs/releases/download/$mirrorTag/$mirrorAssetName"
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
    const val SOURCE_NAME = "Original Arabic + publisher-supplied Hindi PDFs"

    private val collectionSearchAliases = mapOf(
        "bukhari" to "صحيح البخاري صحیح بخاری",
        "muslim" to "صحيح مسلم صحیح مسلم",
        "abu_dawud" to "سنن أبي داود سنن ابی داؤد",
        "tirmidhi" to "جامع الترمذي جامع ترمذی",
        "nasai" to "سنن النسائي سنن نسائی",
        "ibn_majah" to "سنن ابن ماجه سنن ابن ماجہ",
        "muwatta" to "موطأ مالك موطا مالک",
        "musnad_ahmad" to "مسند أحمد مسند احمد",
        "darimi" to "سنن الدارمي سنن دارمی"
    )

    private data class SetDef(
        val id: String, val name: String, val baseSlug: String,
        val firstFileId: Int, val pages: List<Int>
    )

    private val arabicSets = listOf(
        SetDef("bukhari", "Sahih al-Bukhari", "sahih-al-bukhari-jild", 922, listOf(1035, 950)),
        SetDef("muslim", "Sahih Muslim", "sahih-muslim-shareef-jild", 973, listOf(693, 730)),
        SetDef("abu_dawud", "Sunan Abi Dawud", "sunan-abi-daud-jild", 896, listOf(406, 620)),
        SetDef("tirmidhi", "Jami at-Tirmidhi", "sunan-al-tarmizi-jild", 914, listOf(83, 122, 242, 354, 383)),
        SetDef("nasai", "Sunan an-Nasa'i", "sunan-al-nasai-jild", 908, listOf(505, 416, 384, 413, 418, 465)),
        SetDef("ibn_majah", "Sunan Ibn Majah", "sunan-ibn-e-maja-jild", 898, listOf(350, 381)),
        SetDef("muwatta", "Muwatta Imam Malik", "imam-malik-jild", 920, listOf(270, 190)),
        SetDef("musnad_ahmad", "Musnad Imam Ahmad ibn Hanbal", "musnad-al-imam-ahmed-bin-hanbal-jild", 900, listOf(642, 827, 740, 657, 643, 607)),
        SetDef("darimi", "Sunan ad-Darimi", "sunan-al-darmi-jild", 906, listOf(307, 352))
    )

    /** Fixed allow-list: miscellaneous category books and incomplete sets are excluded. */
    private val arabicBooks: List<HadithBook> = arabicSets.flatMapIndexed { collectionIndex, set ->
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
                volumeCount = set.pages.size
            )
        }
    }

    private data class HindiSet(val id: String, val name: String, val driveIds: List<String>)

    private val hindiSets = listOf(
        HindiSet("bukhari", "Sahih al-Bukhari", listOf(
            "1mpn0z-DLLe1ZLdvXcdHZj9aSXTzWq4rd", "13dUFaCavVt6orbwy8DA9gqwzjIpvGiq_",
            "1_mwKYnn_zu7LDl6JYwmfInsYc8uB69bg", "1sd0QqYqnsdtBKgUPsDXs4fjuGlLiMAeC",
            "1XKp7lNXesYW6OdV8n5tO707Ov-eEgHdM", "1-n4rEk4nxaif95wOxmvLun-4sBOc0jUs",
            "1SvUlWGu0wpkbmMxBvUYAVKMxRxB1dYm8", "1errBOG1KVFlHOi32KqaiyEMh-HcgZiPT"
        )),
        HindiSet("muslim", "Sahih Muslim", listOf(
            "1jiHhihSJ2CxGdZEu0LbvnxBdidGjy-ha", "15EMujAmV2pM1dJf_oIcSA50Jdhb1kY9C",
            "1F_ZKtndSPjM5lvnfipMxj8xOTS4l_93D", "1zk21gW4EPQh1v7BvgPHztZtvHFhlsZLd",
            "1quFFndtvkhtocXWYnsQ6pnLvmVINZsVH", "1SAFrPr2ArObc5r_bvdMyfpwi9c9zAPrJ",
            "1jchF3VULXWCSMsIEhYIj-i6S2u8CNco8", "1hnCSrxFU-NGr1bitkDscRJD3Xal_7IPw"
        )),
        HindiSet("tirmidhi", "Jami at-Tirmidhi", listOf(
            "1PrY5MAp44r4TcjpqwfUWujOpZecVWKr8", "1bSjGN-d1JE-pcW9DHipiYA5z0TxL181L",
            "1_8IMFcZyu_S8eWdKTcXvqTTHc3TqHE9A", "1fOmimXIHrKnrdgF0ubJtTGl8P1oQj2Oi"
        )),
        HindiSet("abu_dawud", "Sunan Abi Dawud", listOf(
            "129B1LVhY0UWWVU0DVYbCGoZ2kVqcOhsy", "1ne9fTBvHGloup8lWcrDrNDeMQ_ACkh5Z",
            "1VXC7ruBk-Ve58f5U6TwGuFjDuZev0Y_v", "1uNGl5l7_dysdgfkiFOoeFGbDql6rxG9_",
            "1ysLU8FgtqzOXHLPxXvk8ZSRkWoO3Zj10", "1Lm6yRIFKjQy7OIRH_qqLmdO1x4xDMF-e"
        )),
        HindiSet("nasai", "Sunan an-Nasa'i", listOf(
            "1Psa4gMUSU1R_we6sC5r1NLVQAOmpBa-c", "1SHkf9JFYIgAT6LTvXNNBN6GuoN_nYN9W",
            "1JtiqH_n0-0Tz-LyHDpsM4bRrpc28mgJD", "1OjMnPTJ4xWyXH_gDsVKMWmYgkKlCEWp9",
            "1_ZNBFjrnwRc-aTRuqimwEP0y8Jd7oicU", "https://archive.org/download/sunan-nasai-hindi/sunan-nasai-hindi-vol-6.pdf",
            "13VnK9ypKdeZEY-1t2rCC9b6Ydf97sZTP"
        )),
        HindiSet("ibn_majah", "Sunan Ibn Majah", listOf("1_QeCsGAuU5mJl1kTtx4IPAJKBB0iD328"))
    )

    private val hindiBooks = hindiSets.flatMapIndexed { collectionIndex, set ->
        set.driveIds.mapIndexed { index, driveId ->
            val volume = index + 1
            HadithBook(
                id = "h%02d-%s-%d".format(collectionIndex, set.id, volume),
                collectionId = set.id,
                collectionName = set.name,
                romanTitle = "${set.name} — Hindi · Volume $volume of ${set.driveIds.size}",
                languageCode = "hi",
                languageName = "हिन्दी (Hindi)",
                pageCount = 0,
                publicationYear = 2022,
                fileId = 0,
                slug = "${set.id}-hindi-$volume",
                volume = volume,
                volumeCount = set.driveIds.size,
                remoteUrl = if (driveId.startsWith("https://")) driveId
                    else "https://drive.usercontent.google.com/download?id=$driveId&export=download&confirm=t",
                sourceUrl = "https://www.ekumat.com/pdf_hadith_list"
            )
        }
    }

    private fun chapters(vararg entries: Pair<String, Int>) = entries.mapIndexed { index, entry ->
        HadithChapter("Book ${index + 1}: ${entry.first}", entry.second)
    }

    private val bukhariChapters = chapters(
        "Revelation" to 7, "Belief" to 13, "Knowledge" to 26, "Ablutions (Wudu')" to 48,
        "Bathing (Ghusl)" to 72, "Menstrual Periods" to 81, "Tayammum" to 89, "Prayers" to 94,
        "Sutra of the Musalla" to 124, "Times of Prayer" to 130, "Call to Prayer" to 148,
        "Characteristics of Prayer" to 175, "Friday Prayer" to 206, "Fear Prayer" to 219,
        "The Two Festivals" to 221, "Witr Prayer" to 231, "Prayer for Rain" to 234,
        "Eclipses" to 242, "Prostration during Qur'an Recital" to 249, "Shortening Prayers" to 252,
        "Night Prayer" to 259, "Actions while Praying" to 275, "Funerals" to 286, "Zakat" to 323,
        "Zakat ul-Fitr" to 346, "Hajj" to 349, "Umrah" to 403, "Pilgrims Prevented" to 412,
        "Hunting Penalty during Hajj" to 416, "Virtues of Madinah" to 426, "Fasting" to 432,
        "Taraweeh" to 456, "I'tikaf" to 460, "Sales and Trade" to 465, "As-Salam" to 503,
        "Hiring" to 507, "Transfer of Debt" to 514, "Representation and Authorization" to 519,
        "Agriculture" to 525, "Distribution of Water" to 533, "Loans and Bankruptcy" to 541,
        "Lost Property" to 551, "Oppressions" to 555, "Partnership" to 566, "Mortgaging" to 572,
        "Manumission of Slaves" to 574, "Gifts" to 585, "Witnesses" to 600, "Peacemaking" to 615,
        "Conditions" to 621, "Wills" to 633, "Jihad" to 643, "Khumus" to 713,
        "Beginning of Creation" to 742, "Prophets" to 770, "Virtues of the Prophet and Companions" to 804,
        "Companions of the Prophet" to 852, "Merits of the Ansar" to 884, "Military Expeditions" to 932,
        "Tafsir" to 969, "Virtues of the Qur'an" to 1111, "Marriage" to 1132, "Divorce" to 1177,
        "Supporting the Family" to 1199, "Food" to 1205, "Aqiqa" to 1225, "Hunting and Slaughtering" to 1227,
        "Al-Adha Sacrifice" to 1242, "Drinks" to 1249, "Patients" to 1262, "Medicine" to 1271,
        "Dress" to 1292, "Good Manners" to 1330, "Asking Permission" to 1388, "Invocations" to 1406,
        "Softening the Heart" to 1431, "Divine Will" to 1472, "Oaths and Vows" to 1479,
        "Expiation for Oaths" to 1498, "Inheritance" to 1503, "Hudood" to 1514,
        "Punishment of Warring Disbelievers" to 1521, "Apostates" to 1538, "Compulsion" to 1544,
        "Tricks" to 1547, "Dream Interpretation" to 1555, "Afflictions" to 1572, "Judgments" to 1591,
        "Wishes" to 1612, "Truthful Information" to 1617, "Holding Fast to Qur'an and Sunnah" to 1623,
        "Tawheed" to 1649
    )

    private val englishBooks = listOf(
        HadithBook("e-bukhari", "bukhari", "Sahih al-Bukhari", "Sahih al-Bukhari — English · Complete edition",
            languageCode = "en", languageName = "English", pageCount = 1700, publicationYear = 2009,
            fileId = 0, slug = "sahih-bukhari-english", volume = 1, volumeCount = 1,
            remoteUrl = "https://www.emaanlibrary.com/wp-content/uploads/2019/10/Sahih_Al-Bukhari.pdf",
            sourceUrl = "https://www.emaanlibrary.com/book/kutub-al-sittah-the-six-books-of-hadith/", chapters = bukhariChapters),
        HadithBook("e-muslim", "muslim", "Sahih Muslim", "Sahih Muslim — English · Complete edition",
            languageCode = "en", languageName = "English", pageCount = 1800, publicationYear = 2009,
            fileId = 0, slug = "sahih-muslim-english", volume = 1, volumeCount = 1,
            remoteUrl = "https://www.emaanlibrary.com/wp-content/uploads/2019/10/Sahih_Muslim.pdf",
            sourceUrl = "https://www.emaanlibrary.com/book/kutub-al-sittah-the-six-books-of-hadith/"),
        englishArchive("e-tirmidhi", "tirmidhi", "Jami at-Tirmidhi", "All%20in%20One-Jami%20At-Tirmidhi-Eng.pdf"),
        englishArchive("e-abu-dawud", "abu_dawud", "Sunan Abi Dawud", "All%20in%20One-Sunan%20Abu%20Dawud-Eng.pdf"),
        englishArchive("e-nasai-1", "nasai", "Sunan an-Nasa'i", "All%20in%20One-Sunan%20Nasai-Eng-Vol%201-5.pdf", 1, 2),
        englishArchive("e-nasai-2", "nasai", "Sunan an-Nasa'i", "All%20in%20One-Sunan%20Nasai-Eng-Vol%206.pdf", 2, 2),
        englishArchive("e-ibn-majah", "ibn_majah", "Sunan Ibn Majah", "All%20in%20One-Sunan-Ibn%20Majah-Eng.pdf")
    )

    private fun englishArchive(id: String, collectionId: String, name: String, file: String, volume: Int = 1, volumeCount: Int = 1) =
        HadithBook(id, collectionId, name,
            if (volumeCount == 1) "$name — English · Complete edition" else "$name — English · Volume $volume of $volumeCount",
            languageCode = "en", languageName = "English", pageCount = 0, publicationYear = 2022,
            fileId = 0, slug = id, volume = volume, volumeCount = volumeCount,
            remoteUrl = "https://archive.org/download/islamic-english-books-collection/$file",
            sourceUrl = "https://quranall.net/booksenglish-2.htm")

    val books: List<HadithBook> = arabicBooks + hindiBooks + englishBooks

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
