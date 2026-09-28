package com.example.domain.model

/**
 * Morning and evening adhkar in the order they are usually recited. Every entry is a referenced
 * dua from [DuaDatasetRepository]; how many times to recite it is the dua's recommendedCount.
 */
object AdhkarDatasetRepository {

    private val MORNING_IDS = listOf(
        "dua_muawwidhat",
        "dua_morning_asbahna",
        "dua_allahumma_bika_asbahna",
        "dua_sayyidul_istighfar",
        "dua_afini_fi_badani",
        "dua_kufr_faqr",
        "dua_afiyah_morning",
        "dua_alimal_ghayb",
        "dua_bismillah_la_yadurru",
        "dua_raditu_billahi",
        "dua_ya_hayyu_aslih",
        "dua_fitratil_islam",
        "dua_hasbiyallah",
        "dua_subhanallah_adada_khalqihi",
        "dua_tahleel_10_fajr",
        "dua_subhanallah_wa_bihamdihi",
        "dua_morning_witness",
        "dua_morning_blessing"
    )

    private val EVENING_IDS = listOf(
        "dua_muawwidhat",
        "dua_evening_amsayna",
        "dua_allahumma_bika_amsayna",
        "dua_sayyidul_istighfar",
        "dua_afini_fi_badani",
        "dua_kufr_faqr",
        "dua_afiyah_morning",
        "dua_alimal_ghayb",
        "dua_bismillah_la_yadurru",
        "dua_raditu_billahi",
        "dua_ya_hayyu_aslih",
        "dua_amsayna_fitrah",
        "dua_hasbiyallah",
        "dua_kalimat_tammat",
        "dua_tahleel_10_fajr",
        "dua_subhanallah_wa_bihamdihi",
        "dua_evening_witness",
        "dua_evening_blessing"
    )

    val MORNING_ADHKAR: List<Dua> by lazy { MORNING_IDS.map(::requireDua) }
    val EVENING_ADHKAR: List<Dua> by lazy { EVENING_IDS.map(::requireDua) }

    private fun requireDua(id: String): Dua =
        DuaDatasetRepository.byId(id) ?: error("Adhkar entry $id is missing from the dua dataset")
}

/** A dhikr for the digital tasbeeh, with the hadith that gives its count or virtue. */
data class TasbeehPreset(
    val transliteration: String,
    val arabic: String,
    val goal: Int,
    val source: String
) {
    /** The persisted tasbeeh phrase; kept in the format earlier app versions saved. */
    val phrase: String get() = "$transliteration ($arabic)"

    companion object {
        val ALL = listOf(
            TasbeehPreset("Subhan Allah", "سُبْحَانَ اللَّهِ", 33, "Sahih al-Bukhari 6318; Sahih Muslim 2727"),
            TasbeehPreset("Alhamdulillah", "الْحَمْدُ لِلَّهِ", 33, "Sahih al-Bukhari 6318; Sahih Muslim 2727"),
            TasbeehPreset("Allahu Akbar", "اللَّهُ أَكْبَرُ", 34, "Sahih al-Bukhari 6318; Sahih Muslim 2727"),
            TasbeehPreset("Astaghfirullah", "أَسْتَغْفِرُ اللَّهَ", 100, "Sahih Muslim 2702"),
            TasbeehPreset("La ilaha illallah", "لَا إِلَٰهَ إِلَّا اللَّهُ", 100, "Jami` at-Tirmidhi 3383")
        ) + MoreTasbeehPresets.LIST

        fun forPhrase(phrase: String): TasbeehPreset? = ALL.firstOrNull { it.phrase == phrase }
    }
}
