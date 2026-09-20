package com.neteinstein.whois.core.common

import kotlinx.coroutines.Dispatchers

private object IosDispatcherProvider : DispatcherProvider {
    override val main = Dispatchers.Main

    // Dispatchers.IO is internal on Kotlin/Native (unlike the JVM/Android artifact); Native has
    // no equivalent blocking-IO thread pool convention, so Default is the accepted substitute.
    override val io = Dispatchers.Default
    override val default = Dispatchers.Default
}

actual fun defaultDispatcherProvider(): DispatcherProvider = IosDispatcherProvider
