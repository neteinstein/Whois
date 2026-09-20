package com.neteinstein.whois

import android.content.Intent
import android.os.Build
import android.os.Parcelable

/** [Intent.getParcelableExtra] was deprecated (with a typed replacement) starting API 33. */
@Suppress("DEPRECATION")
internal fun <T : Parcelable> Intent.parcelableExtraCompat(key: String, type: Class<T>): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, type)
    } else {
        getParcelableExtra(key)
    }
