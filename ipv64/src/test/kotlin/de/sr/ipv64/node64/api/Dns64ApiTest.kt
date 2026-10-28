package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.time.Instant

class Dns64ApiTest : Node64ApiTestBaseB() {

    private val dns get() = node().dns64

    @Test
    fun `status decodes nested blocking and instants`() = test<Unit> {
        enqueueJson(
            """{"version":"1.4.2","listen":"0.0.0.0:53","startedAt":"2026-10-01T08:00:00Z","router":"192.168.1.1",
            "routerSource":"auto","retentionDays":30,
            "blocking":{"paused":true,"until_resumed":false,"paused_until":"2026-10-28T12:30:00+02:00"}}""",
        )
        val s = dns.status()
        val r = expectRequest("GET", "/api/dns64/status")
        assertEquals("Bearer node64_test", r.headers["Authorization"])
        assertEquals("1.4.2", s.version)
        assertEquals("0.0.0.0:53", s.listen)
        assertEquals(Instant.parse("2026-10-01T08:00:00Z"), s.startedAt)
        assertEquals("auto", s.routerSource)
        assertEquals(30, s.retentionDays)
        assertEquals(true, s.blocking?.paused)
        assertEquals(false, s.blocking?.untilResumed)
        assertEquals(Instant.parse("2026-10-28T10:30:00Z"), s.blocking?.pausedUntil)
    }

    @Test
    fun `summary sends range and decodes camelCase figures`() = test<Unit> {
        enqueueJson(
            """{"summary":{"total":123456789012,"blocked":42,"cached":7,"clients":5,"avgUpstreamMs":12.5,"droppedLogEntries":3},
            "blocklistSize":900000,"listsLoadedAt":"2026-10-28T01:00:00Z",
            "cache":{"entries":10,"max":4096,"staleEntries":1,"hits":99,"misses":11,"staleServed":2,"refreshed":3,"refreshFailures":0},
            "uptimeSeconds":86400,"version":"1.4.2"}""",
        )
        val s = dns.summary(DNS64Range.DAYS_7)
        expectRequest("GET", "/api/dns64/summary?range=7d")
        assertEquals(123456789012L, s.summary?.total)
        assertEquals(12.5, s.summary?.avgUpstreamMs ?: 0.0, 0.0)
        assertEquals(3L, s.summary?.droppedLogEntries)
        assertEquals(900000L, s.blocklistSize)
        assertEquals(Instant.parse("2026-10-28T01:00:00Z"), s.listsLoadedAt)
        assertEquals(4096L, s.cache?.max)
        assertEquals(2L, s.cache?.staleServed)
        assertEquals(86400L, s.uptimeSeconds)
    }

    @Test
    fun `summary without range sends no query`() = test<Unit> {
        enqueueJson("{}")
        dns.summary()
        expectRequest("GET", "/api/dns64/summary")
    }

    @Test
    fun `timeline decodes unix millisecond buckets`() = test<Unit> {
        enqueueJson("""{"stepMs":300000,"buckets":[{"t":1761600000000,"total":10,"blocked":2},{"t":1761600300000,"total":8,"blocked":0}]}""")
        val t = dns.timeline(DNS64Range.HOUR_1)
        expectRequest("GET", "/api/dns64/timeline?range=1h")
        assertEquals(300000L, t.stepMs)
        assertEquals(2, t.buckets.size)
        assertEquals(1761600000000L, t.buckets[0].t)
        assertEquals(2L, t.buckets[0].blocked)
        assertEquals(8L, t.buckets[1].total)
    }

    @Test
    fun `top sends kind range and limit`() = test<Unit> {
        enqueueJson("""[{"name":"ads.example.com","count":120,"blocked":120},{"name":"192.168.1.20","hostname":"laptop","count":50,"blocked":3}]""")
        val items = dns.top(DNS64TopKind.CLIENTS, DNS64Range.HOURS_24, 10)
        expectRequest("GET", "/api/dns64/top?kind=clients&range=24h&limit=10")
        assertEquals(2, items.size)
        assertEquals("ads.example.com", items[0].name)
        assertNull(items[0].hostname)
        assertEquals("laptop", items[1].hostname)
        assertEquals(3L, items[1].blocked)
    }

