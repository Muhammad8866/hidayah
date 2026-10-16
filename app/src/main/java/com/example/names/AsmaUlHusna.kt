package com.example.names

data class AllahName(
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
 * Al-Asma ul-Husna, the ninety-nine names of Allah, in the well-known order based on the
 * narration of Sayyiduna Abu Hurairah (Jami' at-Tirmidhi 3507).
 */
object AsmaUlHusna {
    const val HADITH = "Allah has ninety-nine names, one hundred less one. Whoever memorises and preserves them will enter Paradise."
    const val HADITH_SOURCE = "Sahih al-Bukhari 2736, Sahih Muslim 2677"
    const val LIST_SOURCE = "Names: the well-known list based on Jami' at-Tirmidhi 3507."

    val names: List<AllahName> = listOf(
        n("الرَّحْمٰنُ", "Ar-Rahman", "The Most Gracious", "نہایت مہربان", "बहुत मेहरबान", "Bahut meharbaan"),
        n("الرَّحِيمُ", "Ar-Raheem", "The Most Merciful", "بڑا رحم والا", "बड़ा रहम वाला", "Bada reham wala"),
        n("الْمَلِكُ", "Al-Malik", "The King", "حقیقی بادشاہ", "हक़ीक़ी बादशाह", "Haqeeqi badshah"),
        n("الْقُدُّوسُ", "Al-Quddus", "The Most Holy", "ہر عیب سے پاک", "हर ऐब से पाक", "Har aib se paak"),
        n("السَّلَامُ", "As-Salam", "The Source of Peace", "سلامتی دینے والا", "सलामती देने वाला", "Salamati dene wala"),
        n("الْمُؤْمِنُ", "Al-Mu'min", "The Giver of Security", "امن دینے والا", "अमन देने वाला", "Aman dene wala"),
        n("الْمُهَيْمِنُ", "Al-Muhaymin", "The Guardian", "نگہبان", "निगहबान", "Nigehbaan"),
        n("الْعَزِيزُ", "Al-Azeez", "The Almighty", "سب پر غالب", "सब पर ग़ालिब", "Sab par ghalib"),
        n("الْجَبَّارُ", "Al-Jabbar", "The Compeller", "زبردست", "ज़बरदस्त", "Zabardast"),
        n("الْمُتَكَبِّرُ", "Al-Mutakabbir", "The Supreme in Greatness", "بڑائی والا", "बड़ाई वाला", "Badaai wala"),
        n("الْخَالِقُ", "Al-Khaliq", "The Creator", "پیدا کرنے والا", "पैदा करने वाला", "Paida karne wala"),
        n("الْبَارِئُ", "Al-Bari'", "The Originator", "جان ڈالنے والا", "जान डालने वाला", "Jaan daalne wala"),
        n("الْمُصَوِّرُ", "Al-Musawwir", "The Fashioner of Forms", "صورت بنانے والا", "सूरत बनाने वाला", "Soorat banane wala"),
        n("الْغَفَّارُ", "Al-Ghaffar", "The Ever-Forgiving", "بہت بخشنے والا", "बहुत बख़्शने वाला", "Bahut bakhshne wala"),
        n("الْقَهَّارُ", "Al-Qahhar", "The Subduer", "سب کو قابو میں رکھنے والا", "सबको क़ाबू में रखने वाला", "Sab ko qaabu mein rakhne wala"),
        n("الْوَهَّابُ", "Al-Wahhab", "The Bestower", "بہت عطا کرنے والا", "बहुत अता करने वाला", "Bahut ata karne wala"),
        n("الرَّزَّاقُ", "Ar-Razzaq", "The Provider", "روزی دینے والا", "रोज़ी देने वाला", "Rozi dene wala"),
        n("الْفَتَّاحُ", "Al-Fattah", "The Opener", "مشکلیں کھولنے والا", "मुश्किलें खोलने वाला", "Mushkilein kholne wala"),
        n("الْعَلِيمُ", "Al-Aleem", "The All-Knowing", "سب کچھ جاننے والا", "सब कुछ जानने वाला", "Sab kuch jaanne wala"),
        n("الْقَابِضُ", "Al-Qabid", "The Withholder", "روزی تنگ کرنے والا", "रोज़ी तंग करने वाला", "Rozi tang karne wala"),
        n("الْبَاسِطُ", "Al-Basit", "The Expander", "روزی کشادہ کرنے والا", "रोज़ी कुशादा करने वाला", "Rozi kushada karne wala"),
        n("الْخَافِضُ", "Al-Khafid", "The Abaser", "پست کرنے والا", "पस्त करने वाला", "Past karne wala"),
        n("الرَّافِعُ", "Ar-Rafi'", "The Exalter", "بلند کرنے والا", "बुलंद करने वाला", "Buland karne wala"),
        n("الْمُعِزُّ", "Al-Mu'izz", "The Giver of Honour", "عزت دینے والا", "इज़्ज़त देने वाला", "Izzat dene wala"),
        n("الْمُذِلُّ", "Al-Mudhill", "The Giver of Dishonour", "ذلت دینے والا", "ज़िल्लत देने वाला", "Zillat dene wala"),
        n("السَّمِيعُ", "As-Samee'", "The All-Hearing", "سب کچھ سننے والا", "सब कुछ सुनने वाला", "Sab kuch sunne wala"),
        n("الْبَصِيرُ", "Al-Baseer", "The All-Seeing", "سب کچھ دیکھنے والا", "सब कुछ देखने वाला", "Sab kuch dekhne wala"),
        n("الْحَكَمُ", "Al-Hakam", "The Judge", "فیصلہ فرمانے والا", "फ़ैसला फ़रमाने वाला", "Faisla farmane wala"),
        n("الْعَدْلُ", "Al-Adl", "The Utterly Just", "انصاف کرنے والا", "इंसाफ़ करने वाला", "Insaaf karne wala"),
        n("اللَّطِيفُ", "Al-Lateef", "The Subtle, Most Gentle", "لطف و کرم فرمانے والا", "लुत्फ़ो करम फ़रमाने वाला", "Lutf o karam farmane wala"),
        n("الْخَبِيرُ", "Al-Khabeer", "The All-Aware", "ہر بات سے باخبر", "हर बात से बाख़बर", "Har baat se bakhabar"),
        n("الْحَلِيمُ", "Al-Haleem", "The Forbearing", "بردبار", "बुर्दबार", "Burdbaar"),
        n("الْعَظِيمُ", "Al-Azeem", "The Magnificent", "عظمت والا", "अज़मत वाला", "Azmat wala"),
        n("الْغَفُورُ", "Al-Ghafoor", "The All-Forgiving", "بخشنے والا", "बख़्शने वाला", "Bakhshne wala"),
        n("الشَّكُورُ", "Ash-Shakoor", "The Most Appreciative", "قدر دان", "क़द्रदान", "Qadardaan"),
        n("الْعَلِيُّ", "Al-Aliyy", "The Most High", "بلند مرتبے والا", "बुलंद मर्तबे वाला", "Buland martabe wala"),
        n("الْكَبِيرُ", "Al-Kabeer", "The Most Great", "سب سے بڑا", "सबसे बड़ा", "Sab se bada"),
        n("الْحَفِيظُ", "Al-Hafeez", "The Preserver", "حفاظت کرنے والا", "हिफ़ाज़त करने वाला", "Hifazat karne wala"),
        n("الْمُقِيتُ", "Al-Muqeet", "The Sustainer", "سب کو روزی پہنچانے والا", "सबको रोज़ी पहुँचाने वाला", "Sab ko rozi pahunchane wala"),
        n("الْحَسِيبُ", "Al-Haseeb", "The Reckoner", "حساب لینے والا", "हिसाब लेने वाला", "Hisaab lene wala"),
        n("الْجَلِيلُ", "Al-Jaleel", "The Majestic", "بزرگی والا", "बुज़ुर्गी वाला", "Buzurgi wala"),
        n("الْكَرِيمُ", "Al-Kareem", "The Most Generous", "کرم فرمانے والا", "करम फ़रमाने वाला", "Karam farmane wala"),
        n("الرَّقِيبُ", "Ar-Raqeeb", "The Watchful", "نگرانی کرنے والا", "निगरानी करने वाला", "Nigrani karne wala"),
        n("الْمُجِيبُ", "Al-Mujeeb", "The Answerer of Prayers", "دعا قبول کرنے والا", "दुआ क़बूल करने वाला", "Dua qabool karne wala"),
        n("الْوَاسِعُ", "Al-Wasi'", "The All-Encompassing", "وسعت والا", "वुसअत वाला", "Wus'at wala"),
        n("الْحَكِيمُ", "Al-Hakeem", "The All-Wise", "حکمت والا", "हिकमत वाला", "Hikmat wala"),
        n("الْوَدُودُ", "Al-Wadood", "The Most Loving", "محبت فرمانے والا", "मोहब्बत फ़रमाने वाला", "Mohabbat farmane wala"),
        n("الْمَجِيدُ", "Al-Majeed", "The Most Glorious", "بڑی شان والا", "बड़ी शान वाला", "Badi shaan wala"),
        n("الْبَاعِثُ", "Al-Ba'ith", "The Resurrector", "مردوں کو اٹھانے والا", "मुर्दों को उठाने वाला", "Murdon ko uthane wala"),
        n("الشَّهِيدُ", "Ash-Shaheed", "The Witness", "ہر چیز کا گواہ", "हर चीज़ का गवाह", "Har cheez ka gawah"),
        n("الْحَقُّ", "Al-Haqq", "The Truth", "سچا، برحق", "सच्चा, बरहक़", "Sachcha, barhaq"),
        n("الْوَكِيلُ", "Al-Wakeel", "The Trustee", "کارساز", "कारसाज़", "Kaarsaaz"),
        n("الْقَوِيُّ", "Al-Qawiyy", "The All-Strong", "قوت والا", "क़ुव्वत वाला", "Quwwat wala"),
        n("الْمَتِينُ", "Al-Mateen", "The Firm", "نہایت مضبوط", "निहायत मज़बूत", "Nihayat mazboot"),
        n("الْوَلِيُّ", "Al-Waliyy", "The Protecting Friend", "مددگار اور دوست", "मददगार और दोस्त", "Madadgaar aur dost"),
        n("الْحَمِيدُ", "Al-Hameed", "The Praiseworthy", "تعریف کے لائق", "तारीफ़ के लायक़", "Tareef ke laaiq"),
        n("الْمُحْصِي", "Al-Muhsi", "The Enumerator", "ہر چیز کو شمار کرنے والا", "हर चीज़ को शुमार करने वाला", "Har cheez ko shumaar karne wala"),
        n("الْمُبْدِئُ", "Al-Mubdi'", "The Originator of Creation", "پہلی بار پیدا کرنے والا", "पहली बार पैदा करने वाला", "Pehli baar paida karne wala"),
        n("الْمُعِيدُ", "Al-Mu'eed", "The Restorer", "دوبارہ پیدا کرنے والا", "दोबारा पैदा करने वाला", "Dobara paida karne wala"),
        n("الْمُحْيِي", "Al-Muhyi", "The Giver of Life", "زندگی دینے والا", "ज़िंदगी देने वाला", "Zindagi dene wala"),
        n("الْمُمِيتُ", "Al-Mumeet", "The Giver of Death", "موت دینے والا", "मौत देने वाला", "Maut dene wala"),
        n("الْحَيُّ", "Al-Hayy", "The Ever-Living", "ہمیشہ زندہ رہنے والا", "हमेशा ज़िंदा रहने वाला", "Hamesha zinda rehne wala"),
        n("الْقَيُّومُ", "Al-Qayyoom", "The Sustainer of All", "سب کو قائم رکھنے والا", "सबको क़ायम रखने वाला", "Sab ko qaaim rakhne wala"),
        n("الْوَاجِدُ", "Al-Wajid", "The Finder", "ہر چیز کو پانے والا", "हर चीज़ को पाने वाला", "Har cheez ko paane wala"),
        n("الْمَاجِدُ", "Al-Majid", "The Noble", "عزت و شرف والا", "इज़्ज़तो शरफ़ वाला", "Izzat o sharaf wala"),
        n("الْوَاحِدُ", "Al-Wahid", "The One", "ایک", "एक", "Ek"),
        n("الْأَحَدُ", "Al-Ahad", "The Unique, the Only One", "یکتا", "यकता", "Yakta"),
        n("الصَّمَدُ", "As-Samad", "The Eternal Refuge", "بے نیاز", "बेनियाज़", "Beniyaz"),
        n("الْقَادِرُ", "Al-Qadir", "The All-Able", "قدرت والا", "क़ुदरत वाला", "Qudrat wala"),
        n("الْمُقْتَدِرُ", "Al-Muqtadir", "The All-Powerful", "پوری قدرت والا", "पूरी क़ुदरत वाला", "Poori qudrat wala"),
        n("الْمُقَدِّمُ", "Al-Muqaddim", "The Expediter", "آگے کرنے والا", "आगे करने वाला", "Aage karne wala"),
        n("الْمُؤَخِّرُ", "Al-Mu'akhkhir", "The Delayer", "پیچھے کرنے والا", "पीछे करने वाला", "Peeche karne wala"),
        n("الْأَوَّلُ", "Al-Awwal", "The First", "سب سے پہلے", "सबसे पहले", "Sab se pehle"),
        n("الْآخِرُ", "Al-Akhir", "The Last", "سب کے بعد", "सबके बाद", "Sab ke baad"),
        n("الظَّاهِرُ", "Az-Zahir", "The Manifest", "ظاہر", "ज़ाहिर", "Zaahir"),
        n("الْبَاطِنُ", "Al-Batin", "The Hidden", "پوشیدہ", "पोशीदा", "Posheeda"),
        n("الْوَالِي", "Al-Wali", "The Governor", "ہر چیز کا مالک و متولی", "हर चीज़ का मालिक व मुतवल्ली", "Har cheez ka maalik o mutawalli"),
        n("الْمُتَعَالِي", "Al-Muta'ali", "The Most Exalted", "سب سے بلند و برتر", "सबसे बुलंद व बरतर", "Sab se buland o bartar"),
        n("الْبَرُّ", "Al-Barr", "The Source of Goodness", "احسان فرمانے والا", "एहसान फ़रमाने वाला", "Ehsaan farmane wala"),
        n("التَّوَّابُ", "At-Tawwab", "The Acceptor of Repentance", "توبہ قبول کرنے والا", "तौबा क़बूल करने वाला", "Tauba qabool karne wala"),
        n("الْمُنْتَقِمُ", "Al-Muntaqim", "The Avenger", "بدلہ لینے والا", "बदला लेने वाला", "Badla lene wala"),
        n("الْعَفُوُّ", "Al-Afuww", "The Pardoner", "معاف فرمانے والا", "माफ़ फ़रमाने वाला", "Maaf farmane wala"),
        n("الرَّءُوفُ", "Ar-Ra'oof", "The Most Kind", "بہت شفقت والا", "बहुत शफ़क़त वाला", "Bahut shafqat wala"),
        n("مَالِكُ الْمُلْكِ", "Malik-ul-Mulk", "Owner of All Sovereignty", "بادشاہی کا مالک", "बादशाही का मालिक", "Badshahi ka maalik"),
        n("ذُو الْجَلَالِ وَالْإِكْرَامِ", "Dhul-Jalali wal-Ikram", "Lord of Majesty and Honour", "جلال اور اکرام والا", "जलाल और इकराम वाला", "Jalaal aur ikraam wala"),
        n("الْمُقْسِطُ", "Al-Muqsit", "The Equitable", "عدل قائم فرمانے والا", "अद्ल क़ायम फ़रमाने वाला", "Adl qaaim farmane wala"),
        n("الْجَامِعُ", "Al-Jami'", "The Gatherer", "سب کو جمع کرنے والا", "सबको जमा करने वाला", "Sab ko jama karne wala"),
        n("الْغَنِيُّ", "Al-Ghaniyy", "The Self-Sufficient", "غنی، کسی کا محتاج نہیں", "ग़नी, किसी का मोहताज नहीं", "Ghani, kisi ka mohtaaj nahi"),
        n("الْمُغْنِي", "Al-Mughni", "The Enricher", "غنی کرنے والا", "ग़नी करने वाला", "Ghani karne wala"),
        n("الْمَانِعُ", "Al-Mani'", "The Preventer", "روکنے والا", "रोकने वाला", "Rokne wala"),
        n("الضَّارُّ", "Ad-Darr", "The One who Decrees Harm", "نقصان کا مالک", "नुक़सान का मालिक", "Nuqsaan ka maalik"),
        n("النَّافِعُ", "An-Nafi'", "The Giver of Benefit", "نفع دینے والا", "नफ़ा देने वाला", "Nafa dene wala"),
        n("النُّورُ", "An-Noor", "The Light", "نور، روشن کرنے والا", "नूर, रोशन करने वाला", "Noor, roshan karne wala"),
        n("الْهَادِي", "Al-Hadi", "The Guide", "ہدایت دینے والا", "हिदायत देने वाला", "Hidayat dene wala"),
        n("الْبَدِيعُ", "Al-Badee'", "The Incomparable Originator", "بے مثال پیدا کرنے والا", "बेमिसाल पैदा करने वाला", "Bemisaal paida karne wala"),
        n("الْبَاقِي", "Al-Baqi", "The Everlasting", "ہمیشہ باقی رہنے والا", "हमेशा बाक़ी रहने वाला", "Hamesha baaqi rehne wala"),
        n("الْوَارِثُ", "Al-Warith", "The Inheritor of All", "سب کا وارث", "सबका वारिस", "Sab ka waaris"),
        n("الرَّشِيدُ", "Ar-Rasheed", "The Guide to the Right Path", "سیدھی راہ دکھانے والا", "सीधी राह दिखाने वाला", "Seedhi raah dikhane wala"),
        n("الصَّبُورُ", "As-Saboor", "The Most Patient", "بڑے صبر والا", "बड़े सब्र वाला", "Bade sabr wala")
    ).mapIndexed { index, name -> name.copy(number = index + 1) }

    private fun n(arabic: String, transliteration: String, english: String, urdu: String, hindi: String, hinglish: String) =
        AllahName(0, arabic, transliteration, english, urdu, hindi, hinglish)
}
