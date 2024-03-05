package com.example.aiish

import android.app.Application
import com.bugfender.android.BuildConfig
import com.bugfender.sdk.Bugfender

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Bugfender.init(this, "IKXPG5Al5cET27U1h3a3EmzFIbob9k0g", BuildConfig.DEBUG)
        Bugfender.enableCrashReporting()
        Bugfender.enableUIEventLogging(this)
    }
}