package com.example.domain.model

// Second batch of authentic duas (Ahle Sunnat wal Jamaat sources, with references).
internal object MoreDuas2 {

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
        // ---------------- Morning & Evening ----------------
        d(
            "dua_allahumma_bika_asbahna", "Morning & Evening", "Allahumma Bika Asbahna",
            "Subah ke waqt.",
            "اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ النُّشُورُ",
            "Allāhumma bika aṣbaḥnā, wa bika amsaynā, wa bika naḥyā, wa bika namūtu, wa ilaykan-nushūr",
            "Ya Allah, Teri hi madad se humne subah ki aur Teri hi madad se shaam ki, Tere hi hukm se jeete aur marte hain, aur Teri hi taraf uth kar jaana hai.",
            "اے اللہ! تیری ہی مدد سے ہم نے صبح کی اور تیری ہی مدد سے شام کی، تیرے ہی حکم سے ہم جیتے اور مرتے ہیں اور تیری ہی طرف اٹھ کر جانا ہے",
            "Nabi ﷺ Sahaba ko subah o shaam yeh sikhate the.",
            "Jami` at-Tirmidhi", "Hadith 3391; Sunan Abi Dawud 5068", "Hasan"
        ),
        d(
            "dua_allahumma_bika_amsayna", "Morning & Evening", "Allahumma Bika Amsayna",
            "Shaam ke waqt.",
            "اللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ الْمَصِيرُ",
            "Allāhumma bika amsaynā, wa bika aṣbaḥnā, wa bika naḥyā, wa bika namūtu, wa ilaykal-maṣīr",
            "Ya Allah, Teri hi madad se humne shaam ki aur Teri hi madad se subah ki, Tere hi hukm se jeete aur marte hain, aur Teri hi taraf lautna hai.",
            "اے اللہ! تیری ہی مدد سے ہم نے شام کی اور تیری ہی مدد سے صبح کی، تیرے ہی حکم سے ہم جیتے اور مرتے ہیں اور تیری ہی طرف لوٹنا ہے",
            "Nabi ﷺ Sahaba ko subah o shaam yeh sikhate the.",
            "Jami` at-Tirmidhi", "Hadith 3391; Sunan Abi Dawud 5068", "Hasan"
        ),
        d(
            "dua_afiyah_morning", "Morning & Evening", "Asking for Afiyah",
            "Nabi ﷺ subah o shaam kabhi yeh dua nahi chhodte the.",
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالْآخِرَةِ",
            "Allāhumma innī as'alukal-'afwa wal-'āfiyata fid-dunyā wal-ākhirah",
            "Ya Allah, main Tujh se dunya aur aakhirat mein maafi aur aafiyat maangta hoon.",
            "اے اللہ! میں تجھ سے دنیا اور آخرت میں معافی اور عافیت کا سوال کرتا ہوں",
            "Aafiyat se behtar koi cheez kisi ko nahi di gayi.",
            "Sunan Abi Dawud", "Hadith 5074; Sunan Ibn Majah 3871", "Sahih"
        ),
        d(
            "dua_afini_fi_badani", "Morning & Evening", "Health of Body, Hearing and Sight",
            "Subah aur shaam teen teen martaba.",
            "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لَا إِلَٰهَ إِلَّا أَنْتَ",
            "Allāhumma 'āfinī fī badanī, Allāhumma 'āfinī fī sam'ī, Allāhumma 'āfinī fī baṣarī, lā ilāha illā Ant",
            "Ya Allah, mere jism mein aafiyat de, meri sunne ki taaqat mein aafiyat de, meri nazar mein aafiyat de. Tere siwa koi mabood nahi.",
            "اے اللہ! میرے بدن میں عافیت دے، میرے کانوں میں عافیت دے، میری آنکھوں میں عافیت دے، تیرے سوا کوئی معبود نہیں",
            "Sehat aur hawaas ki hifazat ki dua.",
            "Sunan Abi Dawud", "Hadith 5090", "Hasan", 3
        ),
        d(
            "dua_ya_hayyu_aslih", "Morning & Evening", "Rectify All My Affairs",
            "Nabi ﷺ ne Hazrat Fatima (RA) ko subah o shaam parhne ki wasiyat ki.",
            "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ، أَصْلِحْ لِي شَأْنِي كُلَّهُ، وَلَا تَكِلْنِي إِلَىٰ نَفْسِي طَرْفَةَ عَيْنٍ",
            "Yā Ḥayyu yā Qayyūmu biraḥmatika astaghīth, aṣliḥ lī sha'nī kullah, wa lā takilnī ilā nafsī ṭarfata 'ayn",
            "Aye Zinda, aye Qaayam rakhne wale, Teri rehmat se faryaad karta hoon, mere saare kaam durust kar de aur mujhe palak jhapakne ke barabar bhi mere nafs ke hawaale na kar.",
            "اے زندہ! اے قائم رکھنے والے! تیری رحمت سے فریاد کرتا ہوں، میرے سارے کام درست کر دے اور مجھے پلک جھپکنے کے برابر بھی میرے نفس کے حوالے نہ کر",
            "Har kaam mein Allah ki madad ki talab.",
            "Sunan an-Nasa'i al-Kubra", "Hadith 10405; Mustadrak al-Hakim 2000", "Hasan"
        ),
        d(
            "dua_subhanallah_adada_khalqihi", "Morning & Evening", "Tasbeeh of Juwayriyah (RA)",
            "Subah teen martaba — ghanton ke zikr se bhaari.",
            "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ",
            "Subḥānallāhi wa biḥamdihī, 'adada khalqihī, wa riḍā nafsihī, wa zinata 'arshihī, wa midāda kalimātih",
            "Allah paak hai apni tareef ke saath — apni makhlooq ki ginti ke barabar, apni raza ke mutabiq, apne Arsh ke wazan ke barabar aur apne kalimaat ki siyaahi ke barabar.",
            "اللہ پاک ہے اپنی تعریف کے ساتھ، اپنی مخلوق کی تعداد کے برابر، اپنی رضا کے مطابق، اپنے عرش کے وزن کے برابر اور اپنے کلمات کی سیاہی کے برابر",
            "Yeh chaar kalimaat teen baar kehna subah se dopahar tak ke zikr se wazan mein bhaari hain.",
            "Sahih Muslim", "Hadith 2726", "Sahih", 3
        ),
        d(
            "dua_salawat", "Morning & Evening", "Durood-e-Ibrahimi",
            "Namaz mein aur har waqt Nabi ﷺ par durood.",
            "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ، كَمَا صَلَّيْتَ عَلَىٰ إِبْرَاهِيمَ وَعَلَىٰ آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ، اللَّهُمَّ بَارِكْ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ، كَمَا بَارَكْتَ عَلَىٰ إِبْرَاهِيمَ وَعَلَىٰ آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ",
            "Allāhumma ṣalli 'alā Muḥammadin wa 'alā āli Muḥammad, kamā ṣallayta 'alā Ibrāhīma wa 'alā āli Ibrāhīm, innaka Ḥamīdum-Majīd. Allāhumma bārik 'alā Muḥammadin wa 'alā āli Muḥammad, kamā bārakta 'alā Ibrāhīma wa 'alā āli Ibrāhīm, innaka Ḥamīdum-Majīd",
            "Ya Allah, Muhammad ﷺ aur aal-e-Muhammad par rehmat naazil farma jaise Tune Ibrahim (AS) aur aal-e-Ibrahim par naazil ki, beshak Tu tareef wala, buzurgi wala hai. Ya Allah, Muhammad ﷺ aur aal-e-Muhammad par barkat naazil farma jaise Tune Ibrahim (AS) aur aal-e-Ibrahim par naazil ki, beshak Tu tareef wala, buzurgi wala hai.",
            "اے اللہ! محمد ﷺ اور آل محمد پر رحمت نازل فرما جیسے تو نے ابراہیم اور آل ابراہیم پر نازل کی، بے شک تو تعریف والا بزرگی والا ہے۔ اے اللہ! محمد ﷺ اور آل محمد پر برکت نازل فرما جیسے تو نے ابراہیم اور آل ابراہیم پر نازل کی، بے شک تو تعریف والا بزرگی والا ہے",
            "Jo ek martaba durood bheje, Allah us par das rehmatein naazil farmata hai (Muslim 408).",
            "Sahih al-Bukhari", "Hadith 3370; Sahih Muslim 408", "Sahih"
        ),

