package com.financer.feature.home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.financer.feature.home.api.HomeComponent
import com.financer.feature.home.api.HomeScreenProvider

internal class DefaultHomeScreenProvider : HomeScreenProvider {

    @Composable
    override fun provideScreen(component: HomeComponent, modifier: Modifier) {
        val defaultComponent = component as? DefaultHomeComponent ?: return
        HomeScreen(
            store = defaultComponent.store,
            addTransactionButtonComponent = defaultComponent.addTransactionButtonComponent,
            headerComponent = defaultComponent.headerComponent,
            listComponent = defaultComponent.listComponent,
            modifier = modifier,
        )
    }
}
