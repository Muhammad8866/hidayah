package com.example.ui.screens

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TallyCounterStoreTest {
    private val prefs
        get() = ApplicationProvider.getApplicationContext<Context>().getSharedPreferences("tally_test", 0)

    @Before
    fun clear() {
        prefs.edit().clear().commit()
    }

    @Test
    fun oldCountMigratesOnceAndResetDoesNotRestoreIt() {
        prefs.edit().putLong("tally_count", 57L).commit()
        val store = TallyCounterStore(prefs)
        val original = store.load()
        assertEquals(57L, original.active.count)
        store.save(original.update(original.active.setCount(0L)))
        assertEquals(0L, store.load().active.count)
        assertEquals(57L, store.load().active.undo().count)
    }

    @Test
    fun countersAndUndoSurviveReopeningAndRemainIndependent() {
        val store = TallyCounterStore(prefs)
        val original = store.load()
        val first = original.active.copy(step = 10L).increment()
        val second = TallyCounter(name = "Inventory", target = 200L, step = 5L).increment().increment()
        store.save(original.update(first).add(second))
        val restored = store.load()
        assertEquals(second.id, restored.activeId)
        assertEquals(10L, restored.active.count)
        assertEquals(5L, restored.active.undo().count)
        assertEquals(200L, restored.active.target)
        assertEquals(10L, restored.counters.first().count)
    }

    @Test
    fun stepCannotUnderflowOrOverflowAndUndoRestoresExactCount() {
        val nearLimit = TallyCounter(count = Long.MAX_VALUE - 2, step = 10L)
        assertEquals(Long.MAX_VALUE, nearLimit.increment().count)
        assertEquals(nearLimit.count, nearLimit.increment().undo().count)
        val small = TallyCounter(count = 3L, step = 10L)
        assertEquals(0L, small.decrement().count)
        assertEquals(3L, small.decrement().undo().count)
        assertEquals(0, TallyCounter().decrement().previousCounts.size)
    }

    @Test
    fun deleteActiveSelectsRemainingCounterAndCannotDeleteLast() {
        val first = TallyCounter(name = "First")
        val second = TallyCounter(name = "Second")
        val remaining = TallyCollection(listOf(first, second), second.id).delete(second.id)
        assertEquals(first.id, remaining.active.id)
        assertEquals(remaining, remaining.delete(first.id))
    }

    @Test
    fun targetDoesNotStopCountingAndHistoryIsBounded() {
        var counter = TallyCounter(target = 5L)
        repeat(100) { counter = counter.increment() }
        assertEquals(100L, counter.count)
        assertEquals(30, counter.previousCounts.size)
        assertEquals(99L, counter.undo().count)
    }
}
