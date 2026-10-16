package com.example.ui.screens

/**
 * Offline Hajj & Umrah guide content and the small bits of logic the screen needs
 * (step order per Hajj type, round counters, progress). Rulings follow Hanafi fiqh
 * (Ahle Sunnat wal Jamaat); every English string here is a translation key.
 */

internal enum class RiteRuling(val label: String) {
    SHART("Condition"), FARD("Fard"), WAJIB("Wajib"), SUNNAH("Sunnah")
}

internal enum class HajjType(val label: String, val summary: String) {
    TAMATTU(
        "Tamattu'",
        "Umrah first in the months of Hajj, leave ihram, then a fresh ihram for Hajj on the 8th. Qurbani is wajib."
    ),
    QIRAN(
        "Qiran",
        "One ihram for Umrah and Hajj together, kept on until Halq on the 10th. Qurbani is wajib."
    ),
    IFRAD(
        "Ifrad",
        "Ihram for Hajj only, without Umrah. Qurbani is recommended, not wajib."
    );

    companion object {
        fun fromKey(key: String?): HajjType = entries.firstOrNull { it.name == key } ?: TAMATTU
    }
}

internal data class RiteDua(
    val arabic: String,
    val meaning: String,
    val reference: String,
    /** Romanised reading for pilgrims who cannot read Arabic yet; devotional, not translated. */
    val transliteration: String = ""
)

internal enum class CounterKind(val total: Int, val unit: String, val countLabel: String) {
    TAWAF(7, "Round", "Count round"),
    SAI(7, "Round", "Count round"),
    RAMI_AQABAH(7, "Pebble", "Count pebble"),
    RAMI_THREE(21, "Pebble", "Count pebble")
}

internal data class RiteStep(
    val id: String,
    val badge: String?,
    val title: String,
    val detail: String,
    val ruling: RiteRuling?,
    val points: List<String> = emptyList(),
    val dua: RiteDua? = null,
    val counter: CounterKind? = null
)

/** What the pilgrim should be doing at a given count, e.g. which direction of Sa'i or which Jamrah. */
internal data class CounterStage(val hint: String, val subCount: Int, val subTotal: Int)

internal fun counterStage(kind: CounterKind, done: Int): CounterStage {
    val count = done.coerceIn(0, kind.total)
    return when (kind) {
        CounterKind.TAWAF -> CounterStage(
            when {
                count >= 7 -> "Tawaf complete. Now pray the two rakat."
                count < 3 -> "Rounds 1–3: men do Raml only in a Tawaf followed by Sa'i"
                else -> "Walk at a normal pace"
            },
            count, 7
        )
        CounterKind.SAI -> CounterStage(
            when {
                count >= 7 -> "Sa'i complete. It ends at Marwah."
                count % 2 == 0 -> "Safa → Marwah"
                else -> "Marwah → Safa"
            },
            count, 7
        )
        CounterKind.RAMI_AQABAH -> CounterStage(
            if (count >= 7) "All seven pebbles thrown" else "Jamrat al-Aqabah (big)",
            count, 7
        )
        CounterKind.RAMI_THREE -> {
            val jamrah = minOf(count / 7, 2)
            val hint = when (count) {
                21 -> "All three Jamarat done for today"
                14 -> "Make dua facing the Qibla, then go to the Aqabah"
                7 -> "Make dua facing the Qibla, then go to the Wusta"
                else -> listOf("Jamrah Ula (small)", "Jamrah Wusta (middle)", "Jamrat al-Aqabah (big)")[jamrah]
            }
            // At a boundary (7, 14) the pilgrim has just finished a Jamrah, so show it full.
            val sub = if (count == 21 || (count > 0 && count % 7 == 0)) 7 else count % 7
            CounterStage(hint, sub, 7)
        }
    }
}

internal fun nextPendingStep(steps: List<RiteStep>, done: Set<String>): RiteStep? =
    steps.firstOrNull { it.id !in done }

// ---------------------------------------------------------------- Duas used by the steps

