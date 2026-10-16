package com.example.ui.screens

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.model.DuaDatasetRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal data class RecitationSettings(
    val fontStep: Int = 0,
    val showMeaning: Boolean = true, val showTransliteration: Boolean = true,
    val dailyReset: Boolean = true,
    val haptics: Boolean = true, val keepScreenOn: Boolean = false)

internal data class RecitationStoreState(
    val counts: Map<String, Int> = emptyMap(), val favorites: Set<String> = emptySet(),
    val settings: RecitationSettings = RecitationSettings(),
    val streak: Int = 0,
    val lastRead: Map<Int, String> = emptyMap(),
    val lastTab: Int = 0)

/** Counts, favourites, settings and streak for the Manzil / Ruqyah / Wazaif screen. */
internal class RecitationStore(context: Context, private val today: () -> LocalDate = { LocalDate.now() }) {
    private val prefs = context.applicationContext.getSharedPreferences("recitation_collection", Context.MODE_PRIVATE)
    private val flow = MutableStateFlow(load())
    val state: StateFlow<RecitationStoreState> get() = flow

    init { refreshDay() }

    fun count(id: String, target: Int): Int = (flow.value.counts[id] ?: 0).coerceIn(0, target.coerceAtLeast(0))

    fun setCount(id: String, count: Int, target: Int) {
        val value = count.coerceIn(0, target.coerceAtLeast(0))
        flow.value = flow.value.copy(counts = flow.value.counts + (id to value))
        prefs.edit().putInt("count_$id", value).apply()
    }

    fun resetAll(ids: Collection<String>) {
        flow.value = flow.value.copy(counts = flow.value.counts - ids.toSet())
        prefs.edit().apply { ids.forEach { remove("count_$it") } }.apply()
    }

    fun toggleFavorite(id: String) {
        val favorites = flow.value.favorites.let { if (id in it) it - id else it + id }
        flow.value = flow.value.copy(favorites = favorites)
        prefs.edit().putStringSet("favorites", favorites.toSet()).apply()
    }

    fun updateSettings(transform: (RecitationSettings) -> RecitationSettings) {
        val settings = transform(flow.value.settings).let { it.copy(fontStep = it.fontStep.coerceIn(0, 3)) }
        flow.value = flow.value.copy(settings = settings)
        prefs.edit().putInt("font_step", settings.fontStep).putBoolean("meaning", settings.showMeaning)
            .putBoolean("transliteration", settings.showTransliteration).putBoolean("daily_reset", settings.dailyReset)
            .putBoolean("haptics", settings.haptics).putBoolean("keep_screen_on", settings.keepScreenOn).apply()
    }

    fun markRead(tab: Int, id: String) {
        if (flow.value.lastRead[tab] == id) return
        flow.value = flow.value.copy(lastRead = flow.value.lastRead + (tab to id))
        prefs.edit().putString("last_read_$tab", id).apply()
    }

    fun setTab(tab: Int) {
        val value = tab.coerceIn(0, 2)
        flow.value = flow.value.copy(lastTab = value)
        prefs.edit().putInt("last_tab", value).apply()
    }

    /** Idempotent per day. A date earlier than the last recorded completion leaves the streak untouched. */
    fun recordCompletion(@Suppress("UNUSED_PARAMETER") tab: Int) {
        val day = today().toEpochDay()
        val last = prefs.long("streak_day")
        if (last != null && day <= last) return
        val streak = if (last != null && day == last + 1) flow.value.streak.coerceAtLeast(0) + 1 else 1
        flow.value = flow.value.copy(streak = streak)
        prefs.edit().putInt("streak", streak).putLong("streak_day", day).apply()
    }

