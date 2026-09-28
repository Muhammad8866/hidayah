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
    "Retry" to "फिर से कोशिश करें"
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
    "Retry" to "دوبارہ کوشش کریں"
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

/** The Hindi pack is the reference: every other language must translate the same UI copy. */
internal val referenceUiPhraseKeys: Set<String> =
    hindiPhrases.keys + duaFeaturePhrases.getValue("hi").keys + duaScreenPhrases.getValue("hi").keys

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
