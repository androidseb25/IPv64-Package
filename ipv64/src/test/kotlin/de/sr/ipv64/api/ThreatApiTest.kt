package de.sr.ipv64.api

import de.sr.ipv64.MockServerTest
import de.sr.ipv64.model.AddThreatWatchRequest
import de.sr.ipv64.model.ThreatFeedAction
import de.sr.ipv64.model.ThreatIpStatus
import de.sr.ipv64.model.ThreatPasswordHash
import de.sr.ipv64.model.ThreatReleaseStatus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mockwebserver3.MockResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest
import kotlin.time.Instant

class ThreatApiTest : MockServerTest() {

    private fun body(text: String): JsonObject = Json.parseToJsonElement(text).jsonObject

    private fun enqueueText(text: String, code: Int = 200) {
        server.enqueue(
            MockResponse.Builder().code(code).setHeader("Content-Type", "text/plain").body(text).build(),
        )
    }

    @Test
    fun `ip decodes lookup`() = test {
        enqueueJson(
            """{"data":{"ip":"203.0.113.7","family":4,"listed":true,"status":"listed","score":87,
                "lists":["v64","ssh"],"kinds":["ssh_bruteforce"],"ports":[{"service":"ssh","port":22}],
                "first_seen":"2026-10-01 10:00:00","last_seen":"2026-10-10 11:00:00","accounts":12,
                "country":"DE","asn":64496,
                "release":{"status":"in_review","protected_until":"2026-10-20T00:00:00Z"},"details":"x"}}""",
        )

        val ip = client().threat.ip("203.0.113.7")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/threat/ip/203.0.113.7", request.apiTarget)
        assertEquals(4, ip.family)
        assertEquals(ThreatIpStatus.LISTED, ip.status)
        assertEquals(87, ip.score)
        assertEquals(listOf("v64", "ssh"), ip.lists)
        assertEquals(22, ip.ports.single().port)
        assertEquals(64496L, ip.asn)
        assertEquals(ThreatReleaseStatus.IN_REVIEW, ip.release?.status)
        assertEquals(Instant.parse("2026-10-20T00:00:00Z"), ip.release?.protectedUntil)
    }

    @Test
    fun `ip encodes ipv6 address as path segment`() = test {
        enqueueJson("""{"data":{"ip":"2001:db8::1","family":6,"listed":false,"status":"not_listed"}}""")

        val ip = client().threat.ip("2001:db8::1")

        assertEquals("/threat/ip/2001:db8::1", takeRequest().apiTarget)
        assertEquals(ThreatIpStatus.NOT_LISTED, ip.status)
    }

    @Test
    fun `stats decodes overview`() = test {
        enqueueJson(
            """{"data":{"generated_at":"2026-10-10T12:00:00Z","window_days":7,
                "threshold":{"accounts":3,"active_accounts_7d":120},
                "sensors":{"active_7d":40,"online":35},"attacks_24h":{"events":1000,"sources":120},
                "timeline":[{"day":"2026-10-10","events":1000}],
                "top_ports":[{"service":"ssh","port":22,"events":500}],
                "top_kinds":[{"kind":"ssh_bruteforce","events":400}],
                "lists":{"v64":1500,"ssh":900},
                "top":{"usernames":[{"value":"root","events":300,"accounts":20}],
                  "countries":[{"country":"CN","ips":50,"events":600}]},
                "info":"cached","license":"CC0"}}""",
        )

        val stats = client().threat.stats()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/threat/stats", request.apiTarget)
        assertEquals(Instant.parse("2026-10-10T12:00:00Z"), stats.generatedAt)
        assertEquals(7, stats.windowDays)
        assertEquals(120L, stats.threshold?.activeAccounts7d)
        assertEquals(35L, stats.sensors?.online)
        assertEquals(1000L, stats.attacks24h?.events)
        assertEquals(mapOf("v64" to 1500L, "ssh" to 900L), stats.lists)
        assertEquals("root", stats.top?.usernames?.single()?.value)
        assertEquals("CN", stats.top?.countries?.single()?.country)
        assertEquals("CC0", stats.license)
    }

