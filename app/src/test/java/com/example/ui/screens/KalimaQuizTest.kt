package com.example.ui.screens

import org.junit.Assert.*
import org.junit.Test

class KalimaQuizTest {
    @Test fun sameSeedGivesSameQuestions() {
        assertEquals(KalimaQuiz.questions(42L), KalimaQuiz.questions(42L))
        assertEquals(KalimaQuiz.questions(7L, 12, english = true), KalimaQuiz.questions(7L, 12, english = true))
        assertNotEquals(KalimaQuiz.questions(1L), KalimaQuiz.questions(2L))
    }

    @Test fun everyQuestionHasOneValidAnswerAmongDistinctOptions() {
        for (seed in 0L until 200L) {
            val questions = KalimaQuiz.questions(seed, 24, english = seed % 2 == 0L)
            assertEquals(24, questions.size)
            questions.forEach { question ->
                val entry = KalimasContent.byId(question.entryId)
                assertNotNull(entry)
                assertEquals(4, question.options.size)
                assertEquals(4, question.options.toSet().size)
                assertTrue(question.correctIndex in question.options.indices)
                assertTrue(question.prompt.isNotBlank())
                val answer = question.options[question.correctIndex]
                if (answer == entry!!.title) return@forEach
                // "What comes next": the answer follows the shown phrase, and the shown phrase is not an option.
                val at = entry.phrases.indexOfFirst { it.arabic == question.arabic }
                assertTrue(at >= 0)
                assertEquals(entry.phrases[at + 1].arabic, answer)
                assertFalse(question.arabic in question.options)
            }
        }
    }

    @Test fun mixesAllThreeQuestionKinds() {
        val questions = KalimaQuiz.questions(3L, 9)
        assertTrue(questions.any { it.arabic == KalimasContent.byId(it.entryId)!!.arabic })
        assertTrue(questions.any { it.arabic == null })
        assertTrue(questions.any { it.arabic != null && it.arabic != KalimasContent.byId(it.entryId)!!.arabic })
        assertTrue(KalimaQuiz.questions(3L, 0).isEmpty())
    }
}
