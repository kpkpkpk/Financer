package com.financer.feature.main.api

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value

interface MainComponent {
    val pages: Value<ChildPages<*, PagesChild>>
    val slots: Value<ChildSlot<*, SlotChild>>

    fun selectPage(index: Int)
    fun openFilterScreen()
    fun openTransactionScreen()
    fun closeSlot()

    sealed interface PagesChild {
        data object Home : PagesChild
        data object Analytics : PagesChild
        data object Settings : PagesChild
    }

    sealed interface SlotChild {
        data object Transaction : SlotChild
        data object Filter : SlotChild
    }
}

fun interface MainComponentFactory {
    fun create(componentContext: ComponentContext): MainComponent
}
