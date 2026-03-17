package com.financer.feature.transaction.presentation

import com.financer.core.data.model.Category
import com.financer.core.data.model.TransactionType

internal sealed interface TransactionStoreMessage {
    data class InitialDataLoaded(
        val transactionId: Long?,
        val amountInput: String,
        val type: TransactionType,
        val selectedCategory: Category?,
        val dateInput: String,
        val note: String,
        val topCategories: List<Category>,
        val allCategories: List<Category>,
    ) : TransactionStoreMessage

    data class AmountChanged(
        val value: String,
        val topCategories: List<Category>,
    ) : TransactionStoreMessage

    data class TypeChanged(
        val type: TransactionType,
        val selectedCategory: Category?,
        val topCategories: List<Category>,
        val allCategories: List<Category>,
    ) : TransactionStoreMessage

    data class CategoryChanged(val selectedCategory: Category?) : TransactionStoreMessage
    data class DateInputChanged(val dateInput: String) : TransactionStoreMessage
    data class NoteChanged(val note: String) : TransactionStoreMessage
    data class SavingChanged(val isSaving: Boolean) : TransactionStoreMessage
}
