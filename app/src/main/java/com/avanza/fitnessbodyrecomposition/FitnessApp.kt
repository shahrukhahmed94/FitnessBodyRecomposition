package com.avanza.fitnessbodyrecomposition

import android.app.Application
import com.avanza.fitnessbodyrecomposition.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class FitnessApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@FitnessApp)
            modules(appModule)
        }
    }
}
