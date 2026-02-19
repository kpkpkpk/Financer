package com.financer.feature.onboarding.domain

import com.financer.core.data.PreferencesRepository

class CompleteOnboardingUseCase(
    private val preferencesRepository: PreferencesRepository,
) {
    operator fun invoke() {
        preferencesRepository.setOnboardingCompleted(true)
    }
}
