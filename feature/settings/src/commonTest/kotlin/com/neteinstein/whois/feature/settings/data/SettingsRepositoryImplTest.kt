package com.neteinstein.whois.feature.settings.data

import com.neteinstein.whois.core.common.AppLanguage
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsRepositoryImplTest {

    @Test
    fun `defaults to English when nothing was persisted yet`() {
        val repository = SettingsRepositoryImpl(MapSettings())

        assertEquals(AppLanguage.ENGLISH, repository.language.value)
    }

    @Test
    fun `setLanguage updates and persists the selection`() {
        val settings = MapSettings()
        val repository = SettingsRepositoryImpl(settings)

        repository.setLanguage(AppLanguage.PORTUGUESE)

        assertEquals(AppLanguage.PORTUGUESE, repository.language.value)
        assertEquals("pt", settings.getStringOrNull("language_code"))
    }

    @Test
    fun `reads a previously persisted language on construction`() {
        val settings = MapSettings("language_code" to "pt")

        val repository = SettingsRepositoryImpl(settings)

        assertEquals(AppLanguage.PORTUGUESE, repository.language.value)
    }
}
