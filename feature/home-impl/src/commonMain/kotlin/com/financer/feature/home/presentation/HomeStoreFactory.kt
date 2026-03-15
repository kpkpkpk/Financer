package com.financer.feature.home.presentation

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.financer.core.data.repository.CategoryRepository
import com.financer.feature.home.domain.DeleteTransactionUseCase
import com.financer.feature.home.domain.GetBalanceUseCase
import com.financer.feature.home.domain.GetTotalSumByTypeInPeriodUseCase
import com.financer.feature.home.domain.GetTransactionsUseCase
import kotlinx.coroutines.flow.Flow

internal class HomeStoreFactory(
    private val storeFactory: StoreFactory,
    private val getBalanceUseCase: GetBalanceUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getTotalSumByTypeInPeriodUseCase: GetTotalSumByTypeInPeriodUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val categoryRepository: CategoryRepository,
    private val onOpenTransaction: (Long?) -> Unit,
    private val onOpenFilter: () -> Unit,
    private val onObserveUpEventProvider: () -> Flow<Unit>,
) {

    fun create(): HomeStore = object : HomeStore, Store<HomeStore.Intent, HomeStore.State, HomeStore.Label>
        by storeFactory.create(
            name = "HomeStore",
            initialState = HomeStore.State(),
            bootstrapper = SimpleBootstrapper(HomeStoreAction.Init),
            executorFactory = {
                HomeStoreExecutor(
                    getBalanceUseCase = getBalanceUseCase,
                    getTransactionsUseCase = getTransactionsUseCase,
                    getTotalSumByTypeInPeriodUseCase = getTotalSumByTypeInPeriodUseCase,
                    deleteTransactionUseCase = deleteTransactionUseCase,
                    categoryRepository = categoryRepository,
                    onOpenTransaction = onOpenTransaction,
                    onOpenFilter = onOpenFilter,
                    onObserveUpEventProvider = onObserveUpEventProvider,
                )
            },
            reducer = HomeStoreReducer()
        ) {}
}
