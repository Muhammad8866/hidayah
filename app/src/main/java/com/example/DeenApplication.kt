package com.example

import android.app.Application
import com.example.core.analytics.AnalyticsProfile
import com.example.core.analytics.AppAnalytics

class DeenApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppAnalytics.init(this)
        AnalyticsProfile.sync(this)
    }
}
