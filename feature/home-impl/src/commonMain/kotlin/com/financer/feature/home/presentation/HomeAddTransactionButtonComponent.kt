package com.financer.feature.home.presentation

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


internal interface HomeAddTransactionButtonComponent: ComponentContext {

    fun onAddTransactionClicked()
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class HomeAddTransactionButtonComponentDefault(
    componentContext: ComponentContext,
    private val store: HomeStore,
) : HomeAddTransactionButtonComponent, ComponentContext by componentContext {
    override fun onAddTransactionClicked() {
        store.accept(HomeStore.Intent.AddTransactionClicked)
    }

}
