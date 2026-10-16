package com.example.ui.screens

internal data class QaidaQuiz(val question: String, val romanQuestion: String, val options: List<String>, val answer: Int)

/** Original beginner exercises; Arabic examples are reading drills, not quoted verses. */
internal data class QaidaLesson(
    val id: String,
    val tajweed: Boolean,
    val title: String,
    val romanTitle: String,
    val rule: String,
    val romanRule: String,
    val examples: List<String>,
    val question: String,
    val romanQuestion: String,
    val options: List<String>,
    val answer: Int,
    val extraQuizzes: List<QaidaQuiz> = emptyList(),
    val tip: String = "",
    val romanTip: String = "",
    /** Rule letters to highlight, space separated; may be empty. */
    val letters: String = ""
) {
    val quizzes: List<QaidaQuiz> get() = listOf(QaidaQuiz(question, romanQuestion, options, answer)) + extraQuizzes
}

internal val qaidaAlphabet = listOf(
    "ا" to "Alif", "ب" to "Baa", "ت" to "Taa", "ث" to "Thaa", "ج" to "Jeem",
    "ح" to "Haa", "خ" to "Khaa", "د" to "Daal", "ذ" to "Dhaal", "ر" to "Raa",
    "ز" to "Zaay", "س" to "Seen", "ش" to "Sheen", "ص" to "Saad", "ض" to "Daad",
    "ط" to "Taa (heavy)", "ظ" to "Dhaa (heavy)", "ع" to "Ayn", "غ" to "Ghayn",
    "ف" to "Faa", "ق" to "Qaaf", "ك" to "Kaaf", "ل" to "Laam", "م" to "Meem",
    "ن" to "Noon", "ه" to "Haa (light)", "و" to "Waaw", "ي" to "Yaa", "ء" to "Hamzah"
)

private fun q(question: String, romanQuestion: String, options: List<String>, answer: Int) =
    QaidaQuiz(question, romanQuestion, options, answer)

