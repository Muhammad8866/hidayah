package com.example.domain.model

// Fourth batch of authentic duas (Ahle Sunnat wal Jamaat sources, with references).
internal object MoreDuas4 {

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
        // ---------------- Anxiety & Distress ----------------
        d(
            "dua_allahu_rabbi", "Anxiety & Distress", "Allah, Allah is My Lord",
            "Gham ya pareshani ke waqt — Nabi ﷺ ne Hazrat Asma bint Umais (RA) ko sikhayi.",
            "اللَّهُ اللَّهُ رَبِّي لَا أُشْرِكُ بِهِ شَيْئًا",
            "Allāhu Allāhu Rabbī lā ushriku bihī shay'ā",
            "Allah, Allah mera Rab hai, main uske saath kisi cheez ko shareek nahi karta.",
            "اللہ، اللہ میرا رب ہے، میں اس کے ساتھ کسی چیز کو شریک نہیں کرتا",
            "Pareshani ke waqt tauheed ka sahaara.",
            "Sunan Abi Dawud", "Hadith 1525; Sunan Ibn Majah 3882", "Sahih"
        ),
        d(
            "dua_rahmataka_arju", "Anxiety & Distress", "Your Mercy I Hope For",
            "Pareshaan haal ki dua (Da'awatul-makroob).",
            "اللَّهُمَّ رَحْمَتَكَ أَرْجُو، فَلَا تَكِلْنِي إِلَىٰ نَفْسِي طَرْفَةَ عَيْنٍ، وَأَصْلِحْ لِي شَأْنِي كُلَّهُ، لَا إِلَٰهَ إِلَّا أَنْتَ",
            "Allāhumma raḥmataka arjū, falā takilnī ilā nafsī ṭarfata 'ayn, wa aṣliḥ lī sha'nī kullah, lā ilāha illā Ant",
            "Ya Allah, main Teri rehmat ka ummeedwaar hoon, pas mujhe palak jhapakne ke barabar bhi mere nafs ke hawaale na kar, aur mere saare kaam durust farma de. Tere siwa koi mabood nahi.",
            "اے اللہ! میں تیری رحمت کا امیدوار ہوں، پس مجھے پلک جھپکنے کے برابر بھی میرے نفس کے حوالے نہ کر، اور میرے سارے کام درست فرما دے، تیرے سوا کوئی معبود نہیں",
            "Nabi ﷺ ne ise pareshaan haal ki dua farmaya.",
            "Sunan Abi Dawud", "Hadith 5090", "Hasan"
        ),
        d(
            "dua_against_enemy_ahzab", "Anxiety & Distress", "Against the Enemy's Armies",
            "Dushman ke muqaable mein — Ghazwa-e-Ahzaab ki dua.",
            "اللَّهُمَّ مُنْزِلَ الْكِتَابِ، سَرِيعَ الْحِسَابِ، اهْزِمِ الْأَحْزَابَ، اللَّهُمَّ اهْزِمْهُمْ وَزَلْزِلْهُمْ",
            "Allāhumma munzilal-kitāb, sarī'al-ḥisāb, ihzimil-aḥzāb, Allāhummahzimhum wa zalzilhum",
            "Ya Allah, kitaab utaarne wale, jald hisaab lene wale! Lashkaron ko shikast de. Ya Allah, unhein shikast de aur unke qadam hila de.",
            "اے اللہ! کتاب نازل کرنے والے، جلد حساب لینے والے! لشکروں کو شکست دے، اے اللہ! انہیں شکست دے اور ان کے قدم ہلا دے",
            "Nabi ﷺ ki dushman ke khilaaf dua.",
            "Sahih al-Bukhari", "Hadith 2933; Sahih Muslim 1742", "Sahih"
        ),
        d(
            "dua_anta_adudi", "Anxiety & Distress", "You Are My Support",
            "Muqaabla ya bade challenge se pehle.",
            "اللَّهُمَّ أَنْتَ عَضُدِي وَنَصِيرِي، بِكَ أَحُولُ، وَبِكَ أَصُولُ، وَبِكَ أُقَاتِلُ",
            "Allāhumma Anta 'aḍudī wa naṣīrī, bika aḥūlu, wa bika aṣūlu, wa bika uqātil",
            "Ya Allah, Tu hi mera baazu aur madadgaar hai. Tere hi zariye main tadbeer karta hoon, Tere hi zariye hamla karta hoon aur Tere hi zariye ladta hoon.",
            "اے اللہ! تو ہی میرا بازو اور مددگار ہے، تیرے ہی ذریعے میں تدبیر کرتا ہوں، تیرے ہی ذریعے حملہ کرتا ہوں اور تیرے ہی ذریعے لڑتا ہوں",
            "Nabi ﷺ jung ke waqt yeh farmate.",
            "Sunan Abi Dawud", "Hadith 2632; Jami` at-Tirmidhi 3584", "Sahih"
        ),
        d(
            "dua_ikfinihim", "Anxiety & Distress", "Suffice Me Against Them",
            "Zaalimon ke shar se — Ashaab-ul-Ukhdood ke ladke ki dua.",
            "اللَّهُمَّ اكْفِنِيهِمْ بِمَا شِئْتَ",
            "Allāhummak-fīnīhim bimā shi't",
            "Ya Allah, jaise Tu chahe unke muqaable mein mere liye kaafi ho ja.",
            "اے اللہ! جیسے تو چاہے ان کے مقابلے میں میرے لیے کافی ہو جا",
            "Allah ne ladke ko zaalimon se bacha liya.",
            "Sahih Muslim", "Hadith 3005", "Sahih"
        ),
        d(
            "dua_qadarullah", "Anxiety & Distress", "When Something Bad Happens",
            "Koi naagawaar baat ho jaaye to 'kaash' kehne ke bajaaye.",
            "قَدَرُ اللَّهِ وَمَا شَاءَ فَعَلَ",
            "Qadarullāhi wa mā shā'a fa'al",
            "Allah ki taqdeer hai, aur usne jo chaaha kiya.",
            "اللہ کی تقدیر ہے اور اس نے جو چاہا کیا",
            "'Kaash' Shaitan ka darwaaza kholta hai.",
            "Sahih Muslim", "Hadith 2664", "Sahih"
        ),
        d(
            "dua_ala_kulli_hal", "Anxiety & Distress", "Praise in Every Situation",
            "Koi naapasand baat pesh aaye to.",
            "الْحَمْدُ لِلَّهِ عَلَىٰ كُلِّ حَالٍ",
            "Al-ḥamdu lillāhi 'alā kulli ḥāl",
            "Har haal mein saari tareef Allah ke liye hai.",
            "ہر حال میں تمام تعریف اللہ کے لیے ہے",
            "Nabi ﷺ naapasand cheez par yeh farmate.",
            "Sunan Ibn Majah", "Hadith 3803", "Hasan"
        ),
        d(
            "dua_dogs_barking", "Anxiety & Distress", "Hearing Dogs Bark at Night",
            "Raat ko kutton ke bhaunkne ya gadhe ke rengne par.",
            "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ",
            "A'ūdhu billāhi minash-shayṭānir-rajīm",
            "Main Allah ki panaah maangta hoon mardood Shaitan se.",
            "میں اللہ کی پناہ مانگتا ہوں شیطان مردود سے",
            "Woh aisi cheezein dekhte hain jo tum nahi dekhte.",
            "Sunan Abi Dawud", "Hadith 5103", "Sahih"
        ),

