package com.example.syntaxfitness

import android.app.Application
import com.example.syntaxfitness.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class SyntaxFitnessApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@SyntaxFitnessApplication)
            modules(appModule)
        }
    }
}