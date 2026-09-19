package com.neteinstein.whois.app

import com.neteinstein.whois.core.common.UrlOpener
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

private const val BRAVE_SCHEME_PREFIX = "brave://open-url?url="

/** Prefers handing the URL to Brave's custom scheme; falls back to the default `https://` URL. */
class IosUrlOpener : UrlOpener {

    override fun open(url: String) {
        val application = UIApplication.sharedApplication
        val braveUrl = NSURL(string = BRAVE_SCHEME_PREFIX + url)
        if (braveUrl != null && application.canOpenURL(braveUrl)) {
            application.openURL(braveUrl)
            return
        }
        NSURL(string = url)?.let { application.openURL(it) }
    }
}
