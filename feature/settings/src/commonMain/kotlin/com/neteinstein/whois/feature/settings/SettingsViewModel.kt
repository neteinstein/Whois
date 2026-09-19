package com.neteinstein.whois.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neteinstein.whois.core.common.AppLanguage
import com.neteinstein.whois.core.common.DispatcherProvider
import com.neteinstein.whois.core.common.UrlOpener
import com.neteinstein.whois.core.ui.navigation.Navigator
import com.neteinstein.whois.feature.settings.domain.ChangeLanguageUseCase
import com.neteinstein.whois.feature.settings.domain.ObserveLanguageUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val PROJECT_URL = "https://github.com/neteinstein/loopgain"

class SettingsViewModel(
    observeLanguageUseCase: ObserveLanguageUseCase,
    private val changeLanguageUseCase: ChangeLanguageUseCase,
    private val navigator: Navigator,
    private val urlOpener: UrlOpener,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    val language: StateFlow<AppLanguage> = observeLanguageUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppLanguage.default,
    )

    fun onLanguageSelected(language: AppLanguage) {
        viewModelScope.launch(dispatchers.default) {
            changeLanguageUseCase(language)
        }
    }

    fun onBackClicked() {
        navigator.popBackStack()
    }

    fun onViewSourceClicked() {
        urlOpener.open(PROJECT_URL)
    }
}
