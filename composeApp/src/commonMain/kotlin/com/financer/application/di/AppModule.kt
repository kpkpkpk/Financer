package com.financer.application.di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.financer.core.data.di.coreDataModule
import com.financer.feature.home.di.homeModule
import com.financer.feature.main.di.mainModule
import com.financer.feature.onboarding.di.onboardingModule
import org.koin.dsl.module

val appModule = module {
    includes(coreDataModule, onboardingModule, homeModule, mainModule)
    single<StoreFactory> { DefaultStoreFactory() }
}
