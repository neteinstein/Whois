package com.neteinstein.whois.feature.search.data

/**
 * Minimal RFC 3986 `application/x-www-form-urlencoded`-style percent encoder for query values.
 * `kotlin.text` has no multiplatform URL encoder (java.net.URLEncoder is JVM/Android-only), so a
 * tiny common implementation avoids an expect/actual pair for something this small.
 */
internal fun String.percentEncodeForQuery(): String {
    val unreserved = ('a'..'z') + ('A'..'Z') + ('0'..'9') + charArrayOf('-', '_', '.', '~').toList()
    val builder = StringBuilder()
    for (byte in encodeToByteArray()) {
        val char = byte.toInt().toChar()
        if (char in unreserved) {
            builder.append(char)
        } else if (char == ' ') {
            builder.append('+')
        } else {
            val unsignedByte = byte.toInt() and 0xFF
            builder.append('%')
            builder.append(unsignedByte.toString(16).uppercase().padStart(2, '0'))
        }
    }
    return builder.toString()
}
