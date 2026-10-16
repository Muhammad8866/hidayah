package com.example.salah

import com.example.ui.localization.localizePhrase

/** Guide copy with reviewed translations and regional drafts marked for review. */
data class SalahText(
    val en: String,
    val translations: Map<String, String> = emptyMap(),
    private val components: List<SalahText> = emptyList(),
    private val template: SalahText? = null,
    private val substitutions: Map<String, SalahText> = emptyMap()
) {
    fun inLanguage(code: String): String = translations[code] ?: when {
        code == "en" -> en
        components.isNotEmpty() -> components.joinToString(" ") { it.inLanguage(code) }
        template != null -> substitutions.entries.fold(template.inLanguage(code)) { text, (name, value) ->
            text.replace("{$name}", value.inLanguage(code))
        }
        else -> localizePhrase(en, code)
    }

    /** Fills `{name}` placeholders once the language has been chosen. */
    fun inLanguage(code: String, vararg values: Pair<String, Any>): String =
        values.fold(inLanguage(code)) { text, (name, value) -> text.replace("{$name}", value.toString()) }

    /** Joins two sentences language by language, so a step never mixes languages. */
    infix fun then(next: SalahText): SalahText =
        SalahText("$en ${next.en}", translations.mapValues { (code, text) -> "$text ${next.inLanguage(code)}" },
            components = listOf(this, next))

    /** Keep the untranslated template until its placeholders can be filled in the chosen language. */
    fun withText(name: String, value: SalahText): SalahText = SalahText(
        en.replace("{$name}", value.en),
        translations.mapValues { (code, text) -> text.replace("{$name}", value.inLanguage(code)) },
        template = this,
        substitutions = mapOf(name to value)
    )
}

/** Every string of the Salah guide is written in the four languages its readers use most. */
internal fun t(en: String, hinglish: String, hi: String, ur: String) =
    SalahText(en, mapOf("hinglish" to hinglish, "hi" to hi, "ur" to ur))

enum class Madhhab(val label: String, val title: SalahText) {
    HANAFI("Hanafi", t("Hanafi", "Hanafi", "हनफ़ी", "حنفی")),
    SHAFII("Shafi‘i", t("Shafi‘i", "Shafi'i", "शाफ़ई", "شافعی")),
    MALIKI("Maliki", t("Maliki", "Maliki", "मालिकी", "مالکی")),
    HANBALI("Hanbali", t("Hanbali", "Hanbali", "हंबली", "حنبلی"))
}

enum class SalahKind(val title: SalahText) {
    FARD(t("Fard", "Farz", "फ़र्ज़", "فرض")),
    SUNNAH_MUAKKADAH(t("Sunnah Muakkadah", "Sunnat-e-Muakkada", "सुन्नत-ए-मुअक्कदा", "سنتِ مؤکدہ")),
    SUNNAH_GHAIR_MUAKKADAH(t("Sunnah Ghair-Muakkadah", "Sunnat-e-Ghair Muakkada", "सुन्नत-ए-ग़ैर मुअक्कदा", "سنتِ غیر مؤکدہ")),
    NAFL(t("Nafl", "Nafl", "नफ़्ल", "نفل")),
    WITR(t("Witr", "Witr", "वित्र", "وتر")),
    WAJIB(t("Wajib", "Wajib", "वाजिब", "واجب")),
    FARD_KIFAYAH(t("Fard Kifayah", "Farz-e-Kifaya", "फ़र्ज़-ए-किफ़ाया", "فرضِ کفایہ")),
    SUNNAH(t("Sunnah", "Sunnat", "सुन्नत", "سنت"))
}

/** How binding one act inside the prayer is, in Hanafi terms. */
enum class Ruling(val title: SalahText) {
    CONDITION(t("Condition", "Shart", "शर्त", "شرط")),
    FARD(t("Fard", "Farz", "फ़र्ज़", "فرض")),
    WAJIB(t("Wajib", "Wajib", "वाजिब", "واجب")),
    SUNNAH(t("Sunnah", "Sunnat", "सुन्नत", "سنت")),
    MUSTAHAB(t("Mustahab", "Mustahab", "मुस्तहब", "مستحب"))
}

/** The prayer shapes the step-by-step guide can walk through. */
enum class GuideForm(val rakats: Int, val kind: SalahKind, val title: SalahText) {
    FARD_2(2, SalahKind.FARD, t("2 Fard", "2 Farz", "2 फ़र्ज़", "2 فرض")),
    FARD_3(3, SalahKind.FARD, t("3 Fard", "3 Farz", "3 फ़र्ज़", "3 فرض")),
    FARD_4(4, SalahKind.FARD, t("4 Fard", "4 Farz", "4 फ़र्ज़", "4 فرض")),
    SUNNAH_2(2, SalahKind.NAFL, t("2 Sunnah / Nafl", "2 Sunnat / Nafl", "2 सुन्नत / नफ़्ल", "2 سنت / نفل")),
    SUNNAH_4(4, SalahKind.NAFL, t("4 Sunnah / Nafl", "4 Sunnat / Nafl", "4 सुन्नत / नफ़्ल", "4 سنت / نفل")),
    WITR_3(3, SalahKind.WITR, t("3 Witr", "3 Witr", "3 वित्र", "3 وتر"));

