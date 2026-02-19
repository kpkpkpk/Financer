package com.financer.feature.onboarding.di

import com.financer.feature.onboarding.domain.CompleteOnboardingUseCase
import com.financer.feature.onboarding.domain.IsOnboardingCompletedUseCase
import org.koin.dsl.module

val onboardingModule = module {
    factory { CompleteOnboardingUseCase(get()) }
    factory { IsOnboardingCompletedUseCase(get()) }
}
