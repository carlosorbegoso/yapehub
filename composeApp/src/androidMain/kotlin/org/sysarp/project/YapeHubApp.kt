package org.sysarp.project

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.sysarp.project.di.appModule

class YapeHubApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Log Koin activity
            androidLogger()
            // Pass Android context
            androidContext(this@YapeHubApp)
            // Load our modules
            modules(appModule)
        }
    }
}
