package com.example.ui.screens

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.model.DuaDatasetRepository
import com.example.festival.FestivalAamal
import com.example.festival.IslamicFestivalRepository
import com.example.quran.FullQuranCatalog
import java.io.IOException
import kotlin.random.Random

/** Where quiz progress lives; SharedPreferences in the app, a map in tests. */
internal interface QuizProgressStore {
    fun getInt(key: String): Int
    fun putInt(key: String, value: Int)
    fun getStrings(key: String): Set<String>
    fun putStrings(key: String, value: Set<String>)
}

internal class PrefsProgressStore(private val prefs: SharedPreferences) : QuizProgressStore {
    override fun getInt(key: String) = prefs.getInt(key, 0)
    override fun putInt(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
    override fun getStrings(key: String): Set<String> = prefs.getStringSet(key, emptySet()).orEmpty().toSet()
    override fun putStrings(key: String, value: Set<String>) = prefs.edit().putStringSet(key, value).apply()
}

/**
 * Builds rounds from the hand-written bank plus every generated source. Each source is walked in
 * a stored random order (seed + position), so nothing repeats until that whole source has been
 * shown; then a new order starts. Missed generated questions come back once for revision.
 */
internal class QuizEngine(
    private val store: QuizProgressStore,
    private val bank: List<QuizQuestion>,
    sources: List<QuizSource>,
    private val random: Random = Random.Default
) {
    private val sources = sources.filter { it.size > 0 }
    private val sourcesByKey = this.sources.associateBy { it.key }
    private val bankById = bank.associateBy { it.id }
    private val orders = HashMap<String, IntArray>()

    fun resolve(id: String): QuizQuestion? {
        val colon = id.indexOf(':')
        if (colon < 0) return bankById[id]
        val index = id.substring(colon + 1).toIntOrNull() ?: return null
        return sourcesByKey[id.substring(0, colon)]?.question(index)
    }

    fun planRound(): List<String> {
        val previous = store.getStrings(PREVIOUS_ROUND)
        val generated = sources.mapNotNull(::next).take(QUIZ_ROUND_SIZE)
        val revision = store.getStrings(MISSED_GENERATED)
            .firstOrNull { it !in previous && it !in generated && resolve(it) != null }
        val bankSlots = (QUIZ_ROUND_SIZE - generated.size - (if (revision != null) 1 else 0)).coerceAtLeast(0)
        val round = (listOfNotNull(revision) + generated + planBank(previous, bankSlots)).shuffled(random)
        store.putStrings(PREVIOUS_ROUND, round.toSet())
        return round
    }

    fun recordAnswer(question: QuizQuestion, correct: Boolean) {
        if (question.generated) {
            val missed = store.getStrings(MISSED_GENERATED).toMutableSet()
            if (correct) missed -= question.id else if (missed.size < MAX_REVISION) missed += question.id
            store.putStrings(MISSED_GENERATED, missed)
        } else {
            val missed = store.getStrings(MISSED_BANK).toMutableSet()
            if (correct) missed -= question.id else missed += question.id
            store.putStrings(SEEN_BANK, store.getStrings(SEEN_BANK) + question.id)
            store.putStrings(MISSED_BANK, missed)
        }
    }

    private fun planBank(previous: Set<String>, count: Int): List<String> {
        if (count == 0 || bank.isEmpty()) return emptyList()
        val seen = store.getStrings(SEEN_BANK)
        val cycleSeen = activeSeenQuestions(bank, seen)
        // Whole bank answered: start a fresh cycle so every question comes round again once.
        if (cycleSeen.size != seen.size) store.putStrings(SEEN_BANK, cycleSeen)
        return planQuizRound(bank, cycleSeen, store.getStrings(MISSED_BANK), previous, random, count)
    }

    private fun next(source: QuizSource): String? {
        val seedKey = "quizgen_${source.key}_seed"
        val positionKey = "quizgen_${source.key}_pos"
        var seed = store.getInt(seedKey)
        var position = store.getInt(positionKey)
        var id: String? = null
        var attempts = 0
        while (id == null && attempts++ <= source.size) {
            if (seed == 0 || position >= source.size) {
                seed = random.nextInt(1, Int.MAX_VALUE)
                position = 0
            }
            val index = order(source, seed)[position++]
            if (source.question(index) != null) id = "${source.key}:$index"
        }
        store.putInt(seedKey, seed)
        store.putInt(positionKey, position)
        return id
    }

    private fun order(source: QuizSource, seed: Int): IntArray =
        orders.getOrPut("${source.key}/$seed/${source.size}") { IntArray(source.size) { it }.apply { shuffle(Random(seed)) } }

    companion object {
        private const val PREVIOUS_ROUND = "quiz_previous_round"
        private const val SEEN_BANK = "quiz_seen"
        private const val MISSED_BANK = "quiz_missed"
        private const val MISSED_GENERATED = "quiz_missed_generated"
        private const val MAX_REVISION = 60

        fun sources(quran: QuranCorpus?, hadith: HadithCorpus?, language: String): List<QuizSource> {
            val surahs = FullQuranCatalog.surahs
            return listOfNotNull(
                quran?.let { QuranSurahOfAyahSource(it, surahs) },
                quran?.let { QuranNextAyahSource(it) },
                quran?.let { QuranMissingWordSource(it, surahs) },
                hadith?.let { HadithCompletionSource(it) },
                hadith?.let { HadithMissingWordSource(it, quran) },
                SurahFactsSource(surahs, language),
                DuaSource(DuaDatasetRepository.DUAS, language),
                AamalSource(IslamicFestivalRepository.FESTIVALS, FestivalAamal::forFestival, language)
            )
        }

        /** Parses the bundled texts once per process; call off the main thread. */
        fun create(context: Context, prefs: SharedPreferences, language: String): QuizEngine =
            QuizEngine(PrefsProgressStore(prefs), quizQuestions, sources(QuizCorpora.quran(context), QuizCorpora.hadith(context), language))
    }
}

/** The Quran and hadith texts in assets/quiz, loaded lazily and kept for the process lifetime. */
private object QuizCorpora {
    @Volatile private var quran: QuranCorpus? = null
    @Volatile private var hadith: HadithCorpus? = null

    fun quran(context: Context): QuranCorpus? = quran ?: synchronized(this) {
        quran ?: read(context, "quiz/quran-simple.txt")?.let(QuranCorpus::parse)?.also { quran = it }
    }

    fun hadith(context: Context): HadithCorpus? = hadith ?: synchronized(this) {
        hadith ?: read(context, "quiz/hadith-sayings.tsv")?.let(HadithCorpus::parse)?.also { hadith = it }
    }

    private fun read(context: Context, path: String): Sequence<String>? = try {
        context.assets.open(path).bufferedReader().use { it.readLines() }.asSequence()
    } catch (e: IOException) {
        null // A missing file only removes those question types; the rest of the quiz still works.
    }
}
