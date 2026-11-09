package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.Sonar64Access
import de.sr.ipv64.node64.model.Sonar64Budget
import de.sr.ipv64.node64.model.Sonar64SettingsRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class Sonar64ApiTest : Node64ApiTestBaseA() {

    @Test
    fun `status decodes node and trust`() = test {
        enqueueJson(
            """{"version":"1.2","running":true,"key_set":true,"participate":true,
            "node":{"node_id":"n1","registered":true,"paused":"quiet","sent_today":5,"plan_expires":"2026-11-01T02:00:00Z"},
            "me":{"lat":1.5},"trust":{"level":"stable","class":"c","need_days":7,"uptime":0.99},"public_url":"https://x"}""",
        )

        val s = node().sonar64.status()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/sonar64/status", request.target)
        assertEquals("1.2", s.version)
        assertEquals("n1", s.node?.nodeId)
        assertEquals("quiet", s.node?.paused)
        assertEquals(5L, s.node?.sentToday)
        assertEquals(Instant.parse("2026-11-01T02:00:00Z"), s.node?.planExpires)
        assertEquals("stable", s.trust?.level)
        assertEquals(7L, s.trust?.needDays)
        assertTrue(s.me != null)
    }

    @Test
    fun `settings decodes`() = test {
        enqueueJson("""{"participate":true,"budget":"normal","quiet":"23-6","lat":null,"access":"dsl"}""")

        val s = node().sonar64.settings()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/sonar64/settings", request.target)
        assertEquals("normal", s.budget)
        assertEquals("23-6", s.quiet)
        assertEquals("dsl", s.access)
    }

    @Test
    fun `updateSettings puts request`() = test {
        enqueueJson("""{"participate":true,"budget":"sparsam"}""")

        val s = node().sonar64.updateSettings(
            Sonar64SettingsRequest(participate = true, budget = Sonar64Budget.SPARSAM, access = Sonar64Access.FIBER, clearLocation = true),
        )

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/sonar64/settings", request.target)
        request.assertJsonBody("""{"participate":true,"budget":"sparsam","clear_location":true,"access":"fiber"}""")
        assertEquals("sparsam", s.budget)
    }

    @Test
    fun `inbound sends since and decodes rows`() = test {
        enqueueJson("""{"ready":true,"rows":[{"from":"ab12","name":"N","trust":"anchor","fam":4,"avg":12.5,"loss":0.1,"at":"2026-11-01T02:00:00Z"}]}""")

        val r = node().sonar64.inbound(since = Instant.parse("2026-11-01T01:00:00Z"))

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/sonar64/inbound?since=2026-11-01T01%3A00%3A00Z", request.target)
        assertEquals(true, r.ready)
        assertEquals("anchor", r.rows.single().trust)
        assertEquals(12.5, r.rows.single().avg ?: 0.0, 0.0)
    }

    @Test
    fun `recent sends n and decodes results`() = test {
        enqueueJson(
            """{"results":[{"at":"2026-11-01T02:00:00Z","target":"a_x","method":"icmp","fam":6,"sent":3,"recv":2,"avg":5.0,"loss":33.3}],
            "inbound":[{"from":"ab12","count":2}],"responder":true}""",
        )

        val r = node().sonar64.recent(n = 10)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/sonar64/recent?n=10", request.target)
        assertEquals("a_x", r.results.single().target)
        assertEquals(2L, r.results.single().recv)
        assertEquals(2L, r.inbound.single().count)
        assertEquals(true, r.responder)
    }
}
