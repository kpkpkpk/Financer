package com.financer.feature.main.presentation

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.financer.feature.home.api.HomeScreenProvider
import com.financer.feature.main.api.MainComponent
import com.financer.feature.transaction.api.TransactionScreenProvider
import org.koin.compose.koinInject

@Composable
fun MainScreen(mainComponent: MainComponent) {
    val homeScreenProvider = koinInject<HomeScreenProvider>()
    val transactionScreenProvider = koinInject<TransactionScreenProvider>()

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
            when (val instance = child?.instance) {
                is MainComponent.SlotChild.Transaction -> {
                    transactionScreenProvider.provideScreen(
                        component = instance.component,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                    )
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

                null -> Unit
            }
        }
    }
}
