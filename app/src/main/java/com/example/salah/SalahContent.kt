package com.example.salah

/** Text is keyed by language so reviewed translations and recitation assets can be added later. */
data class SalahText(val en: String, val translations: Map<String, String> = emptyMap()) {
    fun inLanguage(code: String): String = translations[code] ?: en
}

enum class Madhhab(val label: String) {
    HANAFI("Hanafi"), SHAFII("Shafi‘i"), MALIKI("Maliki"), HANBALI("Hanbali")
}

enum class SalahKind(val label: String) {
    FARD("Fard"), SUNNAH_MUAKKADAH("Sunnah Muakkadah"),
    SUNNAH_GHAIR_MUAKKADAH("Sunnah Ghair-Muakkadah"), NAFL("Nafl"),
    WITR("Witr"), WAJIB("Wajib"), FARD_KIFAYAH("Fard Kifayah")
}

data class Rakats(val count: Int, val kind: SalahKind, val whenPrayed: String, val whenPrayedTranslations: Map<String, String> = emptyMap()) {
    fun whenPrayedIn(language: String): String = whenPrayedTranslations[language] ?: whenPrayed
    fun countLabel(madhhab: Madhhab): String =
        if (kind == SalahKind.WITR && madhhab != Madhhab.HANAFI) "1+ odd" else count.toString()
}
data class DailyPrayer(val id: String, val title: String, val units: List<Rakats>, val titleTranslations: Map<String, String> = emptyMap()) {
    fun titleIn(language: String): String = titleTranslations[language] ?: title
    val fard: Int get() = units.first { it.kind == SalahKind.FARD }.count
}

data class Recitation(
    val id: String,
    val title: SalahText,
    val arabic: String,
    val transliteration: String,
    val meaning: SalahText,
    val source: String,
    val audioAsset: String? = null
)

data class GuideStep(
    val id: String,
    val title: SalahText,
    val instruction: SalahText,
    val recitationIds: List<String> = emptyList(),
    val schoolNotes: Map<Madhhab, String> = emptyMap()
)

data class TopicTranslation(val title: String, val summary: String, val steps: List<String>)

data class SalahTopic(
    val id: String,
    val title: String,
    val kind: SalahKind?,
    val summary: String,
    val steps: List<String>,
    val schoolNotes: Map<Madhhab, String> = emptyMap(),
    val source: String,
    val translations: Map<String, TopicTranslation> = emptyMap(),
    val kindBySchool: Map<Madhhab, SalahKind> = emptyMap()
) {
    fun kindFor(madhhab: Madhhab): SalahKind? = kindBySchool[madhhab] ?: kind
    fun titleIn(language: String): String = translations[language]?.title ?: title
    fun summaryIn(language: String): String = translations[language]?.summary ?: summary
    fun stepsIn(language: String): List<String> = translations[language]?.steps ?: steps
}

/** Educational content only. Prayer times remain in the existing prayer-time module. */
object SalahContent {
    object Notes {
        const val DAILY = "The sequence below is a commonly taught Hanafi pattern. Fard is obligatory; optional Sunnah and Nafl practice varies. Witr is wajib in the Hanafi school and Sunnah in the others."
        const val PRAYER_DETAIL = "This optional-prayer sequence is a Hanafi teaching pattern. Other schools may classify or arrange some Sunnah units differently. Follow your local teacher for detailed practice."
        const val GUIDE = "This teaches a basic Fard sequence. Details such as recitation behind an imam, hand placement and qunut vary by madhhab. Follow a qualified teacher for your school."
        const val TOPIC = "Detailed rulings and exceptions depend on your circumstances and school. Ask a qualified local scholar when unsure."
        const val RECITATION = "Arabic text and English meaning are shown with a learning transliteration. Pronunciation is best learned from a qualified reciter. Audio can be added to each recitation later."
    }
    val schoolSummaries = mapOf(
        Madhhab.HANAFI to "Witr is wajib (three rak‘at); qunut is before ruku‘ in its third rak‘ah. Fajr qunut is not ordinarily read.",
        Madhhab.SHAFII to "Witr is Sunnah. Qunut is practiced after ruku‘ in the second Fajr rak‘ah; Witr qunut is practiced in the latter half of Ramadan.",
        Madhhab.MALIKI to "Witr is Sunnah, commonly a single rak‘ah after two shaf‘ rak‘at. Details of Fajr qunut vary within the school.",
        Madhhab.HANBALI to "Witr is Sunnah and may be offered in an odd number of rak‘at. Qunut in Witr is permitted."
    )
    val daily = listOf(
        DailyPrayer("fajr", "Fajr", listOf(Rakats(2, SalahKind.SUNNAH_MUAKKADAH, "Before Fard"), Rakats(2, SalahKind.FARD, "Dawn"))),
        DailyPrayer("zuhr", "Zuhr", listOf(Rakats(4, SalahKind.SUNNAH_MUAKKADAH, "Before Fard"), Rakats(4, SalahKind.FARD, "Midday"), Rakats(2, SalahKind.SUNNAH_MUAKKADAH, "After Fard"), Rakats(2, SalahKind.NAFL, "Optional after"))),
        DailyPrayer("asr", "Asr", listOf(Rakats(4, SalahKind.SUNNAH_GHAIR_MUAKKADAH, "Optional before"), Rakats(4, SalahKind.FARD, "Afternoon"))),
        DailyPrayer("maghrib", "Maghrib", listOf(Rakats(3, SalahKind.FARD, "Sunset"), Rakats(2, SalahKind.SUNNAH_MUAKKADAH, "After Fard"), Rakats(2, SalahKind.NAFL, "Optional after"))),
        DailyPrayer("isha", "Isha", listOf(Rakats(4, SalahKind.SUNNAH_GHAIR_MUAKKADAH, "Optional before"), Rakats(4, SalahKind.FARD, "Night"), Rakats(2, SalahKind.SUNNAH_MUAKKADAH, "After Fard"), Rakats(2, SalahKind.NAFL, "Optional after"), Rakats(3, SalahKind.WITR, "After Isha")))
    )

