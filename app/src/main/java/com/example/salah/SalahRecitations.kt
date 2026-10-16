package com.example.salah

/**
 * Everything recited in the prayer, in the order it is recited. Quranic text is checked against
 * the bundled Mushaf by SalahContentTest; the other wordings follow the cited hadith.
 */
private val threeTimes = t("At least three times.", "Kam az kam teen baar.", "कम से कम तीन बार।", "کم از کم تین بار۔")

internal val salahRecitations: List<Recitation> = listOf(
    // ---------------------------------------------------------------- Inside the prayer
    Recitation(
        "takbir", RecitationGroup.IN_PRAYER,
        t("Takbir", "Takbeer", "तकबीर", "تکبیر"),
        "اللَّهُ أَكْبَرُ",
        "Allahu Akbar.",
        t("Allah is the Greatest.", "Allah sab se bada hai.", "अल्लाह सबसे बड़ा है।", "اللہ سب سے بڑا ہے۔"),
        "Sahih al-Bukhari 757"
    ),
    Recitation(
        "thana", RecitationGroup.IN_PRAYER,
        t("Thana (opening praise)", "Sana", "सना", "ثنا"),
        "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، وَتَبَارَكَ اسْمُكَ، وَتَعَالَىٰ جَدُّكَ، وَلَا إِلَٰهَ غَيْرُكَ",
        "Subhanakallahumma wa bihamdika, wa tabarakasmuka, wa ta‘ala jadduka, wa la ilaha ghayruk.",
        t(
            "Glory be to You, O Allah, and praise be to You. Blessed is Your name, exalted is Your majesty, and there is no god but You.",
            "Aye Allah, Tu paak hai aur Teri hi tareef hai. Tera naam barkat wala hai, Teri shaan buland hai aur Tere siwa koi mabood nahi.",
            "ऐ अल्लाह, तू पाक है और तेरी ही तारीफ़ है। तेरा नाम बरकत वाला है, तेरी शान बुलंद है और तेरे सिवा कोई माबूद नहीं।",
            "اے اللہ! تو پاک ہے اور تیری ہی تعریف ہے۔ تیرا نام برکت والا ہے، تیری شان بلند ہے اور تیرے سوا کوئی معبود نہیں۔"
        ),
        "Sunan Abi Dawud 775 · Jami` at-Tirmidhi 243"
    ),
    Recitation(
        "taawwuz", RecitationGroup.IN_PRAYER,
        t("Ta‘awwuz", "Ta'awwuz", "तअव्वुज़", "تعوذ"),
        "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ",
        "A‘udhu billahi minash-shaytanir-rajim.",
        t(
            "I seek refuge with Allah from Satan, the rejected.",
            "Main Allah ki panaah maangta hoon shaitan mardood se.",
            "मैं अल्लाह की पनाह माँगता हूँ शैतान मरदूद से।",
            "میں اللہ کی پناہ مانگتا ہوں شیطان مردود سے۔"
        ),
        "Quran 16:98"
    ),
    Recitation(
        "tasmiyah", RecitationGroup.IN_PRAYER,
        t("Tasmiyah (Bismillah)", "Tasmiya (Bismillah)", "तस्मिया (बिस्मिल्लाह)", "تسمیہ (بسم اللہ)"),
        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
        "Bismillahir-rahmanir-rahim.",
        t(
            "In the name of Allah, the Most Gracious, the Most Merciful.",
            "Allah ke naam se shuru, jo bahut meharban, nihayat reham wala hai.",
            "अल्लाह के नाम से शुरू, जो बहुत मेहरबान, निहायत रहम वाला है।",
            "اللہ کے نام سے شروع جو بہت مہربان، نہایت رحم والا ہے۔"
        ),
        "Quran 1:1",
        quran = "1:1-1"
    ),
    Recitation(
        "fatiha", RecitationGroup.IN_PRAYER,
        t("Surah al-Fatihah", "Surah Al-Fatiha", "सूरह अल-फ़ातिहा", "سورۃ الفاتحہ"),
        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nالْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ\nالرَّحْمَٰنِ الرَّحِيمِ\nمَالِكِ يَوْمِ الدِّينِ\nإِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ\nاهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ\nصِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ",
        "Bismillahir-rahmanir-rahim. Alhamdu lillahi rabbil-‘alamin. Ar-rahmanir-rahim. Maliki yawmid-din. Iyyaka na‘budu wa iyyaka nasta‘in. Ihdinas-siratal-mustaqim. Siratal-ladhina an‘amta ‘alayhim, ghayril-maghdubi ‘alayhim wa lad-dallin.",
        t(
            "In the name of Allah, the Most Gracious, the Most Merciful. All praise is for Allah, Lord of all the worlds; the Most Gracious, the Most Merciful; Master of the Day of Recompense. You alone we worship and You alone we ask for help. Guide us on the straight path: the path of those You have favoured, not of those who earned anger, nor of those who went astray.",
            "Allah ke naam se shuru, jo bahut meharban, nihayat reham wala hai. Sab tareef Allah ke liye hai jo tamam jahanon ka Rab hai; bahut meharban, nihayat reham wala; badle ke din ka Malik. Hum Teri hi ibadat karte hain aur Tujh hi se madad maangte hain. Humein seedha raasta chala; un logon ka raasta jin par Tune inaam kiya, na unka jin par ghazab hua aur na gumrahon ka.",
            "अल्लाह के नाम से शुरू, जो बहुत मेहरबान, निहायत रहम वाला है। सब तारीफ़ अल्लाह के लिए है जो तमाम जहानों का रब है; बहुत मेहरबान, निहायत रहम वाला; बदले के दिन का मालिक। हम तेरी ही इबादत करते हैं और तुझ ही से मदद माँगते हैं। हमें सीधा रास्ता चला; उन लोगों का रास्ता जिन पर तूने इनाम किया, न उनका जिन पर ग़ज़ब हुआ और न गुमराहों का।",
            "اللہ کے نام سے شروع جو بہت مہربان، نہایت رحم والا ہے۔ سب تعریف اللہ کے لیے ہے جو تمام جہانوں کا رب ہے؛ بہت مہربان، نہایت رحم والا؛ بدلے کے دن کا مالک۔ ہم تیری ہی عبادت کرتے ہیں اور تجھ ہی سے مدد مانگتے ہیں۔ ہمیں سیدھا راستہ چلا؛ ان لوگوں کا راستہ جن پر تو نے انعام کیا، نہ ان کا جن پر غضب ہوا اور نہ گمراہوں کا۔"
        ),
        "Quran 1:1–7 · Sahih al-Bukhari 756",
        t("Say Ameen quietly after it.", "Iske baad aahista Ameen kahein.", "इसके बाद आहिस्ता आमीन कहें।", "اس کے بعد آہستہ آمین کہیں۔"),
        quran = "1:1-7"
    ),
    Recitation(
        "ruku", RecitationGroup.IN_PRAYER,
        t("Tasbih of ruku‘", "Ruku ki tasbeeh", "रुकू की तस्बीह", "رکوع کی تسبیح"),
        "سُبْحَانَ رَبِّيَ الْعَظِيمِ",
        "Subhana rabbiyal-‘azim.",
        t("Glory be to my Lord, the Magnificent.", "Paak hai mera Rab, azmat wala.", "पाक है मेरा रब, अज़मत वाला।", "پاک ہے میرا رب، عظمت والا۔"),
        "Sunan Abi Dawud 871, 886",
        threeTimes
    ),
    Recitation(
        "tasmi", RecitationGroup.IN_PRAYER,
        t("Tasmi‘ (rising from ruku‘)", "Tasmee (ruku se uthte hue)", "तस्मीअ (रुकू से उठते हुए)", "تسمیع (رکوع سے اٹھتے ہوئے)"),
        "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ",
        "Sami‘allahu liman hamidah.",
        t(
            "Allah hears the one who praises Him.",
            "Allah ne uski sun li jisne uski tareef ki.",
            "अल्लाह ने उसकी सुन ली जिसने उसकी तारीफ़ की।",
            "اللہ نے اس کی سن لی جس نے اس کی تعریف کی۔"
        ),
        "Sahih al-Bukhari 795"
    ),
    Recitation(
        "tahmid", RecitationGroup.IN_PRAYER,
        t("Tahmid (standing upright)", "Tahmeed (seedhe khade hokar)", "तहमीद (सीधे खड़े होकर)", "تحمید (سیدھے کھڑے ہو کر)"),
        "رَبَّنَا وَلَكَ الْحَمْدُ",
        "Rabbana wa lakal-hamd.",
        t(
            "Our Lord, all praise is Yours.",
            "Aye hamare Rab, sab tareef Tere hi liye hai.",
            "ऐ हमारे रब, सब तारीफ़ तेरे ही लिए है।",
            "اے ہمارے رب! سب تعریف تیرے ہی لیے ہے۔"
        ),
        "Sahih al-Bukhari 732"
    ),
    Recitation(
        "sujud", RecitationGroup.IN_PRAYER,
        t("Tasbih of sajdah", "Sajde ki tasbeeh", "सजदे की तस्बीह", "سجدے کی تسبیح"),
        "سُبْحَانَ رَبِّيَ الْأَعْلَى",
        "Subhana rabbiyal-a‘la.",
        t("Glory be to my Lord, the Most High.", "Paak hai mera Rab, sab se buland.", "पाक है मेरा रब, सबसे बुलंद।", "پاک ہے میرا رب، سب سے بلند۔"),
        "Sunan Abi Dawud 871, 886",
        threeTimes
    ),
    Recitation(
        "between", RecitationGroup.IN_PRAYER,
        t("Between the two sajdahs", "Dono sajdon ke darmiyan", "दोनों सजदों के दरमियान", "دونوں سجدوں کے درمیان"),
        "رَبِّ اغْفِرْ لِي",
        "Rabbighfir li.",
        t("My Lord, forgive me.", "Aye mere Rab, mujhe bakhsh de.", "ऐ मेरे रब, मुझे बख़्श दे।", "اے میرے رب! مجھے بخش دے۔"),
        "Sunan Abi Dawud 874",
        t("Recommended, not required.", "Mustahab hai, zaroori nahi.", "मुस्तहब है, ज़रूरी नहीं।", "مستحب ہے، ضروری نہیں۔")
    ),
    Recitation(
        "tashahhud", RecitationGroup.IN_PRAYER,
        t("Tashahhud (At-Tahiyyat)", "At-Tahiyyat (Tashahhud)", "अत्तहिय्यात (तशह्हुद)", "التحیات (تشہد)"),
        "التَّحِيَّاتُ لِلَّهِ وَالصَّلَوَاتُ وَالطَّيِّبَاتُ، السَّلَامُ عَلَيْكَ أَيُّهَا النَّبِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ، السَّلَامُ عَلَيْنَا وَعَلَى عِبَادِ اللَّهِ الصَّالِحِينَ، أَشْهَدُ أَنْ لَا إِلَٰهَ إِلَّا اللَّهُ، وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
        "At-tahiyyatu lillahi was-salawatu wat-tayyibat. As-salamu ‘alayka ayyuhan-nabiyyu wa rahmatullahi wa barakatuh. As-salamu ‘alayna wa ‘ala ‘ibadillahis-salihin. Ash-hadu al-la ilaha illallah, wa ash-hadu anna Muhammadan ‘abduhu wa rasuluh.",
        t(
            "All greetings, prayers and pure words are for Allah. Peace be upon you, O Prophet, and the mercy of Allah and His blessings. Peace be upon us and upon the righteous servants of Allah. I bear witness that there is no god but Allah, and I bear witness that Muhammad ﷺ is His servant and Messenger.",
            "Tamam qauli, badni aur maali ibadatein Allah hi ke liye hain. Salam ho aap par aye Nabi, aur Allah ki rehmat aur uski barkatein. Salam ho hum par aur Allah ke nek bandon par. Main gawahi deta hoon ke Allah ke siwa koi mabood nahi, aur main gawahi deta hoon ke Muhammad ﷺ uske bande aur Rasool hain.",
            "तमाम क़ौली, बदनी और माली इबादतें अल्लाह ही के लिए हैं। सलाम हो आप पर ऐ नबी, और अल्लाह की रहमत और उसकी बरकतें। सलाम हो हम पर और अल्लाह के नेक बंदों पर। मैं गवाही देता हूँ कि अल्लाह के सिवा कोई माबूद नहीं, और मैं गवाही देता हूँ कि मुहम्मद ﷺ उसके बंदे और रसूल हैं।",
            "تمام قولی، بدنی اور مالی عبادتیں اللہ ہی کے لیے ہیں۔ سلام ہو آپ پر اے نبی، اور اللہ کی رحمت اور اس کی برکتیں۔ سلام ہو ہم پر اور اللہ کے نیک بندوں پر۔ میں گواہی دیتا ہوں کہ اللہ کے سوا کوئی معبود نہیں، اور میں گواہی دیتا ہوں کہ محمد ﷺ اس کے بندے اور رسول ہیں۔"
        ),
        "Sahih al-Bukhari 831"
    ),
    Recitation(
        "salawat", RecitationGroup.IN_PRAYER,
        t("Durood-e-Ibrahim", "Durood-e-Ibrahimi", "दुरूद-ए-इब्राहीमी", "درودِ ابراہیمی"),
        "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ، اللَّهُمَّ بَارِكْ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ كَمَا بَارَكْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ",
        "Allahumma salli ‘ala Muhammadin wa ‘ala ali Muhammadin kama sallayta ‘ala Ibrahima wa ‘ala ali Ibrahima innaka Hamidum-Majid. Allahumma barik ‘ala Muhammadin wa ‘ala ali Muhammadin kama barakta ‘ala Ibrahima wa ‘ala ali Ibrahima innaka Hamidum-Majid.",
        t(
            "O Allah, send Your mercy upon Muhammad ﷺ and the family of Muhammad ﷺ as You sent mercy upon Ibrahim and the family of Ibrahim; truly You are Praiseworthy, Glorious. O Allah, bless Muhammad ﷺ and the family of Muhammad ﷺ as You blessed Ibrahim and the family of Ibrahim; truly You are Praiseworthy, Glorious.",
            "Aye Allah, Muhammad ﷺ par aur unki aal par rehmat naazil farma jaise Tune Ibrahim (alaihis-salam) par aur unki aal par rehmat naazil farmayi; beshak Tu tareef wala, buzurgi wala hai. Aye Allah, Muhammad ﷺ par aur unki aal par barkat naazil farma jaise Tune Ibrahim (alaihis-salam) par aur unki aal par barkat naazil farmayi; beshak Tu tareef wala, buzurgi wala hai.",
            "ऐ अल्लाह, मुहम्मद ﷺ पर और उनकी आल पर रहमत नाज़िल फ़रमा जैसे तूने इब्राहीम (अलैहिस्सलाम) पर और उनकी आल पर रहमत नाज़िल फ़रमाई; बेशक तू तारीफ़ वाला, बुज़ुर्गी वाला है। ऐ अल्लाह, मुहम्मद ﷺ पर और उनकी आल पर बरकत नाज़िल फ़रमा जैसे तूने इब्राहीम (अलैहिस्सलाम) पर और उनकी आल पर बरकत नाज़िल फ़रमाई; बेशक तू तारीफ़ वाला, बुज़ुर्गी वाला है।",
            "اے اللہ! محمد ﷺ پر اور ان کی آل پر رحمت نازل فرما جیسے تو نے ابراہیم (علیہ السلام) پر اور ان کی آل پر رحمت نازل فرمائی؛ بے شک تو تعریف والا، بزرگی والا ہے۔ اے اللہ! محمد ﷺ پر اور ان کی آل پر برکت نازل فرما جیسے تو نے ابراہیم (علیہ السلام) پر اور ان کی آل پر برکت نازل فرمائی؛ بے شک تو تعریف والا، بزرگی والا ہے۔"
        ),
        "Sahih al-Bukhari 3370"
    ),
    Recitation(
        "dua_masura", RecitationGroup.IN_PRAYER,
        t("Dua after Durood (Dua-e-Masura)", "Dua-e-Masura", "दुआ-ए-मासूरा", "دعائے ماثورہ"),
        "اللَّهُمَّ إِنِّي ظَلَمْتُ نَفْسِي ظُلْمًا كَثِيرًا، وَلَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ، فَاغْفِرْ لِي مَغْفِرَةً مِنْ عِنْدِكَ، وَارْحَمْنِي، إِنَّكَ أَنْتَ الْغَفُورُ الرَّحِيمُ",
        "Allahumma inni zalamtu nafsi zulman kathira, wa la yaghfirudh-dhunuba illa Anta, faghfir li maghfiratam-min ‘indika warhamni, innaka Antal-Ghafurur-Rahim.",
        t(
            "O Allah, I have greatly wronged myself, and none forgives sins but You. So forgive me with a forgiveness from Yourself and have mercy on me. Truly You are the Forgiving, the Merciful.",
            "Aye Allah, maine apni jaan par bahut zulm kiya, aur Tere siwa gunahon ko koi nahi bakhshta. To apni taraf se mujhe bakhsh de aur mujh par reham farma; beshak Tu hi bakhshne wala, meharban hai.",
            "ऐ अल्लाह, मैंने अपनी जान पर बहुत ज़ुल्म किया, और तेरे सिवा गुनाहों को कोई नहीं बख़्शता। तो अपनी तरफ़ से मुझे बख़्श दे और मुझ पर रहम फ़रमा; बेशक तू ही बख़्शने वाला, मेहरबान है।",
            "اے اللہ! میں نے اپنی جان پر بہت ظلم کیا، اور تیرے سوا گناہوں کو کوئی نہیں بخشتا۔ تو اپنی طرف سے مجھے بخش دے اور مجھ پر رحم فرما؛ بے شک تو ہی بخشنے والا، مہربان ہے۔"
        ),
        "Sahih al-Bukhari 834"
    ),
    Recitation(
        "dua_ibrahim", RecitationGroup.IN_PRAYER,
        t("Another dua after Durood", "Durood ke baad doosri dua", "दुरूद के बाद दूसरी दुआ", "درود کے بعد دوسری دعا"),
        "رَبِّ اجْعَلْنِي مُقِيمَ الصَّلَاةِ وَمِن ذُرِّيَّتِي ۚ رَبَّنَا وَتَقَبَّلْ دُعَاءِ\nرَبَّنَا اغْفِرْ لِي وَلِوَالِدَيَّ وَلِلْمُؤْمِنِينَ يَوْمَ يَقُومُ الْحِسَابُ",
        "Rabbij‘alni muqimas-salati wa min dhurriyyati, Rabbana wa taqabbal du‘a. Rabbanaghfir li wa li-walidayya wa lil-mu’minina yawma yaqumul-hisab.",
        t(
            "My Lord, make me one who establishes the prayer, and my descendants too. Our Lord, accept my supplication. Our Lord, forgive me, my parents and the believers on the Day the reckoning is held.",
            "Aye mere Rab, mujhe namaz qaim karne wala bana aur meri aulad ko bhi. Aye hamare Rab, meri dua qubool farma. Aye hamare Rab, mujhe, mere maa-baap ko aur sab imaan walon ko us din bakhsh de jis din hisab qaim hoga.",
            "ऐ मेरे रब, मुझे नमाज़ क़ायम करने वाला बना और मेरी औलाद को भी। ऐ हमारे रब, मेरी दुआ क़ुबूल फ़रमा। ऐ हमारे रब, मुझे, मेरे माँ-बाप को और सब ईमान वालों को उस दिन बख़्श दे जिस दिन हिसाब क़ायम होगा।",
            "اے میرے رب! مجھے نماز قائم کرنے والا بنا اور میری اولاد کو بھی۔ اے ہمارے رب! میری دعا قبول فرما۔ اے ہمارے رب! مجھے، میرے ماں باپ کو اور سب ایمان والوں کو اس دن بخش دے جس دن حساب قائم ہوگا۔"
        ),
        "Quran 14:40–41",
        quran = "14:40-41"
    ),
    Recitation(
        "salam", RecitationGroup.IN_PRAYER,
        t("Salam", "Salam", "सलाम", "سلام"),
        "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ",
        "As-salamu ‘alaykum wa rahmatullah.",
        t(
            "Peace be upon you, and the mercy of Allah.",
            "Tum par salamti ho aur Allah ki rehmat.",
            "तुम पर सलामती हो और अल्लाह की रहमत।",
            "تم پر سلامتی ہو اور اللہ کی رحمت۔"
        ),
        "Sunan Abi Dawud 996 · Sahih Muslim 582"
    ),

    // ---------------------------------------------------------------- Short surahs
    Recitation(
        "asr", RecitationGroup.SURAHS,
        t("Surah al-‘Asr", "Surah Al-Asr", "सूरह अल-अस्र", "سورۃ العصر"),
        "وَالْعَصْرِ\nإِنَّ الْإِنسَانَ لَفِي خُسْرٍ\nإِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ",
        "Wal-‘asr. Innal-insana lafi khusr. Illal-ladhina amanu wa ‘amilus-salihati wa tawasaw bil-haqqi wa tawasaw bis-sabr.",
        t(
            "By time! Truly man is in loss, except those who believe and do good deeds, and urge one another to the truth and urge one another to patience.",
            "Zamane ki qasam! Beshak insaan zaroor nuqsan mein hai, siwaye un logon ke jo imaan laye aur nek amal kiye, aur ek doosre ko haq ki takeed ki aur ek doosre ko sabr ki wasiyyat ki.",
            "ज़माने की क़सम! बेशक इंसान ज़रूर नुक़सान में है, सिवाय उन लोगों के जो ईमान लाए और नेक अमल किए, और एक-दूसरे को हक़ की ताकीद की और एक-दूसरे को सब्र की वसीयत की।",
            "زمانے کی قسم! بے شک انسان ضرور نقصان میں ہے، سوائے ان لوگوں کے جو ایمان لائے اور نیک عمل کیے، اور ایک دوسرے کو حق کی تاکید کی اور ایک دوسرے کو صبر کی وصیت کی۔"
        ),
        "Quran 103:1–3",
        quran = "103:1-3"
    ),
    Recitation(
        "kawthar", RecitationGroup.SURAHS,
        t("Surah al-Kawthar", "Surah Al-Kausar", "सूरह अल-कौसर", "سورۃ الکوثر"),
        "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ\nفَصَلِّ لِرَبِّكَ وَانْحَرْ\nإِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ",
        "Inna a‘taynakal-kawthar. Fa-salli li-rabbika wanhar. Inna shani’aka huwal-abtar.",
        t(
            "Truly We have granted you al-Kawthar, abundant good. So pray to your Lord and offer the sacrifice. Truly it is your enemy who is cut off from all good.",
            "Beshak humne aap ko Kausar (be-shumar khoobiyan) ata farmayi. To apne Rab ke liye namaz padhiye aur qurbani kijiye. Beshak aap ka dushman hi har khair se mehroom hai.",
            "बेशक हमने आपको कौसर (बेशुमार ख़ूबियाँ) अता फ़रमाई। तो अपने रब के लिए नमाज़ पढ़िए और क़ुर्बानी कीजिए। बेशक आपका दुश्मन ही हर ख़ैर से महरूम है।",
            "بے شک ہم نے آپ کو کوثر (بے شمار خوبیاں) عطا فرمائی۔ تو اپنے رب کے لیے نماز پڑھیے اور قربانی کیجیے۔ بے شک آپ کا دشمن ہی ہر خیر سے محروم ہے۔"
        ),
        "Quran 108:1–3",
        quran = "108:1-3"
    ),
    Recitation(
        "ikhlas", RecitationGroup.SURAHS,
        t("Surah al-Ikhlas", "Surah Al-Ikhlas", "सूरह अल-इख़लास", "سورۃ الاخلاص"),
        "قُلْ هُوَ اللَّهُ أَحَدٌ\nاللَّهُ الصَّمَدُ\nلَمْ يَلِدْ وَلَمْ يُولَدْ\nوَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ",
        "Qul huwallahu ahad. Allahus-samad. Lam yalid wa lam yulad. Wa lam yakul-lahu kufuwan ahad.",
        t(
            "Say: He is Allah, the One. Allah, the Self-Sufficient on whom all depend. He begets not, nor was He begotten, and there is none equal to Him.",
            "Kaho: woh Allah ek hai. Allah be-niyaz hai. Na uski koi aulad hai aur na woh kisi se paida hua. Aur uske barabar ka koi nahi.",
            "कहो: वह अल्लाह एक है। अल्लाह बेनियाज़ है। न उसकी कोई औलाद है और न वह किसी से पैदा हुआ। और उसके बराबर का कोई नहीं।",
            "کہو: وہ اللہ ایک ہے۔ اللہ بے نیاز ہے۔ نہ اس کی کوئی اولاد ہے اور نہ وہ کسی سے پیدا ہوا۔ اور اس کے برابر کا کوئی نہیں۔"
        ),
        "Quran 112:1–4",
        quran = "112:1-4"
    ),
    Recitation(
        "falaq", RecitationGroup.SURAHS,
        t("Surah al-Falaq", "Surah Al-Falaq", "सूरह अल-फ़लक़", "سورۃ الفلق"),
        "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ\nمِن شَرِّ مَا خَلَقَ\nوَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ\nوَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ\nوَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ",
        "Qul a‘udhu bi-rabbil-falaq. Min sharri ma khalaq. Wa min sharri ghasiqin idha waqab. Wa min sharrin-naffathati fil-‘uqad. Wa min sharri hasidin idha hasad.",
        t(
            "Say: I seek refuge with the Lord of daybreak, from the evil of what He has created, from the evil of the darkness when it settles, from the evil of those who blow on knots, and from the evil of an envier when he envies.",
            "Kaho: main subah ke Rab ki panaah leta hoon, uski tamam makhlooq ke shar se, aur andheri raat ke shar se jab woh chha jaye, aur girhon mein phoonkne waliyon ke shar se, aur hasad karne wale ke shar se jab woh hasad kare.",
            "कहो: मैं सुबह के रब की पनाह लेता हूँ, उसकी तमाम मख़लूक़ के शर से, और अँधेरी रात के शर से जब वह छा जाए, और गिरहों में फूँकने वालियों के शर से, और हसद करने वाले के शर से जब वह हसद करे।",
            "کہو: میں صبح کے رب کی پناہ لیتا ہوں، اس کی تمام مخلوق کے شر سے، اور اندھیری رات کے شر سے جب وہ چھا جائے، اور گرہوں میں پھونکنے والیوں کے شر سے، اور حسد کرنے والے کے شر سے جب وہ حسد کرے۔"
        ),
        "Quran 113:1–5",
        quran = "113:1-5"
    ),
    Recitation(
        "nas", RecitationGroup.SURAHS,
        t("Surah an-Nas", "Surah An-Naas", "सूरह अन-नास", "سورۃ الناس"),
        "قُلْ أَعُوذُ بِرَبِّ النَّاسِ\nمَلِكِ النَّاسِ\nإِلَٰهِ النَّاسِ\nمِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ\nالَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ\nمِنَ الْجِنَّةِ وَالنَّاسِ",
        "Qul a‘udhu bi-rabbin-nas. Malikin-nas. Ilahin-nas. Min sharril-waswasil-khannas. Alladhi yuwaswisu fi sudurin-nas. Minal-jinnati wan-nas.",
        t(
            "Say: I seek refuge with the Lord of mankind, the King of mankind, the God of mankind, from the evil of the whisperer who withdraws, who whispers into the hearts of mankind, from among jinn and mankind.",
            "Kaho: main logon ke Rab ki panaah leta hoon, logon ke Baadshah ki, logon ke Mabood ki, us waswasa daalne wale ke shar se jo peeche hat jata hai, jo logon ke dilon mein waswase daalta hai, jinnon mein se ho ya insanon mein se.",
            "कहो: मैं लोगों के रब की पनाह लेता हूँ, लोगों के बादशाह की, लोगों के माबूद की, उस वसवसा डालने वाले के शर से जो पीछे हट जाता है, जो लोगों के दिलों में वसवसे डालता है, जिन्नों में से हो या इंसानों में से।",
            "کہو: میں لوگوں کے رب کی پناہ لیتا ہوں، لوگوں کے بادشاہ کی، لوگوں کے معبود کی، اس وسوسہ ڈالنے والے کے شر سے جو پیچھے ہٹ جاتا ہے، جو لوگوں کے دلوں میں وسوسے ڈالتا ہے، جنوں میں سے ہو یا انسانوں میں سے۔"
        ),
        "Quran 114:1–6",
        quran = "114:1-6"
    ),

    // ---------------------------------------------------------------- Witr
    Recitation(
        "qunut_hanafi", RecitationGroup.WITR,
        t("Dua-e-Qunut", "Dua-e-Qunoot", "दुआ-ए-क़ुनूत", "دعائے قنوت"),
        "اللَّهُمَّ إِنَّا نَسْتَعِينُكَ وَنَسْتَغْفِرُكَ وَنُؤْمِنُ بِكَ وَنَتَوَكَّلُ عَلَيْكَ وَنُثْنِي عَلَيْكَ الْخَيْرَ وَنَشْكُرُكَ وَلَا نَكْفُرُكَ وَنَخْلَعُ وَنَتْرُكُ مَنْ يَفْجُرُكَ، اللَّهُمَّ إِيَّاكَ نَعْبُدُ وَلَكَ نُصَلِّي وَنَسْجُدُ وَإِلَيْكَ نَسْعَىٰ وَنَحْفِدُ وَنَرْجُو رَحْمَتَكَ وَنَخْشَىٰ عَذَابَكَ إِنَّ عَذَابَكَ بِالْكُفَّارِ مُلْحِقٌ",
        "Allahumma inna nasta‘inuka wa nastaghfiruka wa nu’minu bika wa natawakkalu ‘alayka wa nuthni ‘alaykal-khayr, wa nashkuruka wa la nakfuruka wa nakhla‘u wa natruku man yafjuruk. Allahumma iyyaka na‘budu wa laka nusalli wa nasjudu wa ilayka nas‘a wa nahfidu wa narju rahmataka wa nakhsha ‘adhabaka, inna ‘adhabaka bil-kuffari mulhiq.",
        t(
            "O Allah, we seek Your help and Your forgiveness; we believe in You and rely on You; we praise You with all good, we thank You and are not ungrateful to You, and we part from and leave whoever disobeys You. O Allah, You alone we worship; to You we pray and prostrate; towards You we hasten and strive. We hope for Your mercy and fear Your punishment; surely Your punishment overtakes the disbelievers.",
            "Aye Allah, hum Tujh se madad aur maghfirat maangte hain, Tujh par imaan laate aur Tujh par bharosa karte hain, Teri achhi tareef karte hain, Tera shukr karte hain aur Teri na-shukri nahi karte, aur jo Teri nafarmani kare usse alag hote aur use chhod dete hain. Aye Allah, hum Teri hi ibadat karte hain, Tere hi liye namaz padhte aur sajda karte hain, Teri hi taraf daudte aur lapakte hain, Teri rehmat ke ummeedwar hain aur Tere azaab se darte hain; beshak Tera azaab kafiron ko pahunchne wala hai.",
            "ऐ अल्लाह, हम तुझसे मदद और मग़फ़िरत माँगते हैं, तुझ पर ईमान लाते और तुझ पर भरोसा करते हैं, तेरी अच्छी तारीफ़ करते हैं, तेरा शुक्र करते हैं और तेरी नाशुक्री नहीं करते, और जो तेरी नाफ़रमानी करे उससे अलग होते और उसे छोड़ देते हैं। ऐ अल्लाह, हम तेरी ही इबादत करते हैं, तेरे ही लिए नमाज़ पढ़ते और सजदा करते हैं, तेरी ही तरफ़ दौड़ते और लपकते हैं, तेरी रहमत के उम्मीदवार हैं और तेरे अज़ाब से डरते हैं; बेशक तेरा अज़ाब काफ़िरों को पहुँचने वाला है।",
            "اے اللہ! ہم تجھ سے مدد اور مغفرت مانگتے ہیں، تجھ پر ایمان لاتے اور تجھ پر بھروسا کرتے ہیں، تیری اچھی تعریف کرتے ہیں، تیرا شکر کرتے ہیں اور تیری ناشکری نہیں کرتے، اور جو تیری نافرمانی کرے اس سے الگ ہوتے اور اسے چھوڑ دیتے ہیں۔ اے اللہ! ہم تیری ہی عبادت کرتے ہیں، تیرے ہی لیے نماز پڑھتے اور سجدہ کرتے ہیں، تیری ہی طرف دوڑتے اور لپکتے ہیں، تیری رحمت کے امیدوار ہیں اور تیرے عذاب سے ڈرتے ہیں؛ بے شک تیرا عذاب کافروں کو پہنچنے والا ہے۔"
        ),
        "Musannaf Ibn Abi Shaybah 7031 · as-Sunan al-Kubra (al-Bayhaqi) 3143",
        t(
            "The wording recited in the Hanafi school.",
            "Hanafi mazhab mein yahi dua padhi jati hai.",
            "हनफ़ी मज़हब में यही दुआ पढ़ी जाती है।",
            "حنفی مذہب میں یہی دعا پڑھی جاتی ہے۔"
        )
    ),
    Recitation(
        "rabbana_atina", RecitationGroup.WITR,
        t("If you have not memorised the Qunut", "Jise Dua-e-Qunoot yaad na ho", "जिसे दुआ-ए-क़ुनूत याद न हो", "جسے دعائے قنوت یاد نہ ہو"),
        "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
        "Rabbana atina fid-dunya hasanatan wa fil-akhirati hasanatan wa qina ‘adhaban-nar.",
        t(
            "Our Lord, give us good in this world and good in the Hereafter, and save us from the punishment of the Fire.",
            "Aye hamare Rab, humein duniya mein bhalai de aur aakhirat mein bhalai de, aur humein dozakh ke azaab se bacha.",
            "ऐ हमारे रब, हमें दुनिया में भलाई दे और आख़िरत में भलाई दे, और हमें दोज़ख़ के अज़ाब से बचा।",
            "اے ہمارے رب! ہمیں دنیا میں بھلائی دے اور آخرت میں بھلائی دے، اور ہمیں دوزخ کے عذاب سے بچا۔"
        ),
        "Quran 2:201",
        quran = "2:201-201"
    ),
    Recitation(
        "qunut", RecitationGroup.WITR,
        t("Qunut (Allahummahdini)", "Qunoot (Allahummahdini)", "क़ुनूत (अल्लाहुम्मह्दिनी)", "قنوت (اللّٰہم اھدنی)"),
        "اللَّهُمَّ اهْدِنِي فِيمَنْ هَدَيْتَ، وَعَافِنِي فِيمَنْ عَافَيْتَ، وَتَوَلَّنِي فِيمَنْ تَوَلَّيْتَ، وَبَارِكْ لِي فِيمَا أَعْطَيْتَ، وَقِنِي شَرَّ مَا قَضَيْتَ، فَإِنَّكَ تَقْضِي وَلَا يُقْضَىٰ عَلَيْكَ، وَإِنَّهُ لَا يَذِلُّ مَنْ وَالَيْتَ، تَبَارَكْتَ رَبَّنَا وَتَعَالَيْتَ",
        "Allahummahdini fiman hadayt, wa ‘afini fiman ‘afayt, wa tawallani fiman tawallayt, wa barik li fima a‘tayt, wa qini sharra ma qadayt, fa-innaka taqdi wa la yuqda ‘alayk, wa innahu la yadhillu man walayt, tabarakta Rabbana wa ta‘alayt.",
        t(
            "O Allah, guide me among those You have guided, grant me well-being among those You have granted well-being, take me into Your care among those You have taken into Your care, bless me in what You have given, and protect me from the evil of what You have decreed. For You decree and none decrees against You, and the one You befriend is never humiliated. Blessed and exalted are You, our Lord.",
            "Aye Allah, mujhe hidayat de un mein jinhein Tune hidayat di, mujhe aafiyat de un mein jinhein Tune aafiyat di, meri sarparasti farma un mein jinki Tune sarparasti ki, jo Tune diya usmein barkat de, aur jo Tune faisla kiya uske shar se mujhe bacha. Beshak Tu faisla karta hai aur Tere khilaf faisla nahi hota, aur jiska Tu dost ho woh zaleel nahi hota. Aye hamare Rab, Tu barkat wala aur buland hai.",
            "ऐ अल्लाह, मुझे हिदायत दे उनमें जिन्हें तूने हिदायत दी, मुझे आफ़ियत दे उनमें जिन्हें तूने आफ़ियत दी, मेरी सरपरस्ती फ़रमा उनमें जिनकी तूने सरपरस्ती की, जो तूने दिया उसमें बरकत दे, और जो तूने फ़ैसला किया उसके शर से मुझे बचा। बेशक तू फ़ैसला करता है और तेरे ख़िलाफ़ फ़ैसला नहीं होता, और जिसका तू दोस्त हो वह ज़लील नहीं होता। ऐ हमारे रब, तू बरकत वाला और बुलंद है।",
            "اے اللہ! مجھے ہدایت دے ان میں جنہیں تو نے ہدایت دی، مجھے عافیت دے ان میں جنہیں تو نے عافیت دی، میری سرپرستی فرما ان میں جن کی تو نے سرپرستی کی، جو تو نے دیا اس میں برکت دے، اور جو تو نے فیصلہ کیا اس کے شر سے مجھے بچا۔ بے شک تو فیصلہ کرتا ہے اور تیرے خلاف فیصلہ نہیں ہوتا، اور جس کا تو دوست ہو وہ ذلیل نہیں ہوتا۔ اے ہمارے رب! تو برکت والا اور بلند ہے۔"
        ),
        "Jami` at-Tirmidhi 464 · Sunan Abi Dawud 1425",
        t(
            "The wording recited in the Shafi‘i and Hanbali schools.",
            "Shafi'i aur Hanbali mazhab mein yeh dua padhi jati hai.",
            "शाफ़ई और हंबली मज़हब में यह दुआ पढ़ी जाती है।",
            "شافعی اور حنبلی مذہب میں یہ دعا پڑھی جاتی ہے۔"
        )
    ),

    // ---------------------------------------------------------------- After the salam
    Recitation(
        "after_istighfar", RecitationGroup.AFTER_SALAM,
        t("Istighfar after the salam", "Salam ke baad istighfar", "सलाम के बाद इस्तिग़फ़ार", "سلام کے بعد استغفار"),
        "أَسْتَغْفِرُ اللَّهَ\nاللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
        "Astaghfirullah. Allahumma Antas-Salamu wa minkas-salam, tabarakta ya Dhal-Jalali wal-Ikram.",
        t(
            "I seek Allah's forgiveness. O Allah, You are Peace and from You comes peace; blessed are You, O Possessor of majesty and honour.",
            "Main Allah se maghfirat maangta hoon. Aye Allah, Tu hi Salam hai aur Tujh hi se salamti hai; Tu barkat wala hai, aye jalal aur ikram wale.",
            "मैं अल्लाह से मग़फ़िरत माँगता हूँ। ऐ अल्लाह, तू ही सलाम है और तुझ ही से सलामती है; तू बरकत वाला है, ऐ जलाल और इकराम वाले।",
            "میں اللہ سے مغفرت مانگتا ہوں۔ اے اللہ! تو ہی سلام ہے اور تجھ ہی سے سلامتی ہے؛ تو برکت والا ہے، اے جلال اور اکرام والے۔"
        ),
        "Sahih Muslim 591, 592",
        t("Say Astaghfirullah three times.", "Astaghfirullah teen baar kahein.", "अस्तग़फ़िरुल्लाह तीन बार कहें।", "استغفر اللہ تین بار کہیں۔")
    ),
    Recitation(
        "ayat_kursi", RecitationGroup.AFTER_SALAM,
        t("Ayat al-Kursi", "Ayatul Kursi", "आयतुल कुर्सी", "آیت الکرسی"),
        "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
        "Allahu la ilaha illa Huwal-Hayyul-Qayyum. La ta’khudhuhu sinatun wa la nawm. Lahu ma fis-samawati wa ma fil-ard. Man dhal-ladhi yashfa‘u ‘indahu illa bi-idhnih. Ya‘lamu ma bayna aydihim wa ma khalfahum, wa la yuhituna bi-shay’im-min ‘ilmihi illa bima sha’. Wasi‘a kursiyyuhus-samawati wal-ard, wa la ya’uduhu hifzuhuma, wa Huwal-‘Aliyyul-‘Azim.",
        t(
            "Allah — there is no god but He, the Ever-Living, the Sustainer of all. Neither drowsiness nor sleep overtakes Him. To Him belongs whatever is in the heavens and whatever is on the earth. Who can intercede with Him except by His permission? He knows what is before them and what is behind them, and they encompass nothing of His knowledge except what He wills. His Kursi extends over the heavens and the earth, and guarding them does not tire Him. He is the Most High, the Magnificent.",
            "Allah, uske siwa koi mabood nahi; woh zinda hai, sab ko qaim rakhne wala. Use na oongh aati hai na neend. Jo kuch aasmanon mein hai aur jo kuch zameen mein hai sab usi ka hai. Kaun hai jo uski ijazat ke baghair uske paas sifarish kare? Woh jaanta hai jo unke aage hai aur jo unke peeche hai, aur woh uske ilm mein se kisi cheez ka ihata nahi kar sakte magar jitna woh chahe. Uski Kursi aasmanon aur zameen ko ghere hue hai, aur unki hifazat use thakati nahi. Wohi buland, azmat wala hai.",
            "अल्लाह, उसके सिवा कोई माबूद नहीं; वह ज़िंदा है, सबको क़ायम रखने वाला। उसे न ऊँघ आती है न नींद। जो कुछ आसमानों में है और जो कुछ ज़मीन में है सब उसी का है। कौन है जो उसकी इजाज़त के बग़ैर उसके पास सिफ़ारिश करे? वह जानता है जो उनके आगे है और जो उनके पीछे है, और वे उसके इल्म में से किसी चीज़ का इहाता नहीं कर सकते मगर जितना वह चाहे। उसकी कुर्सी आसमानों और ज़मीन को घेरे हुए है, और उनकी हिफ़ाज़त उसे थकाती नहीं। वही बुलंद, अज़मत वाला है।",
            "اللہ، اس کے سوا کوئی معبود نہیں؛ وہ زندہ ہے، سب کو قائم رکھنے والا۔ اسے نہ اونگھ آتی ہے نہ نیند۔ جو کچھ آسمانوں میں ہے اور جو کچھ زمین میں ہے سب اسی کا ہے۔ کون ہے جو اس کی اجازت کے بغیر اس کے پاس سفارش کرے؟ وہ جانتا ہے جو ان کے آگے ہے اور جو ان کے پیچھے ہے، اور وہ اس کے علم میں سے کسی چیز کا احاطہ نہیں کر سکتے مگر جتنا وہ چاہے۔ اس کی کرسی آسمانوں اور زمین کو گھیرے ہوئے ہے، اور ان کی حفاظت اسے تھکاتی نہیں۔ وہی بلند، عظمت والا ہے۔"
        ),
        "Quran 2:255 · as-Sunan al-Kubra (an-Nasa'i) 9848",
        quran = "2:255-255"
    ),
    Recitation(
        "tasbih_fatimi", RecitationGroup.AFTER_SALAM,
        t("Tasbih of Fatimah", "Tasbeeh-e-Fatima", "तस्बीह-ए-फ़ातिमा", "تسبیحِ فاطمہ"),
        "سُبْحَانَ اللَّهِ\nالْحَمْدُ لِلَّهِ\nاللَّهُ أَكْبَرُ",
        "Subhanallah. Alhamdulillah. Allahu Akbar.",
        t(
            "Glory be to Allah. All praise is for Allah. Allah is the Greatest.",
            "Allah paak hai. Sab tareef Allah ke liye hai. Allah sab se bada hai.",
            "अल्लाह पाक है। सब तारीफ़ अल्लाह के लिए है। अल्लाह सबसे बड़ा है।",
            "اللہ پاک ہے۔ سب تعریف اللہ کے لیے ہے۔ اللہ سب سے بڑا ہے۔"
        ),
        "Sahih Muslim 596",
        t(
            "Subhanallah 33 times, Alhamdulillah 33 times, Allahu Akbar 34 times.",
            "Subhanallah 33 baar, Alhamdulillah 33 baar, Allahu Akbar 34 baar.",
            "सुब्हानल्लाह 33 बार, अल्हम्दुलिल्लाह 33 बार, अल्लाहु अकबर 34 बार।",
            "سبحان اللہ 33 بار، الحمد للہ 33 بار، اللہ اکبر 34 بار۔"
        )
    ),

    // ---------------------------------------------------------------- Janazah and other prayers
    Recitation(
        "janazah_adult", RecitationGroup.SPECIAL,
        t("Janazah dua — adult", "Janaze ki dua — baaligh", "जनाज़े की दुआ — बालिग़", "جنازے کی دعا — بالغ"),
        "اللَّهُمَّ اغْفِرْ لِحَيِّنَا وَمَيِّتِنَا، وَشَاهِدِنَا وَغَائِبِنَا، وَصَغِيرِنَا وَكَبِيرِنَا، وَذَكَرِنَا وَأُنْثَانَا، اللَّهُمَّ مَنْ أَحْيَيْتَهُ مِنَّا فَأَحْيِهِ عَلَى الْإِسْلَامِ، وَمَنْ تَوَفَّيْتَهُ مِنَّا فَتَوَفَّهُ عَلَى الْإِيمَانِ",
        "Allahummaghfir li-hayyina wa mayyitina, wa shahidina wa gha’ibina, wa saghirina wa kabirina, wa dhakarina wa unthana. Allahumma man ahyaytahu minna fa-ahyihi ‘alal-Islam, wa man tawaffaytahu minna fa-tawaffahu ‘alal-iman.",
        t(
            "O Allah, forgive our living and our dead, those present and those absent, our young and our old, our men and our women. O Allah, whomever of us You keep alive, keep him alive upon Islam, and whomever of us You take, take him upon faith.",
            "Aye Allah, hamare zindon aur murdon ko, haazir aur ghaib ko, chhoton aur badon ko, mardon aur auraton ko bakhsh de. Aye Allah, hum mein se jise Tu zinda rakhe use Islam par zinda rakh, aur jise maut de use imaan par maut de.",
            "ऐ अल्लाह, हमारे ज़िंदों और मुर्दों को, हाज़िर और ग़ायब को, छोटों और बड़ों को, मर्दों और औरतों को बख़्श दे। ऐ अल्लाह, हम में से जिसे तू ज़िंदा रखे उसे इस्लाम पर ज़िंदा रख, और जिसे मौत दे उसे ईमान पर मौत दे।",
            "اے اللہ! ہمارے زندوں اور مردوں کو، حاضر اور غائب کو، چھوٹوں اور بڑوں کو، مردوں اور عورتوں کو بخش دے۔ اے اللہ! ہم میں سے جسے تو زندہ رکھے اسے اسلام پر زندہ رکھ، اور جسے موت دے اسے ایمان پر موت دے۔"
        ),
        "Jami` at-Tirmidhi 1024 · Sunan Abi Dawud 3201"
    ),
    Recitation(
        "janazah_boy", RecitationGroup.SPECIAL,
        t("Janazah dua — boy", "Janaze ki dua — na-baaligh ladka", "जनाज़े की दुआ — नाबालिग़ लड़का", "جنازے کی دعا — نابالغ لڑکا"),
        "اللَّهُمَّ اجْعَلْهُ لَنَا فَرَطًا، وَاجْعَلْهُ لَنَا أَجْرًا وَذُخْرًا، وَاجْعَلْهُ لَنَا شَافِعًا وَمُشَفَّعًا",
        "Allahummaj‘alhu lana faratan, waj‘alhu lana ajran wa dhukhran, waj‘alhu lana shafi‘an wa mushaffa‘a.",
        t(
            "O Allah, make him one who goes ahead to prepare for us, make him a reward and a treasure for us, and make him an intercessor for us whose intercession is accepted.",
            "Aye Allah, ise hamare liye aage jaakar saman karne wala bana, ise hamare liye ajr aur zakheera bana, aur ise hamari sifarish karne wala bana jiski sifarish qubool ho.",
            "ऐ अल्लाह, इसे हमारे लिए आगे जाकर सामान करने वाला बना, इसे हमारे लिए अज्र और ज़ख़ीरा बना, और इसे हमारी सिफ़ारिश करने वाला बना जिसकी सिफ़ारिश क़ुबूल हो।",
            "اے اللہ! اسے ہمارے لیے آگے جا کر سامان کرنے والا بنا، اسے ہمارے لیے اجر اور ذخیرہ بنا، اور اسے ہماری سفارش کرنے والا بنا جس کی سفارش قبول ہو۔"
        ),
        "Sahih al-Bukhari, Kitab al-Jana'iz (ta‘liq, from al-Hasan al-Basri)"
    ),
    Recitation(
        "janazah_girl", RecitationGroup.SPECIAL,
        t("Janazah dua — girl", "Janaze ki dua — na-baaligh ladki", "जनाज़े की दुआ — नाबालिग़ लड़की", "جنازے کی دعا — نابالغ لڑکی"),
        "اللَّهُمَّ اجْعَلْهَا لَنَا فَرَطًا، وَاجْعَلْهَا لَنَا أَجْرًا وَذُخْرًا، وَاجْعَلْهَا لَنَا شَافِعَةً وَمُشَفَّعَةً",
        "Allahummaj‘alha lana faratan, waj‘alha lana ajran wa dhukhran, waj‘alha lana shafi‘atan wa mushaffa‘ah.",
        t(
            "O Allah, make her one who goes ahead to prepare for us, make her a reward and a treasure for us, and make her an intercessor for us whose intercession is accepted.",
            "Aye Allah, ise hamare liye aage jaakar saman karne wali bana, ise hamare liye ajr aur zakheera bana, aur ise hamari sifarish karne wali bana jiski sifarish qubool ho.",
            "ऐ अल्लाह, इसे हमारे लिए आगे जाकर सामान करने वाली बना, इसे हमारे लिए अज्र और ज़ख़ीरा बना, और इसे हमारी सिफ़ारिश करने वाली बना जिसकी सिफ़ारिश क़ुबूल हो।",
            "اے اللہ! اسے ہمارے لیے آگے جا کر سامان کرنے والی بنا، اسے ہمارے لیے اجر اور ذخیرہ بنا، اور اسے ہماری سفارش کرنے والی بنا جس کی سفارش قبول ہو۔"
        ),
        "Sahih al-Bukhari, Kitab al-Jana'iz (ta‘liq, from al-Hasan al-Basri)"
    ),
    Recitation(
        "istikhara", RecitationGroup.SPECIAL,
        t("Dua of Istikhara", "Istikhare ki dua", "इस्तिख़ारे की दुआ", "استخارے کی دعا"),
        "اللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ، وَأَسْتَقْدِرُكَ بِقُدْرَتِكَ، وَأَسْأَلُكَ مِنْ فَضْلِكَ الْعَظِيمِ، فَإِنَّكَ تَقْدِرُ وَلَا أَقْدِرُ، وَتَعْلَمُ وَلَا أَعْلَمُ، وَأَنْتَ عَلَّامُ الْغُيُوبِ، اللَّهُمَّ إِنْ كُنْتَ تَعْلَمُ أَنَّ هَٰذَا الْأَمْرَ خَيْرٌ لِي فِي دِينِي وَمَعَاشِي وَعَاقِبَةِ أَمْرِي فَاقْدُرْهُ لِي وَيَسِّرْهُ لِي ثُمَّ بَارِكْ لِي فِيهِ، وَإِنْ كُنْتَ تَعْلَمُ أَنَّ هَٰذَا الْأَمْرَ شَرٌّ لِي فِي دِينِي وَمَعَاشِي وَعَاقِبَةِ أَمْرِي فَاصْرِفْهُ عَنِّي وَاصْرِفْنِي عَنْهُ، وَاقْدُرْ لِيَ الْخَيْرَ حَيْثُ كَانَ ثُمَّ أَرْضِنِي بِهِ",
        "Allahumma inni astakhiruka bi-‘ilmika, wa astaqdiruka bi-qudratika, wa as’aluka min fadlikal-‘azim, fa-innaka taqdiru wa la aqdiru, wa ta‘lamu wa la a‘lamu, wa Anta ‘allamul-ghuyub. Allahumma in kunta ta‘lamu anna hadhal-amra khayrul-li fi dini wa ma‘ashi wa ‘aqibati amri faqdurhu li wa yassirhu li thumma barik li fih, wa in kunta ta‘lamu anna hadhal-amra sharrul-li fi dini wa ma‘ashi wa ‘aqibati amri fasrifhu ‘anni wasrifni ‘anhu, waqdur liyal-khayra haythu kana thumma ardini bih.",
        t(
            "O Allah, I seek what is good from You by Your knowledge, I seek ability from You by Your power, and I ask You of Your immense bounty; for You have power and I have none, You know and I do not, and You are the Knower of the unseen. O Allah, if You know this matter to be good for me in my religion, my livelihood and the outcome of my affair, then decree it for me, make it easy for me and bless me in it. And if You know this matter to be bad for me in my religion, my livelihood and the outcome of my affair, then turn it away from me and turn me away from it, and decree for me what is good wherever it may be, then make me content with it.",
            "Aye Allah, main Tere ilm ke zariye Tujh se khair talab karta hoon, Teri qudrat ke zariye Tujh se taqat maangta hoon, aur Tere fazl-e-azeem ka sawal karta hoon; kyunke Tu qudrat rakhta hai aur main nahi rakhta, Tu jaanta hai aur main nahi jaanta, aur Tu ghaibon ka khoob jaanne wala hai. Aye Allah, agar Tere ilm mein yeh kaam mere deen, meri maash aur mere anjaam ke liye behtar hai to ise mere liye muqaddar farma, aasaan farma, phir isme mere liye barkat de. Aur agar Tere ilm mein yeh kaam mere deen, meri maash aur mere anjaam ke liye bura hai to ise mujh se pher de aur mujhe is se pher de, aur mere liye bhalai muqaddar farma jahan bhi ho, phir mujhe us par raazi kar de.",
            "ऐ अल्लाह, मैं तेरे इल्म के ज़रिए तुझसे ख़ैर तलब करता हूँ, तेरी क़ुदरत के ज़रिए तुझसे ताक़त माँगता हूँ, और तेरे फ़ज़्ल-ए-अज़ीम का सवाल करता हूँ; क्योंकि तू क़ुदरत रखता है और मैं नहीं रखता, तू जानता है और मैं नहीं जानता, और तू ग़ैबों का ख़ूब जानने वाला है। ऐ अल्लाह, अगर तेरे इल्म में यह काम मेरे दीन, मेरी मआश और मेरे अंजाम के लिए बेहतर है तो इसे मेरे लिए मुक़द्दर फ़रमा, आसान फ़रमा, फिर इसमें मेरे लिए बरकत दे। और अगर तेरे इल्म में यह काम मेरे दीन, मेरी मआश और मेरे अंजाम के लिए बुरा है तो इसे मुझसे फेर दे और मुझे इससे फेर दे, और मेरे लिए भलाई मुक़द्दर फ़रमा जहाँ भी हो, फिर मुझे उस पर राज़ी कर दे।",
            "اے اللہ! میں تیرے علم کے ذریعے تجھ سے خیر طلب کرتا ہوں، تیری قدرت کے ذریعے تجھ سے طاقت مانگتا ہوں، اور تیرے فضلِ عظیم کا سوال کرتا ہوں؛ کیونکہ تو قدرت رکھتا ہے اور میں نہیں رکھتا، تو جانتا ہے اور میں نہیں جانتا، اور تو غیبوں کا خوب جاننے والا ہے۔ اے اللہ! اگر تیرے علم میں یہ کام میرے دین، میری معاش اور میرے انجام کے لیے بہتر ہے تو اسے میرے لیے مقدر فرما، آسان فرما، پھر اس میں میرے لیے برکت دے۔ اور اگر تیرے علم میں یہ کام میرے دین، میری معاش اور میرے انجام کے لیے برا ہے تو اسے مجھ سے پھیر دے اور مجھے اس سے پھیر دے، اور میرے لیے بھلائی مقدر فرما جہاں بھی ہو، پھر مجھے اس پر راضی کر دے۔"
        ),
        "Sahih al-Bukhari 1162",
        t(
            "At the words ‘hadhal-amr’ bring your matter to mind.",
            "'Hazal-amr' par apne kaam ka khayal karein.",
            "'हाज़ल-अम्र' पर अपने काम का ख़याल करें।",
            "’ھٰذا الامر‘ پر اپنے کام کا خیال کریں۔"
        )
    ),
    Recitation(
        "tasbih_kalima", RecitationGroup.SPECIAL,
        t("Tasbih of Salat-ut-Tasbih", "Salat-ut-Tasbeeh ki tasbeeh", "सलातुत्तस्बीह की तस्बीह", "صلاۃ التسبیح کی تسبیح"),
        "سُبْحَانَ اللَّهِ وَالْحَمْدُ لِلَّهِ وَلَا إِلَٰهَ إِلَّا اللَّهُ وَاللَّهُ أَكْبَرُ",
        "Subhanallahi wal-hamdu lillahi wa la ilaha illallahu wallahu akbar.",
        t(
            "Glory be to Allah, all praise is for Allah, there is no god but Allah, and Allah is the Greatest.",
            "Allah paak hai, sab tareef Allah ke liye hai, Allah ke siwa koi mabood nahi, aur Allah sab se bada hai.",
            "अल्लाह पाक है, सब तारीफ़ अल्लाह के लिए है, अल्लाह के सिवा कोई माबूद नहीं, और अल्लाह सबसे बड़ा है।",
            "اللہ پاک ہے، سب تعریف اللہ کے لیے ہے، اللہ کے سوا کوئی معبود نہیں، اور اللہ سب سے بڑا ہے۔"
        ),
        "Sunan Abi Dawud 1297"
    )
)