    @Test
    fun `watch decodes limits networks and dyndns`() = test {
        enqueueJson(
            """{"data":{"limits":{"max":10,"min_prefix_v4":24,"min_prefix_v6":48},
                "networks":[{"id":1,"cidr":"198.51.100.0/24","label":"Office","verified":true,
                  "created_at":"2026-10-01T00:00:00Z"}],
                "dyndns":[{"ip":"203.0.113.9","domain":"home.ipv64.net"}]}}""",
        )

        val watch = client().threat.watch()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/threat/watch", request.apiTarget)
        assertEquals(24, watch.limits?.minPrefixV4)
        assertEquals("198.51.100.0/24", watch.networks.single().cidr)
        assertEquals(true, watch.networks.single().verified)
        assertEquals("home.ipv64.net", watch.dyndns.single().domain)
    }

    @Test
    fun `addWatch posts request and omits null label`() = test {
        enqueueJson("""{"data":{"id":2,"cidr":"198.51.100.0/24","verified":false}}""", code = 201)

        val network = client().threat.addWatch(AddThreatWatchRequest("198.51.100.5/24"))

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/threat/watch", request.apiTarget)
        assertEquals(setOf("cidr"), body(request.bodyText()).keys)
        assertEquals(2L, network.id)
    }

    @Test
    fun `addWatch convenience overload sends label`() = test {
        enqueueJson("""{"data":{"id":3,"cidr":"203.0.113.1/32","label":"Server"}}""")

        val network = client().threat.addWatch("203.0.113.1", "Server")

        val sent = body(takeRequest().bodyText())
        assertEquals("203.0.113.1", sent["cidr"]?.jsonPrimitive?.content)
        assertEquals("Server", sent["label"]?.jsonPrimitive?.content)
        assertEquals("Server", network.label)
    }

    @Test
    fun `deleteWatch accepts 204`() = test {
        enqueueNoContent()

        client().threat.deleteWatch(2)

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/threat/watch/2", request.apiTarget)
    }

    @Test
    fun `watchAlerts without since and decodes alerts`() = test {
        enqueueJson(
            """{"data":{"alerts":[{"id":11,"ip":"203.0.113.7","cidr":"203.0.113.0/24","network_id":1,
                "source":"network","label":"Office","listed":true,"score":90,"kinds":["ssh_bruteforce"],
                "first_seen":"2026-10-09 10:00:00","last_seen":"2026-10-10 10:00:00",
                "created_at":"2026-10-10T10:01:00Z",
                "details":{"accounts":5,"sensors":3,"events":40,"lists":["v64"],"reason":"threshold",
                  "seen":[{"origin":"honeypot","service":"ssh","port":22,"kind":"ssh_bruteforce","rule":"r1",
                    "count":30,"sensors":2,"last":"2026-10-10T10:00:00Z"}]}}],
                "next":"cursor-42"}}""",
        )

        val alerts = client().threat.watchAlerts()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/threat/watch/alerts", request.apiTarget)
        assertEquals("cursor-42", alerts.next)
        val alert = alerts.alerts.single()
        assertEquals(11L, alert.id)
        assertEquals(1L, alert.networkId)
        assertEquals(Instant.parse("2026-10-10T10:01:00Z"), alert.createdAt)
        assertEquals(5L, alert.details?.accounts)
        assertEquals(22, alert.details?.seen?.single()?.port)
        assertEquals(30L, alert.details?.seen?.single()?.count)
    }

    @Test
    fun `watchAlerts passes since cursor unchanged`() = test {
        enqueueJson("""{"data":{"alerts":[],"next":"cursor-42"}}""")

        client().threat.watchAlerts(since = "cursor 42/+=")

        assertEquals("/threat/watch/alerts?since=cursor%2042%2F%2B%3D", takeRequest().apiTarget)
    }

    private val feedJson =
        """{"data":{"changes":[{"ip":"203.0.113.7","action":"update","lists":["v64","ssh"],
            "previous_lists":["v64"],"at":"2026-10-10T12:00:00Z"},
            {"ip":"198.51.100.1","action":"add","lists":["v64"]}],"next":"c2","more":true}}"""

