package de.sr.ipv64.node64.api

import de.sr.ipv64.model.IpFamily
import de.sr.ipv64.node64.model.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.time.Instant

class Proxy64HostsApiTest : Node64ApiTestBaseB() {

    private val hosts get() = node().proxy.hosts

    private val hostJson = """{"id":3,"kind":"proxy","names":["a.example.com","*.b.example.com"],"enabled":true,"scheme":"https",
        "target_host":"192.168.1.10","target_port":8443,"websockets":true,"block_exploits":true,"cache_assets":false,"cert_id":-1,
        "force_ssl":true,"http2":true,"http3":false,"hsts":true,"hsts_subdomains":false,"min_tls":"1.3","verify_target":false,
        "created_at":"2026-10-20T10:00:00Z","updated_at":"2026-10-21T11:30:00+02:00",
        "reach":{"state":"slow","ms":850,"error":"","checked_at":"2026-10-28T10:00:00Z","since":"2026-10-28T09:00:00Z"},
        "stats":{"requests":1000000000000,"errors":5,"blocked":6},
        "certificate":{"state":"soon","name":"a.example.com","days_left":9,"missing":"b.example.com"},
        "route_error":"re","certificate_error":"ce","redirect_target":"https://x","redirect_scheme":"auto","redirect_code":301,"preserve_path":true,
        "access_list_id":2,
        "locations":[{"path":"/api","scheme":"http","target_host":"10.0.0.1","target_port":80,"forward_path":"/","websockets":false,"access_list_id":5}],
        "advanced":{"request_headers":[{"name":"X-A","value":"1"}],"request_remove":["Cookie"],"response_headers":[{"name":"X-B","value":"2"}],
          "response_remove":["Server"],"host_header":"h.test","timeout_seconds":60,"max_body_mb":100,"no_buffering":true,"gzip":true,
          "trusted_proxies":["10.0.0.0/8"],"access_log":false,"no_limits":true,"geo_mode":"block","geo_countries":["RU","CN"],
          "ai_bots":true,"ai_bots_allow_user":false,"ai_robots":true},
        "dns_records":[{"name":"a.example.com","result":"not_ipv64","error":"e"}],"dns_error":"de",
        "healthchecks":[{"token":"tok","name":"hc","type":"http","state":"up","paused":false,"local":true,"host_only":false,"last_check":"2026-10-28T10:00:00Z"}],
        "status_page_id":8}"""

    private fun assertHost(h: ProxyHost) {
        assertEquals(3L, h.id)
        assertEquals(ProxyHostKind.PROXY, h.kind)
        assertEquals(listOf("a.example.com", "*.b.example.com"), h.names)
        assertEquals(ProxyScheme.HTTPS, h.scheme)
        assertEquals("192.168.1.10", h.targetHost)
        assertEquals(8443, h.targetPort)
        assertEquals(-1L, h.certId)
        assertEquals(MinTlsVersion.TLS_1_3, h.minTls)
        assertEquals(false, h.http3)
        assertEquals(false, h.hstsSubdomains)
        assertEquals(Instant.parse("2026-10-20T10:00:00Z"), h.createdAt)
        assertEquals(Instant.parse("2026-10-21T09:30:00Z"), h.updatedAt)
        assertEquals(HostReachState.SLOW, h.reach?.state)
        assertEquals(850, h.reach?.ms)
        assertEquals(Instant.parse("2026-10-28T10:00:00Z"), h.reach?.checkedAt)
        assertEquals(Instant.parse("2026-10-28T09:00:00Z"), h.reach?.since)
        assertEquals(1000000000000L, h.stats?.requests)
        assertEquals(HostCertificateState.SOON, h.certificate?.state)
        assertEquals(9, h.certificate?.daysLeft)
        assertEquals("b.example.com", h.certificate?.missing)
        assertEquals("re", h.routeError)
        assertEquals("ce", h.certificateError)
        assertEquals(RedirectScheme.AUTO, h.redirectScheme)
        assertEquals(301, h.redirectCode)
        assertEquals(2L, h.accessListId)
        val loc = h.locations.single()
        assertEquals("/api", loc.path)
        assertEquals(ProxyScheme.HTTP, loc.scheme)
        assertEquals(5L, loc.accessListId)
        val adv = h.advanced!!
        assertEquals("X-A", adv.requestHeaders.single().name)
        assertEquals(listOf("Cookie"), adv.requestRemove)
        assertEquals("2", adv.responseHeaders.single().value)
        assertEquals(60, adv.timeoutSeconds)
        assertEquals(ProxyGeoMode.BLOCK, adv.geoMode)
        assertEquals(listOf("RU", "CN"), adv.geoCountries)
        assertEquals(true, adv.aiRobots)
        assertEquals(HostDnsRecordResult.NOT_IPV64, h.dnsRecords.single().result)
        assertEquals("de", h.dnsError)
        val hc = h.healthchecks.single()
        assertEquals("tok", hc.token)
        assertEquals(true, hc.local)
        assertEquals(Instant.parse("2026-10-28T10:00:00Z"), hc.lastCheck)
        assertEquals(8L, h.statusPageId)
    }

