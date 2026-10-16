package com.example.ui.screens

import com.example.domain.model.Dua
import com.example.domain.model.Surah
import com.example.domain.model.englishMeaning
import com.example.domain.model.i18n.textIn
import com.example.festival.Aamal
import com.example.festival.FestivalDefinition
import com.example.ui.localization.bilingualContent
import com.example.ui.localization.localizePhrase
import kotlin.random.Random

/**
 * One kind of generated question. Every answer and reference is read from bundled data, never
 * written by hand, and index `i` always yields the same question (its distractors are seeded by
 * key and index) so a saved round can be rebuilt from its ids. `null` means the index has no
 * usable question in the current language.
 */
internal interface QuizSource {
    val key: String
    val size: Int
    fun question(index: Int): QuizQuestion?
}

private fun seeded(key: String, index: Int) = Random(key.hashCode() * 31 + index)

/** [count] distinct values drawn with [draw] that pass [accept], or null if too few exist. */
private fun <T> Random.distinct(count: Int, draw: Random.() -> T, accept: (T) -> Boolean): List<T>? {
    val picked = LinkedHashSet<T>()
    repeat(count * 40) {
        if (picked.size == count) return picked.toList()
        val candidate = draw()
        if (accept(candidate)) picked += candidate
    }
    return picked.toList().takeIf { it.size == count }
}

// ---------------------------------------------------------------- Arabic text

private val HARAKAT = Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED\\u0640]")
private val ALEF_FORMS = Regex("[إأآٱ]")
private val NON_LETTERS = Regex("[^\\u0621-\\u064A ]")
private val EDGE_PUNCTUATION = Regex("^[،؛.,:!?\"'«»()ـ-]+|[،؛.,:!?\"'«»()ـ-]+$")
private const val BLANK = "……"

/** Bare letters for comparing Arabic: no harakat, tatweel, pause marks or alef variants. */
internal fun arabicLetters(text: String): String =
    text.replace(HARAKAT, "")
        .replace(ALEF_FORMS, "ا").replace('ى', 'ي').replace('ة', 'ه')
        .replace(NON_LETTERS, " ")
        .split(' ').filter { it.isNotEmpty() }.joinToString(" ")

/** Arabic text split into words; [bare] is empty for pause marks and other non-words. */
internal class ArabicPassage(val text: String) {
    val words: List<String> = text.split(' ').filter { it.isNotEmpty() }
    val bare: List<String> = words.map(::arabicLetters)
    val key: String = bare.filter { it.isNotEmpty() }.joinToString(" ")
    val wordCount: Int = bare.count { it.isNotEmpty() }
}

/** Every three-word phrase of a corpus, plus its vocabulary grouped by length for distractors. */
internal class PhraseIndex(passages: List<ArabicPassage>) {
    private val trigrams = HashSet<String>()
    private val wordsByLength = HashMap<Int, MutableList<Pair<String, String>>>()

    init {
        val seen = HashSet<String>()
        for (passage in passages) {
            val bare = passage.bare.filter { it.isNotEmpty() }
            for (i in 0..bare.size - 3) trigrams += "${bare[i]} ${bare[i + 1]} ${bare[i + 2]}"
            passage.words.forEachIndexed { i, word ->
                val letters = passage.bare[i]
                if (letters.length >= 3 && ' ' !in letters && seen.add(letters)) {
                    wordsByLength.getOrPut(letters.length) { mutableListOf() } += letters to word.replace(EDGE_PUNCTUATION, "")
                }
            }
        }
    }

    fun contains(a: String?, b: String?, c: String?) = a != null && b != null && c != null && "$a $b $c" in trigrams

    fun randomWordNear(length: Int, random: Random): Pair<String, String>? {
        val bucket = wordsByLength[length + random.nextInt(-1, 2)] ?: wordsByLength[length] ?: return null
        return bucket[random.nextInt(bucket.size)]
    }
}

internal data class BlankedPassage(val text: String, val answer: String, val distractors: List<String>)

