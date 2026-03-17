package com.financer.feature.transaction.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.financer.core.data.repository.TransactionRepository
import com.financer.feature.transaction.api.TransactionComponent
import com.financer.feature.transaction.domain.CreateTransactionUseCase
import com.financer.feature.transaction.domain.GetAllCategoriesUseCase
import com.financer.feature.transaction.domain.GetTopCategoriesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable

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
    private val uiStateMapper = TransactionUiStateMapper()
    private val slotNavigation = SlotNavigation<SlotConfig>()

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

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TransactionUiState> = store.stateFlow
        .map { uiStateMapper.map(it) }
        .stateIn(scope, SharingStarted.Eagerly, TransactionUiState())

    val slot: Value<ChildSlot<*, SlotChild>> = childSlot(
        source = slotNavigation,
        serializer = SlotConfig.serializer(),
        handleBackButton = true,
    ) { _, _ ->
        SlotChild.CategoryPicker
    }

    fun openCategoryPicker() {
        slotNavigation.activate(SlotConfig.CategoryPicker)
    }

    fun closeCategoryPicker() {
        slotNavigation.dismiss()
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

    sealed interface SlotChild {
        data object CategoryPicker : SlotChild
    }

    @Serializable
    private sealed interface SlotConfig {
        @Serializable
        data object CategoryPicker : SlotConfig
    }
}
