package com.financer.feature.home.di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.financer.feature.home.api.HomeComponentFactory
import com.financer.feature.home.api.HomeScreenProvider
import com.financer.feature.home.domain.DeleteTransactionUseCase
import com.financer.feature.home.domain.GetBalanceUseCase
import com.financer.feature.home.domain.GetTotalSumByTypeInPeriodUseCase
import com.financer.feature.home.domain.GetTransactionsUseCase
import com.financer.feature.home.presentation.DefaultHomeComponent
import com.financer.feature.home.presentation.DefaultHomeScreenProvider
import org.koin.dsl.module

val homeModule = module {
    factory { GetBalanceUseCase(get()) }
    factory { GetTransactionsUseCase(get()) }
    factory { GetTotalSumByTypeInPeriodUseCase(get()) }
    factory { DeleteTransactionUseCase(get()) }

    factory<HomeComponentFactory> {
        val storeFactory: StoreFactory = get()
        val getBalanceUseCase: GetBalanceUseCase = get()
        val getTransactionsUseCase: GetTransactionsUseCase = get()
        val getTotalSumByTypeInPeriodUseCase: GetTotalSumByTypeInPeriodUseCase = get()
        val deleteTransactionUseCase: DeleteTransactionUseCase = get()
        val categoryRepository: com.financer.core.data.repository.CategoryRepository = get()

        HomeComponentFactory { onOpenTransaction, onOpenFilter ->
            DefaultHomeComponent(
                storeFactory = storeFactory,
                getBalanceUseCase = getBalanceUseCase,
                getTransactionsUseCase = getTransactionsUseCase,
                getTotalSumByTypeInPeriodUseCase = getTotalSumByTypeInPeriodUseCase,
                deleteTransactionUseCase = deleteTransactionUseCase,
                categoryRepository = categoryRepository,
                onOpenTransaction = onOpenTransaction,
                onOpenFilter = onOpenFilter
            )
        }
    }

    single<HomeScreenProvider> { DefaultHomeScreenProvider() }
}
