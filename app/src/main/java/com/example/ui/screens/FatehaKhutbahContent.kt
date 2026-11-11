package com.example.ui.screens

import com.example.domain.model.Dua
import com.example.domain.model.DuaDatasetRepository

/*
 * Fateha (Isal-e-Sawab) and Jumu'ah khutbah guides.
 *
 * SCHOLAR REVIEW REQUIRED: the order of the Fateha steps, the non-Quranic khutbah wording, the
 * transliterations and the English meanings below were compiled for this app and have not yet been
 * approved by a qualified scholar. Every Quranic passage is a [RecitationPassage] whose Arabic text is
 * read at runtime from the bundled Tanzil dataset (HifzQuranTextRepository), never typed here.
 * Durood-e-Ibrahimi is reused from [DuaDatasetRepository] ("dua_salawat"). Hadith references are limited
 * to ones that are well established; do not add new references without checking the source.
 */

/** One step of a guide: either Quranic passages (text from the dataset) or a fixed recitation. */
internal sealed interface FkGuideStep {
    val id: String
    val title: String
    val repeat: Int
    val note: String?
}

internal data class FkQuranGuideStep(
    override val id: String,
    override val title: String,
    val passages: List<RecitationPassage>,
    override val repeat: Int = 1,
    override val note: String? = null
) : FkGuideStep

internal data class FkTextGuideStep(
    override val id: String,
    override val title: String,
    val arabic: String,
    val transliteration: String,
    /** English meaning, rendered through localizeUiText. */
    val meaning: String,
    override val repeat: Int = 1,
    override val note: String? = null,
    val source: String? = null
) : FkGuideStep

/** Durood-e-Ibrahimi from the app's dua dataset; its meaning comes from the dua translations. */
internal data class FkDuaGuideStep(
    override val id: String,
    override val title: String,
    val dua: Dua,
    override val repeat: Int = 1,
    override val note: String? = null
) : FkGuideStep

internal data class FkGuideSection(val heading: String, val steps: List<FkGuideStep>)

internal data class FkAdabItem(val title: String, val detail: String, val reference: String)

internal data class FkGuideContent(
    val screenTag: String,
    val title: String,
    val subtitle: String,
    val intro: String,
    val sections: List<FkGuideSection>,
    val adabHeading: String? = null,
    val adab: List<FkAdabItem> = emptyList(),
    val sources: String
) {
    val passages get() = sections.flatMap { it.steps }.filterIsInstance<FkQuranGuideStep>().flatMap { it.passages }
}

private fun durood(id: String, repeat: Int, note: String? = null): FkGuideStep? =
    DuaDatasetRepository.DUAS.firstOrNull { it.id == "dua_salawat" }
        ?.let { FkDuaGuideStep(id, "Durood Shareef", it, repeat, note) }

internal object FatehaGuide {
    val content: FkGuideContent by lazy {
        FkGuideContent(
            screenTag = "fateha",
            title = "Fateha (Isal-e-Sawab)",
            subtitle = "Step-by-step tareeqa with Quran text",
            intro = "Fateha is a voluntary (nafl) practice of reciting Quran and sending its reward to the deceased and to the righteous. Methods vary by region and family; this is one commonly practised order. Recite with sincerity; food is not a condition.",
            sections = listOf(
                FkGuideSection("Recitation", listOfNotNull(
                    durood("fateha_durood_start", 3, "Begin with Durood Shareef."),
                    FkQuranGuideStep("fateha_kafirun", "Surah Al-Kafirun", listOf(RecitationPassage(109, 1, 6))),
                    FkQuranGuideStep("fateha_ikhlas", "Surah Al-Ikhlas", listOf(RecitationPassage(112, 1, 4)), repeat = 3),
                    FkQuranGuideStep("fateha_falaq", "Surah Al-Falaq", listOf(RecitationPassage(113, 1, 5))),
                    FkQuranGuideStep("fateha_nas", "Surah An-Nas", listOf(RecitationPassage(114, 1, 6))),
                    FkQuranGuideStep("fateha_fatiha", "Surah Al-Fatiha", listOf(RecitationPassage(1, 1, 7))),
                    FkQuranGuideStep("fateha_baqarah", "Opening of Surah Al-Baqarah", listOf(RecitationPassage(2, 1, 5))),
                    FkQuranGuideStep("fateha_selected", "Selected ayahs", listOf(
                        RecitationPassage(2, 163, 163), RecitationPassage(21, 107, 107),
                        RecitationPassage(33, 40, 40), RecitationPassage(33, 56, 56),
                        RecitationPassage(37, 180, 182)
                    ), note = "Many recite these ayahs before the closing Durood; some add others."),
                    durood("fateha_durood_end", 3, "Close the recitation with Durood Shareef.")
                )),
                FkGuideSection("Dua of Isal-e-Sawab", listOf(
                    FkTextGuideStep(
                        id = "fateha_isal_dua",
                        title = "Dua of Isal-e-Sawab",
                        arabic = "",
                        transliteration = "",
                        meaning = "O Allah, accept what we have recited by Your grace, and convey its reward first to the blessed soul of the Prophet Muhammad ﷺ, then to his family and Companions, to all the awliya and righteous, and to (name of the deceased) and all believing men and women. Forgive them and have mercy on them.",
                        note = "Raise your hands and make this dua in any language; name the people you wish to send the reward to."
                    )
                ))
            ),
            sources = "Durood-e-Ibrahimi: Sahih al-Bukhari 3370. The order of recitation follows common South Asian practice and needs scholar review."
        )
    }
}

