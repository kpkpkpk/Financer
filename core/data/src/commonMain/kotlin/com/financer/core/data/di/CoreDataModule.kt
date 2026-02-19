package com.financer.core.data.di

import com.financer.core.data.PreferencesRepository
import com.financer.core.data.PreferencesRepositoryImpl
import com.russhwolf.settings.Settings
import org.koin.dsl.module

val coreDataModule = module {
    single { Settings() }
    single<PreferencesRepository> { PreferencesRepositoryImpl(get()) }
}
