package com.example.ui.screens

import com.example.domain.model.DuaDatasetRepository

internal enum class KalimaGroup { KALIMA, IMAN }

internal data class KalimaPhrase(val arabic: String, val roman: String, val urdu: String, val english: String)

internal data class KalimaEntry(
    val id: String, val title: String, val arabic: String,
    val roman: String, val urdu: String, val english: String,
    val group: KalimaGroup,
    val number: Int?,
    val titleArabic: String,
    val nameMeaning: String,
    val nameMeaningEnglish: String,
    val phrases: List<KalimaPhrase>,
    val note: String,
    val noteEnglish: String,
    val reference: String
)

/** Traditional Arabic formulas; explanatory meanings are independently phrased. */
internal object KalimasContent {
    const val SOURCES = "6 Kalime: IslamInPerspective — The six Kalima (30 June 2022)\nhttps://www.islaminperspective.com/topics/fiqh/The-six-Kalima\n\nIman: Dawat-e-Islami USA — Shahadah (Faith)\nhttps://www.dawateislamiusa.org/pillars_of_islam/shahadah-faith"

    /** The numbered collection is a teaching arrangement, not the wording of a single hadith. */
    const val ABOUT = "6 Kalime bachpan se yaad karaye jaane wali riwayati tarteeb hai. Inke alfaaz zikr, tauheed aur tauba ke kalimaat par mushtamil hain; poori ginti wali tarteeb kisi ek hadith ke alfaaz nahin."
    const val ABOUT_ENGLISH = "The six Kalimas are a traditional teaching arrangement memorised from childhood. Their words are statements of remembrance, monotheism and repentance; the numbered collection as a whole is not the wording of one single hadith."

    /** Full Arabic and Roman text is the phrases joined by a space, so the two can never drift apart. */
    private fun entry(
        id: String, title: String, urdu: String, english: String, group: KalimaGroup, number: Int?,
        titleArabic: String, nameMeaning: String, nameMeaningEnglish: String, phrases: List<KalimaPhrase>,
        note: String = "", noteEnglish: String = "", reference: String = ""
    ) = KalimaEntry(id, title, phrases.joinToString(" ") { it.arabic }, phrases.joinToString(" ") { it.roman },
        urdu, english, group, number, titleArabic, nameMeaning, nameMeaningEnglish, phrases, note, noteEnglish, reference)

