package com.financer.feature.main.presentation

import financer.feature.main_impl.generated.resources.Res
import financer.feature.main_impl.generated.resources.chart_line
import financer.feature.main_impl.generated.resources.home
import financer.feature.main_impl.generated.resources.settings
import org.jetbrains.compose.resources.DrawableResource

enum class MainTabs(
    val icon: DrawableResource,
) {
    HOME(Res.drawable.home),
    Analytics(Res.drawable.chart_line),
    SETTINGS(Res.drawable.settings),
}
