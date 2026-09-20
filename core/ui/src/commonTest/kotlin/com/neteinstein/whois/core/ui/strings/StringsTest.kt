package com.neteinstein.whois.core.ui.strings

import com.neteinstein.whois.core.common.AppLanguage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class StringsTest {

    @Test
    fun `stringsFor maps each language to its own copy`() {
        assertEquals(EnglishStrings, stringsFor(AppLanguage.ENGLISH))
        assertEquals(PortugueseStrings, stringsFor(AppLanguage.PORTUGUESE))
        assertNotEquals(EnglishStrings.searchSubtitle, PortugueseStrings.searchSubtitle)
    }
}
