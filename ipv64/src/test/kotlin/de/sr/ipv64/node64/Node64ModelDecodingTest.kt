package de.sr.ipv64.node64

import de.sr.ipv64.internal.IPv64Json
import de.sr.ipv64.node64.model.Check
import de.sr.ipv64.node64.model.DynDnsUpdateNowResult
import de.sr.ipv64.node64.model.DynDnsUpdateOutcome
import de.sr.ipv64.node64.model.Incident
import de.sr.ipv64.node64.model.Link
import de.sr.ipv64.node64.model.Maintenance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Instant

class Node64ModelDecodingTest {

    @Test
    fun `untyped timestamps decode as instants and blank values as null`() {
        val incident = IPv64Json.decodeFromString(
            Incident.serializer(),
            """{"id":1,"started_at":"2026-10-28T10:00:00.123456Z","ended_at":""}""",
        )
        assertEquals(Instant.parse("2026-10-28T10:00:00.123456Z"), incident.startedAt)
        assertNull(incident.endedAt)

        val maintenance = IPv64Json.decodeFromString(
            Maintenance.serializer(),
            """{"start_at":"2026-10-28 10:00:00","end_at":null,"repeat_until":"not a time"}""",
        )
        assertEquals(Instant.parse("2026-10-28T10:00:00Z"), maintenance.startAt)
        assertNull(maintenance.endAt)
        assertNull(maintenance.repeatUntil)

        val check = IPv64Json.decodeFromString(
            Check.serializer(),
            """{"token":"t","state":{"since":"2026-10-28T10:00:00+02:00","down_since":""}}""",
        )
        assertEquals(Instant.parse("2026-10-28T08:00:00Z"), check.state?.since)
        assertNull(check.state?.downSince)
    }

    @Test
    fun `dyndns update now result is decoded`() {
        val result = IPv64Json.decodeFromString(
            DynDnsUpdateNowResult.serializer(),
            """{"result":"sent","target":{"id":7,"fqdn":"home.example.ipv64.net"}}""",
        )
        assertEquals(DynDnsUpdateOutcome.SENT, result.result)
        assertEquals(7L, result.target?.id)
    }

    @Test
    fun `link codes and push tokens are not exposed by toString`() {
        val link = IPv64Json.decodeFromString(Link.serializer(), """{"linked":false,"pending_code":"ABCD-1234"}""")
        assertFalse(link.toString().contains("ABCD-1234"))

        val push = IPv64Json.decodeFromString(
            Check.serializer(),
            """{"token":"secretpushtoken","type":"push","push_url":"http://n/api/push/secretpushtoken"}""",
        )
        assertFalse(push.toString().contains("secretpushtoken"))
    }
}
