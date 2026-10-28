package de.sr.ipv64.api

import de.sr.ipv64.model.CreateHealthcheckRequest
import de.sr.ipv64.model.Healthcheck
import de.sr.ipv64.model.HealthcheckStatus
import de.sr.ipv64.model.HealthcheckType
import de.sr.ipv64.model.IncidentStatus
import de.sr.ipv64.model.StatsRange
import de.sr.ipv64.model.UpdateHealthcheckRequest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Instant

class HealthchecksApiTest : ApiSdkTestBase() {

    private val tok = "0123456789abcdef0123456789abcdef"

    private val checkJson = """{"token":"$tok","name":"Web","type":"http","status":"down",
        "notify":{"on_down":true,"on_up":false,"integrations":[0,5]},
        "created_at":"2026-01-01T00:00:00Z","last_seen_at":"2026-10-10T10:00:00Z","last_alarm_at":null,
        "pings_total":1234,"failed_in_row":3,
        "push":{"url":"https://ipv64.net/hc/$tok","period_seconds":300,"grace_seconds":60,"expected_by":"2026-10-10T10:05:00Z"},
        "check":{"target":"example.com","prefer_ipv4":true,"port":443,"url":"https://example.com/","verify_tls":true,
        "keyword":"ok","interval_seconds":60,"retry_interval_seconds":10,"failures_before_alarm":3,
        "next_check_at":"2026-10-10T10:01:00Z"}}"""

    private fun assertCheck(check: Healthcheck) {
        assertEquals(tok, check.token)
        assertEquals("Web", check.name)
        assertEquals(HealthcheckType.HTTP, check.type)
        assertEquals(HealthcheckStatus.DOWN, check.status)
        assertEquals(true, check.notify?.onDown)
        assertEquals(false, check.notify?.onUp)
        assertEquals(listOf(0L, 5L), check.notify?.integrations)
        assertEquals(Instant.parse("2026-10-10T10:00:00Z"), check.lastSeenAt)
        assertNull(check.lastAlarmAt)
        assertEquals(1234L, check.pingsTotal)
        assertEquals(3, check.failedInRow)
        assertEquals(300, check.push?.periodSeconds)
        assertEquals(Instant.parse("2026-10-10T10:05:00Z"), check.push?.expectedBy)
        assertEquals("example.com", check.check?.target)
        assertEquals(443, check.check?.port)
        assertEquals(true, check.check?.verifyTls)
        assertEquals(3, check.check?.failuresBeforeAlarm)
        assertEquals(Instant.parse("2026-10-10T10:01:00Z"), check.check?.nextCheckAt)
    }

    @Test
    fun `list without filters`() = test {
        enqueueJson("""{"data":[$checkJson]}""")

        val checks = client().healthchecks.list()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/healthchecks", request.apiTarget)
        assertCheck(checks.single())
    }

    @Test
    fun `list with type and status filters`() = test {
        enqueueJson("""{"data":[]}""")

        client().healthchecks.list(type = HealthcheckType.KEYWORD, status = HealthcheckStatus.WARNING)

        assertEquals("/healthchecks?type=keyword&status=warning", takeRequest().apiTarget)
    }

