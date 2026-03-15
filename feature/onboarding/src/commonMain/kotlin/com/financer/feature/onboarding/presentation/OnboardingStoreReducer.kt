package com.financer.feature.onboarding.presentation

import com.arkivanov.mvikotlin.core.store.Reducer

internal class OnboardingStoreReducer : Reducer<OnboardingStore.State, OnboardingStoreMessage> {
    override fun OnboardingStore.State.reduce(msg: OnboardingStoreMessage): OnboardingStore.State =
        when (msg) {
            OnboardingStoreMessage.Completing -> copy(isCompleting = true)
        }
}
