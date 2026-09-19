package com.neteinstein.whois.feature.search

import com.neteinstein.whois.feature.search.data.WhoisSearchRepositoryImpl
import com.neteinstein.whois.feature.search.domain.IncomingShareBus
import com.neteinstein.whois.feature.search.domain.repository.WhoisSearchRepository
import com.neteinstein.whois.feature.search.domain.usecase.ParseSharedContactUseCase
import com.neteinstein.whois.feature.search.domain.usecase.SearchWhoisUseCase
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val searchModule = module {
    single { IncomingShareBus() }
    single<WhoisSearchRepository> { WhoisSearchRepositoryImpl() }
    factory { ParseSharedContactUseCase() }
    factory { SearchWhoisUseCase(repository = get(), urlOpener = get()) }
    viewModel {
        SearchViewModel(
            searchWhoisUseCase = get(),
            parseSharedContactUseCase = get(),
            incomingShareBus = get(),
            navigator = get(),
            dispatchers = get(),
        )
    }
}
