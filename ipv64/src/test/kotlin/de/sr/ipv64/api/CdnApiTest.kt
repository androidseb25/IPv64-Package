package de.sr.ipv64.api

import de.sr.ipv64.model.CdnAccessListMode
import de.sr.ipv64.model.CdnBackendScheme
import de.sr.ipv64.model.CdnCertificate
import de.sr.ipv64.model.CdnDomain
import de.sr.ipv64.model.CdnFrontend
import de.sr.ipv64.model.CdnPendingTask
import de.sr.ipv64.model.CdnPortmapperState
import de.sr.ipv64.model.CdnProxyState
import de.sr.ipv64.model.CdnRecordMode
import de.sr.ipv64.model.CdnTransport
import de.sr.ipv64.model.CreateCdnMappingRequest
import de.sr.ipv64.model.RecordState
import de.sr.ipv64.model.UpdateCdnAccessList
import de.sr.ipv64.model.UpdateCdnCaching
import de.sr.ipv64.model.UpdateCdnDomainRequest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Instant

class CdnApiTest : ApiSdkTestBase() {

    private val domainBody = """"id":"cdn1","domain":"example.com","domain_id":10,"active":true,
        "pending":["config_deploy","certificate","something_new"],"dns_records_enabled":true,
        "server":{"name":"edge-1","hostname":"edge-1.ipv64.net","ipv4":"198.51.100.1","ipv6":"2001:db8::1"},
        "proxy":{"state":"on","frontend":"https_redirect","backend":{"scheme":"https","port":8443},"websocket":true,
        "certificate":"letsencrypt","caching":{"enabled":true,"time_minutes":10,"size_mb":100},
        "access_list":{"enabled":true,"mode":"denylist"}},
        "portmapper":{"state":"suspended_traffic_limit"},"traffic_mb":{"in":12.5,"out":99.0},
        "created_at":"2026-01-01T00:00:00Z""""

    private val domainJson = "{$domainBody}"
    private val detailJson = """{$domainBody,"mappings":[$mappingJson]}"""

    private fun mappingJsonField() = mappingJson

