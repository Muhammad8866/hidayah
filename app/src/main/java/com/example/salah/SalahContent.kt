package com.example.salah

/**
 * Educational content only; prayer times stay in the prayer-time module. The detailed method and
 * rulings follow Hanafi fiqh (Ahle Sunnat wal Jamaat), with notes where the other schools differ.
 */
object SalahContent {
    object Notes {
        val DAILY = t(
            "The rak‘at below follow the Hanafi teaching. Fard is obligatory; Sunnah Muakkadah should not be left without an excuse; Sunnah Ghair-Muakkadah and Nafl are optional and rewarded.",
            "Neeche di gayi rakatein Hanafi ta'leem ke mutabiq hain. Farz laazim hai; Sunnat-e-Muakkada bila uzr na chhodi jaye; Sunnat-e-Ghair Muakkada aur Nafl ikhtiyari hain aur un par sawab hai.",
            "नीचे दी गई रकअतें हनफ़ी तालीम के मुताबिक़ हैं। फ़र्ज़ लाज़िम है; सुन्नत-ए-मुअक्कदा बिला उज़्र न छोड़ी जाए; सुन्नत-ए-ग़ैर मुअक्कदा और नफ़्ल इख़्तियारी हैं और उन पर सवाब है।",
            "نیچے دی گئی رکعتیں حنفی تعلیم کے مطابق ہیں۔ فرض لازم ہے؛ سنتِ مؤکدہ بلا عذر نہ چھوڑی جائے؛ سنتِ غیر مؤکدہ اور نفل اختیاری ہیں اور ان پر ثواب ہے۔"
        )
        val ALOUD = t(
            "In congregation the imam recites aloud in Fajr, in the first two rak‘at of Maghrib and Isha, and in Jumu‘ah and Eid. Zuhr and Asr are quiet. Praying alone, you may recite quietly in all of them.",
            "Jamaat mein imam Fajr, Maghrib aur Isha ki pehli do rakaton, Jumu'ah aur Eid mein aawaz se qira'at karta hai. Zuhr aur Asr mein aahista. Akele padhne wala sab mein aahista padh sakta hai.",
            "जमाअत में इमाम फ़ज्र, मग़रिब और इशा की पहली दो रकअतों, जुमा और ईद में आवाज़ से क़िराअत करता है। ज़ुहर और अस्र में आहिस्ता। अकेले पढ़ने वाला सब में आहिस्ता पढ़ सकता है।",
            "جماعت میں امام فجر، مغرب اور عشاء کی پہلی دو رکعتوں، جمعہ اور عید میں آواز سے قراءت کرتا ہے۔ ظہر اور عصر میں آہستہ۔ اکیلے پڑھنے والا سب میں آہستہ پڑھ سکتا ہے۔"
        )
        val TWELVE_SUNNAH = t(
            "Whoever prays twelve rak‘at of Sunnah in a day and night, a house is built for him in Paradise.",
            "Jo din aur raat mein baarah rakat Sunnat padhe, uske liye Jannat mein ek ghar banaya jata hai.",
            "जो दिन और रात में बारह रकअत सुन्नत पढ़े, उसके लिए जन्नत में एक घर बनाया जाता है।",
            "جو دن اور رات میں بارہ رکعت سنت پڑھے، اس کے لیے جنت میں ایک گھر بنایا جاتا ہے۔"
        )
        const val TWELVE_SUNNAH_SOURCE = "Sahih Muslim 728 · Jami` at-Tirmidhi 415"
        val TIMES = t(
            "Prayer times are on Home, and you can log your prayers in the Prayer tracker. This guide does not change your prayer-time calculation.",
            "Namaz ke auqaat Home par hain, aur apni namazein Namaz tracker mein darj kar sakte hain. Yeh guide namaz ke auqaat ka hisab nahi badalti.",
            "नमाज़ के औक़ात होम पर हैं, और अपनी नमाज़ें नमाज़ ट्रैकर में दर्ज कर सकते हैं। यह गाइड नमाज़ के औक़ात का हिसाब नहीं बदलती।",
            "نماز کے اوقات ہوم پر ہیں، اور اپنی نمازیں نماز ٹریکر میں درج کر سکتے ہیں۔ یہ گائیڈ نماز کے اوقات کا حساب نہیں بدلتی۔"
        )
        val PRAYER_DETAIL = t(
            "This sequence of Sunnah and Nafl is the Hanafi teaching. Other schools may classify or arrange some of them differently.",
            "Sunnat aur Nafl ki yeh tarteeb Hanafi ta'leem hai. Doosre mazahib mein kuch ki haisiyat ya tarteeb mukhtalif ho sakti hai.",
            "सुन्नत और नफ़्ल की यह तरतीब हनफ़ी तालीम है। दूसरे मज़ाहिब में कुछ की हैसियत या तरतीब मुख़्तलिफ़ हो सकती है।",
            "سنت اور نفل کی یہ ترتیب حنفی تعلیم ہے۔ دوسرے مذاہب میں کچھ کی حیثیت یا ترتیب مختلف ہو سکتی ہے۔"
        )
        val GUIDE = t(
            "This guide teaches the method; have your recitation and postures checked by a qualified teacher. Rulings follow Hanafi fiqh (Bahar-e-Shariat, Hissa 3).",
            "Yeh guide tareeqa sikhati hai; qira'at aur arkaan ki durusti kisi mo'tabar ustaad se karwayein. Ahkaam Hanafi fiqh (Bahar-e-Shariat, Hissa 3) ke mutabiq hain.",
            "यह गाइड तरीक़ा सिखाती है; क़िराअत और अरकान की दुरुस्ती किसी मोतबर उस्ताद से करवाएँ। अहकाम हनफ़ी फ़िक़्ह (बहार-ए-शरीअत, हिस्सा 3) के मुताबिक़ हैं।",
            "یہ گائیڈ طریقہ سکھاتی ہے؛ قراءت اور ارکان کی درستی کسی معتبر استاد سے کروائیں۔ احکام حنفی فقہ (بہارِ شریعت، حصہ 3) کے مطابق ہیں۔"
        )
        val TOPIC = t(
            "Detailed rulings and exceptions depend on your circumstances and school. Ask a reliable Sunni scholar when unsure.",
            "Tafseeli ahkaam aur khaas suratein aap ke halaat aur mazhab par munhasir hain. Shak ho to kisi mo'tabar Sunni aalim se poochhein.",
            "तफ़सीली अहकाम और ख़ास सूरतें आपके हालात और मज़हब पर मुनहसिर हैं। शक हो तो किसी मोतबर सुन्नी आलिम से पूछें।",
            "تفصیلی احکام اور خاص صورتیں آپ کے حالات اور مذہب پر منحصر ہیں۔ شک ہو تو کسی معتبر سنی عالم سے پوچھیں۔"
        )
        val RECITATION = t(
            "Each recitation is shown in Arabic with a reading aid and its meaning. Learn the correct pronunciation from a qualified reciter.",
            "Har cheez Arabi mein, padhne ki madad (Roman) aur ma'ne ke saath di gayi hai. Sahih talaffuz kisi qaari ya ustaad se seekhein.",
            "हर चीज़ अरबी में, पढ़ने की मदद (रोमन) और मानी के साथ दी गई है। सही तलफ़्फ़ुज़ किसी क़ारी या उस्ताद से सीखें।",
            "ہر چیز عربی میں، پڑھنے کی مدد (رومن) اور معنی کے ساتھ دی گئی ہے۔ صحیح تلفظ کسی قاری یا استاد سے سیکھیں۔"
        )
        val RULES = t(
            "The rulings every worshipper should know. Tap a topic to read it.",
            "Woh masail jo har namazi ko maloom hone chahiye. Kisi unwan par tap karke padhein.",
            "वे मसाइल जो हर नमाज़ी को मालूम होने चाहिए। किसी उनवान पर टैप करके पढ़ें।",
            "وہ مسائل جو ہر نمازی کو معلوم ہونے چاہییں۔ کسی عنوان پر ٹیپ کر کے پڑھیں۔"
        )
    }

