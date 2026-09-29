package com.example.ui.localization

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text as MaterialText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.example.prayer.HijriCalendarHelper
import java.util.Locale

/** The single language source used by every screen. Arabic-script devotional text is preserved. */
val LocalAppLanguage = compositionLocalOf { "en" }
val LocalAppLocale = compositionLocalOf { appLocale("en") }

/**
 * Converts the app's persisted language id to a real BCP-47 locale.
 * `hinglish` is an app-only id, so it must not be passed directly to Android.
 */
fun appLocale(language: String): Locale = Locale.forLanguageTag(
    when (language) {
        "hinglish" -> "hi-Latn-IN"
        "en" -> "en-IN"
        else -> "$language-IN"
    }
)

fun isRtlAppLanguage(language: String): Boolean = language in setOf("ur", "ks", "sd", "ar", "fa", "ps", "he")

/** Preserve the established month name for LTR locales and Arabic script for RTL locales. */
fun formatHijriDate(date: HijriCalendarHelper.HijriDate, language: String): String {
    val month = if (isRtlAppLanguage(language)) date.monthNameArabic else date.monthNameEnglish
    val yearSuffix = if (isRtlAppLanguage(language)) "هـ" else "AH"
    return "${date.day} $month ${date.year} $yearSuffix"
}

/**
 * Returns a Context whose resources, date/number formatters and layout direction all use the
 * selected app language. Supplying only a Compose Configuration leaves string resources and
 * Android-backed UI on the device locale, which is why the old language switch was inconsistent.
 */
fun Context.withAppLocale(language: String): Context {
    val locale = appLocale(language)
    val configuration = Configuration(resources.configuration).apply {
        setLocale(locale)
        setLayoutDirection(locale)
    }
    val localizedResources = createConfigurationContext(configuration).resources

    // Keep the original Activity in the ContextWrapper chain. Compose discovers owners such as
    // ActivityResultRegistryOwner (permission launchers), OnBackPressedDispatcherOwner and
    // lifecycle owners through that chain. Returning createConfigurationContext() directly
    // disconnects those owners and crashes as soon as rememberLauncherForActivityResult is used.
    return AppLocaleContextWrapper(this, localizedResources)
}

private class AppLocaleContextWrapper(
    baseContext: Context,
    private val localizedResources: Resources
) : ContextWrapper(baseContext) {
    override fun getResources(): Resources = localizedResources
    override fun getAssets() = localizedResources.assets
}

/** Localizes non-Text UI such as icon semantics, field labels and accessibility descriptions. */
@Composable
fun localizedUiText(text: String): String = localizeUiText(text, LocalAppLanguage.current)

