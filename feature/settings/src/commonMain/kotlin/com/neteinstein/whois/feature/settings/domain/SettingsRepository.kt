package com.neteinstein.whois.feature.settings.domain

import com.neteinstein.whois.core.common.AppLanguage
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val language: StateFlow<AppLanguage>

    fun setLanguage(language: AppLanguage)
}