    fun refreshDay() {
        val day = today().toEpochDay()
        val editor = prefs.edit()
        val stored = prefs.long("day")
        if (stored != day) {
            // Only a forward date change clears counts; a clock moved backwards just re-anchors the day.
            if (stored != null && day > stored && flow.value.settings.dailyReset) {
                flow.value.counts.keys.forEach { editor.remove("count_$it") }
                flow.value = flow.value.copy(counts = emptyMap())
            }
            editor.putLong("day", day)
        }
        val last = prefs.long("streak_day")
        if (flow.value.streak != 0 && (last == null || day - last > 1)) {
            flow.value = flow.value.copy(streak = 0)
            editor.putInt("streak", 0)
        }
        editor.apply()
    }

    private fun load(): RecitationStoreState {
        val all = runCatching { prefs.all }.getOrDefault(emptyMap<String, Any?>())
        val counts = all.mapNotNull { (key, value) ->
            if (key.startsWith("count_") && value is Int) key.removePrefix("count_") to value.coerceAtLeast(0) else null
        }.toMap()
        val lastRead = all.mapNotNull { (key, value) ->
            val tab = key.removePrefix("last_read_").toIntOrNull()
            if (key.startsWith("last_read_") && tab != null && value is String) tab to value else null
        }.toMap()
        val favorites = (all["favorites"] as? Set<*>)?.filterIsInstance<String>()?.toSet().orEmpty()
        // One-time migration of the old "Large text" toggle: it applies only until font_step is first written.
        val fontStep = (all["font_step"] as? Int) ?: if (all["large_text"] == true) 2 else 0
        if (all["font_step"] !is Int && fontStep != 0) prefs.edit().putInt("font_step", fontStep).apply()
        fun flag(key: String, default: Boolean) = all[key] as? Boolean ?: default
        return RecitationStoreState(counts, favorites,
            RecitationSettings(fontStep.coerceIn(0, 3), flag("meaning", true), flag("transliteration", true),
                flag("daily_reset", true), flag("haptics", true), flag("keep_screen_on", false)),
            ((all["streak"] as? Int) ?: 0).coerceAtLeast(0), lastRead, ((all["last_tab"] as? Int) ?: 0).coerceIn(0, 2))
    }

    private fun SharedPreferences.long(key: String): Long? =
        runCatching { if (contains(key)) getLong(key, 0) else null }.getOrNull()
}

/** Arabic font size to line height, both in sp. */
internal fun recitationFontSize(step: Int): Pair<Int, Int> = when (step.coerceIn(0, 3)) {
    0 -> 25 to 46
    1 -> 28 to 50
    2 -> 32 to 56
    else -> 38 to 66
}

internal fun recitationShareText(title: String, source: String, arabic: String, transliteration: String, meaning: String): String =
    listOf(title, arabic, transliteration, meaning, source).map(String::trim).filter(String::isNotEmpty).joinToString("\n\n")

internal fun recitationMatches(query: String, text: String): Boolean {
    val words = DuaDatasetRepository.normalizeForSearch(query).split(' ').filter(String::isNotBlank)
    if (words.isEmpty()) return true
    val normalized = DuaDatasetRepository.normalizeForSearch(text)
    return words.all { it in normalized }
}

/**
 * Where "Continue reading" lands: the item last read when it is still unfinished, otherwise the
 * next unfinished item after it, wrapping to the top. -1 when everything is done.
 */
internal fun recitationResumeIndex(ids: List<String>, lastRead: String?, done: (String) -> Boolean): Int {
    if (ids.isEmpty()) return -1
    val start = lastRead?.let(ids::indexOf)?.takeIf { it >= 0 } ?: 0
    return (start until ids.size).firstOrNull { !done(ids[it]) }
        ?: (0 until start).firstOrNull { !done(ids[it]) }
        ?: -1
}

/** Ayah numbers in Arabic-Indic digits, as printed in the mushaf. */
internal fun recitationAyahMark(number: Int): String =
    "﴿" + number.toString().map { if (it in '0'..'9') '٠' + (it - '0') else it }.joinToString("") + "﴾"