/**
 * Hides one word of [passage]. A distractor is rejected if putting it in the gap would recreate
 * any phrase that exists in [known] corpora, so a wrong option can never also be a real reading.
 */
internal fun blankOut(passage: ArabicPassage, vocabulary: PhraseIndex, known: List<PhraseIndex>, random: Random): BlankedPassage? {
    val wordIndexes = passage.bare.indices.filter { passage.bare[it].isNotEmpty() }
    val eligible = wordIndexes.filterIndexed { i, w -> i > 0 && passage.bare[w].length >= 3 && ' ' !in passage.bare[w] }
    if (eligible.isEmpty()) return null
    val target = eligible[random.nextInt(eligible.size)]
    val k = wordIndexes.indexOf(target)
    fun around(offset: Int) = wordIndexes.getOrNull(k + offset)?.let { passage.bare[it] }
    val answerBare = passage.bare[target]
    val definite = answerBare.startsWith("ال")

    var tries = 0
    val distractors = random.distinct(3, {
        tries++
        vocabulary.randomWordNear(answerBare.length, this) ?: ("" to "")
    }) { (bare, display) ->
        bare.isNotEmpty() && display.isNotEmpty() && bare != answerBare &&
            (tries > 60 || bare.startsWith("ال") == definite) &&
            known.none { index ->
                index.contains(around(-2), around(-1), bare) ||
                    index.contains(around(-1), bare, around(1)) ||
                    index.contains(bare, around(1), around(2))
            }
    } ?: return null

    val token = passage.words[target]
    val answer = token.replace(EDGE_PUNCTUATION, "")
    val gap = token.replace(answer, BLANK)
    val text = passage.words.toMutableList().also { it[target] = gap }.joinToString(" ")
    return BlankedPassage(text, answer, distractors.map { it.second })
}

// ---------------------------------------------------------------- Quran (Tanzil text)

internal class QuranCorpus(val ayat: List<Ayah>) {
    class Ayah(val surah: Int, val number: Int, val passage: ArabicPassage)

    val index = PhraseIndex(ayat.map { it.passage })
    private val keyCounts = ayat.groupingBy { it.passage.key }.eachCount()

    /** False for ayat whose wording also occurs elsewhere, which can't identify a single place. */
    fun isUnique(ayah: Ayah) = keyCounts[ayah.passage.key] == 1

    companion object {
        /** Parses Tanzil `surah|ayah|text` lines; the Bismillah Tanzil prefixes to each first ayah is not part of it. */
        fun parse(lines: Sequence<String>): QuranCorpus {
            var bismillah: String? = null
            val ayat = lines.mapNotNull { raw ->
                val line = raw.trimStart('﻿').trim()
                if (line.isEmpty() || line.startsWith("#")) return@mapNotNull null
                val parts = line.split('|', limit = 3)
                val surah = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
                val number = parts.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
                var text = parts.getOrNull(2)?.trim() ?: return@mapNotNull null
                if (surah == 1 && number == 1) bismillah = text
                val prefix = bismillah
                if (number == 1 && surah != 1 && prefix != null && text.startsWith("$prefix ")) text = text.removePrefix("$prefix ")
                Ayah(surah, number, ArabicPassage(text))
            }.toList()
            return QuranCorpus(ayat)
        }
    }
}

private fun quranReference(surah: Int, ayah: Int) = "Quran $surah:$ayah · Tanzil.net"

/** "Which surah is this ayah from?" for every ayah whose wording occurs only once. */
internal class QuranSurahOfAyahSource(private val quran: QuranCorpus, private val surahs: List<Surah>) : QuizSource {
    override val key = "qs"
    private val candidates = quran.ayat.filter { quran.isUnique(it) && it.passage.wordCount in 4..40 }
    override val size get() = candidates.size

    override fun question(index: Int): QuizQuestion? {
        val ayah = candidates.getOrNull(index) ?: return null
        val others = seeded(key, index).distinct(3, { nextInt(1, surahs.size + 1) }) { it != ayah.surah } ?: return null
        return QuizQuestion(
            "$key:$index", "Which surah is this ayah from?", surahs[ayah.surah - 1].englishName,
            others.map { surahs[it - 1].englishName }, quranReference(ayah.surah, ayah.number),
            passage = ayah.passage.text, passageArabic = true
        )
    }
}

