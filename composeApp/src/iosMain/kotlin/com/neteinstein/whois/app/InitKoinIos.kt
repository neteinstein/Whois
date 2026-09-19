package com.neteinstein.whois.app

import com.neteinstein.whois.app.di.iosPlatformModule
import com.neteinstein.whois.app.di.sharedModules
import org.koin.core.context.startKoin

/**
 * Called from `iOSApp.swift`'s `init()`.
 *
 * Named `doInitKoin` rather than `initKoin`: Kotlin/Native's Objective-C exporter treats a
 * top-level function whose name starts with `init` as an initializer-style selector and mangles
 * it, so Swift would not see a plain `InitKoinKt.initKoin()`. Prefixing with `do` sidesteps that.
 */
fun doInitKoin() {
    startKoin {
        modules(sharedModules() + iosPlatformModule)
    }
}
