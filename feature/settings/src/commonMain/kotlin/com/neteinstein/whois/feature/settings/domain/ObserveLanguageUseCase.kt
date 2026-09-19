package com.neteinstein.whois.feature.settings.domain

import com.neteinstein.whois.core.common.AppLanguage
import kotlinx.coroutines.flow.StateFlow

class ObserveLanguageUseCase(
    private val repository: SettingsRepository,
) {
    operator fun invoke(): StateFlow<AppLanguage> = repository.language
}
