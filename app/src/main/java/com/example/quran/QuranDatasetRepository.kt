package com.example.quran

import com.example.domain.model.*

object QuranDatasetRepository {

    val SURAHS = listOf(
        Surah(1, "الفاتحة", "Al-Fatihah", "The Opening", 7, "Meccan", 1),
        Surah(2, "البقرة", "Al-Baqarah", "The Cow", 286, "Medinan", 1),
        Surah(3, "آل عمران", "Ali 'Imran", "Family of Imran", 200, "Medinan", 3),
        Surah(4, "النساء", "An-Nisa", "The Women", 176, "Medinan", 4),
        Surah(18, "الكهف", "Al-Kahf", "The Cave", 110, "Meccan", 15),
        Surah(36, "يس", "Ya-Sin", "Ya-Sin", 83, "Meccan", 22),
        Surah(55, "الرحمن", "Ar-Rahman", "The Beneficent", 78, "Medinan", 27),
        Surah(67, "الملك", "Al-Mulk", "The Sovereignty", 30, "Meccan", 29),
        Surah(112, "الإخلاص", "Al-Ikhlas", "The Sincerity", 4, "Meccan", 30),
        Surah(113, "الفلق", "Al-Falaq", "The Daybreak", 5, "Meccan", 30),
        Surah(114, "الناس", "An-Nas", "Mankind", 6, "Meccan", 30)
    )

