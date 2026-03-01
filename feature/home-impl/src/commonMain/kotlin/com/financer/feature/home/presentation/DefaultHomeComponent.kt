package com.financer.feature.home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.financer.core.data.repository.CategoryRepository
import com.financer.feature.home.api.HomeComponent
import com.financer.feature.home.api.HomeScreenProvider
import com.financer.feature.home.domain.DeleteTransactionUseCase
import com.financer.feature.home.domain.GetBalanceUseCase
import com.financer.feature.home.domain.GetTotalSumByTypeInPeriodUseCase
import com.financer.feature.home.domain.GetTransactionsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class DefaultHomeComponent(
    storeFactory: StoreFactory,
    getBalanceUseCase: GetBalanceUseCase,
    getTransactionsUseCase: GetTransactionsUseCase,
    getTotalSumByTypeInPeriodUseCase: GetTotalSumByTypeInPeriodUseCase,
    deleteTransactionUseCase: DeleteTransactionUseCase,
    categoryRepository: CategoryRepository,
    private val onOpenTransaction: (Long) -> Unit,
    private val onOpenFilter: () -> Unit,
) : HomeComponent {
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val store = HomeStoreFactory(
        storeFactory = storeFactory,
        getBalanceUseCase = getBalanceUseCase,
        getTransactionsUseCase = getTransactionsUseCase,
        getTotalSumByTypeInPeriodUseCase = getTotalSumByTypeInPeriodUseCase,
        deleteTransactionUseCase = deleteTransactionUseCase,
        categoryRepository = categoryRepository
    ).create()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<HomeStore.State> = store.stateFlow

    init {
        store.labels
            .onEach { label ->
                when (label) {
                    is HomeStore.Label.OpenTransaction -> onOpenTransaction(label.transactionId)
                    HomeStore.Label.OpenFilter -> onOpenFilter()
                }
            }
            .launchIn(scope)
    }

    override fun onLoadData() {
        store.accept(HomeStore.Intent.LoadData)
    }

    override fun onTransactionClicked(transactionId: Long) {
        store.accept(HomeStore.Intent.TransactionClicked(transactionId))
    }

    override fun onDeleteRequested(transactionId: Long) {
        store.accept(HomeStore.Intent.DeleteRequested(transactionId))
    }

    override fun onDeleteConfirmed(transactionId: Long) {
        store.accept(HomeStore.Intent.DeleteConfirmed(transactionId))
    }

    override fun onFilterClicked() {
        store.accept(HomeStore.Intent.FilterClicked)
    }

    override fun onDestroy() {
        scope.cancel()
        store.dispose()
    }
}

class DefaultHomeScreenProvider : HomeScreenProvider {
    @Composable
    override fun Screen(component: HomeComponent, modifier: Modifier) {
        val defaultComponent = component as? DefaultHomeComponent ?: return
        HomeScreen(component = defaultComponent, modifier = modifier)
    }
}

