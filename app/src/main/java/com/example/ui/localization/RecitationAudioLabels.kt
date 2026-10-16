package com.example.ui.localization

private val recitationAudioLabels by lazy {
    mapOf(
        "hinglish" to mapOf(
            "Play all" to "Sab sunein",
            "Previous ayah" to "Pichli ayat",
            "Next ayah" to "Agli ayat",
            "Speed" to "Raftaar",
            "Repeat" to "Dohraein",
            "Sleep timer" to "Sleep timer",
            "Off" to "Band",
            "min" to "min"
        ),
        "hi" to mapOf(
            "Play all" to "सब सुनें",
            "Previous ayah" to "पिछली आयत",
            "Next ayah" to "अगली आयत",
            "Speed" to "रफ़्तार",
            "Repeat" to "दोहराएँ",
            "Sleep timer" to "स्लीप टाइमर",
            "Off" to "बंद",
            "min" to "मिनट"
        ),
        "ur" to mapOf(
            "Play all" to "سب سنیں",
            "Previous ayah" to "پچھلی آیت",
            "Next ayah" to "اگلی آیت",
            "Speed" to "رفتار",
            "Repeat" to "دہرائیں",
            "Sleep timer" to "سلیپ ٹائمر",
            "Off" to "بند",
            "min" to "منٹ"
        )
    )
}

internal fun recitationAudioLabel(text: String, language: String): String? = recitationAudioLabels[language]?.get(text)