    val recitations = listOf(
        Recitation("takbir", SalahText("Opening takbir"), "اللَّهُ أَكْبَرُ", "Allahu akbar", SalahText("Allah is the Greatest."), "Sahih al-Bukhari 757"),
        Recitation("fatiha", SalahText("Surah al-Fatihah"), "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nالْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ\nالرَّحْمَٰنِ الرَّحِيمِ\nمَالِكِ يَوْمِ الدِّينِ\nإِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ\nاهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ\nصِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "Bismillahir-rahmanir-rahim. Alhamdu lillahi rabbil-'alamin. Ar-rahmanir-rahim. Maliki yawmid-din. Iyyaka na'budu wa iyyaka nasta'in. Ihdinas-siratal-mustaqim. Siratal-ladhina an'amta 'alayhim ghayril-maghdubi 'alayhim wa lad-dallin.", SalahText("In the name of Allah, the Most Merciful, the Especially Merciful. Praise belongs to Allah, Lord of all worlds; the Most Merciful, the Especially Merciful; Master of the Day of Judgment. You alone we worship and You alone we ask for help. Guide us to the straight path: the path of those You have blessed, not those who earned anger or went astray."), "Quran 1:1–7"),
        Recitation("ikhlas", SalahText("Short surah: al-Ikhlas"), "قُلْ هُوَ اللَّهُ أَحَدٌ\nاللَّهُ الصَّمَدُ\nلَمْ يَلِدْ وَلَمْ يُولَدْ\nوَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ", "Qul huwallahu ahad. Allahus-samad. Lam yalid wa lam yulad. Wa lam yakul-lahu kufuwan ahad.", SalahText("Say: He is Allah, One; Allah, the Eternal Refuge. He neither begets nor is born, and none is comparable to Him."), "Quran 112:1–4"),
        Recitation("ruku", SalahText("In bowing"), "سُبْحَانَ رَبِّيَ الْعَظِيمِ", "Subhana rabbiyal-'azim", SalahText("Glory be to my Lord, the Magnificent."), "Sunan Abi Dawud 871"),
        Recitation("rise", SalahText("Rising from bowing"), "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ\nرَبَّنَا وَلَكَ الْحَمْدُ", "Sami'allahu liman hamidah. Rabbana wa lakal-hamd.", SalahText("Allah hears the one who praises Him. Our Lord, to You belongs all praise."), "Sahih al-Bukhari 795"),
        Recitation("sujud", SalahText("In prostration"), "سُبْحَانَ رَبِّيَ الْأَعْلَى", "Subhana rabbiyal-a'la", SalahText("Glory be to my Lord, the Most High."), "Sunan Abi Dawud 871"),
        Recitation("between", SalahText("Between prostrations"), "رَبِّ اغْفِرْ لِي", "Rabbighfir li", SalahText("My Lord, forgive me."), "Sunan Abi Dawud 874"),
        Recitation("tashahhud", SalahText("Tashahhud"), "التَّحِيَّاتُ لِلَّهِ وَالصَّلَوَاتُ وَالطَّيِّبَاتُ، السَّلَامُ عَلَيْكَ أَيُّهَا النَّبِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ، السَّلَامُ عَلَيْنَا وَعَلَى عِبَادِ اللَّهِ الصَّالِحِينَ، أَشْهَدُ أَنْ لَا إِلَٰهَ إِلَّا اللَّهُ، وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ", "At-tahiyyatu lillahi was-salawatu wat-tayyibat. As-salamu 'alayka ayyuhan-nabiyyu wa rahmatullahi wa barakatuh. As-salamu 'alayna wa 'ala 'ibadillahis-salihin. Ashhadu al-la ilaha illallah, wa ashhadu anna Muhammadan 'abduhu wa rasuluh.", SalahText("All greetings, prayers and good words are for Allah. Peace, mercy and blessings be upon you, O Prophet. Peace be upon us and Allah's righteous servants. I bear witness that there is no god but Allah, and Muhammad is His servant and messenger."), "Sahih al-Bukhari 831"),
        Recitation("salawat", SalahText("Salawat Ibrahimiyyah"), "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ، اللَّهُمَّ بَارِكْ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ كَمَا بَارَكْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ", "Allahumma salli 'ala Muhammadin wa 'ala ali Muhammad kama sallayta 'ala Ibrahima wa 'ala ali Ibrahim innaka hamidum-majid. Allahumma barik 'ala Muhammadin wa 'ala ali Muhammad kama barakta 'ala Ibrahima wa 'ala ali Ibrahim innaka hamidum-majid.", SalahText("O Allah, bless Muhammad and his family as You blessed Ibrahim and his family; You are Praiseworthy and Glorious. O Allah, grant blessing to Muhammad and his family as You granted blessing to Ibrahim and his family; You are Praiseworthy and Glorious."), "Sahih al-Bukhari 3370"),
        Recitation("salam", SalahText("Ending salams"), "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ", "As-salamu 'alaykum wa rahmatullah", SalahText("Peace and Allah's mercy be upon you."), "Sahih Muslim 582"),
        Recitation("qunut", SalahText("Witr qunut (one transmitted wording)"), "اللَّهُمَّ اهْدِنِي فِيمَنْ هَدَيْتَ، وَعَافِنِي فِيمَنْ عَافَيْتَ، وَتَوَلَّنِي فِيمَنْ تَوَلَّيْتَ، وَبَارِكْ لِي فِيمَا أَعْطَيْتَ، وَقِنِي شَرَّ مَا قَضَيْتَ، إِنَّكَ تَقْضِي وَلَا يُقْضَى عَلَيْكَ، إِنَّهُ لَا يَذِلُّ مَنْ وَالَيْتَ، تَبَارَكْتَ رَبَّنَا وَتَعَالَيْتَ", "Allahumma-hdini fiman hadayt, wa 'afini fiman 'afayt, wa tawallani fiman tawallayt, wa barik li fima a'tayt, wa qini sharra ma qadayt. Innaka taqdi wa la yuqda 'alayk. Innahu la yadhillu man walayt. Tabarakta rabbana wa ta'alayt.", SalahText("O Allah, guide me among those You have guided; grant me well-being among those You have granted well-being; care for me among those You have cared for; bless what You have given me; and protect me from the harm of what You have decreed. You decree and none decrees over You. Whoever You befriend is not humiliated. Blessed and Exalted are You, our Lord."), "Sunan Abi Dawud 1425")
    ).associateBy { it.id }

    val wuduSteps = listOf(
        "Make the intention to purify yourself and say Bismillah.",
        "Wash both hands up to the wrists, three times.",
        "Rinse the mouth and clean the nose, three times each.",
        "Wash the whole face, from hairline to chin and ear to ear, three times.",
        "Wash each arm including the elbow, right then left, three times.",
        "Wipe the head with wet hands; wipe the ears. The required extent varies by school.",
        "Wash both feet including the ankles, right then left, three times."
    )

    val topics = listOf(
        SalahTopic("wudu", "Wudu", null, "Purification before Salah", wuduSteps, mapOf(Madhhab.HANAFI to "Wiping at least a quarter of the head is obligatory in Hanafi law.", Madhhab.SHAFII to "Wiping part of the head suffices in Shafi‘i law; intention and sequence are required.", Madhhab.MALIKI to "Wiping the entire head is required in Maliki law.", Madhhab.HANBALI to "Wiping the entire head is required in Hanbali law."), "Quran 5:6"),
        SalahTopic("jamaat", "Jamaat", null, "Congregational prayer", listOf("Arrive with wudu and join the rows without rushing.", "Make your intention and follow the imam's movements; do not precede the imam.", "If joining late, complete missed rak‘at after the imam's final salam."), source = "Sahih al-Bukhari 636"),
        SalahTopic("qaza", "Qaza Salah", SalahKind.FARD, "Making up missed obligatory prayers", listOf("Pray a missed Fard as soon as you reasonably can.", "Keep a careful count of missed prayers and make a manageable plan.", "Intend the particular missed prayer; ask a qualified scholar about ordering, travel and special cases."), mapOf(Madhhab.HANAFI to "Hanafi Witr is wajib and is also made up.", Madhhab.SHAFII to "Witr is Sunnah, not a missed Fard obligation.", Madhhab.MALIKI to "Witr is Sunnah, not a missed Fard obligation.", Madhhab.HANBALI to "Witr is Sunnah, not a missed Fard obligation."), "Sahih al-Bukhari 597"),
        SalahTopic("sahw", "Sajdah Sahw", null, "Prostrations for certain prayer mistakes", listOf("Identify what was omitted or added; an omitted pillar cannot simply be fixed by Sajdah Sahw.", "For a qualifying mistake, perform two prostrations of forgetfulness around the closing of the prayer.", "The timing and what counts as a qualifying mistake depend on your school; check its detailed ruling."), mapOf(Madhhab.HANAFI to "Normally after the final tashahhud and one salam, make two prostrations, sit, then end with salam.", Madhhab.SHAFII to "Normally two prostrations before the closing salam.", Madhhab.MALIKI to "Before or after salam depending on whether the issue was an omission or addition.", Madhhab.HANBALI to "Timing depends on the error; follow the applicable ruling."), "Sahih al-Bukhari 1224"),
        SalahTopic("jumuah", "Jumu‘ah", SalahKind.FARD, "Friday congregational prayer", listOf("Prepare with ghusl and arrive early if possible.", "Listen attentively to the two khutbahs.", "Pray two Fard rak‘at behind the imam instead of Zuhr when Jumu‘ah applies to you.", "If you miss Jumu‘ah, pray Zuhr; follow local scholarly guidance for individual circumstances."), source = "Quran 62:9"),
        SalahTopic("eid", "Eid Salah", null, "Two rak‘at in congregation", listOf("Join the Eid congregation at its announced time.", "Follow the imam for two rak‘at and the extra takbirs.", "Listen to the khutbah after the prayer."), mapOf(Madhhab.HANAFI to "Three additional takbirs in each rak‘ah; second set is before ruku‘.", Madhhab.SHAFII to "Seven additional takbirs in the first rak‘ah and five in the second, before recitation.", Madhhab.MALIKI to "The extra takbirs differ from the Hanafi sequence; follow the imam.", Madhhab.HANBALI to "The extra takbirs differ from the Hanafi sequence; follow the imam."), "Sahih al-Bukhari 956", kindBySchool = mapOf(Madhhab.HANAFI to SalahKind.WAJIB, Madhhab.SHAFII to SalahKind.SUNNAH_MUAKKADAH, Madhhab.MALIKI to SalahKind.SUNNAH_MUAKKADAH, Madhhab.HANBALI to SalahKind.FARD_KIFAYAH)),
        SalahTopic("janazah", "Janazah", SalahKind.FARD_KIFAYAH, "Funeral prayer; no bowing or prostration", listOf("Stand in congregation with the intention for the deceased.", "Follow the imam's takbirs while standing; recite praise, blessings upon the Prophet, and supplication for the deceased as taught in your school.", "End with the imam's salam."), mapOf(Madhhab.HANAFI to "Four takbirs; a common Hanafi order is thana, salawat, dua for the deceased, then salam.", Madhhab.SHAFII to "Al-Fatihah is recited after the first takbir."), "Sahih al-Bukhari 1333"),
        SalahTopic("tahajjud", "Tahajjud", SalahKind.NAFL, "Optional night prayer", listOf("Pray after Isha and before Fajr, preferably after some sleep.", "Pray voluntary rak‘at, commonly in pairs.", "Make sincere dua; pray Witr last if you have not prayed it already."), source = "Quran 17:79"),
        SalahTopic("taraweeh", "Taraweeh", SalahKind.SUNNAH_MUAKKADAH, "Ramadan night prayer", listOf("Pray after Isha during Ramadan, usually with a congregation.", "Follow the imam in units of two rak‘at and rest when the congregation rests.", "The total number practiced differs across communities; follow your mosque and school."), source = "Sahih al-Bukhari 2009"),
        SalahTopic("witr", "Witr", SalahKind.WITR, "Odd-numbered prayer after Isha", listOf("Pray after Isha and before Fajr.", "Learn the number and sequence taught in your school.", "Recite qunut where your school prescribes it; see the dua reference."), mapOf(Madhhab.HANAFI to "Wajib: three rak‘at with one closing salam; qunut in the third before ruku‘.", Madhhab.SHAFII to "Sunnah: at least one odd rak‘ah; qunut in the last rak‘ah in the latter half of Ramadan is a recognized practice.", Madhhab.MALIKI to "Sunnah: commonly one rak‘ah after two rak‘at of shaf‘.", Madhhab.HANBALI to "Sunnah: one or more odd rak‘at; qunut may be recited."), "Sunan Abi Dawud 1425"),
        SalahTopic("duha", "Duha", SalahKind.NAFL, "Optional forenoon prayer", listOf("Wait until after sunrise when the prohibited time has passed.", "Pray at least two rak‘at before midday.", "Offer additional pairs if you wish."), source = "Sahih Muslim 720"),
        SalahTopic("istikhara", "Istikhara", SalahKind.NAFL, "Seeking Allah's guidance for a permissible choice", listOf("Pray two voluntary rak‘at outside the prohibited times.", "Make the taught dua of istikhara and name the choice.", "Consult trustworthy people and proceed with the best available judgment."), source = "Sahih al-Bukhari 1166"),
        SalahTopic("kusuf", "Eclipse prayer", null, "Prayer during a solar or lunar eclipse", listOf("When an eclipse is observed, pray and remember Allah.", "The form of the prayer varies by school; join a knowledgeable congregation where possible."), source = "Sahih al-Bukhari 1041"),
        SalahTopic("istisqa", "Istisqa", null, "Prayer for rain", listOf("Turn to Allah in repentance and ask for rain.", "Join a community prayer and supplication led by a knowledgeable imam where available."), source = "Sahih al-Bukhari 1013"),
        SalahTopic("masjid", "Tahiyyatul Masjid", SalahKind.NAFL, "Greeting the mosque", listOf("On entering a mosque, pray two rak‘at before sitting when the time and circumstances allow.", "If the congregation has begun, join the Fard instead."), source = "Sahih al-Bukhari 444")
    )

    /** Generates each position for a specific Fard length. No UI copy is assembled in composables. */
    fun prayerSteps(rakats: Int, madhhab: Madhhab): List<GuideStep> {
        require(rakats in 2..4)
        val steps = mutableListOf(
            GuideStep("prepare", SalahText("Prepare"), SalahText("Have wudu, clean clothing and place, cover the awrah, face the Qibla and make the intention for this prayer.")),
            GuideStep("opening", SalahText("Opening takbir"), SalahText("Stand and say Allahu akbar to begin."), listOf("takbir"))
        )
        (1..rakats).forEach { unit ->
            val firstTwo = unit <= 2
            steps += GuideStep("recite_$unit", SalahText("Rak‘ah $unit · recite"), SalahText(if (firstTwo) "While standing, recite al-Fatihah and another surah or verses." else "While standing, recite al-Fatihah. In a Fard prayer, the additional surah is normally in the first two rak‘at."), if (firstTwo) listOf("fatiha", "ikhlas") else listOf("fatiha"), if (unit == 2 && rakats == 2) mapOf(Madhhab.SHAFII to "In Fajr, Shafi‘i practice includes qunut after rising from ruku‘ in this rak‘ah.") else emptyMap())
            steps += GuideStep("bow_$unit", SalahText("Rak‘ah $unit · bow"), SalahText("Say Allahu akbar, bow with hands on knees, then rise and stand upright."), listOf("ruku", "rise"))
            steps += GuideStep("prostrate_$unit", SalahText("Rak‘ah $unit · prostrate"), SalahText("Say Allahu akbar, prostrate, sit briefly, then prostrate a second time."), listOf("sujud", "between"))
            if (unit == 2 && rakats > 2) steps += GuideStep("middle", SalahText("First sitting"), SalahText("After the second rak‘ah, sit and recite the tashahhud, then rise for the next rak‘ah."), listOf("tashahhud"))
        }
        steps += GuideStep("final_sitting", SalahText("Final sitting"), SalahText("Sit after the last prostration; recite the tashahhud, blessings on the Prophet, and a personal dua."), listOf("tashahhud", "salawat"))
        steps += GuideStep("finish", SalahText("Finish with salam"), SalahText("Turn to the right and then left, offering the salam to complete the prayer."), listOf("salam"))
        // Passing the school here keeps guide generation school-aware as additional reviewed variations are added.
        return steps.map { step -> if (step.schoolNotes[madhhab] == null) step else step.copy(instruction = SalahText(step.instruction.en + " " + step.schoolNotes.getValue(madhhab))) }
    }
}
