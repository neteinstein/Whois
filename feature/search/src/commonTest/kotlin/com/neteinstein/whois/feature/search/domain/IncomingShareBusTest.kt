package com.neteinstein.whois.feature.search.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield

class IncomingShareBusTest {

    @Test
    fun `submitted vCard text is delivered to an active collector`() = runTest {
        val bus = IncomingShareBus()

        val received = async { bus.sharedContacts.first() }
        yield()
        bus.submit("BEGIN:VCARD\nEND:VCARD")

        assertEquals("BEGIN:VCARD\nEND:VCARD", received.await())
    }
}
