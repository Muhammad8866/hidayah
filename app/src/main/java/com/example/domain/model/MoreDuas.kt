package com.example.domain.model

// Additional authentic duas (Ahle Sunnat wal Jamaat sources, with references).
internal object MoreDuas {

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
            "dua_morning_asbahna", "Morning & Evening", "Morning Remembrance",
            "Subah ke waqt parhi jaane wali masnoon dua.",
            "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
            "Aṣbaḥnā wa aṣbaḥal-mulku lillāh, wal-ḥamdu lillāh, lā ilāha illallāhu waḥdahū lā sharīka lah, lahul-mulku wa lahul-ḥamdu wa huwa 'alā kulli shay'in qadīr",
            "Humne subah ki aur saari badshahat ne Allah ke liye subah ki. Saari tareef Allah ke liye hai. Allah ke siwa koi mabood nahi, woh akela hai, uska koi shareek nahi. Usi ki badshahat hai, usi ki tareef hai aur woh har cheez par qaadir hai.",
            "ہم نے صبح کی اور ساری بادشاہی نے اللہ کے لیے صبح کی، اور تمام تعریفیں اللہ کے لیے ہیں۔ اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے، اس کا کوئی شریک نہیں، اسی کی بادشاہی ہے اور اسی کی تعریف ہے اور وہ ہر چیز پر قادر ہے",
            "Din ki shuruaat Allah ki badshahat ke iqraar aur tauheed se hoti hai.",
            "Sahih Muslim", "Hadith 2723", "Sahih"
        ),
        d(
            "dua_evening_amsayna", "Morning & Evening", "Evening Remembrance",
            "Shaam ke waqt parhi jaane wali masnoon dua.",
            "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
            "Amsaynā wa amsal-mulku lillāh, wal-ḥamdu lillāh, lā ilāha illallāhu waḥdahū lā sharīka lah, lahul-mulku wa lahul-ḥamdu wa huwa 'alā kulli shay'in qadīr",
            "Humne shaam ki aur saari badshahat ne Allah ke liye shaam ki. Saari tareef Allah ke liye hai. Allah ke siwa koi mabood nahi, woh akela hai, uska koi shareek nahi. Usi ki badshahat hai, usi ki tareef hai aur woh har cheez par qaadir hai.",
            "ہم نے شام کی اور ساری بادشاہی نے اللہ کے لیے شام کی، اور تمام تعریفیں اللہ کے لیے ہیں۔ اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے، اس کا کوئی شریک نہیں، اسی کی بادشاہی ہے اور اسی کی تعریف ہے اور وہ ہر چیز پر قادر ہے",
            "Raat ki shuruaat Allah ki badshahat ke iqraar aur tauheed se hoti hai.",
            "Sahih Muslim", "Hadith 2723", "Sahih"
        ),
        d(
            "dua_bismillah_la_yadurru", "Morning & Evening", "Protection from All Harm",
            "Subah aur shaam teen teen martaba.",
            "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            "Bismillāhil-ladhī lā yaḍurru ma'asmihī shay'un fil-arḍi wa lā fis-samā'i wa huwas-Samī'ul-'Alīm",
            "Allah ke naam se, jiske naam ke saath zameen aur aasmaan mein koi cheez nuqsaan nahi pahuncha sakti, aur woh sab kuch sunne wala, jaanne wala hai.",
            "اللہ کے نام کے ساتھ جس کے نام کے ساتھ زمین و آسمان میں کوئی چیز نقصان نہیں پہنچا سکتی، اور وہ سننے والا جاننے والا ہے",
            "Jo ise subah o shaam teen martaba parhe, use koi cheez nuqsaan nahi pahunchayegi.",
            "Sunan Abi Dawud", "Hadith 5088; Jami` at-Tirmidhi 3388", "Hasan Sahih (Imam Tirmizi)", 3
        ),
        d(
            "dua_raditu_billahi", "Morning & Evening", "Contentment with Allah as Lord",
            "Subah aur shaam teen martaba.",
            "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا",
            "Raḍītu billāhi Rabbā, wa bil-Islāmi dīnā, wa bi-Muḥammadin ṣallallāhu 'alayhi wa sallama nabiyyā",
            "Main Allah ke Rab hone par, Islam ke deen hone par aur Muhammad ﷺ ke Nabi hone par raazi hoon.",
            "میں اللہ کے رب ہونے پر، اسلام کے دین ہونے پر اور محمد ﷺ کے نبی ہونے پر راضی ہوں",
            "Allah par haq hai ke qayamat ke din use raazi kare.",
            "Sunan Abi Dawud", "Hadith 5072; Jami` at-Tirmidhi 3389", "Hasan", 3
        ),
        d(
            "dua_hasbiyallah", "Morning & Evening", "Hasbiyallah (Allah is Sufficient)",
            "Subah aur shaam saat martaba.",
            "حَسْبِيَ اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
            "Ḥasbiyallāhu lā ilāha illā huwa, 'alayhi tawakkaltu wa huwa Rabbul-'Arshil-'Aẓīm",
            "Mujhe Allah kaafi hai, uske siwa koi mabood nahi, usi par maine bharosa kiya aur woh Arsh-e-Azeem ka Rab hai.",
            "مجھے اللہ کافی ہے، اس کے سوا کوئی معبود نہیں، اسی پر میں نے بھروسا کیا اور وہ عرش عظیم کا رب ہے",
            "Quran ki ayat; dunya aur aakhirat ki fikron mein Allah kaafi ho jaata hai.",
            "The Noble Quran", "Surah At-Tawbah (9:129); Sunan Abi Dawud 5081", "Ayat Qurani (hadith ki sanad mauqoof)", 7
        ),
        d(
            "dua_subhanallah_wa_bihamdihi", "Morning & Evening", "SubhanAllahi wa bihamdihi",
            "Din mein sau martaba.",
            "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            "Subḥānallāhi wa biḥamdih",
            "Allah paak hai apni tareef ke saath.",
            "اللہ پاک ہے اپنی تعریف کے ساتھ",
            "Jo din mein sau martaba kahe, uske gunaah maaf kar diye jaate hain chahe samundar ki jhaag ke barabar hon.",
            "Sahih al-Bukhari", "Hadith 6405; Sahih Muslim 2691", "Sahih", 100
        ),
        d(
            "dua_tahleel_100", "Morning & Evening", "La ilaha illallahu wahdahu",
            "Din mein sau martaba parhne ki badi fazilat.",
            "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
            "Lā ilāha illallāhu waḥdahū lā sharīka lah, lahul-mulku wa lahul-ḥamdu wa huwa 'alā kulli shay'in qadīr",
            "Allah ke siwa koi mabood nahi, woh akela hai, uska koi shareek nahi. Usi ki badshahat aur usi ki tareef hai aur woh har cheez par qaadir hai.",
            "اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے، اس کا کوئی شریک نہیں، اسی کی بادشاہی اور اسی کی تعریف ہے اور وہ ہر چیز پر قادر ہے",
            "Das ghulaam aazad karne ka sawab, sau nekiyan, sau gunaah maaf, aur shaam tak Shaitan se hifazat.",
            "Sahih al-Bukhari", "Hadith 3293; Sahih Muslim 2691", "Sahih", 100
        ),

        // ---------------- Prayer & Mosque ----------------
        d(
            "dua_leaving_mosque", "Prayer & Mosque", "Leaving the Mosque",
            "Masjid se baayan paon bahar nikaalte waqt.",
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ",
            "Allāhumma innī as'aluka min faḍlik",
            "Ya Allah, main Tujh se Tera fazl maangta hoon.",
            "اے اللہ! میں تجھ سے تیرا فضل مانگتا ہوں",
            "Masjid se nikalte hue Allah ke fazl aur rizq ki talab.",
            "Sahih Muslim", "Hadith 713", "Sahih"
        ),
        d(
            "dua_after_adhan", "Prayer & Mosque", "After the Adhan",
            "Azaan mukammal hone ke baad.",
            "اللَّهُمَّ رَبَّ هَٰذِهِ الدَّعْوَةِ التَّامَّةِ، وَالصَّلَاةِ الْقَائِمَةِ، آتِ مُحَمَّدًا الْوَسِيلَةَ وَالْفَضِيلَةَ، وَابْعَثْهُ مَقَامًا مَحْمُودًا الَّذِي وَعَدْتَهُ",
            "Allāhumma Rabba hādhihid-da'watit-tāmmah, waṣ-ṣalātil-qā'imah, āti Muḥammadanil-wasīlata wal-faḍīlah, wab'ath-hu maqāman maḥmūdanil-ladhī wa'adtah",
            "Ya Allah, is mukammal pukaar aur qaayam hone wali namaz ke Rab! Muhammad ﷺ ko waseela aur fazeelat ata farma, aur unhein us Maqam-e-Mahmood par pahuncha jiska Tune unse waada kiya hai.",
            "اے اللہ! اس کامل دعوت اور قائم ہونے والی نماز کے رب! محمد ﷺ کو وسیلہ اور فضیلت عطا فرما اور انہیں اس مقام محمود پر فائز فرما جس کا تو نے ان سے وعدہ کیا ہے",
            "Jo yeh dua parhe, qayamat ke din uske liye Nabi ﷺ ki shafa'at halal ho jaati hai.",
            "Sahih al-Bukhari", "Hadith 614", "Sahih"
        ),
        d(
            "dua_after_salah_istighfar", "Prayer & Mosque", "After Fard Salah",
            "Salaam pherne ke foran baad.",
            "أَسْتَغْفِرُ اللَّهَ (ثَلَاثًا)، اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
            "Astaghfirullāh (3x). Allāhumma Antas-Salām wa minkas-salām, tabārakta yā Dhal-Jalāli wal-Ikrām",
            "Main Allah se maafi maangta hoon (3 baar). Ya Allah, Tu hi Salaam hai aur Tujh hi se salaamati hai, Tu barkat wala hai aye Jalaal aur Ikraam wale.",
            "میں اللہ سے مغفرت مانگتا ہوں (تین بار)۔ اے اللہ! تو ہی سلام ہے اور تجھ ہی سے سلامتی ہے، تو بابرکت ہے اے جلال اور اکرام والے",
            "Nabi ﷺ har farz namaz ke baad yeh parhte the.",
            "Sahih Muslim", "Hadith 591", "Sahih"
        ),
        d(
            "dua_after_salah_help_dhikr", "Prayer & Mosque", "Help Me Remember You",
            "Har namaz ke baad — Nabi ﷺ ne Hazrat Muaz (RA) ko wasiyat ki.",
            "اللَّهُمَّ أَعِنِّي عَلَىٰ ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ",
            "Allāhumma a'innī 'alā dhikrika wa shukrika wa ḥusni 'ibādatik",
            "Ya Allah, apne zikr, apne shukr aur apni achhi ibaadat par meri madad farma.",
            "اے اللہ! اپنے ذکر، اپنے شکر اور اپنی اچھی عبادت پر میری مدد فرما",
            "Nabi ﷺ ki khaas wasiyat — ibaadat mein husn aur istiqamat.",
            "Sunan Abi Dawud", "Hadith 1522; Sunan an-Nasa'i 1303", "Sahih"
        ),
        d(
            "dua_ayatul_kursi", "Prayer & Mosque", "Ayat al-Kursi after Salah",
            "Har farz namaz ke baad aur sone se pehle.",
            "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
            "Allāhu lā ilāha illā huwal-Ḥayyul-Qayyūm, lā ta'khudhuhū sinatun wa lā nawm, lahū mā fis-samāwāti wa mā fil-arḍ, man dhal-ladhī yashfa'u 'indahū illā bi-idhnih, ya'lamu mā bayna aydīhim wa mā khalfahum, wa lā yuḥīṭūna bi-shay'im-min 'ilmihī illā bimā shā', wasi'a kursiyyuhus-samāwāti wal-arḍ, wa lā ya'ūduhū ḥifẓuhumā, wa huwal-'Aliyyul-'Aẓīm",
            "Allah, uske siwa koi mabood nahi, woh zinda hai, sab ko qaayam rakhne wala. Use na oongh aati hai na neend. Aasmaan o zameen mein jo kuch hai usi ka hai. Kaun hai jo uski ijaazat ke baghair uske paas sifarish kare? Woh jaanta hai jo unke aage aur peeche hai, aur log uske ilm mein se kisi cheez ka ihaata nahi kar sakte magar jitna woh chahe. Uski Kursi aasmaanon aur zameen ko ghere hue hai, aur unki hifazat use thakaati nahi, aur woh buland, azmat wala hai.",
            "اللہ، اس کے سوا کوئی معبود نہیں، زندہ ہے، سب کو قائم رکھنے والا ہے۔ اسے نہ اونگھ آتی ہے نہ نیند۔ آسمانوں اور زمین میں جو کچھ ہے اسی کا ہے۔ کون ہے جو اس کی اجازت کے بغیر اس کے پاس سفارش کرے؟ وہ جانتا ہے جو ان کے آگے اور پیچھے ہے، اور وہ اس کے علم میں سے کسی چیز کا احاطہ نہیں کر سکتے مگر جتنا وہ چاہے۔ اس کی کرسی آسمانوں اور زمین کو گھیرے ہوئے ہے، اور ان کی حفاظت اسے نہیں تھکاتی، اور وہ بلند و عظیم ہے",
            "Har farz namaz ke baad parhne wale ko Jannat mein daakhil hone se sirf maut rokti hai; sone se pehle parhne par subah tak hifazat.",
            "The Noble Quran", "Surah Al-Baqarah (2:255); Sunan an-Nasa'i al-Kubra 9848; Sahih al-Bukhari 2311", "Ayat Qurani; hadith Sahih"
        ),

        // ---------------- Sleep & Awakening ----------------
        d(
            "dua_before_sleep", "Sleep & Awakening", "Before Sleeping",
            "Bistar par letne ke baad.",
            "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
            "Bismika Allāhumma amūtu wa aḥyā",
            "Ya Allah, Tere hi naam ke saath main marta (sota) hoon aur jeeta (jaagta) hoon.",
            "اے اللہ! تیرے ہی نام کے ساتھ میں مرتا اور جیتا ہوں",
            "Nabi ﷺ sone se pehle yeh dua parhte the.",
            "Sahih al-Bukhari", "Hadith 6324", "Sahih"
        ),
        d(
            "dua_sleep_tasbeeh", "Sleep & Awakening", "Tasbeeh-e-Fatimi before Sleep",
            "Sone se pehle — Nabi ﷺ ne Hazrat Fatima aur Hazrat Ali (RA) ko sikhaya.",
            "سُبْحَانَ اللَّهِ (٣٣)، الْحَمْدُ لِلَّهِ (٣٣)، اللَّهُ أَكْبَرُ (٣٤)",
            "SubḥānAllāh (33), Al-ḥamdu lillāh (33), Allāhu Akbar (34)",
            "Allah paak hai (33), saari tareef Allah ke liye hai (33), Allah sab se bara hai (34).",
            "سبحان اللہ (۳۳)، الحمد للہ (۳۳)، اللہ اکبر (۳۴)",
            "Nabi ﷺ ne farmaya yeh tumhare liye khaadim se behtar hai.",
            "Sahih al-Bukhari", "Hadith 6318; Sahih Muslim 2727", "Sahih"
        ),
        d(
            "dua_sleep_qina_adhab", "Sleep & Awakening", "Protection from Punishment",
            "Daayen haath ko rukhsaar ke neeche rakh kar, teen martaba.",
            "اللَّهُمَّ قِنِي عَذَابَكَ يَوْمَ تَبْعَثُ عِبَادَكَ",
            "Allāhumma qinī 'adhābaka yawma tab'athu 'ibādak",
            "Ya Allah, mujhe us din apne azaab se bacha jis din Tu apne bandon ko uthayega.",
            "اے اللہ! مجھے اس دن اپنے عذاب سے بچا جس دن تو اپنے بندوں کو اٹھائے گا",
            "Sone ko maut ki yaad aur aakhirat ki tayyari bana deti hai.",
            "Sunan Abi Dawud", "Hadith 5045; Jami` at-Tirmidhi 3398", "Sahih / Hasan", 3
        ),
        d(
            "dua_bad_dream", "Sleep & Awakening", "After a Bad Dream",
            "Bura khwaab dekhne par: baayin taraf teen baar halka thutkaar, phir karwat badlein.",
            "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ",
            "A'ūdhu billāhi minash-shayṭānir-rajīm",
            "Main Allah ki panaah maangta hoon mardood Shaitan se.",
            "میں اللہ کی پناہ مانگتا ہوں شیطان مردود سے",
            "Bura khwaab nuqsaan nahi dega; kisi ko bataayein nahi.",
            "Sahih Muslim", "Hadith 2261, 2262", "Sahih", 3
        ),

        // ---------------- Anxiety & Distress ----------------
        d(
            "dua_yunus", "Anxiety & Distress", "Dua of Prophet Yunus (AS)",
            "Machhli ke pet mein Hazrat Yunus (AS) ki dua — har musibat mein.",
            "لَّا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ",
            "Lā ilāha illā Anta subḥānaka innī kuntu minaẓ-ẓālimīn",
            "Tere siwa koi mabood nahi, Tu paak hai, beshak main hi zaalimon mein se tha.",
            "تیرے سوا کوئی معبود نہیں، تو پاک ہے، بے شک میں ہی ظالموں میں سے تھا",
            "Jo musalman kisi bhi maamle mein yeh dua kare, Allah uski dua qubool farmata hai.",
            "The Noble Quran", "Surah Al-Anbiya (21:87); Jami` at-Tirmidhi 3505", "Ayat Qurani; hadith Sahih"
        ),
        d(
            "dua_karb", "Anxiety & Distress", "Dua at Time of Distress (Karb)",
            "Sakht pareshani aur takleef ke waqt.",
            "لَا إِلَٰهَ إِلَّا اللَّهُ الْعَظِيمُ الْحَلِيمُ، لَا إِلَٰهَ إِلَّا اللَّهُ رَبُّ الْعَرْشِ الْعَظِيمِ، لَا إِلَٰهَ إِلَّا اللَّهُ رَبُّ السَّمَاوَاتِ وَرَبُّ الْأَرْضِ وَرَبُّ الْعَرْشِ الْكَرِيمِ",
            "Lā ilāha illallāhul-'Aẓīmul-Ḥalīm, lā ilāha illallāhu Rabbul-'Arshil-'Aẓīm, lā ilāha illallāhu Rabbus-samāwāti wa Rabbul-arḍi wa Rabbul-'Arshil-Karīm",
            "Allah ke siwa koi mabood nahi jo azmat wala, burdbaar hai. Allah ke siwa koi mabood nahi jo Arsh-e-Azeem ka Rab hai. Allah ke siwa koi mabood nahi jo aasmaanon, zameen aur Arsh-e-Kareem ka Rab hai.",
            "اللہ کے سوا کوئی معبود نہیں جو عظمت والا بردبار ہے۔ اللہ کے سوا کوئی معبود نہیں جو عرش عظیم کا رب ہے۔ اللہ کے سوا کوئی معبود نہیں جو آسمانوں کا رب، زمین کا رب اور عرش کریم کا رب ہے",
            "Nabi ﷺ pareshani ke waqt yeh dua parhte the.",
            "Sahih al-Bukhari", "Hadith 6346; Sahih Muslim 2730", "Sahih"
        ),
        d(
            "dua_ya_hayyu_ya_qayyum", "Anxiety & Distress", "Ya Hayyu Ya Qayyum",
            "Jab koi maamla bohot bhaari ho jaaye.",
            "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ",
            "Yā Ḥayyu yā Qayyūmu biraḥmatika astaghīth",
            "Aye Zinda, aye sab ko qaayam rakhne wale! Teri rehmat ke waaste se main faryaad karta hoon.",
            "اے زندہ! اے سب کو قائم رکھنے والے! تیری رحمت کے واسطے سے میں فریاد کرتا ہوں",
            "Nabi ﷺ ko jab koi maamla pareshaan karta to yeh parhte.",
            "Jami` at-Tirmidhi", "Hadith 3524", "Hasan"
        ),
        d(
            "dua_hasbunallah", "Anxiety & Distress", "Hasbunallahu wa Ni'mal Wakeel",
            "Dushman ke khauf ya bade khatre ke waqt.",
            "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ",
            "Ḥasbunallāhu wa ni'mal-wakīl",
            "Humein Allah kaafi hai aur woh behtareen kaarsaaz hai.",
            "ہمیں اللہ کافی ہے اور وہ بہترین کارساز ہے",
            "Hazrat Ibrahim (AS) ne aag mein daale jaate waqt aur Nabi ﷺ ne khatre ke waqt yeh kaha.",
            "The Noble Quran", "Surah Aal-e-Imran (3:173); Sahih al-Bukhari 4563", "Ayat Qurani; hadith Sahih"
        ),

        // ---------------- Travel & Leaving Home ----------------
        d(
            "dua_entering_home", "Travel & Leaving Home", "Entering the House",
            "Ghar mein daakhil hote waqt Allah ka naam lein aur salaam karein.",
            "بِسْمِ اللَّهِ",
            "Bismillāh",
            "Allah ke naam se.",
            "اللہ کے نام سے",
            "Ghar mein daakhil hote aur khaate waqt Allah ka zikr karne se Shaitan kehta hai: tumhare liye na yahan raat guzaarne ki jagah hai na khaana.",
            "Sahih Muslim", "Hadith 2018", "Sahih"
        ),
        d(
            "dua_travel", "Travel & Leaving Home", "Dua for Travel (Riding)",
            "Sawari par baith kar teen baar Allahu Akbar ke baad.",
            "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ، وَإِنَّا إِلَىٰ رَبِّنَا لَمُنقَلِبُونَ",
            "Subḥānal-ladhī sakhkhara lanā hādhā wa mā kunnā lahū muqrinīn, wa innā ilā Rabbinā lamunqalibūn",
            "Paak hai woh zaat jisne ise hamare taabe kar diya, warna hum ise qaabu mein laane wale na the, aur beshak hum apne Rab ki taraf lautne wale hain.",
            "پاک ہے وہ ذات جس نے اسے ہمارے تابع کر دیا حالانکہ ہم اسے قابو میں لانے والے نہ تھے، اور بے شک ہم اپنے رب کی طرف لوٹنے والے ہیں",
            "Safar ki masnoon dua — hifazat aur barkat ke liye.",
            "Sahih Muslim", "Hadith 1342; Surah Az-Zukhruf (43:13-14)", "Sahih"
        ),
        d(
            "dua_entering_market", "Travel & Leaving Home", "Entering the Marketplace",
            "Bazaar mein daakhil hote waqt.",
            "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، يُحْيِي وَيُمِيتُ وَهُوَ حَيٌّ لَا يَمُوتُ، بِيَدِهِ الْخَيْرُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
            "Lā ilāha illallāhu waḥdahū lā sharīka lah, lahul-mulku wa lahul-ḥamd, yuḥyī wa yumītu wa huwa ḥayyun lā yamūt, biyadihil-khayr, wa huwa 'alā kulli shay'in qadīr",
            "Allah ke siwa koi mabood nahi, woh akela hai, uska koi shareek nahi. Usi ki badshahat aur tareef hai. Wohi zinda karta aur maarta hai, aur woh khud zinda hai, kabhi nahi marega. Usi ke haath mein bhalai hai aur woh har cheez par qaadir hai.",
            "اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے، اس کا کوئی شریک نہیں، اسی کی بادشاہی اور تعریف ہے، وہی زندہ کرتا اور مارتا ہے اور وہ خود زندہ ہے کبھی نہیں مرے گا، اسی کے ہاتھ میں بھلائی ہے اور وہ ہر چیز پر قادر ہے",
            "Hadith mein iski badi fazilat bayan hui hai (lakhon nekiyan).",
            "Jami` at-Tirmidhi", "Hadith 3428", "Hasan (baaz muhaddiseen ne sanad par kalaam kiya)"
        ),

        // ---------------- Forgiveness & Istighfar ----------------
        d(
            "dua_adam_forgiveness", "Forgiveness & Istighfar", "Dua of Prophet Adam (AS)",
            "Hazrat Adam aur Hazrat Hawwa (AS) ki tauba ki dua.",
            "رَبَّنَا ظَلَمْنَا أَنفُسَنَا وَإِن لَّمْ تَغْفِرْ لَنَا وَتَرْحَمْنَا لَنَكُونَنَّ مِنَ الْخَاسِرِينَ",
            "Rabbanā ẓalamnā anfusanā wa il-lam taghfir lanā wa tarḥamnā lanakūnanna minal-khāsirīn",
            "Aye hamare Rab, humne apni jaanon par zulm kiya, aur agar Tu humein maaf na kare aur hum par reham na kare to hum zaroor nuqsaan uthane walon mein se ho jaayenge.",
            "اے ہمارے رب! ہم نے اپنی جانوں پر ظلم کیا، اور اگر تو نے ہمیں نہ بخشا اور ہم پر رحم نہ کیا تو ہم ضرور نقصان اٹھانے والوں میں سے ہو جائیں گے",
            "Insaaniyat ki pehli tauba — Allah ne ise qubool farmaya.",
            "The Noble Quran", "Surah Al-A'raf (7:23)", "Mutawatir (Quranic Verse)"
        ),
        d(
            "dua_abu_bakr", "Forgiveness & Istighfar", "Dua taught to Abu Bakr (RA)",
            "Namaz mein (tashahhud ke baad) parhne ke liye Nabi ﷺ ne Hazrat Abu Bakr Siddiq (RA) ko sikhayi.",
            "اللَّهُمَّ إِنِّي ظَلَمْتُ نَفْسِي ظُلْمًا كَثِيرًا، وَلَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ، فَاغْفِرْ لِي مَغْفِرَةً مِنْ عِنْدِكَ، وَارْحَمْنِي، إِنَّكَ أَنْتَ الْغَفُورُ الرَّحِيمُ",
            "Allāhumma innī ẓalamtu nafsī ẓulman kathīrā, wa lā yaghfirudh-dhunūba illā Ant, faghfir lī maghfiratam-min 'indik, warḥamnī, innaka Antal-Ghafūrur-Raḥīm",
            "Ya Allah, maine apni jaan par bohot zulm kiya, aur Tere siwa gunaahon ko koi maaf nahi karta, pas apni taraf se mujhe maaf farma aur mujh par reham farma, beshak Tu hi bakhshne wala, reham karne wala hai.",
            "اے اللہ! میں نے اپنی جان پر بہت ظلم کیا اور تیرے سوا کوئی گناہوں کو نہیں بخشتا، پس اپنی طرف سے مجھے بخش دے اور مجھ پر رحم فرما، بے شک تو ہی بخشنے والا مہربان ہے",
            "Namaz ke aakhir mein parhne ki masnoon dua.",
            "Sahih al-Bukhari", "Hadith 834; Sahih Muslim 2705", "Sahih"
        ),
        d(
            "dua_astaghfirullah_atubu", "Forgiveness & Istighfar", "Daily Istighfar",
            "Nabi ﷺ din mein sattar se zyada martaba istighfar karte the.",
            "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
            "Astaghfirullāha wa atūbu ilayh",
            "Main Allah se maafi maangta hoon aur usi ki taraf tauba karta hoon.",
            "میں اللہ سے مغفرت مانگتا ہوں اور اسی کی طرف توبہ کرتا ہوں",
            "Nabi ﷺ ne farmaya: main din mein sattar se zyada martaba Allah se istighfar aur tauba karta hoon.",
            "Sahih al-Bukhari", "Hadith 6307", "Sahih", 100
        ),
        d(
            "dua_laylatul_qadr", "Ramadan & Fasting", "Dua for Laylat al-Qadr",
            "Hazrat Aisha (RA) ko Shab-e-Qadr ke liye sikhayi gayi.",
            "اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي",
            "Allāhumma innaka 'Afuwwun tuḥibbul-'afwa fa'fu 'annī",
            "Ya Allah, Tu bohot maaf karne wala hai, maafi ko pasand karta hai, pas mujhe maaf farma de.",
            "اے اللہ! تو بہت معاف کرنے والا ہے، معافی کو پسند کرتا ہے، پس مجھے معاف فرما دے",
            "Ramzan ki aakhri raaton aur Shab-e-Qadr ki khaas dua.",
            "Jami` at-Tirmidhi", "Hadith 3513; Sunan Ibn Majah 3850", "Hasan Sahih (Imam Tirmizi)"
        ),

        // ---------------- Parents & Family ----------------
        d(
            "dua_family_coolness", "Parents & Family", "Coolness of the Eyes (Spouse & Children)",
            "Naik biwi/shohar aur aulaad ke liye Qurani dua.",
            "رَبَّنَا هَبْ لَنَا مِنْ أَزْوَاجِنَا وَذُرِّيَّاتِنَا قُرَّةَ أَعْيُنٍ وَاجْعَلْنَا لِلْمُتَّقِينَ إِمَامًا",
            "Rabbanā hab lanā min azwājinā wa dhurriyyātinā qurrata a'yunin waj'alnā lil-muttaqīna imāmā",
            "Aye hamare Rab, humein hamari biwiyon aur aulaad se aankhon ki thandak ata farma, aur humein parhezgaaron ka imaam bana.",
            "اے ہمارے رب! ہمیں ہماری بیویوں اور اولاد سے آنکھوں کی ٹھنڈک عطا فرما اور ہمیں پرہیزگاروں کا امام بنا",
            "Ghar mein sukoon aur naik aulaad ke liye.",
            "The Noble Quran", "Surah Al-Furqan (25:74)", "Mutawatir (Quranic Verse)"
        ),
        d(
            "dua_righteous_children", "Parents & Family", "For Righteous Offspring",
            "Hazrat Zakariya (AS) ki dua.",
            "رَبِّ هَبْ لِي مِن لَّدُنكَ ذُرِّيَّةً طَيِّبَةً ۖ إِنَّكَ سَمِيعُ الدُّعَاءِ",
            "Rabbi hab lī mil-ladunka dhurriyyatan ṭayyibah, innaka Samī'ud-du'ā'",
            "Aye mere Rab, mujhe apne paas se paakeeza aulaad ata farma, beshak Tu dua sunne wala hai.",
            "اے میرے رب! مجھے اپنے پاس سے پاکیزہ اولاد عطا فرما، بے شک تو دعا سننے والا ہے",
            "Aulaad ki talab ke liye Qurani dua.",
            "The Noble Quran", "Surah Aal-e-Imran (3:38)", "Mutawatir (Quranic Verse)"
        ),
        d(
            "dua_ibrahim_prayer_family", "Parents & Family", "Establishers of Prayer",
            "Hazrat Ibrahim (AS) ki apne aur aulaad ke liye dua.",
            "رَبِّ اجْعَلْنِي مُقِيمَ الصَّلَاةِ وَمِن ذُرِّيَّتِي ۚ رَبَّنَا وَتَقَبَّلْ دُعَاءِ (٤٠) رَبَّنَا اغْفِرْ لِي وَلِوَالِدَيَّ وَلِلْمُؤْمِنِينَ يَوْمَ يَقُومُ الْحِسَابُ (٤١)",
            "Rabbij-'alnī muqīmaṣ-ṣalāti wa min dhurriyyatī, Rabbanā wa taqabbal du'ā'. Rabbanaghfir lī wa liwālidayya wa lil-mu'minīna yawma yaqūmul-ḥisāb",
            "Aye mere Rab, mujhe aur meri aulaad ko namaz qaayam karne wala bana. Aye hamare Rab, meri dua qubool farma. Aye hamare Rab, mujhe, mere waalidain ko aur sab momino ko hisaab ke din bakhsh de.",
            "اے میرے رب! مجھے اور میری اولاد کو نماز قائم کرنے والا بنا، اے ہمارے رب! میری دعا قبول فرما۔ اے ہمارے رب! مجھے، میرے والدین کو اور مومنوں کو حساب کے دن بخش دے",
            "Namaz ke aakhir mein aam taur par parhi jaati hai.",
            "The Noble Quran", "Surah Ibrahim (14:40-41)", "Mutawatir (Quranic Verse)"
        ),
        d(
            "dua_newlywed", "Parents & Family", "Congratulating the Newlyweds",
            "Nikah karne wale ko mubarakbaad dete waqt.",
            "بَارَكَ اللَّهُ لَكَ، وَبَارَكَ عَلَيْكَ، وَجَمَعَ بَيْنَكُمَا فِي خَيْرٍ",
            "Bārakallāhu laka, wa bāraka 'alayka, wa jama'a baynakumā fī khayr",
            "Allah tumhare liye barkat de, tum par barkat naazil kare aur tum dono ko bhalai mein jamaa rakhe.",
            "اللہ تمہارے لیے برکت دے، تم پر برکت نازل کرے اور تم دونوں کو بھلائی میں جمع رکھے",
            "Nikah ki masnoon mubarakbaad.",
            "Sunan Abi Dawud", "Hadith 2130; Jami` at-Tirmidhi 1091", "Hasan Sahih (Imam Tirmizi)"
        ),

        // ---------------- Food & Gratitude ----------------
        d(
            "dua_before_eating", "Food & Gratitude", "Before Eating",
            "Khaane se pehle. Bhool jaayein to darmiyan mein parhein.",
            "بِسْمِ اللَّهِ — (بھول جائیں تو) بِسْمِ اللَّهِ أَوَّلَهُ وَآخِرَهُ",
            "Bismillāh — (agar bhool jaayein) Bismillāhi awwalahū wa ākhirah",
            "Allah ke naam se. (Bhoolne par:) Allah ke naam se, iske shuru aur aakhir mein.",
            "اللہ کے نام سے۔ (بھول جائیں تو:) اللہ کے نام سے اس کے شروع اور آخر میں",
            "Bismillah parhne se khaane mein Shaitan shareek nahi hota.",
            "Sunan Abi Dawud", "Hadith 3767; Jami` at-Tirmidhi 1858", "Sahih"
        ),
        d(
            "dua_after_eating", "Food & Gratitude", "After Eating",
            "Khaana khatam karne ke baad.",
            "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَٰذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
            "Al-ḥamdu lillāhil-ladhī aṭ'amanī hādhā wa razaqanīhi min ghayri ḥawlim-minnī wa lā quwwah",
            "Saari tareef Allah ke liye jisne mujhe yeh khilaya aur meri kisi taaqat o quwwat ke baghair mujhe yeh rizq diya.",
            "تمام تعریفیں اللہ کے لیے جس نے مجھے یہ کھلایا اور میری کسی طاقت و قوت کے بغیر مجھے یہ رزق دیا",
            "Iske parhne wale ke pichhle gunaah maaf kar diye jaate hain.",
            "Sunan Abi Dawud", "Hadith 4023; Jami` at-Tirmidhi 3458", "Hasan"
        ),
        d(
            "dua_host", "Food & Gratitude", "For the Host",
            "Kisi ke haan khaana khaane ke baad mezbaan ke liye.",
            "اللَّهُمَّ بَارِكْ لَهُمْ فِيمَا رَزَقْتَهُمْ، وَاغْفِرْ لَهُمْ وَارْحَمْهُمْ",
            "Allāhumma bārik lahum fīmā razaqtahum, waghfir lahum warḥamhum",
            "Ya Allah, jo rizq Tune unhein diya hai usmein barkat de, unhein bakhsh de aur un par reham farma.",
            "اے اللہ! جو رزق تو نے انہیں دیا ہے اس میں برکت دے، انہیں بخش دے اور ان پر رحم فرما",
            "Mezbaan ka shukriya masnoon tareeqe se.",
            "Sahih Muslim", "Hadith 2042", "Sahih"
        ),
        d(
            "dua_breaking_fast", "Ramadan & Fasting", "Upon Breaking the Fast",
            "Iftar ke waqt.",
            "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللَّهُ",
            "Dhahabaẓ-ẓama'u wabtallatil-'urūqu wa thabatal-ajru in shā' Allāh",
            "Pyaas chali gayi, ragein tar ho gayin aur agar Allah ne chaha to ajr saabit ho gaya.",
            "پیاس بجھ گئی، رگیں تر ہو گئیں اور اگر اللہ نے چاہا تو اجر ثابت ہو گیا",
            "Nabi ﷺ iftar ke waqt yeh farmate the.",
            "Sunan Abi Dawud", "Hadith 2357", "Hasan"
        ),
        d(
            "dua_new_clothes", "Food & Gratitude", "Wearing New Clothes",
            "Naya kapda pehente waqt (kapde ka naam le kar, jaise qameez ya amaama).",
            "اللَّهُمَّ لَكَ الْحَمْدُ أَنْتَ كَسَوْتَنِيهِ، أَسْأَلُكَ مِنْ خَيْرِهِ وَخَيْرِ مَا صُنِعَ لَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّهِ وَشَرِّ مَا صُنِعَ لَهُ",
            "Allāhumma lakal-ḥamdu Anta kasawtanīh, as'aluka min khayrihī wa khayri mā ṣuni'a lah, wa a'ūdhu bika min sharrihī wa sharri mā ṣuni'a lah",
            "Ya Allah, Tere hi liye saari tareef hai, Tune hi mujhe yeh pehnaya. Main Tujh se iski bhalai aur jis kaam ke liye yeh banaya gaya uski bhalai maangta hoon, aur iske shar aur jis kaam ke liye yeh banaya gaya uske shar se Teri panaah maangta hoon.",
            "اے اللہ! تیرے ہی لیے تمام تعریف ہے، تو نے ہی مجھے یہ پہنایا۔ میں تجھ سے اس کی بھلائی اور جس کام کے لیے یہ بنایا گیا اس کی بھلائی مانگتا ہوں، اور اس کے شر اور جس کام کے لیے یہ بنایا گیا اس کے شر سے تیری پناہ مانگتا ہوں",
            "Nabi ﷺ naya kapda pehente to yeh dua farmate. (Rozana kapde pehente waqt ki dua 'Getting Dressed' mein hai.)",
            "Sunan Abi Dawud", "Hadith 4020; Jami` at-Tirmidhi 1767", "Sahih (Sheikh Albani)"
        ),

        // ---------------- Quranic Duas ----------------
        d(
            "dua_rabbana_atina", "Quranic Duas", "Good in This World and the Hereafter",
            "Nabi ﷺ sab se zyada yeh dua maangte the.",
            "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            "Rabbanā ātinā fid-dunyā ḥasanatan wa fil-ākhirati ḥasanatan wa qinā 'adhāban-nār",
            "Aye hamare Rab, humein dunya mein bhi bhalai de aur aakhirat mein bhi bhalai de, aur humein aag ke azaab se bacha.",
            "اے ہمارے رب! ہمیں دنیا میں بھی بھلائی دے اور آخرت میں بھی بھلائی دے اور ہمیں آگ کے عذاب سے بچا",
            "Dunya aur aakhirat ki har bhalai ko jamaa karne wali dua.",
            "The Noble Quran", "Surah Al-Baqarah (2:201); Sahih al-Bukhari 6389", "Ayat Qurani; hadith Sahih"
        ),
        d(
            "dua_rabbi_zidni_ilma", "Quranic Duas", "Increase in Knowledge",
            "Ilm mein izaafe ke liye.",
            "رَّبِّ زِدْنِي عِلْمًا",
            "Rabbi zidnī 'ilmā",
            "Aye mere Rab, mere ilm mein izaafa farma.",
            "اے میرے رب! میرے علم میں اضافہ فرما",
            "Allah ne Nabi ﷺ ko sirf ilm mein izaafe ki dua maangne ka hukm diya.",
            "The Noble Quran", "Surah Ta-Ha (20:114)", "Mutawatir (Quranic Verse)"
        ),
        d(
            "dua_musa_sharh_sadr", "Quranic Duas", "Opening of the Chest (Musa AS)",
            "Imtihaan, taqreer ya kisi mushkil kaam se pehle.",
            "رَبِّ اشْرَحْ لِي صَدْرِي (٢٥) وَيَسِّرْ لِي أَمْرِي (٢٦) وَاحْلُلْ عُقْدَةً مِّن لِّسَانِي (٢٧) يَفْقَهُوا قَوْلِي (٢٨)",
            "Rabbish-raḥ lī ṣadrī, wa yassir lī amrī, waḥlul 'uqdatam-mil-lisānī, yafqahū qawlī",
            "Aye mere Rab, mera seena khol de, mera kaam aasaan kar de, aur meri zubaan ki girah khol de taake log meri baat samajh sakein.",
            "اے میرے رب! میرا سینہ کھول دے، میرا کام آسان کر دے، اور میری زبان کی گرہ کھول دے تاکہ لوگ میری بات سمجھ سکیں",
            "Hazrat Musa (AS) ki dua — dil ka itminaan aur kaam mein aasaani.",
            "The Noble Quran", "Surah Ta-Ha (20:25-28)", "Mutawatir (Quranic Verse)"
        ),
        d(
            "dua_la_tuzigh", "Quranic Duas", "Steadfastness of the Heart",
            "Hidayat par saabit qadmi ke liye.",
            "رَبَّنَا لَا تُزِغْ قُلُوبَنَا بَعْدَ إِذْ هَدَيْتَنَا وَهَبْ لَنَا مِن لَّدُنكَ رَحْمَةً ۚ إِنَّكَ أَنتَ الْوَهَّابُ",
            "Rabbanā lā tuzigh qulūbanā ba'da idh hadaytanā wa hab lanā mil-ladunka raḥmah, innaka Antal-Wahhāb",
            "Aye hamare Rab, humein hidayat dene ke baad hamare dilon ko tedha na kar, aur humein apne paas se rehmat ata farma, beshak Tu hi bohot ata karne wala hai.",
            "اے ہمارے رب! ہمیں ہدایت دینے کے بعد ہمارے دلوں کو ٹیڑھا نہ کر اور ہمیں اپنے پاس سے رحمت عطا فرما، بے شک تو ہی بہت عطا کرنے والا ہے",
            "Imaan par saabit rehne ki dua.",
            "The Noble Quran", "Surah Aal-e-Imran (3:8)", "Mutawatir (Quranic Verse)"
        ),
        d(
            "dua_ya_muqallib", "Quranic Duas", "O Turner of Hearts",
            "Nabi ﷺ aksar yeh dua maangte the.",
            "يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَىٰ دِينِكَ",
            "Yā Muqallibal-qulūbi thabbit qalbī 'alā dīnik",
            "Aye dilon ko pherne wale, mere dil ko apne deen par saabit rakh.",
            "اے دلوں کو پھیرنے والے! میرے دل کو اپنے دین پر ثابت رکھ",
            "Hazrat Umm-e-Salama (RA) ke mutaabiq Nabi ﷺ ki sab se zyada maangi jaane wali dua.",
            "Jami` at-Tirmidhi", "Hadith 3522", "Hasan"
        ),
        d(
            "dua_la_tuakhidhna", "Quranic Duas", "Last Verses of Surah Al-Baqarah",
            "Raat ko sone se pehle — Surah Baqarah ki aakhri do ayaat.",
            "رَبَّنَا لَا تُؤَاخِذْنَا إِن نَّسِينَا أَوْ أَخْطَأْنَا ۚ رَبَّنَا وَلَا تَحْمِلْ عَلَيْنَا إِصْرًا كَمَا حَمَلْتَهُ عَلَى الَّذِينَ مِن قَبْلِنَا ۚ رَبَّنَا وَلَا تُحَمِّلْنَا مَا لَا طَاقَةَ لَنَا بِهِ ۖ وَاعْفُ عَنَّا وَاغْفِرْ لَنَا وَارْحَمْنَا ۚ أَنتَ مَوْلَانَا فَانصُرْنَا عَلَى الْقَوْمِ الْكَافِرِينَ",
            "Rabbanā lā tu'ākhidhnā in nasīnā aw akhṭa'nā, Rabbanā wa lā taḥmil 'alaynā iṣran kamā ḥamaltahū 'alal-ladhīna min qablinā, Rabbanā wa lā tuḥammilnā mā lā ṭāqata lanā bih, wa'fu 'annā waghfir lanā warḥamnā, Anta mawlānā fanṣurnā 'alal-qawmil-kāfirīn",
            "Aye hamare Rab, agar hum bhool jaayein ya ghalti karein to hamari pakad na farma. Aye hamare Rab, hum par aisa bojh na daal jaisa humse pehle logon par daala. Aye hamare Rab, hum par woh bojh na daal jiski humein taaqat nahi. Humein maaf farma, bakhsh de aur hum par reham farma. Tu hi hamara Maula hai, pas kaafir qaum ke muqaable mein hamari madad farma.",
            "اے ہمارے رب! اگر ہم بھول جائیں یا غلطی کریں تو ہماری پکڑ نہ فرما۔ اے ہمارے رب! ہم پر ایسا بوجھ نہ ڈال جیسا ہم سے پہلے لوگوں پر ڈالا۔ اے ہمارے رب! ہم پر وہ بوجھ نہ ڈال جس کی ہمیں طاقت نہیں۔ ہمیں معاف فرما، بخش دے اور ہم پر رحم فرما۔ تو ہی ہمارا مولیٰ ہے، پس کافر قوم کے مقابلے میں ہماری مدد فرما",
            "Jo raat ko Surah Baqarah ki aakhri do ayaat parhe, woh use kaafi ho jaati hain.",
            "The Noble Quran", "Surah Al-Baqarah (2:286); Sahih al-Bukhari 5009", "Ayat Qurani; hadith Sahih"
        ),

        // ---------------- Protection & Healing ----------------
        d(
            "dua_visiting_sick", "Protection & Healing", "Visiting the Sick",
            "Mareez ki iyaadat ke waqt saat martaba.",
            "أَسْأَلُ اللَّهَ الْعَظِيمَ رَبَّ الْعَرْشِ الْعَظِيمِ أَنْ يَشْفِيَكَ",
            "As'alullāhal-'Aẓīma Rabbal-'Arshil-'Aẓīmi an yashfiyak",
            "Main Azmat wale Allah se, jo Arsh-e-Azeem ka Rab hai, sawaal karta hoon ke tumhein shifa de.",
            "میں عظمت والے اللہ سے جو عرش عظیم کا رب ہے سوال کرتا ہوں کہ تمہیں شفا دے",
            "Agar maut ka waqt na aaya ho to Allah use shifa de deta hai.",
            "Sunan Abi Dawud", "Hadith 3106; Jami` at-Tirmidhi 2083", "Sahih / Hasan", 7
        ),
        d(
            "dua_ruqyah_pain", "Protection & Healing", "For Bodily Pain",
            "Dard ki jagah haath rakh kar teen baar Bismillah, phir saat martaba yeh dua.",
            "أَعُوذُ بِاللَّهِ وَقُدْرَتِهِ مِنْ شَرِّ مَا أَجِدُ وَأُحَاذِرُ",
            "A'ūdhu billāhi wa qudratihī min sharri mā ajidu wa uḥādhir",
            "Main Allah aur uski qudrat ki panaah maangta hoon us takleef ke shar se jo main mehsoos karta hoon aur jiska mujhe khauf hai.",
            "میں اللہ اور اس کی قدرت کی پناہ مانگتا ہوں اس تکلیف کے شر سے جو میں محسوس کرتا ہوں اور جس کا مجھے خوف ہے",
            "Nabi ﷺ ne Hazrat Usman bin Abil-Aas (RA) ko dard ke liye sikhayi.",
            "Sahih Muslim", "Hadith 2202", "Sahih", 7
        ),
        d(
            "dua_ruqyah_nabawi", "Protection & Healing", "Prophetic Ruqyah for the Sick",
            "Mareez par haath pher kar.",
            "اللَّهُمَّ رَبَّ النَّاسِ، أَذْهِبِ الْبَأْسَ، اشْفِ أَنْتَ الشَّافِي، لَا شِفَاءَ إِلَّا شِفَاؤُكَ، شِفَاءً لَا يُغَادِرُ سَقَمًا",
            "Allāhumma Rabban-nās, adhhibil-ba's, ishfi Antash-Shāfī, lā shifā'a illā shifā'uk, shifā'an lā yughādiru saqamā",
            "Ya Allah, logon ke Rab! Takleef door farma, shifa de, Tu hi shifa dene wala hai, Teri shifa ke siwa koi shifa nahi, aisi shifa jo koi bimaari na chhode.",
            "اے اللہ! لوگوں کے رب! تکلیف دور فرما، شفا دے، تو ہی شفا دینے والا ہے، تیری شفا کے سوا کوئی شفا نہیں، ایسی شفا جو کوئی بیماری نہ چھوڑے",
            "Nabi ﷺ apne ghar walon ki iyaadat mein yeh parhte the.",
            "Sahih al-Bukhari", "Hadith 5743; Sahih Muslim 2191", "Sahih"
        ),
        d(
            "dua_kalimat_tammat", "Protection & Healing", "Seeking Refuge in Perfect Words",
            "Kisi jagah padaao karte waqt ya shaam ko.",
            "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            "A'ūdhu bikalimātillāhit-tāmmāti min sharri mā khalaq",
            "Main Allah ke mukammal kalimaat ki panaah maangta hoon har us cheez ke shar se jo usne paida ki.",
            "میں اللہ کے کامل کلمات کی پناہ مانگتا ہوں ہر اس چیز کے شر سے جو اس نے پیدا کی",
            "Jo kisi jagah utar kar yeh parhe, wahan se jaane tak koi cheez use nuqsaan nahi degi.",
            "Sahih Muslim", "Hadith 2708", "Sahih", 3
        ),
        d(
            "dua_muawwidhat", "Protection & Healing", "The Three Quls (Mu'awwidhat)",
            "Subah o shaam teenon soortein teen teen martaba, aur sone se pehle haathon par dam kar ke jism par pherna.",
            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nقُلْ هُوَ اللَّهُ أَحَدٌ (١) اللَّهُ الصَّمَدُ (٢) لَمْ يَلِدْ وَلَمْ يُولَدْ (٣) وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ (٤)\n\n" +
                "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nقُلْ أَعُوذُ بِرَبِّ الْفَلَقِ (١) مِن شَرِّ مَا خَلَقَ (٢) وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ (٣) وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ (٤) وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ (٥)\n\n" +
                "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nقُلْ أَعُوذُ بِرَبِّ النَّاسِ (١) مَلِكِ النَّاسِ (٢) إِلَٰهِ النَّاسِ (٣) مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ (٤) الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ (٥) مِنَ الْجِنَّةِ وَالنَّاسِ (٦)",
            "Bismillāhir-Raḥmānir-Raḥīm. Qul huwallāhu aḥad. Allāhuṣ-Ṣamad. Lam yalid wa lam yūlad. Wa lam yakul-lahū kufuwan aḥad.\n\n" +
                "Bismillāhir-Raḥmānir-Raḥīm. Qul a'ūdhu bi-Rabbil-falaq. Min sharri mā khalaq. Wa min sharri ghāsiqin idhā waqab. Wa min sharrin-naffāthāti fil-'uqad. Wa min sharri ḥāsidin idhā ḥasad.\n\n" +
                "Bismillāhir-Raḥmānir-Raḥīm. Qul a'ūdhu bi-Rabbin-nās. Malikin-nās. Ilāhin-nās. Min sharril-waswāsil-khannās. Alladhī yuwaswisu fī ṣudūrin-nās. Minal-jinnati wan-nās.",
            "(Surah Ikhlas) Kaho: woh Allah ek hai. Allah be-niyaaz hai. Na usne kisi ko jana, na woh kisi se jana gaya. Aur koi uska hamsar nahi.\n\n" +
                "(Surah Falaq) Kaho: main subah ke Rab ki panaah maangta hoon, har us cheez ke shar se jo usne paida ki, aur andheri raat ke shar se jab woh chha jaaye, aur girhon mein phoonkne waliyon ke shar se, aur hasad karne wale ke shar se jab woh hasad kare.\n\n" +
                "(Surah Naas) Kaho: main insaanon ke Rab ki panaah maangta hoon, insaanon ke Badshah ki, insaanon ke Mabood ki, us waswasa daalne wale ke shar se jo (Allah ka zikr sun kar) peechhe hat jaata hai, jo logon ke dilon mein waswasa daalta hai, chahe woh jinnon mein se ho ya insaanon mein se.",
            "(سورۃ الاخلاص) کہو: وہ اللہ ایک ہے۔ اللہ بے نیاز ہے۔ نہ اس نے کسی کو جنا اور نہ وہ جنا گیا۔ اور کوئی اس کا ہمسر نہیں۔\n\n" +
                "(سورۃ الفلق) کہو: میں صبح کے رب کی پناہ مانگتا ہوں، ہر اس چیز کے شر سے جو اس نے پیدا کی، اور اندھیری رات کے شر سے جب وہ چھا جائے، اور گرہوں میں پھونکنے والیوں کے شر سے، اور حسد کرنے والے کے شر سے جب وہ حسد کرے۔\n\n" +
                "(سورۃ الناس) کہو: میں انسانوں کے رب کی پناہ مانگتا ہوں، انسانوں کے بادشاہ کی، انسانوں کے معبود کی، اس وسوسہ ڈالنے والے کے شر سے جو (اللہ کا ذکر سن کر) پیچھے ہٹ جاتا ہے، جو لوگوں کے دلوں میں وسوسہ ڈالتا ہے، خواہ وہ جنوں میں سے ہو یا انسانوں میں سے۔",
            "Subah o shaam teen martaba parhna har cheez se kaafi ho jaata hai.",
            "Jami` at-Tirmidhi", "Hadith 3575; Sunan Abi Dawud 5082; Sahih al-Bukhari 5017; Surah Al-Ikhlas (112), Al-Falaq (113), An-Nas (114)", "Hasan Sahih", 3
        ),
        d(
            "dua_evil_eye", "Protection & Healing", "Protection for Children (Evil Eye)",
            "Nabi ﷺ Hazrat Hasan aur Hazrat Husain (RA) par yeh dam karte the.",
            "أُعِيذُكُمَا بِكَلِمَاتِ اللَّهِ التَّامَّةِ، مِنْ كُلِّ شَيْطَانٍ وَهَامَّةٍ، وَمِنْ كُلِّ عَيْنٍ لَامَّةٍ",
            "U'īdhukumā bikalimātillāhit-tāmmah, min kulli shayṭāniw-wa hāmmah, wa min kulli 'aynin lāmmah",
            "Main tum dono ko Allah ke mukammal kalimaat ki panaah mein deta hoon har shaitan aur zehreele jaanwar se, aur har nuqsaan pahunchane wali nazar se.",
            "میں تم دونوں کو اللہ کے کامل کلمات کی پناہ میں دیتا ہوں ہر شیطان اور زہریلے جانور سے اور ہر لگ جانے والی نظر سے",
            "Bachon ko nazar-e-bad aur shar se bachane ki masnoon dua (ek bache ke liye 'U'īdhuka').",
            "Sahih al-Bukhari", "Hadith 3371", "Sahih"
        ),
        d(
            "dua_seeing_afflicted", "Protection & Healing", "Seeing Someone in Trial",
            "Kisi ko musibat mein dekh kar (dil mein parhein).",
            "الْحَمْدُ لِلَّهِ الَّذِي عَافَانِي مِمَّا ابْتَلَاكَ بِهِ، وَفَضَّلَنِي عَلَىٰ كَثِيرٍ مِمَّنْ خَلَقَ تَفْضِيلًا",
            "Al-ḥamdu lillāhil-ladhī 'āfānī mimmabtalāka bih, wa faḍḍalanī 'alā kathīrim-mimman khalaqa tafḍīlā",
            "Saari tareef Allah ke liye jisne mujhe us se aafiyat di jismein tumhein mubtala kiya, aur mujhe apni bohot si makhlooq par fazeelat di.",
            "تمام تعریفیں اللہ کے لیے جس نے مجھے اس سے عافیت دی جس میں تجھے مبتلا کیا، اور مجھے اپنی بہت سی مخلوق پر فضیلت دی",
            "Parhne wala us musibat se mehfooz rehta hai.",
            "Jami` at-Tirmidhi", "Hadith 3432", "Hasan"
        ),
        d(
            "dua_istikhara", "Protection & Healing", "Salat al-Istikhara",
            "Kisi jaaiz kaam mein khair talab karne ke liye do rakat nafl ke baad.",
            "اللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ، وَأَسْتَقْدِرُكَ بِقُدْرَتِكَ، وَأَسْأَلُكَ مِنْ فَضْلِكَ الْعَظِيمِ، فَإِنَّكَ تَقْدِرُ وَلَا أَقْدِرُ، وَتَعْلَمُ وَلَا أَعْلَمُ، وَأَنْتَ عَلَّامُ الْغُيُوبِ، اللَّهُمَّ إِنْ كُنْتَ تَعْلَمُ أَنَّ هَٰذَا الْأَمْرَ خَيْرٌ لِي فِي دِينِي وَمَعَاشِي وَعَاقِبَةِ أَمْرِي فَاقْدُرْهُ لِي وَيَسِّرْهُ لِي ثُمَّ بَارِكْ لِي فِيهِ، وَإِنْ كُنْتَ تَعْلَمُ أَنَّ هَٰذَا الْأَمْرَ شَرٌّ لِي فِي دِينِي وَمَعَاشِي وَعَاقِبَةِ أَمْرِي فَاصْرِفْهُ عَنِّي وَاصْرِفْنِي عَنْهُ، وَاقْدُرْ لِيَ الْخَيْرَ حَيْثُ كَانَ ثُمَّ أَرْضِنِي بِهِ",
            "Allāhumma innī astakhīruka bi'ilmik, wa astaqdiruka biqudratik, wa as'aluka min faḍlikal-'aẓīm, fa innaka taqdiru wa lā aqdir, wa ta'lamu wa lā a'lam, wa Anta 'allāmul-ghuyūb. Allāhumma in kunta ta'lamu anna hādhal-amra khayrul-lī fī dīnī wa ma'āshī wa 'āqibati amrī faqdurhu lī wa yassirhu lī thumma bārik lī fīh, wa in kunta ta'lamu anna hādhal-amra sharrul-lī fī dīnī wa ma'āshī wa 'āqibati amrī faṣrifhu 'annī waṣrifnī 'anh, waqdur liyal-khayra ḥaythu kāna thumma arḍinī bih",
            "Ya Allah, main Tere ilm ke zariye Tujh se khair talab karta hoon, Teri qudrat ke zariye taaqat maangta hoon, aur Tere fazl-e-azeem ka sawaal karta hoon. Tu qudrat rakhta hai main nahi, Tu jaanta hai main nahi, aur Tu ghaib ka khoob jaanne wala hai. Ya Allah, agar Tere ilm mein yeh kaam (apna kaam zehen mein laayein) mere deen, meri zindagi aur anjaam ke liye behtar hai to ise mere liye muqaddar kar, aasaan kar aur phir usmein barkat de. Aur agar Tere ilm mein yeh kaam mere deen, zindagi aur anjaam ke liye bura hai to ise mujh se aur mujhe is se pher de, aur mere liye bhalai muqaddar kar jahan bhi ho, phir mujhe us par raazi kar de.",
            "اے اللہ! میں تیرے علم کے ذریعے تجھ سے خیر طلب کرتا ہوں، تیری قدرت کے ذریعے طاقت مانگتا ہوں اور تیرے فضل عظیم کا سوال کرتا ہوں، تو قدرت رکھتا ہے میں نہیں رکھتا، تو جانتا ہے میں نہیں جانتا، اور تو غیبوں کا خوب جاننے والا ہے۔ اے اللہ! اگر تیرے علم میں یہ کام میرے دین، میری معاش اور انجام کار کے لیے بہتر ہے تو اسے میرے لیے مقدر کر دے، آسان کر دے، پھر اس میں برکت دے۔ اور اگر تیرے علم میں یہ کام میرے دین، معاش اور انجام کار کے لیے برا ہے تو اسے مجھ سے اور مجھے اس سے پھیر دے، اور میرے لیے بھلائی مقدر کر جہاں بھی ہو، پھر مجھے اس پر راضی کر دے",
            "Nabi ﷺ Sahaba ko har kaam mein istikhara aise sikhate jaise Quran ki soorat.",
            "Sahih al-Bukhari", "Hadith 1162", "Sahih"
        )
    )
}
