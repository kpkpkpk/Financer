package com.financer.feature.main.presentation

import financer.feature.main.generated.resources.Res
import financer.feature.main.generated.resources.chart_line
import financer.feature.main.generated.resources.home
import financer.feature.main.generated.resources.settings
import org.jetbrains.compose.resources.DrawableResource

enum class MainTabs(
    val icon: DrawableResource,
) {
    HOME(Res.drawable.home),
    Analytics(Res.drawable.chart_line),
    SETTINGS(Res.drawable.settings),
}
