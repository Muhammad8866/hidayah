package com.example.hadith

/**
 * Classical Ahle Sunnat books offered as untouched publisher or library PDFs. Each book lists
 * every language edition that could be verified at its source; nothing is machine-translated.
 */
object IslamicBooksCatalog {
    const val BAHAR = "bahar_e_shariat"
    const val ANWAR = "anwar_e_shariat"
    const val SHAWAHID = "shawahid_un_nubuwwat"
    const val MAARIJ = "maarij_un_nubuwwat"

    /** Byte-identical copies live on this release, so a book survives its source going offline. */
    const val MIRROR_TAG = "books-v1"

    private const val URDU = "اردو (Urdu)"
    private const val HINDI = "हिन्दी (Hindi)"
    private const val PERSIAN = "فارسی (Persian)"

    private fun archive(
        id: String, collectionId: String, collectionName: String, title: String,
        languageCode: String, languageName: String, item: String, file: String,
        volume: Int = 1, volumeCount: Int = 1, volumeTitle: String? = null, nativeTitle: String? = null
    ) = HadithBook(
        id = id, collectionId = collectionId, collectionName = collectionName, romanTitle = title,
        nativeTitle = nativeTitle,
        languageCode = languageCode, languageName = languageName, pageCount = 0, fileId = 0,
        slug = id, volume = volume, volumeCount = volumeCount, volumeTitle = volumeTitle,
        remoteUrl = "https://archive.org/download/$item/$file",
        sourceUrl = "https://archive.org/details/$item", mirrorTag = MIRROR_TAG
    )

    /** Maktaba-tul-Madinah's edition: three jild, each published as two PDFs. */
    private val baharUrdu = listOf("1-a", "1-b", "2-a", "2-b", "3-a", "3-b").mapIndexed { index, part ->
        val (jild, half) = part.split('-')
        HadithBook(
            id = "bk-bahar-ur-${index + 1}", collectionId = BAHAR, collectionName = "Bahar-e-Shariat",
            romanTitle = "Bahar-e-Shariat — Urdu · Jild $jild, Part ${half.uppercase()}",
            nativeTitle = "بہارِ شریعت",
            languageCode = "ur", languageName = URDU, pageCount = 0, publicationYear = 2014,
            fileId = 1172 + index, slug = "bahar-e-shariat-jild-$part",
            volume = index + 1, volumeCount = 6, volumeTitle = "Jild $jild · Part ${half.uppercase()}",
            mirrorTag = MIRROR_TAG
        )
    }

    private val baharHindi = archive(
        "bk-bahar-hi", BAHAR, "Bahar-e-Shariat", "Bahar-e-Shariat — Hindi · Complete, all 20 parts",
        "hi", HINDI, "baahar-e-shariyat-mukammal-20-hissa-hindi",
        "baahar%20e%20shariyat%20mukammal%2020%20hissa%20%28hindi%29.pdf",
        volumeTitle = "Complete · all 20 parts", nativeTitle = "बहारे शरीअत"
    )

    /** Only these parts have been published in English so far. */
    private val baharEnglish = listOf(1, 2, 3, 4, 5, 6, 16).map { part ->
        archive(
            "bk-bahar-en-$part", BAHAR, "Bahar-e-Shariat", "Bahar-e-Shariat — English · Part $part of 20",
            "en", "English", "bahaar-e-shariat-english", "Bahaar-e-Shariat-Vol-$part.pdf",
            volume = part, volumeCount = 20, volumeTitle = "Part $part of 20"
        )
    }

    private val anwar = listOf(
        archive("bk-anwar-ur", ANWAR, "Anwar-e-Shariat", "Anwar-e-Shariat — Urdu",
            "ur", URDU, "AnwarEShariat", "anwar%20e%20shariat.pdf", nativeTitle = "انوارِ شریعت"),
        archive("bk-anwar-hi", ANWAR, "Anwar-e-Shariat", "Anwar-e-Shariat — Hindi",
            "hi", HINDI, "anware-shariat-hindi",
            "%E0%A4%85%E0%A4%A8%E0%A4%B5%E0%A4%BE%E0%A4%B0%E0%A5%87%20%E0%A4%B6%E0%A4%B0%E0%A5%80%E0%A4%85%E0%A4%A4%20%E0%A4%B9%E0%A4%BF%E0%A4%82%E0%A4%A6%E0%A5%80.pdf", nativeTitle = "अनवारे शरीअत"),
        archive("bk-anwar-kn", ANWAR, "Anwar-e-Shariat", "Anwar-e-Shariat — Kannada",
            "kn", "ಕನ್ನಡ (Kannada)", "ANWARESHARIATKANADA", "ANWARE%20SHARIAT%20KANADA.pdf")
    )

    private val shawahid = listOf(
        archive("bk-shawahid-ur", SHAWAHID, "Shawahid-un-Nubuwwat", "Shawahid-un-Nubuwwat — Urdu · Translation",
            "ur", URDU, "shawahid-al-nabuwwat", "Shawahid%20Al%20Nabuwwat.pdf", volumeTitle = "Urdu translation", nativeTitle = "شواہد النبوۃ"),
        archive("bk-shawahid-fa", SHAWAHID, "Shawahid-un-Nubuwwat", "Shawahid-un-Nubuwwat — Persian · Original text",
            "fa", PERSIAN, "in.ernet.dli.2015.484821", "2015.484821.Shawahid-Un-Nabuwwat.pdf", volumeTitle = "Original Persian text", nativeTitle = "شواهد النبوة")
    )

