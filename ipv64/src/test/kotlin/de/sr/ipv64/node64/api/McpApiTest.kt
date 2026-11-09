package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.CreateMcpKeyRequest
import de.sr.ipv64.node64.model.UpdateMcpRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class McpApiTest : Node64ApiTestBaseA() {

    @Test
    fun `get decodes mcp`() = test {
        enqueueJson(
            """{"enabled":true,"path":"/mcp","rate_per_minute":60,"areas":[{"name":"dns","available":true}],
            "keys":[{"id":2,"name":"k","hint":"abcd","areas":["dns"],"created_at":"2026-09-01T10:00:00Z",
            "last_used_at":"2026-10-01T10:00:00Z","last_ip":"1.2.3.4"}]}""",
        )
        val m = node().mcp.get()
        val r = takeRequest()
        assertEquals("GET", r.method)
        assertEquals("/api/mcp", r.target)
        assertEquals(60, m.ratePerMinute)
        assertEquals("dns", m.areas.single().name)
        assertEquals("1.2.3.4", m.keys.single().lastIp)
    }

    @Test
    fun `update puts enabled`() = test {
        enqueueJson("""{"enabled":true}""")
        assertEquals(true, node().mcp.update(UpdateMcpRequest(true)).enabled)
        val r = takeRequest()
        assertEquals("PUT", r.method)
        assertEquals("/api/mcp", r.target)
        r.assertJsonBody("""{"enabled":true}""")
    }

    @Test
    fun `createKey posts and redacts secret`() = test {
        enqueueJson("""{"id":2,"name":"k","hint":"abcd","areas":["dns"],"key":"mcp64_SECRET"}""")
        val key = node().mcp.createKey(CreateMcpKeyRequest("k", listOf("dns")))
        val r = takeRequest()
        assertEquals("POST", r.method)
        assertEquals("/api/mcp/keys", r.target)
        r.assertJsonBody("""{"name":"k","areas":["dns"]}""")
        assertEquals("mcp64_SECRET", key.key)
        assertFalse(key.toString().contains("mcp64_SECRET"))
        assertTrue(key.toString().contains("key=<redacted>"))
    }

    @Test
    fun `deleteKey sends DELETE`() = test {
        enqueueEmpty(204)
        node().mcp.deleteKey(2)
        val r = takeRequest()
        assertEquals("DELETE", r.method)
        assertEquals("/api/mcp/keys/2", r.target)
    }
}