        // ---------------- Prayer & Mosque ----------------
        d(
            "dua_wudu_after", "Prayer & Mosque", "After Wudu",
            "Wuzu mukammal karne ke baad.",
            "أَشْهَدُ أَنْ لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ، اللَّهُمَّ اجْعَلْنِي مِنَ التَّوَّابِينَ وَاجْعَلْنِي مِنَ الْمُتَطَهِّرِينَ",
            "Ash-hadu an lā ilāha illallāhu waḥdahū lā sharīka lah, wa ash-hadu anna Muḥammadan 'abduhū wa rasūluh. Allāhummaj-'alnī minat-tawwābīna waj-'alnī minal-mutaṭahhirīn",
            "Main gawahi deta hoon ke Allah ke siwa koi mabood nahi, woh akela hai, uska koi shareek nahi, aur Muhammad ﷺ uske bande aur Rasool hain. Ya Allah, mujhe tauba karne walon aur paak rehne walon mein se bana.",
            "میں گواہی دیتا ہوں کہ اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے اس کا کوئی شریک نہیں، اور محمد ﷺ اس کے بندے اور رسول ہیں۔ اے اللہ! مجھے توبہ کرنے والوں اور پاک رہنے والوں میں سے بنا",
            "Jannat ke aathon darwaaze khol diye jaate hain, jis se chahe daakhil ho.",
            "Sahih Muslim", "Hadith 234; Jami` at-Tirmidhi 55", "Sahih"
        ),
        d(
            "dua_wudu_before", "Prayer & Mosque", "Before Wudu",
            "Wuzu shuru karte waqt.",
            "بِسْمِ اللَّهِ",
            "Bismillāh",
            "Allah ke naam se.",
            "اللہ کے نام سے",
            "Jis ne wuzu par Allah ka naam na liya uska wuzu (kaamil) nahi.",
            "Sunan Abi Dawud", "Hadith 101; Jami` at-Tirmidhi 25", "Hasan (majmoo'i turuq se)"
        ),
        d(
            "dua_istiftah", "Prayer & Mosque", "Opening Supplication (Sana)",
            "Namaz shuru karne ke baad, Takbeer-e-Tahreema ke foran baad.",
            "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، وَتَبَارَكَ اسْمُكَ، وَتَعَالَىٰ جَدُّكَ، وَلَا إِلَٰهَ غَيْرُكَ",
            "Subḥānakallāhumma wa biḥamdik, wa tabārakasmuk, wa ta'ālā jadduk, wa lā ilāha ghayruk",
            "Ya Allah, Tu paak hai apni tareef ke saath, Tera naam barkat wala hai, Teri shaan buland hai aur Tere siwa koi mabood nahi.",
            "اے اللہ! تو پاک ہے اپنی تعریف کے ساتھ، تیرا نام بابرکت ہے، تیری شان بلند ہے اور تیرے سوا کوئی معبود نہیں",
            "Namaz ki masnoon ibtidaai dua.",
            "Sunan Abi Dawud", "Hadith 775, 776; Jami` at-Tirmidhi 243", "Sahih"
        ),
        d(
            "dua_ruku", "Prayer & Mosque", "In Ruku",
            "Ruku mein kam az kam teen martaba.",
            "سُبْحَانَ رَبِّيَ الْعَظِيمِ",
            "Subḥāna Rabbiyal-'Aẓīm",
            "Paak hai mera Rab jo azmat wala hai.",
            "پاک ہے میرا رب جو عظمت والا ہے",
            "Ruku ki masnoon tasbeeh.",
            "Sahih Muslim", "Hadith 772", "Sahih", 3
        ),
        d(
            "dua_sujood", "Prayer & Mosque", "In Sujood",
            "Sajde mein kam az kam teen martaba.",
            "سُبْحَانَ رَبِّيَ الْأَعْلَىٰ",
            "Subḥāna Rabbiyal-A'lā",
            "Paak hai mera Rab jo sab se buland hai.",
            "پاک ہے میرا رب جو سب سے بلند ہے",
            "Banda sajde mein apne Rab ke sab se qareeb hota hai (Muslim 482).",
            "Sahih Muslim", "Hadith 772", "Sahih", 3
        ),
        d(
            "dua_rising_ruku", "Prayer & Mosque", "Rising from Ruku",
            "Ruku se uthte hue aur seedha khade ho kar.",
            "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ، رَبَّنَا وَلَكَ الْحَمْدُ",
            "Sami'allāhu liman ḥamidah, Rabbanā wa lakal-ḥamd",
            "Allah ne us ki sun li jisne uski tareef ki. Aye hamare Rab, Tere hi liye saari tareef hai.",
            "اللہ نے اس کی سن لی جس نے اس کی تعریف کی۔ اے ہمارے رب! تیرے ہی لیے سب تعریف ہے",
            "Jiska 'Rabbana wa lakal-hamd' farishton ke saath mil gaya uske pichhle gunaah maaf.",
            "Sahih al-Bukhari", "Hadith 796", "Sahih"
        ),
        d(
            "dua_between_sajdas", "Prayer & Mosque", "Between the Two Sajdas",
            "Do sajdon ke darmiyan baithe hue.",
            "رَبِّ اغْفِرْ لِي، رَبِّ اغْفِرْ لِي",
            "Rabbighfir lī, Rabbighfir lī",
            "Aye mere Rab, mujhe bakhsh de, aye mere Rab, mujhe bakhsh de.",
            "اے میرے رب! مجھے بخش دے، اے میرے رب! مجھے بخش دے",
            "Jalsa ki masnoon dua.",
            "Sunan Abi Dawud", "Hadith 874; Sunan Ibn Majah 897", "Sahih"
        ),
        d(
            "dua_before_salam_four", "Prayer & Mosque", "Refuge from Four Things (Before Salam)",
            "Aakhri tashahhud aur durood ke baad, salaam se pehle.",
            "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ عَذَابِ جَهَنَّمَ، وَمِنْ عَذَابِ الْقَبْرِ، وَمِنْ فِتْنَةِ الْمَحْيَا وَالْمَمَاتِ، وَمِنْ شَرِّ فِتْنَةِ الْمَسِيحِ الدَّجَّالِ",
            "Allāhumma innī a'ūdhu bika min 'adhābi jahannam, wa min 'adhābil-qabr, wa min fitnatil-maḥyā wal-mamāt, wa min sharri fitnatil-masīḥid-dajjāl",
            "Ya Allah, main Teri panaah maangta hoon jahannam ke azaab se, qabr ke azaab se, zindagi aur maut ke fitne se, aur Maseeh Dajjal ke fitne ke shar se.",
            "اے اللہ! میں تیری پناہ مانگتا ہوں جہنم کے عذاب سے، قبر کے عذاب سے، زندگی اور موت کے فتنے سے اور مسیح دجال کے فتنے کے شر سے",
            "Nabi ﷺ ne in chaar cheezon se panaah maangne ka hukm diya.",
            "Sahih Muslim", "Hadith 588; Sahih al-Bukhari 1377", "Sahih"
        ),
        d(
            "dua_qunoot_witr", "Prayer & Mosque", "Qunoot in Witr",
            "Witr ki namaz mein — Nabi ﷺ ne Hazrat Hasan bin Ali (RA) ko sikhayi.",
            "اللَّهُمَّ اهْدِنِي فِيمَنْ هَدَيْتَ، وَعَافِنِي فِيمَنْ عَافَيْتَ، وَتَوَلَّنِي فِيمَنْ تَوَلَّيْتَ، وَبَارِكْ لِي فِيمَا أَعْطَيْتَ، وَقِنِي شَرَّ مَا قَضَيْتَ، فَإِنَّكَ تَقْضِي وَلَا يُقْضَىٰ عَلَيْكَ، وَإِنَّهُ لَا يَذِلُّ مَنْ وَالَيْتَ، تَبَارَكْتَ رَبَّنَا وَتَعَالَيْتَ",
            "Allāhummah-dinī fīman hadayt, wa 'āfinī fīman 'āfayt, wa tawallanī fīman tawallayt, wa bārik lī fīmā a'ṭayt, wa qinī sharra mā qaḍayt, fa innaka taqḍī wa lā yuqḍā 'alayk, wa innahū lā yadhillu man wālayt, tabārakta Rabbanā wa ta'ālayt",
            "Ya Allah, mujhe hidayat de un logon mein jinhein Tune hidayat di, aafiyat de un mein jinhein Tune aafiyat di, meri sarparasti kar un mein jinki Tune sarparasti ki, jo Tune diya usmein barkat de, aur jo Tune faisla kiya uske shar se bacha. Beshak Tu faisla karta hai aur Tere khilaaf faisla nahi hota, aur jiska Tu dost ho woh zaleel nahi hota. Aye hamare Rab, Tu barkat wala aur buland hai.",
            "اے اللہ! مجھے ہدایت دے ان میں جنہیں تو نے ہدایت دی، مجھے عافیت دے ان میں جنہیں تو نے عافیت دی، میری سرپرستی کر ان میں جن کی تو نے سرپرستی کی، جو تو نے دیا اس میں برکت دے، اور جو تو نے فیصلہ کیا اس کے شر سے بچا، بے شک تو فیصلہ کرتا ہے اور تیرے خلاف فیصلہ نہیں ہوتا، اور جس کا تو دوست ہو وہ ذلیل نہیں ہوتا، اے ہمارے رب تو بابرکت اور بلند ہے",
            "Witr ki masnoon qunoot. (Ahnaaf ke haan 'Allahumma inna nasta'inuka' bhi ma'roof hai.)",
            "Sunan Abi Dawud", "Hadith 1425; Jami` at-Tirmidhi 464", "Sahih"
        ),
        d(
            "dua_qunoot_hanafi", "Prayer & Mosque", "Qunoot (Allahumma Inna Nasta'inuka)",
            "Witr mein — Ahnaaf ke haan ma'mool.",
            "اللَّهُمَّ إِنَّا نَسْتَعِينُكَ وَنَسْتَغْفِرُكَ وَنُؤْمِنُ بِكَ وَنَتَوَكَّلُ عَلَيْكَ وَنُثْنِي عَلَيْكَ الْخَيْرَ وَنَشْكُرُكَ وَلَا نَكْفُرُكَ وَنَخْلَعُ وَنَتْرُكُ مَنْ يَفْجُرُكَ، اللَّهُمَّ إِيَّاكَ نَعْبُدُ وَلَكَ نُصَلِّي وَنَسْجُدُ وَإِلَيْكَ نَسْعَىٰ وَنَحْفِدُ وَنَرْجُو رَحْمَتَكَ وَنَخْشَىٰ عَذَابَكَ إِنَّ عَذَابَكَ بِالْكُفَّارِ مُلْحِقٌ",
            "Allāhumma innā nasta'īnuka wa nastaghfiruka wa nu'minu bika wa natawakkalu 'alayka wa nuthnī 'alaykal-khayr, wa nashkuruka wa lā nakfuruk, wa nakhla'u wa natruku man yafjuruk. Allāhumma iyyāka na'budu wa laka nuṣallī wa nasjudu wa ilayka nas'ā wa naḥfidu wa narjū raḥmataka wa nakhshā 'adhābak, inna 'adhābaka bil-kuffāri mulḥiq",
            "Ya Allah, hum Tujh se madad aur maafi maangte hain, Tujh par imaan laate aur bharosa karte hain, Teri achhi tareef karte hain, Tera shukr karte hain, naashukri nahi karte, aur jo Teri nafarmani kare use chhodte hain. Ya Allah, hum Teri hi ibaadat karte hain, Tere hi liye namaz parhte aur sajda karte hain, Teri taraf daudte aur lapakte hain, Teri rehmat ke ummeedwaar hain aur Tere azaab se darte hain; beshak Tera azaab kaafiron ko pahunchne wala hai.",
            "اے اللہ! ہم تجھ سے مدد اور مغفرت مانگتے ہیں، تجھ پر ایمان لاتے اور بھروسا کرتے ہیں، تیری اچھی تعریف کرتے ہیں، تیرا شکر کرتے ہیں ناشکری نہیں کرتے، اور جو تیری نافرمانی کرے اسے چھوڑتے ہیں۔ اے اللہ! ہم تیری ہی عبادت کرتے ہیں، تیرے لیے نماز پڑھتے اور سجدہ کرتے ہیں، تیری طرف دوڑتے ہیں، تیری رحمت کے امیدوار ہیں اور تیرے عذاب سے ڈرتے ہیں، بے شک تیرا عذاب کافروں کو پہنچنے والا ہے",
            "Hazrat Umar (RA) se qunoot mein manqool.",
            "Musannaf Ibn Abi Shaybah", "Hadith 7031; Sunan al-Kubra lil-Bayhaqi 3143", "Mauqoof Sahih (Hazrat Umar RA se)"
        ),

        // ---------------- Sleep & Awakening ----------------
        d(
            "dua_sleep_aslamtu", "Sleep & Awakening", "Last Words Before Sleep",
            "Wuzu kar ke daayin karwat par let kar — sab se aakhri baat.",
            "اللَّهُمَّ أَسْلَمْتُ نَفْسِي إِلَيْكَ، وَفَوَّضْتُ أَمْرِي إِلَيْكَ، وَأَلْجَأْتُ ظَهْرِي إِلَيْكَ، رَغْبَةً وَرَهْبَةً إِلَيْكَ، لَا مَلْجَأَ وَلَا مَنْجَا مِنْكَ إِلَّا إِلَيْكَ، آمَنْتُ بِكِتَابِكَ الَّذِي أَنْزَلْتَ، وَبِنَبِيِّكَ الَّذِي أَرْسَلْتَ",
            "Allāhumma aslamtu nafsī ilayk, wa fawwaḍtu amrī ilayk, wa alja'tu ẓahrī ilayk, raghbatan wa rahbatan ilayk, lā malja'a wa lā manjā minka illā ilayk, āmantu bikitābikal-ladhī anzalt, wa binabiyyikal-ladhī arsalt",
            "Ya Allah, maine apni jaan Tere supurd ki, apna maamla Tere hawaale kiya, apni pusht Teri panaah mein di, Teri raghbat aur Tere khauf ke saath. Tujh se bachne ki koi jagah aur nijaat nahi magar Teri hi taraf. Main Teri naazil ki hui kitaab aur Tere bheje hue Nabi par imaan laaya.",
            "اے اللہ! میں نے اپنی جان تیرے سپرد کی، اپنا معاملہ تیرے حوالے کیا، اپنی پشت تیری پناہ میں دی، تیری رغبت اور تیرے خوف کے ساتھ، تجھ سے بچنے کی کوئی پناہ اور نجات کی جگہ نہیں مگر تیری ہی طرف، میں تیری نازل کی ہوئی کتاب اور تیرے بھیجے ہوئے نبی پر ایمان لایا",
            "Agar usi raat maut aa jaaye to fitrat (Islam) par maut hogi.",
            "Sahih al-Bukhari", "Hadith 247; Sahih Muslim 2710", "Sahih"
        ),
        d(
            "dua_waking_night", "Sleep & Awakening", "Waking Up at Night",
            "Raat ko aankh khulne par.",
            "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ، الْحَمْدُ لِلَّهِ، وَسُبْحَانَ اللَّهِ، وَلَا إِلَٰهَ إِلَّا اللَّهُ، وَاللَّهُ أَكْبَرُ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ، اللَّهُمَّ اغْفِرْ لِي",
            "Lā ilāha illallāhu waḥdahū lā sharīka lah, lahul-mulku wa lahul-ḥamd, wa huwa 'alā kulli shay'in qadīr. Al-ḥamdu lillāh, wa subḥānallāh, wa lā ilāha illallāh, wallāhu akbar, wa lā ḥawla wa lā quwwata illā billāh. Allāhummaghfir lī",
            "Allah ke siwa koi mabood nahi, woh akela hai, uska koi shareek nahi, usi ki badshahat aur tareef hai aur woh har cheez par qaadir hai. Saari tareef Allah ke liye, Allah paak hai, Allah ke siwa koi mabood nahi, Allah sab se bara hai, aur Allah ke baghair na koi taaqat hai na quwwat. Ya Allah, mujhe bakhsh de.",
            "اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے، اس کا کوئی شریک نہیں، اسی کی بادشاہی اور تعریف ہے اور وہ ہر چیز پر قادر ہے۔ الحمد للہ، سبحان اللہ، لا الہ الا اللہ، اللہ اکبر، ولا حول ولا قوۃ الا باللہ۔ اے اللہ! مجھے بخش دے",
            "Dua qubool hoti hai, aur wuzu kar ke namaz parhe to namaz qubool hoti hai.",
            "Sahih al-Bukhari", "Hadith 1154", "Sahih"
        ),

