package com.financer.feature.transaction.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.financer.feature.transaction.api.TransactionComponent
import com.financer.feature.transaction.api.TransactionScreenProvider

internal class DefaultTransactionScreenProvider : TransactionScreenProvider {

    @Composable
    override fun provideScreen(component: TransactionComponent, modifier: Modifier) {
        val defaultComponent = component as? DefaultTransactionComponent ?: return
        TransactionScreen(
            uiState = defaultComponent.uiState,
            slot = defaultComponent.slot,
            onIntent = defaultComponent.store::accept,
            onOpenCategoryPicker = defaultComponent::openCategoryPicker,
            onCloseCategoryPicker = defaultComponent::closeCategoryPicker,
            modifier = modifier,
        )
    }
}
