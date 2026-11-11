package com.example.hadith

import com.example.hadith.IslamicBooksCatalog.Edition

/**
 * The Ladies / Muslimah shelf: women-focused Ahle Sunnat books, each an edition of the
 * dawateislami.net library list with its own library number. Coverage is not exhaustive; every
 * other book of the library is found through the full list. Rows are checked against the list
 * shipped with the app by IslamicBooksCatalogTest.
 */
object MuslimahBooksCatalog {
    private const val SALAH = "muslimah_islami_behno_ki_namaz"
    private const val SALAH_NAME = "Salah for Islamic Sisters"
    private const val ZEVAR = "muslimah_jannati_zevar"
    private const val SAAS_BAHU = "muslimah_saas_bahu_main_sulha_ka_raaz"
    private const val SAAS_BAHU_NAME = "Saas Bahu Main Sulha Ka Raaz"

    private fun salah(diId: Long, code: String, year: Int, pages: Int, slug: String, language: String, native: String, legacy: String) =
        Edition(SALAH, SALAH_NAME, diId, code, 313, year, pages, slug, "$SALAH_NAME — $language", native, legacyId = legacy)

    private fun single(
        work: String, name: String, diId: Long, fileId: Int, year: Int, pages: Int, slug: String,
        native: String, legacy: String, code: String = "ur", language: String = "Urdu"
    ) = Edition(work, name, diId, code, fileId, year, pages, slug, "$name — $language", native, legacyId = legacy)

    internal val editions: List<Edition> = listOf(
        salah(573, "ur", 2008, 92, "islami-behno-ki-namaz", "Urdu", "اسلامی بہنوں کی نماز", "bk-muslimah-islami-behno-ki-namaz-ur"),
        salah(574, "hi", 2011, 320, "islami-behno-ki-namaz", "Hindi", "इस्लामी बहनों की नमाज़", "bk-muslimah-islami-behno-ki-namaz-hi"),
        salah(1020, "si", 2012, 318, "islami-behno-ki-namaz", "Sindhi", "اسلامي ڀينرن جي نماز (حنفي)", "bk-muslimah-islami-behno-ki-namaz-sd"),
        salah(1827, "en", 2014, 346, "salah-for-islamic-sisters", "English", "Salah For Islamic Sisters", "bk-muslimah-islami-behno-ki-namaz-en"),
        salah(2306, "bn", 2015, 295, "islami-behno-ki-namaz", "Bengali", "ইসলামী বোনদের নামায (হানাফী)", "bk-muslimah-islami-behno-ki-namaz-bn"),
        salah(2971, "gu", 2016, 320, "islami-behno-ki-namaz", "Gujarati", "ઇસ્લામી બહનોં કી નમાઝ", "bk-muslimah-islami-behno-ki-namaz-gu"),
        salah(4293, "te", 2020, 318, "islami-behno-ki-namaz", "Telugu", "ఇస్లామి బహ్నోంకీ నమాజ్", "bk-muslimah-islami-behno-ki-namaz-te"),
        salah(7441, "pt", 2024, 324, "sualah-para-irmas-muculmanas", "Portuguese", "SUALAH PARA IRMÃS MUÇULMANAS", "bk-muslimah-islami-behno-ki-namaz-pt"),
        single(ZEVAR, "Jannati Zevar", 361, 164, 2006, 188, "jannati-zevar", "جنتی زیور", "bk-muslimah-jannati-zevar-ur"),
        single(ZEVAR, "Jannati Zevar", 1351, 164, 2013, 678, "jannati-zevar", "जन्नती ज़ेवर", "bk-muslimah-jannati-zevar-hi", "hi", "Hindi"),
        single(ZEVAR, "Jannati Zevar", 3410, 164, 2017, 688, "jannati-zaiwar", "જન્નતી ઝેવર", "bk-muslimah-jannati-zevar-gu", "gu", "Gujarati"),
        single("muslimah_sunni_bahishti_zewar", "Sunni Bahishti Zewar", 3230, 1619, 2016, 610, "sunni-bahishti-zewar",
            "سنی بہشتی زیور", "bk-muslimah-sunni-bahishti-zewar-ur"),
        single("muslimah_khawateen_kay_masail", "Khawateen Kay Masail", 3224, 1613, 2016, 70, "khawateen-kay-masail",
            "خواتین کے مسائل", "bk-muslimah-khawateen-kay-masail-ur"),
        single("muslimah_chahal_hadees_baraye_khawateen_e_islam", "Chahal Hadees Baraye Khawateen-e-Islam", 3221, 1610, 2016, 20,
            "chahal-hadees-baraye-khawateen-e-islam", "چہل حدیث برائے خواتین اسلام", "bk-muslimah-chahal-hadees-baraye-khawateen-e-islam-ur"),
        single("muslimah_chadar_aur_char_diwari", "Chadar Aur Char Diwari", 3220, 1609, 2016, 273, "chadar-aur-char-diwari",
            "چادر اور چار دیواری", "bk-muslimah-chadar-aur-char-diwari-ur"),
        single("muslimah_aurat_aur_parda", "Aurat Aur Parda", 3219, 1608, 2016, 17, "aurat-aur-parda",
            "عورت اور پردہ", "bk-muslimah-aurat-aur-parda-ur"),
        single("muslimah_aurat_aur_aazadi", "Aurat Aur Aazadi", 3218, 1607, 2016, 68, "aurat-aur-aazadi",
            "عورت اور آزادی", "bk-muslimah-aurat-aur-aazadi-ur"),
        single(SAAS_BAHU, SAAS_BAHU_NAME, 548, 295, 2008, 8, "saas-bahu-main-sulha-ka-raaz",
            "ساس بہو میں صلح کا راز", "bk-muslimah-saas-bahu-main-sulha-ka-raaz-ur"),
        single(SAAS_BAHU, SAAS_BAHU_NAME, 549, 295, 2008, 30, "saas-bahu-main-sulha-ka-raaz",
            "سس ۽ ننهن ۾ صلح جو راز", "bk-muslimah-saas-bahu-main-sulha-ka-raaz-sd", "si", "Sindhi"),
        single("muslimah_jannati_khawateen", "Jannati Khawateen", 8066, 3601, 2025, 160, "jannati-khawateen",
            "جنتی خواتین", "bk-muslimah-jannati-khawateen-ur")
    )

    val books: List<HadithBook> by lazy { editions.map(Edition::book) }
}
