package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.*
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.*
import org.junit.Test
import kotlin.time.Instant

class Proxy64ApiTest : Node64ApiTestBaseB() {

    private val proxy get() = node().proxy

    @Test
    fun `status returns free form object`() = test<Unit> {
        enqueueJson("""{"http_port":8080,"hosts":3,"cache":{"entries":4}}""")
        val s = proxy.status()
        expectRequest("GET", "/api/cdn64/status")
        assertEquals("8080", s["http_port"]!!.jsonPrimitive.content)
        assertEquals("4", s["cache"]!!.jsonObject["entries"]!!.jsonPrimitive.content)
    }

    @Test
    fun `check posts and decodes`() = test<Unit> {
        enqueueJson("""{"checking":true}""")
        assertEquals(true, proxy.check().checking)
        expectRequest("POST", "/api/cdn64/check")
    }

    @Test
    fun `defaultPage decodes numeric string enum`() = test<Unit> {
        enqueueJson("""{"default":"404","default_url":"https://x.test","default_html":"<b>hi</b>"}""")
        val p = proxy.defaultPage()
        expectRequest("GET", "/api/cdn64/default")
        assertEquals(DefaultPageMode.NOT_FOUND, p.default)
        assertEquals("https://x.test", p.defaultUrl)
        assertEquals("<b>hi</b>", p.defaultHtml)
    }

    @Test
    fun `updateDefaultPage puts changed fields`() = test<Unit> {
        enqueueJson("""{"default":"redirect","default_url":"https://x.test"}""")
        val p = proxy.updateDefaultPage(UpdateProxyDefaultPageRequest(DefaultPageMode.REDIRECT, defaultUrl = "https://x.test"))
        assertEquals(js("""{"default":"redirect","default_url":"https://x.test"}"""), expectRequest("PUT", "/api/cdn64/default").jsonBody())
        assertEquals(DefaultPageMode.REDIRECT, p.default)
    }

    @Test
    fun `log sends all filters and maps rejected to 1`() = test<Unit> {
        enqueueJson(
            """[{"id":900,"time":"2026-10-28T10:00:00Z","host_id":3,"client":"1.2.3.4","method":"GET","host":"a.example.com","path":"/x?y=1",
            "status":403,"bytes":1234567890123,"ms":15,"user_agent":"curl/8","reason":"blocklist"}]""",
        )
        val l = proxy.log(host = 3, status = "r", q = "curl", before = 1000, limit = 50, rejected = true)
        expectRequest("GET", "/api/cdn64/log?host=3&status=r&q=curl&before=1000&limit=50&rejected=1")
        val e = l.single()
        assertEquals(900L, e.id)
        assertEquals(Instant.parse("2026-10-28T10:00:00Z"), e.time)
        assertEquals(3L, e.hostId)
        assertEquals("/x?y=1", e.path)
        assertEquals(403, e.status)
        assertEquals(1234567890123L, e.bytes)
        assertEquals(15L, e.ms)
        assertEquals("curl/8", e.userAgent)
        assertEquals("blocklist", e.reason)
    }

    @Test
    fun `log rejected false is not sent`() = test<Unit> {
        enqueueJson("[]")
        proxy.log(rejected = false)
        expectRequest("GET", "/api/cdn64/log")
    }

    @Test
    fun `stats sends hours and host`() = test<Unit> {
        enqueueJson("""{"hosts":[{"host_id":3,"requests":10}],"timeline":[]}""")
        val s = proxy.stats(24, 3)
        expectRequest("GET", "/api/cdn64/stats?hours=24&host=3")
        assertEquals(1, s["hosts"]!!.toString().count { it == '{' })
    }

    @Test
    fun `settings decodes`() = test<Unit> {
        enqueueJson("""{"log_days":14,"anonymize":true}""")
        val s = proxy.settings()
        expectRequest("GET", "/api/cdn64/settings")
        assertEquals(14, s.logDays)
        assertEquals(true, s.anonymize)
    }

