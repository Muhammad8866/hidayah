package com.example.core.analytics

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** Opt-in smoke test: synthetic events only; run on a disposable emulator with DebugView enabled. */
@RunWith(AndroidJUnit4::class)
class AnalyticsSetupInstrumentedTest {
    @Test
    fun configuredAppSendsSyntheticEvents() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        // Never enable collection merely because the regular instrumentation suite is running.
        if (InstrumentationRegistry.getArguments().getString("analyticsSmoke") != "true") return
        val context = instrumentation.targetContext
        // screen_view requires an actual resumed Activity, not only an Application context.
        val activity = instrumentation.startActivitySync(Intent(context, com.example.MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        assertEquals("hidayah-6d0cc", FirebaseApp.getInstance().options.projectId)
        assertEquals("1:379533400732:android:5f9c3afb11db4e084deb59", FirebaseApp.getInstance().options.applicationId)
        val preferences = context.getSharedPreferences("analytics_consent", 0)
        val hadChoice = preferences.contains("enabled")
        val previousChoice = preferences.getBoolean("enabled", false)
        try {
            instrumentation.runOnMainSync {
                AppAnalytics.init(context)
                AppAnalytics.setEnabled(true)
                AppAnalytics.foreground()
                AppAnalytics.emit("screen_view", mapOf("screen_name" to "analytics_setup_test", "screen_class" to "analytics_setup_test"))
                AppAnalytics.interaction("analytics_setup_test", "setup_verification", "tap")
                AppAnalytics.request(200, 125L)
                AppAnalytics.media("setup_test", "play")
                AppAnalytics.background()
            }
            // Give the SDK time to upload its debug batch before restoring consent.
            Thread.sleep(60000)
        } finally {
            instrumentation.runOnMainSync { AppAnalytics.setEnabled(previousChoice) }
            if (!hadChoice) preferences.edit().remove("enabled").commit()
            instrumentation.runOnMainSync { activity.finish() }
        }
    }
}