    @Test
    fun `create sends wire names and omits nulls`() = test {
        enqueueJson(
            """{"data":${checkJson.dropLast(1)},"recent":[{"id":9,"time":"2026-10-10T10:00:00Z","status":"up",
            "message":"ok","latency_ms":12.5,"payload":{"code":200}}]}}""",
            code = 201,
        )

        val detail = client().healthchecks.create(
            CreateHealthcheckRequest(
                name = "Web",
                type = HealthcheckType.HTTP,
                integrations = listOf(0, 5),
                notifyOnDown = true,
                notifyOnUp = false,
                url = "https://example.com/",
                verifyTls = true,
                intervalSeconds = 60,
                retryIntervalSeconds = 10,
                failuresBeforeAlarm = 3,
            ),
        )

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/healthchecks", request.apiTarget)
        val body = request.jsonBody()
        assertEquals(
            setOf(
                "name", "type", "integrations", "notify_on_down", "notify_on_up", "url", "verify_tls",
                "interval_seconds", "retry_interval_seconds", "failures_before_alarm",
            ),
            body.keys,
        )
        assertEquals(JsonPrimitive("http"), body["type"])
        assertEquals(JsonArray(listOf(JsonPrimitive(0), JsonPrimitive(5))), body["integrations"])
        assertEquals(JsonPrimitive(false), body["notify_on_up"])
        assertEquals(JsonPrimitive(60), body["interval_seconds"])
        assertEquals(tok, detail.token)
        assertEquals(HealthcheckType.HTTP, detail.type)
        val event = detail.recent.single()
        assertEquals(9L, event.id)
        assertEquals(HealthcheckStatus.UP, event.status)
        assertEquals(12.5, event.latencyMs)
        assertEquals(JsonPrimitive(200), event.payload?.get("code"))
    }

    @Test
    fun `get requests the check by token`() = test {
        enqueueJson("""{"data":$checkJson}""")

        val detail = client().healthchecks.get(tok)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/healthchecks/$tok", request.apiTarget)
        assertEquals(tok, detail.token)
        assertEquals(emptyList<Any>(), detail.recent)
    }

    @Test
    fun `get encodes special characters in the token segment`() = test {
        enqueueJson("""{"data":$checkJson}""")

        client().healthchecks.get("a b/c")

        assertEquals("/healthchecks/a%20b%2Fc", takeRequest().apiTarget)
    }

    @Test
    fun `update sends only non-null fields`() = test {
        enqueueJson("""{"data":$checkJson}""")

        client().healthchecks.update(tok, UpdateHealthcheckRequest(name = "Renamed", periodSeconds = 600))

        val request = takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/healthchecks/$tok", request.apiTarget)
        val body = request.jsonBody()
        assertEquals(setOf("name", "period_seconds"), body.keys)
        assertEquals(JsonPrimitive("Renamed"), body["name"])
        assertEquals(JsonPrimitive(600), body["period_seconds"])
    }

    @Test
    fun `delete requests DELETE and accepts 204`() = test {
        enqueueNoContent()

        client().healthchecks.delete(tok)

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/healthchecks/$tok", request.apiTarget)
    }

    @Test
    fun `pause posts to pause`() = test {
        enqueueJson("""{"data":${checkJson.replace("\"down\"", "\"paused\"")}}""")

        val check = client().healthchecks.pause(tok)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/healthchecks/$tok/pause", request.apiTarget)
        assertEquals(HealthcheckStatus.PAUSED, check.status)
    }

    @Test
    fun `resume posts to resume`() = test {
        enqueueJson("""{"data":${checkJson.replace("\"down\"", "\"stabilizing\"")}}""")

        val check = client().healthchecks.resume(tok)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/healthchecks/$tok/resume", request.apiTarget)
        assertEquals(HealthcheckStatus.STABILIZING, check.status)
    }

    @Test
    fun `rotateToken posts to rotate-token and returns the new token`() = test {
        enqueueJson("""{"data":${checkJson.replace(tok, "fedcba9876543210fedcba9876543210")}}""")

        val check = client().healthchecks.rotateToken(tok)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/healthchecks/$tok/rotate-token", request.apiTarget)
        assertEquals("fedcba9876543210fedcba9876543210", check.token)
    }

    @Test
    fun `events without parameters sends no query`() = test {
        enqueueJson(
            """{"data":[{"id":3,"time":"2026-10-10T10:00:00Z","status":"down","message":"timeout","latency_ms":null}],
            "meta":{"limit":100,"has_more":true,"next_before":3,"next_after":null}}""",
        )

        val page = client().healthchecks.events(tok)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/healthchecks/$tok/events", request.apiTarget)
        assertEquals(1, page.data.size)
        assertEquals(HealthcheckStatus.DOWN, page.data[0].status)
        assertNull(page.data[0].latencyMs)
        assertEquals(100, page.meta?.limit)
        assertEquals(true, page.meta?.hasMore)
        assertEquals(3L, page.meta?.nextBefore)
        assertNull(page.meta?.nextAfter)
    }

