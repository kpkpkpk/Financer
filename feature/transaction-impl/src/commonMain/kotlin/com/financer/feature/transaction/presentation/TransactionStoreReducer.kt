package com.financer.feature.transaction.presentation

import com.arkivanov.mvikotlin.core.store.Reducer

internal class TransactionStoreReducer : Reducer<TransactionStore.State, TransactionStoreMessage> {
    override fun TransactionStore.State.reduce(msg: TransactionStoreMessage): TransactionStore.State {
        return when (msg) {
            is TransactionStoreMessage.InitialDataLoaded -> copy(
                transactionId = msg.transactionId,
                amountInput = msg.amountInput,
                type = msg.type,
                selectedCategory = msg.selectedCategory,
                dateInput = msg.dateInput,
                note = msg.note,
                topCategories = msg.topCategories,
                allCategories = msg.allCategories,
                isLoading = false,
            )

            is TransactionStoreMessage.AmountChanged -> copy(
                amountInput = msg.value,
                topCategories = msg.topCategories,
            )

            is TransactionStoreMessage.TypeChanged -> copy(
                type = msg.type,
                selectedCategory = msg.selectedCategory,
                topCategories = msg.topCategories,
                allCategories = msg.allCategories,
            )

            is TransactionStoreMessage.CategoryChanged -> copy(
                selectedCategory = msg.selectedCategory,
            )

            is TransactionStoreMessage.DateInputChanged -> copy(dateInput = msg.dateInput)
            is TransactionStoreMessage.NoteChanged -> copy(note = msg.note)
            is TransactionStoreMessage.SavingChanged -> copy(isSaving = msg.isSaving)
        }
    }
}