internal const val TALBIYAH_ARABIC =
    "لَبَّيْكَ اللَّهُمَّ لَبَّيْكَ، لَبَّيْكَ لَا شَرِيكَ لَكَ لَبَّيْكَ، إِنَّ الْحَمْدَ وَالنِّعْمَةَ لَكَ وَالْمُلْكَ، لَا شَرِيكَ لَكَ"

private val talbiyah = RiteDua(
    TALBIYAH_ARABIC,
    "Here I am, O Allah, here I am. Here I am, You have no partner, here I am. All praise, blessings and sovereignty belong to You. You have no partner.",
    "Sahih al-Bukhari 1549 · Sahih Muslim 1184",
    "Labbayk Allāhumma labbayk, labbayka lā sharīka laka labbayk, inna al-ḥamda wan-niʿmata laka wal-mulk, lā sharīka lak"
)

private val tawafDua = RiteDua(
    "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
    "Our Lord, give us good in this world and good in the Hereafter, and save us from the punishment of the Fire. (Between Rukn Yamani and the Black Stone)",
    "Sunan Abi Dawud 1892",
    "Rabbanā ātinā fid-dunyā ḥasanatan wa fil-ākhirati ḥasanatan wa qinā ʿadhāban-nār"
)

private val maqamAyah = RiteDua(
    "وَاتَّخِذُوا مِن مَّقَامِ إِبْرَاهِيمَ مُصَلًّى",
    "And take the Station of Ibrahim as a place of prayer.",
    "Quran 2:125 · Sahih Muslim 1218",
    "Wattakhidhū min maqāmi Ibrāhīma muṣallā"
)

private val safaAyah = RiteDua(
    "إِنَّ الصَّفَا وَالْمَرْوَةَ مِن شَعَائِرِ اللَّهِ",
    "Indeed, Safa and Marwah are among the symbols of Allah. (Recited on first reaching Safa)",
    "Quran 2:158 · Sahih Muslim 1218",
    "Inna aṣ-Ṣafā wal-Marwata min shaʿāʾirillāh"
)

private val arafahDua = RiteDua(
    "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
    "None has the right to be worshipped but Allah alone, without partner. His is the dominion and His is the praise, and He has power over all things.",
    "Jami' at-Tirmidhi 3585",
    "Lā ilāha illallāhu waḥdahu lā sharīka lah, lahul-mulku wa lahul-ḥamd, wa huwa ʿalā kulli shayʾin qadīr"
)

private val muzdalifahAyah = RiteDua(
    "فَإِذَا أَفَضْتُم مِّنْ عَرَفَاتٍ فَاذْكُرُوا اللَّهَ عِندَ الْمَشْعَرِ الْحَرَامِ",
    "When you depart from Arafat, remember Allah at al-Mash'ar al-Haram.",
    "Quran 2:198",
    "Fa idhā afaḍtum min ʿArafātin fadhkurullāha ʿindal-Mashʿaril-Ḥarām"
)

private val ramiTakbir = RiteDua(
    "اللَّهُ أَكْبَرُ",
    "Allah is the Greatest. (With every pebble)",
    "Sahih al-Bukhari 1751",
    "Allāhu Akbar"
)

private val ziyarahAyah = RiteDua(
    "وَلْيَطَّوَّفُوا بِالْبَيْتِ الْعَتِيقِ",
    "And let them circle the Ancient House.",
    "Quran 22:29",
    "Walyaṭṭawwafū bil-baytil-ʿatīq"
)

// ---------------------------------------------------------------- Umrah

