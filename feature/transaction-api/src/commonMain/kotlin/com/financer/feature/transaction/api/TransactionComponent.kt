package com.financer.feature.transaction.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext

interface TransactionComponent {
    fun onDestroy()
}

fun interface TransactionComponentFactory {
    fun create(
        componentContext: ComponentContext,
        transactionId: Long?,
        onClose: () -> Unit,
    ): TransactionComponent
}

interface TransactionScreenProvider {

    @Composable
    fun provideScreen(
        component: TransactionComponent,
        modifier: Modifier,
    )
}
