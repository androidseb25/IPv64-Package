package de.sr.ipv64.api

import de.sr.ipv64.model.IpFamily
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Instant

class MetaApiTest : ApiSdkTestBase() {

    @Test
    fun `info requests the API root and decodes service info`() = test {
        enqueueJson(
            """{"data":{"name":"IPv64 API","version":"2.0.0","documentation":"https://ipv64.net/api/v2/docs",
            "time":"2026-10-10T12:00:00+02:00"}}""",
        )

        val info = client(apiKey = null).meta.info()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/", request.apiTarget)
        assertEquals("IPv64 API", info.name)
        assertEquals("2.0.0", info.version)
        assertEquals("https://ipv64.net/api/v2/docs", info.documentation)
        assertEquals(Instant.parse("2026-10-10T10:00:00Z"), info.time)
    }

    @Test
    fun `health requests GET health`() = test {
        enqueueJson("""{"data":{"status":"ok","version":"2.0.0"}}""")

        val health = client().meta.health()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/health", request.apiTarget)
        assertEquals("ok", health.status)
        assertEquals("2.0.0", health.version)
    }

    @Test
    fun `ip requests GET ip and decodes ipv6 family`() = test {
        enqueueJson("""{"data":{"ip":"2001:db8::1","family":"ipv6"}}""")

        val ip = client().meta.ip()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/ip", request.apiTarget)
        assertEquals("2001:db8::1", ip.ip)
        assertEquals(IpFamily.IPV6, ip.family)
    }

    @Test
    fun `ip falls back to null for an unknown family`() = test {
        enqueueJson("""{"data":{"ip":"203.0.113.1","family":"ipx"}}""")

        val ip = client().meta.ip()

        assertEquals("203.0.113.1", ip.ip)
        assertNull(ip.family)
    }

    @Test
    fun `openApi returns the document without data envelope`() = test {
        enqueueJson("""{"openapi":"3.1.0","info":{"title":"IPv64 API","version":"2.0.0"},"paths":{}}""")

        val doc = client().meta.openApi()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/openapi.json", request.apiTarget)
        assertEquals(JsonPrimitive("3.1.0"), doc["openapi"])
        assertEquals(JsonPrimitive("IPv64 API"), doc["info"]!!.jsonObject["title"])
    }
}