    @Test
    fun `list without filter decodes hosts`() = test<Unit> {
        enqueueJson("[$hostJson]")
        assertHost(hosts.list().single())
        expectRequest("GET", "/api/cdn64/hosts")
    }

    @Test
    fun `list filters by kind`() = test<Unit> {
        enqueueJson("[]")
        hosts.list(ProxyHostKind.PASSTHROUGH)
        expectRequest("GET", "/api/cdn64/hosts?kind=passthrough")
    }

    @Test
    fun `create posts rich input with enum wire names`() = test<Unit> {
        enqueueJson(hostJson)
        val input = ProxyHostInput(
            names = listOf("a.example.com"), enabled = true, scheme = ProxyScheme.HTTP, targetHost = "10.0.0.1", targetPort = 80,
            websockets = true, certId = 0, forceSsl = true, minTls = MinTlsVersion.TLS_1_2,
            requestCertificate = ProxyHostRequestCertificate(RequestCertificateSource.ALIAS, "ipv64.net", "acme", true, 7),
            kind = ProxyHostKind.REDIRECT, redirectTarget = "https://x", redirectScheme = RedirectScheme.HTTPS, redirectCode = 302,
            accessListId = 2,
            locations = listOf(CDNLocation("/api", ProxyScheme.HTTPS, "10.0.0.2", 443, "/", false, 5)),
            advanced = CDNAdvanced(geoMode = ProxyGeoMode.OFF, geoCountries = listOf("DE"), requestHeaders = listOf(CDNHeader("X-A", "1"))),
            createDnsRecord = ProxyHostCreateDnsRecord(HostDnsRecordType.AAAA, "fd00::1"),
            statusPageId = 8,
        )
        assertHost(hosts.create(input))
        val body = expectRequest("POST", "/api/cdn64/hosts").jsonBody()
        assertEquals(
            js(
                """{"names":["a.example.com"],"enabled":true,"scheme":"http","target_host":"10.0.0.1","target_port":80,"websockets":true,
                "cert_id":0,"force_ssl":true,"min_tls":"1.2",
                "request_certificate":{"source":"alias","alias_zone":"ipv64.net","alias_prefix":"acme","staging":true,"dns_account":7},
                "kind":"redirect","redirect_target":"https://x","redirect_scheme":"https","redirect_code":302,"access_list_id":2,
                "locations":[{"path":"/api","scheme":"https","target_host":"10.0.0.2","target_port":443,"forward_path":"/","websockets":false,"access_list_id":5}],
                "advanced":{"geo_mode":"","geo_countries":["DE"],"request_headers":[{"name":"X-A","value":"1"}]},
                "create_dns_record":{"type":"AAAA","content":"fd00::1"},"status_page_id":8}""",
            ),
            body,
        )
    }

    @Test
    fun `create omits null fields`() = test<Unit> {
        enqueueJson(hostJson)
        hosts.create(ProxyHostInput(names = listOf("a.example.com"), targetHost = "h", targetPort = 1))
        assertEquals(js("""{"names":["a.example.com"],"target_host":"h","target_port":1}"""), expectRequest("POST", "/api/cdn64/hosts").jsonBody())
    }

    @Test
    fun `get decodes`() = test<Unit> {
        enqueueJson(hostJson)
        assertHost(hosts.get(3))
        expectRequest("GET", "/api/cdn64/hosts/3")
    }