    private val otherSchoolPrefix = t(
        "The method shown is Hanafi, with notes for your school.",
        "Dikhaya gaya tareeqa Hanafi hai, aap ke mazhab ke liye notes ke saath.",
        "दिखाया गया तरीक़ा हनफ़ी है, आपके मज़हब के लिए नोट्स के साथ।",
        "دکھایا گیا طریقہ حنفی ہے، آپ کے مذہب کے لیے نوٹس کے ساتھ۔"
    )

    val schoolSummaries: Map<Madhhab, SalahText> = mapOf(
        Madhhab.HANAFI to t(
            "The whole guide follows this school. Witr is Wajib (three rak‘at) with Qunut before ruku‘ in the third rak‘ah; Qunut is not ordinarily read in Fajr.",
            "Poori guide isi mazhab ke mutabiq hai. Witr Wajib hai (teen rakat) aur Qunoot teesri rakat mein ruku se pehle; Fajr mein aam taur par Qunoot nahi padhi jati.",
            "पूरी गाइड इसी मज़हब के मुताबिक़ है। वित्र वाजिब है (तीन रकअत) और क़ुनूत तीसरी रकअत में रुकू से पहले; फ़ज्र में आम तौर पर क़ुनूत नहीं पढ़ी जाती।",
            "پوری گائیڈ اسی مذہب کے مطابق ہے۔ وتر واجب ہے (تین رکعت) اور قنوت تیسری رکعت میں رکوع سے پہلے؛ فجر میں عام طور پر قنوت نہیں پڑھی جاتی۔"
        ),
        Madhhab.SHAFII to (otherSchoolPrefix then t(
            "Witr is Sunnah. Qunut is read after ruku‘ in the second rak‘ah of Fajr, and in Witr during the latter half of Ramadan.",
            "Witr Sunnat hai. Qunoot Fajr ki doosri rakat mein ruku ke baad padhi jati hai, aur Witr mein Ramzan ke aakhri nisf mein.",
            "वित्र सुन्नत है। क़ुनूत फ़ज्र की दूसरी रकअत में रुकू के बाद पढ़ी जाती है, और वित्र में रमज़ान के आख़िरी निस्फ़ में।",
            "وتر سنت ہے۔ قنوت فجر کی دوسری رکعت میں رکوع کے بعد پڑھی جاتی ہے، اور وتر میں رمضان کے آخری نصف میں۔"
        )),
        Madhhab.MALIKI to (otherSchoolPrefix then t(
            "Witr is Sunnah, commonly a single rak‘ah after two rak‘at of shaf‘. The details of the Fajr Qunut vary within the school.",
            "Witr Sunnat hai, aam taur par do rakat shaf' ke baad ek rakat. Fajr ki Qunoot ki tafseel mein mazhab ke andar ikhtilaf hai.",
            "वित्र सुन्नत है, आम तौर पर दो रकअत शफ़अ के बाद एक रकअत। फ़ज्र की क़ुनूत की तफ़सील में मज़हब के अंदर इख़्तिलाफ़ है।",
            "وتر سنت ہے، عام طور پر دو رکعت شفع کے بعد ایک رکعت۔ فجر کی قنوت کی تفصیل میں مذہب کے اندر اختلاف ہے۔"
        )),
        Madhhab.HANBALI to (otherSchoolPrefix then t(
            "Witr is Sunnah and may be offered in an odd number of rak‘at. Qunut in Witr is permitted.",
            "Witr Sunnat hai aur taaq rakaton mein padhi ja sakti hai. Witr mein Qunoot jaiz hai.",
            "वित्र सुन्नत है और ताक़ रकअतों में पढ़ी जा सकती है। वित्र में क़ुनूत जायज़ है।",
            "وتر سنت ہے اور طاق رکعتوں میں پڑھی جا سکتی ہے۔ وتر میں قنوت جائز ہے۔"
        ))
    )