    @Test
    fun `feed 200 returns feed and etag`() = test {
        server.enqueue(
            MockResponse.Builder().code(200)
                .setHeader("Content-Type", "application/json")
                .setHeader("ETag", "\"abc123\"")
                .body(feedJson).build(),
        )

        val result = client().threat.feed(since = "c1")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/threat/feed?since=c1", request.apiTarget)
        assertNull(request.headers["If-None-Match"])
        assertEquals("\"abc123\"", result.etag)
        assertFalse(result.notModified)
        val feed = result.feed!!
        assertEquals("c2", feed.next)
        assertEquals(true, feed.more)
        assertEquals(ThreatFeedAction.UPDATE, feed.changes[0].action)
        assertEquals(listOf("v64"), feed.changes[0].previousLists)
        assertEquals(Instant.parse("2026-10-10T12:00:00Z"), feed.changes[0].at)
        assertEquals(ThreatFeedAction.ADD, feed.changes[1].action)
        assertNull(feed.changes[1].at)
    }

    @Test
    fun `feed without cursor has no query`() = test {
        enqueueJson("""{"data":{"changes":[],"next":"c1","more":false}}""")

        val result = client().threat.feed()

        assertEquals("/threat/feed", takeRequest().apiTarget)
        assertNull(result.etag)
        assertEquals("c1", result.feed?.next)
    }

    @Test
    fun `feed 304 returns notModified and keeps given etag`() = test {
        server.enqueue(MockResponse.Builder().code(304).setHeader("ETag", "\"abc123\"").build())

        val result = client().threat.feed(since = "c1", ifNoneMatch = "\"abc123\"")

        val request = takeRequest()
        assertEquals("/threat/feed?since=c1", request.apiTarget)
        assertEquals("\"abc123\"", request.headers["If-None-Match"])
        assertNull(result.feed)
        assertTrue(result.notModified)
        assertEquals("\"abc123\"", result.etag)
    }

    @Test
    fun `feed 304 without etag header falls back to request etag`() = test {
        server.enqueue(MockResponse.Builder().code(304).build())

        val result = client().threat.feed(since = "c1", ifNoneMatch = "\"zzz\"")

        takeRequest()
        assertTrue(result.notModified)
        assertEquals("\"zzz\"", result.etag)
    }

    @Test
    fun `passwordRangeRaw returns text and sends prefix only`() = test {
        val text = "0018A45C4D1DEF81644B54AB7F969B88D65:3\r\n00D4F6E8FA6EECAD2A3AA415EEC418D38EC:2\r\n"
        enqueueText(text)

        val raw = client().threat.passwordRangeRaw("21BD1")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/threat/passwords/range/21BD1", request.apiTarget)
        assertEquals(text, raw)
    }

    @Test
    fun `passwordRange parses lines uppercases suffix and keeps padding`() = test {
        enqueueText("0018a45c4d1def81644b54ab7f969b88d65:3\r\nBAD LINE\r\n:5\r\nXX:abc\r\n00D4F6E8FA6EECAD2A3AA415EEC418D38EC:0\n\n")

        val hashes = client().threat.passwordRange("21BD1")

        assertEquals(
            listOf(
                ThreatPasswordHash("0018A45C4D1DEF81644B54AB7F969B88D65", 3),
                ThreatPasswordHash("00D4F6E8FA6EECAD2A3AA415EEC418D38EC", 0),
            ),
            hashes,
        )
    }

    private fun sha1(password: String): String =
        MessageDigest.getInstance("SHA-1").digest(password.toByteArray()).joinToString("") { "%02X".format(it) }

    @Test
    fun `passwordCount sends only the five char prefix and finds suffix`() = test {
        val hash = sha1("P@ssw0rd")
        enqueueText("0000000000000000000000000000000000A:1\r\n${hash.substring(5).lowercase()}:4242\r\n")

        val count = client().threat.passwordCount("P@ssw0rd")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/threat/passwords/range/${hash.substring(0, 5)}", request.apiTarget)
        assertFalse(request.target.contains(hash.substring(5)))
        assertEquals(4242L, count)
    }

    @Test
    fun `passwordCount returns zero when suffix missing or padding`() = test {
        val hash = sha1("unknown-password")
        enqueueText("0000000000000000000000000000000000A:7\r\n")
        enqueueText("${hash.substring(5)}:0\r\n")

        assertEquals(0L, client().threat.passwordCount("unknown-password"))
        assertEquals(0L, client().threat.passwordCount("unknown-password"))
    }
}
