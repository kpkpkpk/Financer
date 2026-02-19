package com.financer.feature.onboarding.presentation

import com.arkivanov.mvikotlin.core.store.Store

interface OnboardingStore : Store<OnboardingStore.Intent, OnboardingStore.State, OnboardingStore.Label> {

    sealed interface Intent {
        data object StartClicked : Intent
    }

    data class State(
        val isCompleting: Boolean = false,
    )

    sealed interface Label {
        data object OnboardingCompleted : Label
    }
}
