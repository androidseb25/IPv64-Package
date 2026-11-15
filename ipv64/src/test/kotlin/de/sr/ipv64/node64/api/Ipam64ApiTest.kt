package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.Ipam64AddressInput
import de.sr.ipv64.node64.model.Ipam64AddressRole
import de.sr.ipv64.node64.model.Ipam64AddressStatus
import de.sr.ipv64.node64.model.Ipam64AllocateInput
import de.sr.ipv64.node64.model.Ipam64ConflictKind
import de.sr.ipv64.node64.model.Ipam64EntryKind
import de.sr.ipv64.node64.model.Ipam64FindingKind
import de.sr.ipv64.node64.model.Ipam64PrefixInput
import de.sr.ipv64.node64.model.Ipam64PrefixStatus
import de.sr.ipv64.node64.model.Ipam64RangeInput
import de.sr.ipv64.node64.model.Ipam64RangeKind
import de.sr.ipv64.node64.model.Ipam64ScanInput
import de.sr.ipv64.node64.model.Ipam64ScanMode
import org.junit.Assert.assertEquals
import org.junit.Test

class Ipam64ApiTest : Node64ApiTestBaseB() {

    private val addressJson = """{"uid":"a1","released":false,"dns":true,"dns_zone":"lan","ptr":true,"dhcp":false,
        "updated":"2026-11-15T10:00:00Z","updated_by":"admin","address":"192.168.1.10","status":"active","role":"gateway",
        "name":"gw","mac":"aa:bb","description":"d","tags":["x"]}"""

    @Test
    fun `prefixes decodes tree`() = test<Unit> {
        enqueueJson("""{"prefixes":[{"prefix":"192.168.1.0/24","status":"active","scan":"passive","uid":"p1","family":4,
            "dhcp":true,"interface":"eth0","pool_start":"192.168.1.100","size":254,"used":3,"used_pct":1.2,"mark_full":false}]}""")
        val list = node().ipam64.prefixes()
        expectRequest("GET", "/api/ipam64/prefixes")
        val net = list.prefixes.single()
        assertEquals(Ipam64PrefixStatus.ACTIVE, net.status)
        assertEquals(Ipam64ScanMode.PASSIVE, net.scan)
        assertEquals("eth0", net.networkInterface)
        assertEquals(254L, net.size)
        assertEquals(1.2, net.usedPct!!, 0.0)
    }

    @Test
    fun `create update delete prefix`() = test<Unit> {
        val json = """{"uid":"p1","family":4,"prefix":"10.0.0.0/8","status":"container","updated":"2026-11-15T10:00:00Z"}"""
        enqueueJson(json, 201)
        val created = node().ipam64.createPrefix(
            Ipam64PrefixInput("10.0.0.0/8", status = Ipam64PrefixStatus.CONTAINER, markFull = true, vlan = 5),
        )
        val post = expectRequest("POST", "/api/ipam64/prefixes")
        assertEquals(js("""{"prefix":"10.0.0.0/8","status":"container","mark_full":true,"vlan":5}"""), post.jsonBody())
        assertEquals("p1", created.uid)
        assertEquals(Ipam64PrefixStatus.CONTAINER, created.status)

        enqueueJson(json)
        node().ipam64.updatePrefix("p1", Ipam64PrefixInput("10.0.0.0/8", dynamic = false))
        val put = expectRequest("PUT", "/api/ipam64/prefixes/p1")
        assertEquals(js("""{"prefix":"10.0.0.0/8","dynamic":false}"""), put.jsonBody())

        enqueueNoContent()
        node().ipam64.deletePrefix("p1")
        expectRequest("DELETE", "/api/ipam64/prefixes/p1")
    }

    @Test
    fun `addresses sends query and decodes page`() = test<Unit> {
        enqueueJson("""{"addresses":[{"prefix":"192.168.1.0/24","ip":"192.168.1.10","kind":"fixed","sources":["ipam","dns"],
            "state":"online","conflict":"mac","macs":["aa"],"last_seen":"2026-11-15T10:00:00Z","vendor":"random"}],
            "total":1,"offset":0,"limit":50}""")
        val page = node().ipam64.addresses(prefix = "192.168.1.0/24", q = "gw", limit = 50, offset = 0)
        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/ipam64/addresses", request.url.encodedPath)
        assertEquals("192.168.1.0/24", request.url.queryParameter("prefix"))
        assertEquals("gw", request.url.queryParameter("q"))
        assertEquals("50", request.url.queryParameter("limit"))
        assertEquals("0", request.url.queryParameter("offset"))
        val entry = page.addresses.single()
        assertEquals(Ipam64EntryKind.FIXED, entry.kind)
        assertEquals(listOf("ipam", "dns"), entry.sources)
        assertEquals(Ipam64ConflictKind.MAC, entry.conflict)
        assertEquals(1, page.total)
    }

