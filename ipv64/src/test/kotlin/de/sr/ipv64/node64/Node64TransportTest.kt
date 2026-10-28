package de.sr.ipv64.node64

import de.sr.ipv64.MockServerTest
import de.sr.ipv64.Node64ApiException
import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.SharedOkHttpClient
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.internal.node64ApiException
import kotlinx.serialization.Serializable
import mockwebserver3.MockResponse
import okio.Buffer
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Node64TransportTest : MockServerTest() {

    @Serializable
    data class Sample(val status: String? = null, val retentionDays: Int? = null)

    private fun transport(apiKey: String? = "node64_key", actor: String? = null): HttpTransport = HttpTransport(
        client = SharedOkHttpClient.instance,
        baseUrl = server.url("/api"),
        defaultHeaders = buildMap {
            put("User-Agent", Node64.DEFAULT_USER_AGENT)
            apiKey?.let { put("Authorization", "Bearer $it") }
            actor?.let { put("X-Agent-Actor", it) }
        },
        errorMapper = ::node64ApiException,
    )

    @Test
    fun `decodes plain json without envelope`() = test {
        enqueueJson("""{"status":"ok","retentionDays":30,"unknown":1}""")

        val result: Sample = transport().json(Method.GET, ApiPath("dns64", "settings"))

        val request = takeRequest()
        assertEquals("/api/dns64/settings", request.target)
        assertEquals("Bearer node64_key", request.headers["Authorization"])
        assertEquals(Sample("ok", 30), result)
    }

    @Test
    fun `decodes top level arrays`() = test {
        enqueueJson("""[{"status":"a"},{"status":"b"}]""")

        val result: List<Sample> = transport().json(Method.GET, ApiPath("checks"))

        assertEquals(listOf("a", "b"), result.map { it.status })
    }

    @Test
    fun `jsonOrNull returns null for empty bodies`() = test {
        server.enqueue(MockResponse.Builder().code(201).build())
        enqueueJson("""{"ok":true}""")

        assertNull(transport().jsonOrNull(Method.POST, ApiPath("checks")))
        assertEquals("""{"ok":true}""", transport().jsonOrNull(Method.POST, ApiPath("checks")).toString())
    }

    @Test
    fun `bytes returns binary bodies unchanged`() = test {
        val payload = byteArrayOf(0x50, 0x4B, 0x03, 0x04, 0x00, -1, -2)
        server.enqueue(MockResponse.Builder().code(200).body(Buffer().write(payload)).build())

        val bytes = transport().bytes(Method.GET, ApiPath("certs", "1", "bundle"), headers = mapOf("Accept" to "*/*"))

        assertArrayEquals(payload, bytes)
        assertEquals("*/*", takeRequest().headers["Accept"])
    }

    @Test
    fun `maps error documents to Node64ApiException`() = test {
        server.enqueue(
            MockResponse.Builder().code(422).setHeader("Content-Type", "application/json")
                .body("""{"error":"name required","field":"name"}""").build(),
        )

        val e = runCatching { transport().json<Sample>(Method.POST, ApiPath("checks")) }.exceptionOrNull()

        e as Node64ApiException
        assertEquals(422, e.status)
        assertTrue(e.isValidationError)
        assertEquals("name required", e.error)
        assertEquals("name", e.field)
        assertTrue(e.message!!.contains("name required"))
    }

    @Test
    fun `rate limit exposes Retry-After header`() = test {
        server.enqueue(
            MockResponse.Builder().code(429).setHeader("Retry-After", "10")
                .body("""{"error":"too many requests"}""").build(),
        )

        val e = runCatching { transport().json<Sample>(Method.GET, ApiPath("status")) }.exceptionOrNull()

        e as Node64ApiException
        assertTrue(e.isRateLimited)
        assertEquals(10, e.retryAfterSeconds)
    }

    @Test
    fun `plain text errors are kept as error message`() = test {
        server.enqueue(MockResponse.Builder().code(404).body("404 page not found\n").build())

        val e = runCatching { transport().json<Sample>(Method.GET, ApiPath("nope")) }.exceptionOrNull()

        e as Node64ApiException
        assertTrue(e.isNotFound)
        assertEquals("404 page not found", e.error)
    }

    @Test
    fun `client sends actor header and withActor switches it`() = test {
        enqueueJson("""{"status":"ok"}""")
        enqueueJson("""{"status":"ok"}""")
        val node = Node64(baseUrl = server.url("/api").toString(), apiKey = "node64_key", actor = "android")

        node.node.health()
        node.withActor(null).node.health()

        val first = takeRequest()
        assertEquals("/api/health", first.target)
        assertEquals("android", first.headers["X-Agent-Actor"])
        assertEquals("Bearer node64_key", first.headers["Authorization"])
        assertNull(takeRequest().headers["X-Agent-Actor"])
    }

    @Test
    fun `toString hides the api key`() {
        assertFalse(Node64(baseUrl = "http://node.local/api", apiKey = "node64_secret").toString().contains("node64_secret"))
    }
}
