package de.sr.ipv64.node64

import de.sr.ipv64.MockServerTest
import de.sr.ipv64.internal.IPv64Json
import de.sr.ipv64.node64.model.CDNAccessListInput
import de.sr.ipv64.node64.model.CDNAccessListInputUser
import de.sr.ipv64.node64.model.CertBundleFormat
import mockwebserver3.MockResponse
import okio.Buffer
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Node64ReviewFixesTest : MockServerTest() {

    private fun node() = Node64(baseUrl = server.url("/api").toString(), apiKey = "node64_key")

    @Test
    fun `bundle returns content etag and expiry`() = test {
        val payload = byteArrayOf(0x50, 0x4B, 0x03, 0x04)
        server.enqueue(
            MockResponse.Builder().code(200)
                .addHeader("ETag", "\"abc\"")
                .addHeader("X-Cert-Not-After", "2027-01-01T00:00:00Z")
                .body(Buffer().write(payload)).build(),
        )

        val result = node().certificates.bundle(7, CertBundleFormat.ZIP)

        assertEquals("/api/certs/7/bundle?format=zip", takeRequest().target)
        assertArrayEquals(payload, result.content)
        assertEquals("\"abc\"", result.etag)
        assertEquals("2027-01-01T00:00:00Z", result.notAfter)
        assertFalse(result.notModified)
    }

    @Test
    fun `bundle reports 304 as not modified`() = test {
        server.enqueue(MockResponse.Builder().code(304).build())

        val result = node().certificates.bundle(7, ifNoneMatch = "\"abc\"")

        assertEquals("\"abc\"", takeRequest().headers["If-None-Match"])
        assertTrue(result.notModified)
        assertNull(result.content)
        assertEquals("\"abc\"", result.etag)
    }

    @Test
    fun `addList sends url and optional name`() = test {
        enqueueJson("""{"port":53}""")

        node().dns64.addList("https://example.com/list.txt")

        assertEquals("""{"url":"https://example.com/list.txt"}""", takeRequest().bodyText())
    }

    @Test
    fun `access list user without password keeps the current one`() {
        val body = IPv64Json.encodeToString(
            CDNAccessListInput.serializer(),
            CDNAccessListInput(users = listOf(CDNAccessListInputUser("lea"))),
        )
        assertEquals("""{"users":[{"username":"lea"}]}""", body)
        assertEquals(
            "CDNAccessListInputUser(username=lea, password=<redacted>)",
            CDNAccessListInputUser("lea", "secret").toString(),
        )
    }
}