        // ---------------- Travel & Leaving Home ----------------
        d(
            "dua_leaving_home_adilla", "Travel & Leaving Home", "Refuge from Going Astray",
            "Ghar se nikalte waqt — Hazrat Umm-e-Salama (RA) ki riwayat.",
            "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ أَنْ أَضِلَّ أَوْ أُضَلَّ، أَوْ أَزِلَّ أَوْ أُزَلَّ، أَوْ أَظْلِمَ أَوْ أُظْلَمَ، أَوْ أَجْهَلَ أَوْ يُجْهَلَ عَلَيَّ",
            "Allāhumma innī a'ūdhu bika an aḍilla aw uḍall, aw azilla aw uzall, aw aẓlima aw uẓlam, aw ajhala aw yujhala 'alayy",
            "Ya Allah, main Teri panaah maangta hoon is se ke main gumraah hoon ya gumraah kiya jaaoon, phisloon ya phisla diya jaaoon, zulm karoon ya mujh par zulm ho, jahaalat karoon ya mere saath jahaalat ki jaaye.",
            "اے اللہ! میں تیری پناہ مانگتا ہوں اس سے کہ میں گمراہ ہوں یا گمراہ کیا جاؤں، پھسلوں یا پھسلا دیا جاؤں، ظلم کروں یا مجھ پر ظلم ہو، جہالت کروں یا مجھ سے جہالت کی جائے",
            "Nabi ﷺ jab bhi ghar se nikalte to yeh parhte.",
            "Sunan Abi Dawud", "Hadith 5094; Jami` at-Tirmidhi 3427", "Sahih (Imam Tirmizi: Hasan Sahih)"
        ),
        d(
            "dua_ascending_descending", "Travel & Leaving Home", "Going Up and Coming Down",
            "Safar mein charhai par 'Allahu Akbar', utraai par 'SubhanAllah'.",
            "اللَّهُ أَكْبَرُ — سُبْحَانَ اللَّهِ",
            "Allāhu akbar — Subḥānallāh",
            "Allah sab se bara hai (charhai par). — Allah paak hai (utraai par).",
            "اللہ سب سے بڑا ہے (چڑھائی پر) — اللہ پاک ہے (اترائی پر)",
            "Sahaba (RA) ka safar mein ma'mool.",
            "Sahih al-Bukhari", "Hadith 2993", "Sahih"
        ),
        d(
            "dua_traveller_sahar", "Travel & Leaving Home", "Traveller's Dua Before Dawn",
            "Safar mein sehri ke waqt.",
            "سَمَّعَ سَامِعٌ بِحَمْدِ اللَّهِ وَحُسْنِ بَلَائِهِ عَلَيْنَا، رَبَّنَا صَاحِبْنَا وَأَفْضِلْ عَلَيْنَا، عَائِذًا بِاللَّهِ مِنَ النَّارِ",
            "Sammi'a sāmi'um-biḥamdillāhi wa ḥusni balā'ihī 'alaynā, Rabbanā ṣāḥibnā wa afḍil 'alaynā, 'ā'idham-billāhi minan-nār",
            "Sunne wale ne Allah ki tareef aur hum par uske achhe inaamaat ko (doosron tak) suna diya. Aye hamare Rab, hamara saathi ban aur hum par fazl farma — main aag se Allah ki panaah maangte hue (yeh kehta hoon).",
            "سننے والے نے اللہ کی تعریف اور ہم پر اس کے اچھے انعامات کو سنا دیا، اے ہمارے رب! ہمارا ساتھی بن اور ہم پر فضل فرما، آگ سے اللہ کی پناہ مانگتے ہوئے",
            "Nabi ﷺ safar mein sehri ke waqt yeh farmate.",
            "Sahih Muslim", "Hadith 2718", "Sahih"
        ),
        d(
            "dua_zawwadakallah", "Travel & Leaving Home", "To the Traveller: May Allah Provide Taqwa",
            "Musafir ko rukhsat karte waqt.",
            "زَوَّدَكَ اللَّهُ التَّقْوَىٰ، وَغَفَرَ ذَنْبَكَ، وَيَسَّرَ لَكَ الْخَيْرَ حَيْثُمَا كُنْتَ",
            "Zawwadakallāhut-taqwā, wa ghafara dhanbak, wa yassara lakal-khayra ḥaythumā kunt",
            "Allah tumhein taqwa ka zaad-e-raah de, tumhare gunaah maaf kare aur tum jahan bhi ho tumhare liye bhalai aasaan kare.",
            "اللہ تمہیں تقویٰ کا زاد راہ دے، تمہارے گناہ معاف کرے اور تم جہاں بھی ہو تمہارے لیے بھلائی آسان کرے",
            "Nabi ﷺ ne safar par jaane wale ko yeh dua di.",
            "Jami` at-Tirmidhi", "Hadith 3444", "Hasan Gharib (Imam Tirmizi)"
        ),
        d(
            "dua_traveller_to_resident", "Travel & Leaving Home", "Traveller to Those Staying Behind",
            "Safar par jaane wala ghar walon ko rukhsat karte waqt.",
            "أَسْتَوْدِعُكُمُ اللَّهَ الَّذِي لَا تَضِيعُ وَدَائِعُهُ",
            "Astawdi'ukumullāhal-ladhī lā taḍī'u wadā'i'uh",
            "Main tumhein us Allah ke supurd karta hoon jiske paas rakhi amaanatein zaaya nahi hotin.",
            "میں تمہیں اس اللہ کے سپرد کرتا ہوں جس کے پاس رکھی امانتیں ضائع نہیں ہوتیں",
            "Ghar walon ki hifazat Allah ke supurd.",
            "Sunan Ibn Majah", "Hadith 2825; Musnad Ahmad 9230", "Hasan"
        ),
        d(
            "dua_entering_village", "Travel & Leaving Home", "Entering a Town or Village",
            "Kisi basti/shehar mein daakhil hone se pehle.",
            "اللَّهُمَّ رَبَّ السَّمَاوَاتِ السَّبْعِ وَمَا أَظْلَلْنَ، وَرَبَّ الْأَرَضِينَ السَّبْعِ وَمَا أَقْلَلْنَ، وَرَبَّ الشَّيَاطِينِ وَمَا أَضْلَلْنَ، وَرَبَّ الرِّيَاحِ وَمَا ذَرَيْنَ، فَإِنَّا نَسْأَلُكَ خَيْرَ هَٰذِهِ الْقَرْيَةِ وَخَيْرَ أَهْلِهَا، وَنَعُوذُ بِكَ مِنْ شَرِّهَا وَشَرِّ أَهْلِهَا وَشَرِّ مَا فِيهَا",
            "Allāhumma Rabbas-samāwātis-sab'i wa mā aẓlaln, wa Rabbal-araḍīnas-sab'i wa mā aqlaln, wa Rabbash-shayāṭīni wa mā aḍlaln, wa Rabbar-riyāḥi wa mā dharayn, fa-innā nas'aluka khayra hādhihil-qaryati wa khayra ahlihā, wa na'ūdhu bika min sharrihā wa sharri ahlihā wa sharri mā fīhā",
            "Ya Allah, saat aasmaanon aur jin par unka saaya hai unke Rab, saat zameenon aur jo unhon ne uthaya hai unke Rab, shaitanon aur jinhein unhon ne gumraah kiya unke Rab, hawaaon aur jo unhon ne uraaya unke Rab! Hum Tujh se is basti ki aur iske rehne walon ki bhalai maangte hain, aur iske, iske logon ke aur jo is mein hai uske shar se Teri panaah maangte hain.",
            "اے اللہ! سات آسمانوں اور جن پر ان کا سایہ ہے ان کے رب، سات زمینوں اور جو انہوں نے اٹھایا ان کے رب، شیطانوں اور جنہیں انہوں نے گمراہ کیا ان کے رب، ہواؤں اور جو انہوں نے اڑایا ان کے رب! ہم تجھ سے اس بستی کی اور اس کے رہنے والوں کی بھلائی مانگتے ہیں اور اس کے، اس کے لوگوں کے اور جو اس میں ہے اس کے شر سے تیری پناہ مانگتے ہیں",
            "Nabi ﷺ jis basti mein daakhil hona chahte yeh parhte.",
            "Sunan an-Nasa'i al-Kubra", "Hadith 8826; Sahih Ibn Hibban 2709", "Hasan (Hafiz Ibn Hajar)"
        ),
        d(
            "dua_vehicle_stumbles", "Travel & Leaving Home", "When the Vehicle Stumbles",
            "Sawari phisle ya thokar lage to 'Shaitan barbaad ho' kehne ke bajaaye.",
            "بِسْمِ اللَّهِ",
            "Bismillāh",
            "Allah ke naam se.",
            "اللہ کے نام سے",
            "Bismillah kehne se Shaitan makkhi jaisa chhota ho jaata hai.",
            "Sunan Abi Dawud", "Hadith 4982", "Sahih"
        ),

