package com.neteinstein.whois.app.di

import com.neteinstein.whois.app.AndroidUrlOpener
import com.neteinstein.whois.core.common.UrlOpener
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidPlatformModule = module {
    single<UrlOpener> { AndroidUrlOpener(androidContext()) }
}
