package com.financer.feature.home.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.financer.core.data.model.Category
import com.financer.core.data.repository.CategoryRepository
import com.financer.feature.home.domain.DeleteTransactionUseCase
import com.financer.feature.home.domain.GetBalanceUseCase
import com.financer.feature.home.domain.GetTotalSumByTypeInPeriodUseCase
import com.financer.feature.home.domain.GetTransactionsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class HomeStoreExecutor(
    private val getBalanceUseCase: GetBalanceUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getTotalSumByTypeInPeriodUseCase: GetTotalSumByTypeInPeriodUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val categoryRepository: CategoryRepository,
    private val onOpenTransaction: (Long?) -> Unit,
    private val onOpenFilter: () -> Unit,
    private val onObserveUpEventProvider: () -> Flow<Unit>,
) : CoroutineExecutor<HomeStore.Intent, HomeStoreAction, HomeStore.State, HomeStoreMessage, HomeStore.Label>() {

    private var observeJob: Job? = null
    private var observeUpEventJob: Job? = null

    override fun executeAction(action: HomeStoreAction) {
        when (action) {
            HomeStoreAction.Init -> {
                observeData(state().period)
                observeHomeUpEvent()
            }
        }
    }

    override fun executeIntent(intent: HomeStore.Intent) {
        when (intent) {
            is HomeStore.Intent.TransactionClicked -> onOpenTransaction(intent.transactionId)
            is HomeStore.Intent.DeleteRequested -> Unit
            is HomeStore.Intent.DeleteConfirmed -> {
                scope.launch { deleteTransactionUseCase(intent.transactionId) }
            }

            HomeStore.Intent.FilterClicked -> onOpenFilter()
            HomeStore.Intent.AddTransactionClicked -> onOpenTransaction(null)
        }
    }

    private fun observeHomeUpEvent() {
        observeUpEventJob?.cancel()
        observeUpEventJob = onObserveUpEventProvider().onEach {
            publish(HomeStore.Label.ScrollFeedToUp)
        }.launchIn(scope)
    }

    private fun observeData(period: HomeStore.Period?) {
        observeJob?.cancel()
        observeJob = scope.launch {
            val categoriesById = categoryRepository.getAll().associateBy(Category::id)
            combine(
                getBalanceUseCase(),
                getTotalSumByTypeInPeriodUseCase(period?.start, period?.end),
                getTransactionsUseCase(period?.start, period?.end),
            ) { balance, summary, transactions ->
                HomeStoreMessage.DataLoaded(
                    balance = balance,
                    income = summary.incomeSum,
                    expense = summary.expenseSum,
                    transactions = transactions,
                    categories = categoriesById,
                )
            }.collect { msg ->
                dispatch(msg)
            }
        }
    }
}
