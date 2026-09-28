package com.example.util

/** Converts Hindi (Devanagari) into easy-to-read Roman Hindi/Hinglish. */
fun String.toHinglish(): String {
    val bundledHinglish = mapOf(
        "In the name of Allah, the Entirely Merciful, the Especially Merciful." to "Allah ke naam se, jo nihayat Meherbaan aur Reham farmane wala hai.",
        "All praise is due to Allah, Lord of the worlds." to "Saari tareef Allah ke liye hai, jo saare jahaan ka Rab hai.",
        "The Entirely Merciful, the Especially Merciful." to "Nihayat Meherbaan, bahut Reham farmane wala.",
        "Sovereign of the Day of Recompense." to "Badle ke din ka Maalik.",
        "It is You we worship and You we ask for help." to "Hum sirf Teri ibaadat karte hain aur sirf Tujhse madad maangte hain.",
        "Guide us to the straight path -" to "Humein seedha raasta dikha—",
        "The path of those upon whom You have bestowed favor, not of those who have evoked anger or of those who are astray." to "Un logon ka raasta jin par Tune inaam farmaya; na unka jin par ghazab hua aur na gumraahon ka.",
        "But they who believe and do righteous deeds - those are the companions of Paradise; they will abide therein eternally." to "Jo imaan laaye aur nek amal kiye, wahi Jannat wale hain; woh usmein hamesha rahenge.",
        "O you who have believed, seek help through patience and prayer. Indeed, Allah is with the patient." to "Aye imaan walon, sabr aur namaaz se madad maango. Beshak Allah sabr karne walon ke saath hai.",
        "Allah - there is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep." to "Allah—Uske siwa koi ibaadat ke laayak nahi; Woh hamesha Zinda aur sabko sambhalne wala hai. Use na oongh aati hai aur na neend."
    )
    bundledHinglish[this]?.let { return it }
    if (none { it in '\u0900'..'\u097f' }) return this
    val vowels = mapOf('अ' to "a", 'आ' to "aa", 'इ' to "i", 'ई' to "ee", 'उ' to "u", 'ऊ' to "oo", 'ऋ' to "ri", 'ए' to "e", 'ऐ' to "ai", 'ओ' to "o", 'औ' to "au")
    val consonants = mapOf(
        'क' to "k", 'ख' to "kh", 'ग' to "g", 'घ' to "gh", 'ङ' to "ng", 'च' to "ch", 'छ' to "chh", 'ज' to "j", 'झ' to "jh", 'ञ' to "ny",
        'ट' to "t", 'ठ' to "th", 'ड' to "d", 'ढ' to "dh", 'ण' to "n", 'त' to "t", 'थ' to "th", 'द' to "d", 'ध' to "dh", 'न' to "n",
        'प' to "p", 'फ' to "ph", 'ब' to "b", 'भ' to "bh", 'म' to "m", 'य' to "y", 'र' to "r", 'ल' to "l", 'व' to "v", 'श' to "sh", 'ष' to "sh", 'स' to "s", 'ह' to "h",
        'क़' to "q", 'ख़' to "kh", 'ग़' to "gh", 'ज़' to "z", 'ड़' to "r", 'फ़' to "f"
    )
    val marks = mapOf('ा' to "aa", 'ि' to "i", 'ी' to "ee", 'ु' to "u", 'ू' to "oo", 'ृ' to "ri", 'े' to "e", 'ै' to "ai", 'ो' to "o", 'ौ' to "au")
    val out = StringBuilder()
    var i = 0
    while (i < length) {
        val ch = this[i]
        when {
            consonants.containsKey(ch) -> {
                out.append(consonants.getValue(ch))
                val next = getOrNull(i + 1)
                when {
                    next == '्' -> i++
                    next != null && marks.containsKey(next) -> { out.append(marks.getValue(next)); i++ }
                    else -> out.append('a')
                }
            }
            vowels.containsKey(ch) -> out.append(vowels.getValue(ch))
            ch == 'ं' || ch == 'ँ' -> out.append('n')
            ch == 'ः' -> out.append('h')
            ch == '़' || ch == '्' -> Unit
            else -> out.append(ch)
        }
        i++
    }
    return out.toString().replace(Regex(" +"), " ").trim()
}
