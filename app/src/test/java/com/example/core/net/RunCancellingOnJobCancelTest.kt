package com.example.core.net

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RunCancellingOnJobCancelTest {
    @Test
    fun completedWorkDoesNotCallCancel() = runBlocking {
        val cancels = AtomicInteger()
        val result = runCancellingOnJobCancel({ cancels.incrementAndGet() }) { 42 }
        assertEquals(42, result)
        assertEquals(0, cancels.get())
    }

    @Test
    fun cancellingCallerCancelsBlockingWork() = runBlocking {
        val started = CountDownLatch(1)
        val stop = CountDownLatch(1)
        val job = launch(Dispatchers.IO) {
            runCatching {
                runCancellingOnJobCancel({ stop.countDown() }) {
                    started.countDown()
                    stop.await(10, TimeUnit.SECONDS)
                }
            }
        }
        assertTrue(started.await(5, TimeUnit.SECONDS))
        job.cancelAndJoin()
        assertEquals(0L, stop.count)
    }

    @Test(expected = IllegalStateException::class)
    fun failingWorkPropagates(): Unit = runBlocking {
        runCancellingOnJobCancel({}) { throw IllegalStateException("boom") }
    }
}
