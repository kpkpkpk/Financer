package com.financer.feature.onboarding.presentation

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.financer.feature.onboarding.domain.CompleteOnboardingUseCase

class OnboardingStoreFactory(
    private val storeFactory: StoreFactory,
    private val completeOnboarding: CompleteOnboardingUseCase,
) {

    fun create(): OnboardingStore =
        object : OnboardingStore,
            Store<OnboardingStore.Intent, OnboardingStore.State, OnboardingStore.Label>
            by storeFactory.create(
                name = "OnboardingStore",
                initialState = OnboardingStore.State(),
                executorFactory = { Executor(completeOnboarding) },
                reducer = Reducer,
            ) {}

    private sealed interface Msg {
        data object Completing : Msg
    }

    private class Executor(
        private val completeOnboarding: CompleteOnboardingUseCase,
    ) : CoroutineExecutor<OnboardingStore.Intent, Nothing, OnboardingStore.State, Msg, OnboardingStore.Label>() {

        override fun executeIntent(intent: OnboardingStore.Intent) {
            when (intent) {
                is OnboardingStore.Intent.StartClicked -> {
                    dispatch(Msg.Completing)
                    completeOnboarding()
                    publish(OnboardingStore.Label.OnboardingCompleted)
                }
            }
        }
    }

    private object Reducer : com.arkivanov.mvikotlin.core.store.Reducer<OnboardingStore.State, Msg> {
        override fun OnboardingStore.State.reduce(msg: Msg): OnboardingStore.State =
            when (msg) {
                is Msg.Completing -> copy(isCompleting = true)
            }
    }
}
