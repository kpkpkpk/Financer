package com.financer.feature.transaction.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.financer.core.data.repository.TransactionRepository
import com.financer.feature.transaction.api.TransactionComponent
import com.financer.feature.transaction.domain.CreateTransactionUseCase
import com.financer.feature.transaction.domain.GetAllCategoriesUseCase
import com.financer.feature.transaction.domain.GetTopCategoriesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class DefaultTransactionComponent(
    componentContext: ComponentContext,
    transactionId: Long?,
    storeFactory: StoreFactory,
    transactionRepository: TransactionRepository,
    createTransactionUseCase: CreateTransactionUseCase,
    getTopCategoriesUseCase: GetTopCategoriesUseCase,
    getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val onClose: () -> Unit,
) : TransactionComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    val store = instanceKeeper.getStore {
        TransactionStoreFactory(
            storeFactory = storeFactory,
            transactionId = transactionId,
            transactionRepository = transactionRepository,
            createTransactionUseCase = createTransactionUseCase,
            getTopCategoriesUseCase = getTopCategoriesUseCase,
            getAllCategoriesUseCase = getAllCategoriesUseCase,
        ).create()
    }

    init {
        store.labels
            .onEach { label ->
                when (label) {
                    TransactionStore.Label.Close -> onClose()
                    TransactionStore.Label.TransactionSaved -> onClose()
                }
            }
            .launchIn(scope)

        lifecycle.doOnDestroy { scope.cancel() }
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
