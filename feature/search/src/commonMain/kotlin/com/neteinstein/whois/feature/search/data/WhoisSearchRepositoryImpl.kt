package com.neteinstein.whois.feature.search.data

import com.neteinstein.whois.feature.search.domain.model.SearchQuery
import com.neteinstein.whois.feature.search.domain.repository.WhoisSearchRepository

private const val BRAVE_SEARCH_BASE_URL = "https://search.brave.com/search"

class WhoisSearchRepositoryImpl : WhoisSearchRepository {

    override fun buildSearchUrl(query: SearchQuery): String {
        val prompt = buildPrompt(query)
        return "$BRAVE_SEARCH_BASE_URL?q=${prompt.percentEncodeForQuery()}"
    }

    private fun buildPrompt(query: SearchQuery): String {
        val details = buildList {
            if (query.name.isNotBlank()) add("name \"${query.name.trim()}\"")
            if (query.phone.isNotBlank()) add("phone number \"${query.phone.trim()}\"")
            if (query.company.isNotBlank()) add("company \"${query.company.trim()}\"")
            if (query.address.isNotBlank()) add("address \"${query.address.trim()}\"")
        }.joinToString(", ")

        return "Who is this person or company? Publicly available information for $details. " +
            "Summarize identity, occupation or business, and any public online presence."
    }
}