    @Test
    fun `updateSettings puts`() = test<Unit> {
        enqueueJson("""{"log_days":7,"anonymize":false}""")
        proxy.updateSettings(UpdateProxySettingsRequest(logDays = 7))
        assertEquals(js("""{"log_days":7}"""), expectRequest("PUT", "/api/cdn64/settings").jsonBody())
    }

    private val blocklistsJson = """{"lists":[{"id":"spam","name":"Spam","category":"abuse","text":"t","license":"CC0","source":"https://s",
        "enabled":true,"entries":123456,"updated":"2026-10-28T01:00:00Z","changed":"2026-10-27T01:00:00Z","error":"","edition":"daily",
        "live_denied":true,"live_error":"denied"}],
        "allow":["1.2.3.4"],"blocked":99,"active":true,
        "countries":[{"code":"DE","entries":5000,"updated":"2026-10-28T01:00:00Z","error":""}],
        "geo_blocked":7,"token_set":true,
        "ai_bots":{"needed":true,"entries":10,"users":2,"updated":"2026-10-28T01:00:00Z","changed":"2026-10-28T02:00:00Z","error":"","blocked":4}}"""

    private fun assertBlocklists(b: CDNBlocklists) {
        val l = b.lists.single()
        assertEquals("spam", l.id)
        assertEquals("CC0", l.license)
        assertEquals(123456L, l.entries)
        assertEquals(Instant.parse("2026-10-28T01:00:00Z"), l.updated)
        assertEquals(Instant.parse("2026-10-27T01:00:00Z"), l.changed)
        assertEquals(BlocklistEdition.DAILY, l.edition)
        assertEquals(true, l.liveDenied)
        assertEquals("denied", l.liveError)
        assertEquals(listOf("1.2.3.4"), b.allow)
        assertEquals(99L, b.blocked)
        assertEquals("DE", b.countries.single().code)
        assertEquals(5000L, b.countries.single().entries)
        assertEquals(7L, b.geoBlocked)
        assertEquals(true, b.tokenSet)
        assertEquals(4L, b.aiBots?.blocked)
        assertEquals(2L, b.aiBots?.users)
    }

    @Test
    fun `blocklists decodes`() = test<Unit> {
        enqueueJson(blocklistsJson)
        assertBlocklists(proxy.blocklists())
        expectRequest("GET", "/api/cdn64/blocklists")
    }

    @Test
    fun `updateBlocklists puts and token redacted in toString`() = test<Unit> {
        enqueueJson(blocklistsJson)
        val req = UpdateBlocklistsRequest(enabled = listOf("spam"), allow = listOf("1.2.3.4"), token = "s3cr3t-token")
        assertBlocklists(proxy.updateBlocklists(req))
        assertEquals(
            js("""{"enabled":["spam"],"allow":["1.2.3.4"],"token":"s3cr3t-token"}"""),
            expectRequest("PUT", "/api/cdn64/blocklists").jsonBody(),
        )
        assertFalse(req.toString().contains("s3cr3t-token"))
        assertTrue(req.toString().contains("<redacted>"))
        assertTrue(req.toString().contains("spam"))
    }

    @Test
    fun `refreshBlocklists posts`() = test<Unit> {
        enqueueJson(blocklistsJson)
        assertBlocklists(proxy.refreshBlocklists())
        expectRequest("POST", "/api/cdn64/blocklists/refresh")
    }

    private val honeypotJson = """{"settings":{"decoy":true,"access":false,"block":true,"block_hours":12},
        "decoy_available":true,"access_available":false,
        "blocks":[{"ip":"2001:db8::1","reason":"scan","since":"2026-10-28T08:00:00Z","until":"2026-10-28T20:00:00Z"}],
        "rejected":5,"decoyed":6,"reported":7}"""

    private fun assertHoneypot(h: CDNHoneypotView) {
        assertEquals(true, h.settings?.decoy)
        assertEquals(false, h.settings?.access)
        assertEquals(12, h.settings?.blockHours)
        assertEquals(true, h.decoyAvailable)
        assertEquals(false, h.accessAvailable)
        val b = h.blocks.single()
        assertEquals("2001:db8::1", b.ip)
        assertEquals("scan", b.reason)
        assertEquals(Instant.parse("2026-10-28T08:00:00Z"), b.since)
        assertEquals(Instant.parse("2026-10-28T20:00:00Z"), b.until)
        assertEquals(5L, h.rejected)
        assertEquals(6L, h.decoyed)
        assertEquals(7L, h.reported)
    }

