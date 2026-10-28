package de.sr.ipv64.api

import de.sr.ipv64.MockServerTest
import de.sr.ipv64.model.HoneypotListToken
import de.sr.ipv64.model.HoneypotMode
import de.sr.ipv64.model.HoneypotProfile
import de.sr.ipv64.model.HoneypotReport
import de.sr.ipv64.model.HoneypotReportPeriod
import de.sr.ipv64.model.HoneypotReportSensor
import de.sr.ipv64.model.HoneypotReportSource
import de.sr.ipv64.model.HoneypotSourceOrigin
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.zip.GZIPInputStream
import kotlin.time.Instant

class HoneypotApiTest : MockServerTest() {

    private val report = HoneypotReport(
        schema = 1,
        reportId = "rep-1",
        sensor = HoneypotReportSensor("sensor-1", "1.0.0", HoneypotProfile.STANDARD, HoneypotMode.MODULE),
        period = HoneypotReportPeriod(Instant.parse("2026-10-10T10:00:00Z"), Instant.parse("2026-10-10T10:05:00Z")),
        sources = listOf(
            HoneypotReportSource(
                ip = "203.0.113.7", kind = "ssh_bruteforce", service = "ssh", port = 22, count = 14,
                first = Instant.parse("2026-10-10T10:00:01Z"), last = Instant.parse("2026-10-10T10:04:59Z"),
                source = HoneypotSourceOrigin.HONEYPOT,
            ),
        ),
    )

    private val acceptedJson =
        """{"data":{"accepted":1,"rejected":2,"rejected_reasons":{"private_ip":2},"report_id":"rep-1","duplicate":false}}"""

    private fun assertReportJson(json: JsonObject) {
        assertEquals(setOf("schema", "report_id", "sensor", "period", "sources"), json.keys)
        assertEquals(1, json["schema"]?.jsonPrimitive?.int)
        assertEquals("rep-1", json["report_id"]?.jsonPrimitive?.content)
        val sensor = json["sensor"]!!.jsonObject
        assertEquals("standard", sensor["profile"]?.jsonPrimitive?.content)
        assertEquals("module", sensor["mode"]?.jsonPrimitive?.content)
        assertFalse(sensor.containsKey("country"))
        assertEquals("2026-10-10T10:00:00Z", json["period"]!!.jsonObject["start"]?.jsonPrimitive?.content)
        val source = json["sources"]!!.jsonArray.single().jsonObject
        assertEquals("honeypot", source["source"]?.jsonPrimitive?.content)
        assertEquals("203.0.113.7", source["ip"]?.jsonPrimitive?.content)
        assertEquals("2026-10-10T10:04:59Z", source["last"]?.jsonPrimitive?.content)
        assertFalse(source.containsKey("rule"))
    }

    @Test
    fun `submitReport sends plain json by default`() = test {
        enqueueJson(acceptedJson)

        val accepted = client().honeypot.submitReport(report)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/honeypot/reports", request.apiTarget)
        assertNull(request.headers["Content-Encoding"])
        assertTrue(request.headers["Content-Type"]!!.startsWith("application/json"))
        assertReportJson(Json.parseToJsonElement(request.bodyText()).jsonObject)
        assertEquals(1L, accepted.accepted)
        assertEquals(2L, accepted.rejected)
        assertEquals(mapOf("private_ip" to 2L), accepted.rejectedReasons)
        assertEquals("rep-1", accepted.reportId)
        assertEquals(false, accepted.duplicate)
    }

    @Test
    fun `submitReport with gzip compresses body and sets content-encoding`() = test {
        enqueueJson("""{"data":{"accepted":0,"rejected":0,"report_id":"rep-1","duplicate":true}}""")

        val accepted = client().honeypot.submitReport(report, gzip = true)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/honeypot/reports", request.apiTarget)
        assertEquals("gzip", request.headers["Content-Encoding"])
        assertTrue(request.headers["Content-Type"]!!.startsWith("application/json"))
        val raw = request.body!!.toByteArray()
        assertEquals(0x1f, raw[0].toInt() and 0xff)
        assertEquals(0x8b, raw[1].toInt() and 0xff)
        val text = GZIPInputStream(raw.inputStream()).use { it.readBytes() }.toString(Charsets.UTF_8)
        assertReportJson(Json.parseToJsonElement(text).jsonObject)
        assertEquals(true, accepted.duplicate)
    }

    @Test
    fun `sensors decodes list`() = test {
        enqueueJson(
            """{"data":[{"id":"sensor-1","version":"1.0.0","profile":"standard","mode":"module",
                "first_seen":"2026-09-01T00:00:00Z","last_seen":"2026-10-10T12:00:00+02:00","online":true,
                "reports":120,"disabled":false,"sources_24h":10,"sources_7d":55,"events_24h":300,"events_7d":2100}]}""",
        )

        val sensors = client().honeypot.sensors()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/honeypot/sensors", request.apiTarget)
        val s = sensors.single()
        assertEquals("sensor-1", s.id)
        assertEquals("standard", s.profile)
        assertEquals(Instant.parse("2026-10-10T10:00:00Z"), s.lastSeen)
        assertEquals(120L, s.reports)
        assertEquals(2100L, s.events7d)
        assertEquals(55L, s.sources7d)
    }

    @Test
    fun `listToken decodes token`() = test {
        enqueueJson("""{"data":{"token":"TOKSECRET","live":true,"base":"https://lists.ipv64.net/TOKSECRET/"}}""")

        val token = client().honeypot.listToken()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/honeypot/list-token", request.apiTarget)
        assertEquals("TOKSECRET", token.token)
        assertEquals(true, token.live)
        assertEquals("https://lists.ipv64.net/TOKSECRET/", token.base)
    }

    @Test
    fun `listToken toString redacts token and base`() {
        val text = HoneypotListToken("TOKSECRET", true, "https://x/TOKSECRET/").toString()
        assertFalse(text.contains("TOKSECRET"))
        assertTrue(text.contains("live=true"))
    }

    @Test
    fun `stats decodes nested statistics`() = test {
        enqueueJson(
            """{"data":{"sensors":2,"sensors_online":1,"contributor":true,"last_report":"2026-10-10T12:00:00Z",
                "events_24h":300,"events_7d":2100,"sources_24h":10,"sources_7d":55,
                "top_kinds":[{"kind":"ssh_bruteforce","events":200,"sources":8}],
                "top_ports":[{"service":"ssh","port":22,"events":250,"sources":9}],
                "timeline":[{"day":"2026-10-09","events":150},{"day":"2026-10-10","events":300}]}}""",
        )

        val stats = client().honeypot.stats()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/honeypot/stats", request.apiTarget)
        assertEquals(2L, stats.sensors)
        assertEquals(true, stats.contributor)
        assertEquals(Instant.parse("2026-10-10T12:00:00Z"), stats.lastReport)
        assertEquals("ssh_bruteforce", stats.topKinds.single().kind)
        assertEquals(22, stats.topPorts.single().port)
        assertEquals(listOf("2026-10-09", "2026-10-10"), stats.timeline.map { it.day })
        assertEquals(300L, stats.timeline[1].events)
    }
}