        // ---------------- Protection & Healing (refuge) ----------------
        d(
            "dua_ajz_kasal_haram", "Protection & Healing", "Refuge from Weakness and Old Age",
            "Rozana panaah ki jaame dua.",
            "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْعَجْزِ وَالْكَسَلِ، وَالْجُبْنِ وَالْهَرَمِ، وَأَعُوذُ بِكَ مِنْ فِتْنَةِ الْمَحْيَا وَالْمَمَاتِ، وَأَعُوذُ بِكَ مِنْ عَذَابِ الْقَبْرِ",
            "Allāhumma innī a'ūdhu bika minal-'ajzi wal-kasal, wal-jubni wal-haram, wa a'ūdhu bika min fitnatil-maḥyā wal-mamāt, wa a'ūdhu bika min 'adhābil-qabr",
            "Ya Allah, main Teri panaah maangta hoon laachaari aur susti se, buzdili aur burhaape se, zindagi aur maut ke fitne se, aur qabr ke azaab se.",
            "اے اللہ! میں تیری پناہ مانگتا ہوں عاجزی اور سستی سے، بزدلی اور بڑھاپے سے، زندگی اور موت کے فتنے سے، اور قبر کے عذاب سے",
            "Nabi ﷺ ki kasrat se maangi jaane wali dua.",
            "Sahih al-Bukhari", "Hadith 6367; Sahih Muslim 2706", "Sahih"
        ),
        d(
            "dua_sharri_ma_amiltu", "Protection & Healing", "Refuge from the Evil of My Deeds",
            "Hazrat Aisha (RA) ke mutaabiq Nabi ﷺ ki dua.",
            "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ شَرِّ مَا عَمِلْتُ، وَمِنْ شَرِّ مَا لَمْ أَعْمَلْ",
            "Allāhumma innī a'ūdhu bika min sharri mā 'amiltu, wa min sharri mā lam a'mal",
            "Ya Allah, main Teri panaah maangta hoon us amal ke shar se jo maine kiya, aur us ke shar se jo maine nahi kiya.",
            "اے اللہ! میں تیری پناہ مانگتا ہوں اس عمل کے شر سے جو میں نے کیا اور اس کے شر سے جو میں نے نہیں کیا",
            "Kiye aur na-kiye dono ke anjaam se hifazat.",
            "Sahih Muslim", "Hadith 2716", "Sahih"
        ),
        d(
            "dua_jahdil_bala", "Protection & Healing", "Refuge from Severe Trials",
            "Sakht aazmaaish aur dushmanon ki khushi se panaah.",
            "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ جَهْدِ الْبَلَاءِ، وَدَرَكِ الشَّقَاءِ، وَسُوءِ الْقَضَاءِ، وَشَمَاتَةِ الْأَعْدَاءِ",
            "Allāhumma innī a'ūdhu bika min jahdil-balā', wa darakish-shaqā', wa sū'il-qaḍā', wa shamātatil-a'dā'",
            "Ya Allah, main Teri panaah maangta hoon sakht aazmaaish se, bad-bakhti ke aa pakadne se, bure faisle se aur dushmanon ke hansne se.",
            "اے اللہ! میں تیری پناہ مانگتا ہوں سخت آزمائش سے، بدبختی کے آ پکڑنے سے، برے فیصلے سے اور دشمنوں کے ہنسنے سے",
            "Nabi ﷺ in chaar cheezon se panaah maangte the.",
            "Sahih al-Bukhari", "Hadith 6347; Sahih Muslim 2707", "Sahih"
        ),
        d(
            "dua_fitnat_ghina_faqr", "Protection & Healing", "Refuge from the Trials of Wealth and Poverty",
            "Qabr, jahannam, maaldaari aur ghurbat ke fitnon se panaah (dua ka hissa).",
            "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْكَسَلِ وَالْهَرَمِ، وَالْمَأْثَمِ وَالْمَغْرَمِ، وَمِنْ فِتْنَةِ الْقَبْرِ وَعَذَابِ الْقَبْرِ، وَمِنْ فِتْنَةِ النَّارِ وَعَذَابِ النَّارِ، وَمِنْ شَرِّ فِتْنَةِ الْغِنَىٰ، وَأَعُوذُ بِكَ مِنْ فِتْنَةِ الْفَقْرِ",
            "Allāhumma innī a'ūdhu bika minal-kasali wal-haram, wal-ma'thami wal-maghram, wa min fitnatil-qabri wa 'adhābil-qabr, wa min fitnatin-nāri wa 'adhābin-nār, wa min sharri fitnatil-ghinā, wa a'ūdhu bika min fitnatil-faqr",
            "Ya Allah, main Teri panaah maangta hoon susti aur burhaape se, gunaah aur qarz se, qabr ke fitne aur azaab se, aag ke fitne aur azaab se, maaldaari ke fitne ke shar se, aur ghurbat ke fitne se.",
            "اے اللہ! میں تیری پناہ مانگتا ہوں سستی اور بڑھاپے سے، گناہ اور قرض سے، قبر کے فتنے اور عذاب سے، آگ کے فتنے اور عذاب سے، مالداری کے فتنے کے شر سے اور فقر کے فتنے سے",
            "Nabi ﷺ ki jaame panaah ki dua.",
            "Sahih al-Bukhari", "Hadith 6368; Sahih Muslim 589", "Sahih"
        ),
        d(
            "dua_kufr_faqr", "Protection & Healing", "Refuge from Disbelief and Poverty",
            "Subah o shaam teen martaba.",
            "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْكُفْرِ وَالْفَقْرِ، وَأَعُوذُ بِكَ مِنْ عَذَابِ الْقَبْرِ، لَا إِلَٰهَ إِلَّا أَنْتَ",
            "Allāhumma innī a'ūdhu bika minal-kufri wal-faqr, wa a'ūdhu bika min 'adhābil-qabr, lā ilāha illā Ant",
            "Ya Allah, main kufr aur faqr se Teri panaah maangta hoon, aur qabr ke azaab se Teri panaah maangta hoon. Tere siwa koi mabood nahi.",
            "اے اللہ! میں کفر اور فقر سے تیری پناہ مانگتا ہوں، اور قبر کے عذاب سے تیری پناہ مانگتا ہوں، تیرے سوا کوئی معبود نہیں",
            "Hazrat Abu Bakrah (RA) ne Nabi ﷺ ko yeh parhte suna.",
            "Sunan Abi Dawud", "Hadith 5090", "Hasan", 3
        ),
        d(
            "dua_sharri_sami", "Protection & Healing", "Refuge from the Evil of My Senses",
            "Hazrat Shakal bin Humaid (RA) ko sikhayi gayi.",
            "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ شَرِّ سَمْعِي، وَمِنْ شَرِّ بَصَرِي، وَمِنْ شَرِّ لِسَانِي، وَمِنْ شَرِّ قَلْبِي، وَمِنْ شَرِّ مَنِيِّي",
            "Allāhumma innī a'ūdhu bika min sharri sam'ī, wa min sharri baṣarī, wa min sharri lisānī, wa min sharri qalbī, wa min sharri maniyyī",
            "Ya Allah, main Teri panaah maangta hoon apne kaanon ke shar se, apni aankhon ke shar se, apni zubaan ke shar se, apne dil ke shar se aur apni shehwat ke shar se.",
            "اے اللہ! میں تیری پناہ مانگتا ہوں اپنے کانوں کے شر سے، اپنی آنکھوں کے شر سے، اپنی زبان کے شر سے، اپنے دل کے شر سے اور اپنی شہوت کے شر سے",
            "Aaza aur nafs ko gunaah se bachane ki dua.",
            "Sunan Abi Dawud", "Hadith 1551; Jami` at-Tirmidhi 3492", "Sahih"
        ),
        d(
            "dua_hadm_gharaq", "Protection & Healing", "Refuge from Sudden Death and Accidents",
            "Haadsaat aur buri maut se panaah.",
            "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَدْمِ، وَأَعُوذُ بِكَ مِنَ التَّرَدِّي، وَأَعُوذُ بِكَ مِنَ الْغَرَقِ وَالْحَرَقِ وَالْهَرَمِ، وَأَعُوذُ بِكَ أَنْ يَتَخَبَّطَنِي الشَّيْطَانُ عِنْدَ الْمَوْتِ، وَأَعُوذُ بِكَ أَنْ أَمُوتَ فِي سَبِيلِكَ مُدْبِرًا، وَأَعُوذُ بِكَ أَنْ أَمُوتَ لَدِيغًا",
            "Allāhumma innī a'ūdhu bika minal-hadm, wa a'ūdhu bika minat-taraddī, wa a'ūdhu bika minal-gharaqi wal-ḥaraqi wal-haram, wa a'ūdhu bika an yatakhabbaṭaniyash-shayṭānu 'indal-mawt, wa a'ūdhu bika an amūta fī sabīlika mudbirā, wa a'ūdhu bika an amūta ladīghā",
            "Ya Allah, main Teri panaah maangta hoon (makaan) girne se, oonchai se girne se, doobne, jalne aur burhaape se, maut ke waqt Shaitan ke behkaane se, Teri raah mein peeth pher kar marne se, aur (zehreele jaanwar ke) daste se marne se.",
            "اے اللہ! میں تیری پناہ مانگتا ہوں (مکان) گرنے سے، اونچائی سے گرنے سے، ڈوبنے، جلنے اور بڑھاپے سے، موت کے وقت شیطان کے بہکانے سے، تیری راہ میں پیٹھ پھیر کر مرنے سے، اور ڈسے جانے سے مرنے سے",
            "Nabi ﷺ ki panaah ki dua.",
            "Sunan Abi Dawud", "Hadith 1552; Sunan an-Nasa'i 5531", "Sahih"
        ),
        d(
            "dua_ju_khiyanah", "Protection & Healing", "Refuge from Hunger and Betrayal",
            "Bhook aur khayanat se panaah.",
            "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْجُوعِ فَإِنَّهُ بِئْسَ الضَّجِيعُ، وَأَعُوذُ بِكَ مِنَ الْخِيَانَةِ فَإِنَّهَا بِئْسَتِ الْبِطَانَةُ",
            "Allāhumma innī a'ūdhu bika minal-jū'i fa-innahū bi'saḍ-ḍajī', wa a'ūdhu bika minal-khiyānati fa-innahā bi'satil-biṭānah",
            "Ya Allah, main bhook se Teri panaah maangta hoon kyunki woh bura saathi hai, aur khayanat se Teri panaah maangta hoon kyunki woh bura raazdaar hai.",
            "اے اللہ! میں بھوک سے تیری پناہ مانگتا ہوں کیونکہ وہ برا ساتھی ہے، اور خیانت سے تیری پناہ مانگتا ہوں کیونکہ وہ برا رازدار ہے",
            "Nabi ﷺ ki panaah ki dua.",
            "Sunan Abi Dawud", "Hadith 1547; Sunan an-Nasa'i 5468", "Hasan"
        ),

