package com.neteinstein.whois.feature.search.data

import com.neteinstein.whois.feature.search.domain.model.SearchQuery
import kotlin.test.Test
import kotlin.test.assertTrue

class WhoisSearchRepositoryImplTest {

    private val repository = WhoisSearchRepositoryImpl()

    @Test
    fun `builds a brave search url containing the encoded query details`() {
        val url = repository.buildSearchUrl(SearchQuery(name = "John Doe", phone = "+1 555"))

        assertTrue(url.startsWith("https://search.brave.com/search?q="))
        assertTrue(url.contains("John+Doe") || url.contains("John%20Doe"))
        assertTrue(url.contains("%2B1"), "expected percent-encoded '+' from the phone number")
    }

    @Test
    fun `omits blank fields from the prompt`() {
        val url = repository.buildSearchUrl(SearchQuery(name = "Acme"))

        // "company" alone would also match the fixed "person or company?" boilerplate, so these
        // check for the per-field label pattern (`<field>+%22`, i.e. `field "` url-encoded).
        assertTrue(!url.contains("phone+number+%22"))
        assertTrue(!url.contains("company+%22"))
        assertTrue(!url.contains("address+%22"))
    }
}
