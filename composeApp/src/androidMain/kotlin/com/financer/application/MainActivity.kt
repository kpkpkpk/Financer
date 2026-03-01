package com.financer.application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.financer.application.navigation.RootComponent
import com.financer.feature.main.api.MainComponentFactory
import com.financer.feature.onboarding.domain.CompleteOnboardingUseCase
import com.financer.feature.onboarding.domain.IsOnboardingCompletedUseCase
import org.koin.core.context.GlobalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val koin = GlobalContext.get()
        val rootComponent = RootComponent(
            componentContext = defaultComponentContext(),
            storeFactory = koin.get<StoreFactory>(),
            mainComponentFactory = koin.get<MainComponentFactory>(),
            isOnboardingCompleted = koin.get<IsOnboardingCompletedUseCase>(),
            completeOnboarding = koin.get<CompleteOnboardingUseCase>(),
        )

        setContent {
            App(rootComponent)
        }
    }
}
