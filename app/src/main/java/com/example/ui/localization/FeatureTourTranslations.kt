package com.example.ui.localization

/** One language's copy for the first-launch feature tour. [steps] are title/body pairs in tour order. */
internal class FeatureTourCopy(
    val skip: String,
    val next: String,
    val done: String,
    val replayTitle: String,
    val steps: List<Pair<String, String>>
)

/**
 * The tour is long-form copy, so it is written as whole sentences per language. Languages not
 * listed show the whole card in English rather than a word-by-word mix.
 */
internal fun featureTourCopy(language: String): FeatureTourCopy =
    featureTourCopies[language] ?: featureTourCopies.getValue("en").let { english ->
        val draft = WorldDraftPhrases.forLanguage(language) + IndicDraftPhrases.forLanguage(language)
        if (draft.isEmpty()) english else FeatureTourCopy(
            skip = draft[english.skip] ?: english.skip,
            next = draft[english.next] ?: english.next,
            done = draft[english.done] ?: english.done,
            replayTitle = draft[english.replayTitle] ?: english.replayTitle,
            steps = english.steps.map { (title, body) ->
                (draft[title] ?: title) to (draft[body] ?: body)
            }
        )
    }

/** The "replay tour" row under More, for [localizeUiText]. */
internal val featureTourLabels: Map<String, Map<String, String>> by lazy {
    featureTourCopies.mapValues { (_, copy) -> mapOf("App tour" to copy.replayTitle) }
}

