package com.financer.core.data.model

data class Category(
    val id: Long = 0,
    val name: String,
    val emoji: String,
    val type: TransactionType,
    val isDefault: Boolean = true
)