        // ---------------- Comprehensive Duas ----------------
        d(
            "dua_huda_tuqa", "Comprehensive Duas", "Guidance, Piety, Chastity and Sufficiency",
            "Nabi ﷺ ki mukhtasar jaame dua.",
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْهُدَىٰ وَالتُّقَىٰ، وَالْعَفَافَ وَالْغِنَىٰ",
            "Allāhumma innī as'alukal-hudā wat-tuqā, wal-'afāfa wal-ghinā",
            "Ya Allah, main Tujh se hidayat, taqwa, paakdamani aur be-niyaazi maangta hoon.",
            "اے اللہ! میں تجھ سے ہدایت، تقویٰ، پاکدامنی اور بے نیازی مانگتا ہوں",
            "Deen aur dunya ki chaar bunyaadi nematein.",
            "Sahih Muslim", "Hadith 2721", "Sahih"
        ),
        d(
            "dua_aslih_li_dini", "Comprehensive Duas", "Rectify My Religion, World and Hereafter",
            "Deen, dunya aur aakhirat — teenon ki islaah.",
            "اللَّهُمَّ أَصْلِحْ لِي دِينِيَ الَّذِي هُوَ عِصْمَةُ أَمْرِي، وَأَصْلِحْ لِي دُنْيَايَ الَّتِي فِيهَا مَعَاشِي، وَأَصْلِحْ لِي آخِرَتِي الَّتِي فِيهَا مَعَادِي، وَاجْعَلِ الْحَيَاةَ زِيَادَةً لِي فِي كُلِّ خَيْرٍ، وَاجْعَلِ الْمَوْتَ رَاحَةً لِي مِنْ كُلِّ شَرٍّ",
            "Allāhumma aṣliḥ lī dīniyal-ladhī huwa 'iṣmatu amrī, wa aṣliḥ lī dunyāyal-latī fīhā ma'āshī, wa aṣliḥ lī ākhiratiyal-latī fīhā ma'ādī, waj'alil-ḥayāta ziyādatal-lī fī kulli khayr, waj'alil-mawta rāḥatal-lī min kulli sharr",
            "Ya Allah, mera deen durust kar de jo mere har kaam ki hifazat hai, meri dunya durust kar de jis mein meri rozi hai, meri aakhirat durust kar de jahan mujhe lautna hai. Zindagi ko mere liye har bhalai mein izaafa bana, aur maut ko har buraai se raahat bana de.",
            "اے اللہ! میرا دین درست کر دے جو میرے ہر کام کی حفاظت ہے، میری دنیا درست کر دے جس میں میری روزی ہے، میری آخرت درست کر دے جہاں مجھے لوٹنا ہے، زندگی کو میرے لیے ہر بھلائی میں اضافہ بنا اور موت کو ہر برائی سے راحت بنا دے",
            "Nabi ﷺ ki jaame dua.",
            "Sahih Muslim", "Hadith 2720", "Sahih"
        ),
        d(
            "dua_musarrifal_qulub", "Comprehensive Duas", "Turn Our Hearts to Your Obedience",
            "Dil ko itaa'at par jamaane ki dua.",
            "اللَّهُمَّ مُصَرِّفَ الْقُلُوبِ صَرِّفْ قُلُوبَنَا عَلَىٰ طَاعَتِكَ",
            "Allāhumma muṣarrifal-qulūbi ṣarrif qulūbanā 'alā ṭā'atik",
            "Ya Allah, dilon ko pherne wale, hamare dilon ko apni itaa'at ki taraf pher de.",
            "اے اللہ! دلوں کو پھیرنے والے، ہمارے دلوں کو اپنی اطاعت کی طرف پھیر دے",
            "Tamaam insaanon ke dil Rahman ki do ungliyon ke darmiyan hain.",
            "Sahih Muslim", "Hadith 2654", "Sahih"
        ),
        d(
            "dua_jannah_nar", "Comprehensive Duas", "Asking for Jannah, Refuge from Fire",
            "Tashahhud ke baad ya har waqt.",
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْجَنَّةَ، وَأَعُوذُ بِكَ مِنَ النَّارِ",
            "Allāhumma innī as'alukal-jannah, wa a'ūdhu bika minan-nār",
            "Ya Allah, main Tujh se Jannat maangta hoon aur aag se Teri panaah maangta hoon.",
            "اے اللہ! میں تجھ سے جنت مانگتا ہوں اور آگ سے تیری پناہ مانگتا ہوں",
            "Nabi ﷺ ne farmaya: hum bhi isi ke ird gird dua karte hain.",
            "Sunan Abi Dawud", "Hadith 792; Sunan Ibn Majah 910", "Sahih"
        ),
        d(
            "dua_khayri_kullih", "Comprehensive Duas", "All Good, Now and Later",
            "Nabi ﷺ ne Hazrat Aisha (RA) ko yeh jaame dua sikhayi (dua ka hissa).",
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنَ الْخَيْرِ كُلِّهِ، عَاجِلِهِ وَآجِلِهِ، مَا عَلِمْتُ مِنْهُ وَمَا لَمْ أَعْلَمْ، وَأَعُوذُ بِكَ مِنَ الشَّرِّ كُلِّهِ، عَاجِلِهِ وَآجِلِهِ، مَا عَلِمْتُ مِنْهُ وَمَا لَمْ أَعْلَمْ، اللَّهُمَّ إِنِّي أَسْأَلُكَ الْجَنَّةَ وَمَا قَرَّبَ إِلَيْهَا مِنْ قَوْلٍ أَوْ عَمَلٍ، وَأَعُوذُ بِكَ مِنَ النَّارِ وَمَا قَرَّبَ إِلَيْهَا مِنْ قَوْلٍ أَوْ عَمَلٍ",
            "Allāhumma innī as'aluka minal-khayri kullih, 'ājilihī wa ājilih, mā 'alimtu minhu wa mā lam a'lam, wa a'ūdhu bika minash-sharri kullih, 'ājilihī wa ājilih, mā 'alimtu minhu wa mā lam a'lam, Allāhumma innī as'alukal-jannata wa mā qarraba ilayhā min qawlin aw 'amal, wa a'ūdhu bika minan-nāri wa mā qarraba ilayhā min qawlin aw 'amal",
            "Ya Allah, main Tujh se har bhalai maangta hoon — jaldi wali aur der wali, jo main jaanta hoon aur jo nahi jaanta — aur har buraai se Teri panaah maangta hoon — jaldi wali aur der wali, jo main jaanta hoon aur jo nahi jaanta. Ya Allah, main Tujh se Jannat aur us qaul o amal ka sawaal karta hoon jo uske qareeb kare, aur aag se aur us qaul o amal se panaah maangta hoon jo uske qareeb kare.",
            "اے اللہ! میں تجھ سے ہر بھلائی مانگتا ہوں، جلدی والی اور دیر والی، جو میں جانتا ہوں اور جو نہیں جانتا، اور ہر برائی سے تیری پناہ مانگتا ہوں، جلدی والی اور دیر والی، جو میں جانتا ہوں اور جو نہیں جانتا۔ اے اللہ! میں تجھ سے جنت اور اس قول و عمل کا سوال کرتا ہوں جو اس کے قریب کرے، اور آگ سے اور اس قول و عمل سے پناہ مانگتا ہوں جو اس کے قریب کرے",
            "Har khair ko samettne wali dua.",
            "Sunan Ibn Majah", "Hadith 3846; Musnad Ahmad 25019", "Sahih"
        ),
        d(
            "dua_ihdini_saddidni", "Comprehensive Duas", "Guide Me and Keep Me Upright",
            "Nabi ﷺ ne Hazrat Ali (RA) ko sikhayi.",
            "اللَّهُمَّ اهْدِنِي وَسَدِّدْنِي",
            "Allāhummahdinī wa saddidnī",
            "Ya Allah, mujhe hidayat de aur mujhe seedha rakh.",
            "اے اللہ! مجھے ہدایت دے اور مجھے سیدھا رکھ",
            "Hidayat ko raaste ki hidayat se aur sadaad ko teer ke seedhe pan se yaad karo.",
            "Sahih Muslim", "Hadith 2725", "Sahih"
        ),
        d(
            "dua_hubbaka", "Comprehensive Duas", "Asking for Allah's Love",
            "Hazrat Dawood (AS) ki dua jo Nabi ﷺ ne bayan farmayi.",
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ حُبَّكَ، وَحُبَّ مَنْ يُحِبُّكَ، وَالْعَمَلَ الَّذِي يُبَلِّغُنِي حُبَّكَ، اللَّهُمَّ اجْعَلْ حُبَّكَ أَحَبَّ إِلَيَّ مِنْ نَفْسِي وَأَهْلِي وَمِنَ الْمَاءِ الْبَارِدِ",
            "Allāhumma innī as'aluka ḥubbak, wa ḥubba may-yuḥibbuk, wal-'amalal-ladhī yuballighunī ḥubbak, Allāhummaj'al ḥubbaka aḥabba ilayya min nafsī wa ahlī wa minal-mā'il-bārid",
            "Ya Allah, main Tujh se Teri muhabbat maangta hoon, aur us ki muhabbat jo Tujh se muhabbat kare, aur aisa amal jo mujhe Teri muhabbat tak pahuncha de. Ya Allah, apni muhabbat ko meri jaan, mere ghar walon aur thande paani se bhi zyada mujhe mehboob bana de.",
            "اے اللہ! میں تجھ سے تیری محبت مانگتا ہوں، اور اس کی محبت جو تجھ سے محبت کرے، اور ایسا عمل جو مجھے تیری محبت تک پہنچا دے۔ اے اللہ! اپنی محبت کو میری جان، میرے گھر والوں اور ٹھنڈے پانی سے بھی زیادہ مجھے محبوب بنا دے",
            "Allah ki muhabbat sab se badi daulat.",
            "Jami` at-Tirmidhi", "Hadith 3490", "Hasan Gharib (Imam Tirmizi)"
        ),
        d(
            "dua_filal_khayrat", "Comprehensive Duas", "Doing Good and Leaving Evil",
            "Khwaab mein Allah ne Nabi ﷺ ko yeh dua sikhayi.",
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ فِعْلَ الْخَيْرَاتِ، وَتَرْكَ الْمُنْكَرَاتِ، وَحُبَّ الْمَسَاكِينِ، وَأَنْ تَغْفِرَ لِي وَتَرْحَمَنِي، وَإِذَا أَرَدْتَ فِتْنَةَ قَوْمٍ فَتَوَفَّنِي غَيْرَ مَفْتُونٍ، أَسْأَلُكَ حُبَّكَ، وَحُبَّ مَنْ يُحِبُّكَ، وَحُبَّ عَمَلٍ يُقَرِّبُ إِلَىٰ حُبِّكَ",
            "Allāhumma innī as'aluka fi'lal-khayrāt, wa tarkal-munkarāt, wa ḥubbal-masākīn, wa an taghfira lī wa tarḥamanī, wa idhā aradta fitnata qawmin fatawaffanī ghayra maftūn, as'aluka ḥubbak, wa ḥubba may-yuḥibbuk, wa ḥubba 'amalin yuqarribu ilā ḥubbik",
            "Ya Allah, main Tujh se naikiyan karne, buraaiyan chhodne aur miskeenon se muhabbat ka sawaal karta hoon, aur yeh ke Tu mujhe bakhsh de aur mujh par reham farma. Aur jab Tu kisi qaum ko fitne mein daalna chahe to mujhe fitne se bacha kar maut de. Main Tujh se Teri muhabbat, Tujh se muhabbat karne walon ki muhabbat, aur us amal ki muhabbat maangta hoon jo Teri muhabbat ke qareeb kare.",
            "اے اللہ! میں تجھ سے نیکیاں کرنے، برائیاں چھوڑنے اور مسکینوں سے محبت کا سوال کرتا ہوں، اور یہ کہ تو مجھے بخش دے اور مجھ پر رحم فرما، اور جب تو کسی قوم کو فتنے میں ڈالنا چاہے تو مجھے فتنے سے بچا کر موت دے، میں تجھ سے تیری محبت، تجھ سے محبت کرنے والوں کی محبت اور اس عمل کی محبت مانگتا ہوں جو تیری محبت کے قریب کرے",
            "Nabi ﷺ ne farmaya: yeh haq hai, ise seekho aur sikhao.",
            "Jami` at-Tirmidhi", "Hadith 3235", "Hasan Sahih (Imam Tirmizi; Imam Bukhari ne Sahih kaha)"
        ),
        d(
            "dua_bi_ilmikal_ghayb", "Comprehensive Duas", "Let Me Live While Life Is Good",
            "Hazrat Ammar bin Yasir (RA) ki namaz ki dua (ibtidaai hissa).",
            "اللَّهُمَّ بِعِلْمِكَ الْغَيْبَ، وَقُدْرَتِكَ عَلَى الْخَلْقِ، أَحْيِنِي مَا عَلِمْتَ الْحَيَاةَ خَيْرًا لِي، وَتَوَفَّنِي إِذَا عَلِمْتَ الْوَفَاةَ خَيْرًا لِي، اللَّهُمَّ وَأَسْأَلُكَ خَشْيَتَكَ فِي الْغَيْبِ وَالشَّهَادَةِ، وَأَسْأَلُكَ كَلِمَةَ الْحَقِّ فِي الرِّضَا وَالْغَضَبِ، وَأَسْأَلُكَ الْقَصْدَ فِي الْفَقْرِ وَالْغِنَىٰ",
            "Allāhumma bi'ilmikal-ghayb, wa qudratika 'alal-khalq, aḥyinī mā 'alimtal-ḥayāta khayral-lī, wa tawaffanī idhā 'alimtal-wafāta khayral-lī, Allāhumma wa as'aluka khashyataka fil-ghaybi wash-shahādah, wa as'aluka kalimatal-ḥaqqi fir-riḍā wal-ghaḍab, wa as'alukal-qaṣda fil-faqri wal-ghinā",
            "Ya Allah, Tere ilm-e-ghaib aur makhlooq par Teri qudrat ke waaste se — jab tak Tere ilm mein zindagi mere liye behtar ho mujhe zinda rakh, aur jab maut behtar ho to maut de. Ya Allah, main chhupe aur khule mein Tera khauf, khushi aur ghusse mein haq baat, aur ghurbat aur maaldaari mein miyaana-rawi maangta hoon.",
            "اے اللہ! تیرے علم غیب اور مخلوق پر تیری قدرت کے واسطے سے، جب تک تیرے علم میں زندگی میرے لیے بہتر ہو مجھے زندہ رکھ اور جب موت بہتر ہو تو موت دے۔ اے اللہ! میں تجھ سے چھپے اور کھلے میں تیرا خوف، خوشی اور غصے میں حق بات، اور فقر اور مالداری میں میانہ روی مانگتا ہوں",
            "Nabi ﷺ namaz mein yeh dua farmate the.",
            "Sunan an-Nasa'i", "Hadith 1305", "Sahih"
        ),
        d(
            "dua_ism_azam_mannan", "Comprehensive Duas", "Ism-e-Azam: Al-Mannan",
            "Nabi ﷺ ne farmaya is shakhs ne Ism-e-Azam ke saath dua ki.",
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ بِأَنَّ لَكَ الْحَمْدَ، لَا إِلَٰهَ إِلَّا أَنْتَ، الْمَنَّانُ، بَدِيعُ السَّمَاوَاتِ وَالْأَرْضِ، يَا ذَا الْجَلَالِ وَالْإِكْرَامِ، يَا حَيُّ يَا قَيُّومُ",
            "Allāhumma innī as'aluka bi-anna lakal-ḥamd, lā ilāha illā Ant, al-Mannān, Badī'us-samāwāti wal-arḍ, yā Dhal-Jalāli wal-Ikrām, yā Ḥayyu yā Qayyūm",
            "Ya Allah, main Tujh se sawaal karta hoon is waaste se ke saari tareef Tere liye hai, Tere siwa koi mabood nahi, Tu bohot ehsaan karne wala, aasmaanon aur zameen ko be-misaal banane wala hai. Aye Jalaal o Ikraam wale, aye Zinda, aye Qaayam rakhne wale!",
            "اے اللہ! میں تجھ سے سوال کرتا ہوں اس واسطے سے کہ تمام تعریف تیرے لیے ہے، تیرے سوا کوئی معبود نہیں، تو بہت احسان کرنے والا، آسمانوں اور زمین کو بے مثال بنانے والا ہے، اے جلال و اکرام والے، اے زندہ، اے قائم رکھنے والے!",
            "Is naam se dua ki jaaye to qubool hoti hai.",
            "Sunan Abi Dawud", "Hadith 1495; Sunan an-Nasa'i 1300", "Sahih"
        ),
        d(
            "dua_ism_azam_ahad_samad", "Comprehensive Duas", "Ism-e-Azam: Al-Ahad As-Samad",
            "Nabi ﷺ ne farmaya is ne Allah se uske Ism-e-Azam ke saath sawaal kiya.",
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ بِأَنِّي أَشْهَدُ أَنَّكَ أَنْتَ اللَّهُ، لَا إِلَٰهَ إِلَّا أَنْتَ، الْأَحَدُ الصَّمَدُ، الَّذِي لَمْ يَلِدْ وَلَمْ يُولَدْ، وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ",
            "Allāhumma innī as'aluka bi-annī ash-hadu annaka Antallāh, lā ilāha illā Ant, al-Aḥadus-Ṣamad, alladhī lam yalid wa lam yūlad, wa lam yakul-lahū kufuwan aḥad",
            "Ya Allah, main Tujh se sawaal karta hoon is waaste se ke main gawahi deta hoon ke Tu hi Allah hai, Tere siwa koi mabood nahi, akela, be-niyaaz, jisne na kisi ko jana na woh jana gaya, aur na koi uska hamsar hai.",
            "اے اللہ! میں تجھ سے سوال کرتا ہوں اس واسطے سے کہ میں گواہی دیتا ہوں کہ تو ہی اللہ ہے، تیرے سوا کوئی معبود نہیں، اکیلا، بے نیاز، جس نے نہ کسی کو جنا نہ وہ جنا گیا، اور نہ کوئی اس کا ہمسر ہے",
            "Jab is ke zariye maanga jaaye to ata hota hai, aur dua ki jaaye to qubool hoti hai.",
            "Sunan Abi Dawud", "Hadith 1493; Jami` at-Tirmidhi 3475", "Sahih"
        ),
        d(
            "dua_thabbitni_hadiyan", "Comprehensive Duas", "Make Me Firm, Guided and a Guide",
            "Nabi ﷺ ne Hazrat Jareer (RA) ke liye yeh dua ki (apne liye 'thabbitni' kehein).",
            "اللَّهُمَّ ثَبِّتْنِي، وَاجْعَلْنِي هَادِيًا مَهْدِيًّا",
            "Allāhumma thabbitnī, waj'alnī hādiyam-mahdiyyā",
            "Ya Allah, mujhe saabit qadam rakh aur mujhe hidayat dene wala aur hidayat yafta bana.",
            "اے اللہ! مجھے ثابت قدم رکھ اور مجھے ہدایت دینے والا اور ہدایت یافتہ بنا",
            "Asal dua Hazrat Jareer (RA) ke liye 'thabbit-hu' ke alfaaz se hai.",
            "Sahih al-Bukhari", "Hadith 3020; Sahih Muslim 2476", "Sahih"
        ),

