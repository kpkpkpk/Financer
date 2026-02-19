package com.financer.feature.onboarding.domain

import com.financer.core.data.PreferencesRepository

class IsOnboardingCompletedUseCase(
    private val preferencesRepository: PreferencesRepository,
) {
    operator fun invoke(): Boolean = preferencesRepository.isOnboardingCompleted()
}