    val AL_FATIHAH_AYAT = listOf(
        Ayah(
            surahNumber = 1,
            ayahNumber = 1,
            arabicText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            transliteration = "Bismillāhir-Raḥmānir-Raḥīm",
            englishTranslation = "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
            urduTranslation = "شروع اللہ کا نام لے کر جو بڑا مہربان نہایت رحم والا ہے",
            words = listOf(
                QuranWord(1, "بِسْمِ", "Bismi", "In name (of)", "نام سے", "س م و", "Preposition + Noun"),
                QuranWord(2, "اللَّهِ", "Allāhi", "Allah", "اللہ کے", "إ ل ه", "Proper Name"),
                QuranWord(3, "الرَّحْمَٰنِ", "Ar-Raḥmāni", "The Entirely Merciful", "بڑا مہربان", "ر ح م", "Attribute / Adjective"),
                QuranWord(4, "الرَّحِيمِ", "Ar-Raḥīmi", "The Especially Merciful", "نہایت رحم والا", "ر ح م", "Attribute / Adjective")
            ),
            tafsirSummary = "The Basmalah precedes all actions in Islam. Ar-Rahman encompasses all creations with vast mercy; Ar-Rahim refers to the special mercy bestowed upon believers.",
            tafsirSource = "Tafsir Ibn Kathir (Vol 1, p. 28)",
            juzNumber = 1
        ),
        Ayah(
            surahNumber = 1,
            ayahNumber = 2,
            arabicText = "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
            transliteration = "Al-ḥamdu lillāhi Rabbil-'ālamīn",
            englishTranslation = "All praise is due to Allah, Lord of the worlds.",
            urduTranslation = "سب تعریفیں اللہ ہی کے لیے ہیں جو تمام جہانوں کا پالنے والا ہے",
            words = listOf(
                QuranWord(5, "الْحَمْدُ", "Al-ḥamdu", "All praise", "سب تعریف", "ح م د", "Definite Noun"),
                QuranWord(6, "لِلَّهِ", "Lillāhi", "Due to Allah", "اللہ کے لیے", "إ ل ه", "Preposition + Proper Noun"),
                QuranWord(7, "رَبِّ", "Rabbi", "Lord / Sustainer", "رب / پالنہار", "ر ب ب", "Noun in construct"),
                QuranWord(8, "الْعَالَمِينَ", "Al-'ālamīna", "The worlds / creations", "تمام جہانوں کا", "ع ل م", "Plural Noun")
            ),
            tafsirSummary = "Hamd combines total praise, gratitude, love, and veneration to the absolute Sustainer, Cherisher, and Provider of all realms of existence.",
            tafsirSource = "Ma'ariful Quran (Mufti Shafi Usmani, Vol 1)",
            juzNumber = 1
        ),
        Ayah(
            surahNumber = 1,
            ayahNumber = 3,
            arabicText = "الرَّحْمَٰنِ الرَّحِيمِ",
            transliteration = "Ar-Raḥmānir-Raḥīm",
            englishTranslation = "The Entirely Merciful, the Especially Merciful.",
            urduTranslation = "بڑا مہربان نہایت رحم فرمانے والا ہے",
            words = listOf(
                QuranWord(9, "الرَّحْمَٰنِ", "Ar-Raḥmāni", "The Entirely Merciful", "نہایت مہربان", "ر ح م", "Adjective"),
                QuranWord(10, "الرَّحِيمِ", "Ar-Raḥīmi", "The Especially Merciful", "رحم والا", "ر ح م", "Adjective")
            ),
            tafsirSummary = "Reiteration of Divine mercy follows the mention of Lordship, demonstrating that sovereignty over all worlds is governed through profound compassion.",
            tafsirSource = "Tafsir Al-Qurtubi",
            juzNumber = 1
        ),
        Ayah(
            surahNumber = 1,
            ayahNumber = 4,
            arabicText = "مَالِكِ يَوْمِ الدِّينِ",
            transliteration = "Māliki Yawmid-Dīn",
            englishTranslation = "Sovereign of the Day of Recompense.",
            urduTranslation = "روزِ جزا کا مالک ہے",
            words = listOf(
                QuranWord(11, "مَالِكِ", "Māliki", "Master / Sovereign", "مالک", "م ل ك", "Active Participle"),
                QuranWord(12, "يَوْمِ", "Yawmi", "Day", "دن کا", "ي و م", "Noun"),
                QuranWord(13, "الدِّينِ", "Ad-Dīni", "Judgment / Recompense", "جزا و سزا کا", "د ي ن", "Noun")
            ),
            tafsirSummary = "Allah is the Master of this world and the Hereafter, but on the Day of Recompense no one will possess even the illusion of independence or dominion.",
            tafsirSource = "Tafsir As-Sa'di",
            juzNumber = 1
        ),
        Ayah(
            surahNumber = 1,
            ayahNumber = 5,
            arabicText = "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
            transliteration = "Iyyāka na'budu wa iyyāka nasta'īn",
            englishTranslation = "It is You we worship and You we ask for help.",
            urduTranslation = "ہم صرف تیری ہی عبادت کرتے ہیں اور تجھ ہی سے مدد مانگتے ہیں",
            words = listOf(
                QuranWord(14, "إِيَّاكَ", "Iyyāka", "You alone", "تجھ ہی کو", "إ ي ي", "Pronoun of exclusivity"),
                QuranWord(15, "نَعْبُدُ", "Na'budu", "We worship", "ہم عبادت کرتے ہیں", "ع ب د", "Imperfect Verb 1st Plural"),
                QuranWord(16, "وَإِيَّاكَ", "Wa iyyāka", "And You alone", "اور تجھ ہی سے", "إ ي ي", "Conjunction + Pronoun"),
                QuranWord(17, "نَسْتَعِينُ", "Nasta'īnu", "We seek assistance", "ہم مدد چاہتے ہیں", "ع و ن", "Verb (Form X)")
            ),
            tafsirSummary = "The core creed of pure monotheism (Tawhid): worship precedes seeking help. We acknowledge our total helplessness without Divine grace.",
            tafsirSource = "Madarij as-Salikin (Ibn Qayyim)",
            juzNumber = 1
        ),
        Ayah(
            surahNumber = 1,
            ayahNumber = 6,
            arabicText = "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ",
            transliteration = "Ihdinaṣ-Ṣirāṭal-Mustaqīm",
            englishTranslation = "Guide us to the straight path -",
            urduTranslation = "ہمیں سیدھے راستے پر چلا",
            words = listOf(
                QuranWord(18, "اهْدِنَا", "Ihdinā", "Guide us", "ہمیں ہدایت دے", "ه د ي", "Imperative verb + pronoun"),
                QuranWord(19, "الصِّرَاطَ", "Aṣ-Ṣirāṭa", "The path", "راستہ", "ص ر ط", "Definite Noun"),
                QuranWord(20, "الْمُسْتَقِيمَ", "Al-Mustaqīma", "The straight", "سیدھا", "ق و م", "Active Participle")
            ),
            tafsirSummary = "The greatest supplication a human being can make: steadfast perseverance upon the clear truth revealed to the Prophets.",
            tafsirSource = "Tafsir Ibn Kathir",
            juzNumber = 1
        ),
        Ayah(
            surahNumber = 1,
            ayahNumber = 7,
            arabicText = "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ",
            transliteration = "Ṣirāṭal-ladhīna an'amta 'alayhim ghayril-maghḍūbi 'alayhim wa laḍ-ḍāllīn",
            englishTranslation = "The path of those upon whom You have bestowed favor, not of those who have evoked anger or of those who are astray.",
            urduTranslation = "ان لوگوں کا راستہ جن پر تو نے انعام فرمایا، نہ ان کا جن پر غضب ہوا اور نہ گمراہوں کا",
            words = listOf(
                QuranWord(21, "صِرَاطَ", "Ṣirāṭa", "Path of", "راستہ", "ص ر ط", "Noun"),
                QuranWord(22, "الَّذِينَ", "Alladhīna", "Those who", "ان کا جن", "ل ذ ي", "Relative pronoun"),
                QuranWord(23, "أَنْعَمْتَ", "An'amta", "You bestowed favor", "تو نے انعام کیا", "ن ع م", "Verb 2nd Masc."),
                QuranWord(24, "عَلَيْهِمْ", "'Alayhim", "Upon them", "ان پر", "ع ل ى", "Preposition + Pronoun"),
                QuranWord(25, "غَيْرِ", "Ghayri", "Not of", "سوائے", "غ ي ر", "Noun of negation"),
                QuranWord(26, "الْمَغْضُوبِ", "Al-Maghḍūbi", "Those who incurred wrath", "جن پر غضب ہوا", "غ ض ب", "Passive participle"),
                QuranWord(27, "عَلَيْهِمْ", "'Alayhim", "Upon them", "ان پر", "ع ل ى", "Preposition + Pronoun"),
                QuranWord(28, "وَلَا", "Wa lā", "And nor", "اور نہ", "ل ا", "Negative conjunction"),
                QuranWord(29, "الضَّالِّينَ", "Aḍ-Ḍāllīn", "Those astray", "گمراہ لوگ", "ض ل ل", "Active Participle plural")
            ),
            tafsirSummary = "The path of the Prophets, truthful, martyrs and righteous, avoiding both arrogant rebellion against known truth and ignorant misguidance.",
            tafsirSource = "Tafsir Ibn Kathir / Sahih Bukhari 782",
            juzNumber = 1
        )
    )