        // ---------------- Forgiveness & Istighfar ----------------
        d(
            "dua_rabbighfir_tub_alayya", "Forgiveness & Istighfar", "Forgive Me and Accept My Repentance",
            "Ek majlis mein sau martaba — Nabi ﷺ ka ma'mool.",
            "رَبِّ اغْفِرْ لِي وَتُبْ عَلَيَّ، إِنَّكَ أَنْتَ التَّوَّابُ الرَّحِيمُ",
            "Rabbighfir lī wa tub 'alayy, innaka Antat-Tawwābur-Raḥīm",
            "Aye mere Rab, mujhe bakhsh de aur meri tauba qubool farma, beshak Tu hi tauba qubool karne wala, reham karne wala hai.",
            "اے میرے رب! مجھے بخش دے اور میری توبہ قبول فرما، بے شک تو ہی توبہ قبول کرنے والا مہربان ہے",
            "Sahaba ne ek majlis mein Nabi ﷺ se sau martaba yeh gin kar suna.",
            "Sunan Abi Dawud", "Hadith 1516; Jami` at-Tirmidhi 3434", "Sahih", 100
        ),

        // ---------------- Death & Janaza ----------------
        d(
            "dua_rafiq_ala", "Death & Janaza", "Dua of the Dying Person",
            "Nabi ﷺ ki aakhri dua — mareez apne liye.",
            "اللَّهُمَّ اغْفِرْ لِي وَارْحَمْنِي، وَأَلْحِقْنِي بِالرَّفِيقِ الْأَعْلَىٰ",
            "Allāhummaghfir lī warḥamnī, wa alḥiqnī bir-rafīqil-a'lā",
            "Ya Allah, mujhe bakhsh de, mujh par reham farma aur mujhe Rafeeq-e-A'la se mila de.",
            "اے اللہ! مجھے بخش دے، مجھ پر رحم فرما اور مجھے رفیق اعلیٰ سے ملا دے",
            "Nabi ﷺ ne wisaal ke waqt yeh dua farmayi.",
            "Sahih al-Bukhari", "Hadith 5674; Sahih Muslim 2444", "Sahih"
        ),
        d(
            "dua_la_bas_tahur", "Death & Janaza", "Comforting the Sick: Purification",
            "Mareez ki iyaadat mein tasalli ke liye.",
            "لَا بَأْسَ، طَهُورٌ إِنْ شَاءَ اللَّهُ",
            "Lā ba's, ṭahūrun in shā' Allāh",
            "Koi harj nahi, in sha Allah (yeh bimaari gunaahon se) paak karne wali hai.",
            "کوئی حرج نہیں، ان شاء اللہ (یہ بیماری گناہوں سے) پاک کرنے والی ہے",
            "Nabi ﷺ mareez ke paas jaate to yeh farmate.",
            "Sahih al-Bukhari", "Hadith 3616, 5656", "Sahih"
        ),
        d(
            "dua_talqeen", "Death & Janaza", "Talqeen for the Dying",
            "Qareeb-ul-maut shakhs ke paas kalima parhein (usse zabardasti na kahein).",
            "لَا إِلَٰهَ إِلَّا اللَّهُ",
            "Lā ilāha illallāh",
            "Allah ke siwa koi mabood nahi.",
            "اللہ کے سوا کوئی معبود نہیں",
            "Jiska aakhri kalaam 'La ilaha illallah' ho woh Jannat mein daakhil hoga (Abu Dawud 3116).",
            "Sahih Muslim", "Hadith 916; Sunan Abi Dawud 3116", "Sahih"
        ),
        d(
            "dua_ahyini_ma_kanat", "Death & Janaza", "Instead of Wishing for Death",
            "Takleef mein maut ki tamanna ke bajaaye.",
            "اللَّهُمَّ أَحْيِنِي مَا كَانَتِ الْحَيَاةُ خَيْرًا لِي، وَتَوَفَّنِي إِذَا كَانَتِ الْوَفَاةُ خَيْرًا لِي",
            "Allāhumma aḥyinī mā kānatil-ḥayātu khayral-lī, wa tawaffanī idhā kānatil-wafātu khayral-lī",
            "Ya Allah, jab tak zindagi mere liye behtar ho mujhe zinda rakh, aur jab maut mere liye behtar ho to mujhe maut de.",
            "اے اللہ! جب تک زندگی میرے لیے بہتر ہو مجھے زندہ رکھ اور جب موت میرے لیے بہتر ہو تو مجھے موت دے",
            "Nabi ﷺ ne maut ki tamanna se mana farma kar yeh sikhaya.",
            "Sahih al-Bukhari", "Hadith 5671; Sahih Muslim 2680", "Sahih"
        ),