        // ---------------- Anxiety & Distress ----------------
        d(
            "dua_hamm_ibn_masud", "Anxiety & Distress", "Dua for Grief (Ibn Mas'ud)",
            "Gham aur pareshani mein — Allah gham ko khushi se badal deta hai.",
            "اللَّهُمَّ إِنِّي عَبْدُكَ، ابْنُ عَبْدِكَ، ابْنُ أَمَتِكَ، نَاصِيَتِي بِيَدِكَ، مَاضٍ فِيَّ حُكْمُكَ، عَدْلٌ فِيَّ قَضَاؤُكَ، أَسْأَلُكَ بِكُلِّ اسْمٍ هُوَ لَكَ سَمَّيْتَ بِهِ نَفْسَكَ، أَوْ أَنْزَلْتَهُ فِي كِتَابِكَ، أَوْ عَلَّمْتَهُ أَحَدًا مِنْ خَلْقِكَ، أَوِ اسْتَأْثَرْتَ بِهِ فِي عِلْمِ الْغَيْبِ عِنْدَكَ، أَنْ تَجْعَلَ الْقُرْآنَ رَبِيعَ قَلْبِي، وَنُورَ صَدْرِي، وَجِلَاءَ حُزْنِي، وَذَهَابَ هَمِّي",
            "Allāhumma innī 'abduk, ibnu 'abdik, ibnu amatik, nāṣiyatī biyadik, māḍin fiyya ḥukmuk, 'adlun fiyya qaḍā'uk, as'aluka bikulli-smin huwa lak, sammayta bihī nafsak, aw anzaltahū fī kitābik, aw 'allamtahū aḥadan min khalqik, awista'tharta bihī fī 'ilmil-ghaybi 'indak, an taj'alal-Qur'āna rabī'a qalbī, wa nūra ṣadrī, wa jalā'a ḥuznī, wa dhahāba hammī",
            "Ya Allah, main Tera banda hoon, Tere bande ka beta, Teri bandi ka beta. Meri peshaani Tere haath mein hai, mujh par Tera hukm jaari hai, mere baare mein Tera faisla insaaf wala hai. Main Tujh se Tere har us naam ke waaste se sawaal karta hoon jo Tune apna rakha, ya apni kitaab mein utaara, ya apni kisi makhlooq ko sikhaya, ya apne paas ilm-e-ghaib mein mehfooz rakha — ke Quran ko mere dil ki bahaar, mere seene ka noor, mere gham ka ilaaj aur meri fikr ko door karne wala bana de.",
            "اے اللہ! میں تیرا بندہ ہوں، تیرے بندے کا بیٹا، تیری بندی کا بیٹا، میری پیشانی تیرے ہاتھ میں ہے، مجھ پر تیرا حکم جاری ہے، میرے بارے میں تیرا فیصلہ عدل ہے۔ میں تجھ سے تیرے ہر اس نام کے واسطے سے سوال کرتا ہوں جو تو نے اپنا رکھا، یا اپنی کتاب میں نازل کیا، یا اپنی مخلوق میں سے کسی کو سکھایا، یا اپنے پاس علم غیب میں محفوظ رکھا، کہ تو قرآن کو میرے دل کی بہار، میرے سینے کا نور، میرے غم کا علاج اور میری فکر کو دور کرنے والا بنا دے",
            "Allah uski fikr aur gham door kar ke khushi ata farmata hai.",
            "Musnad Ahmad", "Hadith 3712; Sahih Ibn Hibban 972", "Sahih (Ibn Hibban)"
        ),
        d(
            "dua_difficulty_sahl", "Anxiety & Distress", "When Something Is Difficult",
            "Koi kaam mushkil lage to.",
            "اللَّهُمَّ لَا سَهْلَ إِلَّا مَا جَعَلْتَهُ سَهْلًا، وَأَنْتَ تَجْعَلُ الْحَزْنَ إِذَا شِئْتَ سَهْلًا",
            "Allāhumma lā sahla illā mā ja'altahū sahlā, wa Anta taj'alul-ḥazna idhā shi'ta sahlā",
            "Ya Allah, koi cheez aasaan nahi magar jise Tu aasaan kar de, aur Tu jab chahe mushkil ko aasaan kar deta hai.",
            "اے اللہ! کوئی چیز آسان نہیں مگر جسے تو آسان کر دے، اور تو جب چاہے مشکل کو آسان کر دیتا ہے",
            "Imtihaan aur mushkil kaamon mein aasaani.",
            "Sahih Ibn Hibban", "Hadith 974", "Sahih"
        ),
        d(
            "dua_musibah", "Anxiety & Distress", "Upon a Calamity (Istirja)",
            "Kisi bhi nuqsaan ya musibat par.",
            "إِنَّا لِلَّهِ وَإِنَّا إِلَيْهِ رَاجِعُونَ، اللَّهُمَّ أْجُرْنِي فِي مُصِيبَتِي وَأَخْلِفْ لِي خَيْرًا مِنْهَا",
            "Innā lillāhi wa innā ilayhi rāji'ūn, Allāhumma'-jurnī fī muṣībatī wa akhlif lī khayram-minhā",
            "Hum Allah hi ke hain aur usi ki taraf lautne wale hain. Ya Allah, mujhe meri musibat mein ajr de aur mujhe is se behtar badla ata farma.",
            "ہم اللہ ہی کے ہیں اور اسی کی طرف لوٹنے والے ہیں۔ اے اللہ! مجھے میری مصیبت میں اجر دے اور اس سے بہتر بدلہ عطا فرما",
            "Allah zaroor behtar badla deta hai — Hazrat Umm-e-Salama (RA) ko Nabi ﷺ mile.",
            "Sahih Muslim", "Hadith 918", "Sahih"
        ),
        d(
            "dua_debt", "Anxiety & Distress", "For Relief from Debt",
            "Qarz ki adaaigi ke liye — Hazrat Ali (RA) ne sikhayi.",
            "اللَّهُمَّ اكْفِنِي بِحَلَالِكَ عَنْ حَرَامِكَ، وَأَغْنِنِي بِفَضْلِكَ عَمَّنْ سِوَاكَ",
            "Allāhummak-finī biḥalālika 'an ḥarāmik, wa aghninī bifaḍlika 'amman siwāk",
            "Ya Allah, apne halaal ke zariye mujhe haraam se bacha kar kaafi ho ja, aur apne fazl se mujhe apne siwa sab se be-niyaaz kar de.",
            "اے اللہ! اپنے حلال کے ذریعے مجھے حرام سے بچا کر کافی ہو جا اور اپنے فضل سے مجھے اپنے سوا سب سے بے نیاز کر دے",
            "Pahaar ke barabar qarz ho to bhi Allah ada karwa deta hai.",
            "Jami` at-Tirmidhi", "Hadith 3563", "Hasan"
        ),
        d(
            "dua_anger", "Anxiety & Distress", "When Angry",
            "Ghussa aane par.",
            "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ",
            "A'ūdhu billāhi minash-shayṭānir-rajīm",
            "Main Allah ki panaah maangta hoon mardood Shaitan se.",
            "میں اللہ کی پناہ مانگتا ہوں شیطان مردود سے",
            "Nabi ﷺ ne farmaya: agar yeh keh le to uska ghussa chala jaaye.",
            "Sahih al-Bukhari", "Hadith 6115; Sahih Muslim 2610", "Sahih"
        ),
        d(
            "dua_waswasa", "Anxiety & Distress", "Against Waswasa in Faith",
            "Imaan ke baare mein shaitaani waswase aayein to.",
            "آمَنْتُ بِاللَّهِ وَرُسُلِهِ",
            "Āmantu billāhi wa rusulih",
            "Main Allah aur uske Rasoolon par imaan laaya.",
            "میں اللہ اور اس کے رسولوں پر ایمان لایا",
            "Waswasa aaye to yeh kahe aur ruk jaaye (A'udhu billah bhi parhe).",
            "Sahih Muslim", "Hadith 134", "Sahih"
        ),

        // ---------------- Travel & Leaving Home ----------------
        d(
            "dua_travel_full", "Travel & Leaving Home", "Complete Travel Supplication",
            "Safar par rawaana hote waqt, 'Subhanalladhi sakhkhara' ke baad.",
            "اللَّهُمَّ إِنَّا نَسْأَلُكَ فِي سَفَرِنَا هَٰذَا الْبِرَّ وَالتَّقْوَىٰ، وَمِنَ الْعَمَلِ مَا تَرْضَىٰ، اللَّهُمَّ هَوِّنْ عَلَيْنَا سَفَرَنَا هَٰذَا وَاطْوِ عَنَّا بُعْدَهُ، اللَّهُمَّ أَنْتَ الصَّاحِبُ فِي السَّفَرِ، وَالْخَلِيفَةُ فِي الْأَهْلِ",
            "Allāhumma innā nas'aluka fī safarinā hādhal-birra wat-taqwā, wa minal-'amali mā tarḍā. Allāhumma hawwin 'alaynā safaranā hādhā waṭwi 'annā bu'dah. Allāhumma Antaṣ-ṣāḥibu fis-safar, wal-khalīfatu fil-ahl",
            "Ya Allah, hum is safar mein Tujh se naiki aur taqwa maangte hain aur aisa amal jise Tu pasand kare. Ya Allah, hamara yeh safar hum par aasaan kar de aur iski doori ko samet de. Ya Allah, Tu hi safar mein saathi aur ghar walon mein nigehbaan hai.",
            "اے اللہ! ہم اس سفر میں تجھ سے نیکی اور تقویٰ مانگتے ہیں اور ایسا عمل جسے تو پسند کرے۔ اے اللہ! ہمارا یہ سفر ہم پر آسان کر دے اور اس کی دوری کو لپیٹ دے۔ اے اللہ! تو ہی سفر میں ساتھی اور گھر والوں میں نگہبان ہے",
            "Safar ki mukammal masnoon dua.",
            "Sahih Muslim", "Hadith 1342", "Sahih"
        ),
        d(
            "dua_return_travel", "Travel & Leaving Home", "Returning from Travel",
            "Safar se wapsi par.",
            "آيِبُونَ تَائِبُونَ عَابِدُونَ لِرَبِّنَا حَامِدُونَ",
            "Āyibūna tā'ibūna 'ābidūna li-Rabbinā ḥāmidūn",
            "Hum lautne wale, tauba karne wale, ibaadat karne wale aur apne Rab ki tareef karne wale hain.",
            "ہم لوٹنے والے، توبہ کرنے والے، عبادت کرنے والے اور اپنے رب کی تعریف کرنے والے ہیں",
            "Nabi ﷺ safar se wapsi par yeh farmate the.",
            "Sahih Muslim", "Hadith 1342", "Sahih"
        ),
        d(
            "dua_farewell_traveller", "Travel & Leaving Home", "Bidding Farewell to a Traveller",
            "Musafir ko rukhsat karte waqt.",
            "أَسْتَوْدِعُ اللَّهَ دِينَكَ وَأَمَانَتَكَ وَخَوَاتِيمَ عَمَلِكَ",
            "Astawdi'ullāha dīnaka wa amānataka wa khawātīma 'amalik",
            "Main tumhara deen, tumhari amaanat aur tumhare aamaal ka anjaam Allah ke supurd karta hoon.",
            "میں تمہارا دین، تمہاری امانت اور تمہارے اعمال کا انجام اللہ کے سپرد کرتا ہوں",
            "Allah ke supurd ki hui cheez zaaya nahi hoti.",
            "Sunan Abi Dawud", "Hadith 2600; Jami` at-Tirmidhi 3443", "Sahih"
        ),
        d(
            "dua_entering_town", "Travel & Leaving Home", "Stopping at a Place",
            "Kisi jagah padaao ya naye shehar mein pahunch kar.",
            "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            "A'ūdhu bikalimātillāhit-tāmmāti min sharri mā khalaq",
            "Main Allah ke mukammal kalimaat ki panaah maangta hoon har makhlooq ke shar se.",
            "میں اللہ کے کامل کلمات کی پناہ مانگتا ہوں ہر مخلوق کے شر سے",
            "Wahan se kooch karne tak koi cheez nuqsaan nahi degi.",
            "Sahih Muslim", "Hadith 2708", "Sahih"
        ),
        d(
            "dua_boarding_ship", "Travel & Leaving Home", "Boarding a Ship / Vehicle",
            "Kashti ya sawari par sawaar hote waqt — Hazrat Nooh (AS) ka qaul.",
            "بِسْمِ اللَّهِ مَجْرَاهَا وَمُرْسَاهَا ۚ إِنَّ رَبِّي لَغَفُورٌ رَّحِيمٌ",
            "Bismillāhi majrāhā wa mursāhā, inna Rabbī la-Ghafūrur-Raḥīm",
            "Allah hi ke naam se iska chalna aur thehrna hai, beshak mera Rab bakhshne wala, reham karne wala hai.",
            "اللہ ہی کے نام سے اس کا چلنا اور ٹھہرنا ہے، بے شک میرا رب بخشنے والا مہربان ہے",
            "Qurani ayat — Nooh (AS) ki kashti ki dua.",
            "The Noble Quran", "Surah Hud (11:41)", "Mutawatir (Quranic Verse)"
        ),