    @Test
    fun `top with only kind`() = test<Unit> {
        enqueueJson("[]")
        assertTrue(dns.top(DNS64TopKind.BLOCKED).isEmpty())
        expectRequest("GET", "/api/dns64/top?kind=blocked")
    }

    @Test
    fun `log sends all filters and decodes rows`() = test<Unit> {
        enqueueJson(
            """{"rows":[{"t":1761600123456,"client":"192.168.1.20","clientName":"laptop","domain":"ads.example.com","qtype":"A",
            "status":"blocked","reason":"cat:social","rcode":"NOERROR","upstream":"cloudflare","ms":1.25,"proto":"doh","group":"Kids"}],"total":1500}""",
        )
        val page = dns.log(DNS64LogStatus.BLOCKED, "ads", 50, 100, "Kids")
        expectRequest("GET", "/api/dns64/log?status=blocked&q=ads&limit=50&offset=100&group=Kids")
        assertEquals(1500L, page.total)
        val row = page.rows.single()
        assertEquals(1761600123456L, row.t)
        assertEquals("laptop", row.clientName)
        assertEquals("cat:social", row.reason)
        assertEquals(1.25, row.ms ?: 0.0, 0.0)
        assertEquals("doh", row.proto)
        assertEquals("Kids", row.group)
    }

    @Test
    fun `log with empty group filter keeps the empty parameter`() = test<Unit> {
        enqueueJson("""{"rows":[],"total":0}""")
        dns.log(group = "")
        expectRequest("GET", "/api/dns64/log?group=")
    }

    @Test
    fun `lists decodes blocklist status`() = test<Unit> {
        enqueueJson(
            """[{"name":"StevenBlack","url":"https://example.org/hosts","rules":150000,"updatedAt":"2026-10-28T03:00:00Z","error":"","failures":0},
            {"name":"Broken","url":"https://example.org/x","rules":0,"error":"timeout","failures":3}]""",
        )
        val lists = dns.lists()
        expectRequest("GET", "/api/dns64/lists")
        assertEquals(150000L, lists[0].rules)
        assertEquals(Instant.parse("2026-10-28T03:00:00Z"), lists[0].updatedAt)
        assertEquals("timeout", lists[1].error)
        assertEquals(3, lists[1].failures)
        assertNull(lists[1].updatedAt)
    }

    private val settingsJson = """{"port":53,"bind":"0.0.0.0","router":"192.168.1.1","retention_days":30,"upstream_mode":"fastest",
        "upstreams":[{"name":"Cloudflare","url":"https://cloudflare-dns.com/dns-query","ips":["1.1.1.1","1.0.0.1"],"enabled":true,"custom":false}],
        "lists":[{"name":"Mine","url":"https://example.org/l.txt","enabled":true,"custom":true}],
        "list_hours":24,"block_mode":"nxdomain","cache_size":8192,"anonymize":"mask","allow_nets":["192.168.0.0/16"],
        "doh":true,"dot":false,"doq":true,"doh_port":443,"dot_port":853,"names":["dns.home.lan"],"cert_mode":"acme",
        "notify_upstream":true,"notify_lists":false,"acme_domain":"example.ipv64.net","acme_prefix":"dns","acme_email":"a@b.c",
        "acme_staging":true,"upstream_count":2,"cert_id":7,"firefox_canary":true}"""

    private fun assertSettings(s: DNS64Settings) {
        assertEquals(53, s.port)
        assertEquals(30, s.retentionDays)
        assertEquals("fastest", s.upstreamMode)
        assertEquals(listOf("1.1.1.1", "1.0.0.1"), s.upstreams.single().ips)
        assertEquals(true, s.lists.single().custom)
        assertEquals(24, s.listHours)
        assertEquals("nxdomain", s.blockMode)
        assertEquals(8192, s.cacheSize)
        assertEquals("mask", s.anonymize)
        assertEquals(listOf("192.168.0.0/16"), s.allowNets)
        assertEquals(true, s.doq)
        assertEquals(443, s.dohPort)
        assertEquals(853, s.dotPort)
        assertEquals(listOf("dns.home.lan"), s.names)
        assertEquals("acme", s.certMode)
        assertEquals(false, s.notifyLists)
        assertEquals("example.ipv64.net", s.acmeDomain)
        assertEquals("dns", s.acmePrefix)
        assertEquals("a@b.c", s.acmeEmail)
        assertEquals(true, s.acmeStaging)
        assertEquals(2, s.upstreamCount)
        assertEquals(7, s.certId)
        assertEquals(true, s.firefoxCanary)
    }

