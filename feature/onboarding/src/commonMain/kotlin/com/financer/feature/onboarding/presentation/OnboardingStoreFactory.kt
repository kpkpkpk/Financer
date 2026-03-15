package com.financer.feature.onboarding.presentation

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.financer.feature.onboarding.domain.CompleteOnboardingUseCase
import com.financer.feature.onboarding.presentation.OnboardingStore.Intent
import com.financer.feature.onboarding.presentation.OnboardingStore.Label
import com.financer.feature.onboarding.presentation.OnboardingStore.State

class OnboardingStoreFactory(
    private val storeFactory: StoreFactory,
    private val completeOnboarding: CompleteOnboardingUseCase,
) {

    fun create(): OnboardingStore = object : OnboardingStore, Store<Intent, State, Label>
        by storeFactory.create(
            name = "OnboardingStore",
            initialState = State(),
            executorFactory = { OnboardingStoreExecutor(completeOnboarding) },
            reducer = OnboardingStoreReducer(),
        ) {}
}
