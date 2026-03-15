package com.financer.feature.main.api

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.financer.feature.home.api.HomeComponent
import com.financer.feature.transaction.api.TransactionComponent

interface MainComponent {
    val pages: Value<ChildPages<*, PagesChild>>
    val slots: Value<ChildSlot<*, SlotChild>>

    fun selectPage(index: Int)
    fun openFilterScreen()
    fun openTransactionScreen(transactionId: Long? = null)
    fun closeSlot()

    sealed interface PagesChild {
        data class Home(val component: HomeComponent) : PagesChild
        data object Analytics : PagesChild
        data object Settings : PagesChild
    }

    sealed interface SlotChild {
        data class Transaction(val component: TransactionComponent) : SlotChild
        data object Filter : SlotChild
    }
}

fun interface MainComponentFactory {
    fun create(componentContext: ComponentContext): MainComponent
}
