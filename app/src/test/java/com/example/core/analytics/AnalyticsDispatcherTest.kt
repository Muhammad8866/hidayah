package com.example.core.analytics

import org.junit.Assert.*
import org.junit.Test

class AnalyticsDispatcherTest {
    @Test fun `app lifecycle event does not use the SDK reserved background name`() {
        assertFalse(AnalyticsEventPolicy.accepts("app_background", mapOf("duration_ms" to 100L)))
        assertTrue(AnalyticsEventPolicy.accepts("app_session_end", mapOf("duration_ms" to 100L)))
    }
    @Test fun `no collection before consent after decline or after revocation`() {
        val sent = mutableListOf<String>()
        val dispatcher = AnalyticsDispatcher { name, _ -> sent += name }
        val params = mapOf<String, Any>("screen" to "home_screen", "component" to "open_settings", "action" to "tap")
        dispatcher.dispatch(null, "ui_interaction", params)
        dispatcher.dispatch(false, "ui_interaction", params)
        assertTrue(sent.isEmpty())
        dispatcher.dispatch(true, "ui_interaction", params)
        assertEquals(listOf("ui_interaction"), sent)
        dispatcher.dispatch(false, "ui_interaction", params)
        assertEquals(1, sent.size)
    }

    @Test fun `private fields and raw text never reach the sink`() {
        val sent = mutableListOf<String>()
        val dispatcher = AnalyticsDispatcher { name, _ -> sent += name }
        for (key in listOf("query", "email", "latitude", "longitude", "amount", "prayer_status", "count", "url", "content_id", "user_id", "auth_token")) {
            dispatcher.dispatch(true, "ui_interaction", mapOf(key to "private_value"))
        }
        for (text in listOf("my search text", "person@example.com", "https://example.com", "28.613921", "", "x".repeat(101))) {
            dispatcher.dispatch(true, "ui_interaction", mapOf("component" to text))
        }
        dispatcher.dispatch(true, "custom_unreviewed_event", emptyMap())
        assertTrue(sent.isEmpty())
    }

    @Test fun `structured performance and progress events retain numeric types`() {
        val sent = mutableListOf<Map<String, Any>>()
        val dispatcher = AnalyticsDispatcher { _, params -> sent += params }
        dispatcher.dispatch(true, "download_state", mapOf("result" to "progress", "milestone" to 75L, "duration_ms" to 1234L))
        assertEquals(75L, sent.single()["milestone"])
        dispatcher.dispatch(true, "download_state", mapOf("milestone" to -1L))
        dispatcher.dispatch(true, "download_state", mapOf("milestone" to "75"))
        assertEquals(1, sent.size)
    }

    @Test fun `analytics failure never breaks the user action`() {
        val dispatcher = AnalyticsDispatcher { _, _ -> error("backend unavailable") }
        dispatcher.dispatch(true, "app_foreground", emptyMap())
    }

    @Test fun `catalog identifiers become schema tokens`() {
        assertEquals("ar_alafasy", AnalyticsEventPolicy.token("ar.alafasy"))
        assertEquals("hanafi", AnalyticsEventPolicy.token("HANAFI"))
        assertEquals("n_1_25x", AnalyticsEventPolicy.token("1.25x"))
        assertEquals("unknown", AnalyticsEventPolicy.token("نماز"))
        assertEquals(36, AnalyticsEventPolicy.token("a".repeat(80), limit = 36).length)
        val sent = mutableListOf<Map<String, Any>>()
        val dispatcher = AnalyticsDispatcher { _, params -> sent += params }
        dispatcher.dispatch(true, "content_select", mapOf("feature" to "quran_audio", "item" to "surah_36", "action" to "play"))
        dispatcher.dispatch(true, "settings_change", mapOf("setting" to "set_language", "option" to "ur"))
        dispatcher.dispatch(true, "app_launch", mapOf("source" to "home_widget"))
        assertEquals(3, sent.size)
        // Raw text is rejected even in the new parameters; callers must pass a token.
        dispatcher.dispatch(true, "content_select", mapOf("feature" to "quran_audio", "item" to "Surah Yasin"))
        dispatcher.dispatch(true, "settings_change", mapOf("setting" to "set_location", "option" to "New Delhi"))
        assertEquals(3, sent.size)
    }

    @Test fun `user properties are a closed list and follow consent`() {
        val sent = mutableListOf<Pair<String, String>>()
        val dispatcher = AnalyticsDispatcher(propertySink = { name, value -> sent += name to value }) { _, _ -> }
        dispatcher.property(null, "app_language", "ur")
        dispatcher.property(false, "app_language", "ur")
        dispatcher.property(true, "city", "delhi")
        dispatcher.property(true, "user_id", "abc123")
        dispatcher.property(true, "app_language", "Urdu (Pakistan)")
        dispatcher.property(true, "prayer_method", "x".repeat(37))
        assertTrue(sent.isEmpty())
        dispatcher.property(true, "app_language", "ur")
        dispatcher.property(true, "prayer_method", "karachi")
        assertEquals(listOf("app_language" to "ur", "prayer_method" to "karachi"), sent)
        AnalyticsDispatcher(propertySink = { _, _ -> error("backend unavailable") }) { _, _ -> }
            .property(true, "app_language", "ur")
    }
}

