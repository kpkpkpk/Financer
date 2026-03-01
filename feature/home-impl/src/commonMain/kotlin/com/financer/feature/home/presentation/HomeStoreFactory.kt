package com.financer.feature.home.presentation

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.financer.core.data.model.Category
import com.financer.core.data.model.Transaction
import com.financer.core.data.model.TransactionType
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeStoreFactory(
    private val storeFactory: StoreFactory,
    private val getBalanceUseCase: GetBalanceUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getTotalSumByTypeInPeriodUseCase: GetTotalSumByTypeInPeriodUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val categoryRepository: CategoryRepository,
) {

    fun create(): HomeStore = object : HomeStore, Store<Intent, State, Label>
        by storeFactory.create(
            name = "HomeStore",
            initialState = State(),
            executorFactory = { Executor() },
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data class DataLoaded(
            val balance: Long,
            val income: Long,
            val expense: Long,
            val items: List<HomeStore.ListItem>,
        ) : Msg
    }

    private inner class Executor : CoroutineExecutor<Intent, Nothing, State, Msg, Label>() {
        private var observeJob: Job? = null

        override fun executeIntent(intent: Intent) {
            when (intent) {
                Intent.LoadData -> observeData(state().period)
                is Intent.TransactionClicked -> publish(Label.OpenTransaction(intent.transactionId))
                is Intent.DeleteRequested -> Unit
                is Intent.DeleteConfirmed -> {
                    scope.launch { deleteTransactionUseCase(intent.transactionId) }
                }

                Intent.FilterClicked -> publish(Label.OpenFilter)
            }
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
                        items = transactions.toUiItems(categoriesById)
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
                    items = msg.items,
                )
            }
    }
}

private fun List<Transaction>.toUiItems(
    categoriesById: Map<Long, Category>
): List<HomeStore.ListItem> {
    if (isEmpty()) {
        return listOf(HomeStore.ListItem.EmptyState)
    }

    return sortedByDescending { it.date }
        .groupBy { it.date.date }
        .entries
        .sortedByDescending { it.key }
        .flatMap { (date, transactions) ->
            buildList {
                add(HomeStore.ListItem.DateHeader(date = date, title = date.toHeaderLabel()))

                transactions.forEach { transaction ->
                    val category = categoriesById[transaction.categoryId]
                    add(
                        HomeStore.ListItem.Transaction(
                            item = HomeStore.TransactionItem(
                                id = transaction.id,
                                categoryEmoji = category?.emoji ?: "*",
                                categoryName = category?.name,
                                time = transaction.date.toTimeLabel(),
                                amount = transaction.amount,
                                isIncome = transaction.type == TransactionType.INCOME
                            )
                        )
                    )
                }
            }
        }
}
