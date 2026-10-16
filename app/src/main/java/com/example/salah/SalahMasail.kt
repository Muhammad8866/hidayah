package com.example.salah

/**
 * The rulings a worshipper needs around the prayer: purification, what the prayer is made of,
 * what spoils it, and the common situations (congregation, mistakes, missed prayers, travel).
 * Hanafi rulings follow Bahar-e-Shariat, Hissa 2–4; other schools are covered by school notes.
 */
internal val salahRules: List<SalahTopic> = listOf(
    SalahTopic(
        "sharait",
        t("Conditions of Salah", "Namaz ki shartein", "नमाज़ की शर्तें", "نماز کی شرطیں"),
        null,
        t(
            "Six things that must be in place before the prayer",
            "Chhe cheezein jo namaz se pehle zaroori hain",
            "छह चीज़ें जो नमाज़ से पहले ज़रूरी हैं",
            "چھ چیزیں جو نماز سے پہلے ضروری ہیں"
        ),
        listOf(
            t(
                "Taharah: the body, the clothes and the place of prayer are clean, and you are in wudu, or have made ghusl where it is due.",
                "Taharat: badan, kapde aur namaz ki jagah paak ho, aur wuzu ho; ghusl farz ho to ghusl kiya ho.",
                "तहारत: बदन, कपड़े और नमाज़ की जगह पाक हो, और वुज़ू हो; ग़ुस्ल फ़र्ज़ हो तो ग़ुस्ल किया हो।",
                "طہارت: بدن، کپڑے اور نماز کی جگہ پاک ہو، اور وضو ہو؛ غسل فرض ہو تو غسل کیا ہو۔"
            ),
            t(
                "Satr-e-awrah: a man covers from the navel to below the knees; a woman covers her whole body except the face, the hands and the feet.",
                "Satr-e-aurat: mard naaf se ghutnon ke neeche tak dhaanke; aurat chehre, hatheliyon aur paon ke siwa poora badan dhaanke.",
                "सत्र-ए-औरत: मर्द नाफ़ से घुटनों के नीचे तक ढाँके; औरत चेहरे, हथेलियों और पाँव के सिवा पूरा बदन ढाँके।",
                "سترِ عورت: مرد ناف سے گھٹنوں کے نیچے تک ڈھانکے؛ عورت چہرے، ہتھیلیوں اور پاؤں کے سوا پورا بدن ڈھانکے۔"
            ),
            t(
                "Istiqbal-e-Qibla: facing the Ka‘bah.",
                "Istiqbal-e-Qibla: Kaaba sharif ki taraf munh karna.",
                "इस्तिक़बाल-ए-क़िबला: काबा शरीफ़ की तरफ़ मुँह करना।",
                "استقبالِ قبلہ: کعبہ شریف کی طرف منہ کرنا۔"
            ),
            t(
                "Waqt: the time of that prayer has entered.",
                "Waqt: us namaz ka waqt ho chuka ho.",
                "वक़्त: उस नमाज़ का वक़्त हो चुका हो।",
                "وقت: اس نماز کا وقت ہو چکا ہو۔"
            ),
            t(
                "Niyyah: a firm intention in the heart for that particular prayer.",
                "Niyyat: dil mein us khaas namaz ka pakka irada.",
                "नीयत: दिल में उस ख़ास नमाज़ का पक्का इरादा।",
                "نیت: دل میں اس خاص نماز کا پکا ارادہ۔"
            ),
            t(
                "Takbir-e-Tahrimah: beginning the prayer with Allahu Akbar.",
                "Takbeer-e-Tahreema: Allahu Akbar keh kar namaz shuru karna.",
                "तकबीर-ए-तहरीमा: अल्लाहु अकबर कहकर नमाज़ शुरू करना।",
                "تکبیرِ تحریمہ: اللہ اکبر کہہ کر نماز شروع کرنا۔"
            )
        ),
        "Quran 5:6, 7:31, 2:144, 4:103 · Bahar-e-Shariat, Hissa 3"
    ),
    SalahTopic(
        "faraiz",
        t("Fard acts of Salah", "Namaz ke faraiz", "नमाज़ के फ़राइज़", "نماز کے فرائض"),
        null,
        t(
            "Seven pillars inside the prayer",
            "Namaz ke andar ke saat farz",
            "नमाज़ के अंदर के सात फ़र्ज़",
            "نماز کے اندر کے سات فرض"
        ),
        listOf(
            t("Takbir-e-Tahrimah.", "Takbeer-e-Tahreema.", "तकबीर-ए-तहरीमा।", "تکبیرِ تحریمہ۔"),
            t(
                "Qiyam: standing, for one who is able, in Fard and Wajib prayers.",
                "Qiyam: Farz aur Wajib namaz mein khada hona, jo khada ho sakta ho.",
                "क़ियाम: फ़र्ज़ और वाजिब नमाज़ में खड़ा होना, जो खड़ा हो सकता हो।",
                "قیام: فرض اور واجب نماز میں کھڑا ہونا، جو کھڑا ہو سکتا ہو۔"
            ),
            t(
                "Qira’at: reciting the Quran while standing.",
                "Qira'at: qiyam mein Quran padhna.",
                "क़िराअत: क़ियाम में क़ुरआन पढ़ना।",
                "قراءت: قیام میں قرآن پڑھنا۔"
            ),
            t("Ruku‘.", "Ruku.", "रुकू।", "رکوع۔"),
            t(
                "Sujud: two sajdahs in every rak‘ah.",
                "Sujood: har rakat mein do sajde.",
                "सुजूद: हर रकअत में दो सजदे।",
                "سجود: ہر رکعت میں دو سجدے۔"
            ),
            t(
                "Qa‘dah Akhirah: sitting at the end for the length of the Tashahhud.",
                "Qa'da-e-Akheera: aakhir mein At-Tahiyyat ki miqdaar baithna.",
                "क़ादा-ए-अख़ीरा: आख़िर में अत्तहिय्यात की मिक़दार बैठना।",
                "قعدۂ اخیرہ: آخر میں التحیات کی مقدار بیٹھنا۔"
            ),
            t(
                "Khuruj bi-sun‘ihi: leaving the prayer by an act of your own, such as the salam.",
                "Khurooj bi-sun'ihi: apne iraade se namaz khatm karna, jaise salam pherna.",
                "ख़ुरूज बि-सुनइही: अपने इरादे से नमाज़ ख़त्म करना, जैसे सलाम फेरना।",
                "خروج بصنعہٖ: اپنے ارادے سے نماز ختم کرنا، جیسے سلام پھیرنا۔"
            )
        ),
        "Quran 22:77, 73:20, 2:238 · Sahih al-Bukhari 757 · Bahar-e-Shariat, Hissa 3",
        footnote = t(
            "A missed Fard cannot be repaired by Sajdah Sahw; the prayer must be prayed again.",
            "Farz chhoot jaye to Sajda-e-Sahw se talaafi nahi hoti; namaz dobara padhni hogi.",
            "फ़र्ज़ छूट जाए तो सजदा-ए-सह्व से तलाफ़ी नहीं होती; नमाज़ दोबारा पढ़नी होगी।",
            "فرض چھوٹ جائے تو سجدۂ سہو سے تلافی نہیں ہوتی؛ نماز دوبارہ پڑھنی ہوگی۔"
        )
    ),
    SalahTopic(
        "wajibat",
        t("Wajib acts of Salah", "Namaz ke wajibaat", "नमाज़ के वाजिबात", "نماز کے واجبات"),
        null,
        t(
            "The necessary acts that complete the pillars",
            "Woh zaroori kaam jo faraiz ko mukammal karte hain",
            "वे ज़रूरी काम जो फ़राइज़ को मुकम्मल करते हैं",
            "وہ ضروری کام جو فرائض کو مکمل کرتے ہیں"
        ),
        listOf(
            t(
                "Beginning the prayer with the words Allahu Akbar.",
                "Namaz 'Allahu Akbar' ke lafz se shuru karna.",
                "नमाज़ 'अल्लाहु अकबर' के लफ़्ज़ से शुरू करना।",
                "نماز ’اللہ اکبر‘ کے لفظ سے شروع کرنا۔"
            ),
            t(
                "Reciting Surah al-Fatihah, and adding a surah or three short verses in the first two rak‘at of Fard and in every rak‘ah of Witr, Sunnah and Nafl.",
                "Surah Fatiha padhna, aur Farz ki pehli do rakaton mein aur Witr, Sunnat aur Nafl ki har rakat mein surat ya teen chhoti aayatein milana.",
                "सूरह फ़ातिहा पढ़ना, और फ़र्ज़ की पहली दो रकअतों में और वित्र, सुन्नत और नफ़्ल की हर रकअत में सूरत या तीन छोटी आयतें मिलाना।",
                "سورۃ فاتحہ پڑھنا، اور فرض کی پہلی دو رکعتوں میں اور وتر، سنت اور نفل کی ہر رکعت میں سورت یا تین چھوٹی آیتیں ملانا۔"
            ),
            t(
                "Reciting al-Fatihah before the surah.",
                "Fatiha ko surat se pehle padhna.",
                "फ़ातिहा को सूरत से पहले पढ़ना।",
                "فاتحہ کو سورت سے پہلے پڑھنا۔"
            ),
            t(
                "Qawmah, standing upright after ruku‘, and Jalsa, sitting upright between the two sajdahs.",
                "Qauma (ruku ke baad seedha khada hona) aur Jalsa (dono sajdon ke darmiyan seedha baithna).",
                "क़ौमा (रुकू के बाद सीधा खड़ा होना) और जलसा (दोनों सजदों के दरमियान सीधा बैठना)।",
                "قومہ (رکوع کے بعد سیدھا کھڑا ہونا) اور جلسہ (دونوں سجدوں کے درمیان سیدھا بیٹھنا)۔"
            ),
            t(
                "Ta‘dil-e-arkan: staying still in ruku‘, qawmah, sajdah and jalsa for at least one ‘Subhanallah’.",
                "Ta'deel-e-arkaan: ruku, qauma, sajda aur jalsa mein kam az kam ek baar 'Subhanallah' kehne ki miqdaar thehrna.",
                "तादील-ए-अरकान: रुकू, क़ौमा, सजदा और जलसा में कम से कम एक बार 'सुब्हानल्लाह' कहने की मिक़दार ठहरना।",
                "تعدیلِ ارکان: رکوع، قومہ، سجدہ اور جلسہ میں کم از کم ایک بار ’سبحان اللہ‘ کہنے کی مقدار ٹھہرنا۔"
            ),
            t(
                "Qa‘dah Ula, the first sitting in a three- or four-rak‘at prayer, and reciting the full Tashahhud in both sittings.",
                "Qa'da-e-Oola (teen ya chaar rakat wali namaz ka pehla qa'da), aur dono qa'don mein poori At-Tahiyyat padhna.",
                "क़ादा-ए-ऊला (तीन या चार रकअत वाली नमाज़ का पहला क़ादा), और दोनों क़ादों में पूरी अत्तहिय्यात पढ़ना।",
                "قعدۂ اولیٰ (تین یا چار رکعت والی نماز کا پہلا قعدہ)، اور دونوں قعدوں میں پوری التحیات پڑھنا۔"
            ),
            t(
                "Adding nothing after the Tashahhud in the first sitting of Fard, Witr and Sunnah Muakkadah.",
                "Farz, Witr aur Sunnat-e-Muakkada ke pehle qa'de mein At-Tahiyyat ke baad kuch na badhana.",
                "फ़र्ज़, वित्र और सुन्नत-ए-मुअक्कदा के पहले क़ादे में अत्तहिय्यात के बाद कुछ न बढ़ाना।",
                "فرض، وتر اور سنتِ مؤکدہ کے پہلے قعدے میں التحیات کے بعد کچھ نہ بڑھانا۔"
            ),
            t(
                "Ending the prayer with the word ‘As-salam’ twice.",
                "Lafz 'As-salam' do baar keh kar namaz khatm karna.",
                "लफ़्ज़ 'अस्सलाम' दो बार कहकर नमाज़ ख़त्म करना।",
                "لفظ ’السلام‘ دو بار کہہ کر نماز ختم کرنا۔"
            ),
            t(
                "Dua-e-Qunut in Witr, and the takbir before it.",
                "Witr mein Dua-e-Qunoot aur usse pehle ki takbeer.",
                "वित्र में दुआ-ए-क़ुनूत और उससे पहले की तकबीर।",
                "وتر میں دعائے قنوت اور اس سے پہلے کی تکبیر۔"
            ),
            t(
                "The six extra takbirs of the two Eid prayers.",
                "Eidain ki chhe zaaid takbeerein.",
                "ईदैन की छह ज़ाइद तकबीरें।",
                "عیدین کی چھ زائد تکبیریں۔"
            ),
            t(
                "The imam reciting aloud in Fajr, Maghrib, Isha, Jumu‘ah and Eid, and quietly in Zuhr and Asr.",
                "Imam ka Fajr, Maghrib, Isha, Jumu'ah aur Eid mein aawaz se, aur Zuhr aur Asr mein aahista qira'at karna.",
                "इमाम का फ़ज्र, मग़रिब, इशा, जुमा और ईद में आवाज़ से, और ज़ुहर और अस्र में आहिस्ता क़िराअत करना।",
                "امام کا فجر، مغرب، عشاء، جمعہ اور عید میں آواز سے، اور ظہر اور عصر میں آہستہ قراءت کرنا۔"
            ),
            t(
                "The follower staying silent while the imam recites, and following the imam in every act.",
                "Muqtadi ka imam ki qira'at ke waqt khamosh rehna aur har rukn mein imam ki pairwi karna.",
                "मुक़्तदी का इमाम की क़िराअत के वक़्त ख़ामोश रहना और हर रुक्न में इमाम की पैरवी करना।",
                "مقتدی کا امام کی قراءت کے وقت خاموش رہنا اور ہر رکن میں امام کی پیروی کرنا۔"
            ),
            t(
                "Keeping every Fard and Wajib in its place, without delaying or repeating it.",
                "Har Farz aur Wajib ko uski jagah par ada karna, na der karna na dohrana.",
                "हर फ़र्ज़ और वाजिब को उसकी जगह पर अदा करना, न देर करना न दोहराना।",
                "ہر فرض اور واجب کو اس کی جگہ پر ادا کرنا، نہ دیر کرنا نہ دہرانا۔"
            )
        ),
        "Sahih al-Bukhari 756, 757 · Sahih Muslim 404 · Bahar-e-Shariat, Hissa 3",
        numbered = false,
        footnote = t(
            "If a Wajib is left by mistake, Sajdah Sahw repairs it. If it is left on purpose, the prayer must be prayed again.",
            "Wajib bhool se chhoot jaye to Sajda-e-Sahw se talaafi ho jati hai. Jaan boojh kar chhoda to namaz dobara padhna Wajib hai.",
            "वाजिब भूल से छूट जाए तो सजदा-ए-सह्व से तलाफ़ी हो जाती है। जान-बूझकर छोड़ा तो नमाज़ दोबारा पढ़ना वाजिब है।",
            "واجب بھول سے چھوٹ جائے تو سجدۂ سہو سے تلافی ہو جاتی ہے۔ جان بوجھ کر چھوڑا تو نماز دوبارہ پڑھنا واجب ہے۔"
        )
    ),
    SalahTopic(
        "sunan",
        t("Sunnah acts of Salah", "Namaz ki sunnatein", "नमाज़ की सुन्नतें", "نماز کی سنتیں"),
        null,
        t(
            "They perfect the prayer; missing one does not break it",
            "Inse namaz kaamil hoti hai; chhootne se namaz nahi toot-ti",
            "इनसे नमाज़ कामिल होती है; छूटने से नमाज़ नहीं टूटती",
            "ان سے نماز کامل ہوتی ہے؛ چھوٹنے سے نماز نہیں ٹوٹتی"
        ),
        listOf(
            t(
                "Raising the hands for Takbir-e-Tahrimah: men to the ears, women to the shoulders.",
                "Takbeer-e-Tahreema ke liye haath uthana: mard kaanon tak, aurat kandhon tak.",
                "तकबीर-ए-तहरीमा के लिए हाथ उठाना: मर्द कानों तक, औरत कंधों तक।",
                "تکبیرِ تحریمہ کے لیے ہاتھ اٹھانا: مرد کانوں تک، عورت کندھوں تک۔"
            ),
            t(
                "Folding the hands: men below the navel, women on the chest.",
                "Haath baandhna: mard naaf ke neeche, aurat seene par.",
                "हाथ बाँधना: मर्द नाफ़ के नीचे, औरत सीने पर।",
                "ہاتھ باندھنا: مرد ناف کے نیچے، عورت سینے پر۔"
            ),
            t(
                "Reciting Thana, Ta‘awwuz, Tasmiyah and Ameen, all of them quietly.",
                "Sana, Ta'awwuz, Tasmiya aur Ameen — sab aahista padhna.",
                "सना, तअव्वुज़, तस्मिया और आमीन — सब आहिस्ता पढ़ना।",
                "ثنا، تعوذ، تسمیہ اور آمین — سب آہستہ پڑھنا۔"
            ),
            t(
                "Saying Allahu Akbar at every change of position.",
                "Har rukn mein jaate waqt Allahu Akbar kehna.",
                "हर रुक्न में जाते वक़्त अल्लाहु अकबर कहना।",
                "ہر رکن میں جاتے وقت اللہ اکبر کہنا۔"
            ),
            t(
                "Reciting the tasbih three times in ruku‘ and in sajdah.",
                "Ruku aur sajde mein teen teen baar tasbeeh padhna.",
                "रुकू और सजदे में तीन-तीन बार तस्बीह पढ़ना।",
                "رکوع اور سجدے میں تین تین بار تسبیح پڑھنا۔"
            ),
            t(
                "Saying Sami‘allahu liman hamidah when rising and Rabbana wa lakal-hamd when upright.",
                "Ruku se uthte hue Sami'allahu liman hamidah aur seedhe hokar Rabbana wa lakal-hamd kehna.",
                "रुकू से उठते हुए समिअल्लाहु लिमन हमिदह और सीधे होकर रब्बना व लकल-हम्द कहना।",
                "رکوع سے اٹھتے ہوئے سمع اللہ لمن حمدہ اور سیدھے ہو کر ربنا ولک الحمد کہنا۔"
            ),
            t(
                "Going into sajdah knees first, then hands, then nose and forehead; and rising in the reverse order.",
                "Sajde mein pehle ghutne, phir haath, phir naak aur peshani rakhna; aur uthte waqt iska ulta.",
                "सजदे में पहले घुटने, फिर हाथ, फिर नाक और पेशानी रखना; और उठते वक़्त इसका उल्टा।",
                "سجدے میں پہلے گھٹنے، پھر ہاتھ، پھر ناک اور پیشانی رکھنا؛ اور اٹھتے وقت اس کا الٹا۔"
            ),
            t(
                "Reciting Durood-e-Ibrahim and a dua after the final Tashahhud.",
                "Aakhri At-Tahiyyat ke baad Durood-e-Ibrahimi aur dua padhna.",
                "आख़िरी अत्तहिय्यात के बाद दुरूद-ए-इब्राहीमी और दुआ पढ़ना।",
                "آخری التحیات کے بعد درودِ ابراہیمی اور دعا پڑھنا۔"
            ),
            t(
                "Turning the face to the right, then to the left, at the salam.",
                "Salam mein pehle daayin phir baayin taraf munh pherna.",
                "सलाम में पहले दाईं फिर बाईं तरफ़ मुँह फेरना।",
                "سلام میں پہلے دائیں پھر بائیں طرف منہ پھیرنا۔"
            )
        ),
        "Sunan Abi Dawud 775, 886 · Sahih al-Bukhari 789 · Bahar-e-Shariat, Hissa 3",
        numbered = false
    ),
    SalahTopic(
        "mufsidat",
        t("What breaks the Salah", "Namaz todne wali cheezein", "नमाज़ तोड़ने वाली चीज़ें", "نماز توڑنے والی چیزیں"),
        null,
        t(
            "After any of these the prayer is started again",
            "In mein se kuch ho jaye to namaz naye sire se padhein",
            "इनमें से कुछ हो जाए तो नमाज़ नए सिरे से पढ़ें",
            "ان میں سے کچھ ہو جائے تو نماز نئے سرے سے پڑھیں"
        ),
        listOf(
            t(
                "Speaking, even one word — on purpose, by mistake or out of forgetfulness.",
                "Baat karna, chahe ek lafz ho — jaan boojh kar, ghalti se ya bhool kar.",
                "बात करना, चाहे एक लफ़्ज़ हो — जान-बूझकर, ग़लती से या भूलकर।",
                "بات کرنا، چاہے ایک لفظ ہو — جان بوجھ کر، غلطی سے یا بھول کر۔"
            ),
            t(
                "Greeting someone, answering a greeting, or replying to a sneeze.",
                "Kisi ko salam karna, salam ka jawab dena ya chheenk ka jawab dena.",
                "किसी को सलाम करना, सलाम का जवाब देना या छींक का जवाब देना।",
                "کسی کو سلام کرنا، سلام کا جواب دینا یا چھینک کا جواب دینا۔"
            ),
            t(
                "Eating or drinking, even a little.",
                "Khana ya peena, chahe thoda sa ho.",
                "खाना या पीना, चाहे थोड़ा सा हो।",
                "کھانا یا پینا، چاہے تھوڑا سا ہو۔"
            ),
            t(
                "‘Amal-e-kathir: so much movement that an onlooker would think you are not praying.",
                "Amal-e-kaseer: itni harkat ke dekhne wala samjhe yeh namaz mein nahi hai.",
                "अमल-ए-कसीर: इतनी हरकत कि देखने वाला समझे यह नमाज़ में नहीं है।",
                "عملِ کثیر: اتنی حرکت کہ دیکھنے والا سمجھے یہ نماز میں نہیں ہے۔"
            ),
            t(
                "Turning the chest away from the Qibla without a valid excuse.",
                "Bila uzr seena Qible se pher lena.",
                "बिला उज़्र सीना क़िबले से फेर लेना।",
                "بلا عذر سینہ قبلے سے پھیر لینا۔"
            ),
            t(
                "Laughing aloud. An adult's loud laugh in a prayer that has ruku‘ and sajdah also breaks the wudu.",
                "Aawaz se hansna. Baaligh ka ruku-sajde wali namaz mein qahqaha lagana wuzu bhi tod deta hai.",
                "आवाज़ से हँसना। बालिग़ का रुकू-सजदे वाली नमाज़ में क़हक़हा लगाना वुज़ू भी तोड़ देता है।",
                "آواز سے ہنسنا۔ بالغ کا رکوع سجدے والی نماز میں قہقہہ لگانا وضو بھی توڑ دیتا ہے۔"
            ),
            t(
                "Crying or groaning aloud from pain or worldly grief. Weeping from the fear of Allah does not break it.",
                "Dard ya duniyavi gham se aawaz ke saath rona ya karahna. Allah ke khauf se rona namaz nahi todta.",
                "दर्द या दुनियावी ग़म से आवाज़ के साथ रोना या कराहना। अल्लाह के ख़ौफ़ से रोना नमाज़ नहीं तोड़ता।",
                "درد یا دنیاوی غم سے آواز کے ساتھ رونا یا کراہنا۔ اللہ کے خوف سے رونا نماز نہیں توڑتا۔"
            ),
            t(
                "The wudu breaking, or a quarter of a limb of the awrah staying uncovered for the time of three tasbih.",
                "Wuzu toot jana, ya satr ke kisi uzw ka chauthai hissa teen tasbeeh ki miqdaar khula rehna.",
                "वुज़ू टूट जाना, या सत्र के किसी उज़्व का चौथाई हिस्सा तीन तस्बीह की मिक़दार खुला रहना।",
                "وضو ٹوٹ جانا، یا ستر کے کسی عضو کا چوتھائی حصہ تین تسبیح کی مقدار کھلا رہنا۔"
            ),
            t(
                "Leaving out a Fard, or a mistake in recitation that corrupts the meaning.",
                "Koi Farz chhod dena, ya qira'at mein aisi ghalti jisse ma'na bigad jaye.",
                "कोई फ़र्ज़ छोड़ देना, या क़िराअत में ऐसी ग़लती जिससे मानी बिगड़ जाए।",
                "کوئی فرض چھوڑ دینا، یا قراءت میں ایسی غلطی جس سے معنی بگڑ جائے۔"
            )
        ),
        "Sahih Muslim 537 · Sahih al-Bukhari 1199 · Bahar-e-Shariat, Hissa 3",
        numbered = false
    ),
    SalahTopic(
        "makruhat",
        t("Disliked acts (Makruhat)", "Namaz ke makroohaat", "नमाज़ के मकरूहात", "نماز کے مکروہات"),
        null,
        t(
            "They reduce the reward of the prayer",
            "Inse namaz ka sawab kam ho jata hai",
            "इनसे नमाज़ का सवाब कम हो जाता है",
            "ان سے نماز کا ثواب کم ہو جاتا ہے"
        ),
        listOf(
            t(
                "Playing with the clothes, the body or the beard.",
                "Kapde, badan ya daadhi se khelna.",
                "कपड़े, बदन या दाढ़ी से खेलना।",
                "کپڑے، بدن یا داڑھی سے کھیلنا۔"
            ),
            t(
                "Looking around by turning the face, or looking up at the sky.",
                "Munh pher kar idhar-udhar dekhna, ya aasman ki taraf nazar uthana.",
                "मुँह फेरकर इधर-उधर देखना, या आसमान की तरफ़ नज़र उठाना।",
                "منہ پھیر کر اِدھر اُدھر دیکھنا، یا آسمان کی طرف نظر اٹھانا۔"
            ),
            t(
                "Praying while holding back the urge to relieve yourself or to pass wind.",
                "Peshab, pakhane ya reeh ki shiddat ko rok kar namaz padhna.",
                "पेशाब, पाख़ाने या रीह की शिद्दत को रोककर नमाज़ पढ़ना।",
                "پیشاب، پاخانے یا ریح کی شدت کو روک کر نماز پڑھنا۔"
            ),
            t(
                "Gathering or folding up the clothes, such as rolled-up sleeves, or letting a cloth hang loose from the shoulders.",
                "Kapda sametna ya modna, jaise aasteen chadhi hona, ya kapda kandhon se latka chhodna (sadl).",
                "कपड़ा समेटना या मोड़ना, जैसे आस्तीन चढ़ी होना, या कपड़ा कंधों से लटका छोड़ना (सद्ल)।",
                "کپڑا سمیٹنا یا موڑنا، جیسے آستین چڑھی ہونا، یا کپڑا کندھوں سے لٹکا چھوڑنا (سدل)۔"
            ),
            t(
                "Cracking the knuckles or interlacing the fingers.",
                "Ungliyan chatkhana ya ek haath ki ungliyan doosre mein daalna.",
                "उँगलियाँ चटख़ाना या एक हाथ की उँगलियाँ दूसरे में डालना।",
                "انگلیاں چٹخانا یا ایک ہاتھ کی انگلیاں دوسرے میں ڈالنا۔"
            ),
            t(
                "Placing the hands on the hips.",
                "Kamar (kokh) par haath rakhna.",
                "कमर (कोख) पर हाथ रखना।",
                "کمر (کوکھ) پر ہاتھ رکھنا۔"
            ),
            t(
                "A man laying his forearms flat on the ground in sajdah.",
                "Mard ka sajde mein kalaiyan zameen par bichhana.",
                "मर्द का सजदे में कलाइयाँ ज़मीन पर बिछाना।",
                "مرد کا سجدے میں کلائیاں زمین پر بچھانا۔"
            ),
            t(
                "Praying in clothes that carry a picture of a living being, or with one in front of you.",
                "Jaandaar ki tasveer wale kapde mein, ya tasveer saamne ho to namaz padhna.",
                "जानदार की तस्वीर वाले कपड़े में, या तस्वीर सामने हो तो नमाज़ पढ़ना।",
                "جاندار کی تصویر والے کپڑے میں، یا تصویر سامنے ہو تو نماز پڑھنا۔"
            ),
            t(
                "Praying when food is served and the heart is drawn to it.",
                "Khana saamne ho aur dil usi mein laga ho, us waqt namaz padhna.",
                "खाना सामने हो और दिल उसी में लगा हो, उस वक़्त नमाज़ पढ़ना।",
                "کھانا سامنے ہو اور دل اسی میں لگا ہو، اس وقت نماز پڑھنا۔"
            ),
            t(
                "Rushing: not standing upright after ruku‘, or not sitting upright between the two sajdahs.",
                "Jaldbaazi: ruku ke baad seedha khada na hona ya dono sajdon ke darmiyan seedha na baithna.",
                "जल्दबाज़ी: रुकू के बाद सीधा खड़ा न होना या दोनों सजदों के दरमियान सीधा न बैठना।",
                "جلد بازی: رکوع کے بعد سیدھا کھڑا نہ ہونا یا دونوں سجدوں کے درمیان سیدھا نہ بیٹھنا۔"
            )
        ),
        "Sahih al-Bukhari 750, 751, 822, 1220 · Sahih Muslim 560 · Bahar-e-Shariat, Hissa 3",
        schoolNotes = mapOf(
            Madhhab.HANAFI to t(
                "A prayer offered with a Makruh-e-Tahrimi act is valid but must be repeated; a Makruh-e-Tanzihi act only lessens the reward.",
                "Makrooh-e-tahreemi ke saath padhi gayi namaz ho to jati hai magar use dohrana Wajib hai; makrooh-e-tanzeehi se sirf sawab kam hota hai.",
                "मकरूह-ए-तहरीमी के साथ पढ़ी गई नमाज़ हो तो जाती है मगर उसे दोहराना वाजिब है; मकरूह-ए-तंज़ीही से सिर्फ़ सवाब कम होता है।",
                "مکروہِ تحریمی کے ساتھ پڑھی گئی نماز ہو تو جاتی ہے مگر اسے دہرانا واجب ہے؛ مکروہِ تنزیہی سے صرف ثواب کم ہوتا ہے۔"
            )
        ),
        numbered = false
    ),
    SalahTopic(
        "times",
        t("Times when Salah is not prayed", "Makrooh auqaat", "मकरूह औक़ात", "مکروہ اوقات"),
        null,
        t(
            "Three forbidden times, and three when Nafl is not prayed",
            "Teen mamnoo waqt, aur teen waqt jin mein Nafl nahi",
            "तीन ममनू वक़्त, और तीन वक़्त जिनमें नफ़्ल नहीं",
            "تین ممنوع وقت، اور تین وقت جن میں نفل نہیں"
        ),
        listOf(
            t(
                "Sunrise: from the first edge of the sun until it has risen well, about twenty minutes.",
                "Tuloo-e-aaftab: sooraj ka kinara nikalne se achhi tarah buland hone tak, taqriban 20 minute.",
                "तुलू-ए-आफ़ताब: सूरज का किनारा निकलने से अच्छी तरह बुलंद होने तक, तक़रीबन 20 मिनट।",
                "طلوعِ آفتاب: سورج کا کنارہ نکلنے سے اچھی طرح بلند ہونے تک، تقریباً 20 منٹ۔"
            ),
            t(
                "Midday: from Dahwa-e-Kubra until the sun passes its highest point (Zawal).",
                "Nisf-un-nahar: Zahwa-e-Kubra se sooraj dhalne (Zawal) tak.",
                "निस्फ़ुन्नहार: ज़हवा-ए-कुबरा से सूरज ढलने (ज़वाल) तक।",
                "نصف النہار: ضحوۂ کبریٰ سے سورج ڈھلنے (زوال) تک۔"
            ),
            t(
                "Sunset: from when the sun turns pale until it has set, about twenty minutes — except that day's Asr, which must still be prayed.",
                "Ghuroob-e-aaftab: sooraj ke peela padne se doobne tak, taqriban 20 minute — magar usi din ki Asr padh li jaye.",
                "ग़ुरूब-ए-आफ़ताब: सूरज के पीला पड़ने से डूबने तक, तक़रीबन 20 मिनट — मगर उसी दिन की अस्र पढ़ ली जाए।",
                "غروبِ آفتاب: سورج کے پیلا پڑنے سے ڈوبنے تک، تقریباً 20 منٹ — مگر اسی دن کی عصر پڑھ لی جائے۔"
            ),
            t(
                "In these three times no prayer is allowed: not Fard, Wajib, Nafl or Qaza, nor Sajdah Tilawat.",
                "In teeno waqton mein koi namaz jaiz nahi: na Farz, na Wajib, na Nafl, na Qaza, na Sajda-e-Tilawat.",
                "इन तीनों वक़्तों में कोई नमाज़ जायज़ नहीं: न फ़र्ज़, न वाजिब, न नफ़्ल, न क़ज़ा, न सजदा-ए-तिलावत।",
                "ان تینوں وقتوں میں کوئی نماز جائز نہیں: نہ فرض، نہ واجب، نہ نفل، نہ قضا، نہ سجدۂ تلاوت۔"
            ),
            t(
                "From true dawn until the Fard of Fajr, no Nafl is prayed except the two Sunnah of Fajr.",
                "Subh-e-sadiq se Fajr ke Farz tak Fajr ki do Sunnaton ke siwa koi Nafl nahi.",
                "सुबह-ए-सादिक़ से फ़ज्र के फ़र्ज़ तक फ़ज्र की दो सुन्नतों के सिवा कोई नफ़्ल नहीं।",
                "صبحِ صادق سے فجر کے فرض تک فجر کی دو سنتوں کے سوا کوئی نفل نہیں۔"
            ),
            t(
                "After the Fard of Fajr until sunrise, and after the Fard of Asr until sunset, Nafl is not prayed; Qaza and Sajdah Tilawat are allowed.",
                "Fajr ke Farz ke baad tuloo tak, aur Asr ke Farz ke baad ghuroob tak Nafl nahi; Qaza aur Sajda-e-Tilawat jaiz hain.",
                "फ़ज्र के फ़र्ज़ के बाद तुलू तक, और अस्र के फ़र्ज़ के बाद ग़ुरूब तक नफ़्ल नहीं; क़ज़ा और सजदा-ए-तिलावत जायज़ हैं।",
                "فجر کے فرض کے بعد طلوع تک، اور عصر کے فرض کے بعد غروب تک نفل نہیں؛ قضا اور سجدۂ تلاوت جائز ہیں۔"
            )
        ),
        "Sahih Muslim 831 · Sahih al-Bukhari 586 · Bahar-e-Shariat, Hissa 3",
        schoolNotes = mapOf(
            Madhhab.SHAFII to t(
                "Prayers that have a cause, such as Qaza, Tahiyyat-ul-Masjid and Janazah, may be prayed in these times.",
                "Sabab wali namazein, jaise Qaza, Tahiyyatul Masjid aur Janaza, in waqton mein padhi ja sakti hain.",
                "सबब वाली नमाज़ें, जैसे क़ज़ा, तहिय्यतुल मस्जिद और जनाज़ा, इन वक़्तों में पढ़ी जा सकती हैं।",
                "سبب والی نمازیں، جیسے قضا، تحیۃ المسجد اور جنازہ، ان وقتوں میں پڑھی جا سکتی ہیں۔"
            )
        ),
        numbered = false
    ),
    SalahTopic(
        "wudu",
        t("Wudu", "Wuzu", "वुज़ू", "وضو"),
        null,
        t("Purification before Salah", "Namaz se pehle ki paaki", "नमाज़ से पहले की पाकी", "نماز سے پہلے کی پاکی"),
        listOf(
            t(
                "Make the intention to purify yourself and say Bismillah.",
                "Paaki ki niyyat karein aur Bismillah padhein.",
                "पाकी की नीयत करें और बिस्मिल्लाह पढ़ें।",
                "پاکی کی نیت کریں اور بسم اللہ پڑھیں۔"
            ),
            t(
                "Wash both hands up to the wrists, three times.",
                "Dono haath gatton tak teen baar dhoyein.",
                "दोनों हाथ गट्टों तक तीन बार धोएँ।",
                "دونوں ہاتھ گٹوں تک تین بار دھوئیں۔"
            ),
            t(
                "Rinse the mouth three times, using a miswak, and clean the nose three times.",
                "Teen baar kulli karein (miswak ke saath) aur teen baar naak mein paani chadha kar saaf karein.",
                "तीन बार कुल्ली करें (मिस्वाक के साथ) और तीन बार नाक में पानी चढ़ाकर साफ़ करें।",
                "تین بار کلی کریں (مسواک کے ساتھ) اور تین بار ناک میں پانی چڑھا کر صاف کریں۔"
            ),
            t(
                "Wash the whole face, from hairline to chin and ear to ear, three times.",
                "Poora chehra teen baar dhoyein: peshani ke baalon se thodi ke neeche tak aur ek kaan ki lau se doosre kaan ki lau tak.",
                "पूरा चेहरा तीन बार धोएँ: पेशानी के बालों से ठोड़ी के नीचे तक और एक कान की लौ से दूसरे कान की लौ तक।",
                "پورا چہرہ تین بار دھوئیں: پیشانی کے بالوں سے ٹھوڑی کے نیچے تک اور ایک کان کی لو سے دوسرے کان کی لو تک۔"
            ),
            t(
                "Wash each arm including the elbow, right then left, three times.",
                "Dono haath kohniyon samet teen teen baar dhoyein, pehle daayan phir baayan.",
                "दोनों हाथ कोहनियों समेत तीन-तीन बार धोएँ, पहले दायाँ फिर बायाँ।",
                "دونوں ہاتھ کہنیوں سمیت تین تین بار دھوئیں، پہلے دایاں پھر بایاں۔"
            ),
            t(
                "Wipe the whole head once with wet hands, then the ears. The extent that is required varies by school.",
                "Geele haathon se poore sar ka ek baar masah karein, phir kaanon ka. Kitna masah farz hai, isme mazahib ka farq hai.",
                "गीले हाथों से पूरे सर का एक बार मसह करें, फिर कानों का। कितना मसह फ़र्ज़ है, इसमें मज़ाहिब का फ़र्क़ है।",
                "گیلے ہاتھوں سے پورے سر کا ایک بار مسح کریں، پھر کانوں کا۔ کتنا مسح فرض ہے، اس میں مذاہب کا فرق ہے۔"
            ),
            t(
                "Wash both feet including the ankles, right then left, three times.",
                "Dono paon takhnon samet teen teen baar dhoyein, pehle daayan phir baayan.",
                "दोनों पाँव टख़नों समेत तीन-तीन बार धोएँ, पहले दायाँ फिर बायाँ।",
                "دونوں پاؤں ٹخنوں سمیت تین تین بار دھوئیں، پہلے دایاں پھر بایاں۔"
            )
        ),
        "Quran 5:6 · Sahih al-Bukhari 159",
        schoolNotes = mapOf(
            Madhhab.HANAFI to t(
                "Four acts are Fard: washing the face, washing both arms with the elbows, wiping a quarter of the head, and washing both feet with the ankles. The rest is Sunnah.",
                "Chaar cheezein Farz hain: chehra dhona, kohniyon samet dono haath dhona, chauthai sar ka masah, aur takhnon samet dono paon dhona. Baaqi Sunnat hai.",
                "चार चीज़ें फ़र्ज़ हैं: चेहरा धोना, कोहनियों समेत दोनों हाथ धोना, चौथाई सर का मसह, और टख़नों समेत दोनों पाँव धोना। बाक़ी सुन्नत है।",
                "چار چیزیں فرض ہیں: چہرہ دھونا، کہنیوں سمیت دونوں ہاتھ دھونا، چوتھائی سر کا مسح، اور ٹخنوں سمیت دونوں پاؤں دھونا۔ باقی سنت ہے۔"
            ),
            Madhhab.SHAFII to t(
                "Wiping part of the head suffices; the intention and the order of the limbs are also Fard.",
                "Sar ke kuch hisse ka masah kaafi hai; niyyat aur tarteeb bhi Farz hain.",
                "सर के कुछ हिस्से का मसह काफ़ी है; नीयत और तरतीब भी फ़र्ज़ हैं।",
                "سر کے کچھ حصے کا مسح کافی ہے؛ نیت اور ترتیب بھی فرض ہیں۔"
            ),
            Madhhab.MALIKI to t(
                "Wiping the entire head is Fard, as are the intention, rubbing the limbs and washing without a break.",
                "Poore sar ka masah Farz hai; niyyat, aaza ko malna aur lagataar dhona bhi Farz hain.",
                "पूरे सर का मसह फ़र्ज़ है; नीयत, आज़ा को मलना और लगातार धोना भी फ़र्ज़ हैं।",
                "پورے سر کا مسح فرض ہے؛ نیت، اعضا کو ملنا اور لگاتار دھونا بھی فرض ہیں۔"
            ),
            Madhhab.HANBALI to t(
                "Wiping the entire head with the ears is Fard, as are rinsing the mouth and nose, the order and washing without a break.",
                "Kaanon samet poore sar ka masah Farz hai; kulli, naak mein paani, tarteeb aur lagataar dhona bhi Farz hain.",
                "कानों समेत पूरे सर का मसह फ़र्ज़ है; कुल्ली, नाक में पानी, तरतीब और लगातार धोना भी फ़र्ज़ हैं।",
                "کانوں سمیت پورے سر کا مسح فرض ہے؛ کلی، ناک میں پانی، ترتیب اور لگاتار دھونا بھی فرض ہیں۔"
            )
        )
    ),
    SalahTopic(
        "wudu_breakers",
        t("What breaks wudu", "Wuzu todne wali cheezein", "वुज़ू तोड़ने वाली चीज़ें", "وضو توڑنے والی چیزیں"),
        null,
        t(
            "After any of these, make a fresh wudu before praying",
            "In ke baad namaz se pehle naya wuzu karein",
            "इनके बाद नमाज़ से पहले नया वुज़ू करें",
            "ان کے بعد نماز سے پہلے نیا وضو کریں"
        ),
        listOf(
            t(
                "Anything leaving the front or back passage: urine, stool or wind.",
                "Aage ya peeche ke raaste se kuch nikalna: peshab, pakhana ya reeh.",
                "आगे या पीछे के रास्ते से कुछ निकलना: पेशाब, पाख़ाना या रीह।",
                "آگے یا پیچھے کے راستے سے کچھ نکلنا: پیشاب، پاخانہ یا ریح۔"
            ),
            t(
                "Blood or pus flowing from the body to a place that must be washed in wudu or ghusl.",
                "Khoon ya peep ka nikal kar aisi jagah beh jana jise wuzu ya ghusl mein dhona zaroori hai.",
                "ख़ून या पीप का निकलकर ऐसी जगह बह जाना जिसे वुज़ू या ग़ुस्ल में धोना ज़रूरी है।",
                "خون یا پیپ کا نکل کر ایسی جگہ بہہ جانا جسے وضو یا غسل میں دھونا ضروری ہے۔"
            ),
            t("A mouthful of vomit.", "Munh bhar qai.", "मुँह भर क़ै।", "منہ بھر قے۔"),
            t(
                "Sleeping while lying down or leaning on something.",
                "Let kar ya tek laga kar so jana.",
                "लेटकर या टेक लगाकर सो जाना।",
                "لیٹ کر یا ٹیک لگا کر سو جانا۔"
            ),
            t(
                "Fainting, insanity or intoxication.",
                "Behoshi, junoon ya nasha.",
                "बेहोशी, जुनून या नशा।",
                "بے ہوشی، جنون یا نشہ۔"
            ),
            t(
                "An adult laughing aloud in a prayer that has ruku‘ and sajdah.",
                "Baaligh ka ruku-sajde wali namaz mein qahqaha lagana.",
                "बालिग़ का रुकू-सजदे वाली नमाज़ में क़हक़हा लगाना।",
                "بالغ کا رکوع سجدے والی نماز میں قہقہہ لگانا۔"
            )
        ),
        "Quran 5:6 · Sunan Abi Dawud 203 · Bahar-e-Shariat, Hissa 2",
        schoolNotes = mapOf(
            Madhhab.SHAFII to t(
                "Flowing blood and vomit do not break wudu; touching the private parts with the palm, or the skin of a non-mahram of the opposite sex, does.",
                "Behta khoon aur qai wuzu nahi todte; hatheli se sharmgah chhoona ya na-mahram ki jild chhoona wuzu tod deta hai.",
                "बहता ख़ून और क़ै वुज़ू नहीं तोड़ते; हथेली से शर्मगाह छूना या ना-महरम की जिल्द छूना वुज़ू तोड़ देता है।",
                "بہتا خون اور قے وضو نہیں توڑتے؛ ہتھیلی سے شرمگاہ چھونا یا نامحرم کی جلد چھونا وضو توڑ دیتا ہے۔"
            )
        ),
        numbered = false
    ),
    SalahTopic(
        "ghusl",
        t("Ghusl", "Ghusl", "ग़ुस्ल", "غسل"),
        null,
        t(
            "The bath that removes major impurity",
            "Badi napaki door karne wala ghusl",
            "बड़ी नापाकी दूर करने वाला ग़ुस्ल",
            "بڑی ناپاکی دور کرنے والا غسل"
        ),
        listOf(
            t(
                "Ghusl becomes Fard after janabah, and when menstruation or post-natal bleeding ends.",
                "Ghusl janabat ke baad, aur haiz ya nifas khatm hone par Farz hota hai.",
                "ग़ुस्ल जनाबत के बाद, और हैज़ या निफ़ास ख़त्म होने पर फ़र्ज़ होता है।",
                "غسل جنابت کے بعد، اور حیض یا نفاس ختم ہونے پر فرض ہوتا ہے۔"
            ),
            t(
                "Make the intention, wash both hands to the wrists, then wash the private parts and any impurity on the body.",
                "Niyyat karein, dono haath gatton tak dhoyein, phir sharmgah aur badan par lagi najasat dhoyein.",
                "नीयत करें, दोनों हाथ गट्टों तक धोएँ, फिर शर्मगाह और बदन पर लगी नजासत धोएँ।",
                "نیت کریں، دونوں ہاتھ گٹوں تک دھوئیں، پھر شرمگاہ اور بدن پر لگی نجاست دھوئیں۔"
            ),
            t("Perform a complete wudu.", "Poora wuzu karein.", "पूरा वुज़ू करें।", "پورا وضو کریں۔"),
            t(
                "Pour water over the head three times, then over the right shoulder three times, then the left, and rub the whole body.",
                "Teen baar sar par paani bahayein, phir teen baar daayein kandhe par, phir baayein par, aur poore badan ko malein.",
                "तीन बार सर पर पानी बहाएँ, फिर तीन बार दाएँ कंधे पर, फिर बाएँ पर, और पूरे बदन को मलें।",
                "تین بار سر پر پانی بہائیں، پھر تین بار دائیں کندھے پر، پھر بائیں پر، اور پورے بدن کو ملیں۔"
            )
        ),
        "Quran 5:6 · Sahih al-Bukhari 248 · Bahar-e-Shariat, Hissa 2",
        schoolNotes = mapOf(
            Madhhab.HANAFI to t(
                "Three acts are Fard: rinsing the whole mouth, rinsing the nose up to the soft part, and water reaching every part of the body so that not a hair's breadth stays dry.",
                "Teen cheezein Farz hain: poore munh ki kulli (gharara), naak ki narm haddi tak paani chadhana, aur poore badan par is tarah paani bahana ke baal barabar jagah bhi sookhi na rahe.",
                "तीन चीज़ें फ़र्ज़ हैं: पूरे मुँह की कुल्ली (ग़रारा), नाक की नर्म हड्डी तक पानी चढ़ाना, और पूरे बदन पर इस तरह पानी बहाना कि बाल बराबर जगह भी सूखी न रहे।",
                "تین چیزیں فرض ہیں: پورے منہ کی کلی (غرارہ)، ناک کی نرم ہڈی تک پانی چڑھانا، اور پورے بدن پر اس طرح پانی بہانا کہ بال برابر جگہ بھی سوکھی نہ رہے۔"
            ),
            Madhhab.SHAFII to t(
                "The intention and water reaching the whole body are Fard; rinsing the mouth and nose is Sunnah.",
                "Niyyat aur poore badan par paani pahunchana Farz hai; kulli aur naak mein paani Sunnat hai.",
                "नीयत और पूरे बदन पर पानी पहुँचाना फ़र्ज़ है; कुल्ली और नाक में पानी सुन्नत है।",
                "نیت اور پورے بدن پر پانی پہنچانا فرض ہے؛ کلی اور ناک میں پانی سنت ہے۔"
            )
        )
    ),
    SalahTopic(
        "tayammum",
        t("Tayammum", "Tayammum", "तयम्मुम", "تیمم"),
        null,
        t(
            "Dry purification when water cannot be used",
            "Paani na mile ya nuqsan de to paaki ka tareeqa",
            "पानी न मिले या नुक़सान दे तो पाकी का तरीक़ा",
            "پانی نہ ملے یا نقصان دے تو پاکی کا طریقہ"
        ),
        listOf(
            t(
                "It is allowed when water is not available within about a mile, or when using it would cause or worsen an illness.",
                "Yeh us waqt jaiz hai jab taqriban ek meel tak paani na ho, ya paani ke istemal se beemari hone ya badhne ka andesha ho.",
                "यह उस वक़्त जायज़ है जब तक़रीबन एक मील तक पानी न हो, या पानी के इस्तेमाल से बीमारी होने या बढ़ने का अंदेशा हो।",
                "یہ اس وقت جائز ہے جب تقریباً ایک میل تک پانی نہ ہو، یا پانی کے استعمال سے بیماری ہونے یا بڑھنے کا اندیشہ ہو۔"
            ),
            t(
                "Make the intention of purification for the prayer.",
                "Namaz ke liye paaki haasil karne ki niyyat karein.",
                "नमाज़ के लिए पाकी हासिल करने की नीयत करें।",
                "نماز کے لیے پاکی حاصل کرنے کی نیت کریں۔"
            ),
            t(
                "Strike both palms on clean earth such as soil, sand or stone, shake off the excess and wipe the whole face.",
                "Dono hatheliyan paak mitti, ret ya patthar par maarein, zyada gard jhaad dein aur poore chehre par pherein.",
                "दोनों हथेलियाँ पाक मिट्टी, रेत या पत्थर पर मारें, ज़्यादा गर्द झाड़ दें और पूरे चेहरे पर फेरें।",
                "دونوں ہتھیلیاں پاک مٹی، ریت یا پتھر پر ماریں، زیادہ گرد جھاڑ دیں اور پورے چہرے پر پھیریں۔"
            ),
            t(
                "Strike again and wipe each arm from the fingertips up to and including the elbow.",
                "Dobara maarein aur dono haathon par ungliyon se kohniyon samet pherein.",
                "दोबारा मारें और दोनों हाथों पर उँगलियों से कोहनियों समेत फेरें।",
                "دوبارہ ماریں اور دونوں ہاتھوں پر انگلیوں سے کہنیوں سمیت پھیریں۔"
            ),
            t(
                "It is broken by whatever breaks wudu, and by becoming able to use water.",
                "Jo cheezein wuzu todti hain unse, aur paani par qudrat ho jane se tayammum toot jata hai.",
                "जो चीज़ें वुज़ू तोड़ती हैं उनसे, और पानी पर क़ुदरत हो जाने से तयम्मुम टूट जाता है।",
                "جو چیزیں وضو توڑتی ہیں ان سے، اور پانی پر قدرت ہو جانے سے تیمم ٹوٹ جاتا ہے۔"
            )
        ),
        "Quran 4:43, 5:6 · Sahih al-Bukhari 335 · Bahar-e-Shariat, Hissa 2",
        schoolNotes = mapOf(
            Madhhab.SHAFII to t(
                "Only earth that has dust may be used; two strikes, with the arms wiped to the elbows.",
                "Sirf gard wali mitti se jaiz hai; do zarbein, haathon ka masah kohniyon tak.",
                "सिर्फ़ गर्द वाली मिट्टी से जायज़ है; दो ज़र्बें, हाथों का मसह कोहनियों तक।",
                "صرف گرد والی مٹی سے جائز ہے؛ دو ضربیں، ہاتھوں کا مسح کہنیوں تک۔"
            ),
            Madhhab.HANBALI to t(
                "One strike is used, wiping the face and then the hands to the wrists.",
                "Ek hi zarb se chehre aur phir gatton tak haathon ka masah kiya jata hai.",
                "एक ही ज़र्ब से चेहरे और फिर गट्टों तक हाथों का मसह किया जाता है।",
                "ایک ہی ضرب سے چہرے اور پھر گٹوں تک ہاتھوں کا مسح کیا جاتا ہے۔"
            )
        )
    ),
    SalahTopic(
        "women",
        t("Differences for women", "Auraton ki namaz ka farq", "औरतों की नमाज़ का फ़र्क़", "عورتوں کی نماز کا فرق"),
        null,
        t(
            "Hanafi method — the aim is greater concealment",
            "Hanafi tareeqa — maqsad zyada parda hai",
            "हनफ़ी तरीक़ा — मक़सद ज़्यादा पर्दा है",
            "حنفی طریقہ — مقصد زیادہ پردہ ہے"
        ),
        listOf(
            t(
                "Raise the hands only to the shoulders for the takbir, keeping them inside the shawl.",
                "Takbeer ke waqt haath sirf kandhon tak uthayein, chadar ke andar hi.",
                "तकबीर के वक़्त हाथ सिर्फ़ कंधों तक उठाएँ, चादर के अंदर ही।",
                "تکبیر کے وقت ہاتھ صرف کندھوں تک اٹھائیں، چادر کے اندر ہی۔"
            ),
            t(
                "Place the right palm over the left hand on the chest.",
                "Seene par baayein haath ke upar daayin hatheli rakhein.",
                "सीने पर बाएँ हाथ के ऊपर दाईं हथेली रखें।",
                "سینے پر بائیں ہاتھ کے اوپر دائیں ہتھیلی رکھیں۔"
            ),
            t(
                "In ruku‘ bend only a little, with the fingers together on the knees and the arms close to the body.",
                "Ruku mein thoda sa jhukein, ungliyan mili hui ghutnon par hon aur baazu jism se mile hon.",
                "रुकू में थोड़ा सा झुकें, उँगलियाँ मिली हुई घुटनों पर हों और बाज़ू जिस्म से मिले हों।",
                "رکوع میں تھوڑا سا جھکیں، انگلیاں ملی ہوئی گھٹنوں پر ہوں اور بازو جسم سے ملے ہوں۔"
            ),
            t(
                "In sajdah stay drawn together: the belly on the thighs and the arms on the ground, close to the sides.",
                "Sajde mein simat kar rahein: pet raanon se laga ho aur baazu zameen par, karwaton se mile hue.",
                "सजदे में सिमटकर रहें: पेट रानों से लगा हो और बाज़ू ज़मीन पर, करवटों से मिले हुए।",
                "سجدے میں سمٹ کر رہیں: پیٹ رانوں سے لگا ہو اور بازو زمین پر، کروٹوں سے ملے ہوئے۔"
            ),
            t(
                "In both sittings sit on the left hip with both feet out to the right.",
                "Dono qa'don mein dono paon daayin taraf nikaal kar baayein sureen par baithein.",
                "दोनों क़ादों में दोनों पाँव दाईं तरफ़ निकालकर बाएँ सुरीन पर बैठें।",
                "دونوں قعدوں میں دونوں پاؤں دائیں طرف نکال کر بائیں سرین پر بیٹھیں۔"
            ),
            t(
                "Recite quietly in every prayer. Women do not call the adhan or the iqamah.",
                "Har namaz mein aahista padhein. Auratein azaan aur iqamat nahi kehtin.",
                "हर नमाज़ में आहिस्ता पढ़ें। औरतें अज़ान और इक़ामत नहीं कहतीं।",
                "ہر نماز میں آہستہ پڑھیں۔ عورتیں اذان اور اقامت نہیں کہتیں۔"
            ),
            t(
                "During menstruation and post-natal bleeding the prayer is not offered, and it is not made up afterwards.",
                "Haiz aur nifas ke dinon mein namaz nahi padhi jati, aur baad mein uski qaza bhi nahi.",
                "हैज़ और निफ़ास के दिनों में नमाज़ नहीं पढ़ी जाती, और बाद में उसकी क़ज़ा भी नहीं।",
                "حیض اور نفاس کے دنوں میں نماز نہیں پڑھی جاتی، اور بعد میں اس کی قضا بھی نہیں۔"
            )
        ),
        "Sahih al-Bukhari 321 · Sahih Muslim 335 · Bahar-e-Shariat, Hissa 3",
        numbered = false
    ),
    SalahTopic(
        "jamaat",
        t("Jamaat", "Jamaat", "जमाअत", "جماعت"),
        null,
        t("Praying in congregation", "Jamaat ke saath namaz", "जमाअत के साथ नमाज़", "جماعت کے ساتھ نماز"),
        listOf(
            t(
                "Arrive with wudu and join the rows without rushing.",
                "Ba-wuzu aayein aur sukoon se, bina daude, saf mein shamil hon.",
                "बा-वुज़ू आएँ और सुकून से, बिना दौड़े, सफ़ में शामिल हों।",
                "باوضو آئیں اور سکون سے، بغیر دوڑے، صف میں شامل ہوں۔"
            ),
            t(
                "Straighten the rows, stand shoulder to shoulder and fill the front row first.",
                "Safein seedhi karein, kandhe se kandha mila kar khade hon aur pehle agli saf poori karein.",
                "सफ़ें सीधी करें, कंधे से कंधा मिलाकर खड़े हों और पहले अगली सफ़ पूरी करें।",
                "صفیں سیدھی کریں، کندھے سے کندھا ملا کر کھڑے ہوں اور پہلے اگلی صف پوری کریں۔"
            ),
            t(
                "Make your intention and follow the imam's movements; do not precede the imam.",
                "Niyyat karein aur imam ki pairwi karein; kisi rukn mein imam se aage na badhein.",
                "नीयत करें और इमाम की पैरवी करें; किसी रुक्न में इमाम से आगे न बढ़ें।",
                "نیت کریں اور امام کی پیروی کریں؛ کسی رکن میں امام سے آگے نہ بڑھیں۔"
            ),
            t(
                "If joining late, complete the missed rak‘at after the imam's final salam.",
                "Der se shamil hon to chhooti hui rakatein imam ke salam ke baad poori karein.",
                "देर से शामिल हों तो छूटी हुई रकअतें इमाम के सलाम के बाद पूरी करें।",
                "دیر سے شامل ہوں تو چھوٹی ہوئی رکعتیں امام کے سلام کے بعد پوری کریں۔"
            ),
            t(
                "Prayer in congregation is twenty-seven times more rewarded than praying alone.",
                "Jamaat ki namaz akele ki namaz se sattaees darje afzal hai.",
                "जमाअत की नमाज़ अकेले की नमाज़ से सत्ताईस दर्जे अफ़ज़ल है।",
                "جماعت کی نماز اکیلے کی نماز سے ستائیس درجے افضل ہے۔"
            )
        ),
        "Sahih al-Bukhari 645, 636, 723",
        schoolNotes = mapOf(
            Madhhab.HANAFI to t(
                "Congregation is Wajib for men who are able. The follower recites Thana, the tasbihat, Tashahhud, Durood and dua, but stays silent during the imam's recitation.",
                "Qudrat rakhne wale mardon par jamaat Wajib hai. Muqtadi Sana, tasbeehat, At-Tahiyyat, Durood aur dua padhta hai, magar imam ki qira'at ke waqt khamosh rehta hai.",
                "क़ुदरत रखने वाले मर्दों पर जमाअत वाजिब है। मुक़्तदी सना, तस्बीहात, अत्तहिय्यात, दुरूद और दुआ पढ़ता है, मगर इमाम की क़िराअत के वक़्त ख़ामोश रहता है।",
                "قدرت رکھنے والے مردوں پر جماعت واجب ہے۔ مقتدی ثنا، تسبیحات، التحیات، درود اور دعا پڑھتا ہے، مگر امام کی قراءت کے وقت خاموش رہتا ہے۔"
            ),
            Madhhab.SHAFII to t(
                "The follower recites al-Fatihah in every rak‘ah, even behind the imam.",
                "Muqtadi har rakat mein, imam ke peeche bhi, Surah Fatiha padhta hai.",
                "मुक़्तदी हर रकअत में, इमाम के पीछे भी, सूरह फ़ातिहा पढ़ता है।",
                "مقتدی ہر رکعت میں، امام کے پیچھے بھی، سورۃ فاتحہ پڑھتا ہے۔"
            )
        )
    ),
    SalahTopic(
        "masbuq",
        t("Joining late (Masbuq)", "Der se shamil hona (Masbooq)", "देर से शामिल होना (मस्बूक़)", "دیر سے شامل ہونا (مسبوق)"),
        null,
        t(
            "Completing the rak‘at you missed",
            "Chhooti hui rakatein poori karna",
            "छूटी हुई रकअतें पूरी करना",
            "چھوٹی ہوئی رکعتیں پوری کرنا"
        ),
        listOf(
            t(
                "Say the takbir while standing, then join the imam in whatever position he is in.",
                "Khade hokar takbeer kahein, phir imam jis haalat mein ho usi mein shamil ho jayein.",
                "खड़े होकर तकबीर कहें, फिर इमाम जिस हालत में हो उसी में शामिल हो जाएँ।",
                "کھڑے ہو کر تکبیر کہیں، پھر امام جس حالت میں ہو اسی میں شامل ہو جائیں۔"
            ),
            t(
                "If you reach the imam in ruku‘, that rak‘ah counts; if you join after the ruku‘, it does not.",
                "Agar imam ko ruku mein paa lein to woh rakat mil gayi; ruku ke baad shamil hue to woh rakat nahi mili.",
                "अगर इमाम को रुकू में पा लें तो वह रकअत मिल गई; रुकू के बाद शामिल हुए तो वह रकअत नहीं मिली।",
                "اگر امام کو رکوع میں پا لیں تو وہ رکعت مل گئی؛ رکوع کے بعد شامل ہوئے تو وہ رکعت نہیں ملی۔"
            ),
            t(
                "In the imam's final sitting recite only the Tashahhud, slowly.",
                "Imam ke aakhri qa'de mein sirf At-Tahiyyat thehar thehar kar padhein.",
                "इमाम के आख़िरी क़ादे में सिर्फ़ अत्तहिय्यात ठहर-ठहरकर पढ़ें।",
                "امام کے آخری قعدے میں صرف التحیات ٹھہر ٹھہر کر پڑھیں۔"
            ),
            t(
                "When the imam has given his salam, stand up without giving salam and complete what you missed.",
                "Jab imam salam pher le to salam phere baghair khade ho jayein aur chhooti hui rakatein poori karein.",
                "जब इमाम सलाम फेर ले तो सलाम फेरे बग़ैर खड़े हो जाएँ और छूटी हुई रकअतें पूरी करें।",
                "جب امام سلام پھیر لے تو سلام پھیرے بغیر کھڑے ہو جائیں اور چھوٹی ہوئی رکعتیں پوری کریں۔"
            )
        ),
        "Sahih al-Bukhari 636 · Sahih Muslim 602 · Bahar-e-Shariat, Hissa 3",
        schoolNotes = mapOf(
            Madhhab.HANAFI to t(
                "For recitation, the rak‘at you make up are your first ones: recite Thana, al-Fatihah and a surah in them.",
                "Qira'at ke lihaz se chhooti hui rakatein aap ki pehli rakatein hain: un mein Sana, Fatiha aur surat padhein.",
                "क़िराअत के लिहाज़ से छूटी हुई रकअतें आपकी पहली रकअतें हैं: उनमें सना, फ़ातिहा और सूरत पढ़ें।",
                "قراءت کے لحاظ سے چھوٹی ہوئی رکعتیں آپ کی پہلی رکعتیں ہیں: ان میں ثنا، فاتحہ اور سورت پڑھیں۔"
            ),
            Madhhab.SHAFII to t(
                "What you prayed with the imam is the beginning of your prayer; complete the rest in order after it.",
                "Jo rakatein imam ke saath padhin woh aap ki namaz ka shuru hain; baaqi usi tarteeb se poori karein.",
                "जो रकअतें इमाम के साथ पढ़ीं वे आपकी नमाज़ का शुरू हैं; बाक़ी उसी तरतीब से पूरी करें।",
                "جو رکعتیں امام کے ساتھ پڑھیں وہ آپ کی نماز کا شروع ہیں؛ باقی اسی ترتیب سے پوری کریں۔"
            )
        )
    ),
    SalahTopic(
        "sahw",
        t("Sajdah Sahw", "Sajda-e-Sahw", "सजदा-ए-सह्व", "سجدۂ سہو"),
        null,
        t(
            "Two prostrations that repair a mistake",
            "Bhool ki talaafi ke do sajde",
            "भूल की तलाफ़ी के दो सजदे",
            "بھول کی تلافی کے دو سجدے"
        ),
        listOf(
            t(
                "Identify what was omitted or added; an omitted pillar cannot simply be fixed by Sajdah Sahw.",
                "Dekhein kya chhoota ya badh gaya; chhoota hua Farz sirf Sajda-e-Sahw se poora nahi hota.",
                "देखें क्या छूटा या बढ़ गया; छूटा हुआ फ़र्ज़ सिर्फ़ सजदा-ए-सह्व से पूरा नहीं होता।",
                "دیکھیں کیا چھوٹا یا بڑھ گیا؛ چھوٹا ہوا فرض صرف سجدۂ سہو سے پورا نہیں ہوتا۔"
            ),
            t(
                "For a qualifying mistake, perform two prostrations of forgetfulness at the close of the prayer.",
                "Sahw wali ghalti par namaz ke aakhir mein bhool ke do sajde karein.",
                "सह्व वाली ग़लती पर नमाज़ के आख़िर में भूल के दो सजदे करें।",
                "سہو والی غلطی پر نماز کے آخر میں بھول کے دو سجدے کریں۔"
            ),
            t(
                "The timing and what counts as a qualifying mistake depend on your school; check its detailed ruling.",
                "Sajde ka waqt aur kaun si ghalti par sajda hai, yeh mazhab ke mutabiq hai; tafseeli hukm dekh lein.",
                "सजदे का वक़्त और कौन सी ग़लती पर सजदा है, यह मज़हब के मुताबिक़ है; तफ़सीली हुक्म देख लें।",
                "سجدے کا وقت اور کون سی غلطی پر سجدہ ہے، یہ مذہب کے مطابق ہے؛ تفصیلی حکم دیکھ لیں۔"
            )
        ),
        "Sahih al-Bukhari 1224, 1226 · Sunan Abi Dawud 1038",
        schoolNotes = mapOf(
            Madhhab.HANAFI to t(
                "It is Wajib when a Wajib is missed, delayed or repeated by mistake — for example forgetting the first sitting or the Qunut. In the final sitting recite the Tashahhud, give one salam to the right, make two sajdahs, then sit and recite the Tashahhud, Durood and dua again and end with both salams.",
                "Bhool se koi Wajib chhoot jaye, uski jagah se der ho ya dobara ho jaye to yeh Wajib hai — jaise pehla qa'da ya Qunoot bhool jana. Aakhri qa'de mein At-Tahiyyat padh kar daayin taraf ek salam pherein, do sajde karein, phir baith kar dobara At-Tahiyyat, Durood aur dua padhein aur dono salam pher dein.",
                "भूल से कोई वाजिब छूट जाए, उसकी जगह से देर हो या दोबारा हो जाए तो यह वाजिब है — जैसे पहला क़ादा या क़ुनूत भूल जाना। आख़िरी क़ादे में अत्तहिय्यात पढ़कर दाईं तरफ़ एक सलाम फेरें, दो सजदे करें, फिर बैठकर दोबारा अत्तहिय्यात, दुरूद और दुआ पढ़ें और दोनों सलाम फेर दें।",
                "بھول سے کوئی واجب چھوٹ جائے، اس کی جگہ سے دیر ہو یا دوبارہ ہو جائے تو یہ واجب ہے — جیسے پہلا قعدہ یا قنوت بھول جانا۔ آخری قعدے میں التحیات پڑھ کر دائیں طرف ایک سلام پھیریں، دو سجدے کریں، پھر بیٹھ کر دوبارہ التحیات، درود اور دعا پڑھیں اور دونوں سلام پھیر دیں۔"
            ),
            Madhhab.SHAFII to t(
                "Normally two prostrations before the closing salam.",
                "Aam taur par salam se pehle do sajde.",
                "आम तौर पर सलाम से पहले दो सजदे।",
                "عام طور پر سلام سے پہلے دو سجدے۔"
            ),
            Madhhab.MALIKI to t(
                "Before the salam for an omission, and after the salam for an addition.",
                "Kami ho to salam se pehle, zyadti ho to salam ke baad.",
                "कमी हो तो सलाम से पहले, ज़्यादती हो तो सलाम के बाद।",
                "کمی ہو تو سلام سے پہلے، زیادتی ہو تو سلام کے بعد۔"
            ),
            Madhhab.HANBALI to t(
                "The timing depends on the error; follow the applicable ruling.",
                "Waqt ghalti ki nau'iyat par hai; mutalliqa hukm par amal karein.",
                "वक़्त ग़लती की नौइयत पर है; मुतअल्लिक़ा हुक्म पर अमल करें।",
                "وقت غلطی کی نوعیت پر ہے؛ متعلقہ حکم پر عمل کریں۔"
            )
        )
    ),
    SalahTopic(
        "qaza",
        t("Qaza Salah", "Qaza namaz", "क़ज़ा नमाज़", "قضا نماز"),
        SalahKind.FARD,
        t(
            "Making up missed obligatory prayers",
            "Chhooti hui Farz namazein ada karna",
            "छूटी हुई फ़र्ज़ नमाज़ें अदा करना",
            "چھوٹی ہوئی فرض نمازیں ادا کرنا"
        ),
        listOf(
            t(
                "Pray a missed Fard as soon as you reasonably can.",
                "Chhooti hui Farz namaz jitni jaldi ho sake padh lein.",
                "छूटी हुई फ़र्ज़ नमाज़ जितनी जल्दी हो सके पढ़ लें।",
                "چھوٹی ہوئی فرض نماز جتنی جلدی ہو سکے پڑھ لیں۔"
            ),
            t(
                "Keep a careful count of missed prayers and make a manageable plan.",
                "Qaza namazon ka theek hisab rakhein aur unhe ada karne ka aasaan nizam banayein.",
                "क़ज़ा नमाज़ों का ठीक हिसाब रखें और उन्हें अदा करने का आसान निज़ाम बनाएँ।",
                "قضا نمازوں کا ٹھیک حساب رکھیں اور انہیں ادا کرنے کا آسان نظام بنائیں۔"
            ),
            t(
                "Intend the particular missed prayer; ask a qualified scholar about ordering, travel and special cases.",
                "Jis namaz ki qaza hai usi ki niyyat karein; tarteeb, safar aur khaas suraton ke baare mein kisi mo'tabar aalim se poochhein.",
                "जिस नमाज़ की क़ज़ा है उसी की नीयत करें; तरतीब, सफ़र और ख़ास सूरतों के बारे में किसी मोतबर आलिम से पूछें।",
                "جس نماز کی قضا ہے اسی کی نیت کریں؛ ترتیب، سفر اور خاص صورتوں کے بارے میں کسی معتبر عالم سے پوچھیں۔"
            ),
            t(
                "Along with making them up, repent sincerely for having missed them.",
                "Qaza ke saath saath namaz chhodne par sachchi tauba bhi karein.",
                "क़ज़ा के साथ-साथ नमाज़ छोड़ने पर सच्ची तौबा भी करें।",
                "قضا کے ساتھ ساتھ نماز چھوڑنے پر سچی توبہ بھی کریں۔"
            )
        ),
        "Sahih al-Bukhari 597 · Sahih Muslim 684",
        schoolNotes = mapOf(
            Madhhab.HANAFI to t(
                "Fard and Witr are made up; Sunnah prayers are not, except the Sunnah of Fajr when made up with its Fard before midday. Qaza is prayed at any time except the three forbidden times.",
                "Farz aur Witr ki qaza hai; Sunnaton ki nahi, siwaye Fajr ki Sunnat ke jab Farz ke saath zawal se pehle qaza ki jaye. Qaza teen mamnoo waqton ke siwa har waqt padhi ja sakti hai.",
                "फ़र्ज़ और वित्र की क़ज़ा है; सुन्नतों की नहीं, सिवाय फ़ज्र की सुन्नत के जब फ़र्ज़ के साथ ज़वाल से पहले क़ज़ा की जाए। क़ज़ा तीन ममनू वक़्तों के सिवा हर वक़्त पढ़ी जा सकती है।",
                "فرض اور وتر کی قضا ہے؛ سنتوں کی نہیں، سوائے فجر کی سنت کے جب فرض کے ساتھ زوال سے پہلے قضا کی جائے۔ قضا تین ممنوع وقتوں کے سوا ہر وقت پڑھی جا سکتی ہے۔"
            )
        ) + listOf(Madhhab.SHAFII, Madhhab.MALIKI, Madhhab.HANBALI).associateWith {
            t(
                "Witr is Sunnah, so only the missed Fard prayers are an obligation to make up.",
                "Witr Sunnat hai, is liye sirf chhoote hue Farz ki qaza zaroori hai.",
                "वित्र सुन्नत है, इसलिए सिर्फ़ छूटे हुए फ़र्ज़ की क़ज़ा ज़रूरी है।",
                "وتر سنت ہے، اس لیے صرف چھوٹے ہوئے فرض کی قضا ضروری ہے۔"
            )
        }
    ),
    SalahTopic(
        "musafir",
        t("Traveller's prayer (Qasr)", "Musafir ki namaz (Qasr)", "मुसाफ़िर की नमाज़ (क़स्र)", "مسافر کی نماز (قصر)"),
        null,
        t(
            "Shortening the four-rak‘at Fard on a journey",
            "Safar mein chaar rakat Farz ko do padhna",
            "सफ़र में चार रकअत फ़र्ज़ को दो पढ़ना",
            "سفر میں چار رکعت فرض کو دو پڑھنا"
        ),
        listOf(
            t(
                "The four-rak‘at Fard of Zuhr, Asr and Isha are prayed as two rak‘at.",
                "Zuhr, Asr aur Isha ke chaar Farz do rakat padhe jate hain.",
                "ज़ुहर, अस्र और इशा के चार फ़र्ज़ दो रकअत पढ़े जाते हैं।",
                "ظہر، عصر اور عشاء کے چار فرض دو رکعت پڑھے جاتے ہیں۔"
            ),
            t(
                "Fajr, Maghrib and Witr are not shortened.",
                "Fajr, Maghrib aur Witr mein qasr nahi.",
                "फ़ज्र, मग़रिब और वित्र में क़स्र नहीं।",
                "فجر، مغرب اور وتر میں قصر نہیں۔"
            ),
            t(
                "Behind a resident imam, a traveller prays the full four rak‘at.",
                "Muqeem imam ke peeche musafir poori chaar rakat padhega.",
                "मुक़ीम इमाम के पीछे मुसाफ़िर पूरी चार रकअत पढ़ेगा।",
                "مقیم امام کے پیچھے مسافر پوری چار رکعت پڑھے گا۔"
            ),
            t(
                "Sunnah prayers are offered when there is calm and time; the Sunnah of Fajr should not be left.",
                "Itminan aur waqt ho to Sunnatein padhein; Fajr ki Sunnat na chhodein.",
                "इत्मीनान और वक़्त हो तो सुन्नतें पढ़ें; फ़ज्र की सुन्नत न छोड़ें।",
                "اطمینان اور وقت ہو تو سنتیں پڑھیں؛ فجر کی سنت نہ چھوڑیں۔"
            )
        ),
        "Quran 4:101 · Sahih Muslim 686 · Sahih al-Bukhari 1090 · Bahar-e-Shariat, Hissa 4",
        schoolNotes = mapOf(
            Madhhab.HANAFI to t(
                "A traveller is one who leaves the built-up area of their town intending a journey of about 92 km (three days' travel). Qasr is then Wajib, and it ends on returning home or on intending to stay fifteen days or more in one place.",
                "Musafir woh hai jo taqriban 92 kilometre (teen din ki raah) ke safar ke iraade se apni aabadi se bahar nikal jaye. Us par qasr Wajib hai; ghar laut aane ya kisi ek jagah pandrah din ya zyada thehrne ki niyyat se qasr khatm ho jata hai.",
                "मुसाफ़िर वह है जो तक़रीबन 92 किलोमीटर (तीन दिन की राह) के सफ़र के इरादे से अपनी आबादी से बाहर निकल जाए। उस पर क़स्र वाजिब है; घर लौट आने या किसी एक जगह पंद्रह दिन या ज़्यादा ठहरने की नीयत से क़स्र ख़त्म हो जाता है।",
                "مسافر وہ ہے جو تقریباً 92 کلومیٹر (تین دن کی راہ) کے سفر کے ارادے سے اپنی آبادی سے باہر نکل جائے۔ اس پر قصر واجب ہے؛ گھر لوٹ آنے یا کسی ایک جگہ پندرہ دن یا زیادہ ٹھہرنے کی نیت سے قصر ختم ہو جاتا ہے۔"
            )
        ) + listOf(Madhhab.SHAFII, Madhhab.MALIKI, Madhhab.HANBALI).associateWith {
            t(
                "The journey is about 81 km or more. Qasr is not Wajib, it ends with an intended stay of four days or more, and two prayers may be joined on a journey.",
                "Safar taqriban 81 kilometre ya zyada ka ho. Qasr Wajib nahi, chaar din ya zyada thehrne ki niyyat se khatm ho jata hai, aur safar mein do namazein jama karna jaiz hai.",
                "सफ़र तक़रीबन 81 किलोमीटर या ज़्यादा का हो। क़स्र वाजिब नहीं, चार दिन या ज़्यादा ठहरने की नीयत से ख़त्म हो जाता है, और सफ़र में दो नमाज़ें जमा करना जायज़ है।",
                "سفر تقریباً 81 کلومیٹر یا زیادہ کا ہو۔ قصر واجب نہیں، چار دن یا زیادہ ٹھہرنے کی نیت سے ختم ہو جاتا ہے، اور سفر میں دو نمازیں جمع کرنا جائز ہے۔"
            )
        },
        numbered = false
    )
)
