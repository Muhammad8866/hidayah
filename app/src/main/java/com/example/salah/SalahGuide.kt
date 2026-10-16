package com.example.salah

/**
 * The step-by-step method of Salah. The sequence and rulings follow Hanafi fiqh (Ahle Sunnat wal
 * Jamaat) as taught in Bahar-e-Shariat, Hissa 3; the other schools get a note on what differs.
 */

private fun SalahText.with(name: String, value: SalahText): SalahText = withText(name, value)

private val toEars = t("to the ears", "kaanon tak", "कानों तक", "کانوں تک")
private val toShoulders = t("to the shoulders", "kandhon tak", "कंधों तक", "کندھوں تک")

private val sitMan = t(
    "Sit on the left foot laid flat, keep the right foot upright with its toes towards the Qibla, and rest the hands on the thighs near the knees.",
    "Baayan paon bichha kar us par baithein, daayan paon khada rakhein ke ungliyan Qible ki taraf hon, aur haath raanon par ghutnon ke paas rakhein.",
    "बायाँ पाँव बिछाकर उस पर बैठें, दायाँ पाँव खड़ा रखें कि उँगलियाँ क़िबले की तरफ़ हों, और हाथ रानों पर घुटनों के पास रखें।",
    "بایاں پاؤں بچھا کر اس پر بیٹھیں، دایاں پاؤں کھڑا رکھیں کہ انگلیاں قبلے کی طرف ہوں، اور ہاتھ رانوں پر گھٹنوں کے پاس رکھیں۔"
)
private val sitWoman = t(
    "Sit on the left hip with both feet out to the right side, and rest the hands on the thighs with the fingers together.",
    "Dono paon daayin taraf nikaal kar baayein sureen par baithein, aur haath raanon par rakhein, ungliyan mili hui.",
    "दोनों पाँव दाईं तरफ़ निकालकर बाएँ सुरीन पर बैठें, और हाथ रानों पर रखें, उँगलियाँ मिली हुई।",
    "دونوں پاؤں دائیں طرف نکال کر بائیں سرین پر بیٹھیں، اور ہاتھ رانوں پر رکھیں، انگلیاں ملی ہوئی۔"
)

private val raiseFinger = t(
    "At ‘ash-hadu al-la ilaha’ raise the index finger of the right hand, and lower it at ‘illallah’.",
    "'Ash-hadu al-la ilaha' par daayein haath ki shahadat ki ungli uthayein aur 'illallah' par gira dein.",
    "'अश्हदु अल्ला इलाहा' पर दाएँ हाथ की शहादत की उँगली उठाएँ और 'इल्लल्लाह' पर गिरा दें।",
    "’اشہد ان لا الٰہ‘ پر دائیں ہاتھ کی شہادت کی انگلی اٹھائیں اور ’الا اللہ‘ پر گرا دیں۔"
)

