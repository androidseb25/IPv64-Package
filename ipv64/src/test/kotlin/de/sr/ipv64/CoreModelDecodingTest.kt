package de.sr.ipv64

import de.sr.ipv64.internal.IPv64Json
import de.sr.ipv64.model.CdnDomain
import de.sr.ipv64.model.CdnPendingTask
import de.sr.ipv64.model.CreateStatusPageRequest
import de.sr.ipv64.model.Integration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CoreModelDecodingTest {

    @Test
    fun `unknown pending task of a cdn domain does not break decoding`() {
        val domain = IPv64Json.decodeFromString(
            CdnDomain.serializer(),
            """{"id":"x","pending":["dns","some_future_task","certificate"]}""",
        )
        assertEquals(listOf(CdnPendingTask.DNS, CdnPendingTask.CERTIFICATE), domain.pending)
        assertEquals(
            """{"id":"x","pending":["dns","certificate"]}""",
            IPv64Json.encodeToString(CdnDomain.serializer(), domain),
        )
    }

    @Test
    fun `secrets are not exposed by toString`() {
        val integration = IPv64Json.decodeFromString(
            Integration.serializer(),
            """{"id":12,"type":"telegram","options":{"apitoken":"secret-token"}}""",
        )
        assertFalse(integration.toString().contains("secret-token"))
        assertFalse(CreateStatusPageRequest(password = "hunter2").toString().contains("hunter2"))
    }
}
