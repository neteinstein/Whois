package com.neteinstein.whois.core.ui.strings

import androidx.compose.runtime.staticCompositionLocalOf
import com.neteinstein.whois.core.common.AppLanguage

val LocalStrings = staticCompositionLocalOf { EnglishStrings }

fun stringsFor(language: AppLanguage): Strings = when (language) {
    AppLanguage.ENGLISH -> EnglishStrings
    AppLanguage.PORTUGUESE -> PortugueseStrings
}