    val entries = listOf(
        entry("tayyibah", "1. Kalima Tayyibah",
            "Allah ke siwa koi ibadat ke layak nahin. Muhammad ﷺ Allah ke Rasool hain.",
            "Allah alone deserves worship. Muhammad ﷺ is Allah's Messenger.",
            KalimaGroup.KALIMA, 1, "كلمة طيبة", "Paakeeza kalima", "The pure word",
            listOf(
                KalimaPhrase("لَا إِلٰهَ إِلَّا اللّٰهُ", "La ilaha illallahu",
                    "Allah ke siwa koi ibadat ke layak nahin", "None deserves worship except Allah"),
                KalimaPhrase("مُحَمَّدٌ رَسُولُ اللّٰهِ", "Muhammadur Rasulullah.",
                    "Muhammad ﷺ Allah ke Rasool hain", "Muhammad ﷺ is the Messenger of Allah")
            ),
            note = "Yeh Islam ka bunyadi kalima hai: pehle hisse mein Allah ki tauheed aur doosre hisse mein Huzoor ﷺ ki risalat ka iqrar hai.",
            noteEnglish = "This is the foundational statement of Islam: its first part affirms the oneness of Allah and its second part the messengership of the Prophet ﷺ."),
        entry("shahadat", "2. Kalima Shahadat",
            "Main gawahi deta hoon ke Allah ke siwa koi ibadat ke layak nahin, Woh akela hai, Uska koi shareek nahin. Aur main gawahi deta hoon ke Muhammad ﷺ Uske bande aur Rasool hain.",
            "I testify that Allah alone deserves worship and has no partner. I also testify that Muhammad ﷺ is His servant and Messenger.",
            KalimaGroup.KALIMA, 2, "كلمة شهادت", "Gawahi ka kalima", "The word of testimony",
            listOf(
                KalimaPhrase("أَشْهَدُ أَنْ لَا إِلٰهَ إِلَّا اللّٰهُ", "Ashhadu al-la ilaha illallahu",
                    "Main gawahi deta hoon ke Allah ke siwa koi ibadat ke layak nahin",
                    "I testify that none deserves worship except Allah"),
                KalimaPhrase("وَحْدَهُ لَا شَرِيكَ لَهُ", "wahdahu la sharika lahu",
                    "Woh akela hai, Uska koi shareek nahin", "He is One, He has no partner"),
                KalimaPhrase("وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ", "wa ashhadu anna Muhammadan abduhu wa rasuluh.",
                    "Aur main gawahi deta hoon ke Muhammad ﷺ Uske bande aur Rasool hain",
                    "And I testify that Muhammad ﷺ is His servant and His Messenger")
            ),
            note = "Shahadat ka iqrar Islam ke paanch arkan mein se pehla rukn hai.",
            noteEnglish = "Bearing this testimony is the first of the five pillars of Islam.",
            reference = "Sahih al-Bukhari 8; Sahih Muslim 16 (Islam ke paanch arkan)"),
        entry("tamjeed", "3. Kalima Tamjeed",
            "Allah paak hai, saari tareef Allah ke liye hai. Allah ke siwa koi ibadat ke layak nahin aur Allah sab se bada hai. Gunah se bachne aur neki karne ki taqat Allah hi se hai, Jo buland aur azmat wala hai.",
            "Allah is free of imperfection; every praise belongs to Him. He alone deserves worship and is the Greatest. Our ability to turn away from evil and do good comes from Allah, the Most High, the Magnificent.",
            KalimaGroup.KALIMA, 3, "كلمة تمجيد", "Buzurgi bayan karne ka kalima", "The word of glorification",
            listOf(
                KalimaPhrase("سُبْحَانَ اللّٰهِ", "Subhanallahi", "Allah paak hai", "Allah is free of every imperfection"),
                KalimaPhrase("وَالْحَمْدُ لِلّٰهِ", "walhamdu lillahi",
                    "Aur saari tareef Allah ke liye hai", "And all praise belongs to Allah"),
                KalimaPhrase("وَلَا إِلٰهَ إِلَّا اللّٰهُ", "wa la ilaha illallahu",
                    "Aur Allah ke siwa koi ibadat ke layak nahin", "And none deserves worship except Allah"),
                KalimaPhrase("وَاللّٰهُ أَكْبَرُ", "wallahu akbar.", "Aur Allah sab se bada hai", "And Allah is the Greatest"),
                KalimaPhrase("وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللّٰهِ الْعَلِيِّ الْعَظِيمِ",
                    "Wa la hawla wa la quwwata illa billahil aliyyil azeem.",
                    "Burai se bachne ki taqat aur neki ki quwwat sirf Allah hi se hai, Jo buland aur azmat wala hai",
                    "There is no power to avoid evil and no strength to do good except through Allah, the Most High, the Magnificent")
            ),
            note = "Subhanallah, Alhamdulillah, La ilaha illallah aur Allahu Akbar ko hadith mein Allah ke nazdeek sab se pasandeeda kalimaat farmaya gaya hai.",
            noteEnglish = "SubhanAllah, Alhamdulillah, La ilaha illallah and Allahu Akbar are described in hadith as the words most beloved to Allah.",
            reference = "Sahih Muslim 2137"),
        entry("tauheed", "4. Kalima Tauheed",
            "Allah ke siwa koi ibadat ke layak nahin. Woh akela hai, Uska koi shareek nahin. Badshahi aur tareef Usi ki hai. Woh zindagi aur maut deta hai aur hamesha zinda hai, kabhi nahin marega. Woh jalal aur karam wala hai. Bhalai Usi ke haath mein hai aur Woh har cheez par qudrat rakhta hai.",
            "Allah alone deserves worship, without any partner. All sovereignty and praise are His. He gives life and death, yet He lives forever. Majesty and honour belong to Him. All goodness is in His hand, and His power extends over everything.",
            KalimaGroup.KALIMA, 4, "كلمة توحيد", "Allah ko ek maanne ka kalima", "The word of Allah's oneness",
            listOf(
                KalimaPhrase("لَا إِلٰهَ إِلَّا اللّٰهُ وَحْدَهُ لَا شَرِيكَ لَهُ", "La ilaha illallahu wahdahu la sharika lahu,",
                    "Allah ke siwa koi ibadat ke layak nahin, Woh akela hai, Uska koi shareek nahin",
                    "None deserves worship except Allah; He is One, He has no partner"),
                KalimaPhrase("لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ", "lahul mulku wa lahul hamdu,",
                    "Badshahi Usi ki hai aur tareef Usi ke liye hai", "His is the sovereignty and His is the praise"),
                KalimaPhrase("يُحْيِي وَيُمِيتُ", "yuhyi wa yumitu",
                    "Woh zindagi deta hai aur maut deta hai", "He gives life and He gives death"),
                KalimaPhrase("وَهُوَ حَيٌّ لَا يَمُوتُ أَبَدًا أَبَدًا", "wa huwa hayyul-la yamutu abadan abada,",
                    "Aur Woh zinda hai, kabhi bhi nahin marega", "And He is Ever-Living; He will never, ever die"),
                KalimaPhrase("ذُو الْجَلَالِ وَالْإِكْرَامِ", "dhul-jalali wal-ikram,",
                    "Woh jalal aur ikram wala hai", "Possessor of majesty and honour"),
                KalimaPhrase("بِيَدِهِ الْخَيْرُ", "biyadihil khayru",
                    "Bhalai Usi ke haath mein hai", "In His hand is all good"),
                KalimaPhrase("وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", "wa huwa ala kulli shay'in qadeer.",
                    "Aur Woh har cheez par qadir hai", "And He has power over everything")
            ),
            note = "Is kalime mein Allah ki wahdaniyat, Uski badshahi, hamesha ki zindagi aur har cheez par qudrat ka bayan hai.",
            noteEnglish = "This Kalima declares Allah's oneness, His sovereignty, His everlasting life and His power over all things."),
        entry("istighfar", "5. Kalima Istighfar",
            "Main apne Rab Allah se har gunah ki maafi mangta hoon jo jaan boojh kar ya ghalti se, chhup kar ya khule aam kiya. Jin gunahon ko main jaanta hoon aur jinhen nahin jaanta, sab se tauba karta hoon. Beshak Tu ghaib jaanne wala, aib chhupane wala aur gunah bakhshne wala hai. Taqat aur quwwat buland aur azmat wale Allah hi se hai.",
            "My Lord Allah, forgive every sin I have committed, deliberately or by mistake, privately or openly. I repent of the sins I recognise and those I do not. You know the unseen, conceal faults and forgive sins. All strength and ability come from Allah, the Most High, the Magnificent.",
            KalimaGroup.KALIMA, 5, "كلمة استغفار", "Bakhshish talab karne ka kalima", "The word of seeking forgiveness",
            listOf(
                KalimaPhrase("أَسْتَغْفِرُ اللّٰهَ رَبِّي مِنْ كُلِّ ذَنْبٍ", "Astaghfirullaha rabbi min kulli dhanbin",
                    "Main apne Rab Allah se har gunah ki maafi mangta hoon",
                    "I seek forgiveness from Allah, my Lord, for every sin"),
                KalimaPhrase("أَذْنَبْتُهُ عَمَدًا أَوْ خَطَأً", "adhnabtuhu amadan aw khata'an",
                    "Jo maine jaan boojh kar kiya ya ghalti se", "That I committed deliberately or by mistake"),
                KalimaPhrase("سِرًّا أَوْ عَلَانِيَةً", "sirran aw alaniyatan,",
                    "Chhup kar kiya ya khule aam", "Secretly or openly"),
                KalimaPhrase("وَأَتُوبُ إِلَيْهِ مِنَ الذَّنْبِ الَّذِي أَعْلَمُ", "wa atubu ilayhi minadh-dhanbil-ladhi a'lamu",
                    "Aur main Uski bargah mein us gunah se tauba karta hoon jise main jaanta hoon",
                    "And I turn to Him in repentance from the sin that I know"),
                KalimaPhrase("وَمِنَ الذَّنْبِ الَّذِي لَا أَعْلَمُ", "wa minadh-dhanbil-ladhi la a'lamu.",
                    "Aur us gunah se bhi jise main nahin jaanta", "And from the sin that I do not know"),
                KalimaPhrase("إِنَّكَ أَنْتَ عَلَّامُ الْغُيُوبِ", "Innaka anta allamul-ghuyubi",
                    "Beshak Tu hi ghaib ki baaton ko khoob jaanne wala hai", "Truly You are the Knower of all that is unseen"),
                KalimaPhrase("وَسَتَّارُ الْعُيُوبِ", "wa sattarul-uyubi",
                    "Aur aibon ko chhupane wala hai", "And the Concealer of faults"),
                KalimaPhrase("وَغَفَّارُ الذُّنُوبِ", "wa ghaffarudh-dhunubi,",
                    "Aur gunahon ko bakhshne wala hai", "And the Forgiver of sins"),
                KalimaPhrase("وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللّٰهِ الْعَلِيِّ الْعَظِيمِ",
                    "wa la hawla wa la quwwata illa billahil aliyyil azeem.",
                    "Burai se bachne ki taqat aur neki ki quwwat sirf Allah hi se hai, Jo buland aur azmat wala hai",
                    "There is no power to avoid evil and no strength to do good except through Allah, the Most High, the Magnificent")
            ),
            note = "Yeh tauba ka kalima hai: banda apne jaane aur anjaane, chhupe aur zahir tamam gunahon se Allah ki bargah mein rujoo karta hai.",
            noteEnglish = "This is a Kalima of repentance: the servant turns to Allah from every sin, known or unknown, hidden or open."),
        entry("radd_kufr", "6. Kalima Radd-e-Kufr",
            "Ya Allah! Main jaan boojh kar Tere saath kisi ko shareek karne se Teri panah mangta hoon, aur jo mujhe maloom nahin uski maafi mangta hoon. Main tauba karta hoon aur kufr, shirk, jhoot, gheebat, bid'at, chughli, behayai, bohtan aur sab gunahon se bezari karta hoon. Main Islam qabool karta hoon aur kehta hoon: Allah ke siwa koi ibadat ke layak nahin, Muhammad ﷺ Allah ke Rasool hain.",
            "O Allah, protect me from knowingly assigning a partner to You, and forgive what I do unknowingly. I repent and renounce disbelief, associating partners with You, lying, backbiting, religious innovation, tale-bearing, indecency, slander and every sin. I submit to Islam and declare that Allah alone deserves worship and Muhammad ﷺ is His Messenger.",
            KalimaGroup.KALIMA, 6, "كلمة ردِّ كفر", "Kufr ko radd karne ka kalima", "The word of rejecting disbelief",
            listOf(
                KalimaPhrase("اللّٰهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ أَنْ أُشْرِكَ بِكَ شَيْئًا وَأَنَا أَعْلَمُ بِهِ",
                    "Allahumma inni a'udhu bika min an ushrika bika shay'an wa ana a'lamu bihi,",
                    "Ya Allah! Main Teri panah mangta hoon is se ke jaan boojh kar kisi cheez ko Tera shareek banaun",
                    "O Allah, I seek Your protection from knowingly associating anything with You"),
                KalimaPhrase("وَأَسْتَغْفِرُكَ لِمَا لَا أَعْلَمُ بِهِ", "wa astaghfiruka lima la a'lamu bihi.",
                    "Aur jo mujhe maloom nahin us ki Tujh se maafi mangta hoon",
                    "And I seek Your forgiveness for what I do not know"),
                KalimaPhrase("تُبْتُ عَنْهُ", "Tubtu anhu", "Maine us se tauba ki", "I repent from it"),
                KalimaPhrase("وَتَبَرَّأْتُ مِنَ الْكُفْرِ وَالشِّرْكِ", "wa tabarra'tu minal-kufri wash-shirki",
                    "Aur main kufr aur shirk se bezar hua", "And I free myself from disbelief and from associating partners with Allah"),
                KalimaPhrase("وَالْكِذْبِ وَالْغِيبَةِ وَالْبِدْعَةِ", "wal-kidhbi wal-ghibati wal-bid'ati",
                    "Aur jhoot, gheebat aur bid'at se", "And from lying, backbiting and religious innovation"),
                KalimaPhrase("وَالنَّمِيمَةِ وَالْفَوَاحِشِ وَالْبُهْتَانِ", "wan-namimati wal-fawahishi wal-buhtani",
                    "Aur chughli, behayai ke kaamon aur bohtan se", "And from tale-bearing, indecent acts and slander"),
                KalimaPhrase("وَالْمَعَاصِي كُلِّهَا", "wal-ma'asi kulliha,",
                    "Aur tamam gunahon se", "And from all sins"),
                KalimaPhrase("وَأَسْلَمْتُ وَأَقُولُ", "wa aslamtu wa aqulu",
                    "Aur main Islam laya aur kehta hoon", "And I submit to Islam and I say"),
                KalimaPhrase("لَا إِلٰهَ إِلَّا اللّٰهُ مُحَمَّدٌ رَسُولُ اللّٰهِ", "la ilaha illallahu Muhammadur Rasulullah.",
                    "Allah ke siwa koi ibadat ke layak nahin, Muhammad ﷺ Allah ke Rasool hain",
                    "None deserves worship except Allah; Muhammad ﷺ is the Messenger of Allah")
            ),
            note = "Is kalime mein shirk se Allah ki panah, kufr aur bade gunahon se bezari, aur aakhir mein Kalima Tayyibah ke saath iman ki tajdeed hai.",
            noteEnglish = "This Kalima seeks Allah's protection from shirk, disowns disbelief and major sins, and ends by renewing faith with Kalima Tayyibah."),
        entry("mujmal", "Iman-e-Mujmal",
            "Main Allah par iman laya, jaisa Woh Apne naamon aur sifat ke saath hai. Maine Uske tamam ahkam qabool kiye, zaban se iqrar aur dil se tasdeeq ke saath.",
            "I affirm my faith in Allah with all His names and attributes, accepting every command of His through spoken acknowledgment and conviction in my heart.",
            KalimaGroup.IMAN, null, "إيمانِ مجمل", "Iman ka mukhtasar bayan", "Faith in brief",
            listOf(
                KalimaPhrase("آمَنْتُ بِاللّٰهِ كَمَا هُوَ", "Amantu billahi kama huwa",
                    "Main Allah par iman laya jaisa ke Woh hai", "I believe in Allah as He is"),
                KalimaPhrase("بِأَسْمَائِهِ وَصِفَاتِهِ", "bi asma'ihi wa sifatihi",
                    "Apne naamon aur Apni sifaton ke saath", "With His names and His attributes"),
                KalimaPhrase("وَقَبِلْتُ جَمِيعَ أَحْكَامِهِ", "wa qabiltu jami'a ahkamihi,",
                    "Aur maine Uske tamam ahkam qabool kiye", "And I have accepted all His commands"),
                KalimaPhrase("إِقْرَارٌ بِاللِّسَانِ وَتَصْدِيقٌ بِالْقَلْبِ", "iqrarum bil-lisani wa tasdiqum bil-qalb.",
                    "Zaban se iqrar hai aur dil se tasdeeq hai", "Affirming with the tongue and confirming with the heart")
            ),
            note = "Mujmal ka matlab hai mukhtasar. Is mein iman ka ijmaali iqrar hai: zaban se iqrar aur dil se tasdeeq.",
            noteEnglish = "Mujmal means concise. It is a summary declaration of faith: affirmation by the tongue and conviction in the heart."),
        entry("mufassal", "Iman-e-Mufassal",
            "Main Allah, Uske farishton, Uski kitabon, Uske Rasoolon, aakhirat ke din par iman laya. Aur is par ke taqdeer ki bhalai aur burai Allah Ta'ala ki taraf se hai, aur maut ke baad dobara uthaya jayega.",
            "I affirm belief in Allah, His angels, His books, His messengers and the Last Day; in destiny, both good and bad, being from Allah Most High; and in being raised again after death.",
            KalimaGroup.IMAN, null, "إيمانِ مفصل", "Iman ka tafseeli bayan", "Faith in detail",
            listOf(
                KalimaPhrase("آمَنْتُ بِاللّٰهِ", "Amantu billahi", "Main iman laya Allah par", "I believe in Allah"),
                KalimaPhrase("وَمَلَائِكَتِهِ", "wa mala'ikatihi", "Aur Uske farishton par", "And His angels"),
                KalimaPhrase("وَكُتُبِهِ", "wa kutubihi", "Aur Uski kitabon par", "And His books"),
                KalimaPhrase("وَرُسُلِهِ", "wa rusulihi", "Aur Uske Rasoolon par", "And His messengers"),
                KalimaPhrase("وَالْيَوْمِ الْآخِرِ", "wal-yawmil-akhiri", "Aur aakhirat ke din par", "And the Last Day"),
                KalimaPhrase("وَالْقَدَرِ خَيْرِهِ وَشَرِّهِ مِنَ اللّٰهِ تَعَالَىٰ", "wal-qadari khayrihi wa sharrihi minallahi ta'ala",
                    "Aur is par ke achhi aur buri taqdeer Allah Ta'ala ki taraf se hai",
                    "And that destiny, its good and its bad, is from Allah Most High"),
                KalimaPhrase("وَالْبَعْثِ بَعْدَ الْمَوْتِ", "wal-ba'thi ba'dal-mawt.",
                    "Aur maut ke baad dobara uthaye jaane par", "And in being raised again after death")
            ),
            note = "Mufassal ka matlab hai tafseeli. Is mein iman ki bunyadi baatein gini gayi hain: Allah, farishte, kitabein, Rasool, aakhirat ka din aur taqdeer. Yeh baatein Hadith-e-Jibreel mein bayan hui hain.",
            noteEnglish = "Mufassal means detailed. It lists the articles of faith: Allah, the angels, the books, the messengers, the Last Day and destiny. These are taught in the Hadith of Jibril.",
            reference = "Hadith-e-Jibreel — Sahih Muslim 8")
    )

    private val searchText: Map<String, String> = entries.associate { entry ->
        entry.id to DuaDatasetRepository.normalizeForSearch(buildString {
            append("${entry.title} ${entry.titleArabic} ${entry.arabic} ${entry.roman} ${entry.urdu} ${entry.english} ")
            append("${entry.nameMeaning} ${entry.nameMeaningEnglish} ${entry.note} ${entry.noteEnglish} ${entry.reference} ")
            entry.phrases.forEach { append("${it.urdu} ${it.english} ") }
        })
    }

    fun byId(id: String): KalimaEntry? = entries.firstOrNull { it.id == id }

    fun search(query: String, group: KalimaGroup? = null): List<KalimaEntry> {
        val words = DuaDatasetRepository.normalizeForSearch(query).split(' ').filter(String::isNotBlank)
        return entries.filter { entry ->
            (group == null || entry.group == group) && words.all(searchText.getValue(entry.id)::contains)
        }
    }

    fun shareText(entry: KalimaEntry, english: Boolean): String = listOf(
        entry.title, entry.arabic, entry.roman, if (english) entry.english else entry.urdu, "— Hidayah"
    ).joinToString("\n\n")
}

