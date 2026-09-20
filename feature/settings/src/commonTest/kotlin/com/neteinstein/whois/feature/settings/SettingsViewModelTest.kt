package com.neteinstein.whois.feature.settings

import com.neteinstein.whois.core.common.AppLanguage
import com.neteinstein.whois.core.common.DispatcherProvider
import com.neteinstein.whois.core.common.UrlOpener
import com.neteinstein.whois.core.ui.navigation.Destination
import com.neteinstein.whois.core.ui.navigation.Navigator
import com.neteinstein.whois.feature.settings.domain.ChangeLanguageUseCase
import com.neteinstein.whois.feature.settings.domain.ObserveLanguageUseCase
import com.neteinstein.whois.feature.settings.domain.SettingsRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

private class FakeSettingsRepository : SettingsRepository {
    private val _language = MutableStateFlow(AppLanguage.default)
    override val language: StateFlow<AppLanguage> = _language

    override fun setLanguage(language: AppLanguage) {
        _language.value = language
    }
}

private class RecordingUrlOpener : UrlOpener {
    var openedUrl: String? = null
    override fun open(url: String) {
        openedUrl = url
    }
}

class SettingsViewModelTest {

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
        repository: SettingsRepository = FakeSettingsRepository(),
        navigator: Navigator = Navigator(),
        urlOpener: RecordingUrlOpener = RecordingUrlOpener()
    ) = SettingsViewModel(
        observeLanguageUseCase = ObserveLanguageUseCase(repository),
        changeLanguageUseCase = ChangeLanguageUseCase(repository),
        navigator = navigator,
        urlOpener = urlOpener,
        dispatchers = dispatchers
    )

    @Test
    fun `selecting a language persists it through the repository`() = runTest(testDispatcher) {
        val repository = FakeSettingsRepository()
        val viewModel = createViewModel(repository = repository)

        viewModel.onLanguageSelected(AppLanguage.PORTUGUESE)
        advanceUntilIdle()

        assertEquals(AppLanguage.PORTUGUESE, repository.language.value)
    }

    @Test
    fun `back click pops the navigator back stack`() {
        val navigator = Navigator()
        navigator.navigate(Destination.Settings)
        val viewModel = createViewModel(navigator = navigator)

        viewModel.onBackClicked()

        assertEquals(listOf(Destination.Splash), navigator.backStack.value)
    }

    @Test
    fun `view source click opens the project url`() {
        val urlOpener = RecordingUrlOpener()
        val viewModel = createViewModel(urlOpener = urlOpener)

        viewModel.onViewSourceClicked()

        assertEquals("https://github.com/neteinstein/loopgain", urlOpener.openedUrl)
    }
}