private val hindiPhrases = linkedMapOf(
    "Settings & Privacy" to "सेटिंग्स और गोपनीयता",
    "App Language" to "ऐप की भाषा", "Choose language" to "भाषा चुनें",
    "Choose app language" to "ऐप की भाषा चुनें", "Close" to "बंद करें",
    "Home" to "होम", "Ramzan" to "रमज़ान", "Quran" to "क़ुरआन", "Masjid" to "मस्जिद", "Community Masjid" to "कम्युनिटी मस्जिद",
    "Duas" to "दुआएँ", "Festivals" to "त्योहार", "Settings" to "सेटिंग्स",
    "Prayer Calculation Settings" to "नमाज़ के समय की सेटिंग्स",
    "Calculation Authority" to "गणना पद्धति", "Asr Juristic Calculation" to "असर की गणना पद्धति",
    "Hijri Moon Sighting Adjustment" to "हिजरी चाँद समायोजन",
    "Prayer Notifications Service" to "नमाज़ सूचना सेवा", "Enable Prayer Alerts" to "नमाज़ की सूचनाएँ चालू करें",
    "Privacy & Offline Architecture" to "गोपनीयता और ऑफ़लाइन व्यवस्था",
    "Content Governance & Sources" to "सामग्री और स्रोत",
    "Today's Prayer Schedule" to "आज की नमाज़ का समय", "Calculated Salah Start" to "नमाज़ शुरू होने का समय",
    "Explore The Noble Quran" to "पवित्र क़ुरआन पढ़ें", "Daily Adhkar" to "रोज़ाना अज़कार",
    "Morning & Evening" to "सुबह और शाम", "Qibla Direction" to "क़िबला की दिशा",
    "Major Cities" to "प्रमुख शहर", "Custom Lat / Long" to "अपना अक्षांश / देशांतर",
    "Lessons" to "पाठ", "AI Assistant" to "एआई सहायक", "Read" to "पढ़ें", "Audio" to "ऑडियो",
    "Surah wise" to "सूरह के अनुसार", "Para wise" to "पारे के अनुसार",
    "Morning" to "सुबह", "Evening" to "शाम", "Done" to "पूरा",
    "Gentle Digital Tasbeeh" to "डिजिटल तस्बीह", "Goal" to "लक्ष्य",
    "Verified Timetables" to "सत्यापित समय-सारणी", "Congregational Iqamah Timings" to "जमाअत की इक़ामत के समय",
    "Friday Jummah Sessions" to "जुमा की नमाज़ें", "Suggest Edit" to "सुधार सुझाएँ",
    "Why This Time?" to "यह समय क्यों?", "Distance to Kaaba" to "काबा से दूरी",
    "Magnetic Sensor Calibration" to "चुंबकीय सेंसर कैलिब्रेशन", "Ramzan Timetable" to "रमज़ान समय-सारणी",
    "Sehri" to "सहरी", "Iftar" to "इफ़्तार", "Online dates" to "ऑनलाइन तारीख़ें",
    "Prayer Times Alerts" to "नमाज़ के समय की सूचनाएँ",
    "Peaceful notifications when each prayer time enters" to "हर नमाज़ का समय शुरू होने पर शांत सूचना",
    "Time for" to "समय हो गया:", "Salah has commenced for" to "नमाज़ शुरू हो गई है—स्थान:",
    "Salah time has commenced at" to "नमाज़ का समय शुरू हो गया:",
    "Indeed, prayer has been decreed upon the believers a decree of specified times." to "निस्संदेह, ईमान वालों पर नमाज़ निश्चित समय पर फ़र्ज़ की गई है।",
    "The selected language applies across the app." to "चुनी हुई भाषा पूरे ऐप में लागू होती है।",
    "Retry" to "फिर से कोशिश करें",
    "The Essentials of Wudu (Ablution)" to "वुज़ू (अब्लूशन) के मूल तत्व",
    "Understand the 4 Quranic obligations of Wudu and common authentic prophetic sunnahs." to "वुज़ू के चार क़ुरानी अनिवार्यताओं और आम प्रामाणिक पैग़ंबरी सुन्नतों को समझें।",
    """
                Wudu is the spiritual and physical purification required before standing in prayer.

                The Quran establishes four obligatory pillars (Fara'id) in Surah Al-Ma'idah (5:6):
                1. Washing the face from forehead to chin and ear to ear.
                2. Washing both arms including the elbows.
                3. Wiping at least a quarter of the head with wet hands (Hanafi; Sunnat is wiping the whole head).
                4. Washing both feet up to and including the ankles.

                Prophetic Sunnahs (Recommended Practices):
                - Starting with Bismillah and intention (Niyyah) in the heart.
                - Washing the hands thrice up to wrists.
                - Rinsing the mouth (Madmadah) and cleaning the nose (Istinshaq) thrice.
                - Running wet fingers through the beard and between fingers and toes (Takhlil).
                - Maintaining sequential order (Tartib) without prolonged interruption (Muwalat).
            """.trimIndent() to
        "वुज़ू नमाज़ के लिए आवश्यक आध्यात्मिक और शारीरिक शुद्धि है।\n\nक़ुरान ने सूरह अल-माएदा (5:6) में चार अनिवार्य स्तंभ (फराएद) स्थापित किए हैं:\n1. माथे से ठोड़ी तक और कान से कान तक चेहरा धोना।\n2. दोनों बाहें कोहनी सहित धोना।\n3. गीले हाथों से कम से कम सिर के एक चौथाई हिस्से को पोंछना (हनफ़ी; सुन्नत पूरे सिर को पोंछना है)।\n4. दोनों पैर टखनों सहित धोना।\n\nमुस्तहब सुन्नतें (सुझाई गई प्रथाएं):\n- बिस्मिल्लाह और दिल में नीयत (इरादा) के साथ शुरू करना।\n- हाथों को कलाई तक तीन बार धोना।\n- कुल्ली करना (मदमादह) और नाक साफ़ करना (इस्तिनशाक़) तीन बार।\n- दाढ़ी और उंगलियों व पैर की उंगलियों के बीच गीली उंगलियां चलाना (तख़लील)।\n- क्रमिक क्रम (तर्तीब) बनाए रखना बिना लंबे रुकावट (मुवालात) के।",
    "Do not waste water even if by a flowing stream." to "बहती नदी के पास भी पानी बर्बाद न करें।",
    "Ensure dry spots on heels and elbows are reached." to "सुनिश्चित करें कि एड़ियों और कोहनियों के सूखे स्थान तक पानी पहुँचे।",
    "Recite the Kalimah Shahadah and supplication after finishing." to "वुज़ू पूरा करने के बाद कलिमा शहादत और दुआ पढ़ें।",
    "Attaining Khushu' (Tranquility) in Prayer" to "नमाज़ में ख़ुशू (शांति) प्राप्त करना",
    "How to calm a wandering mind and feel divine connection during your daily prayers." to "रोज़ मर्रा की नमाज़ों में भटकते हुए मन को शांत करके अल्लाह से जुड़ाव कैसे महसूस करें।",
    """
                Khushu' is inner humility, stillness of the limbs, and attentive contemplation of what one is reciting.

                Practical Anchors for Khushu':
                - Prepare before the Takbir: Perform wudu mindfully, arrive before iqamah, and eliminate immediate physical distractions.
                - Contemplate the Words: Recite slowly (Tartil). When saying 'Al-Hamdu lillahi Rabbil-Alameen', remember Allah responds directly: 'My servant has praised Me' (Sahih Muslim 395).
                - Fix the Gaze: Direct your gaze respectfully at the place of prostration (Sujud), rather than looking around.
                - Remember the Final Prayer: The Prophet (peace be upon him) advised: 'Pray as if it were your farewell prayer' (Sunan Ibn Majah 4171).
            """.trimIndent() to
        "ख़ुशू अंतरंग विनम्रता, अंगों की स्थिरता और जो पढ़ा जा रहा है उसके प्रति सावधान चिंतन है।\n\nख़ुशू के लिए व्यावहारिक आधार:\n- तकबीर से पहले तैयारी: ध्यान से वुज़ू करें, इक़ामत से पहले पहुंचें और तत्काल शारीरिक व्यस्तताओं को दूर करें।\n- शब्दों पर चिंतन: धीरे-धीरे पढ़ें (तर्तील)। 'अल-हम्दु लिल्लाहि रब्बिल-अलमीन' कहते समय याद रखें कि अल्लाह सीधे जवाब देते हैं: 'मेरे बंदे ने मेरी प्रशंसा की' (सही मुस्लिम 395)।\n- दृष्टि को स्थिर करें: सम्मान से सजदे की जगह पर अपनी दृष्टि रखें, चारों ओर न देखें।\n- अंतिम नमाज़ को याद रखें: पैग़ंबर (शांति उनके ऊपर हो) ने सलाह दी: 'ऐसे नमाज़ पढ़ो जैसे यह तुम्हारी आखिरी नमाज़ हो' (सुनन इब्न माजह 4171)।",
    "Pause for 2 seconds after each ayah before reciting the next." to "हर आयत के बाद अगली आयत पढ़ने से पहले 2 सेकंड रुकें।",
    "Vary the short surahs recited in Salah to prevent mechanical repetition." to "नमाज़ में पढ़ी जाने वाली छोटी सूरों को यांत्रिक दोहराव से बचने के लिए बदलते रहें।",
    "Take a deep breath and consciously let go of worldly tasks before saying 'Allahu Akbar'." to "'अल्लाहु अकबर' कहने से पहले गहरी सांस लें और सांसारिक कामों को जागरूकता से छोड़ दें।",
    "Rights of Neighbors in Islam" to "इस्लाम में पड़ोसियों के अधिकार",
    "The high ethical status of neighbors regardless of their faith or background." to "पड़ोसी की उच्च नैतिक स्थिति, उनके धर्म या पृष्ठभूमि की परवाह किए बिना।",
    """
                The Prophet Muhammad (peace be upon him) said: 'Jibril kept enjoining good treatment of the neighbor upon me until I thought he would assign him a share of inheritance.' (Sahih al-Bukhari 6014).

                Islamic teachings identify three classes of neighbors:
                1. A neighbor who is a relative and Muslim: has three rights (kinship, Islam, neighborhood).
                2. A neighbor who is Muslim: has two rights (Islam and neighborhood).
                3. A neighbor of different faith: has the sacred right of good neighborhood and kindness.

                Harm to neighbors is strictly condemned: 'By Allah he does not believe... whose neighbor is not safe from his harm.' (Sahih al-Bukhari 6016).
            """.trimIndent() to
        "पैग़ंबर मुहम्मद (शांति उनके ऊपर हो) ने कहा: 'जिब्रील मुझे पड़ोसी के साथ अच्छा व्यवहार करने की निरंतर सलाह देते रहे यहां तक कि मैंने सोचा कि वे उसे विरासत में हिस्सा दे देंगे।' (सही अल-बुख़ारी 6014)।\n\nइस्लामी शिक्षाएं पड़ोसियों के तीन वर्गों को चिह्नित करती हैं:\n1. एक पड़ोसी जो रिश्तेदार और मुसलमान है: के तीन अधिकार (रिश्तेदारी, इस्लाम, पड़ोस)।\n2. एक पड़ोसी जो मुसलमान है: के दो अधिकार (इस्लाम और पड़ोस)।\n3. भिन्न धर्म का एक पड़ोसी: अच्छे पड़ोस और दया का पवित्र अधिकार है।\n\nपड़ोसियों को नुकसान पहुंचाना कड़ाई से निंदित है: 'अल्लाह की कसम, वह ईमान नहीं रखता... जिसका पड़ोसी उसके नुकसान से सुरक्षित नहीं है।' (सही अल-बुख़ारी 6016)।",
    "Share homemade food or fruit occasionally." to "कभी-कभार घर का बना खाना या फल पड़ोसियों के साथ शेयर करें।",
    "Keep noise levels respectful during late night and early mornings." to "रात को देर से और सुबह जल्दी शोर के स्तर को सम्मानजनक रखें।",
    "Help senior neighbors with groceries or physical tasks." to "बुज़ुर्ग पड़ोसियों को किराने की दुकान या शारीरिक कामों में मदद करें।",
    "The Noble Quran, Surah Al-Ma'idah (5:6)" to "महान क़ुरान, सूरह अल-माएदा (5:6)",
    "Sahih al-Bukhari 159 (Hadith of Uthman describing Prophet's wudu)" to "सही अल-बुख़ारी 159 (हज़रत उस्मान (रज़ि.) की रिवायत जिसमें पैग़ंबर के वुज़ू का वर्णन है)",
    "Al-Hidaya, Kitab al-Taharah (Imam Marghinani, Hanafi)" to "अल-हिदायह, किताब अत-तहारह (इमाम मार्गिनानी, हनफ़ी)",
    "Surah Al-Mu'minun (23:1-2): 'Successful indeed are the believers who are humble in their prayers.'" to "सूरह अल-मोमिनून (23:1-2): 'निस्संदेह, सफल हैं ईमानवाले जो अपनी नमाज़ों में विनम्र हैं।'",
    "Ihya Ulum al-Din, Kitab Asrar al-Salah (Imam Ghazali)" to "इह्या उलूम अद-दीन, किताब असरार अस-सलाह (इमाम ग़ज़ाली)",
    "Sahih Muslim 395 (Hadith Qudsi on Surat al-Fatihah)" to "सही मुस्लिम 395 (सूरह अल-फातिहा के बारे में हदीस क़ुद्सी)",
    "Sahih al-Bukhari 6014, 6016" to "सही अल-बुख़ारी 6014, 6016",
    "Sahih Muslim 2625 (Abu Dharr on adding water to broth to share with neighbors)" to "सही मुस्लिम 2625 (हज़रत अबूधर (रज़ि.) का वाक़िया शोरबे में पानी मिलाकर पड़ोसियों को देने का)",
    "Adab al-Mufrad (Imam al-Bukhari)" to "अदाब अल-मुफ़रद (इमाम अल-बुख़ारी)",
    "Islamic jurisprudence regarding marital dissolution (talaq), complex inheritance, and personal legal verdicts requires direct review of both parties' detailed circumstances by a qualified local mufti or Islamic scholar." to "तलाक़, जटिल विरासत और व्यक्तिगत शरई फैसलों के संबंध में इस्लामी न्यायशास्त्र को एक योग्य स्थानीय मुफ़्ती या इस्लामी विद्वान द्वारा दोनों पक्षों की विस्तृत परिस्थितियों की सीधी समीक्षा की आवश्यकता है।",
    "Legal verdicts in personal disputes cannot be issued by automated systems or general textbooks, as individual statements, intentions, and local contracts directly impact the legal validity." to "व्यक्तिगत विवादों में शरई फैसले स्वचालित प्रणालियों या सामान्य पाठ्यपुस्तकों द्वारा जारी नहीं किए जा सकते, क्योंकि व्यक्तिगत बयान, इरादे और स्थानीय अनुबंध शरई वैधता को सीधे प्रभावित करते हैं।",
    "In Islam, patience (Sabr) is coupled directly with prayer (Salah) as the believers' active fortress in moments of hardship. Rather than passive endurance, Sabr means emotional resilience, avoiding panic, and maintaining trust in Allah's wisdom." to "इस्लाम में, धैर्य (सब्र) को नमाज़ (सलाह) के साथ सीधे जोड़ा गया है, मुसलमानों का सक्रिय क़िला कठिन समय में। सब्र निष्क्रिय सहन करना नहीं बल्कि भावनात्मक लचीलापन, घबराहट से बचना और अल्लाह की बुद्धिमत्ता पर विश्वास बनाए रखना है।",
    "Ibn Kathir notes in his Tafsir: When heavy trials occur, two aids support the human being: restraining the soul from forbidden despair (patience) and turning to the coolness of the eyes in prayer." to "इब्न कसीर अपनी तफ़सीर में नोट करते हैं: जब भारी परीक्षाएं आती हैं, तो दो चीज़ें मनुष्य का सहारा देती हैं: नफ़्स को हराम नाउम्मीदी से रोकना (धैर्य) और नमाज़ में आंखों की ठंडक की ओर मुड़ना।",
    "Scholars emphasize three types of Sabr: patience upon acts of obedience, patience against committing sins, and patience with painful decrees." to "विद्वान सब्र की तीन किस्मों पर जोर देते हैं: आज्ञाकारिता के कार्यों पर धैर्य, गुनाहों से बचने पर धैर्य, और दुःखद फैसलों पर धैर्य।",
    "Kindness and continuous prayer for parents (Birr al-Walidayn) holds the highest priority in Islamic ethics, placed directly next to the monotheistic worship of Allah." to "माता-पिता के प्रति दया और निरंतर प्रार्थना (बर्र अल-वालिदैन) इस्लामी नैतिकता में सर्वोच्च प्राथमिकता रखती है, जो अल्लाह की एकेश्वरवादी पूजा के सीधे बाद आती है।",
    "Al-Qurtubi explains that filial piety remains obligatory even if parents are of another faith, provided they do not command disobedience to God." to "अल-क़ुरतुबी समझाते हैं कि माता-पिता की सेवा अनिवार्य रहती है भले ही वे दूसरे धर्म के हों, बशर्ते वे अल्लाह की नाफरमानी का आदेश न दें।",
    "Wudu is the ritual purification obligatory for Salah. Its core Quranic requirements are washing the face, arms including elbows, wiping the head, and washing feet to the ankles." to "वुज़ू नमाज़ के लिए अनिवार्य अनुष्ठान शुद्धि है। इसकी मूल क़ुरानी आवश्यकताएं चेहरा धोना, कोहनी सहित बाहें धोना, सिर पोंछना और पैर टखनों तक धोना हैं।",
    "All recognized schools agree on the 4 pillars mentioned in the Quranic verse, with differences regarding whether rubbing (Dalk) and uninterrupted continuity (Muwalat) are obligatory or recommended." to "सभी मान्यता प्राप्त मज़हब क़ुरानी आयत में उल्लिखित 4 स्तंभों पर सहमत हैं, लेकिन इस बारे में अंतर हैं कि रगड़ (दलक़) और निरंतरता (मुवालात) अनिवार्य हैं या सुझाव के रूप में।",
    "Hanafi school considers wiping a quarter of the head sufficient; Shafi'i considers wiping any portion of hair sufficient; Maliki and Hanbali mandate wiping the entire head." to "हनफ़ी मज़हब सिर के एक चौथाई भाग को पोंछना पर्याप्त मानता है; शाफ़िई बालों के किसी भी भाग को पोंछना पर्याप्त मानता है; मालिकी और हनबली पूरे सिर को पोंछना अनिवार्य करते हैं।",
    "According to authentic Quranic and Prophetic sources, believers are instructed to build their daily life upon constant mindfulness of Allah (Taqwa), establishing prayer, speaking truth, and upholding righteous character." to "प्रामाणिक क़ुरानी और पैग़ंबरी स्रोतों के अनुसार, मुसलमानों को अपने दैनिक जीवन को अल्लाह के निरंतर सचेतन (तक़वा), नमाज़ की स्थापना, सच बोलने और धार्मिक चरित्र पर आधारित करने का निर्देश दिया गया है।",
    "Classical commentators emphasize that Islamic practice balances personal spiritual duties (Ibadah) with exemplary social ethics (Mu'amalat)." to "शास्त्रीय व्याख्याकार इस बात पर जोर देते हैं कि इस्लामी प्रथा व्यक्तिगत आध्यात्मिक कर्तव्यों (इबादत) को अनुकरणीय सामाजिक नैतिकता (मुआमलात) के साथ संतुलित करती है।",
    "High-stakes personal fiqh questions require in-person consultation with a qualified local Islamic scholar." to "उच्च-दांव व्यक्तिगत फिक़्ह प्रश्नों के लिए एक योग्य स्थानीय इस्लामी विद्वान से व्यक्तिगत परामर्श की आवश्यकता होती है।",
    // Festival guidance (Aamal titles/details, Cautions, festival names/descriptions) — real Hindi
    // script, replacing any fallback.
    "Naye chaand ki dua" to "नए चाँद की दुआ",
    "Chaand dekh kar padhein: اللّٰهُمَّ أَهِلَّهُ عَلَيْنَا بِالْأَمْنِ وَالْإِيمَانِ وَالسَّلَامَةِ وَالْإِسْلَامِ رَبِّي وَرَبُّكَ اللّٰهُ" to "चाँद देखकर पढ़ें: اللّٰهُمَّ أَهِلَّهُ عَلَيْنَا بِالْأَمْنِ وَالْإِيمَانِ وَالسَّلَامَةِ وَالْإِسْلَامِ رَبِّي وَرَبُّكَ اللّٰهُ",
    "Muharram ke roze" to "मुहर्रम के रोज़े",
    "Ramzan ke baad sabse afzal roze Allah ke mahine Muharram ke hain — is mahine zyada nafl roze rakhein." to "रमज़ान के बाद सबसे बेहतर रोज़े अल्लाह के महीने मुहर्रम के हैं — इस महीने अधिक नफ़्ल रोज़े रखें।",
    "Tauba aur istighfar" to "तौबा और इस्तिग़फ़ार",
    "Guzre saal ke gunahon se tauba karein aur naye saal ke liye neki ka irada karein." to "गुज़रे साल के गुनाहों से तौबा करें और नए साल के लिए नेकी का इरादा करें।",
    "Hurmat wale mahine ka lihaaz" to "हुरमत वाले महीने का लिहाज़",
    "Muharram chaar hurmat wale mahinon mein se hai — in mein apne upar zulm (gunah) se bachein." to "मुहर्रम चार हुरमत वाले महीनों में से है — इन में अपने ऊपर ज़ुल्म (गुनाह) से बचें।",
    "Ashura ka roza (9 + 10 ya 10 + 11)" to "आशूरा का रोज़ा (9 + 10 या 10 + 11)",
    "10 Muharram ka roza pichle ek saal ke gunahon ka kaffara hai. Yahood ki mushabahat se bachne ke liye saath mein 9 (ya 11) ka roza bhi rakhein." to "10 मुहर्रम का रोज़ा पिछले एक साल के गुनाहों का कफ़्फ़ारा है। यहूदियों की मुशाबहत से बचने के लिए साथ में 9 (या 11) का रोज़ा भी रखें।",
    "Ghar walon par kushadagi" to "घर वालों पर कुशादगी",
    "Ashura ke din ghar walon par khane-peene mein wus'at karein — Allah saal bhar rizq mein barkat deta hai." to "आशूरा के दिन घर वालों पर खाने-पीने में कुशादगी करें — अल्लाह साल भर रिज़क़ में बरकत देता है।",
    "Nafl namaz (Ashura ki namaz)" to "नफ़्ल नमाज़ (आशूरा की नमाज़)",
    "Chasht ya kisi bhi makrooh waqt ke alawa 2-2 rakat nafl padhein; har rakat mein Fatiha ke baad Surah Ikhlas padh sakte hain." to "चाश्त या किसी भी मकरूह वक़्त के अलावा 2-2 रकात नफ़्ल पढ़ें; हर रकात में फातिहा के बाद सूरह इख़लास पढ़ सकते हैं।",
    "Zikr-e-Imam Husain (RA) aur Esaal-e-Sawab" to "ज़िक्रे इमाम हुसैन (रज़ि.) और ईसाले सवाब",
    "Shuhada-e-Karbala ke liye Quran khwani, Fatiha aur sadqa karke sawab pahunchayein. Matam, nauha aur seena-kobi shariat mein mana hai." to "शहीदे कर्बला के लिए क़ुरान ख़वानी, फातिहा और सदक़ा करके सवाब पहुँचाएं। मातम, नौहा और सीना कूबी शरीयत में ممनूअ है।",
    "Sadqa aur yateem par shafqat" to "सदक़ा और यतीम पर शफ़क़त",
    "Is din ghareebon ko khana khilayein aur sadqa karein." to "इस दिन ग़रीबों को खाना खिलाएं और सदक़ा करें।",
    "Durood Shareef ki kasrat" to "दरूद शरीफ़ की कसरत",
    "Kam az kam 100 martaba Durood-e-Ibrahimi ya koi bhi durood padhein. Ek durood par Allah 10 rehmatein bhejta hai." to "कम से कम 100 बार दरूदे इब्राहीमी या कोई भी दरूद पढ़ें। एक दरूद पर अल्लाह 10 रहमतें भेजता है।",
    "Peer ka roza" to "पीर का रोज़ा",
    "Nabi ﷺ se Peer ke roze ke baare mein poocha gaya to farmaya: 'Is din meri wiladat hui.' — Peer (Monday) ka roza rakhein." to "नबी ﷺ से पीर के रोज़े के बारे में पूछा गया तो फ़रमाया: 'इस दिन मेरी विलादत हुई।' — पीर का रोज़ा रखें।",
    "Seerat-un-Nabi ﷺ ka mutala'a" to "सीरते नबी ﷺ का मुतालआ",
    "Seerat parhein/sunein, Naat aur Mehfil-e-Milad mein shirkat karein, bachon ko Nabi ﷺ ki zindagi sunayein." to "सीरत पढ़ें/सुनें, नात और महफ़िले मीलाद में शरकत करें, बच्चों को नबी ﷺ की जिंदगी सुनाएं।",
    "Surah aur tilawat" to "सूरह और तिलावत",
    "Surah Al-Ahzab ki ayat 56, Surah Al-Fath aur Surah Yaseen ki tilawat karein." to "सूरह अल-अहज़ाब की आयत 56, सूरह अल-फ़तह और सूरह यासीन की तिलावत करें।",
    "Sadqa aur khana khilana" to "सदक़ा और खाना खिलाना",
    "Khushi ke izhaar mein ghareebon ko khana khilayein; fuzool-kharchi aur gair-shar'i kaamon se bachein." to "खुशी के इज़हार में ग़रीबों को खाना खिलाएं; फ़ुज़ूल ख़र्ची और गैर शरई कामों से बचें।",
    "Esaal-e-Sawab" to "ईसाले सवाब",
    "Quran khwani (Fatiha, Surah Ikhlas 3 baar, Ayat-ul-Kursi) karke Huzoor Ghaus-e-Azam Shaykh Abdul Qadir Jilani (RA) ko sawab pahunchayein." to "क़ुरान ख़वानी (फातिहा, सूरह इख़लास 3 बार, आयत अल-कुर्सी) करके हज़रत ग़ौसे आज़म शेख़ अब्दुल क़ादिर जीलानी (रह.) को सवाब पहुँचाएं।",
    "Niyaz / khana khilana" to "नियाज़ / खाना खिलाना",
    "Allah ke liye khana pakakar ghareebon aur logon ko khilayein, sawab buzurg ko pahunchayein." to "अल्लाह के लिए खाना पकाकर ग़रीबों और लोगों को खिलाएं, सवाब बुज़ुर्गों को पहुँचाएं।",
    "Seerat-e-Ghaus-e-Azam ka mutala'a" to "सीरते ग़ौसे आज़म का मुतालआ",
    "Unki kitab 'Futuh al-Ghaib' / 'Ghunyat-ut-Talibeen' se naseehatein parhein aur un par amal karein." to "उनकी किताब 'फुतूह अल-ग़ैब' / 'ग़ुन्यतु अत-तालिबीन' से नसीहतें पढ़ें और उन पर अमल करें।",
    "Rajab ki dua" to "रजब की दुआ",
    "اللّٰهُمَّ بَارِكْ لَنَا فِي رَجَبٍ وَشَعْبَانَ وَبَلِّغْنَا رَمَضَانَ — Aye Allah! Rajab aur Sha'ban mein barkat de aur hamein Ramzan tak pahuncha." to "اللّٰهُمَّ بَارِكْ لَنَا فِي رَجَبٍ وَشَعْبَانَ وَبَلِّغْنَا رَمَضَانَ — ऐ अल्लाह! रजब और शाबान में बरकत दे और हमें रमज़ान तक पहुँचा।",
    "Nafl roze" to "नफ़्ल रोज़े",
    "Rajab hurmat wala mahina hai — is mein nafl roze (khaas kar Peer/Jumeraat aur Ayyam-e-Beez 13-14-15) rakhein." to "रजब हुरमत वाला महीना है — इस में नफ़्ल रोज़े (खासकर पीर/जुमरात और ईयामे बीज़ 13-14-15) रखें।",
    "Tauba aur Ramzan ki tayyari" to "तौबा और रमज़ान की तैयारी",
    "Qaza namazein/roze poore karne ki shuruaat karein aur istighfar ki kasrat karein." to "क़ज़ा नमाज़ें/रोज़े पूरे करने की शुरुआत करें और इस्तिग़फ़ार की कसरत करें।",
    "Namaz ki pabandi" to "नमाज़ की पाबंदी",
    "Isi raat paanch waqt ki namaz farz hui — is raat se namaz ki pakki pabandi ka ahd karein." to "इसी रात पाँच वक़्त की नमाज़ फ़र्ज़ हुई — इस रात से नमाज़ की पक्की पाबंदी का अहद करें।",
    "Waqia-e-Me'raj ka zikr" to "वाक़िए मिराज का ज़िक्र",
    "Surah Bani Israel (Al-Isra) ki pehli ayat aur Surah An-Najm (ayat 1-18) ki tilawat karein aur waqia-e-Me'raj sunein/sunayein." to "सूरह बनी इस्राईल (अल-इसरा) की पहली आयत और सूरह अन-नजम (आयत 1-18) की तिलावत करें और वाक़िए मिराज सुनें/सुनाएं।",
    "Nafl ibadat aur durood" to "नफ़्ल इबादत और दरूद",
    "Isha ke baad nafl namaz, tilawat, durood aur dua mein waqt guzarein." to "इशा के बाद नफ़्ल नमाज़, तिलावत, दरूद और दुआ में वक़्त गुज़ारें।",
    "27 Rajab ka roza" to "27 रजब का रोज़ा",
    "Kuch buzurg is din nafl roza rakhte hain; niyyat aam nafl roze ki rakhein." to "कुछ बुज़ुर्ग इस दिन नफ़्ल रोज़ा रखते हैं; नीयत आम नफ़्ल रोज़े की रखें।",
    "Tauba aur maghfirat ki dua" to "तौबा और मग़फ़िरत की दुआ",
    "Is raat Allah apni makhlooq ki taraf tawajjuh farmata hai aur mushrik aur keena rakhne wale ke siwa sab ki maghfirat karta hai — dil saaf karke tauba karein." to "इस रात अल्लाह अपनी मख़लूक़ की तरफ़ तवज्जुह फ़रमाता है और मुशरिक और कीना रखने वाले के सिवा सब की मग़फ़िरत करता है — दिल साफ़ करके तौबा करें।",
    "Raat mein nafl ibadat" to "रात में नफ़्ल इबादत",
    "Raat mein nafl namaz, tilawat (Surah Yaseen, Surah Mulk, Surah Dukhan) aur zikr karein. Kuch buzurg 6 rakat nafl Maghrib ke baad aur Salat-ut-Tasbeeh padhte hain." to "रात में नफ़्ल नमाज़, तिलावत (सूरह यासीन, सूरह मुलक, सूरह दुख़ान) और ज़िक्र करें। कुछ बुज़ुर्ग 6 रकात नफ़्ल मग़रिब के बाद और सलात अत-तसबीह पढ़ते हैं।",
    "Qabristan ki ziyarat" to "क़ब्रिस्तान की ज़ियारत",
    "Nabi ﷺ is raat Jannat-ul-Baqee tashreef le gaye aur ahl-e-qubur ke liye dua ki — marhoomeen ke liye dua aur Fatiha padhein." to "नबी ﷺ इस रात जन्नतुल बक़ी तशरीफ़ ले गए और अहले क़ुबूर के लिए दुआ की — मरहूमीन के लिए दुआ और फातिहा पढ़ें।",
    "15 Sha'ban ka roza" to "15 शाबान का रोज़ा",
    "Agle din (15 Sha'ban) ka roza rakhein." to "अगले दिन (15 शाबान) का रोज़ा रखें।",
    "Aatish-baazi aur fuzooliyat se parhez" to "आतिशबाज़ी और फ़ुज़ूलियत से परहेज़",
    "Patakhe, chiraghaan aur halwe ko zaruri samajhna shariat se sabit nahi — waqt ibadat mein lagayein." to "पटाखे, चिराग़ां और हलवे को ज़रूरी समझना शरीयत से साबित नहीं — वक़्त इबादत में लगाएं।",
    "Roza aur Sehri" to "रोज़ा और सहरी",
    "Sehri zaroor karein, isme barkat hai. Roze ki niyyat: وَبِصَوْمِ غَدٍ نَوَيْتُ مِنْ شَهْرِ رَمَضَانَ" to "सहरी ज़रूर करें, इस में बरकत है। रोज़े की नीयत: وَبِصَوْمِ غَدٍ نَوَيْتُ مِنْ شَهْرِ رَمَضَانَ",
    "Iftar ki dua" to "इफ़्तार की दुआ",
    "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللّٰهُ — Khajoor ya paani se iftar karein." to "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللّٰهُ — खजूर या पानी से इफ़्तार करें।",
    "Taraweeh" to "तरावीह",
    "Isha ke baad 20 rakat Taraweeh (Sunnat-e-Mu'akkadah) jama'at ke saath padhein." to "इशा के बाद 20 रकात तरावीह (सुन्नत मुअक्कदह) जमाअत के साथ पढ़ें।",
    "Quran ki tilawat" to "क़ुरान की तिलावत",
    "Ramzan Quran ka mahina hai — kam az kam ek Quran khatam karne ki koshish karein." to "रमज़ान क़ुरान का महीना है — कम से कम एक क़ुरान ख़तम करने की कोशिश करें।",
    "Sadqa aur iftar karana" to "सदक़ा और इफ़्तार कराना",
    "Kisi roze-daar ko iftar karane par us jitna sawab milta hai." to "किसी रोज़ेदार को इफ़्तार कराने पर उस जितना सवाब मिलता है।",
    "Shab-e-Qadr ki dua" to "शबे क़द्र की दुआ",
    "اللّٰهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي — Aye Allah! Tu maaf karne wala hai, maafi ko pasand karta hai, mujhe maaf kar de." to "اللّٰهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي — ऐ अल्लाह! तू माफ़ करने वाला है, माफ़ी को पसंद करता है, मुझे माफ़ कर दे।",
    "Qiyam-ul-Lail" to "क़ियामुल लैल",
    "Imaan aur sawab ki niyyat se is raat qiyam karne wale ke pichle gunah maaf kar diye jaate hain. Nafl namaz, Salat-ut-Tasbeeh padhein." to "ईमान और सवाब की नीयत से इस रात क़ियाम करने वाले के पिछले गुनाह माफ़ कर दिए जाते हैं। नफ़्ल नमाज़, सलात अत-तसबीह पढ़ें।",
    "Surah Al-Qadr aur tilawat" to "सूरह अल-क़द्र और तिलावत",
    "Surah Al-Qadr, Surah Yaseen, Surah Mulk aur jitna ho sake Quran padhein." to "सूरह अल-क़द्र, सूरह यासीन, सूरह मुलक और जितना हो सके क़ुरान पढ़ें।",
    "Aakhri ashre ki taaq raatein" to "आखरी अशरे की ताक़ रातें",
    "Shab-e-Qadr ko 21, 23, 25, 27, 29 ki raaton mein talash karein. Aitikaf Sunnat-e-Mu'akkadah alal kifaya hai." to "शबे क़द्र को 21, 23, 25, 27, 29 की रातों में तलाश करें। अतिकाफ़ सुन्नत मुअक्कदह अलल किफ़ाया है।",
    "Sadqa-e-Fitr" to "सदक़े फ़ित्र",
    "Eid ki namaz se pehle har ghar ke fard ki taraf se Sadqa-e-Fitr ada karein (wajib)." to "ईद की नमाज़ से पहले हर घर के फ़र्द की तरफ़ से सदक़े फ़ित्र अदा करें (वाजिब)।",
    "Eid ki Sunnatein" to "ईद की सुन्नतें",
    "Ghusl, miswak, achhe kapde, khushbu, namaz se pehle taaq khajoorein khana, ek raaste se jaana doosre se aana." to "ग़ुस्ल, मिसवाक़, अच्छे कपड़े, खुशबू, नमाज़ से पहले ताक़ खजूरें खाना, एक रास्ते से जाना दूसरे से आना।",
    "Takbeer" to "तकबीर",
    "Eidgah jaate hue aahista takbeer: اللّٰهُ أَكْبَرُ اللّٰهُ أَكْبَرُ لَا إِلٰهَ إِلَّا اللّٰهُ وَاللّٰهُ أَكْبَرُ اللّٰهُ أَكْبَرُ وَلِلّٰهِ الْحَمْدُ" to "ईदगाह जाते हुए आहिस्ता तकबीर: اللّٰهُ أَكْبَرُ اللّٰهُ أَكْبَرُ لَا إِلٰهَ إِلَّا اللّٰهُ وَاللّٰهُ أَكْبَرُ اللّٰهُ أَكْبَرُ وَلِلّٰهِ الْحَمْدُ",
    "Eid ki namaz" to "ईद की नमाज़",
    "2 rakat wajib, 6 zaid takbeeron ke saath (Hanafi), phir khutba sunein." to "2 रकात वाजिब, 6 ज़ायद तकबीरों के साथ (हनफ़ी), फिर खुत्बा सुनें।",
    "Shawwal ke 6 roze" to "शव्वाल के 6 रोज़े",
    "Ramzan ke baad Shawwal ke 6 roze rakhne wala goya poore saal roze rakhta hai." to "रमज़ान के बाद शव्वाल के 6 रोज़े रखने वाला गोया पूरे साल रोज़े रखता है।",
    "Arafah ka roza" to "अरफ़ा का रोज़ा",
    "Ghair-haji ke liye 9 Zil-Hijjah ka roza pichle aur agle saal ke gunahon ka kaffara hai." to "ग़ैर हाजी के लिए 9 ज़िलहज के रोज़े पिछले और अगले साल के गुनाहों का कफ़्फ़ारा है।",
    "Arafah ki behtareen dua" to "अरफ़ा की बेहतरीन दुआ",
    "Takbeer-e-Tashreeq" to "तकबीरे तशरीक़",
    "9 Zil-Hijjah ki Fajr se 13 ki Asr tak har farz namaz ke baad ek baar: اللّٰهُ أَكْبَرُ اللّٰهُ أَكْبَرُ لَا إِلٰهَ إِلَّا اللّٰهُ وَاللّٰهُ أَكْبَرُ اللّٰهُ أَكْبَرُ وَلِلّٰهِ الْحَمْدُ (wajib)" to "9 ज़िलहज की फ़ज्र से 13 की अस्र तक हर फ़र्ज़ नमाज़ के बाद एक बार: اللّٰهُ أَكْبَرُ اللّٰهُ أَكْبَرُ لَا إِلٰهَ إِلَّا اللّٰهُ وَاللّٰهُ أَكْبَرُ اللّٰهُ أَكْبَرُ وَلِلّٰهِ الْحَمْدُ (वाजिब)",
    "Zil-Hijjah ke pehle 10 din" to "ज़िलहज के पहले 10 दिन",
    "In dinon ke nek amal Allah ko sabse zyada mehboob hain — zikr, tahleel, takbeer aur tahmeed ki kasrat karein." to "इन दिनों के नेक अमल अल्लाह को सबसे ज़्यादा महबूब हैं — ज़िक्र, तहलील, तकबीर और तहमीद की कसरत करें।",
    "Baal-nakhun na katna" to "बाल-नाख़ून न काटना",
    "Qurbani karne wala 1 Zil-Hijjah se qurbani tak baal aur nakhun na kaate (mustahab)." to "क़ुर्बानी करने वाला 1 ज़िलहज से क़ुर्बानी तक बाल और नाख़ून न काटे (मुस्तहब)।",
    "Qurbani" to "क़ुर्बानी",
    "Sahib-e-nisaab par qurbani wajib hai (10–12 Zil-Hijjah). Zibah ke waqt: بِسْمِ اللّٰهِ اللّٰهُ أَكْبَرُ" to "साहिब-ए-निसाब पर क़ुर्बानी वाजिब है (10–12 ज़िलहज)। ज़िबह के वक़्त: بِسْمِ اللّٰهِ اللّٰهُ أَكْبَرُ",
    "Ghusl, achhe kapde, khushbu; namaz se pehle kuch na khana (qurbani ke gosht se khana mustahab), raaste mein buland awaaz se takbeer." to "ग़ुस्ल, अच्छे कपड़े, खुशबू; नमाज़ से पहले कुछ न खाना (क़ुर्बानी के गोश्त से खाना मुस्तहब है), रास्ते में बुलंद आवाज़ से तकबीर।",
    "Eid-ul-Adha ki namaz" to "ईद-उल-अज़हा की नमाज़",
    "2 rakat wajib, 6 zaid takbeeron ke saath (Hanafi), phir khutba." to "2 रकात वाजिब, 6 ज़ायद तकबीरों के साथ (हनफ़ी), फिर खुत्बा।",
    "Gosht ki taqseem" to "गोश्त की तक़सीम",
    "Mustahab hai ki gosht ke 3 hisse karein: ghar, rishtedaar aur ghareeb." to "मुस्तहब है कि गोश्त के 3 हिस्से करें: घर, रिश्तेदार और ग़रीब।",
    "Takbeer-e-Tashreeq jaari rakhein" to "तकबीरे तशरीक़ जारी रखें",
    "13 Zil-Hijjah ki Asr tak har farz namaz ke baad takbeer padhein. 11–13 Ayyam-e-Tashreeq mein roza mana hai." to "13 ज़िलहज की अस्र तक हर फ़र्ज़ नमाज़ के बाद तकबीर पढ़ें। 11–13 ईयामे तशरीक़ में रोज़ा ममनूअ है।",
    // Festival cautions
    "Muharram ko sog ka mahina samajh kar nikah, safar ya khushi ke jaiz kaam manhoos na samjhein." to "मुहर्रम को सोग का महीना समझकर निकाह, सफ़र या खुशी के जायज़ काम मनहूस न समझें।",
    "Raaij taziye na banayein, na unki ta'zeem ya unse muraad maangein." to "राइज तज़िए न बनाएं, न उनकी तअज़ीम करें न उनसे मुराद मांगें।",
    "Matam, seena-kobi, nauha aur apne jism ko nuqsan pahunchane se bachein." to "मातम, सीना कूबी, नौहा और अपने जिस्म को नुक़सान पहुँचाने से बचें।",
    "Milad ki mehfil ya juloos mein gaane-bajane, be-pardagi, namaz chhorne aur fuzool-kharchi se bachein." to "मीलाद की महफ़िल या जुलूस में गाने-बजाने, बे-पर्दगी, नमाज़ छोड़ने और फ़ुज़ूल ख़र्ची से बचें।",
    "Niyaz Allah ke naam par karein; Ghaus-e-Azam ko Allah ke barabar ya mustaqil madadgar na samjhein. Kisi khaas tareekh ko farz na samjhein." to "नियाज़ अल्लाह के नाम पर करें; ग़ौसे आज़म को अल्लाह के बराबर या मुस्तक़िल मददगार न समझें। किसी खास तारीख़ को फ़र्ज़ न समझें।",
    "Rajab ke kisi khaas roze ya namaz ko farz ya lazmi Sunnat na batayein." to "रजब के किसी खास रोज़े या नमाज़ को फ़र्ज़ या लाज़मी सुन्नत न बताएं।",
    "27 Rajab ko Me'raj ki qat'i tareekh ya kisi khaas namaz/roze ko lazmi Sunnat na samjhein." to "27 रजब को मिराज की क़त्अी तारीख़ या किसी खास नमाज़/रोज़े को लाज़मी सुन्नत न समझें।",
    "Patakhe, aatish-bazi, logon ko takleef aur halwe ko zaruri rasm samajhne se bachein." to "पटाखे, आतिशबाज़ी, लोगों को तकलीफ़ और हलवे को ज़रूरी रस्म समझने से बचें।",
    "Roze mein jhoot, gheebat, jhagra aur namaz ki kotahi se bachein." to "रोज़े में झूठ, ग़ीबत, झगड़ा और नमाज़ की कोताही से बचें।",
    "Sirf 27vin raat par bharosa karke aakhri ashre ki baqi taaq raatein na chhorein." to "सिर्फ़ 27वीं रात पर भरोसा करके आखरी अशरे की बाक़ी ताक़ रातें न छोड़ें।",
    "Eid ke din roza na rakhein; namaz aur Sadqa-e-Fitr mein kotahi na karein." to "ईद के दिन रोज़ा न रखें; नमाज़ और सदक़े फ़ित्र में कोताही न करें।",
    "Hajj mein wuquf-e-Arafat karne wale par ghair-haji ke roze ki naseehat na thopein." to "हज़ में वुक़ूफ़े अरफ़ात करने वाले पर ग़ैर हाजी के रोज़े की नसीहत न थोपें।",
    "Qurbani ko numaaish ya riya na banayein; safai aur janwaron ke saath narmi ka khayal rakhein." to "क़ुर्बानी को नुमाइश या रिया न बनाएं; सफ़ाई और जानवरों के साथ नर्मी का खयाल रखें।",
    // Festival names/descriptions
    "Hijri Naya Saal" to "हिजरी नया साल",
    "First day of Muharram and the start of the new Hijri year." to "मुहर्रम का पहला दिन और नए हिजरी साल की ابتداء।",
    "Youm-e-Ashura" to "यौम-ए-आशूरा",
    "10th Muharram. Sunnah fast (with 9th or 11th); remembrance of Imam Husain (RA) and Karbala." to "10 मुहर्रम। सुन्नत रोज़ा (9 या 11 के साथ); इमाम हुसैन (रज़ि.) और कर्बला की यादा।",
    "Eid Milad-un-Nabi ﷺ" to "ईद मिलाद-उन-नबी ﷺ",
    "12th Rabi' al-Awwal, commemoration of the birth of the Prophet Muhammad ﷺ." to "12 रबीअुल-अव्वल, पैग़ंबर मुहम्मद ﷺ की जन्म की यादा।",
    "Giyarhvi Sharif" to "गियारहवीं शरीफ़",
    "11th Rabi' al-Thani, remembrance of Shaykh Abdul Qadir Jilani (RA)." to "11 रबीअुस-सानी, शेख़ अब्दुल क़ादिर जीलानी (रह.) की यादा।",
    "Rajab ka Aaghaz" to "रजब का आग़ाज़",
    "Beginning of Rajab, one of the four sacred months." to "रजब की शुरुआत, चार हुरमत वाले महीनों में से एक।",
    "Shab-e-Meraj" to "शबे मिराज",
    "Night of the Prophet's ﷺ night journey and ascension (27th Rajab)." to "नबी ﷺ की रात की यात्रा और मिराज की रात (27 रजब)।",
    "Shab-e-Baraat" to "शबे बरात",
    "Night of 15th Sha'ban, a night of forgiveness and worship." to "15 शाबान की रात, मग़फ़िरत और इबादत की रात।",
    "Ramzan ka Pehla Roza" to "रमज़ान का पहला रोज़ा",
    "First fast of Ramadan. Taraweeh begins the night before." to "रमज़ान का पहला रोज़ा। तरावीह पहले वाली रात से शुरू हो।",
    "Shab-e-Qadr" to "शबे क़द्र",
    "Commonly observed on the 27th night; seek it in the odd nights of the last ten." to "आमतौर पर 27वीं रात को मनाया जाता है; आखरी अशरे की ताक़ रातों में इसे तलाश करें।",
    "Eid-ul-Fitr" to "ईद-उल-फ़ित्र",
    "1st Shawwal. Pay Sadaqat al-Fitr before the Eid prayer." to "1 शव्वाल। ईद की नमाज़ से पहले सदक़े फ़ित्र अदा करें।",
    "Youm-e-Arafah" to "यौम-ए-अरफ़ा",
    "9th Dhu al-Hijjah. Fasting expiates sins of two years (Sahih Muslim)." to "9 ज़िलहज। रोज़ा दो साल के गुनाहों का कफ़्फ़ारा है (सही मुस्लिम)।",
    "Eid-ul-Adha (Bakra Eid)" to "ईद-उल-अज़हा (बक़रा ईद)",
    "10th Dhu al-Hijjah. Qurbani from the 10th to the 12th." to "10 ज़िलहज। क़ुर्बानी 10 से 12 तारीख़ तक होती है।"
)

