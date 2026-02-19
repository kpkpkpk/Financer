package com.financer.core.data

import com.russhwolf.settings.Settings

internal class PreferencesRepositoryImpl(
    private val settings: Settings,
) : PreferencesRepository {

    override fun isOnboardingCompleted(): Boolean =
        settings.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    override fun setOnboardingCompleted(completed: Boolean) {
        settings.putBoolean(KEY_ONBOARDING_COMPLETED, completed)
    }

    private companion object {
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}
