package com.neteinstein.whois.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neteinstein.whois.core.common.DispatcherProvider
import com.neteinstein.whois.core.ui.navigation.Destination
import com.neteinstein.whois.core.ui.navigation.Navigator
import com.neteinstein.whois.feature.search.domain.IncomingShareBus
import com.neteinstein.whois.feature.search.domain.model.SearchQuery
import com.neteinstein.whois.feature.search.domain.usecase.ParseSharedContactUseCase
import com.neteinstein.whois.feature.search.domain.usecase.SearchWhoisUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchWhoisUseCase: SearchWhoisUseCase,
    private val parseSharedContactUseCase: ParseSharedContactUseCase,
    private val incomingShareBus: IncomingShareBus,
    private val navigator: Navigator,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    init {
        viewModelScope.launch(dispatchers.default) {
            incomingShareBus.sharedContacts.collect { rawVCardText ->
                onSharedContactReceived(rawVCardText)
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, showEmptyFieldsError = false) }

    fun onPhoneChange(value: String) = _uiState.update { it.copy(phone = value, showEmptyFieldsError = false) }

    fun onCompanyChange(value: String) = _uiState.update { it.copy(company = value, showEmptyFieldsError = false) }

    fun onAddressChange(value: String) = _uiState.update { it.copy(address = value, showEmptyFieldsError = false) }

    fun onSearchClicked() {
        viewModelScope.launch(dispatchers.default) {
            performSearch(currentQuery())
        }
    }

    fun onSettingsClicked() {
        navigator.navigate(Destination.Settings)
    }

    private suspend fun onSharedContactReceived(rawVCardText: String) {
        val query = parseSharedContactUseCase(rawVCardText)
        _uiState.update {
            it.copy(
                name = query.name,
                phone = query.phone,
                company = query.company,
                address = query.address,
                sharedContactBannerVisible = true,
            )
        }
        performSearch(query)
    }

    private suspend fun performSearch(query: SearchQuery) {
        val opened = searchWhoisUseCase(query)
        if (!opened) {
            _uiState.update { it.copy(showEmptyFieldsError = true) }
        }
    }

    private fun currentQuery(): SearchQuery = uiState.value.let {
        SearchQuery(name = it.name, phone = it.phone, company = it.company, address = it.address)
    }
}
