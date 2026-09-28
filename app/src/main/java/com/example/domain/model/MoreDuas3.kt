package com.example.domain.model

// Third batch of authentic duas (Ahle Sunnat wal Jamaat sources, with references).
internal object MoreDuas3 {

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

    private const val QURAN = "The Noble Quran"
    private const val MUTAWATIR = "Mutawatir (Quranic Verse)"

    val LIST = listOf(
        // ---------------- Quranic Duas ----------------
        d(
            "dua_q_ma_khalaqta_batila", "Quranic Duas", "Reflecting on Creation",
            "Aasmaan o zameen ki takhleeq par ghaur karne walon ki dua.",
            "رَبَّنَا مَا خَلَقْتَ هَٰذَا بَاطِلًا سُبْحَانَكَ فَقِنَا عَذَابَ النَّارِ",
            "Rabbanā mā khalaqta hādhā bāṭilā, subḥānaka faqinā 'adhāban-nār",
            "Aye hamare Rab, Tune yeh sab bekaar paida nahi kiya, Tu paak hai, pas humein aag ke azaab se bacha.",
            "اے ہمارے رب! تو نے یہ سب بے کار پیدا نہیں کیا، تو پاک ہے، پس ہمیں آگ کے عذاب سے بچا",
            "Ahl-e-aql (ulul-albab) ki dua.",
            QURAN, "Surah Aal-e-Imran (3:191)", MUTAWATIR
        ),
        d(
            "dua_q_sami_na_munadiyan", "Quranic Duas", "We Heard the Caller to Faith",
            "Imaan aur naik logon ke saath maut ki dua.",
            "رَّبَّنَا إِنَّنَا سَمِعْنَا مُنَادِيًا يُنَادِي لِلْإِيمَانِ أَنْ آمِنُوا بِرَبِّكُمْ فَآمَنَّا ۚ رَبَّنَا فَاغْفِرْ لَنَا ذُنُوبَنَا وَكَفِّرْ عَنَّا سَيِّئَاتِنَا وَتَوَفَّنَا مَعَ الْأَبْرَارِ",
            "Rabbanā innanā sami'nā munādiyan yunādī lil-īmāni an āminū bi-Rabbikum fa-āmannā, Rabbanā faghfir lanā dhunūbanā wa kaffir 'annā sayyi'ātinā wa tawaffanā ma'al-abrār",
            "Aye hamare Rab, humne ek pukaarne wale ko imaan ki taraf pukaarte suna ke apne Rab par imaan lao, to hum imaan le aaye. Aye hamare Rab, hamare gunaah bakhsh de, hamari buraiyan hum se door kar de aur humein naik logon ke saath maut de.",
            "اے ہمارے رب! ہم نے ایک پکارنے والے کو ایمان کی طرف پکارتے سنا کہ اپنے رب پر ایمان لاؤ، تو ہم ایمان لے آئے۔ اے ہمارے رب! ہمارے گناہ بخش دے، ہماری برائیاں ہم سے دور کر دے اور ہمیں نیک لوگوں کے ساتھ موت دے",
            "Maghfirat aur husn-e-khatima ki dua.",
            QURAN, "Surah Aal-e-Imran (3:193)", MUTAWATIR
        ),
        d(
            "dua_q_atina_ma_waadtana", "Quranic Duas", "Grant Us What You Promised",
            "Qayamat ki ruswai se panaah.",
            "رَبَّنَا وَآتِنَا مَا وَعَدتَّنَا عَلَىٰ رُسُلِكَ وَلَا تُخْزِنَا يَوْمَ الْقِيَامَةِ ۗ إِنَّكَ لَا تُخْلِفُ الْمِيعَادَ",
            "Rabbanā wa ātinā mā wa'attanā 'alā rusulika wa lā tukhzinā yawmal-qiyāmah, innaka lā tukhliful-mī'ād",
            "Aye hamare Rab, humein woh ata farma jiska Tune apne Rasoolon ke zariye waada kiya, aur qayamat ke din humein ruswa na kar, beshak Tu waada khilaafi nahi karta.",
            "اے ہمارے رب! ہمیں وہ عطا فرما جس کا تو نے اپنے رسولوں کے ذریعے وعدہ کیا، اور قیامت کے دن ہمیں رسوا نہ کر، بے شک تو وعدہ خلافی نہیں کرتا",
            "Aakhirat ki izzat ki talab.",
            QURAN, "Surah Aal-e-Imran (3:194)", MUTAWATIR
        ),
        d(
            "dua_q_israfana", "Quranic Duas", "Forgive Our Excesses",
            "Pichhle Nabiyon ke saathiyon ki dua.",
            "رَبَّنَا اغْفِرْ لَنَا ذُنُوبَنَا وَإِسْرَافَنَا فِي أَمْرِنَا وَثَبِّتْ أَقْدَامَنَا وَانصُرْنَا عَلَى الْقَوْمِ الْكَافِرِينَ",
            "Rabbanaghfir lanā dhunūbanā wa isrāfanā fī amrinā wa thabbit aqdāmanā wanṣurnā 'alal-qawmil-kāfirīn",
            "Aye hamare Rab, hamare gunaah aur hamare kaamon mein hamari zyaadati maaf farma, hamare qadam jama de aur kaafir qaum par hamari madad farma.",
            "اے ہمارے رب! ہمارے گناہ اور ہمارے کاموں میں ہماری زیادتی معاف فرما، ہمارے قدم جما دے اور کافر قوم پر ہماری مدد فرما",
            "Saabit qadmi aur maafi ki dua.",
            QURAN, "Surah Aal-e-Imran (3:147)", MUTAWATIR
        ),
        d(
            "dua_q_jamiun_nas", "Quranic Duas", "You Will Gather Mankind",
            "Qayamat par yaqeen ka iqraar.",
            "رَبَّنَا إِنَّكَ جَامِعُ النَّاسِ لِيَوْمٍ لَّا رَيْبَ فِيهِ ۚ إِنَّ اللَّهَ لَا يُخْلِفُ الْمِيعَادَ",
            "Rabbanā innaka jāmi'un-nāsi li-yawmil-lā rayba fīh, innallāha lā yukhliful-mī'ād",
            "Aye hamare Rab, beshak Tu logon ko us din jamaa karne wala hai jismein koi shak nahi. Beshak Allah waada khilaafi nahi karta.",
            "اے ہمارے رب! بے شک تو لوگوں کو اس دن جمع کرنے والا ہے جس میں کوئی شک نہیں، بے شک اللہ وعدہ خلافی نہیں کرتا",
            "Raasikh fil-ilm logon ki dua.",
            QURAN, "Surah Aal-e-Imran (3:9)", MUTAWATIR
        ),
        d(
            "dua_q_maash_shahidin", "Quranic Duas", "Write Us Among the Witnesses",
            "Haq pehchaan kar imaan laane walon ki dua.",
            "رَبَّنَا آمَنَّا فَاكْتُبْنَا مَعَ الشَّاهِدِينَ",
            "Rabbanā āmannā faktubnā ma'ash-shāhidīn",
            "Aye hamare Rab, hum imaan laaye, pas humein gawahi dene walon ke saath likh le.",
            "اے ہمارے رب! ہم ایمان لائے، پس ہمیں گواہی دینے والوں کے ساتھ لکھ لے",
            "Imaan ki qabooliyat ki dua.",
            QURAN, "Surah Al-Ma'idah (5:83)", MUTAWATIR
        ),
        d(
            "dua_q_la_tajalna_zalimin", "Quranic Duas", "Not With the Wrongdoers",
            "Ahl-e-A'raaf ki dua.",
            "رَبَّنَا لَا تَجْعَلْنَا مَعَ الْقَوْمِ الظَّالِمِينَ",
            "Rabbanā lā taj'alnā ma'al-qawmiẓ-ẓālimīn",
            "Aye hamare Rab, humein zaalim logon ke saath na kar.",
            "اے ہمارے رب! ہمیں ظالم لوگوں کے ساتھ نہ کر",
            "Zaalimon ke anjaam se panaah.",
            QURAN, "Surah Al-A'raf (7:47)", MUTAWATIR
        ),
        d(
            "dua_q_tawaffana_muslimin", "Quranic Duas", "Patience and Death as Muslims",
            "Firaun ke jaadugaron ki dua jab woh imaan laaye.",
            "رَبَّنَا أَفْرِغْ عَلَيْنَا صَبْرًا وَتَوَفَّنَا مُسْلِمِينَ",
            "Rabbanā afrigh 'alaynā ṣabran wa tawaffanā muslimīn",
            "Aye hamare Rab, hum par sabr undel de aur humein musalman hone ki haalat mein maut de.",
            "اے ہمارے رب! ہم پر صبر انڈیل دے اور ہمیں مسلمان ہونے کی حالت میں موت دے",
            "Aazmaaish mein sabr aur imaan par khaatma.",
            QURAN, "Surah Al-A'raf (7:126)", MUTAWATIR
        ),
        d(
            "dua_q_la_tajalna_fitnah", "Quranic Duas", "Not a Trial for the Oppressors",
            "Hazrat Musa (AS) ki qaum ki dua.",
            "عَلَى اللَّهِ تَوَكَّلْنَا رَبَّنَا لَا تَجْعَلْنَا فِتْنَةً لِّلْقَوْمِ الظَّالِمِينَ (٨٥) وَنَجِّنَا بِرَحْمَتِكَ مِنَ الْقَوْمِ الْكَافِرِينَ (٨٦)",
            "'Alallāhi tawakkalnā, Rabbanā lā taj'alnā fitnatal-lil-qawmiẓ-ẓālimīn, wa najjinā biraḥmatika minal-qawmil-kāfirīn",
            "Humne Allah par bharosa kiya. Aye hamare Rab, humein zaalim qaum ke liye aazmaaish na bana, aur apni rehmat se humein kaafir qaum se nijaat de.",
            "ہم نے اللہ پر بھروسا کیا، اے ہمارے رب! ہمیں ظالم قوم کے لیے آزمائش نہ بنا، اور اپنی رحمت سے ہمیں کافر قوم سے نجات دے",
            "Zulm aur zaalimon se nijaat ki dua.",
            QURAN, "Surah Yunus (10:85-86)", MUTAWATIR
        ),
        d(
            "dua_q_nuh_ma_laysa_li", "Quranic Duas", "Dua of Nuh (AS) for Forgiveness",
            "Hazrat Nooh (AS) ki tauba ki dua.",
            "رَبِّ إِنِّي أَعُوذُ بِكَ أَنْ أَسْأَلَكَ مَا لَيْسَ لِي بِهِ عِلْمٌ ۖ وَإِلَّا تَغْفِرْ لِي وَتَرْحَمْنِي أَكُن مِّنَ الْخَاسِرِينَ",
            "Rabbi innī a'ūdhu bika an as'alaka mā laysa lī bihī 'ilm, wa illā taghfir lī wa tarḥamnī akum-minal-khāsirīn",
            "Aye mere Rab, main Teri panaah maangta hoon is se ke Tujh se woh maangoon jiska mujhe ilm nahi. Agar Tu mujhe maaf na kare aur mujh par reham na kare to main nuqsaan uthane walon mein se ho jaaunga.",
            "اے میرے رب! میں تیری پناہ مانگتا ہوں اس سے کہ تجھ سے وہ مانگوں جس کا مجھے علم نہیں، اور اگر تو نے مجھے نہ بخشا اور مجھ پر رحم نہ کیا تو میں نقصان اٹھانے والوں میں سے ہو جاؤں گا",
            "Adab ke saath dua maangne ki taleem.",
            QURAN, "Surah Hud (11:47)", MUTAWATIR
        ),
        d(
            "dua_q_mudkhala_sidq", "Quranic Duas", "Entrance and Exit of Truth",
            "Kisi naye kaam, ghar ya jagah mein daakhil hone se pehle.",
            "رَّبِّ أَدْخِلْنِي مُدْخَلَ صِدْقٍ وَأَخْرِجْنِي مُخْرَجَ صِدْقٍ وَاجْعَل لِّي مِن لَّدُنكَ سُلْطَانًا نَّصِيرًا",
            "Rabbi adkhilnī mudkhala ṣidqiw-wa akhrijnī mukhraja ṣidqiw-waj'al lī mil-ladunka sulṭānan naṣīrā",
            "Aye mere Rab, mujhe sachchai ke saath daakhil kar aur sachchai ke saath nikaal, aur apne paas se mujhe madadgaar ghalba ata farma.",
            "اے میرے رب! مجھے سچائی کے ساتھ داخل کر اور سچائی کے ساتھ نکال، اور اپنے پاس سے مجھے مددگار غلبہ عطا فرما",
            "Hijrat ke mauqe par Nabi ﷺ ko sikhayi gayi dua.",
            QURAN, "Surah Al-Isra (17:80)", MUTAWATIR
        ),
        d(
            "dua_q_la_tadharni_fardan", "Quranic Duas", "Dua of Zakariya (AS) — Do Not Leave Me Alone",
            "Aulaad ki talab ke liye.",
            "رَبِّ لَا تَذَرْنِي فَرْدًا وَأَنتَ خَيْرُ الْوَارِثِينَ",
            "Rabbi lā tadharnī fardaw-wa Anta khayrul-wārithīn",
            "Aye mere Rab, mujhe akela na chhod, aur Tu sab se behtar waaris hai.",
            "اے میرے رب! مجھے اکیلا نہ چھوڑ اور تو سب سے بہتر وارث ہے",
            "Allah ne is dua par Hazrat Yahya (AS) ata farmaye.",
            QURAN, "Surah Al-Anbiya (21:89)", MUTAWATIR
        ),
        d(
            "dua_q_munzalan_mubarakan", "Quranic Duas", "A Blessed Landing Place",
            "Naye ghar mein shift hone ya kahin utarne par — Hazrat Nooh (AS) ki dua.",
            "رَّبِّ أَنزِلْنِي مُنزَلًا مُّبَارَكًا وَأَنتَ خَيْرُ الْمُنزِلِينَ",
            "Rabbi anzilnī munzalam-mubārakaw-wa Anta khayrul-munzilīn",
            "Aye mere Rab, mujhe barkat wali jagah utaar, aur Tu sab se behtar utaarne wala hai.",
            "اے میرے رب! مجھے برکت والی جگہ اتار اور تو سب سے بہتر اتارنے والا ہے",
            "Naye maqaam mein barkat ki dua.",
            QURAN, "Surah Al-Mu'minun (23:29)", MUTAWATIR
        ),
        d(
            "dua_q_amanna_faghfir", "Quranic Duas", "We Believe, So Forgive Us",
            "Allah ke naik bandon ki dua.",
            "رَبَّنَا آمَنَّا فَاغْفِرْ لَنَا وَارْحَمْنَا وَأَنتَ خَيْرُ الرَّاحِمِينَ",
            "Rabbanā āmannā faghfir lanā warḥamnā wa Anta khayrur-rāḥimīn",
            "Aye hamare Rab, hum imaan laaye, pas humein bakhsh de aur hum par reham farma, aur Tu sab se behtar reham karne wala hai.",
            "اے ہمارے رب! ہم ایمان لائے، پس ہمیں بخش دے اور ہم پر رحم فرما اور تو سب سے بہتر رحم کرنے والا ہے",
            "Maghfirat aur rehmat ki jaame dua.",
            QURAN, "Surah Al-Mu'minun (23:109)", MUTAWATIR
        ),
        d(
            "dua_q_isrif_jahannam", "Quranic Duas", "Avert the Punishment of Hell",
            "Ibaad-ur-Rahman (Rehman ke bandon) ki dua.",
            "رَبَّنَا اصْرِفْ عَنَّا عَذَابَ جَهَنَّمَ ۖ إِنَّ عَذَابَهَا كَانَ غَرَامًا (٦٥) إِنَّهَا سَاءَتْ مُسْتَقَرًّا وَمُقَامًا (٦٦)",
            "Rabbanaṣrif 'annā 'adhāba jahannam, inna 'adhābahā kāna gharāmā, innahā sā'at mustaqarraw-wa muqāmā",
            "Aye hamare Rab, jahannam ka azaab hum se pher de, beshak uska azaab chimat jaane wala hai. Beshak woh bura thikaana aur buri jagah hai.",
            "اے ہمارے رب! جہنم کا عذاب ہم سے پھیر دے، بے شک اس کا عذاب چمٹ جانے والا ہے، بے شک وہ برا ٹھکانا اور بری جگہ ہے",
            "Jahannam se panaah ki Qurani dua.",
            QURAN, "Surah Al-Furqan (25:65-66)", MUTAWATIR
        ),
        d(
            "dua_q_hab_li_hukman", "Quranic Duas", "Dua of Ibrahim (AS) — Wisdom and Jannah",
            "Hikmat, naik logon ki sohbat aur Jannat ki talab.",
            "رَبِّ هَبْ لِي حُكْمًا وَأَلْحِقْنِي بِالصَّالِحِينَ (٨٣) وَاجْعَل لِّي لِسَانَ صِدْقٍ فِي الْآخِرِينَ (٨٤) وَاجْعَلْنِي مِن وَرَثَةِ جَنَّةِ النَّعِيمِ (٨٥)",
            "Rabbi hab lī ḥukmaw-wa alḥiqnī biṣ-ṣāliḥīn, waj'al lī lisāna ṣidqin fil-ākhirīn, waj'alnī miw-warathati jannatin-na'īm",
            "Aye mere Rab, mujhe hikmat ata farma aur naik logon ke saath mila de, baad walon mein mera zikr-e-khair jaari rakh, aur mujhe nematon wali Jannat ke waarison mein se bana.",
            "اے میرے رب! مجھے حکمت عطا فرما اور نیک لوگوں کے ساتھ ملا دے، بعد والوں میں میرا ذکر خیر جاری رکھ، اور مجھے نعمتوں والی جنت کے وارثوں میں سے بنا",
            "Hazrat Ibrahim (AS) ki jaame dua.",
            QURAN, "Surah Ash-Shu'ara (26:83-85)", MUTAWATIR
        ),
        d(
            "dua_q_lut_najjini_ahli", "Quranic Duas", "Dua of Lut (AS) — Save My Family",
            "Buraai ke maahaul se ghar walon ki hifazat.",
            "رَبِّ نَجِّنِي وَأَهْلِي مِمَّا يَعْمَلُونَ",
            "Rabbi najjinī wa ahlī mimmā ya'malūn",
            "Aye mere Rab, mujhe aur mere ghar walon ko unke kaamon (ke wabaal) se nijaat de.",
            "اے میرے رب! مجھے اور میرے گھر والوں کو ان کے کاموں (کے وبال) سے نجات دے",
            "Buraai se ghar walon ki hifazat.",
            QURAN, "Surah Ash-Shu'ara (26:169)", MUTAWATIR
        ),
        d(
            "dua_q_sulaiman_awzini", "Quranic Duas", "Dua of Sulaiman (AS) for Gratitude",
            "Nemat par shukr ki taufeeq.",
            "رَبِّ أَوْزِعْنِي أَنْ أَشْكُرَ نِعْمَتَكَ الَّتِي أَنْعَمْتَ عَلَيَّ وَعَلَىٰ وَالِدَيَّ وَأَنْ أَعْمَلَ صَالِحًا تَرْضَاهُ وَأَدْخِلْنِي بِرَحْمَتِكَ فِي عِبَادِكَ الصَّالِحِينَ",
            "Rabbi awzi'nī an ashkura ni'matakal-latī an'amta 'alayya wa 'alā wālidayya wa an a'mala ṣāliḥan tarḍāhu wa adkhilnī biraḥmatika fī 'ibādikaṣ-ṣāliḥīn",
            "Aye mere Rab, mujhe taufeeq de ke main Teri us nemat ka shukr karoon jo Tune mujh par aur mere waalidain par ki, aur aisa naik amal karoon jo Tujhe pasand ho, aur apni rehmat se mujhe apne naik bandon mein daakhil farma.",
            "اے میرے رب! مجھے توفیق دے کہ میں تیری اس نعمت کا شکر کروں جو تو نے مجھ پر اور میرے والدین پر کی، اور ایسا نیک عمل کروں جو تجھے پسند ہو، اور اپنی رحمت سے مجھے اپنے نیک بندوں میں داخل فرما",
            "Hazrat Sulaiman (AS) ki shukr ki dua.",
            QURAN, "Surah An-Naml (27:19)", MUTAWATIR
        ),
        d(
            "dua_q_musa_zalamtu", "Quranic Duas", "Dua of Musa (AS) — I Wronged Myself",
            "Ghalti ho jaane par foran tauba.",
            "رَبِّ إِنِّي ظَلَمْتُ نَفْسِي فَاغْفِرْ لِي",
            "Rabbi innī ẓalamtu nafsī faghfir lī",
            "Aye mere Rab, maine apni jaan par zulm kiya, pas mujhe bakhsh de.",
            "اے میرے رب! میں نے اپنی جان پر ظلم کیا، پس مجھے بخش دے",
            "Allah ne Hazrat Musa (AS) ko maaf farma diya.",
            QURAN, "Surah Al-Qasas (28:16)", MUTAWATIR
        ),
        d(
            "dua_q_musa_najjini", "Quranic Duas", "Dua of Musa (AS) — Save Me from Oppressors",
            "Zaalimon ke khauf mein.",
            "رَبِّ نَجِّنِي مِنَ الْقَوْمِ الظَّالِمِينَ",
            "Rabbi najjinī minal-qawmiẓ-ẓālimīn",
            "Aye mere Rab, mujhe zaalim qaum se nijaat de.",
            "اے میرے رب! مجھے ظالم قوم سے نجات دے",
            "Misr se nikalte waqt Hazrat Musa (AS) ki dua.",
            QURAN, "Surah Al-Qasas (28:21)", MUTAWATIR
        ),
        d(
            "dua_q_lut_unsurni", "Quranic Duas", "Dua of Lut (AS) — Help Against Corruptors",
            "Fasaad karne walon ke muqaable mein madad.",
            "رَبِّ انصُرْنِي عَلَى الْقَوْمِ الْمُفْسِدِينَ",
            "Rabbinṣurnī 'alal-qawmil-mufsidīn",
            "Aye mere Rab, fasaad karne wali qaum ke muqaable mein meri madad farma.",
            "اے میرے رب! فساد کرنے والی قوم کے مقابلے میں میری مدد فرما",
            "Haq par saabit rehne walon ki dua.",
            QURAN, "Surah Al-'Ankabut (29:30)", MUTAWATIR
        ),
        d(
            "dua_q_hab_li_salihin", "Quranic Duas", "Dua of Ibrahim (AS) for a Righteous Child",
            "Naik aulaad ki talab.",
            "رَبِّ هَبْ لِي مِنَ الصَّالِحِينَ",
            "Rabbi hab lī minaṣ-ṣāliḥīn",
            "Aye mere Rab, mujhe naik (aulaad) ata farma.",
            "اے میرے رب! مجھے نیک (اولاد) عطا فرما",
            "Is dua par Hazrat Ismail (AS) ata hue.",
            QURAN, "Surah As-Saffat (37:100)", MUTAWATIR
        ),
        d(
            "dua_q_wasita_kulla_shay", "Quranic Duas", "Dua of the Angels Bearing the Throne",
            "Arsh uthane wale farishte momino ke liye yeh dua karte hain.",
            "رَبَّنَا وَسِعْتَ كُلَّ شَيْءٍ رَّحْمَةً وَعِلْمًا فَاغْفِرْ لِلَّذِينَ تَابُوا وَاتَّبَعُوا سَبِيلَكَ وَقِهِمْ عَذَابَ الْجَحِيمِ",
            "Rabbanā wasi'ta kulla shay'ir-raḥmataw-wa 'ilman faghfir lilladhīna tābū wattaba'ū sabīlaka wa qihim 'adhābal-jaḥīm",
            "Aye hamare Rab, Teri rehmat aur ilm har cheez ko ghere hue hai, pas un logon ko bakhsh de jinhon ne tauba ki aur Tere raaste par chale, aur unhein jahannam ke azaab se bacha.",
            "اے ہمارے رب! تیری رحمت اور علم ہر چیز کو گھیرے ہوئے ہے، پس ان لوگوں کو بخش دے جنہوں نے توبہ کی اور تیرے راستے پر چلے، اور انہیں جہنم کے عذاب سے بچا",
            "Farishton ki dua mein shaamil hone ki niyyat se.",
            QURAN, "Surah Ghafir (40:7)", MUTAWATIR
        ),
        d(
            "dua_q_ikhwanina", "Quranic Duas", "For Our Brothers Who Preceded Us",
            "Pichhle momino ke liye aur dil ki safai ki dua.",
            "رَبَّنَا اغْفِرْ لَنَا وَلِإِخْوَانِنَا الَّذِينَ سَبَقُونَا بِالْإِيمَانِ وَلَا تَجْعَلْ فِي قُلُوبِنَا غِلًّا لِّلَّذِينَ آمَنُوا رَبَّنَا إِنَّكَ رَءُوفٌ رَّحِيمٌ",
            "Rabbanaghfir lanā wa li-ikhwāninal-ladhīna sabaqūnā bil-īmān, wa lā taj'al fī qulūbinā ghillal-lilladhīna āmanū, Rabbanā innaka Ra'ūfur-Raḥīm",
            "Aye hamare Rab, humein aur hamare un bhaiyon ko bakhsh de jo humse pehle imaan laaye, aur hamare dilon mein imaan walon ke liye koi keena na rakh. Aye hamare Rab, beshak Tu bara shafqat wala, reham wala hai.",
            "اے ہمارے رب! ہمیں اور ہمارے ان بھائیوں کو بخش دے جو ہم سے پہلے ایمان لائے، اور ہمارے دلوں میں ایمان والوں کے لیے کوئی کینہ نہ رکھ، اے ہمارے رب! بے شک تو بڑا شفقت والا مہربان ہے",
            "Sahaba-e-Kiraam (RA) aur pichhle momino ke liye dua.",
            QURAN, "Surah Al-Hashr (59:10)", MUTAWATIR
        ),
        d(
            "dua_q_alayka_tawakkalna", "Quranic Duas", "Upon You We Rely (Ibrahim AS)",
            "Hazrat Ibrahim (AS) aur unke saathiyon ki dua.",
            "رَّبَّنَا عَلَيْكَ تَوَكَّلْنَا وَإِلَيْكَ أَنَبْنَا وَإِلَيْكَ الْمَصِيرُ (٤) رَبَّنَا لَا تَجْعَلْنَا فِتْنَةً لِّلَّذِينَ كَفَرُوا وَاغْفِرْ لَنَا رَبَّنَا ۖ إِنَّكَ أَنتَ الْعَزِيزُ الْحَكِيمُ (٥)",
            "Rabbanā 'alayka tawakkalnā wa ilayka anabnā wa ilaykal-maṣīr, Rabbanā lā taj'alnā fitnatal-lilladhīna kafarū waghfir lanā Rabbanā, innaka Antal-'Azīzul-Ḥakīm",
            "Aye hamare Rab, humne Tujh par bharosa kiya, Teri taraf rujoo kiya aur Teri hi taraf lautna hai. Aye hamare Rab, humein kaafiron ke liye aazmaaish na bana aur humein bakhsh de. Aye hamare Rab, beshak Tu ghalib, hikmat wala hai.",
            "اے ہمارے رب! ہم نے تجھ پر بھروسا کیا، تیری طرف رجوع کیا اور تیری ہی طرف لوٹنا ہے۔ اے ہمارے رب! ہمیں کافروں کے لیے آزمائش نہ بنا اور ہمیں بخش دے، اے ہمارے رب! بے شک تو غالب حکمت والا ہے",
            "Tawakkul aur rujoo ki dua.",
            QURAN, "Surah Al-Mumtahanah (60:4-5)", MUTAWATIR
        ),
        d(
            "dua_q_atmim_nurana", "Quranic Duas", "Perfect Our Light",
            "Qayamat ke din momino ki dua.",
            "رَبَّنَا أَتْمِمْ لَنَا نُورَنَا وَاغْفِرْ لَنَا ۖ إِنَّكَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
            "Rabbanā atmim lanā nūranā waghfir lanā, innaka 'alā kulli shay'in qadīr",
            "Aye hamare Rab, hamare liye hamara noor poora kar de aur humein bakhsh de, beshak Tu har cheez par qaadir hai.",
            "اے ہمارے رب! ہمارے لیے ہمارا نور پورا کر دے اور ہمیں بخش دے، بے شک تو ہر چیز پر قادر ہے",
            "Pul-Siraat par noor ki talab.",
            QURAN, "Surah At-Tahrim (66:8)", MUTAWATIR
        ),
        d(
            "dua_q_asiya_baytan", "Quranic Duas", "Dua of Asiya — A House in Jannah",
            "Firaun ki biwi Hazrat Asiya ki dua.",
            "رَبِّ ابْنِ لِي عِندَكَ بَيْتًا فِي الْجَنَّةِ وَنَجِّنِي مِن فِرْعَوْنَ وَعَمَلِهِ وَنَجِّنِي مِنَ الْقَوْمِ الظَّالِمِينَ",
            "Rabbibni lī 'indaka baytan fil-jannati wa najjinī min Fir'awna wa 'amalihī wa najjinī minal-qawmiẓ-ẓālimīn",
            "Aye mere Rab, mere liye apne paas Jannat mein ghar bana de, aur mujhe Firaun aur uske amal se nijaat de, aur zaalim qaum se nijaat de.",
            "اے میرے رب! میرے لیے اپنے پاس جنت میں گھر بنا دے، اور مجھے فرعون اور اس کے عمل سے نجات دے اور ظالم قوم سے نجات دے",
            "Allah ne momino ke liye misaal banaya.",
            QURAN, "Surah At-Tahrim (66:11)", MUTAWATIR
        ),

        // ---------------- Prayer & Mosque ----------------
        d(
            "dua_adhan_response", "Prayer & Mosque", "Responding to the Adhan",
            "Muazzin jo kahe wohi dohraayein; 'Hayya 'alas-salah' aur 'Hayya 'alal-falah' par yeh kahein.",
            "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            "Lā ḥawla wa lā quwwata illā billāh",
            "Allah ke baghair na koi taaqat hai aur na koi quwwat.",
            "اللہ کے بغیر نہ کوئی طاقت ہے اور نہ کوئی قوت",
            "Dil se jawab dene wala Jannat mein daakhil hoga.",
            "Sahih Muslim", "Hadith 385", "Sahih"
        ),
        d(
            "dua_adhan_shahadah", "Prayer & Mosque", "After the Muazzin's Shahadah",
            "Muazzin ki shahadatain sun kar.",
            "وَأَنَا أَشْهَدُ أَنْ لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ وَأَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ، رَضِيتُ بِاللَّهِ رَبًّا، وَبِمُحَمَّدٍ رَسُولًا، وَبِالْإِسْلَامِ دِينًا",
            "Wa anā ash-hadu an lā ilāha illallāhu waḥdahū lā sharīka lahū wa anna Muḥammadan 'abduhū wa rasūluh, raḍītu billāhi Rabbā, wa bi-Muḥammadin rasūlā, wa bil-Islāmi dīnā",
            "Aur main bhi gawahi deta hoon ke Allah ke siwa koi mabood nahi, woh akela hai, uska koi shareek nahi, aur Muhammad ﷺ uske bande aur Rasool hain. Main Allah ke Rab, Muhammad ﷺ ke Rasool aur Islam ke deen hone par raazi hoon.",
            "اور میں بھی گواہی دیتا ہوں کہ اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے اس کا کوئی شریک نہیں، اور محمد ﷺ اس کے بندے اور رسول ہیں، میں اللہ کے رب، محمد ﷺ کے رسول اور اسلام کے دین ہونے پر راضی ہوں",
            "Uske gunaah maaf kar diye jaate hain.",
            "Sahih Muslim", "Hadith 386", "Sahih"
        ),
        d(
            "dua_istiftah_baid", "Prayer & Mosque", "Opening Dua: Distance Me from Sins",
            "Takbeer-e-Tahreema ke baad (Sana ki jagah bhi parh sakte hain).",
            "اللَّهُمَّ بَاعِدْ بَيْنِي وَبَيْنَ خَطَايَايَ كَمَا بَاعَدْتَ بَيْنَ الْمَشْرِقِ وَالْمَغْرِبِ، اللَّهُمَّ نَقِّنِي مِنَ الْخَطَايَا كَمَا يُنَقَّى الثَّوْبُ الْأَبْيَضُ مِنَ الدَّنَسِ، اللَّهُمَّ اغْسِلْ خَطَايَايَ بِالْمَاءِ وَالثَّلْجِ وَالْبَرَدِ",
            "Allāhumma bā'id baynī wa bayna khaṭāyāya kamā bā'adta baynal-mashriqi wal-maghrib, Allāhumma naqqinī minal-khaṭāyā kamā yunaqqath-thawbul-abyaḍu minad-danas, Allāhummaghsil khaṭāyāya bil-mā'i wath-thalji wal-barad",
            "Ya Allah, mere aur mere gunaahon ke darmiyan itni doori kar de jitni Tune mashriq aur maghrib mein ki. Ya Allah, mujhe gunaahon se aise saaf kar de jaise safed kapda mail se saaf kiya jaata hai. Ya Allah, mere gunaah paani, barf aur olon se dho de.",
            "اے اللہ! میرے اور میرے گناہوں کے درمیان اتنی دوری کر دے جتنی تو نے مشرق اور مغرب میں کی۔ اے اللہ! مجھے گناہوں سے ایسے صاف کر دے جیسے سفید کپڑا میل سے صاف کیا جاتا ہے۔ اے اللہ! میرے گناہ پانی، برف اور اولوں سے دھو دے",
            "Nabi ﷺ takbeer aur qiraat ke darmiyan yeh parhte the.",
            "Sahih al-Bukhari", "Hadith 744; Sahih Muslim 598", "Sahih"
        ),
        d(
            "dua_hamdan_kathiran", "Prayer & Mosque", "Abundant Praise After Ruku",
            "Ruku se uth kar 'Rabbana lakal-hamd' ke saath.",
            "رَبَّنَا وَلَكَ الْحَمْدُ حَمْدًا كَثِيرًا طَيِّبًا مُبَارَكًا فِيهِ",
            "Rabbanā wa lakal-ḥamdu ḥamdan kathīran ṭayyibam-mubārakan fīh",
            "Aye hamare Rab, Tere hi liye tareef hai — bohot zyada, paakeeza aur barkat wali tareef.",
            "اے ہمارے رب! تیرے ہی لیے تعریف ہے، بہت زیادہ، پاکیزہ اور برکت والی تعریف",
            "Tees se zyada farishte ise pehle likhne mein ek doosre se aage badh rahe the.",
            "Sahih al-Bukhari", "Hadith 799", "Sahih"
        ),
        d(
            "dua_subbuhun_quddus", "Prayer & Mosque", "Subbuhun Quddus (Ruku & Sujood)",
            "Ruku aur sajde mein.",
            "سُبُّوحٌ قُدُّوسٌ رَبُّ الْمَلَائِكَةِ وَالرُّوحِ",
            "Subbūḥun Quddūsun Rabbul-malā'ikati war-rūḥ",
            "(Allah) bohot paak, nihaayat muqaddas hai, farishton aur Rooh (Jibreel) ka Rab.",
            "(اللہ) بہت پاک، نہایت مقدس ہے، فرشتوں اور روح (جبریل) کا رب",
            "Nabi ﷺ ruku o sujood mein yeh parhte the.",
            "Sahih Muslim", "Hadith 487", "Sahih"
        ),
        d(
            "dua_sujood_dhanbi_kullah", "Prayer & Mosque", "In Sujood: Forgive All My Sins",
            "Sajde mein.",
            "اللَّهُمَّ اغْفِرْ لِي ذَنْبِي كُلَّهُ، دِقَّهُ وَجِلَّهُ، وَأَوَّلَهُ وَآخِرَهُ، وَعَلَانِيَتَهُ وَسِرَّهُ",
            "Allāhummaghfir lī dhanbī kullah, diqqahū wa jillah, wa awwalahū wa ākhirah, wa 'alāniyatahū wa sirrah",
            "Ya Allah, mere saare gunaah maaf farma — chhote aur bade, pehle aur baad ke, zaahir aur chhupe.",
            "اے اللہ! میرے سارے گناہ معاف فرما، چھوٹے اور بڑے، پہلے اور بعد کے، ظاہر اور چھپے",
            "Sajda dua ki qabooliyat ki jagah hai.",
            "Sahih Muslim", "Hadith 483", "Sahih"
        ),
        d(
            "dua_sujood_ridaka", "Prayer & Mosque", "Refuge in Your Pleasure",
            "Sajde mein — Hazrat Aisha (RA) ne Nabi ﷺ ko yeh parhte suna.",
            "اللَّهُمَّ إِنِّي أَعُوذُ بِرِضَاكَ مِنْ سَخَطِكَ، وَبِمُعَافَاتِكَ مِنْ عُقُوبَتِكَ، وَأَعُوذُ بِكَ مِنْكَ، لَا أُحْصِي ثَنَاءً عَلَيْكَ، أَنْتَ كَمَا أَثْنَيْتَ عَلَىٰ نَفْسِكَ",
            "Allāhumma innī a'ūdhu biriḍāka min sakhaṭik, wa bimu'āfātika min 'uqūbatik, wa a'ūdhu bika mink, lā uḥṣī thanā'an 'alayk, Anta kamā athnayta 'alā nafsik",
            "Ya Allah, main Teri raza ke zariye Teri naraazgi se, Teri maafi ke zariye Teri saza se panaah maangta hoon, aur Tujh se Teri hi panaah maangta hoon. Main Teri tareef ka haq ada nahi kar sakta, Tu waisa hi hai jaisi Tune apni tareef ki.",
            "اے اللہ! میں تیری رضا کے ذریعے تیری ناراضگی سے، تیری معافی کے ذریعے تیری سزا سے پناہ مانگتا ہوں، اور تجھ سے تیری ہی پناہ مانگتا ہوں، میں تیری تعریف کا حق ادا نہیں کر سکتا، تو ویسا ہی ہے جیسی تو نے اپنی تعریف کی",
            "Nabi ﷺ ki sajde ki dua.",
            "Sahih Muslim", "Hadith 486", "Sahih"
        ),
        d(
            "dua_tashahhud", "Prayer & Mosque", "Tashahhud (At-Tahiyyat)",
            "Har qa'de mein — Hazrat Abdullah bin Mas'ud (RA) ki riwayat (Ahnaaf ka ma'mool).",
            "التَّحِيَّاتُ لِلَّهِ وَالصَّلَوَاتُ وَالطَّيِّبَاتُ، السَّلَامُ عَلَيْكَ أَيُّهَا النَّبِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ، السَّلَامُ عَلَيْنَا وَعَلَىٰ عِبَادِ اللَّهِ الصَّالِحِينَ، أَشْهَدُ أَنْ لَا إِلَٰهَ إِلَّا اللَّهُ، وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
            "At-taḥiyyātu lillāhi waṣ-ṣalawātu waṭ-ṭayyibāt, as-salāmu 'alayka ayyuhan-nabiyyu wa raḥmatullāhi wa barakātuh, as-salāmu 'alaynā wa 'alā 'ibādillāhiṣ-ṣāliḥīn, ash-hadu an lā ilāha illallāh, wa ash-hadu anna Muḥammadan 'abduhū wa rasūluh",
            "Tamaam qauli, badani aur maali ibaadatein Allah ke liye hain. Aye Nabi, aap par salaam aur Allah ki rehmat aur barkatein hon. Hum par aur Allah ke naik bandon par salaam ho. Main gawahi deta hoon ke Allah ke siwa koi mabood nahi, aur gawahi deta hoon ke Muhammad ﷺ uske bande aur Rasool hain.",
            "تمام قولی، بدنی اور مالی عبادتیں اللہ کے لیے ہیں۔ اے نبی! آپ پر سلام اور اللہ کی رحمت اور برکتیں ہوں۔ ہم پر اور اللہ کے نیک بندوں پر سلام ہو۔ میں گواہی دیتا ہوں کہ اللہ کے سوا کوئی معبود نہیں اور گواہی دیتا ہوں کہ محمد ﷺ اس کے بندے اور رسول ہیں",
            "Nabi ﷺ ise Quran ki soorat ki tarah sikhate the.",
            "Sahih al-Bukhari", "Hadith 831; Sahih Muslim 402", "Sahih"
        ),
        d(
            "dua_sajdah_tilawah", "Prayer & Mosque", "Sajdah of Recitation",
            "Sajda-e-Tilawat mein.",
            "سَجَدَ وَجْهِيَ لِلَّذِي خَلَقَهُ، وَشَقَّ سَمْعَهُ وَبَصَرَهُ، بِحَوْلِهِ وَقُوَّتِهِ",
            "Sajada wajhiya lilladhī khalaqah, wa shaqqa sam'ahū wa baṣarah, biḥawlihī wa quwwatih",
            "Mere chehre ne us zaat ko sajda kiya jisne use paida kiya aur apni taaqat o quwwat se uske kaan aur aankhein banaayin.",
            "میرے چہرے نے اس ذات کو سجدہ کیا جس نے اسے پیدا کیا اور اپنی طاقت و قوت سے اس کے کان اور آنکھیں بنائیں",
            "Nabi ﷺ raat ke sajda-e-tilawat mein yeh parhte the.",
            "Jami` at-Tirmidhi", "Hadith 580; Sunan Abi Dawud 1414", "Hasan Sahih (Imam Tirmizi)"
        ),
        d(
            "dua_after_witr", "Prayer & Mosque", "After Witr",
            "Witr ke salaam ke baad teen martaba, teesri baar awaaz buland kar ke.",
            "سُبْحَانَ الْمَلِكِ الْقُدُّوسِ",
            "Subḥānal-Malikil-Quddūs",
            "Paak hai Badshah, nihaayat muqaddas.",
            "پاک ہے بادشاہ، نہایت مقدس",
            "Nabi ﷺ witr ke baad yeh farmate.",
            "Sunan an-Nasa'i", "Hadith 1699; Sunan Abi Dawud 1430", "Sahih", 3
        ),
        d(
            "dua_tahajjud_nur", "Prayer & Mosque", "Tahajjud Opening Supplication",
            "Raat ki namaz (tahajjud) shuru karte waqt (dua ka ibtidaai hissa).",
            "اللَّهُمَّ لَكَ الْحَمْدُ، أَنْتَ قَيِّمُ السَّمَاوَاتِ وَالْأَرْضِ وَمَنْ فِيهِنَّ، وَلَكَ الْحَمْدُ، أَنْتَ نُورُ السَّمَاوَاتِ وَالْأَرْضِ وَمَنْ فِيهِنَّ، وَلَكَ الْحَمْدُ، أَنْتَ الْحَقُّ، وَوَعْدُكَ الْحَقُّ، وَلِقَاؤُكَ حَقٌّ، وَقَوْلُكَ حَقٌّ، وَالْجَنَّةُ حَقٌّ، وَالنَّارُ حَقٌّ، وَالنَّبِيُّونَ حَقٌّ، وَمُحَمَّدٌ ﷺ حَقٌّ، وَالسَّاعَةُ حَقٌّ",
            "Allāhumma lakal-ḥamd, Anta qayyimus-samāwāti wal-arḍi wa man fīhinn, wa lakal-ḥamd, Anta nūrus-samāwāti wal-arḍi wa man fīhinn, wa lakal-ḥamd, Antal-ḥaqq, wa wa'dukal-ḥaqq, wa liqā'uka ḥaqq, wa qawluka ḥaqq, wal-jannatu ḥaqq, wan-nāru ḥaqq, wan-nabiyyūna ḥaqq, wa Muḥammadun ṣallallāhu 'alayhi wa sallama ḥaqq, was-sā'atu ḥaqq",
            "Ya Allah, Tere hi liye tareef hai, Tu aasmaanon, zameen aur jo un mein hai unka qaayam rakhne wala hai. Tere hi liye tareef hai, Tu aasmaanon, zameen aur jo un mein hai unka noor hai. Tere hi liye tareef hai, Tu haq hai, Tera waada haq hai, Teri mulaqaat haq hai, Tera qaul haq hai, Jannat haq hai, Jahannam haq hai, Anbiya haq hain, Muhammad ﷺ haq hain aur Qayamat haq hai.",
            "اے اللہ! تیرے ہی لیے تعریف ہے، تو آسمانوں، زمین اور جو ان میں ہے ان کا قائم رکھنے والا ہے۔ تیرے ہی لیے تعریف ہے، تو آسمانوں، زمین اور جو ان میں ہے ان کا نور ہے۔ تیرے ہی لیے تعریف ہے، تو حق ہے، تیرا وعدہ حق ہے، تیری ملاقات حق ہے، تیرا قول حق ہے، جنت حق ہے، جہنم حق ہے، انبیاء حق ہیں، محمد ﷺ حق ہیں اور قیامت حق ہے",
            "Nabi ﷺ tahajjud ke liye uth kar yeh parhte the (mukammal dua Bukhari mein hai).",
            "Sahih al-Bukhari", "Hadith 1120; Sahih Muslim 769", "Sahih"
        ),
        d(
            "dua_rabba_jibrail", "Prayer & Mosque", "Guide Me in Disputed Matters",
            "Tahajjud ki ibtida mein — ikhtilaaf ke waqt haq ki hidayat.",
            "اللَّهُمَّ رَبَّ جِبْرَائِيلَ وَمِيكَائِيلَ وَإِسْرَافِيلَ، فَاطِرَ السَّمَاوَاتِ وَالْأَرْضِ، عَالِمَ الْغَيْبِ وَالشَّهَادَةِ، أَنْتَ تَحْكُمُ بَيْنَ عِبَادِكَ فِيمَا كَانُوا فِيهِ يَخْتَلِفُونَ، اهْدِنِي لِمَا اخْتُلِفَ فِيهِ مِنَ الْحَقِّ بِإِذْنِكَ، إِنَّكَ تَهْدِي مَنْ تَشَاءُ إِلَىٰ صِرَاطٍ مُسْتَقِيمٍ",
            "Allāhumma Rabba Jibrā'īla wa Mīkā'īla wa Isrāfīl, fāṭiras-samāwāti wal-arḍ, 'ālimal-ghaybi wash-shahādah, Anta taḥkumu bayna 'ibādika fīmā kānū fīhi yakhtalifūn, ihdinī limakhtulifa fīhi minal-ḥaqqi bi-idhnik, innaka tahdī man tashā'u ilā ṣirāṭim-mustaqīm",
            "Ya Allah, Jibreel, Mikaeel aur Israfeel ke Rab, aasmaanon aur zameen ke paida karne wale, chhupe aur khule ke jaanne wale! Tu apne bandon ke darmiyan un baaton ka faisla karega jin mein woh ikhtilaaf karte the. Jis haq mein ikhtilaaf kiya gaya, apne hukm se us mein meri rehnumai farma. Beshak Tu jise chahe seedhe raaste ki hidayat deta hai.",
            "اے اللہ! جبرائیل، میکائیل اور اسرافیل کے رب، آسمانوں اور زمین کے پیدا کرنے والے، غیب اور حاضر کے جاننے والے! تو اپنے بندوں کے درمیان ان باتوں کا فیصلہ کرے گا جن میں وہ اختلاف کرتے تھے۔ جس حق میں اختلاف کیا گیا، اپنے حکم سے اس میں میری رہنمائی فرما، بے شک تو جسے چاہے سیدھے راستے کی ہدایت دیتا ہے",
            "Nabi ﷺ raat ki namaz isi dua se shuru karte the.",
            "Sahih Muslim", "Hadith 770", "Sahih"
        ),
        d(
            "dua_going_to_mosque_nur", "Prayer & Mosque", "Going to the Mosque (Dua of Light)",
            "Fajr ke liye masjid jaate hue.",
            "اللَّهُمَّ اجْعَلْ فِي قَلْبِي نُورًا، وَفِي بَصَرِي نُورًا، وَفِي سَمْعِي نُورًا، وَعَنْ يَمِينِي نُورًا، وَعَنْ يَسَارِي نُورًا، وَفَوْقِي نُورًا، وَتَحْتِي نُورًا، وَأَمَامِي نُورًا، وَخَلْفِي نُورًا، وَاجْعَلْ لِي نُورًا",
            "Allāhummaj'al fī qalbī nūrā, wa fī baṣarī nūrā, wa fī sam'ī nūrā, wa 'an yamīnī nūrā, wa 'an yasārī nūrā, wa fawqī nūrā, wa taḥtī nūrā, wa amāmī nūrā, wa khalfī nūrā, waj'al lī nūrā",
            "Ya Allah, mere dil mein noor kar de, meri aankhon mein noor, mere kaanon mein noor, mere daayen noor, mere baayen noor, mere upar noor, mere neeche noor, mere aage noor, mere peechhe noor, aur mere liye noor bana de.",
            "اے اللہ! میرے دل میں نور کر دے، میری آنکھوں میں نور، میرے کانوں میں نور، میرے دائیں نور، میرے بائیں نور، میرے اوپر نور، میرے نیچے نور، میرے آگے نور، میرے پیچھے نور، اور میرے لیے نور بنا دے",
            "Nabi ﷺ namaz ke liye nikalte hue yeh parhte the.",
            "Sahih al-Bukhari", "Hadith 6316; Sahih Muslim 763", "Sahih"
        ),
        d(
            "dua_waswasa_salah", "Prayer & Mosque", "Waswasa During Salah (Khinzab)",
            "Namaz mein shaitaani waswase aayein to panaah maang kar baayin taraf teen martaba halka thutkaar.",
            "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ",
            "A'ūdhu billāhi minash-shayṭānir-rajīm",
            "Main Allah ki panaah maangta hoon mardood Shaitan se.",
            "میں اللہ کی پناہ مانگتا ہوں شیطان مردود سے",
            "Hazrat Usman bin Abil-Aas (RA) ne yeh kiya aur waswasa chala gaya.",
            "Sahih Muslim", "Hadith 2203", "Sahih", 3
        ),

        // ---------------- Morning & Evening ----------------
        d(
            "dua_alimal_ghayb", "Morning & Evening", "Knower of the Unseen",
            "Subah, shaam aur sone se pehle — Nabi ﷺ ne Hazrat Abu Bakr (RA) ko sikhayi.",
            "اللَّهُمَّ عَالِمَ الْغَيْبِ وَالشَّهَادَةِ، فَاطِرَ السَّمَاوَاتِ وَالْأَرْضِ، رَبَّ كُلِّ شَيْءٍ وَمَلِيكَهُ، أَشْهَدُ أَنْ لَا إِلَٰهَ إِلَّا أَنْتَ، أَعُوذُ بِكَ مِنْ شَرِّ نَفْسِي، وَمِنْ شَرِّ الشَّيْطَانِ وَشِرْكِهِ",
            "Allāhumma 'Ālimal-ghaybi wash-shahādah, Fāṭiras-samāwāti wal-arḍ, Rabba kulli shay'in wa malīkah, ash-hadu an lā ilāha illā Ant, a'ūdhu bika min sharri nafsī, wa min sharrish-shayṭāni wa shirkih",
            "Ya Allah, ghaib aur haazir ke jaanne wale, aasmaanon aur zameen ke paida karne wale, har cheez ke Rab aur Maalik! Main gawahi deta hoon ke Tere siwa koi mabood nahi. Main apne nafs ke shar se, aur Shaitan ke shar aur uske shirk se Teri panaah maangta hoon.",
            "اے اللہ! غیب اور حاضر کے جاننے والے، آسمانوں اور زمین کے پیدا کرنے والے، ہر چیز کے رب اور مالک! میں گواہی دیتا ہوں کہ تیرے سوا کوئی معبود نہیں، میں اپنے نفس کے شر سے اور شیطان کے شر اور اس کے شرک سے تیری پناہ مانگتا ہوں",
            "Nafs aur Shaitan ke shar se hifazat.",
            "Sunan Abi Dawud", "Hadith 5067; Jami` at-Tirmidhi 3392", "Sahih (Imam Tirmizi: Hasan Sahih)"
        ),
        d(
            "dua_fitratil_islam", "Morning & Evening", "Upon the Fitrah of Islam",
            "Subah ke waqt.",
            "أَصْبَحْنَا عَلَىٰ فِطْرَةِ الْإِسْلَامِ، وَعَلَىٰ كَلِمَةِ الْإِخْلَاصِ، وَعَلَىٰ دِينِ نَبِيِّنَا مُحَمَّدٍ ﷺ، وَعَلَىٰ مِلَّةِ أَبِينَا إِبْرَاهِيمَ حَنِيفًا مُسْلِمًا وَمَا كَانَ مِنَ الْمُشْرِكِينَ",
            "Aṣbaḥnā 'alā fiṭratil-Islām, wa 'alā kalimatil-ikhlāṣ, wa 'alā dīni nabiyyinā Muḥammadin ṣallallāhu 'alayhi wa sallam, wa 'alā millati abīnā Ibrāhīma ḥanīfam-muslimā, wa mā kāna minal-mushrikīn",
            "Humne Islam ki fitrat par, kalima-e-ikhlaas par, apne Nabi Muhammad ﷺ ke deen par aur apne baap Ibrahim (AS) ki millat par subah ki, jo yaksu musalman the aur mushrikon mein se na the.",
            "ہم نے اسلام کی فطرت پر، کلمۂ اخلاص پر، اپنے نبی محمد ﷺ کے دین پر اور اپنے باپ ابراہیم کی ملت پر صبح کی جو یکسو مسلمان تھے اور مشرکوں میں سے نہ تھے",
            "Har subah imaan ki tajdeed.",
            "Musnad Ahmad", "Hadith 15360", "Sahih"
        ),
        d(
            "dua_amsayna_fitrah", "Morning & Evening", "Evening upon the Fitrah of Islam",
            "Shaam ke waqt.",
            "أَمْسَيْنَا عَلَىٰ فِطْرَةِ الْإِسْلَامِ، وَعَلَىٰ كَلِمَةِ الْإِخْلَاصِ، وَعَلَىٰ دِينِ نَبِيِّنَا مُحَمَّدٍ ﷺ، وَعَلَىٰ مِلَّةِ أَبِينَا إِبْرَاهِيمَ حَنِيفًا مُسْلِمًا وَمَا كَانَ مِنَ الْمُشْرِكِينَ",
            "Amsaynā 'alā fiṭratil-Islām, wa 'alā kalimatil-ikhlāṣ, wa 'alā dīni nabiyyinā Muḥammadin ṣallallāhu 'alayhi wa sallam, wa 'alā millati abīnā Ibrāhīma ḥanīfam-muslimā, wa mā kāna minal-mushrikīn",
            "Humne Islam ki fitrat par, kalima-e-ikhlaas par, apne Nabi Muhammad ﷺ ke deen par aur apne baap Ibrahim (AS) ki millat par shaam ki, jo yaksu musalman the aur mushrikon mein se na the.",
            "ہم نے اسلام کی فطرت پر، کلمۂ اخلاص پر، اپنے نبی محمد ﷺ کے دین پر اور اپنے باپ ابراہیم کی ملت پر شام کی جو یکسو مسلمان تھے اور مشرکوں میں سے نہ تھے",
            "Har shaam imaan ki tajdeed.",
            "Musnad Ahmad", "Hadith 15360", "Sahih"
        ),
        d(
            "dua_tahleel_10_fajr", "Morning & Evening", "Ten Tahleel After Fajr and Maghrib",
            "Fajr (aur Maghrib) ke baad, paon mode aur baat kiye baghair das martaba.",
            "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، يُحْيِي وَيُمِيتُ، وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
            "Lā ilāha illallāhu waḥdahū lā sharīka lah, lahul-mulku wa lahul-ḥamd, yuḥyī wa yumīt, wa huwa 'alā kulli shay'in qadīr",
            "Allah ke siwa koi mabood nahi, woh akela hai, uska koi shareek nahi, usi ki badshahat aur tareef hai, wohi zinda karta aur maarta hai, aur woh har cheez par qaadir hai.",
            "اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے، اس کا کوئی شریک نہیں، اسی کی بادشاہی اور تعریف ہے، وہی زندہ کرتا اور مارتا ہے اور وہ ہر چیز پر قادر ہے",
            "Das nekiyan, das gunaah maaf, das darje buland aur din bhar Shaitan se hifazat.",
            "Jami` at-Tirmidhi", "Hadith 3474", "Hasan Sahih Gharib (Imam Tirmizi)", 10
        ),
        d(
            "dua_la_hawla_kanz", "Morning & Evening", "A Treasure of Jannah",
            "Har waqt kasrat se.",
            "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            "Lā ḥawla wa lā quwwata illā billāh",
            "Allah ke baghair na koi taaqat hai aur na koi quwwat.",
            "اللہ کے بغیر نہ کوئی طاقت ہے اور نہ کوئی قوت",
            "Yeh Jannat ke khazaanon mein se ek khazaana hai.",
            "Sahih al-Bukhari", "Hadith 6384; Sahih Muslim 2704", "Sahih"
        ),
        d(
            "dua_four_beloved_words", "Morning & Evening", "The Four Most Beloved Words",
            "Allah ko sab se pasandeeda kalimaat.",
            "سُبْحَانَ اللَّهِ، وَالْحَمْدُ لِلَّهِ، وَلَا إِلَٰهَ إِلَّا اللَّهُ، وَاللَّهُ أَكْبَرُ",
            "Subḥānallāh, wal-ḥamdu lillāh, wa lā ilāha illallāh, wallāhu akbar",
            "Allah paak hai, saari tareef Allah ke liye hai, Allah ke siwa koi mabood nahi, aur Allah sab se bara hai.",
            "اللہ پاک ہے، تمام تعریف اللہ کے لیے ہے، اللہ کے سوا کوئی معبود نہیں اور اللہ سب سے بڑا ہے",
            "Nabi ﷺ ko yeh har us cheez se zyada mehboob hain jis par suraj nikalta hai.",
            "Sahih Muslim", "Hadith 2137, 2695", "Sahih"
        ),
        d(
            "dua_two_light_words", "Morning & Evening", "Two Words Light on the Tongue",
            "Zubaan par halke, meezan mein bhaari.",
            "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
            "Subḥānallāhi wa biḥamdih, subḥānallāhil-'Aẓīm",
            "Allah paak hai apni tareef ke saath, Allah paak hai jo azmat wala hai.",
            "اللہ پاک ہے اپنی تعریف کے ساتھ، اللہ پاک ہے جو عظمت والا ہے",
            "Rahman ko mehboob, zubaan par halke aur meezan mein bhaari. (Bukhari ki aakhri hadith)",
            "Sahih al-Bukhari", "Hadith 6406, 7563; Sahih Muslim 2694", "Sahih"
        ),
        d(
            "dua_ya_dhal_jalal", "Morning & Evening", "Ya Dhal-Jalali wal-Ikram",
            "Dua mein ise laazim pakdo — Nabi ﷺ ka hukm.",
            "يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
            "Yā Dhal-Jalāli wal-Ikrām",
            "Aye Jalaal aur Ikraam wale!",
            "اے جلال اور اکرام والے!",
            "Nabi ﷺ ne farmaya: 'Ya Dhal-Jalali wal-Ikram' ko laazim pakdo.",
            "Jami` at-Tirmidhi", "Hadith 3525; Musnad Ahmad 17596", "Sahih"
        ),

        // ---------------- Sleep & Awakening ----------------
        d(
            "dua_sleep_wadatu_janbi", "Sleep & Awakening", "In Your Name I Lay Down",
            "Bistar jhaad kar letne ke baad.",
            "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي، وَبِكَ أَرْفَعُهُ، إِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ",
            "Bismika Rabbī waḍa'tu janbī, wa bika arfa'uh, in amsakta nafsī farḥamhā, wa in arsaltahā faḥfaẓhā bimā taḥfaẓu bihī 'ibādakaṣ-ṣāliḥīn",
            "Aye mere Rab, Tere naam se maine apna pehlu rakha aur Tere hi naam se uthaunga. Agar Tu meri rooh rok le to us par reham farma, aur agar chhod de to uski aise hifazat farma jaise apne naik bandon ki hifazat karta hai.",
            "اے میرے رب! تیرے نام سے میں نے اپنا پہلو رکھا اور تیرے ہی نام سے اٹھاؤں گا، اگر تو میری روح روک لے تو اس پر رحم فرما، اور اگر چھوڑ دے تو اس کی ایسی حفاظت فرما جیسے اپنے نیک بندوں کی حفاظت کرتا ہے",
            "Sone se pehle rooh ko Allah ke supurd karna.",
            "Sahih al-Bukhari", "Hadith 6320; Sahih Muslim 2714", "Sahih"
        ),
        d(
            "dua_sleep_khalaqta_nafsi", "Sleep & Awakening", "You Created My Soul",
            "Sone se pehle — Hazrat Ibn Umar (RA) ki riwayat.",
            "اللَّهُمَّ خَلَقْتَ نَفْسِي وَأَنْتَ تَوَفَّاهَا، لَكَ مَمَاتُهَا وَمَحْيَاهَا، إِنْ أَحْيَيْتَهَا فَاحْفَظْهَا، وَإِنْ أَمَتَّهَا فَاغْفِرْ لَهَا، اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَافِيَةَ",
            "Allāhumma khalaqta nafsī wa Anta tawaffāhā, laka mamātuhā wa maḥyāhā, in aḥyaytahā faḥfaẓhā, wa in amattahā faghfir lahā, Allāhumma innī as'alukal-'āfiyah",
            "Ya Allah, Tune meri jaan paida ki aur Tu hi use wafaat dega. Uska marna aur jeena Tere hi liye hai. Agar Tu use zinda rakhe to uski hifazat farma, aur agar maut de to use bakhsh de. Ya Allah, main Tujh se aafiyat maangta hoon.",
            "اے اللہ! تو نے میری جان پیدا کی اور تو ہی اسے وفات دے گا، اس کا مرنا اور جینا تیرے ہی لیے ہے، اگر تو اسے زندہ رکھے تو اس کی حفاظت فرما، اور اگر موت دے تو اسے بخش دے، اے اللہ! میں تجھ سے عافیت مانگتا ہوں",
            "Nabi ﷺ ne sone ke waqt yeh parhne ka hukm diya.",
            "Sahih Muslim", "Hadith 2712", "Sahih"
        ),
        d(
            "dua_sleep_atamana", "Sleep & Awakening", "Gratitude Before Sleep",
            "Bistar par jaate waqt.",
            "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا، وَكَفَانَا وَآوَانَا، فَكَمْ مِمَّنْ لَا كَافِيَ لَهُ وَلَا مُؤْوِيَ",
            "Al-ḥamdu lillāhil-ladhī aṭ'amanā wa saqānā, wa kafānā wa āwānā, fakam mimman lā kāfiya lahū wa lā mu'wī",
            "Saari tareef Allah ke liye jisne humein khilaya, pilaya, hamari zaroorat poori ki aur humein thikaana diya. Kitne hi log hain jinka na koi kifaayat karne wala hai na thikaana dene wala.",
            "تمام تعریف اللہ کے لیے جس نے ہمیں کھلایا، پلایا، ہماری کفایت کی اور ہمیں ٹھکانا دیا، کتنے ہی لوگ ہیں جن کا نہ کوئی کفایت کرنے والا ہے نہ ٹھکانا دینے والا",
            "Nabi ﷺ bistar par jaate waqt yeh farmate.",
            "Sahih Muslim", "Hadith 2715", "Sahih"
        ),
        d(
            "dua_sleep_rabbas_samawat", "Sleep & Awakening", "Lord of the Heavens — Pay Our Debt",
            "Sone se pehle daayin karwat par — Hazrat Fatima (RA) ko sikhayi.",
            "اللَّهُمَّ رَبَّ السَّمَاوَاتِ وَرَبَّ الْأَرْضِ وَرَبَّ الْعَرْشِ الْعَظِيمِ، رَبَّنَا وَرَبَّ كُلِّ شَيْءٍ، فَالِقَ الْحَبِّ وَالنَّوَىٰ، وَمُنْزِلَ التَّوْرَاةِ وَالْإِنْجِيلِ وَالْفُرْقَانِ، أَعُوذُ بِكَ مِنْ شَرِّ كُلِّ شَيْءٍ أَنْتَ آخِذٌ بِنَاصِيَتِهِ، اللَّهُمَّ أَنْتَ الْأَوَّلُ فَلَيْسَ قَبْلَكَ شَيْءٌ، وَأَنْتَ الْآخِرُ فَلَيْسَ بَعْدَكَ شَيْءٌ، وَأَنْتَ الظَّاهِرُ فَلَيْسَ فَوْقَكَ شَيْءٌ، وَأَنْتَ الْبَاطِنُ فَلَيْسَ دُونَكَ شَيْءٌ، اقْضِ عَنَّا الدَّيْنَ وَأَغْنِنَا مِنَ الْفَقْرِ",
            "Allāhumma Rabbas-samāwāti wa Rabbal-arḍi wa Rabbal-'Arshil-'Aẓīm, Rabbanā wa Rabba kulli shay', fāliqal-ḥabbi wan-nawā, wa munzilat-Tawrāti wal-Injīli wal-Furqān, a'ūdhu bika min sharri kulli shay'in Anta ākhidhum-bināṣiyatih, Allāhumma Antal-Awwalu falaysa qablaka shay', wa Antal-Ākhiru falaysa ba'daka shay', wa Antaẓ-Ẓāhiru falaysa fawqaka shay', wa Antal-Bāṭinu falaysa dūnaka shay', iqḍi 'annad-dayna wa aghninā minal-faqr",
            "Ya Allah, aasmaanon, zameen aur Arsh-e-Azeem ke Rab, hamare aur har cheez ke Rab, daane aur guthli ko phaadne wale, Taurat, Injeel aur Furqan utaarne wale! Main har us cheez ke shar se Teri panaah maangta hoon jiski peshaani Tere qabze mein hai. Ya Allah, Tu hi Awwal hai, Tujh se pehle kuch nahi; Tu hi Aakhir hai, Tere baad kuch nahi; Tu hi Zaahir hai, Tere upar kuch nahi; Tu hi Baatin hai, Tujh se chhupa kuch nahi. Hamara qarz ada kar de aur humein faqr se ghani kar de.",
            "اے اللہ! آسمانوں، زمین اور عرش عظیم کے رب، ہمارے اور ہر چیز کے رب، دانے اور گٹھلی کو پھاڑنے والے، تورات، انجیل اور فرقان نازل کرنے والے! میں ہر اس چیز کے شر سے تیری پناہ مانگتا ہوں جس کی پیشانی تیرے قبضے میں ہے۔ اے اللہ! تو ہی اول ہے تجھ سے پہلے کچھ نہیں، تو ہی آخر ہے تیرے بعد کچھ نہیں، تو ہی ظاہر ہے تیرے اوپر کچھ نہیں، تو ہی باطن ہے تجھ سے پوشیدہ کچھ نہیں، ہمارا قرض ادا کر دے اور ہمیں فقر سے غنی کر دے",
            "Qarz aur tangdasti se nijaat ki dua.",
            "Sahih Muslim", "Hadith 2713", "Sahih"
        ),
        d(
            "dua_sleep_fear", "Sleep & Awakening", "Fear or Unease in Sleep",
            "Neend mein ghabrahat ya dar lage to.",
            "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّةِ مِنْ غَضَبِهِ وَعِقَابِهِ، وَشَرِّ عِبَادِهِ، وَمِنْ هَمَزَاتِ الشَّيَاطِينِ وَأَنْ يَحْضُرُونِ",
            "A'ūdhu bikalimātillāhit-tāmmati min ghaḍabihī wa 'iqābih, wa sharri 'ibādih, wa min hamazātish-shayāṭīni wa an yaḥḍurūn",
            "Main Allah ke mukammal kalimaat ki panaah maangta hoon uske ghazab aur saza se, uske bandon ke shar se, aur shaitanon ke waswason se aur is se ke woh mere paas aayein.",
            "میں اللہ کے کامل کلمات کی پناہ مانگتا ہوں اس کے غضب اور سزا سے، اس کے بندوں کے شر سے، اور شیطانوں کے وسوسوں سے اور اس سے کہ وہ میرے پاس آئیں",
            "Aisa karne se woh cheez nuqsaan nahi degi.",
            "Jami` at-Tirmidhi", "Hadith 3528; Sunan Abi Dawud 3893", "Hasan Gharib (Imam Tirmizi)"
        ),
        d(
            "dua_waking_afani", "Sleep & Awakening", "Waking: He Restored My Soul",
            "Jaagne par.",
            "الْحَمْدُ لِلَّهِ الَّذِي عَافَانِي فِي جَسَدِي، وَرَدَّ عَلَيَّ رُوحِي، وَأَذِنَ لِي بِذِكْرِهِ",
            "Al-ḥamdu lillāhil-ladhī 'āfānī fī jasadī, wa radda 'alayya rūḥī, wa adhina lī bidhikrih",
            "Saari tareef Allah ke liye jisne mere jism ko aafiyat di, meri rooh mujhe lauta di aur mujhe apne zikr ki ijaazat di.",
            "تمام تعریف اللہ کے لیے جس نے میرے جسم کو عافیت دی، میری روح مجھے لوٹا دی اور مجھے اپنے ذکر کی اجازت دی",
            "Nayi subah par shukr.",
            "Jami` at-Tirmidhi", "Hadith 3401", "Hasan"
        )
    )
}
