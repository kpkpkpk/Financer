package com.financer.feature.home.domain

import com.financer.core.common.coroutines.runCatchingCoroutine
import com.financer.core.data.model.TransactionType
import com.financer.core.data.repository.TransactionRepository
import com.financer.feature.home.domain.model.TotalSumByType
import kotlinx.datetime.LocalDateTime

class GetTotalSumByTypeInPeriodUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(startDate: LocalDateTime, endDate: LocalDateTime): TotalSumByType {
        return runCatchingCoroutine {
            val transactions = transactionRepository.getSumByTypeAndPeriod(startDate, endDate)
            TotalSumByType(
                incomeSum = transactions[TransactionType.INCOME] ?: 0,
                expenseSum = transactions[TransactionType.EXPENSE] ?: 0
            )
        }.getOrDefault(TotalSumByType(incomeSum = 0L, expenseSum = 0L))
    }
}