    @Test
    fun `create update delete address`() = test<Unit> {
        enqueueJson(addressJson, 201)
        val created = node().ipam64.createAddress(
            Ipam64AddressInput("192.168.1.10", status = Ipam64AddressStatus.ACTIVE, role = Ipam64AddressRole.NONE, dns = true),
        )
        val post = expectRequest("POST", "/api/ipam64/addresses")
        assertEquals(js("""{"address":"192.168.1.10","status":"active","role":"","dns":true}"""), post.jsonBody())
        assertEquals(Ipam64AddressRole.GATEWAY, created.role)
        assertEquals("lan", created.dnsZone)

        enqueueJson(addressJson)
        node().ipam64.updateAddress("a1", Ipam64AddressInput("192.168.1.10", ptr = true))
        val put = expectRequest("PUT", "/api/ipam64/addresses/a1")
        assertEquals(js("""{"address":"192.168.1.10","ptr":true}"""), put.jsonBody())

        enqueueNoContent()
        node().ipam64.deleteAddress("a1")
        expectRequest("DELETE", "/api/ipam64/addresses/a1")
    }

    @Test
    fun `available and allocate`() = test<Unit> {
        enqueueJson("""{"prefix":"192.168.1.0/24","available":["192.168.1.2","192.168.1.3"]}""")
        val free = node().ipam64.available("192.168.1.0/24", range = "r1", count = 2)
        val request = takeRequest()
        assertEquals("/api/ipam64/available", request.url.encodedPath)
        assertEquals("192.168.1.0/24", request.url.queryParameter("prefix"))
        assertEquals("r1", request.url.queryParameter("range"))
        assertEquals("2", request.url.queryParameter("count"))
        assertEquals(listOf("192.168.1.2", "192.168.1.3"), free.available)

        enqueueJson(addressJson, 201)
        val address = node().ipam64.allocate(Ipam64AllocateInput("192.168.1.0/24", name = "host", dhcp = false))
        val post = expectRequest("POST", "/api/ipam64/available")
        assertEquals(js("""{"prefix":"192.168.1.0/24","name":"host","dhcp":false}"""), post.jsonBody())
        assertEquals("a1", address.uid)
    }

    @Test
    fun `conflicts decodes findings and unknown devices`() = test<Unit> {
        enqueueJson("""{"findings":[{"kind":"double","severity":"conflict","ip":"10.0.0.5","prefix":"10.0.0.0/24","macs":["a","b"]}],
            "unknown":[{"ip":"10.0.0.9","prefix":"10.0.0.0/24","mac":"cc","vendor":"V","first_seen":"2026-11-15T10:00:00Z"}]}""")
        val result = node().ipam64.conflicts()
        expectRequest("GET", "/api/ipam64/conflicts")
        assertEquals(Ipam64FindingKind.DOUBLE, result.findings.single().kind)
        assertEquals(listOf("a", "b"), result.findings.single().macs)
        assertEquals("V", result.unknown.single().vendor)
    }

    @Test
    fun `ranges crud`() = test<Unit> {
        val json = """{"uid":"r1","start":"10.0.0.10","end":"10.0.0.20","kind":"reserved","name":"n"}"""
        enqueueJson("""{"ranges":[$json]}""")
        assertEquals(Ipam64RangeKind.RESERVED, node().ipam64.ranges().ranges.single().kind)
        expectRequest("GET", "/api/ipam64/ranges")

        enqueueJson(json, 201)
        node().ipam64.createRange(Ipam64RangeInput("10.0.0.10", "10.0.0.20", kind = Ipam64RangeKind.RESERVED))
        val post = expectRequest("POST", "/api/ipam64/ranges")
        assertEquals(js("""{"start":"10.0.0.10","end":"10.0.0.20","kind":"reserved"}"""), post.jsonBody())

        enqueueJson(json)
        val updated = node().ipam64.updateRange("r1", Ipam64RangeInput("10.0.0.10", "10.0.0.20", name = "n"))
        val put = expectRequest("PUT", "/api/ipam64/ranges/r1")
        assertEquals(js("""{"start":"10.0.0.10","end":"10.0.0.20","name":"n"}"""), put.jsonBody())
        assertEquals("r1", updated.uid)

        enqueueNoContent()
        node().ipam64.deleteRange("r1")
        expectRequest("DELETE", "/api/ipam64/ranges/r1")
    }

    @Test
    fun `scan state and start`() = test<Unit> {
        enqueueJson("""{"running":true,"prefix":"192.168.1.0/24","started":"2026-11-15T10:00:00Z","found":3}""")
        val state = node().ipam64.scan()
        expectRequest("GET", "/api/ipam64/scan")
        assertEquals(true, state.running)
        assertEquals(3, state.found)

        enqueueJson("""{"running":true,"prefix":"192.168.1.0/24"}""", 202)
        node().ipam64.startScan(Ipam64ScanInput("192.168.1.0/24"))
        val post = expectRequest("POST", "/api/ipam64/scan")
        assertEquals(js("""{"prefix":"192.168.1.0/24"}"""), post.jsonBody())
    }
}
