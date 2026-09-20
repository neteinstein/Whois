package com.neteinstein.whois.app

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.neteinstein.whois.core.common.UrlOpener

private const val BRAVE_PACKAGE_NAME = "com.brave.browser"

/** Prefers launching Brave explicitly; falls back to whatever browser the user has set as default. */
class AndroidUrlOpener(private val context: Context) : UrlOpener {
    override fun open(url: String) {
        val uri = Uri.parse(url)
        val braveIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(BRAVE_PACKAGE_NAME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(braveIntent)
        } catch (_: ActivityNotFoundException) {
            val fallbackIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallbackIntent)
        }
    }
}
