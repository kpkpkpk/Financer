package com.financer.feature.main.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.financer.feature.home.api.HomeScreenProvider
import com.financer.feature.main.api.MainComponent
import org.jetbrains.compose.resources.painterResource

@Composable
fun MainContent(
    mainComponent: MainComponent,
    homeScreenProvider: HomeScreenProvider,
    modifier: Modifier = Modifier,
) {
    val pagesState by mainComponent.pages.subscribeAsState()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(
                tonalElevation = 2.dp,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                MainTabs.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = tab.ordinal == pagesState.selectedIndex,
                        onClick = { mainComponent.selectPage(tab.ordinal) },
                        icon = {
                            Icon(
                                painter = painterResource(tab.icon),
                                contentDescription = null,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            val selectedChild = pagesState.items.getOrNull(pagesState.selectedIndex)

            if (selectedChild is Child.Created) {
                when (val child = selectedChild.instance) {
                    is MainComponent.PagesChild.Home -> homeScreenProvider.provideScreen(
                        component = child.component,
                        modifier = Modifier,
                    )

                    is MainComponent.PagesChild.Analytics -> PlaceholderPage("Инсайты")
                    is MainComponent.PagesChild.Settings -> PlaceholderPage("Настройки")
                }
            }
        }
    }
}

@Composable
private fun PlaceholderPage(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

