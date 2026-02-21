package com.financer.application

import android.app.Application
import com.financer.application.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class FinancerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@FinancerApp)
            modules(appModule)
        }
    }
}
