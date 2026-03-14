package com.financer.feature.home.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.financer.core.data.repository.CategoryRepository
import com.financer.feature.home.api.HomeComponent
import com.financer.feature.home.domain.DeleteTransactionUseCase
import com.financer.feature.home.domain.GetBalanceUseCase
import com.financer.feature.home.domain.GetTotalSumByTypeInPeriodUseCase
import com.financer.feature.home.domain.GetTransactionsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
private data class SavedScrollState(
    val firstVisibleItemIndex: Int = 0,
    val firstVisibleItemScrollOffset: Int = 0,
    val toolbarHeightOffsetPx: Float = 0f,
)

internal class DefaultHomeComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    getBalanceUseCase: GetBalanceUseCase,
    getTransactionsUseCase: GetTransactionsUseCase,
    getTotalSumByTypeInPeriodUseCase: GetTotalSumByTypeInPeriodUseCase,
    deleteTransactionUseCase: DeleteTransactionUseCase,
    categoryRepository: CategoryRepository,
    uiStateMapper: HomeUiStateMapper,
    private val onOpenTransaction: (Long?) -> Unit,
    private val onOpenFilter: () -> Unit,
    private val onUpEvent: () -> Flow<Unit>,
) : HomeComponent, ComponentContext by componentContext {
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    val store = HomeStoreFactory(
        storeFactory = storeFactory,
        getBalanceUseCase = getBalanceUseCase,
        getTransactionsUseCase = getTransactionsUseCase,
        getTotalSumByTypeInPeriodUseCase = getTotalSumByTypeInPeriodUseCase,
        deleteTransactionUseCase = deleteTransactionUseCase,
        categoryRepository = categoryRepository,
        onOpenTransaction = onOpenTransaction,
        onOpenFilter = onOpenFilter,
        onObserveUpEventProvider = onUpEvent,
    ).create()

    val headerComponent: HomeHeaderComponent
    val listComponent: HomeListComponent
    val addTransactionButtonComponent: HomeAddTransactionButtonComponent

    init {
        val restored = stateKeeper.consume(
            "scroll_state", SavedScrollState.serializer()
        )

        headerComponent = DefaultHomeHeaderComponent(
            componentContext = componentContext.childContext("homeHeader"),
            store = store,
            uiStateMapper = uiStateMapper,
            scope = scope,
            initialToolbarHeightOffsetPx = restored?.toolbarHeightOffsetPx ?: 0f,
        )

        addTransactionButtonComponent = HomeAddTransactionButtonComponentDefault(
            componentContext.childContext("addTransactionButton"),
            store
        )

        listComponent = DefaultHomeListComponent(
            componentContext = componentContext.childContext("homeList"),
            store = store,
            uiStateMapper = uiStateMapper,
            scope = scope,
            initialFirstVisibleItemIndex = restored?.firstVisibleItemIndex ?: 0,
            initialFirstVisibleItemScrollOffset = restored?.firstVisibleItemScrollOffset ?: 0,
        )

        stateKeeper.register("scroll_state", SavedScrollState.serializer()) {
            SavedScrollState(
                firstVisibleItemIndex = listComponent.savedFirstVisibleItemIndex,
                firstVisibleItemScrollOffset = listComponent.savedFirstVisibleItemScrollOffset,
                toolbarHeightOffsetPx = headerComponent.savedToolbarHeightOffsetPx,
            )
        }
    }

    override fun onDestroy() {
        scope.cancel()
        store.dispose()
    }
}
