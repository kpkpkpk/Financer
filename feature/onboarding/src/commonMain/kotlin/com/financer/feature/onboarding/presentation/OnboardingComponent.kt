package com.financer.feature.onboarding.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.financer.feature.onboarding.domain.CompleteOnboardingUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.arkivanov.mvikotlin.extensions.coroutines.labels

class OnboardingComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    completeOnboarding: CompleteOnboardingUseCase,
    private val onFinished: () -> Unit,
) : ComponentContext by componentContext {

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val store = instanceKeeper.getStore {
        OnboardingStoreFactory(storeFactory, completeOnboarding).create()
    }

    init {
        store.labels
            .onEach { label ->
                when (label) {
                    is OnboardingStore.Label.OnboardingCompleted -> onFinished()
                }
            }
            .launchIn(scope)

        lifecycle.doOnDestroy { scope.cancel() }
    }

    fun onStartClicked() {
        store.accept(OnboardingStore.Intent.StartClicked)
    }
}