private val urduPhrases = linkedMapOf(
    "Settings & Privacy" to "ترتیبات اور رازداری",
    "App Language" to "ایپ کی زبان", "Choose language" to "زبان منتخب کریں",
    "Choose app language" to "ایپ کی زبان منتخب کریں", "Close" to "بند کریں",
    "Home" to "ہوم", "Ramzan" to "رمضان", "Quran" to "قرآن", "Masjid" to "مسجد", "Community Masjid" to "کمیونٹی مسجد",
    "Duas" to "دعائیں", "Festivals" to "تہوار", "Settings" to "ترتیبات",
    "Prayer Calculation Settings" to "نماز کے اوقات کی ترتیبات",
    "Calculation Authority" to "حساب کا طریقہ", "Asr Juristic Calculation" to "عصر کے حساب کا طریقہ",
    "Hijri Moon Sighting Adjustment" to "ہجری چاند کی رویت کی درستی",
    "Prayer Notifications Service" to "نماز کی اطلاع کی خدمت", "Enable Prayer Alerts" to "نماز کی اطلاعات فعال کریں",
    "Privacy & Offline Architecture" to "رازداری اور آف لائن نظام",
    "Content Governance & Sources" to "مواد اور مآخذ",
    "Today's Prayer Schedule" to "آج کی نمازوں کے اوقات", "Calculated Salah Start" to "نماز شروع ہونے کا وقت",
    "Explore The Noble Quran" to "قرآن کریم پڑھیں", "Daily Adhkar" to "روزانہ اذکار",
    "Morning & Evening" to "صبح اور شام", "Qibla Direction" to "قبلہ کی سمت",
    "Major Cities" to "بڑے شہر", "Custom Lat / Long" to "اپنا عرض / طول بلد",
    "Lessons" to "اسباق", "AI Assistant" to "اے آئی معاون", "Read" to "پڑھیں", "Audio" to "آڈیو",
    "Surah wise" to "سورت کے لحاظ سے", "Para wise" to "پارے کے لحاظ سے",
    "Morning" to "صبح", "Evening" to "شام", "Done" to "مکمل",
    "Gentle Digital Tasbeeh" to "ڈیجیٹل تسبیح", "Goal" to "ہدف",
    "Verified Timetables" to "تصدیق شدہ اوقات", "Congregational Iqamah Timings" to "جماعت کی اقامت کے اوقات",
    "Friday Jummah Sessions" to "جمعہ کی نمازیں", "Suggest Edit" to "درستی تجویز کریں",
    "Why This Time?" to "یہ وقت کیوں؟", "Distance to Kaaba" to "کعبہ سے فاصلہ",
    "Magnetic Sensor Calibration" to "مقناطیسی سینسر کی درستی", "Ramzan Timetable" to "رمضان اوقات نامہ",
    "Sehri" to "سحری", "Iftar" to "افطار", "Online dates" to "آن لائن تاریخیں",
    "Prayer Times Alerts" to "نماز کے اوقات کی اطلاعات",
    "Peaceful notifications when each prayer time enters" to "ہر نماز کا وقت شروع ہونے پر پُرسکون اطلاع",
    "Time for" to "وقت ہو گیا:", "Salah has commenced for" to "نماز شروع ہو گئی ہے، مقام:",
    "Salah time has commenced at" to "نماز کا وقت شروع ہو گیا:",
    "Indeed, prayer has been decreed upon the believers a decree of specified times." to "بے شک نماز مومنوں پر مقررہ وقتوں میں فرض کی گئی ہے۔",
    "The selected language applies across the app." to "منتخب زبان پوری ایپ پر لاگو ہوتی ہے۔",
    "Retry" to "دوبارہ کوشش کریں",
    "The Essentials of Wudu (Ablution)" to "وضو کے بنیادی ارکان",
    "Understand the 4 Quranic obligations of Wudu and common authentic prophetic sunnahs." to "وضو کے چار قرآنی فرائض اور مشہور صحیح نبوی سنتوں کو سمجھیں۔",
    """
                Wudu is the spiritual and physical purification required before standing in prayer.

                The Quran establishes four obligatory pillars (Fara'id) in Surah Al-Ma'idah (5:6):
                1. Washing the face from forehead to chin and ear to ear.
                2. Washing both arms including the elbows.
                3. Wiping at least a quarter of the head with wet hands (Hanafi; Sunnat is wiping the whole head).
                4. Washing both feet up to and including the ankles.

                Prophetic Sunnahs (Recommended Practices):
                - Starting with Bismillah and intention (Niyyah) in the heart.
                - Washing the hands thrice up to wrists.
                - Rinsing the mouth (Madmadah) and cleaning the nose (Istinshaq) thrice.
                - Running wet fingers through the beard and between fingers and toes (Takhlil).
                - Maintaining sequential order (Tartib) without prolonged interruption (Muwalat).
            """.trimIndent() to
        "وضو نماز میں کھڑے ہونے سے پہلے درکار روحانی اور جسمانی طہارت ہے۔\n\nقرآن نے سورۃ المائدہ (٥:٦) میں چار فرائض بیان فرمائے ہیں:\n١. پیشانی سے ٹھوڑی تک اور کان سے کان تک چہرہ دھونا۔\n٢. دونوں بازو کہنیوں سمیت دھونا۔\n٣. گیلے ہاتھوں سے کم از کم سر کے چوتھائی حصے کا مسح کرنا (احناف کے نزدیک؛ سنت پورے سر کا مسح ہے)۔\n٤. دونوں پاؤں ٹخنوں سمیت دھونا۔\n\nمستحب نبوی سنتیں:\n- بسم اللہ اور دل میں نیت سے شروع کرنا۔\n- ہاتھوں کو کلائی تک تین بار دھونا۔\n- کلی کرنا اور ناک صاف کرنا تین تین بار۔\n- داڑھی اور انگلیوں کا خلال کرنا۔\n- ترتیب اور موالات کو بغیر طویل وقفے کے برقرار رکھنا۔",
    "Do not waste water even if by a flowing stream." to "بہتی ندی پر ہونے کے باوجود پانی ضائع نہ کریں۔",
    "Ensure dry spots on heels and elbows are reached." to "ایڑیوں اور کہنیوں پر خشک جگہ باقی نہ رہنے دیں۔",
    "Recite the Kalimah Shahadah and supplication after finishing." to "وضو مکمل کرنے کے بعد کلمہ شہادت اور دعا پڑھیں۔",
    "Attaining Khushu' (Tranquility) in Prayer" to "نماز میں خشوع حاصل کرنا",
    "How to calm a wandering mind and feel divine connection during your daily prayers." to "روزمرہ کی نمازوں میں بھٹکتے ذہن کو پرسکون کر کے اللہ سے تعلق کیسے محسوس کریں۔",
    """
                Khushu' is inner humility, stillness of the limbs, and attentive contemplation of what one is reciting.

                Practical Anchors for Khushu':
                - Prepare before the Takbir: Perform wudu mindfully, arrive before iqamah, and eliminate immediate physical distractions.
                - Contemplate the Words: Recite slowly (Tartil). When saying 'Al-Hamdu lillahi Rabbil-Alameen', remember Allah responds directly: 'My servant has praised Me' (Sahih Muslim 395).
                - Fix the Gaze: Direct your gaze respectfully at the place of prostration (Sujud), rather than looking around.
                - Remember the Final Prayer: The Prophet (peace be upon him) advised: 'Pray as if it were your farewell prayer' (Sunan Ibn Majah 4171).
            """.trimIndent() to
        "خشوع دل کی عاجزی، اعضاء کا سکون اور جو پڑھا جا رہا ہے اس میں غور و فکر ہے۔\n\nخشوع کے عملی نکات:\n- تکبیر سے پہلے تیاری: توجہ سے وضو کریں، اقامت سے پہلے پہنچیں اور فوری مشغولیات دور کریں۔\n- الفاظ میں غور: ٹھہر ٹھہر کر (ترتیل) پڑھیں۔ 'الحمد للہ رب العالمین' کہتے وقت یاد رکھیں کہ اللہ فوراً جواب دیتا ہے: 'میرے بندے نے میری تعریف کی' (صحیح مسلم ٣٩٥)۔\n- نظر کا تعین: ادب سے سجدے کی جگہ پر نظر رکھیں، ادھر ادھر نہ دیکھیں۔\n- آخری نماز کی یاد: نبی ﷺ نے فرمایا: 'ایسے نماز پڑھو جیسے یہ تمہاری آخری نماز ہو' (سنن ابن ماجہ ٤١٧١)۔",
    "Pause for 2 seconds after each ayah before reciting the next." to "ہر آیت کے بعد اگلی آیت پڑھنے سے پہلے دو سیکنڈ رکیں۔",
    "Vary the short surahs recited in Salah to prevent mechanical repetition." to "میکانکی تکرار سے بچنے کے لیے نماز میں مختلف چھوٹی سورتیں پڑھیں۔",
    "Take a deep breath and consciously let go of worldly tasks before saying 'Allahu Akbar'." to "'اللہ اکبر' کہنے سے پہلے گہرا سانس لیں اور دنیاوی کاموں کو شعوری طور پر چھوڑ دیں۔",
    "Rights of Neighbors in Islam" to "اسلام میں پڑوسیوں کے حقوق",
    "The high ethical status of neighbors regardless of their faith or background." to "پڑوسی کا اعلیٰ اخلاقی مقام، چاہے اس کا مذہب یا پس منظر کچھ بھی ہو۔",
    """
                The Prophet Muhammad (peace be upon him) said: 'Jibril kept enjoining good treatment of the neighbor upon me until I thought he would assign him a share of inheritance.' (Sahih al-Bukhari 6014).

                Islamic teachings identify three classes of neighbors:
                1. A neighbor who is a relative and Muslim: has three rights (kinship, Islam, neighborhood).
                2. A neighbor who is Muslim: has two rights (Islam and neighborhood).
                3. A neighbor of different faith: has the sacred right of good neighborhood and kindness.

                Harm to neighbors is strictly condemned: 'By Allah he does not believe... whose neighbor is not safe from his harm.' (Sahih al-Bukhari 6016).
            """.trimIndent() to
        "نبی کریم ﷺ نے فرمایا: 'جبریل مجھے پڑوسی کے ساتھ بھلائی کی اتنی تاکید کرتے رہے کہ میں نے گمان کیا کہ وہ اسے وارث بنا دیں گے' (صحیح البخاری ٦٠١٤)۔\n\nاسلامی تعلیمات میں پڑوسی تین قسم کے ہیں:\n١. رشتہ دار اور مسلمان پڑوسی: اس کے تین حق ہیں (قرابت، اسلام، ہمسائیگی)۔\n٢. مسلمان پڑوسی: اس کے دو حق ہیں (اسلام اور ہمسائیگی)۔\n٣. غیر مسلم پڑوسی: اس کا حسنِ ہمسائیگی اور بھلائی کا مقدس حق ہے۔\n\nپڑوسی کو تکلیف دینے کی سخت مذمت کی گئی ہے: 'اللہ کی قسم وہ ایمان نہیں رکھتا... جس کا پڑوسی اس کی ایذا سے محفوظ نہ ہو' (صحیح البخاری ٦٠١٦)۔",
    "Share homemade food or fruit occasionally." to "کبھی کبھار گھر کا کھانا یا پھل پڑوسیوں سے شیئر کریں۔",
    "Keep noise levels respectful during late night and early mornings." to "رات گئے اور صبح سویرے شور کو مناسب سطح پر رکھیں۔",
    "Help senior neighbors with groceries or physical tasks." to "بزرگ پڑوسیوں کی سودا سلف یا جسمانی کاموں میں مدد کریں۔",
    "The Noble Quran, Surah Al-Ma'idah (5:6)" to "قرآن کریم، سورۃ المائدہ (٥:٦)",
    "Sahih al-Bukhari 159 (Hadith of Uthman describing Prophet's wudu)" to "صحیح البخاری ١٥٩ (حضرت عثمان رضی اللہ عنہ کی روایت میں نبی ﷺ کے وضو کی صفت)",
    "Al-Hidaya, Kitab al-Taharah (Imam Marghinani, Hanafi)" to "الہدایہ، کتاب الطہارہ (امام مرغینانی، حنفی)",
    "Surah Al-Mu'minun (23:1-2): 'Successful indeed are the believers who are humble in their prayers.'" to "سورۃ المؤمنون (٢٣:١-٢): 'بے شک ایمان والے کامیاب ہو گئے جو اپنی نماز میں خشوع اختیار کرتے ہیں'۔",
    "Ihya Ulum al-Din, Kitab Asrar al-Salah (Imam Ghazali)" to "احیاء علوم الدین، کتاب اسرار الصلاۃ (امام غزالی)",
    "Sahih Muslim 395 (Hadith Qudsi on Surat al-Fatihah)" to "صحیح مسلم ٣٩٥ (سورۃ الفاتحہ کے بارے میں حدیث قدسی)",
    "Sahih al-Bukhari 6014, 6016" to "صحیح البخاری ٦٠١٤، ٦٠١٦",
    "Sahih Muslim 2625 (Abu Dharr on adding water to broth to share with neighbors)" to "صحیح مسلم ٢٦٢٥ (حضرت ابوذر رضی اللہ عنہ کا شوربے میں پانی بڑھا کر پڑوسیوں کو دینے کا واقعہ)",
    "Adab al-Mufrad (Imam al-Bukhari)" to "الادب المفرد (امام بخاری)",
    "Islamic jurisprudence regarding marital dissolution (talaq), complex inheritance, and personal legal verdicts requires direct review of both parties' detailed circumstances by a qualified local mufti or Islamic scholar." to "طلاق، پیچیدہ وراثت اور ذاتی شرعی فیصلوں کے معاملے میں دونوں فریقین کے حالات کا براہ راست جائزہ کسی مستند مقامی مفتی یا عالم دین سے لینا ضروری ہے۔",
    "Legal verdicts in personal disputes cannot be issued by automated systems or general textbooks, as individual statements, intentions, and local contracts directly impact the legal validity." to "ذاتی تنازعات میں شرعی فیصلے خودکار نظام یا عمومی کتابوں سے جاری نہیں کیے جا سکتے، کیونکہ افراد کے بیانات، نیت اور مقامی معاہدے فیصلے کی شرعی حیثیت پر براہ راست اثر ڈالتے ہیں۔",
    "In Islam, patience (Sabr) is coupled directly with prayer (Salah) as the believers' active fortress in moments of hardship. Rather than passive endurance, Sabr means emotional resilience, avoiding panic, and maintaining trust in Allah's wisdom." to "اسلام میں صبر کو نماز کے ساتھ جوڑا گیا ہے، جو مشکل وقت میں مومن کا فعال قلعہ ہیں۔ صبر محض خاموشی سے برداشت کرنا نہیں بلکہ جذباتی استقامت، گھبراہٹ سے بچنا اور اللہ کی حکمت پر بھروسہ رکھنا ہے۔",
    "Ibn Kathir notes in his Tafsir: When heavy trials occur, two aids support the human being: restraining the soul from forbidden despair (patience) and turning to the coolness of the eyes in prayer." to "امام ابن کثیر اپنی تفسیر میں لکھتے ہیں: سخت آزمائش کے وقت انسان کو دو چیزیں سہارا دیتی ہیں: نفس کو حرام جزع سے روکنا (صبر) اور نماز میں آنکھوں کی ٹھنڈک حاصل کرنا۔",
    "Scholars emphasize three types of Sabr: patience upon acts of obedience, patience against committing sins, and patience with painful decrees." to "علماء صبر کی تین اقسام بیان کرتے ہیں: اطاعت پر صبر، گناہ سے بچنے پر صبر، اور تکلیف دہ تقدیر پر صبر۔",
    "Kindness and continuous prayer for parents (Birr al-Walidayn) holds the highest priority in Islamic ethics, placed directly next to the monotheistic worship of Allah." to "والدین کے ساتھ نیکی اور ان کے لیے مسلسل دعا (بر الوالدین) اسلامی اخلاقیات میں سب سے اعلیٰ ترجیح رکھتی ہے، جو اللہ کی توحید کے فوراً بعد آتی ہے۔",
    "Al-Qurtubi explains that filial piety remains obligatory even if parents are of another faith, provided they do not command disobedience to God." to "امام قرطبی وضاحت کرتے ہیں کہ والدین کی خدمت واجب رہتی ہے چاہے وہ دوسرے مذہب کے ہوں، بشرطیکہ وہ اللہ کی نافرمانی کا حکم نہ دیں۔",
    "Wudu is the ritual purification obligatory for Salah. Its core Quranic requirements are washing the face, arms including elbows, wiping the head, and washing feet to the ankles." to "وضو نماز کے لیے فرض طہارت ہے۔ اس کے بنیادی قرآنی تقاضے چہرہ دھونا، کہنیوں سمیت بازو دھونا، سر کا مسح کرنا اور پاؤں ٹخنوں تک دھونا ہیں۔",
    "All recognized schools agree on the 4 pillars mentioned in the Quranic verse, with differences regarding whether rubbing (Dalk) and uninterrupted continuity (Muwalat) are obligatory or recommended." to "تمام معتبر مسالک آیت میں مذکور چار فرائض پر متفق ہیں، البتہ دلک اور موالات کے واجب یا مستحب ہونے میں اختلاف پایا جاتا ہے۔",
    "Hanafi school considers wiping a quarter of the head sufficient; Shafi'i considers wiping any portion of hair sufficient; Maliki and Hanbali mandate wiping the entire head." to "احناف کے نزدیک سر کے چوتھائی حصے کا مسح کافی ہے؛ شافعیہ کے نزدیک بال کے کسی بھی حصے کا مسح کافی ہے؛ جبکہ مالکیہ اور حنابلہ پورے سر کے مسح کو واجب قرار دیتے ہیں۔",
    "According to authentic Quranic and Prophetic sources, believers are instructed to build their daily life upon constant mindfulness of Allah (Taqwa), establishing prayer, speaking truth, and upholding righteous character." to "مستند قرآنی اور نبوی ماخذ کے مطابق، مومنوں کو ہدایت دی گئی ہے کہ وہ اپنی روزمرہ زندگی اللہ کے مسلسل تقویٰ، نماز کے قیام، سچ بولنے اور نیک اخلاق پر استوار کریں۔",
    "Classical commentators emphasize that Islamic practice balances personal spiritual duties (Ibadah) with exemplary social ethics (Mu'amalat)." to "قدیم مفسرین اس بات پر زور دیتے ہیں کہ اسلامی طرز عمل ذاتی روحانی فرائض (عبادات) اور مثالی معاشرتی اخلاق (معاملات) کے درمیان توازن رکھتا ہے۔",
    "High-stakes personal fiqh questions require in-person consultation with a qualified local Islamic scholar." to "انتہائی حساس ذاتی فقہی سوالات کے لیے کسی مستند مقامی عالم دین سے بالمشافہ مشورہ ضروری ہے۔",

    // Festival guidance (Aamal titles/details, Cautions, festival names/descriptions) — real Urdu
    // script, replacing the Roman-Hindi fallback these previously showed.
    "Naye chaand ki dua" to "نئے چاند کی دعا",
    "Chaand dekh kar padhein: اللّٰهُمَّ أَهِلَّهُ عَلَيْنَا بِالْأَمْنِ وَالْإِيمَانِ وَالسَّلَامَةِ وَالْإِسْلَامِ رَبِّي وَرَبُّكَ اللّٰهُ" to "چاند دیکھ کر پڑھیں: اللّٰهُمَّ أَهِلَّهُ عَلَيْنَا بِالْأَمْنِ وَالْإِيمَانِ وَالسَّلَامَةِ وَالْإِسْلَامِ رَبِّي وَرَبُّكَ اللّٰهُ",
    "Muharram ke roze" to "محرم کے روزے",
    "Ramzan ke baad sabse afzal roze Allah ke mahine Muharram ke hain — is mahine zyada nafl roze rakhein." to "رمضان کے بعد سب سے افضل روزے اللہ کے مہینے محرم کے ہیں — اس مہینے زیادہ نفل روزے رکھیں۔",
    "Tauba aur istighfar" to "توبہ اور استغفار",
    "Guzre saal ke gunahon se tauba karein aur naye saal ke liye neki ka irada karein." to "گزرے سال کے گناہوں سے توبہ کریں اور نئے سال کے لیے نیکی کا ارادہ کریں۔",
    "Hurmat wale mahine ka lihaaz" to "حرمت والے مہینے کا لحاظ",
    "Muharram chaar hurmat wale mahinon mein se hai — in mein apne upar zulm (gunah) se bachein." to "محرم چار حرمت والے مہینوں میں سے ہے — ان میں اپنے اوپر ظلم (گناہ) سے بچیں۔",
    "Ashura ka roza (9 + 10 ya 10 + 11)" to "عاشورہ کا روزہ (9 + 10 یا 10 + 11)",
    "10 Muharram ka roza pichle ek saal ke gunahon ka kaffara hai. Yahood ki mushabahat se bachne ke liye saath mein 9 (ya 11) ka roza bhi rakhein." to "10 محرم کا روزہ پچھلے ایک سال کے گناہوں کا کفارہ ہے۔ یہود کی مشابہت سے بچنے کے لیے ساتھ میں 9 (یا 11) کا روزہ بھی رکھیں۔",
    "Ghar walon par kushadagi" to "گھر والوں پر کشادگی",
    "Ashura ke din ghar walon par khane-peene mein wus'at karein — Allah saal bhar rizq mein barkat deta hai." to "عاشورہ کے دن گھر والوں پر کھانے پینے میں وسعت کریں — اللہ سال بھر رزق میں برکت دیتا ہے۔",
    "Nafl namaz (Ashura ki namaz)" to "نفل نماز (عاشورہ کی نماز)",
    "Chasht ya kisi bhi makrooh waqt ke alawa 2-2 rakat nafl padhein; har rakat mein Fatiha ke baad Surah Ikhlas padh sakte hain." to "چاشت یا کسی بھی مکروہ وقت کے علاوہ 2-2 رکعت نفل پڑھیں؛ ہر رکعت میں فاتحہ کے بعد سورہ اخلاص پڑھ سکتے ہیں۔",
    "Zikr-e-Imam Husain (RA) aur Esaal-e-Sawab" to "ذکرِ امام حسین (رضی اللہ عنہ) اور ایصالِ ثواب",
    "Shuhada-e-Karbala ke liye Quran khwani, Fatiha aur sadqa karke sawab pahunchayein. Matam, nauha aur seena-kobi shariat mein mana hai." to "شہدائے کربلا کے لیے قرآن خوانی، فاتحہ اور صدقہ کر کے ثواب پہنچائیں۔ ماتم، نوحہ اور سینہ کوبی شریعت میں ممنوع ہے۔",
    "Sadqa aur yateem par shafqat" to "صدقہ اور یتیم پر شفقت",
    "Is din ghareebon ko khana khilayein aur sadqa karein." to "اس دن غریبوں کو کھانا کھلائیں اور صدقہ کریں۔",
    "Durood Shareef ki kasrat" to "درود شریف کی کثرت",
    "Kam az kam 100 martaba Durood-e-Ibrahimi ya koi bhi durood padhein. Ek durood par Allah 10 rehmatein bhejta hai." to "کم از کم 100 مرتبہ درودِ ابراہیمی یا کوئی بھی درود پڑھیں۔ ایک درود پر اللہ 10 رحمتیں بھیجتا ہے۔",
    "Peer ka roza" to "پیر کا روزہ",
    "Nabi ﷺ se Peer ke roze ke baare mein poocha gaya to farmaya: 'Is din meri wiladat hui.' — Peer (Monday) ka roza rakhein." to "نبی ﷺ سے پیر کے روزے کے بارے میں پوچھا گیا تو فرمایا: 'اس دن میری ولادت ہوئی۔' — پیر کے دن روزہ رکھیں۔",
    "Seerat-un-Nabi ﷺ ka mutala'a" to "سیرت النبی ﷺ کا مطالعہ",
    "Seerat parhein/sunein, Naat aur Mehfil-e-Milad mein shirkat karein, bachon ko Nabi ﷺ ki zindagi sunayein." to "سیرت پڑھیں/سنیں، نعت اور محفلِ میلاد میں شرکت کریں، بچوں کو نبی ﷺ کی زندگی سنائیں۔",
    "Surah aur tilawat" to "سورہ اور تلاوت",
    "Surah Al-Ahzab ki ayat 56, Surah Al-Fath aur Surah Yaseen ki tilawat karein." to "سورہ الاحزاب کی آیت 56، سورہ الفتح اور سورہ یٰسین کی تلاوت کریں۔",
    "Sadqa aur khana khilana" to "صدقہ اور کھانا کھلانا",
    "Khushi ke izhaar mein ghareebon ko khana khilayein; fuzool-kharchi aur gair-shar'i kaamon se bachein." to "خوشی کے اظہار میں غریبوں کو کھانا کھلائیں؛ فضول خرچی اور غیر شرعی کاموں سے بچیں۔",
    "Esaal-e-Sawab" to "ایصالِ ثواب",
    "Quran khwani (Fatiha, Surah Ikhlas 3 baar, Ayat-ul-Kursi) karke Huzoor Ghaus-e-Azam Shaykh Abdul Qadir Jilani (RA) ko sawab pahunchayein." to "قرآن خوانی (فاتحہ، سورہ اخلاص 3 بار، آیت الکرسی) کر کے حضور غوثِ اعظم شیخ عبدالقادر جیلانی (رحمۃ اللہ علیہ) کو ثواب پہنچائیں۔",
    "Niyaz / khana khilana" to "نیاز / کھانا کھلانا",
    "Allah ke liye khana pakakar ghareebon aur logon ko khilayein, sawab buzurg ko pahunchayein." to "اللہ کے لیے کھانا پکا کر غریبوں اور لوگوں کو کھلائیں، ثواب بزرگ کو پہنچائیں۔",
    "Seerat-e-Ghaus-e-Azam ka mutala'a" to "سیرتِ غوثِ اعظم کا مطالعہ",
    "Unki kitab 'Futuh al-Ghaib' / 'Ghunyat-ut-Talibeen' se naseehatein parhein aur un par amal karein." to "ان کی کتاب 'فتوح الغیب' / 'غنیۃ الطالبین' سے نصیحتیں پڑھیں اور ان پر عمل کریں۔",
    "Rajab ki dua" to "رجب کی دعا",
    "اللّٰهُمَّ بَارِكْ لَنَا فِي رَجَبٍ وَشَعْبَانَ وَبَلِّغْنَا رَمَضَانَ — Aye Allah! Rajab aur Sha'ban mein barkat de aur hamein Ramzan tak pahuncha." to "اللّٰهُمَّ بَارِكْ لَنَا فِي رَجَبٍ وَشَعْبَانَ وَبَلِّغْنَا رَمَضَانَ — اے اللہ! رجب اور شعبان میں برکت دے اور ہمیں رمضان تک پہنچا۔",
    "Nafl roze" to "نفل روزے",
    "Rajab hurmat wala mahina hai — is mein nafl roze (khaas kar Peer/Jumeraat aur Ayyam-e-Beez 13-14-15) rakhein." to "رجب حرمت والا مہینہ ہے — اس میں نفل روزے (خاص کر پیر/جمعرات اور ایامِ بیض 13-14-15) رکھیں۔",
    "Tauba aur Ramzan ki tayyari" to "توبہ اور رمضان کی تیاری",
    "Qaza namazein/roze poore karne ki shuruaat karein aur istighfar ki kasrat karein." to "قضا نمازیں/روزے پورے کرنے کی شروعات کریں اور استغفار کی کثرت کریں۔",
    "Namaz ki pabandi" to "نماز کی پابندی",
    "Isi raat paanch waqt ki namaz farz hui — is raat se namaz ki pakki pabandi ka ahd karein." to "اسی رات پانچ وقت کی نماز فرض ہوئی — اس رات سے نماز کی پکی پابندی کا عہد کریں۔",
    "Waqia-e-Me'raj ka zikr" to "واقعۂ معراج کا ذکر",
    "Surah Bani Israel (Al-Isra) ki pehli ayat aur Surah An-Najm (ayat 1-18) ki tilawat karein aur waqia-e-Me'raj sunein/sunayein." to "سورہ بنی اسرائیل (الاسراء) کی پہلی آیت اور سورہ النجم (آیت 1-18) کی تلاوت کریں اور واقعۂ معراج سنیں/سنائیں۔",
    "Nafl ibadat aur durood" to "نفل عبادت اور درود",
    "Isha ke baad nafl namaz, tilawat, durood aur dua mein waqt guzarein." to "عشاء کے بعد نفل نماز، تلاوت، درود اور دعا میں وقت گزاریں۔",
    "27 Rajab ka roza" to "27 رجب کا روزہ",
    "Kuch buzurg is din nafl roza rakhte hain; niyyat aam nafl roze ki rakhein." to "کچھ بزرگ اس دن نفل روزہ رکھتے ہیں؛ نیت عام نفل روزے کی رکھیں۔",
    "Tauba aur maghfirat ki dua" to "توبہ اور مغفرت کی دعا",
    "Is raat Allah apni makhlooq ki taraf tawajjuh farmata hai aur mushrik aur keena rakhne wale ke siwa sab ki maghfirat karta hai — dil saaf karke tauba karein." to "اس رات اللہ اپنی مخلوق کی طرف توجہ فرماتا ہے اور مشرک اور کینہ رکھنے والے کے سوا سب کی مغفرت کرتا ہے — دل صاف کر کے توبہ کریں۔",
    "Raat mein nafl ibadat" to "رات میں نفل عبادت",
    "Raat mein nafl namaz, tilawat (Surah Yaseen, Surah Mulk, Surah Dukhan) aur zikr karein. Kuch buzurg 6 rakat nafl Maghrib ke baad aur Salat-ut-Tasbeeh padhte hain." to "رات میں نفل نماز، تلاوت (سورہ یٰسین، سورہ ملک، سورہ دخان) اور ذکر کریں۔ کچھ بزرگ 6 رکعت نفل مغرب کے بعد اور صلاۃ التسبیح پڑھتے ہیں۔",
    "Qabristan ki ziyarat" to "قبرستان کی زیارت",
    "Nabi ﷺ is raat Jannat-ul-Baqee tashreef le gaye aur ahl-e-qubur ke liye dua ki — marhoomeen ke liye dua aur Fatiha padhein." to "نبی ﷺ اس رات جنت البقیع تشریف لے گئے اور اہلِ قبور کے لیے دعا کی — مرحومین کے لیے دعا اور فاتحہ پڑھیں۔",
    "15 Sha'ban ka roza" to "15 شعبان کا روزہ",
    "Agle din (15 Sha'ban) ka roza rakhein." to "اگلے دن (15 شعبان) کا روزہ رکھیں۔",
    "Aatish-baazi aur fuzooliyat se parhez" to "آتش بازی اور فضول رسوم سے پرہیز",
    "Patakhe, chiraghaan aur halwe ko zaruri samajhna shariat se sabit nahi — waqt ibadat mein lagayein." to "پٹاخے، چراغاں اور حلوے کو ضروری سمجھنا شریعت سے ثابت نہیں — وقت عبادت میں لگائیں۔",
    "Roza aur Sehri" to "روزہ اور سحری",
    "Sehri zaroor karein, isme barkat hai. Roze ki niyyat: وَبِصَوْمِ غَدٍ نَوَيْتُ مِنْ شَهْرِ رَمَضَانَ" to "سحری ضرور کریں، اس میں برکت ہے۔ روزے کی نیت: وَبِصَوْمِ غَدٍ نَوَيْتُ مِنْ شَهْرِ رَمَضَانَ",
    "Iftar ki dua" to "افطار کی دعا",
    "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللّٰهُ — Khajoor ya paani se iftar karein." to "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللّٰهُ — کھجور یا پانی سے افطار کریں۔",
    "Taraweeh" to "تراویح",
    "Isha ke baad 20 rakat Taraweeh (Sunnat-e-Mu'akkadah) jama'at ke saath padhein." to "عشاء کے بعد 20 رکعت تراویح (سنتِ مؤکدہ) جماعت کے ساتھ پڑھیں۔",
    "Quran ki tilawat" to "قرآن کی تلاوت",
    "Ramzan Quran ka mahina hai — kam az kam ek Quran khatam karne ki koshish karein." to "رمضان قرآن کا مہینہ ہے — کم از کم ایک قرآن ختم کرنے کی کوشش کریں۔",
    "Sadqa aur iftar karana" to "صدقہ اور افطار کرانا",
    "Kisi roze-daar ko iftar karane par us jitna sawab milta hai." to "کسی روزے دار کو افطار کرانے پر اس جتنا ثواب ملتا ہے۔",
    "Shab-e-Qadr ki dua" to "شبِ قدر کی دعا",
    "اللّٰهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي — Aye Allah! Tu maaf karne wala hai, maafi ko pasand karta hai, mujhe maaf kar de." to "اللّٰهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي — اے اللہ! تو معاف کرنے والا ہے، معافی کو پسند کرتا ہے، مجھے معاف کر دے۔",
    "Qiyam-ul-Lail" to "قیام اللیل",
    "Imaan aur sawab ki niyyat se is raat qiyam karne wale ke pichle gunah maaf kar diye jaate hain. Nafl namaz, Salat-ut-Tasbeeh padhein." to "ایمان اور ثواب کی نیت سے اس رات قیام کرنے والے کے پچھلے گناہ معاف کر دیے جاتے ہیں۔ نفل نماز، صلاۃ التسبیح پڑھیں۔",
    "Surah Al-Qadr aur tilawat" to "سورہ القدر اور تلاوت",
    "Surah Al-Qadr, Surah Yaseen, Surah Mulk aur jitna ho sake Quran padhein." to "سورہ القدر، سورہ یٰسین، سورہ ملک اور جتنا ہو سکے قرآن پڑھیں۔",
    "Aakhri ashre ki taaq raatein" to "آخری عشرے کی طاق راتیں",
    "Shab-e-Qadr ko 21, 23, 25, 27, 29 ki raaton mein talash karein. Aitikaf Sunnat-e-Mu'akkadah alal kifaya hai." to "شبِ قدر کو 21، 23، 25، 27، 29 کی راتوں میں تلاش کریں۔ اعتکاف سنتِ مؤکدہ علی الکفایہ ہے۔",
    "Sadqa-e-Fitr" to "صدقہ فطر",
    "Eid ki namaz se pehle har ghar ke fard ki taraf se Sadqa-e-Fitr ada karein (wajib)." to "عید کی نماز سے پہلے ہر گھر کے فرد کی طرف سے صدقہ فطر ادا کریں (واجب)۔",
    "Eid ki Sunnatein" to "عید کی سنتیں",
    "Ghusl, miswak, achhe kapde, khushbu, namaz se pehle taaq khajoorein khana, ek raaste se jaana doosre se aana." to "غسل، مسواک، اچھے کپڑے، خوشبو، نماز سے پہلے طاق کھجوریں کھانا، ایک راستے سے جانا دوسرے سے آنا۔",
    "Takbeer" to "تکبیر",
    "Eidgah jaate hue aahista takbeer: اللّٰهُ أَكْبَرُ اللّٰهُ أَكْبَرُ لَا إِلٰهَ إِلَّا اللّٰهُ وَاللّٰهُ أَكْبَرُ اللّٰهُ أَكْبَرُ وَلِلّٰهِ الْحَمْدُ" to "عیدگاہ جاتے ہوئے آہستہ تکبیر: اللّٰهُ أَكْبَرُ اللّٰهُ أَكْبَرُ لَا إِلٰهَ إِلَّا اللّٰهُ وَاللّٰهُ أَكْبَرُ اللّٰهُ أَكْبَرُ وَلِلّٰهِ الْحَمْدُ",
    "Eid ki namaz" to "عید کی نماز",
    "2 rakat wajib, 6 zaid takbeeron ke saath (Hanafi), phir khutba sunein." to "2 رکعت واجب، 6 زائد تکبیروں کے ساتھ (حنفی)، پھر خطبہ سنیں۔",
    "Shawwal ke 6 roze" to "شوال کے 6 روزے",
    "Ramzan ke baad Shawwal ke 6 roze rakhne wala goya poore saal roze rakhta hai." to "رمضان کے بعد شوال کے 6 روزے رکھنے والا گویا پورے سال روزے رکھتا ہے۔",
    "Arafah ka roza" to "عرفہ کا روزہ",
    "Ghair-haji ke liye 9 Zil-Hijjah ka roza pichle aur agle saal ke gunahon ka kaffara hai." to "غیر حاجی کے لیے 9 ذوالحجہ کا روزہ پچھلے اور اگلے سال کے گناہوں کا کفارہ ہے۔",
    "Arafah ki behtareen dua" to "عرفہ کی بہترین دعا",
    "Takbeer-e-Tashreeq" to "تکبیرِ تشریق",
    "9 Zil-Hijjah ki Fajr se 13 ki Asr tak har farz namaz ke baad ek baar: اللّٰهُ أَكْبَرُ اللّٰهُ أَكْبَرُ لَا إِلٰهَ إِلَّا اللّٰهُ وَاللّٰهُ أَكْبَرُ اللّٰهُ أَكْبَرُ وَلِلّٰهِ الْحَمْدُ (wajib)" to "9 ذوالحجہ کی فجر سے 13 کی عصر تک ہر فرض نماز کے بعد ایک بار: اللّٰهُ أَكْبَرُ اللّٰهُ أَكْبَرُ لَا إِلٰهَ إِلَّا اللّٰهُ وَاللّٰهُ أَكْبَرُ اللّٰهُ أَكْبَرُ وَلِلّٰهِ الْحَمْدُ (واجب)",
    "Zil-Hijjah ke pehle 10 din" to "ذوالحجہ کے پہلے 10 دن",
    "In dinon ke nek amal Allah ko sabse zyada mehboob hain — zikr, tahleel, takbeer aur tahmeed ki kasrat karein." to "ان دنوں کے نیک اعمال اللہ کو سب سے زیادہ محبوب ہیں — ذکر، تہلیل، تکبیر اور تحمید کی کثرت کریں۔",
    "Baal-nakhun na katna" to "بال ناخن نہ کاٹنا",
    "Qurbani karne wala 1 Zil-Hijjah se qurbani tak baal aur nakhun na kaate (mustahab)." to "قربانی کرنے والا 1 ذوالحجہ سے قربانی تک بال اور ناخن نہ کاٹے (مستحب)۔",
    "Qurbani" to "قربانی",
    "Sahib-e-nisaab par qurbani wajib hai (10–12 Zil-Hijjah). Zibah ke waqt: بِسْمِ اللّٰهِ اللّٰهُ أَكْبَرُ" to "صاحبِ نصاب پر قربانی واجب ہے (10–12 ذوالحجہ)۔ ذبح کے وقت: بِسْمِ اللّٰهِ اللّٰهُ أَكْبَرُ",
    "Ghusl, achhe kapde, khushbu; namaz se pehle kuch na khana (qurbani ke gosht se khana mustahab), raaste mein buland awaaz se takbeer." to "غسل، اچھے کپڑے، خوشبو؛ نماز سے پہلے کچھ نہ کھانا (قربانی کے گوشت سے کھانا مستحب ہے)، راستے میں بلند آواز سے تکبیر۔",
    "Eid-ul-Adha ki namaz" to "عید الاضحیٰ کی نماز",
    "2 rakat wajib, 6 zaid takbeeron ke saath (Hanafi), phir khutba." to "2 رکعت واجب، 6 زائد تکبیروں کے ساتھ (حنفی)، پھر خطبہ۔",
    "Gosht ki taqseem" to "گوشت کی تقسیم",
    "Mustahab hai ki gosht ke 3 hisse karein: ghar, rishtedaar aur ghareeb." to "مستحب ہے کہ گوشت کے 3 حصے کریں: گھر، رشتہ دار اور غریب۔",
    "Takbeer-e-Tashreeq jaari rakhein" to "تکبیرِ تشریق جاری رکھیں",
    "13 Zil-Hijjah ki Asr tak har farz namaz ke baad takbeer padhein. 11–13 Ayyam-e-Tashreeq mein roza mana hai." to "13 ذوالحجہ کی عصر تک ہر فرض نماز کے بعد تکبیر پڑھیں۔ 11–13 ایامِ تشریق میں روزہ منع ہے۔",

    // Festival cautions
    "Muharram ko sog ka mahina samajh kar nikah, safar ya khushi ke jaiz kaam manhoos na samjhein." to "محرم کو سوگ کا مہینہ سمجھ کر نکاح، سفر یا خوشی کے جائز کام منحوس نہ سمجھیں۔",
    "Raaij taziye na banayein, na unki ta'zeem ya unse muraad maangein." to "رائج تعزیے نہ بنائیں، نہ ان کی تعظیم کریں نہ ان سے مراد مانگیں۔",
    "Matam, seena-kobi, nauha aur apne jism ko nuqsan pahunchane se bachein." to "ماتم، سینہ کوبی، نوحہ اور اپنے جسم کو نقصان پہنچانے سے بچیں۔",
    "Milad ki mehfil ya juloos mein gaane-bajane, be-pardagi, namaz chhorne aur fuzool-kharchi se bachein." to "میلاد کی محفل یا جلوس میں گانے بجانے، بے پردگی، نماز چھوڑنے اور فضول خرچی سے بچیں۔",
    "Niyaz Allah ke naam par karein; Ghaus-e-Azam ko Allah ke barabar ya mustaqil madadgar na samjhein. Kisi khaas tareekh ko farz na samjhein." to "نیاز اللہ کے نام پر کریں؛ غوثِ اعظم کو اللہ کے برابر یا مستقل مددگار نہ سمجھیں۔ کسی خاص تاریخ کو فرض نہ سمجھیں۔",
    "Rajab ke kisi khaas roze ya namaz ko farz ya lazmi Sunnat na batayein." to "رجب کے کسی خاص روزے یا نماز کو فرض یا لازمی سنت نہ بتائیں۔",
    "27 Rajab ko Me'raj ki qat'i tareekh ya kisi khaas namaz/roze ko lazmi Sunnat na samjhein." to "27 رجب کو معراج کی قطعی تاریخ یا کسی خاص نماز/روزے کو لازمی سنت نہ سمجھیں۔",
    "Patakhe, aatish-bazi, logon ko takleef aur halwe ko zaruri rasm samajhne se bachein." to "پٹاخے، آتش بازی، لوگوں کو تکلیف اور حلوے کو ضروری رسم سمجھنے سے بچیں۔",
    "Roze mein jhoot, gheebat, jhagra aur namaz ki kotahi se bachein." to "روزے میں جھوٹ، غیبت، جھگڑا اور نماز کی کوتاہی سے بچیں۔",
    "Sirf 27vin raat par bharosa karke aakhri ashre ki baqi taaq raatein na chhorein." to "صرف 27ویں رات پر بھروسا کر کے آخری عشرے کی باقی طاق راتیں نہ چھوڑیں۔",
    "Eid ke din roza na rakhein; namaz aur Sadqa-e-Fitr mein kotahi na karein." to "عید کے دن روزہ نہ رکھیں؛ نماز اور صدقہ فطر میں کوتاہی نہ کریں۔",
    "Hajj mein wuquf-e-Arafat karne wale par ghair-haji ke roze ki naseehat na thopein." to "حج میں وقوفِ عرفات کرنے والے پر غیر حاجی کے روزے کی نصیحت نہ تھوپیں۔",
    "Qurbani ko numaaish ya riya na banayein; safai aur janwaron ke saath narmi ka khayal rakhein." to "قربانی کو نمائش یا ریا نہ بنائیں؛ صفائی اور جانوروں کے ساتھ نرمی کا خیال رکھیں۔",

    // Festival names/localNames and descriptions
    "Hijri Naya Saal" to "ہجری نیا سال",
    "First day of Muharram and the start of the new Hijri year." to "محرم کا پہلا دن اور نئے ہجری سال کی ابتدا۔",
    "Youm-e-Ashura" to "یومِ عاشورہ",
    "10th Muharram. Sunnah fast (with 9th or 11th); remembrance of Imam Husain (RA) and Karbala." to "10 محرم۔ سنت روزہ (9 یا 11 کے ساتھ)؛ امام حسین رضی اللہ عنہ اور کربلا کی یاد۔",
    "Eid Milad-un-Nabi ﷺ" to "عید میلاد النبی ﷺ",
    "12th Rabi' al-Awwal, commemoration of the birth of the Prophet Muhammad ﷺ." to "12 ربیع الاول، نبی محمد ﷺ کی ولادت کی یاد۔",
    "Giyarhvi Sharif" to "گیارہویں شریف",
    "11th Rabi' al-Thani, remembrance of Shaykh Abdul Qadir Jilani (RA)." to "11 ربیع الثانی، شیخ عبدالقادر جیلانی رحمۃ اللہ علیہ کی یاد۔",
    "Rajab ka Aaghaz" to "رجب کا آغاز",
    "Beginning of Rajab, one of the four sacred months." to "رجب کی ابتدا، چار حرمت والے مہینوں میں سے ایک۔",
    "Shab-e-Meraj" to "شبِ معراج",
    "Night of the Prophet's ﷺ night journey and ascension (27th Rajab)." to "نبی ﷺ کے شب کے سفر اور معراج کی رات (27 رجب)۔",
    "Shab-e-Baraat" to "شبِ برات",
    "Night of 15th Sha'ban, a night of forgiveness and worship." to "15 شعبان کی رات، مغفرت اور عبادت کی رات۔",
    "Ramzan ka Pehla Roza" to "رمضان کا پہلا روزہ",
    "First fast of Ramadan. Taraweeh begins the night before." to "رمضان کا پہلا روزہ۔ تراویح پچھلی رات سے شروع ہوتی ہے۔",
    "Shab-e-Qadr" to "شبِ قدر",
    "Commonly observed on the 27th night; seek it in the odd nights of the last ten." to "عام طور پر 27ویں رات منائی جاتی ہے؛ آخری عشرے کی طاق راتوں میں اسے تلاش کریں۔",
    "Eid-ul-Fitr" to "عید الفطر",
    "1st Shawwal. Pay Sadaqat al-Fitr before the Eid prayer." to "یکم شوال۔ عید کی نماز سے پہلے صدقہ فطر ادا کریں۔",
    "Youm-e-Arafah" to "یومِ عرفہ",
    "9th Dhu al-Hijjah. Fasting expiates sins of two years (Sahih Muslim)." to "9 ذوالحجہ۔ اس کا روزہ دو سال کے گناہوں کا کفارہ ہے (صحیح مسلم)۔",
    "Eid-ul-Adha (Bakra Eid)" to "عید الاضحیٰ (بقرعید)",
    "10th Dhu al-Hijjah. Qurbani from the 10th to the 12th." to "10 ذوالحجہ۔ قربانی 10 سے 12 تاریخ تک ہوتی ہے۔"
)

