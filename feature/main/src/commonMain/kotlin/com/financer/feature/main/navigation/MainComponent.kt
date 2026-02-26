package com.financer.feature.main.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

class MainComponent(
    componentContext: ComponentContext
) : ComponentContext by componentContext {

    private val pagesNavigation = PagesNavigation<PagesConfig>()
    private val slotNavigation = SlotNavigation<SlotsConfig>()

    val pages: Value<ChildPages<*, PagesChild>> = childPages(
        source = pagesNavigation,
        serializer = PagesConfig.serializer(),
        initialPages = {
            Pages(
                items = listOf(
                    PagesConfig.Home,
                    PagesConfig.Analytics,
                    PagesConfig.Settings,
                ),
                selectedIndex = 0
            )
        }
    ) { config, context ->
        when (config) {
            PagesConfig.Home -> PagesChild.Home()
            PagesConfig.Analytics -> PagesChild.Analytics()
            PagesConfig.Settings -> PagesChild.Settings()
        }
    }

    val slots: Value<ChildSlot<*, SlotChild>> = childSlot(
        source = slotNavigation,
        serializer = SlotsConfig.serializer(),
        handleBackButton = true,
    ) { config, context ->
        when (config) {
            SlotsConfig.Filter -> SlotChild.Filter()
            SlotsConfig.Transaction -> SlotChild.Transaction()
        }
    }


    fun selectPage(index: Int) {
        pagesNavigation.select(index)
    }

    fun openFilterScreen() {
        slotNavigation.activate(SlotsConfig.Filter)
    }

    fun openTransactionScreen() {
        slotNavigation.activate(SlotsConfig.Transaction)
    }

    fun closeSlot() {
        slotNavigation.dismiss()
    }

    sealed class PagesChild {
        class Home : PagesChild()
        class Analytics : PagesChild()
        class Settings : PagesChild()
    }

    sealed class SlotChild {
        class Transaction : SlotChild()
        class Filter : SlotChild()
    }

    @Serializable
    private sealed interface PagesConfig {
        @Serializable
        object Home : PagesConfig

        @Serializable
        object Analytics : PagesConfig

        @Serializable
        object Settings : PagesConfig
    }

    @Serializable
    private sealed interface SlotsConfig {
        @Serializable
        object Transaction : SlotsConfig

        @Serializable
        object Filter : SlotsConfig
    }
}
