package com.neteinstein.whois.feature.search.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class ParseSharedContactUseCaseTest {

    private val useCase = ParseSharedContactUseCase()

    @Test
    fun `extracts name phone company and address from a vCard`() = runTest {
        val vCard = """
            BEGIN:VCARD
            VERSION:3.0
            N:Doe;John;;;
            FN:John Doe
            ORG:Acme Corp;Engineering
            TEL;TYPE=CELL:+1 555 123 4567
            ADR;TYPE=HOME:;;123 Main St;Springfield;IL;62704;USA
            NOTE:Secret PIN is 1234 - never share
            END:VCARD
        """.trimIndent()

        val result = useCase(vCard)

        assertEquals("John Doe", result.name)
        assertEquals("+1 555 123 4567", result.phone)
        assertEquals("Acme Corp", result.company)
        assertEquals("123 Main St, Springfield, IL, 62704, USA", result.address)
    }

    @Test
    fun `never surfaces the NOTE property anywhere in the result`() = runTest {
        val vCard = """
            BEGIN:VCARD
            FN:Jane Roe
            NOTE:This must never leak into a search query
            END:VCARD
        """.trimIndent()

        val result = useCase(vCard)

        assertEquals("Jane Roe", result.name)
        assertEquals("", result.phone)
        assertEquals("", result.company)
        assertEquals("", result.address)
    }

    @Test
    fun `falls back to structured N when FN is absent`() = runTest {
        val vCard = """
            BEGIN:VCARD
            N:Doe;John;;;
            END:VCARD
        """.trimIndent()

        val result = useCase(vCard)

        assertEquals("Doe John", result.name)
    }
}
