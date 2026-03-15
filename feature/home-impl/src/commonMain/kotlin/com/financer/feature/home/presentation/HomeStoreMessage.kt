package com.financer.feature.home.presentation

import com.financer.core.data.model.Category
import com.financer.core.data.model.Transaction

internal sealed interface HomeStoreMessage {
    data class DataLoaded(
        val balance: Long,
        val income: Long,
        val expense: Long,
        val transactions: List<Transaction>,
        val categories: Map<Long, Category>,
    ) : HomeStoreMessage
}
