package com.example.core.analytics

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build
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
        reportPriorAnrs(context.applicationContext)
    }

    @Volatile private var currentWake: String? = null
    @Volatile private var currentPhase: String? = null

    /**
     * Names what woke the process (a receiver or service and its intent action). A background ANR
     * reports no keys or logs, so the label also goes into the process state summary, which the
     * system keeps with the exit record for [reportPriorAnrs] to send on the next launch.
     */
    fun wake(context: Context, component: String, action: String?) {
        val label = ExitDiagnostics.wakeLabel(component, action)
        currentWake = label
        writeProcessSummary(context)
        if (_choice.value == true) runCatching {
            crashes?.setCustomKey("wake_source", label)
            crashes?.log("wake $label")
        }
    }

    /** How far Application.onCreate got; safe before [init], as Firebase starts before onCreate. */
    fun startupPhase(context: Context, phase: String) {
        val label = ExitDiagnostics.token(phase)
        currentPhase = label
        writeProcessSummary(context)
        if (_choice.value != false) runCatching { FirebaseCrashlytics.getInstance().setCustomKey("startup_phase", label) }
    }

    private fun writeProcessSummary(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) runCatching {
            context.getSystemService(ActivityManager::class.java)?.setProcessStateSummary(ExitDiagnostics.truncateUtf8(
                ExitDiagnostics.processSummary(currentWake, currentPhase), ExitDiagnostics.SUMMARY_MAX_BYTES))
        }
    }

    /**
     * Sends each ANR the system recorded since the last report as one non-fatal, carrying the
     * system's own reason (e.g. "Broadcast of Intent { act=… cmp=… }") and the dead process's
     * wake source. Crashlytics' own ANR events for background timeouts arrive without either.
     */
    private fun reportPriorAnrs(app: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        runCatching {
            Thread({
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) runCatching {
                    val crashlytics = crashes ?: return@runCatching
                    if (_choice.value != true) return@runCatching
                    val manager = app.getSystemService(ActivityManager::class.java) ?: return@runCatching
                    val prefs = app.getSharedPreferences(DIAGNOSTICS_PREFS, Context.MODE_PRIVATE)
                    // Identity map: two exits can share every field, and each must keep its own trace.
                    val infoOf = java.util.IdentityHashMap<ExitDiagnostics.ExitRecord, ApplicationExitInfo>()
                    val exits = manager.getHistoricalProcessExitReasons(app.packageName, 0, MAX_EXITS)
                        .filter { it.reason == ApplicationExitInfo.REASON_ANR }
                        .map { info ->
                            ExitDiagnostics.ExitRecord(info.timestamp, info.description, info.importance,
                                ExitDiagnostics.decodeSummary(info.processStateSummary)).also { infoOf[it] = info }
                        }
                    val fresh = ExitDiagnostics.newAnrExits(exits, prefs.getLong(LAST_ANR_KEY, 0L))
                    if (fresh.isEmpty()) return@runCatching
                    // Only reported exits have their trace read; a background ANR's description can be just "bg anr".
                    fresh.forEach { record ->
                        val subject = ExitDiagnostics.anrSubject(readTraceHead(infoOf[record]))
                        crashlytics.recordException(PriorAnrReport(ExitDiagnostics.formatAnrReport(record.copy(subject = subject))))
                    }
                    // commit: the marker must be on disk before this process can die, or the ANR is sent again.
                    prefs.edit().putLong(LAST_ANR_KEY, fresh.maxOf { it.timestamp }).commit()
                }
            }, "anr-exit-report").apply { isDaemon = true }.start()
        }
    }

    /** The first [ExitDiagnostics.TRACE_HEAD_BYTES] of an exit's ANR trace, or null; never throws. */
    private fun readTraceHead(info: ApplicationExitInfo?): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        if (info == null) return null
        return runCatching {
            info.traceInputStream?.use { input ->
                val buffer = ByteArray(ExitDiagnostics.TRACE_HEAD_BYTES)
                var total = 0
                while (total < buffer.size) {
                    val read = input.read(buffer, total, buffer.size - total)
                    if (read <= 0) break
                    total += read
                }
                if (total == 0) null else String(buffer, 0, total, Charsets.UTF_8)
            }
        }.getOrNull()
    }

    private const val DIAGNOSTICS_PREFS = "analytics_diagnostics"
    private const val LAST_ANR_KEY = "last_reported_anr_exit"
    private const val MAX_EXITS = 16

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

    /** One onboarding funnel step from [AnalyticsEventPolicy.onboardingSteps]; others are ignored. */
    fun onboarding(step: String) {
        if (step in AnalyticsEventPolicy.onboardingSteps) milestone("onboarding", step)
    }

    /** Call with the current streak length; only fixed buckets (d3, d7, d14, d30, d100) are sent. */
    fun streak(feature: String, days: Int) {
        AnalyticsEventPolicy.streakBucket(days)?.let { milestone(feature, "streak", it) }
    }

    /** Time from a play request until the player is ready; start at play(), call [AudioLoad.ready]. */
    class AudioLoad internal constructor(private val feature: String) {
        private val epoch = consentEpoch.get()
        private val started = SystemClock.elapsedRealtime()
        private var done = false
        private val trace: Trace? = if (_choice.value == true) runCatching {
            performance?.newTrace("audio_load")?.also { it.putAttribute("feature", token(feature)); it.start() }
        }.getOrNull() else null
        @Synchronized fun ready() = finish("ready")
        @Synchronized fun failed() = finish("failed")
        private fun finish(result: String) {
            if (done) return
            done = true
            runCatching { trace?.putAttribute("result", result); trace?.stop() }
            if (epoch == consentEpoch.get()) emit("media_load", mapOf("feature" to token(feature),
                "result" to result, "duration_ms" to (SystemClock.elapsedRealtime() - started).coerceAtLeast(0)))
        }
    }
    fun audioLoad(feature: String) = AudioLoad(feature)

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

/** One fixed type, so every prior-ANR report groups into a single Crashlytics issue. */
internal class PriorAnrReport(message: String) : Exception(message)
