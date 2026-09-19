package com.neteinstein.whois.core.common

import kotlin.test.Test
import kotlin.test.assertEquals

class AppLanguageTest {
    @Test
    fun `fromCode resolves known codes`() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("en"))
        assertEquals(AppLanguage.PORTUGUESE, AppLanguage.fromCode("pt"))
    }

    @Test
    fun `fromCode falls back to default for unknown or null codes`() {
        assertEquals(AppLanguage.default, AppLanguage.fromCode("fr"))
        assertEquals(AppLanguage.default, AppLanguage.fromCode(null))
    }
}
