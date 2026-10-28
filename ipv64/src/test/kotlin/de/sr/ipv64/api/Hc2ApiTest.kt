package de.sr.ipv64.api

import de.sr.ipv64.MockServerTest
import de.sr.ipv64.model.Hc2ChannelType
import de.sr.ipv64.model.Hc2SettingsUpdate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class Hc2ApiTest : MockServerTest() {

    private fun body(text: String): JsonObject = Json.parseToJsonElement(text).jsonObject

    @Test
    fun `account decodes plan limits and locations`() = test {
        enqueueJson(
            """{"data":{"plan":"pro","online_measuring_points":{"used":4,"limit":50},
                "checks_per_day":{"used":1000,"limit":100000},"locations_per_check":2,"sms_per_day":10,
                "locations":[{"code":"nbg","name":"Nuernberg"},{"code":"fsn","name":"Falkenstein"}],
                "nodes":{"total":3,"online":2},"checks":12}}""",
        )

        val account = client().hc2.account()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/hc2/account", request.apiTarget)
        assertEquals("pro", account.plan)
        assertEquals(4L, account.onlineMeasuringPoints?.used)
        assertEquals(100000L, account.checksPerDay?.limit)
        assertEquals(listOf("nbg", "fsn"), account.locations.map { it.code })
        assertEquals(2, account.nodes?.online)
        assertEquals(12, account.checks)
    }

    @Test
    fun `settings returns raw json object`() = test {
        enqueueJson("""{"data":{"notify.quiet_enabled":true,"notify.quiet_from":"22:00","notify.rate_per_hour":30}}""")

        val settings = client().hc2.settings()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/hc2/settings", request.apiTarget)
        assertEquals("22:00", settings["notify.quiet_from"]?.jsonPrimitive?.content)
        assertEquals(30, settings["notify.rate_per_hour"]?.jsonPrimitive?.int)
    }

    @Test
    fun `updateSettings patches only set properties with dotted names`() = test {
        enqueueJson("""{"data":{"notify.quiet_enabled":false}}""")

        val result = client().hc2.updateSettings(Hc2SettingsUpdate(quietEnabled = false, ratePerHour = 20))

        val request = takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/hc2/settings", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals(setOf("notify.quiet_enabled", "notify.rate_per_hour"), sent.keys)
        assertEquals(20, sent["notify.rate_per_hour"]?.jsonPrimitive?.int)
        assertEquals(false, result["notify.quiet_enabled"]?.jsonPrimitive?.boolean)
    }

    @Test
    fun `events sends no query by default and decodes page meta`() = test {
        enqueueJson(
            """{"data":[{"id":101,"event_id":"ev-1","token":"tok","source":"loc:nbg","source_label":"Nuernberg",
                "kind":"state_change","time":"2026-10-10T12:00:00+02:00","from_state":"up","to_state":"down",
                "reason":"timeout","message":"no answer","duration_seconds":60,"flaps":2,"silent":false,
                "incident_id":9}],
               "meta":{"limit":100,"has_more":true,"next_before":101,"next_after":205}}""",
        )

        val page = client().hc2.events()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/hc2/events", request.apiTarget)
        val event = page.data.single()
        assertEquals(101L, event.id)
        assertEquals("ev-1", event.eventId)
        assertEquals("down", event.toState)
        assertEquals(Instant.parse("2026-10-10T10:00:00Z"), event.time)
        assertEquals(60L, event.durationSeconds)
        assertEquals(9L, event.incidentId)
        assertEquals(100, page.meta?.limit)
        assertEquals(true, page.meta?.hasMore)
        assertEquals(101L, page.meta?.nextBefore)
        assertEquals(205L, page.meta?.nextAfter)
    }

    @Test
    fun `events encodes query parameters including ISO instant`() = test {
        enqueueJson("""{"data":[],"meta":{"limit":10,"has_more":false}}""")

        val page = client().hc2.events(
            limit = 10,
            before = 500,
            after = 100,
            since = Instant.parse("2026-10-10T10:00:00Z"),
        )

        assertEquals(
            "/hc2/events?limit=10&before=500&after=100&since=2026-10-10T10%3A00%3A00Z",
            takeRequest().apiTarget,
        )
        assertTrue(page.data.isEmpty())
        assertEquals(false, page.meta?.hasMore)
        assertNull(page.meta?.nextBefore)
    }

    @Test
    fun `incidents decodes list and encodes query`() = test {
        enqueueJson(
            """{"data":[{"id":9,"token":"tok","name":"Website","status":"open",
                "started_at":"2026-10-10T12:00:00+02:00","ended_at":null,"duration_seconds":120,
                "reason":"timeout","message":"x","sources":["loc:nbg","loc:fsn"]}]}""",
        )

        val incidents = client().hc2.incidents(limit = 5, open = true, since = Instant.parse("2026-10-01T00:00:00Z"))

        assertEquals("/hc2/incidents?limit=5&open=true&since=2026-10-01T00%3A00%3A00Z", takeRequest().apiTarget)
        val incident = incidents.single()
        assertEquals("open", incident.status)
        assertNull(incident.endedAt)
        assertEquals(Instant.parse("2026-10-10T10:00:00Z"), incident.startedAt)
        assertEquals(listOf("loc:nbg", "loc:fsn"), incident.sources)
    }

    @Test
    fun `incidents without parameters has no query`() = test {
        enqueueJson("""{"data":[]}""")

        client().hc2.incidents()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/hc2/incidents", request.apiTarget)
    }

    @Test
    fun `testrun decodes results`() = test {
        enqueueJson(
            """{"data":{"id":77,"status":"ok","target":"https://example.org","applied":true,
                "results":[{"source":"loc:nbg","ok":true,"message":"200 OK"},
                           {"source":"loc:fsn","ok":false,"message":"timeout"}],"error":null}}""",
        )

        val run = client().hc2.testrun(77)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/hc2/testruns/77", request.apiTarget)
        assertEquals("ok", run.status)
        assertEquals(true, run.applied)
        assertEquals(2, run.results.size)
        assertEquals(false, run.results[1].ok)
        assertNull(run.error)
    }

    @Test
    fun `channelTypes decodes fields`() = test {
        enqueueJson(
            """{"data":[{"type":"telegram","label":"Telegram","needs_verification":false,
                "fields":[{"key":"bot_token","label":"Bot token","kind":"password","required":true,"secret":true},
                          {"key":"mode","label":"Mode","kind":"select","required":false,"secret":false,
                           "options":["a","b"],"default":"a"}]}]}""",
        )

        val types: List<Hc2ChannelType> = client().hc2.channelTypes()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/hc2/channel-types", request.apiTarget)
        val type = types.single()
        assertEquals("telegram", type.type)
        assertEquals(false, type.needsVerification)
        assertEquals(true, type.fields[0].secret)
        assertEquals(listOf("a", "b"), type.fields[1].options)
        assertEquals("a", type.fields[1].default)
    }
}
