package de.sr.ipv64.api

import de.sr.ipv64.model.CreateRecordRequest
import de.sr.ipv64.model.RecordState
import de.sr.ipv64.model.RecordType
import de.sr.ipv64.model.UpdateRecordRequest
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Instant

class RecordsApiTest : ApiSdkTestBase() {

    private val recordJson = """{"id":100,"domain_id":10,"domain":"home.ipv64.net","prefix":"mail",
        "fqdn":"mail.home.ipv64.net","type":"MX","content":"10 mail.example.com","ttl":60,"state":"active",
        "pending_dns":false,"token":"tok123","updated_at":"2026-10-10T12:00:00+02:00"}"""

    private fun assertRecord(record: de.sr.ipv64.model.Record) {
        assertEquals(100L, record.id)
        assertEquals(10L, record.domainId)
        assertEquals("mail.home.ipv64.net", record.fqdn)
        assertEquals(RecordType.MX, record.type)
        assertEquals("10 mail.example.com", record.content)
        assertEquals(60, record.ttl)
        assertEquals(RecordState.ACTIVE, record.state)
        assertEquals(false, record.pendingDns)
        assertEquals("tok123", record.token)
        assertEquals(Instant.parse("2026-10-10T10:00:00Z"), record.updatedAt)
    }

    @Test
    fun `list without filters`() = test {
        enqueueJson("""{"data":[$recordJson]}""")

        val records = client().records.list("home.ipv64.net")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/domains/home.ipv64.net/records", request.apiTarget)
        assertRecord(records.single())
    }

    @Test
    fun `list with type and prefix filters`() = test {
        enqueueJson("""{"data":[]}""")

        client().records.list("10", type = RecordType.AAAA, prefix = "www")

        assertEquals("/domains/10/records?type=AAAA&prefix=www", takeRequest().apiTarget)
    }

    @Test
    fun `create sends type content and prefix`() = test {
        enqueueJson("""{"data":$recordJson}""", code = 201)

        val record = client().records.create("10", RecordType.MX, "10 mail.example.com", prefix = "mail")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/domains/10/records", request.apiTarget)
        val body = request.jsonBody()
        assertEquals(JsonPrimitive("MX"), body["type"])
        assertEquals(JsonPrimitive("10 mail.example.com"), body["content"])
        assertEquals(JsonPrimitive("mail"), body["prefix"])
        assertRecord(record)
    }

    @Test
    fun `create without prefix omits it`() = test {
        enqueueJson("""{"data":$recordJson}""", code = 201)

        client().records.create("10", CreateRecordRequest(RecordType.A, "203.0.113.1"))

        assertEquals(setOf("type", "content"), takeRequest().jsonBody().keys)
    }

    @Test
    fun `deleteByContent sends type content and prefix as query`() = test {
        enqueueNoContent()

        client().records.deleteByContent("10", RecordType.TXT, "v=spf1 -all", prefix = "_acme")

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/domains/10/records?type=TXT&content=v%3Dspf1%20-all&prefix=_acme", request.apiTarget)
        assertEquals("v=spf1 -all", request.url.queryParameter("content"))
    }

    @Test
    fun `deleteByContent without prefix omits it`() = test {
        enqueueNoContent()

        client().records.deleteByContent("10", RecordType.A, "203.0.113.1")

        assertEquals("/domains/10/records?type=A&content=203.0.113.1", takeRequest().apiTarget)
    }

    @Test
    fun `get requests record by token and decodes it`() = test {
        enqueueJson("""{"data":$recordJson}""")

        val record = client().records.get("tok123")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/records/tok123", request.apiTarget)
        assertRecord(record)
    }

    @Test
    fun `get encodes a token with space and slash as a single segment`() = test {
        enqueueJson("""{"data":$recordJson}""")

        client().records.get("to ken/1")

        assertEquals("/records/to%20ken%2F1", takeRequest().apiTarget)
    }

    @Test
    fun `update sends only non-null fields`() = test {
        enqueueJson("""{"data":$recordJson}""")

        client().records.update("100", content = "203.0.113.9")

        val request = takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/records/100", request.apiTarget)
        val body = request.jsonBody()
        assertEquals(setOf("content"), body.keys)
        assertEquals(JsonPrimitive("203.0.113.9"), body["content"])
    }

    @Test
    fun `update from request object with all fields`() = test {
        enqueueJson("""{"data":$recordJson}""")

        client().records.update("100", UpdateRecordRequest(RecordType.CNAME, "app", "target.example.com"))

        val body = takeRequest().jsonBody()
        assertEquals(JsonPrimitive("CNAME"), body["type"])
        assertEquals(JsonPrimitive("app"), body["prefix"])
        assertEquals(JsonPrimitive("target.example.com"), body["content"])
    }

    @Test
    fun `delete requests DELETE records record`() = test {
        enqueueNoContent()

        client().records.delete("100")

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/records/100", request.apiTarget)
    }
}
