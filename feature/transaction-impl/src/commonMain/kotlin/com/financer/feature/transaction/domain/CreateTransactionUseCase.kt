package com.financer.feature.transaction.domain

import com.financer.core.data.model.Transaction
import com.financer.core.data.model.TransactionType
import com.financer.core.data.repository.TransactionRepository
import kotlinx.datetime.LocalDateTime

sealed interface SaveTransactionResult {
    data object Success : SaveTransactionResult
    data object InvalidAmount : SaveTransactionResult
    data object CategoryNotSelected : SaveTransactionResult
}

class CreateTransactionUseCase(
    private val transactionRepository: TransactionRepository,
) {

    suspend operator fun invoke(
        transactionId: Long?,
        type: TransactionType,
        amount: Long,
        categoryId: Long?,
        date: LocalDateTime,
        note: String,
    ): SaveTransactionResult {
        if (amount <= 0L) return SaveTransactionResult.InvalidAmount
        val resolvedCategoryId = categoryId ?: return SaveTransactionResult.CategoryNotSelected

        val transaction = Transaction(
            id = transactionId ?: 0L,
            type = type,
            amount = amount,
            categoryId = resolvedCategoryId,
            date = date,
            note = note.trim().ifBlank { null },
        )

        if (transactionId == null) {
            transactionRepository.insert(transaction)
        } else {
            transactionRepository.update(transaction)
        }

        return SaveTransactionResult.Success
    }
}
