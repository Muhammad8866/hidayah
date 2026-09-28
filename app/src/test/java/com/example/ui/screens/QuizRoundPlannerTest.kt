package com.example.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class QuizRoundPlannerTest {
    private val questions = (1..12).map {
        QuizQuestion("q$it", "Question $it", "Right", listOf("A", "B", "C"), "Reference")
    }

    @Test fun roundHasTenDistinctQuestions() {
        val round = planQuizRound(questions, emptySet(), emptySet(), emptySet(), Random(1))
        assertEquals(10, round.size)
        assertEquals(10, round.toSet().size)
    }

    @Test fun nextRoundUsesUnseenQuestionsFirst() {
        val first = planQuizRound(questions, emptySet(), emptySet(), emptySet(), Random(1))
        val second = planQuizRound(questions, first.toSet(), emptySet(), first.toSet(), Random(2))
        assertEquals(questions.map { it.id }.toSet() - first.toSet(), second.take(2).toSet())
    }

    @Test fun missedQuestionsArePreferredForRevision() {
        val round = planQuizRound(questions, questions.map { it.id }.toSet(), setOf("q2", "q4"), emptySet(), Random(3))
        assertTrue(round.take(2).toSet().containsAll(setOf("q2", "q4")))
    }

    @Test fun seenListResetsOnlyAfterWholeBankIsSeen() {
        val all = questions.map { it.id }.toSet()
        assertEquals(setOf("q1"), activeSeenQuestions(questions, setOf("q1", "removed_id")))
        assertEquals(emptySet<String>(), activeSeenQuestions(questions, all))
    }

    @Test fun fullBankIsPlayedBeforeAnyQuestionRepeats() {
        var seen = emptySet<String>()
        var previous = emptySet<String>()
        val shown = mutableListOf<String>()
        repeat(quizQuestions.size / QUIZ_ROUND_SIZE) { roundNumber ->
            seen = activeSeenQuestions(quizQuestions, seen)
            val round = planQuizRound(quizQuestions, seen, emptySet(), previous, Random(roundNumber))
            shown += round
            seen = seen + round
            previous = round.toSet()
        }
        assertEquals(shown.size, shown.toSet().size)
    }

    @Test fun questionBankIsWellFormed() {
        assertTrue(quizQuestions.size >= 100)
        assertEquals(quizQuestions.size, quizQuestions.map { it.id }.toSet().size)
        quizQuestions.forEach {
            assertEquals(it.id, 4, it.options.toSet().size)
            assertTrue(it.id, it.reference.isNotBlank())
        }
    }
}
