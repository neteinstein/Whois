package com.neteinstein.whois.core.ui.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Minimal backstack navigator shared by every screen via Koin, so a feature module can navigate
 * without depending on `:composeApp` (which is what actually renders `backStack.last()`).
 */
class Navigator(startDestination: Destination = Destination.Splash) {
    private val _backStack = MutableStateFlow(listOf(startDestination))
    val backStack: StateFlow<List<Destination>> = _backStack

    fun navigate(destination: Destination) {
        _backStack.update { it + destination }
    }

    /** Replaces the whole stack, used by Splash once its intro delay/routing decision is done. */
    fun replaceAll(destination: Destination) {
        _backStack.update { listOf(destination) }
    }

    fun popBackStack(): Boolean {
        val current = _backStack.value
        if (current.size <= 1) return false
        _backStack.update { it.dropLast(1) }
        return true
    }
}