/** What a follower of another school should know before using the Hanafi sequence below. */
internal val schoolDifferenceNotes: Map<Madhhab, SalahText> = mapOf(
    Madhhab.SHAFII to t(
        "These steps follow the Hanafi method. In the Shafi‘i school: the hands are folded above the navel; the opening dua is ‘Wajjahtu wajhiya…’; Bismillah is recited as part of al-Fatihah and Ameen is said aloud in the loud prayers; the hands are raised when going into ruku‘ and when rising from it; the follower also recites al-Fatihah; and Qunut is read in Fajr after the ruku‘ of the second rak‘ah. Learn the details from a teacher of your school.",
        "Yeh tareeqa Hanafi mazhab ke mutabiq hai. Shafi'i mazhab mein: haath naaf ke upar baandhe jate hain; shuru ki dua 'Wajjahtu wajhiya…' hai; Bismillah Fatiha ka hissa samajh kar padhi jati hai aur jahri namazon mein Ameen aawaz se kahi jati hai; ruku mein jaate aur ruku se uthte waqt haath uthaye jate hain; muqtadi bhi Surah Fatiha padhta hai; aur Fajr ki doosri rakat mein ruku ke baad Qunoot padhi jati hai. Tafseel apne mazhab ke ustaad se seekhein.",
        "यह तरीक़ा हनफ़ी मज़हब के मुताबिक़ है। शाफ़ई मज़हब में: हाथ नाफ़ के ऊपर बाँधे जाते हैं; शुरू की दुआ 'वज्जहतु वजहिया…' है; बिस्मिल्लाह फ़ातिहा का हिस्सा समझकर पढ़ी जाती है और जहरी नमाज़ों में आमीन आवाज़ से कही जाती है; रुकू में जाते और रुकू से उठते वक़्त हाथ उठाए जाते हैं; मुक़्तदी भी सूरह फ़ातिहा पढ़ता है; और फ़ज्र की दूसरी रकअत में रुकू के बाद क़ुनूत पढ़ी जाती है। तफ़सील अपने मज़हब के उस्ताद से सीखें।",
        "یہ طریقہ حنفی مذہب کے مطابق ہے۔ شافعی مذہب میں: ہاتھ ناف کے اوپر باندھے جاتے ہیں؛ شروع کی دعا ’وجہت وجہی…‘ ہے؛ بسم اللہ فاتحہ کا حصہ سمجھ کر پڑھی جاتی ہے اور جہری نمازوں میں آمین آواز سے کہی جاتی ہے؛ رکوع میں جاتے اور رکوع سے اٹھتے وقت ہاتھ اٹھائے جاتے ہیں؛ مقتدی بھی سورۃ فاتحہ پڑھتا ہے؛ اور فجر کی دوسری رکعت میں رکوع کے بعد قنوت پڑھی جاتی ہے۔ تفصیل اپنے مذہب کے استاد سے سیکھیں۔"
    ),
    Madhhab.MALIKI to t(
        "These steps follow the Hanafi method. In the Maliki school: the arms are commonly left at the sides; no opening dua, Ta‘awwuz or Bismillah is recited in a Fard prayer; the follower does not recite behind the imam in the loud prayers; Qunut is read quietly in Fajr; and the prayer is ended with one obligatory salam. Learn the details from a teacher of your school.",
        "Yeh tareeqa Hanafi mazhab ke mutabiq hai. Maliki mazhab mein: aam taur par haath khule chhode jate hain; Farz namaz mein shuru ki dua, Ta'awwuz aur Bismillah nahi padhi jati; jahri namazon mein muqtadi imam ke peeche qira'at nahi karta; Fajr mein Qunoot aahista padhi jati hai; aur namaz ek farz salam se khatm hoti hai. Tafseel apne mazhab ke ustaad se seekhein.",
        "यह तरीक़ा हनफ़ी मज़हब के मुताबिक़ है। मालिकी मज़हब में: आम तौर पर हाथ खुले छोड़े जाते हैं; फ़र्ज़ नमाज़ में शुरू की दुआ, तअव्वुज़ और बिस्मिल्लाह नहीं पढ़ी जाती; जहरी नमाज़ों में मुक़्तदी इमाम के पीछे क़िराअत नहीं करता; फ़ज्र में क़ुनूत आहिस्ता पढ़ी जाती है; और नमाज़ एक फ़र्ज़ सलाम से ख़त्म होती है। तफ़सील अपने मज़हब के उस्ताद से सीखें।",
        "یہ طریقہ حنفی مذہب کے مطابق ہے۔ مالکی مذہب میں: عام طور پر ہاتھ کھلے چھوڑے جاتے ہیں؛ فرض نماز میں شروع کی دعا، تعوذ اور بسم اللہ نہیں پڑھی جاتی؛ جہری نمازوں میں مقتدی امام کے پیچھے قراءت نہیں کرتا؛ فجر میں قنوت آہستہ پڑھی جاتی ہے؛ اور نماز ایک فرض سلام سے ختم ہوتی ہے۔ تفصیل اپنے مذہب کے استاد سے سیکھیں۔"
    ),
    Madhhab.HANBALI to t(
        "These steps follow the Hanafi method. In the Hanbali school: the same Thana is recited; Ameen is said aloud in the loud prayers; the hands are raised when going into ruku‘ and when rising from it; ‘Rabbighfir li’ between the two sajdahs is required; and Witr is commonly prayed as two rak‘at and then one, with Qunut after ruku‘. Learn the details from a teacher of your school.",
        "Yeh tareeqa Hanafi mazhab ke mutabiq hai. Hanbali mazhab mein: yahi Sana padhi jati hai; jahri namazon mein Ameen aawaz se kahi jati hai; ruku mein jaate aur ruku se uthte waqt haath uthaye jate hain; dono sajdon ke darmiyan 'Rabbighfir li' kehna zaroori hai; aur Witr aam taur par do rakat aur phir ek rakat padhi jati hai, Qunoot ruku ke baad. Tafseel apne mazhab ke ustaad se seekhein.",
        "यह तरीक़ा हनफ़ी मज़हब के मुताबिक़ है। हंबली मज़हब में: यही सना पढ़ी जाती है; जहरी नमाज़ों में आमीन आवाज़ से कही जाती है; रुकू में जाते और रुकू से उठते वक़्त हाथ उठाए जाते हैं; दोनों सजदों के दरमियान 'रब्बिग़फ़िर ली' कहना ज़रूरी है; और वित्र आम तौर पर दो रकअत और फिर एक रकअत पढ़ी जाती है, क़ुनूत रुकू के बाद। तफ़सील अपने मज़हब के उस्ताद से सीखें।",
        "یہ طریقہ حنفی مذہب کے مطابق ہے۔ حنبلی مذہب میں: یہی ثنا پڑھی جاتی ہے؛ جہری نمازوں میں آمین آواز سے کہی جاتی ہے؛ رکوع میں جاتے اور رکوع سے اٹھتے وقت ہاتھ اٹھائے جاتے ہیں؛ دونوں سجدوں کے درمیان ’ربِّ اغفر لی‘ کہنا ضروری ہے؛ اور وتر عام طور پر دو رکعت اور پھر ایک رکعت پڑھی جاتی ہے، قنوت رکوع کے بعد۔ تفصیل اپنے مذہب کے استاد سے سیکھیں۔"
    )
)

/** Surahs suggested for rak‘at one to four; they keep the order of the Mushaf. */
private val suggestedSurahs = listOf("kawthar", "ikhlas", "falaq", "nas")

