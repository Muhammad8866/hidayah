package com.example.ui.screens

import com.example.domain.model.DuaDatasetRepository
import com.example.quran.FullQuranCatalog

data class RecitationPassage(val surah: Int, val first: Int, val last: Int) {
    init {
        val chapter = FullQuranCatalog.surahs.firstOrNull { it.number == surah }
        require(chapter != null && first >= 1 && last >= first && last <= chapter.totalAyat) {
            "Invalid Quran passage $surah:$first-$last"
        }
    }
    val id get() = "quran_${surah}_${first}_$last"
    val title get() = FullQuranCatalog.surahs.first { it.number == surah }.englishName
    val reference get() = "$surah:$first" + if (first == last) "" else "-$last"
}

/** Traditional Manzil selection; the compilation is distinct from the Quran's seven divisions. */
object RecitationCollection {
    const val MANZIL_SOURCE = "https://jamiat.org.za/manzil-the-remedy-for-black-magic/"
    val manzil = listOf(
        RecitationPassage(1, 1, 7), RecitationPassage(2, 1, 5), RecitationPassage(2, 163, 163),
        RecitationPassage(2, 255, 257), RecitationPassage(2, 284, 286), RecitationPassage(3, 18, 18),
        RecitationPassage(3, 26, 27), RecitationPassage(7, 54, 56), RecitationPassage(17, 110, 111),
        RecitationPassage(23, 115, 118), RecitationPassage(37, 1, 11), RecitationPassage(55, 33, 40),
        RecitationPassage(59, 21, 24), RecitationPassage(72, 1, 4), RecitationPassage(109, 1, 6),
        RecitationPassage(112, 1, 4), RecitationPassage(113, 1, 5), RecitationPassage(114, 1, 6)
    )
    val ruqyah = listOf(RecitationPassage(1, 1, 7), RecitationPassage(2, 255, 255),
        RecitationPassage(2, 285, 286), RecitationPassage(112, 1, 4),
        RecitationPassage(113, 1, 5), RecitationPassage(114, 1, 6))
    val ruqyahDuas get() = DuaDatasetRepository.DUAS.filter { it.category == "Protection & Healing" }
    /** Wazaif categories in display order. */
    val wazaifCategories = listOf("Morning & Evening", "Forgiveness & Istighfar", "Sleep & Awakening")
    val wazaif get() = DuaDatasetRepository.DUAS.filter {
        it.category in setOf("Morning & Evening", "Forgiveness & Istighfar", "Sleep & Awakening")
    }
}

