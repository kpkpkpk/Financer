package com.financer.feature.home.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface HomeComponent {
    fun onLoadData()
    fun onTransactionClicked(transactionId: Long)
    fun onDeleteRequested(transactionId: Long)
    fun onDeleteConfirmed(transactionId: Long)
    fun onFilterClicked()
    fun onDestroy()
}

fun interface HomeComponentFactory {
    fun create(
        onOpenTransaction: (Long) -> Unit,
        onOpenFilter: () -> Unit,
    ): HomeComponent
}

interface HomeScreenProvider {
    @Composable
    fun Screen(
        component: HomeComponent,
        modifier: Modifier = Modifier,
    )
}

