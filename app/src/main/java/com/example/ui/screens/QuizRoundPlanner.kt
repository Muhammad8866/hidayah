package com.example.ui.screens

import kotlin.random.Random

/**
 * Stable IDs let the question bank grow without invalidating saved practice progress.
 *
 * Hand-written bank questions are English UI copy translated through the phrase packs. Generated
 * questions (ids of the form `source:index`) carry text that is already in the reader's language
 * or in Arabic, so it is shown verbatim: [question] is a prompt whose `{x}` is filled with
 * [promptArg], and [passage] is the ayah, hadith or dua being asked about.
 */
internal data class QuizQuestion(
    val id: String,
    val question: String,
    val answer: String,
    val distractors: List<String>,
    val reference: String,
    val passage: String? = null,
    val passageArabic: Boolean = false,
    val optionsArabic: Boolean = false,
    val promptArg: String? = null
) {
    val options: List<String> get() = distractors + answer
    val generated: Boolean get() = ':' in id
}

internal const val QUIZ_ROUND_SIZE = 10

/**
 * Questions already shown in the current cycle. Once every question has been seen, a new cycle
 * starts so the whole bank is gone through again before anything repeats.
 */
internal fun activeSeenQuestions(questions: List<QuizQuestion>, seen: Set<String>): Set<String> {
    val known = seen.filterTo(mutableSetOf()) { id -> questions.any { it.id == id } }
    return if (questions.all { it.id in known }) emptySet() else known
}

/**
 * Pick each question at most once per round. Unseen questions of the current cycle always come
 * first (previously missed ones leading, so a new cycle opens with revision); only when fewer
 * than a full round remain is the round topped up, preferring missed questions and avoiding the
 * round just played.
 */
internal fun planQuizRound(
    questions: List<QuizQuestion>,
    seen: Set<String>,
    missed: Set<String>,
    previousRound: Set<String>,
    random: Random = Random.Default,
    size: Int = QUIZ_ROUND_SIZE
): List<String> {
    val cycleSeen = activeSeenQuestions(questions, seen)
    val shuffled = questions.shuffled(random)
    val fresh = shuffled.filter { it.id !in cycleSeen }
    val outsidePrevious = shuffled.filter { it.id in cycleSeen && it.id !in previousRound }
    val recent = shuffled.filter { it.id in cycleSeen && it.id in previousRound }
    val freshOutsidePrevious = fresh.filter { it.id !in previousRound }
    return (freshOutsidePrevious.filter { it.id in missed } +
        freshOutsidePrevious.filter { it.id !in missed } +
        fresh.filter { it.id in previousRound } +
        outsidePrevious.filter { it.id in missed } +
        outsidePrevious.filter { it.id !in missed } +
        recent.filter { it.id in missed } +
        recent.filter { it.id !in missed })
        .distinctBy { it.id }
        .take(size)
        .map { it.id }
}
