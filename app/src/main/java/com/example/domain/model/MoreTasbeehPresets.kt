package com.example.domain.model

/** Additional counted dhikr with an explicit hadith count. */
internal object MoreTasbeehPresets {
    val LIST = listOf(
        TasbeehPreset(
            "Subhan Allahi wa bihamdihi",
            "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            100,
            "Sahih al-Bukhari 6405"
        ),
        TasbeehPreset(
            "La ilaha illallahu wahdahu la sharika lah",
            "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            100,
            "Sahih al-Bukhari 6403"
        )
    )
}
