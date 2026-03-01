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
internal data class HomeListUiState(
    val items: List<HomeListItem> = listOf(HomeListItem.EmptyState),
)

internal interface HomeListComponent: ComponentContext {
    val uiState: StateFlow<HomeListUiState>
    fun onTransactionClicked(transactionId: Long)
    fun onDeleteRequested(transactionId: Long)
    fun onDeleteConfirmed(transactionId: Long)
    var savedFirstVisibleItemIndex: Int
    var savedFirstVisibleItemScrollOffset: Int
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultHomeListComponent(
    private val store: HomeStore,
    componentContext: ComponentContext,
    uiStateMapper: HomeUiStateMapper,
    scope: CoroutineScope,
    initialFirstVisibleItemIndex: Int,
    initialFirstVisibleItemScrollOffset: Int,
) : HomeListComponent, ComponentContext by componentContext {

    override val uiState: StateFlow<HomeListUiState> = store.stateFlow
        .map { uiStateMapper.mapList(it) }
        .stateIn(scope, SharingStarted.Eagerly, HomeListUiState())

    override fun onTransactionClicked(transactionId: Long) {
        store.accept(HomeStore.Intent.TransactionClicked(transactionId))
    }

    override fun onDeleteRequested(transactionId: Long) {
        store.accept(HomeStore.Intent.DeleteRequested(transactionId))
    }

    override fun onDeleteConfirmed(transactionId: Long) {
        store.accept(HomeStore.Intent.DeleteConfirmed(transactionId))
    }

    override var savedFirstVisibleItemIndex: Int = initialFirstVisibleItemIndex
    override var savedFirstVisibleItemScrollOffset: Int = initialFirstVisibleItemScrollOffset
}
