package com.financer.feature.main.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.financer.feature.home.api.HomeScreenProvider
import com.financer.feature.main.api.MainComponent
import org.koin.compose.koinInject

@Composable
fun MainScreen(mainComponent: MainComponent) {
    val homeScreenProvider = koinInject<HomeScreenProvider>()

    Box(modifier = Modifier.fillMaxSize()) {
        MainContent(
            mainComponent = mainComponent,
            homeScreenProvider = homeScreenProvider
        )

        val slotState by mainComponent.slots.subscribeAsState()
        AnimatedContent(
            targetState = slotState.child,
            transitionSpec = {
                slideInVertically { it } togetherWith slideOutVertically { it }
            }
        ) { child ->
            when (child?.instance) {
                is MainComponent.SlotChild.Transaction -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Добавление транзакции")
                    }
                }

                is MainComponent.SlotChild.Filter -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Фильтр")
                    }
                }

                else -> Unit
            }
        }
    }
}
