package com.neteinstein.whois.core.common

import kotlinx.coroutines.Dispatchers

private object IosDispatcherProvider : DispatcherProvider {
    override val main = Dispatchers.Main
    override val io = Dispatchers.IO
    override val default = Dispatchers.Default
}

actual fun defaultDispatcherProvider(): DispatcherProvider = IosDispatcherProvider
