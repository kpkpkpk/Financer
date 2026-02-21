package com.financer.core.data.di

import com.financer.core.data.DefaultCategories
import com.financer.core.data.PreferencesRepository
import com.financer.core.data.PreferencesRepositoryImpl
import com.financer.core.data.db.DatabaseDriverFactory
import com.financer.core.data.db.FinancerDatabase
import com.financer.core.data.repository.CategoryRepository
import com.financer.core.data.repository.CategoryRepositoryImpl
import com.financer.core.data.repository.TransactionRepository
import com.financer.core.data.repository.TransactionRepositoryImpl
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformDataModule: Module

val coreDataModule = module {
    includes(platformDataModule)

    single { Settings() }
    single<PreferencesRepository> { PreferencesRepositoryImpl(get()) }

    single {
        val driverFactory: DatabaseDriverFactory = get()
        FinancerDatabase(driverFactory.createDriver())
    }

    single<CategoryRepository> {
        CategoryRepositoryImpl(get()).also { repo ->
            DefaultCategories.prepopulateIfNeeded(repo)
        }
    }

    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
}
