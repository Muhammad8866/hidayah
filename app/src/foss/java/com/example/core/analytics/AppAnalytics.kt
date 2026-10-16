package com.example.core.analytics

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** The F-Droid build carries no analytics, crash or performance SDK: every call is a no-op. */
@Suppress("UNUSED_PARAMETER")
object AppAnalytics {
    private val _choice = MutableStateFlow<Boolean?>(false)
    val choice = _choice.asStateFlow()

    fun init(context: Context) = Unit
    internal fun emit(event: String, parameters: Map<String, Any>) = Unit
    fun token(raw: String): String = AnalyticsEventPolicy.token(raw)

    fun interaction(screen: String, component: String, action: String = "tap", item: String? = null) = Unit
    fun trackAction(screen: String, component: String, callback: () -> Unit): () -> Unit = callback
    fun trackItemAction(screen: String, component: String, item: String, callback: () -> Unit): () -> Unit = callback
    fun trackOptionalAction(screen: String, component: String, callback: (() -> Unit)?): (() -> Unit)? = callback

    internal data class Interval(val name: String, val start: Long, val epoch: Long)
    internal fun startScreen(name: String) = Interval(name, 0, 0)
    internal fun endScreen(interval: Interval) = Unit

    fun foreground() = Unit
    fun background() = Unit
    fun media(feature: String, action: String) = Unit
    fun playbackState(feature: String, state: Int) = Unit
    fun mediaState(feature: String, result: String, code: String? = null) = Unit
    fun permission(permission: String, granted: Boolean) = Unit
    fun setting(setting: String, option: String? = null) = Unit
    fun userProperty(name: String, value: String) = Unit
    fun content(feature: String, item: String, action: String = "open") = Unit
    fun milestone(feature: String, action: String, item: String? = null) = Unit
    fun prayerAlert(action: String, prayer: String, result: String? = null) = Unit
    fun widget(action: String) = Unit
    fun launch(source: String) = Unit
    fun readerSession(feature: String, pages: Int, startedAt: Long) = Unit
    fun request(status: Int, duration: Long, failed: Boolean = false) = Unit

    class Download internal constructor() {
        fun progress(copied: Long, total: Long) = Unit
        fun finish(result: String) = Unit
    }
    fun download() = Download()
}