    @Test
    fun `honeypot decodes`() = test<Unit> {
        enqueueJson(honeypotJson)
        assertHoneypot(proxy.honeypot())
        expectRequest("GET", "/api/cdn64/honeypot")
    }

    @Test
    fun `updateHoneypot puts`() = test<Unit> {
        enqueueJson(honeypotJson)
        assertHoneypot(proxy.updateHoneypot(CDNHoneypotSettingsRequest(decoy = true, block = false, blockHours = 6)))
        assertEquals(js("""{"decoy":true,"block":false,"block_hours":6}"""), expectRequest("PUT", "/api/cdn64/honeypot").jsonBody())
    }

    @Test
    fun `removeHoneypotBlock deletes ipv4`() = test<Unit> {
        enqueueNoContent()
        proxy.removeHoneypotBlock("1.2.3.4")
        expectRequest("DELETE", "/api/cdn64/honeypot/blocks/1.2.3.4")
    }

    @Test
    fun `removeHoneypotBlock keeps ipv6 as a single path segment`() = test<Unit> {
        enqueueNoContent()
        proxy.removeHoneypotBlock("2001:db8::1")
        val r = takeRequest()
        assertEquals("DELETE", r.method)
        assertTrue(r.target, r.target == "/api/cdn64/honeypot/blocks/2001:db8::1" || r.target == "/api/cdn64/honeypot/blocks/2001%3Adb8%3A%3A1")
    }

    private val limitsJson = """{"limits":{"enabled":true,"conns_per_ip":20,"conn_rate":10,"req_rate":50,"req_burst":100,"max_conns":5000,
        "target_conns":200,"ban_after":10,"ban_minutes":30,"exempt_lan":true},
        "defaults":{"enabled":false,"conns_per_ip":10},
        "bans":[{"ip":"2001:db8:1:2::/64","until":"2026-10-28T12:00:00Z"}],"rejected":3,"conns":44}"""

    private fun assertLimits(v: CDNLimitsView) {
        val l = v.limits!!
        assertEquals(true, l.enabled)
        assertEquals(20, l.connsPerIp)
        assertEquals(10, l.connRate)
        assertEquals(50, l.reqRate)
        assertEquals(100, l.reqBurst)
        assertEquals(5000, l.maxConns)
        assertEquals(200, l.targetConns)
        assertEquals(10, l.banAfter)
        assertEquals(30, l.banMinutes)
        assertEquals(true, l.exemptLan)
        assertEquals(10, v.defaults?.connsPerIp)
        assertEquals("2001:db8:1:2::/64", v.bans.single().ip)
        assertEquals(Instant.parse("2026-10-28T12:00:00Z"), v.bans.single().until)
        assertEquals(3L, v.rejected)
        assertEquals(44L, v.conns)
    }

    @Test
    fun `limits decodes`() = test<Unit> {
        enqueueJson(limitsJson)
        assertLimits(proxy.limits())
        expectRequest("GET", "/api/cdn64/limits")
    }

    @Test
    fun `updateLimits puts snake_case body`() = test<Unit> {
        enqueueJson(limitsJson)
        assertLimits(proxy.updateLimits(CDNLimitsRequest(enabled = true, connsPerIp = 20, reqBurst = 100, banMinutes = 30, exemptLan = false)))
        assertEquals(
            js("""{"enabled":true,"conns_per_ip":20,"req_burst":100,"ban_minutes":30,"exempt_lan":false}"""),
            expectRequest("PUT", "/api/cdn64/limits").jsonBody(),
        )
    }

    @Test
    fun `removeLimitBan deletes ipv4`() = test<Unit> {
        enqueueNoContent()
        proxy.removeLimitBan("1.2.3.4")
        expectRequest("DELETE", "/api/cdn64/limits/bans/1.2.3.4")
    }