        // ---------------- Daily Life & Manners ----------------
        d(
            "dua_for_one_i_cursed", "Daily Life & Manners", "For Someone I Hurt with Words",
            "Ghusse mein kisi ko bura bhala keh diya ho to uske liye.",
            "اللَّهُمَّ فَأَيُّمَا مُؤْمِنٍ سَبَبْتُهُ فَاجْعَلْ ذَٰلِكَ لَهُ قُرْبَةً إِلَيْكَ يَوْمَ الْقِيَامَةِ",
            "Allāhumma fa-ayyumā mu'minin sababtuhū faj'al dhālika lahū qurbatan ilayka yawmal-qiyāmah",
            "Ya Allah, jis momin ko bhi maine bura bhala kaha ho, use qayamat ke din uske liye Tere qurb ka zariya bana de.",
            "اے اللہ! جس مومن کو بھی میں نے برا بھلا کہا ہو، اسے قیامت کے دن اس کے لیے اپنے قرب کا ذریعہ بنا دے",
            "Nabi ﷺ ki dua.",
            "Sahih al-Bukhari", "Hadith 6361; Sahih Muslim 2601", "Sahih"
        ),
        d(
            "dua_praising_someone", "Daily Life & Manners", "When Praising Someone",
            "Kisi ki tareef karni ho to mubaalagha ke bajaaye.",
            "أَحْسِبُ فُلَانًا، وَاللَّهُ حَسِيبُهُ، وَلَا أُزَكِّي عَلَى اللَّهِ أَحَدًا",
            "Aḥsibu fulānā, wallāhu ḥasībuh, wa lā uzakkī 'alallāhi aḥadā",
            "Main fulaan ko aisa samajhta hoon, aur Allah hi uska hisaab lene wala hai, aur main Allah ke muqaable mein kisi ki paakeezgi bayan nahi karta.",
            "میں فلاں کو ایسا سمجھتا ہوں، اور اللہ ہی اس کا حساب لینے والا ہے، اور میں اللہ کے مقابلے میں کسی کی پاکیزگی بیان نہیں کرتا",
            "Nabi ﷺ ne tareef ka yeh adab sikhaya.",
            "Sahih Muslim", "Hadith 3000; Sahih al-Bukhari 2662", "Sahih"
        ),
        d(
            "dua_being_praised", "Daily Life & Manners", "When You Are Praised",
            "Apni tareef sun kar — ek Sahabi (RA) ka amal.",
            "اللَّهُمَّ لَا تُؤَاخِذْنِي بِمَا يَقُولُونَ، وَاغْفِرْ لِي مَا لَا يَعْلَمُونَ",
            "Allāhumma lā tu'ākhidhnī bimā yaqūlūn, waghfir lī mā lā ya'lamūn",
            "Ya Allah, jo yeh keh rahe hain uspar meri pakad na farma, aur mere woh gunaah maaf farma jo yeh nahi jaante.",
            "اے اللہ! جو یہ کہہ رہے ہیں اس پر میری پکڑ نہ فرما، اور میرے وہ گناہ معاف فرما جو یہ نہیں جانتے",
            "Tareef par takabbur se bachne ka tareeqa.",
            "Al-Adab al-Mufrad (Bukhari)", "Hadith 761", "Mauqoof Sahih (Sahabi ka qaul, Sheikh Albani)"
        ),
        d(
            "dua_non_muslim_sneezes", "Daily Life & Manners", "When a Non-Muslim Sneezes",
            "Gair-muslim chheenk kar 'Alhamdulillah' kahe to.",
            "يَهْدِيكُمُ اللَّهُ وَيُصْلِحُ بَالَكُمْ",
            "Yahdīkumullāhu wa yuṣliḥu bālakum",
            "Allah tumhein hidayat de aur tumhara haal durust kare.",
            "اللہ تمہیں ہدایت دے اور تمہارا حال درست کرے",
            "Yahood Nabi ﷺ ke paas chheenkte the ke 'Yarhamukallah' kahein, to aap yeh farmate.",
            "Sunan Abi Dawud", "Hadith 5038; Jami` at-Tirmidhi 2739", "Sahih"
        ),
        d(
            "dua_reply_ahl_kitab_salam", "Daily Life & Manners", "Replying to Salam of People of the Book",
            "Ahl-e-kitab salaam karein to.",
            "وَعَلَيْكُمْ",
            "Wa 'alaykum",
            "Aur tum par bhi.",
            "اور تم پر بھی",
            "Nabi ﷺ ka hukm.",
            "Sahih al-Bukhari", "Hadith 6258; Sahih Muslim 2163", "Sahih"
        ),
        d(
            "dua_fasting_invited", "Ramadan & Fasting", "When Invited While Fasting",
            "Roze ki haalat mein daawat mile ya koi jhagda kare.",
            "إِنِّي صَائِمٌ",
            "Innī ṣā'im",
            "Main roze se hoon.",
            "میں روزے سے ہوں",
            "Rozadaar daawat qubool kare to mezbaan ke liye dua kare.",
            "Sahih Muslim", "Hadith 1150; Sahih al-Bukhari 1894", "Sahih"
        ),
        d(
            "dua_for_who_gave_drink", "Food & Gratitude", "For Whoever Fed Me",
            "Jo khilaaye pilaaye uske liye.",
            "اللَّهُمَّ أَطْعِمْ مَنْ أَطْعَمَنِي، وَاسْقِ مَنْ سَقَانِي",
            "Allāhumma aṭ'im man aṭ'amanī, wasqi man saqānī",
            "Ya Allah, jisne mujhe khilaya use khila, aur jisne mujhe pilaya use pila.",
            "اے اللہ! جس نے مجھے کھلایا اسے کھلا اور جس نے مجھے پلایا اسے پلا",
            "Nabi ﷺ ne Hazrat Miqdad (RA) ke liye yeh dua farmayi.",
            "Sahih Muslim", "Hadith 2055", "Sahih"
        ),
        d(
            "dua_eid_greeting", "Daily Life & Manners", "Eid Greeting",
            "Eid ke din ek doosre ko — Sahaba-e-Kiraam (RA) ka ma'mool.",
            "تَقَبَّلَ اللَّهُ مِنَّا وَمِنْكَ",
            "Taqabbalallāhu minnā wa mink",
            "Allah hum se aur tum se (aamaal) qubool farmaye.",
            "اللہ ہم سے اور تم سے (اعمال) قبول فرمائے",
            "Sahaba Eid par mil kar yeh kehte the.",
            "Fath al-Bari (Ibn Hajar)", "2/446 — Hazrat Jubair bin Nufair se", "Hasan (Hafiz Ibn Hajar), Sahaba ka amal"
        ),

