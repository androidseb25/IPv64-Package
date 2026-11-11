package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.time.Instant

class Dns64DhcpApiTest : Node64ApiTestBaseB() {

    private val dhcp get() = node().dns64.dhcp

    private val scopeJson = """{"id":2,"name":"Heimnetz","interface":"eth0","subnet":"192.168.1.0/24","range_start":"192.168.1.100",
        "range_end":"192.168.1.199","router":"192.168.1.1","dns":["192.168.1.2"],"domain":"home.lan","lease_hours":24,"ntp":["pool.ntp.org"],
        "authoritative":true,"boot_server":"10.0.0.5","boot_bios":"a.kpxe","boot_uefi":"a.efi","boot_ipxe":"a.ipxe","relay":false,
        "enabled":true,"created_at":"2026-10-20T10:00:00Z"}"""

    private val reservationJson = """{"id":4,"scope_id":2,"mac":"aa:bb:cc:dd:ee:ff","ip":"192.168.1.20","name":"nas","comment":"c"}"""

    private val routerJson = """{"interface":"eth0","source":"fe80::1","router_lifetime":1800,"managed":true,"other":false,
        "prefixes":[{"prefix":"2001:db8::/64","slaac":true,"on_link":true,"valid_lifetime":86400}],"dns":["fe80::2"],"domains":["home.lan"],
        "last_seen":"2026-10-28T09:00:00Z"}"""

    @Test
    fun `get returns free-form object`() = test<Unit> {
        enqueueJson("""{"mode":"on"}""")
        assertEquals(js("""{"mode":"on"}"""), dhcp.get())
        expectRequest("GET", "/api/dns64/dhcp")
    }

    @Test
    fun `setMode puts mode`() = test<Unit> {
        enqueueNoContent()
        dhcp.setMode(DNS64DhcpMode.OBSERVE)
        assertEquals(js("""{"mode":"observe"}"""), expectRequest("PUT", "/api/dns64/dhcp/mode").jsonBody())
    }

    @Test
    fun `setRole puts role`() = test<Unit> {
        enqueueNoContent()
        dhcp.setRole(DNS64DhcpRole.RESERVE)
        assertEquals(js("""{"role":"reserve"}"""), expectRequest("PUT", "/api/dns64/dhcp/role").jsonBody())
    }

    @Test
    fun `listScopes decodes scopes`() = test<Unit> {
        enqueueJson("[$scopeJson]")
        val s = dhcp.listScopes().single()
        expectRequest("GET", "/api/dns64/dhcp/scopes")
        assertEquals(2L, s.id)
        assertEquals("eth0", s.interfaceName)
        assertEquals("192.168.1.0/24", s.subnet)
        assertEquals("192.168.1.100", s.rangeStart)
        assertEquals("192.168.1.199", s.rangeEnd)
        assertEquals(listOf("192.168.1.2"), s.dns)
        assertEquals(24, s.leaseHours)
        assertEquals(listOf("pool.ntp.org"), s.ntp)
        assertEquals(true, s.authoritative)
        assertEquals("a.ipxe", s.bootIpxe)
        assertEquals(false, s.relay)
        assertEquals(true, s.enabled)
        assertEquals(Instant.parse("2026-10-20T10:00:00Z"), s.createdAt)
    }

    @Test
    fun `createScope posts request without null optionals`() = test<Unit> {
        enqueueJson(scopeJson)
        dhcp.createScope(DNS64DHCPScope(interfaceName = "eth0", subnet = "192.168.1.0/24", rangeStart = "192.168.1.100", rangeEnd = "192.168.1.199", leaseHours = 12))
        assertEquals(
            js("""{"interface":"eth0","subnet":"192.168.1.0/24","range_start":"192.168.1.100","range_end":"192.168.1.199","lease_hours":12}"""),
            expectRequest("POST", "/api/dns64/dhcp/scopes").jsonBody(),
        )
    }

    @Test
    fun `updateScope puts to id`() = test<Unit> {
        enqueueJson(scopeJson)
        assertEquals(2L, dhcp.updateScope(2, DNS64DHCPScope(name = "neu")).id)
        assertEquals(js("""{"name":"neu"}"""), expectRequest("PUT", "/api/dns64/dhcp/scopes/2").jsonBody())
    }

    @Test
    fun `deleteScope enableScope disableScope`() = test<Unit> {
        enqueueNoContent(); enqueueNoContent(); enqueueNoContent()
        dhcp.deleteScope(2)
        expectRequest("DELETE", "/api/dns64/dhcp/scopes/2")
        dhcp.enableScope(2)
        expectRequest("POST", "/api/dns64/dhcp/scopes/2/enable")
        dhcp.disableScope(2)
        expectRequest("POST", "/api/dns64/dhcp/scopes/2/disable")
    }

