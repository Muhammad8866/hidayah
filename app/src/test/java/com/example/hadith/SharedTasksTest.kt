package com.example.hadith

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

/**
 * The chapter index downloads a whole PDF; these pin that it runs once per book, outlives the dialog
 * on Wi-Fi, and stops with the last dialog on mobile data.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SharedTasksTest {
    /** Stands in for the store's own scope, which no screen can cancel. [keep] answers "Wi-Fi?". */
    private fun TestScope.storeTasks(keep: () -> Boolean = { true }) =
        SharedTasks<String>(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)), keep)

    @Test fun `on mobile data closing the index cancels the download`() = runTest {
        val tasks = storeTasks(keep = { false })
        val gate = CompletableDeferred<Unit>()
        var stopped = false
        val dialog = launch {
            tasks.awaitShared("bahar-1") {
                try { gate.await(); "done" } catch (cancelled: CancellationException) { stopped = true; throw cancelled }
            }
        }
        advanceUntilIdle()
        assertTrue(tasks.isRunning("bahar-1"))
        dialog.cancelAndJoin()
        advanceUntilIdle()
        assertTrue("the download must stop when nobody waits on a metered network", stopped)
        assertFalse(tasks.isRunning("bahar-1"))
    }

    @Test fun `on Wi-Fi closing the index leaves the download running`() = runTest {
        val tasks = storeTasks(keep = { true })
        val downloads = AtomicInteger()
        val gate = CompletableDeferred<Unit>()
        val dialog = launch { tasks.awaitShared("bahar-1") { downloads.incrementAndGet(); gate.await(); "done" } }
        advanceUntilIdle()
        dialog.cancelAndJoin()
        advanceUntilIdle()
        assertTrue(tasks.isRunning("bahar-1"))
        val reopened = async { tasks.awaitShared("bahar-1") { downloads.incrementAndGet(); "second download" } }
        advanceUntilIdle()
        gate.complete(Unit)
        assertEquals("done", reopened.await())
        assertEquals(1, downloads.get())
    }

    @Test fun `on mobile data one window closing keeps the download for the other`() = runTest {
        val tasks = storeTasks(keep = { false })
        val gate = CompletableDeferred<Unit>()
        val first = launch { tasks.awaitShared("muslim-2") { gate.await(); "done" } }
        val second = async { tasks.awaitShared("muslim-2") { error("the second window must join the first download") } }
        advanceUntilIdle()
        first.cancelAndJoin()
        advanceUntilIdle()
        assertTrue("a window still waits, so the download must go on", tasks.isRunning("muslim-2"))
        gate.complete(Unit)
        assertEquals("done", second.await())
    }

    @Test fun `the network is checked when the last window closes`() = runTest {
        var metered = false
        val tasks = storeTasks(keep = { !metered })
        val gate = CompletableDeferred<Unit>()
        val dialog = launch { tasks.awaitShared("bahar-2") { gate.await(); "done" } }
        advanceUntilIdle()
        metered = true // Wi-Fi dropped to mobile data while the index was open.
        dialog.cancelAndJoin()
        advanceUntilIdle()
        assertFalse(tasks.isRunning("bahar-2"))
    }

    @Test fun `after a cancelled download a new request starts afresh`() = runTest {
        val tasks = storeTasks(keep = { false })
        val downloads = AtomicInteger()
        // A blocking network read ignores cancellation for a while; the cancelled task must not be joined.
        val release = CompletableDeferred<Unit>()
        val dialog = launch {
            tasks.awaitShared("bahar-3") {
                downloads.incrementAndGet()
                withContext(NonCancellable) { release.await() }
                "stale"
            }
        }
        advanceUntilIdle()
        dialog.cancelAndJoin()
        advanceUntilIdle()
        val fresh = async { tasks.awaitShared("bahar-3") { downloads.incrementAndGet(); "fresh" } }
        advanceUntilIdle()
        assertEquals("fresh", fresh.await())
        assertEquals(2, downloads.get())
        release.complete(Unit)
        advanceUntilIdle()
    }

    @Test fun `opening the index twice while it loads starts one download`() = runTest {
        val tasks = storeTasks()
        val downloads = AtomicInteger()
        val gate = CompletableDeferred<Unit>()
        val first = tasks.run("bukhari-1") { downloads.incrementAndGet(); gate.await(); "chapters" }
        val second = tasks.run("bukhari-1") { downloads.incrementAndGet(); "second download" }
        assertSame(first, second)
        gate.complete(Unit)
        assertEquals("chapters", second.await())
        assertEquals(1, downloads.get())
    }

    @Test fun `closing the index does not cancel the download`() = runTest {
        val tasks = storeTasks()
        val gate = CompletableDeferred<Unit>()
        // The dialog waits for the task inside its own coroutine, as LaunchedEffect does.
        val dialog = launch { tasks.run("bahar-1") { gate.await(); "done" }.await() }
        advanceUntilIdle()
        assertTrue(tasks.isRunning("bahar-1"))
        dialog.cancelAndJoin()
        assertTrue("closing the dialog must leave the download running", tasks.isRunning("bahar-1"))
        val reopened = tasks.run("bahar-1") { error("reopening must join the running download") }
        gate.complete(Unit)
        assertEquals("done", reopened.await())
    }

    @Test fun `a finished task is forgotten so a later request runs again`() = runTest {
        val tasks = storeTasks()
        val first = tasks.run("nasai-3") { "first" }
        assertEquals("first", first.await())
        advanceUntilIdle()
        assertFalse(tasks.isRunning("nasai-3"))
        val second = tasks.run("nasai-3") { "second" }
        assertNotSame(first, second)
        assertEquals("second", second.await())
    }

    @Test fun `a failed download reaches the waiter and can be retried`() = runTest {
        val tasks = storeTasks()
        val failed = tasks.run("tirmidhi-1") { throw IOException("Offline") }
        val error = runCatching { failed.await() }.exceptionOrNull()
        assertTrue("got $error", error is IOException)
        advanceUntilIdle()
        assertFalse(tasks.isRunning("tirmidhi-1"))
        assertEquals("retried", tasks.run("tirmidhi-1") { "retried" }.await())
    }

    @Test fun `different books download independently`() = runTest {
        val tasks = storeTasks()
        val gate = CompletableDeferred<Unit>()
        val bukhari = tasks.run("bukhari-1") { gate.await(); "bukhari" }
        val muslim = tasks.run("muslim-1") { "muslim" }
        assertNotSame(bukhari, muslim)
        assertEquals("muslim", muslim.await())
        assertTrue(tasks.isRunning("bukhari-1"))
        gate.complete(Unit)
        assertEquals("bukhari", bukhari.await())
    }

    @Test fun `many concurrent requests for one book share a single task`() = runTest {
        val tasks = storeTasks()
        val downloads = AtomicInteger()
        val gate = CompletableDeferred<Unit>()
        val requests = List(1_000) { tasks.run("abu-dawud-1") { downloads.incrementAndGet(); gate.await(); "ok" } }
        assertTrue(requests.all { it === requests.first() })
        gate.complete(Unit)
        assertEquals("ok", requests.last().await())
        assertEquals(1, downloads.get())
    }
}
