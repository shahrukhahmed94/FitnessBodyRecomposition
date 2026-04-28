package com.tsapps.fitnessbodyrecomposition

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.tsapps.fitnessbodyrecomposition.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class FitnessApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Google Mobile Ads SDK
        MobileAds.initialize(this) {}

        startKoin {
            androidLogger()
            androidContext(this@FitnessApp)
            modules(appModule)
        }
    }
}