/** "Which ayah comes next?" — shown ayah is unique, so its successor is unambiguous. */
internal class QuranNextAyahSource(private val quran: QuranCorpus) : QuizSource {
    override val key = "qn"
    private val candidates = quran.ayat.indices.filter { i ->
        val ayah = quran.ayat[i]
        val next = quran.ayat.getOrNull(i + 1)
        next != null && next.surah == ayah.surah && quran.isUnique(ayah) &&
            ayah.passage.wordCount in 3..40 && next.passage.wordCount in 1..30
    }
    override val size get() = candidates.size

    override fun question(index: Int): QuizQuestion? {
        val position = candidates.getOrNull(index) ?: return null
        val ayah = quran.ayat[position]
        val next = quran.ayat[position + 1]
        val length = next.passage.wordCount
        val others = seeded(key, index).distinct(3, { quran.ayat[nextInt(quran.ayat.size)] }) { other ->
            other.passage.key != next.passage.key && other.passage.key != ayah.passage.key &&
                other.passage.wordCount in maxOf(1, length / 2)..(length * 2 + 2)
        } ?: return null
        return QuizQuestion(
            "$key:$index", "Which ayah comes next?", next.passage.text, others.map { it.passage.text },
            quranReference(next.surah, next.number), passage = ayah.passage.text, passageArabic = true, optionsArabic = true
        )
    }
}

/** "Fill in the missing word" for ayat of five words or more. */
internal class QuranMissingWordSource(private val quran: QuranCorpus, private val surahs: List<Surah>) : QuizSource {
    override val key = "qw"
    private val candidates = quran.ayat.filter { it.passage.wordCount in 5..40 }
    override val size get() = candidates.size

    override fun question(index: Int): QuizQuestion? {
        val ayah = candidates.getOrNull(index) ?: return null
        val blank = blankOut(ayah.passage, quran.index, listOf(quran.index), seeded(key, index)) ?: return null
        return QuizQuestion(
            "$key:$index", "Fill in the missing word ({x})", blank.answer, blank.distractors,
            quranReference(ayah.surah, ayah.number), passage = blank.text, passageArabic = true, optionsArabic = true,
            promptArg = "${surahs[ayah.surah - 1].englishName} ${ayah.surah}:${ayah.number}"
        )
    }
}

// ---------------------------------------------------------------- Hadith (Arabic matn)

internal class HadithCorpus(val sayings: List<Saying>) {
    class Saying(val collection: String, val number: String, val book: Int, val passage: ArabicPassage) {
        val citation: String get() = if (collection == "bukhari") "Sahih al-Bukhari $number" else "Sahih Muslim $number"
        val opening: String get() = passage.bare.filter { it.isNotEmpty() }.take(2).joinToString(" ")
    }

    val index = PhraseIndex(sayings.map { it.passage })

    companion object {
        /** Parses `collection<TAB>number<TAB>book<TAB>saying` rows from hadith-sayings.tsv. */
        fun parse(lines: Sequence<String>): HadithCorpus = HadithCorpus(
            lines.mapNotNull { line ->
                if (line.isBlank() || line.startsWith("#")) return@mapNotNull null
                val parts = line.split('\t', limit = 4)
                if (parts.size < 4) return@mapNotNull null
                Saying(parts[0], parts[1], parts[2].toIntOrNull() ?: 0, ArabicPassage(parts[3].trim()))
            }.toList()
        )
    }
}

/** "Complete this saying": the ending of another saying from a different chapter is a distractor. */
internal class HadithCompletionSource(private val hadith: HadithCorpus) : QuizSource {
    override val key = "hc"
    private val candidates = hadith.sayings.filter { it.passage.words.size >= 10 }
    override val size get() = candidates.size

