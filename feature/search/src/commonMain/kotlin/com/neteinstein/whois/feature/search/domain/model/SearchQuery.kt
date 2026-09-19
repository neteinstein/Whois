package com.neteinstein.whois.feature.search.domain.model

/** Whatever the user knows about the person or company they want to look up. */
data class SearchQuery(
    val name: String = "",
    val phone: String = "",
    val company: String = "",
    val address: String = "",
) {
    val isBlank: Boolean
        get() = name.isBlank() && phone.isBlank() && company.isBlank() && address.isBlank()
}
