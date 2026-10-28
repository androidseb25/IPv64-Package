package de.sr.ipv64.api

import de.sr.ipv64.MockServerTest
import de.sr.ipv64.model.Hc2CheckInput
import de.sr.ipv64.model.Hc2CheckParams
import de.sr.ipv64.model.Hc2CheckStateFilter
import de.sr.ipv64.model.Hc2CheckType
import de.sr.ipv64.model.Hc2CheckUpdate
import de.sr.ipv64.model.Hc2Execution
import de.sr.ipv64.model.Hc2IpMode
import de.sr.ipv64.model.Hc2MoveCheckRequest
import de.sr.ipv64.model.Hc2Policy
import de.sr.ipv64.model.Hc2StatsRange
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class Hc2ChecksApiTest : MockServerTest() {

    private fun body(text: String): JsonObject = Json.parseToJsonElement(text).jsonObject

    private val token = "0123456789abcdef0123456789abcdef"

    private val checkJson = """
        {"name":"Website","type":"http","target":"https://example.org","interval_seconds":60,
         "retry_interval_seconds":10,"retries":2,"connect_timeout_seconds":3.5,"timeout_seconds":10.0,
         "invert":false,"stabilize":2,"ip_mode":"both","tags":["prod","web"],"group":"sites",
         "paused":false,"critical":true,"execution":"online","locations":["nbg","fsn"],"policy":"majority",
         "notify_down":true,"notify_up":true,"channels":[1,2],"repeat_minutes":30,"token":"$token",
         "revision":4,"created_at":"2026-10-10T12:00:00+02:00","updated_at":"2026-10-11T08:00:00Z",
         "params":{"method":"GET","accept_status":"200-299","follow_redirects":true,"basic_user":"u",
           "basic_password":"PWSECRET","headers":{"Authorization":"HDRSECRET"}},
         "state":{"state":"up","since":"2026-10-10T12:00:00Z","last_check_at":"2026-10-11T08:00:00Z",
           "last_status":"ok","last_latency_ms":42.5},
         "push":{"url":"https://push","hub_url":"https://hub","node_path":"/p"}}
    """.trimIndent()

    private fun assertCheck(check: de.sr.ipv64.model.Hc2Check) {
        assertEquals("Website", check.name)
        assertEquals(Hc2CheckType.HTTP, check.type)
        assertEquals(60, check.intervalSeconds)
        assertEquals(3.5, check.connectTimeoutSeconds)
        assertEquals(Hc2IpMode.BOTH, check.ipMode)
        assertEquals(listOf("prod", "web"), check.tags)
        assertEquals(Hc2Execution.ONLINE, check.execution)
        assertEquals(Hc2Policy.MAJORITY, check.policy)
        assertEquals(listOf(1L, 2L), check.channels)
        assertEquals(token, check.token)
        assertEquals(Instant.parse("2026-10-10T10:00:00Z"), check.createdAt)
        assertEquals("up", check.state?.state)
        assertEquals(42.5, check.state?.lastLatencyMs)
        assertEquals("https://hub", check.push?.hubUrl)
        assertEquals("GET", check.params?.method)
        assertEquals(true, check.params?.followRedirects)
        assertEquals("PWSECRET", check.params?.basicPassword)
    }

    @Test
    fun `list without filters has no query`() = test {
        enqueueJson("""{"data":[$checkJson]}""")

        val checks = client().hc2.checks.list()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/hc2/checks", request.apiTarget)
        assertCheck(checks.single())
    }

    @Test
    fun `list encodes all filters`() = test {
        enqueueJson("""{"data":[]}""")

        client().hc2.checks.list(
            type = "http",
            state = Hc2CheckStateFilter.DOWN,
            execution = Hc2Execution.LOCAL,
            nodeId = "node-1",
            tag = "prod",
            group = "sites",
            q = "my site",
        )

        assertEquals(
            "/hc2/checks?type=http&state=down&execution=local&node_id=node-1&tag=prod&group=sites&q=my%20site",
            takeRequest().apiTarget,
        )
    }

    @Test
    fun `create sends input and omits null fields`() = test {
        enqueueJson(
            """{"data":${checkJson.dropLast(1)},"sources":[{"source":"loc:nbg","label":"Nuernberg","state":"up",
                "last_latency_ms":40.0,"consecutive_ok":10,"consecutive_fail":0}],
                "availability_percent":{"24h":99.9,"30d":99.5,"1y":98.0}}}""",
            code = 201,
        )

        val detail = client().hc2.checks.create(
            Hc2CheckInput(
                name = "Website",
                type = Hc2CheckType.HTTP,
                target = "https://example.org",
                params = Hc2CheckParams(method = "GET", acceptStatus = "200-299"),
                execution = Hc2Execution.ONLINE,
                locations = listOf("nbg", "fsn"),
                policy = Hc2Policy.MAJORITY,
                channels = listOf(1, 2),
            ),
        )

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/hc2/checks", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals(
            setOf("name", "type", "target", "params", "execution", "locations", "policy", "channels"),
            sent.keys,
        )
        assertEquals("http", sent["type"]?.jsonPrimitive?.content)
        assertEquals("majority", sent["policy"]?.jsonPrimitive?.content)
        assertEquals(setOf("method", "accept_status"), sent["params"]!!.jsonObject.keys)
        assertEquals(listOf("nbg", "fsn"), sent["locations"]!!.jsonArray.map { it.jsonPrimitive.content })
        assertEquals("Website", detail.name)
        assertEquals(Hc2CheckType.HTTP, detail.type)
        assertEquals("loc:nbg", detail.sources.single().source)
        assertEquals(10L, detail.sources.single().consecutiveOk)
        assertEquals(99.9, detail.availabilityPercent?.last24h)
        assertEquals(99.5, detail.availabilityPercent?.last30d)
        assertEquals(98.0, detail.availabilityPercent?.last1y)
    }

    @Test
    fun `get returns detail`() = test {
        enqueueJson("""{"data":${checkJson.dropLast(1)},"sources":[],"availability_percent":{"24h":100.0}}}""")

        val detail = client().hc2.checks.get(token)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/hc2/checks/$token", request.apiTarget)
        assertEquals(token, detail.token)
        assertEquals(100.0, detail.availabilityPercent?.last24h)
    }

    @Test
    fun `update patches only set properties`() = test {
        enqueueJson("""{"data":$checkJson}""")

        client().hc2.checks.update(token, Hc2CheckUpdate(paused = true, forceMove = false))

        val request = takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/hc2/checks/$token", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals(setOf("paused", "force_move"), sent.keys)
        assertTrue(sent["paused"]!!.jsonPrimitive.boolean)
        assertFalse(sent["force_move"]!!.jsonPrimitive.boolean)
    }

    @Test
    fun `delete accepts 204`() = test {
        enqueueNoContent()

        client().hc2.checks.delete(token)

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/hc2/checks/$token", request.apiTarget)
    }

    @Test
    fun `pause posts and returns check`() = test {
        enqueueJson("""{"data":$checkJson}""")

        val check = client().hc2.checks.pause(token)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/hc2/checks/$token/pause", request.apiTarget)
        assertCheck(check)
    }

    @Test
    fun `resume posts and returns check`() = test {
        enqueueJson("""{"data":$checkJson}""")

        client().hc2.checks.resume(token)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/hc2/checks/$token/resume", request.apiTarget)
    }

    @Test
    fun `run decodes queue result`() = test {
        enqueueJson("""{"data":{"queued":true,"command_id":55,"node_id":"node-1","detail":"queued"}}""", code = 202)

        val result = client().hc2.checks.run(token)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/hc2/checks/$token/run", request.apiTarget)
        assertEquals(true, result.queued)
        assertEquals(55L, result.commandId)
        assertEquals("node-1", result.nodeId)
    }

    @Test
    fun `move posts request and returns testrun`() = test {
        enqueueJson("""{"data":{"id":88,"status":"running","applied":false,"results":[]}}""", code = 202)

        val run = client().hc2.checks.move(
            token,
            Hc2MoveCheckRequest(execution = Hc2Execution.LOCAL, nodeId = "node-1"),
        )

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/hc2/checks/$token/move", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals(setOf("execution", "node_id"), sent.keys)
        assertEquals("local", sent["execution"]?.jsonPrimitive?.content)
        assertEquals(88L, run.id)
        assertEquals("running", run.status)
    }

    @Test
    fun `measurements decodes and encodes query`() = test {
        enqueueJson(
            """{"data":[{"time":"2026-10-10T12:00:00+02:00","source":"loc:nbg","source_label":"Nuernberg",
                "status":"ok","latency_ms":12.5,"family":"ipv4","details":{"code":200}}]}""",
        )

        val list = client().hc2.checks.measurements(
            token,
            limit = 50,
            since = Instant.parse("2026-10-10T00:00:00Z"),
            until = Instant.parse("2026-10-11T00:00:00Z"),
            source = "loc:nbg",
        )

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals(
            "/hc2/checks/$token/measurements?limit=50&since=2026-10-10T00%3A00%3A00Z" +
                "&until=2026-10-11T00%3A00%3A00Z&source=loc%3Anbg",
            request.apiTarget,
        )
        val m = list.single()
        assertEquals(Instant.parse("2026-10-10T10:00:00Z"), m.time)
        assertEquals(12.5, m.latencyMs)
        assertEquals(200, m.details?.get("code")?.jsonPrimitive?.int)
    }

    @Test
    fun `measurements without query`() = test {
        enqueueJson("""{"data":[]}""")

        client().hc2.checks.measurements(token)

        assertEquals("/hc2/checks/$token/measurements", takeRequest().apiTarget)
    }

    @Test
    fun `events returns page with meta`() = test {
        enqueueJson(
            """{"data":[{"id":3,"kind":"state_change","to_state":"up","time":"2026-10-10T12:00:00Z"}],
                "meta":{"limit":20,"has_more":false,"next_after":3}}""",
        )

        val page = client().hc2.checks.events(token, limit = 20, before = 9, after = 1, since = Instant.parse("2026-10-10T00:00:00Z"))

        assertEquals(
            "/hc2/checks/$token/events?limit=20&before=9&after=1&since=2026-10-10T00%3A00%3A00Z",
            takeRequest().apiTarget,
        )
        assertEquals(3L, page.data.single().id)
        assertEquals(false, page.meta?.hasMore)
        assertEquals(3L, page.meta?.nextAfter)
        assertNull(page.meta?.nextBefore)
    }

    @Test
    fun `incidents of a check`() = test {
        enqueueJson("""{"data":[{"id":1,"status":"closed","started_at":"2026-10-10T12:00:00Z","ended_at":"2026-10-10T12:05:00Z"}]}""")

        val incidents = client().hc2.checks.incidents(token, limit = 10)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/hc2/checks/$token/incidents?limit=10", request.apiTarget)
        assertEquals(Instant.parse("2026-10-10T12:05:00Z"), incidents.single().endedAt)
    }

    @Test
    fun `stats encodes range wire value and decodes series`() = test {
        enqueueJson(
            """{"data":{"range":"7d","bucket":"1h","measurements":1000,"availability_percent":99.7,
                "sources":[{"source":"loc:nbg","label":"Nuernberg","measurements":500,"availability_percent":99.8,
                  "latency_ms":{"min":5.0,"avg":12.0,"max":80.0}}],
                "series":[{"time":"2026-10-10T12:00:00Z","source":"loc:nbg","measurements":60,
                  "availability_percent":100.0,"latency_avg_ms":11.0,"latency_max_ms":30.0}]}}""",
        )

        val stats = client().hc2.checks.stats(token, Hc2StatsRange.DAYS_7)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/hc2/checks/$token/stats?range=7d", request.apiTarget)
        assertEquals("1h", stats.bucket)
        assertEquals(1000L, stats.measurements)
        assertEquals(99.7, stats.availabilityPercent)
        assertEquals(80.0, stats.sources.single().latencyMs?.max)
        assertEquals(Instant.parse("2026-10-10T12:00:00Z"), stats.series.single().time)
        assertEquals(11.0, stats.series.single().latencyAvgMs)
    }

    @Test
    fun `stats without range has no query`() = test {
        enqueueJson("""{"data":{"range":"24h"}}""")

        client().hc2.checks.stats(token)

        assertEquals("/hc2/checks/$token/stats", takeRequest().apiTarget)
    }

    @Test
    fun `toString of check params redacts password and header values`() {
        val text = Hc2CheckParams(basicPassword = "PWSECRET", headers = mapOf("Authorization" to "HDRSECRET")).toString()
        assertFalse(text.contains("PWSECRET"))
        assertFalse(text.contains("HDRSECRET"))
    }
}
