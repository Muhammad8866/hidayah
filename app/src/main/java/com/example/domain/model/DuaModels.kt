package com.example.domain.model

import java.text.Normalizer
import java.util.Locale

data class Dua(
    val id: String,
    val category: String,
    val title: String,
    val context: String,
    val arabicText: String,
    val transliteration: String,
    val englishTranslation: String,
    val urduTranslation: String,
    val benefit: String,
    val sourceCollection: String,
    val sourceReference: String,
    val authenticityGrading: String,
    val recommendedCount: Int = 1
) {
    /**
     * Short grade for badges: "Quran", "Sahih", "Hasan Sahih", "Hasan" or "Mauqoof".
     * The full scholarly note stays in [authenticityGrading].
     */
    val gradeLabel: String
        get() = when {
            sourceCollection == QURAN_COLLECTION ||
                authenticityGrading.startsWith("Mutawatir") ||
                authenticityGrading.startsWith("Ayat Qurani") -> "Quran"
            authenticityGrading.startsWith("Mauqoof") -> "Mauqoof"
            authenticityGrading.startsWith("Hasan Sahih") -> "Hasan Sahih"
            authenticityGrading.startsWith("Hasan") -> "Hasan"
            else -> "Sahih"
        }

    companion object {
        const val QURAN_COLLECTION = "The Noble Quran"
    }
}

object DuaDatasetRepository {

    const val ALL_CATEGORY = "All Duas"

    val CATEGORIES = listOf(
        ALL_CATEGORY,
        "Morning & Evening",
        "Prayer & Mosque",
        "Sleep & Awakening",
        "Anxiety & Distress",
        "Travel & Leaving Home",
        "Forgiveness & Istighfar",
        "Parents & Family",
        "Food & Gratitude",
        "Quranic Duas",
        "Protection & Healing",
        "Weather & Nature",
        "Death & Janaza",
        "Daily Life & Manners",
        "Hajj & Umrah",
        "Ramadan & Fasting",
        "Comprehensive Duas"
    )

