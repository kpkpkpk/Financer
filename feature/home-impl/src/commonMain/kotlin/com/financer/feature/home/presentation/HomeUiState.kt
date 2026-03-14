package com.financer.feature.home.presentation

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface HomeListItem {
    val key: String

    data class DateHeader(
        override val key: String,
        val title: TextValue,
    ) : HomeListItem

    data class Transaction(
        override val key: String,
        val item: HomeTransactionItem,
    ) : HomeListItem

    data object EmptyState : HomeListItem {
        override val key: String = "empty-state"
    }

    class Space(val heightDp: Int): HomeListItem {
        override val key: String = "space+$heightDp"
    }
}

@Immutable
internal data class HomeTransactionItem(
    val id: Long,
    val categoryEmoji: String,
    val categoryName: String?,
    val time: String,
    val formattedAmount: String,
    val isIncome: Boolean,
)
