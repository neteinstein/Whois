package com.neteinstein.whois.feature.search.domain.usecase

import com.neteinstein.whois.core.common.UseCase
import com.neteinstein.whois.feature.search.domain.model.SearchQuery

/**
 * Parses the raw vCard text an OS contacts app hands us through a share intent into a
 * [SearchQuery].
 *
 * Security note: the vCard `NOTE` property is *never* read here, on purpose. Contact notes
 * routinely hold sensitive personal context the user jotted down for themselves (medical
 * details, passwords, relationship history); silently forwarding that into a public web search
 * would be a privacy leak, so it is excluded even though parsing it would be trivial.
 */
class ParseSharedContactUseCase : UseCase<String, SearchQuery>() {
    override suspend fun invoke(params: String): SearchQuery {
        var formattedName = ""
        var structuredName = ""
        var phone = ""
        var company = ""
        var address = ""

        params.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { rawLine ->
                val separatorIndex = rawLine.indexOf(':')
                if (separatorIndex <= 0) return@forEach
                val propertyPart = rawLine.substring(0, separatorIndex)
                val value = rawLine.substring(separatorIndex + 1).trim()
                if (value.isEmpty()) return@forEach

                // Property names may carry a "group." prefix (e.g. "item1.TEL") and ";TYPE=..."
                // parameters; only the bare property name before the first ';' matters here.
                val propertyName = propertyPart
                    .substringAfterLast('.')
                    .substringBefore(';')
                    .trim()
                    .uppercase()

                when (propertyName) {
                    "FN" -> if (formattedName.isEmpty()) formattedName = value
                    "N" ->
                        if (structuredName.isEmpty()) {
                            structuredName = value.split(';').filter { it.isNotBlank() }.joinToString(" ")
                        }
                    "ORG" -> if (company.isEmpty()) company = value.substringBefore(';').trim()
                    "TEL" -> if (phone.isEmpty()) phone = value
                    "ADR" -> if (address.isEmpty()) address = formatAddress(value)
                    // NOTE and any other property are intentionally ignored.
                }
            }

        // FN (formatted name) is the vCard's canonical display name and takes priority over the
        // structured N property whenever both are present - regardless of which one appears
        // first in the source text (real vCards, and this class's own test fixture, list N
        // before FN).
        val name = formattedName.ifEmpty { structuredName }

        return SearchQuery(name = name, phone = phone, company = company, address = address)
    }

    /** vCard ADR value is "PO Box;Extended;Street;City;Region;PostalCode;Country". */
    private fun formatAddress(rawAdr: String): String {
        val parts = rawAdr.split(';')
        val street = parts.getOrNull(2).orEmpty()
        val city = parts.getOrNull(3).orEmpty()
        val region = parts.getOrNull(4).orEmpty()
        val postalCode = parts.getOrNull(5).orEmpty()
        val country = parts.getOrNull(6).orEmpty()
        return listOf(street, city, region, postalCode, country)
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }
}
