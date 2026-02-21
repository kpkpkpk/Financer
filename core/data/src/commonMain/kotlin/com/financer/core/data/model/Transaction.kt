package com.financer.core.data.model

data class Transaction(
    val id: Long = 0,
    val type: TransactionType,
    val amount: Long,
    val categoryId: Long,
    val date: String,
    val note: String? = null
)
