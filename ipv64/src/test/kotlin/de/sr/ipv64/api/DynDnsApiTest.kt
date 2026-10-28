package de.sr.ipv64.api

import de.sr.ipv64.model.DynDnsOutcome
import de.sr.ipv64.model.DynDnsUpdateRequest
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

class DynDnsApiTest : ApiSdkTestBase() {

    private val resultJson = """{"data":{"domain":"home.ipv64.net","prefix":"nas","changed":true,"results":[
        {"ip":"203.0.113.7","type":"A","result":"updated"},
        {"ip":"2001:db8::7","type":"AAAA","result":"created"},
        {"ip":"10.0.0.1","type":"A","result":"invalid_ip"}]}}"""

    @Test
    fun `update sends all fields and decodes per ip results`() = test {
        enqueueJson(resultJson)

        val result = client().dynDns.update("home.ipv64.net", ipv4 = "203.0.113.7", ipv6 = "2001:db8::7", prefix = "nas")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/domains/home.ipv64.net/dyndns", request.apiTarget)
        val body = request.jsonBody()
        assertEquals(JsonPrimitive("203.0.113.7"), body["ipv4"])
        assertEquals(JsonPrimitive("2001:db8::7"), body["ipv6"])
        assertEquals(JsonPrimitive("nas"), body["prefix"])
        assertEquals("home.ipv64.net", result.domain)
        assertEquals("nas", result.prefix)
        assertEquals(true, result.changed)
        assertEquals(3, result.results.size)
        assertEquals(DynDnsOutcome.UPDATED, result.results[0].result)
        assertEquals("AAAA", result.results[1].type)
        assertEquals(DynDnsOutcome.CREATED, result.results[1].result)
        assertEquals(DynDnsOutcome.INVALID_IP, result.results[2].result)
    }

    @Test
    fun `update without ips sends an empty object`() = test {
        enqueueJson(resultJson)

        client().dynDns.update("10")

        val request = takeRequest()
        assertEquals("/domains/10/dyndns", request.apiTarget)
        assertEquals(0, request.jsonBody().size)
    }

    @Test
    fun `update from request object sends only set fields and encodes domain`() = test {
        enqueueJson(resultJson)

        client().dynDns.update("my domain/x", DynDnsUpdateRequest(ipv6 = "2001:db8::1"))

        val request = takeRequest()
        assertEquals("/domains/my%20domain%2Fx/dyndns", request.apiTarget)
        assertEquals(setOf("ipv6"), request.jsonBody().keys)
    }
}
