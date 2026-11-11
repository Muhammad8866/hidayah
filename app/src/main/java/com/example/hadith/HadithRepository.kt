package com.example.hadith

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    private val cleanupScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    fun init(context: Context) {
        HadithPdfStore.init(context)
        BookSearchIndex.init(context)
        val appContext = context.applicationContext
        cleanupScope.launch { runCatching { HadithPdfStore.purgeRemovedEditions(appContext) } }
    }
    suspend fun load(forceRefresh: Boolean = false) { _state.value = HadithCatalogState() }
}
