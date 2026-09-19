package com.neteinstein.whois.app.di

import com.neteinstein.whois.app.IosUrlOpener
import com.neteinstein.whois.core.common.UrlOpener
import org.koin.dsl.module

val iosPlatformModule = module {
    single<UrlOpener> { IosUrlOpener() }
}