    private val beforeFard = t("Before the Fard", "Farz se pehle", "फ़र्ज़ से पहले", "فرض سے پہلے")
    private val theFard = t("The obligatory prayer", "Farz namaz", "फ़र्ज़ नमाज़", "فرض نماز")
    private val afterFard = t("After the Fard", "Farz ke baad", "फ़र्ज़ के बाद", "فرض کے بعد")
    private val afterSunnah = t("After the Sunnah", "Sunnat ke baad", "सुन्नत के बाद", "سنت کے بعد")
    private val afterNafl = t("After the Nafl", "Nafl ke baad", "नफ़्ल के बाद", "نفل کے بعد")
    private val afterWitr = t("After Witr", "Witr ke baad", "वित्र के बाद", "وتر کے بعد")

    val daily: List<DailyPrayer> = listOf(
        DailyPrayer(
            "fajr", t("Fajr", "Fajr", "फ़ज्र", "فجر"),
            listOf(Rakats(2, SalahKind.SUNNAH_MUAKKADAH, beforeFard), Rakats(2, SalahKind.FARD, theFard)),
            t(
                "The two Sunnah before the Fard are the most emphasised Sunnah of the day.",
                "Farz se pehle ki do Sunnatein din bhar ki sab se zyada takeed wali Sunnatein hain.",
                "फ़र्ज़ से पहले की दो सुन्नतें दिन भर की सबसे ज़्यादा ताकीद वाली सुन्नतें हैं।",
                "فرض سے پہلے کی دو سنتیں دن بھر کی سب سے زیادہ تاکید والی سنتیں ہیں۔"
            )
        ),
        DailyPrayer(
            "zuhr", t("Zuhr", "Zuhr", "ज़ुहर", "ظہر"),
            listOf(
                Rakats(4, SalahKind.SUNNAH_MUAKKADAH, beforeFard), Rakats(4, SalahKind.FARD, theFard),
                Rakats(2, SalahKind.SUNNAH_MUAKKADAH, afterFard), Rakats(2, SalahKind.NAFL, afterSunnah)
            ),
            t(
                "Its time begins when the sun declines from its highest point.",
                "Iska waqt sooraj dhalne (Zawal) ke baad shuru hota hai.",
                "इसका वक़्त सूरज ढलने (ज़वाल) के बाद शुरू होता है।",
                "اس کا وقت سورج ڈھلنے (زوال) کے بعد شروع ہوتا ہے۔"
            )
        ),
        DailyPrayer(
            "asr", t("Asr", "Asr", "अस्र", "عصر"),
            listOf(Rakats(4, SalahKind.SUNNAH_GHAIR_MUAKKADAH, beforeFard), Rakats(4, SalahKind.FARD, theFard)),
            t(
                "After the Fard of Asr no Nafl is prayed until sunset.",
                "Asr ke Farz ke baad ghuroob tak koi Nafl nahi padha jata.",
                "अस्र के फ़र्ज़ के बाद ग़ुरूब तक कोई नफ़्ल नहीं पढ़ा जाता।",
                "عصر کے فرض کے بعد غروب تک کوئی نفل نہیں پڑھا جاتا۔"
            )
        ),
        DailyPrayer(
            "maghrib", t("Maghrib", "Maghrib", "मग़रिब", "مغرب"),
            listOf(
                Rakats(3, SalahKind.FARD, theFard), Rakats(2, SalahKind.SUNNAH_MUAKKADAH, afterFard),
                Rakats(2, SalahKind.NAFL, afterSunnah)
            ),
            t(
                "Pray it without delay once the sun has set.",
                "Sooraj doobte hi bina der kiye padhein.",
                "सूरज डूबते ही बिना देर किए पढ़ें।",
                "سورج ڈوبتے ہی بغیر دیر کیے پڑھیں۔"
            )
        ),
        DailyPrayer(
            "isha", t("Isha", "Isha", "इशा", "عشاء"),
            listOf(
                Rakats(4, SalahKind.SUNNAH_GHAIR_MUAKKADAH, beforeFard), Rakats(4, SalahKind.FARD, theFard),
                Rakats(2, SalahKind.SUNNAH_MUAKKADAH, afterFard), Rakats(2, SalahKind.NAFL, afterSunnah),
                Rakats(3, SalahKind.WITR, afterNafl), Rakats(2, SalahKind.NAFL, afterWitr)
            ),
            t(
                "Witr comes after the Fard and Sunnah; it is best as the last prayer of the night.",
                "Witr Farz aur Sunnat ke baad hai; behtar hai ke yeh raat ki aakhri namaz ho.",
                "वित्र फ़र्ज़ और सुन्नत के बाद है; बेहतर है कि यह रात की आख़िरी नमाज़ हो।",
                "وتر فرض اور سنت کے بعد ہے؛ بہتر ہے کہ یہ رات کی آخری نماز ہو۔"
            )
        )
    )

