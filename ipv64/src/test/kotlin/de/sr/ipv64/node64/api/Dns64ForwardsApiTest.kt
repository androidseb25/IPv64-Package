package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.time.Instant

class Dns64ForwardsApiTest : Node64ApiTestBaseB() {

    private val forwards get() = node().dns64.forwards

    private val forwardJson = """{"id":3,"domain":"corp.example","servers":["10.0.0.1","tls://10.0.0.2"],"comment":"VPN","enabled":true,
        "created_at":"2026-10-20T10:00:00Z","local_only":true,
        "state":[{"label":"10.0.0.1","url":"10.0.0.1:53","requests":10,"errors":1,"avg_ms":3.5,"healthy":true,"last_error":"x","last_error_at":"2026-10-28T09:00:00Z"}]}"""

    @Test
    fun `list decodes forwards with server state`() = test<Unit> {
        enqueueJson("[$forwardJson]")
        val f = forwards.list().single()
        expectRequest("GET", "/api/dns64/forwards")
        assertEquals(3L, f.id)
        assertEquals("corp.example", f.domain)
        assertEquals(listOf("10.0.0.1", "tls://10.0.0.2"), f.servers)
        assertEquals("VPN", f.comment)
        assertEquals(true, f.enabled)
        assertEquals(Instant.parse("2026-10-20T10:00:00Z"), f.createdAt)
        assertEquals(true, f.localOnly)
        val s = f.state.single()
        assertEquals("10.0.0.1:53", s.url)
        assertEquals(10L, s.requests)
        assertEquals(1L, s.errors)
        assertEquals(3.5, s.avgMs ?: 0.0, 0.0)
        assertEquals(true, s.healthy)
        assertEquals("x", s.lastError)
        assertEquals(Instant.parse("2026-10-28T09:00:00Z"), s.lastErrorAt)
    }

    @Test
    fun `create posts request`() = test<Unit> {
        enqueueJson(forwardJson)
        forwards.create(DNS64ForwardRequest("corp.example", listOf("10.0.0.1"), "VPN", true))
        assertEquals(
            js("""{"domain":"corp.example","servers":["10.0.0.1"],"comment":"VPN","local_only":true}"""),
            expectRequest("POST", "/api/dns64/forwards").jsonBody(),
        )
    }

    @Test
    fun `create omits null optionals`() = test<Unit> {
        enqueueJson(forwardJson)
        forwards.create(DNS64ForwardRequest("192.168.0.0/16", listOf("10.0.0.1")))
        assertEquals(js("""{"domain":"192.168.0.0/16","servers":["10.0.0.1"]}"""), expectRequest("POST", "/api/dns64/forwards").jsonBody())
    }

    @Test
    fun `update puts to id`() = test<Unit> {
        enqueueJson(forwardJson)
        assertEquals(3L, forwards.update(3, DNS64ForwardRequest("corp.example", listOf("10.0.0.9"))).id)
        assertEquals(js("""{"domain":"corp.example","servers":["10.0.0.9"]}"""), expectRequest("PUT", "/api/dns64/forwards/3").jsonBody())
    }

    @Test
    fun `delete`() = test<Unit> {
        enqueueNoContent()
        forwards.delete(3)
        expectRequest("DELETE", "/api/dns64/forwards/3")
    }

    @Test
    fun `enable`() = test<Unit> {
        enqueueNoContent()
        forwards.enable(3)
        expectRequest("POST", "/api/dns64/forwards/3/enable")
    }

    @Test
    fun `disable`() = test<Unit> {
        enqueueNoContent()
        forwards.disable(3)
        expectRequest("POST", "/api/dns64/forwards/3/disable")
    }

    @Test
    fun `test posts and decodes result`() = test<Unit> {
        enqueueJson("""{"ok":false,"rcode":"SERVFAIL","server":"10.0.0.1:53","ms":12.75,"error":"timeout"}""")
        val res = forwards.test(3)
        expectRequest("POST", "/api/dns64/forwards/3/test")
        assertEquals(false, res.ok)
        assertEquals("SERVFAIL", res.rcode)
        assertEquals("10.0.0.1:53", res.server)
        assertEquals(12.75, res.ms ?: 0.0, 0.0)
        assertEquals("timeout", res.error)
    }
}
