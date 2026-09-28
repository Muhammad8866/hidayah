package com.example.festival

/**
 * Practical Ahl-e-Sunnat guidance in simple Roman Hindi with a genuine English copy.
 * References are shown beside each caution in the UI.
 */
data class FestivalCaution(
    val advice: String,
    val source: String,
    val englishAdvice: String,
    val englishSource: String = source
)

object FestivalCautions {
    private val cautions = mapOf(
        "new_year" to listOf(FestivalCaution(
            "Muharram ko sog ka mahina samajh kar nikah, safar ya khushi ke jaiz kaam manhoos na samjhein.",
            "Dawat-e-Islami: The month of Safar is not accursed; Quran 9:36",
            "Do not treat Muharram as a month of mourning, or consider nikah, travel or other permissible happy occasions unlucky in it.")),
        "ashura" to listOf(
            FestivalCaution(
                "Raaij taziye na banayein, na unki ta'zeem ya unse muraad maangein.",
                "Fatawa Razawiyya, jild 24, ta'ziya-dari ke masail",
                "Do not make the customary taziyas, nor venerate them or ask them to fulfil wishes.",
                "Fatawa Razawiyya, vol. 24, rulings on ta'ziya-dari"),
            FestivalCaution(
                "Matam, seena-kobi, nauha aur apne jism ko nuqsan pahunchane se bachein.",
                "Sahih Bukhari 1294; Fatawa Razawiyya, jild 24",
                "Avoid matam, chest-beating, nauha and harming your own body.",
                "Sahih Bukhari 1294; Fatawa Razawiyya, vol. 24")),
        "mawlid" to listOf(FestivalCaution(
            "Milad ki mehfil ya juloos mein gaane-bajane, be-pardagi, namaz chhorne aur fuzool-kharchi se bachein.",
            "Dawat-e-Islami: Blessings of Celebrating the Blessed Birthday of the Holy Prophet",
            "In Milad gatherings or processions, avoid music, immodesty, missing prayers and extravagance.")),
        "gyarhvi" to listOf(FestivalCaution(
            "Niyaz Allah ke naam par karein; Ghaus-e-Azam ko Allah ke barabar ya mustaqil madadgar na samjhein. Kisi khaas tareekh ko farz na samjhein.",
            "Quran 1:5; Ahl-e-Sunnat ka esaal-e-sawab ka mamool",
            "Offer niyaz in the name of Allah; do not consider Ghaus-e-Azam equal to Allah or an independent helper. Do not treat any particular date as obligatory.",
            "Quran 1:5; the Ahl-e-Sunnat practice of esaal-e-sawab")),
        "rajab_start" to listOf(FestivalCaution(
            "Rajab ke kisi khaas roze ya namaz ko farz ya lazmi Sunnat na batayein.",
            "Dawat-e-Islami: Rajab Ka Mahena Kesy Guzarain",
            "Do not present any particular fast or prayer of Rajab as fard or a binding Sunnah.")),
        "miraj" to listOf(FestivalCaution(
            "27 Rajab ko Me'raj ki qat'i tareekh ya kisi khaas namaz/roze ko lazmi Sunnat na samjhein.",
            "Quran 17:1; tareekh aur khaas amal ki riwayat par ikhtilaf",
            "Do not consider 27 Rajab the definite date of the Mi'raj, or any particular prayer/fast on it a binding Sunnah.",
            "Quran 17:1; the date and the narrations about specific deeds are disputed")),
        "baraat" to listOf(FestivalCaution(
            "Patakhe, aatish-bazi, logon ko takleef aur halwe ko zaruri rasm samajhne se bachein.",
            "Dawat-e-Islami: Aaqa Ka Mahina; Fatawa Razawiyya",
            "Avoid firecrackers, fireworks, troubling people, and treating halwa as a necessary custom.")),
        "ramadan" to listOf(FestivalCaution(
            "Roze mein jhoot, gheebat, jhagra aur namaz ki kotahi se bachein.",
            "Sahih Bukhari 1903; Quran 2:183",
            "While fasting, avoid lying, backbiting, quarrelling and neglecting prayer.")),
        "qadr" to listOf(FestivalCaution(
            "Sirf 27vin raat par bharosa karke aakhri ashre ki baqi taaq raatein na chhorein.",
            "Sahih Bukhari 2017",
            "Do not rely on the 27th night alone and neglect the other odd nights of the last ten.")),
        "eid_fitr" to listOf(FestivalCaution(
            "Eid ke din roza na rakhein; namaz aur Sadqa-e-Fitr mein kotahi na karein.",
            "Sahih Bukhari 1990, 1503",
            "Do not fast on the day of Eid; do not neglect the prayer or Sadqa-e-Fitr.")),
        "arafah" to listOf(FestivalCaution(
            "Hajj mein wuquf-e-Arafat karne wale par ghair-haji ke roze ki naseehat na thopein.",
            "Sahih Bukhari 1988; Sahih Muslim 1162",
            "Do not impose the non-pilgrim's fast of Arafah on those performing wuquf at Arafat during Hajj.")),
        "eid_adha" to listOf(FestivalCaution(
            "Qurbani ko numaaish ya riya na banayein; safai aur janwaron ke saath narmi ka khayal rakhein.",
            "Quran 22:37; Sahih Muslim 1955",
            "Do not make Qurbani a show or for riya; keep things clean and be gentle with the animals."))
    )

    fun forFestival(id: String): List<FestivalCaution> = cautions[id].orEmpty()
}
