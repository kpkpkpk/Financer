package com.financer.feature.transaction.presentation

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.financer.core.data.repository.TransactionRepository
import com.financer.feature.transaction.domain.CreateTransactionUseCase
import com.financer.feature.transaction.domain.GetAllCategoriesUseCase
import com.financer.feature.transaction.domain.GetTopCategoriesUseCase
import com.financer.feature.transaction.presentation.TransactionStore.Intent
import com.financer.feature.transaction.presentation.TransactionStore.Label
import com.financer.feature.transaction.presentation.TransactionStore.State

internal class TransactionStoreFactory(
    private val storeFactory: StoreFactory,
    private val transactionId: Long?,
    private val transactionRepository: TransactionRepository,
    private val createTransactionUseCase: CreateTransactionUseCase,
    private val getTopCategoriesUseCase: GetTopCategoriesUseCase,
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
) {

    fun create(): TransactionStore =
        object : TransactionStore, Store<Intent, State, Label> by storeFactory.create(
            name = "TransactionStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(TransactionStoreAction.Init),
            executorFactory = {
                TransactionStoreExecutor(
                    transactionId = transactionId,
                    transactionRepository = transactionRepository,
                    createTransactionUseCase = createTransactionUseCase,
                    getTopCategoriesUseCase = getTopCategoriesUseCase,
                    getAllCategoriesUseCase = getAllCategoriesUseCase,
                )
            },
            reducer = TransactionStoreReducer(),
        ) {}
}
