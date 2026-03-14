package com.financer.feature.home.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.Flow

interface HomeComponent {
    fun onDestroy()
}

fun interface HomeComponentFactory {
    fun create(
        componentContext: ComponentContext,
        onOpenTransaction: (Long?) -> Unit,
        onOpenFilter: () -> Unit,
        onUpEventListener: ()-> Flow<Unit>
    ): HomeComponent
}

interface HomeScreenProvider {

    @Composable
    fun provideScreen(
        component: HomeComponent,
        modifier: Modifier,
    )
}
