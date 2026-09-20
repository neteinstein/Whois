package com.neteinstein.whois.core.ui.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NavigatorTest {

    @Test
    fun `starts with a single-entry back stack`() {
        val navigator = Navigator(Destination.Splash)

        assertEquals(listOf(Destination.Splash), navigator.backStack.value)
    }

    @Test
    fun `navigate pushes onto the back stack`() {
        val navigator = Navigator(Destination.Search)

        navigator.navigate(Destination.Settings)

        assertEquals(listOf(Destination.Search, Destination.Settings), navigator.backStack.value)
    }

    @Test
    fun `popBackStack removes the top entry and returns true`() {
        val navigator = Navigator(Destination.Search)
        navigator.navigate(Destination.Settings)

        val popped = navigator.popBackStack()

        assertTrue(popped)
        assertEquals(listOf(Destination.Search), navigator.backStack.value)
    }

    @Test
    fun `popBackStack on a single-entry stack is a no-op and returns false`() {
        val navigator = Navigator(Destination.Search)

        val popped = navigator.popBackStack()

        assertFalse(popped)
        assertEquals(listOf(Destination.Search), navigator.backStack.value)
    }

    @Test
    fun `replaceAll clears history`() {
        val navigator = Navigator(Destination.Splash)
        navigator.navigate(Destination.Search)
        navigator.navigate(Destination.Settings)

        navigator.replaceAll(Destination.Search)

        assertEquals(listOf(Destination.Search), navigator.backStack.value)
    }
}
