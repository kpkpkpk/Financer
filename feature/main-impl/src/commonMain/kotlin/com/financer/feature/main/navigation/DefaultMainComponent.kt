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
import com.financer.feature.home.api.HomeComponentFactory
import com.financer.feature.main.api.MainComponent
import kotlinx.serialization.Serializable

class DefaultMainComponent(
    componentContext: ComponentContext,
    homeComponentFactory: HomeComponentFactory
) : MainComponent, ComponentContext by componentContext {

    private val pagesNavigation = PagesNavigation<PagesConfig>()
    private val slotNavigation = SlotNavigation<SlotsConfig>()

    override val pages: Value<ChildPages<*, MainComponent.PagesChild>> = childPages(
        source = pagesNavigation,
        serializer = PagesConfig.serializer(),
        initialPages = {
            Pages(
                items = listOf(
                    PagesConfig.Home,
                    PagesConfig.Analytics,
                    PagesConfig.Settings,
                ),
                selectedIndex = 0,
            )
        },
    ) { config, _ ->
        when (config) {
            PagesConfig.Home -> MainComponent.PagesChild.Home(
                homeComponentFactory.create(
                    onOpenFilter = { openFilterScreen() },
                    onOpenTransaction = { openTransactionScreen() }
                )
            )

            PagesConfig.Analytics -> MainComponent.PagesChild.Analytics
            PagesConfig.Settings -> MainComponent.PagesChild.Settings
        }
    }

    override val slots: Value<ChildSlot<*, MainComponent.SlotChild>> = childSlot(
        source = slotNavigation,
        serializer = SlotsConfig.serializer(),
        handleBackButton = true,
    ) { config, _ ->
        when (config) {
            SlotsConfig.Filter -> MainComponent.SlotChild.Filter
            SlotsConfig.Transaction -> MainComponent.SlotChild.Transaction
        }
    }

    override fun selectPage(index: Int) {
        pagesNavigation.select(index)
    }

    override fun openFilterScreen() {
        slotNavigation.activate(SlotsConfig.Filter)
    }

    override fun openTransactionScreen() {
        slotNavigation.activate(SlotsConfig.Transaction)
    }

    override fun closeSlot() {
        slotNavigation.dismiss()
    }

    @Serializable
    private sealed interface PagesConfig {
        @Serializable
        data object Home : PagesConfig

        @Serializable
        data object Analytics : PagesConfig

        @Serializable
        data object Settings : PagesConfig
    }

    @Serializable
    private sealed interface SlotsConfig {
        @Serializable
        data object Transaction : SlotsConfig

        @Serializable
        data object Filter : SlotsConfig
    }
}