    /** Shown beside the five daily prayers; it replaces Zuhr on Friday. */
    val jumuah = DailyPrayer(
        "jumuah", t("Jumu‘ah", "Jumu'ah", "जुमा", "جمعہ"),
        listOf(
            Rakats(4, SalahKind.SUNNAH_MUAKKADAH, beforeFard), Rakats(2, SalahKind.FARD, theFard),
            Rakats(4, SalahKind.SUNNAH_MUAKKADAH, afterFard), Rakats(2, SalahKind.SUNNAH_GHAIR_MUAKKADAH, afterSunnah),
            Rakats(2, SalahKind.NAFL, afterSunnah)
        ),
        t(
            "It takes the place of Zuhr on Friday for those on whom it is Fard. The two Fard are prayed behind the imam after the khutbah.",
            "Jin par Farz hai unke liye Jumu'ah ke din yeh Zuhr ki jagah hai. Do Farz khutbe ke baad imam ke peeche padhe jate hain.",
            "जिन पर फ़र्ज़ है उनके लिए जुमे के दिन यह ज़ुहर की जगह है। दो फ़र्ज़ ख़ुत्बे के बाद इमाम के पीछे पढ़े जाते हैं।",
            "جن پر فرض ہے ان کے لیے جمعہ کے دن یہ ظہر کی جگہ ہے۔ دو فرض خطبے کے بعد امام کے پیچھے پڑھے جاتے ہیں۔"
        )
    )

