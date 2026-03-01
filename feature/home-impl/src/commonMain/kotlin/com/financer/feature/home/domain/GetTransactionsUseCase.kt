package com.financer.feature.home.domain

import com.financer.core.data.model.Transaction
import com.financer.core.data.model.TransactionType
import com.financer.core.data.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDateTime

class GetTransactionsUseCase(
    private val transactionRepository: TransactionRepository
) {
    private val useMockTransactions = true

    operator fun invoke(
        _startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Transaction>> {
        if (!useMockTransactions) {
            return transactionRepository.getByPeriod(_startDate, endDate)
        }

        val mockTransactions = List(50) { index ->
            val position = index + 1
            val isIncome = position % 7 == 0
            val dayShift = index / 5
            val day = (endDate.dayOfMonth - dayShift).coerceAtLeast(1)

            Transaction(
                id = position.toLong(),
                type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
                amount = if (isIncome) {
                    15_000L + position * 640L
                } else {
                    250L + position * 135L
                },
                categoryId = ((position % 20) + 1).toLong(),
                date = LocalDateTime(
                    year = endDate.year,
                    month = endDate.month,
                    day = day,
                    hour = 8 + (index % 12),
                    minute = (index * 7) % 60
                ),
                note = "Mock transaction #$position",
            )
        }

        return flowOf(mockTransactions)
    }
}