internal fun buildPrayerSteps(
    form: GuideForm,
    madhhab: Madhhab,
    worshipper: Worshipper,
    prayingAs: PrayingAs
): List<GuideStep> {
    val man = worshipper == Worshipper.MAN
    val alone = prayingAs == PrayingAs.ALONE
    val fard = form.kind == SalahKind.FARD
    val sit = if (man) sitMan else sitWoman
    val steps = mutableListOf<GuideStep>()

    steps += GuideStep(
        "prepare",
        t("Before you begin", "Shuru karne se pehle", "शुरू करने से पहले", "شروع کرنے سے پہلے"),
        t(
            "Be in wudu with a clean body, clean clothes and a clean place; cover the awrah; make sure the time of the prayer has entered; and stand facing the Qibla.",
            "Ba-wuzu hon, badan, kapde aur jagah paak hon; satr dhaka ho; namaz ka waqt ho chuka ho; aur Qible ki taraf munh karke khade hon.",
            "बा-वुज़ू हों, बदन, कपड़े और जगह पाक हों; सत्र ढका हो; नमाज़ का वक़्त हो चुका हो; और क़िबले की तरफ़ मुँह करके खड़े हों।",
            "باوضو ہوں، بدن، کپڑے اور جگہ پاک ہوں؛ ستر ڈھکا ہو؛ نماز کا وقت ہو چکا ہو؛ اور قبلے کی طرف منہ کر کے کھڑے ہوں۔"
        ),
        Ruling.CONDITION,
        points = listOf(
            t(
                "Keep about four fingers' space between the feet.",
                "Dono paon ke darmiyan taqriban chaar ungal ka faasla rakhein.",
                "दोनों पाँवों के दरमियान तक़रीबन चार उँगल का फ़ासला रखें।",
                "دونوں پاؤں کے درمیان تقریباً چار انگل کا فاصلہ رکھیں۔"
            )
        )
    )
    steps += GuideStep(
        "niyyah",
        t("Niyyah (intention)", "Niyyat", "नीयत", "نیت"),
        t(
            "Intend in your heart which prayer you are offering, for example ‘two rak‘at Fard of Fajr, for Allah’. Saying it with the tongue is recommended, not required.",
            "Dil mein pakka irada karein ke kaun si namaz padh rahe hain, jaise 'do rakat Farz Fajr, Allah ke liye'. Zabaan se kehna mustahab hai, zaroori nahi.",
            "दिल में पक्का इरादा करें कि कौन सी नमाज़ पढ़ रहे हैं, जैसे 'दो रकअत फ़र्ज़ फ़ज्र, अल्लाह के लिए'। ज़बान से कहना मुस्तहब है, ज़रूरी नहीं।",
            "دل میں پکا ارادہ کریں کہ کون سی نماز پڑھ رہے ہیں، جیسے ’دو رکعت فرض فجر، اللہ کے لیے‘۔ زبان سے کہنا مستحب ہے، ضروری نہیں۔"
        ),
        Ruling.CONDITION,
        points = if (alone) emptyList() else listOf(
            t(
                "Also intend that you are following this imam.",
                "Yeh bhi niyyat karein ke is imam ke peeche padh raha hoon.",
                "यह भी नीयत करें कि इस इमाम के पीछे पढ़ रहा हूँ।",
                "یہ بھی نیت کریں کہ اس امام کے پیچھے پڑھ رہا ہوں۔"
            )
        )
    )
    steps += GuideStep(
        "takbir",
        t("Takbir-e-Tahrimah", "Takbeer-e-Tahreema", "तकबीर-ए-तहरीमा", "تکبیرِ تحریمہ"),
        if (man) t(
            "Raise both hands until the thumbs touch the earlobes, palms facing the Qibla, and say Allahu Akbar. Then lower them and place the right hand over the left below the navel.",
            "Dono haath kaanon tak uthayein ke angoothe kaan ki lau se lag jayein aur hatheliyan Qible ki taraf hon, phir Allahu Akbar kahein. Phir haath naaf ke neeche baandh lein, daayan haath baayein ke upar.",
            "दोनों हाथ कानों तक उठाएँ कि अँगूठे कान की लौ से लग जाएँ और हथेलियाँ क़िबले की तरफ़ हों, फिर अल्लाहु अकबर कहें। फिर हाथ नाफ़ के नीचे बाँध लें, दायाँ हाथ बाएँ के ऊपर।",
            "دونوں ہاتھ کانوں تک اٹھائیں کہ انگوٹھے کان کی لو سے لگ جائیں اور ہتھیلیاں قبلے کی طرف ہوں، پھر اللہ اکبر کہیں۔ پھر ہاتھ ناف کے نیچے باندھ لیں، دایاں ہاتھ بائیں کے اوپر۔"
        ) else t(
            "Raise both hands to the shoulders, keeping them inside the shawl, and say Allahu Akbar. Then place the right palm over the back of the left hand on the chest.",
            "Dono haath chadar ke andar hi kandhon tak uthayein aur Allahu Akbar kahein. Phir seene par baayein haath ki pusht par daayin hatheli rakh lein.",
            "दोनों हाथ चादर के अंदर ही कंधों तक उठाएँ और अल्लाहु अकबर कहें। फिर सीने पर बाएँ हाथ की पुश्त पर दाईं हथेली रख लें।",
            "دونوں ہاتھ چادر کے اندر ہی کندھوں تک اٹھائیں اور اللہ اکبر کہیں۔ پھر سینے پر بائیں ہاتھ کی پشت پر دائیں ہتھیلی رکھ لیں۔"
        ),
        Ruling.FARD,
        points = listOf(
            t(
                "While standing, keep your eyes on the place of sajdah.",
                "Qiyam mein nazar sajde ki jagah par rakhein.",
                "क़ियाम में नज़र सजदे की जगह पर रखें।",
                "قیام میں نظر سجدے کی جگہ پر رکھیں۔"
            )
        ),
        recitationIds = listOf("takbir")
    )
    steps += if (alone) GuideStep(
        "thana",
        t("Thana", "Sana", "सना", "ثنا"),
        t(
            "Recite Thana quietly, then Ta‘awwuz and Tasmiyah.",
            "Aahista Sana padhein, phir Ta'awwuz aur Tasmiya.",
            "आहिस्ता सना पढ़ें, फिर तअव्वुज़ और तस्मिया।",
            "آہستہ ثنا پڑھیں، پھر تعوذ اور تسمیہ۔"
        ),
        Ruling.SUNNAH,
        recitationIds = listOf("thana", "taawwuz", "tasmiyah")
    ) else GuideStep(
        "thana",
        t("Thana", "Sana", "सना", "ثنا"),
        t(
            "Recite only Thana quietly, then stay silent. Behind the imam you do not recite Ta‘awwuz, Tasmiyah, al-Fatihah or a surah.",
            "Sirf Sana aahista padhein, phir khamosh rahein. Imam ke peeche Ta'awwuz, Tasmiya, Surah Fatiha aur surat nahi padhi jati.",
            "सिर्फ़ सना आहिस्ता पढ़ें, फिर ख़ामोश रहें। इमाम के पीछे तअव्वुज़, तस्मिया, सूरह फ़ातिहा और सूरत नहीं पढ़ी जाती।",
            "صرف ثنا آہستہ پڑھیں، پھر خاموش رہیں۔ امام کے پیچھے تعوذ، تسمیہ، سورۃ فاتحہ اور سورت نہیں پڑھی جاتی۔"
        ),
        Ruling.SUNNAH,
        points = listOf(
            t(
                "If the imam has already begun reciting aloud, do not recite Thana either; listen.",
                "Agar imam buland aawaz se qira'at shuru kar chuka ho to Sana bhi na padhein, sunein.",
                "अगर इमाम बुलंद आवाज़ से क़िराअत शुरू कर चुका हो तो सना भी न पढ़ें, सुनें।",
                "اگر امام بلند آواز سے قراءت شروع کر چکا ہو تو ثنا بھی نہ پڑھیں، سنیں۔"
            )
        ),
        recitationIds = listOf("thana")
    )

    val qiraatTitle = t("Qira’at (recitation)", "Qira'at", "क़िराअत", "قراءت")
    for (rakah in 1..form.rakats) {
        val withSurah = rakah <= 2 || !fard
        steps += when {
            !alone -> GuideStep(
                "qiraat_$rakah", qiraatTitle,
                t(
                    "Stand silently and listen to the imam. Do not recite al-Fatihah or a surah, whether he recites aloud or quietly.",
                    "Khamosh khade rahein aur imam ki qira'at sunein. Imam aawaz se padhe ya aahista, aap Fatiha ya surat na padhein.",
                    "ख़ामोश खड़े रहें और इमाम की क़िराअत सुनें। इमाम आवाज़ से पढ़े या आहिस्ता, आप फ़ातिहा या सूरत न पढ़ें।",
                    "خاموش کھڑے رہیں اور امام کی قراءت سنیں۔ امام آواز سے پڑھے یا آہستہ، آپ فاتحہ یا سورت نہ پڑھیں۔"
                ),
                Ruling.WAJIB, rakah,
                points = if (rakah > 2) emptyList() else listOf(
                    t(
                        "When the imam finishes al-Fatihah aloud, say Ameen quietly.",
                        "Jab imam aawaz se Fatiha khatm kare to aahista Ameen kahein.",
                        "जब इमाम आवाज़ से फ़ातिहा ख़त्म करे तो आहिस्ता आमीन कहें।",
                        "جب امام آواز سے فاتحہ ختم کرے تو آہستہ آمین کہیں۔"
                    )
                )
            )
            rakah == 1 -> GuideStep(
                "qiraat_$rakah", qiraatTitle,
                t(
                    "Recite Surah al-Fatihah, say Ameen quietly, then recite a surah or at least three short verses.",
                    "Surah Fatiha padhein, aahista Ameen kahein, phir koi surat ya kam az kam teen chhoti aayatein padhein.",
                    "सूरह फ़ातिहा पढ़ें, आहिस्ता आमीन कहें, फिर कोई सूरत या कम से कम तीन छोटी आयतें पढ़ें।",
                    "سورۃ فاتحہ پڑھیں، آہستہ آمین کہیں، پھر کوئی سورت یا کم از کم تین چھوٹی آیتیں پڑھیں۔"
                ),
                Ruling.FARD, rakah,
                points = listOf(
                    t(
                        "Any surah you know may be recited. Keep the order of the Quran: the surah of the second rak‘ah should come after that of the first.",
                        "Jo surat yaad ho padh sakte hain. Quran ki tarteeb ka khayal rakhein: doosri rakat ki surat pehli rakat wali ke baad ki ho.",
                        "जो सूरत याद हो पढ़ सकते हैं। क़ुरआन की तरतीब का ख़याल रखें: दूसरी रकअत की सूरत पहली रकअत वाली के बाद की हो।",
                        "جو سورت یاد ہو پڑھ سکتے ہیں۔ قرآن کی ترتیب کا خیال رکھیں: دوسری رکعت کی سورت پہلی رکعت والی کے بعد کی ہو۔"
                    )
                ),
                recitationIds = listOf("fatiha", suggestedSurahs[0])
            )
            withSurah -> GuideStep(
                "qiraat_$rakah", qiraatTitle,
                t(
                    "Recite Bismillah, Surah al-Fatihah and another surah, as in the first rak‘ah.",
                    "Bismillah, Surah Fatiha aur ek aur surat padhein, pehli rakat ki tarah.",
                    "बिस्मिल्लाह, सूरह फ़ातिहा और एक और सूरत पढ़ें, पहली रकअत की तरह।",
                    "بسم اللہ، سورۃ فاتحہ اور ایک اور سورت پڑھیں، پہلی رکعت کی طرح۔"
                ),
                Ruling.FARD, rakah,
                points = if (rakah <= 2) emptyList() else listOf(
                    t(
                        "In Sunnah, Nafl and Witr a surah is added after al-Fatihah in every rak‘ah.",
                        "Sunnat, Nafl aur Witr ki har rakat mein Fatiha ke baad surat milayi jati hai.",
                        "सुन्नत, नफ़्ल और वित्र की हर रकअत में फ़ातिहा के बाद सूरत मिलाई जाती है।",
                        "سنت، نفل اور وتر کی ہر رکعت میں فاتحہ کے بعد سورت ملائی جاتی ہے۔"
                    )
                ),
                recitationIds = listOf("fatiha", suggestedSurahs[rakah - 1])
            )
            else -> GuideStep(
                "qiraat_$rakah", qiraatTitle,
                t(
                    "Recite Bismillah and Surah al-Fatihah only. No surah is added in the third and fourth rak‘at of a Fard prayer.",
                    "Sirf Bismillah aur Surah Fatiha padhein. Farz ki teesri aur chauthi rakat mein surat nahi milayi jati.",
                    "सिर्फ़ बिस्मिल्लाह और सूरह फ़ातिहा पढ़ें। फ़र्ज़ की तीसरी और चौथी रकअत में सूरत नहीं मिलाई जाती।",
                    "صرف بسم اللہ اور سورۃ فاتحہ پڑھیں۔ فرض کی تیسری اور چوتھی رکعت میں سورت نہیں ملائی جاتی۔"
                ),
                Ruling.SUNNAH, rakah,
                recitationIds = listOf("fatiha")
            )
        }

        if (form == GuideForm.WITR_3 && rakah == 3) {
            steps += GuideStep(
                "qunut",
                t("Dua-e-Qunut", "Dua-e-Qunoot", "दुआ-ए-क़ुनूत", "دعائے قنوت"),
                t(
                    "After the surah of the third rak‘ah, raise the hands {hands} saying Allahu Akbar, fold them again and recite Dua-e-Qunut quietly. Then go into ruku‘.",
                    "Teesri rakat ki surat ke baad Allahu Akbar kehte hue haath {hands} uthayein, phir baandh lein aur aahista Dua-e-Qunoot padhein. Uske baad ruku karein.",
                    "तीसरी रकअत की सूरत के बाद अल्लाहु अकबर कहते हुए हाथ {hands} उठाएँ, फिर बाँध लें और आहिस्ता दुआ-ए-क़ुनूत पढ़ें। उसके बाद रुकू करें।",
                    "تیسری رکعت کی سورت کے بعد اللہ اکبر کہتے ہوئے ہاتھ {hands} اٹھائیں، پھر باندھ لیں اور آہستہ دعائے قنوت پڑھیں۔ اس کے بعد رکوع کریں۔"
                ).with("hands", if (man) toEars else toShoulders),
                Ruling.WAJIB, rakah,
                points = listOf(
                    t(
                        "If you have not memorised it yet, recite ‘Rabbana atina…’ or say ‘Allahummaghfir li’ three times, and keep learning it.",
                        "Agar abhi yaad na ho to 'Rabbana atina…' padhein ya teen baar 'Allahummaghfir li' kahein, aur ise yaad karte rahein.",
                        "अगर अभी याद न हो तो 'रब्बना आतिना…' पढ़ें या तीन बार 'अल्लाहुम्मग़फ़िर ली' कहें, और इसे याद करते रहें।",
                        "اگر ابھی یاد نہ ہو تو ’ربنا آتنا…‘ پڑھیں یا تین بار ’اللّٰہم اغفر لی‘ کہیں، اور اسے یاد کرتے رہیں۔"
                    )
                ),
                recitationIds = if (madhhab == Madhhab.HANAFI || madhhab == Madhhab.MALIKI) listOf("qunut_hanafi", "rabbana_atina") else listOf("qunut")
            )
        }

        steps += GuideStep(
            "ruku_$rakah",
            t("Ruku‘", "Ruku", "रुकू", "رکوع"),
            if (man) t(
                "Say Allahu Akbar and bow. Grip the knees with the fingers spread, keep the back level and the head in line with it. Recite the tasbih at least three times.",
                "Allahu Akbar kehte hue ruku mein jayein. Ghutnon ko haathon se pakdein, ungliyan khuli hon, peeth bichhi hui aur sar peeth ki seedh mein ho. Tasbeeh kam az kam teen baar padhein.",
                "अल्लाहु अकबर कहते हुए रुकू में जाएँ। घुटनों को हाथों से पकड़ें, उँगलियाँ खुली हों, पीठ बिछी हुई और सर पीठ की सीध में हो। तस्बीह कम से कम तीन बार पढ़ें।",
                "اللہ اکبر کہتے ہوئے رکوع میں جائیں۔ گھٹنوں کو ہاتھوں سے پکڑیں، انگلیاں کھلی ہوں، پیٹھ بچھی ہوئی اور سر پیٹھ کی سیدھ میں ہو۔ تسبیح کم از کم تین بار پڑھیں۔"
            ) else t(
                "Say Allahu Akbar and bow only as far as the hands reach the knees. Rest the hands on the knees with the fingers together, keep the arms close to the body and do not straighten the back fully. Recite the tasbih at least three times.",
                "Allahu Akbar kehte hue sirf itna jhukein ke haath ghutnon tak pahunch jayein. Haath ghutnon par rakhein, ungliyan mili hui hon, baazu jism se mile rahein aur peeth poori seedhi na karein. Tasbeeh kam az kam teen baar padhein.",
                "अल्लाहु अकबर कहते हुए सिर्फ़ इतना झुकें कि हाथ घुटनों तक पहुँच जाएँ। हाथ घुटनों पर रखें, उँगलियाँ मिली हुई हों, बाज़ू जिस्म से मिले रहें और पीठ पूरी सीधी न करें। तस्बीह कम से कम तीन बार पढ़ें।",
                "اللہ اکبر کہتے ہوئے صرف اتنا جھکیں کہ ہاتھ گھٹنوں تک پہنچ جائیں۔ ہاتھ گھٹنوں پر رکھیں، انگلیاں ملی ہوئی ہوں، بازو جسم سے ملے رہیں اور پیٹھ پوری سیدھی نہ کریں۔ تسبیح کم از کم تین بار پڑھیں۔"
            ),
            Ruling.FARD, rakah,
            recitationIds = listOf("ruku")
        )
        steps += GuideStep(
            "qawmah_$rakah",
            t("Qawmah (standing up)", "Qauma", "क़ौमा", "قومہ"),
            if (alone) t(
                "Rise saying Sami‘allahu liman hamidah, stand fully upright with the arms at the sides, and say Rabbana wa lakal-hamd.",
                "Sami'allahu liman hamidah kehte hue uthein, haath chhod kar bilkul seedhe khade ho jayein aur Rabbana wa lakal-hamd kahein.",
                "समिअल्लाहु लिमन हमिदह कहते हुए उठें, हाथ छोड़कर बिल्कुल सीधे खड़े हो जाएँ और रब्बना व लकल-हम्द कहें।",
                "سمع اللہ لمن حمدہ کہتے ہوئے اٹھیں، ہاتھ چھوڑ کر بالکل سیدھے کھڑے ہو جائیں اور ربنا ولک الحمد کہیں۔"
            ) else t(
                "When the imam says Sami‘allahu liman hamidah, rise, stand fully upright and say only Rabbana wa lakal-hamd.",
                "Jab imam Sami'allahu liman hamidah kahe to uthein, bilkul seedhe khade ho jayein aur sirf Rabbana wa lakal-hamd kahein.",
                "जब इमाम समिअल्लाहु लिमन हमिदह कहे तो उठें, बिल्कुल सीधे खड़े हो जाएँ और सिर्फ़ रब्बना व लकल-हम्द कहें।",
                "جب امام سمع اللہ لمن حمدہ کہے تو اٹھیں، بالکل سیدھے کھڑے ہو جائیں اور صرف ربنا ولک الحمد کہیں۔"
            ),
            Ruling.WAJIB, rakah,
            points = listOf(
                t(
                    "Pause until every joint has settled before going down.",
                    "Itni der thehrein ke har jod apni jagah aa jaye, phir sajde mein jayein.",
                    "इतनी देर ठहरें कि हर जोड़ अपनी जगह आ जाए, फिर सजदे में जाएँ।",
                    "اتنی دیر ٹھہریں کہ ہر جوڑ اپنی جگہ آ جائے، پھر سجدے میں جائیں۔"
                )
            ),
            recitationIds = if (alone) listOf("tasmi", "tahmid") else listOf("tahmid")
        )

        val staysSeated = rakah == form.rakats || (rakah == 2 && form.rakats > 2)
        steps += GuideStep(
            "sujud_$rakah",
            t("Two sajdahs", "Dono sajde", "दोनों सजदे", "دونوں سجدے"),
            if (man) t(
                "Say Allahu Akbar and go down: knees first, then hands, then nose and forehead between the hands. Keep the elbows raised, the arms away from the sides, the belly away from the thighs, and the toes pressed towards the Qibla. Recite the tasbih at least three times.",
                "Allahu Akbar kehte hue sajde mein jayein: pehle ghutne, phir haath, phir naak aur peshani dono haathon ke beech rakhein. Kohniyan zameen se uthi hon, baazu karwaton se aur pet raanon se alag rahe, aur paon ki ungliyan Qible ki taraf lagi hon. Tasbeeh kam az kam teen baar padhein.",
                "अल्लाहु अकबर कहते हुए सजदे में जाएँ: पहले घुटने, फिर हाथ, फिर नाक और पेशानी दोनों हाथों के बीच रखें। कोहनियाँ ज़मीन से उठी हों, बाज़ू करवटों से और पेट रानों से अलग रहे, और पाँव की उँगलियाँ क़िबले की तरफ़ लगी हों। तस्बीह कम से कम तीन बार पढ़ें।",
                "اللہ اکبر کہتے ہوئے سجدے میں جائیں: پہلے گھٹنے، پھر ہاتھ، پھر ناک اور پیشانی دونوں ہاتھوں کے بیچ رکھیں۔ کہنیاں زمین سے اٹھی ہوں، بازو کروٹوں سے اور پیٹ رانوں سے الگ رہے، اور پاؤں کی انگلیاں قبلے کی طرف لگی ہوں۔ تسبیح کم از کم تین بار پڑھیں۔"
            ) else t(
                "Say Allahu Akbar and go down into sajdah drawn together: the arms resting on the ground close to the sides, the belly on the thighs and both feet turned out to the right. Recite the tasbih at least three times.",
                "Allahu Akbar kehte hue simat kar sajda karein: baazu zameen par bichhe hon aur karwaton se mile hon, pet raanon se laga ho aur dono paon daayin taraf nikle hon. Tasbeeh kam az kam teen baar padhein.",
                "अल्लाहु अकबर कहते हुए सिमटकर सजदा करें: बाज़ू ज़मीन पर बिछे हों और करवटों से मिले हों, पेट रानों से लगा हो और दोनों पाँव दाईं तरफ़ निकले हों। तस्बीह कम से कम तीन बार पढ़ें।",
                "اللہ اکبر کہتے ہوئے سمٹ کر سجدہ کریں: بازو زمین پر بچھے ہوں اور کروٹوں سے ملے ہوں، پیٹ رانوں سے لگا ہو اور دونوں پاؤں دائیں طرف نکلے ہوں۔ تسبیح کم از کم تین بار پڑھیں۔"
            ),
            Ruling.FARD, rakah,
            points = listOf(
                t(
                    "Rise with Allahu Akbar and sit upright for a moment (Jalsa). You may say Rabbighfir li.",
                    "Allahu Akbar kehte hue uthein aur ek lamha seedhe baith jayein (Jalsa). Rabbighfir li keh sakte hain.",
                    "अल्लाहु अकबर कहते हुए उठें और एक लम्हा सीधे बैठ जाएँ (जलसा)। रब्बिग़फ़िर ली कह सकते हैं।",
                    "اللہ اکبر کہتے ہوئے اٹھیں اور ایک لمحہ سیدھے بیٹھ جائیں (جلسہ)۔ ربِّ اغفر لی کہہ سکتے ہیں۔"
                ),
                t(
                    "Say Allahu Akbar and make the second sajdah in the same way.",
                    "Allahu Akbar keh kar doosra sajda bhi isi tarah karein.",
                    "अल्लाहु अकबर कहकर दूसरा सजदा भी इसी तरह करें।",
                    "اللہ اکبر کہہ کر دوسرا سجدہ بھی اسی طرح کریں۔"
                ),
                if (staysSeated) t(
                    "After the second sajdah say Allahu Akbar and remain seated.",
                    "Doosre sajde ke baad Allahu Akbar keh kar baithe rahein.",
                    "दूसरे सजदे के बाद अल्लाहु अकबर कहकर बैठे रहें।",
                    "دوسرے سجدے کے بعد اللہ اکبر کہہ کر بیٹھے رہیں۔"
                ) else t(
                    "Then stand up for the next rak‘ah saying Allahu Akbar, without sitting or leaning on the ground.",
                    "Phir Allahu Akbar kehte hue agli rakat ke liye khade ho jayein, baghair baithe aur zameen par tek lagaye.",
                    "फिर अल्लाहु अकबर कहते हुए अगली रकअत के लिए खड़े हो जाएँ, बग़ैर बैठे और ज़मीन पर टेक लगाए।",
                    "پھر اللہ اکبر کہتے ہوئے اگلی رکعت کے لیے کھڑے ہو جائیں، بغیر بیٹھے اور زمین پر ٹیک لگائے۔"
                )
            ),
            recitationIds = listOf("sujud", "between")
        )

        if (rakah == 2 && form.rakats > 2) {
            steps += GuideStep(
                "qadah_ula",
                t("First sitting (Qa‘dah Ula)", "Qa'da-e-Oola (pehla qa'da)", "क़ादा-ए-ऊला (पहला क़ादा)", "قعدۂ اولیٰ (پہلا قعدہ)"),
                sit then t("Recite only the Tashahhud.", "Sirf At-Tahiyyat padhein.", "सिर्फ़ अत्तहिय्यात पढ़ें।", "صرف التحیات پڑھیں۔"),
                Ruling.WAJIB,
                points = listOfNotNull(
                    raiseFinger,
                    t(
                        "Then stand for the third rak‘ah saying Allahu Akbar.",
                        "Phir Allahu Akbar kehte hue teesri rakat ke liye khade ho jayein.",
                        "फिर अल्लाहु अकबर कहते हुए तीसरी रकअत के लिए खड़े हो जाएँ।",
                        "پھر اللہ اکبر کہتے ہوئے تیسری رکعت کے لیے کھڑے ہو جائیں۔"
                    ),
                    if (form != GuideForm.SUNNAH_4) null else t(
                        "In four rak‘at of Sunnah Ghair-Muakkadah or Nafl, also recite Durood and dua here and begin the third rak‘ah with Thana. In Sunnah Muakkadah recite only the Tashahhud.",
                        "Chaar rakat Sunnat-e-Ghair Muakkada ya Nafl mein yahan Durood aur dua bhi padhein aur teesri rakat Sana se shuru karein. Sunnat-e-Muakkada mein sirf At-Tahiyyat padhein.",
                        "चार रकअत सुन्नत-ए-ग़ैर मुअक्कदा या नफ़्ल में यहाँ दुरूद और दुआ भी पढ़ें और तीसरी रकअत सना से शुरू करें। सुन्नत-ए-मुअक्कदा में सिर्फ़ अत्तहिय्यात पढ़ें।",
                        "چار رکعت سنتِ غیر مؤکدہ یا نفل میں یہاں درود اور دعا بھی پڑھیں اور تیسری رکعت ثنا سے شروع کریں۔ سنتِ مؤکدہ میں صرف التحیات پڑھیں۔"
                    )
                ),
                recitationIds = listOf("tashahhud")
            )
        }
    }

    steps += GuideStep(
        "qadah_akhirah",
        t("Final sitting (Qa‘dah Akhirah)", "Qa'da-e-Akheera (aakhri qa'da)", "क़ादा-ए-अख़ीरा (आख़िरी क़ादा)", "قعدۂ اخیرہ (آخری قعدہ)"),
        sit then t(
            "Recite the Tashahhud, then Durood-e-Ibrahim, then a dua.",
            "At-Tahiyyat padhein, phir Durood-e-Ibrahimi, phir dua.",
            "अत्तहिय्यात पढ़ें, फिर दुरूद-ए-इब्राहीमी, फिर दुआ।",
            "التحیات پڑھیں، پھر درودِ ابراہیمی، پھر دعا۔"
        ),
        Ruling.FARD,
        points = listOf(
            raiseFinger,
            t(
                "Either of the two duas shown is enough.",
                "Dikhayi gayi dono duaon mein se koi ek kaafi hai.",
                "दिखाई गई दोनों दुआओं में से कोई एक काफ़ी है।",
                "دکھائی گئی دونوں دعاؤں میں سے کوئی ایک کافی ہے۔"
            )
        ),
        recitationIds = listOf("tashahhud", "salawat", "dua_masura", "dua_ibrahim")
    )
    steps += GuideStep(
        "salam",
        t("Salam", "Salam", "सलाम", "سلام"),
        t(
            "Turn the face to the right and say As-salamu ‘alaykum wa rahmatullah, then to the left and say it again. The prayer is complete.",
            "Daayin taraf munh pher kar As-salamu alaykum wa rahmatullah kahein, phir baayin taraf pher kar dobara kahein. Namaz mukammal ho gayi.",
            "दाईं तरफ़ मुँह फेरकर अस्सलामु अलैकुम व रहमतुल्लाह कहें, फिर बाईं तरफ़ फेरकर दोबारा कहें। नमाज़ मुकम्मल हो गई।",
            "دائیں طرف منہ پھیر کر السلام علیکم ورحمۃ اللہ کہیں، پھر بائیں طرف پھیر کر دوبارہ کہیں۔ نماز مکمل ہو گئی۔"
        ),
        Ruling.WAJIB,
        points = if (alone) emptyList() else listOf(
            t(
                "Give each salam just after the imam, never before him.",
                "Har salam imam ke foran baad pherein, usse pehle hargiz nahi.",
                "हर सलाम इमाम के फ़ौरन बाद फेरें, उससे पहले हरगिज़ नहीं।",
                "ہر سلام امام کے فوراً بعد پھیریں، اس سے پہلے ہرگز نہیں۔"
            )
        ),
        recitationIds = listOf("salam")
    )
    steps += GuideStep(
        "after",
        t("After the salam", "Salam ke baad", "सलाम के बाद", "سلام کے بعد"),
        t(
            "Say Astaghfirullah three times, recite Ayat al-Kursi and the Tasbih of Fatimah, then raise your hands and make dua.",
            "Teen baar Astaghfirullah kahein, Ayatul Kursi aur Tasbeeh-e-Fatima padhein, phir haath utha kar dua maangein.",
            "तीन बार अस्तग़फ़िरुल्लाह कहें, आयतुल कुर्सी और तस्बीह-ए-फ़ातिमा पढ़ें, फिर हाथ उठाकर दुआ माँगें।",
            "تین بار استغفر اللہ کہیں، آیت الکرسی اور تسبیحِ فاطمہ پڑھیں، پھر ہاتھ اٹھا کر دعا مانگیں۔"
        ),
        Ruling.MUSTAHAB,
        points = if (!fard) emptyList() else listOf(
            t(
                "After a Fard that is followed by Sunnah (Zuhr, Maghrib, Isha), make a short dua, pray the Sunnah, and recite these afterwards.",
                "Jin Farzon ke baad Sunnatein hain (Zuhr, Maghrib, Isha) unke baad mukhtasar dua karke pehle Sunnatein padhein, phir yeh azkaar padhein.",
                "जिन फ़र्ज़ों के बाद सुन्नतें हैं (ज़ुहर, मग़रिब, इशा) उनके बाद मुख़्तसर दुआ करके पहले सुन्नतें पढ़ें, फिर ये अज़कार पढ़ें।",
                "جن فرضوں کے بعد سنتیں ہیں (ظہر، مغرب، عشاء) ان کے بعد مختصر دعا کر کے پہلے سنتیں پڑھیں، پھر یہ اذکار پڑھیں۔"
            )
        ),
        recitationIds = listOf("after_istighfar", "ayat_kursi", "tasbih_fatimi")
    )
    return steps
}
