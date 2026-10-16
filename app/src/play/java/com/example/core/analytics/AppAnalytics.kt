package com.example.core.analytics

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicLong

/** Product telemetry starts automatically; the closed event schema excludes private data. */
object AppAnalytics {
    private var initialized = false
    private var sdk: FirebaseAnalytics? = null
    private var crashes: FirebaseCrashlytics? = null
    private var performance: FirebasePerformance? = null
    private val _choice = MutableStateFlow<Boolean?>(null)
    val choice = _choice.asStateFlow()
    private val consentEpoch = AtomicLong()
    @Volatile private var foreground = false
    private var foregroundStart = 0L
    private val dispatcher = AnalyticsDispatcher(
        propertySink = { name, value ->
            sdk?.setUserProperty(name, value)
            crashes?.setCustomKey(name, value)
        }
    ) { event, parameters ->
        val bundle = Bundle()
        parameters.forEach { (key, value) ->
            when (value) { is String -> bundle.putString(key, value); is Long -> bundle.putLong(key, value) }
        }
        sdk?.logEvent(event, bundle)
        // Crash reports carry the same schema-checked trail, so a crash shows what led to it.
        crashes?.log("$event ${parameters.values.joinToString("/")}")
    }

    @Synchronized fun init(context: Context) {
        if (initialized) return
        sdk = runCatching { FirebaseAnalytics.getInstance(context.applicationContext) }.getOrNull()
        crashes = runCatching { FirebaseCrashlytics.getInstance() }.getOrNull()
        performance = runCatching { FirebasePerformance.getInstance() }.getOrNull()
        applyConsent(true)
        _choice.value = true
        initialized = true
    }

    private fun applyConsent(enabled: Boolean) {
        val denied = FirebaseAnalytics.ConsentStatus.DENIED
        sdk?.setConsent(mapOf(
            FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE to if (enabled) FirebaseAnalytics.ConsentStatus.GRANTED else denied,
            FirebaseAnalytics.ConsentType.AD_STORAGE to denied,
            FirebaseAnalytics.ConsentType.AD_USER_DATA to denied,
            FirebaseAnalytics.ConsentType.AD_PERSONALIZATION to denied
        ))
        sdk?.setAnalyticsCollectionEnabled(enabled)
        runCatching { crashes?.setCrashlyticsCollectionEnabled(enabled) }
        runCatching { performance?.isPerformanceCollectionEnabled = enabled }
    }

    @Synchronized internal fun emit(event: String, parameters: Map<String, Any>) {
        dispatcher.dispatch(_choice.value, event, parameters)
    }

    /** Normalizes an app catalog or enum identifier for the `item` and `option` parameters. */
    fun token(raw: String): String = AnalyticsEventPolicy.token(raw)

    /** [item] names which fixed entry of a shared control was used, e.g. the tab or shortcut. */
    fun interaction(screen: String, component: String, action: String = "tap", item: String? = null) {
        val params = mutableMapOf<String, Any>("screen" to screen, "component" to component, "action" to action)
        if (item != null) params["item"] = token(item)
        emit("ui_interaction", params)
    }

    fun trackAction(screen: String, component: String, callback: () -> Unit): () -> Unit = {
        interaction(screen, component)
        callback()
    }
    fun trackItemAction(screen: String, component: String, item: String, callback: () -> Unit): () -> Unit = {
        interaction(screen, component, item = item)
        callback()
    }
    fun trackOptionalAction(screen: String, component: String, callback: (() -> Unit)?): (() -> Unit)? =
        callback?.let { trackAction(screen, component, it) }

    internal data class Interval(val name: String, val start: Long, val epoch: Long)
    internal fun startScreen(name: String): Interval {
        emit("screen_view", mapOf("screen_name" to name, "screen_class" to name))
        crashes?.setCustomKey("screen", name)
        return Interval(name, SystemClock.elapsedRealtime(), consentEpoch.get())
    }
    internal fun endScreen(interval: Interval) {
        if (interval.epoch == consentEpoch.get()) emit("screen_engagement", mapOf(
            "screen" to interval.name,
            "duration_ms" to (SystemClock.elapsedRealtime() - interval.start).coerceAtLeast(0)))
    }

    @Synchronized fun foreground() {
        if (foreground) return
        foreground = true
        foregroundStart = SystemClock.elapsedRealtime()
        emit("app_foreground", emptyMap())
    }
    @Synchronized fun background() {
        if (!foreground) return
        foreground = false
        emit("app_session_end", mapOf("duration_ms" to
            (SystemClock.elapsedRealtime() - foregroundStart).coerceAtLeast(0)))
    }

