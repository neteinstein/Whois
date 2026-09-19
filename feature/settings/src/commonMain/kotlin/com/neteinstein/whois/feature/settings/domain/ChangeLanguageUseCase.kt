package com.neteinstein.whois.feature.settings.domain

import com.neteinstein.whois.core.common.AppLanguage
import com.neteinstein.whois.core.common.UseCase

class ChangeLanguageUseCase(
    private val repository: SettingsRepository,
) : UseCase<AppLanguage, Unit> {
    override suspend fun invoke(params: AppLanguage) {
        repository.setLanguage(params)
    }
}