private val hindiWords = mapOf(
    "Prayer" to "नमाज़", "Schedule" to "समय-सारणी", "Location" to "स्थान", "Language" to "भाषा",
    "Today" to "आज", "Tomorrow" to "कल", "Next" to "अगली", "Time" to "समय", "Direction" to "दिशा",
    "Distance" to "दूरी", "Search" to "खोजें", "Source" to "स्रोत", "Sources" to "स्रोत",
    "Translation" to "अनुवाद", "Explanation" to "व्याख्या", "Settings" to "सेटिंग्स", "Privacy" to "गोपनीयता",
    "Enable" to "चालू करें", "Disable" to "बंद करें", "Save" to "सहेजें", "Cancel" to "रद्द करें",
    "Back" to "वापस", "Select" to "चुनें", "City" to "शहर", "Nearby" to "आस-पास", "Found" to "मिले",
    "Page" to "पृष्ठ", "Read" to "पढ़ें", "Listen" to "सुनें", "Chapter" to "अध्याय", "Daily" to "रोज़ाना",
    "Morning" to "सुबह", "Evening" to "शाम", "Days" to "दिन", "Hours" to "घंटे", "Minutes" to "मिनट"
)

private val urduWords = mapOf(
    "Prayer" to "نماز", "Schedule" to "اوقات", "Location" to "مقام", "Language" to "زبان",
    "Today" to "آج", "Tomorrow" to "کل", "Next" to "اگلی", "Time" to "وقت", "Direction" to "سمت",
    "Distance" to "فاصلہ", "Search" to "تلاش", "Source" to "ماخذ", "Sources" to "مآخذ",
    "Translation" to "ترجمہ", "Explanation" to "وضاحت", "Settings" to "ترتیبات", "Privacy" to "رازداری",
    "Enable" to "فعال کریں", "Disable" to "بند کریں", "Save" to "محفوظ کریں", "Cancel" to "منسوخ",
    "Back" to "واپس", "Select" to "منتخب کریں", "City" to "شہر", "Nearby" to "قریب", "Found" to "ملے",
    "Page" to "صفحہ", "Read" to "پڑھیں", "Listen" to "سنیں", "Chapter" to "باب", "Daily" to "روزانہ",
    "Morning" to "صبح", "Evening" to "شام", "Days" to "دن", "Hours" to "گھنٹے", "Minutes" to "منٹ"
)