    fun media(feature: String, action: String) = emit("media_action", mapOf("feature" to feature, "action" to action))
    fun playbackState(feature: String, state: Int) {
        val result = when (state) { 2 -> "buffering"; 3 -> "ready"; 4 -> "completed"; else -> return }
        mediaState(feature, result)
    }
    fun mediaState(feature: String, result: String, code: String? = null) {
        val params = mutableMapOf<String, Any>("feature" to feature, "result" to result)
        if (code != null) params["error_code"] = code
        emit("media_state", params)
    }
    fun permission(permission: String, granted: Boolean) = emit("permission_result", mapOf(
        "permission" to permission, "result" to if (granted) "granted" else "denied"))
    /** [option] is the fixed choice that was picked, never a typed or measured value. */
    fun setting(setting: String, option: String? = null) {
        val params = mutableMapOf<String, Any>("setting" to setting)
        if (option != null) params["option"] = token(option)
        emit("settings_change", params)
    }

    /** A user-scoped dimension from the closed list in [AnalyticsEventPolicy.userProperties]. */
    @Synchronized fun userProperty(name: String, value: String) =
        dispatcher.property(_choice.value, name, AnalyticsEventPolicy.token(value, limit = 36))

    /** Which catalog entry was opened or played: a reciter, a book collection, a surah. */
    fun content(feature: String, item: String, action: String = "open") = emit("content_select",
        mapOf("feature" to feature, "item" to token(item), "action" to action))

    /** A product outcome such as a finished khatm or a completed Hifz session; no counts. */
    fun milestone(feature: String, action: String, item: String? = null) {
        val params = mutableMapOf<String, Any>("feature" to feature, "action" to action)
        if (item != null) params["item"] = token(item)
        emit("feature_milestone", params)
    }

    /** [result] tells an azan alert from a plain notification. */
    fun prayerAlert(action: String, prayer: String, result: String? = null) {
        val params = mutableMapOf<String, Any>("action" to action, "item" to token(prayer))
        if (result != null) params["result"] = result
        emit("prayer_alert", params)
    }
    fun widget(action: String) = emit("widget_event", mapOf("feature" to "prayer_widget", "action" to action))
    /** How the app was opened: launcher, prayer notification or home-screen widget. */
    fun launch(source: String) = emit("app_launch", mapOf("source" to source))

    /** One reading sitting in a paged reader; [pages] counts page turns, not which pages. */
    fun readerSession(feature: String, pages: Int, startedAt: Long) = emit("reader_session", mapOf(
        "feature" to feature, "value" to pages.coerceAtLeast(0).toLong(),
        "duration_ms" to (SystemClock.elapsedRealtime() - startedAt).coerceAtLeast(0)))
    fun request(status: Int, duration: Long, failed: Boolean = false) = emit("request_result", mapOf(
        "feature" to "content_network", "status_code" to status.coerceAtLeast(0).toLong(),
        "duration_ms" to duration.coerceAtLeast(0), "result" to if (failed) "failed" else "completed"))

    /** One bounded set of milestones per transfer; no URL, path, title or content identifier. */
    class Download internal constructor(private val epoch: Long = consentEpoch.get()) {
        private val started = SystemClock.elapsedRealtime()
        private var milestone = 0
        private val trace: Trace? = if (_choice.value == true) runCatching {
            performance?.newTrace("content_download")?.also { it.start() }
        }.getOrNull() else null
        init { send("started") }
        @Synchronized fun progress(copied: Long, total: Long) {
            if (total <= 0) return
            val reached = ((copied.toDouble() / total * 100).toInt().coerceIn(0, 100) / 25) * 25
            if (reached > milestone && reached in 25..75) {
                milestone = reached
                send("progress", reached.toLong())
            }
        }
        fun finish(result: String) {
            send(result)
            runCatching { trace?.putAttribute("result", token(result)); trace?.stop() }
        }
        private fun send(result: String, progress: Long? = null) {
            if (epoch != consentEpoch.get()) return
            val params = mutableMapOf<String, Any>("feature" to "content_download", "result" to result,
                "duration_ms" to (SystemClock.elapsedRealtime() - started).coerceAtLeast(0))
            if (progress != null) params["milestone"] = progress
            emit("download_state", params)
        }
    }
    fun download() = Download()
}