        // ---------------- Forgiveness & Istighfar ----------------
        d(
            "dua_istighfar_hayyul_qayyum", "Forgiveness & Istighfar", "Istighfar with Hayyul-Qayyum",
            "Tauba ke liye.",
            "أَسْتَغْفِرُ اللَّهَ الَّذِي لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ وَأَتُوبُ إِلَيْهِ",
            "Astaghfirullāhal-ladhī lā ilāha illā huwal-Ḥayyul-Qayyūmu wa atūbu ilayh",
            "Main us Allah se maafi maangta hoon jiske siwa koi mabood nahi, jo zinda aur qaayam rakhne wala hai, aur usi ki taraf tauba karta hoon.",
            "میں اس اللہ سے مغفرت مانگتا ہوں جس کے سوا کوئی معبود نہیں، جو زندہ اور قائم رکھنے والا ہے، اور اسی کی طرف توبہ کرتا ہوں",
            "Gunaah maaf ho jaate hain chahe maidan-e-jang se bhaaga ho.",
            "Sunan Abi Dawud", "Hadith 1517; Jami` at-Tirmidhi 3577", "Sahih"
        ),
        d(
            "dua_rabbighfir_warham", "Forgiveness & Istighfar", "Rabbighfir Warham",
            "Qurani dua — maghfirat aur rehmat.",
            "رَّبِّ اغْفِرْ وَارْحَمْ وَأَنتَ خَيْرُ الرَّاحِمِينَ",
            "Rabbighfir warḥam wa Anta khayrur-rāḥimīn",
            "Aye mere Rab, bakhsh de aur reham farma, aur Tu sab se behtar reham karne wala hai.",
            "اے میرے رب! بخش دے اور رحم فرما اور تو سب سے بہتر رحم کرنے والا ہے",
            "Surah Mominoon ki aakhri ayat.",
            "The Noble Quran", "Surah Al-Mu'minun (23:118)", "Mutawatir (Quranic Verse)"
        ),
        d(
            "dua_majlis_kaffarah", "Forgiveness & Istighfar", "Kaffarat al-Majlis",
            "Majlis/baithak se uthte waqt.",
            "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، أَشْهَدُ أَنْ لَا إِلَٰهَ إِلَّا أَنْتَ، أَسْتَغْفِرُكَ وَأَتُوبُ إِلَيْكَ",
            "Subḥānakallāhumma wa biḥamdik, ash-hadu an lā ilāha illā Ant, astaghfiruka wa atūbu ilayk",
            "Ya Allah, Tu paak hai apni tareef ke saath. Main gawahi deta hoon ke Tere siwa koi mabood nahi. Main Tujh se maafi maangta hoon aur Teri taraf tauba karta hoon.",
            "اے اللہ! تو پاک ہے اپنی تعریف کے ساتھ، میں گواہی دیتا ہوں کہ تیرے سوا کوئی معبود نہیں، میں تجھ سے مغفرت مانگتا ہوں اور تیری طرف توبہ کرتا ہوں",
            "Majlis mein jo ghalat baatein huin unka kaffara ban jaati hai.",
            "Jami` at-Tirmidhi", "Hadith 3433; Sunan Abi Dawud 4858", "Hasan Sahih (Imam Tirmizi)"
        ),
        d(
            "dua_jami_forgiveness", "Forgiveness & Istighfar", "Comprehensive Forgiveness",
            "Nabi ﷺ ki jaame dua.",
            "اللَّهُمَّ اغْفِرْ لِي خَطِيئَتِي وَجَهْلِي، وَإِسْرَافِي فِي أَمْرِي، وَمَا أَنْتَ أَعْلَمُ بِهِ مِنِّي",
            "Allāhummaghfir lī khaṭī'atī wa jahlī, wa isrāfī fī amrī, wa mā Anta a'lamu bihī minnī",
            "Ya Allah, meri ghalti, meri naadaani, mere kaam mein meri zyaadati, aur woh sab jo Tu mujh se zyada jaanta hai, maaf farma de.",
            "اے اللہ! میری خطا، میری نادانی، میرے معاملے میں میری زیادتی اور جو تو مجھ سے زیادہ جانتا ہے، سب معاف فرما دے",
            "Nabi ﷺ yeh dua maanga karte the.",
            "Sahih al-Bukhari", "Hadith 6398; Sahih Muslim 2719", "Sahih"
        ),

        // ---------------- Parents & Family ----------------
        d(
            "dua_parents_nuh", "Parents & Family", "Dua of Nuh (AS) for Parents",
            "Waalidain aur momineen ke liye.",
            "رَّبِّ اغْفِرْ لِي وَلِوَالِدَيَّ وَلِمَن دَخَلَ بَيْتِيَ مُؤْمِنًا وَلِلْمُؤْمِنِينَ وَالْمُؤْمِنَاتِ",
            "Rabbighfir lī wa liwālidayya wa liman dakhala baytiya mu'minan wa lil-mu'minīna wal-mu'mināt",
            "Aye mere Rab, mujhe, mere waalidain ko, jo imaan ki haalat mein mere ghar mein daakhil ho usko, aur sab momin mardon aur auraton ko bakhsh de.",
            "اے میرے رب! مجھے، میرے والدین کو، جو ایمان کی حالت میں میرے گھر میں داخل ہو اس کو اور سب مومن مردوں اور عورتوں کو بخش دے",
            "Hazrat Nooh (AS) ki dua.",
            "The Noble Quran", "Surah Nuh (71:28)", "Mutawatir (Quranic Verse)"
        ),
        d(
            "dua_shukr_parents", "Parents & Family", "Gratitude for Parents & Righteous Offspring",
            "Chaalees saal ki umr ki Qurani dua.",
            "رَبِّ أَوْزِعْنِي أَنْ أَشْكُرَ نِعْمَتَكَ الَّتِي أَنْعَمْتَ عَلَيَّ وَعَلَىٰ وَالِدَيَّ وَأَنْ أَعْمَلَ صَالِحًا تَرْضَاهُ وَأَصْلِحْ لِي فِي ذُرِّيَّتِي ۖ إِنِّي تُبْتُ إِلَيْكَ وَإِنِّي مِنَ الْمُسْلِمِينَ",
            "Rabbi awzi'nī an ashkura ni'matakal-latī an'amta 'alayya wa 'alā wālidayya wa an a'mala ṣāliḥan tarḍāhu wa aṣliḥ lī fī dhurriyyatī, innī tubtu ilayka wa innī minal-muslimīn",
            "Aye mere Rab, mujhe taufeeq de ke main Teri us nemat ka shukr ada karoon jo Tune mujh par aur mere waalidain par ki, aur aise naik amal karoon jinhein Tu pasand kare, aur meri aulaad ko bhi naik bana de. Main Teri taraf tauba karta hoon aur main farmabardaron mein se hoon.",
            "اے میرے رب! مجھے توفیق دے کہ میں تیری اس نعمت کا شکر ادا کروں جو تو نے مجھ پر اور میرے والدین پر کی، اور ایسے نیک عمل کروں جنہیں تو پسند کرے، اور میری اولاد کو بھی نیک بنا دے، میں تیری طرف توبہ کرتا ہوں اور میں فرمانبرداروں میں سے ہوں",
            "Shukr, naik amal aur aulaad ki islaah — sab ek dua mein.",
            "The Noble Quran", "Surah Al-Ahqaf (46:15)", "Mutawatir (Quranic Verse)"
        ),
        d(
            "dua_intimacy", "Parents & Family", "Before Marital Relations",
            "Mian biwi ke milaap se pehle.",
            "بِسْمِ اللَّهِ، اللَّهُمَّ جَنِّبْنَا الشَّيْطَانَ، وَجَنِّبِ الشَّيْطَانَ مَا رَزَقْتَنَا",
            "Bismillāh, Allāhumma jannibnash-shayṭān, wa jannibish-shayṭāna mā razaqtanā",
            "Allah ke naam se. Ya Allah, humein Shaitan se door rakh aur jo (aulaad) Tu humein de use bhi Shaitan se door rakh.",
            "اللہ کے نام سے، اے اللہ! ہمیں شیطان سے دور رکھ اور جو اولاد تو ہمیں دے اسے بھی شیطان سے دور رکھ",
            "Agar aulaad muqaddar ho to Shaitan use kabhi nuqsaan nahi pahunchayega.",
            "Sahih al-Bukhari", "Hadith 141; Sahih Muslim 1434", "Sahih"
        ),
        d(
            "dua_ibrahim_children_protection", "Parents & Family", "Protection of Home and Children",
            "Hazrat Ibrahim (AS) ki dua — shehar aur aulaad ki hifazat.",
            "رَبِّ اجْعَلْ هَٰذَا الْبَلَدَ آمِنًا وَاجْنُبْنِي وَبَنِيَّ أَن نَّعْبُدَ الْأَصْنَامَ",
            "Rabbij-'al hādhal-balada āminan wajnubnī wa baniyya an na'budal-aṣnām",
            "Aye mere Rab, is shehar ko aman wala bana de aur mujhe aur meri aulaad ko buton ki ibaadat se bacha.",
            "اے میرے رب! اس شہر کو امن والا بنا دے اور مجھے اور میری اولاد کو بتوں کی عبادت سے بچا",
            "Tauheed par qaayam rehne ki dua.",
            "The Noble Quran", "Surah Ibrahim (14:35)", "Mutawatir (Quranic Verse)"
        ),

