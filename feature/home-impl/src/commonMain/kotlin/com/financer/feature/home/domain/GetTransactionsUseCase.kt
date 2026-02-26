package com.financer.feature.home.domain

import com.financer.core.common.coroutines.runCatchingCoroutine
import com.financer.core.data.model.Transaction
import com.financer.core.data.repository.TransactionRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class GetTransactionsUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<List<Transaction>> {
        return runCatchingCoroutine {
            transactionRepository.getByPeriod(startDate, endDate)
        }
    }
}
