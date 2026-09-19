package com.neteinstein.whois.core.common

/** Languages the app UI can be displayed in, independent of the OS locale. */
enum class AppLanguage(val code: String) {
    ENGLISH("en"),
    PORTUGUESE("pt")
    ;

    companion object {
        val default: AppLanguage = ENGLISH

        fun fromCode(code: String?): AppLanguage = entries.firstOrNull { it.code == code } ?: default
    }
}
