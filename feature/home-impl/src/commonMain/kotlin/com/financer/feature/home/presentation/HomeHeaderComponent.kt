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

@Immutable
internal data class HomeHeaderUiState(
    val formattedBalance: String = "",
    val formattedIncome: String = "",
    val formattedExpense: String = "",
    val periodPreset: HomeStore.PeriodPreset = HomeStore.PeriodPreset.Custom,
    val periodCustomTitle: String = "",
)

internal interface HomeHeaderComponent : ComponentContext {
    val uiState: StateFlow<HomeHeaderUiState>
    fun onFilterClicked()
    var savedToolbarHeightOffsetPx: Float
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultHomeHeaderComponent(
    componentContext: ComponentContext,
    private val store: HomeStore,
    uiStateMapper: HomeUiStateMapper,
    scope: CoroutineScope,
    initialToolbarHeightOffsetPx: Float,
) : HomeHeaderComponent, ComponentContext by componentContext {

    override val uiState: StateFlow<HomeHeaderUiState> = store.stateFlow
        .map { uiStateMapper.mapHeader(it) }
        .stateIn(scope, SharingStarted.Eagerly, HomeHeaderUiState())

    override fun onFilterClicked() {
        store.accept(HomeStore.Intent.FilterClicked)
    }

    override var savedToolbarHeightOffsetPx: Float = initialToolbarHeightOffsetPx
}