    private fun assertDomain(d: CdnDomain) {
        assertEquals("cdn1", d.id)
        assertEquals("example.com", d.domain)
        assertEquals(10L, d.domainId)
        assertEquals(listOf(CdnPendingTask.CONFIG_DEPLOY, CdnPendingTask.CERTIFICATE), d.pending)
        assertEquals("edge-1.ipv64.net", d.server?.hostname)
        assertEquals("2001:db8::1", d.server?.ipv6)
        assertEquals(CdnProxyState.ON, d.proxy?.state)
        assertEquals(CdnFrontend.HTTPS_REDIRECT, d.proxy?.frontend)
        assertEquals(CdnBackendScheme.HTTPS, d.proxy?.backend?.scheme)
        assertEquals(8443, d.proxy?.backend?.port)
        assertEquals(true, d.proxy?.websocket)
        assertEquals(CdnCertificate.LETSENCRYPT, d.proxy?.certificate)
        assertEquals(10, d.proxy?.caching?.timeMinutes)
        assertEquals(100, d.proxy?.caching?.sizeMb)
        assertEquals(CdnAccessListMode.DENYLIST, d.proxy?.accessList?.mode)
        assertEquals(CdnPortmapperState.SUSPENDED_TRAFFIC_LIMIT, d.portmapper?.state)
        assertEquals(12.5, d.trafficMb?.incoming)
        assertEquals(99.0, d.trafficMb?.outgoing)
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), d.createdAt)
    }

    @Test
    fun `list requests GET cdn`() = test {
        enqueueJson("""{"data":[$domainJson]}""")

        val list = client().cdn.list()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/cdn", request.apiTarget)
        assertDomain(list.single())
    }

    @Test
    fun `add sends domain`() = test {
        enqueueJson("""{"data":$detailJson}""", code = 202)

        val detail = client().cdn.add("example.com")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/cdn", request.apiTarget)
        assertEquals(mapOf("domain" to JsonPrimitive("example.com")), request.jsonBody())
        assertEquals("cdn1", detail.id)
        assertEquals(1, detail.mappings.size)
    }

    @Test
    fun `get decodes detail with mappings and encodes token`() = test {
        enqueueJson("""{"data":$detailJson}""")
        enqueueJson("""{"data":$detailJson}""")

        val detail = client().cdn.get("cdn1")
        client().cdn.get("a b/c")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/cdn/cdn1", request.apiTarget)
        assertEquals(CdnProxyState.ON, detail.proxy?.state)
        val mapping = detail.mappings.single()
        assertEquals("map1", mapping.id)
        assertEquals(CdnTransport.UDP, mapping.transport)
        assertEquals(51000, mapping.listenPort)
        assertEquals(25565, mapping.targetPort)
        assertEquals("/cdn/a%20b%2Fc", takeRequest().apiTarget)
    }

    @Test
    fun `update sends nested objects and omits nulls`() = test {
        enqueueJson("""{"data":$detailJson}""")

        client().cdn.update(
            "cdn1",
            UpdateCdnDomainRequest(
                frontend = CdnFrontend.HTTP_AND_HTTPS,
                backendScheme = CdnBackendScheme.HTTP,
                backendPort = 8080,
                caching = UpdateCdnCaching(enabled = true, timeMinutes = 5),
                accessList = UpdateCdnAccessList(mode = CdnAccessListMode.ALLOWLIST),
            ),
        )

        val request = takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/cdn/cdn1", request.apiTarget)
        val body = request.jsonBody()
        assertEquals(setOf("frontend", "backend_scheme", "backend_port", "caching", "access_list"), body.keys)
        assertEquals(JsonPrimitive("http_and_https"), body["frontend"])
        assertEquals(JsonPrimitive("http"), body["backend_scheme"])
        assertEquals(JsonPrimitive(8080), body["backend_port"])
        val caching = body["caching"]!!.jsonObject
        assertEquals(setOf("enabled", "time_minutes"), caching.keys)
        assertEquals(setOf("mode"), body["access_list"]!!.jsonObject.keys)
        assertEquals(JsonPrimitive("allowlist"), body["access_list"]!!.jsonObject["mode"])
    }

    @Test
    fun `update with websocket only`() = test {
        enqueueJson("""{"data":$detailJson}""")

        client().cdn.update("cdn1", UpdateCdnDomainRequest(websocket = false))

        assertEquals(mapOf("websocket" to JsonPrimitive(false)), takeRequest().jsonBody())
    }

    @Test
    fun `delete requests DELETE and decodes the 202 body`() = test {
        enqueueJson("""{"data":$domainJson}""", code = 202)

        val result = client().cdn.delete("cdn1")

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/cdn/cdn1", request.apiTarget)
        assertDomain(result)
    }

    @Test
    fun `action endpoints post to their paths`() = test {
        val actions = listOf(
            "proxy/enable" to { c: de.sr.ipv64.IPv64 -> suspend { c.cdn.enableProxy("cdn1") } },
            "proxy/disable" to { c: de.sr.ipv64.IPv64 -> suspend { c.cdn.disableProxy("cdn1") } },
            "portmapper/enable" to { c: de.sr.ipv64.IPv64 -> suspend { c.cdn.enablePortmapper("cdn1") } },
            "portmapper/disable" to { c: de.sr.ipv64.IPv64 -> suspend { c.cdn.disablePortmapper("cdn1") } },
            "redeploy" to { c: de.sr.ipv64.IPv64 -> suspend { c.cdn.redeploy("cdn1") } },
            "reset-traffic" to { c: de.sr.ipv64.IPv64 -> suspend { c.cdn.resetTraffic("cdn1") } },
            "move" to { c: de.sr.ipv64.IPv64 -> suspend { c.cdn.move("cdn1") } },
            "renew-certificate" to { c: de.sr.ipv64.IPv64 -> suspend { c.cdn.renewCertificate("cdn1") } },
        )
        val ipv64 = client()

        for ((path, call) in actions) {
            enqueueJson("""{"data":$domainJson}""", code = 202)
            val result = call(ipv64)()
            val request = takeRequest()
            assertEquals(path, "POST", request.method)
            assertEquals(path, "/cdn/cdn1/$path", request.apiTarget)
            assertDomain(result)
        }
    }

    @Test
    fun `records requests GET and decodes cdn record states`() = test {
        enqueueJson(
            """{"data":[{"id":100,"domain_id":10,"prefix":"www","fqdn":"www.example.com","type":"A",
            "content":"203.0.113.7","ttl":60,"state":"cdn_partial","token":"tok"}]}""",
        )

        val records = client().cdn.records("cdn1")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/cdn/cdn1/records", request.apiTarget)
        assertEquals(RecordState.CDN_PARTIAL, records.single().state)
        assertEquals("www.example.com", records.single().fqdn)
    }

    @Test
    fun `enableRecord sends mode and encodes record segment`() = test {
        enqueueJson("""{"data":{"id":100,"state":"cdn_proxied","token":"tok"}}""", code = 202)

        val record = client().cdn.enableRecord("cdn1", "rec tok/1", CdnRecordMode.OTHER_FAMILY_ONLY)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/cdn/cdn1/records/rec%20tok%2F1/enable", request.apiTarget)
        assertEquals(mapOf("mode" to JsonPrimitive("other_family_only")), request.jsonBody())
        assertEquals(RecordState.CDN_PROXIED, record.state)
    }

    @Test
    fun `enableRecord without mode sends empty object`() = test {
        enqueueJson("""{"data":{"id":100}}""", code = 202)

        client().cdn.enableRecord("cdn1", "100")

        val request = takeRequest()
        assertEquals("/cdn/cdn1/records/100/enable", request.apiTarget)
        assertEquals(0, request.jsonBody().size)
    }

    @Test
    fun `disableRecord posts to disable`() = test {
        enqueueJson("""{"data":{"id":100,"state":"active"}}""", code = 202)

        val record = client().cdn.disableRecord("cdn1", "100")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/cdn/cdn1/records/100/disable", request.apiTarget)
        assertEquals(RecordState.ACTIVE, record.state)
    }

    @Test
    fun `mappings requests GET`() = test {
        enqueueJson("""{"data":[$mappingJson]}""")

        val mappings = client().cdn.mappings("cdn1")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/cdn/cdn1/mappings", request.apiTarget)
        val mapping = mappings.single()
        assertEquals("ssh", mapping.name)
        assertEquals("rec1", mapping.record)
        assertEquals("home.example.com", mapping.fqdn)
    }

    @Test
    fun `createMapping sends wire names and omits nulls`() = test {
        enqueueJson("""{"data":$mappingJson}""", code = 201)

        val mapping = client().cdn.createMapping(
            "cdn1",
            CreateCdnMappingRequest(record = "rec1", targetPort = 25565, transport = CdnTransport.UDP, name = "ssh"),
        )

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/cdn/cdn1/mappings", request.apiTarget)
        val body = request.jsonBody()
        assertEquals(setOf("record", "target_port", "transport", "name"), body.keys)
        assertEquals(JsonPrimitive(25565), body["target_port"])
        assertEquals(JsonPrimitive("udp"), body["transport"])
        assertEquals("map1", mapping.id)
    }

    @Test
    fun `deleteMapping requests DELETE`() = test {
        enqueueNoContent()

        client().cdn.deleteMapping("cdn1", "map1")

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/cdn/cdn1/mappings/map1", request.apiTarget)
    }

    @Test
    fun `accessList requests GET and decodes entries`() = test {
        enqueueJson("""{"data":[{"id":1,"address":"203.0.113.0/24"},{"id":2,"address":"2001:db8::1"}]}""")

        val entries = client().cdn.accessList("cdn1")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/cdn/cdn1/access-list", request.apiTarget)
        assertEquals(2, entries.size)
        assertEquals(1L, entries[0].id)
        assertEquals("203.0.113.0/24", entries[0].address)
    }

    @Test
    fun `addAccessListEntry sends address`() = test {
        enqueueJson("""{"data":{"id":3,"address":"198.51.100.5"}}""", code = 201)

        val entry = client().cdn.addAccessListEntry("cdn1", "198.51.100.5")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/cdn/cdn1/access-list", request.apiTarget)
        assertEquals(mapOf("address" to JsonPrimitive("198.51.100.5")), request.jsonBody())
        assertEquals(3L, entry.id)
    }

    @Test
    fun `removeAccessListEntry requests DELETE`() = test {
        enqueueNoContent()

        client().cdn.removeAccessListEntry("cdn1", "3")

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/cdn/cdn1/access-list/3", request.apiTarget)
    }

    private companion object {
        const val mappingJson = """{"id":"map1","name":"ssh","listen_port":51000,"transport":"udp",
            "target_port":25565,"record":"rec1","fqdn":"home.example.com"}"""
    }
}
