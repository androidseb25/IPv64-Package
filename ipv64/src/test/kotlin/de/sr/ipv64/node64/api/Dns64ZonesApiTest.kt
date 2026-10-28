package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.time.Instant

class Dns64ZonesApiTest : Node64ApiTestBaseB() {

    private val zones get() = node().dns64.zones

    private val json = """{"id":2,"name":"home.lan","authoritative":true,"devices":false,"comment":"LAN","enabled":true,
        "created_at":"2026-10-20T10:00:00Z","ttl":300,"refresh":3600,"retry":600,"expire":86400,"minimum":60,
        "hostmaster":"admin.home.lan","dynamic":true,"tsig_name":"key1"}"""

    private fun assertZone(z: DNS64Zone) {
        assertEquals(2L, z.id)
        assertEquals("home.lan", z.name)
        assertEquals(true, z.authoritative)
        assertEquals(false, z.devices)
        assertEquals("LAN", z.comment)
        assertEquals(true, z.enabled)
        assertEquals(Instant.parse("2026-10-20T10:00:00Z"), z.createdAt)
        assertEquals(300, z.ttl)
        assertEquals(3600, z.refresh)
        assertEquals(600, z.retry)
        assertEquals(86400, z.expire)
        assertEquals(60, z.minimum)
        assertEquals("admin.home.lan", z.hostmaster)
        assertEquals(true, z.dynamic)
        assertEquals("key1", z.tsigName)
    }

    @Test
    fun `list`() = test<Unit> {
        enqueueJson("[$json]")
        assertZone(zones.list().single())
        expectRequest("GET", "/api/dns64/zones")
    }

    @Test
    fun `create posts all fields`() = test<Unit> {
        enqueueJson(json)
        assertZone(zones.create(DNS64ZoneRequest("home.lan", true, false, "LAN", 300, 3600, 600, 86400, 60, "admin.home.lan")))
        assertEquals(
            js("""{"name":"home.lan","authoritative":true,"devices":false,"comment":"LAN","ttl":300,"refresh":3600,"retry":600,
                "expire":86400,"minimum":60,"hostmaster":"admin.home.lan"}"""),
            expectRequest("POST", "/api/dns64/zones").jsonBody(),
        )
    }

    @Test
    fun `create omits null optionals`() = test<Unit> {
        enqueueJson(json)
        zones.create(DNS64ZoneRequest("192.168.1.0/24"))
        assertEquals(js("""{"name":"192.168.1.0/24"}"""), expectRequest("POST", "/api/dns64/zones").jsonBody())
    }

    @Test
    fun `update`() = test<Unit> {
        enqueueJson(json)
        assertZone(zones.update(2, DNS64ZoneRequest("home.lan", ttl = 60)))
        assertEquals(js("""{"name":"home.lan","ttl":60}"""), expectRequest("PUT", "/api/dns64/zones/2").jsonBody())
    }

    @Test
    fun `delete`() = test<Unit> {
        enqueueNoContent()
        zones.delete(2)
        expectRequest("DELETE", "/api/dns64/zones/2")
    }

    @Test
    fun `enable`() = test<Unit> {
        enqueueNoContent()
        zones.enable(2)
        expectRequest("POST", "/api/dns64/zones/2/enable")
    }

    @Test
    fun `disable`() = test<Unit> {
        enqueueNoContent()
        zones.disable(2)
        expectRequest("POST", "/api/dns64/zones/2/disable")
    }

    @Test
    fun `zonefile returns text and sends Accept text plain`() = test<Unit> {
        val zone = "\$ORIGIN home.lan.\n@ IN SOA ns.home.lan. admin.home.lan. (1 3600 600 86400 60)\nnas IN A 192.168.1.10\n"
        enqueueText(zone)
        val text = zones.zonefile(2)
        val r = expectRequest("GET", "/api/dns64/zones/2/zonefile")
        assertEquals("text/plain", r.headers["Accept"])
        assertEquals(zone, text)
    }
}
