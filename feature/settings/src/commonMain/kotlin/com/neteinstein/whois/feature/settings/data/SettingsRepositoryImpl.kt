package com.neteinstein.whois.feature.settings.data

import com.neteinstein.whois.core.common.AppLanguage
import com.neteinstein.whois.feature.settings.domain.SettingsRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val KEY_LANGUAGE = "language_code"

class SettingsRepositoryImpl(private val settings: Settings) : SettingsRepository {

    private val _language = MutableStateFlow(
        AppLanguage.fromCode(settings.getStringOrNull(KEY_LANGUAGE))
    )
    override val language: StateFlow<AppLanguage> = _language

    override fun setLanguage(language: AppLanguage) {
        settings.putString(KEY_LANGUAGE, language.code)
        _language.value = language
    }
}
