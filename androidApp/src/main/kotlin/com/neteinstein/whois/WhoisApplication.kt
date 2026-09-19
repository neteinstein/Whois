package com.neteinstein.whois

import android.app.Application
import com.neteinstein.whois.app.initKoinAndroid

class WhoisApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoinAndroid(this)
    }
}