internal val umrahSteps = listOf(
    RiteStep(
        "ihram", null, "Ihram",
        "Before crossing the miqat: ghusl, wear the ihram, pray two rakat, make the niyyah and recite the Talbiyah.",
        RiteRuling.SHART,
        listOf(
            "Men wear two plain unstitched sheets; women wear their normal modest clothes.",
            "Pray two rakat nafl with the head covered, outside the makruh times.",
            "Uncover the head, make the niyyah of Umrah and say the Talbiyah. Ihram begins with niyyah and Talbiyah together.",
            "Keep reciting the Talbiyah until you begin the Tawaf."
        ),
        talbiyah
    ),
    RiteStep(
        "tawaf", null, "Tawaf",
        "Seven circuits around the Kaaba, starting and ending at the Black Stone, with the Kaaba on your left.",
        RiteRuling.FARD,
        listOf(
            "Wudu is required for Tawaf.",
            "Men: Idtiba throughout this Tawaf — the upper sheet under the right arm, right shoulder bare.",
            "Begin at the Black Stone line: kiss it, touch it, or simply raise your palms towards it and say Bismillah, Allahu Akbar.",
            "Men do Raml (brisk short steps) in the first three rounds.",
            "Go around the Hatim, never through it, and keep your chest from turning towards the Kaaba."
        ),
        tawafDua,
        CounterKind.TAWAF
    ),
    RiteStep(
        "rakat", null, "Two rakat of Tawaf",
        "Pray behind Maqam Ibrahim if possible, otherwise anywhere in the Haram, then drink Zamzam.",
        RiteRuling.WAJIB,
        listOf(
            "Cover both shoulders again before praying.",
            "Sunnah: Surah al-Kafirun in the first rakah and al-Ikhlas in the second.",
            "Make dua at the Multazam, then drink Zamzam standing, facing the Qibla."
        ),
        maqamAyah
    ),
    RiteStep(
        "sai", null, "Sa'i",
        "Seven rounds between Safa and Marwah, starting at Safa and ending at Marwah.",
        RiteRuling.WAJIB,
        listOf(
            "Safa to Marwah is one round; Marwah back to Safa is the second.",
            "On Safa and Marwah face the Kaaba, raise your hands as in dua, and praise Allah.",
            "Men jog lightly between the green lights; women walk normally.",
            "Wudu is recommended for Sa'i but not required."
        ),
        safaAyah,
        CounterKind.SAI
    ),
    RiteStep(
        "halq", null, "Halq or Qasr",
        "Men shave or trim the head; women trim a fingertip's length. The ihram restrictions are lifted.",
        RiteRuling.WAJIB,
        listOf(
            "Shaving is better for men: the Prophet ﷺ prayed three times for those who shave.",
            "Trimming must cover at least a quarter of the head, a fingertip's length; the whole head is better.",
            "Do it inside the Haram. Your Umrah is complete — may Allah accept it."
        )
    )
)

// ---------------------------------------------------------------- Hajj