    override fun question(index: Int): QuizQuestion? {
        val saying = candidates.getOrNull(index) ?: return null
        val words = saying.passage.words
        val split = maxOf((words.size + 1) / 2, words.size - 14)
        val answer = words.drop(split).joinToString(" ")
        val answerKey = arabicLetters(answer)
        val tailLength = words.size - split
        val others = seeded(key, index).distinct(3, { hadith.sayings[nextInt(hadith.sayings.size)] }) { other ->
            other.passage.words.size >= tailLength + 2 && other.opening != saying.opening &&
                (other.book != saying.book || other.collection != saying.collection) &&
                arabicLetters(other.passage.words.takeLast(tailLength).joinToString(" ")) != answerKey
        } ?: return null
        return QuizQuestion(
            "$key:$index", "Complete this saying of the Prophet ﷺ", answer,
            others.map { it.passage.words.takeLast(tailLength).joinToString(" ") }, saying.citation,
            passage = words.take(split).joinToString(" ") + " " + BLANK, passageArabic = true, optionsArabic = true
        )
    }
}

/** "Fill in the missing word" in a Prophetic saying, checked against both hadith and Quran phrases. */
internal class HadithMissingWordSource(private val hadith: HadithCorpus, private val quran: QuranCorpus?) : QuizSource {
    override val key = "hw"
    private val candidates = hadith.sayings.filter { it.passage.wordCount >= 8 }
    private val known = listOfNotNull(hadith.index, quran?.index)
    override val size get() = candidates.size

    override fun question(index: Int): QuizQuestion? {
        val saying = candidates.getOrNull(index) ?: return null
        val blank = blankOut(saying.passage, hadith.index, known, seeded(key, index)) ?: return null
        return QuizQuestion(
            "$key:$index", "Fill in the missing word ({x})", blank.answer, blank.distractors, saying.citation,
            passage = blank.text, passageArabic = true, optionsArabic = true, promptArg = saying.citation
        )
    }
}

// ---------------------------------------------------------------- Surah list

/** Six facts per surah from the app's surah catalogue: number, ayat, Arabic name, order and meaning. */
internal class SurahFactsSource(private val surahs: List<Surah>, private val language: String) : QuizSource {
    override val key = "sf"
    override val size get() = FACTS * surahs.size

    override fun question(index: Int): QuizQuestion? {
        if (index !in 0 until size) return null
        val fact = index / surahs.size
        val surah = surahs[index % surahs.size]
        val n = surah.number
        val random = seeded(key, index)
        fun name(number: Int) = surahs[number - 1].englishName
        fun nearby(vararg exclude: Int) = random.distinct(3, { (n + nextInt(-6, 7)).coerceIn(1, surahs.size) }) { it !in exclude }
            ?.map(::name)
        val reference = "Quran · Surah $n (${surah.englishName})"
        val id = "$key:$index"
        return when (fact) {
            0 -> QuizQuestion(id, "Which surah is number {x} in the Quran?", name(n), nearby(n) ?: return null, reference, promptArg = "$n")
            1 -> {
                val count = surah.totalAyat
                val wrong = random.distinct(3, { count + listOf(-10, -5, -3, -2, -1, 1, 2, 3, 5, 10)[nextInt(10)] }) { it > 0 && it != count }
                    ?: return null
                QuizQuestion(id, "How many ayat are in Surah {x}?", "$count", wrong.map { "$it" }, reference, promptArg = surah.englishName)
            }
            2 -> QuizQuestion(
                id, "Which surah has this Arabic name?", name(n), nearby(n) ?: return null, reference,
                passage = surah.arabicName, passageArabic = true
            )
            3 -> if (n == surahs.size) null else
                QuizQuestion(id, "Which surah comes after Surah {x}?", name(n + 1), nearby(n, n + 1) ?: return null, reference, promptArg = surah.englishName)
            4 -> if (n == 1) null else
                QuizQuestion(id, "Which surah comes before Surah {x}?", name(n - 1), nearby(n, n - 1) ?: return null, reference, promptArg = surah.englishName)
            else -> {
                // Some names (Ya-Sin, Qaf, Luqman) are their own meaning.
                if (language in setOf("ur", "hi")) return null
                val meaning = localizePhrase(surah.englishMeaning, language)
                if (lettersOnly(meaning) == lettersOnly(surah.englishName) || surahs.count { it.englishMeaning == meaning } > 1) return null
                val others = random.distinct(3, { surahs[nextInt(surahs.size)] }) { it.number != n && it.englishMeaning != meaning } ?: return null
                QuizQuestion(id, "Which surah's name means “{x}”?", surah.englishName, others.map { it.englishName }, reference, promptArg = meaning)
            }
        }
    }

