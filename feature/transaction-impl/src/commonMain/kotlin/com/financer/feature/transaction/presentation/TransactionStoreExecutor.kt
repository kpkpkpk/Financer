package com.financer.feature.transaction.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.financer.core.data.model.TransactionType
import com.financer.core.data.repository.TransactionRepository
import com.financer.feature.transaction.domain.CreateTransactionUseCase
import com.financer.feature.transaction.domain.GetAllCategoriesUseCase
import com.financer.feature.transaction.domain.GetTopCategoriesUseCase
import com.financer.feature.transaction.domain.SaveTransactionResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class TransactionStoreExecutor(
    private val transactionId: Long?,
    private val transactionRepository: TransactionRepository,
    private val createTransactionUseCase: CreateTransactionUseCase,
    private val getTopCategoriesUseCase: GetTopCategoriesUseCase,
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
) : CoroutineExecutor<
    TransactionStore.Intent,
    TransactionStoreAction,
    TransactionStore.State,
    TransactionStoreMessage,
    TransactionStore.Label,
    >() {

    private var recommendationJob: Job? = null

    override fun executeAction(action: TransactionStoreAction) {
        when (action) {
            TransactionStoreAction.Init -> {
                scope.launch {
                    loadInitialData()
                }
            }
        }
    }

    override fun executeIntent(intent: TransactionStore.Intent) {
        when (intent) {
            is TransactionStore.Intent.AmountChanged -> handleAmountChanged(intent.value)
            is TransactionStore.Intent.TypeToggled -> handleTypeChanged(intent.type)
            is TransactionStore.Intent.CategorySelected -> {
                val selectedCategory = state().allCategories.firstOrNull { it.id == intent.categoryId }
                dispatch(TransactionStoreMessage.CategoryChanged(selectedCategory))
            }

            is TransactionStore.Intent.DateChanged -> {
                dispatch(TransactionStoreMessage.DateChanged(intent.value))
            }

            is TransactionStore.Intent.NoteChanged -> {
                dispatch(TransactionStoreMessage.NoteChanged(intent.value))
            }

            TransactionStore.Intent.Confirm -> handleConfirm()
            TransactionStore.Intent.Close -> publish(TransactionStore.Label.Close)
        }
    }

    private fun handleAmountChanged(rawValue: String) {
        recommendationJob?.cancel()
        recommendationJob = scope.launch {
            dispatch(
                TransactionStoreMessage.AmountChanged(
                    value = rawValue,
                    topCategories = resolveTopCategories(
                        type = state().type,
                        amountInput = rawValue,
                    ),
                )
            )
        }
    }

    private fun handleTypeChanged(type: TransactionType) {
        if (state().type == type) return

        recommendationJob?.cancel()
        recommendationJob = scope.launch {
            val allCategories = getAllCategoriesUseCase(type)
            val selectedCategory = state().selectedCategory
                ?.takeIf { current -> current.type == type }
                ?.let { current -> allCategories.firstOrNull { it.id == current.id } }

            dispatch(
                TransactionStoreMessage.TypeChanged(
                    type = type,
                    selectedCategory = selectedCategory,
                    topCategories = resolveTopCategories(type, state().amountInput),
                    allCategories = allCategories,
                )
            )
        }
    }

    private fun handleConfirm() {
        if (state().isSaving) return

        scope.launch {
            dispatch(TransactionStoreMessage.SavingChanged(true))
            when (
                createTransactionUseCase(
                    transactionId = state().transactionId,
                    type = state().type,
                    amount = amountInputToKopecks(state().amountInput),
                    categoryId = state().selectedCategory?.id,
                    date = state().date,
                    note = state().note,
                )
            ) {
                SaveTransactionResult.Success -> publish(TransactionStore.Label.TransactionSaved)
                SaveTransactionResult.InvalidAmount,
                SaveTransactionResult.CategoryNotSelected -> {
                    dispatch(TransactionStoreMessage.SavingChanged(false))
                }
            }
        }
    }

    private suspend fun loadInitialData() {
        val transaction = transactionId?.let { transactionRepository.getById(it) }
        val resolvedType = transaction?.type ?: TransactionType.EXPENSE
        val allCategories = getAllCategoriesUseCase(resolvedType)
        val selectedCategory = transaction?.categoryId?.let { categoryId ->
            allCategories.firstOrNull { it.id == categoryId }
        }
        val amountInput = transaction?.amount?.let(::formatAmountInput).orEmpty()

        dispatch(
            TransactionStoreMessage.InitialDataLoaded(
                transactionId = transaction?.id,
                amountInput = amountInput,
                type = resolvedType,
                selectedCategory = selectedCategory,
                date = transaction?.date ?: TransactionStore.State().date,
                note = transaction?.note.orEmpty(),
                topCategories = resolveTopCategories(resolvedType, amountInput),
                allCategories = allCategories,
            )
        )
    }

    private suspend fun resolveTopCategories(
        type: TransactionType,
        amountInput: String,
    ) = getTopCategoriesUseCase(
        type = type,
        amount = amountInputToKopecks(amountInput),
    )
}
