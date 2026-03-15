package com.financer.feature.transaction.presentation

import com.arkivanov.mvikotlin.core.store.Store
import com.financer.core.data.model.Category
import com.financer.core.data.model.TransactionType
import kotlin.time.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal interface TransactionStore :
    Store<TransactionStore.Intent, TransactionStore.State, TransactionStore.Label> {

    sealed interface Intent {
        data class AmountChanged(val value: String) : Intent
        data class TypeToggled(val type: TransactionType) : Intent
        data class CategorySelected(val categoryId: Long) : Intent
        data class DateChanged(val value: LocalDateTime) : Intent
        data class NoteChanged(val value: String) : Intent
        data object Confirm : Intent
        data object Close : Intent
    }

    data class State(
        val transactionId: Long? = null,
        val amountInput: String = "",
        val type: TransactionType = TransactionType.EXPENSE,
        val selectedCategory: Category? = null,
        val date: LocalDateTime = currentDateTime(),
        val note: String = "",
        val topCategories: List<Category> = emptyList(),
        val allCategories: List<Category> = emptyList(),
        val isSaving: Boolean = false,
        val isLoading: Boolean = true,
    )

    sealed interface Label {
        data object TransactionSaved : Label
        data object Close : Label
    }
}

private fun currentDateTime(): LocalDateTime {
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
}
