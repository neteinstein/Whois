package com.neteinstein.whois.feature.search.domain.usecase

import com.neteinstein.whois.core.common.UrlOpener
import com.neteinstein.whois.core.common.UseCase
import com.neteinstein.whois.feature.search.domain.model.SearchQuery
import com.neteinstein.whois.feature.search.domain.repository.WhoisSearchRepository

/**
 * Builds the Brave Search URL for [SearchQuery] and opens it. Returns `false` (without opening
 * anything) when every field is blank, so the caller can surface a validation message instead.
 */
class SearchWhoisUseCase(private val repository: WhoisSearchRepository, private val urlOpener: UrlOpener) :
    UseCase<SearchQuery, Boolean> {
    override suspend fun invoke(params: SearchQuery): Boolean {
        if (params.isBlank) return false
        urlOpener.open(repository.buildSearchUrl(params))
        return true
    }
}
