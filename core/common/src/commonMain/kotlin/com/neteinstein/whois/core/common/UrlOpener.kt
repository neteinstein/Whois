package com.neteinstein.whois.core.common

/**
 * Opens a URL in an external browser, preferring Brave when it is installed.
 * Implemented per-platform because "is Brave installed" and "launch a URL" are
 * inherently platform concerns (Android [android.content.Intent] vs iOS `UIApplication`).
 */
interface UrlOpener {
    fun open(url: String)
}
