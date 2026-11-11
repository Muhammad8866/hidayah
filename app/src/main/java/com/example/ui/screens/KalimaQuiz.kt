package com.example.ui.screens

import com.example.ui.localization.localizePhrase
import kotlin.random.Random

internal data class KalimaQuestion(
    val prompt: String, val arabic: String?, val options: List<String>, val correctIndex: Int, val entryId: String
)

internal object KalimaQuiz {
    private const val OPTION_COUNT = 4

    /** Deterministic for a seed; mixes "which kalima is this Arabic?", "which kalima has this meaning?", "what comes next after this phrase?" */
    fun questions(seed: Long, count: Int = 8, english: Boolean = false, language: String = "en"): List<KalimaQuestion> {
        if (count <= 0) return emptyList()
        // English mode follows the app language; Roman Urdu prompts stay as written.
        val tr: (String) -> String = { if (english) localizePhrase(it, language) else it }
        val random = Random(seed)
        val order = KalimasContent.entries.shuffled(random)
        return List(count) { index ->
            val entry = order[index % order.size]
            when (index % 3) {
                0 -> whichArabic(entry, random, english, tr)
                1 -> whichMeaning(entry, random, english, tr)
                else -> whatComesNext(entry, random, english, tr)
            }
        }
    }

    private fun whichArabic(entry: KalimaEntry, random: Random, english: Boolean, tr: (String) -> String) = build(
        prompt = if (english) tr("Which one is this?") else "Yeh kaun sa kalima hai?",
        arabic = entry.arabic, correct = tr(entry.title), pool = KalimasContent.entries.map { tr(it.title) },
        entryId = entry.id, random = random
    )

    private fun whichMeaning(entry: KalimaEntry, random: Random, english: Boolean, tr: (String) -> String) = build(
        prompt = if (english) tr("Which one has this meaning?") + "\n\n" + tr(entry.english)
        else "Yeh tarjuma kis ka hai?\n\n${entry.urdu}",
        arabic = null, correct = tr(entry.title), pool = KalimasContent.entries.map { tr(it.title) },
        entryId = entry.id, random = random
    )

    private fun whatComesNext(entry: KalimaEntry, random: Random, english: Boolean, tr: (String) -> String): KalimaQuestion {
        val at = random.nextInt(entry.phrases.size - 1)
        val shown = entry.phrases[at].arabic
        return build(
            prompt = if (english) tr(entry.title) + ": " + tr("what comes next after this?") else "${entry.title}: is ke baad kya aata hai?",
            arabic = shown, correct = entry.phrases[at + 1].arabic,
            // Never offer the shown phrase itself as an answer.
            pool = KalimasContent.entries.flatMap { it.phrases }.map { it.arabic }.filter { it != shown },
            entryId = entry.id, random = random
        )
    }

    private fun build(
        prompt: String, arabic: String?, correct: String, pool: List<String>, entryId: String, random: Random
    ): KalimaQuestion {
        val wrong = pool.distinct().filter { it != correct }.shuffled(random).take(OPTION_COUNT - 1)
        val options = (wrong + correct).shuffled(random)
        return KalimaQuestion(prompt, arabic, options, options.indexOf(correct), entryId)
    }
}