    @Test
    fun `events sends all cursor parameters`() = test {
        enqueueJson("""{"data":[],"meta":{"limit":10,"has_more":false}}""")

        client().healthchecks.events(tok, limit = 10, before = 50L, after = 20L, since = Instant.parse("2026-10-01T00:00:00Z"))

        val request = takeRequest()
        assertEquals(
            "/healthchecks/$tok/events?limit=10&before=50&after=20&since=2026-10-01T00%3A00%3A00Z",
            request.apiTarget,
        )
        assertEquals("2026-10-01T00:00:00Z", request.url.queryParameter("since"))
    }

    @Test
    fun `incidents sends limit and decodes events`() = test {
        enqueueJson(
            """{"data":[{"id":1,"status":"closed","created_at":"2026-10-09T10:00:00Z","closed_at":"2026-10-09T10:30:00Z",
            "events":[{"time":"2026-10-09T10:00:00Z","kind":"down","message":"timeout"},
            {"time":"2026-10-09T10:30:00Z","kind":"up","message":"back"}]},
            {"id":2,"status":"open","created_at":"2026-10-10T10:00:00Z","closed_at":null,"events":[]}]}""",
        )

        val incidents = client().healthchecks.incidents(tok, limit = 25)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/healthchecks/$tok/incidents?limit=25", request.apiTarget)
        assertEquals(2, incidents.size)
        assertEquals(IncidentStatus.CLOSED, incidents[0].status)
        assertEquals(Instant.parse("2026-10-09T10:30:00Z"), incidents[0].closedAt)
        assertEquals(2, incidents[0].events.size)
        assertEquals("down", incidents[0].events[0].kind)
        assertEquals(IncidentStatus.OPEN, incidents[1].status)
        assertNull(incidents[1].closedAt)
    }

    @Test
    fun `incidents without limit sends no query`() = test {
        enqueueJson("""{"data":[]}""")

        client().healthchecks.incidents(tok)

        assertEquals("/healthchecks/$tok/incidents", takeRequest().apiTarget)
    }

    @Test
    fun `stats sends range and decodes series`() = test {
        enqueueJson(
            """{"data":{"range":"7d","from":"2026-10-03T00:00:00Z","to":"2026-10-10T00:00:00Z","measurements":1000,
            "availability_percent":99.95,"latency_ms":{"avg":20.5,"min":5.0,"max":300.0},"bucket_seconds":3600,
            "series":[{"time":"2026-10-03T00:00:00Z","measurements":60,"availability_percent":100.0,
            "latency_avg_ms":19.0,"latency_max_ms":80.0}]}}""",
        )

        val stats = client().healthchecks.stats(tok, StatsRange.DAYS_7)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/healthchecks/$tok/stats?range=7d", request.apiTarget)
        assertEquals("7d", stats.range)
        assertEquals(Instant.parse("2026-10-03T00:00:00Z"), stats.from)
        assertEquals(1000, stats.measurements)
        assertEquals(99.95, stats.availabilityPercent)
        assertEquals(20.5, stats.latencyMs?.avg)
        assertEquals(300.0, stats.latencyMs?.max)
        assertEquals(3600, stats.bucketSeconds)
        val point = stats.series.single()
        assertEquals(60, point.measurements)
        assertEquals(80.0, point.latencyMaxMs)
    }

    @Test
    fun `stats without range sends no query`() = test {
        enqueueJson("""{"data":{"range":"24h","series":[]}}""")

        client().healthchecks.stats(tok)

        assertEquals("/healthchecks/$tok/stats", takeRequest().apiTarget)
    }

    @Test
    fun `testNotification posts and decodes sent flag`() = test {
        enqueueJson("""{"data":{"sent":true}}""")

        val result = client().healthchecks.testNotification(tok)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/healthchecks/$tok/test-notification", request.apiTarget)
        assertEquals(true, result.sent)
    }
}