internal object KhutbahGuide {
    private const val HAMD_ARABIC = "اَلْحَمْدُ لِلّٰهِ نَحْمَدُهٗ وَنَسْتَعِيْنُهٗ وَنَسْتَغْفِرُهٗ وَنُؤْمِنُ بِهٖ وَنَتَوَكَّلُ عَلَيْهِ، وَنَعُوْذُ بِاللّٰهِ مِنْ شُرُوْرِ اَنْفُسِنَا وَمِنْ سَيِّاٰتِ اَعْمَالِنَا، مَنْ يَّهْدِهِ اللّٰهُ فَلَا مُضِلَّ لَهٗ وَمَنْ يُّضْلِلْهُ فَلَا هَادِيَ لَهٗ، وَنَشْهَدُ اَنْ لَّا اِلٰهَ اِلَّا اللّٰهُ وَحْدَهٗ لَا شَرِيْكَ لَهٗ، وَنَشْهَدُ اَنَّ سَيِّدَنَا وَمَوْلَانَا مُحَمَّدًا عَبْدُهٗ وَرَسُوْلُهٗ"
    private const val HAMD_TRANSLIT = "Al-ḥamdu lillāhi naḥmaduhū wa nasta'īnuhū wa nastaghfiruhū wa nu'minu bihī wa natawakkalu 'alayh, wa na'ūdhu billāhi min shurūri anfusinā wa min sayyi'āti a'mālinā, man yahdihillāhu falā muḍilla lah, wa man yuḍlilhu falā hādiya lah, wa nashhadu an lā ilāha illallāhu waḥdahū lā sharīka lah, wa nashhadu anna sayyidanā wa mawlānā Muḥammadan 'abduhū wa rasūluh"
    private const val HAMD_MEANING = "All praise is for Allah. We praise Him, seek His help and His forgiveness, believe in Him and rely on Him. We seek refuge in Allah from the evils of our souls and our bad deeds. Whom Allah guides, none can misguide; whom He leaves astray, none can guide. We bear witness that there is no god but Allah, alone without partner, and that our master Muhammad ﷺ is His servant and Messenger."

