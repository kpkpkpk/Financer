package com.financer.feature.home.presentation

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.financer.core.data.model.Category
import com.financer.core.data.model.Transaction
import com.financer.core.data.repository.CategoryRepository
import com.financer.feature.home.domain.DeleteTransactionUseCase
import com.financer.feature.home.domain.GetBalanceUseCase
import com.financer.feature.home.domain.GetTotalSumByTypeInPeriodUseCase
import com.financer.feature.home.domain.GetTransactionsUseCase
import com.financer.feature.home.presentation.HomeStore.Intent
import com.financer.feature.home.presentation.HomeStore.Label
import com.financer.feature.home.presentation.HomeStore.Period
import com.financer.feature.home.presentation.HomeStore.State
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

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

    fun create(): HomeStore = object : HomeStore, Store<Intent, State, Label> by storeFactory.create(
        name = "HomeStore",
        initialState = State(),
        bootstrapper = SimpleBootstrapper(Action.Init),
        executorFactory = { Executor() },
        reducer = ReducerImpl
    ) {}

    private sealed interface Action {
        data object Init : Action
    }

    private sealed interface Msg {
        data class DataLoaded(
            val balance: Long,
            val income: Long,
            val expense: Long,
            val transactions: List<Transaction>,
            val categories: Map<Long, Category>,
        ) : Msg

    }

    private inner class Executor : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        private var observeJob: Job? = null
        private var observeUpEventJob: Job? = null

        override fun executeAction(action: Action) {
            when (action) {
                Action.Init -> {
                    observeData(state().period)
                    observeHomeUpEvent()
                }
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.TransactionClicked -> onOpenTransaction(intent.transactionId)
                is Intent.DeleteRequested -> Unit
                is Intent.DeleteConfirmed -> {
                    scope.launch { deleteTransactionUseCase(intent.transactionId) }
                }

                Intent.FilterClicked -> onOpenFilter()
                Intent.AddTransactionClicked -> onOpenTransaction(null)
            }
        }

        private fun observeHomeUpEvent() {
            observeUpEventJob?.cancel()
            observeUpEventJob = onObserveUpEventProvider().onEach {
                publish(Label.ScrollFeedToUp)
            }.launchIn(scope)
        }

        private fun observeData(period: Period) {
            observeJob?.cancel()
            observeJob = scope.launch {
                val categoriesById = categoryRepository.getAll().associateBy(Category::id)
                combine(
                    getBalanceUseCase(),
                    getTotalSumByTypeInPeriodUseCase(period.start, period.end),
                    getTransactionsUseCase(period.start, period.end),
                ) { balance, summary, transactions ->
                    Msg.DataLoaded(
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

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.DataLoaded -> copy(
                    balance = msg.balance,
                    income = msg.income,
                    expense = msg.expense,
                    transactions = msg.transactions,
                    categories = msg.categories,
                )
            }
    }
}
