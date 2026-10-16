package com.example.core.analytics

import java.util.Locale

/**
 * Closed schema: never accept URLs, text entered by users, coordinates, city or masjid names,
 * amounts, counts or notes. `item` and `option` carry fixed identifiers from the app's own
 * catalogs and settings (a reciter, a book collection, a surah number), never user content.
 */
internal object AnalyticsEventPolicy {
    val events = setOf("ui_interaction", "screen_view", "screen_engagement", "app_foreground",
        "app_session_end", "media_action", "media_state", "download_state", "search_usage",
        "scroll_progress", "permission_result", "settings_change", "request_result",
        "content_select", "feature_milestone", "prayer_alert", "widget_event", "app_launch",
        "reader_session")
    private val labels = setOf("screen", "screen_name", "screen_class", "feature", "component",
        "action", "result", "permission", "setting", "error_code", "item", "option", "source")
    private val numbers = setOf("duration_ms", "value", "milestone", "status_code")
    private val token = Regex("[a-z][a-z0-9_]{0,99}")

    /** User-scoped dimensions: fixed setting choices only, so reports can be split by audience. */
    val userProperties = setOf("app_language", "prayer_method", "asr_method", "high_latitude_rule",
        "hijri_calendar", "madhhab", "quran_reader_mode", "prayer_alerts", "azan_alert",
        "home_widget", "location_mode", "exact_alarms")
    private val propertyValue = Regex("[a-z][a-z0-9_]{0,35}")
    private val separators = Regex("[^a-z0-9]+")

    fun accepts(event: String, parameters: Map<String, Any>): Boolean =
        event in events && parameters.size <= 25 && parameters.all { (key, value) ->
            when (key) {
                in labels -> value is String && token.matches(value)
                in numbers -> value is Long && value >= 0
                else -> false
            }
        }

    fun acceptsUserProperty(name: String, value: String): Boolean =
        name in userProperties && propertyValue.matches(value)

    /** Turns a catalog or enum identifier such as "ar.alafasy" or "HANAFI" into a schema token. */
    fun token(raw: String, limit: Int = 100): String {
        val cleaned = raw.lowercase(Locale.ROOT).replace(separators, "_").trim('_')
        if (cleaned.isEmpty()) return "unknown"
        return (if (cleaned.first() in 'a'..'z') cleaned else "n_$cleaned").take(limit).trimEnd('_')
    }
}