    val DUAS = listOf(
        Dua(
            id = "dua_waking_up",
            category = "Sleep & Awakening",
            title = "Upon Waking Up",
            context = "Neend se jaagte hi Allah ka shukr ada karne ke liye.",
            arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
            transliteration = "Al-ḥamdu lillāhil-ladhī aḥyānā ba'da mā amātanā wa ilayhin-nushūr",
            englishTranslation = "Saari tareef Allah ke liye hai, jisne humein maut dene ke baad zindagi di, aur usi ki taraf dobara uthkar jaana hai.",
            urduTranslation = "تمام تعریفیں اللہ کے لیے ہیں جس نے ہمیں مارنے کے بعد زندہ کیا اور اسی کی طرف اٹھ کر جانا ہے",
            benefit = "Nabi ﷺ neend se jaag kar yeh dua parhte the — din ki shuruaat shukr aur aakhirat ki yaad se.",
            sourceCollection = "Sahih al-Bukhari",
            sourceReference = "Hadith 6312",
            authenticityGrading = "Sahih"
        ),
        Dua(
            id = "dua_leaving_home",
            category = "Travel & Leaving Home",
            title = "Upon Leaving the House",
            context = "Ghar se bahar qadam rakhte waqt Allah ki hifazat ke liye.",
            arabicText = "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            transliteration = "Bismillāhi tawakkaltu 'alallāh, wa lā ḥawla wa lā quwwata illā billāh",
            englishTranslation = "Allah ke naam se; maine Allah par bharosa kiya. Allah ke baghair na koi taaqat hai aur na koi quwwat.",
            urduTranslation = "اللہ کے نام کے ساتھ، میں نے اللہ پر بھروسا کیا، اور اللہ کے بغیر نہ کوئی طاقت ہے نہ قوت",
            benefit = "Farishte kehte hain: 'Tumhein hidayat di gayi, tumhari kifaayat ki gayi aur tumhein bacha liya gaya', aur Shaitan door ho jaata hai.",
            sourceCollection = "Sunan Abi Dawud",
            sourceReference = "Hadith 5095; Jami` at-Tirmidhi 3426",
            authenticityGrading = "Hasan Sahih (Imam Tirmizi)"
        ),
        Dua(
            id = "dua_anxiety_distress",
            category = "Anxiety & Distress",
            title = "Relief from Anxiety and Sorrow",
            context = "Gham, fikr, qarz ke bojh aur zehni pareshani ke waqt.",
            arabicText = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ، وَالْبُخْلِ وَالْجُبْنِ، وَضَلَعِ الدَّيْنِ وَغَلَبَةِ الرِّجَالِ",
            transliteration = "Allāhumma innī a'ūdhu bika minal-hammi wal-ḥazan, wal-'ajzi wal-kasal, wal-bukhli wal-jubn, wa ḍala'id-dayni wa ghalabatir-rijāl",
            englishTranslation = "Ya Allah, main gham aur fikr, kamzori aur susti, kanjoosi aur buzdili, qarz ke bojh aur logon ke ghalbay se Teri panaah maangta hoon.",
            urduTranslation = "اے اللہ! میں غم اور فکر سے، عاجزی اور سستی سے، بخل اور بزدلی سے، قرض کے بوجھ اور لوگوں کے دباؤ سے تیری پناہ مانگتا ہوں",
            benefit = "Nabi ﷺ kasrat se yeh dua maangte the — gham, susti aur qarz ki fikr se panaah.",
            sourceCollection = "Sahih al-Bukhari",
            sourceReference = "Hadith 6363",
            authenticityGrading = "Sahih"
        ),
        Dua(
            id = "dua_sayyidul_istighfar",
            category = "Forgiveness & Istighfar",
            title = "Sayyid al-Istighfar (Chief of Forgiveness)",
            context = "Subah aur shaam — Nabi ﷺ ki sikhayi hui tauba ki sab se afzal dua.",
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ لَكَ بِذَنْبِي، فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            transliteration = "Allāhumma Anta Rabbī lā ilāha illā Anta, khalaqtanī wa anā 'abduka, wa anā 'alā 'ahdika wa wa'dika mastaṭa'tu, a'ūdhu bika min sharri mā ṣana'tu, abū'u laka bini'matika 'alayya, wa abū'u laka bidhanbī, faghfir lī fa-innahū lā yaghfirudh-dhunūba illā Anta",
            englishTranslation = "Ya Allah, Tu hi mera Rab hai, Tere siwa koi ibaadat ke laayak nahi. Tune mujhe paida kiya aur main Tera banda hoon. Main apni poori koshish ke saath Tere ahad aur waade par qaayam hoon. Main apne kiye ke shar se Teri panaah maangta hoon. Main apne upar Teri nematon aur apne gunaahon ka iqraar karta hoon; isliye mujhe maaf farma, kyunki Tere siwa gunaah koi maaf nahi kar sakta.",
            urduTranslation = "اے اللہ! تو ہی میرا رب ہے، تیرے سوا کوئی معبود نہیں۔ تو نے مجھے پیدا کیا اور میں تیرا بندہ ہوں، اور میں اپنی استطاعت کے مطابق تیرے عہد اور وعدے پر قائم ہوں۔ میں اپنے اعمال کے شر سے تیری پناہ مانگتا ہوں۔ میں اپنے اوپر تیری نعمتوں کا اعتراف کرتا ہوں اور اپنے گناہوں کا اقرار کرتا ہوں، پس مجھے بخش دے کیونکہ تیرے سوا کوئی گناہوں کو معاف نہیں کر سکتا",
            benefit = "Jo yaqeen ke saath shaam ko parhe aur usi raat faut ho jaaye woh Jannat mein jaayega; aur subah parhe to bhi yahi fazilat hai.",
            sourceCollection = "Sahih al-Bukhari",
            sourceReference = "Hadith 6306",
            authenticityGrading = "Sahih"
        ),
        Dua(
            id = "dua_parents",
            category = "Parents & Family",
            title = "Supplication for Parents",
            context = "Maa baap ke liye Qurani dua — unki bachpan ki parwarish ke badle mein.",
            arabicText = "رَّبِّ ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا",
            transliteration = "Rabbir-ḥamhumā kamā rabbayānī ṣaghīrā",
            englishTranslation = "Aye mere Rab, un dono par reham farma, jaise unhone bachpan mein mujhe paala.",
            urduTranslation = "اے میرے رب! ان دونوں پر رحم فرما جیسا کہ انہوں نے بچپن میں مجھے پالا",
            benefit = "Allah ne khud waalidain ke liye yeh dua maangne ka hukm diya.",
            sourceCollection = Dua.QURAN_COLLECTION,
            sourceReference = "Surah Al-Isra (17:24)",
            authenticityGrading = "Mutawatir (Quranic Verse)"
        ),
        Dua(
            id = "dua_entering_mosque",
            category = "Prayer & Mosque",
            title = "Entering the Mosque",
            context = "Masjid mein daayan paon rakhte waqt.",
            arabicText = "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
            transliteration = "Allāhummaf-taḥ lī abwāba raḥmatik",
            englishTranslation = "Ya Allah, mere liye apni rehmat ke darwaaze khol de.",
            urduTranslation = "اے اللہ! میرے لیے اپنی رحمت کے دروازے کھول دے",
            benefit = "Nabi ﷺ ne masjid mein daakhil hote waqt yeh dua parhne ki taleem di.",
            sourceCollection = "Sahih Muslim",
            sourceReference = "Hadith 713",
            authenticityGrading = "Sahih"
        )
    ) + MoreDuas.LIST + MoreDuas2.LIST + MoreDuas3.LIST + MoreDuas4.LIST + MoreDuas5.LIST + MoreDuas6.LIST

