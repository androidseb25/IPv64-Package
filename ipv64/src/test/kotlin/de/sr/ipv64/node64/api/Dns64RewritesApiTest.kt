package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.time.Instant

class Dns64RewritesApiTest : Node64ApiTestBaseB() {

    private val rewrites get() = node().dns64.rewrites

    private val json = """{"id":5,"name":"*.example.com","target":"192.168.1.5","comment":"test","enabled":false,"created_at":"2026-10-20T10:00:00Z"}"""

    private fun assertRewrite(r: DNS64Rewrite) {
        assertEquals(5L, r.id)
        assertEquals("*.example.com", r.name)
        assertEquals("192.168.1.5", r.target)
        assertEquals("test", r.comment)
        assertEquals(false, r.enabled)
        assertEquals(Instant.parse("2026-10-20T10:00:00Z"), r.createdAt)
    }

    @Test
    fun `list`() = test<Unit> {
        enqueueJson("[$json,{\"id\":6,\"name\":\"a\",\"target\":\"b\"}]")
        val l = rewrites.list()
        expectRequest("GET", "/api/dns64/rewrites")
        assertRewrite(l[0])
        assertEquals("b", l[1].target)
        assertNull(l[1].createdAt)
    }

    @Test
    fun `create`() = test<Unit> {
        enqueueJson(json)
        assertRewrite(rewrites.create(DNS64RewriteRequest("*.example.com", "192.168.1.5", "test")))
        assertEquals(
            js("""{"name":"*.example.com","target":"192.168.1.5","comment":"test"}"""),
            expectRequest("POST", "/api/dns64/rewrites").jsonBody(),
        )
    }

    @Test
    fun `create omits null comment`() = test<Unit> {
        enqueueJson(json)
        rewrites.create(DNS64RewriteRequest("a", "b"))
        assertEquals(js("""{"name":"a","target":"b"}"""), expectRequest("POST", "/api/dns64/rewrites").jsonBody())
    }

    @Test
    fun `update`() = test<Unit> {
        enqueueJson(json)
        assertRewrite(rewrites.update(5, DNS64RewriteRequest("a", "c")))
        assertEquals(js("""{"name":"a","target":"c"}"""), expectRequest("PUT", "/api/dns64/rewrites/5").jsonBody())
    }

    @Test
    fun `delete`() = test<Unit> {
        enqueueNoContent()
        rewrites.delete(5)
        expectRequest("DELETE", "/api/dns64/rewrites/5")
    }

    @Test
    fun `enable`() = test<Unit> {
        enqueueNoContent()
        rewrites.enable(5)
        expectRequest("POST", "/api/dns64/rewrites/5/enable")
    }

    @Test
    fun `disable`() = test<Unit> {
        enqueueNoContent()
        rewrites.disable(5)
        expectRequest("POST", "/api/dns64/rewrites/5/disable")
    }
}
