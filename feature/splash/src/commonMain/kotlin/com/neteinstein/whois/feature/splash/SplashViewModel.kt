package com.neteinstein.whois.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neteinstein.whois.core.common.DispatcherProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val SPLASH_MIN_DURATION_MS = 1400L

class SplashViewModel(
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    init {
        viewModelScope.launch(dispatchers.default) {
            delay(SPLASH_MIN_DURATION_MS)
            _isReady.value = true
        }
    }
}