    @Test
    fun `settings decodes snake_case fields`() = test<Unit> {
        enqueueJson(settingsJson)
        assertSettings(dns.settings())
        expectRequest("GET", "/api/dns64/settings")
    }

    @Test
    fun `addList posts url and name`() = test<Unit> {
        enqueueJson(settingsJson)
        assertSettings(dns.addList("https://example.org/list.txt", "My list"))
        val r = expectRequest("POST", "/api/dns64/lists")
        assertEquals(js("""{"url":"https://example.org/list.txt","name":"My list"}"""), r.jsonBody())
        assertTrue(r.headers["Content-Type"]!!.startsWith("application/json"))
    }

    @Test
    fun `addList without name omits it`() = test<Unit> {
        enqueueJson(settingsJson)
        dns.addList("https://example.org/list.txt")
        assertEquals(js("""{"url":"https://example.org/list.txt"}"""), expectRequest("POST", "/api/dns64/lists").jsonBody())
    }

    @Test
    fun `removeList is DELETE on the collection with url query`() = test<Unit> {
        enqueueJson(settingsJson)
        assertSettings(dns.removeList("https://example.org/list.txt"))
        val r = expectRequest("DELETE", "/api/dns64/lists?url=https%3A%2F%2Fexample.org%2Flist.txt")
            }

    @Test
    fun `upstreams decodes stats`() = test<Unit> {
        enqueueJson(
            """{"mode":"fastest","servers":[{"name":"Cloudflare","url":"https://cloudflare-dns.com/dns-query","proto":"doh","ips":["1.1.1.1"],
            "requests":1000,"wins":600,"errors":2,"avgMs":14.2,"healthy":false,"lastError":"timeout","lastErrorAt":"2026-10-28T09:00:00Z","active":true}],
            "localDNS":"192.168.1.1","localSource":"auto","count":2}""",
        )
        val u = dns.upstreams()
        expectRequest("GET", "/api/dns64/upstreams")
        assertEquals("fastest", u.mode)
        assertEquals("192.168.1.1", u.localDns)
        assertEquals("auto", u.localSource)
        assertEquals(2, u.count)
        val s = u.servers.single()
        assertEquals("doh", s.proto)
        assertEquals(600L, s.wins)
        assertEquals(14.2, s.avgMs ?: 0.0, 0.0)
        assertEquals(false, s.healthy)
        assertEquals("timeout", s.lastError)
        assertEquals(Instant.parse("2026-10-28T09:00:00Z"), s.lastErrorAt)
        assertEquals(true, s.active)
    }

    @Test
    fun `addUpstream posts request`() = test<Unit> {
        enqueueJson(settingsJson)
        dns.addUpstream(DNS64AddUpstreamRequest("tls://dns.quad9.net", "Quad9", listOf("9.9.9.9")))
        val r = expectRequest("POST", "/api/dns64/upstreams")
        assertEquals(js("""{"url":"tls://dns.quad9.net","name":"Quad9","ips":["9.9.9.9"]}"""), r.jsonBody())
    }

    @Test
    fun `addUpstream omits null optionals`() = test<Unit> {
        enqueueJson(settingsJson)
        dns.addUpstream(DNS64AddUpstreamRequest("tls://dns.quad9.net"))
        assertEquals(js("""{"url":"tls://dns.quad9.net"}"""), expectRequest("POST", "/api/dns64/upstreams").jsonBody())
    }

    @Test
    fun `removeUpstream is DELETE with url query`() = test<Unit> {
        enqueueJson(settingsJson)
        dns.removeUpstream("tls://dns.quad9.net")
        expectRequest("DELETE", "/api/dns64/upstreams?url=tls%3A%2F%2Fdns.quad9.net")
    }

