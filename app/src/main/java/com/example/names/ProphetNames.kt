package com.example.names

data class ProphetName(
    val number: Int,
    val arabic: String,
    val transliteration: String,
    val english: String,
    val urdu: String,
    val hindi: String,
    val hinglish: String
) {
    /** Languages without their own reviewed meaning read the English one. */
    fun meaning(language: String): String = when (language) {
        "ur" -> urdu
        "hi" -> hindi
        "hinglish" -> hinglish
        else -> NameMeaningTranslations.of(language, english)
    }
}

/**
 * Asma un-Nabi ﷺ: the widely recited list of ninety-nine blessed names of the Prophet Muhammad ﷺ,
 * in its well-known order.
 */
object ProphetNames {
    const val HADITH = "I have five names: I am Muhammad and Ahmad; I am Al-Mahi, through whom Allah wipes out disbelief; I am Al-Hashir, at whose feet the people will be gathered; and I am Al-Aqib, the last of the Prophets."
    const val HADITH_SOURCE = "Sahih al-Bukhari 3532, Sahih Muslim 2354"
    const val LIST_SOURCE = "Names: the widely recited list of ninety-nine blessed names, drawn from the Quran, Hadith and the books of Seerah."

    val names: List<ProphetName> = listOf(
        n("مُحَمَّدٌ", "Muhammad", "The Most Praised", "بہت تعریف کیے گئے", "बहुत तारीफ़ किए गए", "Bahut tareef kiye gaye"),
        n("أَحْمَدُ", "Ahmad", "The One who Praises Allah Most", "سب سے زیادہ حمد کرنے والے", "सबसे ज़्यादा हम्द करने वाले", "Sab se zyada hamd karne wale"),
        n("حَامِدٌ", "Hamid", "The Praiser of Allah", "اللہ کی حمد کرنے والے", "अल्लाह की हम्द करने वाले", "Allah ki hamd karne wale"),
        n("مَحْمُودٌ", "Mahmud", "The Praised One", "تعریف کیے گئے", "तारीफ़ किए गए", "Tareef kiye gaye"),
        n("قَاسِمٌ", "Qasim", "The Distributor", "تقسیم فرمانے والے", "तक़सीम फ़रमाने वाले", "Taqseem farmane wale"),
        n("عَاقِبٌ", "Aqib", "The Last of the Prophets", "سب نبیوں کے بعد آنے والے", "सब नबियों के बाद आने वाले", "Sab nabiyon ke baad aane wale"),
        n("فَاتِحٌ", "Fatih", "The Opener", "کھولنے والے، فتح پانے والے", "खोलने वाले, फ़तह पाने वाले", "Kholne wale, fatah paane wale"),
        n("شَاهِدٌ", "Shahid", "The Witness", "گواہی دینے والے", "गवाही देने वाले", "Gawahi dene wale"),
        n("حَاشِرٌ", "Hashir", "The Gatherer", "جن کے قدموں پر لوگ جمع کیے جائیں گے", "जिनके क़दमों पर लोग जमा किए जाएँगे", "Jinke qadmon par log jama kiye jaayenge"),
        n("رَشِيدٌ", "Rashid", "The Rightly Guided", "ہدایت یافتہ، راہِ راست والے", "हिदायत याफ़्ता, सीधी राह वाले", "Hidayat yaafta, seedhi raah wale"),
        n("مَشْهُودٌ", "Mashhud", "The One who is Witnessed", "جن کی گواہی دی گئی", "जिनकी गवाही दी गई", "Jinki gawahi di gayi"),
        n("بَشِيرٌ", "Bashir", "The Bringer of Good News", "خوش خبری سنانے والے", "ख़ुशख़बरी सुनाने वाले", "Khushkhabri sunane wale"),
        n("نَذِيرٌ", "Nadhir", "The Warner", "ڈر سنانے والے", "डर सुनाने वाले", "Dar sunane wale"),
        n("دَاعٍ", "Da'i", "The Caller to Allah", "اللہ کی طرف بلانے والے", "अल्लाह की तरफ़ बुलाने वाले", "Allah ki taraf bulane wale"),
        n("شَافٍ", "Shafi", "The Healer", "شفا دینے والے", "शिफ़ा देने वाले", "Shifa dene wale"),
        n("هَادٍ", "Hadi", "The Guide", "ہدایت دینے والے", "हिदायत देने वाले", "Hidayat dene wale"),
        n("مَهْدِيٌّ", "Mahdi", "The Guided One", "ہدایت پائے ہوئے", "हिदायत पाए हुए", "Hidayat paaye hue"),
        n("مَاحٍ", "Mahi", "The Eraser of Disbelief", "کفر کو مٹانے والے", "कुफ़्र को मिटाने वाले", "Kufr ko mitane wale"),
        n("مُنْجٍ", "Munji", "The Deliverer", "نجات دلانے والے", "निजात दिलाने वाले", "Nijaat dilane wale"),
        n("نَاهٍ", "Nahi", "The Forbidder of Evil", "برائی سے روکنے والے", "बुराई से रोकने वाले", "Burai se rokne wale"),
        n("رَسُولٌ", "Rasul", "The Messenger", "اللہ کے رسول", "अल्लाह के रसूल", "Allah ke Rasool"),
        n("نَبِيٌّ", "Nabi", "The Prophet", "اللہ کے نبی", "अल्लाह के नबी", "Allah ke Nabi"),
        n("أُمِّيٌّ", "Ummi", "The Unlettered Prophet", "جنہوں نے کسی مخلوق سے پڑھنا نہ سیکھا", "जिन्होंने किसी मख़लूक़ से पढ़ना न सीखा", "Jinhone kisi makhlooq se padhna na seekha"),
        n("تِهَامِيٌّ", "Tihami", "The One from Tihamah", "تہامہ والے", "तिहामा वाले", "Tihama wale"),
        n("هَاشِمِيٌّ", "Hashimi", "The One from Banu Hashim", "بنو ہاشم والے", "बनू हाशिम वाले", "Banu Hashim wale"),
        n("أَبْطَحِيٌّ", "Abtahi", "The One from Abtah (Makkah)", "ابطح (مکہ) والے", "अबतह (मक्का) वाले", "Abtah (Makkah) wale"),
        n("عَزِيزٌ", "Aziz", "The Honoured, the Mighty", "عزت و غلبے والے", "इज़्ज़त और ग़लबे वाले", "Izzat aur ghalbe wale"),
        n("حَرِيصٌ عَلَيْكُمْ", "Harisun Alaykum", "Deeply Concerned for You", "تمہاری بھلائی کے بہت چاہنے والے", "तुम्हारी भलाई के बहुत चाहने वाले", "Tumhari bhalai ke bahut chaahne wale"),
        n("رَءُوفٌ", "Ra'uf", "The Most Kind", "بہت شفقت فرمانے والے", "बहुत शफ़क़त फ़रमाने वाले", "Bahut shafqat farmane wale"),
        n("رَحِيمٌ", "Rahim", "The Merciful", "رحم فرمانے والے", "रहम फ़रमाने वाले", "Reham farmane wale"),
        n("طٰهٰ", "Taha", "Taha (a Quranic name)", "طٰہٰ (قرآنی نام)", "ताहा (क़ुरआनी नाम)", "Taha (Qurani naam)"),
        n("مُجْتَبىٰ", "Mujtaba", "The Chosen One", "چنے ہوئے", "चुने हुए", "Chune hue"),
        n("طٰسٓ", "Tasin", "Ta-Sin (a Quranic name)", "طٰسٓ (قرآنی نام)", "ता-सीन (क़ुरआनी नाम)", "Ta-Seen (Qurani naam)"),
        n("مُرْتَضىٰ", "Murtada", "The One Allah is Pleased With", "پسند کیے ہوئے", "पसंद किए हुए", "Pasand kiye hue"),
        n("حٰمٓ", "Ha-Mim", "Ha-Mim (a Quranic name)", "حٰمٓ (قرآنی نام)", "हा-मीम (क़ुरआनी नाम)", "Ha-Meem (Qurani naam)"),
        n("مُصْطَفىٰ", "Mustafa", "The Selected One", "برگزیدہ", "बरगुज़ीदा", "Barguzeeda"),
        n("يٰسٓ", "Yasin", "Ya-Sin (a Quranic name)", "یٰسٓ (قرآنی نام)", "या-सीन (क़ुरआनी नाम)", "Ya-Seen (Qurani naam)"),
        n("أَوْلىٰ", "Awla", "Closer to the Believers than Their Own Selves", "مومنوں کے ان کی جانوں سے زیادہ قریب", "मोमिनों के उनकी जानों से ज़्यादा क़रीब", "Mominon ke unki jaanon se zyada qareeb"),
        n("مُزَّمِّلٌ", "Muzzammil", "The Enwrapped One", "چادر اوڑھنے والے", "चादर ओढ़ने वाले", "Chadar odhne wale"),
        n("وَلِيٌّ", "Wali", "The Friend and Guardian", "دوست، مددگار", "दोस्त, मददगार", "Dost, madadgaar"),
        n("مُدَّثِّرٌ", "Muddaththir", "The Cloaked One", "کملی اوڑھنے والے", "कमली ओढ़ने वाले", "Kamli odhne wale"),
        n("مَتِينٌ", "Matin", "The Firm and Steadfast", "مضبوط، ثابت قدم", "मज़बूत, साबित क़दम", "Mazboot, saabit qadam"),
        n("مُصَدِّقٌ", "Musaddiq", "The Confirmer of the Truth", "تصدیق فرمانے والے", "तस्दीक़ फ़रमाने वाले", "Tasdeeq farmane wale"),
        n("طَيِّبٌ", "Tayyib", "The Pure and Good", "پاکیزہ", "पाकीज़ा", "Pakeeza"),
        n("نَاصِرٌ", "Nasir", "The Helper", "مدد فرمانے والے", "मदद फ़रमाने वाले", "Madad farmane wale"),
        n("مَنْصُورٌ", "Mansur", "The One Helped by Allah", "جن کی مدد کی گئی", "जिनकी मदद की गई", "Jinki madad ki gayi"),
        n("مِصْبَاحٌ", "Misbah", "The Lamp", "چراغ", "चिराग़", "Chiraagh"),
        n("آمِرٌ", "Amir", "The Commander of Good", "نیکی کا حکم دینے والے", "नेकी का हुक्म देने वाले", "Neki ka hukm dene wale"),
        n("حِجَازِيٌّ", "Hijazi", "The One from Hijaz", "حجاز والے", "हिजाज़ वाले", "Hijaz wale"),
        n("نِزَارِيٌّ", "Nizari", "The Descendant of Nizar", "نزار کی اولاد سے", "निज़ार की औलाद से", "Nizar ki aulaad se"),
        n("قُرَشِيٌّ", "Qurashi", "The One from Quraysh", "قریش والے", "क़ुरैश वाले", "Quraysh wale"),
        n("مُضَرِيٌّ", "Mudari", "The Descendant of Mudar", "مضر کی اولاد سے", "मुज़र की औलाद से", "Muzar ki aulaad se"),
        n("نَبِيُّ التَّوْبَةِ", "Nabi at-Tawbah", "The Prophet of Repentance", "توبہ کے نبی", "तौबा के नबी", "Tauba ke Nabi"),
        n("حَافِظٌ", "Hafiz", "The Protector", "حفاظت فرمانے والے", "हिफ़ाज़त फ़रमाने वाले", "Hifazat farmane wale"),
        n("كَامِلٌ", "Kamil", "The Perfect One", "کامل", "कामिल", "Kaamil"),
        n("صَادِقٌ", "Sadiq", "The Truthful", "سچے", "सच्चे", "Sachche"),
        n("أَمِينٌ", "Amin", "The Trustworthy", "امانت دار", "अमानतदार", "Amanatdaar"),
        n("عَبْدُ اللهِ", "Abdullah", "The Servant of Allah", "اللہ کے بندے", "अल्लाह के बंदे", "Allah ke bande"),
        n("كَلِيمُ اللهِ", "Kalimullah", "The One who Spoke with Allah", "اللہ سے کلام فرمانے والے", "अल्लाह से कलाम फ़रमाने वाले", "Allah se kalaam farmane wale"),
        n("حَبِيبُ اللهِ", "Habibullah", "The Beloved of Allah", "اللہ کے محبوب", "अल्लाह के महबूब", "Allah ke mehboob"),
        n("نَجِيُّ اللهِ", "Najiyyullah", "The Confidant of Allah", "اللہ کے رازدار", "अल्लाह के राज़दार", "Allah ke raazdaar"),
        n("صَفِيُّ اللهِ", "Safiyyullah", "The Chosen Friend of Allah", "اللہ کے چنے ہوئے", "अल्लाह के चुने हुए", "Allah ke chune hue"),
        n("خَاتَمُ الْأَنْبِيَاءِ", "Khatam al-Anbiya", "The Seal of the Prophets", "آخری نبی", "आख़िरी नबी", "Aakhiri Nabi"),
        n("حَسِيبٌ", "Hasib", "The Noble, the Sufficient", "عالی نسب، کفایت کرنے والے", "आली नसब, किफ़ायत करने वाले", "Aali nasab, kifayat karne wale"),
        n("مُجِيبٌ", "Mujib", "The Responsive", "جواب دینے والے", "जवाब देने वाले", "Jawab dene wale"),
        n("شَكُورٌ", "Shakur", "The Most Thankful", "بہت شکر کرنے والے", "बहुत शुक्र करने वाले", "Bahut shukr karne wale"),
        n("مُقْتَصِدٌ", "Muqtasid", "The Moderate", "میانہ روی والے", "मियाना-रवी वाले", "Miyana-ravi wale"),
        n("رَسُولُ الرَّحْمَةِ", "Rasul ar-Rahmah", "The Messenger of Mercy", "رحمت کے رسول", "रहमत के रसूल", "Rehmat ke Rasool"),
        n("قَوِيٌّ", "Qawi", "The Strong", "طاقت والے", "ताक़त वाले", "Taaqat wale"),
        n("حَفِيٌّ", "Hafi", "The Gracious, the Well-Informed", "بہت مہربان، خوب باخبر", "बहुत मेहरबान, ख़ूब बाख़बर", "Bahut meharbaan, khoob bakhabar"),
        n("مَأْمُونٌ", "Ma'mun", "The Trusted One", "جن پر اعتماد کیا گیا", "जिन पर एतिमाद किया गया", "Jin par aitmaad kiya gaya"),
        n("مَعْلُومٌ", "Ma'lum", "The Well-Known", "جانے پہچانے", "जाने-पहचाने", "Jaane-pehchane"),
        n("حَقٌّ", "Haqq", "The Truth", "سچے، برحق", "सच्चे, बरहक़", "Sachche, barhaq"),
        n("مُبِينٌ", "Mubin", "The One who Makes Clear", "حق کو واضح کرنے والے", "हक़ को वाज़ेह करने वाले", "Haq ko waazeh karne wale"),
        n("مُطِيعٌ", "Muti'", "The Obedient to Allah", "اللہ کے فرمانبردار", "अल्लाह के फ़रमाँबरदार", "Allah ke farmanbardar"),
        n("أَوَّلٌ", "Awwal", "The First", "سب سے پہلے", "सबसे पहले", "Sab se pehle"),
        n("آخِرٌ", "Akhir", "The Last", "سب سے آخری", "सबसे आख़िरी", "Sab se aakhiri"),
        n("ظَاهِرٌ", "Zahir", "The Manifest", "ظاہر", "ज़ाहिर", "Zaahir"),
        n("بَاطِنٌ", "Batin", "The Hidden", "پوشیدہ", "पोशीदा", "Posheeda"),
        n("يَتِيمٌ", "Yatim", "The Orphan, the Unique", "یتیم، بے مثل", "यतीम, बेमिसाल", "Yateem, bemisaal"),
        n("كَرِيمٌ", "Karim", "The Generous", "کرم فرمانے والے", "करम फ़रमाने वाले", "Karam farmane wale"),
        n("حَكِيمٌ", "Hakim", "The Wise", "حکمت والے", "हिकमत वाले", "Hikmat wale"),
        n("سَيِّدٌ", "Sayyid", "The Master", "سردار", "सरदार", "Sardaar"),
        n("سِرَاجٌ", "Siraj", "The Shining Lamp", "روشن چراغ", "रोशन चिराग़", "Roshan chiraagh"),
        n("مُنِيرٌ", "Munir", "The Illuminating One", "روشن کرنے والے", "रोशन करने वाले", "Roshan karne wale"),
        n("مُحَرَّمٌ", "Muharram", "The Sacred, the Revered", "حرمت والے", "हुरमत वाले", "Hurmat wale"),
        n("مُكَرَّمٌ", "Mukarram", "The Honoured", "عزت دیے گئے", "इज़्ज़त दिए गए", "Izzat diye gaye"),
        n("مُبَشِّرٌ", "Mubashshir", "The Giver of Glad Tidings", "بشارت دینے والے", "बशारत देने वाले", "Basharat dene wale"),
        n("مُذَكِّرٌ", "Mudhakkir", "The Reminder", "نصیحت فرمانے والے", "नसीहत फ़रमाने वाले", "Naseehat farmane wale"),
        n("مُطَهَّرٌ", "Mutahhar", "The Purified", "پاک کیے گئے", "पाक किए गए", "Paak kiye gaye"),
        n("قَرِيبٌ", "Qarib", "The Near One", "قریب", "क़रीब", "Qareeb"),
        n("خَلِيلٌ", "Khalil", "The Close Friend of Allah", "اللہ کے گہرے دوست", "अल्लाह के गहरे दोस्त", "Allah ke gehre dost"),
        n("مَدْعُوٌّ", "Mad'u", "The One who is Called", "بلائے گئے", "बुलाए गए", "Bulaye gaye"),
        n("جَوَّادٌ", "Jawwad", "The Most Generous", "بہت سخی", "बहुत सख़ी", "Bahut sakhi"),
        n("خَاتِمٌ", "Khatim", "The Seal, the Final One", "سلسلۂ نبوت ختم فرمانے والے", "नुबुव्वत का सिलसिला ख़त्म फ़रमाने वाले", "Nubuwwat ka silsila khatm farmane wale"),
        n("عَادِلٌ", "Adil", "The Just", "انصاف فرمانے والے", "इंसाफ़ फ़रमाने वाले", "Insaaf farmane wale"),
        n("شَهِيرٌ", "Shahir", "The Renowned", "مشہور", "मशहूर", "Mashhoor"),
        n("شَهِيدٌ", "Shahid (Shaheed)", "The Ever-Present Witness", "گواہ، حاضر و ناظر", "गवाह, हाज़िर व नाज़िर", "Gawah, haazir o naazir"),
        n("رَسُولُ الْمَلَاحِمِ", "Rasul al-Malahim", "The Messenger of the Great Battles", "بڑی جنگوں کے رسول", "बड़ी जंगों के रसूल", "Badi jangon ke Rasool")
    ).mapIndexed { index, name -> name.copy(number = index + 1) }

    private fun n(arabic: String, transliteration: String, english: String, urdu: String, hindi: String, hinglish: String) =
        ProphetName(0, arabic, transliteration, english, urdu, hindi, hinglish)
}
