package com.financer.feature.home.domain

import com.financer.core.data.model.Transaction
import com.financer.core.data.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

class GetTransactionsUseCase(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Transaction>> {
        return transactionRepository.getByPeriod(startDate, endDate)
    }
}
