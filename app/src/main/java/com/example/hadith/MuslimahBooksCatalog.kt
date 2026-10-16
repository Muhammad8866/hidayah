package com.example.hadith

/** Women-focused Ahle Sunnat editions verified at Dawat-e-Islami. Original PDFs only.
 * Source manifest: docs/muslimah-book-sources.json. Coverage is not exhaustive.
 * A print the publisher no longer links is served from the books-v1 release instead.
 * Regenerate with tools/generate_muslimah_catalog.py after reviewing source changes. */
object MuslimahBooksCatalog {
    val books: List<HadithBook> = listOf(
        HadithBook(
            id = "bk-muslimah-islami-behno-ki-namaz-ur",
            collectionId = "muslimah_islami_behno_ki_namaz", collectionName = "Salah for Islamic Sisters",
            romanTitle = "Salah for Islamic Sisters — Urdu",
            nativeTitle = "اسلامی بہنوں کی نماز",
            languageCode = "ur", languageName = "Urdu",
            pageCount = 308, fileId = 0,
            slug = "islami-behno-ki-namaz", volume = 1, volumeCount = 1,
            remoteUrl = "https://data2.dawateislami.net/Data/Books/Download/ur/pdf/2008/313-1.pdf",
            sourceUrl = "https://www.dawateislami.net/bookslibrary/ur/islami-behno-ki-namaz", mirrored = false
        ),
        HadithBook(
            id = "bk-muslimah-islami-behno-ki-namaz-hi",
            collectionId = "muslimah_islami_behno_ki_namaz", collectionName = "Salah for Islamic Sisters",
            romanTitle = "Salah for Islamic Sisters — Hindi",
            nativeTitle = "इस्लामी बहनों की नमाज़",
            languageCode = "hi", languageName = "Hindi",
            pageCount = 320, fileId = 0,
            slug = "islami-behno-ki-namaz", volume = 1, volumeCount = 1,
            remoteUrl = "https://data2.dawateislami.net/Data/Books/Download/hi/pdf/2011/313-1.pdf",
            sourceUrl = "https://www.dawateislami.net/bookslibrary/hi/islami-behno-ki-namaz", mirrored = false
        ),
        HadithBook(
            id = "bk-muslimah-islami-behno-ki-namaz-hi-2011",
            collectionId = "muslimah_islami_behno_ki_namaz", collectionName = "Salah for Islamic Sisters",
            romanTitle = "Salah for Islamic Sisters — Hindi · 2011 print",
            nativeTitle = "इस्लामी बहनों की नमाज़",
            languageCode = "hi", languageName = "Hindi",
            pageCount = 316, fileId = 0,
            slug = "islami-behno-ki-namaz", volume = 1, volumeCount = 1,
            remoteUrl = "https://github.com/Muhammad8866/deen-companion-hadith-pdfs/releases/download/books-v1/hi-muslimah-islami-behno-ki-namaz-2011.pdf",
            sourceUrl = "https://www.dawateislami.net/bookslibrary/hi/islami-behno-ki-namaz", mirrored = false, volumeTitle = "2011 print"
        ),
        HadithBook(
            id = "bk-muslimah-islami-behno-ki-namaz-sd",
            collectionId = "muslimah_islami_behno_ki_namaz", collectionName = "Salah for Islamic Sisters",
            romanTitle = "Salah for Islamic Sisters — Sindhi",
            nativeTitle = "اسلامي ڀينرن جي نماز (حنفي)",
            languageCode = "sd", languageName = "Sindhi",
            pageCount = 0, fileId = 0,
            slug = "islami-behno-ki-namaz", volume = 1, volumeCount = 1,
            remoteUrl = "https://data2.dawateislami.net/Data/Books/Download/si/pdf/2012/313-1.pdf",
            sourceUrl = "https://www.dawateislami.net/bookslibrary/si/islami-behno-ki-namaz", mirrored = false
        ),
        HadithBook(
            id = "bk-muslimah-islami-behno-ki-namaz-en",
            collectionId = "muslimah_islami_behno_ki_namaz", collectionName = "Salah for Islamic Sisters",
            romanTitle = "Salah for Islamic Sisters — English",
            nativeTitle = "Salah For Islamic Sisters",
            languageCode = "en", languageName = "English",
            pageCount = 346, fileId = 0,
            slug = "salah-for-islamic-sisters", volume = 1, volumeCount = 1,
            remoteUrl = "https://data2.dawateislami.net/Data/Books/Download/en/pdf/2014/313-1.pdf",
            sourceUrl = "https://www.dawateislami.net/bookslibrary/en/salah-for-islamic-sisters", mirrored = false
        ),
        HadithBook(
            id = "bk-muslimah-islami-behno-ki-namaz-bn",
            collectionId = "muslimah_islami_behno_ki_namaz", collectionName = "Salah for Islamic Sisters",
            romanTitle = "Salah for Islamic Sisters — Bengali",
            nativeTitle = "ইসলামী বোনদের নামায (হানাফী)",
            languageCode = "bn", languageName = "Bengali",
            pageCount = 0, fileId = 0,
            slug = "islami-behno-ki-namaz", volume = 1, volumeCount = 1,
            remoteUrl = "https://data2.dawateislami.net/Data/Books/Download/bn/pdf/2015/313-1.pdf",
            sourceUrl = "https://www.dawateislami.net/bookslibrary/bn/islami-behno-ki-namaz", mirrored = false
        ),
        HadithBook(
            id = "bk-muslimah-islami-behno-ki-namaz-gu",
            collectionId = "muslimah_islami_behno_ki_namaz", collectionName = "Salah for Islamic Sisters",
            romanTitle = "Salah for Islamic Sisters — Gujarati",
            nativeTitle = "ઇસ્લામી બહનોં કી નમાઝ",
            languageCode = "gu", languageName = "Gujarati",
            pageCount = 0, fileId = 0,
            slug = "islami-behno-ki-namaz", volume = 1, volumeCount = 1,
            remoteUrl = "https://data2.dawateislami.net/Data/Books/Download/gu/pdf/2016/313-1.pdf",
            sourceUrl = "https://www.dawateislami.net/bookslibrary/gu/islami-behno-ki-namaz", mirrored = false
        ),
        HadithBook(
            id = "bk-muslimah-islami-behno-ki-namaz-te",
            collectionId = "muslimah_islami_behno_ki_namaz", collectionName = "Salah for Islamic Sisters",
            romanTitle = "Salah for Islamic Sisters — Telugu",
            nativeTitle = "ఇస్లామి బహ్నోంకీ నమాజ్",
            languageCode = "te", languageName = "Telugu",
            pageCount = 318, fileId = 0,
            slug = "islami-behno-ki-namaz", volume = 1, volumeCount = 1,
            remoteUrl = "https://data2.dawateislami.net/Data/Books/Download/te/pdf/2020/313-1.pdf",
            sourceUrl = "https://www.dawateislami.net/bookslibrary/te/islami-behno-ki-namaz", mirrored = false
        ),
        HadithBook(
            id = "bk-muslimah-islami-behno-ki-namaz-pt",
            collectionId = "muslimah_islami_behno_ki_namaz", collectionName = "Salah for Islamic Sisters",
            romanTitle = "Salah for Islamic Sisters — Portuguese",
            nativeTitle = "SUALAH PARA IRMÃS MUÇULMANAS",
            languageCode = "pt", languageName = "Portuguese",
            pageCount = 324, fileId = 0,
            slug = "sualah-para-irmas-muculmanas", volume = 1, volumeCount = 1,
            remoteUrl = "https://data2.dawateislami.net/Data/Books/Download/pt/pdf/2024/313-1.pdf",
            sourceUrl = "https://www.dawateislami.net/bookslibrary/pt/sualah-para-irmas-muculmanas", mirrored = false
        ),
        HadithBook(
            id = "bk-muslimah-jannati-zevar-ur",
            collectionId = "muslimah_jannati_zevar", collectionName = "Jannati Zevar",
            romanTitle = "Jannati Zevar — Urdu",
            nativeTitle = "جنتی زیور",
            languageCode = "ur", languageName = "Urdu",
            pageCount = 678, fileId = 0,
            slug = "jannati-zevar", volume = 1, volumeCount = 1,
            remoteUrl = "https://data2.dawateislami.net/Data/Books/Download/ur/pdf/2006/164-1.pdf",
            sourceUrl = "https://www.dawateislami.net/bookslibrary/ur/jannati-zevar", mirrored = false
        ),
        HadithBook(
            id = "bk-muslimah-jannati-zevar-hi",
            collectionId = "muslimah_jannati_zevar", collectionName = "Jannati Zevar",
            romanTitle = "Jannati Zevar — Hindi",
            nativeTitle = "जन्नती ज़ेवर",
            languageCode = "hi", languageName = "Hindi",
            pageCount = 0, fileId = 0,
            slug = "jannati-zevar", volume = 1, volumeCount = 1,
            remoteUrl = "https://data2.dawateislami.net/Data/Books/Download/hi/pdf/2013/164-1.pdf",
            sourceUrl = "https://www.dawateislami.net/bookslibrary/hi/jannati-zevar", mirrored = false
        ),
        HadithBook(
            id = "bk-muslimah-jannati-zevar-gu",
            collectionId = "muslimah_jannati_zevar", collectionName = "Jannati Zevar",
            romanTitle = "Jannati Zevar — Gujarati",
            nativeTitle = "જન્નતી ઝેવર",
            languageCode = "gu", languageName = "Gujarati",
            pageCount = 0, fileId = 0,
            slug = "jannati-zaiwar", volume = 1, volumeCount = 1,
            remoteUrl = "https://data2.dawateislami.net/Data/Books/Download/gu/pdf/2017/164-1.pdf",
            sourceUrl = "https://www.dawateislami.net/bookslibrary/gu/jannati-zaiwar", mirrored = false
        ),
        HadithBook(
            id = "bk-muslimah-sunni-bahishti-zewar-ur",
            collectionId = "muslimah_sunni_bahishti_zewar", collectionName = "Sunni Bahishti Zewar",
            romanTitle = "Sunni Bahishti Zewar — Urdu",
            nativeTitle = "سنی بہشتی زیور",
            languageCode = "ur", languageName = "Urdu",
            pageCount = 0, fileId = 0,
            slug = "sunni-bahishti-zewar", volume = 1, volumeCount = 1,
            remoteUrl = "https://data2.dawateislami.net/Data/Books/Download/ur/pdf/2016/1619-1.pdf",
            sourceUrl = "https://www.dawateislami.net/bookslibrary/ur/sunni-bahishti-zewar", mirrored = false
        ),
        HadithBook(
            id = "bk-muslimah-khawateen-kay-masail-ur",
            collectionId = "muslimah_khawateen_kay_masail", collectionName = "Khawateen Kay Masail",
            romanTitle = "Khawateen Kay Masail — Urdu",
            nativeTitle = "خواتین کے مسائل",
            languageCode = "ur", languageName = "Urdu",
            pageCount = 0, fileId = 0,
            slug = "khawateen-kay-masail", volume = 1, volumeCount = 1,
            remoteUrl = "https://data2.dawateislami.net/Data/Books/Download/ur/pdf/2016/1613-1.pdf",
            sourceUrl = "https://www.dawateislami.net/bookslibrary/ur/khawateen-kay-masail", mirrored = false
        ),
        HadithBook(
            id = "bk-muslimah-chahal-hadees-baraye-khawateen-e-islam-ur",
            collectionId = "muslimah_chahal_hadees_baraye_khawateen_e_islam", collectionName = "Chahal Hadees Baraye Khawateen-e-Islam",
            romanTitle = "Chahal Hadees Baraye Khawateen-e-Islam — Urdu",
            nativeTitle = "چہل حدیث برائے خواتین اسلام",
            languageCode = "ur", languageName = "Urdu",
            pageCount = 0, fileId = 0,
            slug = "chahal-hadees-baraye-khawateen-e-islam", volume = 1, volumeCount = 1,
            remoteUrl = "https://data2.dawateislami.net/Data/Books/Download/ur/pdf/2016/1610-1.pdf",
            sourceUrl = "https://www.dawateislami.net/bookslibrary/ur/chahal-hadees-baraye-khawateen-e-islam", mirrored = false
        ),
        HadithBook(
            id = "bk-muslimah-chadar-aur-char-diwari-ur",
            collectionId = "muslimah_chadar_aur_char_diwari", collectionName = "Chadar Aur Char Diwari",
            romanTitle = "Chadar Aur Char Diwari — Urdu",
            nativeTitle = "چادر اور چار دیواری",
            languageCode = "ur", languageName = "Urdu",
            pageCount = 0, fileId = 0,
            slug = "chadar-aur-char-diwari", volume = 1, volumeCount = 1,
            remoteUrl = "https://data2.dawateislami.net/Data/Books/Download/ur/pdf/2016/1609-1.pdf",
            sourceUrl = "https://www.dawateislami.net/bookslibrary/ur/chadar-aur-char-diwari", mirrored = false
        ),
        HadithBook(
            id = "bk-muslimah-aurat-aur-parda-ur",
            collectionId = "muslimah_aurat_aur_parda", collectionName = "Aurat Aur Parda",
            romanTitle = "Aurat Aur Parda — Urdu",
            nativeTitle = "عورت اور پردہ",
            languageCode = "ur", languageName = "Urdu",
            pageCount = 0, fileId = 0,
            slug = "aurat-aur-parda", volume = 1, volumeCount = 1,
            remoteUrl = "https://data2.dawateislami.net/Data/Books/Download/ur/pdf/2016/1608-1.pdf",
            sourceUrl = "https://www.dawateislami.net/bookslibrary/ur/aurat-aur-parda", mirrored = false
        ),
        HadithBook(
            id = "bk-muslimah-aurat-aur-aazadi-ur",
            collectionId = "muslimah_aurat_aur_aazadi", collectionName = "Aurat Aur Aazadi",
            romanTitle = "Aurat Aur Aazadi — Urdu",
            nativeTitle = "عورت اور آزادی",
            languageCode = "ur", languageName = "Urdu",
            pageCount = 0, fileId = 0,
            slug = "aurat-aur-aazadi", volume = 1, volumeCount = 1,
            remoteUrl = "https://data2.dawateislami.net/Data/Books/Download/ur/pdf/2016/1607-1.pdf",
            sourceUrl = "https://www.dawateislami.net/bookslibrary/ur/aurat-aur-aazadi", mirrored = false
        ),
        HadithBook(
            id = "bk-muslimah-saas-bahu-main-sulha-ka-raaz-ur",
            collectionId = "muslimah_saas_bahu_main_sulha_ka_raaz", collectionName = "Saas Bahu Main Sulha Ka Raaz",
            romanTitle = "Saas Bahu Main Sulha Ka Raaz — Urdu",
            nativeTitle = "ساس بہو میں صلح کا راز",
            languageCode = "ur", languageName = "Urdu",
            pageCount = 32, fileId = 0,
            slug = "saas-bahu-main-sulha-ka-raaz", volume = 1, volumeCount = 1,
            remoteUrl = "https://data2.dawateislami.net/Data/Books/Download/ur/pdf/2008/295-1.pdf",
            sourceUrl = "https://www.dawateislami.net/bookslibrary/ur/saas-bahu-main-sulha-ka-raaz", mirrored = false
        ),
        HadithBook(
            id = "bk-muslimah-saas-bahu-main-sulha-ka-raaz-sd",
            collectionId = "muslimah_saas_bahu_main_sulha_ka_raaz", collectionName = "Saas Bahu Main Sulha Ka Raaz",
            romanTitle = "Saas Bahu Main Sulha Ka Raaz — Sindhi",
            nativeTitle = "سس ۽ ننهن ۾ صلح جو راز",
            languageCode = "sd", languageName = "Sindhi",
            pageCount = 0, fileId = 0,
            slug = "saas-bahu-main-sulha-ka-raaz", volume = 1, volumeCount = 1,
            remoteUrl = "https://data2.dawateislami.net/Data/Books/Download/si/pdf/2008/295-1.pdf",
            sourceUrl = "https://www.dawateislami.net/bookslibrary/si/saas-bahu-main-sulha-ka-raaz", mirrored = false
        ),
        HadithBook(
            id = "bk-muslimah-jannati-khawateen-ur",
            collectionId = "muslimah_jannati_khawateen", collectionName = "Jannati Khawateen",
            romanTitle = "Jannati Khawateen — Urdu",
            nativeTitle = "جنتی خواتین",
            languageCode = "ur", languageName = "Urdu",
            pageCount = 172, fileId = 0,
            slug = "jannati-khawateen", volume = 1, volumeCount = 1,
            remoteUrl = "https://data2.dawateislami.net/Data/Books/Download/ur/pdf/2025/3601-1.pdf",
            sourceUrl = "https://www.dawateislami.net/bookslibrary/ur/jannati-khawateen", mirrored = false
        ),
    )
}