// Steps 6-10 each describe the four tiles highlighted in More, in the order they appear there.
private val featureTourCopies: Map<String, FeatureTourCopy> = mapOf(
    "en" to FeatureTourCopy(
        skip = "Skip", next = "Next", done = "Get started", replayTitle = "App tour",
        steps = listOf(
            "Assalamu Alaikum" to "Welcome to Hidayah. Take a quick tour of everything the app offers.",
            "Prayer times" to "Home shows today's prayer times for your location, a countdown to the next prayer, and shortcuts to the Qibla direction and prayer alerts.",
            "Quran" to "Read the full Quran, learn it word by word with audio, listen to tafsir and memorise with the Hifz coach.",
            "Duas, Adhkar & Tasbeeh" to "Duas for every occasion, morning and evening adhkar, and a tasbeeh counter are all here.",
            "Masjid" to "See the masjids around you and their jamaat timings, shared by the community.",
            "Namaz & daily goals" to "Learn the method of Namaz, log your prayers and Qaza balance, set a daily Quran goal and work out your Zakat.",
            "Hajj, quiz & more" to "A step-by-step Hajj & Umrah guide, a private Muslimah mode, an Islamic quiz and City challenges.",
            "Qibla, calendar & Hadith" to "Find the Qibla direction, check Hijri dates, see the Sehri and Iftar timetable for Ramzan, and read Hadith.",
            "Names, duas & books" to "The 99 Names of Allah and of the Prophet ﷺ with meanings, the 40 Rabbana duas, and Islamic books.",
            "Library, Naat & Azan" to "Books in many languages in the World Library, Naat without music, Azan from around the world, and Islamic festivals with their aamal.",
            "Settings" to "Language, alerts and prayer time settings are here. Tap App tour to see this guide again any time."
        )
    ),
    "hinglish" to FeatureTourCopy(
        skip = "Skip karein", next = "Aage", done = "Shuru karein", replayTitle = "App ka tour",
        steps = listOf(
            "Assalamu Alaikum" to "Hidayah mein khush aamdeed. Aaiye, app ke saare features ek nazar mein dekh lete hain.",
            "Namaz ke auqaat" to "Home par aapki location ke hisaab se aaj ke namaz ke auqaat, agli namaz ka countdown, aur Qibla aur namaz alert ke shortcuts milte hain.",
            "Quran" to "Poora Quran padhein, audio ke saath lafz-ba-lafz seekhein, tafsir sunein aur Hifz coach ke saath yaad karein.",
            "Duaein, Azkar aur Tasbih" to "Har mauqe ki duaein, subah-shaam ke azkar aur tasbih counter, sab yahan hain.",
            "Masjid" to "Apne aas-paas ki masjidein aur unki jamaat ke auqaat dekhein, jo community ne share kiye hain.",
            "Namaz aur rozana ke goals" to "Namaz ka tareeqa seekhein, apni namazein aur Qaza ka hisaab rakhein, rozana ka Quran goal banayein aur Zakat nikalein.",
            "Hajj, quiz aur bahut kuch" to "Hajj aur Umrah ki step-by-step guide, private Muslimah mode, Islamic quiz aur City challenges.",
            "Qibla, calendar aur Hadith" to "Qibla ki direction dekhein, Hijri tareekh dekhein, Ramzan ke Sehri-Iftar ke auqaat dekhein aur Hadith padhein.",
            "Naam, duaein aur kitabein" to "Allah ke 99 naam aur Nabi ﷺ ke 99 naam maani ke saath, 40 Rabbana duaein aur Islami kitabein.",
            "Library, Naat aur Azan" to "World Library mein kai zabaanon ki kitabein, bina music ki Naat, duniya bhar ki Azan, aur Islami tyohar unke aamal ke saath.",
            "Settings" to "Language, alerts aur namaz ke waqt ki settings yahan hain. Yeh guide dobara dekhne ke liye App ka tour dabayein."
        )
    ),
    "hi" to FeatureTourCopy(
        skip = "छोड़ें", next = "आगे", done = "शुरू करें", replayTitle = "ऐप का टूर",
        steps = listOf(
            "अस्सलामु अलैकुम" to "हिदायह में आपका स्वागत है। आइए, ऐप की सारी सुविधाएँ एक नज़र में देख लेते हैं।",
            "नमाज़ के समय" to "होम पर आपकी लोकेशन के हिसाब से आज की नमाज़ों के समय, अगली नमाज़ का काउंटडाउन, और क़िबला व नमाज़ की सूचना के शॉर्टकट मिलते हैं।",
            "क़ुरआन" to "पूरा क़ुरआन पढ़ें, ऑडियो के साथ शब्द-दर-शब्द सीखें, तफ़सीर सुनें और हिफ़्ज़ कोच के साथ याद करें।",
            "दुआएँ, अज़कार और तस्बीह" to "हर मौक़े की दुआएँ, सुबह-शाम के अज़कार और तस्बीह काउंटर, सब यहाँ हैं।",
            "मस्जिद" to "अपने आस-पास की मस्जिदें और उनकी जमाअत के समय देखें, जो कम्युनिटी ने साझा किए हैं।",
            "नमाज़ और रोज़ाना के लक्ष्य" to "नमाज़ का तरीक़ा सीखें, अपनी नमाज़ों और क़ज़ा का हिसाब रखें, रोज़ाना का क़ुरआन लक्ष्य बनाएँ और ज़कात निकालें।",
            "हज, क्विज़ और बहुत कुछ" to "हज और उमरा की क़दम-दर-क़दम गाइड, निजी मुस्लिमा मोड, इस्लामी क्विज़ और सिटी चैलेंज।",
            "क़िबला, कैलेंडर और हदीस" to "क़िबला की दिशा देखें, हिजरी तारीख़ें देखें, रमज़ान के सहरी-इफ़्तार के समय देखें और हदीस पढ़ें।",
            "नाम, दुआएँ और किताबें" to "अल्लाह के 99 नाम और नबी ﷺ के 99 नाम अर्थ के साथ, 40 रब्बना दुआएँ और इस्लामी किताबें।",
            "लाइब्रेरी, नात और अज़ान" to "वर्ल्ड लाइब्रेरी में कई भाषाओं की किताबें, बिना संगीत की नात, दुनिया भर की अज़ान, और इस्लामी त्योहार उनके आमाल के साथ।",
            "सेटिंग्स" to "भाषा, सूचनाएँ और नमाज़ के समय की सेटिंग्स यहाँ हैं। यह गाइड दोबारा देखने के लिए ऐप का टूर दबाएँ।"
        )
    ),
    "ur" to FeatureTourCopy(
        skip = "چھوڑیں", next = "آگے", done = "شروع کریں", replayTitle = "ایپ کا تعارف",
        steps = listOf(
            "السلام علیکم" to "ہدایہ میں خوش آمدید۔ آئیے، ایپ کی تمام سہولیات ایک نظر میں دیکھ لیتے ہیں۔",
            "نماز کے اوقات" to "ہوم پر آپ کے مقام کے مطابق آج کی نمازوں کے اوقات، اگلی نماز تک باقی وقت، اور قبلہ اور نماز کی اطلاع کے شارٹ کٹ ملتے ہیں۔",
            "قرآن" to "پورا قرآن پڑھیں، آڈیو کے ساتھ لفظ بہ لفظ سیکھیں، تفسیر سنیں اور حفظ کوچ کے ساتھ یاد کریں۔",
            "دعائیں، اذکار اور تسبیح" to "ہر موقع کی دعائیں، صبح و شام کے اذکار اور تسبیح کاؤنٹر، سب یہاں ہیں۔",
            "مسجد" to "اپنے آس پاس کی مساجد اور ان کی جماعت کے اوقات دیکھیں، جو کمیونٹی نے شیئر کیے ہیں۔",
            "نماز اور روزانہ کے اہداف" to "نماز کا طریقہ سیکھیں، اپنی نمازوں اور قضا کا حساب رکھیں، روزانہ کا قرآن ہدف بنائیں اور زکوٰۃ نکالیں۔",
            "حج، کوئز اور بہت کچھ" to "حج و عمرہ کی قدم بہ قدم رہنمائی، نجی مسلمہ موڈ، اسلامی کوئز اور سٹی چیلنجز۔",
            "قبلہ، کیلنڈر اور حدیث" to "قبلہ کی سمت دیکھیں، ہجری تاریخیں دیکھیں، رمضان کے سحری و افطار کے اوقات دیکھیں اور حدیث پڑھیں۔",
            "نام، دعائیں اور کتابیں" to "اللہ کے 99 نام اور نبی ﷺ کے 99 نام معانی کے ساتھ، 40 ربنا دعائیں اور اسلامی کتابیں۔",
            "لائبریری، نعت اور اذان" to "ورلڈ لائبریری میں کئی زبانوں کی کتابیں، بغیر موسیقی کی نعت، دنیا بھر کی اذان، اور اسلامی تہوار ان کے اعمال کے ساتھ۔",
            "ترتیبات" to "زبان، اطلاعات اور نماز کے اوقات کی ترتیبات یہاں ہیں۔ یہ رہنمائی دوبارہ دیکھنے کے لیے ایپ کا تعارف دبائیں۔"
        )
    ),
    "bn" to FeatureTourCopy(
        skip = "এড়িয়ে যান", next = "পরবর্তী", done = "শুরু করুন", replayTitle = "অ্যাপ পরিচিতি",
        steps = listOf(
            "আসসালামু আলাইকুম" to "হিদায়াহ-তে আপনাকে স্বাগতম। আসুন, অ্যাপের সব সুবিধা এক নজরে দেখে নিই।",
            "নামাজের সময়" to "হোমে আপনার অবস্থান অনুযায়ী আজকের নামাজের সময়, পরবর্তী নামাজের কাউন্টডাউন, এবং কিবলা ও নামাজের সতর্কতার শর্টকাট পাবেন।",
            "কুরআন" to "সম্পূর্ণ কুরআন পড়ুন, অডিওসহ শব্দে শব্দে শিখুন, তাফসির শুনুন এবং হিফজ কোচের সাথে মুখস্থ করুন।",
            "দোয়া, জিকির ও তাসবিহ" to "প্রতিটি উপলক্ষের দোয়া, সকাল-সন্ধ্যার জিকির এবং তাসবিহ কাউন্টার, সবই এখানে।",
            "মসজিদ" to "আপনার আশপাশের মসজিদ এবং কমিউনিটির শেয়ার করা জামাতের সময় দেখুন।",
            "নামাজ ও দৈনিক লক্ষ্য" to "নামাজের নিয়ম শিখুন, আপনার নামাজ ও কাজার হিসাব রাখুন, দৈনিক কুরআন লক্ষ্য ঠিক করুন এবং জাকাত হিসাব করুন।",
            "হজ, কুইজ ও আরও" to "হজ ও উমরাহর ধাপে ধাপে গাইড, ব্যক্তিগত মুসলিমা মোড, ইসলামি কুইজ এবং সিটি চ্যালেঞ্জ।",
            "কিবলা, ক্যালেন্ডার ও হাদিস" to "কিবলার দিক দেখুন, হিজরি তারিখ দেখুন, রমজানের সেহরি-ইফতারের সময় দেখুন এবং হাদিস পড়ুন।",
            "নাম, দোয়া ও বই" to "অর্থসহ আল্লাহর ৯৯ নাম ও নবী ﷺ-এর ৯৯ নাম, ৪০টি রাব্বানা দোয়া এবং ইসলামি বই।",
            "লাইব্রেরি, নাত ও আজান" to "ওয়ার্ল্ড লাইব্রেরিতে নানা ভাষার বই, বাদ্যযন্ত্র ছাড়া নাত, বিশ্বের আজান, এবং আমলসহ ইসলামি উৎসব।",
            "সেটিংস" to "ভাষা, সতর্কতা ও নামাজের সময়ের সেটিংস এখানে। এই গাইড আবার দেখতে অ্যাপ পরিচিতি-তে চাপ দিন।"
        )
    ),
    "mr" to FeatureTourCopy(
        skip = "वगळा", next = "पुढे", done = "सुरू करा", replayTitle = "ॲपची ओळख",
        steps = listOf(
            "अस्सलामु अलैकुम" to "हिदायहमध्ये आपले स्वागत आहे. चला, ॲपची सर्व वैशिष्ट्ये एका नजरेत पाहूया.",
            "नमाजाच्या वेळा" to "होमवर तुमच्या ठिकाणानुसार आजच्या नमाजाच्या वेळा, पुढील नमाजाचे काउंटडाउन, आणि किब्ला व नमाजाच्या सूचनेचे शॉर्टकट मिळतात.",
            "कुरआन" to "संपूर्ण कुरआन वाचा, ऑडिओसह शब्दशः शिका, तफसीर ऐका आणि हिफ्ज कोचसोबत पाठ करा.",
            "दुआ, अजकार आणि तस्बीह" to "प्रत्येक प्रसंगाच्या दुआ, सकाळ-संध्याकाळचे अजकार आणि तस्बीह काउंटर, सर्व येथे आहे.",
            "मशीद" to "तुमच्या जवळच्या मशिदी आणि कम्युनिटीने शेअर केलेल्या जमाअतच्या वेळा पाहा.",
            "नमाज आणि रोजची ध्येये" to "नमाजाची पद्धत शिका, तुमच्या नमाजांचा आणि कजाचा हिशोब ठेवा, रोजचे कुरआन ध्येय ठरवा आणि जकात काढा.",
            "हज, क्विझ आणि बरेच काही" to "हज व उमराचे टप्प्याटप्प्याने मार्गदर्शन, खाजगी मुस्लिमा मोड, इस्लामी क्विझ आणि सिटी चॅलेंज.",
            "किब्ला, कॅलेंडर आणि हदीस" to "किब्लाची दिशा पाहा, हिजरी तारखा पाहा, रमजानच्या सहरी-इफ्तारच्या वेळा पाहा आणि हदीस वाचा.",
            "नावे, दुआ आणि पुस्तके" to "अर्थासह अल्लाहची ९९ नावे आणि नबी ﷺ यांची ९९ नावे, ४० रब्बना दुआ आणि इस्लामी पुस्तके.",
            "लायब्ररी, नात आणि अजान" to "वर्ल्ड लायब्ररीमध्ये अनेक भाषांतील पुस्तके, संगीताशिवाय नात, जगभरातील अजान, आणि आमालसह इस्लामी सण.",
            "सेटिंग्ज" to "भाषा, सूचना आणि नमाजाच्या वेळेची सेटिंग्ज येथे आहेत. हे मार्गदर्शन पुन्हा पाहण्यासाठी ॲपची ओळख दाबा."
        )
    ),
    "gu" to FeatureTourCopy(
        skip = "છોડો", next = "આગળ", done = "શરૂ કરો", replayTitle = "ઍપનો પરિચય",
        steps = listOf(
            "અસ્સલામુ અલૈકુમ" to "હિદાયહમાં આપનું સ્વાગત છે. ચાલો, ઍપની બધી સુવિધાઓ એક નજરમાં જોઈ લઈએ.",
            "નમાઝના સમય" to "હોમ પર તમારા સ્થળ મુજબ આજની નમાઝના સમય, આગલી નમાઝનું કાઉન્ટડાઉન, અને કિબ્લા તથા નમાઝની સૂચનાના શૉર્ટકટ મળે છે.",
            "કુરઆન" to "આખું કુરઆન વાંચો, ઑડિયો સાથે શબ્દ-દર-શબ્દ શીખો, તફસીર સાંભળો અને હિફ્ઝ કોચ સાથે યાદ કરો.",
            "દુઆઓ, અઝકાર અને તસ્બીહ" to "દરેક પ્રસંગની દુઆઓ, સવાર-સાંજના અઝકાર અને તસ્બીહ કાઉન્ટર, બધું અહીં છે.",
            "મસ્જિદ" to "તમારી આસપાસની મસ્જિદો અને કમ્યુનિટીએ શેર કરેલા જમાઅતના સમય જુઓ.",
            "નમાઝ અને રોજના લક્ષ્ય" to "નમાઝની રીત શીખો, તમારી નમાઝો અને કઝાનો હિસાબ રાખો, રોજનું કુરઆન લક્ષ્ય નક્કી કરો અને ઝકાત ગણો.",
            "હજ, ક્વિઝ અને ઘણું બધું" to "હજ અને ઉમરાહની પગલાવાર માર્ગદર્શિકા, ખાનગી મુસ્લિમા મોડ, ઇસ્લામી ક્વિઝ અને સિટી ચેલેન્જ.",
            "કિબ્લા, કૅલેન્ડર અને હદીસ" to "કિબ્લાની દિશા જુઓ, હિજરી તારીખો જુઓ, રમઝાનના સહેરી-ઇફ્તારના સમય જુઓ અને હદીસ વાંચો.",
            "નામ, દુઆઓ અને પુસ્તકો" to "અર્થ સાથે અલ્લાહના ૯૯ નામ અને નબી ﷺ ના ૯૯ નામ, ૪૦ રબ્બના દુઆઓ અને ઇસ્લામી પુસ્તકો.",
            "લાઇબ્રેરી, નાત અને અઝાન" to "વર્લ્ડ લાઇબ્રેરીમાં ઘણી ભાષાઓના પુસ્તકો, સંગીત વગરની નાત, દુનિયાભરની અઝાન, અને આમાલ સાથે ઇસ્લામી તહેવારો.",
            "સેટિંગ્સ" to "ભાષા, સૂચનાઓ અને નમાઝના સમયની સેટિંગ્સ અહીં છે. આ માર્ગદર્શિકા ફરી જોવા ઍપનો પરિચય દબાવો."
        )
    ),
    "ar" to FeatureTourCopy(
        skip = "تخطٍّ", next = "التالي", done = "ابدأ", replayTitle = "جولة في التطبيق",
        steps = listOf(
            "السلام عليكم" to "مرحبًا بك في هداية. خذ جولة سريعة على كل ما يقدمه التطبيق.",
            "أوقات الصلاة" to "تعرض الصفحة الرئيسية أوقات صلاة اليوم حسب موقعك، والوقت المتبقي للصلاة القادمة، واختصارات لاتجاه القبلة وتنبيهات الصلاة.",
            "القرآن" to "اقرأ القرآن كاملًا، وتعلّمه كلمة بكلمة مع الصوت، واستمع إلى التفسير، واحفظ مع مساعد الحفظ.",
            "الأدعية والأذكار والتسبيح" to "أدعية لكل مناسبة، وأذكار الصباح والمساء، وعدّاد التسبيح، كلها هنا.",
            "المسجد" to "تعرّف على المساجد القريبة منك وأوقات الجماعة فيها كما شاركها المجتمع.",
            "الصلاة والأهداف اليومية" to "تعلّم صفة الصلاة، وسجّل صلواتك وما عليك من قضاء، وحدّد هدفًا يوميًا للقرآن، واحسب زكاتك.",
            "الحج والمسابقة وغيرها" to "دليل الحج والعمرة خطوة بخطوة، ووضع خاص بالمسلمة، ومسابقة إسلامية، وتحديات المدينة.",
            "القبلة والتقويم والحديث" to "اعرف اتجاه القبلة، وراجع التواريخ الهجرية، واطّلع على مواعيد السحور والإفطار في رمضان، واقرأ الحديث.",
            "الأسماء والأدعية والكتب" to "أسماء الله الحسنى وأسماء النبي ﷺ مع معانيها، وأربعون دعاءً من أدعية «ربنا»، وكتب إسلامية.",
            "المكتبة والمدائح والأذان" to "كتب بلغات كثيرة في المكتبة العالمية، ومدائح نبوية بلا موسيقى، وأذان من أنحاء العالم، والمناسبات الإسلامية مع أعمالها.",
            "الإعدادات" to "إعدادات اللغة والتنبيهات وأوقات الصلاة هنا. اضغط على جولة في التطبيق لمشاهدة هذا الدليل مرة أخرى."
        )
    )
)