    private fun lettersOnly(text: String) = text.lowercase().filter { it.isLetter() }

    private companion object { const val FACTS = 6 }
}

// ---------------------------------------------------------------- Duas

/** Occasion, meaning and Arabic text of each bundled dua; wrong options come from other categories. */
internal class DuaSource(duas: List<Dua>, private val language: String) : QuizSource {
    override val key = "du"
    private val duas = duas.distinctBy { arabicLetters(it.arabicText) }.filter { ArabicPassage(it.arabicText).wordCount in 2..40 }
    private val titles = this.duas.associateWith { it.textIn(language)?.title ?: localizePhrase(it.title, language) }
    private val titleCounts = titles.values.groupingBy { it }.eachCount()
    override val size get() = TYPES * duas.size

    private fun meaning(dua: Dua) = dua.textIn(language)?.meaning
        ?: if (language == "ur") dua.urduTranslation else bilingualContent(dua.englishTranslation, dua.englishMeaning, language).text

    private fun uniqueTitle(dua: Dua) = titleCounts[titles.getValue(dua)] == 1

    override fun question(index: Int): QuizQuestion? {
        if (index !in 0 until size) return null
        val dua = duas[index % duas.size]
        val random = seeded(key, index)
        val id = "$key:$index"
        val reference = "${dua.sourceCollection} · ${dua.sourceReference}"
        fun others(accept: (Dua) -> Boolean) = random.distinct(3, { duas[nextInt(duas.size)] }) { it.category != dua.category && accept(it) }
        return when (index / duas.size) {
            0 -> {
                if (!uniqueTitle(dua)) return null
                val title = titles.getValue(dua)
                val wrong = others { uniqueTitle(it) && titles.getValue(it) != title } ?: return null
                QuizQuestion(id, "When is this dua recited?", title, wrong.map { titles.getValue(it) }, reference, passage = dua.arabicText, passageArabic = true)
            }
            1 -> {
                val answer = meaning(dua)
                if (answer.isBlank() || answer.length > MAX_MEANING) return null
                val wrong = others { meaning(it).let { m -> m.isNotBlank() && m.length <= MAX_MEANING && m != answer } } ?: return null
                QuizQuestion(id, "What does this dua mean?", answer, wrong.map(::meaning), reference, passage = dua.arabicText, passageArabic = true)
            }
            else -> {
                if (!uniqueTitle(dua) || ArabicPassage(dua.arabicText).wordCount > 30) return null
                val wrong = others { ArabicPassage(it.arabicText).wordCount <= 30 } ?: return null
                QuizQuestion(
                    id, "Which is the dua for: {x}?", dua.arabicText, wrong.map { it.arabicText }, reference,
                    optionsArabic = true, promptArg = titles.getValue(dua)
                )
            }
        }
    }

    private companion object {
        const val TYPES = 3
        const val MAX_MEANING = 180
    }
}

// ---------------------------------------------------------------- Festival aamal

/**
 * "Which occasion is this deed for?" only for deeds tied to one occasion (Tauba, Sadqa or the Eid
 * prayer belong to several, so they're left out), and never offering an occasion from the same
 * Hijri month as a wrong option. Also the Hijri date of each occasion.
 */
