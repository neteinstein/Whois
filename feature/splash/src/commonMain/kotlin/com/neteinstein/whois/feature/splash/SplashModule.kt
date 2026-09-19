package com.neteinstein.whois.feature.splash

import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val splashModule = module {
    viewModel { SplashViewModel(dispatchers = get()) }
}
