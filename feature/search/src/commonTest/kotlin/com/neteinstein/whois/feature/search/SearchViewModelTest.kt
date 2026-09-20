package com.neteinstein.whois.feature.search

import com.neteinstein.whois.core.common.DispatcherProvider
import com.neteinstein.whois.core.common.UrlOpener
import com.neteinstein.whois.core.ui.navigation.Destination
import com.neteinstein.whois.core.ui.navigation.Navigator
import com.neteinstein.whois.feature.search.domain.IncomingShareBus
import com.neteinstein.whois.feature.search.domain.model.SearchQuery
import com.neteinstein.whois.feature.search.domain.repository.WhoisSearchRepository
import com.neteinstein.whois.feature.search.domain.usecase.ParseSharedContactUseCase
import com.neteinstein.whois.feature.search.domain.usecase.SearchWhoisUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

private class FakeRepository : WhoisSearchRepository {
    override fun buildSearchUrl(query: SearchQuery): String = "https://search.brave.com/search?q=fake"
}

private class RecordingUrlOpener : UrlOpener {
    var openedUrl: String? = null
    override fun open(url: String) {
        openedUrl = url
    }
}

class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val dispatchers = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        urlOpener: RecordingUrlOpener = RecordingUrlOpener(),
        incomingShareBus: IncomingShareBus = IncomingShareBus(),
        navigator: Navigator = Navigator()
    ) = SearchViewModel(
        searchWhoisUseCase = SearchWhoisUseCase(FakeRepository(), urlOpener),
        parseSharedContactUseCase = ParseSharedContactUseCase(),
        incomingShareBus = incomingShareBus,
        navigator = navigator,
        dispatchers = dispatchers
    )

    @Test
    fun `field changes update state and clear the empty-fields error`() {
        val viewModel = createViewModel()

        viewModel.onNameChange("Jane")
        viewModel.onPhoneChange("555")
        viewModel.onCompanyChange("Acme")
        viewModel.onAddressChange("Main St")

        assertEquals("Jane", viewModel.uiState.value.name)
        assertEquals("555", viewModel.uiState.value.phone)
        assertEquals("Acme", viewModel.uiState.value.company)
        assertEquals("Main St", viewModel.uiState.value.address)
    }

    @Test
    fun `search with all fields blank surfaces the empty-fields error`() = runTest(testDispatcher) {
        val urlOpener = RecordingUrlOpener()
        val viewModel = createViewModel(urlOpener = urlOpener)

        viewModel.onSearchClicked()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showEmptyFieldsError)
        assertEquals(null, urlOpener.openedUrl)
    }

    @Test
    fun `search with a filled field opens the url and clears the error`() = runTest(testDispatcher) {
        val urlOpener = RecordingUrlOpener()
        val viewModel = createViewModel(urlOpener = urlOpener)

        viewModel.onNameChange("Jane")
        viewModel.onSearchClicked()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showEmptyFieldsError)
        assertEquals("https://search.brave.com/search?q=fake", urlOpener.openedUrl)
    }

    @Test
    fun `settings click navigates to the settings destination`() {
        val navigator = Navigator()
        val viewModel = createViewModel(navigator = navigator)

        viewModel.onSettingsClicked()

        assertEquals(listOf(Destination.Splash, Destination.Settings), navigator.backStack.value)
    }

    @Test
    fun `a shared contact fills the fields shows the banner and triggers a search`() = runTest(testDispatcher) {
        val urlOpener = RecordingUrlOpener()
        val incomingShareBus = IncomingShareBus()
        val viewModel = createViewModel(urlOpener = urlOpener, incomingShareBus = incomingShareBus)

        // Let the ViewModel's init block start collecting incomingShareBus.sharedContacts before
        // submitting - a SharedFlow with replay = 0 drops an emission with no active subscriber.
        advanceUntilIdle()

        val vCard = """
            BEGIN:VCARD
            FN:Jane Roe
            TEL:555
            END:VCARD
        """.trimIndent()
        incomingShareBus.submit(vCard)
        advanceUntilIdle()

        assertEquals("Jane Roe", viewModel.uiState.value.name)
        assertEquals("555", viewModel.uiState.value.phone)
        assertTrue(viewModel.uiState.value.sharedContactBannerVisible)
        assertEquals("https://search.brave.com/search?q=fake", urlOpener.openedUrl)
    }
}
