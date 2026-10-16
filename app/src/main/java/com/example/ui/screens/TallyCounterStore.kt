package com.example.ui.screens

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

internal const val TALLY_MAX_NAME = 40
internal const val TALLY_MAX_STEP = 1_000_000L
internal const val TALLY_MAX_ROUNDS = 10_000
private const val TALLY_HISTORY = 30
private const val TALLY_DAYS_KEPT = 14

internal fun tallyToday(): Long = LocalDate.now().toEpochDay()

private fun safeAdd(a: Long, b: Long): Long = if (b > 0 && a > Long.MAX_VALUE - b) Long.MAX_VALUE else (a + b).coerceAtLeast(0)

/**
 * One named counter. [target] is the size of one round (e.g. 33); [rounds] is how many rounds the user
 * aims for (0 = open-ended). [count] is the running total and is never capped by the target.
 */
internal data class TallyCounter(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val count: Long = 0,
    val target: Long = 0,
    val step: Long = 1,
    val previousCounts: List<Long> = emptyList(),
    val rounds: Int = 0,
    val todayDay: Long = 0,
    val todayCount: Long = 0,
    val lifetime: Long = 0
) {
    /** Rounds fully completed so far (0 when no target). */
    val completedRounds: Long get() = if (target > 0) count / target else 0
    /** Progress inside the current round; a just-finished round shows as full rather than empty. */
    val inRound: Long get() = when {
        target <= 0 -> count
        count > 0 && count % target == 0L -> target
        else -> count % target
    }
    /** Total the user is aiming for, or 0 when open-ended. Overflow-safe. */
    val goal: Long get() = when {
        target <= 0 -> 0
        rounds <= 0 -> target
        target > Long.MAX_VALUE / rounds -> Long.MAX_VALUE
        else -> target * rounds
    }
    val goalReached: Boolean get() = goal > 0 && count >= goal
    fun todayOn(day: Long): Long = if (todayDay == day) todayCount else 0

    /** Records [delta] taps into today's activity; negative deltas (undo/decrement) never go below zero. */
    private fun track(delta: Long, day: Long): TallyCounter {
        val base = todayOn(day)
        return copy(todayDay = day, todayCount = safeAdd(base, delta),
            lifetime = if (delta > 0) safeAdd(lifetime, delta) else lifetime)
    }

    fun setCount(value: Long): TallyCounter {
        val next = value.coerceAtLeast(0)
        if (next == count) return this
        return copy(count = next, previousCounts = (previousCounts + count).takeLast(TALLY_HISTORY))
    }

    fun increment(day: Long = tallyToday()): TallyCounter {
        val next = safeAdd(count, step)
        return setCount(next).track(next - count, day)
    }

    fun decrement(day: Long = tallyToday()): TallyCounter {
        val next = (count - step).coerceAtLeast(0)
        return setCount(next).track(next - count, day)
    }

    /** Resets to zero but keeps the old value in history so it can be undone. Does not touch today's activity. */
    fun reset(): TallyCounter = setCount(0)

    fun undo(day: Long = tallyToday()): TallyCounter = previousCounts.lastOrNull()?.let { previous ->
        val undone = copy(count = previous, previousCounts = previousCounts.dropLast(1))
        // Only taps (small steps) count as activity; undoing a reset/manual edit is not activity.
        val delta = previous - count
        if (delta in -step..-1) undone.track(delta, day) else undone
    } ?: this
}

internal data class TallyCollection(val counters: List<TallyCounter>, val activeId: String) {
    val active: TallyCounter get() = counters.firstOrNull { it.id == activeId } ?: counters.first()
    fun update(counter: TallyCounter) = copy(counters = counters.map { if (it.id == counter.id) counter else it })
    fun add(counter: TallyCounter) = copy(counters = counters + counter, activeId = counter.id)
    fun select(id: String) = if (counters.any { it.id == id }) copy(activeId = id) else this
    fun delete(id: String): TallyCollection {
        if (counters.size <= 1 || counters.none { it.id == id }) return this
        val index = counters.indexOfFirst { it.id == id }
        val remaining = counters.filterNot { it.id == id }
        val nextActive = if (activeId == id) remaining[(index - 1).coerceAtLeast(0)].id else activeId
        return copy(counters = remaining, activeId = nextActive)
    }
    fun todayTotal(day: Long = tallyToday()): Long = counters.fold(0L) { sum, c -> safeAdd(sum, c.todayOn(day)) }
}

