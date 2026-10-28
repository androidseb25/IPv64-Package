package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.BulkCheckAction
import de.sr.ipv64.node64.model.BulkCheckRequest
import de.sr.ipv64.node64.model.Check
import de.sr.ipv64.node64.model.CheckExecution
import de.sr.ipv64.node64.model.CheckIpMode
import de.sr.ipv64.node64.model.CheckOwner
import de.sr.ipv64.node64.model.CheckPolicy
import de.sr.ipv64.node64.model.CheckRequest
import de.sr.ipv64.node64.model.CheckStateValue
import de.sr.ipv64.node64.model.CheckType
import de.sr.ipv64.node64.model.ChartBucket
import de.sr.ipv64.node64.model.ChartRange
import de.sr.ipv64.node64.model.MeasurementStatus
import de.sr.ipv64.node64.model.OnlineTestRequest
import de.sr.ipv64.node64.model.OnlineTestStatus
import de.sr.ipv64.node64.model.PushStatus
import de.sr.ipv64.node64.model.StatsRange
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class ChecksApiTest : Node64ApiTestBaseA() {

    private val checkJson = """{"token":"tok123","name":"Web","type":"http","target":"https://example.com",
        "params":{"expect_status":200,"secret_header":"PARAM-SECRET"},"interval_seconds":60,"retry_interval_seconds":10,
        "retries":2,"connect_timeout_seconds":3.5,"timeout_seconds":10,"invert":false,"stabilize":1,"ip_mode":"both",
        "tags":["prod","web"],"description":"main site","paused":false,"parent":"par1","critical":true,"execution":"both",
        "policy":"majority","locations":["node","fra"],"notify_degraded":true,"notify_down":true,"notify_up":false,
        "channels":[1,2],"repeat_minutes":30,"revision":4,"created_at":"2026-09-01T10:00:00Z","updated_at":"",
        "push_url":"https://node/api/push/tok123","group":"g1","nodes":["n-1"],"owner":"self",
        "state":{"state":"down","since":"2026-10-28T08:00:00Z","last_check_at":"2026-10-28T09:59:00Z","next_check_at":"",
        "last_status":"down","last_latency_ms":123.4,"last_reason":"timeout","last_message":"timed out","consecutive_fail":3,
        "consecutive_ok":0,"last_push_at":"","down_since":"2026-10-28T08:00:00Z","incident_id":77,"maintenance":false}}"""

    private fun assertCheck(c: Check) {
        assertEquals("tok123", c.token)
        assertEquals(CheckType.HTTP, c.type)
        assertEquals("https://example.com", c.target)
        assertEquals(JsonPrimitive(200), c.params!!["expect_status"])
        assertEquals(60, c.intervalSeconds)
        assertEquals(10, c.retryIntervalSeconds)
        assertEquals(2, c.retries)
        assertEquals(3.5, c.connectTimeoutSeconds!!, 0.0)
        assertEquals(10.0, c.timeoutSeconds!!, 0.0)
        assertEquals(CheckIpMode.BOTH, c.ipMode)
        assertEquals(listOf("prod", "web"), c.tags)
        assertEquals(CheckExecution.BOTH, c.execution)
        assertEquals(CheckPolicy.MAJORITY, c.policy)
        assertEquals(listOf("node", "fra"), c.locations)
        assertEquals(listOf(1L, 2L), c.channels)
        assertEquals(30, c.repeatMinutes)
        assertEquals(4L, c.revision)
        assertEquals(Instant.parse("2026-09-01T10:00:00Z"), c.createdAt)
        assertNull(c.updatedAt)
        assertEquals("https://node/api/push/tok123", c.pushUrl)
        assertEquals(CheckOwner.SELF, c.owner)
        assertEquals(listOf("n-1"), c.nodes)
        val s = c.state!!
        assertEquals(CheckStateValue.DOWN, s.state)
        assertEquals(Instant.parse("2026-10-28T08:00:00Z"), s.since)
        assertEquals(Instant.parse("2026-10-28T09:59:00Z"), s.lastCheckAt)
        assertNull(s.nextCheckAt)
        assertNull(s.lastPushAt)
        assertEquals(123.4, s.lastLatencyMs!!, 0.0)
        assertEquals(3, s.consecutiveFail)
        assertEquals(77L, s.incidentId)
    }

    @Test
    fun `list without filters has no query`() = test {
        enqueueJson("[$checkJson]")

        val checks = node().checks.list()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/checks", request.target)
        assertCheck(checks.single())
    }

    @Test
    fun `list sends all filters`() = test {
        enqueueJson("[]")

        node().checks.list(type = CheckType.KEYWORD, state = CheckStateValue.FLAPPING, tag = "prod", q = "my web")

        assertEquals("/api/checks?type=keyword&state=flapping&tag=prod&q=my%20web", takeRequest().target)
    }

    @Test
    fun `check toString redacts params and push token`() = test {
        enqueueJson("""[$checkJson,{"token":"PUSH-TOKEN-1","name":"hb","type":"push","push_url":"https://n/api/push/PUSH-TOKEN-1"}]""")

        val checks = node().checks.list()

        assertFalse(checks[0].toString().contains("PARAM-SECRET"))
        assertFalse(checks[0].toString().contains("/api/push/"))
        assertTrue(checks[0].toString().contains("tok123"))
        assertFalse(checks[1].toString().contains("PUSH-TOKEN-1"))
    }

    @Test
    fun `create posts request without nulls and decodes check`() = test {
        enqueueJson(checkJson, code = 201)
        val params = buildJsonObject { put("expect_status", JsonPrimitive(200)) }

        val check = node().checks.create(
            CheckRequest(
                name = "Web",
                type = CheckType.HTTP,
                target = "https://example.com",
                params = params,
                intervalSeconds = 60,
                timeoutSeconds = 10.0,
                ipMode = CheckIpMode.IPV4,
                tags = listOf("prod"),
                execution = CheckExecution.ONLINE,
                policy = CheckPolicy.ANY,
                locations = listOf("fra"),
                channels = listOf(1L),
                invert = false,
            ),
        )

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/checks", request.target)
        assertTrue(request.headers["Content-Type"]!!.startsWith("application/json"))
        request.assertJsonBody(
            """{"name":"Web","type":"http","target":"https://example.com","params":{"expect_status":200},
            "interval_seconds":60,"timeout_seconds":10.0,"invert":false,"ip_mode":"ipv4","tags":["prod"],
            "execution":"online","policy":"any","locations":["fra"],"channels":[1]}""",
        )
        assertCheck(check!!)
    }

    @Test
    fun `create returns null for empty 201 body`() = test {
        enqueueEmpty(201)

        val check = node().checks.create(CheckRequest("x", CheckType.PUSH))

        assertNull(check)
        takeRequest().assertJsonBody("""{"name":"x","type":"push"}""")
    }

    @Test
    fun `request toString redacts params`() {
        val params = buildJsonObject { put("password", JsonPrimitive("hunter2")) }

        assertFalse(CheckRequest("x", CheckType.HTTP, params = params).toString().contains("hunter2"))
        assertFalse(OnlineTestRequest("x", CheckType.HTTP, token = "SECRET-TOK", params = params).toString().let {
            it.contains("hunter2") || it.contains("SECRET-TOK")
        })
    }

    @Test
    fun `get fetches by token`() = test {
        enqueueJson(checkJson)

        assertCheck(node().checks.get("tok123"))

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/checks/tok123", request.target)
    }

    @Test
    fun `update puts request and returns null for empty body`() = test {
        enqueueEmpty()

        val result = node().checks.update("tok123", CheckRequest("Web2", CheckType.TCP, target = "example.com:22", retries = 1))

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/checks/tok123", request.target)
        request.assertJsonBody("""{"name":"Web2","type":"tcp","target":"example.com:22","retries":1}""")
        assertNull(result)
    }

    @Test
    fun `update decodes check when body present`() = test {
        enqueueJson(checkJson)

        assertCheck(node().checks.update("tok123", CheckRequest("Web", CheckType.HTTP))!!)
    }

    @Test
    fun `delete sends DELETE`() = test {
        enqueueNoContent()

        node().checks.delete("tok123")

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/checks/tok123", request.target)
    }

    @Test
    fun `pause posts and decodes check`() = test {
        enqueueJson(checkJson)

        assertCheck(node().checks.pause("tok123"))

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/checks/tok123/pause", request.target)
    }

    @Test
    fun `resume posts and decodes check`() = test {
        enqueueJson(checkJson)

        assertCheck(node().checks.resume("tok123"))

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/checks/tok123/resume", request.target)
    }

    @Test
    fun `run posts and ignores body`() = test {
        enqueueEmpty(202)

        node().checks.run("tok123")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/checks/tok123/run", request.target)
    }

    @Test
    fun `clone posts and decodes the copy`() = test {
        enqueueJson(checkJson, code = 201)

        assertCheck(node().checks.clone("tok123"))

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/checks/tok123/clone", request.target)
    }

    @Test
    fun `measurements without params has no query and decodes values`() = test {
        enqueueJson(
            """[{"seq":10,"time":"2026-10-28T09:00:00Z","status":"up","latency_ms":42.5,"reason":"","message":"ok",
            "family":"ipv4","details":{"status_code":200},"maintenance":false,"dependent":false},
            {"seq":9,"time":"","status":"down","reason":"timeout","maintenance":true,"dependent":true}]""",
        )

        val list = node().checks.measurements("tok123")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/checks/tok123/measurements", request.target)
        assertEquals(2, list.size)
        assertEquals(10L, list[0].seq)
        assertEquals(Instant.parse("2026-10-28T09:00:00Z"), list[0].time)
        assertEquals(MeasurementStatus.UP, list[0].status)
        assertEquals(42.5, list[0].latencyMs!!, 0.0)
        assertEquals("ipv4", list[0].family)
        assertEquals(JsonPrimitive(200), list[0].details!!["status_code"])
        assertNull(list[1].time)
        assertEquals(MeasurementStatus.DOWN, list[1].status)
        assertEquals(true, list[1].maintenance)
        assertEquals(true, list[1].dependent)
    }

    @Test
    fun `measurements sends all params`() = test {
        enqueueJson("[]")

        node().checks.measurements("tok123", limit = 100, before = 50, from = "2026-10-01T00:00:00Z", to = "2026-10-02T00:00:00Z")

        assertEquals(
            "/api/checks/tok123/measurements?limit=100&before=50&from=2026-10-01T00%3A00%3A00Z&to=2026-10-02T00%3A00%3A00Z",
            takeRequest().target,
        )
    }

    private val eventJson = """{"id":5,"event_id":"ev-5","token":"tok123","kind":"state","time":"2026-10-28T08:00:00Z",
        "from":"up","to":"down","reason":"timeout","message":"m","duration_seconds":600,"flaps":2,"maintenance":false,
        "silent":true,"incident_id":77}"""

    private fun assertEvent(e: de.sr.ipv64.node64.model.Event) {
        assertEquals(5L, e.id)
        assertEquals("ev-5", e.eventId)
        assertEquals("tok123", e.token)
        assertEquals("state", e.kind)
        assertEquals(Instant.parse("2026-10-28T08:00:00Z"), e.time)
        assertEquals("up", e.from)
        assertEquals("down", e.to)
        assertEquals(600L, e.durationSeconds)
        assertEquals(2, e.flaps)
        assertEquals(true, e.silent)
        assertEquals(77L, e.incidentId)
    }

    @Test
    fun `events sends limit and before`() = test {
        enqueueJson("[$eventJson]")

        val events = node().checks.events("tok123", limit = 20, before = 9)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/checks/tok123/events?limit=20&before=9", request.target)
        assertEvent(events.single())
    }

    @Test
    fun `events without params has no query`() = test {
        enqueueJson("[]")

        node().checks.events("tok123")

        assertEquals("/api/checks/tok123/events", takeRequest().target)
    }

    private val incidentJson = """{"id":77,"token":"tok123","started_at":"2026-10-28T08:00:00Z","ended_at":"",
        "open":true,"reason":"timeout","message":"m"}"""

    @Test
    fun `incidents sends limit and decodes open incident`() = test {
        enqueueJson("[$incidentJson]")

        val incidents = node().checks.incidents("tok123", limit = 5)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/checks/tok123/incidents?limit=5", request.target)
        val i = incidents.single()
        assertEquals(77L, i.id)
        assertEquals(Instant.parse("2026-10-28T08:00:00Z"), i.startedAt)
        assertNull(i.endedAt)
        assertEquals(true, i.open)
    }

    @Test
    fun `stats without range has no query and decodes series`() = test {
        enqueueJson(
            """{"range":"24h","from":"2026-10-27T10:00:00Z","to":"2026-10-28T10:00:00Z","count":1440,"up_count":1430,
            "up_percent":99.3,"avg_ms":50.5,"min_ms":10,"max_ms":900.0,"bucket_seconds":3600,
            "series":[{"time":"2026-10-27T10:00:00Z","count":60,"up_count":60,"avg_ms":48.0,"min_ms":11.0,"max_ms":120.0,"up_percent":100}]}""",
        )

        val stats = node().checks.stats("tok123")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/checks/tok123/stats", request.target)
        assertEquals("24h", stats.range)
        assertEquals(Instant.parse("2026-10-27T10:00:00Z"), stats.from)
        assertEquals(1440L, stats.count)
        assertEquals(99.3, stats.upPercent!!, 0.0)
        assertEquals(10.0, stats.minMs!!, 0.0)
        assertEquals(3600L, stats.bucketSeconds)
        val p = stats.series.single()
        assertEquals(Instant.parse("2026-10-27T10:00:00Z"), p.time)
        assertEquals(60L, p.count)
        assertEquals(100.0, p.upPercent!!, 0.0)
    }

    @Test
    fun `stats sends range value`() = test {
        enqueueJson("{}")

        node().checks.stats("tok123", StatsRange.DAYS_7)

        assertEquals("/api/checks/tok123/stats?range=7d", takeRequest().target)
    }

    @Test
    fun `chart sends range and decodes series`() = test {
        enqueueJson(
            """{"range":"6h","bucket":"hour","series":[{"source":"node","label":"Home",
            "points":[{"t":"2026-10-28T09:00:00Z","ms":40.5,"up":1},{"t":"2026-10-28T10:00:00Z","ms":null,"up":0.5}]}]}""",
        )

        val chart = node().checks.chart("tok123", ChartRange.HOURS_6)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/checks/tok123/chart?range=6h", request.target)
        assertEquals(ChartBucket.HOUR, chart.bucket)
        val series = chart.series.single()
        assertEquals("node", series.source)
        assertEquals("Home", series.label)
        assertEquals(2, series.points.size)
        assertEquals(Instant.parse("2026-10-28T09:00:00Z"), series.points[0].t)
        assertEquals(40.5, series.points[0].ms!!, 0.0)
        assertNull(series.points[1].ms)
        assertEquals(0.5, series.points[1].up!!, 0.0)
    }

    @Test
    fun `allEvents uses global endpoint`() = test {
        enqueueJson("[$eventJson]")

        val events = node().checks.allEvents(limit = 10, before = 100)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/events?limit=10&before=100", request.target)
        assertEvent(events.single())
    }

    @Test
    fun `allIncidents uses global endpoint`() = test {
        enqueueJson("[$incidentJson]")

        val incidents = node().checks.allIncidents(limit = 3)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/incidents?limit=3", request.target)
        assertEquals(77L, incidents.single().id)
    }

    @Test
    fun `allIncidents and allEvents without params have no query`() = test {
        enqueueJson("[]")
        enqueueJson("[]")

        node().checks.allIncidents()
        node().checks.allEvents()

        assertEquals("/api/incidents", takeRequest().target)
        assertEquals("/api/events", takeRequest().target)
    }

    @Test
    fun `types returns undocumented json or null`() = test {
        enqueueJson("""[{"type":"http","fields":[]}]""")
        enqueueEmpty()

        val types = node().checks.types()
        val none = node().checks.types()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/check-types", request.target)
        assertEquals(1, types!!.jsonArray.size)
        assertNull(none)
    }

    @Test
    fun `bulk with request object posts action tokens and tag`() = test {
        enqueueJson("""{"done":2,"failed":[{"token":"b","error":"not found"}]}""")

        val result = node().checks.bulk(BulkCheckRequest(BulkCheckAction.TAG, listOf("a", "b", "c"), "prod"))

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/checks/bulk", request.target)
        request.assertJsonBody("""{"action":"tag","tokens":["a","b","c"],"tag":"prod"}""")
        assertEquals(2, result.done)
        assertEquals("b", result.failed.single().token)
        assertEquals("not found", result.failed.single().error)
    }

    @Test
    fun `bulk convenience overload omits tag when null`() = test {
        enqueueJson("""{"done":1}""")

        val result = node().checks.bulk(BulkCheckAction.PAUSE, listOf("a"))

        val request = takeRequest()
        assertEquals("/api/checks/bulk", request.target)
        request.assertJsonBody("""{"action":"pause","tokens":["a"]}""")
        assertEquals(1, result.done)
        assertTrue(result.failed.isEmpty())
    }

    @Test
    fun `onlineTest posts request and decodes location results`() = test {
        enqueueJson(
            """{"status":"failed","error":"one location failed","results":[
            {"location":"fra","ok":true,"message":"200 OK"},{"location":"nyc","ok":false,"message":"timeout"}]}""",
        )

        val result = node().checks.onlineTest(
            OnlineTestRequest(
                name = "Web",
                type = CheckType.HTTP,
                target = "https://example.com",
                locations = listOf("fra", "nyc"),
                timeoutSeconds = 5.0,
            ),
        )

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/checks/online-test", request.target)
        request.assertJsonBody(
            """{"name":"Web","type":"http","target":"https://example.com","locations":["fra","nyc"],"timeout_seconds":5.0}""",
        )
        assertEquals(OnlineTestStatus.FAILED, result.status)
        assertEquals("one location failed", result.error)
        assertEquals(2, result.results.size)
        assertEquals("fra", result.results[0].location)
        assertEquals(true, result.results[0].ok)
        assertEquals(false, result.results[1].ok)
        assertEquals("timeout", result.results[1].message)
    }

    @Test
    fun `push uses GET with status and msg query`() = test {
        enqueueJson("""{"status":"ok"}""")

        val result = node().checks.push("tok123", PushStatus.DOWN, "disk full")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/push/tok123?status=down&msg=disk%20full", request.target)
        assertTrue(request.bodyBytes().isEmpty())
        assertEquals("ok", result.status)
        assertNull(result.error)
    }

    @Test
    fun `push without params has no query`() = test {
        enqueueJson("""{"status":"ok"}""")

        node().checks.push("tok123")

        assertEquals("/api/push/tok123", takeRequest().target)
    }

    @Test
    fun `pushPayload text posts text plain body`() = test {
        enqueueJson("""{"status":"ok"}""")

        val result = node().checks.pushPayload("tok123", message = "backup done: 12 files", status = PushStatus.UP, msg = "m")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/push/tok123?status=up&msg=m", request.target)
        assertTrue(request.headers["Content-Type"]!!.startsWith("text/plain"))
        assertEquals("backup done: 12 files", request.bodyText())
        assertEquals("ok", result.status)
    }

    @Test
    fun `pushPayload text without message sends empty text body`() = test {
        enqueueJson("""{"status":"ok"}""")

        node().checks.pushPayload("tok123")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/push/tok123", request.target)
        assertTrue(request.headers["Content-Type"]!!.startsWith("text/plain"))
        assertEquals("", request.bodyText())
    }

    @Test
    fun `pushPayload json posts json body`() = test {
        enqueueJson("""{"status":"ok"}""")
        val payload = buildJsonObject {
            put("files", JsonPrimitive(12))
            put("ok", JsonPrimitive(true))
        }

        node().checks.pushPayload("tok123", payload, status = PushStatus.UP)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/push/tok123?status=up", request.target)
        assertTrue(request.headers["Content-Type"]!!.startsWith("application/json"))
        request.assertJsonBody("""{"files":12,"ok":true}""")
    }

    @Test
    fun `push error maps to Node64ApiException`() = test {
        server.enqueue(
            mockwebserver3.MockResponse.Builder().code(404).setHeader("Content-Type", "application/json")
                .body("""{"error":"unknown token"}""").build(),
        )

        val e = runCatching { node().checks.push("nope") }.exceptionOrNull()

        e as de.sr.ipv64.Node64ApiException
        assertTrue(e.isNotFound)
        assertEquals("unknown token", e.error)
    }
}