internal fun hajjSteps(type: HajjType): List<RiteStep> {
    val opening = when (type) {
        HajjType.TAMATTU -> listOf(
            RiteStep(
                "t_umrah", "Before 8 Dhul Hijjah", "Umrah of Tamattu'",
                "Enter ihram at the miqat with the niyyah of Umrah, perform the full Umrah, then shave or trim and leave ihram. Stay in Makkah until the 8th.",
                RiteRuling.FARD,
                listOf("Follow the Umrah tab step by step.", "Keep away from sin; use these days for Tawaf, Quran and dua."),
                talbiyah
            )
        )
        HajjType.QIRAN -> listOf(
            RiteStep(
                "q_ihram", "At the miqat", "Ihram of Qiran",
                "Make the niyyah of both Umrah and Hajj together and recite the Talbiyah. This ihram stays on until Halq on the 10th.",
                RiteRuling.SHART,
                emptyList(),
                talbiyah
            ),
            RiteStep(
                "q_umrah", "On reaching Makkah", "Umrah Tawaf and Sa'i, then Tawaf al-Qudum",
                "Perform the Tawaf and Sa'i of Umrah but do not shave. Then perform Tawaf al-Qudum; you may do the Sa'i of Hajj after it.",
                RiteRuling.FARD,
                listOf("Stay in ihram and keep reciting the Talbiyah after the Umrah."),
                tawafDua,
                CounterKind.TAWAF
            )
        )
        HajjType.IFRAD -> listOf(
            RiteStep(
                "i_ihram", "At the miqat", "Ihram of Ifrad",
                "Make the niyyah of Hajj only and recite the Talbiyah. This ihram stays on until Halq on the 10th.",
                RiteRuling.SHART,
                emptyList(),
                talbiyah
            ),
            RiteStep(
                "i_qudum", "On reaching Makkah", "Tawaf al-Qudum",
                "The arrival Tawaf, Sunnah for pilgrims from outside the miqat. You may do the Sa'i of Hajj after it.",
                RiteRuling.SUNNAH,
                listOf("If you will do Sa'i after it, men do Raml and Idtiba in this Tawaf."),
                tawafDua,
                CounterKind.TAWAF
            )
        )
    }
    val withQurbani = type != HajjType.IFRAD
    return opening + listOf(
        RiteStep(
            "mina", "8 Dhul Hijjah", "Mina",
            if (type == HajjType.TAMATTU) "Put on ihram in Makkah with the niyyah of Hajj, recite the Talbiyah and go to Mina. Pray Dhuhr to the next Fajr there."
            else "Stay in your ihram and go to Mina with the Talbiyah. Pray Dhuhr to the next Fajr there.",
            RiteRuling.SUNNAH,
            listOf("Staying in Mina on the 8th and the night of the 9th is Sunnah.", "Recite the Talbiyah often."),
            talbiyah
        ),
        RiteStep(
            "arafat", "9 Dhul Hijjah", "Wuquf at Arafat",
            "Wuquf at Arafat from after midday until sunset, the essential pillar of Hajj. Spend it in dua.",
            RiteRuling.FARD,
            listOf(
                "Being in Arafat for any moment from zawal on the 9th until dawn of the 10th fulfils the fard; staying until sunset is wajib.",
                "Hanafi: Dhuhr and Asr are combined only behind the Imam of Hajj at Masjid Namirah. In your tent, pray each at its own time.",
                "Stay inside the boundary markers of Arafat.",
                "Spend the afternoon standing in dua, istighfar and Talbiyah, facing the Qibla.",
                "Leaving Arafat before sunset makes a Dam due, unless you return before sunset."
            ),
            arafahDua
        ),
        RiteStep(
            "muzdalifah", "Night of the 10th", "Muzdalifah",
            "Leave Arafat after sunset without praying Maghrib. At Muzdalifah pray Maghrib and Isha together in Isha time and spend the night there.",
            RiteRuling.WAJIB,
            listOf(
                "The wajib wuquf is between dawn and sunrise on the 10th: pray Fajr early and stay in dhikr.",
                "Pick up chickpea-sized pebbles: 49 in all, or 70 if you will stay for the 13th.",
                "The sick, elderly and weak may leave after midnight because of the crowd, with no penalty."
            ),
            muzdalifahAyah
        ),
        RiteStep(
            "day10", "10 Dhul Hijjah",
            if (withQurbani) "Rami, Qurbani, Halq" else "Rami and Halq",
            "Stone only Jamrat al-Aqabah (the big one) with seven pebbles, one at a time. Stop the Talbiyah with the first pebble.",
            RiteRuling.WAJIB,
            listOfNotNull(
                if (withQurbani) "Then offer the Qurbani (Dam-e-Shukr), then shave or trim. Hanafi: this order is wajib; breaking it makes a Dam due."
                else "Then shave or trim. Qurbani is recommended for Ifrad; if you offer it, do so before Halq.",
                "The Sunnah time for this Rami is from sunrise until zawal.",
                "After Halq everything forbidden in ihram becomes lawful except marital relations, until Tawaf al-Ziyarah."
            ),
            ramiTakbir,
            CounterKind.RAMI_AQABAH
        ),
        RiteStep(
            "ziyarah", "10–12 Dhul Hijjah", "Tawaf al-Ziyarah",
            "The obligatory Tawaf of Hajj. Perform it after Halq, best on the 10th, and before sunset on the 12th.",
            RiteRuling.FARD,
            listOf(
                if (type == HajjType.TAMATTU) "Then perform the Sa'i of Hajj between Safa and Marwah."
                else "Perform the Sa'i of Hajj now if you did not do it after Tawaf al-Qudum.",
                "Delaying it past the 12th without excuse makes a Dam due. A woman in her period waits until she is pure, with no penalty.",
                "After this Tawaf, marital relations also become lawful."
            ),
            ziyarahAyah,
            CounterKind.TAWAF
        ),
        RiteStep(
            "tashriq", "11–12 Dhul Hijjah", "Rami of the three Jamarat",
            "Spend the days in Mina. Each day after zawal stone all three — Ula, Wusta, then Aqabah — with seven pebbles each.",
            RiteRuling.WAJIB,
            listOf(
                "After the Ula and the Wusta, step aside, face the Qibla and make a long dua. Do not stop for dua after the Aqabah.",
                "You may leave Mina on the 12th before sunset. If you are still in Mina at dawn on the 13th, the Rami of the 13th becomes wajib.",
                "The sick and the weak who cannot reach the Jamarat may appoint someone to stone for them.",
                "Reset the counter each day."
            ),
            ramiTakbir,
            CounterKind.RAMI_THREE
        ),
        RiteStep(
            "wida", "Before leaving Makkah", "Farewell Tawaf",
            "Tawaf al-Wida, wajib for pilgrims from outside the miqat. Make it your last act in Makkah.",
            RiteRuling.WAJIB,
            listOf(
                "No Raml, Idtiba or Sa'i in this Tawaf.",
                "A woman in her period is excused from it.",
                "Drink Zamzam, make dua at the Multazam and leave with a heart longing to return."
            ),
            tawafDua,
            CounterKind.TAWAF
        )
    )
}

