package com.neteinstein.whois.core.ui.navigation

/**
 * The app's full navigation graph, deliberately hand-rolled instead of pulling in
 * Compose Multiplatform Navigation: with only three screens and no deep-link needs, a small
 * sealed backstack (see `Navigator` in :composeApp) avoids taking on a fast-moving
 * multiplatform-navigation dependency for little benefit.
 */
sealed interface Destination {
    data object Splash : Destination

    data object Search : Destination

    data object Settings : Destination
}
