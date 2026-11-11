package com.example

import android.app.Application
import com.example.core.analytics.AnalyticsProfile
import com.example.core.analytics.AppAnalytics

class DeenApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // The phase survives in the process state summary, so an ANR report shows whether onCreate finished.
        AppAnalytics.startupPhase(this, "app_create")
        AppAnalytics.init(this)
        AppAnalytics.startupPhase(this, "analytics_ready")
        AnalyticsProfile.sync(this)
        AppAnalytics.startupPhase(this, "profile_synced")
    }
}
