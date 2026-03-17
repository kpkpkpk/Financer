package com.financer.feature.transaction.presentation

import com.arkivanov.mvikotlin.core.store.Store
import com.financer.core.data.model.Category
import com.financer.core.data.model.TransactionType
import kotlin.time.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

internal interface TransactionStore :
    Store<TransactionStore.Intent, TransactionStore.State, TransactionStore.Label> {

    sealed interface Intent {
        data class AmountChanged(val value: String) : Intent
        data class TypeToggled(val type: TransactionType) : Intent
        data class CategorySelected(val categoryId: Long) : Intent
        data class DateInputChanged(val value: String) : Intent
        data class NoteChanged(val value: String) : Intent
        data object Confirm : Intent
        data object Close : Intent
    }

    data class State(
        val transactionId: Long? = null,
        val amountInput: String = "",
        val type: TransactionType = TransactionType.EXPENSE,
        val selectedCategory: Category? = null,
        val dateInput: String = currentDateDigits(),
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

internal fun currentDateDigits(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return dateTimeToDigits(now)
}

internal fun dateTimeToDigits(dt: LocalDateTime): String {
    val d = dt.day.toString().padStart(2, '0')
    val m = dt.month.number.toString().padStart(2, '0')
    val y = dt.year.toString().padStart(4, '0')
    return "$d$m$y"
}

internal fun digitsToDateTime(digits: String): LocalDateTime? {
    if (digits.length != 8) return null
    val day = digits.substring(0, 2).toIntOrNull() ?: return null
    val month = digits.substring(2, 4).toIntOrNull() ?: return null
    val year = digits.substring(4, 8).toIntOrNull() ?: return null
    if (day !in 1..31 || month !in 1..12 || year !in 2000..2100) return null
    return try {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        LocalDateTime(year, month, day, now.hour, now.minute)
    } catch (_: Exception) {
        null
    }
}