        // ---------------- Food & Gratitude ----------------
        d(
            "dua_drinking_milk", "Food & Gratitude", "When Drinking Milk",
            "Doodh peene par.",
            "اللَّهُمَّ بَارِكْ لَنَا فِيهِ وَزِدْنَا مِنْهُ",
            "Allāhumma bārik lanā fīhi wa zidnā minh",
            "Ya Allah, hamare liye ismein barkat de aur humein aur zyada ata farma.",
            "اے اللہ! ہمارے لیے اس میں برکت دے اور ہمیں اور زیادہ عطا فرما",
            "Doodh khaane aur peene dono ki jagah kaafi hota hai.",
            "Jami` at-Tirmidhi", "Hadith 3455; Sunan Abi Dawud 3730", "Hasan"
        ),
        d(
            "dua_iftar_host", "Ramadan & Fasting", "When Breaking Fast with Others",
            "Kisi ke haan iftar karne par.",
            "أَفْطَرَ عِنْدَكُمُ الصَّائِمُونَ، وَأَكَلَ طَعَامَكُمُ الْأَبْرَارُ، وَصَلَّتْ عَلَيْكُمُ الْمَلَائِكَةُ",
            "Afṭara 'indakumuṣ-ṣā'imūn, wa akala ṭa'āmakumul-abrār, wa ṣallat 'alaykumul-malā'ikah",
            "Tumhare haan roze daar iftar karein, naik log tumhara khaana khaayein aur farishte tumhare liye dua karein.",
            "تمہارے ہاں روزہ دار افطار کریں، نیک لوگ تمہارا کھانا کھائیں اور فرشتے تمہارے لیے دعا کریں",
            "Nabi ﷺ ki mezbaan ke liye dua.",
            "Sunan Abi Dawud", "Hadith 3854", "Sahih"
        ),
        d(
            "dua_seeing_first_fruit", "Food & Gratitude", "Seeing the First Fruit",
            "Mausam ka pehla phal dekh kar.",
            "اللَّهُمَّ بَارِكْ لَنَا فِي ثَمَرِنَا، وَبَارِكْ لَنَا فِي مَدِينَتِنَا، وَبَارِكْ لَنَا فِي صَاعِنَا، وَبَارِكْ لَنَا فِي مُدِّنَا",
            "Allāhumma bārik lanā fī thamarinā, wa bārik lanā fī madīnatinā, wa bārik lanā fī ṣā'inā, wa bārik lanā fī muddinā",
            "Ya Allah, hamare phalon mein, hamare shehar mein, hamare saa' aur hamare mudd (naap tol) mein barkat de.",
            "اے اللہ! ہمارے پھلوں میں، ہمارے شہر میں، ہمارے صاع میں اور ہمارے مد میں برکت دے",
            "Rizq aur shehar mein barkat ki dua.",
            "Sahih Muslim", "Hadith 1373", "Sahih"
        ),
        d(
            "dua_good_news", "Food & Gratitude", "Upon Pleasing News",
            "Khushi ki khabar milne par (aur Sajda-e-Shukr).",
            "الْحَمْدُ لِلَّهِ الَّذِي بِنِعْمَتِهِ تَتِمُّ الصَّالِحَاتُ",
            "Al-ḥamdu lillāhil-ladhī bini'matihī tatimmuṣ-ṣāliḥāt",
            "Saari tareef us Allah ke liye jiski nemat se achhe kaam mukammal hote hain.",
            "تمام تعریفیں اس اللہ کے لیے جس کی نعمت سے اچھے کام مکمل ہوتے ہیں",
            "Nabi ﷺ pasandeeda cheez dekh kar yeh farmate.",
            "Sunan Ibn Majah", "Hadith 3803", "Hasan"
        ),
        d(
            "dua_looking_mirror", "Daily Life & Manners", "Looking in the Mirror",
            "Aaina dekhte waqt.",
            "اللَّهُمَّ كَمَا حَسَّنْتَ خَلْقِي فَحَسِّنْ خُلُقِي",
            "Allāhumma kamā ḥassanta khalqī fa-ḥassin khuluqī",
            "Ya Allah, jaise Tune meri soorat achhi banayi, waise hi mere akhlaaq bhi achhe bana de.",
            "اے اللہ! جیسے تو نے میری صورت اچھی بنائی ویسے ہی میرے اخلاق بھی اچھے بنا دے",
            "Nabi ﷺ yeh dua farmate the (aaine ki qaid ke baghair bhi saabit hai).",
            "Musnad Ahmad", "Hadith 3823, 24392", "Sahih (Sheikh Shu'aib Arna'ut)"
        ),

        // ---------------- Quranic Duas ----------------
        d(
            "dua_rabbana_taqabbal", "Quranic Duas", "Acceptance of Deeds",
            "Hazrat Ibrahim aur Ismail (AS) ne Kaaba ki tameer ke waqt maangi.",
            "رَبَّنَا تَقَبَّلْ مِنَّا ۖ إِنَّكَ أَنتَ السَّمِيعُ الْعَلِيمُ (١٢٧) وَتُبْ عَلَيْنَا ۖ إِنَّكَ أَنتَ التَّوَّابُ الرَّحِيمُ (١٢٨)",
            "Rabbanā taqabbal minnā, innaka Antas-Samī'ul-'Alīm, wa tub 'alaynā, innaka Antat-Tawwābur-Raḥīm",
            "Aye hamare Rab, humse qubool farma, beshak Tu sunne wala, jaanne wala hai. Aur hamari tauba qubool farma, beshak Tu tauba qubool karne wala, reham karne wala hai.",
            "اے ہمارے رب! ہم سے قبول فرما، بے شک تو سننے والا جاننے والا ہے، اور ہماری توبہ قبول فرما، بے شک تو توبہ قبول کرنے والا مہربان ہے",
            "Har naik amal ke baad qabooliyat ki dua.",
            "The Noble Quran", "Surah Al-Baqarah (2:127-128)", "Mutawatir (Quranic Verse)"
        ),
        d(
            "dua_sabr", "Quranic Duas", "For Patience and Steadfastness",
            "Mushkil aur muqaable ke waqt.",
            "رَبَّنَا أَفْرِغْ عَلَيْنَا صَبْرًا وَثَبِّتْ أَقْدَامَنَا وَانصُرْنَا عَلَى الْقَوْمِ الْكَافِرِينَ",
            "Rabbanā afrigh 'alaynā ṣabran wa thabbit aqdāmanā wanṣurnā 'alal-qawmil-kāfirīn",
            "Aye hamare Rab, hum par sabr undel de, hamare qadam jama de aur kaafir qaum ke muqaable mein hamari madad farma.",
            "اے ہمارے رب! ہم پر صبر انڈیل دے، ہمارے قدم جما دے اور کافر قوم کے مقابلے میں ہماری مدد فرما",
            "Talut ke lashkar ki dua.",
            "The Noble Quran", "Surah Al-Baqarah (2:250)", "Mutawatir (Quranic Verse)"
        ),
        d(
            "dua_rabbana_innana_amanna", "Quranic Duas", "Forgiveness and Protection from Fire",
            "Qurani dua.",
            "رَبَّنَا إِنَّنَا آمَنَّا فَاغْفِرْ لَنَا ذُنُوبَنَا وَقِنَا عَذَابَ النَّارِ",
            "Rabbanā innanā āmannā faghfir lanā dhunūbanā wa qinā 'adhāban-nār",
            "Aye hamare Rab, hum imaan laaye, pas hamare gunaah maaf farma aur humein aag ke azaab se bacha.",
            "اے ہمارے رب! ہم ایمان لائے، پس ہمارے گناہ معاف فرما اور ہمیں آگ کے عذاب سے بچا",
            "Muttaqeen ki sifat mein bayan ki gayi dua.",
            "The Noble Quran", "Surah Aal-e-Imran (3:16)", "Mutawatir (Quranic Verse)"
        ),
        d(
            "dua_rabbana_hab_lana_rahmah", "Quranic Duas", "Guidance in Our Affair (Ashab al-Kahf)",
            "Ashaab-e-Kahf ki dua.",
            "رَبَّنَا آتِنَا مِن لَّدُنكَ رَحْمَةً وَهَيِّئْ لَنَا مِنْ أَمْرِنَا رَشَدًا",
            "Rabbanā ātinā mil-ladunka raḥmatan wa hayyi' lanā min amrinā rashadā",
            "Aye hamare Rab, humein apne paas se rehmat ata farma aur hamare kaam mein hamare liye durusti ka saamaan kar de.",
            "اے ہمارے رب! ہمیں اپنے پاس سے رحمت عطا فرما اور ہمارے کام میں ہمارے لیے درستی کا سامان کر دے",
            "Faislon aur mushkil haalaat mein rehnumai.",
            "The Noble Quran", "Surah Al-Kahf (18:10)", "Mutawatir (Quranic Verse)"
        ),
        d(
            "dua_musa_faqir", "Quranic Duas", "Dua of Musa (AS) for Provision",
            "Rizq aur khair ki talab — Madyan mein Hazrat Musa (AS) ki dua.",
            "رَبِّ إِنِّي لِمَا أَنزَلْتَ إِلَيَّ مِنْ خَيْرٍ فَقِيرٌ",
            "Rabbi innī limā anzalta ilayya min khayrin faqīr",
            "Aye mere Rab, jo bhi bhalai Tu mujh par utaare, main uska mohtaaj hoon.",
            "اے میرے رب! جو بھلائی بھی تو مجھ پر اتارے میں اس کا محتاج ہوں",
            "Is dua ke baad Allah ne Musa (AS) ko ghar, rozgaar aur biwi ata ki.",
            "The Noble Quran", "Surah Al-Qasas (28:24)", "Mutawatir (Quranic Verse)"
        ),
        d(
            "dua_ayyub", "Quranic Duas", "Dua of Ayyub (AS) in Illness",
            "Bimaari aur takleef mein.",
            "أَنِّي مَسَّنِيَ الضُّرُّ وَأَنتَ أَرْحَمُ الرَّاحِمِينَ",
            "Annī massaniyaḍ-ḍurru wa Anta arḥamur-rāḥimīn",
            "(Aye mere Rab) mujhe takleef pahunchi hai aur Tu sab reham karne walon se zyada reham karne wala hai.",
            "(اے میرے رب!) مجھے تکلیف پہنچی ہے اور تو سب رحم کرنے والوں سے زیادہ رحم کرنے والا ہے",
            "Allah ne Hazrat Ayyub (AS) ki takleef door farma di.",
            "The Noble Quran", "Surah Al-Anbiya (21:83)", "Mutawatir (Quranic Verse)"
        ),
        d(
            "dua_husn_khatimah", "Quranic Duas", "Die as a Muslim (Yusuf AS)",
            "Achhe khaatme ki dua.",
            "فَاطِرَ السَّمَاوَاتِ وَالْأَرْضِ أَنتَ وَلِيِّي فِي الدُّنْيَا وَالْآخِرَةِ ۖ تَوَفَّنِي مُسْلِمًا وَأَلْحِقْنِي بِالصَّالِحِينَ",
            "Fāṭiras-samāwāti wal-arḍ, Anta waliyyī fid-dunyā wal-ākhirah, tawaffanī musliman wa alḥiqnī biṣ-ṣāliḥīn",
            "Aye aasmaanon aur zameen ke paida karne wale, Tu hi dunya aur aakhirat mein mera kaarsaaz hai. Mujhe Islam ki haalat mein maut de aur naik logon ke saath mila de.",
            "اے آسمانوں اور زمین کے پیدا کرنے والے! تو ہی دنیا اور آخرت میں میرا کارساز ہے، مجھے اسلام کی حالت میں موت دے اور نیک لوگوں کے ساتھ ملا دے",
            "Hazrat Yusuf (AS) ki dua.",
            "The Noble Quran", "Surah Yusuf (12:101)", "Mutawatir (Quranic Verse)"
        ),
        d(
            "dua_shaitan_hamazat", "Quranic Duas", "Refuge from Devils' Whispers",
            "Shaitaani waswason se panaah.",
            "رَّبِّ أَعُوذُ بِكَ مِنْ هَمَزَاتِ الشَّيَاطِينِ (٩٧) وَأَعُوذُ بِكَ رَبِّ أَن يَحْضُرُونِ (٩٨)",
            "Rabbi a'ūdhu bika min hamazātish-shayāṭīn, wa a'ūdhu bika Rabbi an yaḥḍurūn",
            "Aye mere Rab, main shaitanon ke waswason se Teri panaah maangta hoon, aur aye mere Rab, is se bhi Teri panaah ke woh mere paas aayein.",
            "اے میرے رب! میں شیطانوں کے وسوسوں سے تیری پناہ مانگتا ہوں، اور اے میرے رب! اس سے بھی تیری پناہ کہ وہ میرے پاس آئیں",
            "Qurani panaah ki dua.",
            "The Noble Quran", "Surah Al-Mu'minun (23:97-98)", "Mutawatir (Quranic Verse)"
        ),

