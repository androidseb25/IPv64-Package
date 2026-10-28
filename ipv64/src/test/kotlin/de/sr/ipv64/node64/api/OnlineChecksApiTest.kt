package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.CheckOwner
import de.sr.ipv64.node64.model.CheckRequest
import de.sr.ipv64.node64.model.CheckStateValue
import de.sr.ipv64.node64.model.CheckType
import de.sr.ipv64.node64.model.OnlineCheckAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class OnlineChecksApiTest : Node64ApiTestBaseA() {

    private val checkJson = """{"token":"on1","name":"Online","type":"icmp","target":"example.com","interval_seconds":300,
        "tags":["a"],"locations":["fra","nyc"],"owner":"online","created_at":"2026-09-01T00:00:00Z",
        "state":{"state":"up","since":"2026-10-01T00:00:00Z","last_latency_ms":12.0}}"""

    private fun assertCheck(c: de.sr.ipv64.node64.model.Check) {
        assertEquals("on1", c.token)
        assertEquals(CheckType.ICMP, c.type)
        assertEquals(300, c.intervalSeconds)
        assertEquals(listOf("fra", "nyc"), c.locations)
        assertEquals(CheckOwner.ONLINE, c.owner)
        assertEquals(Instant.parse("2026-09-01T00:00:00Z"), c.createdAt)
        assertEquals(CheckStateValue.UP, c.state!!.state)
        assertEquals(Instant.parse("2026-10-01T00:00:00Z"), c.state!!.since)
        assertEquals(12.0, c.state!!.lastLatencyMs!!, 0.0)
    }

    @Test
    fun `list decodes checks`() = test {
        enqueueJson("[$checkJson]")

        val checks = node().onlineChecks.list()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/online-checks", request.target)
        assertCheck(checks.single())
    }

    @Test
    fun `create posts request`() = test {
        enqueueJson(checkJson, code = 201)

        val check = node().onlineChecks.create(
            CheckRequest("Online", CheckType.ICMP, target = "example.com", locations = listOf("fra", "nyc"), intervalSeconds = 300),
        )

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/online-checks", request.target)
        assertTrue(request.headers["Content-Type"]!!.startsWith("application/json"))
        request.assertJsonBody(
            """{"name":"Online","type":"icmp","target":"example.com","interval_seconds":300,"locations":["fra","nyc"]}""",
        )
        assertCheck(check)
    }

    @Test
    fun `get fetches by token`() = test {
        enqueueJson(checkJson)

        assertCheck(node().onlineChecks.get("on1"))

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/online-checks/on1", request.target)
    }

    @Test
    fun `update puts request`() = test {
        enqueueJson(checkJson)

        assertCheck(node().onlineChecks.update("on1", CheckRequest("Online", CheckType.ICMP, paused = true)))

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/online-checks/on1", request.target)
        request.assertJsonBody("""{"name":"Online","type":"icmp","paused":true}""")
    }

    @Test
    fun `delete sends DELETE`() = test {
        enqueueNoContent()

        node().onlineChecks.delete("on1")

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/online-checks/on1", request.target)
    }

    @Test
    fun `action posts to the action segment`() = test {
        enqueueNoContent()

        node().onlineChecks.action("on1", OnlineCheckAction.PAUSE)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/online-checks/on1/pause", request.target)
    }

    @Test
    fun `pause resume and run use their endpoints`() = test {
        enqueueNoContent()
        enqueueNoContent()
        enqueueNoContent()

        node().onlineChecks.pause("on1")
        node().onlineChecks.resume("on1")
        node().onlineChecks.run("on1")

        val first = takeRequest()
        assertEquals("POST", first.method)
        assertEquals("/api/online-checks/on1/pause", first.target)
        assertEquals("/api/online-checks/on1/resume", takeRequest().target)
        val third = takeRequest()
        assertEquals("POST", third.method)
        assertEquals("/api/online-checks/on1/run", third.target)
    }
}
