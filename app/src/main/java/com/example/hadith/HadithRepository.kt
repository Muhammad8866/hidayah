package com.example.hadith

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HadithCatalogState(
    val books: List<HadithBook> = HadithCatalog.books,
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val error: String? = null,
    val isCached: Boolean = true
)

/** The verified complete-set index ships in the app, so browsing works offline. */
object HadithRepository {
    private val _state = MutableStateFlow(HadithCatalogState())
    val state: StateFlow<HadithCatalogState> = _state.asStateFlow()
    fun init(context: Context) = HadithPdfStore.init(context)
    suspend fun load(forceRefresh: Boolean = false) { _state.value = HadithCatalogState() }
}
