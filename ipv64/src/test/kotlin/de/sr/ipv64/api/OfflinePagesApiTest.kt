package de.sr.ipv64.api

import de.sr.ipv64.model.CreateOfflinePageRequest
import de.sr.ipv64.model.OfflinePage
import de.sr.ipv64.model.UpdateOfflinePageRequest
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class OfflinePagesApiTest : ApiSdkTestBase() {

    private val pageJson = """{"id":"off1","name":"Maintenance","domain":"example.com","email":"hi@example.com",
        "phone":"+49 123","alternative_url":"https://backup.example.com","text":"Back soon",
        "icon_url":"https://ipv64.net/icons/off1.png","views":42,"in_use_by_policy":"pol1","active":true,
        "preview_url":"https://ipv64.net/offline/off1"}"""

    private fun assertPage(page: OfflinePage) {
        assertEquals("off1", page.id)
        assertEquals("Maintenance", page.name)
        assertEquals("example.com", page.domain)
        assertEquals("+49 123", page.phone)
        assertEquals("https://backup.example.com", page.alternativeUrl)
        assertEquals("https://ipv64.net/icons/off1.png", page.iconUrl)
        assertEquals(42L, page.views)
        assertEquals("pol1", page.inUseByPolicy)
        assertEquals(true, page.active)
        assertEquals("https://ipv64.net/offline/off1", page.previewUrl)
    }

    @Test
    fun `list requests GET offline-pages`() = test {
        enqueueJson("""{"data":[$pageJson]}""")

        val pages = client().offlinePages.list()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/offline-pages", request.apiTarget)
        assertPage(pages.single())
    }

    @Test
    fun `create default sends empty object`() = test {
        enqueueJson("""{"data":$pageJson}""", code = 201)

        client().offlinePages.create()

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/offline-pages", request.apiTarget)
        assertEquals(0, request.jsonBody().size)
    }

    @Test
    fun `create sends alternative_url with wire name`() = test {
        enqueueJson("""{"data":$pageJson}""", code = 201)

        val page = client().offlinePages.create(
            CreateOfflinePageRequest(name = "Maintenance", alternativeUrl = "https://backup.example.com", text = "Back soon"),
        )

        val body = takeRequest().jsonBody()
        assertEquals(setOf("name", "alternative_url", "text"), body.keys)
        assertEquals(JsonPrimitive("https://backup.example.com"), body["alternative_url"])
        assertPage(page)
    }

    @Test
    fun `get requests page by id and encodes it`() = test {
        enqueueJson("""{"data":$pageJson}""")
        enqueueJson("""{"data":$pageJson}""")

        assertPage(client().offlinePages.get("off1"))
        client().offlinePages.get("a b/c")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/offline-pages/off1", request.apiTarget)
        assertEquals("/offline-pages/a%20b%2Fc", takeRequest().apiTarget)
    }

    @Test
    fun `update sends only non-null fields`() = test {
        enqueueJson("""{"data":$pageJson}""")

        client().offlinePages.update("off1", UpdateOfflinePageRequest(email = "x@example.com"))

        val request = takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/offline-pages/off1", request.apiTarget)
        assertEquals(setOf("email"), request.jsonBody().keys)
    }

    @Test
    fun `delete requests DELETE and accepts 204`() = test {
        enqueueNoContent()

        client().offlinePages.delete("off1")

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/offline-pages/off1", request.apiTarget)
    }

    @Test
    fun `uploadIcon with bytes sends multipart field icon`() = test {
        enqueueJson("""{"data":$pageJson}""")
        val bytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 1, 2, 3)

        val page = client().offlinePages.uploadIcon("off1", bytes, "logo.png", "image/png")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/offline-pages/off1/icon", request.apiTarget)
        val contentType = request.headers["Content-Type"]!!
        assertTrue(contentType, contentType.startsWith("multipart/form-data"))
        val body = request.body!!.toByteArray()
        val text = String(body, Charsets.ISO_8859_1)
        assertTrue(text, text.contains("""name="icon"; filename="logo.png""""))
        assertTrue(text, text.contains("Content-Type: image/png"))
        assertTrue(indexOf(body, bytes) >= 0)
        assertPage(page)
    }

    @Test
    fun `uploadIcon with file derives media type from extension`() = test {
        enqueueJson("""{"data":$pageJson}""")
        val bytes = byteArrayOf(1, 2, 3, 4, 5, 6)
        val file = File.createTempFile("logo", ".webp").apply { writeBytes(bytes); deleteOnExit() }

        client().offlinePages.uploadIcon("off1", file)

        val request = takeRequest()
        assertEquals("/offline-pages/off1/icon", request.apiTarget)
        assertTrue(request.headers["Content-Type"]!!.startsWith("multipart/form-data"))
        val body = request.body!!.toByteArray()
        val text = String(body, Charsets.ISO_8859_1)
        assertTrue(text, text.contains("""name="icon"; filename="${file.name}""""))
        assertTrue(text, text.contains("Content-Type: image/webp"))
        assertTrue(indexOf(body, bytes) >= 0)
    }

    @Test
    fun `uploadIcon with file and explicit media type`() = test {
        enqueueJson("""{"data":$pageJson}""")
        val file = File.createTempFile("logo", ".bin").apply { writeBytes(byteArrayOf(9, 8, 7)); deleteOnExit() }

        client().offlinePages.uploadIcon("off1", file, "image/jpeg")

        val text = String(takeRequest().body!!.toByteArray(), Charsets.ISO_8859_1)
        assertTrue(text, text.contains("Content-Type: image/jpeg"))
    }

    @Test
    fun `deleteIcon requests DELETE on icon`() = test {
        enqueueNoContent()

        client().offlinePages.deleteIcon("off1")

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/offline-pages/off1/icon", request.apiTarget)
        assertNull(request.headers["Content-Type"]?.takeIf { it.startsWith("multipart") })
    }

    private fun indexOf(haystack: ByteArray, needle: ByteArray): Int {
        outer@ for (i in 0..haystack.size - needle.size) {
            for (j in needle.indices) if (haystack[i + j] != needle[j]) continue@outer
            return i
        }
        return -1
    }
}