/**
 * Persists counters as JSON in SharedPreferences under the original `tally_collection_v1` key. New fields are
 * optional, so data written by older versions loads unchanged and older builds ignore the new fields.
 */
internal class TallyCounterStore(private val prefs: SharedPreferences) {
    fun load(): TallyCollection {
        val saved = runCatching {
            val root = JSONObject(prefs.getString(KEY, "") ?: "")
            val array = root.getJSONArray("counters")
            val counters = (0 until array.length()).mapNotNull { index ->
                runCatching {
                    val item = array.getJSONObject(index)
                    val history = item.optJSONArray("history") ?: JSONArray()
                    TallyCounter(
                        id = item.getString("id").ifBlank { UUID.randomUUID().toString() },
                        name = item.optString("name").take(TALLY_MAX_NAME),
                        count = item.optLong("count").coerceAtLeast(0),
                        target = item.optLong("target").coerceAtLeast(0),
                        step = item.optLong("step", 1).coerceIn(1, TALLY_MAX_STEP),
                        previousCounts = (maxOf(0, history.length() - TALLY_HISTORY) until history.length())
                            .map { history.optLong(it).coerceAtLeast(0) },
                        rounds = item.optInt("rounds", 0).coerceIn(0, TALLY_MAX_ROUNDS),
                        todayDay = item.optLong("todayDay", 0),
                        todayCount = item.optLong("todayCount", 0).coerceAtLeast(0),
                        lifetime = item.optLong("lifetime", 0).coerceAtLeast(0)
                    )
                }.getOrNull()
            }.distinctBy { it.id }
            require(counters.isNotEmpty())
            val requested = root.optString("active")
            TallyCollection(counters, counters.firstOrNull { it.id == requested }?.id ?: counters.first().id)
        }.getOrNull()
        if (saved != null) return saved
        // The first advanced counter inherits the earlier single counter exactly once.
        val counter = TallyCounter(count = prefs.getLong("tally_count", 0).coerceAtLeast(0))
        return TallyCollection(listOf(counter), counter.id).also { save(it) }
    }

    /** Daily totals across all counters for recent days (epochDay -> taps). */
    fun loadDaily(): Map<Long, Long> = runCatching {
        val obj = JSONObject(prefs.getString(DAILY_KEY, "") ?: "")
        obj.keys().asSequence().mapNotNull { k -> k.toLongOrNull()?.let { it to obj.optLong(k).coerceAtLeast(0) } }.toMap()
    }.getOrDefault(emptyMap())

    fun save(collection: TallyCollection, daily: Map<Long, Long>? = null, sync: Boolean = false) {
        val array = JSONArray()
        collection.counters.forEach { counter ->
            array.put(JSONObject().put("id", counter.id).put("name", counter.name)
                .put("count", counter.count).put("target", counter.target).put("step", counter.step)
                .put("history", JSONArray(counter.previousCounts))
                .put("rounds", counter.rounds).put("todayDay", counter.todayDay)
                .put("todayCount", counter.todayCount).put("lifetime", counter.lifetime))
        }
        val editor = prefs.edit().putString(KEY, JSONObject().put("active", collection.activeId)
            .put("counters", array).toString())
        if (daily != null) {
            val obj = JSONObject()
            daily.entries.sortedByDescending { it.key }.take(TALLY_DAYS_KEPT).forEach { obj.put(it.key.toString(), it.value) }
            editor.putString(DAILY_KEY, obj.toString())
        }
        if (sync) editor.commit() else editor.apply()
    }

    companion object {
        private const val KEY = "tally_collection_v1"
        private const val DAILY_KEY = "tally_daily_v1"

        fun recordDaily(daily: Map<Long, Long>, day: Long, delta: Long): Map<Long, Long> {
            if (delta == 0L) return daily
            val next = safeAdd(daily[day] ?: 0, delta)
            return (daily + (day to next)).entries.sortedByDescending { it.key }.take(TALLY_DAYS_KEPT)
                .associate { it.key to it.value }
        }
    }
}
