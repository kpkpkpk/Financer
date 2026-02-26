package com.financer.core.data.model

import kotlinx.datetime.LocalDateTime

data class Transaction(
    val id: Long = 0,
    val type: TransactionType,
    val amount: Long,
    val categoryId: Long,
    val date: LocalDateTime,
    val note: String? = null
)
