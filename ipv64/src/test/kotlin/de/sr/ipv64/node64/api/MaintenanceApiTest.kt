package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.MaintenanceInput
import de.sr.ipv64.node64.model.MaintenanceRepeat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Instant

class MaintenanceApiTest : Node64ApiTestBaseA() {

    private val maintenanceJson = """{"id":4,"name":"Nightly","start_at":"2026-11-01T02:00:00Z","end_at":"2026-11-01T03:00:00Z",
        "repeat":"weekly","repeat_until":"","tokens":["a","b"],"active":true,"created_at":"2026-10-28T09:00:00Z",
        "remote_id":88,"revision":2}"""

    @Test
    fun `list decodes windows with lenient timestamps`() = test {
        enqueueJson("[$maintenanceJson]")

        val list = node().maintenance.list()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/maintenance", request.target)
        val m = list.single()
        assertEquals(4L, m.id)
        assertEquals("Nightly", m.name)
        assertEquals(Instant.parse("2026-11-01T02:00:00Z"), m.startAt)
        assertEquals(Instant.parse("2026-11-01T03:00:00Z"), m.endAt)
        assertEquals(MaintenanceRepeat.WEEKLY, m.repeat)
        assertNull(m.repeatUntil)
        assertEquals(listOf("a", "b"), m.tokens)
        assertEquals(true, m.active)
        assertEquals(Instant.parse("2026-10-28T09:00:00Z"), m.createdAt)
        assertEquals(88L, m.remoteId)
        assertEquals(2L, m.revision)
    }

    @Test
    fun `create posts input and decodes response`() = test {
        enqueueJson(maintenanceJson, code = 201)

        val result = node().maintenance.create(
            MaintenanceInput(
                name = "Nightly",
                startAt = Instant.parse("2026-11-01T02:00:00Z"),
                endAt = Instant.parse("2026-11-01T03:00:00Z"),
                repeat = MaintenanceRepeat.WEEKLY,
                repeatUntil = Instant.parse("2027-01-01T00:00:00Z"),
                tokens = listOf("a", "b"),
                active = true,
            ),
        )

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/maintenance", request.target)
        request.assertJsonBody(
            """{"name":"Nightly","start_at":"2026-11-01T02:00:00Z","end_at":"2026-11-01T03:00:00Z","repeat":"weekly",
            "repeat_until":"2027-01-01T00:00:00Z","tokens":["a","b"],"active":true}""",
        )
        assertEquals(4L, result!!.id)
    }

    @Test
    fun `create returns null for empty body and omits optional fields`() = test {
        enqueueEmpty(201)

        val result = node().maintenance.create(
            MaintenanceInput("x", Instant.parse("2026-11-01T02:00:00Z"), Instant.parse("2026-11-01T03:00:00Z")),
        )

        assertNull(result)
        takeRequest().assertJsonBody("""{"name":"x","start_at":"2026-11-01T02:00:00Z","end_at":"2026-11-01T03:00:00Z"}""")
    }

    @Test
    fun `update puts input and returns null for empty body`() = test {
        enqueueEmpty()

        val result = node().maintenance.update(
            4,
            MaintenanceInput("y", Instant.parse("2026-11-02T02:00:00Z"), Instant.parse("2026-11-02T03:00:00Z"), MaintenanceRepeat.NONE),
        )

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/maintenance/4", request.target)
        request.assertJsonBody(
            """{"name":"y","start_at":"2026-11-02T02:00:00Z","end_at":"2026-11-02T03:00:00Z","repeat":"none"}""",
        )
        assertNull(result)
    }

    @Test
    fun `update decodes response when present`() = test {
        enqueueJson(maintenanceJson)

        val result = node().maintenance.update(
            4,
            MaintenanceInput("Nightly", Instant.parse("2026-11-01T02:00:00Z"), Instant.parse("2026-11-01T03:00:00Z")),
        )

        assertEquals(MaintenanceRepeat.WEEKLY, result!!.repeat)
    }

    @Test
    fun `delete sends DELETE`() = test {
        enqueueNoContent()

        node().maintenance.delete(4)

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/maintenance/4", request.target)
    }
}
