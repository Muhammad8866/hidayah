package com.example.core.analytics

/** Kept independent of Firebase so the consent/privacy boundary can be tested. */
internal class AnalyticsDispatcher(
    private val propertySink: (String, String) -> Unit = { _, _ -> },
    private val sink: (String, Map<String, Any>) -> Unit
) {
    fun dispatch(consent: Boolean?, event: String, parameters: Map<String, Any>) {
        if (consent != true || !AnalyticsEventPolicy.accepts(event, parameters)) return
        runCatching { sink(event, parameters) }
    }

    fun property(consent: Boolean?, name: String, value: String) {
        if (consent != true || !AnalyticsEventPolicy.acceptsUserProperty(name, value)) return
        runCatching { propertySink(name, value) }
    }
}