/** Copy used throughout the Duas, Adhkar and Tasbeeh feature. */
private val duaFeaturePhrases = mapOf(
    "hinglish" to mapOf(
        "Adhkar" to "Azkar", "Tasbeeh" to "Tasbih", "All Duas" to "Saari Duaein",
        "Morning & Evening" to "Subah aur Shaam", "Prayer & Mosque" to "Namaz aur Masjid",
        "Sleep & Awakening" to "Sona aur Jaagna", "Anxiety & Distress" to "Pareshani aur Bechaini",
        "Travel & Leaving Home" to "Safar aur Ghar se Nikalna",
        "Forgiveness & Istighfar" to "Maafi aur Istighfar", "Parents & Family" to "Walidain aur Gharwale",
        "Food & Gratitude" to "Khana aur Shukr", "Quranic Duas" to "Qurani Duaein",
        "Protection & Healing" to "Hifazat aur Shifa", "Weather & Nature" to "Mausam aur Qudrat",
        "Death & Janaza" to "Maut aur Janaza", "Daily Life & Manners" to "Rozmarra aur Adaab",
        "Hajj & Umrah" to "Hajj aur Umrah", "Comprehensive Duas" to "Jaame Duaein", "Ramadan & Fasting" to "Ramazan aur Roza",
        "Morning (صباح)" to "Subah (صباح)", "Evening (مساء)" to "Shaam (مساء)",
        "Virtue / Benefit:" to "Fazilat / faida:",
        "Count your daily dhikr peacefully without competitive point scores" to "Roz ka zikr sukoon se ginein, bina kisi competition ke",
        "Reset" to "Dobara shuru", "Count" to "Ginti"
    ),
    "hi" to mapOf(
        "Adhkar" to "अज़कार", "Tasbeeh" to "तस्बीह", "All Duas" to "सभी दुआएँ",
        "Morning & Evening" to "सुबह और शाम", "Prayer & Mosque" to "नमाज़ और मस्जिद",
        "Sleep & Awakening" to "सोना और जागना", "Anxiety & Distress" to "परेशानी और बेचैनी",
        "Travel & Leaving Home" to "सफ़र और घर से निकलना",
        "Forgiveness & Istighfar" to "माफ़ी और इस्तिग़फ़ार", "Parents & Family" to "माता-पिता और परिवार",
        "Food & Gratitude" to "खाना और शुक्र", "Quranic Duas" to "क़ुरआनी दुआएँ",
        "Protection & Healing" to "हिफ़ाज़त और शिफ़ा", "Weather & Nature" to "मौसम और क़ुदरत",
        "Death & Janaza" to "मौत और जनाज़ा", "Daily Life & Manners" to "रोज़मर्रा और अदब",
        "Hajj & Umrah" to "हज और उमरा", "Comprehensive Duas" to "व्यापक दुआएँ", "Ramadan & Fasting" to "रमज़ान और रोज़ा",
        "Morning (صباح)" to "सुबह (صباح)", "Evening (مساء)" to "शाम (مساء)",
        "Virtue / Benefit:" to "फ़ज़ीलत / फ़ायदा:",
        "Count your daily dhikr peacefully without competitive point scores" to "बिना किसी मुकाबले के सुकून से अपना रोज़ाना ज़िक्र गिनें",
        "Reset" to "फिर शुरू करें", "Count" to "गिनती"
    ),
    "ur" to mapOf(
        "Adhkar" to "اذکار", "Tasbeeh" to "تسبیح", "All Duas" to "تمام دعائیں",
        "Morning & Evening" to "صبح اور شام", "Prayer & Mosque" to "نماز اور مسجد",
        "Sleep & Awakening" to "سونا اور جاگنا", "Anxiety & Distress" to "پریشانی اور بے چینی",
        "Travel & Leaving Home" to "سفر اور گھر سے نکلنا",
        "Forgiveness & Istighfar" to "مغفرت اور استغفار", "Parents & Family" to "والدین اور خاندان",
        "Food & Gratitude" to "کھانا اور شکر", "Quranic Duas" to "قرآنی دعائیں",
        "Protection & Healing" to "حفاظت اور شفا", "Weather & Nature" to "موسم اور قدرت",
        "Death & Janaza" to "موت اور جنازہ", "Daily Life & Manners" to "روزمرہ زندگی اور آداب",
        "Hajj & Umrah" to "حج اور عمرہ", "Comprehensive Duas" to "جامع دعائیں", "Ramadan & Fasting" to "رمضان اور روزہ",
        "Morning (صباح)" to "صبح (صباح)", "Evening (مساء)" to "شام (مساء)",
        "Virtue / Benefit:" to "فضیلت / فائدہ:",
        "Count your daily dhikr peacefully without competitive point scores" to "بغیر مقابلے کے سکون سے اپنا روزانہ ذکر شمار کریں",
        "Reset" to "دوبارہ شروع کریں", "Count" to "شمار"
    )
)

