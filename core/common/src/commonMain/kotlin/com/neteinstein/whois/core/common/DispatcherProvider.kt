package com.neteinstein.whois.core.common

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Injectable indirection over [kotlinx.coroutines.Dispatchers] so ViewModels and use cases
 * stay testable (tests supply a single-threaded/test dispatcher for all three).
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

expect fun defaultDispatcherProvider(): DispatcherProvider
