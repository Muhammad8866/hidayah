package com.example.salah

/** Prayers beyond the five daily ones: Witr, the congregational prayers and the Nawafil. */
private val otherSchools = listOf(Madhhab.SHAFII, Madhhab.MALIKI, Madhhab.HANBALI)

internal val salahSpecialPrayers: List<SalahTopic> = listOf(
    SalahTopic(
        "witr",
        t("Witr", "Witr", "वित्र", "وتر"),
        SalahKind.WITR,
        t(
            "The odd-numbered prayer after Isha",
            "Isha ke baad ki taaq namaz",
            "इशा के बाद की ताक़ नमाज़",
            "عشاء کے بعد کی طاق نماز"
        ),
        listOf(
            t(
                "Pray it after the Fard and Sunnah of Isha, at any time before Fajr begins. It is best as the last prayer of the night.",
                "Isha ke Farz aur Sunnat ke baad, Fajr shuru hone se pehle kisi bhi waqt padhein. Behtar hai ke yeh raat ki aakhri namaz ho.",
                "इशा के फ़र्ज़ और सुन्नत के बाद, फ़ज्र शुरू होने से पहले किसी भी वक़्त पढ़ें। बेहतर है कि यह रात की आख़िरी नमाज़ हो।",
                "عشاء کے فرض اور سنت کے بعد، فجر شروع ہونے سے پہلے کسی بھی وقت پڑھیں۔ بہتر ہے کہ یہ رات کی آخری نماز ہو۔"
            ),
            t(
                "Learn the number and sequence taught in your school.",
                "Rakaton ki ta'daad aur tareeqa apne mazhab ke mutabiq seekhein.",
                "रकअतों की तादाद और तरीक़ा अपने मज़हब के मुताबिक़ सीखें।",
                "رکعتوں کی تعداد اور طریقہ اپنے مذہب کے مطابق سیکھیں۔"
            ),
            t(
                "Recite the Qunut where your school prescribes it; the wording is shown below.",
                "Jahan aap ke mazhab mein Qunoot hai wahan padhein; dua neeche di gayi hai.",
                "जहाँ आपके मज़हब में क़ुनूत है वहाँ पढ़ें; दुआ नीचे दी गई है।",
                "جہاں آپ کے مذہب میں قنوت ہے وہاں پڑھیں؛ دعا نیچے دی گئی ہے۔"
            )
        ),
        "Sunan Abi Dawud 1416, 1419 · Sunan an-Nasa'i 1699, 1701",
        schoolNotes = mapOf(
            Madhhab.HANAFI to t(
                "Wajib: three rak‘at with one salam. Sit for the Tashahhud after the second rak‘ah, recite al-Fatihah and a surah in all three, and in the third say a takbir with raised hands and recite Dua-e-Qunut before ruku‘. A missed Witr is made up.",
                "Wajib: teen rakat ek salam se. Doosri rakat ke baad At-Tahiyyat ke liye baithein, teeno rakaton mein Fatiha aur surat padhein, aur teesri mein haath utha kar takbeer kahein aur ruku se pehle Dua-e-Qunoot padhein. Chhoot jaye to iski qaza hai.",
                "वाजिब: तीन रकअत एक सलाम से। दूसरी रकअत के बाद अत्तहिय्यात के लिए बैठें, तीनों रकअतों में फ़ातिहा और सूरत पढ़ें, और तीसरी में हाथ उठाकर तकबीर कहें और रुकू से पहले दुआ-ए-क़ुनूत पढ़ें। छूट जाए तो इसकी क़ज़ा है।",
                "واجب: تین رکعت ایک سلام سے۔ دوسری رکعت کے بعد التحیات کے لیے بیٹھیں، تینوں رکعتوں میں فاتحہ اور سورت پڑھیں، اور تیسری میں ہاتھ اٹھا کر تکبیر کہیں اور رکوع سے پہلے دعائے قنوت پڑھیں۔ چھوٹ جائے تو اس کی قضا ہے۔"
            ),
            Madhhab.SHAFII to t(
                "Sunnah: at least one odd rak‘ah; Qunut in the last rak‘ah during the latter half of Ramadan is a recognised practice.",
                "Sunnat: kam az kam ek taaq rakat; Ramzan ke aakhri nisf mein aakhri rakat mein Qunoot padhna ma'roof hai.",
                "सुन्नत: कम से कम एक ताक़ रकअत; रमज़ान के आख़िरी निस्फ़ में आख़िरी रकअत में क़ुनूत पढ़ना मारूफ़ है।",
                "سنت: کم از کم ایک طاق رکعت؛ رمضان کے آخری نصف میں آخری رکعت میں قنوت پڑھنا معروف ہے۔"
            ),
            Madhhab.MALIKI to t(
                "Sunnah: commonly one rak‘ah after two rak‘at of shaf‘.",
                "Sunnat: aam taur par do rakat shaf' ke baad ek rakat.",
                "सुन्नत: आम तौर पर दो रकअत शफ़अ के बाद एक रकअत।",
                "سنت: عام طور پر دو رکعت شفع کے بعد ایک رکعت۔"
            ),
            Madhhab.HANBALI to t(
                "Sunnah: one or more odd rak‘at; Qunut may be recited.",
                "Sunnat: ek ya zyada taaq rakatein; Qunoot padhi ja sakti hai.",
                "सुन्नत: एक या ज़्यादा ताक़ रकअतें; क़ुनूत पढ़ी जा सकती है।",
                "سنت: ایک یا زیادہ طاق رکعتیں؛ قنوت پڑھی جا سکتی ہے۔"
            )
        ),
        recitationIds = listOf("qunut_hanafi", "rabbana_atina", "qunut"),
        learnForm = GuideForm.WITR_3
    ),
    SalahTopic(
        "jumuah",
        t("Jumu‘ah", "Jumu'ah", "जुमा", "جمعہ"),
        SalahKind.FARD,
        t(
            "The Friday congregational prayer",
            "Jumu'ah ki ba-jamaat namaz",
            "जुमे की बा-जमाअत नमाज़",
            "جمعہ کی باجماعت نماز"
        ),
        listOf(
            t(
                "Take a ghusl, wear clean clothes and perfume, and go early.",
                "Ghusl karein, saaf kapde pehnein, khushboo lagayein aur jaldi masjid jayein.",
                "ग़ुस्ल करें, साफ़ कपड़े पहनें, ख़ुशबू लगाएँ और जल्दी मस्जिद जाएँ।",
                "غسل کریں، صاف کپڑے پہنیں، خوشبو لگائیں اور جلدی مسجد جائیں۔"
            ),
            t(
                "Stop buying, selling and other work once the adhan is called.",
                "Azaan hote hi khareed-o-farokht aur doosre kaam chhod dein.",
                "अज़ान होते ही ख़रीद-ओ-फ़रोख़्त और दूसरे काम छोड़ दें।",
                "اذان ہوتے ہی خرید و فروخت اور دوسرے کام چھوڑ دیں۔"
            ),
            t(
                "Listen attentively to the two khutbahs; speaking during them is not allowed.",
                "Dono khutbe khamoshi aur tawajjoh se sunein; khutbe ke dauran baat karna jaiz nahi.",
                "दोनों ख़ुत्बे ख़ामोशी और तवज्जोह से सुनें; ख़ुत्बे के दौरान बात करना जायज़ नहीं।",
                "دونوں خطبے خاموشی اور توجہ سے سنیں؛ خطبے کے دوران بات کرنا جائز نہیں۔"
            ),
            t(
                "Pray two Fard rak‘at behind the imam instead of Zuhr when Jumu‘ah applies to you.",
                "Jis par Jumu'ah Farz hai woh Zuhr ki jagah imam ke peeche do rakat Farz padhe.",
                "जिस पर जुमा फ़र्ज़ है वह ज़ुहर की जगह इमाम के पीछे दो रकअत फ़र्ज़ पढ़े।",
                "جس پر جمعہ فرض ہے وہ ظہر کی جگہ امام کے پیچھے دو رکعت فرض پڑھے۔"
            ),
            t(
                "If you miss Jumu‘ah, pray Zuhr; follow local scholarly guidance for individual circumstances.",
                "Jumu'ah chhoot jaye to Zuhr padhein; apni soorat-e-haal mein muqami ulama se rehnumai lein.",
                "जुमा छूट जाए तो ज़ुहर पढ़ें; अपनी सूरत-ए-हाल में मुक़ामी उलमा से रहनुमाई लें।",
                "جمعہ چھوٹ جائے تو ظہر پڑھیں؛ اپنی صورتِ حال میں مقامی علماء سے رہنمائی لیں۔"
            )
        ),
        "Quran 62:9 · Sahih al-Bukhari 883",
        schoolNotes = mapOf(
            Madhhab.HANAFI to t(
                "Fourteen rak‘at: 4 Sunnah Muakkadah, 2 Fard, 4 Sunnah Muakkadah, 2 Sunnah and 2 Nafl. Jumu‘ah is Fard on sane, adult, resident, healthy men; women, travellers and the sick pray Zuhr, though Jumu‘ah counts for them if they attend.",
                "Chaudah rakat: 4 Sunnat-e-Muakkada, 2 Farz, 4 Sunnat-e-Muakkada, 2 Sunnat aur 2 Nafl. Jumu'ah aaqil, baaligh, muqeem, tandurust mardon par Farz hai; auratein, musafir aur mareez Zuhr padhein, albatta shareek ho jayein to unka Jumu'ah ho jayega.",
                "चौदह रकअत: 4 सुन्नत-ए-मुअक्कदा, 2 फ़र्ज़, 4 सुन्नत-ए-मुअक्कदा, 2 सुन्नत और 2 नफ़्ल। जुमा आक़िल, बालिग़, मुक़ीम, तंदुरुस्त मर्दों पर फ़र्ज़ है; औरतें, मुसाफ़िर और मरीज़ ज़ुहर पढ़ें, अलबत्ता शरीक हो जाएँ तो उनका जुमा हो जाएगा।",
                "چودہ رکعت: 4 سنتِ مؤکدہ، 2 فرض، 4 سنتِ مؤکدہ، 2 سنت اور 2 نفل۔ جمعہ عاقل، بالغ، مقیم، تندرست مردوں پر فرض ہے؛ عورتیں، مسافر اور مریض ظہر پڑھیں، البتہ شریک ہو جائیں تو ان کا جمعہ ہو جائے گا۔"
            )
        )
    ),
    SalahTopic(
        "eid",
        t("Eid Salah", "Eid ki namaz", "ईद की नमाज़", "عید کی نماز"),
        null,
        t(
            "Two rak‘at in congregation with extra takbirs",
            "Zaaid takbeeron ke saath do rakat ba-jamaat",
            "ज़ाइद तकबीरों के साथ दो रकअत बा-जमाअत",
            "زائد تکبیروں کے ساتھ دو رکعت باجماعت"
        ),
        listOf(
            t(
                "There is no adhan or iqamah. On Eid al-Fitr eat something sweet before going and give Sadaqat-ul-Fitr before the prayer; on Eid al-Adha eat after the prayer.",
                "Na azaan hai na iqamat. Eid-ul-Fitr mein jaane se pehle kuch meetha khayein aur namaz se pehle Sadaqa-e-Fitr ada karein; Eid-ul-Azha mein namaz ke baad khayein.",
                "न अज़ान है न इक़ामत। ईदुल-फ़ित्र में जाने से पहले कुछ मीठा खाएँ और नमाज़ से पहले सदक़ा-ए-फ़ित्र अदा करें; ईदुल-अज़हा में नमाज़ के बाद खाएँ।",
                "نہ اذان ہے نہ اقامت۔ عید الفطر میں جانے سے پہلے کچھ میٹھا کھائیں اور نماز سے پہلے صدقۂ فطر ادا کریں؛ عید الاضحیٰ میں نماز کے بعد کھائیں۔"
            ),
            t(
                "Join the Eid congregation at its announced time.",
                "Muqarrar waqt par Eid ki jamaat mein shamil hon.",
                "मुक़र्रर वक़्त पर ईद की जमाअत में शामिल हों।",
                "مقررہ وقت پر عید کی جماعت میں شامل ہوں۔"
            ),
            t(
                "Follow the imam for two rak‘at and the extra takbirs.",
                "Imam ke peeche do rakat aur zaaid takbeerein ada karein.",
                "इमाम के पीछे दो रकअत और ज़ाइद तकबीरें अदा करें।",
                "امام کے پیچھے دو رکعت اور زائد تکبیریں ادا کریں۔"
            ),
            t(
                "Listen to the khutbah after the prayer.",
                "Namaz ke baad khutba sunein.",
                "नमाज़ के बाद ख़ुत्बा सुनें।",
                "نماز کے بعد خطبہ سنیں۔"
            )
        ),
        "Sahih al-Bukhari 953, 956",
        schoolNotes = mapOf(
            Madhhab.HANAFI to t(
                "Wajib, with six extra takbirs. First rak‘ah: after Thana, three takbirs — raise the hands each time, let them hang after the first two and fold them after the third; then the recitation. Second rak‘ah: after the recitation, three takbirs letting the hands hang each time, then a fourth takbir to go into ruku‘.",
                "Wajib hai, chhe zaaid takbeeron ke saath. Pehli rakat: Sana ke baad teen takbeerein — har baar haath uthayein, pehli do ke baad chhod dein aur teesri ke baad baandh lein; phir qira'at. Doosri rakat: qira'at ke baad teen takbeerein, har baar haath utha kar chhod dein, phir chauthi takbeer keh kar ruku mein jayein.",
                "वाजिब है, छह ज़ाइद तकबीरों के साथ। पहली रकअत: सना के बाद तीन तकबीरें — हर बार हाथ उठाएँ, पहली दो के बाद छोड़ दें और तीसरी के बाद बाँध लें; फिर क़िराअत। दूसरी रकअत: क़िराअत के बाद तीन तकबीरें, हर बार हाथ उठाकर छोड़ दें, फिर चौथी तकबीर कहकर रुकू में जाएँ।",
                "واجب ہے، چھ زائد تکبیروں کے ساتھ۔ پہلی رکعت: ثنا کے بعد تین تکبیریں — ہر بار ہاتھ اٹھائیں، پہلی دو کے بعد چھوڑ دیں اور تیسری کے بعد باندھ لیں؛ پھر قراءت۔ دوسری رکعت: قراءت کے بعد تین تکبیریں، ہر بار ہاتھ اٹھا کر چھوڑ دیں، پھر چوتھی تکبیر کہہ کر رکوع میں جائیں۔"
            ),
            Madhhab.SHAFII to t(
                "Seven additional takbirs in the first rak‘ah and five in the second, before the recitation.",
                "Pehli rakat mein saat aur doosri mein paanch zaaid takbeerein, qira'at se pehle.",
                "पहली रकअत में सात और दूसरी में पाँच ज़ाइद तकबीरें, क़िराअत से पहले।",
                "پہلی رکعت میں سات اور دوسری میں پانچ زائد تکبیریں، قراءت سے پہلے۔"
            )
        ) + listOf(Madhhab.MALIKI, Madhhab.HANBALI).associateWith {
            t(
                "The extra takbirs differ from the Hanafi sequence; follow the imam.",
                "Zaaid takbeeron ki tarteeb Hanafi tareeqe se mukhtalif hai; imam ki pairwi karein.",
                "ज़ाइद तकबीरों की तरतीब हनफ़ी तरीक़े से मुख़्तलिफ़ है; इमाम की पैरवी करें।",
                "زائد تکبیروں کی ترتیب حنفی طریقے سے مختلف ہے؛ امام کی پیروی کریں۔"
            )
        },
        kindBySchool = mapOf(
            Madhhab.HANAFI to SalahKind.WAJIB,
            Madhhab.SHAFII to SalahKind.SUNNAH_MUAKKADAH,
            Madhhab.MALIKI to SalahKind.SUNNAH_MUAKKADAH,
            Madhhab.HANBALI to SalahKind.FARD_KIFAYAH
        )
    ),
    SalahTopic(
        "janazah",
        t("Janazah", "Namaz-e-Janaza", "नमाज़-ए-जनाज़ा", "نمازِ جنازہ"),
        SalahKind.FARD_KIFAYAH,
        t(
            "The funeral prayer: standing, with no ruku‘ or sajdah",
            "Khade hokar padhi jati hai; isme ruku aur sajda nahi",
            "खड़े होकर पढ़ी जाती है; इसमें रुकू और सजदा नहीं",
            "کھڑے ہو کر پڑھی جاتی ہے؛ اس میں رکوع اور سجدہ نہیں"
        ),
        listOf(
            t(
                "Stand in the rows with wudu and intend the prayer for Allah and the dua for this deceased.",
                "Ba-wuzu saf mein khade hon aur niyyat karein: namaz Allah ke liye, dua is mayyit ke liye.",
                "बा-वुज़ू सफ़ में खड़े हों और नीयत करें: नमाज़ अल्लाह के लिए, दुआ इस मय्यित के लिए।",
                "باوضو صف میں کھڑے ہوں اور نیت کریں: نماز اللہ کے لیے، دعا اس میت کے لیے۔"
            ),
            t(
                "Follow the imam's takbirs while standing; recite praise, blessings upon the Prophet ﷺ, and the supplication for the deceased as taught in your school.",
                "Khade khade imam ki takbeeron ki pairwi karein; apne mazhab ke mutabiq sana, Durood aur mayyit ke liye dua padhein.",
                "खड़े-खड़े इमाम की तकबीरों की पैरवी करें; अपने मज़हब के मुताबिक़ सना, दुरूद और मय्यित के लिए दुआ पढ़ें।",
                "کھڑے کھڑے امام کی تکبیروں کی پیروی کریں؛ اپنے مذہب کے مطابق ثنا، درود اور میت کے لیے دعا پڑھیں۔"
            ),
            t(
                "End with the imam's salam.",
                "Imam ke saath salam pher kar namaz khatm karein.",
                "इमाम के साथ सलाम फेरकर नमाज़ ख़त्म करें।",
                "امام کے ساتھ سلام پھیر کر نماز ختم کریں۔"
            )
        ),
        "Sahih al-Bukhari 1333 · Jami` at-Tirmidhi 1024",
        schoolNotes = mapOf(
            Madhhab.HANAFI to t(
                "Four takbirs; the hands are raised only at the first. After the first takbir recite Thana; after the second, Durood-e-Ibrahim; after the third, the dua for the deceased; after the fourth, give salam to both sides.",
                "Chaar takbeerein; haath sirf pehli takbeer mein uthaye jate hain. Pehli takbeer ke baad Sana, doosri ke baad Durood-e-Ibrahimi, teesri ke baad mayyit ki dua, aur chauthi ke baad dono taraf salam.",
                "चार तकबीरें; हाथ सिर्फ़ पहली तकबीर में उठाए जाते हैं। पहली तकबीर के बाद सना, दूसरी के बाद दुरूद-ए-इब्राहीमी, तीसरी के बाद मय्यित की दुआ, और चौथी के बाद दोनों तरफ़ सलाम।",
                "چار تکبیریں؛ ہاتھ صرف پہلی تکبیر میں اٹھائے جاتے ہیں۔ پہلی تکبیر کے بعد ثنا، دوسری کے بعد درودِ ابراہیمی، تیسری کے بعد میت کی دعا، اور چوتھی کے بعد دونوں طرف سلام۔"
            ),
            Madhhab.SHAFII to t(
                "Al-Fatihah is recited after the first takbir, and the hands are raised at every takbir.",
                "Pehli takbeer ke baad Surah Fatiha padhi jati hai, aur har takbeer par haath uthaye jate hain.",
                "पहली तकबीर के बाद सूरह फ़ातिहा पढ़ी जाती है, और हर तकबीर पर हाथ उठाए जाते हैं।",
                "پہلی تکبیر کے بعد سورۃ فاتحہ پڑھی جاتی ہے، اور ہر تکبیر پر ہاتھ اٹھائے جاتے ہیں۔"
            )
        ),
        recitationIds = listOf("thana", "salawat", "janazah_adult", "janazah_boy", "janazah_girl")
    ),
    SalahTopic(
        "taraweeh",
        t("Taraweeh", "Taraweeh", "तरावीह", "تراویح"),
        SalahKind.SUNNAH_MUAKKADAH,
        t("The night prayer of Ramadan", "Ramzan ki raat ki namaz", "रमज़ान की रात की नमाज़", "رمضان کی رات کی نماز"),
        listOf(
            t(
                "Pray after Isha during Ramadan, usually with a congregation.",
                "Ramzan mein Isha ke baad padhein, aam taur par jamaat ke saath.",
                "रमज़ान में इशा के बाद पढ़ें, आम तौर पर जमाअत के साथ।",
                "رمضان میں عشاء کے بعد پڑھیں، عام طور پر جماعت کے ساتھ۔"
            ),
            t(
                "Follow the imam in units of two rak‘at and rest when the congregation rests.",
                "Imam ke peeche do do rakat padhein aur jab jamaat aaraam kare to aaraam karein.",
                "इमाम के पीछे दो-दो रकअत पढ़ें और जब जमाअत आराम करे तो आराम करें।",
                "امام کے پیچھے دو دو رکعت پڑھیں اور جب جماعت آرام کرے تو آرام کریں۔"
            ),
            t(
                "The total number practised differs across communities; follow your mosque and school.",
                "Rakaton ki ta'daad mein jagah jagah farq hai; apni masjid aur mazhab ke mutabiq padhein.",
                "रकअतों की तादाद में जगह-जगह फ़र्क़ है; अपनी मस्जिद और मज़हब के मुताबिक़ पढ़ें।",
                "رکعتوں کی تعداد میں جگہ جگہ فرق ہے؛ اپنی مسجد اور مذہب کے مطابق پڑھیں۔"
            )
        ),
        "Sahih al-Bukhari 2009, 2010 · Muwatta Imam Malik (Yazid ibn Ruman)",
        schoolNotes = mapOf(
            Madhhab.HANAFI to t(
                "Twenty rak‘at in ten salams, with a short rest after every four. It is Sunnah Muakkadah for men and women, and completing the Quran once in it is Sunnah. In Ramadan Witr is prayed in congregation after it.",
                "Bees rakat das salam se, har chaar rakat ke baad thoda aaraam (tarweeha). Yeh mardon aur auraton dono ke liye Sunnat-e-Muakkada hai, aur isme ek baar Quran khatm karna Sunnat hai. Ramzan mein iske baad Witr jamaat se padhe jate hain.",
                "बीस रकअत दस सलाम से, हर चार रकअत के बाद थोड़ा आराम (तरवीहा)। यह मर्दों और औरतों दोनों के लिए सुन्नत-ए-मुअक्कदा है, और इसमें एक बार क़ुरआन ख़त्म करना सुन्नत है। रमज़ान में इसके बाद वित्र जमाअत से पढ़े जाते हैं।",
                "بیس رکعت دس سلام سے، ہر چار رکعت کے بعد تھوڑا آرام (ترویحہ)۔ یہ مردوں اور عورتوں دونوں کے لیے سنتِ مؤکدہ ہے، اور اس میں ایک بار قرآن ختم کرنا سنت ہے۔ رمضان میں اس کے بعد وتر جماعت سے پڑھے جاتے ہیں۔"
            )
        ) + listOf(Madhhab.SHAFII, Madhhab.HANBALI).associateWith {
            t(
                "Twenty rak‘at, prayed two by two.",
                "Bees rakat, do do karke.",
                "बीस रकअत, दो-दो करके।",
                "بیس رکعت، دو دو کر کے۔"
            )
        }
    ),
    SalahTopic(
        "tahajjud",
        t("Tahajjud", "Tahajjud", "तहज्जुद", "تہجد"),
        SalahKind.NAFL,
        t("The night prayer after sleep", "Neend ke baad raat ki namaz", "नींद के बाद रात की नमाज़", "نیند کے بعد رات کی نماز"),
        listOf(
            t(
                "Pray after Isha and some sleep, before Fajr begins; the last third of the night is best.",
                "Isha ke baad kuch so kar, Fajr shuru hone se pehle padhein; raat ka aakhri tihai hissa sab se behtar hai.",
                "इशा के बाद कुछ सोकर, फ़ज्र शुरू होने से पहले पढ़ें; रात का आख़िरी तिहाई हिस्सा सबसे बेहतर है।",
                "عشاء کے بعد کچھ سو کر، فجر شروع ہونے سے پہلے پڑھیں؛ رات کا آخری تہائی حصہ سب سے بہتر ہے۔"
            ),
            t(
                "Pray at least two rak‘at; eight are commonly prayed, in pairs.",
                "Kam az kam do rakat padhein; aam taur par aath rakat do do karke padhi jati hain.",
                "कम से कम दो रकअत पढ़ें; आम तौर पर आठ रकअत दो-दो करके पढ़ी जाती हैं।",
                "کم از کم دو رکعت پڑھیں؛ عام طور پر آٹھ رکعت دو دو کر کے پڑھی جاتی ہیں۔"
            ),
            t(
                "Make sincere dua; pray Witr last if you have not prayed it already.",
                "Dil se dua maangein; Witr na padhe hon to aakhir mein padhein.",
                "दिल से दुआ माँगें; वित्र न पढ़े हों तो आख़िर में पढ़ें।",
                "دل سے دعا مانگیں؛ وتر نہ پڑھے ہوں تو آخر میں پڑھیں۔"
            )
        ),
        "Quran 17:79 · Sahih Muslim 1163",
        learnForm = GuideForm.SUNNAH_2
    ),
    SalahTopic(
        "ishraq",
        t("Ishraq", "Ishraq", "इशराक़", "اشراق"),
        SalahKind.NAFL,
        t("Two rak‘at after sunrise", "Sooraj nikalne ke baad do rakat", "सूरज निकलने के बाद दो रकअत", "سورج نکلنے کے بعد دو رکعت"),
        listOf(
            t(
                "After Fajr in congregation, stay seated in the remembrance of Allah until the sun has risen.",
                "Fajr jamaat se padh kar sooraj nikalne tak wahin baith kar Allah ka zikr karein.",
                "फ़ज्र जमाअत से पढ़कर सूरज निकलने तक वहीं बैठकर अल्लाह का ज़िक्र करें।",
                "فجر جماعت سے پڑھ کر سورج نکلنے تک وہیں بیٹھ کر اللہ کا ذکر کریں۔"
            ),
            t(
                "About twenty minutes after sunrise, pray two rak‘at Nafl.",
                "Tuloo ke taqriban 20 minute baad do rakat Nafl padhein.",
                "तुलू के तक़रीबन 20 मिनट बाद दो रकअत नफ़्ल पढ़ें।",
                "طلوع کے تقریباً 20 منٹ بعد دو رکعت نفل پڑھیں۔"
            ),
            t(
                "The hadith promises the reward of a complete Hajj and Umrah.",
                "Hadees mein iska sawab ek mukammal Hajj aur Umrah ke barabar bataya gaya hai.",
                "हदीस में इसका सवाब एक मुकम्मल हज और उमरा के बराबर बताया गया है।",
                "حدیث میں اس کا ثواب ایک مکمل حج اور عمرہ کے برابر بتایا گیا ہے۔"
            )
        ),
        "Jami` at-Tirmidhi 586",
        learnForm = GuideForm.SUNNAH_2
    ),
    SalahTopic(
        "duha",
        t("Duha (Chasht)", "Chasht (Duha)", "चाश्त (ज़ुहा)", "چاشت (ضحیٰ)"),
        SalahKind.NAFL,
        t("The forenoon prayer", "Din chadhe ki namaz", "दिन चढ़े की नमाज़", "دن چڑھے کی نماز"),
        listOf(
            t(
                "Wait until after sunrise when the prohibited time has passed.",
                "Sooraj nikalne ke baad makrooh waqt guzar jane dein.",
                "सूरज निकलने के बाद मकरूह वक़्त गुज़र जाने दें।",
                "سورج نکلنے کے بعد مکروہ وقت گزر جانے دیں۔"
            ),
            t(
                "Pray at least two rak‘at before midday.",
                "Zawal se pehle kam az kam do rakat padhein.",
                "ज़वाल से पहले कम से कम दो रकअत पढ़ें।",
                "زوال سے پہلے کم از کم دو رکعت پڑھیں۔"
            ),
            t(
                "Add more pairs if you wish, up to twelve rak‘at; the best time is when a quarter of the day has passed.",
                "Chahein to do do karke aur padhein, baarah rakat tak; behtar waqt chauthai din chadhne par hai.",
                "चाहें तो दो-दो करके और पढ़ें, बारह रकअत तक; बेहतर वक़्त चौथाई दिन चढ़ने पर है।",
                "چاہیں تو دو دو کر کے اور پڑھیں، بارہ رکعت تک؛ بہتر وقت چوتھائی دن چڑھنے پر ہے۔"
            )
        ),
        "Sahih Muslim 720",
        learnForm = GuideForm.SUNNAH_2
    ),
    SalahTopic(
        "awwabin",
        t("Awwabin", "Awwabeen", "अव्वाबीन", "اوابین"),
        SalahKind.NAFL,
        t("Six rak‘at after Maghrib", "Maghrib ke baad chhe rakat", "मग़रिब के बाद छह रकअत", "مغرب کے بعد چھ رکعت"),
        listOf(
            t(
                "After the Fard and Sunnah of Maghrib, pray six rak‘at in three pairs.",
                "Maghrib ke Farz aur Sunnat ke baad chhe rakat do do karke padhein.",
                "मग़रिब के फ़र्ज़ और सुन्नत के बाद छह रकअत दो-दो करके पढ़ें।",
                "مغرب کے فرض اور سنت کے بعد چھ رکعت دو دو کر کے پڑھیں۔"
            ),
            t(
                "Avoid idle talk between them.",
                "Inke darmiyan fuzool baat na karein.",
                "इनके दरमियान फ़ुज़ूल बात न करें।",
                "ان کے درمیان فضول بات نہ کریں۔"
            )
        ),
        "Jami` at-Tirmidhi 435 (weak chain) · Bahar-e-Shariat, Hissa 4",
        learnForm = GuideForm.SUNNAH_2
    ),
    SalahTopic(
        "tasbih",
        t("Salat-ut-Tasbih", "Salat-ut-Tasbeeh", "सलातुत्तस्बीह", "صلاۃ التسبیح"),
        SalahKind.NAFL,
        t(
            "Four rak‘at with three hundred tasbih",
            "Chaar rakat, teen sau tasbeeh ke saath",
            "चार रकअत, तीन सौ तस्बीह के साथ",
            "چار رکعت، تین سو تسبیح کے ساتھ"
        ),
        listOf(
            t(
                "Pray four rak‘at Nafl with one salam, outside the forbidden times, reciting the tasbih shown below.",
                "Mamnoo waqton ke alawa chaar rakat Nafl ek salam se padhein, aur neeche di gayi tasbeeh padhte rahein.",
                "ममनू वक़्तों के अलावा चार रकअत नफ़्ल एक सलाम से पढ़ें, और नीचे दी गई तस्बीह पढ़ते रहें।",
                "ممنوع وقتوں کے علاوہ چار رکعت نفل ایک سلام سے پڑھیں، اور نیچے دی گئی تسبیح پڑھتے رہیں۔"
            ),
            t(
                "In every rak‘ah recite it 15 times before al-Fatihah (after Thana in the first rak‘ah), and 10 times after the surah.",
                "Har rakat mein Fatiha se pehle 15 baar (pehli rakat mein Sana ke baad), aur surat ke baad 10 baar padhein.",
                "हर रकअत में फ़ातिहा से पहले 15 बार (पहली रकअत में सना के बाद), और सूरत के बाद 10 बार पढ़ें।",
                "ہر رکعت میں فاتحہ سے پہلے 15 بار (پہلی رکعت میں ثنا کے بعد)، اور سورت کے بعد 10 بار پڑھیں۔"
            ),
            t(
                "Then 10 times in ruku‘ after its tasbih, 10 times standing after ruku‘, 10 times in each sajdah after its tasbih, and 10 times sitting between the two sajdahs.",
                "Phir ruku mein ruku ki tasbeeh ke baad 10 baar, ruku se uth kar 10 baar, har sajde mein sajde ki tasbeeh ke baad 10 baar, aur dono sajdon ke darmiyan baith kar 10 baar.",
                "फिर रुकू में रुकू की तस्बीह के बाद 10 बार, रुकू से उठकर 10 बार, हर सजदे में सजदे की तस्बीह के बाद 10 बार, और दोनों सजदों के दरमियान बैठकर 10 बार।",
                "پھر رکوع میں رکوع کی تسبیح کے بعد 10 بار، رکوع سے اٹھ کر 10 بار، ہر سجدے میں سجدے کی تسبیح کے بعد 10 بار، اور دونوں سجدوں کے درمیان بیٹھ کر 10 بار۔"
            ),
            t(
                "That makes 75 in each rak‘ah and 300 in all. Do not count aloud; press the fingers lightly to keep count.",
                "Is tarah har rakat mein 75 aur kul 300 hui. Zabaan se na ginein; ungliyon ko halka sa daba kar ginti rakhein.",
                "इस तरह हर रकअत में 75 और कुल 300 हुईं। ज़बान से न गिनें; उँगलियों को हल्का सा दबाकर गिनती रखें।",
                "اس طرح ہر رکعت میں 75 اور کل 300 ہوئیں۔ زبان سے نہ گنیں؛ انگلیوں کو ہلکا سا دبا کر گنتی رکھیں۔"
            )
        ),
        "Sunan Abi Dawud 1297 · Jami` at-Tirmidhi 481, 482",
        recitationIds = listOf("tasbih_kalima")
    ),
    SalahTopic(
        "istikhara",
        t("Istikhara", "Istikhara", "इस्तिख़ारा", "استخارہ"),
        SalahKind.NAFL,
        t(
            "Seeking Allah's guidance in a permissible choice",
            "Jaiz kaam mein Allah se khair talab karna",
            "जायज़ काम में अल्लाह से ख़ैर तलब करना",
            "جائز کام میں اللہ سے خیر طلب کرنا"
        ),
        listOf(
            t(
                "Pray two voluntary rak‘at outside the prohibited times.",
                "Makrooh waqton ke alawa do rakat Nafl padhein.",
                "मकरूह वक़्तों के अलावा दो रकअत नफ़्ल पढ़ें।",
                "مکروہ وقتوں کے علاوہ دو رکعت نفل پڑھیں۔"
            ),
            t(
                "After the salam recite the dua of Istikhara shown below, bringing your matter to mind.",
                "Salam ke baad neeche di gayi Istikhare ki dua padhein aur apne kaam ka khayal karein.",
                "सलाम के बाद नीचे दी गई इस्तिख़ारे की दुआ पढ़ें और अपने काम का ख़याल करें।",
                "سلام کے بعد نیچے دی گئی استخارے کی دعا پڑھیں اور اپنے کام کا خیال کریں۔"
            ),
            t(
                "Consult trustworthy people and act on what your heart settles upon.",
                "Mo'tabar logon se mashwara karein aur jis taraf dil ka itminan ho us par amal karein.",
                "मोतबर लोगों से मशवरा करें और जिस तरफ़ दिल का इत्मीनान हो उस पर अमल करें।",
                "معتبر لوگوں سے مشورہ کریں اور جس طرف دل کا اطمینان ہو اس پر عمل کریں۔"
            )
        ),
        "Sahih al-Bukhari 1162",
        recitationIds = listOf("istikhara"),
        learnForm = GuideForm.SUNNAH_2
    ),
    SalahTopic(
        "hajat",
        t("Salat-ul-Hajat", "Salat-ul-Hajat", "सलातुल हाजत", "صلاۃ الحاجت"),
        SalahKind.NAFL,
        t("Two rak‘at when you have a need", "Kisi haajat ke waqt do rakat", "किसी हाजत के वक़्त दो रकअत", "کسی حاجت کے وقت دو رکعت"),
        listOf(
            t(
                "Make a careful wudu and pray two rak‘at Nafl.",
                "Achhi tarah wuzu karke do rakat Nafl padhein.",
                "अच्छी तरह वुज़ू करके दो रकअत नफ़्ल पढ़ें।",
                "اچھی طرح وضو کر کے دو رکعت نفل پڑھیں۔"
            ),
            t(
                "Praise Allah, send Durood upon the Prophet ﷺ, then ask Allah for your need through the Prophet of Mercy ﷺ.",
                "Allah ki hamd karein, Nabi ﷺ par Durood bhejein, phir Nabi-e-Rahmat ﷺ ke waseele se Allah se apni haajat maangein.",
                "अल्लाह की हम्द करें, नबी ﷺ पर दुरूद भेजें, फिर नबी-ए-रहमत ﷺ के वसीले से अल्लाह से अपनी हाजत माँगें।",
                "اللہ کی حمد کریں، نبی ﷺ پر درود بھیجیں، پھر نبیِ رحمت ﷺ کے وسیلے سے اللہ سے اپنی حاجت مانگیں۔"
            ),
            t(
                "Keep asking with patience and trust in Allah.",
                "Sabr aur Allah par bharose ke saath dua karte rahein.",
                "सब्र और अल्लाह पर भरोसे के साथ दुआ करते रहें।",
                "صبر اور اللہ پر بھروسے کے ساتھ دعا کرتے رہیں۔"
            )
        ),
        "Sunan Ibn Majah 1385 · Jami` at-Tirmidhi 3578",
        learnForm = GuideForm.SUNNAH_2
    ),
    SalahTopic(
        "tahiyyat_wudu",
        t("Tahiyyat-ul-Wudu", "Tahiyyatul Wuzu", "तहिय्यतुल वुज़ू", "تحیۃ الوضو"),
        SalahKind.NAFL,
        t("Two rak‘at after wudu", "Wuzu ke baad do rakat", "वुज़ू के बाद दो रकअत", "وضو کے بعد دو رکعت"),
        listOf(
            t(
                "After completing wudu, before the limbs dry, pray two rak‘at with full attention.",
                "Wuzu ke baad aaza sookhne se pehle poori tawajjoh se do rakat padhein.",
                "वुज़ू के बाद आज़ा सूखने से पहले पूरी तवज्जोह से दो रकअत पढ़ें।",
                "وضو کے بعد اعضا سوکھنے سے پہلے پوری توجہ سے دو رکعت پڑھیں۔"
            ),
            t(
                "It is not prayed in the forbidden times.",
                "Yeh makrooh waqton mein nahi padhi jati.",
                "यह मकरूह वक़्तों में नहीं पढ़ी जाती।",
                "یہ مکروہ وقتوں میں نہیں پڑھی جاتی۔"
            )
        ),
        "Sahih al-Bukhari 1149 · Sahih Muslim 234",
        learnForm = GuideForm.SUNNAH_2
    ),
    SalahTopic(
        "masjid",
        t("Tahiyyat-ul-Masjid", "Tahiyyatul Masjid", "तहिय्यतुल मस्जिद", "تحیۃ المسجد"),
        SalahKind.NAFL,
        t("Greeting the mosque", "Masjid mein daakhil hone ki namaz", "मस्जिद में दाख़िल होने की नमाज़", "مسجد میں داخل ہونے کی نماز"),
        listOf(
            t(
                "On entering a mosque, pray two rak‘at before sitting when the time and circumstances allow.",
                "Masjid mein daakhil hokar, waqt aur mauqa ho to baithne se pehle do rakat padhein.",
                "मस्जिद में दाख़िल होकर, वक़्त और मौक़ा हो तो बैठने से पहले दो रकअत पढ़ें।",
                "مسجد میں داخل ہو کر، وقت اور موقع ہو تو بیٹھنے سے پہلے دو رکعت پڑھیں۔"
            ),
            t(
                "If the congregation has begun, join the Fard instead.",
                "Agar jamaat khadi ho chuki ho to Farz mein shamil ho jayein.",
                "अगर जमाअत खड़ी हो चुकी हो तो फ़र्ज़ में शामिल हो जाएँ।",
                "اگر جماعت کھڑی ہو چکی ہو تو فرض میں شامل ہو جائیں۔"
            )
        ),
        "Sahih al-Bukhari 444",
        schoolNotes = mapOf(
            Madhhab.HANAFI to t(
                "It is not prayed in the makruh times. A Fard or Sunnah prayed on entering takes its place.",
                "Makrooh waqton mein nahi padhi jati. Daakhil hote hi Farz ya Sunnat padh li to wahi iske qaim-maqam hai.",
                "मकरूह वक़्तों में नहीं पढ़ी जाती। दाख़िल होते ही फ़र्ज़ या सुन्नत पढ़ ली तो वही इसके क़ायम-मक़ाम है।",
                "مکروہ وقتوں میں نہیں پڑھی جاتی۔ داخل ہوتے ہی فرض یا سنت پڑھ لی تو وہی اس کے قائم مقام ہے۔"
            )
        ),
        learnForm = GuideForm.SUNNAH_2
    ),
    SalahTopic(
        "tilawat",
        t("Sajdah Tilawat", "Sajda-e-Tilawat", "सजदा-ए-तिलावत", "سجدۂ تلاوت"),
        SalahKind.WAJIB,
        t(
            "The prostration for a verse of sajdah",
            "Aayat-e-sajda ka sajda",
            "आयत-ए-सजदा का सजदा",
            "آیتِ سجدہ کا سجدہ"
        ),
        listOf(
            t(
                "It is performed on reciting or hearing a verse of sajdah in the Quran.",
                "Quran ki aayat-e-sajda padhne ya sunne par yeh sajda kiya jata hai.",
                "क़ुरआन की आयत-ए-सजदा पढ़ने या सुनने पर यह सजदा किया जाता है।",
                "قرآن کی آیتِ سجدہ پڑھنے یا سننے پر یہ سجدہ کیا جاتا ہے۔"
            ),
            t(
                "With wudu and facing the Qibla, say Allahu Akbar, make one sajdah, recite its tasbih three times, and rise saying Allahu Akbar.",
                "Ba-wuzu, Qibla rukh hokar Allahu Akbar kehte hue ek sajda karein, teen baar sajde ki tasbeeh padhein aur Allahu Akbar kehte hue uthein.",
                "बा-वुज़ू, क़िबला रुख़ होकर अल्लाहु अकबर कहते हुए एक सजदा करें, तीन बार सजदे की तस्बीह पढ़ें और अल्लाहु अकबर कहते हुए उठें।",
                "باوضو، قبلہ رخ ہو کر اللہ اکبر کہتے ہوئے ایک سجدہ کریں، تین بار سجدے کی تسبیح پڑھیں اور اللہ اکبر کہتے ہوئے اٹھیں۔"
            )
        ),
        "Sahih al-Bukhari 1075 · Sahih Muslim 81",
        schoolNotes = mapOf(
            Madhhab.HANAFI to t(
                "Wajib, at fourteen places in the Quran. There is no raising of the hands, no Tashahhud and no salam; going down from standing is recommended.",
                "Wajib hai, Quran mein chaudah maqamaat par. Isme na haath uthana hai, na At-Tahiyyat, na salam; khade hokar sajde mein jana mustahab hai.",
                "वाजिब है, क़ुरआन में चौदह मक़ामात पर। इसमें न हाथ उठाना है, न अत्तहिय्यात, न सलाम; खड़े होकर सजदे में जाना मुस्तहब है।",
                "واجب ہے، قرآن میں چودہ مقامات پر۔ اس میں نہ ہاتھ اٹھانا ہے، نہ التحیات، نہ سلام؛ کھڑے ہو کر سجدے میں جانا مستحب ہے۔"
            )
        ) + otherSchools.associateWith {
            t(
                "Sunnah rather than Wajib; the number of places, and whether it ends with a salam, differ between the schools.",
                "Wajib nahi balke Sunnat hai; maqamaat ki ta'daad aur salam ke baare mein mazahib ka farq hai.",
                "वाजिब नहीं बल्कि सुन्नत है; मक़ामात की तादाद और सलाम के बारे में मज़ाहिब का फ़र्क़ है।",
                "واجب نہیں بلکہ سنت ہے؛ مقامات کی تعداد اور سلام کے بارے میں مذاہب کا فرق ہے۔"
            )
        },
        kindBySchool = otherSchools.associateWith { SalahKind.SUNNAH },
        recitationIds = listOf("sujud")
    ),
    SalahTopic(
        "kusuf",
        t("Eclipse prayer", "Girhan ki namaz", "ग्रहण की नमाज़", "گرہن کی نماز"),
        SalahKind.SUNNAH,
        t(
            "Prayer during a solar or lunar eclipse",
            "Sooraj ya chaand girhan ke waqt ki namaz",
            "सूरज या चाँद ग्रहण के वक़्त की नमाज़",
            "سورج یا چاند گرہن کے وقت کی نماز"
        ),
        listOf(
            t(
                "When an eclipse is seen, turn to prayer, dua, istighfar and charity until it clears.",
                "Girhan nazar aaye to uske khatm hone tak namaz, dua, istighfar aur sadqe mein lage rahein.",
                "ग्रहण नज़र आए तो उसके ख़त्म होने तक नमाज़, दुआ, इस्तिग़फ़ार और सदक़े में लगे रहें।",
                "گرہن نظر آئے تو اس کے ختم ہونے تک نماز، دعا، استغفار اور صدقے میں لگے رہیں۔"
            ),
            t(
                "The form of the prayer varies by school; join a knowledgeable congregation where possible.",
                "Namaz ka tareeqa mazhab ke mutabiq mukhtalif hai; ho sake to kisi aalim ki jamaat mein shamil hon.",
                "नमाज़ का तरीक़ा मज़हब के मुताबिक़ मुख़्तलिफ़ है; हो सके तो किसी आलिम की जमाअत में शामिल हों।",
                "نماز کا طریقہ مذہب کے مطابق مختلف ہے؛ ہو سکے تو کسی عالم کی جماعت میں شامل ہوں۔"
            )
        ),
        "Sahih al-Bukhari 1040, 1041",
        schoolNotes = mapOf(
            Madhhab.HANAFI to t(
                "Two rak‘at like any Nafl, each with one ruku‘. The solar eclipse prayer is in congregation with a long recitation and no khutbah; the lunar eclipse prayer is prayed alone.",
                "Aam Nafl ki tarah do rakat, har rakat mein ek ruku. Sooraj girhan ki namaz jamaat se, lambi qira'at ke saath aur baghair khutbe ke; chaand girhan ki namaz akele padhi jati hai.",
                "आम नफ़्ल की तरह दो रकअत, हर रकअत में एक रुकू। सूरज ग्रहण की नमाज़ जमाअत से, लंबी क़िराअत के साथ और बग़ैर ख़ुत्बे के; चाँद ग्रहण की नमाज़ अकेले पढ़ी जाती है।",
                "عام نفل کی طرح دو رکعت، ہر رکعت میں ایک رکوع۔ سورج گرہن کی نماز جماعت سے، لمبی قراءت کے ساتھ اور بغیر خطبے کے؛ چاند گرہن کی نماز اکیلے پڑھی جاتی ہے۔"
            )
        ) + otherSchools.associateWith {
            t(
                "Each rak‘ah of the solar eclipse prayer has two standings and two ruku‘.",
                "Sooraj girhan ki namaz ki har rakat mein do qiyam aur do ruku hain.",
                "सूरज ग्रहण की नमाज़ की हर रकअत में दो क़ियाम और दो रुकू हैं।",
                "سورج گرہن کی نماز کی ہر رکعت میں دو قیام اور دو رکوع ہیں۔"
            )
        }
    ),
    SalahTopic(
        "istisqa",
        t("Istisqa", "Namaz-e-Istisqa", "नमाज़-ए-इस्तिस्क़ा", "نمازِ استسقاء"),
        null,
        t("The prayer for rain", "Baarish ki dua ki namaz", "बारिश की दुआ की नमाज़", "بارش کی دعا کی نماز"),
        listOf(
            t(
                "Turn to Allah with repentance, istighfar and charity, and ask Him for rain.",
                "Tauba, istighfar aur sadqe ke saath Allah ki taraf rujoo karein aur baarish ki dua maangein.",
                "तौबा, इस्तिग़फ़ार और सदक़े के साथ अल्लाह की तरफ़ रुजू करें और बारिश की दुआ माँगें।",
                "توبہ، استغفار اور صدقے کے ساتھ اللہ کی طرف رجوع کریں اور بارش کی دعا مانگیں۔"
            ),
            t(
                "Join a community prayer and supplication led by a knowledgeable imam where available.",
                "Jahan ho sake kisi aalim imam ki iqtida mein ijtimai namaz aur dua mein shamil hon.",
                "जहाँ हो सके किसी आलिम इमाम की इक़्तिदा में इज्तिमाई नमाज़ और दुआ में शामिल हों।",
                "جہاں ہو سکے کسی عالم امام کی اقتدا میں اجتماعی نماز اور دعا میں شامل ہوں۔"
            )
        ),
        "Sahih al-Bukhari 1013"
    )
)
