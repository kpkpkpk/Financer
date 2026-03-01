package com.financer.feature.home.presentation

import com.arkivanov.mvikotlin.core.store.Store
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

interface HomeStore : Store<HomeStore.Intent, HomeStore.State, HomeStore.Label> {

    sealed interface Intent {
        data object LoadData : Intent
        data class TransactionClicked(val transactionId: Long) : Intent
        data class DeleteRequested(val transactionId: Long) : Intent
        data class DeleteConfirmed(val transactionId: Long) : Intent
        data object FilterClicked : Intent
    }

    data class State(
        val balance: Long = 0L,
        val income: Long = 0L,
        val expense: Long = 0L,
        val items: List<ListItem> = listOf(ListItem.EmptyState),
        val period: Period = Period.currentMonth(),
    )

    data class Period(
        val start: LocalDateTime,
        val end: LocalDateTime,
        val preset: PeriodPreset = PeriodPreset.Custom,
        val customTitle: String = "",
    ) {
        companion object
    }

    enum class PeriodPreset {
        ThisMonth,
        Custom,
    }

    sealed interface ListItem {
        data class DateHeader(
            val date: LocalDate,
            val title: String,
        ) : ListItem

        data class Transaction(
            val item: TransactionItem,
        ) : ListItem

        data object EmptyState : ListItem
    }

    data class TransactionItem(
        val id: Long,
        val categoryEmoji: String,
        val categoryName: String?,
        val time: String,
        val amount: Long,
        val isIncome: Boolean,
    )

    sealed interface Label {
        data class OpenTransaction(val transactionId: Long) : Label
        data object OpenFilter : Label
    }
}