internal class AamalSource(
    private val festivals: List<FestivalDefinition>,
    aamalFor: (String) -> List<Aamal>,
    private val language: String
) : QuizSource {
    override val key = "am"
    private val deeds = festivals.flatMap { festival ->
        aamalFor(festival.id).filter { it.title in OCCASION_SPECIFIC }.map { festival to it }
    }
    // Laylat al-Qadr is only "commonly observed" on the 27th, so its date isn't asked.
    private val dated = festivals.filter { it.id != "qadr" }
    override val size get() = deeds.size + dated.size

    private fun festivalName(festival: FestivalDefinition) =
        bilingualContent(festival.localName, festival.name, language).text

    private fun date(festival: FestivalDefinition) =
        "${festival.hijriDay} ${(HIJRI_MONTHS[language] ?: HIJRI_MONTHS.getValue("en"))[festival.hijriMonth - 1]}"

    override fun question(index: Int): QuizQuestion? {
        val random = seeded(key, index)
        val id = "$key:$index"
        deeds.getOrNull(index)?.let { (festival, deed) ->
            val wrong = random.distinct(3, { festivals[nextInt(festivals.size)] }) { it.hijriMonth != festival.hijriMonth } ?: return null
            return QuizQuestion(
                id, "On which occasion is this deed especially recommended?", festivalName(festival), wrong.map(::festivalName),
                if (language == "en") deed.englishSource else deed.source,
                passage = bilingualContent(deed.title, deed.englishTitle, language).text
            )
        }
        val festival = dated.getOrNull(index - deeds.size) ?: return null
        val wrong = random.distinct(3, { dated[nextInt(dated.size)] }) { date(it) != date(festival) } ?: return null
        return QuizQuestion(
            id, "On which Hijri date is {x}?", date(festival), wrong.map(::date),
            "Islamic calendar · ${festival.name}", promptArg = festivalName(festival)
        )
    }

    private companion object {
        val OCCASION_SPECIFIC = setOf(
            "Ashura ka roza (9 + 10 ya 10 + 11)", "Ghar walon par kushadagi", "Zikr-e-Imam Husain (RA) aur Esaal-e-Sawab",
            "Seerat-un-Nabi ﷺ ka mutala'a", "Seerat-e-Ghaus-e-Azam ka mutala'a", "Rajab ki dua", "Waqia-e-Me'raj ka zikr",
            "27 Rajab ka roza", "Qabristan ki ziyarat", "15 Sha'ban ka roza", "Aatish-baazi aur fuzooliyat se parhez",
            "Roza aur Sehri", "Iftar ki dua", "Taraweeh", "Shab-e-Qadr ki dua", "Surah Al-Qadr aur tilawat",
            "Aakhri ashre ki taaq raatein", "Sadqa-e-Fitr", "Shawwal ke 6 roze", "Arafah ka roza", "Arafah ki behtareen dua",
            "Zil-Hijjah ke pehle 10 din", "Baal-nakhun na katna", "Qurbani", "Gosht ki taqseem", "Eid-ul-Adha ki namaz"
        )
        val HIJRI_MONTHS = mapOf(
            "en" to listOf("Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani", "Jumada al-Ula", "Jumada al-Akhirah",
                "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhul Qa'dah", "Dhul Hijjah"),
            "hinglish" to listOf("Muharram", "Safar", "Rabi-ul-Awwal", "Rabi-us-Sani", "Jumadal-Ula", "Jumadal-Ukhra",
                "Rajab", "Shaban", "Ramzan", "Shawwal", "Zul Qada", "Zul Hijja"),
            "hi" to listOf("मुहर्रम", "सफ़र", "रबीउल-अव्वल", "रबीउस-सानी", "जुमादल-ऊला", "जुमादल-उख़रा",
                "रजब", "शाबान", "रमज़ान", "शव्वाल", "ज़ुल-क़ादा", "ज़ुल-हिज्जा"),
            "ur" to listOf("محرم", "صفر", "ربیع الاول", "ربیع الثانی", "جمادی الاولیٰ", "جمادی الآخرہ",
                "رجب", "شعبان", "رمضان", "شوال", "ذوالقعدہ", "ذوالحجہ")
        )
    }
}
