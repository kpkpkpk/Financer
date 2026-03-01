package com.financer.application

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.financer.application.di.appModule
import com.financer.application.navigation.RootComponent
import com.financer.feature.main.api.MainComponentFactory
import com.financer.feature.onboarding.domain.CompleteOnboardingUseCase
import com.financer.feature.onboarding.domain.IsOnboardingCompletedUseCase
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

fun initKoin() {
    startKoin {
        modules(appModule)
    }
}

fun MainViewController() = ComposeUIViewController {
    val koin = KoinPlatform.getKoin()
    val lifecycle = LifecycleRegistry()
    val rootComponent = RootComponent(
        componentContext = DefaultComponentContext(lifecycle = lifecycle),
        storeFactory = koin.get<StoreFactory>(),
        mainComponentFactory = koin.get<MainComponentFactory>(),
        isOnboardingCompleted = koin.get<IsOnboardingCompletedUseCase>(),
        completeOnboarding = koin.get<CompleteOnboardingUseCase>(),
    )
    App(rootComponent)
}