        // ---------------- Protection & Healing ----------------
        d(
            "dua_bad_character", "Protection & Healing", "Refuge from Bad Character",
            "Bure akhlaaq, aamaal aur khwahishaat se panaah.",
            "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ مُنْكَرَاتِ الْأَخْلَاقِ، وَالْأَعْمَالِ، وَالْأَهْوَاءِ",
            "Allāhumma innī a'ūdhu bika min munkarātil-akhlāq, wal-a'māl, wal-ahwā'",
            "Ya Allah, main bure akhlaaq, bure aamaal aur buri khwahishaat se Teri panaah maangta hoon.",
            "اے اللہ! میں برے اخلاق، برے اعمال اور بری خواہشات سے تیری پناہ مانگتا ہوں",
            "Nabi ﷺ ki panaah ki dua.",
            "Jami` at-Tirmidhi", "Hadith 3591", "Sahih"
        ),
        d(
            "dua_zawal_nimah", "Protection & Healing", "Refuge from Loss of Blessings",
            "Nemat chhin jaane aur achaanak musibat se panaah.",
            "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ زَوَالِ نِعْمَتِكَ، وَتَحَوُّلِ عَافِيَتِكَ، وَفُجَاءَةِ نِقْمَتِكَ، وَجَمِيعِ سَخَطِكَ",
            "Allāhumma innī a'ūdhu bika min zawāli ni'matik, wa taḥawwuli 'āfiyatik, wa fujā'ati niqmatik, wa jamī'i sakhaṭik",
            "Ya Allah, main Teri panaah maangta hoon Teri nemat ke zawaal se, Teri di hui aafiyat ke badal jaane se, Tere achaanak azaab se aur Teri har naraazgi se.",
            "اے اللہ! میں تیری پناہ مانگتا ہوں تیری نعمت کے زوال سے، تیری عافیت کے بدل جانے سے، تیرے اچانک عذاب سے اور تیری ہر ناراضگی سے",
            "Nabi ﷺ ki dua.",
            "Sahih Muslim", "Hadith 2739", "Sahih"
        ),
        d(
            "dua_useless_knowledge", "Protection & Healing", "Refuge from Unbeneficial Knowledge",
            "Be-faaida ilm aur be-khauf dil se panaah.",
            "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ عِلْمٍ لَا يَنْفَعُ، وَمِنْ قَلْبٍ لَا يَخْشَعُ، وَمِنْ نَفْسٍ لَا تَشْبَعُ، وَمِنْ دَعْوَةٍ لَا يُسْتَجَابُ لَهَا",
            "Allāhumma innī a'ūdhu bika min 'ilmin lā yanfa', wa min qalbin lā yakhsha', wa min nafsin lā tashba', wa min da'watin lā yustajābu lahā",
            "Ya Allah, main Teri panaah maangta hoon aise ilm se jo faaida na de, aise dil se jo na dare, aise nafs se jo sair na ho, aur aisi dua se jo qubool na ho.",
            "اے اللہ! میں تیری پناہ مانگتا ہوں ایسے علم سے جو نفع نہ دے، ایسے دل سے جو نہ ڈرے، ایسے نفس سے جو سیر نہ ہو، اور ایسی دعا سے جو قبول نہ ہو",
            "Nabi ﷺ ki panaah ki dua.",
            "Sahih Muslim", "Hadith 2722", "Sahih"
        ),
        d(
            "dua_beneficial_knowledge", "Protection & Healing", "Beneficial Knowledge & Halal Rizq",
            "Fajr ki namaz ke baad.",
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وَعَمَلًا مُتَقَبَّلًا",
            "Allāhumma innī as'aluka 'ilman nāfi'ā, wa rizqan ṭayyibā, wa 'amalan mutaqabbalā",
            "Ya Allah, main Tujh se nafa dene wala ilm, paakeeza rizq aur qubool hone wala amal maangta hoon.",
            "اے اللہ! میں تجھ سے نفع دینے والا علم، پاکیزہ رزق اور قبول ہونے والا عمل مانگتا ہوں",
            "Nabi ﷺ Fajr ke salaam ke baad yeh dua farmate.",
            "Sunan Ibn Majah", "Hadith 925", "Sahih"
        ),
        d(
            "dua_leprosy_diseases", "Protection & Healing", "Refuge from Serious Diseases",
            "Buri bimaariyon se hifazat.",
            "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْبَرَصِ، وَالْجُنُونِ، وَالْجُذَامِ، وَمِنْ سَيِّئِ الْأَسْقَامِ",
            "Allāhumma innī a'ūdhu bika minal-baraṣ, wal-junūn, wal-judhām, wa min sayyi'il-asqām",
            "Ya Allah, main Teri panaah maangta hoon safed daagh (bars), deewaangi, kodh aur tamaam buri bimaariyon se.",
            "اے اللہ! میں تیری پناہ مانگتا ہوں برص سے، دیوانگی سے، کوڑھ سے اور تمام بری بیماریوں سے",
            "Sehat ki hifazat ki masnoon dua.",
            "Sunan Abi Dawud", "Hadith 1554; Sunan an-Nasa'i 5493", "Sahih"
        ),
        d(
            "dua_fear_people", "Protection & Healing", "Fear of Enemies or Oppressors",
            "Kisi qaum ya shakhs ke shar ka khauf ho.",
            "اللَّهُمَّ إِنَّا نَجْعَلُكَ فِي نُحُورِهِمْ، وَنَعُوذُ بِكَ مِنْ شُرُورِهِمْ",
            "Allāhumma innā naj'aluka fī nuḥūrihim, wa na'ūdhu bika min shurūrihim",
            "Ya Allah, hum Tujhe unke muqaable mein karte hain aur unke shar se Teri panaah maangte hain.",
            "اے اللہ! ہم تجھے ان کے مقابلے میں کرتے ہیں اور ان کی شرارتوں سے تیری پناہ مانگتے ہیں",
            "Nabi ﷺ kisi qaum se khatra mehsoos karte to yeh farmate.",
            "Sunan Abi Dawud", "Hadith 1537", "Sahih"
        ),
        d(
            "dua_shirk_protection", "Protection & Healing", "Protection from Hidden Shirk",
            "Riya aur chhupe shirk se bachne ke liye.",
            "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ أَنْ أُشْرِكَ بِكَ وَأَنَا أَعْلَمُ، وَأَسْتَغْفِرُكَ لِمَا لَا أَعْلَمُ",
            "Allāhumma innī a'ūdhu bika an ushrika bika wa anā a'lam, wa astaghfiruka limā lā a'lam",
            "Ya Allah, main Teri panaah maangta hoon is se ke jaante boojhte Tere saath shirk karoon, aur jo anjaane mein ho uski maafi maangta hoon.",
            "اے اللہ! میں تیری پناہ مانگتا ہوں اس سے کہ جانتے بوجھتے تیرے ساتھ شرک کروں، اور جو انجانے میں ہو اس کی معافی مانگتا ہوں",
            "Shirk-e-khafi (chhupa shirk) chiunti ki chaal se bhi zyada chhupa hai.",
            "Al-Adab al-Mufrad (Bukhari)", "Hadith 716", "Sahih (Sheikh Albani)"
        ),

        // ---------------- Weather & Nature ----------------
        d(
            "dua_rain", "Weather & Nature", "When It Rains",
            "Baarish hote waqt.",
            "اللَّهُمَّ صَيِّبًا نَافِعًا",
            "Allāhumma ṣayyiban nāfi'ā",
            "Ya Allah, nafa dene wali baarish barsa.",
            "اے اللہ! نفع دینے والی بارش برسا",
            "Baarish dua ki qabooliyat ka waqt hai.",
            "Sahih al-Bukhari", "Hadith 1032", "Sahih"
        ),
        d(
            "dua_after_rain", "Weather & Nature", "After Rain",
            "Baarish ke baad.",
            "مُطِرْنَا بِفَضْلِ اللَّهِ وَرَحْمَتِهِ",
            "Muṭirnā bifaḍlillāhi wa raḥmatih",
            "Hum par Allah ke fazl aur uski rehmat se baarish hui.",
            "ہم پر اللہ کے فضل اور اس کی رحمت سے بارش ہوئی",
            "Yeh kehne wala Allah par imaan rakhne wala hai.",
            "Sahih al-Bukhari", "Hadith 846; Sahih Muslim 71", "Sahih"
        ),
        d(
            "dua_istisqa", "Weather & Nature", "Asking for Rain (Istisqa)",
            "Baarish na hone par.",
            "اللَّهُمَّ أَغِثْنَا، اللَّهُمَّ أَغِثْنَا، اللَّهُمَّ أَغِثْنَا",
            "Allāhumma aghithnā, Allāhumma aghithnā, Allāhumma aghithnā",
            "Ya Allah, hum par baarish barsa (teen martaba).",
            "اے اللہ! ہم پر بارش برسا (تین بار)",
            "Nabi ﷺ ne Jumu'ah ke khutbe mein yeh dua ki aur baarish hui.",
            "Sahih al-Bukhari", "Hadith 1014; Sahih Muslim 897", "Sahih", 3
        ),
        d(
            "dua_heavy_rain", "Weather & Nature", "Excessive Rain",
            "Baarish zyada ho jaaye to.",
            "اللَّهُمَّ حَوَالَيْنَا وَلَا عَلَيْنَا",
            "Allāhumma ḥawālaynā wa lā 'alaynā",
            "Ya Allah, hamare aas paas (barsa), hum par nahi.",
            "اے اللہ! ہمارے آس پاس (برسا)، ہم پر نہیں",
            "Nabi ﷺ ki dua se baadal hat gaye.",
            "Sahih al-Bukhari", "Hadith 1013; Sahih Muslim 897", "Sahih"
        ),
        d(
            "dua_wind", "Weather & Nature", "When the Wind Blows",
            "Tez hawa/aandhi chalne par.",
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ خَيْرَهَا، وَخَيْرَ مَا فِيهَا، وَخَيْرَ مَا أُرْسِلَتْ بِهِ، وَأَعُوذُ بِكَ مِنْ شَرِّهَا، وَشَرِّ مَا فِيهَا، وَشَرِّ مَا أُرْسِلَتْ بِهِ",
            "Allāhumma innī as'aluka khayrahā, wa khayra mā fīhā, wa khayra mā ursilat bih, wa a'ūdhu bika min sharrihā, wa sharri mā fīhā, wa sharri mā ursilat bih",
            "Ya Allah, main Tujh se iski bhalai, jo is mein hai uski bhalai aur jis ke saath yeh bheji gayi uski bhalai maangta hoon, aur iske shar, jo is mein hai uske shar aur jis ke saath yeh bheji gayi uske shar se Teri panaah maangta hoon.",
            "اے اللہ! میں تجھ سے اس کی بھلائی، جو اس میں ہے اس کی بھلائی اور جس کے ساتھ یہ بھیجی گئی اس کی بھلائی مانگتا ہوں، اور اس کے شر، جو اس میں ہے اس کے شر اور جس کے ساتھ یہ بھیجی گئی اس کے شر سے تیری پناہ مانگتا ہوں",
            "Nabi ﷺ aandhi ke waqt yeh dua farmate.",
            "Sahih Muslim", "Hadith 899", "Sahih"
        ),
        d(
            "dua_thunder", "Weather & Nature", "Hearing Thunder",
            "Baadal ki garaj sun kar — Hazrat Abdullah bin Zubair (RA) ka amal.",
            "سُبْحَانَ الَّذِي يُسَبِّحُ الرَّعْدُ بِحَمْدِهِ وَالْمَلَائِكَةُ مِنْ خِيفَتِهِ",
            "Subḥānal-ladhī yusabbiḥur-ra'du biḥamdihī wal-malā'ikatu min khīfatih",
            "Paak hai woh zaat jiski tasbeeh garaj uski tareef ke saath karti hai aur farishte uske khauf se.",
            "پاک ہے وہ ذات جس کی تسبیح گرج اس کی تعریف کے ساتھ کرتی ہے اور فرشتے اس کے خوف سے",
            "Surah Ra'd (13:13) se maakhooz.",
            "Muwatta Imam Malik", "Kitab al-Kalam, Hadith 26; Al-Adab al-Mufrad 723", "Mauqoof Sahih"
        ),
        d(
            "dua_new_moon", "Weather & Nature", "Seeing the New Moon",
            "Naya chaand dekh kar.",
            "اللَّهُمَّ أَهِلَّهُ عَلَيْنَا بِالْيُمْنِ وَالْإِيمَانِ، وَالسَّلَامَةِ وَالْإِسْلَامِ، رَبِّي وَرَبُّكَ اللَّهُ",
            "Allāhumma ahillahū 'alaynā bil-yumni wal-īmān, was-salāmati wal-Islām, Rabbī wa Rabbukallāh",
            "Ya Allah, is chaand ko hum par barkat, imaan, salaamati aur Islam ke saath taloo' farma. (Aye chaand) mera aur tera Rab Allah hai.",
            "اے اللہ! اس چاند کو ہم پر برکت، ایمان، سلامتی اور اسلام کے ساتھ طلوع فرما، (اے چاند) میرا اور تیرا رب اللہ ہے",
            "Har naye mahine ki masnoon dua.",
            "Jami` at-Tirmidhi", "Hadith 3451", "Hasan"
        ),

