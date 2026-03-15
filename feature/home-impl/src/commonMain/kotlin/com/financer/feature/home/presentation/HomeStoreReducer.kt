package com.financer.feature.home.presentation

import com.arkivanov.mvikotlin.core.store.Reducer

internal class HomeStoreReducer : Reducer<HomeStore.State, HomeStoreMessage> {
    override fun HomeStore.State.reduce(msg: HomeStoreMessage): HomeStore.State =
        when (msg) {
            is HomeStoreMessage.DataLoaded -> copy(
                balance = msg.balance,
                income = msg.income,
                expense = msg.expense,
                transactions = msg.transactions,
                categories = msg.categories,
            )
        }
}