        // ---------------- Parents & Family ----------------
        d(
            "dua_wedding_night", "Parents & Family", "For the Bride After Nikah",
            "Nikah ke baad biwi ki peshaani par haath rakh kar.",
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ خَيْرَهَا وَخَيْرَ مَا جَبَلْتَهَا عَلَيْهِ، وَأَعُوذُ بِكَ مِنْ شَرِّهَا وَشَرِّ مَا جَبَلْتَهَا عَلَيْهِ",
            "Allāhumma innī as'aluka khayrahā wa khayra mā jabaltahā 'alayh, wa a'ūdhu bika min sharrihā wa sharri mā jabaltahā 'alayh",
            "Ya Allah, main Tujh se iski bhalai aur us fitrat ki bhalai maangta hoon jis par Tune ise paida kiya, aur iske shar aur us fitrat ke shar se Teri panaah maangta hoon.",
            "اے اللہ! میں تجھ سے اس کی بھلائی اور اس فطرت کی بھلائی مانگتا ہوں جس پر تو نے اسے پیدا کیا، اور اس کے شر اور اس فطرت کے شر سے تیری پناہ مانگتا ہوں",
            "Nabi ﷺ ki taleem — nikah mein barkat.",
            "Sunan Abi Dawud", "Hadith 2160; Sunan Ibn Majah 1918", "Hasan"
        ),

