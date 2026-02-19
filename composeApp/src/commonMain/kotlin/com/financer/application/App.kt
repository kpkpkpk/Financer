package com.financer.application

import androidx.compose.runtime.Composable
import com.financer.application.navigation.RootComponent
import com.financer.application.navigation.RootContent
import com.financer.core.ui.theme.FinancerTheme

@Composable
fun App(rootComponent: RootComponent) {
    FinancerTheme {
        RootContent(rootComponent)
    }
}
