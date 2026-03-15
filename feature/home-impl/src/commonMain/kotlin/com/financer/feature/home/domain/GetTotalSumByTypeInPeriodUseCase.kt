package com.financer.feature.home.domain

import com.financer.core.data.model.TransactionType
import com.financer.core.data.repository.TransactionRepository
import com.financer.feature.home.domain.model.TotalSumByType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime

class GetTotalSumByTypeInPeriodUseCase(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(startDate: LocalDateTime?, endDate: LocalDateTime?): Flow<TotalSumByType> {
        val source = if (startDate == null || endDate == null) {
            transactionRepository.getSumByType()
        } else {
            transactionRepository.getSumByTypeAndPeriod(startDate, endDate)
        }

        return source.map { transactions ->
            TotalSumByType(
                incomeSum = transactions[TransactionType.INCOME] ?: 0,
                expenseSum = transactions[TransactionType.EXPENSE] ?: 0
            )
        }
    }
}