    @Test
    fun `cache decodes counters`() = test<Unit> {
        enqueueJson("""{"entries":5,"max":100,"staleEntries":1,"hits":10,"misses":4,"staleServed":2,"refreshed":6,"refreshFailures":1}""")
        val c = dns.cache()
        expectRequest("GET", "/api/dns64/cache")
        assertEquals(5L, c.entries)
        assertEquals(100L, c.max)
        assertEquals(1L, c.staleEntries)
        assertEquals(10L, c.hits)
        assertEquals(4L, c.misses)
        assertEquals(6L, c.refreshed)
        assertEquals(1L, c.refreshFailures)
    }

    @Test
    fun `flushCache posts and decodes count`() = test<Unit> {
        enqueueJson("""{"flushed":321}""")
        assertEquals(321L, dns.flushCache().flushed)
        val r = expectRequest("POST", "/api/dns64/cache/flush")
            }

    @Test
    fun `allowlist decodes domains with instants`() = test<Unit> {
        enqueueJson("""[{"domain":"example.com","addedAt":"2026-10-20T10:00:00Z"},{"domain":"foo.org"}]""")
        val l = dns.allowlist()
        expectRequest("GET", "/api/dns64/allowlist")
        assertEquals(Instant.parse("2026-10-20T10:00:00Z"), l[0].addedAt)
        assertEquals("foo.org", l[1].domain)
        assertNull(l[1].addedAt)
    }

    @Test
    fun `allow posts domain`() = test<Unit> {
        enqueueJson("""{"domain":"example.com","addedAt":"2026-10-28T10:00:00Z"}""")
        val d = dns.allow("example.com")
        val r = expectRequest("POST", "/api/dns64/allowlist")
        assertEquals(js("""{"domain":"example.com"}"""), r.jsonBody())
        assertEquals("example.com", d.domain)
        assertNotNull(d.addedAt)
    }

    @Test
    fun `removeAllowed is DELETE with domain query`() = test<Unit> {
        enqueueNoContent()
        dns.removeAllowed("example.com")
        expectRequest("DELETE", "/api/dns64/allowlist?domain=example.com")
    }

    @Test
    fun `importAllowlist posts domains and decodes result`() = test<Unit> {
        enqueueJson("""{"imported":8,"invalid":2}""")
        val res = dns.importAllowlist(listOf("a.com", "b.org"))
        val r = expectRequest("POST", "/api/dns64/allowlist/import")
        assertEquals(js("""{"domains":["a.com","b.org"]}"""), r.jsonBody())
        assertEquals(8, res.imported)
        assertEquals(2, res.invalid)
    }

    @Test
    fun `updateSettings sends only set fields with enum wire names`() = test<Unit> {
        enqueueJson(settingsJson)
        assertSettings(
            dns.updateSettings(
                DNS64SettingsRequest(
                    retentionDays = 14,
                    upstreamMode = DNS64UpstreamMode.ORDERED,
                    upstreams = listOf(DNS64Upstream(name = "Q9", url = "tls://dns.quad9.net", ips = listOf("9.9.9.9"), enabled = true)),
                    lists = emptyList(),
                    blockMode = DNS64BlockMode.NXDOMAIN,
                    anonymize = DNS64Anonymize.HIDE,
                    allowNets = listOf("10.0.0.0/8"),
                    doh = true,
                    dohPort = 8443,
                    certMode = DNS64CertMode.CENTRAL,
                    certId = 9,
                    acmeStaging = false,
                    upstreamCount = 3,
                    firefoxCanary = false,
                ),
            ),
        )
        val r = expectRequest("PUT", "/api/dns64/settings")
        assertEquals(
            js(
                """{"retention_days":14,"upstream_mode":"ordered",
                "upstreams":[{"name":"Q9","url":"tls://dns.quad9.net","ips":["9.9.9.9"],"enabled":true}],
                "lists":[],"block_mode":"nxdomain","anonymize":"hide","allow_nets":["10.0.0.0/8"],"doh":true,"doh_port":8443,
                "cert_mode":"central","cert_id":9,"acme_staging":false,"upstream_count":3,"firefox_canary":false}""",
            ),
            r.jsonBody(),
        )
    }

    @Test
    fun `updateSettings with empty request sends empty object`() = test<Unit> {
        enqueueJson(settingsJson)
        dns.updateSettings(DNS64SettingsRequest())
        assertEquals(js("{}"), expectRequest("PUT", "/api/dns64/settings").jsonBody())
    }