// ---------------------------------------------------------------- Essentials

internal enum class GuideTone { DO, DONT, INFO }

internal data class GuideLine(val text: String, val tone: GuideTone)

internal data class GuideSection(
    val id: String,
    val title: String,
    val summary: String,
    val lines: List<GuideLine>,
    val reference: String
)

private fun dont(text: String) = GuideLine(text, GuideTone.DONT)
private fun allowed(text: String) = GuideLine(text, GuideTone.DO)
private fun info(text: String) = GuideLine(text, GuideTone.INFO)

internal val pillarsSection = GuideSection(
    "pillars", "Fard and Wajib", "What makes Hajj and Umrah valid",
    listOf(
        info("Hajj fard: Ihram, Wuquf at Arafat, Tawaf al-Ziyarah. Missing one cannot be made up by Dam."),
        info("Hajj wajib: Muzdalifah, Sa'i, Rami, Qurbani for Qiran and Tamattu', Halq or Qasr, Tawaf al-Wida."),
        info("Umrah: Ihram is the condition and Tawaf is fard; Sa'i and Halq or Qasr are wajib."),
        info("Missing a wajib without a valid excuse makes a Dam due: one goat or sheep slaughtered within the Haram."),
        info("Crossing the miqat towards Makkah without ihram makes a Dam due, unless you go back and enter ihram there."),
        info("The madhhabs differ on some details, such as combining prayers at Arafat and the order of the 10th's rites. Follow your own scholar.")
    ),
    "Al-Hidayah, Kitab al-Hajj · Bahar-e-Shariat, Hissa 6"
)

internal val ihramSection = GuideSection(
    "ihram_rules", "Rules of ihram", "What is forbidden and what is allowed",
    listOf(
        dont("Perfume or scented soap on the body or the ihram"),
        dont("Cutting or removing hair or nails"),
        dont("Marital relations and anything leading to them"),
        dont("Hunting, or helping someone hunt"),
        dont("Arguing, fighting and sinful speech"),
        dont("Covering the face, for men and women"),
        dont("Men: stitched clothes, covering the head, and shoes that cover the raised bone on top of the foot"),
        dont("Women: gloves, or a veil touching the face — a cap visor keeps it off the face"),
        allowed("Bathing without scented soap"),
        allowed("Changing or washing the ihram sheets"),
        allowed("An umbrella, belt, wallet, watch and glasses"),
        allowed("Women: normal stitched clothes, socks and shoes")
    ),
    "Quran 2:196–197 · Quran 5:95 · Sahih al-Bukhari 1542, 1838"
)

internal val womenSection = GuideSection(
    "women", "For women", "Masail specific to sisters",
    listOf(
        info("A mahram must accompany a woman on the journey."),
        info("Recite the Talbiyah quietly."),
        info("No Raml, no Idtiba and no jogging between the green lights."),
        info("Trim a fingertip's length from the ends of the hair; never shave."),
        info("During menses do every rite except Tawaf and its two rakat."),
        info("Tawaf al-Wida is waived for a woman in her period.")
    ),
    "Sahih al-Bukhari 1862, 305, 1755"
)

