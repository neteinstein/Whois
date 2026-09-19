package com.neteinstein.whois.app.di

import com.neteinstein.whois.core.common.DispatcherProvider
import com.neteinstein.whois.core.common.defaultDispatcherProvider
import com.neteinstein.whois.core.ui.navigation.Navigator
import com.neteinstein.whois.feature.search.searchModule
import com.neteinstein.whois.feature.settings.settingsModule
import com.neteinstein.whois.feature.splash.splashModule
import org.koin.dsl.module

/** Bindings shared by every feature; each feature's own DI lives in its module (e.g. [searchModule]). */
val coreModule = module {
    single { Navigator() }
    single<DispatcherProvider> { defaultDispatcherProvider() }
}

/**
 * All Koin modules the app needs. Kept in one list (rather than scattering `modules(...)` calls
 * per platform) so `:composeApp`, the Android `initKoinAndroid`, and iOS's `doInitKoin` all start
 * from the exact same graph.
 */
fun sharedModules() = listOf(
    coreModule,
    splashModule,
    searchModule,
    settingsModule,
)
