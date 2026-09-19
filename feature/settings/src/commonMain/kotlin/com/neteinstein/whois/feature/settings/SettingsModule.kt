package com.neteinstein.whois.feature.settings

import com.neteinstein.whois.feature.settings.data.SettingsRepositoryImpl
import com.neteinstein.whois.feature.settings.domain.ChangeLanguageUseCase
import com.neteinstein.whois.feature.settings.domain.ObserveLanguageUseCase
import com.neteinstein.whois.feature.settings.domain.SettingsRepository
import com.russhwolf.settings.Settings
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    single { Settings() }
    single<SettingsRepository> { SettingsRepositoryImpl(settings = get()) }
    factory { ChangeLanguageUseCase(repository = get()) }
    factory { ObserveLanguageUseCase(repository = get()) }
    viewModel {
        SettingsViewModel(
            observeLanguageUseCase = get(),
            changeLanguageUseCase = get(),
            navigator = get(),
            urlOpener = get(),
            dispatchers = get(),
        )
    }
}
