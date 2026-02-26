package com.financer.feature.home.domain

import com.financer.core.common.coroutines.runCatchingCoroutine
import com.financer.core.data.model.TransactionType
import com.financer.core.data.repository.TransactionRepository

internal class GetBalanceUseCase(
    private val transactionsRepository: TransactionRepository
) {

    suspend operator fun invoke(): Result<Long> {
        return runCatchingCoroutine {
            val operations = transactionsRepository.getAll()
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