    private val duasById: Map<String, Dua> by lazy { DUAS.associateBy { it.id } }

    // Precomputed once: every searchable field, folded to plain lowercase letters.
    private val searchIndex: Map<String, String> by lazy {
        DUAS.associate { dua ->
            dua.id to normalizeForSearch(
                listOf(
                    dua.title, dua.context, dua.category, dua.transliteration, dua.englishTranslation,
                    dua.urduTranslation, dua.arabicText, dua.benefit, dua.sourceCollection, dua.sourceReference
                ).joinToString(" ")
            )
        }
    }

    fun byId(id: String): Dua? = duasById[id]

    /**
     * Duas in [category] (or all of them for [ALL_CATEGORY]) whose text contains every word of
     * [query]. Matching ignores case, harakat and transliteration marks, so "subhanallah" finds
     * "Subḥānallāh" and "الحمد لله" finds the fully vowelled Arabic.
     */
    fun filter(category: String, query: String, additionalSearchText: (Dua) -> String = { "" }): List<Dua> {
        val inCategory = if (category == ALL_CATEGORY) DUAS else DUAS.filter { it.category == category }
        val words = normalizeForSearch(query).split(' ').filter { it.isNotEmpty() }
        if (words.isEmpty()) return inCategory
        return inCategory.filter { dua ->
            val haystack = searchIndex.getValue(dua.id)
            val localizedHaystack = normalizeForSearch(additionalSearchText(dua))
            words.all { it in haystack || it in localizedHaystack }
        }
    }

    fun searchDuas(query: String): List<Dua> = filter(ALL_CATEGORY, query)

    internal fun normalizeForSearch(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(COMBINING_MARKS, "")
            .lowercase(Locale.ROOT)
            .replace(JOINERS, "")
            .replace(NON_WORD, " ")
            .trim()

    private val COMBINING_MARKS = Regex("\\p{M}+")
    private val JOINERS = Regex("['‘’`ʿʾ\\-]")
    private val NON_WORD = Regex("[^\\p{L}\\p{N}]+")
}
