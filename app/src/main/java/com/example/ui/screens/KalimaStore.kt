package com.example.ui.screens

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal enum class KalimaMeaning { ROMAN_URDU, ENGLISH }

internal data class KalimaUserState(
    val favorites: Set<String> = emptySet(), val memorized: Set<String> = emptySet(),
    val lastReadId: String? = null, val arabicScale: Float = 1f,
    val meaning: KalimaMeaning = KalimaMeaning.ROMAN_URDU,
    val showRoman: Boolean = true, val showMeaning: Boolean = true,
    val counts: Map<String, Int> = emptyMap()
)

/** Reader progress and preferences for the Kalimas screen; ids no longer in the content are dropped. */
internal class KalimaStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("kalimas", Context.MODE_PRIVATE)
    private val ids = KalimasContent.entries.map { it.id }.toSet()
    private val _state = MutableStateFlow(load())
    val state: StateFlow<KalimaUserState> = _state.asStateFlow()

    private fun load(): KalimaUserState {
        fun idSet(key: String) = prefs.getStringSet(key, emptySet()).orEmpty().filter { it in ids }.toSet()
        return KalimaUserState(
            favorites = idSet(KEY_FAVORITES),
            memorized = idSet(KEY_MEMORIZED),
            lastReadId = prefs.getString(KEY_LAST_READ, null)?.takeIf { it in ids },
            arabicScale = clampScale(prefs.getFloat(KEY_SCALE, 1f)),
            meaning = KalimaMeaning.values().firstOrNull { it.name == prefs.getString(KEY_MEANING, null) }
                ?: KalimaMeaning.ROMAN_URDU,
            showRoman = prefs.getBoolean(KEY_SHOW_ROMAN, true),
            showMeaning = prefs.getBoolean(KEY_SHOW_MEANING, true),
            counts = ids.mapNotNull { id ->
                prefs.getInt(KEY_COUNT_PREFIX + id, 0).takeIf { it > 0 }?.let { id to it }
            }.toMap()
        )
    }

    private fun clampScale(scale: Float) = if (scale.isNaN()) 1f else scale.coerceIn(MIN_SCALE, MAX_SCALE)

    fun toggleFavorite(id: String) {
        if (id !in ids) return
        val current = _state.value.favorites
        val updated = if (id in current) current - id else current + id
        _state.value = _state.value.copy(favorites = updated)
        prefs.edit().putStringSet(KEY_FAVORITES, updated.toSet()).apply()
    }

    fun setMemorized(id: String, value: Boolean) {
        if (id !in ids) return
        val current = _state.value.memorized
        val updated = if (value) current + id else current - id
        _state.value = _state.value.copy(memorized = updated)
        prefs.edit().putStringSet(KEY_MEMORIZED, updated.toSet()).apply()
    }

    fun setLastRead(id: String) {
        if (id !in ids || _state.value.lastReadId == id) return
        _state.value = _state.value.copy(lastReadId = id)
        prefs.edit().putString(KEY_LAST_READ, id).apply()
    }

    fun setArabicScale(scale: Float) {
        val clamped = clampScale(scale)
        _state.value = _state.value.copy(arabicScale = clamped)
        prefs.edit().putFloat(KEY_SCALE, clamped).apply()
    }

    fun setMeaning(meaning: KalimaMeaning) {
        _state.value = _state.value.copy(meaning = meaning)
        prefs.edit().putString(KEY_MEANING, meaning.name).apply()
    }

    fun setShowRoman(value: Boolean) {
        _state.value = _state.value.copy(showRoman = value)
        prefs.edit().putBoolean(KEY_SHOW_ROMAN, value).apply()
    }

    fun setShowMeaning(value: Boolean) {
        _state.value = _state.value.copy(showMeaning = value)
        prefs.edit().putBoolean(KEY_SHOW_MEANING, value).apply()
    }

    fun incrementCount(id: String) {
        if (id !in ids) return
        val next = ((_state.value.counts[id] ?: 0) + 1).coerceAtMost(MAX_COUNT)
        _state.value = _state.value.copy(counts = _state.value.counts + (id to next))
        prefs.edit().putInt(KEY_COUNT_PREFIX + id, next).apply()
    }

    fun resetCount(id: String) {
        if (id !in ids) return
        _state.value = _state.value.copy(counts = _state.value.counts - id)
        prefs.edit().remove(KEY_COUNT_PREFIX + id).apply()
    }

    companion object {
        const val MIN_SCALE = 0.8f
        const val MAX_SCALE = 1.8f
        private const val MAX_COUNT = 999_999
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_MEMORIZED = "memorized"
        private const val KEY_LAST_READ = "last_read"
        private const val KEY_SCALE = "arabic_scale"
        private const val KEY_MEANING = "meaning"
        private const val KEY_SHOW_ROMAN = "show_roman"
        private const val KEY_SHOW_MEANING = "show_meaning"
        private const val KEY_COUNT_PREFIX = "count_"
    }
}
