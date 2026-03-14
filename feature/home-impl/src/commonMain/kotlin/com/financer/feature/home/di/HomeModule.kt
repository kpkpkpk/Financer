package com.financer.feature.home.di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.financer.core.data.repository.CategoryRepository
import com.financer.feature.home.api.HomeComponentFactory
import com.financer.feature.home.api.HomeScreenProvider
import com.financer.feature.home.domain.DeleteTransactionUseCase
import com.financer.feature.home.domain.GetBalanceUseCase
import com.financer.feature.home.domain.GetTotalSumByTypeInPeriodUseCase
import com.financer.feature.home.domain.GetTransactionsUseCase
import com.financer.feature.home.presentation.DefaultHomeComponent
import com.financer.feature.home.presentation.DefaultHomeScreenProvider
import com.financer.feature.home.presentation.HomeUiStateMapper
import org.koin.dsl.module

val homeModule = module {
    factory { GetBalanceUseCase(get()) }
    factory { GetTransactionsUseCase(get()) }
    factory { GetTotalSumByTypeInPeriodUseCase(get()) }
    factory { DeleteTransactionUseCase(get()) }
    factory { HomeUiStateMapper() }

    factory<HomeComponentFactory> {
        val storeFactory: StoreFactory = get()
        val getBalanceUseCase: GetBalanceUseCase = get()
        val getTransactionsUseCase: GetTransactionsUseCase = get()
        val getTotalSumByTypeInPeriodUseCase: GetTotalSumByTypeInPeriodUseCase = get()
        val deleteTransactionUseCase: DeleteTransactionUseCase = get()
        val categoryRepository: CategoryRepository = get()
        val uiStateMapper: HomeUiStateMapper = get()

        HomeComponentFactory { componentContext, onOpenTransaction, onOpenFilter, onUpEvent ->
            DefaultHomeComponent(
                componentContext = componentContext,
                storeFactory = storeFactory,
                getBalanceUseCase = getBalanceUseCase,
                getTransactionsUseCase = getTransactionsUseCase,
                getTotalSumByTypeInPeriodUseCase = getTotalSumByTypeInPeriodUseCase,
                deleteTransactionUseCase = deleteTransactionUseCase,
                categoryRepository = categoryRepository,
                uiStateMapper = uiStateMapper,
                onOpenTransaction = onOpenTransaction,
                onOpenFilter = onOpenFilter,
                onUpEvent = onUpEvent
            )
        }
    }

    single<HomeScreenProvider> { DefaultHomeScreenProvider() }
}
