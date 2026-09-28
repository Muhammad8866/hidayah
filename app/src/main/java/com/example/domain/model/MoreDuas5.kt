package com.example.domain.model

// Fifth batch of authentic duas (Ahle Sunnat wal Jamaat sources, with references).
internal object MoreDuas5 {

    private const val QURAN = "The Noble Quran"
    private const val MUTAWATIR = "Mutawatir (Quranic Verse)"

    private fun d(
        id: String, category: String, title: String, context: String,
        arabic: String, translit: String, roman: String, urdu: String,
        benefit: String, collection: String, reference: String, grading: String,
        count: Int = 1
    ) = Dua(
        id = id, category = category, title = title, context = context,
        arabicText = arabic, transliteration = translit,
        englishTranslation = roman, urduTranslation = urdu, benefit = benefit,
        sourceCollection = collection, sourceReference = reference,
        authenticityGrading = grading, recommendedCount = count
    )

    val LIST = listOf(
        // ---------------- Comprehensive Duas ----------------
        d(
            "dua_rabbi_ainni", "Comprehensive Duas", "My Lord, Help Me and Not Against Me",
            "Nabi ﷺ ki jaami' dua (ibtidaai hissa).",
            "رَبِّ أَعِنِّي وَلَا تُعِنْ عَلَيَّ، وَانْصُرْنِي وَلَا تَنْصُرْ عَلَيَّ، وَامْكُرْ لِي وَلَا تَمْكُرْ عَلَيَّ، وَاهْدِنِي وَيَسِّرِ الْهُدَىٰ لِي، وَانْصُرْنِي عَلَىٰ مَنْ بَغَىٰ عَلَيَّ",
            "Rabbi a'innī wa lā tu'in 'alayy, wanṣurnī wa lā tanṣur 'alayy, wamkur lī wa lā tamkur 'alayy, wahdinī wa yassiril-hudā lī, wanṣurnī 'alā man baghā 'alayy",
            "Aye mere Rab, meri madad kar aur mere khilaaf madad na kar, mujhe ghalba de aur mujh par ghalba na de, mere haq mein tadbeer kar aur mere khilaaf tadbeer na kar, mujhe hidaayat de aur hidaayat ko mere liye aasaan kar, aur jo mujh par zulm kare uske khilaaf meri madad kar.",
            "اے میرے رب! میری مدد کر اور میرے خلاف مدد نہ کر، مجھے غلبہ دے اور مجھ پر غلبہ نہ دے، میرے حق میں تدبیر کر اور میرے خلاف تدبیر نہ کر، مجھے ہدایت دے اور ہدایت کو میرے لیے آسان کر، اور جو مجھ پر زیادتی کرے اس کے خلاف میری مدد کر",
            "Dunya aur deen ki har madad ko jama karne wali dua.",
            "Jami` at-Tirmidhi", "Hadith 3551; Sunan Abi Dawud 1510", "Hasan Sahih"
        ),
        d(
            "dua_ati_nafsi_taqwaha", "Comprehensive Duas", "Grant My Soul Its Taqwa",
            "Nafs ki islaah aur tazkiya ke liye.",
            "اللَّهُمَّ آتِ نَفْسِي تَقْوَاهَا، وَزَكِّهَا أَنْتَ خَيْرُ مَنْ زَكَّاهَا، أَنْتَ وَلِيُّهَا وَمَوْلَاهَا",
            "Allāhumma āti nafsī taqwāhā, wa zakkihā Anta khayru man zakkāhā, Anta waliyyuhā wa mawlāhā",
            "Ya Allah, mere nafs ko uska taqwa ata farma aur use paak kar de, Tu hi use sab se behtar paak karne wala hai, Tu hi uska kaarsaaz aur maalik hai.",
            "اے اللہ! میرے نفس کو اس کا تقویٰ عطا فرما اور اسے پاک کر دے، تو ہی اسے سب سے بہتر پاک کرنے والا ہے، تو ہی اس کا کارساز اور مالک ہے",
            "Nabi ﷺ yeh dua farmaya karte the.",
            "Sahih Muslim", "Hadith 2722", "Sahih"
        ),
        d(
            "dua_iqsim_lana_khashyatik", "Comprehensive Duas", "Grant Us Such Fear of You",
            "Majlis se uthte waqt — Nabi ﷺ aksar yeh dua kiye baghair na uthte.",
            "اللَّهُمَّ اقْسِمْ لَنَا مِنْ خَشْيَتِكَ مَا يَحُولُ بَيْنَنَا وَبَيْنَ مَعَاصِيكَ، وَمِنْ طَاعَتِكَ مَا تُبَلِّغُنَا بِهِ جَنَّتَكَ، وَمِنَ الْيَقِينِ مَا تُهَوِّنُ بِهِ عَلَيْنَا مُصِيبَاتِ الدُّنْيَا، وَمَتِّعْنَا بِأَسْمَاعِنَا وَأَبْصَارِنَا وَقُوَّتِنَا مَا أَحْيَيْتَنَا، وَاجْعَلْهُ الْوَارِثَ مِنَّا، وَاجْعَلْ ثَأْرَنَا عَلَىٰ مَنْ ظَلَمَنَا، وَانْصُرْنَا عَلَىٰ مَنْ عَادَانَا، وَلَا تَجْعَلْ مُصِيبَتَنَا فِي دِينِنَا، وَلَا تَجْعَلِ الدُّنْيَا أَكْبَرَ هَمِّنَا وَلَا مَبْلَغَ عِلْمِنَا، وَلَا تُسَلِّطْ عَلَيْنَا مَنْ لَا يَرْحَمُنَا",
            "Allāhummaqsim lanā min khashyatika mā yaḥūlu baynanā wa bayna ma'āṣīk, wa min ṭā'atika mā tuballighunā bihī jannatak, wa minal-yaqīni mā tuhawwinu bihī 'alaynā muṣībātid-dunyā, wa matti'nā bi asmā'inā wa abṣārinā wa quwwatinā mā aḥyaytanā, waj'alhul-wāritha minnā, waj'al tha'ranā 'alā man ẓalamanā, wanṣurnā 'alā man 'ādānā, wa lā taj'al muṣībatanā fī dīninā, wa lā taj'alid-dunyā akbara hamminā wa lā mablagha 'ilminā, wa lā tusalliṭ 'alaynā man lā yarḥamunā",
            "Ya Allah, hamein apna itna khauf ata kar jo hamare aur Teri nafarmaani ke darmiyaan haail ho jaaye, apni itni itaa'at jo hamein Teri jannat tak pahuncha de, aur itna yaqeen jis se dunya ki museebatein hum par aasaan ho jaayein. Jab tak hamein zinda rakhe hamare kaanon, aankhon aur quwwat se hamein faida pahuncha aur ise hamara waaris bana. Hamara badla us se le jo hum par zulm kare, dushmanon ke khilaaf hamari madad kar, hamari museebat hamare deen mein na daal, dunya ko hamara sab se bara gham aur hamare ilm ki intiha na bana, aur hum par aise shakhs ko musallat na kar jo hum par rahm na kare.",
            "اے اللہ! ہمیں اپنا اتنا خوف عطا کر جو ہمارے اور تیری نافرمانیوں کے درمیان حائل ہو جائے، اپنی اتنی اطاعت جو ہمیں تیری جنت تک پہنچا دے، اور اتنا یقین جس سے دنیا کی مصیبتیں ہم پر آسان ہو جائیں۔ جب تک ہمیں زندہ رکھے ہمارے کانوں، آنکھوں اور قوت سے فائدہ پہنچا اور اسے ہمارا وارث بنا، ہمارا بدلہ اس سے لے جو ہم پر ظلم کرے، دشمنوں کے خلاف ہماری مدد کر، ہماری مصیبت ہمارے دین میں نہ ڈال، دنیا کو ہمارا سب سے بڑا غم اور ہمارے علم کی انتہا نہ بنا، اور ہم پر ایسے شخص کو مسلط نہ کر جو ہم پر رحم نہ کرے",
            "Deen, dunya aur aakhirat ki bhalaaiyon ki jaami' dua.",
            "Jami` at-Tirmidhi", "Hadith 3502", "Hasan"
        ),
        d(
            "dua_infani_bima_allamtani", "Comprehensive Duas", "Benefit Me With What You Taught Me",
            "Ilm haasil karte waqt ya parhaai se pehle.",
            "اللَّهُمَّ انْفَعْنِي بِمَا عَلَّمْتَنِي، وَعَلِّمْنِي مَا يَنْفَعُنِي، وَزِدْنِي عِلْمًا",
            "Allāhummanfa'nī bimā 'allamtanī, wa 'allimnī mā yanfa'unī, wa zidnī 'ilmā",
            "Ya Allah, jo Tu ne mujhe sikhaya us se mujhe faida de, mujhe woh sikha jo mujhe faida de, aur mere ilm mein izaafa farma.",
            "اے اللہ! جو تو نے مجھے سکھایا اس سے مجھے فائدہ دے، مجھے وہ سکھا جو مجھے فائدہ دے، اور میرے علم میں اضافہ فرما",
            "Nafe' ilm aur uspar amal ki dua.",
            "Sunan Ibn Majah", "Hadith 251; Jami` at-Tirmidhi 3599", "Sahih"
        ),

        // ---------------- Protection & Healing ----------------
        d(
            "dua_jar_su", "Protection & Healing", "Refuge From a Bad Neighbour",
            "Bure padosi se hifaazat ke liye.",
            "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ جَارِ السُّوءِ فِي دَارِ الْمُقَامَةِ، فَإِنَّ جَارَ الْبَادِيَةِ يَتَحَوَّلُ",
            "Allāhumma innī a'ūdhu bika min jāris-sū'i fī dāril-muqāmah, fa inna jāral-bādiyati yataḥawwal",
            "Ya Allah, main mustaqil rehne ki jagah ke bure padosi se Teri panaah maangta hoon, kyunki safar ka padosi to badal jaata hai.",
            "اے اللہ! میں مستقل رہنے کی جگہ کے برے پڑوسی سے تیری پناہ مانگتا ہوں، کیونکہ سفر کا پڑوسی تو بدل جاتا ہے",
            "Nabi ﷺ ne is se panaah maangne ka hukm diya.",
            "Sunan an-Nasa'i", "Hadith 5502", "Hasan"
        ),
        d(
            "dua_ishfi_abdak", "Protection & Healing", "For a Sick Person's Recovery",
            "Mareez ki iyaadat ke waqt uske liye.",
            "اللَّهُمَّ اشْفِ عَبْدَكَ يَنْكَأُ لَكَ عَدُوًّا، أَوْ يَمْشِي لَكَ إِلَىٰ صَلَاةٍ",
            "Allāhummashfi 'abdaka yanka'u laka 'aduwwan, aw yamshī laka ilā ṣalāh",
            "Ya Allah, apne bande ko shifa de taake woh Tere liye dushman ka muqaabla kare ya Tere liye namaz ki taraf chal kar jaaye.",
            "اے اللہ! اپنے بندے کو شفا دے تاکہ وہ تیرے لیے دشمن کا مقابلہ کرے یا تیرے لیے نماز کی طرف چل کر جائے",
            "Nabi ﷺ ne mareez ke liye yeh kehna sikhaya.",
            "Sunan Abi Dawud", "Hadith 3107", "Hasan"
        ),

        // ---------------- Prayer & Mosque ----------------
        d(
            "dua_la_mania", "Prayer & Mosque", "None Can Withhold What You Give",
            "Farz namaz ke salaam ke baad.",
            "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ، اللَّهُمَّ لَا مَانِعَ لِمَا أَعْطَيْتَ، وَلَا مُعْطِيَ لِمَا مَنَعْتَ، وَلَا يَنْفَعُ ذَا الْجَدِّ مِنْكَ الْجَدُّ",
            "Lā ilāha illallāhu waḥdahū lā sharīka lah, lahul-mulku wa lahul-ḥamdu wa Huwa 'alā kulli shay'in qadīr. Allāhumma lā māni'a limā a'ṭayt, wa lā mu'ṭiya limā mana't, wa lā yanfa'u dhal-jaddi minkal-jadd",
            "Allah ke siwa koi mabood nahi, woh akela hai, uska koi shareek nahi, usi ki baadshahat hai aur usi ki tareef hai aur woh har cheez par qaadir hai. Ya Allah, jo Tu de use koi rokne wala nahi aur jo Tu roke use koi dene wala nahi, aur kisi maaldaar ko uski maaldaari Tere muqaable mein faida nahi deti.",
            "اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے، اس کا کوئی شریک نہیں، اسی کی بادشاہت ہے اور اسی کی تعریف ہے اور وہ ہر چیز پر قادر ہے۔ اے اللہ! جو تو دے اسے کوئی روکنے والا نہیں اور جو تو روکے اسے کوئی دینے والا نہیں، اور کسی مالدار کو اس کی مالداری تیرے مقابلے میں فائدہ نہیں دیتی",
            "Nabi ﷺ har farz namaz ke baad yeh farmate.",
            "Sahih al-Bukhari", "Hadith 844; Sahih Muslim 593", "Sahih"
        ),

        // ---------------- Daily Life & Manners ----------------
        d(
            "dua_slaughtering", "Daily Life & Manners", "When Slaughtering an Animal",
            "Qurbani ya zabah karte waqt.",
            "بِسْمِ اللَّهِ وَاللَّهُ أَكْبَرُ",
            "Bismillāhi wallāhu akbar",
            "Allah ke naam se, aur Allah sab se bara hai.",
            "اللہ کے نام سے، اور اللہ سب سے بڑا ہے",
            "Nabi ﷺ ne qurbani ke waqt Bismillah aur takbeer kahi.",
            "Sahih Muslim", "Hadith 1966; Sahih al-Bukhari 5565", "Sahih"
        ),
        d(
            "dua_undressing", "Daily Life & Manners", "When Taking Off Clothes",
            "Kapde utaarte waqt (ghusl ya kapde badalte waqt).",
            "بِسْمِ اللَّهِ",
            "Bismillāh",
            "Allah ke naam se.",
            "اللہ کے نام سے",
            "Jinnaat ki nazar aur insaan ke satar ke darmiyaan parda ban jaata hai.",
            "Jami` at-Tirmidhi", "Hadith 606", "Sahih"
        ),
        d(
            "dua_ilbas_jadidan", "Daily Life & Manners", "Seeing Someone in New Clothes",
            "Kisi Muslim bhai ko naye kapde pehne dekh kar.",
            "الْبَسْ جَدِيدًا، وَعِشْ حَمِيدًا، وَمُتْ شَهِيدًا",
            "Ilbas jadīdā, wa 'ish ḥamīdā, wa mut shahīdā",
            "Naya pehno, qaabil-e-tareef zindagi guzaaro aur shaheed ho kar maro.",
            "نیا پہنو، قابلِ تعریف زندگی گزارو اور شہید ہو کر مرو",
            "Nabi ﷺ ne Hazrat Umar (RA) ko naye kapdon mein dekh kar yeh dua di.",
            "Sunan Ibn Majah", "Hadith 3558", "Sahih"
        ),

        // ---------------- Hajj & Umrah ----------------
        d(
            "dua_hajj_la_riya", "Hajj & Umrah", "A Hajj Free of Showing Off",
            "Hajj ki niyyat ke waqt ikhlaas ke liye.",
            "اللَّهُمَّ حَجَّةٌ لَا رِيَاءَ فِيهَا وَلَا سُمْعَةَ",
            "Allāhumma ḥajjatun lā riyā'a fīhā wa lā sum'ah",
            "Ya Allah, aisa Hajj jis mein na dikhaawa ho na shohrat talabi.",
            "اے اللہ! ایسا حج جس میں نہ دکھاوا ہو نہ شہرت طلبی",
            "Nabi ﷺ ne Hajj ke waqt yeh dua farmayi.",
            "Sunan Ibn Majah", "Hadith 2890", "Sahih"
        ),

        // ---------------- Quranic Duas ----------------
        d(
            "dua_q_muslimayni_laka", "Quranic Duas", "Dua of Ibrahim & Ismail (AS) — Make Us Submissive",
            "Apne aur apni aulaad ke liye farmaanbardaari ki dua.",
            "رَبَّنَا وَاجْعَلْنَا مُسْلِمَيْنِ لَكَ وَمِنْ ذُرِّيَّتِنَا أُمَّةً مُسْلِمَةً لَكَ وَأَرِنَا مَنَاسِكَنَا وَتُبْ عَلَيْنَا ۖ إِنَّكَ أَنْتَ التَّوَّابُ الرَّحِيمُ",
            "Rabbanā waj'alnā muslimayni laka wa min dhurriyyatinā ummatan muslimatan lak, wa arinā manāsikanā wa tub 'alaynā, innaka Antat-Tawwābur-Raḥīm",
            "Aye hamare Rab, hum dono ko apna farmaanbardaar bana aur hamari aulaad mein se bhi ek ummat ko apna farmaanbardaar bana, hamein hamare ibaadat ke tareeqe dikha aur hamari tauba qubool farma, beshak Tu hi bahut tauba qubool karne wala, nihaayat rahm wala hai.",
            "اے ہمارے رب! ہم دونوں کو اپنا فرمانبردار بنا اور ہماری اولاد میں سے بھی ایک امت کو اپنا فرمانبردار بنا، ہمیں ہماری عبادت کے طریقے دکھا اور ہماری توبہ قبول فرما، بے شک تو ہی بہت توبہ قبول کرنے والا، نہایت رحم والا ہے",
            "Kaaba ki taameer ke waqt ki dua.",
            QURAN, "Surah Al-Baqarah (2:128)", MUTAWATIR
        ),
        d(
            "dua_q_iftah_baynana", "Quranic Duas", "Dua of Shu'aib (AS) — Judge Between Us",
            "Haq aur baatil ke darmiyaan faisle ke liye.",
            "رَبَّنَا افْتَحْ بَيْنَنَا وَبَيْنَ قَوْمِنَا بِالْحَقِّ وَأَنْتَ خَيْرُ الْفَاتِحِينَ",
            "Rabbanaftaḥ baynanā wa bayna qawminā bil-ḥaqqi wa Anta khayrul-fātiḥīn",
            "Aye hamare Rab, hamare aur hamari qaum ke darmiyaan haq ke saath faisla kar de, aur Tu sab se behtar faisla karne wala hai.",
            "اے ہمارے رب! ہمارے اور ہماری قوم کے درمیان حق کے ساتھ فیصلہ کر دے، اور تو سب سے بہتر فیصلہ کرنے والا ہے",
            "Hazrat Shu'aib (AS) ki dua jab qaum ne inkaar kiya.",
            QURAN, "Surah Al-A'raf (7:89)", MUTAWATIR
        ),
        d(
            "dua_q_la_akuna_zahiran", "Quranic Duas", "Dua of Musa (AS) — Never a Helper of Criminals",
            "Allah ki ne'maton ka shukr aur gunahgaaron ki madad se bachne ka ahd.",
            "رَبِّ بِمَا أَنْعَمْتَ عَلَيَّ فَلَنْ أَكُونَ ظَهِيرًا لِلْمُجْرِمِينَ",
            "Rabbi bimā an'amta 'alayya falan akūna ẓahīral-lil-mujrimīn",
            "Aye mere Rab, jaisa Tu ne mujh par inaam kiya, main kabhi mujrimon ka madadgaar nahi banunga.",
            "اے میرے رب! جیسا تو نے مجھ پر انعام کیا، میں کبھی مجرموں کا مددگار نہیں بنوں گا",
            "Tauba ke baad Hazrat Musa (AS) ka ahd.",
            QURAN, "Surah Al-Qasas (28:17)", MUTAWATIR
        ),
        d(
            "dua_q_nuh_unsurni", "Quranic Duas", "Dua of Nuh (AS) — Help Me, They Deny Me",
            "Jab log jhutlaayein aur haq ko na maanein.",
            "رَبِّ انْصُرْنِي بِمَا كَذَّبُونِ",
            "Rabbinṣurnī bimā kadhdhabūn",
            "Aye mere Rab, meri madad farma kyunki unhon ne mujhe jhutla diya.",
            "اے میرے رب! میری مدد فرما کیونکہ انہوں نے مجھے جھٹلا دیا",
            "Hazrat Nuh (AS) ki dua jo qubool hui.",
            QURAN, "Surah Al-Mu'minun (23:26)", MUTAWATIR
        ),
        d(
            "dua_q_ihkum_bil_haqq", "Quranic Duas", "My Lord, Judge With Truth",
            "Jhooti baaton aur ilzaamaat ke muqaable mein.",
            "رَبِّ احْكُمْ بِالْحَقِّ ۗ وَرَبُّنَا الرَّحْمَٰنُ الْمُسْتَعَانُ عَلَىٰ مَا تَصِفُونَ",
            "Rabbiḥkum bil-ḥaqq, wa Rabbunar-Raḥmānul-musta'ānu 'alā mā taṣifūn",
            "Aye mere Rab, haq ke saath faisla farma. Aur hamara Rab Rehmaan hai, jis se un baaton par madad maangi jaati hai jo tum banaate ho.",
            "اے میرے رب! حق کے ساتھ فیصلہ فرما، اور ہمارا رب رحمٰن ہے جس سے ان باتوں پر مدد مانگی جاتی ہے جو تم بناتے ہو",
            "Nabi ﷺ ko sikhayi gayi dua.",
            QURAN, "Surah Al-Anbiya (21:112)", MUTAWATIR
        ),

        // ---------------- Protection & Healing ----------------
        d(
            "dua_ghalabatid_dayn", "Protection & Healing", "Refuge From Crushing Debt and Enemies",
            "Qarz ke bojh aur dushmanon ke ghalbe se hifaazat.",
            "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ غَلَبَةِ الدَّيْنِ، وَغَلَبَةِ الْعَدُوِّ، وَشَمَاتَةِ الْأَعْدَاءِ",
            "Allāhumma innī a'ūdhu bika min ghalabatid-dayn, wa ghalabatil-'aduww, wa shamātatil-a'dā'",
            "Ya Allah, main qarz ke ghalbe, dushman ke ghalbe aur dushmanon ke khush hone se Teri panaah maangta hoon.",
            "اے اللہ! میں قرض کے غلبے، دشمن کے غلبے اور دشمنوں کے خوش ہونے سے تیری پناہ مانگتا ہوں",
            "Nabi ﷺ in cheezon se panaah maanga karte the.",
            "Sunan an-Nasa'i", "Hadith 5475", "Sahih"
        ),
        d(
            "dua_faqr_qillah_dhillah", "Protection & Healing", "Refuge From Poverty and Humiliation",
            "Tangdasti, kami aur zillat se bachne ke liye.",
            "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْفَقْرِ وَالْقِلَّةِ وَالذِّلَّةِ، وَأَعُوذُ بِكَ مِنْ أَنْ أَظْلِمَ أَوْ أُظْلَمَ",
            "Allāhumma innī a'ūdhu bika minal-faqri wal-qillati wadh-dhillah, wa a'ūdhu bika min an aẓlima aw uẓlam",
            "Ya Allah, main faqr, kami aur zillat se Teri panaah maangta hoon, aur is baat se ke main zulm karun ya mujh par zulm kiya jaaye.",
            "اے اللہ! میں فقر، کمی اور ذلت سے تیری پناہ مانگتا ہوں، اور اس بات سے کہ میں ظلم کروں یا مجھ پر ظلم کیا جائے",
            "Nabi ﷺ ki sikhayi hui panaah.",
            "Sunan Abi Dawud", "Hadith 1544; Sunan an-Nasa'i 5460", "Sahih"
        ),

        // ---------------- Comprehensive Duas ----------------
        d(
            "dua_five_things_new_muslim", "Comprehensive Duas", "Forgive, Have Mercy, Guide, Heal, Provide",
            "Nabi ﷺ naye Musalmaan ko namaz ke saath yeh dua sikhate.",
            "اللَّهُمَّ اغْفِرْ لِي، وَارْحَمْنِي، وَاهْدِنِي، وَعَافِنِي، وَارْزُقْنِي",
            "Allāhummaghfir lī, warḥamnī, wahdinī, wa 'āfinī, warzuqnī",
            "Ya Allah, mujhe bakhsh de, mujh par rahm farma, mujhe hidaayat de, mujhe aafiyat de aur mujhe rizq de.",
            "اے اللہ! مجھے بخش دے، مجھ پر رحم فرما، مجھے ہدایت دے، مجھے عافیت دے اور مجھے رزق دے",
            "Nabi ﷺ ne farmaya: yeh kalimaat tumhare liye dunya aur aakhirat jama kar dete hain.",
            "Sahih Muslim", "Hadith 2697", "Sahih"
        ),

        // ---------------- Prayer & Mosque ----------------
        d(
            "dua_ruku_laka_raka_tu", "Prayer & Mosque", "In Ruku — To You I Bowed",
            "Ruku mein (khaas taur par nafl namaz mein).",
            "اللَّهُمَّ لَكَ رَكَعْتُ، وَبِكَ آمَنْتُ، وَلَكَ أَسْلَمْتُ، خَشَعَ لَكَ سَمْعِي وَبَصَرِي وَمُخِّي وَعَظْمِي وَعَصَبِي",
            "Allāhumma laka raka'tu, wa bika āmantu, wa laka aslamtu, khasha'a laka sam'ī wa baṣarī wa mukhkhī wa 'aẓmī wa 'aṣabī",
            "Ya Allah, main ne Tere liye ruku kiya, Tujh par imaan laaya aur Tera farmaanbardaar hua. Mere kaan, aankhein, dimaagh, haddiyaan aur patthe Tere aage jhuk gaye.",
            "اے اللہ! میں نے تیرے لیے رکوع کیا، تجھ پر ایمان لایا اور تیرا فرمانبردار ہوا، میرے کان، آنکھیں، دماغ، ہڈیاں اور پٹھے تیرے آگے جھک گئے",
            "Nabi ﷺ ruku mein yeh parhte.",
            "Sahih Muslim", "Hadith 771", "Sahih"
        ),
        d(
            "dua_sujood_laka_sajadtu", "Prayer & Mosque", "In Sujood — To You I Prostrated",
            "Sajde mein (khaas taur par nafl namaz mein).",
            "اللَّهُمَّ لَكَ سَجَدْتُ، وَبِكَ آمَنْتُ، وَلَكَ أَسْلَمْتُ، سَجَدَ وَجْهِي لِلَّذِي خَلَقَهُ وَصَوَّرَهُ، وَشَقَّ سَمْعَهُ وَبَصَرَهُ، تَبَارَكَ اللَّهُ أَحْسَنُ الْخَالِقِينَ",
            "Allāhumma laka sajadtu, wa bika āmantu, wa laka aslamtu, sajada wajhī lilladhī khalaqahū wa ṣawwarah, wa shaqqa sam'ahū wa baṣarah, tabārakallāhu aḥsanul-khāliqīn",
            "Ya Allah, main ne Tujhe sajda kiya, Tujh par imaan laaya aur Tera farmaanbardaar hua. Mera chehra us zaat ke aage sajde mein hai jis ne use banaya, uski soorat banayi aur uske kaan aur aankhein kholin. Barkat wala hai Allah, sab se behtar banane wala.",
            "اے اللہ! میں نے تجھے سجدہ کیا، تجھ پر ایمان لایا اور تیرا فرمانبردار ہوا، میرا چہرہ اس ذات کے آگے سجدے میں ہے جس نے اسے پیدا کیا، اس کی صورت بنائی اور اس کے کان اور آنکھیں کھولیں، بڑی برکت والا ہے اللہ جو سب سے بہتر پیدا کرنے والا ہے",
            "Nabi ﷺ sajde mein yeh parhte.",
            "Sahih Muslim", "Hadith 771", "Sahih"
        ),
        d(
            "dua_ruku_sujood_subhanaka", "Prayer & Mosque", "Glory Be to You, Our Lord — Forgive Me",
            "Ruku aur sajde mein.",
            "سُبْحَانَكَ اللَّهُمَّ رَبَّنَا وَبِحَمْدِكَ، اللَّهُمَّ اغْفِرْ لِي",
            "Subḥānakallāhumma Rabbanā wa biḥamdik, Allāhummaghfir lī",
            "Ya Allah, hamare Rab, Tu paak hai apni tareef ke saath. Ya Allah, mujhe bakhsh de.",
            "اے اللہ! ہمارے رب، تو پاک ہے اپنی تعریف کے ساتھ، اے اللہ! مجھے بخش دے",
            "Hazrat Aisha (RA): Nabi ﷺ ruku aur sujood mein kasrat se yeh parhte.",
            "Sahih al-Bukhari", "Hadith 817; Sahih Muslim 484", "Sahih"
        ),

        // ---------------- Quranic Duas ----------------
        d(
            "dua_q_musa_li_akhi", "Quranic Duas", "Dua of Musa (AS) — Forgive Me and My Brother",
            "Apne aur apne bhai/azeezon ke liye maghfirat.",
            "رَبِّ اغْفِرْ لِي وَلِأَخِي وَأَدْخِلْنَا فِي رَحْمَتِكَ ۖ وَأَنْتَ أَرْحَمُ الرَّاحِمِينَ",
            "Rabbighfir lī wa li-akhī wa adkhilnā fī raḥmatik, wa Anta arḥamur-rāḥimīn",
            "Aye mere Rab, mujhe aur mere bhai ko bakhsh de aur hamein apni rehmat mein daakhil farma, aur Tu sab rahm karne walon se badh kar rahm karne wala hai.",
            "اے میرے رب! مجھے اور میرے بھائی کو بخش دے اور ہمیں اپنی رحمت میں داخل فرما، اور تو سب رحم کرنے والوں سے بڑھ کر رحم کرنے والا ہے",
            "Hazrat Musa (AS) ki Haroon (AS) ke liye dua.",
            QURAN, "Surah Al-A'raf (7:151)", MUTAWATIR
        ),
        d(
            "dua_q_ufawwidu_amri", "Quranic Duas", "I Entrust My Affair to Allah",
            "Jab log mukhaalifat karein aur maamla Allah ke supurd karna ho.",
            "وَأُفَوِّضُ أَمْرِي إِلَى اللَّهِ ۚ إِنَّ اللَّهَ بَصِيرٌ بِالْعِبَادِ",
            "Wa ufawwiḍu amrī ilallāh, innallāha baṣīrum bil-'ibād",
            "Aur main apna maamla Allah ke supurd karta hoon, beshak Allah bandon ko khoob dekhne wala hai.",
            "اور میں اپنا معاملہ اللہ کے سپرد کرتا ہوں، بے شک اللہ بندوں کو خوب دیکھنے والا ہے",
            "Aal-e-Firaun ke momin ka qaul; is ke baad Allah ne use unki buri chaalon se bacha liya.",
            QURAN, "Surah Ghafir (40:44)", MUTAWATIR
        ),
        d(
            "dua_q_hawariyyun", "Quranic Duas", "Dua of the Disciples — Record Us With the Witnesses",
            "Imaan aur ittiba-e-Rasool par saabit qadmi ke liye.",
            "رَبَّنَا آمَنَّا بِمَا أَنْزَلْتَ وَاتَّبَعْنَا الرَّسُولَ فَاكْتُبْنَا مَعَ الشَّاهِدِينَ",
            "Rabbanā āmannā bimā anzalta wattaba'nar-rasūla faktubnā ma'ash-shāhidīn",
            "Aye hamare Rab, jo Tu ne utaara hum us par imaan laaye aur hum ne Rasool ki pairwi ki, pas hamein gawaahi dene walon mein likh le.",
            "اے ہمارے رب! جو تو نے اتارا ہم اس پر ایمان لائے اور ہم نے رسول کی پیروی کی، پس ہمیں گواہی دینے والوں میں لکھ لے",
            "Hazrat Isa (AS) ke hawaariyon ki dua.",
            QURAN, "Surah Aal-e-Imran (3:53)", MUTAWATIR
        ),
        d(
            "dua_q_la_tukhzini", "Quranic Duas", "Dua of Ibrahim (AS) — Do Not Disgrace Me",
            "Qayamat ki ruswaai se bachne ke liye.",
            "وَلَا تُخْزِنِي يَوْمَ يُبْعَثُونَ",
            "Wa lā tukhzinī yawma yub'athūn",
            "Aur mujhe us din ruswa na karna jis din log uthaaye jaayenge.",
            "اور مجھے اس دن رسوا نہ کرنا جس دن لوگ اٹھائے جائیں گے",
            "Hazrat Ibrahim (AS) ki aakhirat ke liye dua.",
            QURAN, "Surah Ash-Shu'ara (26:87)", MUTAWATIR
        ),

        // ---------------- Ramadan & Fasting ----------------
        d(
            "dua_iftar_laka_sumtu", "Ramadan & Fasting", "Iftar — For You I Fasted",
            "Roza kholte waqt (iftar ki mash-hoor dua).",
            "اللَّهُمَّ لَكَ صُمْتُ، وَعَلَىٰ رِزْقِكَ أَفْطَرْتُ",
            "Allāhumma laka ṣumtu, wa 'alā rizqika afṭart",
            "Ya Allah, main ne Tere liye roza rakha aur Tere hi rizq se iftar kiya.",
            "اے اللہ! میں نے تیرے لیے روزہ رکھا اور تیرے ہی رزق سے افطار کیا",
            "Nabi ﷺ iftar ke waqt yeh farmate. Mursal riwayat hai jise doosri riwayaat se taqwiyat milti hai; Ahnaaf aur jamhoor ke haan iftar par iska parhna ma'mool hai.",
            "Sunan Abi Dawud", "Hadith 2358 (Mursal, shawahid ke saath)", "Hasan li-ghairihi"
        ),
        d(
            "dua_iftar_ibn_umar", "Ramadan & Fasting", "Iftar — By Your Vast Mercy, Forgive Me",
            "Iftar ke waqt — Hazrat Abdullah bin Amr (RA) yeh dua karte; roze daar ki dua iftar par radd nahi hoti.",
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ بِرَحْمَتِكَ الَّتِي وَسِعَتْ كُلَّ شَيْءٍ أَنْ تَغْفِرَ لِي",
            "Allāhumma innī as'aluka biraḥmatikal-latī wasi'at kulla shay'in an taghfira lī",
            "Ya Allah, main Teri us rehmat ke waseele se jo har cheez par chhaayi hui hai, Tujh se sawaal karta hoon ke mujhe bakhsh de.",
            "اے اللہ! میں تیری اس رحمت کے وسیلے سے جو ہر چیز پر چھائی ہوئی ہے، تجھ سے سوال کرتا ہوں کہ مجھے بخش دے",
            "Hadith: iftar ke waqt roze daar ki dua radd nahi ki jaati.",
            "Sunan Ibn Majah", "Hadith 1753", "Hasan (Hafiz Ibn Hajar)"
        ),
        d(
            "dua_inni_saim", "Ramadan & Fasting", "When Provoked While Fasting",
            "Roze ki haalat mein koi gaali de ya jhagda kare.",
            "إِنِّي صَائِمٌ، إِنِّي صَائِمٌ",
            "Innī ṣā'im, innī ṣā'im",
            "Main roze se hoon, main roze se hoon.",
            "میں روزے سے ہوں، میں روزے سے ہوں",
            "Nabi ﷺ ne farmaya: roza dhaal hai, pas roze daar na fahash baat kare na jahaalat — koi jhagde to do baar yeh kahe.",
            "Sahih al-Bukhari", "Hadith 1894, 1904; Sahih Muslim 1151", "Sahih"
        ),
        d(
            "dua_eid_takbeer", "Ramadan & Fasting", "Takbeer on the Way to Eid",
            "Chaand raat se Eid ul Fitr ki namaz tak, khaas taur par Eidgah jaate hue.",
            "اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، لَا إِلَٰهَ إِلَّا اللَّهُ، وَاللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، وَلِلَّهِ الْحَمْدُ",
            "Allāhu akbar, Allāhu akbar, lā ilāha illallāh, wallāhu akbar, Allāhu akbar, wa lillāhil-ḥamd",
            "Allah sab se bara hai, Allah sab se bara hai, Allah ke siwa koi mabood nahi, aur Allah sab se bara hai, Allah sab se bara hai, aur tamaam tareef Allah hi ke liye hai.",
            "اللہ سب سے بڑا ہے، اللہ سب سے بڑا ہے، اللہ کے سوا کوئی معبود نہیں، اور اللہ سب سے بڑا ہے، اللہ سب سے بڑا ہے، اور تمام تعریف اللہ ہی کے لیے ہے",
            "Quran ka hukm: 'taake tum ginti poori karo aur Allah ki badaai bayan karo' (Al-Baqarah 2:185). Hazrat Ibn Mas'ud (RA) yahi takbeer kehte.",
            "Musannaf Ibn Abi Shaybah", "5633, 5650; Surah Al-Baqarah (2:185)", "Mauqoof (Sahih isnad)"
        ),

        // ---------------- Comprehensive Duas ----------------
        d(
            "dua_ahsin_aqibatana", "Comprehensive Duas", "Make Our End Good in All Affairs",
            "Har kaam ke achhe anjaam ke liye.",
            "اللَّهُمَّ أَحْسِنْ عَاقِبَتَنَا فِي الْأُمُورِ كُلِّهَا، وَأَجِرْنَا مِنْ خِزْيِ الدُّنْيَا وَعَذَابِ الْآخِرَةِ",
            "Allāhumma aḥsin 'āqibatanā fil-umūri kullihā, wa ajirnā min khizyid-dunyā wa 'adhābil-ākhirah",
            "Ya Allah, tamaam kaamon mein hamara anjaam achha farma, aur hamein dunya ki ruswaai aur aakhirat ke azaab se bacha le.",
            "اے اللہ! تمام کاموں میں ہمارا انجام اچھا فرما، اور ہمیں دنیا کی رسوائی اور آخرت کے عذاب سے بچا لے",
            "Jo yeh dua kare woh aazmaaish se pehle wafaat pa jaaye to (fitne se mehfooz) mara.",
            "Musnad Ahmad", "Hadith 17628; Sahih Ibn Hibban 949", "Hasan"
        ),
        d(
            "dua_fadlika_wa_rahmatik", "Comprehensive Duas", "I Ask of Your Bounty and Mercy",
            "Rizq aur rehmat talab karte waqt.",
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ وَرَحْمَتِكَ، فَإِنَّهُ لَا يَمْلِكُهَا إِلَّا أَنْتَ",
            "Allāhumma innī as'aluka min faḍlika wa raḥmatik, fa innahū lā yamlikuhā illā Ant",
            "Ya Allah, main Tujh se Tera fazl aur Teri rehmat maangta hoon, kyunki inka maalik Tere siwa koi nahi.",
            "اے اللہ! میں تجھ سے تیرا فضل اور تیری رحمت مانگتا ہوں، کیونکہ ان کا مالک تیرے سوا کوئی نہیں",
            "Nabi ﷺ ke paas mehmaan aaye aur khaana na tha to aap ne yeh dua ki, phir hadiya aa gaya.",
            "Al-Mu'jam al-Kabir (at-Tabarani)", "Hadith 10379", "Sahih"
        ),
        d(
            "dua_allahu_akbar_kabira_2696", "Comprehensive Duas", "Words Taught to a Bedouin",
            "Zikr aur dua ka jaami' kalima — ek dehaati ko sikhaya gaya.",
            "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، اللَّهُ أَكْبَرُ كَبِيرًا، وَالْحَمْدُ لِلَّهِ كَثِيرًا، سُبْحَانَ اللَّهِ رَبِّ الْعَالَمِينَ، لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ الْعَزِيزِ الْحَكِيمِ",
            "Lā ilāha illallāhu waḥdahū lā sharīka lah, Allāhu akbaru kabīrā, wal-ḥamdu lillāhi kathīrā, subḥānallāhi Rabbil-'ālamīn, lā ḥawla wa lā quwwata illā billāhil-'Azīzil-Ḥakīm",
            "Allah ke siwa koi mabood nahi, woh akela hai, uska koi shareek nahi. Allah sab se bahut bara hai, aur Allah ki bahut zyada tareef hai, paak hai Allah jo tamaam jahaanon ka Rab hai, aur gunaah se bachne aur neki karne ki taaqat sirf Allah ghaalib, hikmat wale ki taraf se hai.",
            "اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے، اس کا کوئی شریک نہیں، اللہ سب سے بہت بڑا ہے، اور اللہ کی بہت زیادہ تعریف ہے، پاک ہے اللہ جو تمام جہانوں کا رب ہے، اور گناہ سے بچنے اور نیکی کرنے کی طاقت صرف اللہ غالب، حکمت والے کی طرف سے ہے",
            "Is ke baad 'Allahummaghfir li warhamni wahdini warzuqni' kehne ki talqeen farmayi.",
            "Sahih Muslim", "Hadith 2696", "Sahih"
        ),

        // ---------------- Forgiveness & Istighfar ----------------
        d(
            "dua_ma_qaddamtu_akhkhartu", "Forgiveness & Istighfar", "Forgive What I Sent Before and After",
            "Tashahhud aur durood ke baad salaam se pehle.",
            "اللَّهُمَّ اغْفِرْ لِي مَا قَدَّمْتُ وَمَا أَخَّرْتُ، وَمَا أَسْرَرْتُ وَمَا أَعْلَنْتُ، وَمَا أَسْرَفْتُ، وَمَا أَنْتَ أَعْلَمُ بِهِ مِنِّي، أَنْتَ الْمُقَدِّمُ وَأَنْتَ الْمُؤَخِّرُ، لَا إِلَٰهَ إِلَّا أَنْتَ",
            "Allāhummaghfir lī mā qaddamtu wa mā akhkhart, wa mā asrartu wa mā a'lant, wa mā asraft, wa mā Anta a'lamu bihī minnī, Antal-Muqaddimu wa Antal-Mu'akhkhir, lā ilāha illā Ant",
            "Ya Allah, mere woh gunaah bakhsh de jo main ne pehle kiye aur jo baad mein kiye, jo chhup kar kiye aur jo khule aam kiye, jo zyaadti ki, aur jinhein Tu mujh se zyada jaanta hai. Tu hi aage karne wala aur Tu hi peeche karne wala hai, Tere siwa koi mabood nahi.",
            "اے اللہ! میرے وہ گناہ بخش دے جو میں نے پہلے کیے اور جو بعد میں کیے، جو چھپ کر کیے اور جو کھلے عام کیے، جو زیادتی کی، اور جنہیں تو مجھ سے زیادہ جانتا ہے، تو ہی آگے کرنے والا اور تو ہی پیچھے کرنے والا ہے، تیرے سوا کوئی معبود نہیں",
            "Nabi ﷺ namaz ke aakhir mein tashahhud aur salaam ke darmiyaan yeh parhte.",
            "Sahih Muslim", "Hadith 771", "Sahih"
        ),
        d(
            "dua_jiddi_wa_hazli", "Forgiveness & Istighfar", "Forgive My Seriousness and My Jest",
            "Jaane anjaane har tarah ke gunaahon ki maafi.",
            "اللَّهُمَّ اغْفِرْ لِي جِدِّي وَهَزْلِي، وَخَطَئِي وَعَمْدِي، وَكُلُّ ذَٰلِكَ عِنْدِي",
            "Allāhummaghfir lī jiddī wa hazlī, wa khaṭa'ī wa 'amdī, wa kullu dhālika 'indī",
            "Ya Allah, mere woh gunaah bakhsh de jo sanjeedgi mein hue aur jo mazaaq mein, jo ghalti se hue aur jo jaan boojh kar, aur yeh sab mujh mein hain.",
            "اے اللہ! میرے وہ گناہ بخش دے جو سنجیدگی میں ہوئے اور جو مذاق میں، جو غلطی سے ہوئے اور جو جان بوجھ کر، اور یہ سب مجھ میں ہیں",
            "Nabi ﷺ ki jaami' istighfar.",
            "Sahih al-Bukhari", "Hadith 6399; Sahih Muslim 2719", "Sahih"
        ),

        // ---------------- Prayer & Mosque ----------------
        d(
            "dua_rising_ruku_mil", "Prayer & Mosque", "After Ruku — Praise Filling the Heavens",
            "Ruku se uth kar 'Rabbana lakal hamd' ke saath.",
            "رَبَّنَا لَكَ الْحَمْدُ، مِلْءَ السَّمَاوَاتِ وَمِلْءَ الْأَرْضِ، وَمِلْءَ مَا شِئْتَ مِنْ شَيْءٍ بَعْدُ",
            "Rabbanā lakal-ḥamd, mil'as-samāwāti wa mil'al-arḍ, wa mil'a mā shi'ta min shay'in ba'd",
            "Aye hamare Rab, Tere hi liye tamaam tareef hai, aasmaanon bhar, zameen bhar, aur uske baad jo cheez Tu chahe us bhar.",
            "اے ہمارے رب! تیرے ہی لیے تمام تعریف ہے، آسمانوں بھر، زمین بھر، اور اس کے بعد جو چیز تو چاہے اس بھر",
            "Nabi ﷺ ruku se uth kar yeh farmate.",
            "Sahih Muslim", "Hadith 476", "Sahih"
        ),
        d(
            "dua_istiftah_kabira", "Prayer & Mosque", "Allah Is Greatest, Abundantly Praised",
            "Namaz shuru karte waqt (sana ki ek soorat).",
            "اللَّهُ أَكْبَرُ كَبِيرًا، وَالْحَمْدُ لِلَّهِ كَثِيرًا، وَسُبْحَانَ اللَّهِ بُكْرَةً وَأَصِيلًا",
            "Allāhu akbaru kabīrā, wal-ḥamdu lillāhi kathīrā, wa subḥānallāhi bukratan wa aṣīlā",
            "Allah sab se bahut bara hai, Allah ki bahut zyada tareef hai, aur subah o shaam Allah ki paaki hai.",
            "اللہ سب سے بہت بڑا ہے، اللہ کی بہت زیادہ تعریف ہے، اور صبح و شام اللہ کی پاکی ہے",
            "Nabi ﷺ ne farmaya: is ke liye aasmaan ke darwaaze khol diye gaye.",
            "Sahih Muslim", "Hadith 601", "Sahih"
        ),

        // ---------------- Anxiety & Distress ----------------
        d(
            "dua_q_ashku_bathhi", "Anxiety & Distress", "Dua of Yaqub (AS) — I Complain Only to Allah",
            "Gehre gham aur judaai ke waqt.",
            "إِنَّمَا أَشْكُو بَثِّي وَحُزْنِي إِلَى اللَّهِ",
            "Innamā ashkū baththī wa ḥuznī ilallāh",
            "Main apni pareshani aur gham ki shikaayat sirf Allah se karta hoon.",
            "میں اپنی پریشانی اور غم کی شکایت صرف اللہ سے کرتا ہوں",
            "Hazrat Yaqub (AS) ne Yusuf (AS) ki judaai mein yeh kaha.",
            QURAN, "Surah Yusuf (12:86)", MUTAWATIR
        ),

        // ---------------- Protection & Healing ----------------
        d(
            "dua_q_khayrun_hafiza", "Protection & Healing", "Allah Is the Best Protector",
            "Bachchon ya azeezon ko safar par bhejte waqt.",
            "فَاللَّهُ خَيْرٌ حَافِظًا ۖ وَهُوَ أَرْحَمُ الرَّاحِمِينَ",
            "Fallāhu khayrun ḥāfiẓā, wa Huwa arḥamur-rāḥimīn",
            "Pas Allah hi behtareen hifaazat karne wala hai, aur woh sab rahm karne walon se badh kar rahm karne wala hai.",
            "پس اللہ ہی بہترین حفاظت کرنے والا ہے، اور وہ سب رحم کرنے والوں سے بڑھ کر رحم کرنے والا ہے",
            "Hazrat Yaqub (AS) ne apne bete ko Allah ke supurd karte hue kaha.",
            QURAN, "Surah Yusuf (12:64)", MUTAWATIR
        ),

        // ---------------- Quranic Duas ----------------
        d(
            "dua_q_aqraba_rashada", "Quranic Duas", "May My Lord Guide Me Closer to Right",
            "Kisi kaam mein behtar raah ki talaash ho.",
            "عَسَىٰ أَنْ يَهْدِيَنِ رَبِّي لِأَقْرَبَ مِنْ هَٰذَا رَشَدًا",
            "'Asā an yahdiyani Rabbī li-aqraba min hādhā rashadā",
            "Umeed hai ke mera Rab mujhe is se bhi zyada bhalaai ke qareeb raasta dikha de.",
            "امید ہے کہ میرا رب مجھے اس سے بھی زیادہ بھلائی کے قریب راستہ دکھا دے",
            "Nabi ﷺ ko bhool jaane par yeh kehne ki talqeen hui.",
            QURAN, "Surah Al-Kahf (18:24)", MUTAWATIR
        ),
        d(
            "dua_q_sawa_as_sabil", "Quranic Duas", "Dua of Musa (AS) — Guide Me to the Right Way",
            "Naye safar ya anjaan raaste par chalte waqt.",
            "عَسَىٰ رَبِّي أَنْ يَهْدِيَنِي سَوَاءَ السَّبِيلِ",
            "'Asā Rabbī an yahdiyanī sawā'as-sabīl",
            "Umeed hai ke mera Rab mujhe seedha raasta dikhayega.",
            "امید ہے کہ میرا رب مجھے سیدھا راستہ دکھائے گا",
            "Hazrat Musa (AS) ne Madyan ki taraf rukh karte hue kaha.",
            QURAN, "Surah Al-Qasas (28:22)", MUTAWATIR
        )
    )
}