    @Test
    fun `removeLimitBan encodes ipv6 prefix slash inside one segment`() = test<Unit> {
        enqueueNoContent()
        proxy.removeLimitBan("2001:db8:1:2::/64")
        val r = takeRequest()
        assertEquals("DELETE", r.method)
        assertTrue(r.target, r.target.startsWith("/api/cdn64/limits/bans/2001"))
        assertFalse(r.target, r.target.removePrefix("/api/cdn64/limits/bans/").contains('/'))
    }

    private val onlineJson = """{"configured":true,"tunnel":"tn_abc123",
        "edges":[{"name":"srv07","host":"srv07.ipv64.net","connected":true,"transport":"quic","total":12345}],
        "api_ready":true,
        "names":[{"name":"a.example.com","host_id":3,"remote_id":99,"mode":1,"check":"hc_1","created_at":"2026-10-20T10:00:00Z"}],
        "quota":{"domains":{"used":2,"old_cdn":1,"limit":10},"traffic_mb":{"used":12.5,"old_cdn":1.5,"limit":10240},"bandwidth_mbit":0},
        "synced":"2026-10-28T11:00:00Z","sync_error":"","udp_buffer":2097152,"udp_buffer_low":true}"""

    private fun assertOnline(o: CDNOnlineStatus) {
        assertEquals(true, o.configured)
        assertEquals("tn_abc123", o.tunnel)
        val e = o.edges.single()
        assertEquals("srv07.ipv64.net", e.host)
        assertEquals(true, e.connected)
        assertEquals("quic", e.transport)
        assertEquals(12345, e.total)
        assertEquals(true, o.apiReady)
        val n = o.names.single()
        assertEquals(3L, n.hostId)
        assertEquals(99L, n.remoteId)
        assertEquals(1, n.mode)
        assertEquals("hc_1", n.check)
        assertEquals(Instant.parse("2026-10-20T10:00:00Z"), n.createdAt)
        assertEquals(2, o.quota?.domains?.used)
        assertEquals(10, o.quota?.domains?.limit)
        assertEquals(12.5, o.quota?.trafficMb?.used ?: 0.0, 0.0)
        assertEquals(10240L, o.quota?.trafficMb?.limit)
        assertEquals(0, o.quota?.bandwidthMbit)
        assertEquals(Instant.parse("2026-10-28T11:00:00Z"), o.synced)
        assertEquals(2097152L, o.udpBuffer)
        assertEquals(true, o.udpBufferLow)
    }

    @Test
    fun `online decodes`() = test<Unit> {
        enqueueJson(onlineJson)
        assertOnline(proxy.online())
        expectRequest("GET", "/api/cdn64/online")
    }

    @Test
    fun `setOnlineTunnel puts request and redacts token`() = test<Unit> {
        enqueueJson(onlineJson)
        val req = SetOnlineTunnelRequest("tn_abc123", "tunnel-secret-token", listOf(CDNOnlineEdgeInput("srv07", "srv07.ipv64.net", 443, listOf("1.2.3.4"))))
        assertOnline(proxy.setOnlineTunnel(req))
        assertEquals(
            js("""{"tunnel":"tn_abc123","token":"tunnel-secret-token","edges":[{"name":"srv07","host":"srv07.ipv64.net","port":443,"ips":["1.2.3.4"]}]}"""),
            expectRequest("PUT", "/api/cdn64/online").jsonBody(),
        )
        assertFalse(req.toString().contains("tunnel-secret-token"))
        assertTrue(req.toString().contains("<redacted>"))
        assertTrue(req.toString().contains("tn_abc123"))
    }

    @Test
    fun `removeOnline`() = test<Unit> {
        enqueueNoContent()
        proxy.removeOnline()
        expectRequest("DELETE", "/api/cdn64/online")
    }

    @Test
    fun `registerOnlineTunnel posts`() = test<Unit> {
        enqueueJson(onlineJson)
        assertOnline(proxy.registerOnlineTunnel())
        expectRequest("POST", "/api/cdn64/online/tunnel")
    }
}