    val AL_BAQARAH_KEY_AYAT = listOf(
        Ayah(
            surahNumber = 2,
            ayahNumber = 82,
            arabicText = "وَالَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ أُولَٰئِكَ أَصْحَابُ الْجَنَّةِ ۖ هُمْ فِيهَا خَالِدُونَ",
            transliteration = "Wal-ladhīna āmanū wa 'amiluṣ-ṣāliḥāti ulā'ika aṣ-ḥābul-Jannah, hum fīhā khālidūn",
            englishTranslation = "But they who believe and do righteous deeds - those are the companions of Paradise; they will abide therein eternally.",
            urduTranslation = "اور جو لوگ ایمان لائے اور انہوں نے نیک اعمال کیے، وہی اہلِ جنت ہیں، وہ اس میں ہمیشہ رہیں گے",
            words = listOf(
                QuranWord(101, "وَالَّذِينَ", "Wal-ladhīna", "And those who", "اور وہ جو", "ل ذ ي", "Conjunction + Pronoun"),
                QuranWord(102, "آمَنُوا", "Āmanū", "Believed", "ایمان لائے", "أ م ن", "Verb"),
                QuranWord(103, "وَعَمِلُوا", "Wa 'amilū", "And did", "اور کیے", "ع م ل", "Verb"),
                QuranWord(104, "الصَّالِحَاتِ", "Aṣ-Ṣāliḥāti", "Righteous deeds", "نیک اعمال", "ص ل ح", "Noun Plural"),
                QuranWord(105, "أُولَٰئِكَ", "Ulā'ika", "Those are", "وہی لوگ", "أ ل ك", "Demonstrative"),
                QuranWord(106, "أَصْحَابُ", "Aṣ-ḥābu", "Companions of", "ساتھی", "ص ح ب", "Noun"),
                QuranWord(107, "الْجَنَّةِ", "Al-Jannah", "Paradise", "جنت کے", "ج ن ن", "Proper Noun"),
                QuranWord(108, "خَالِدُونَ", "Khālidūn", "Eternally abiding", "ہمیشہ رہنے والے", "خ ل د", "Active Participle")
            ),
            tafsirSummary = "Faith must be validated by righteous deeds. Sincere belief accompanied by virtuous conduct leads by Allah's mercy to eternal peace in Jannah.",
            tafsirSource = "Tafsir Al-Baghawi",
            juzNumber = 1
        ),
        Ayah(
            surahNumber = 2,
            ayahNumber = 153,
            arabicText = "يَا أَيُّهَا الَّذِينَ آمَنُوا اسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ ۚ إِنَّ اللَّهَ مَعَ الصَّابِرِينَ",
            transliteration = "Yā ayyuhal-ladhīna āmanus-ta'īnū biṣ-ṣabri waṣ-ṣalāh; innallāha ma'aṣ-ṣābirīn",
            englishTranslation = "O you who have believed, seek help through patience and prayer. Indeed, Allah is with the patient.",
            urduTranslation = "اے ایمان والو! صبر اور نماز کے ذریعے مدد چاہو، بے شک اللہ صبر کرنے والوں کے ساتھ ہے",
            words = listOf(
                QuranWord(201, "يَا أَيُّهَا", "Yā ayyuhā", "O you", "اے لوگو", "ي أ ي", "Vocative Particle"),
                QuranWord(202, "اسْتَعِينُوا", "Ista'īnū", "Seek help", "مدد چاہو", "ع و ن", "Imperative Verb"),
                QuranWord(203, "بِالصَّبْرِ", "Biṣ-ṣabri", "Through patience", "صبر سے", "ص ب ر", "Noun with preposition"),
                QuranWord(204, "وَالصَّلَاةِ", "Waṣ-ṣalāh", "And prayer", "اور نماز سے", "ص ل و", "Noun"),
                QuranWord(205, "الصَّابِرِينَ", "Aṣ-ṣābirīn", "The patient ones", "صبر کرنے والے", "ص ب ر", "Plural Participle")
            ),
            tafsirSummary = "When worldly trials press hard upon the heart, the dual anchors of steadfast patience (Sabr) and heartfelt prayer (Salah) connect the soul directly to Divine fortitude.",
            tafsirSource = "Tafsir Ibn Kathir (Vol 1, p. 450)",
            juzNumber = 2
        ),
        Ayah(
            surahNumber = 2,
            ayahNumber = 255,
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ",
            transliteration = "Allāhu lā ilāha illā Huwal-Ḥayyul-Qayyūm; lā ta'khudhuhū sinatuw-wa lā nawm",
            englishTranslation = "Allah - there is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep.",
            urduTranslation = "اللہ وہ ہے جس کے سوا کوئی معبود نہیں، وہ ہمیشہ زندہ اور سب کا تھامنے والا ہے، نہ اسے اونگھ آتی ہے نہ نیند",
            words = listOf(
                QuranWord(301, "الْحَيُّ", "Al-Ḥayyu", "The Ever-Living", "ہمیشہ زندہ", "ح ي ي", "Divine Name"),
                QuranWord(302, "الْقَيُّومُ", "Al-Qayyūm", "The Self-Sustaining", "سب کا تھامنے والا", "ق و م", "Divine Name"),
                QuranWord(303, "سِنَةٌ", "Sinatun", "Drowsiness", "اونگھ", "و س ن", "Noun"),
                QuranWord(304, "نَوْمٌ", "Nawmun", "Sleep", "نیند", "ن و م", "Noun")
            ),
            tafsirSummary = "Ayat al-Kursi (The Verse of the Throne) is the greatest verse in the Quran according to authentic hadith in Sahih Muslim (810). It establishes the absolute supremacy and unceasing preservation of the Creator.",
            tafsirSource = "Sahih Muslim 810 / Tafsir Ibn Kathir",
            juzNumber = 3
        )
    )

    /** Only complete bundled reader data is shown in the surah picker. */
    /** Full 114-surah table of contents; text is loaded when the reader opens a surah. */
    val AVAILABLE_SURAHS: List<Surah> = FullQuranCatalog.surahs

    fun getAyatForSurah(surahNumber: Int): List<Ayah> {
        return when (surahNumber) {
            1 -> AL_FATIHAH_AYAT
            2 -> AL_BAQARAH_KEY_AYAT
            else -> emptyList()
        }
    }

    fun searchQuran(query: String): List<Ayah> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        val all = AL_FATIHAH_AYAT + AL_BAQARAH_KEY_AYAT
        return all.filter {
            it.arabicText.contains(q) ||
            it.englishTranslation.lowercase().contains(q) ||
            it.transliteration.lowercase().contains(q) ||
            it.urduTranslation.contains(q)
        }
    }
}
