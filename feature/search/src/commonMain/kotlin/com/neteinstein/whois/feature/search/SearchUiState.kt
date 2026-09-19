package com.neteinstein.whois.feature.search

data class SearchUiState(
    val name: String = "",
    val phone: String = "",
    val company: String = "",
    val address: String = "",
    val showEmptyFieldsError: Boolean = false,
    val sharedContactBannerVisible: Boolean = false,
)
