package com.example.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecitationAudioTest {
    private val queue = listOf(RecitationPassage(2, 255, 257), RecitationPassage(112, 1, 4))

    @Test fun urlsCoverEveryAyahOfThePassage() {
        val urls = recitationAudioUrls(queue[0])
        assertEquals(3, urls.size)
        assertEquals("https://everyayah.com/data/Alafasy_128kbps/002255.mp3", urls.first())
        assertEquals("https://everyayah.com/data/Alafasy_128kbps/002257.mp3", urls.last())
    }

    @Test fun stepsCrossPassageBoundaries() {
        assertEquals(0 to 256, recitationStep(queue, 0, 255, true))
        assertEquals(1 to 1, recitationStep(queue, 0, 257, true))
        assertNull(recitationStep(queue, 1, 4, true))
        assertEquals(0 to 257, recitationStep(queue, 1, 1, false))
        assertEquals(0 to 255, recitationStep(queue, 0, 255, false))
    }

    @Test fun repeatCountsPlaysAndZeroLoopsForever() {
        assertFalse(recitationShouldReplay(1, 1))
        assertTrue(recitationShouldReplay(3, 2))
        assertFalse(recitationShouldReplay(3, 3))
        assertTrue(recitationShouldReplay(0, 100))
    }

    @Test fun chipsCycleThroughTheirOptions() {
        assertEquals(1.25f, recitationSpeeds.recitationNext(1f))
        assertEquals(1f, recitationSpeeds.recitationNext(.75f))
        assertEquals(0, recitationRepeats.recitationNext(7))
        assertEquals(1, recitationRepeats.recitationNext(0))
        assertEquals(10, nextRecitationSleep(null))
        assertEquals(20, nextRecitationSleep(8))
        assertEquals(30, nextRecitationSleep(20))
        assertNull(nextRecitationSleep(30))
    }
}
