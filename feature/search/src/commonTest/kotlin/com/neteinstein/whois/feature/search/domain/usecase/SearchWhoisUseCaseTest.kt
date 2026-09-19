package com.neteinstein.whois.feature.search.domain.usecase

import com.neteinstein.whois.core.common.UrlOpener
import com.neteinstein.whois.feature.search.domain.model.SearchQuery
import com.neteinstein.whois.feature.search.domain.repository.WhoisSearchRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

private class FakeRepository : WhoisSearchRepository {
    override fun buildSearchUrl(query: SearchQuery): String = "https://search.brave.com/search?q=fake"
}

private class RecordingUrlOpener : UrlOpener {
    var openedUrl: String? = null
    override fun open(url: String) {
        openedUrl = url
    }
}

class SearchWhoisUseCaseTest {

    @Test
    fun `does nothing and returns false when every field is blank`() = runTest {
        val urlOpener = RecordingUrlOpener()
        val useCase = SearchWhoisUseCase(FakeRepository(), urlOpener)

        val opened = useCase(SearchQuery())

        assertFalse(opened)
        assertEquals(null, urlOpener.openedUrl)
    }

    @Test
    fun `opens the built url when at least one field is filled`() = runTest {
        val urlOpener = RecordingUrlOpener()
        val useCase = SearchWhoisUseCase(FakeRepository(), urlOpener)

        val opened = useCase(SearchQuery(name = "Jane"))

        assertTrue(opened)
        assertEquals("https://search.brave.com/search?q=fake", urlOpener.openedUrl)
    }
}