internal val mistakesSection = GuideSection(
    "mistakes", "Common mistakes", "Protect your Hajj and Umrah",
    listOf(
        dont("Keeping the right shoulder bare the whole time — Idtiba is only during Tawaf"),
        dont("Pushing or hurting people to kiss the Black Stone — pointing is enough"),
        dont("Walking through the Hatim — it is part of the Kaaba"),
        dont("Doing Tawaf without wudu"),
        dont("Leaving Arafat before sunset"),
        dont("Throwing large stones, shoes, or all seven pebbles at once"),
        dont("Spending precious time on the phone instead of dua"),
        dont("Starting Sa'i at Marwah, or counting Safa to Marwah and back as one round"),
        dont("Praying the two rakat of Tawaf at a makruh time — wait until it passes"),
        dont("Leaving the ihram by cutting only a few hairs"),
        dont("Assuming the Tawaf counter is right after a break — if unsure, build on the lower number")
    ),
    "Bahar-e-Shariat, Hissa 6"
)

internal val essentialSections = listOf(pillarsSection, ihramSection, womenSection, mistakesSection)

internal data class PrepItem(val id: String, val text: String)

internal data class PrepGroup(val title: String, val items: List<PrepItem>)

internal val preparationChecklist = listOf(
    PrepGroup(
        "Heart and rights",
        listOf(
            PrepItem("tauba", "Sincere tauba and a pure niyyah"),
            PrepItem("debts", "Repay debts and settle others' rights"),
            PrepItem("wasiyyat", "Write your wasiyyat (will)"),
            PrepItem("halal", "Use only halal money for the journey"),
            PrepItem("masail", "Learn the masail from a Sunni scholar")
        )
    ),
    PrepGroup(
        "Documents",
        listOf(
            PrepItem("passport", "Passport, visa and Hajj or Umrah permit"),
            PrepItem("vaccine", "Meningitis (ACWY) vaccination certificate"),
            PrepItem("contacts", "Group leader and hotel contacts, written down")
        )
    ),
    PrepGroup(
        "Packing",
        listOf(
            PrepItem("ihram_sets", "Two sets of ihram (men)"),
            PrepItem("unscented", "Unscented soap, shampoo and deodorant"),
            PrepItem("belt", "Ihram belt and sandals that leave the top of the foot bare"),
            PrepItem("pouch", "Small pouch for pebbles"),
            PrepItem("medicine", "Personal medicines and prescriptions"),
            PrepItem("umbrella", "Umbrella, water bottle and power bank"),
            PrepItem("prayer_mat", "Light prayer mat and a small Quran or dua book"),
            PrepItem("id_card", "ID card or wristband from your group, worn at all times"),
            PrepItem("sun", "Sunscreen without perfume, and comfortable broken-in footwear"),
            PrepItem("copies", "Photocopies of passport and visa, kept apart from the originals")
        )
    )
)

internal data class MadinahPlace(val title: String, val detail: String, val reference: String)

internal const val MADINAH_SALAM_ARABIC = "اَلصَّلَاةُ وَالسَّلَامُ عَلَيْكَ يَا رَسُولَ اللَّهِ"

internal val madinahPlaces = listOf(
    MadinahPlace(
        "Masjid an-Nabawi",
        "One prayer here is better than a thousand elsewhere, except Masjid al-Haram.",
        "Sahih al-Bukhari 1190"
    ),
    MadinahPlace(
        "Riyad al-Jannah",
        "Between the blessed home and the minbar is a garden of Paradise.",
        "Sahih al-Bukhari 1196"
    ),
    MadinahPlace(
        "Masjid Quba",
        "Prayer in Masjid Quba carries the reward of an Umrah.",
        "Jami' at-Tirmidhi 324"
    ),
    MadinahPlace(
        "Jannat al-Baqi and Uhud",
        "Visit and pray for the Ahl al-Bayt, the Sahaba and the martyrs, as the Prophet ﷺ did.",
        "Sahih Muslim 974"
    )
)
