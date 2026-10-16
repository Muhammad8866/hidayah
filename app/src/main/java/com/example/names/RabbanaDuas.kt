package com.example.names

data class RabbanaDua(
    val number: Int,
    val arabic: String,
    val transliteration: String,
    val reference: String,
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
 * The forty supplications of the Noble Quran that call upon Allah as "Rabbana" (Our Lord), in
 * mushaf order. The Arabic is the Quranic text itself; the meanings are plain-language renderings.
 */
object RabbanaDuas {
    const val INTRO = "Forty supplications from the Noble Quran that call upon Allah as Rabbana, Our Lord."
    const val SOURCE = "Arabic: the text of the Noble Quran, in mushaf order. Meanings are simple renderings of each verse."

    val duas: List<RabbanaDua> = listOf(
        d(
            "رَبَّنَا تَقَبَّلْ مِنَّا ۖ إِنَّكَ أَنتَ السَّمِيعُ الْعَلِيمُ",
            "Rabbana taqabbal minna, innaka Antas-Sami'ul-'Alim",
            "Al-Baqarah 2:127",
            "Our Lord, accept this from us. Surely You are the All-Hearing, the All-Knowing.",
            "اے ہمارے رب! ہم سے قبول فرما، بے شک تو ہی سننے والا، جاننے والا ہے۔",
            "ऐ हमारे रब! हमसे क़बूल फ़रमा, बेशक तू ही सुनने वाला, जानने वाला है।",
            "Aye hamare Rab! Hum se qabool farma, beshak Tu hi sunne wala, jaanne wala hai."
        ),
        d(
            "رَبَّنَا وَاجْعَلْنَا مُسْلِمَيْنِ لَكَ وَمِن ذُرِّيَّتِنَا أُمَّةً مُّسْلِمَةً لَّكَ وَأَرِنَا مَنَاسِكَنَا وَتُبْ عَلَيْنَا ۖ إِنَّكَ أَنتَ التَّوَّابُ الرَّحِيمُ",
            "Rabbana waj'alna muslimayni laka wa min dhurriyyatina ummatan muslimatan laka, wa arina manasikana wa tub 'alayna, innaka Antat-Tawwabur-Rahim",
            "Al-Baqarah 2:128",
            "Our Lord, make us both submissive to You, and from our descendants a nation submissive to You. Show us our rites of worship and accept our repentance. Surely You are the Acceptor of repentance, the Most Merciful.",
            "اے ہمارے رب! ہم دونوں کو اپنا فرمانبردار بنا اور ہماری اولاد میں سے ایک امت اپنی فرمانبردار بنا، اور ہمیں ہماری عبادت کے طریقے بتا اور ہماری توبہ قبول فرما، بے شک تو ہی بہت توبہ قبول کرنے والا، مہربان ہے۔",
            "ऐ हमारे रब! हम दोनों को अपना फ़रमाँबरदार बना और हमारी औलाद में से एक उम्मत अपनी फ़रमाँबरदार बना, और हमें हमारी इबादत के तरीक़े बता और हमारी तौबा क़बूल फ़रमा, बेशक तू ही बहुत तौबा क़बूल करने वाला, मेहरबान है।",
            "Aye hamare Rab! Hum dono ko apna farmanbardar bana aur hamari aulaad mein se ek ummat apni farmanbardar bana, aur humein hamari ibadat ke tareeqe bata aur hamari tauba qabool farma, beshak Tu hi bahut tauba qabool karne wala, meharbaan hai."
        ),
        d(
            "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            "Rabbana atina fid-dunya hasanatan wa fil-akhirati hasanatan wa qina 'adhaban-nar",
            "Al-Baqarah 2:201",
            "Our Lord, give us good in this world and good in the Hereafter, and save us from the punishment of the Fire.",
            "اے ہمارے رب! ہمیں دنیا میں بھلائی عطا فرما اور آخرت میں بھی بھلائی عطا فرما اور ہمیں دوزخ کے عذاب سے بچا۔",
            "ऐ हमारे रब! हमें दुनिया में भलाई अता फ़रमा और आख़िरत में भी भलाई अता फ़रमा और हमें दोज़ख़ के अज़ाब से बचा।",
            "Aye hamare Rab! Humein duniya mein bhalai ata farma aur aakhirat mein bhi bhalai ata farma aur humein dozakh ke azaab se bacha."
        ),
        d(
            "رَبَّنَا أَفْرِغْ عَلَيْنَا صَبْرًا وَثَبِّتْ أَقْدَامَنَا وَانصُرْنَا عَلَى الْقَوْمِ الْكَافِرِينَ",
            "Rabbana afrigh 'alayna sabran wa thabbit aqdamana wansurna 'alal-qawmil-kafirin",
            "Al-Baqarah 2:250",
            "Our Lord, pour patience upon us, make our feet firm, and help us against the disbelieving people.",
            "اے ہمارے رب! ہم پر صبر انڈیل دے اور ہمارے قدم جما دے اور کافر قوم کے مقابلے میں ہماری مدد فرما۔",
            "ऐ हमारे रब! हम पर सब्र उंडेल दे और हमारे क़दम जमा दे और काफ़िर क़ौम के मुक़ाबले में हमारी मदद फ़रमा।",
            "Aye hamare Rab! Hum par sabr undel de aur hamare qadam jama de aur kafir qaum ke muqable mein hamari madad farma."
        ),
        d(
            "رَبَّنَا لَا تُؤَاخِذْنَا إِن نَّسِينَا أَوْ أَخْطَأْنَا",
            "Rabbana la tu'akhidhna in nasina aw akhta'na",
            "Al-Baqarah 2:286",
            "Our Lord, do not take us to task if we forget or make a mistake.",
            "اے ہمارے رب! اگر ہم بھول جائیں یا غلطی کر بیٹھیں تو ہماری پکڑ نہ فرما۔",
            "ऐ हमारे रब! अगर हम भूल जाएँ या ग़लती कर बैठें तो हमारी पकड़ न फ़रमा।",
            "Aye hamare Rab! Agar hum bhool jaayein ya ghalti kar baithein to hamari pakad na farma."
        ),
        d(
            "رَبَّنَا وَلَا تَحْمِلْ عَلَيْنَا إِصْرًا كَمَا حَمَلْتَهُ عَلَى الَّذِينَ مِن قَبْلِنَا",
            "Rabbana wa la tahmil 'alayna isran kama hamaltahu 'alalladhina min qablina",
            "Al-Baqarah 2:286",
            "Our Lord, do not place on us a heavy burden like the one You placed on those before us.",
            "اے ہمارے رب! ہم پر ویسا بھاری بوجھ نہ ڈال جیسا تو نے ہم سے پہلے لوگوں پر ڈالا تھا۔",
            "ऐ हमारे रब! हम पर वैसा भारी बोझ न डाल जैसा तूने हमसे पहले लोगों पर डाला था।",
            "Aye hamare Rab! Hum par waisa bhaari bojh na daal jaisa Tune hum se pehle logon par daala tha."
        ),
        d(
            "رَبَّنَا وَلَا تُحَمِّلْنَا مَا لَا طَاقَةَ لَنَا بِهِ ۖ وَاعْفُ عَنَّا وَاغْفِرْ لَنَا وَارْحَمْنَا ۚ أَنتَ مَوْلَانَا فَانصُرْنَا عَلَى الْقَوْمِ الْكَافِرِينَ",
            "Rabbana wa la tuhammilna ma la taqata lana bih, wa'fu 'anna waghfir lana warhamna, Anta mawlana fansurna 'alal-qawmil-kafirin",
            "Al-Baqarah 2:286",
            "Our Lord, do not burden us with what we have no strength to bear. Pardon us, forgive us and have mercy on us. You are our Protector, so help us against the disbelieving people.",
            "اے ہمارے رب! ہم پر وہ بوجھ نہ ڈال جس کی ہم میں طاقت نہیں، اور ہمیں معاف فرما اور ہمیں بخش دے اور ہم پر رحم فرما، تو ہی ہمارا مولیٰ ہے، پس کافر قوم کے مقابلے میں ہماری مدد فرما۔",
            "ऐ हमारे रब! हम पर वह बोझ न डाल जिसकी हममें ताक़त नहीं, और हमें माफ़ फ़रमा और हमें बख़्श दे और हम पर रहम फ़रमा, तू ही हमारा मौला है, पस काफ़िर क़ौम के मुक़ाबले में हमारी मदद फ़रमा।",
            "Aye hamare Rab! Hum par woh bojh na daal jiski hum mein taaqat nahi, aur humein maaf farma aur humein bakhsh de aur hum par reham farma, Tu hi hamara Maula hai, pas kafir qaum ke muqable mein hamari madad farma."
        ),
        d(
            "رَبَّنَا لَا تُزِغْ قُلُوبَنَا بَعْدَ إِذْ هَدَيْتَنَا وَهَبْ لَنَا مِن لَّدُنكَ رَحْمَةً ۚ إِنَّكَ أَنتَ الْوَهَّابُ",
            "Rabbana la tuzigh qulubana ba'da idh hadaytana wa hab lana min ladunka rahmah, innaka Antal-Wahhab",
            "Aal-e-Imran 3:8",
            "Our Lord, do not let our hearts deviate after You have guided us, and grant us mercy from Yourself. Surely You are the Bestower.",
            "اے ہمارے رب! ہمیں ہدایت دینے کے بعد ہمارے دل ٹیڑھے نہ کر اور ہمیں اپنے پاس سے رحمت عطا فرما، بے شک تو ہی بہت عطا فرمانے والا ہے۔",
            "ऐ हमारे रब! हमें हिदायत देने के बाद हमारे दिल टेढ़े न कर और हमें अपने पास से रहमत अता फ़रमा, बेशक तू ही बहुत अता फ़रमाने वाला है।",
            "Aye hamare Rab! Humein hidayat dene ke baad hamare dil tedhe na kar aur humein apne paas se rehmat ata farma, beshak Tu hi bahut ata farmane wala hai."
        ),
        d(
            "رَبَّنَا إِنَّكَ جَامِعُ النَّاسِ لِيَوْمٍ لَّا رَيْبَ فِيهِ ۚ إِنَّ اللَّهَ لَا يُخْلِفُ الْمِيعَادَ",
            "Rabbana innaka jami'un-nasi li-yawmin la rayba fih, innallaha la yukhliful-mi'ad",
            "Aal-e-Imran 3:9",
            "Our Lord, You will surely gather all people on a Day about which there is no doubt. Surely Allah does not break His promise.",
            "اے ہمارے رب! بے شک تو سب لوگوں کو اس دن جمع کرنے والا ہے جس میں کوئی شک نہیں، بے شک اللہ وعدے کے خلاف نہیں کرتا۔",
            "ऐ हमारे रब! बेशक तू सब लोगों को उस दिन जमा करने वाला है जिसमें कोई शक नहीं, बेशक अल्लाह वादे के ख़िलाफ़ नहीं करता।",
            "Aye hamare Rab! Beshak Tu sab logon ko us din jama karne wala hai jis mein koi shak nahi, beshak Allah waade ke khilaf nahi karta."
        ),
        d(
            "رَبَّنَا إِنَّنَا آمَنَّا فَاغْفِرْ لَنَا ذُنُوبَنَا وَقِنَا عَذَابَ النَّارِ",
            "Rabbana innana amanna faghfir lana dhunubana wa qina 'adhaban-nar",
            "Aal-e-Imran 3:16",
            "Our Lord, we have believed, so forgive us our sins and save us from the punishment of the Fire.",
            "اے ہمارے رب! ہم ایمان لائے، پس ہمارے گناہ بخش دے اور ہمیں دوزخ کے عذاب سے بچا۔",
            "ऐ हमारे रब! हम ईमान लाए, पस हमारे गुनाह बख़्श दे और हमें दोज़ख़ के अज़ाब से बचा।",
            "Aye hamare Rab! Hum imaan laaye, pas hamare gunaah bakhsh de aur humein dozakh ke azaab se bacha."
        ),
        d(
            "رَبَّنَا آمَنَّا بِمَا أَنزَلْتَ وَاتَّبَعْنَا الرَّسُولَ فَاكْتُبْنَا مَعَ الشَّاهِدِينَ",
            "Rabbana amanna bima anzalta wattaba'nar-Rasula faktubna ma'ash-shahidin",
            "Aal-e-Imran 3:53",
            "Our Lord, we believe in what You have revealed and we follow the Messenger, so write us among those who bear witness.",
            "اے ہمارے رب! ہم اس پر ایمان لائے جو تو نے نازل فرمایا اور ہم نے رسول کی پیروی کی، پس ہمیں گواہی دینے والوں میں لکھ لے۔",
            "ऐ हमारे रब! हम उस पर ईमान लाए जो तूने नाज़िल फ़रमाया और हमने रसूल की पैरवी की, पस हमें गवाही देने वालों में लिख ले।",
            "Aye hamare Rab! Hum us par imaan laaye jo Tune naazil farmaya aur humne Rasool ki pairvi ki, pas humein gawahi dene walon mein likh le."
        ),
        d(
            "رَبَّنَا اغْفِرْ لَنَا ذُنُوبَنَا وَإِسْرَافَنَا فِي أَمْرِنَا وَثَبِّتْ أَقْدَامَنَا وَانصُرْنَا عَلَى الْقَوْمِ الْكَافِرِينَ",
            "Rabbanaghfir lana dhunubana wa israfana fi amrina wa thabbit aqdamana wansurna 'alal-qawmil-kafirin",
            "Aal-e-Imran 3:147",
            "Our Lord, forgive us our sins and our excesses in our affairs, make our feet firm, and help us against the disbelieving people.",
            "اے ہمارے رب! ہمارے گناہ اور ہمارے کام میں ہماری زیادتیاں بخش دے اور ہمارے قدم جما دے اور کافر قوم کے مقابلے میں ہماری مدد فرما۔",
            "ऐ हमारे रब! हमारे गुनाह और हमारे काम में हमारी ज़्यादतियाँ बख़्श दे और हमारे क़दम जमा दे और काफ़िर क़ौम के मुक़ाबले में हमारी मदद फ़रमा।",
            "Aye hamare Rab! Hamare gunaah aur hamare kaam mein hamari zyadtiyan bakhsh de aur hamare qadam jama de aur kafir qaum ke muqable mein hamari madad farma."
        ),
        d(
            "رَبَّنَا مَا خَلَقْتَ هَـٰذَا بَاطِلًا سُبْحَانَكَ فَقِنَا عَذَابَ النَّارِ",
            "Rabbana ma khalaqta hadha batilan, subhanaka faqina 'adhaban-nar",
            "Aal-e-Imran 3:191",
            "Our Lord, You have not created this without purpose. Glory be to You, so save us from the punishment of the Fire.",
            "اے ہمارے رب! تو نے یہ سب بے مقصد پیدا نہیں کیا، تو پاک ہے، پس ہمیں دوزخ کے عذاب سے بچا۔",
            "ऐ हमारे रब! तूने यह सब बेमक़सद पैदा नहीं किया, तू पाक है, पस हमें दोज़ख़ के अज़ाब से बचा।",
            "Aye hamare Rab! Tune yeh sab bemaqsad paida nahi kiya, Tu paak hai, pas humein dozakh ke azaab se bacha."
        ),
        d(
            "رَبَّنَا إِنَّكَ مَن تُدْخِلِ النَّارَ فَقَدْ أَخْزَيْتَهُ ۖ وَمَا لِلظَّالِمِينَ مِنْ أَنصَارٍ",
            "Rabbana innaka man tudkhilin-nara faqad akhzaytah, wa ma liz-zalimina min ansar",
            "Aal-e-Imran 3:192",
            "Our Lord, whoever You admit to the Fire, You have surely disgraced him, and the wrongdoers have no helpers.",
            "اے ہمارے رب! بے شک جسے تو نے دوزخ میں داخل کیا اسے تو نے رسوا کر دیا، اور ظالموں کا کوئی مددگار نہیں۔",
            "ऐ हमारे रब! बेशक जिसे तूने दोज़ख़ में दाख़िल किया उसे तूने रुसवा कर दिया, और ज़ालिमों का कोई मददगार नहीं।",
            "Aye hamare Rab! Beshak jise Tune dozakh mein daakhil kiya use Tune ruswa kar diya, aur zalimon ka koi madadgaar nahi."
        ),
        d(
            "رَبَّنَا إِنَّنَا سَمِعْنَا مُنَادِيًا يُنَادِي لِلْإِيمَانِ أَنْ آمِنُوا بِرَبِّكُمْ فَآمَنَّا",
            "Rabbana innana sami'na munadiyan yunadi lil-imani an aminu bi-Rabbikum fa-amanna",
            "Aal-e-Imran 3:193",
            "Our Lord, we heard a caller calling to faith, saying: Believe in your Lord, so we believed.",
            "اے ہمارے رب! ہم نے ایک پکارنے والے کو سنا جو ایمان کی طرف بلاتا تھا کہ اپنے رب پر ایمان لاؤ، تو ہم ایمان لے آئے۔",
            "ऐ हमारे रब! हमने एक पुकारने वाले को सुना जो ईमान की तरफ़ बुलाता था कि अपने रब पर ईमान लाओ, तो हम ईमान ले आए।",
            "Aye hamare Rab! Humne ek pukaarne wale ko suna jo imaan ki taraf bulata tha ke apne Rab par imaan lao, to hum imaan le aaye."
        ),
        d(
            "رَبَّنَا فَاغْفِرْ لَنَا ذُنُوبَنَا وَكَفِّرْ عَنَّا سَيِّئَاتِنَا وَتَوَفَّنَا مَعَ الْأَبْرَارِ",
            "Rabbana faghfir lana dhunubana wa kaffir 'anna sayyi'atina wa tawaffana ma'al-abrar",
            "Aal-e-Imran 3:193",
            "Our Lord, forgive us our sins, remove from us our misdeeds, and let us die with the righteous.",
            "اے ہمارے رب! ہمارے گناہ بخش دے اور ہماری برائیاں ہم سے مٹا دے اور ہمیں نیک لوگوں کے ساتھ موت دے۔",
            "ऐ हमारे रब! हमारे गुनाह बख़्श दे और हमारी बुराइयाँ हमसे मिटा दे और हमें नेक लोगों के साथ मौत दे।",
            "Aye hamare Rab! Hamare gunaah bakhsh de aur hamari buraiyan hum se mita de aur humein nek logon ke saath maut de."
        ),
        d(
            "رَبَّنَا وَآتِنَا مَا وَعَدتَّنَا عَلَىٰ رُسُلِكَ وَلَا تُخْزِنَا يَوْمَ الْقِيَامَةِ ۗ إِنَّكَ لَا تُخْلِفُ الْمِيعَادَ",
            "Rabbana wa atina ma wa'adtana 'ala rusulika wa la tukhzina yawmal-qiyamah, innaka la tukhliful-mi'ad",
            "Aal-e-Imran 3:194",
            "Our Lord, grant us what You promised us through Your messengers, and do not disgrace us on the Day of Resurrection. Surely You do not break Your promise.",
            "اے ہمارے رب! ہمیں وہ عطا فرما جس کا تو نے اپنے رسولوں کے ذریعے ہم سے وعدہ فرمایا اور قیامت کے دن ہمیں رسوا نہ کر، بے شک تو وعدے کے خلاف نہیں کرتا۔",
            "ऐ हमारे रब! हमें वह अता फ़रमा जिसका तूने अपने रसूलों के ज़रिए हमसे वादा फ़रमाया और क़यामत के दिन हमें रुसवा न कर, बेशक तू वादे के ख़िलाफ़ नहीं करता।",
            "Aye hamare Rab! Humein woh ata farma jiska Tune apne Rasoolon ke zariye hum se waada farmaya aur qayamat ke din humein ruswa na kar, beshak Tu waade ke khilaf nahi karta."
        ),
        d(
            "رَبَّنَا آمَنَّا فَاكْتُبْنَا مَعَ الشَّاهِدِينَ",
            "Rabbana amanna faktubna ma'ash-shahidin",
            "Al-Ma'idah 5:83",
            "Our Lord, we have believed, so write us among those who bear witness.",
            "اے ہمارے رب! ہم ایمان لائے، پس ہمیں گواہی دینے والوں میں لکھ لے۔",
            "ऐ हमारे रब! हम ईमान लाए, पस हमें गवाही देने वालों में लिख ले।",
            "Aye hamare Rab! Hum imaan laaye, pas humein gawahi dene walon mein likh le."
        ),
        d(
            "رَبَّنَا أَنزِلْ عَلَيْنَا مَائِدَةً مِّنَ السَّمَاءِ تَكُونُ لَنَا عِيدًا لِّأَوَّلِنَا وَآخِرِنَا وَآيَةً مِّنكَ ۖ وَارْزُقْنَا وَأَنتَ خَيْرُ الرَّازِقِينَ",
            "Rabbana anzil 'alayna ma'idatan minas-sama'i takunu lana 'idan li-awwalina wa akhirina wa ayatan minka, warzuqna wa Anta khayrur-raziqin",
            "Al-Ma'idah 5:114",
            "Our Lord, send down to us a table spread from heaven, to be a festival for the first of us and the last of us and a sign from You. Provide for us, for You are the best of providers.",
            "اے ہمارے رب! ہم پر آسمان سے ایک دسترخوان نازل فرما جو ہمارے اگلوں اور پچھلوں کے لیے عید ہو اور تیری طرف سے ایک نشانی ہو، اور ہمیں رزق عطا فرما اور تو سب سے بہتر رزق دینے والا ہے۔",
            "ऐ हमारे रब! हम पर आसमान से एक दस्तरख़्वान नाज़िल फ़रमा जो हमारे अगलों और पिछलों के लिए ईद हो और तेरी तरफ़ से एक निशानी हो, और हमें रिज़्क़ अता फ़रमा और तू सबसे बेहतर रिज़्क़ देने वाला है।",
            "Aye hamare Rab! Hum par aasmaan se ek dastarkhwan naazil farma jo hamare aglon aur pichhlon ke liye Eid ho aur Teri taraf se ek nishani ho, aur humein rizq ata farma aur Tu sab se behtar rizq dene wala hai."
        ),
        d(
            "رَبَّنَا ظَلَمْنَا أَنفُسَنَا وَإِن لَّمْ تَغْفِرْ لَنَا وَتَرْحَمْنَا لَنَكُونَنَّ مِنَ الْخَاسِرِينَ",
            "Rabbana zalamna anfusana wa in lam taghfir lana wa tarhamna lanakunanna minal-khasirin",
            "Al-A'raf 7:23",
            "Our Lord, we have wronged ourselves. If You do not forgive us and have mercy on us, we will surely be among the losers.",
            "اے ہمارے رب! ہم نے اپنی جانوں پر ظلم کیا، اور اگر تو نے ہمیں نہ بخشا اور ہم پر رحم نہ فرمایا تو ہم ضرور نقصان اٹھانے والوں میں سے ہو جائیں گے۔",
            "ऐ हमारे रब! हमने अपनी जानों पर ज़ुल्म किया, और अगर तूने हमें न बख़्शा और हम पर रहम न फ़रमाया तो हम ज़रूर नुक़सान उठाने वालों में से हो जाएँगे।",
            "Aye hamare Rab! Humne apni jaanon par zulm kiya, aur agar Tune humein na bakhsha aur hum par reham na farmaya to hum zaroor nuqsaan uthane walon mein se ho jaayenge."
        ),
        d(
            "رَبَّنَا لَا تَجْعَلْنَا مَعَ الْقَوْمِ الظَّالِمِينَ",
            "Rabbana la taj'alna ma'al-qawmiz-zalimin",
            "Al-A'raf 7:47",
            "Our Lord, do not place us with the wrongdoing people.",
            "اے ہمارے رب! ہمیں ظالم لوگوں کے ساتھ نہ کر۔",
            "ऐ हमारे रब! हमें ज़ालिम लोगों के साथ न कर।",
            "Aye hamare Rab! Humein zalim logon ke saath na kar."
        ),
        d(
            "رَبَّنَا افْتَحْ بَيْنَنَا وَبَيْنَ قَوْمِنَا بِالْحَقِّ وَأَنتَ خَيْرُ الْفَاتِحِينَ",
            "Rabbanaftah baynana wa bayna qawmina bil-haqqi wa Anta khayrul-fatihin",
            "Al-A'raf 7:89",
            "Our Lord, decide between us and our people with truth, for You are the best of those who decide.",
            "اے ہمارے رب! ہمارے اور ہماری قوم کے درمیان حق کے ساتھ فیصلہ فرما دے اور تو سب سے بہتر فیصلہ فرمانے والا ہے۔",
            "ऐ हमारे रब! हमारे और हमारी क़ौम के दरमियान हक़ के साथ फ़ैसला फ़रमा दे और तू सबसे बेहतर फ़ैसला फ़रमाने वाला है।",
            "Aye hamare Rab! Hamare aur hamari qaum ke darmiyan haq ke saath faisla farma de aur Tu sab se behtar faisla farmane wala hai."
        ),
        d(
            "رَبَّنَا أَفْرِغْ عَلَيْنَا صَبْرًا وَتَوَفَّنَا مُسْلِمِينَ",
            "Rabbana afrigh 'alayna sabran wa tawaffana muslimin",
            "Al-A'raf 7:126",
            "Our Lord, pour patience upon us and let us die as Muslims.",
            "اے ہمارے رب! ہم پر صبر انڈیل دے اور ہمیں مسلمان ہونے کی حالت میں موت دے۔",
            "ऐ हमारे रब! हम पर सब्र उंडेल दे और हमें मुसलमान होने की हालत में मौत दे।",
            "Aye hamare Rab! Hum par sabr undel de aur humein musalmaan hone ki haalat mein maut de."
        ),
        d(
            "رَبَّنَا لَا تَجْعَلْنَا فِتْنَةً لِّلْقَوْمِ الظَّالِمِينَ وَنَجِّنَا بِرَحْمَتِكَ مِنَ الْقَوْمِ الْكَافِرِينَ",
            "Rabbana la taj'alna fitnatan lil-qawmiz-zalimin, wa najjina bi-rahmatika minal-qawmil-kafirin",
            "Yunus 10:85-86",
            "Our Lord, do not make us a trial for the wrongdoing people, and save us by Your mercy from the disbelieving people.",
            "اے ہمارے رب! ہمیں ظالم لوگوں کے لیے آزمائش نہ بنا، اور اپنی رحمت سے ہمیں کافر قوم سے نجات دے۔",
            "ऐ हमारे रब! हमें ज़ालिम लोगों के लिए आज़माइश न बना, और अपनी रहमत से हमें काफ़िर क़ौम से निजात दे।",
            "Aye hamare Rab! Humein zalim logon ke liye aazmaish na bana, aur apni rehmat se humein kafir qaum se nijaat de."
        ),
        d(
            "رَبَّنَا إِنَّكَ تَعْلَمُ مَا نُخْفِي وَمَا نُعْلِنُ ۗ وَمَا يَخْفَىٰ عَلَى اللَّهِ مِن شَيْءٍ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ",
            "Rabbana innaka ta'lamu ma nukhfi wa ma nu'lin, wa ma yakhfa 'alallahi min shay'in fil-ardi wa la fis-sama'",
            "Ibrahim 14:38",
            "Our Lord, You surely know what we conceal and what we reveal. Nothing on earth or in heaven is hidden from Allah.",
            "اے ہمارے رب! بے شک تو جانتا ہے جو ہم چھپاتے ہیں اور جو ظاہر کرتے ہیں، اور اللہ سے کوئی چیز چھپی نہیں، نہ زمین میں اور نہ آسمان میں۔",
            "ऐ हमारे रब! बेशक तू जानता है जो हम छुपाते हैं और जो ज़ाहिर करते हैं, और अल्लाह से कोई चीज़ छुपी नहीं, न ज़मीन में और न आसमान में।",
            "Aye hamare Rab! Beshak Tu jaanta hai jo hum chhupate hain aur jo zaahir karte hain, aur Allah se koi cheez chhupi nahi, na zameen mein aur na aasmaan mein."
        ),
        d(
            "رَبِّ اجْعَلْنِي مُقِيمَ الصَّلَاةِ وَمِن ذُرِّيَّتِي ۚ رَبَّنَا وَتَقَبَّلْ دُعَاءِ",
            "Rabbij'alni muqimas-salati wa min dhurriyyati, Rabbana wa taqabbal du'a",
            "Ibrahim 14:40",
            "My Lord, make me one who establishes prayer, and my descendants too. Our Lord, accept my supplication.",
            "اے میرے رب! مجھے نماز قائم کرنے والا بنا اور میری اولاد میں سے بھی، اے ہمارے رب! اور میری دعا قبول فرما۔",
            "ऐ मेरे रब! मुझे नमाज़ क़ायम करने वाला बना और मेरी औलाद में से भी, ऐ हमारे रब! और मेरी दुआ क़बूल फ़रमा।",
            "Aye mere Rab! Mujhe namaz qaayam karne wala bana aur meri aulaad mein se bhi, aye hamare Rab! Aur meri dua qabool farma."
        ),
        d(
            "رَبَّنَا اغْفِرْ لِي وَلِوَالِدَيَّ وَلِلْمُؤْمِنِينَ يَوْمَ يَقُومُ الْحِسَابُ",
            "Rabbanaghfir li wa li-walidayya wa lil-mu'minina yawma yaqumul-hisab",
            "Ibrahim 14:41",
            "Our Lord, forgive me, my parents and the believers on the Day when the reckoning is established.",
            "اے ہمارے رب! مجھے اور میرے ماں باپ کو اور سب ایمان والوں کو اس دن بخش دے جس دن حساب قائم ہوگا۔",
            "ऐ हमारे रब! मुझे और मेरे माँ-बाप को और सब ईमान वालों को उस दिन बख़्श दे जिस दिन हिसाब क़ायम होगा।",
            "Aye hamare Rab! Mujhe aur mere maa-baap ko aur sab imaan walon ko us din bakhsh de jis din hisaab qaayam hoga."
        ),
        d(
            "رَبَّنَا آتِنَا مِن لَّدُنكَ رَحْمَةً وَهَيِّئْ لَنَا مِنْ أَمْرِنَا رَشَدًا",
            "Rabbana atina min ladunka rahmatan wa hayyi' lana min amrina rashada",
            "Al-Kahf 18:10",
            "Our Lord, grant us mercy from Yourself and provide for us right guidance in our affair.",
            "اے ہمارے رب! ہمیں اپنے پاس سے رحمت عطا فرما اور ہمارے کام میں ہمارے لیے رہنمائی کا سامان فرما۔",
            "ऐ हमारे रब! हमें अपने पास से रहमत अता फ़रमा और हमारे काम में हमारे लिए रहनुमाई का सामान फ़रमा।",
            "Aye hamare Rab! Humein apne paas se rehmat ata farma aur hamare kaam mein hamare liye rehnumai ka saamaan farma."
        ),
        d(
            "رَبَّنَا إِنَّنَا نَخَافُ أَن يَفْرُطَ عَلَيْنَا أَوْ أَن يَطْغَىٰ",
            "Rabbana innana nakhafu an yafruta 'alayna aw an yatgha",
            "Ta-Ha 20:45",
            "Our Lord, we fear that he may hasten to harm us or that he may transgress.",
            "اے ہمارے رب! ہمیں ڈر ہے کہ وہ ہم پر زیادتی کرے یا سرکشی کرے۔",
            "ऐ हमारे रब! हमें डर है कि वह हम पर ज़्यादती करे या सरकशी करे।",
            "Aye hamare Rab! Humein dar hai ke woh hum par zyadti kare ya sarkashi kare."
        ),
        d(
            "رَبَّنَا آمَنَّا فَاغْفِرْ لَنَا وَارْحَمْنَا وَأَنتَ خَيْرُ الرَّاحِمِينَ",
            "Rabbana amanna faghfir lana warhamna wa Anta khayrur-rahimin",
            "Al-Mu'minun 23:109",
            "Our Lord, we have believed, so forgive us and have mercy on us, for You are the best of the merciful.",
            "اے ہمارے رب! ہم ایمان لائے، پس ہمیں بخش دے اور ہم پر رحم فرما اور تو سب سے بہتر رحم فرمانے والا ہے۔",
            "ऐ हमारे रब! हम ईमान लाए, पस हमें बख़्श दे और हम पर रहम फ़रमा और तू सबसे बेहतर रहम फ़रमाने वाला है।",
            "Aye hamare Rab! Hum imaan laaye, pas humein bakhsh de aur hum par reham farma aur Tu sab se behtar reham farmane wala hai."
        ),
        d(
            "رَبَّنَا اصْرِفْ عَنَّا عَذَابَ جَهَنَّمَ ۖ إِنَّ عَذَابَهَا كَانَ غَرَامًا إِنَّهَا سَاءَتْ مُسْتَقَرًّا وَمُقَامًا",
            "Rabbanasrif 'anna 'adhaba jahannam, inna 'adhabaha kana gharama, innaha sa'at mustaqarran wa muqama",
            "Al-Furqan 25:65-66",
            "Our Lord, turn away from us the punishment of Hell. Its punishment is truly unrelenting; it is indeed an evil abode and resting place.",
            "اے ہمارے رب! ہم سے جہنم کا عذاب پھیر دے، بے شک اس کا عذاب چمٹ جانے والا ہے، بے شک وہ بہت برا ٹھکانا اور بری جگہ ہے۔",
            "ऐ हमारे रब! हमसे जहन्नम का अज़ाब फेर दे, बेशक उसका अज़ाब चिमट जाने वाला है, बेशक वह बहुत बुरा ठिकाना और बुरी जगह है।",
            "Aye hamare Rab! Hum se jahannam ka azaab pher de, beshak uska azaab chimat jaane wala hai, beshak woh bahut bura thikana aur buri jagah hai."
        ),
        d(
            "رَبَّنَا هَبْ لَنَا مِنْ أَزْوَاجِنَا وَذُرِّيَّاتِنَا قُرَّةَ أَعْيُنٍ وَاجْعَلْنَا لِلْمُتَّقِينَ إِمَامًا",
            "Rabbana hab lana min azwajina wa dhurriyyatina qurrata a'yunin waj'alna lil-muttaqina imama",
            "Al-Furqan 25:74",
            "Our Lord, grant us from our spouses and our children the coolness of our eyes, and make us leaders of the God-fearing.",
            "اے ہمارے رب! ہمیں ہماری بیویوں اور ہماری اولاد سے آنکھوں کی ٹھنڈک عطا فرما اور ہمیں پرہیزگاروں کا پیشوا بنا۔",
            "ऐ हमारे रब! हमें हमारी बीवियों और हमारी औलाद से आँखों की ठंडक अता फ़रमा और हमें परहेज़गारों का पेशवा बना।",
            "Aye hamare Rab! Humein hamari biwiyon aur hamari aulaad se aankhon ki thandak ata farma aur humein parhezgaaron ka peshwa bana."
        ),
        d(
            "إِنَّ رَبَّنَا لَغَفُورٌ شَكُورٌ",
            "Inna Rabbana la-Ghafurun Shakur",
            "Fatir 35:34",
            "Surely our Lord is All-Forgiving, Most Appreciative.",
            "بے شک ہمارا رب بخشنے والا، قدر فرمانے والا ہے۔",
            "बेशक हमारा रब बख़्शने वाला, क़द्र फ़रमाने वाला है।",
            "Beshak hamara Rab bakhshne wala, qadr farmane wala hai."
        ),
        d(
            "رَبَّنَا وَسِعْتَ كُلَّ شَيْءٍ رَّحْمَةً وَعِلْمًا فَاغْفِرْ لِلَّذِينَ تَابُوا وَاتَّبَعُوا سَبِيلَكَ وَقِهِمْ عَذَابَ الْجَحِيمِ",
            "Rabbana wasi'ta kulla shay'in rahmatan wa 'ilman faghfir lilladhina tabu wattaba'u sabilaka wa qihim 'adhabal-jahim",
            "Ghafir 40:7",
            "Our Lord, You encompass everything in mercy and knowledge, so forgive those who repent and follow Your path, and save them from the punishment of Hell.",
            "اے ہمارے رب! تیری رحمت اور علم ہر چیز کو گھیرے ہوئے ہے، پس انہیں بخش دے جنہوں نے توبہ کی اور تیرے راستے کی پیروی کی اور انہیں دوزخ کے عذاب سے بچا۔",
            "ऐ हमारे रब! तेरी रहमत और इल्म हर चीज़ को घेरे हुए है, पस उन्हें बख़्श दे जिन्होंने तौबा की और तेरे रास्ते की पैरवी की और उन्हें दोज़ख़ के अज़ाब से बचा।",
            "Aye hamare Rab! Teri rehmat aur ilm har cheez ko ghere hue hai, pas unhein bakhsh de jinhone tauba ki aur Tere raaste ki pairvi ki aur unhein dozakh ke azaab se bacha."
        ),
        d(
            "رَبَّنَا وَأَدْخِلْهُمْ جَنَّاتِ عَدْنٍ الَّتِي وَعَدتَّهُمْ وَمَن صَلَحَ مِنْ آبَائِهِمْ وَأَزْوَاجِهِمْ وَذُرِّيَّاتِهِمْ ۚ إِنَّكَ أَنتَ الْعَزِيزُ الْحَكِيمُ وَقِهِمُ السَّيِّئَاتِ ۚ وَمَن تَقِ السَّيِّئَاتِ يَوْمَئِذٍ فَقَدْ رَحِمْتَهُ ۚ وَذَٰلِكَ هُوَ الْفَوْزُ الْعَظِيمُ",
            "Rabbana wa adkhilhum jannati 'adninillati wa'adtahum wa man salaha min aba'ihim wa azwajihim wa dhurriyyatihim, innaka Antal-'Azizul-Hakim, wa qihimus-sayyi'at, wa man taqis-sayyi'ati yawma'idhin faqad rahimtah, wa dhalika huwal-fawzul-'azim",
            "Ghafir 40:8-9",
            "Our Lord, admit them to the Gardens of Eternity which You have promised them, along with the righteous among their parents, spouses and children. Surely You are the Almighty, the All-Wise. And protect them from evil deeds; whoever You protect from evil that Day, You have truly shown him mercy, and that is the supreme success.",
            "اے ہمارے رب! انہیں ہمیشہ رہنے کے ان باغوں میں داخل فرما جن کا تو نے ان سے وعدہ فرمایا ہے، اور ان کے باپ دادا، بیویوں اور اولاد میں سے جو نیک ہوں انہیں بھی، بے شک تو ہی غالب، حکمت والا ہے۔ اور انہیں برائیوں سے بچا، اور جسے تو نے اس دن برائیوں سے بچا لیا تو بے شک تو نے اس پر رحم فرمایا، اور یہی بڑی کامیابی ہے۔",
            "ऐ हमारे रब! उन्हें हमेशा रहने के उन बाग़ों में दाख़िल फ़रमा जिनका तूने उनसे वादा फ़रमाया है, और उनके बाप-दादा, बीवियों और औलाद में से जो नेक हों उन्हें भी, बेशक तू ही ग़ालिब, हिकमत वाला है। और उन्हें बुराइयों से बचा, और जिसे तूने उस दिन बुराइयों से बचा लिया तो बेशक तूने उस पर रहम फ़रमाया, और यही बड़ी कामयाबी है।",
            "Aye hamare Rab! Unhein hamesha rehne ke un baaghon mein daakhil farma jinka Tune unse waada farmaya hai, aur unke baap-dada, biwiyon aur aulaad mein se jo nek hon unhein bhi, beshak Tu hi ghalib, hikmat wala hai. Aur unhein buraiyon se bacha, aur jise Tune us din buraiyon se bacha liya to beshak Tune us par reham farmaya, aur yahi badi kaamyabi hai."
        ),
        d(
            "رَبَّنَا اغْفِرْ لَنَا وَلِإِخْوَانِنَا الَّذِينَ سَبَقُونَا بِالْإِيمَانِ وَلَا تَجْعَلْ فِي قُلُوبِنَا غِلًّا لِّلَّذِينَ آمَنُوا",
            "Rabbanaghfir lana wa li-ikhwaninalladhina sabaquna bil-imani wa la taj'al fi qulubina ghillan lilladhina amanu",
            "Al-Hashr 59:10",
            "Our Lord, forgive us and our brothers who preceded us in faith, and do not place in our hearts any ill-will towards those who believe.",
            "اے ہمارے رب! ہمیں اور ہمارے ان بھائیوں کو بخش دے جو ہم سے پہلے ایمان لائے، اور ہمارے دلوں میں ایمان والوں کے لیے کوئی کینہ نہ رکھ۔",
            "ऐ हमारे रब! हमें और हमारे उन भाइयों को बख़्श दे जो हमसे पहले ईमान लाए, और हमारे दिलों में ईमान वालों के लिए कोई कीना न रख।",
            "Aye hamare Rab! Humein aur hamare un bhaiyon ko bakhsh de jo hum se pehle imaan laaye, aur hamare dilon mein imaan walon ke liye koi keena na rakh."
        ),
        d(
            "رَبَّنَا إِنَّكَ رَءُوفٌ رَّحِيمٌ",
            "Rabbana innaka Ra'ufur-Rahim",
            "Al-Hashr 59:10",
            "Our Lord, surely You are Most Kind, Most Merciful.",
            "اے ہمارے رب! بے شک تو نہایت شفقت والا، مہربان ہے۔",
            "ऐ हमारे रब! बेशक तू निहायत शफ़क़त वाला, मेहरबान है।",
            "Aye hamare Rab! Beshak Tu nihayat shafqat wala, meharbaan hai."
        ),
        d(
            "رَبَّنَا عَلَيْكَ تَوَكَّلْنَا وَإِلَيْكَ أَنَبْنَا وَإِلَيْكَ الْمَصِيرُ",
            "Rabbana 'alayka tawakkalna wa ilayka anabna wa ilaykal-masir",
            "Al-Mumtahanah 60:4",
            "Our Lord, in You we put our trust, to You we turn, and to You is the final return.",
            "اے ہمارے رب! ہم نے تجھی پر بھروسا کیا اور تیری ہی طرف رجوع کیا اور تیری ہی طرف لوٹنا ہے۔",
            "ऐ हमारे रब! हमने तुझी पर भरोसा किया और तेरी ही तरफ़ रुजू किया और तेरी ही तरफ़ लौटना है।",
            "Aye hamare Rab! Humne Tujhi par bharosa kiya aur Teri hi taraf rujoo kiya aur Teri hi taraf lautna hai."
        ),
        d(
            "رَبَّنَا لَا تَجْعَلْنَا فِتْنَةً لِّلَّذِينَ كَفَرُوا وَاغْفِرْ لَنَا رَبَّنَا ۖ إِنَّكَ أَنتَ الْعَزِيزُ الْحَكِيمُ",
            "Rabbana la taj'alna fitnatan lilladhina kafaru waghfir lana Rabbana, innaka Antal-'Azizul-Hakim",
            "Al-Mumtahanah 60:5",
            "Our Lord, do not make us a trial for those who disbelieve, and forgive us, our Lord. Surely You are the Almighty, the All-Wise.",
            "اے ہمارے رب! ہمیں کافروں کے لیے آزمائش نہ بنا اور ہمیں بخش دے، اے ہمارے رب! بے شک تو ہی غالب، حکمت والا ہے۔",
            "ऐ हमारे रब! हमें काफ़िरों के लिए आज़माइश न बना और हमें बख़्श दे, ऐ हमारे रब! बेशक तू ही ग़ालिब, हिकमत वाला है।",
            "Aye hamare Rab! Humein kafiron ke liye aazmaish na bana aur humein bakhsh de, aye hamare Rab! Beshak Tu hi ghalib, hikmat wala hai."
        ),
        d(
            "رَبَّنَا أَتْمِمْ لَنَا نُورَنَا وَاغْفِرْ لَنَا ۖ إِنَّكَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
            "Rabbana atmim lana nurana waghfir lana, innaka 'ala kulli shay'in Qadir",
            "At-Tahrim 66:8",
            "Our Lord, perfect our light for us and forgive us. Surely You have power over all things.",
            "اے ہمارے رب! ہمارے لیے ہمارا نور پورا فرما دے اور ہمیں بخش دے، بے شک تو ہر چیز پر قادر ہے۔",
            "ऐ हमारे रब! हमारे लिए हमारा नूर पूरा फ़रमा दे और हमें बख़्श दे, बेशक तू हर चीज़ पर क़ादिर है।",
            "Aye hamare Rab! Hamare liye hamara noor poora farma de aur humein bakhsh de, beshak Tu har cheez par qaadir hai."
        )
    ).mapIndexed { index, dua -> dua.copy(number = index + 1) }

    private fun d(
        arabic: String, transliteration: String, reference: String,
        english: String, urdu: String, hindi: String, hinglish: String
    ) = RabbanaDua(0, arabic, transliteration, reference, english, urdu, hindi, hinglish)
}
