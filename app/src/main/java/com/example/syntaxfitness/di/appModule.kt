package com.example.syntaxfitness.di


import com.example.syntaxfitness.data.local.database.FitnessDatabase
import com.example.syntaxfitness.data.local.repository.RunRepository
import com.example.syntaxfitness.data.local.repository.RunRepositoryImpl
import com.example.syntaxfitness.ui.running.viewmodel.RunningViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    // Database
    single { FitnessDatabase.getDatabase(androidContext()) }

    // DAO
    single { get<FitnessDatabase>().runDao() }

    // Repository
    single<RunRepository> { RunRepositoryImpl(get()) }

    // ViewModel
    single { RunningViewModel(get()) }
}