        // ---------------- Death & Janaza ----------------
        d(
            "dua_janaza_adult", "Death & Janaza", "Janaza Prayer (Adult)",
            "Namaz-e-Janaza ki teesri takbeer ke baad.",
            "اللَّهُمَّ اغْفِرْ لِحَيِّنَا وَمَيِّتِنَا، وَشَاهِدِنَا وَغَائِبِنَا، وَصَغِيرِنَا وَكَبِيرِنَا، وَذَكَرِنَا وَأُنْثَانَا، اللَّهُمَّ مَنْ أَحْيَيْتَهُ مِنَّا فَأَحْيِهِ عَلَى الْإِسْلَامِ، وَمَنْ تَوَفَّيْتَهُ مِنَّا فَتَوَفَّهُ عَلَى الْإِيمَانِ",
            "Allāhummaghfir liḥayyinā wa mayyitinā, wa shāhidinā wa ghā'ibinā, wa ṣaghīrinā wa kabīrinā, wa dhakarinā wa unthānā. Allāhumma man aḥyaytahū minnā fa-aḥyihī 'alal-Islām, wa man tawaffaytahū minnā fatawaffahū 'alal-īmān",
            "Ya Allah, hamare zindon aur murdon, haazir aur ghaib, chhoton aur badon, mardon aur auraton sab ko bakhsh de. Ya Allah, hum mein se jise zinda rakhe use Islam par zinda rakh, aur jise maut de use imaan par maut de.",
            "اے اللہ! ہمارے زندوں اور مردوں، حاضر اور غائب، چھوٹوں اور بڑوں، مردوں اور عورتوں سب کو بخش دے۔ اے اللہ! ہم میں سے جسے زندہ رکھے اسے اسلام پر زندہ رکھ اور جسے موت دے اسے ایمان پر موت دے",
            "Baaligh mayyit ki masnoon dua.",
            "Jami` at-Tirmidhi", "Hadith 1024; Sunan Abi Dawud 3201", "Sahih"
        ),
        d(
            "dua_janaza_child", "Death & Janaza", "Janaza Prayer (Child)",
            "Na-baaligh bache ki namaz-e-janaza mein (ladki ho to 'ij'alhā').",
            "اللَّهُمَّ اجْعَلْهُ لَنَا فَرَطًا، وَاجْعَلْهُ لَنَا أَجْرًا وَذُخْرًا، وَاجْعَلْهُ لَنَا شَافِعًا وَمُشَفَّعًا",
            "Allāhummaj-'alhu lanā faraṭā, waj-'alhu lanā ajran wa dhukhrā, waj-'alhu lanā shāfi'an wa mushaffa'ā",
            "Ya Allah, ise hamare liye aage jaane wala (mezbaan) bana, ise hamare liye ajr aur zakheera bana, aur ise hamare liye sifarish karne wala aur jiski sifarish qubool ho, bana.",
            "اے اللہ! اسے ہمارے لیے آگے جانے والا بنا، اسے ہمارے لیے اجر اور ذخیرہ بنا، اور اسے ہمارے لیے سفارش کرنے والا اور مقبول سفارش والا بنا",
            "Ahnaaf ke haan ma'mool masnoon dua.",
            "Sahih al-Bukhari (ta'leeqan)", "Kitab al-Jana'iz, Baab 65 (Hazrat Hasan Basri se)", "Mauqoof (Aasaar se saabit)"
        ),
        d(
            "dua_mayyit_awf", "Death & Janaza", "Detailed Dua for the Deceased",
            "Janaze mein ya mayyit ke liye.",
            "اللَّهُمَّ اغْفِرْ لَهُ وَارْحَمْهُ، وَعَافِهِ وَاعْفُ عَنْهُ، وَأَكْرِمْ نُزُلَهُ، وَوَسِّعْ مُدْخَلَهُ، وَاغْسِلْهُ بِالْمَاءِ وَالثَّلْجِ وَالْبَرَدِ، وَنَقِّهِ مِنَ الْخَطَايَا كَمَا نَقَّيْتَ الثَّوْبَ الْأَبْيَضَ مِنَ الدَّنَسِ",
            "Allāhummaghfir lahū warḥamhu, wa 'āfihi wa'fu 'anh, wa akrim nuzulah, wa wassi' mudkhalah, waghsilhu bil-mā'i wath-thalji wal-barad, wa naqqihī minal-khaṭāyā kamā naqqaytath-thawbal-abyaḍa minad-danas",
            "Ya Allah, ise bakhsh de, is par reham farma, ise aafiyat de, ise maaf farma, iski mehmaani izzat wali kar, iski qabr kushaada kar, ise paani, barf aur olon se dho de, aur ise gunaahon se aise saaf kar de jaise safed kapda mail se saaf kiya jaata hai.",
            "اے اللہ! اسے بخش دے، اس پر رحم فرما، اسے عافیت دے، اسے معاف فرما، اس کی مہمانی عزت والی کر، اس کی قبر کشادہ کر، اسے پانی، برف اور اولوں سے دھو دے، اور اسے گناہوں سے ایسے صاف کر دے جیسے سفید کپڑا میل سے صاف کیا جاتا ہے",
            "Hazrat Auf bin Malik (RA) ne tamanna ki ke kaash yeh mayyit main hota.",
            "Sahih Muslim", "Hadith 963", "Sahih"
        ),
        d(
            "dua_visiting_graves", "Death & Janaza", "Visiting the Graves",
            "Qabristan mein daakhil hote waqt.",
            "السَّلَامُ عَلَيْكُمْ أَهْلَ الدِّيَارِ مِنَ الْمُؤْمِنِينَ وَالْمُسْلِمِينَ، وَإِنَّا إِنْ شَاءَ اللَّهُ بِكُمْ لَلَاحِقُونَ، أَسْأَلُ اللَّهَ لَنَا وَلَكُمُ الْعَافِيَةَ",
            "As-salāmu 'alaykum ahlad-diyāri minal-mu'minīna wal-muslimīn, wa innā in shā' Allāhu bikum la-lāḥiqūn, as'alullāha lanā wa lakumul-'āfiyah",
            "Aye momin aur muslim ghar walo, tum par salaam ho. Hum bhi in sha Allah tumse milne wale hain. Main Allah se apne aur tumhare liye aafiyat maangta hoon.",
            "اے مومن اور مسلمان گھر والو! تم پر سلام ہو، ہم بھی ان شاء اللہ تم سے ملنے والے ہیں، میں اللہ سے اپنے اور تمہارے لیے عافیت مانگتا ہوں",
            "Qabron ki ziyarat maut aur aakhirat yaad dilaati hai.",
            "Sahih Muslim", "Hadith 975", "Sahih"
        ),
        d(
            "dua_closing_eyes", "Death & Janaza", "Upon Closing the Eyes of the Deceased",
            "Mayyit ki aankhein band karte waqt (naam le kar).",
            "اللَّهُمَّ اغْفِرْ لِفُلَانٍ، وَارْفَعْ دَرَجَتَهُ فِي الْمَهْدِيِّينَ، وَاخْلُفْهُ فِي عَقِبِهِ فِي الْغَابِرِينَ، وَاغْفِرْ لَنَا وَلَهُ يَا رَبَّ الْعَالَمِينَ، وَافْسَحْ لَهُ فِي قَبْرِهِ، وَنَوِّرْ لَهُ فِيهِ",
            "Allāhummaghfir li-(fulān), warfa' darajatahū fil-mahdiyyīn, wakhlufhu fī 'aqibihī fil-ghābirīn, waghfir lanā wa lahū yā Rabbal-'ālamīn, wafsaḥ lahū fī qabrihī, wa nawwir lahū fīh",
            "Ya Allah, (fulaan) ko bakhsh de, hidayat yafta logon mein iska darja buland kar, iske peechhe rehne walon mein iska jaanasheen ban ja, aye Rabbul-Aalameen humein aur ise bakhsh de, iski qabr kushaada kar aur usmein roshni kar de.",
            "اے اللہ! (فلاں) کو بخش دے، ہدایت یافتہ لوگوں میں اس کا درجہ بلند کر، اس کے پیچھے رہنے والوں میں اس کا جانشین بن جا، اے رب العالمین ہمیں اور اسے بخش دے، اس کی قبر کشادہ کر اور اس میں روشنی کر دے",
            "Nabi ﷺ ne Hazrat Abu Salama (RA) ke liye yeh dua farmayi.",
            "Sahih Muslim", "Hadith 920", "Sahih"
        ),
        d(
            "dua_condolence", "Death & Janaza", "Condolence (Ta'ziyah)",
            "Kisi ke intiqaal par ghar walon se ta'ziyat.",
            "إِنَّ لِلَّهِ مَا أَخَذَ، وَلَهُ مَا أَعْطَىٰ، وَكُلُّ شَيْءٍ عِنْدَهُ بِأَجَلٍ مُسَمًّى، فَلْتَصْبِرْ وَلْتَحْتَسِبْ",
            "Inna lillāhi mā akhadh, wa lahū mā a'ṭā, wa kullu shay'in 'indahū bi-ajalin musammā, faltaṣbir waltaḥtasib",
            "Beshak Allah hi ka tha jo usne le liya aur usi ka hai jo usne diya, aur uske haan har cheez ka ek muqarrar waqt hai, pas sabr karo aur sawab ki ummeed rakho.",
            "بے شک اللہ ہی کا تھا جو اس نے لے لیا اور اسی کا ہے جو اس نے دیا، اور اس کے ہاں ہر چیز کا ایک مقررہ وقت ہے، پس صبر کرو اور ثواب کی امید رکھو",
            "Nabi ﷺ ne apni saahibzaadi ko yeh paighaam bheja.",
            "Sahih al-Bukhari", "Hadith 1284; Sahih Muslim 923", "Sahih"
        ),
        d(
            "dua_placing_in_grave", "Death & Janaza", "Placing the Deceased in the Grave",
            "Mayyit ko qabr mein utaarte waqt.",
            "بِسْمِ اللَّهِ وَعَلَىٰ مِلَّةِ رَسُولِ اللَّهِ",
            "Bismillāhi wa 'alā millati Rasūlillāh",
            "Allah ke naam se aur Rasoolullah ﷺ ki millat par.",
            "اللہ کے نام سے اور رسول اللہ ﷺ کی ملت پر",
            "Tadfeen ki masnoon dua.",
            "Sunan Abi Dawud", "Hadith 3213; Jami` at-Tirmidhi 1046", "Sahih"
        ),