    companion object {
        fun fromKey(key: String?): GuideForm = entries.firstOrNull { it.name == key } ?: FARD_2

        /** The guide that teaches a unit of a daily prayer, e.g. the 4 Sunnah before Zuhr. */
        fun forUnit(count: Int, kind: SalahKind): GuideForm? = when (kind) {
            SalahKind.FARD -> entries.firstOrNull { it.kind == SalahKind.FARD && it.rakats == count }
            SalahKind.WITR -> WITR_3
            SalahKind.SUNNAH_MUAKKADAH, SalahKind.SUNNAH_GHAIR_MUAKKADAH, SalahKind.NAFL, SalahKind.SUNNAH ->
                entries.firstOrNull { it.kind == SalahKind.NAFL && it.rakats == count }
            SalahKind.WAJIB, SalahKind.FARD_KIFAYAH -> null
        }
    }
}

/** Hanafi fiqh teaches a more concealed posture for women. */
enum class Worshipper(val title: SalahText) {
    MAN(t("Man", "Mard", "मर्द", "مرد")),
    WOMAN(t("Woman", "Aurat", "औरत", "عورت"))
}

enum class PrayingAs(val title: SalahText) {
    ALONE(t("Alone", "Akele", "अकेले", "اکیلے")),
    BEHIND_IMAM(t("Behind an imam", "Imam ke peeche", "इमाम के पीछे", "امام کے پیچھے"))
}

data class Rakats(val count: Int, val kind: SalahKind, val whenPrayed: SalahText) {
    /** Witr is a fixed three only in the Hanafi school. */
    fun countLabel(madhhab: Madhhab): String =
        if (kind == SalahKind.WITR && madhhab != Madhhab.HANAFI) "1+" else count.toString()

    val form: GuideForm? get() = GuideForm.forUnit(count, kind)
}

data class DailyPrayer(val id: String, val title: SalahText, val units: List<Rakats>, val note: SalahText) {
    val fard: Int get() = units.first { it.kind == SalahKind.FARD }.count
    val total: Int get() = units.sumOf { it.count }
}

enum class RecitationGroup(val title: SalahText) {
    IN_PRAYER(t("Inside the prayer", "Namaz ke andar", "नमाज़ के अंदर", "نماز کے اندر")),
    SURAHS(t("Short surahs", "Chhoti surtein", "छोटी सूरतें", "چھوٹی سورتیں")),
    WITR(t("Witr", "Witr", "वित्र", "وتر")),
    AFTER_SALAM(t("After the salam", "Salam ke baad", "सलाम के बाद", "سلام کے بعد")),
    SPECIAL(t("Janazah and other prayers", "Janaza aur doosri namazein", "जनाज़ा और दूसरी नमाज़ें", "جنازہ اور دوسری نمازیں"))
}

/** [quran] is "surah:from-to" for text that must match the bundled Mushaf letter for letter. */
data class Recitation(
    val id: String,
    val group: RecitationGroup,
    val title: SalahText,
    val arabic: String,
    val transliteration: String,
    val meaning: SalahText,
    val source: String,
    val note: SalahText? = null,
    val quran: String? = null
)

data class GuideStep(
    val id: String,
    val title: SalahText,
    val instruction: SalahText,
    val ruling: Ruling? = null,
    val rakah: Int? = null,
    val points: List<SalahText> = emptyList(),
    val recitationIds: List<String> = emptyList()
)

/** A masail section or a special prayer: a short lesson with its school notes and reference. */
data class SalahTopic(
    val id: String,
    val title: SalahText,
    val kind: SalahKind?,
    val summary: SalahText,
    val steps: List<SalahText>,
    val source: String,
    val schoolNotes: Map<Madhhab, SalahText> = emptyMap(),
    val kindBySchool: Map<Madhhab, SalahKind> = emptyMap(),
    val recitationIds: List<String> = emptyList(),
    val learnForm: GuideForm? = null,
    /** Ordered actions are numbered; lists of rulings are bulleted. */
    val numbered: Boolean = true,
    /** A ruling that applies to the whole list, in every school. */
    val footnote: SalahText? = null
) {
    fun kindFor(madhhab: Madhhab): SalahKind? = kindBySchool[madhhab] ?: kind
}