    val allPrayers: List<DailyPrayer> = daily + jumuah

    private val niyyahTemplate = t(
        "I intend {count} rak‘at {kind} of {prayer}, for Allah Ta‘ala, facing the Ka‘bah.",
        "Niyyat ki maine {count} rakat namaz {kind}, waqt {prayer} ka, waste Allah Ta'ala ke, munh mera Kaaba sharif ki taraf.",
        "नीयत की मैंने {count} रकअत नमाज़ {kind}, वक़्त {prayer} का, वास्ते अल्लाह तआला के, मुँह मेरा काबा शरीफ़ की तरफ़।",
        "نیت کی میں نے {count} رکعت نماز {kind}، وقت {prayer} کا، واسطے اللہ تعالیٰ کے، منہ میرا کعبہ شریف کی طرف۔"
    )

    /** The customary wording of the intention; it is the resolve of the heart that counts. */
    fun niyyah(prayer: DailyPrayer, unit: Rakats, language: String): String = niyyahTemplate.inLanguage(
        language,
        "count" to unit.count,
        "kind" to unit.kind.title.inLanguage(language),
        "prayer" to prayer.title.inLanguage(language)
    )

    val recitationList: List<Recitation> = salahRecitations
    val recitations: Map<String, Recitation> = salahRecitations.associateBy { it.id }

    /** Purification, the make-up of the prayer and everyday rulings. */
    val rules: List<SalahTopic> = salahRules

    /** Witr, the congregational prayers and the Nawafil. */
    val topics: List<SalahTopic> = salahSpecialPrayers

    val guideSchoolNotes: Map<Madhhab, SalahText> = schoolDifferenceNotes

    /** Every position of one prayer, in order. No guide copy is assembled in composables. */
    fun prayerSteps(
        form: GuideForm,
        madhhab: Madhhab,
        worshipper: Worshipper = Worshipper.MAN,
        prayingAs: PrayingAs = PrayingAs.ALONE
    ): List<GuideStep> = buildPrayerSteps(form, madhhab, worshipper, prayingAs)
}