    @Test
    fun `listReservations decodes`() = test<Unit> {
        enqueueJson("[$reservationJson]")
        val r = dhcp.listReservations().single()
        expectRequest("GET", "/api/dns64/dhcp/reservations")
        assertEquals(4L, r.id)
        assertEquals(2L, r.scopeId)
        assertEquals("aa:bb:cc:dd:ee:ff", r.mac)
        assertEquals("192.168.1.20", r.ip)
        assertEquals("nas", r.name)
        assertEquals("c", r.comment)
    }

    @Test
    fun `createReservation posts request`() = test<Unit> {
        enqueueJson(reservationJson)
        assertEquals(4L, dhcp.createReservation(DNS64DHCPReservation(scopeId = 2, mac = "aa:bb:cc:dd:ee:ff", ip = "192.168.1.20", groupId = 7)).id)
        assertEquals(
            js("""{"scope_id":2,"mac":"aa:bb:cc:dd:ee:ff","ip":"192.168.1.20","group_id":7}"""),
            expectRequest("POST", "/api/dns64/dhcp/reservations").jsonBody(),
        )
    }

    @Test
    fun `updateReservation and deleteReservation`() = test<Unit> {
        enqueueJson(reservationJson)
        dhcp.updateReservation(4, DNS64DHCPReservation(scopeId = 2, mac = "aa:bb:cc:dd:ee:ff", ip = "192.168.1.21"))
        assertEquals(
            js("""{"scope_id":2,"mac":"aa:bb:cc:dd:ee:ff","ip":"192.168.1.21"}"""),
            expectRequest("PUT", "/api/dns64/dhcp/reservations/4").jsonBody(),
        )
        enqueueNoContent()
        dhcp.deleteReservation(4)
        expectRequest("DELETE", "/api/dns64/dhcp/reservations/4")
    }

    @Test
    fun `releaseLease deletes with scope and mac`() = test<Unit> {
        enqueueNoContent()
        dhcp.releaseLease(2, "aa:bb:cc:dd:ee:ff")
        expectRequest("DELETE", "/api/dns64/dhcp/leases/2/aa:bb:cc:dd:ee:ff")
    }

    @Test
    fun `getIpv6 decodes state`() = test<Unit> {
        enqueueJson(
            """{"config":{"enabled":true,"interfaces":["eth0"],"domain":"home.lan"},"running":true,"error":"x",
            "status":[{"interface":"eth0","dns":["fe80::2"],"last_sent":"2026-10-28T09:00:00Z","error":"e","error_kind":"no_addr"}],
            "routers":[$routerJson]}""",
        )
        val v = dhcp.getIpv6()
        expectRequest("GET", "/api/dns64/dhcp/ipv6")
        assertEquals(true, v.config?.enabled)
        assertEquals(listOf("eth0"), v.config?.interfaces)
        assertEquals("home.lan", v.config?.domain)
        assertEquals(true, v.running)
        assertEquals("x", v.error)
        val st = v.status.single()
        assertEquals("eth0", st.interfaceName)
        assertEquals(DNS64DhcpIpv6ErrorKind.NO_ADDR, st.errorKind)
        assertEquals(Instant.parse("2026-10-28T09:00:00Z"), st.lastSent)
        val r = v.routers.single()
        assertEquals("fe80::1", r.source)
        assertEquals(1800L, r.routerLifetime)
        assertEquals(true, r.managed)
        assertEquals(false, r.other)
        assertEquals("2001:db8::/64", r.prefixes.single().prefix)
        assertEquals(true, r.prefixes.single().onLink)
        assertEquals(86400L, r.prefixes.single().validLifetime)
        assertEquals(listOf("home.lan"), r.domains)
    }

    @Test
    fun `setIpv6 puts config`() = test<Unit> {
        enqueueNoContent()
        dhcp.setIpv6(DNS64DhcpIpv6Config(enabled = true, interfaces = listOf("eth0"), domain = "home.lan"))
        assertEquals(
            js("""{"enabled":true,"interfaces":["eth0"],"domain":"home.lan"}"""),
            expectRequest("PUT", "/api/dns64/dhcp/ipv6").jsonBody(),
        )
    }

    @Test
    fun `probeIpv6 posts and decodes routers`() = test<Unit> {
        enqueueJson("""{"routers":[$routerJson],"error":"x"}""")
        val p = dhcp.probeIpv6()
        expectRequest("POST", "/api/dns64/dhcp/ipv6/probe")
        assertEquals("fe80::1", p.routers.single().source)
        assertEquals("x", p.error)
    }
}
