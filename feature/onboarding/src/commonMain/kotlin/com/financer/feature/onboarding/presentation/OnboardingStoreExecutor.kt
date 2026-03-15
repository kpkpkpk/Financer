package com.financer.feature.onboarding.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.financer.feature.onboarding.domain.CompleteOnboardingUseCase
import com.financer.feature.onboarding.presentation.OnboardingStore.Intent
import com.financer.feature.onboarding.presentation.OnboardingStore.Label
import com.financer.feature.onboarding.presentation.OnboardingStore.State

internal class OnboardingStoreExecutor(
    private val completeOnboarding: CompleteOnboardingUseCase,
) : CoroutineExecutor<Intent, Nothing, State, OnboardingStoreMessage, Label>() {

    override fun executeIntent(intent: Intent) {
        when (intent) {
            OnboardingStore.Intent.StartClicked -> {
                dispatch(OnboardingStoreMessage.Completing)
                completeOnboarding()
                publish(Label.OnboardingCompleted)
            }
        }
    }
}