    val content: FkGuideContent by lazy {
        FkGuideContent(
            screenTag = "khutbah",
            title = "Jumu'ah Khutbah",
            subtitle = "Arabic Friday khutbah with meaning",
            intro = "The Friday khutbah is delivered in two parts with a short sitting between them. Below is a commonly used Arabic text in South Asian masjids; imams often add other ayahs and advice.",
            sections = listOf(
                FkGuideSection("First khutbah", listOf(
                    FkTextGuideStep("khutbah1_hamd", "Praise and testimony", HAMD_ARABIC, HAMD_TRANSLIT, HAMD_MEANING),
                    FkTextGuideStep(
                        "khutbah1_amma_bad", "Amma ba'd",
                        "اَمَّا بَعْدُ! فَاَعُوْذُ بِاللّٰهِ مِنَ الشَّيْطٰنِ الرَّجِيْمِ، بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ",
                        "Ammā ba'd, fa a'ūdhu billāhi minash-shayṭānir-rajīm, bismillāhir-raḥmānir-raḥīm",
                        "Then after that: I seek refuge in Allah from Satan the accursed. In the name of Allah, the Most Gracious, the Most Merciful."
                    ),
                    FkQuranGuideStep("khutbah1_ayah", "Ayah of taqwa", listOf(RecitationPassage(3, 102, 102)),
                        note = "Imams recite different ayahs here according to the topic."),
                    FkTextGuideStep(
                        "khutbah1_close", "Closing of the first khutbah",
                        "بَارَكَ اللّٰهُ لَنَا وَلَكُمْ فِي الْقُرْاٰنِ الْعَظِيْمِ، وَنَفَعَنَا وَاِيَّاكُمْ بِالْاٰيٰتِ وَالذِّكْرِ الْحَكِيْمِ",
                        "Bārakallāhu lanā wa lakum fil-Qur'ānil-'aẓīm, wa nafa'anā wa iyyākum bil-āyāti wadh-dhikril-ḥakīm",
                        "May Allah bless us and you through the Mighty Quran, and benefit us and you by its verses and wise reminder.",
                        note = "The imam then sits briefly; this is a time to make dua silently."
                    )
                )),
                FkGuideSection("Second khutbah", listOf(
                    FkTextGuideStep("khutbah2_hamd", "Praise and testimony", HAMD_ARABIC, HAMD_TRANSLIT, HAMD_MEANING),
                    FkQuranGuideStep("khutbah2_salawat_ayah", "Ayah of Durood", listOf(RecitationPassage(33, 56, 56))),
                    FkTextGuideStep(
                        "khutbah2_salawat", "Durood and remembrance of the Companions",
                        "اَللّٰهُمَّ صَلِّ عَلٰى سَيِّدِنَا مُحَمَّدٍ وَّعَلٰى اٰلِهٖ وَاَصْحَابِهٖ اَجْمَعِيْنَ، وَارْضَ اللّٰهُمَّ عَنِ الْخُلَفَاءِ الرَّاشِدِيْنَ اَبِيْ بَكْرٍ وَّعُمَرَ وَعُثْمَانَ وَعَلِيٍّ، وَعَنْ سَائِرِ الصَّحَابَةِ وَاَهْلِ الْبَيْتِ اَجْمَعِيْنَ",
                        "Allāhumma ṣalli 'alā sayyidinā Muḥammadin wa 'alā ālihī wa aṣḥābihī ajma'īn, warḍallāhumma 'anil-khulafā'ir-rāshidīna Abī Bakrin wa 'Umara wa 'Uthmāna wa 'Aliyy, wa 'an sā'iriṣ-ṣaḥābati wa ahlil-bayti ajma'īn",
                        "O Allah, send blessings upon our master Muhammad ﷺ, his family and all his Companions. O Allah, be pleased with the rightly guided caliphs Abu Bakr, Umar, Uthman and Ali, and with all the Companions and the Prophet's household."
                    ),
                    FkTextGuideStep(
                        "khutbah2_ibad", "Address to the servants of Allah",
                        "عِبَادَ اللّٰهِ! رَحِمَكُمُ اللّٰهُ",
                        "'Ibādallāh, raḥimakumullāh",
                        "Servants of Allah, may Allah have mercy on you."
                    ),
                    FkQuranGuideStep("khutbah2_ayah", "Ayah of justice and kindness", listOf(RecitationPassage(16, 90, 90))),
                    FkTextGuideStep(
                        "khutbah2_close", "Closing of the second khutbah",
                        "اُذْكُرُوا اللّٰهَ يَذْكُرْكُمْ، وَادْعُوْهُ يَسْتَجِبْ لَكُمْ، وَلَذِكْرُ اللّٰهِ اَكْبَرُ",
                        "Udhkurullāha yadhkurkum, wad'ūhu yastajib lakum, wa ladhikrullāhi akbar",
                        "Remember Allah and He will remember you; call upon Him and He will answer you; and the remembrance of Allah is the greatest.",
                        note = "The iqamah follows and the two rak'at of Jumu'ah are prayed."
                    )
                ))
            ),
            adabHeading = "Sunnah and aadaab of Jumu'ah",
            adab = listOf(
                FkAdabItem("Take a bath (ghusl)", "Bathe, clean yourself, use oil or perfume and wear clean clothes before going to Jumu'ah.", "Sahih al-Bukhari 877, 883"),
                FkAdabItem("Go early", "The one who comes earliest earns the most reward; the angels stop recording arrivals once the imam comes out.", "Sahih al-Bukhari 881"),
                FkAdabItem("Listen silently", "Do not talk during the khutbah, not even to tell someone to be quiet.", "Sahih al-Bukhari 934"),
                FkAdabItem("Leave trade at the call", "When the call for Jumu'ah is given, hurry to the remembrance of Allah and leave buying and selling.", "Quran 62:9"),
                FkAdabItem("Recite Surah Al-Kahf", "Reciting Surah Al-Kahf on Friday brings light between the two Fridays.", "al-Hakim, al-Mustadrak; al-Bayhaqi"),
                FkAdabItem("Send abundant Durood", "Send much Durood on the Prophet ﷺ on Friday, for it is presented to him.", "Sunan Abi Dawud 1047")
            ),
            sources = "Khutbah wording follows texts commonly used in South Asian masjids and needs scholar review. Hadith references are shown on each item."
        )
    }
}