private val hinglishPhrases = mapOf(
    "Settings & Privacy" to "Settings aur Privacy", "Settings" to "Settings",
    "App Language" to "App ki Language", "Choose language" to "Language chunein",
    "Choose app language" to "App ki language chunein", "Close" to "Band karein",
    "Home" to "Home", "Ramzan" to "Ramzan", "Quran" to "Quran", "Masjid" to "Masjid", "Community Masjid" to "Community Masjid",
    "Duas" to "Duaein", "Festivals" to "Tyohar",
    "The selected language applies across the app." to "Chuni hui language poore app mein lagu hoti hai.",
    "Prayer Calculation Settings" to "Namaz ke waqt ki settings",
    "Calculation Authority" to "Waqt calculate karne ka tareeqa",
    "Asr Juristic Calculation" to "Asr calculate karne ka tareeqa",
    "Hijri Moon Sighting Adjustment" to "Hijri chaand ki date adjust karein",
    "Prayer Notifications Service" to "Namaz notification service",
    "Enable Prayer Alerts" to "Namaz alerts chalu karein",
    "Privacy & Offline Architecture" to "Privacy aur offline system",
    "Content Governance & Sources" to "Content aur sources",
    "Today's Prayer Schedule" to "Aaj ki namaz ka waqt",
    "Calculated Salah Start" to "Namaz shuru hone ka waqt",
    "Explore The Noble Quran" to "Quran-e-Pak padhein", "Daily Adhkar" to "Rozana azkar",
    "Qibla Direction" to "Qibla ki direction",
    "Major Cities" to "Bade shehar", "Custom Lat / Long" to "Apna Lat / Long",
    "Lessons" to "Sabak", "AI Assistant" to "AI madadgaar", "Read" to "Padhein", "Audio" to "Audio",
    "Surah wise" to "Surah ke hisaab se", "Para wise" to "Para ke hisaab se",
    "Morning" to "Subah", "Evening" to "Shaam", "Done" to "Ho gaya",
    "Gentle Digital Tasbeeh" to "Digital Tasbih", "Goal" to "Maqsad",
    "Verified Timetables" to "Verified timetable",
    "Congregational Iqamah Timings" to "Jamaat ki iqamah ka waqt",
    "Friday Jummah Sessions" to "Jumma ki namaz", "Suggest Edit" to "Sudhaar batayein",
    "Why This Time?" to "Yeh waqt kyun?", "Distance to Kaaba" to "Kaaba se doori",
    "Magnetic Sensor Calibration" to "Magnetic sensor calibration",
    "Ramzan Timetable" to "Ramzan ka timetable", "Sehri" to "Sehri", "Iftar" to "Iftar",
    "Online dates" to "Online tareekhein", "Prayer Times Alerts" to "Namaz ke waqt ke alerts",
    "Peaceful notifications when each prayer time enters" to "Har namaz ka waqt shuru hone par notification",
    "Time for" to "Waqt ho gaya:", "Salah has commenced for" to "Namaz shuru ho gayi—jagah:",
    "Salah time has commenced at" to "Namaz ka waqt shuru ho gaya:",
    "Indeed, prayer has been decreed upon the believers a decree of specified times." to "Beshak namaz imaan walon par muqarrar waqton mein farz ki gayi hai.",
    "Retry" to "Dobara try karein",
    "We have entered the morning and the sovereignty belongs to Allah, and all praise is for Allah. There is no deity except Allah alone without partner." to "Humne subah ki aur saari badshahat Allah hi ki hai. Saari tareef Allah ke liye hai. Allah ke siwa koi mabood nahi, woh akela hai, uska koi shareek nahi.",
    "We have entered the evening and sovereignty belongs to Allah, and all praise is for Allah. There is no deity except Allah alone without partner." to "Humne shaam ki aur saari badshahat Allah hi ki hai. Saari tareef Allah ke liye hai. Allah ke siwa koi mabood nahi, woh akela hai, uska koi shareek nahi.",
    "In the name of Allah, with whose name nothing on earth or in heaven can cause harm, and He is the All-Hearing, All-Knowing." to "Allah ke naam se, jiske naam ke saath zameen aur aasmaan mein koi cheez nuqsaan nahi pahuncha sakti, aur woh sab kuch sunne wala, jaanne wala hai.",
    "I seek refuge in the perfect words of Allah from the evil of what He has created." to "Main Allah ke kaamil kalimaat ki panaah maangta hoon uski paida ki hui cheezon ke shar se.",
    "Glory be to Allah and His is the praise." to "Allah paak hai apni tareef ke saath."
)

private val hinglishWords = mapOf(
    "Prayer" to "Namaz", "Schedule" to "Waqt", "Location" to "Jagah",
    "Language" to "Language", "Today" to "Aaj", "Tomorrow" to "Kal",
    "Next" to "Agli", "Time" to "Waqt", "Direction" to "Direction",
    "Distance" to "Doori", "Search" to "Khojein", "Source" to "Source",
    "Translation" to "Tarjuma", "Explanation" to "Samjhaai",
    "Privacy" to "Privacy", "Enable" to "Chalu karein", "Disable" to "Band karein",
    "Save" to "Save karein", "Cancel" to "Cancel karein", "Back" to "Wapas",
    "Select" to "Chunein", "City" to "Shehar", "Nearby" to "Aas-paas",
    "Page" to "Safha", "Listen" to "Sunein", "Daily" to "Rozana",
    "Days" to "Din", "Hours" to "Ghante", "Minutes" to "Minute"
)

/**
 * One app language: whole reviewed phrases, plus single words that may appear inside
 * dynamic labels ("Next Prayer", "12 Minutes").
 */
internal class UiLanguagePack(
    val phrases: Map<String, String>,
    val words: Map<String, String> = emptyMap()
) {
    private val wordsByLowercase = words.mapKeys { it.key.lowercase() }

    // A single longest-first pass: translated output is never re-translated, and keys only
    // match whole English words, so "Already" or "Masjids" are not broken mid-word.
    private val fragmentPattern: Regex by lazy {
        val alternatives = (phrases.keys.map { it to false } + words.keys.map { it to true })
            .sortedByDescending { it.first.length }
            .joinToString("|") { (key, ignoreCase) ->
                if (ignoreCase) "(?i:${Regex.escape(key)})" else Regex.escape(key)
            }
        Regex("(?<![A-Za-z0-9])(?:$alternatives)(?![A-Za-z0-9])")
    }

    fun localize(text: String): String {
        phrases[text]?.let { return it }
        return fragmentPattern.replace(text) { match ->
            phrases[match.value] ?: wordsByLowercase[match.value.lowercase()] ?: match.value
        }
    }

    fun withAdditionalWords(additionalWords: Map<String, String>): UiLanguagePack =
        UiLanguagePack(phrases, words + additionalWords)

    fun withAdditionalPhrases(additionalPhrases: Map<String, String>): UiLanguagePack =
        UiLanguagePack(phrases + additionalPhrases, words)
}

