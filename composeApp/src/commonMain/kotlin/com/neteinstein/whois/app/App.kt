package com.neteinstein.whois.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.neteinstein.whois.core.ui.navigation.Destination
import com.neteinstein.whois.core.ui.navigation.Navigator
import com.neteinstein.whois.core.ui.strings.LocalStrings
import com.neteinstein.whois.core.ui.strings.stringsFor
import com.neteinstein.whois.core.ui.theme.Motion
import com.neteinstein.whois.core.ui.theme.WhoisTheme
import com.neteinstein.whois.feature.search.SearchScreen
import com.neteinstein.whois.feature.settings.SettingsScreen
import com.neteinstein.whois.feature.settings.domain.SettingsRepository
import com.neteinstein.whois.feature.splash.SplashScreen
import org.koin.compose.koinInject

@Composable
fun App() {
    val navigator = koinInject<Navigator>()
    val settingsRepository = koinInject<SettingsRepository>()

    val language by settingsRepository.language.collectAsState()
    val backStack by navigator.backStack.collectAsState()

    WhoisTheme {
        CompositionLocalProvider(LocalStrings provides stringsFor(language)) {
            Surface(color = MaterialTheme.colorScheme.background) {
                AnimatedContent(
                    targetState = backStack.last(),
                    transitionSpec = {
                        val duration = Motion.DURATION_MEDIUM
                        (
                            slideInHorizontally(tween(duration, easing = Motion.emphasized)) { it / 4 } +
                                fadeIn(tween(duration))
                            ).togetherWith(
                            slideOutHorizontally(tween(duration, easing = Motion.emphasized)) { -it / 4 } +
                                fadeOut(tween(duration))
                        )
                    },
                    label = "app-navigation"
                ) { destination ->
                    when (destination) {
                        Destination.Splash -> SplashScreen(
                            onFinished = { navigator.replaceAll(Destination.Search) }
                        )
                        Destination.Search -> SearchScreen()
                        Destination.Settings -> SettingsScreen()
                    }
                }
            }
        }
    }
}
