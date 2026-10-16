package com.example.ui.screens

import kotlin.random.Random

internal data class KalimaQuestion(
    val prompt: String, val arabic: String?, val options: List<String>, val correctIndex: Int, val entryId: String
)

internal object KalimaQuiz {
    private const val OPTION_COUNT = 4

    /** Deterministic for a seed; mixes "which kalima is this Arabic?", "which kalima has this meaning?", "what comes next after this phrase?" */
    fun questions(seed: Long, count: Int = 8, english: Boolean = false): List<KalimaQuestion> {
        if (count <= 0) return emptyList()
        val random = Random(seed)
        val order = KalimasContent.entries.shuffled(random)
        return List(count) { index ->
            val entry = order[index % order.size]
            when (index % 3) {
                0 -> whichArabic(entry, random, english)
                1 -> whichMeaning(entry, random, english)
                else -> whatComesNext(entry, random, english)
            }
        }
    }

    private fun whichArabic(entry: KalimaEntry, random: Random, english: Boolean) = build(
        prompt = if (english) "Which one is this?" else "Yeh kaun sa kalima hai?",
        arabic = entry.arabic, correct = entry.title, pool = KalimasContent.entries.map { it.title },
        entryId = entry.id, random = random
    )

    private fun whichMeaning(entry: KalimaEntry, random: Random, english: Boolean) = build(
        prompt = if (english) "Which one has this meaning?\n\n${entry.english}"
        else "Yeh tarjuma kis ka hai?\n\n${entry.urdu}",
        arabic = null, correct = entry.title, pool = KalimasContent.entries.map { it.title },
        entryId = entry.id, random = random
    )

    private fun whatComesNext(entry: KalimaEntry, random: Random, english: Boolean): KalimaQuestion {
        val at = random.nextInt(entry.phrases.size - 1)
        val shown = entry.phrases[at].arabic
        return build(
            prompt = if (english) "${entry.title}: what comes next after this?" else "${entry.title}: is ke baad kya aata hai?",
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