        // ---------------- Weather & Nature ----------------
        d(
            "dua_istisqa_ibadak", "Weather & Nature", "Water Your Servants and Animals",
            "Baarish ki talab (istisqa).",
            "اللَّهُمَّ اسْقِ عِبَادَكَ وَبَهَائِمَكَ، وَانْشُرْ رَحْمَتَكَ، وَأَحْيِ بَلَدَكَ الْمَيِّتَ",
            "Allāhummasqi 'ibādaka wa bahā'imak, wanshur raḥmatak, wa aḥyi baladakal-mayyit",
            "Ya Allah, apne bandon aur jaanwaron ko sairaab kar, apni rehmat phaila de aur apni murda zameen ko zinda kar de.",
            "اے اللہ! اپنے بندوں اور جانوروں کو سیراب کر، اپنی رحمت پھیلا دے اور اپنی مردہ زمین کو زندہ کر دے",
            "Nabi ﷺ baarish ke liye yeh dua farmate.",
            "Sunan Abi Dawud", "Hadith 1176", "Hasan"
        ),
        d(
            "dua_seeing_clouds", "Weather & Nature", "Seeing Dark Clouds",
            "Aasmaan par ghata ya baadal dekh kar; baarish ho to 'Allahumma sayyiban nafi'a'.",
            "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ شَرِّهَا",
            "Allāhumma innī a'ūdhu bika min sharrihā",
            "Ya Allah, main iske shar se Teri panaah maangta hoon.",
            "اے اللہ! میں اس کے شر سے تیری پناہ مانگتا ہوں",
            "Nabi ﷺ baadal dekh kar kaam chhod dete aur yeh dua karte.",
            "Sunan Abi Dawud", "Hadith 5099; Sunan Ibn Majah 3889", "Sahih"
        ),

        // ---------------- Hajj & Umrah ----------------
        d(
            "dua_jamarat", "Hajj & Umrah", "Stoning the Jamarat",
            "Har kankri maarte waqt.",
            "اللَّهُ أَكْبَرُ",
            "Allāhu akbar",
            "Allah sab se bara hai.",
            "اللہ سب سے بڑا ہے",
            "Nabi ﷺ har kankri ke saath takbeer kehte, phir pehle aur doosre jamrah ke baad Qibla rukh ho kar lambi dua karte.",
            "Sahih al-Bukhari", "Hadith 1751, 1753", "Sahih", 7
        )
    )
}
