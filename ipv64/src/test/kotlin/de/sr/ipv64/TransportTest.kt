package de.sr.ipv64

import de.sr.ipv64.model.IpFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import mockwebserver3.MockResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.time.Instant

class TransportTest : MockServerTest() {

    @Test
    fun `sends bearer token by default`() = test {
        enqueueJson("""{"data":{"status":"ok","version":"2.0.0"}}""")

        val health = client().meta.health()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/health", request.apiTarget)
        assertEquals("Bearer test-key", request.headers["Authorization"])
        assertNull(request.headers["X-API-Key"])
        assertEquals("ok", health.status)
    }

    @Test
    fun `sends X-API-Key header when configured`() = test {
        enqueueJson("""{"data":{"status":"ok"}}""")

        client(authScheme = IPv64AuthScheme.ApiKeyHeader).meta.health()

        val request = takeRequest()
        assertEquals("test-key", request.headers["X-API-Key"])
        assertNull(request.headers["Authorization"])
    }

    @Test
    fun `sends no credentials without api key`() = test {
        enqueueJson("""{"data":{"ip":"203.0.113.10","family":"ipv4"}}""")

        val ip = client(apiKey = null).meta.ip()

        val request = takeRequest()
        assertNull(request.headers["Authorization"])
        assertNull(request.headers["X-API-Key"])
        assertEquals("203.0.113.10", ip.ip)
        assertEquals(IpFamily.IPV4, ip.family)
    }

    @Test
    fun `withActAs adds act-as header and keeps the key`() = test {
        enqueueJson("""{"data":{"status":"ok"}}""")
        enqueueJson("""{"data":{"status":"ok"}}""")

        val base = client()
        base.withActAs("other@example.com").meta.health()
        base.meta.health()

        assertEquals("other@example.com", takeRequest().headers["X-IPv64-Act-As"])
        assertNull(takeRequest().headers["X-IPv64-Act-As"])
    }

    @Test
    fun `parses iso timestamps with offset`() = test {
        enqueueJson("""{"data":{"name":"IPv64.net API","version":"2.0.0","time":"2026-10-10T12:00:00+02:00"}}""")

        val info = client().meta.info()

        assertEquals("/", takeRequest().apiTarget)
        assertEquals(Instant.parse("2026-10-10T10:00:00Z"), info.time)
    }

    @Test
    fun `ignores unknown fields and unknown enum values`() = test {
        enqueueJson("""{"data":{"ip":"::1","family":"ipv9","new_field":{"x":1}}}""")

        val ip = client().meta.ip()

        assertEquals("::1", ip.ip)
        assertNull(ip.family)
    }

    @Test
    fun `maps problem documents to IPv64ApiException`() = test {
        enqueueProblem(404, "domain_not_found", "Domain 'x.ipv64.de' not found.", ""","field":"domain"""")

        try {
            client().meta.health()
            fail("expected exception")
        } catch (e: IPv64ApiException) {
            assertEquals(404, e.status)
            assertTrue(e.isNotFound)
            assertEquals("domain_not_found", e.code)
            assertEquals("Domain 'x.ipv64.de' not found.", e.detail)
            assertEquals("domain", e.field)
            assertTrue(e.message!!.contains("domain_not_found"))
        }
    }

    @Test
    fun `exposes retry-after on rate limit`() = test {
        enqueueProblem(429, "rate_limited", "Too many requests.", ""","retry_after":10""")

        val e = runCatching { client().meta.health() }.exceptionOrNull() as IPv64ApiException

        assertTrue(e.isRateLimited)
        assertEquals(10, e.retryAfterSeconds)
    }

    @Test
    fun `falls back to Retry-After header and non-json error bodies`() = test {
        server.enqueue(
            MockResponse.Builder().code(503).setHeader("Retry-After", "7").body("<html>down</html>").build(),
        )

        val e = runCatching { client().meta.health() }.exceptionOrNull() as IPv64ApiException

        assertEquals(503, e.status)
        assertNull(e.problem)
        assertEquals(7, e.retryAfterSeconds)
        assertEquals("<html>down</html>", e.rawBody)
    }

    @Test
    fun `malformed json becomes IPv64SerializationException`() = test {
        enqueueJson("""{"data": "not an object"}""")

        val e = runCatching { client().meta.health() }.exceptionOrNull()

        assertTrue(e is IPv64SerializationException)
    }

    @Test
    fun `connection failures become IPv64NetworkException`() = test {
        val url = server.url("/api/v2").toString()
        server.close()

        val e = runCatching { IPv64(apiKey = "k", baseUrl = url).meta.health() }.exceptionOrNull()

        assertTrue(e is IPv64NetworkException)
    }

    @Test
    fun `cancellation cancels the http call`() = runBlocking {
        server.enqueue(
            MockResponse.Builder().code(200).body("""{"data":{}}""").headersDelay(5, TimeUnit.SECONDS).build(),
        )
        val job = launch(Dispatchers.IO) { client().meta.health() }
        takeRequest()
        job.cancel()
        withTimeout(2_000) { job.join() }
        assertTrue(job.isCancelled)
    }

    @Test
    fun `toString hides the api key`() {
        val text = IPv64(apiKey = "super-secret").toString()
        assertFalse(text.contains("super-secret"))
    }
}
