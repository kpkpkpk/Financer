package com.financer.feature.onboarding.presentation

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
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
            executorFactory = { Executor(completeOnboarding) },
            reducer = ReducerImpl,
        ) {}

    private sealed interface Msg {
        data object Completing : Msg
    }

    private class Executor(
        private val completeOnboarding: CompleteOnboardingUseCase,
    ) : CoroutineExecutor<Intent, Nothing, State, Msg, Label>() {

        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.StartClicked -> {
                    dispatch(Msg.Completing)
                    completeOnboarding()
                    publish(Label.OnboardingCompleted)
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.Completing -> copy(isCompleting = true)
            }
    }
}