/** Rules follow riwayat Hafs 'an 'Aasim as commonly taught; options are kept language-neutral where possible. */
internal val qaidaLessons = listOf(
    // ---------- Noorani Qaida track ----------
    QaidaLesson("letters", false, "Arabic letters", "Arabic huroof",
        "Read from right to left. Identify each letter before joining sounds.",
        "Daayein se baayein padhein. Pehle har harf pehchanein.",
        listOf("ا ب ت ث ج ح خ", "د ذ ر ز س ش ص ض", "ط ظ ع غ ف ق ك", "ل م ن ه و ي ء"),
        "Which letter is Baa?", "Baa kaunsa harf hai?", listOf("ت", "ب", "ث"), 1,
        extraQuizzes = listOf(
            q("Which letter is Jeem?", "Jeem kaunsa harf hai?", listOf("ح", "خ", "ج"), 2),
            q("Which letter is Sheen?", "Sheen kaunsa harf hai?", listOf("ش", "س", "ص"), 0),
            q("Which letter is Qaaf?", "Qaaf kaunsa harf hai?", listOf("ف", "ق", "ك"), 1)
        ),
        tip = "Many letters share a shape and differ only by dots (ب ت ث, ج ح خ). Count the dots and see whether they are above or below.",
        romanTip = "Kai huroof ki shakl ek jaisi hai, farq sirf nuqton ka hai (ب ت ث, ج ح خ). Nuqte ginein aur dekhein upar hain ya neeche."),
    QaidaLesson("shapes", false, "Joining letters", "Huroof jorna",
        "Letters change shape when joined. Alif, daal, dhaal, raa, zaay and waaw do not join to the following letter.",
        "Jurne par shakl badalti hai. ا د ذ ر ز و agle harf se nahi jurte.",
        listOf("بـ  ـبـ  ـب", "بَتَ  كَتَبَ", "دَرَسَ  وَزَنَ"),
        "Which letter does not join to the following letter?", "Kaunsa harf agle harf se nahi jurta?", listOf("ب", "ت", "د"), 2,
        extraQuizzes = listOf(
            q("How many letters do not join to the following letter?", "Kitne huroof agle harf se nahi jurte?", listOf("4", "6", "8"), 1),
            q("Which is the beginning form of Baa?", "Baa ki shuru wali shakl kaunsi hai?", listOf("ـب", "ـبـ", "بـ"), 2),
            q("Which word has three joined letters?", "Kis lafz mein teeno huroof jure hue hain?", listOf("كَتَبَ", "وَزَنَ", "دَرَسَ"), 0)
        ),
        tip = "A letter keeps its dots in every form. Look for the dots to recognise it inside a word.",
        romanTip = "Harf ke nuqte har shakl mein wahi rehte hain. Lafz ke andar harf ko nuqton se pehchanein.",
        letters = "ا د ذ ر ز و"),
    QaidaLesson("fatha", false, "Fatha / Zabar", "Zabar",
        "The mark above gives a short a sound. Keep it short.", "Upar zabar se chhoti 'a' awaaz aati hai.",
        listOf("بَ  تَ  جَ", "كَتَبَ"), "Which syllable is ba?", "'Ba' kaunsa hai?", listOf("بِ", "بُ", "بَ"), 2,
        extraQuizzes = listOf(
            q("Where is the fatha written?", "Zabar kahan likha jata hai?", listOf("Below / Neeche", "Above / Upar", "After / Baad mein"), 1),
            q("Which syllable is ta?", "'Ta' kaunsa hai?", listOf("تَ", "تِ", "تُ"), 0),
            q("How long is the fatha sound?", "Zabar ki awaaz kitni lambi hai?", listOf("1 count", "2 count", "4 count"), 0)
        ),
        tip = "Do not stretch the fatha into an alif: say بَ, not بَا.",
        romanTip = "Zabar ko kheench kar alif na banayein: بَ parhein, بَا nahi."),
    QaidaLesson("kasra", false, "Kasra / Zer", "Zer",
        "The mark below gives a short i sound.", "Neeche zer se chhoti 'i' awaaz aati hai.",
        listOf("بِ  تِ  جِ", "بِ  بَ  بِ"), "Which syllable is bi?", "'Bi' kaunsa hai?", listOf("بِ", "بَ", "بُ"), 0,
        extraQuizzes = listOf(
            q("Where is the kasra written?", "Zer kahan likha jata hai?", listOf("Above / Upar", "Below / Neeche", "After / Baad mein"), 1),
            q("Which syllable is ji?", "'Ji' kaunsa hai?", listOf("جَ", "جُ", "جِ"), 2),
            q("Which syllable is ti?", "'Ti' kaunsa hai?", listOf("تُ", "تِ", "تَ"), 1)
        ),
        tip = "Do not stretch the kasra into a yaa: say بِ, not بِي.",
        romanTip = "Zer ko kheench kar yaa na banayein: بِ parhein, بِي nahi."),
    QaidaLesson("damma", false, "Damma / Pesh", "Pesh",
        "The small curl above gives a short u sound.", "Upar pesh se chhoti 'u' awaaz aati hai.",
        listOf("بُ  تُ  جُ", "بَ  بِ  بُ"), "Which syllable is bu?", "'Bu' kaunsa hai?", listOf("بَ", "بُ", "بِ"), 1,
        extraQuizzes = listOf(
            q("Which syllable is tu?", "'Tu' kaunsa hai?", listOf("تُ", "تَ", "تِ"), 0),
            q("The damma looks like a small…", "Pesh kis chhote harf jaisa dikhta hai?", listOf("ا", "و", "ي"), 1),
            q("Which syllable is ju?", "'Ju' kaunsa hai?", listOf("جِ", "جَ", "جُ"), 2)
        ),
        tip = "Round the lips for damma, and do not stretch it into a waaw: say بُ, not بُو.",
        romanTip = "Pesh par hont gol karein aur kheench kar waaw na banayein: بُ parhein, بُو nahi."),
    QaidaLesson("tanween", false, "Tanween", "Do zabar, zer aur pesh",
        "Double vowel marks add an n sound when continuing: an, in, un. Two fathas are usually written with an alif.",
        "Milakar parhne mein do harkat se 'an, in, un' hota hai. Do zabar ke saath aksar alif likha hota hai.",
        listOf("بًا  بٍ  بٌ", "تًا  تٍ  تٌ"), "Which ends in un when continuing?", "Milakar parhne mein 'un' kaunsa hai?", listOf("بًا", "بٍ", "بٌ"), 2,
        extraQuizzes = listOf(
            q("Which is 'ban' (two fathas)?", "'Ban' (do zabar) kaunsa hai?", listOf("بٌ", "بًا", "بٍ"), 1),
            q("Which hidden letter's sound does tanween add?", "Tanween mein kis harf ki awaaz barhti hai?", listOf("م", "ن", "ل"), 1),
            q("When stopping on بًا, what is read?", "بًا par rukein to kya parha jayega?", listOf("بَا", "بَنْ", "بْ"), 0)
        ),
        tip = "Tanween comes only at the end of a word. When stopping, two fathas become an alif; two kasras or dammas become sukoon.",
        romanTip = "Tanween sirf lafz ke aakhir mein aati hai. Rukte waqt do zabar alif ban jata hai; do zer ya do pesh sukoon ho jate hain."),
    QaidaLesson("sukoon", false, "Sukoon", "Sakin harf",
        "Sukoon removes the vowel. Join the consonant to the preceding sound.",
        "Sukoon wale harf ko pichhli awaaz se jorein; extra harkat na lagayein.",
        listOf("أَبْ  أَتْ  أَمْ", "قُلْ  مِنْ  كَمْ"), "Which has a sukoon on Baa?", "Baa par sukoon kahan hai?", listOf("أَبْ", "بَ", "بُ"), 0,
        extraQuizzes = listOf(
            q("Which mark is the sukoon?", "Sukoon ki alamat kaunsi hai?", listOf("ّ", "ْ", "ُ"), 1),
            q("Which reads 'am'?", "'Am' kaunsa hai?", listOf("أَمْ", "أَمَ", "مَا"), 0),
            q("A letter with sukoon has how many vowels?", "Sakin harf par kitni harkat hoti hai?", listOf("0", "1", "2"), 0)
        ),
        tip = "Do not bounce or add a vowel on a sakin letter (except the qalqalah letters): قُلْ, not قُلَ.",
        romanTip = "Sakin harf par harkat ya jhatka na dein (siwaye qalqalah ke huroof ke): قُلْ parhein, قُلَ nahi."),
    QaidaLesson("shadda", false, "Shadda", "Tashdeed",
        "Shadda doubles a consonant: first still, then with its vowel.",
        "Tashdeed mein harf do baar: pehle sakin, phir harkat ke saath.",
        listOf("أَبَّ  أَبِّ  أَبُّ", "رَبِّ  حَقٌّ"), "Which mark doubles a consonant?", "Harf ko dohraane wali alamat?", listOf("ْ", "ّ", "َ"), 1,
        extraQuizzes = listOf(
            q("A letter with shadda counts as how many letters?", "Mushaddad harf kitne harf ke barabar hai?", listOf("1", "2", "3"), 1),
            q("رَبِّ is read as…", "رَبِّ kaise parha jayega?", listOf("ra-bi", "rab-bi", "raa-bi"), 1),
            q("Which has shadda with fatha?", "Tashdeed ke saath zabar kis par hai?", listOf("أَبِّ", "أَبَّ", "أَبُّ"), 1)
        ),
        tip = "Press on the letter and hold it briefly; do not read it as a single light letter.",
        romanTip = "Mushaddad harf par zor dekar zara thehrein; use ek halke harf ki tarah na parhein."),
    QaidaLesson("makharij", false, "Makharij (where letters come from)", "Makharij: huroof ki jagah",
        "Every letter has an exit point (makhraj). There are five main areas: the empty space of the mouth (madd letters), the throat (ء ه ع ح غ خ), the tongue (most letters), the lips (ف ب م و) and the nose (ghunnah).",
        "Har harf ka ek makhraj hai. Paanch bari jagahein hain: munh ka khaali hissa (madd ke huroof), halaq (ء ه ع ح غ خ), zabaan (aksar huroof), hont (ف ب م و) aur naak (ghunna).",
        listOf("ء ه  |  ع ح  |  غ خ", "ف ب م و", "أَ عَ  |  هَ حَ  |  كَ قَ"),
        "Which letters come from the throat?", "Halaq se kaunse huroof nikalte hain?", listOf("ف ب م", "ع ح غ خ", "س ص ز"), 1,
        extraQuizzes = listOf(
            q("Which letter is made with the lips?", "Honton se kaunsa harf nikalta hai?", listOf("م", "ك", "ر"), 0),
            q("Where does ghunnah come from?", "Ghunna kahan se nikalta hai?", listOf("Throat / Halaq", "Lips / Hont", "Nose / Naak"), 2),
            q("How many throat letters are there?", "Halaq ke huroof kitne hain?", listOf("3", "6", "10"), 1)
        ),
        tip = "Similar-sounding pairs differ by makhraj: ء/ع, ه/ح, ك/ق. Listen to a teacher and copy the sound.",
        romanTip = "Milti-julti awaazon ka farq makhraj se hai: ء/ع, ه/ح, ك/ق. Ustad se sun kar waisi hi awaaz nikalein.",
        letters = "ء ه ع ح غ خ ف ب م و"),
    QaidaLesson("heavy_light", false, "Heavy and light letters", "Mote aur bareek huroof",
        "Seven letters are always heavy (full mouth): خ ص ض غ ط ق ظ. The rest are light, except Alif, Raa and the Laam of Allah's name, which change with their surroundings.",
        "Saat huroof hamesha mote (pur) parhe jate hain: خ ص ض غ ط ق ظ. Baqi bareek hain, siwaye Alif, Raa aur lafz Allah ke Laam ke, jo halat ke mutabiq badalte hain.",
        listOf("خ ص ض غ ط ق ظ", "سَ صَ  |  تَ طَ  |  كَ قَ", "ذَ ظَ  |  دَ ضَ"),
        "Which is a heavy letter?", "Mota harf kaunsa hai?", listOf("س", "ص", "ت"), 1,
        extraQuizzes = listOf(
            q("How many letters are always heavy?", "Hamesha mote parhe jane wale huroof kitne hain?", listOf("5", "7", "9"), 1),
            q("Which is the light partner of ط?", "ط ka bareek jora kaunsa hai?", listOf("ت", "ق", "ظ"), 0),
            q("Which is heavy?", "Mota kaunsa hai?", listOf("ك", "ق", "ف"), 1)
        ),
        tip = "For heavy letters raise the back of the tongue; do not round the lips. Keep light letters thin, especially next to a heavy one.",
        romanTip = "Mote huroof mein zabaan ki jar upar uthayein; hont gol na karein. Bareek huroof ko patla rakhein, khaas kar mote harf ke paas.",
        letters = "خ ص ض غ ط ق ظ"),

    // ---------- Tajweed track ----------
    QaidaLesson("madd", true, "Natural madd", "Madd tabee'i",
        "Alif after fatha, yaa sakinah after kasra and waaw sakinah after damma are stretched for two counts; keep a steady pace.",
        "Zabar ke baad alif, zer ke baad yaa sakin aur pesh ke baad waaw sakin ko do count kheenchein; raftaar barabar rakhein.",
        listOf("بَا  بِي  بُو", "قَالَ  قِيلَ  يَقُولُ"), "How many counts for natural madd?", "Madd tabee'i kitne count?", listOf("1", "2", "6"), 1,
        extraQuizzes = listOf(
            q("Which are the madd letters?", "Madd ke huroof kaunse hain?", listOf("ا و ي", "ب ت ث", "ن م"), 0),
            q("Which has a madd?", "Madd kis mein hai?", listOf("بَ", "بَا", "أَبْ"), 1),
            q("Yaa is a madd letter when the letter before it has…", "Yaa madd ka harf tab hai jab us se pehle ho…", listOf("Fatha / Zabar", "Kasra / Zer", "Damma / Pesh"), 1)
        ),
        tip = "Neither cut the madd to one count nor pull it beyond two.",
        romanTip = "Madd ko na ek count par kaatein, na do se zyada kheenchein.",
        letters = "ا و ي"),
    QaidaLesson("madd_types", true, "Longer madds", "Madd ki qismein",
        "When hamzah or sukoon follows a madd letter, the stretch is longer. Muttasil (hamzah in the same word): 4–5 counts. Munfasil (hamzah at the start of the next word): 4–5 counts. Laazim (a permanent sukoon or shadda follows): always 6. 'Aarid (sukoon only because you stop): 2, 4 or 6.",
        "Madd ke harf ke baad hamzah ya sukoon aaye to madd lamba hota hai. Muttasil (hamzah usi lafz mein): 4–5 count. Munfasil (hamzah agle lafz ke shuru mein): 4–5 count. Laazim (asli sukoon ya tashdeed): hamesha 6. 'Aarid (rukne ki wajah se sukoon): 2, 4 ya 6.",
        listOf("جَاءَ  سُوءَ  جِيءَ", "يَا أَيُّهَا  |  فِي أَنْفُسِكُمْ", "الضَّالِّينَ  |  حَاجَّ", "نَسْتَعِينُ ← نَسْتَعِينْ"),
        "How many counts is madd laazim?", "Madd laazim kitne count ka hai?", listOf("2", "4", "6"), 2,
        extraQuizzes = listOf(
            q("In جَاءَ the hamzah is in the same word. Which madd is it?", "جَاءَ mein hamzah usi lafz mein hai. Yeh kaunsa madd hai?", listOf("Muttasil", "Munfasil", "'Aarid"), 0),
            q("The madd caused by stopping at the end of an ayah is…", "Aayat ke aakhir mein rukne se banne wala madd?", listOf("Laazim", "'Aarid", "Muttasil"), 1),
            q("In يَا أَيُّهَا the hamzah begins the next word. Which madd is it?", "يَا أَيُّهَا mein hamzah agle lafz ke shuru mein hai. Kaunsa madd?", listOf("Muttasil", "Munfasil", "Laazim"), 1)
        ),
        tip = "Choose one length with your teacher and keep it the same throughout the recitation.",
        romanTip = "Ustad ke saath ek miqdaar tay karein aur poori tilawat mein wahi rakhein.",
        letters = "ء"),
    QaidaLesson("ghunnah", true, "Ghunnah", "Ghunna",
        "Noon and meem with shadda carry a nasal sound for two counts.",
        "Noon aur meem mushaddad mein do count naak ki awaaz rakhein.",
        listOf("نَّ  مَّ", "إِنَّ  ثُمَّ  عَمَّ"), "Which pair needs ghunnah with shadda?", "Tashdeed par kin mein ghunna hai?", listOf("بّ تّ", "لّ رّ", "نّ مّ"), 2,
        extraQuizzes = listOf(
            q("How many counts is the ghunnah?", "Ghunna kitne count ka hai?", listOf("1", "2", "6"), 1),
            q("Where does the ghunnah sound come from?", "Ghunna ki awaaz kahan se aati hai?", listOf("Nose / Naak", "Throat / Halaq", "Lips / Hont"), 0),
            q("Which word has ghunnah?", "Kis lafz mein ghunna hai?", listOf("إِنَّ", "قُلْ", "لَكَ"), 0)
        ),
        tip = "Hold the nasal sound for its full length; do not rush past the shadda.",
        romanTip = "Naak ki awaaz poori miqdaar tak rakhein; tashdeed par jaldi na karein.",
        letters = "ن م"),
    QaidaLesson("izhar", true, "Izhar", "Izhar: saaf parhna",
        "Read noon sakinah or tanween clearly before the six throat letters.",
        "Halaq ke chhe huroof se pehle noon sakin ya tanween saaf parhein.",
        listOf("ء ه ع ح غ خ", "مِنْ عِلْمٍ", "مَنْ آمَنَ  |  أَنْعَمْتَ"), "Which letter triggers Izhar?", "Izhar ka harf kaunsa hai?", listOf("ع", "ب", "ي"), 0,
        extraQuizzes = listOf(
            q("How many Izhar letters are there?", "Izhar ke huroof kitne hain?", listOf("4", "6", "15"), 1),
            q("Izhar means…", "Izhar ka matlab?", listOf("Clear / Saaf", "Merge / Milana", "Hide / Chhupana"), 0),
            q("Which letter triggers Izhar?", "Izhar ka harf kaunsa hai?", listOf("م", "ت", "ح"), 2)
        ),
        tip = "Do not lengthen the noon or add ghunnah, and do not bounce it.",
        romanTip = "Noon ko lamba na karein, ghunna na barhayein aur jhatka na dein.",
        letters = "ء ه ع ح غ خ"),
    QaidaLesson("idgham", true, "Idgham", "Idgham: milana",
        "Across words, merge noon sakinah or tanween into ي ن م و with ghunnah; ل ر without it. Same-word exceptions need teacher guidance.",
        "Do lafzon mein ي ن م و par ghunna se, ل ر par bina ghunna milayein. Ek lafz ke exceptions ustad se seekhein.",
        listOf("ي ن م و  |  ل ر", "مِنْ مَالٍ", "مَنْ يَقُولُ  |  مِنْ رَبِّهِمْ"), "Which pair merges without ghunnah?", "Bina ghunna milane wale huroof?", listOf("ي و", "ن م", "ل ر"), 2,
        extraQuizzes = listOf(
            q("How many Idgham letters are there?", "Idgham ke huroof kitne hain?", listOf("4", "6", "15"), 1),
            q("Idgham means…", "Idgham ka matlab?", listOf("Merge / Milana", "Stop / Rukna", "Stretch / Kheenchna"), 0),
            q("Which letter merges with ghunnah?", "Ghunna ke saath kaunsa harf milta hai?", listOf("ر", "ل", "و"), 2)
        ),
        tip = "Inside one word (دُنْيَا, صِنْوَانٌ, قِنْوَانٌ, بُنْيَانٌ) there is no idgham; read the noon clearly.",
        romanTip = "Ek hi lafz mein (دُنْيَا, صِنْوَانٌ, قِنْوَانٌ, بُنْيَانٌ) idgham nahi hota; noon saaf parhein.",
        letters = "ي ر م ل و ن"),
    QaidaLesson("iqlab", true, "Iqlab", "Iqlab: awaaz badalna",
        "Before Baa, noon sakinah or tanween becomes a concealed meem sound with ghunnah.",
        "Baa se pehle noon sakin ya tanween ko chhupe meem ki awaaz aur ghunna se parhein.",
        listOf("نْ + ب", "مِنْ بَعْدِ", "سَمِيعٌ بَصِيرٌ"), "Which letter triggers Iqlab?", "Iqlab kis harf par hota hai?", listOf("ب", "ل", "ع"), 0,
        extraQuizzes = listOf(
            q("In Iqlab the noon changes into the sound of…", "Iqlab mein noon kis harf ki awaaz ban jata hai?", listOf("م", "ل", "ر"), 0),
            q("How many Iqlab letters are there?", "Iqlab ke huroof kitne hain?", listOf("1", "6", "15"), 0),
            q("How many counts is the ghunnah in Iqlab?", "Iqlab mein ghunna kitne count ka hai?", listOf("0", "2", "6"), 1)
        ),
        tip = "Many mushafs show a small م above the noon here. Close the lips gently, without pressing.",
        romanTip = "Aksar mushaf mein yahan noon par chhota م likha hota hai. Hont narmi se band karein, dabayein nahi.",
        letters = "ب"),
    QaidaLesson("ikhfa", true, "Ikhfa", "Ikhfa: chhupana",
        "Conceal noon sakinah or tanween with ghunnah before these fifteen letters.",
        "In pandrah huroof se pehle noon sakin ya tanween ko ghunna se chhupayein.",
        listOf("ت ث ج د ذ ز س ش ص ض ط ظ ف ق ك", "أَنْتُمْ", "مِنْ قَبْلُ  |  كُنْتُمْ"), "Which letter triggers Ikhfa?", "Ikhfa ka harf kaunsa hai?", listOf("ه", "ر", "ت"), 2,
        extraQuizzes = listOf(
            q("How many Ikhfa letters are there?", "Ikhfa ke huroof kitne hain?", listOf("6", "15", "1"), 1),
            q("Ikhfa means…", "Ikhfa ka matlab?", listOf("Hide / Chhupana", "Merge / Milana", "Clear / Saaf"), 0),
            q("Which letter triggers Ikhfa?", "Ikhfa ka harf kaunsa hai?", listOf("ق", "ع", "ل"), 0)
        ),
        tip = "The tongue does not touch the place of noon; keep it ready for the next letter and hold the ghunnah for two counts.",
        romanTip = "Zabaan noon ke makhraj ko na chhuye; agle harf ke liye tayyar rakhein aur do count ghunna karein.",
        letters = "ت ث ج د ذ ز س ش ص ض ط ظ ف ق ك"),
    QaidaLesson("meem", true, "Meem sakinah", "Meem sakin",
        "Before Baa conceal with ghunnah; before Meem merge with ghunnah; otherwise read clearly.",
        "Baa par ghunna se chhupayein, meem par ghunna se milayein, baqi par saaf parhein.",
        listOf("مْ + ب  |  مْ + م", "هُمْ بِهِ", "لَهُمْ مَا  |  لَهُمْ فِيهَا"), "What happens to meem sakinah before Meem?", "Meem sakin ke baad meem aaye to?", listOf("Izhar", "Idgham + ghunnah", "Qalqalah"), 1,
        extraQuizzes = listOf(
            q("Meem sakinah before Baa is called…", "Meem sakin ke baad Baa aaye to kya kehlata hai?", listOf("Ikhfa shafawi", "Izhar shafawi", "Iqlab"), 0),
            q("In لَهُمْ فِيهَا the meem is read with…", "لَهُمْ فِيهَا mein meem kaise parha jayega?", listOf("Ikhfa", "Izhar", "Idgham"), 1),
            q("How many counts is the ghunnah in هُمْ بِهِ?", "هُمْ بِهِ mein ghunna kitne count ka hai?", listOf("0", "2", "6"), 1)
        ),
        tip = "Take extra care to read the meem clearly before و and ف, because their places are close to it.",
        romanTip = "و aur ف se pehle meem ko khaas tor par saaf parhein, kyunke inka makhraj qareeb hai.",
        letters = "ب م"),
    QaidaLesson("qalqalah", true, "Qalqalah", "Qalqalah",
        "When ق ط ب ج د are still, give a small release without adding a vowel.",
        "ق ط ب ج د sakin hon to halki uchhal dein; extra harkat na lagayein.",
        listOf("ق ط ب ج د", "أَبْ  أَجْ  أَدْ", "أَقْ  أَطْ"), "Which is a qalqalah letter?", "Qalqalah ka harf kaunsa hai?", listOf("س", "ق", "م"), 1,
        extraQuizzes = listOf(
            q("How many qalqalah letters are there?", "Qalqalah ke huroof kitne hain?", listOf("3", "5", "7"), 1),
            q("Qalqalah happens when the letter has…", "Qalqalah tab hota hai jab harf par ho…", listOf("Sukoon", "Fatha / Zabar", "Madd"), 0),
            q("Which is a qalqalah letter?", "Qalqalah ka harf kaunsa hai?", listOf("ت", "ك", "ط"), 2)
        ),
        tip = "The bounce is stronger when you stop on the letter. Do not turn it into a fatha or kasra.",
        romanTip = "Harf par rukne mein uchhal zyada hoti hai. Ise zabar ya zer na banayein.",
        letters = "ق ط ب ج د"),
    QaidaLesson("laam", true, "Rules of Laam", "Laam ke qaide",
        "The Laam in the name Allah is heavy after fatha or damma and light after kasra. In ال, the laam is read before moon letters (الْقَمَرُ) and is silent before sun letters, which take a shadda (الشَّمْسُ).",
        "Lafz Allah ka Laam zabar ya pesh ke baad mota aur zer ke baad bareek hota hai. ال ka laam qamari huroof se pehle parha jata hai (الْقَمَرُ) aur shamsi huroof se pehle nahi parha jata; un par tashdeed aati hai (الشَّمْسُ).",
        listOf("هُوَ اللَّهُ  |  رَسُولُ اللَّهِ", "بِسْمِ اللَّهِ  |  لِلَّهِ", "الْقَمَرُ  |  الشَّمْسُ"),
        "After kasra, the Laam of Allah's name is…", "Zer ke baad lafz Allah ka Laam?", listOf("Heavy / Mota", "Light / Bareek", "Silent / Khamosh"), 1,
        extraQuizzes = listOf(
            q("In الشَّمْسُ the laam of ال is…", "الشَّمْسُ mein ال ka laam?", listOf("Read / Parha jata hai", "Silent / Nahi parha jata", "Stretched / Kheencha jata hai"), 1),
            q("Which word has a moon letter after ال?", "Kis lafz mein ال ke baad qamari harf hai?", listOf("الْقَمَرُ", "النَّاسُ", "الشَّمْسُ"), 0),
            q("In رَسُولُ اللَّهِ the Laam is…", "رَسُولُ اللَّهِ mein Laam?", listOf("Heavy / Mota", "Light / Bareek", "Silent / Khamosh"), 0)
        ),
        tip = "The highlighted letters are the fourteen sun letters. Every other laam in the Quran is light.",
        romanTip = "Numayan huroof chaudah shamsi huroof hain. Iske ilawa Quran ka har laam bareek hai.",
        letters = "ت ث د ذ ر ز س ش ص ض ط ظ ل ن"),
    QaidaLesson("raa", true, "Rules of Raa", "Raa ke qaide",
        "Raa is heavy with fatha or damma and light with kasra. Raa sakinah follows the vowel before it: heavy after fatha or damma, light after kasra.",
        "Raa par zabar ya pesh ho to mota, zer ho to bareek. Raa sakin pichhli harkat ke mutabiq hai: zabar ya pesh ke baad mota, zer ke baad bareek.",
        listOf("رَ  رُ  |  رِ", "رَبِّ  رُسُلٌ  |  رِجَالٌ", "مَرْيَمَ  قُرْآنٌ  |  فِرْعَوْنَ"),
        "Raa with kasra is…", "Zer wala Raa?", listOf("Heavy / Mota", "Light / Bareek", "Silent / Khamosh"), 1,
        extraQuizzes = listOf(
            q("In فِرْعَوْنَ the raa sakinah is…", "فِرْعَوْنَ mein raa sakin?", listOf("Light / Bareek", "Heavy / Mota", "Silent / Khamosh"), 0),
            q("Which raa is heavy?", "Mota raa kaunsa hai?", listOf("رِ", "رَ", "فِرْ"), 1),
            q("In قُرْآنٌ the raa sakinah is…", "قُرْآنٌ mein raa sakin?", listOf("Heavy / Mota", "Light / Bareek", "Silent / Khamosh"), 0)
        ),
        tip = "A few words are exceptions, e.g. raa sakinah after kasra but before a heavy letter (قِرْطَاسٍ, مِرْصَادًا) is heavy. Learn these with a teacher.",
        romanTip = "Kuch alfaaz mustasna hain, jaise zer ke baad raa sakin jiske baad mota harf ho (قِرْطَاسٍ, مِرْصَادًا) mota parha jata hai. Yeh ustad se seekhein.",
        letters = "ر"),
    QaidaLesson("waqf", true, "Stopping (Waqf)", "Waqf: rukne ke qaide",
        "When stopping, drop the last short vowel and read sukoon; two fathas become alif; ة becomes ه. Signs: م must stop; ط good to stop; ج stop or continue; ز and ص continuing is better; قلى stopping is better; صلى continuing is better; لا do not stop; ∴ ∴ stop at one of the two places, not both.",
        "Rukte waqt aakhri harkat chhor kar sukoon parhein; do zabar alif ban jata hai; ة ko ه parhein. Alamaat: م rukna zaroori; ط rukna behtar; ج rukna ya milana dono theek; ز aur ص milana behtar; قلى rukna behtar; صلى milana behtar; لا na rukein; ∴ ∴ do mein se ek jagah rukein, dono par nahi.",
        listOf("م  ط  ج  ز  ص", "قلى  صلى  لا  ∴", "نَسْتَعِينُ ← نَسْتَعِينْ", "عَلِيمًا ← عَلِيمَا  |  رَحْمَةٌ ← رَحْمَهْ"),
        "Which sign means you must stop?", "Kis alamat par rukna zaroori hai?", listOf("م", "لا", "ز"), 0,
        extraQuizzes = listOf(
            q("Which sign means do not stop?", "Kis alamat par nahi rukna?", listOf("ج", "لا", "ط"), 1),
            q("When stopping on رَحْمَةٌ, the ة is read as…", "رَحْمَةٌ par rukein to ة kya parha jayega?", listOf("ت", "ه", "ن"), 1),
            q("At which sign are stopping and continuing equally fine?", "Kis alamat par rukna aur milana dono barabar hain?", listOf("ج", "م", "لا"), 0)
        ),
        tip = "If your breath ends where there is no sign, stop on a complete word, then go back a word or two and continue. Never stop in the middle of a word.",
        romanTip = "Agar saans wahan toot jaye jahan alamat nahi, to poore lafz par rukein, phir ek do lafz peeche se dobara shuru karein. Lafz ke beech mein kabhi na rukein.",
        letters = "م ط ج ز ص قلى صلى لا ∴")
)

/** Blank query returns every lesson; otherwise matches id, both titles, and the rule/tip in the chosen language. */
internal fun qaidaSearch(query: String, roman: Boolean): List<QaidaLesson> {
    val needle = query.trim().lowercase(java.util.Locale.ROOT)
    if (needle.isEmpty()) return qaidaLessons
    return qaidaLessons.filter { lesson ->
        listOf(lesson.id.replace('_', ' '), lesson.title, lesson.romanTitle, lesson.letters,
            if (roman) lesson.romanRule else lesson.rule, if (roman) lesson.romanTip else lesson.tip)
            .any { it.lowercase(java.util.Locale.ROOT).contains(needle) }
    }
}
