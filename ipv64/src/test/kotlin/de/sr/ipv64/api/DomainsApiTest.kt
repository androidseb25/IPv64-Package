package de.sr.ipv64.api

import de.sr.ipv64.model.CreateDomainRequest
import de.sr.ipv64.model.DomainKind
import de.sr.ipv64.model.IgnoreFamily
import de.sr.ipv64.model.RecordState
import de.sr.ipv64.model.RecordType
import de.sr.ipv64.model.UpdateDomainRequest
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class DomainsApiTest : ApiSdkTestBase() {

    private val domainJson = """{"id":10,"name":"home.ipv64.net","kind":"dyndns","zone":"ipv64.net",
        "wildcard":true,"ignore":"ipv6","ipv6_prefix":"2001:db8:1234:5600::/56","active":true,
        "update_token":"SECRET-UPDATE-TOKEN","update_count":99,
        "created_at":"2026-01-01T00:00:00Z","updated_at":"2026-10-10T10:00:00+02:00"}"""

    private val detailJson = """{"id":10,"name":"home.ipv64.net","kind":"own","zone":null,"wildcard":false,
        "ignore":"none","active":true,"update_token":"SECRET-UPDATE-TOKEN","update_count":0,
        "created_at":"2026-01-01T00:00:00Z",
        "records":[{"id":100,"domain_id":10,"domain":"home.ipv64.net","prefix":"www","fqdn":"www.home.ipv64.net",
        "type":"A","content":"203.0.113.7","ttl":60,"state":"cdn_proxied","pending_dns":true,"token":"rectok",
        "updated_at":"2026-10-10T10:00:00Z"}]}"""

    @Test
    fun `list without filter sends no query and decodes domains`() = test {
        enqueueJson("""{"data":[$domainJson]}""")

        val domains = client().domains.list()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/domains", request.apiTarget)
        val domain = domains.single()
        assertEquals(10L, domain.id)
        assertEquals(DomainKind.DYNDNS, domain.kind)
        assertEquals("ipv64.net", domain.zone)
        assertEquals(true, domain.wildcard)
        assertEquals(IgnoreFamily.IPV6, domain.ignore)
        assertEquals("2001:db8:1234:5600::/56", domain.ipv6Prefix)
        assertEquals("SECRET-UPDATE-TOKEN", domain.updateToken)
        assertEquals(99L, domain.updateCount)
        assertEquals(Instant.parse("2026-10-10T08:00:00Z"), domain.updatedAt)
    }

    @Test
    fun `list sends kind filter`() = test {
        enqueueJson("""{"data":[]}""")

        client().domains.list(DomainKind.OWN)

        assertEquals("/domains?kind=own", takeRequest().apiTarget)
    }

    @Test
    fun `create with name and own sends both fields`() = test {
        enqueueJson("""{"data":$detailJson}""", code = 201)

        val detail = client().domains.create("home.ipv64.net", own = true)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/domains", request.apiTarget)
        val body = request.jsonBody()
        assertEquals(JsonPrimitive("home.ipv64.net"), body["name"])
        assertEquals(JsonPrimitive(true), body["own"])
        assertEquals(DomainKind.OWN, detail.kind)
        val record = detail.records.single()
        assertEquals(RecordType.A, record.type)
        assertEquals(RecordState.CDN_PROXIED, record.state)
        assertEquals(60, record.ttl)
        assertEquals(true, record.pendingDns)
        assertEquals("rectok", record.token)
    }

    @Test
    fun `create without own omits the field`() = test {
        enqueueJson("""{"data":$detailJson}""", code = 201)

        client().domains.create("home.ipv64.net")

        val body = takeRequest().jsonBody()
        assertEquals(setOf("name"), body.keys)
    }

    @Test
    fun `create from request object`() = test {
        enqueueJson("""{"data":$detailJson}""", code = 201)

        client().domains.create(CreateDomainRequest(name = "x.example.com", own = false))

        val body = takeRequest().jsonBody()
        assertEquals(JsonPrimitive("x.example.com"), body["name"])
        assertEquals(JsonPrimitive(false), body["own"])
    }

    @Test
    fun `get requests the domain with records`() = test {
        enqueueJson("""{"data":$detailJson}""")

        val detail = client().domains.get("home.ipv64.net")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/domains/home.ipv64.net", request.apiTarget)
        assertEquals(1, detail.records.size)
        assertEquals("www.home.ipv64.net", detail.records[0].fqdn)
    }

    @Test
    fun `get encodes special characters in the domain segment`() = test {
        enqueueJson("""{"data":$detailJson}""")

        client().domains.get("we ird/name")

        assertEquals("/domains/we%20ird%2Fname", takeRequest().apiTarget)
    }

    @Test
    fun `update sends only non-null fields`() = test {
        enqueueJson("""{"data":$detailJson}""")

        client().domains.update("10", wildcard = true)

        val request = takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/domains/10", request.apiTarget)
        val body = request.jsonBody()
        assertEquals(setOf("wildcard"), body.keys)
        assertEquals(JsonPrimitive(true), body["wildcard"])
    }

    @Test
    fun `update sends all fields with wire names`() = test {
        enqueueJson("""{"data":$detailJson}""")

        client().domains.update("10", wildcard = false, ignore = IgnoreFamily.IPV4, ipv6Prefix = "")

        val body = takeRequest().jsonBody()
        assertEquals(JsonPrimitive(false), body["wildcard"])
        assertEquals(JsonPrimitive("ipv4"), body["ignore"])
        assertEquals(JsonPrimitive(""), body["ipv6_prefix"])
    }

    @Test
    fun `update from request object sends empty object when nothing set`() = test {
        enqueueJson("""{"data":$detailJson}""")

        client().domains.update("10", UpdateDomainRequest())

        val request = takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals(0, request.jsonBody().size)
    }

    @Test
    fun `delete requests DELETE domains domain and accepts 204`() = test {
        enqueueNoContent()

        client().domains.delete("10")

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/domains/10", request.apiTarget)
    }

    @Test
    fun `rotateToken posts and returns domain with new token`() = test {
        enqueueJson("""{"data":${domainJson.replace("SECRET-UPDATE-TOKEN", "NEW-TOKEN")}}""")

        val domain = client().domains.rotateToken("10")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/domains/10/rotate-token", request.apiTarget)
        assertEquals("NEW-TOKEN", domain.updateToken)
    }

    @Test
    fun `reset posts and decodes the detail`() = test {
        enqueueJson("""{"data":$detailJson}""")

        val detail = client().domains.reset("home.ipv64.net")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/domains/home.ipv64.net/reset", request.apiTarget)
        assertEquals(10L, detail.id)
    }

    @Test
    fun `toString of Domain and DomainDetail redacts the update token`() = test {
        enqueueJson("""{"data":[$domainJson]}""")
        enqueueJson("""{"data":$detailJson}""")

        val domain = client().domains.list().single()
        val detail = client().domains.get("10")

        for (text in listOf(domain.toString(), detail.toString())) {
            assertFalse(text, text.contains("SECRET-UPDATE-TOKEN"))
            assertTrue(text, text.contains("updateToken=<redacted>"))
            assertTrue(text, text.contains("name=home.ipv64.net"))
        }
    }
}
