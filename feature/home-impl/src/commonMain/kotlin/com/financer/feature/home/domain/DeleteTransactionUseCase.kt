package com.financer.feature.home.domain

import com.financer.core.data.repository.TransactionRepository

class DeleteTransactionUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(transactionId: Long) {
        transactionRepository.deleteById(transactionId)
    }
}