    @Test
    fun `pause posts minutes and decodes blocking`() = test<Unit> {
        enqueueJson("""{"paused":true,"until_resumed":false,"paused_until":"2026-10-28T11:00:00Z"}""")
        val b = dns.pause(30)
        val r = expectRequest("POST", "/api/dns64/pause")
        assertEquals(js("""{"minutes":30}"""), r.jsonBody())
        assertEquals(true, b.paused)
        assertEquals(Instant.parse("2026-10-28T11:00:00Z"), b.pausedUntil)
    }

    @Test
    fun `pause zero is sent explicitly`() = test<Unit> {
        enqueueJson("""{"paused":true,"until_resumed":true}""")
        val b = dns.pause(0)
        assertEquals(js("""{"minutes":0}"""), expectRequest("POST", "/api/dns64/pause").jsonBody())
        assertEquals(true, b.untilResumed)
        assertNull(b.pausedUntil)
    }

    @Test
    fun `resume posts without body`() = test<Unit> {
        enqueueJson("""{"paused":false}""")
        assertEquals(false, dns.resume().paused)
        assertEquals(js("{}"), expectRequest("POST", "/api/dns64/resume").jsonBody())
    }

    @Test
    fun `devices decodes last seen and group id`() = test<Unit> {
        enqueueJson(
            """[{"ip":"192.168.1.20","name":"laptop","mac":"aa:bb:cc:dd:ee:ff","group":"Kids","group_id":4,"queries":777,"last_seen":"2026-10-28T09:59:00Z"}]""",
        )
        val d = dns.devices().single()
        expectRequest("GET", "/api/dns64/devices")
        assertEquals("aa:bb:cc:dd:ee:ff", d.mac)
        assertEquals(4L, d.groupId)
        assertEquals(777L, d.queries)
        assertEquals(Instant.parse("2026-10-28T09:59:00Z"), d.lastSeen)
    }

    @Test
    fun `deviceNames decodes ip lists`() = test<Unit> {
        enqueueJson("""[{"name":"laptop.home.lan","ips":["192.168.1.20","fd00::20"]}]""")
        val d = dns.deviceNames().single()
        expectRequest("GET", "/api/dns64/device-names")
        assertEquals("laptop.home.lan", d.name)
        assertEquals(listOf("192.168.1.20", "fd00::20"), d.ips)
    }

    @Test
    fun `caCertificate returns exact bytes and sends Accept`() = test<Unit> {
        val der = byteArrayOf(0x30, -126, 0x01, 0x0A, 0x00, -1, -128, 0x7F)
        enqueueBytes(der, "application/x-x509-ca-cert")
        val bytes = dns.caCertificate()
        val r = expectRequest("GET", "/api/dns64/ca.crt")
        assertEquals("application/x-x509-ca-cert", r.headers["Accept"])
        assertArrayEquals(der, bytes)
    }

    @Test
    fun `mobileConfig returns exact bytes with query and Accept`() = test<Unit> {
        val profile = "<?xml version=\"1.0\"?><plist/>".toByteArray() + byteArrayOf(0, -1, 0x80.toByte())
        enqueueBytes(profile, "application/x-apple-aspen-config")
        val bytes = dns.mobileConfig(DNS64MobileConfigProto.DOT, "dns.home.lan")
        val r = expectRequest("GET", "/api/dns64/mobileconfig?proto=dot&host=dns.home.lan")
        assertEquals("application/x-apple-aspen-config", r.headers["Accept"])
        assertArrayEquals(profile, bytes)
    }

    @Test
    fun `mobileConfig without parameters has no query`() = test<Unit> {
        enqueueBytes(byteArrayOf(1))
        dns.mobileConfig()
        expectRequest("GET", "/api/dns64/mobileconfig")
    }

    @Test
    fun `error documents become Node64ApiException`() = test<Unit> {
        server.enqueue(
            mockwebserver3.MockResponse.Builder().code(422).setHeader("Content-Type", "application/json")
                .body("""{"error":"invalid minutes","field":"minutes"}""").build(),
        )
        val e = runCatching { dns.pause(5000) }.exceptionOrNull()
        e as de.sr.ipv64.Node64ApiException
        assertEquals("minutes", e.field)
        assertEquals("invalid minutes", e.error)
    }
}