// The older regional maps lost their complete Community Masjid entry. Compose it from the
// localized feature qualifier and each pack's existing Masjid label.
private val communityWords = mapOf(
    "as" to "কমিউনিটি", "bn" to "কমিউনিটি", "brx" to "कम्युनिटी",
    "doi" to "कम्युनिटी", "gu" to "કમ્યુનિટી", "kn" to "ಕಮ್ಯುನಿಟಿ",
    "ks" to "کمیونٹی", "kok" to "कम्युनिटी", "mai" to "कम्युनिटी",
    "ml" to "കമ്മ്യൂണിറ്റി", "mni" to "কমিউনিটি", "mr" to "कम्युनिटी",
    "ne" to "कम्युनिटी", "or" to "କମ୍ୟୁନିଟି", "pa" to "ਕਮਿਊਨਿਟੀ",
    "sa" to "समुदाय", "sat" to "ᱠᱚᱢᱭᱩᱱᱤᱴᱤ", "sd" to "ڪميونٽي",
    "ta" to "கம்யூனிட்டி", "te" to "కమ్యూనిటీ",
    "ar" to "المجتمع", "fa" to "اجتماع", "tr" to "Topluluk", "id" to "Komunitas",
    "fr" to "Communauté", "es" to "Comunidad",
    "ru" to "Сообщество", "de" to "Gemeinschafts", "pt" to "Comunitária", "zh" to "社区",
    "sw" to "Jamii", "ha" to "Al'umma", "ms" to "Komuniti", "it" to "Comunità",
    "nl" to "Gemeenschaps", "vi" to "Cộng đồng", "th" to "ชุมชน", "tl" to "Komunidad",
    "ps" to "ټولنیز",
    "ja" to "コミュニティ", "ko" to "커뮤니티", "el" to "Κοινοτικό", "pl" to "Społecznościowy",
    "uk" to "Громадська", "so" to "Bulshada", "am" to "የማህበረሰብ", "uz" to "Jamoat",
    "he" to "קהילתי", "si" to "ප්‍රජා"
)

private fun regionalPack(code: String, pack: UiLanguagePack): UiLanguagePack = pack
    .withAdditionalWords(controlWordsByLanguage.getValue(code))
    .withAdditionalPhrases(
        duaScreenPhrases.getValue(code) + mapOf(
            "Community Masjid" to "${communityWords.getValue(code)} ${pack.phrases.getValue("Masjid")}"
        )
    )

private val languagePacks: Map<String, UiLanguagePack> = mapOf(
    "hinglish" to UiLanguagePack(
        dailyToolPhrases.getValue("hinglish") + quizPhrases.getValue("hinglish") + quizGeneratorPhrases.getValue("hinglish") + hajjUmrahPhrases.getValue("hinglish") + prayerTrackerPhrases.getValue("hinglish") + hinglishPhrases + duaFeaturePhrases.getValue("hinglish") + duaScreenPhrases.getValue("hinglish") +
            extendedUiPhrases.getValue("hinglish"),
        hinglishWords + controlWordsByLanguage.getValue("hinglish")
    ),
    "hi" to UiLanguagePack(
        dailyToolPhrases.getValue("hi") + quizPhrases.getValue("hi") + quizGeneratorPhrases.getValue("hi") + hajjUmrahPhrases.getValue("hi") + prayerTrackerPhrases.getValue("hi") + hindiPhrases + duaFeaturePhrases.getValue("hi") + duaScreenPhrases.getValue("hi") +
            extendedUiPhrases.getValue("hi"),
        hindiWords + controlWordsByLanguage.getValue("hi")
    ),
    "ur" to UiLanguagePack(
        dailyToolPhrases.getValue("ur") + quizPhrases.getValue("ur") + quizGeneratorPhrases.getValue("ur") + hajjUmrahPhrases.getValue("ur") + prayerTrackerPhrases.getValue("ur") + urduPhrases + duaFeaturePhrases.getValue("ur") + duaScreenPhrases.getValue("ur") +
            extendedUiPhrases.getValue("ur"),
        urduWords + controlWordsByLanguage.getValue("ur")
    ),
    "as" to regionalPack("as", assamesePack),
    "bn" to regionalPack("bn", bengaliPack),
    "brx" to regionalPack("brx", bodoPack),
    "doi" to regionalPack("doi", dogriPack),
    "gu" to regionalPack("gu", gujaratiPack),
    "kn" to regionalPack("kn", kannadaPack),
    "ks" to regionalPack("ks", kashmiriPack),
    "kok" to regionalPack("kok", konkaniPack),
    "mai" to regionalPack("mai", maithiliPack),
    "ml" to regionalPack("ml", malayalamPack),
    "mni" to regionalPack("mni", manipuriPack),
    "mr" to regionalPack("mr", marathiPack),
    "ne" to regionalPack("ne", nepaliPack),
    "or" to regionalPack("or", odiaPack),
    "pa" to regionalPack("pa", punjabiPack),
    "sa" to regionalPack("sa", sanskritPack),
    "sat" to regionalPack("sat", santaliPack),
    "sd" to regionalPack("sd", sindhiPack),
    "ta" to regionalPack("ta", tamilPack),
    "te" to regionalPack("te", teluguPack),
    "ar" to regionalPack("ar", arabicPack),
    "fa" to regionalPack("fa", persianPack),
    "tr" to regionalPack("tr", turkishPack),
    "id" to regionalPack("id", indonesianPack),
    "fr" to regionalPack("fr", frenchPack),
    "es" to regionalPack("es", spanishPack),
    "ru" to regionalPack("ru", russianPack),
    "de" to regionalPack("de", germanPack),
    "pt" to regionalPack("pt", portuguesePack),
    "zh" to regionalPack("zh", chinesePack),
    "sw" to regionalPack("sw", swahiliPack),
    "ha" to regionalPack("ha", hausaPack),
    "ms" to regionalPack("ms", malayPack),
    "it" to regionalPack("it", italianPack),
    "nl" to regionalPack("nl", dutchPack),
    "vi" to regionalPack("vi", vietnamesePack),
    "th" to regionalPack("th", thaiPack),
    "tl" to regionalPack("tl", filipinoPack),
    "ps" to regionalPack("ps", pashtoPack),
    "ja" to regionalPack("ja", japanesePack),
    "ko" to regionalPack("ko", koreanPack),
    "el" to regionalPack("el", greekPack),
    "pl" to regionalPack("pl", polishPack),
    "uk" to regionalPack("uk", ukrainianPack),
    "so" to regionalPack("so", somaliPack),
    "am" to regionalPack("am", amharicPack),
    "uz" to regionalPack("uz", uzbekPack),
    "he" to regionalPack("he", hebrewPack),
    "si" to regionalPack("si", sinhalaPack)
)

/**
 * The frozen set of core UI copy keys every supported language must translate. This is a fixed
 * snapshot, not `hindiPhrases.keys` directly: Hindi (and other languages) may carry additional
 * long-form content (Lessons, Festival guidance, Assistant responses) beyond this core set, and
 * that extra content must not become a universal parity requirement for every other language.
 */
private val coreUiReferenceKeys: Set<String> = setOf(
    "Settings & Privacy", "App Language", "Choose language", "Choose app language", "Close",
    "Home", "Ramzan", "Quran", "Masjid", "Community Masjid",
    "Duas", "Festivals", "Settings",
    "Prayer Calculation Settings",
    "Calculation Authority", "Asr Juristic Calculation",
    "Hijri Moon Sighting Adjustment",
    "Prayer Notifications Service", "Enable Prayer Alerts",
    "Privacy & Offline Architecture",
    "Content Governance & Sources",
    "Today's Prayer Schedule", "Calculated Salah Start",
    "Explore The Noble Quran", "Daily Adhkar",
    "Morning & Evening", "Qibla Direction",
    "Major Cities", "Custom Lat / Long",
    "Lessons", "AI Assistant", "Read", "Audio",
    "Surah wise", "Para wise",
    "Morning", "Evening", "Done",
    "Gentle Digital Tasbeeh", "Goal",
    "Verified Timetables", "Congregational Iqamah Timings",
    "Friday Jummah Sessions", "Suggest Edit",
    "Why This Time?", "Distance to Kaaba",
    "Magnetic Sensor Calibration", "Ramzan Timetable",
    "Sehri", "Iftar", "Online dates",
    "Prayer Times Alerts",
    "Peaceful notifications when each prayer time enters",
    "Time for", "Salah has commenced for",
    "Salah time has commenced at",
    "Indeed, prayer has been decreed upon the believers a decree of specified times.",
    "The selected language applies across the app.",
    "Retry"
)

/** The Hindi pack is the reference: every other language must translate the same core UI copy. */
internal val referenceUiPhraseKeys: Set<String> =
    coreUiReferenceKeys + duaFeaturePhrases.getValue("hi").keys + duaScreenPhrases.getValue("hi").keys

internal fun uiLanguagePack(language: String): UiLanguagePack? = languagePacks[language]

/** Language lookup shared by background receivers/services where no Compose state exists. */
fun persistedAppLanguage(context: Context): String {
    val saved = context.getSharedPreferences("deen_app_preferences", Context.MODE_PRIVATE)
        .getString("language_code", "en")
    return saved?.takeIf { it == "en" || languagePacks.containsKey(it) } ?: "en"
}

fun localizeUiText(text: String, language: String): String {
    if (language == "en" || text.isBlank()) return text
    if (text == "More") return moreNavigationLabels[language] ?: text
    homeExtraTimesLabels[language]?.get(text)?.let { return it }
    homeRefreshLabels[language]?.get(text)?.let { return it }
    homeDashboardLabels[language]?.get(text)?.let { return it }
    homeShortcutLabels[language]?.get(text)?.let { return it }
    return languagePacks[language]?.localize(text) ?: text
}

/** Copy introduced by the refreshed Home dashboard. Other languages fall back to word-level packs. */
private val homeRefreshLabels = mapOf(
    "hinglish" to mapOf(
        "Next prayer" to "Agli namaz",
        "Starts in" to "Shuru hone mein",
        "Calculating prayer times…" to "Namaz ke auqaat nikale ja rahe hain…",
        "Today's progress" to "Aaj ki progress",
        "Salah" to "Namaz",
        "Continue Quran" to "Quran jaari rakhein",
        "View all" to "Sab dekhein",
        "Show less" to "Kam dikhayein",
        "Show more" to "Aur dikhayein",
        "Sehri, Ishraq, Chasht, Zawal and Iftar" to "Sehri, Ishraq, Chasht, Zawal aur Iftar",
        "Finish eating before this time" to "Is waqt se pehle khana khatam karein",
        "About 20 minutes after sunrise" to "Suraj nikalne ke taqriban 20 minute baad",
        "Mid-morning, before Zawal" to "Din chadhe, Zawal se pehle",
        "Avoid praying at this time" to "Is waqt namaz na padhein",
        "Open your fast at Maghrib" to "Maghrib par roza kholein",
        "Prayer times unavailable" to "Namaz ke auqaat dastiyab nahin",
        "Turn on location so we can find prayer times for where you are." to "Location on karein taaki aapke shehar ke auqaat mil sakein.",
        "We couldn't work out prayer times for this location. Please try again." to "Is jagah ke liye namaz ke auqaat nahin nikal sake. Dobara koshish karein.",
        "Try again" to "Dobara koshish karein",
        "Location settings" to "Location settings",
        "Calculation details" to "Hisaab ki tafseel",
        "Allow location" to "Location ki ijazat dein",
        "Turn on location" to "Location on karein",
        "pages" to "safhe",
        "prayed" to "padhi"
    ),
    "hi" to mapOf(
        "Next prayer" to "अगली नमाज़",
        "Starts in" to "शुरू होने में",
        "Calculating prayer times…" to "नमाज़ के समय निकाले जा रहे हैं…",
        "Today's progress" to "आज की प्रगति",
        "Salah" to "नमाज़",
        "Continue Quran" to "क़ुरआन जारी रखें",
        "View all" to "सभी देखें",
        "Show less" to "कम दिखाएँ",
        "Show more" to "और दिखाएँ",
        "Sehri, Ishraq, Chasht, Zawal and Iftar" to "सहरी, इशराक़, चाश्त, ज़वाल और इफ़्तार",
        "Finish eating before this time" to "इस समय से पहले खाना समाप्त करें",
        "About 20 minutes after sunrise" to "सूर्योदय के लगभग 20 मिनट बाद",
        "Mid-morning, before Zawal" to "दिन चढ़े, ज़वाल से पहले",
        "Avoid praying at this time" to "इस समय नमाज़ न पढ़ें",
        "Open your fast at Maghrib" to "मग़रिब पर रोज़ा खोलें",
        "Prayer times unavailable" to "नमाज़ के समय उपलब्ध नहीं",
        "Turn on location so we can find prayer times for where you are." to "लोकेशन चालू करें ताकि आपकी जगह के नमाज़ के समय मिल सकें।",
        "We couldn't work out prayer times for this location. Please try again." to "इस जगह के लिए नमाज़ के समय नहीं निकल सके। कृपया फिर से कोशिश करें।",
        "Try again" to "फिर से कोशिश करें",
        "Location settings" to "लोकेशन सेटिंग्स",
        "Calculation details" to "गणना का विवरण",
        "Allow location" to "लोकेशन की अनुमति दें",
        "Turn on location" to "लोकेशन चालू करें",
        "pages" to "पन्ने",
        "prayed" to "पढ़ीं"
    ),
    "ur" to mapOf(
        "Next prayer" to "اگلی نماز",
        "Starts in" to "شروع ہونے میں",
        "Calculating prayer times…" to "نماز کے اوقات نکالے جا رہے ہیں…",
        "Today's progress" to "آج کی پیش رفت",
        "Salah" to "نماز",
        "Continue Quran" to "قرآن جاری رکھیں",
        "View all" to "سب دیکھیں",
        "Show less" to "کم دکھائیں",
        "Show more" to "مزید دکھائیں",
        "Sehri, Ishraq, Chasht, Zawal and Iftar" to "سحری، اشراق، چاشت، زوال اور افطار",
        "Finish eating before this time" to "اس وقت سے پہلے کھانا ختم کریں",
        "About 20 minutes after sunrise" to "طلوعِ آفتاب کے تقریباً ۲۰ منٹ بعد",
        "Mid-morning, before Zawal" to "دن چڑھے، زوال سے پہلے",
        "Avoid praying at this time" to "اس وقت نماز نہ پڑھیں",
        "Open your fast at Maghrib" to "مغرب پر روزہ افطار کریں",
        "Prayer times unavailable" to "نماز کے اوقات دستیاب نہیں",
        "Turn on location so we can find prayer times for where you are." to "لوکیشن آن کریں تاکہ آپ کی جگہ کے نماز کے اوقات مل سکیں۔",
        "We couldn't work out prayer times for this location. Please try again." to "اس جگہ کے لیے نماز کے اوقات نہیں نکل سکے۔ دوبارہ کوشش کریں۔",
        "Try again" to "دوبارہ کوشش کریں",
        "Location settings" to "لوکیشن کی ترتیبات",
        "Calculation details" to "حساب کی تفصیل",
        "Allow location" to "لوکیشن کی اجازت دیں",
        "Turn on location" to "لوکیشن آن کریں",
        "pages" to "صفحات",
        "prayed" to "ادا کیں"
    )
)

private val homeExtraTimesLabels = mapOf(
    "hinglish" to mapOf(
        "Other Islamic Times" to "Deeni auqaat",
        "Sehri ends" to "Sehri khatam",
        "Ishraq (approx.)" to "Ishraq (taqriban)",
        "Chasht (approx.)" to "Chasht (taqriban)",
        "Zawal (solar noon)" to "Zawal (nisf-un-nahar)",
        "Ishraq is about 20 minutes after sunrise; Chasht is a mid-morning guide. Check local guidance for prayer windows." to
            "Ishraq suraj nikalne ke taqriban 20 minute baad; Chasht subah ka andazati waqt hai. Muqami rehnumai dekhein."
    ),
    "hi" to mapOf(
        "Other Islamic Times" to "अन्य इस्लामी समय",
        "Sehri ends" to "सहरी समाप्त",
        "Ishraq (approx.)" to "इशराक़ (लगभग)",
        "Chasht (approx.)" to "चाश्त (लगभग)",
        "Zawal (solar noon)" to "ज़वाल (सौर दोपहर)",
        "Ishraq is about 20 minutes after sunrise; Chasht is a mid-morning guide. Check local guidance for prayer windows." to
            "इशराक़ सूर्योदय के लगभग 20 मिनट बाद है; चाश्त सुबह का अनुमानित समय है। स्थानीय मार्गदर्शन देखें।"
    ),
    "ur" to mapOf(
        "Other Islamic Times" to "دیگر اسلامی اوقات",
        "Sehri ends" to "سحری ختم",
        "Ishraq (approx.)" to "اشراق (تقریباً)",
        "Chasht (approx.)" to "چاشت (تقریباً)",
        "Zawal (solar noon)" to "زوال (نصف النہار)",
        "Ishraq is about 20 minutes after sunrise; Chasht is a mid-morning guide. Check local guidance for prayer windows." to
            "اشراق طلوع آفتاب کے تقریباً ۲۰ منٹ بعد ہے؛ چاشت صبح کا اندازاً وقت ہے۔ مقامی رہنمائی دیکھیں۔"
    )
)

private val moreNavigationLabels = mapOf(
    "hinglish" to "Aur",
    "hi" to "और",
    "ur" to "مزید",
    "as" to "অধিক",
    "bn" to "আরও",
    "doi" to "होर",
    "gu" to "વધુ",
    "kn" to "ಇನ್ನಷ್ಟು",
    "ks" to "مزید",
    "kok" to "आनीक",
    "mai" to "आओर",
    "ml" to "കൂടുതൽ",
    "mni" to "ꯑꯇꯩ",
    "mr" to "अधिक",
    "ne" to "थप",
    "or" to "ଅଧିକ",
    "pa" to "ਹੋਰ",
    "sa" to "अधिकम्",
    "sd" to "وڌيڪ",
    "ta" to "மேலும்",
    "te" to "మరిన్ని"
)

private val moreSubtitleKeys = listOf(
    "Tools & resources",
    "Direction of the Kaaba",
    "Hijri dates & key days",
    "Sehri & Iftar times",
    "Sayings of the Prophet ﷺ",
    "Voice-only recitations",
    "Islamic occasions & aamal",
    "Language, alerts & prayer times",
    "Log salah & Qaza balance",
    "Daily pages & khatm",
    "Estimate your 2.5% Zakat",
    "Step-by-step guide",
    "Private cycle-aware log",
    "Test your knowledge"
)

