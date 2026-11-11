package com.example.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

class QuizGeneratorsTest {
    private val quran = QuranCorpus.parse(File("src/main/assets/quiz/quran-simple.txt").readLines().asSequence())
    private val hadith = HadithCorpus.parse(File("src/main/assets/quiz/hadith-sayings.tsv").readLines().asSequence())

    @Test fun tanzilTextParsesWithoutPrefixedBismillah() {
        assertEquals(6236, quran.ayat.size)
        assertEquals("الم", quran.ayat.first { it.surah == 2 && it.number == 1 }.passage.key)
        assertTrue(quran.ayat.first().passage.key.startsWith("بسم الله"))
        assertTrue(hadith.sayings.size > 3000)
    }

    @Test fun everyLanguageProducesWellFormedQuestions() {
        for (language in listOf("en", "hinglish", "hi", "ur")) {
            for (source in QuizEngine.sources(quran, hadith, language)) {
                val step = maxOf(1, source.size / 400)
                var made = 0
                for (index in 0 until source.size step step) {
                    val q = source.question(index) ?: continue
                    made++
                    val where = "${source.key}:$index ($language)"
                    assertEquals(where, "${source.key}:$index", q.id)
                    assertEquals(where, 4, q.options.toSet().size)
                    assertTrue(where, q.options.all { it.isNotBlank() })
                    assertTrue(where, q.reference.isNotBlank())
                    assertEquals(where, q, source.question(index)) // rebuilt identically from its id
                }
                assertTrue("${source.key} ($language) made no questions", made > 0)
            }
        }
    }

    @Test fun rightToLeftLanguagesAskHijriDatesWithArabicMonthNames() {
        for (language in listOf("ar", "fa", "ps", "sd", "ks", "he")) {
            val source = AamalSource(com.example.festival.IslamicFestivalRepository.FESTIVALS, com.example.festival.FestivalAamal::forFestival, language)
            val dated = (0 until source.size).mapNotNull(source::question).filter { it.question == "On which Hijri date is {x}?" }
            assertTrue(language, dated.isNotEmpty())
            for (q in dated) for (option in q.options) {
                assertTrue("${q.id} ($language): $option", option.none { it in 'A'..'Z' || it in 'a'..'z' })
            }
        }
    }

    @Test fun englishQuizTextIsUnchangedBySurahLocalization() {
        val surahs = com.example.quran.FullQuranCatalog.surahs
        val facts = SurahFactsSource(surahs, "en")
        val first = facts.question(0)!!
        assertEquals("Quran · Surah 1 (${surahs[0].englishName})", first.reference)
        assertEquals(surahs[0].englishName, first.answer)
        val ofAyah = QuranSurahOfAyahSource(quran, surahs, "en")
        val q = (0 until ofAyah.size).firstNotNullOf(ofAyah::question)
        assertTrue(q.options.all { option -> surahs.any { it.englishName == option } })
    }

    @Test fun aamalReferenceFollowsTheReaderLanguage() {
        val source = AamalSource(com.example.festival.IslamicFestivalRepository.FESTIVALS, com.example.festival.FestivalAamal::forFestival, "en")
        val deeds = com.example.festival.IslamicFestivalRepository.FESTIVALS.flatMap { com.example.festival.FestivalAamal.forFestival(it.id) }
        val q = (0 until source.size).mapNotNull(source::question).first { it.question == "On which occasion is this deed especially recommended?" }
        assertTrue(q.reference, deeds.any { it.englishSource == q.reference })
    }

    @Test fun missingWordDistractorsNeverFormARealPhrase() {
        val source = QuranMissingWordSource(quran, com.example.quran.FullQuranCatalog.surahs)
        for (index in 0 until source.size step 37) {
            val q = source.question(index) ?: continue
            val words = q.passage!!.split(' ')
            val gap = words.indexOfFirst { "……" in it }
            for (option in q.distractors) {
                val filled = words.toMutableList().also { it[gap] = option }.joinToString(" ")
                val letters = arabicLetters(filled).split(' ')
                val at = arabicLetters(words.take(gap).joinToString(" ")).split(' ').count { it.isNotEmpty() }
                for (start in (at - 2)..at) {
                    if (start < 0 || start + 2 >= letters.size) continue
                    assertTrue("qw:$index '$option'", !quran.index.contains(letters[start], letters[start + 1], letters[start + 2]))
                }
            }
        }
    }

    @Test fun reportQuestionCounts() {
        for (language in listOf("en", "ur")) {
            val counts = QuizEngine.sources(quran, hadith, language).associate { source ->
                source.key to (0 until source.size).count { source.question(it) != null }
            }
            println("QUIZ_COUNTS $language $counts total=${counts.values.sum()}")
        }
    }

    @Test fun engineWalksEachSourceWithoutRepeats() {
        val store = MapStore()
        val engine = QuizEngine(store, quizQuestions, QuizEngine.sources(quran, hadith, "en"), Random(7))
        val shown = mutableListOf<String>()
        repeat(60) {
            val round = engine.planRound()
            assertEquals(QUIZ_ROUND_SIZE, round.size)
            round.forEach { assertNotNull(it, engine.resolve(it)) }
            shown += round.filter { ':' in it }
        }
        // Small sources (aamal) cycle sooner, so check the large ones only.
        val large = shown.filter { it.substringBefore(':') in setOf("qs", "qn", "qw", "hc", "hw", "du", "sf") }
        assertEquals(large.size, large.toSet().size)
    }

    private class MapStore : QuizProgressStore {
        private val ints = HashMap<String, Int>()
        private val strings = HashMap<String, Set<String>>()
        override fun getInt(key: String) = ints[key] ?: 0
        override fun putInt(key: String, value: Int) { ints[key] = value }
        override fun getStrings(key: String) = strings[key].orEmpty()
        override fun putStrings(key: String, value: Set<String>) { strings[key] = value }
    }
}
