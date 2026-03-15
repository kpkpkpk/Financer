package com.financer.feature.home.presentation

internal sealed interface HomeStoreAction {
    data object Init : HomeStoreAction
}