/** Screen copy of the Salah guide, kept beside its content so all of it is translated together. */
object SalahUi {
    val HERO_TITLE = t("Your prayer guide", "Aap ki namaz guide", "आपकी नमाज़ गाइड", "آپ کی نماز گائیڈ")
    val SCHOOL_LINE = t("{school} school", "{school} mazhab", "{school} मज़हब", "{school} مذہب")
    val CHANGE = t("Change", "Badlein", "बदलें", "بدلیں")
    val CHOOSE_MADHHAB = t("Choose madhhab", "Mazhab chunein", "मज़हब चुनें", "مذہب منتخب کریں")
    val CLOSE = t("Close", "Band karein", "बंद करें", "بند کریں")

    val TAB_DAILY = t("Rak‘at", "Rakatein", "रकअतें", "رکعتیں")
    val TAB_GUIDE = t("How to pray", "Tareeqa", "तरीक़ा", "طریقہ")
    val TAB_RECITATIONS = t("Recitations", "Kya padhein", "क्या पढ़ें", "کیا پڑھیں")
    val TAB_RULES = t("Masail", "Masail", "मसाइल", "مسائل")
    val TAB_MORE = t("More prayers", "Aur namazein", "और नमाज़ें", "مزید نمازیں")

    val RAKAT_SUMMARY = t("{total} rak‘at · {fard} Fard", "{total} rakat · {fard} Farz", "{total} रकअत · {fard} फ़र्ज़", "{total} رکعت · {fard} فرض")
    val RAKAT = t("rak‘at", "rakat", "रकअत", "رکعت")
    val LEARN = t("Learn step by step", "Qadam ba qadam seekhein", "क़दम-ब-क़दम सीखें", "قدم بہ قدم سیکھیں")
    val LEARN_FARD = t("Learn the {count} Fard", "{count} Farz seekhein", "{count} फ़र्ज़ सीखें", "{count} فرض سیکھیں")
    val LEARN_UNIT = t("Learn", "Seekhein", "सीखें", "سیکھیں")
    val NIYYAH = t("Niyyah", "Niyyat", "नीयत", "نیت")

    val GUIDE_TITLE = t("Step-by-step Salah", "Namaz qadam ba qadam", "नमाज़ क़दम-ब-क़दम", "نماز قدم بہ قدم")
    val GUIDE_HINT = t(
        "Choose the prayer, then follow each position in order.",
        "Namaz chunein, phir har rukn tarteeb se dekhein.",
        "नमाज़ चुनें, फिर हर रुक्न तरतीब से देखें।",
        "نماز منتخب کریں، پھر ہر رکن ترتیب سے دیکھیں۔"
    )
    val BEGINNER = t("Beginner mode", "Beginner mode", "बिगिनर मोड", "مبتدی موڈ")
    val BEGINNER_HINT = t("One step at a time", "Ek waqt mein ek qadam", "एक वक़्त में एक क़दम", "ایک وقت میں ایک قدم")
    val STEP_OF = t("Step {n} of {total}", "Qadam {n} / {total}", "क़दम {n} / {total}", "قدم {n} / {total}")
    val RAKAH_N = t("Rak‘ah {n}", "Rakat {n}", "रकअत {n}", "رکعت {n}")
    val PREVIOUS = t("Previous", "Pichhla", "पिछला", "پچھلا")
    val NEXT = t("Next", "Agla", "अगला", "اگلا")
    val START_AGAIN = t("Start again", "Dobara shuru karein", "दोबारा शुरू करें", "دوبارہ شروع کریں")
    val SHOW = t("Show", "Dikhayein", "दिखाएँ", "دکھائیں")
    val HIDE = t("Hide", "Chhupayein", "छुपाएँ", "چھپائیں")

    val QIBLA = t("Qibla", "Qibla", "क़िबला", "قبلہ")
    val QIBLA_HINT = t(
        "Find the direction of the Ka‘bah before you pray.",
        "Namaz se pehle Kaaba ka rukh maloom karein.",
        "नमाज़ से पहले काबा का रुख़ मालूम करें।",
        "نماز سے پہلے کعبہ کا رخ معلوم کریں۔"
    )
    val OPEN = t("Open", "Kholein", "खोलें", "کھولیں")
    val REFERENCE = t("Reference", "Hawala", "हवाला", "حوالہ")
}
