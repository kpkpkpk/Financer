package com.financer.feature.onboarding.presentation

internal sealed interface OnboardingStoreMessage {
    data object Completing : OnboardingStoreMessage
}
