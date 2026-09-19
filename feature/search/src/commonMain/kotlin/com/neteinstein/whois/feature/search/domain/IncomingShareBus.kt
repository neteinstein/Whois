package com.neteinstein.whois.feature.search.domain

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Bridges platform-specific share-intent handling (Android's `MainActivity.onNewIntent`, an
 * iOS share extension, ...) into the shared [com.neteinstein.whois.feature.search.SearchViewModel]
 * without either side depending on the other's platform APIs.
 */
class IncomingShareBus {
    private val _sharedContacts = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val sharedContacts: SharedFlow<String> = _sharedContacts

    /** [rawVCardText] is the untouched vCard text handed over by the OS share sheet. */
    fun submit(rawVCardText: String) {
        _sharedContacts.tryEmit(rawVCardText)
    }
}
