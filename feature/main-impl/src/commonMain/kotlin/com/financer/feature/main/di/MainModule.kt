package com.financer.feature.main.di

import com.financer.feature.main.api.MainComponentFactory
import com.financer.feature.main.navigation.DefaultMainComponent
import org.koin.dsl.module

val mainModule = module {
    single<MainComponentFactory> {
        MainComponentFactory { componentContext ->
            DefaultMainComponent(componentContext, homeComponentFactory = get())
        }
    }
}
