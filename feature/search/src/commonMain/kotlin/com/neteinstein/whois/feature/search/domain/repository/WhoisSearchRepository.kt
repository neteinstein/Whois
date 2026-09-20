package com.neteinstein.whois.feature.search.domain.repository

import com.neteinstein.whois.feature.search.domain.model.SearchQuery

interface WhoisSearchRepository {
    /** Builds a Brave Search URL asking, in natural language, who/what [query] describes. */
    fun buildSearchUrl(query: SearchQuery): String
}