        // ---------------- Daily Life & Manners ----------------
        d(
            "dua_sneezing", "Daily Life & Manners", "Sneezing",
            "Chheenk aane par; sunne wala 'Yarhamukallah' kahe, phir chheenkne wala jawab de.",
            "الْحَمْدُ لِلَّهِ — يَرْحَمُكَ اللَّهُ — يَهْدِيكُمُ اللَّهُ وَيُصْلِحُ بَالَكُمْ",
            "Al-ḥamdu lillāh — Yarḥamukallāh — Yahdīkumullāhu wa yuṣliḥu bālakum",
            "Saari tareef Allah ke liye. — Allah tum par reham kare. — Allah tumhein hidayat de aur tumhara haal durust kare.",
            "الحمد للہ — اللہ تم پر رحم کرے — اللہ تمہیں ہدایت دے اور تمہارا حال درست کرے",
            "Musalman ke musalman par haqooq mein se hai.",
            "Sahih al-Bukhari", "Hadith 6224", "Sahih"
        ),
        d(
            "dua_toilet_enter", "Daily Life & Manners", "Entering the Toilet",
            "Baitul-khala mein baayan paon rakhne se pehle.",
            "بِسْمِ اللَّهِ، اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْخُبُثِ وَالْخَبَائِثِ",
            "Bismillāh, Allāhumma innī a'ūdhu bika minal-khubuthi wal-khabā'ith",
            "Allah ke naam se. Ya Allah, main naapaak jinnon (mard aur aurat) se Teri panaah maangta hoon.",
            "اللہ کے نام سے، اے اللہ! میں ناپاک جنوں اور جنیوں سے تیری پناہ مانگتا ہوں",
            "Bismillah jinnaat aur insaan ke parde ke darmiyan aad ban jaati hai.",
            "Sahih al-Bukhari", "Hadith 142; Jami` at-Tirmidhi 606", "Sahih"
        ),
        d(
            "dua_toilet_exit", "Daily Life & Manners", "Leaving the Toilet",
            "Baitul-khala se daayan paon bahar nikaalte waqt.",
            "غُفْرَانَكَ",
            "Ghufrānak",
            "(Ya Allah) main Teri maghfirat chahta hoon.",
            "(اے اللہ!) میں تیری مغفرت چاہتا ہوں",
            "Nabi ﷺ baitul-khala se nikal kar yeh farmate.",
            "Sunan Abi Dawud", "Hadith 30; Jami` at-Tirmidhi 7", "Sahih"
        ),
        d(
            "dua_thanking", "Daily Life & Manners", "Thanking Someone",
            "Koi ehsaan kare to.",
            "جَزَاكَ اللَّهُ خَيْرًا",
            "Jazākallāhu khayrā",
            "Allah tumhein behtareen badla de.",
            "اللہ تمہیں بہترین بدلہ دے",
            "Jisne yeh kaha usne tareef ka haq ada kar diya.",
            "Jami` at-Tirmidhi", "Hadith 2035", "Hasan Sahih (Imam Tirmizi)"
        ),
        d(
            "dua_love_for_allah", "Daily Life & Manners", "When Someone Says 'I Love You for Allah'",
            "Koi kahe 'main tum se Allah ke liye muhabbat karta hoon'.",
            "أَحَبَّكَ الَّذِي أَحْبَبْتَنِي لَهُ",
            "Aḥabbakal-ladhī aḥbabtanī lah",
            "Woh (Allah) tum se muhabbat kare jiske liye tumne mujh se muhabbat ki.",
            "وہ (اللہ) تم سے محبت کرے جس کے لیے تم نے مجھ سے محبت کی",
            "Allah ke liye muhabbat ki masnoon jawabi dua.",
            "Sunan Abi Dawud", "Hadith 5125", "Hasan"
        ),
        d(
            "dua_seeing_muslim_smile", "Daily Life & Manners", "Greeting (Salaam)",
            "Musalman se milte waqt.",
            "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ",
            "As-salāmu 'alaykum wa raḥmatullāhi wa barakātuh",
            "Tum par salaamati, Allah ki rehmat aur uski barkatein hon.",
            "تم پر سلامتی ہو اور اللہ کی رحمت اور اس کی برکتیں",
            "Poora salaam kehne par tees nekiyan milti hain.",
            "Sunan Abi Dawud", "Hadith 5195; Jami` at-Tirmidhi 2689", "Hasan / Sahih"
        ),
        d(
            "dua_dressing", "Daily Life & Manners", "Getting Dressed",
            "Kapde pehente waqt.",
            "الْحَمْدُ لِلَّهِ الَّذِي كَسَانِي هَٰذَا الثَّوْبَ وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
            "Al-ḥamdu lillāhil-ladhī kasānī hādhath-thawba wa razaqanīhi min ghayri ḥawlim-minnī wa lā quwwah",
            "Saari tareef Allah ke liye jisne mujhe yeh kapda pehnaya aur meri kisi taaqat o quwwat ke baghair ata kiya.",
            "تمام تعریفیں اللہ کے لیے جس نے مجھے یہ کپڑا پہنایا اور میری کسی طاقت و قوت کے بغیر عطا کیا",
            "Pichhle gunaahon ki maafi ki basharat.",
            "Sunan Abi Dawud", "Hadith 4023", "Hasan"
        ),
        d(
            "dua_rooster_donkey", "Daily Life & Manners", "Hearing a Rooster or Donkey",
            "Murgh ki awaaz par Allah ka fazl maangein, gadhe ke rengne par Shaitan se panaah.",
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ — أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ",
            "Allāhumma innī as'aluka min faḍlik — A'ūdhu billāhi minash-shayṭānir-rajīm",
            "Ya Allah, main Tera fazl maangta hoon (murgh). — Main Allah ki panaah maangta hoon mardood Shaitan se (gadha).",
            "اے اللہ! میں تیرا فضل مانگتا ہوں (مرغ) — میں اللہ کی پناہ مانگتا ہوں شیطان مردود سے (گدھا)",
            "Murgh farishta dekhta hai aur gadha Shaitan.",
            "Sahih al-Bukhari", "Hadith 3303; Sahih Muslim 2729", "Sahih"
        ),
        d(
            "dua_something_liked", "Daily Life & Manners", "Seeing Something Admirable (Against Evil Eye)",
            "Kisi ki achhi cheez dekh kar barkat ki dua dein.",
            "مَا شَاءَ اللَّهُ، اللَّهُمَّ بَارِكْ عَلَيْهِ",
            "Mā shā' Allāh, Allāhumma bārik 'alayh",
            "Jo Allah ne chaaha. Ya Allah, ismein barkat de.",
            "جو اللہ نے چاہا، اے اللہ! اس میں برکت دے",
            "Barkat ki dua dene se nazar nahi lagti.",
            "Sunan Ibn Majah", "Hadith 3509; Surah Al-Kahf (18:39)", "Sahih"
        ),
        d(
            "dua_sell_buy_debt_repaid", "Daily Life & Manners", "When Repaying a Debt",
            "Qarz lautaate waqt qarz dene wale ke liye.",
            "بَارَكَ اللَّهُ لَكَ فِي أَهْلِكَ وَمَالِكَ",
            "Bārakallāhu laka fī ahlika wa mālik",
            "Allah tumhare ghar walon aur maal mein barkat de.",
            "اللہ تمہارے گھر والوں اور مال میں برکت دے",
            "Qarz ka badla shukr aur ada karna hai.",
            "Sunan an-Nasa'i", "Hadith 4683; Sunan Ibn Majah 2424", "Hasan"
        ),

        // ---------------- Hajj & Umrah ----------------
        d(
            "dua_talbiyah", "Hajj & Umrah", "Talbiyah",
            "Ihraam baandhne ke baad buland awaaz se.",
            "لَبَّيْكَ اللَّهُمَّ لَبَّيْكَ، لَبَّيْكَ لَا شَرِيكَ لَكَ لَبَّيْكَ، إِنَّ الْحَمْدَ وَالنِّعْمَةَ لَكَ وَالْمُلْكَ، لَا شَرِيكَ لَكَ",
            "Labbayk Allāhumma labbayk, labbayka lā sharīka laka labbayk, innal-ḥamda wan-ni'mata laka wal-mulk, lā sharīka lak",
            "Main haazir hoon, Ya Allah main haazir hoon. Main haazir hoon, Tera koi shareek nahi, main haazir hoon. Beshak saari tareef, nemat aur badshahat Teri hi hai, Tera koi shareek nahi.",
            "میں حاضر ہوں، اے اللہ میں حاضر ہوں، میں حاضر ہوں، تیرا کوئی شریک نہیں، میں حاضر ہوں، بے شک تمام تعریف، نعمت اور بادشاہی تیری ہی ہے، تیرا کوئی شریک نہیں",
            "Hajj aur Umrah ka shi'aar.",
            "Sahih al-Bukhari", "Hadith 1549; Sahih Muslim 1184", "Sahih"
        ),
        d(
            "dua_yamani_hajar", "Hajj & Umrah", "Between Rukn Yamani and Hajar Aswad",
            "Tawaf mein Rukn-e-Yamani aur Hajar-e-Aswad ke darmiyan.",
            "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            "Rabbanā ātinā fid-dunyā ḥasanatan wa fil-ākhirati ḥasanatan wa qinā 'adhāban-nār",
            "Aye hamare Rab, humein dunya mein bhi bhalai de aur aakhirat mein bhi bhalai de, aur humein aag ke azaab se bacha.",
            "اے ہمارے رب! ہمیں دنیا میں بھی بھلائی دے اور آخرت میں بھی بھلائی دے اور ہمیں آگ کے عذاب سے بچا",
            "Nabi ﷺ tawaf mein is jagah yeh parhte the.",
            "Sunan Abi Dawud", "Hadith 1892", "Hasan"
        ),
        d(
            "dua_safa_marwa", "Hajj & Umrah", "On Safa and Marwah",
            "Safa par charh kar Qibla rukh ho kar (teen martaba).",
            "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ، أَنْجَزَ وَعْدَهُ، وَنَصَرَ عَبْدَهُ، وَهَزَمَ الْأَحْزَابَ وَحْدَهُ",
            "Lā ilāha illallāhu waḥdahū lā sharīka lah, lahul-mulku wa lahul-ḥamdu wa huwa 'alā kulli shay'in qadīr, lā ilāha illallāhu waḥdah, anjaza wa'dah, wa naṣara 'abdah, wa hazamal-aḥzāba waḥdah",
            "Allah ke siwa koi mabood nahi, woh akela hai, uska koi shareek nahi, usi ki badshahat aur tareef hai aur woh har cheez par qaadir hai. Allah ke siwa koi mabood nahi, woh akela hai, usne apna waada poora kiya, apne bande ki madad ki aur akele saare lashkaron ko shikast di.",
            "اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے، اس کا کوئی شریک نہیں، اسی کی بادشاہی اور تعریف ہے اور وہ ہر چیز پر قادر ہے۔ اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے، اس نے اپنا وعدہ پورا کیا، اپنے بندے کی مدد کی اور اکیلے سب لشکروں کو شکست دی",
            "Nabi ﷺ ka Safa o Marwah par zikr.",
            "Sahih Muslim", "Hadith 1218", "Sahih", 3
        ),
        d(
            "dua_arafah", "Hajj & Umrah", "Best Dua of the Day of Arafah",
            "Yaum-e-Arafah — sab se behtar dua.",
            "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
            "Lā ilāha illallāhu waḥdahū lā sharīka lah, lahul-mulku wa lahul-ḥamd, wa huwa 'alā kulli shay'in qadīr",
            "Allah ke siwa koi mabood nahi, woh akela hai, uska koi shareek nahi, usi ki badshahat aur tareef hai aur woh har cheez par qaadir hai.",
            "اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے، اس کا کوئی شریک نہیں، اسی کی بادشاہی اور تعریف ہے اور وہ ہر چیز پر قادر ہے",
            "Sab se behtar dua Arafah ke din ki dua hai, aur sab se behtar kalma yeh hai.",
            "Jami` at-Tirmidhi", "Hadith 3585", "Hasan"
        ),
        d(
            "dua_takbeer_tashreeq", "Hajj & Umrah", "Takbeer-e-Tashreeq",
            "9 Zul-Hijjah Fajr se 13 Zul-Hijjah Asr tak har farz namaz ke baad.",
            "اللَّهُ أَكْبَرُ اللَّهُ أَكْبَرُ، لَا إِلَٰهَ إِلَّا اللَّهُ، وَاللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ وَلِلَّهِ الْحَمْدُ",
            "Allāhu akbar Allāhu akbar, lā ilāha illallāh, wallāhu akbar, Allāhu akbar wa lillāhil-ḥamd",
            "Allah sab se bara hai, Allah sab se bara hai. Allah ke siwa koi mabood nahi. Allah sab se bara hai, Allah sab se bara hai, aur saari tareef Allah hi ke liye hai.",
            "اللہ سب سے بڑا ہے، اللہ سب سے بڑا ہے، اللہ کے سوا کوئی معبود نہیں، اور اللہ سب سے بڑا ہے، اللہ سب سے بڑا ہے اور اللہ ہی کے لیے تمام تعریف ہے",
            "Hazrat Ali aur Ibn Mas'ud (RA) se manqool — Ahnaaf ke haan waajib.",
            "Musannaf Ibn Abi Shaybah", "Hadith 5633, 5651", "Mauqoof Sahih (Sahaba se)"
        ),
        d(
            "dua_zamzam", "Hajj & Umrah", "Drinking Zamzam",
            "Zamzam peete waqt jo niyyat ho us ki dua karein.",
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا وَاسِعًا، وَشِفَاءً مِنْ كُلِّ دَاءٍ",
            "Allāhumma innī as'aluka 'ilman nāfi'ā, wa rizqan wāsi'ā, wa shifā'an min kulli dā'",
            "Ya Allah, main Tujh se nafa dene wala ilm, kushaada rizq aur har bimaari se shifa maangta hoon.",
            "اے اللہ! میں تجھ سے نفع دینے والا علم، کشادہ رزق اور ہر بیماری سے شفا مانگتا ہوں",
            "Zamzam jis niyyat se piya jaaye usi ke liye hai (Ibn Majah 3062).",
            "Sunan ad-Daraqutni", "Hadith 2738 (Hazrat Ibn Abbas RA se)", "Mauqoof — sanad mein kalaam hai; asal hadith Ibn Majah 3062 Hasan"
        )
    )
}
