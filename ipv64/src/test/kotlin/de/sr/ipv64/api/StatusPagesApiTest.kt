package de.sr.ipv64.api

import de.sr.ipv64.model.CreateStatusPageRequest
import de.sr.ipv64.model.StatusPage
import de.sr.ipv64.model.UpdateStatusPageRequest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class StatusPagesApiTest : ApiSdkTestBase() {

    private val pageJson = """{"id":4,"name":"Status","slug":"my-status","url":"https://ipv64.net/status/my-status",
        "description":"All systems","password_protected":true,"design":2,"autorefresh":false,
        "healthchecks":["hc1","hc2"],"created_at":"2026-01-01T00:00:00Z","last_viewed_at":null}"""

    private fun assertPage(page: StatusPage) {
        assertEquals(4L, page.id)
        assertEquals("my-status", page.slug)
        assertEquals("https://ipv64.net/status/my-status", page.url)
        assertEquals(true, page.passwordProtected)
        assertEquals(2, page.design)
        assertEquals(false, page.autorefresh)
        assertEquals(listOf("hc1", "hc2"), page.healthchecks)
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), page.createdAt)
        assertNull(page.lastViewedAt)
    }

    @Test
    fun `list requests GET status-pages`() = test {
        enqueueJson("""{"data":[$pageJson]}""")

        val pages = client().statusPages.list()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/status-pages", request.apiTarget)
        assertPage(pages.single())
    }

    @Test
    fun `create default sends empty object`() = test {
        enqueueJson("""{"data":$pageJson}""", code = 201)

        client().statusPages.create()

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/status-pages", request.apiTarget)
        assertEquals(0, request.jsonBody().size)
    }

    @Test
    fun `create sends all fields with wire names`() = test {
        enqueueJson("""{"data":$pageJson}""", code = 201)

        val page = client().statusPages.create(
            CreateStatusPageRequest(
                name = "Status", description = "All systems", slug = "my-status", password = "pw",
                design = 2, autorefresh = false, healthchecks = listOf("hc1", "hc2"),
            ),
        )

        val body = takeRequest().jsonBody()
        assertEquals(setOf("name", "description", "slug", "password", "design", "autorefresh", "healthchecks"), body.keys)
        assertEquals(JsonPrimitive("pw"), body["password"])
        assertEquals(JsonPrimitive(false), body["autorefresh"])
        assertEquals(JsonArray(listOf(JsonPrimitive("hc1"), JsonPrimitive("hc2"))), body["healthchecks"])
        assertPage(page)
    }

    @Test
    fun `get requests status page by id and encodes it`() = test {
        enqueueJson("""{"data":$pageJson}""")
        enqueueJson("""{"data":$pageJson}""")

        assertPage(client().statusPages.get("4"))
        client().statusPages.get("a b/c")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/status-pages/4", request.apiTarget)
        assertEquals("/status-pages/a%20b%2Fc", takeRequest().apiTarget)
    }

    @Test
    fun `update sends only non-null fields`() = test {
        enqueueJson("""{"data":$pageJson}""")

        client().statusPages.update("4", UpdateStatusPageRequest(name = "New", healthchecks = emptyList()))

        val request = takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/status-pages/4", request.apiTarget)
        val body = request.jsonBody()
        assertEquals(setOf("name", "healthchecks"), body.keys)
        assertEquals(JsonArray(emptyList()), body["healthchecks"])
    }

    @Test
    fun `delete requests DELETE and accepts 204`() = test {
        enqueueNoContent()

        client().statusPages.delete("4")

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/status-pages/4", request.apiTarget)
    }

    @Test
    fun `toString of requests redacts the password`() {
        val create = CreateStatusPageRequest(name = "S", password = "hunter2").toString()
        val update = UpdateStatusPageRequest(name = "S", password = "hunter2").toString()
        val none = CreateStatusPageRequest(name = "S").toString()

        for (text in listOf(create, update)) {
            assertFalse(text, text.contains("hunter2"))
            assertTrue(text, text.contains("password=<redacted>"))
            assertTrue(text, text.contains("name=S"))
        }
        assertTrue(none, none.contains("password=null"))
    }
}