    private val maarij = (1..3).map { volume ->
        archive(
            "bk-maarij-ur-$volume", MAARIJ, "Maarij-un-Nubuwwat", "Maarij-un-Nubuwwat — Urdu · Volume $volume of 3",
            "ur", URDU, "maarijunnabuwwat-ur", "0049${5 + volume}_Maarij-un-Nabuwwat-$volume-ur.pdf",
            volume = volume, volumeCount = 3, nativeTitle = "معارج النبوۃ"
        )
    } + archive("bk-maarij-fa", MAARIJ, "Maarij-un-Nubuwwat", "Maarij-un-Nubuwwat — Persian · Original text",
        "fa", PERSIAN, "dli.ministry.26293", "18064.11321-.pdf", volumeTitle = "Original Persian text", nativeTitle = "معارج النبوة")

    /** The four classical works, without the Ladies / Muslimah shelf. */
    val classicalBooks: List<HadithBook> = baharUrdu + baharHindi + baharEnglish + anwar + shawahid + maarij

    val books: List<HadithBook> = classicalBooks + MuslimahBooksCatalog.books

    /** Which shelf a work sits on in the Islamic Books library. */
    enum class Shelf { CLASSICAL, MUSLIMAH }

    /**
     * One work (a title in every edition and volume it was verified in). [nativeTitle] and [author]
     * are catalogue metadata as named by the publisher; blank where the source names none.
     */
    data class Work(
        val id: String,
        val name: String,
        val nativeTitle: String,
        val author: String,
        val shelf: Shelf,
        val books: List<HadithBook>
    ) {
        /** Edition languages, Urdu first, then the order the catalogue lists them in. */
        val languages: List<String> = books.map { it.languageCode }.distinct()
            .sortedBy { if (it == "ur") 0 else 1 }

        fun editions(language: String): List<HadithBook> =
            books.filter { it.languageCode == language }.sortedBy { it.volume }
    }

    private val authors = mapOf(
        BAHAR to "Sadr ash-Shariah Mufti Amjad Ali Azmi · d. 1367 AH",
        ANWAR to "Faqih-e-Millat Mufti Jalaluddin Ahmad Amjadi · d. 1422 AH",
        SHAWAHID to "Mawlana Nur ad-Din Abd ar-Rahman Jami · d. 898 AH",
        MAARIJ to "Mulla Muin ad-Din Kashifi al-Harawi · d. 907 AH",
        "muslimah_islami_behno_ki_namaz" to "Ameer-e-Ahle Sunnat Maulana Muhammad Ilyas Attar Qadri",
        "muslimah_jannati_zevar" to "Allama Abdul Mustafa Azmi · Al-Madinatul Ilmiyah edition",
        "muslimah_sunni_bahishti_zewar" to "Khalil-e-Millat Mufti Muhammad Khalil Khan Qadri Barkati",
        "muslimah_chadar_aur_char_diwari" to "Khalil-e-Millat Mufti Muhammad Khalil Khan Qadri Barkati",
        "muslimah_aurat_aur_parda" to "Professor Dr. Muhammad Masood Ahmad",
        "muslimah_saas_bahu_main_sulha_ka_raaz" to "Al-Madinatul Ilmiyah · Dawat-e-Islami",
        "muslimah_jannati_khawateen" to "Maulana Abu Usaid Ubaid Raza Attari Madani"
    )

    /** Every work in catalogue order: the classical shelf first, then Ladies / Muslimah. */
    val works: List<Work> = books.groupBy { it.collectionId }.map { (id, editions) ->
        val urdu = editions.firstOrNull { it.languageCode == "ur" }
        Work(
            id = id,
            name = editions.first().collectionName,
            nativeTitle = (urdu ?: editions.first()).nativeTitle ?: editions.first().collectionName,
            author = authors[id].orEmpty(),
            shelf = if (MuslimahBooksCatalog.books.any { it.collectionId == id }) Shelf.MUSLIMAH else Shelf.CLASSICAL,
            books = editions
        )
    }

    fun work(id: String?): Work? = works.firstOrNull { it.id == id }

    fun book(id: String?): HadithBook? = books.firstOrNull { it.id == id }

    /** App languages whose readers usually read another script's edition. */
    private val readerScript = mapOf("hinglish" to "hi", "ks" to "ur", "sd" to "sd", "pa" to "ur", "ps" to "ur")

    /**
     * The edition language to show first for [work]: the reader's saved choice, then the app's
     * language, then Urdu (the original language of most of these works), then the first edition.
     */
    fun preferredLanguage(work: Work, saved: String?, appLanguage: String): String {
        val available = work.languages
        return listOfNotNull(saved, appLanguage, readerScript[appLanguage], "ur")
            .firstOrNull { it in available } ?: available.first()
    }
}
