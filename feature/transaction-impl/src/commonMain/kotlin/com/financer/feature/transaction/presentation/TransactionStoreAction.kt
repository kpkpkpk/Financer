package com.financer.feature.transaction.presentation

internal sealed interface TransactionStoreAction {
    data object Init : TransactionStoreAction
}
