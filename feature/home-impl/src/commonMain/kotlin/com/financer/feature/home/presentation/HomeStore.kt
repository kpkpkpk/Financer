package com.financer.feature.home.presentation

import com.arkivanov.mvikotlin.core.store.Store
import com.financer.core.data.model.Category
import com.financer.core.data.model.Transaction
import kotlinx.datetime.LocalDateTime

internal interface HomeStore : Store<HomeStore.Intent, HomeStore.State, HomeStore.Label> {

    sealed interface Intent {
        data class TransactionClicked(val transactionId: Long) : Intent
        data class DeleteRequested(val transactionId: Long) : Intent
        data class DeleteConfirmed(val transactionId: Long) : Intent
        data object FilterClicked : Intent
        data object AddTransactionClicked : Intent
    }

    data class State(
        val balance: Long = 0L,
        val income: Long = 0L,
        val expense: Long = 0L,
        val transactions: List<Transaction> = emptyList(),
        val categories: Map<Long, Category> = emptyMap(),
        val period: Period? = null,
    )

    data class Period(
        val start: LocalDateTime,
        val end: LocalDateTime,
        val preset: PeriodPreset = PeriodPreset.Custom,
        val customTitle: String = "",
    )

    enum class PeriodPreset {
        ThisMonth,
        Custom,
    }

    sealed interface Label {

        data object ScrollFeedToUp: Label
    }
}
