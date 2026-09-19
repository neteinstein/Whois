package com.neteinstein.whois.app

import android.content.Context
import com.neteinstein.whois.app.di.androidPlatformModule
import com.neteinstein.whois.app.di.sharedModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/** Called once from [com.neteinstein.whois.WhoisApplication.onCreate] in `:androidApp`. */
fun initKoinAndroid(context: Context) {
    startKoin {
        androidContext(context)
        modules(sharedModules() + androidPlatformModule)
    }
}
