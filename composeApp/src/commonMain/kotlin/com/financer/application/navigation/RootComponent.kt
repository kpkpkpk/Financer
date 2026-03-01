package com.financer.application.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.financer.feature.main.api.MainComponent
import com.financer.feature.main.api.MainComponentFactory
import com.financer.feature.onboarding.domain.CompleteOnboardingUseCase
import com.financer.feature.onboarding.domain.IsOnboardingCompletedUseCase
import com.financer.feature.onboarding.presentation.OnboardingComponent
import kotlinx.serialization.Serializable

class RootComponent(
    componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val mainComponentFactory: MainComponentFactory,
    isOnboardingCompleted: IsOnboardingCompletedUseCase,
    private val completeOnboarding: CompleteOnboardingUseCase,
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    val childStack: Value<ChildStack<*, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = if (isOnboardingCompleted()) Config.Main else Config.Onboarding,
        handleBackButton = true,
        childFactory = ::createChild,
    )

    private fun createChild(config: Config, childContext: ComponentContext): Child =
        when (config) {
            is Config.Onboarding -> Child.Onboarding(
                OnboardingComponent(
                    componentContext = childContext,
                    storeFactory = storeFactory,
                    completeOnboarding = completeOnboarding,
                    onFinished = { navigation.replaceAll(Config.Main) },
                )
            )

            is Config.Main -> Child.Main(mainComponentFactory.create(childContext))
        }

    sealed class Child {
        data class Onboarding(val component: OnboardingComponent) : Child()
        data class Main(val mainComponent: MainComponent) : Child()
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Onboarding : Config

        @Serializable
        data object Main : Config
    }
}
