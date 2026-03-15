package com.financer.feature.transaction.di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.financer.core.data.repository.TransactionRepository
import com.financer.feature.transaction.api.TransactionComponentFactory
import com.financer.feature.transaction.api.TransactionScreenProvider
import com.financer.feature.transaction.domain.CreateTransactionUseCase
import com.financer.feature.transaction.domain.GetAllCategoriesUseCase
import com.financer.feature.transaction.domain.GetTopCategoriesUseCase
import com.financer.feature.transaction.presentation.DefaultTransactionComponent
import com.financer.feature.transaction.presentation.DefaultTransactionScreenProvider
import org.koin.dsl.module

val transactionModule = module {
    factory { CreateTransactionUseCase(get()) }
    factory { GetTopCategoriesUseCase(get(), get()) }
    factory { GetAllCategoriesUseCase(get()) }

    factory<TransactionComponentFactory> {
        val storeFactory: StoreFactory = get()
        val transactionRepository: TransactionRepository = get()
        val createTransactionUseCase: CreateTransactionUseCase = get()
        val getTopCategoriesUseCase: GetTopCategoriesUseCase = get()
        val getAllCategoriesUseCase: GetAllCategoriesUseCase = get()

        TransactionComponentFactory { componentContext, transactionId, onClose ->
            DefaultTransactionComponent(
                componentContext = componentContext,
                transactionId = transactionId,
                storeFactory = storeFactory,
                transactionRepository = transactionRepository,
                createTransactionUseCase = createTransactionUseCase,
                getTopCategoriesUseCase = getTopCategoriesUseCase,
                getAllCategoriesUseCase = getAllCategoriesUseCase,
                onClose = onClose,
            )
        }
    }

    single<TransactionScreenProvider> { DefaultTransactionScreenProvider() }
}
