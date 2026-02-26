package com.financer.application.navigation

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.financer.feature.main.presentation.MainScreen
import com.financer.feature.onboarding.presentation.OnboardingScreen

@Composable
fun RootContent(component: RootComponent) {
    Children(
        stack = component.childStack,
        animation = stackAnimation(fade()),
    ) { child ->
        when (val instance = child.instance) {
            is RootComponent.Child.Onboarding -> OnboardingScreen(
                component = instance.component,
            )
            is RootComponent.Child.Main -> MainScreen(instance.mainComponent)
        }
    }
}
