package com.financer.feature.home.domain

import com.financer.core.data.model.TransactionType
import com.financer.core.data.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class GetBalanceUseCase(
    private val transactionsRepository: TransactionRepository
) {

    operator fun invoke(): Flow<Long> {
        return transactionsRepository.getAll().map { operations ->
            var total = 0L
            operations.forEach {
                when (it.type) {
                    TransactionType.INCOME -> total += it.amount
                    TransactionType.EXPENSE -> total -= it.amount
                }
            }
            total
        }
    }
}