/** Values follow [moreSubtitleKeys] order. Languages missing here show no subtitle. */
private val moreSubtitles: Map<String, List<String>> = mapOf(
    "hinglish" to listOf("Tools aur resources", "Kaaba ki taraf rukh", "Hijri tareekhein aur khaas din",
        "Sehri aur Iftar ka waqt", "Nabi ﷺ ke irshadaat", "Sirf awaaz, bina music",
        "Islami mauqe aur aamal", "Zabaan, alerts aur namaz ke auqaat",
        "Namaz aur qaza ka hisaab", "Rozana safhe aur khatm", "Apni 2.5% zakat ka andaza",
        "Qadam ba qadam tareeqa", "Private, cycle ke hisaab se log", "Apni maloomat parkhein"),
    "hi" to listOf("टूल्स और संसाधन", "काबा की दिशा", "हिजरी तारीख़ें और ख़ास दिन",
        "सहरी और इफ़्तार का समय", "नबी ﷺ के इरशादात", "सिर्फ़ आवाज़, बिना संगीत",
        "इस्लामी मौक़े और आमाल", "भाषा, सूचनाएँ और नमाज़ का समय",
        "नमाज़ और क़ज़ा का हिसाब", "रोज़ के पन्ने और ख़त्म", "अपनी 2.5% ज़कात का अनुमान",
        "क़दम-ब-क़दम तरीक़ा", "निजी, माहवारी के हिसाब से लॉग", "अपनी जानकारी परखें"),
    "ur" to listOf("ٹولز اور وسائل", "کعبہ کی سمت", "ہجری تاریخیں اور خاص دن",
        "سحری اور افطار کے اوقات", "نبی ﷺ کے ارشادات", "صرف آواز، بغیر موسیقی",
        "اسلامی مواقع اور اعمال", "زبان، اطلاعات اور نماز کے اوقات",
        "نماز اور قضا کا حساب", "روزانہ صفحات اور ختم", "اپنی ڈھائی فیصد زکوٰۃ کا اندازہ",
        "قدم بہ قدم طریقہ", "نجی، ایام کے حساب سے ریکارڈ", "اپنی معلومات پرکھیں"),
    "as" to listOf("সঁজুলি আৰু সম্পদ", "কাবাৰ দিশ", "হিজৰী তাৰিখ আৰু বিশেষ দিন",
        "ছেহৰী আৰু ইফতাৰৰ সময়", "নবী ﷺৰ বাণী", "কেৱল কণ্ঠ, বাদ্যযন্ত্ৰ নোহোৱাকৈ",
        "ইছলামী উপলক্ষ আৰু আমল", "ভাষা, সতৰ্কবাণী আৰু নামাজৰ সময়"),
    "bn" to listOf("টুলস ও রিসোর্স", "কাবার দিক", "হিজরি তারিখ ও বিশেষ দিন",
        "সেহরি ও ইফতারের সময়", "নবী ﷺ-এর বাণী", "শুধু কণ্ঠ, বাদ্যযন্ত্র ছাড়া",
        "ইসলামি উপলক্ষ ও আমল", "ভাষা, সতর্কতা ও নামাজের সময়"),
    "gu" to listOf("સાધનો અને સંસાધનો", "કાબાની દિશા", "હિજરી તારીખો અને ખાસ દિવસો",
        "સેહરી અને ઇફ્તારનો સમય", "નબી ﷺ ના ફરમાન", "માત્ર અવાજ, સંગીત વગર",
        "ઇસ્લામી પ્રસંગો અને આમાલ", "ભાષા, સૂચનાઓ અને નમાઝનો સમય"),
    "kn" to listOf("ಉಪಕರಣಗಳು ಮತ್ತು ಸಂಪನ್ಮೂಲಗಳು", "ಕಾಬಾದ ದಿಕ್ಕು", "ಹಿಜರಿ ದಿನಾಂಕಗಳು ಮತ್ತು ವಿಶೇಷ ದಿನಗಳು",
        "ಸೆಹ್ರಿ ಮತ್ತು ಇಫ್ತಾರ್ ಸಮಯ", "ಪ್ರವಾದಿ ﷺ ಅವರ ವಚನಗಳು", "ಕೇವಲ ಧ್ವನಿ, ಸಂಗೀತವಿಲ್ಲದೆ",
        "ಇಸ್ಲಾಮಿಕ್ ಸಂದರ್ಭಗಳು ಮತ್ತು ಆಮಾಲ್", "ಭಾಷೆ, ಸೂಚನೆಗಳು ಮತ್ತು ನಮಾಜ್ ಸಮಯ"),
    "ml" to listOf("ഉപകരണങ്ങളും വിഭവങ്ങളും", "കഅ്ബയുടെ ദിശ", "ഹിജ്റ തീയതികളും പ്രത്യേക ദിനങ്ങളും",
        "സഹ്‌രി, ഇഫ്താർ സമയം", "നബി ﷺ യുടെ വചനങ്ങൾ", "ശബ്ദം മാത്രം, സംഗീതമില്ലാതെ",
        "ഇസ്‌ലാമിക സന്ദർഭങ്ങളും അമലുകളും", "ഭാഷ, അറിയിപ്പുകൾ, നമസ്കാര സമയം"),
    "mr" to listOf("साधने आणि संसाधने", "काबाची दिशा", "हिजरी तारखा आणि खास दिवस",
        "सहरी आणि इफ्तारची वेळ", "नबी ﷺ यांची वचने", "फक्त आवाज, संगीताशिवाय",
        "इस्लामी प्रसंग आणि आमाल", "भाषा, सूचना आणि नमाजच्या वेळा"),
    "ne" to listOf("उपकरण र स्रोतहरू", "काबाको दिशा", "हिजरी मितिहरू र विशेष दिन",
        "सहरी र इफ्तारको समय", "नबी ﷺ का वचनहरू", "आवाज मात्र, संगीत बिना",
        "इस्लामी अवसर र आमाल", "भाषा, सूचना र नमाजको समय"),
    "or" to listOf("ଉପକରଣ ଓ ସମ୍ବଳ", "କାବାର ଦିଗ", "ହିଜରୀ ତାରିଖ ଓ ବିଶେଷ ଦିନ",
        "ସେହରୀ ଓ ଇଫତାର ସମୟ", "ନବୀ ﷺ ଙ୍କ ବାଣୀ", "କେବଳ ସ୍ୱର, ସଙ୍ଗୀତ ବିନା",
        "ଇସଲାମିକ ଅବସର ଓ ଆମାଲ", "ଭାଷା, ସୂଚନା ଓ ନମାଜ ସମୟ"),
    "pa" to listOf("ਟੂਲ ਅਤੇ ਸਰੋਤ", "ਕਾਬਾ ਦੀ ਦਿਸ਼ਾ", "ਹਿਜਰੀ ਤਾਰੀਖਾਂ ਅਤੇ ਖਾਸ ਦਿਨ",
        "ਸਹਿਰੀ ਅਤੇ ਇਫ਼ਤਾਰ ਦਾ ਸਮਾਂ", "ਨਬੀ ﷺ ਦੇ ਫ਼ਰਮਾਨ", "ਸਿਰਫ਼ ਆਵਾਜ਼, ਸੰਗੀਤ ਤੋਂ ਬਿਨਾਂ",
        "ਇਸਲਾਮੀ ਮੌਕੇ ਅਤੇ ਆਮਾਲ", "ਭਾਸ਼ਾ, ਸੂਚਨਾਵਾਂ ਅਤੇ ਨਮਾਜ਼ ਦਾ ਸਮਾਂ"),
    "sd" to listOf("اوزار ۽ وسيلا", "ڪعبي جو رخ", "هجري تاريخون ۽ خاص ڏينهن",
        "سحري ۽ افطار جو وقت", "نبي ﷺ جا ارشاد", "رڳو آواز، موسيقي کان سواءِ",
        "اسلامي موقعا ۽ عمل", "ٻولي، اطلاع ۽ نماز جا وقت"),
    "ta" to listOf("கருவிகள் & வளங்கள்", "கஅபாவின் திசை", "ஹிஜ்ரி தேதிகள் & சிறப்பு நாட்கள்",
        "ஸஹர் & இஃப்தார் நேரம்", "நபி ﷺ அவர்களின் பொன்மொழிகள்", "குரல் மட்டும், இசை இல்லை",
        "இஸ்லாமிய நிகழ்வுகள் & அமல்கள்", "மொழி, அறிவிப்புகள் & தொழுகை நேரம்"),
    "te" to listOf("సాధనాలు & వనరులు", "కాబా దిశ", "హిజ్రీ తేదీలు & ప్రత్యేక రోజులు",
        "సెహ్రీ & ఇఫ్తార్ సమయాలు", "ప్రవక్త ﷺ వచనాలు", "కేవలం స్వరం, సంగీతం లేకుండా",
        "ఇస్లామిక్ సందర్భాలు & ఆమాల్", "భాష, హెచ్చరికలు & నమాజ్ సమయాలు")
)

/**
 * Secondary copy on the More tab. Returns null when the language has no reviewed translation
 * so the screen can hide the line rather than mix English into a localized layout.
 */
fun localizedMoreSubtitle(text: String, language: String): String? {
    if (language == "en") return text
    val index = moreSubtitleKeys.indexOf(text).takeIf { it >= 0 } ?: return null
    return moreSubtitles[language]?.getOrNull(index)
}

private val homeDashboardLabels = mapOf(
    "hinglish" to mapOf("Quick access" to "Zaroori shortcuts", "Sunrise" to "Sunrise"),
    "hi" to mapOf("Quick access" to "त्वरित पहुँच", "Sunrise" to "सूर्योदय"),
    "ur" to mapOf("Quick access" to "فوری رسائی", "Sunrise" to "طلوعِ آفتاب"),
    "as" to mapOf("Quick access" to "দ্ৰুত প্ৰৱেশ", "Sunrise" to "সূৰ্যোদয়"),
    "bn" to mapOf("Quick access" to "দ্রুত প্রবেশ", "Sunrise" to "সূর্যোদয়"),
    "doi" to mapOf("Quick access" to "फौरी पुज्ज", "Sunrise" to "सूरज चढ़ना"),
    "gu" to mapOf("Quick access" to "ઝડપી ઍક્સેસ", "Sunrise" to "સૂર્યોદય"),
    "kn" to mapOf("Quick access" to "ತ್ವರಿತ ಪ್ರವೇಶ", "Sunrise" to "ಸೂರ್ಯೋದಯ"),
    "ks" to mapOf("Quick access" to "فوری رسائی", "Sunrise" to "طلوعِ آفتاب"),
    "kok" to mapOf("Quick access" to "वेगान प्रवेश", "Sunrise" to "सूर्योदय"),
    "mai" to mapOf("Quick access" to "त्वरित पहुँच", "Sunrise" to "सूर्योदय"),
    "ml" to mapOf("Quick access" to "വേഗത്തിലുള്ള പ്രവേശനം", "Sunrise" to "സൂര്യോദയം"),
    "mr" to mapOf("Quick access" to "त्वरित प्रवेश", "Sunrise" to "सूर्योदय"),
    "ne" to mapOf("Quick access" to "छिटो पहुँच", "Sunrise" to "सूर्योदय"),
    "or" to mapOf("Quick access" to "ଦ୍ରୁତ ପ୍ରବେଶ", "Sunrise" to "ସୂର୍ଯ୍ୟୋଦୟ"),
    "pa" to mapOf("Quick access" to "ਤੁਰੰਤ ਪਹੁੰਚ", "Sunrise" to "ਸੂਰਜ ਚੜ੍ਹਨਾ"),
    "sa" to mapOf("Quick access" to "शीघ्रप्रवेशः", "Sunrise" to "सूर्योदयः"),
    "sd" to mapOf("Quick access" to "تڪڙي رسائي", "Sunrise" to "سج اڀرڻ"),
    "ta" to mapOf("Quick access" to "விரைவு அணுகல்", "Sunrise" to "சூரிய உதயம்"),
    "te" to mapOf("Quick access" to "త్వరిత ప్రాప్యత", "Sunrise" to "సూర్యోదయం")
)

/** Short labels for the Home shortcut grid and checklist, where full titles would wrap to three lines. */
private val homeShortcutLabels = mapOf(
    "hinglish" to mapOf("Qibla" to "Qibla", "Hadith" to "Hadees", "Calendar" to "Calendar"),
    "hi" to mapOf("Qibla" to "क़िबला", "Hadith" to "हदीस", "Calendar" to "कैलेंडर"),
    "ur" to mapOf("Qibla" to "قبلہ", "Hadith" to "حدیث", "Calendar" to "تقویم"),
    "as" to mapOf("Qibla" to "কিবলা", "Hadith" to "হাদীছ", "Calendar" to "পঞ্জিকা", "Salah" to "নামাজ", "Naat" to "নাত", "Zakat" to "যাকাত"),
    "bn" to mapOf("Qibla" to "কিবলা", "Hadith" to "হাদিস", "Calendar" to "ক্যালেন্ডার", "Salah" to "নামাজ", "Naat" to "নাত", "Zakat" to "যাকাত"),
    "brx" to mapOf("Qibla" to "किबला", "Hadith" to "हदीस", "Calendar" to "कैलेन्डार", "Salah" to "नमाज", "Naat" to "नात", "Zakat" to "जाकात", "Quick access" to "गोख्रों नायनाय"),
    "doi" to mapOf("Qibla" to "किबला", "Hadith" to "हदीस", "Calendar" to "कैलेंडर", "Salah" to "नमाज", "Naat" to "नात", "Zakat" to "ज़कात"),
    "gu" to mapOf("Qibla" to "કિબ્લા", "Hadith" to "હદીસ", "Calendar" to "કેલેન્ડર", "Salah" to "નમાઝ", "Naat" to "નાત", "Zakat" to "ઝકાત"),
    "kn" to mapOf("Qibla" to "ಕಿಬ್ಲಾ", "Hadith" to "ಹದೀಸ್", "Calendar" to "ಕ್ಯಾಲೆಂಡರ್", "Salah" to "ನಮಾಜ್", "Naat" to "ನಾತ್", "Zakat" to "ಝಕಾತ್"),
    "ks" to mapOf("Qibla" to "قبلہ", "Hadith" to "حدیث", "Calendar" to "کیلنڈر", "Salah" to "نماز", "Naat" to "نعت", "Zakat" to "زکوٰت"),
    "kok" to mapOf("Qibla" to "किबला", "Hadith" to "हदीस", "Calendar" to "दिनदर्शिका", "Salah" to "नमाज", "Naat" to "नात", "Zakat" to "जकात"),
    "mai" to mapOf("Qibla" to "किबला", "Hadith" to "हदीस", "Calendar" to "कैलेंडर", "Salah" to "नमाज", "Naat" to "नात", "Zakat" to "जकात"),
    "ml" to mapOf("Qibla" to "ഖിബ്‌ല", "Hadith" to "ഹദീസ്", "Calendar" to "കലണ്ടർ", "Salah" to "നമസ്കാരം", "Naat" to "നഅ്ത്", "Zakat" to "സകാത്ത്"),
    "mni" to mapOf("Qibla" to "কিবলা", "Hadith" to "হাদিস", "Calendar" to "কেলেন্দর", "Salah" to "নমাজ", "Naat" to "নাত", "Zakat" to "যাকাত", "Quick access" to "থুনা য়েংনবা"),
    "mr" to mapOf("Qibla" to "किब्ला", "Hadith" to "हदीस", "Calendar" to "दिनदर्शिका", "Salah" to "नमाज", "Naat" to "नात", "Zakat" to "जकात"),
    "ne" to mapOf("Qibla" to "किब्ला", "Hadith" to "हदीस", "Calendar" to "पात्रो", "Salah" to "नमाज", "Naat" to "नात", "Zakat" to "जकात"),
    "or" to mapOf("Qibla" to "କିବଲା", "Hadith" to "ହଦିସ", "Calendar" to "କ୍ୟାଲେଣ୍ଡର", "Salah" to "ନମାଜ", "Naat" to "ନାତ", "Zakat" to "ଜକାତ"),
    "pa" to mapOf("Qibla" to "ਕਿਬਲਾ", "Hadith" to "ਹਦੀਸ", "Calendar" to "ਕੈਲੰਡਰ", "Salah" to "ਨਮਾਜ਼", "Naat" to "ਨਾਤ", "Zakat" to "ਜ਼ਕਾਤ"),
    "sa" to mapOf("Qibla" to "क़िब्ला", "Hadith" to "हदीस्", "Calendar" to "पञ्चाङ्गम्", "Salah" to "नमाज्", "Naat" to "नात्", "Zakat" to "ज़कात्"),
    "sat" to mapOf("Qibla" to "ᱠᱤᱵᱞᱟ", "Hadith" to "ᱦᱟᱫᱤᱥ", "Calendar" to "ᱠᱮᱞᱮᱱᱰᱟᱨ", "Salah" to "ᱱᱚᱢᱟᱡ", "Naat" to "ᱱᱟᱛ", "Zakat" to "ᱡᱟᱠᱟᱛ", "Quick access" to "ᱞᱚᱜᱚᱱ ᱧᱮᱞ"),
    "sd" to mapOf("Qibla" to "قبلو", "Hadith" to "حديث", "Calendar" to "ڪئلينڊر", "Salah" to "نماز", "Naat" to "نعت", "Zakat" to "زڪوٰت"),
    "ta" to mapOf("Qibla" to "கிப்லா", "Hadith" to "ஹதீஸ்", "Calendar" to "நாட்காட்டி", "Salah" to "தொழுகை", "Naat" to "நாத்", "Zakat" to "ஜகாத்"),
    "te" to mapOf("Qibla" to "ఖిబ్లా", "Hadith" to "హదీస్", "Calendar" to "క్యాలెండర్", "Salah" to "నమాజ్", "Naat" to "నాత్", "Zakat" to "జకాత్")
)

/**
 * Whole-phrase lookup only, for reviewed content such as dua titles and meanings. Unlike
 * [localizeUiText] it never swaps single words, which would turn "Daily Istighfar" into
 * half-translated text like "रोज़ाना Istighfar".
 */
fun localizePhrase(text: String, language: String): String {
    if (language == "en" || text.isBlank()) return text
    return languagePacks[language]?.phrases?.get(text) ?: text
}

/** Display reviewed whole-content translations without substituting isolated UI words. */
@Composable
fun LocalizedPhraseText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = LocalTextStyle.current
) = MaterialText(
    text = localizePhrase(text, LocalAppLanguage.current),
    modifier = modifier,
    color = color,
    fontWeight = fontWeight,
    lineHeight = lineHeight,
    overflow = overflow,
    maxLines = maxLines,
    style = style
)

/** Localizes a grade label from Dua.gradeLabel, e.g. "Quran" or "Hasan Sahih". */
fun localizeHadithGrade(label: String, language: String): String {
    if (label == "Quran") return localizeUiText(label, language)
    val words = hadithGradeWords[language] ?: return label
    return label.split(' ').joinToString(" ") { words[it] ?: it }
}


@Composable
fun LocalizedText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) = MaterialText(
    text = localizeUiText(text, LocalAppLanguage.current), modifier = modifier, color = color,
    fontSize = fontSize, fontStyle = fontStyle, fontWeight = fontWeight, fontFamily = fontFamily,
    letterSpacing = letterSpacing, textDecoration = textDecoration, textAlign = textAlign,
    lineHeight = lineHeight, overflow = overflow, softWrap = softWrap, maxLines = maxLines,
    minLines = minLines, onTextLayout = onTextLayout, style = style
)