    @Test
    fun `update puts partial input`() = test<Unit> {
        enqueueJson(hostJson)
        assertHost(hosts.update(3, ProxyHostInput(enabled = false, http3 = true)))
        assertEquals(js("""{"enabled":false,"http3":true}"""), expectRequest("PUT", "/api/cdn64/hosts/3").jsonBody())
    }

    @Test
    fun `delete`() = test<Unit> {
        enqueueNoContent()
        hosts.delete(3)
        expectRequest("DELETE", "/api/cdn64/hosts/3")
    }

    @Test
    fun `enable`() = test<Unit> {
        enqueueJson(hostJson)
        assertHost(hosts.enable(3))
        expectRequest("POST", "/api/cdn64/hosts/3/enable")
    }

    @Test
    fun `disable`() = test<Unit> {
        enqueueJson(hostJson)
        assertHost(hosts.disable(3))
        expectRequest("POST", "/api/cdn64/hosts/3/disable")
    }

    @Test
    fun `createHealthcheck`() = test<Unit> {
        enqueueJson("""{"token":"abc123"}""")
        assertEquals("abc123", hosts.createHealthcheck(3).token)
        expectRequest("POST", "/api/cdn64/hosts/3/healthcheck")
    }

    @Test
    fun `enableOnline sends request and decodes per name results`() = test<Unit> {
        enqueueJson(
            """{"results":[{"name":"a.example.com","result":"on"},{"name":"x.test","result":"skipped","code":"placeholder","text":"skip"},
            {"name":"b.example.com","result":"error","code":"c","error":"boom"}],"check":"hc_9","check_error":"ce"}""",
        )
        val res = hosts.enableOnline(3, EnableHostOnlineRequest(mode = 2, healthcheck = true))
        assertEquals(js("""{"mode":2,"healthcheck":true}"""), expectRequest("POST", "/api/cdn64/hosts/3/online").jsonBody())
        assertEquals(listOf(OnlineResultState.ON, OnlineResultState.SKIPPED, OnlineResultState.ERROR), res.results.map { it.result })
        assertEquals("placeholder", res.results[1].code)
        assertEquals("skip", res.results[1].text)
        assertEquals("boom", res.results[2].error)
        assertEquals("hc_9", res.check)
        assertEquals("ce", res.checkError)
    }

    @Test
    fun `enableOnline without request sends no content type body`() = test<Unit> {
        enqueueJson("""{"results":[]}""")
        val res = hosts.enableOnline(3)
        val r = expectRequest("POST", "/api/cdn64/hosts/3/online")
        assertTrue(res.results.isEmpty())
        assertTrue(r.bodyText().let { it.isEmpty() || it == "{}" })
    }

    @Test
    fun `disableOnline`() = test<Unit> {
        enqueueNoContent()
        hosts.disableOnline(3)
        expectRequest("DELETE", "/api/cdn64/hosts/3/online")
    }

    @Test
    fun `testOnline decodes probes`() = test<Unit> {
        enqueueJson(
            """[{"name":"a.example.com","tls":true,"dns":["1.2.3.4","fd00::1"],"dns_ok":true,
            "probes":[{"edge":"srv07","addr":"1.2.3.4","family":"ipv4","ok":true,"skipped":false,"ms":42,"trusted":true},
                      {"edge":"srv08","addr":"fd00::1","family":"ipv6","ok":false,"skipped":true,"error":"unreachable"}]}]""",
        )
        val t = hosts.testOnline(3).single()
        expectRequest("POST", "/api/cdn64/hosts/3/online/test")
        assertEquals("a.example.com", t.name)
        assertEquals(true, t.tls)
        assertEquals(listOf("1.2.3.4", "fd00::1"), t.dns)
        assertEquals(true, t.dnsOk)
        assertEquals(IpFamily.IPV4, t.probes[0].family)
        assertEquals(42L, t.probes[0].ms)
        assertEquals(true, t.probes[0].trusted)
        assertEquals(IpFamily.IPV6, t.probes[1].family)
        assertEquals(true, t.probes[1].skipped)
        assertEquals("unreachable", t.probes[1].error)
    }
}
