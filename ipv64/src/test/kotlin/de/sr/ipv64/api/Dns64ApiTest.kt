package de.sr.ipv64.api

import de.sr.ipv64.MockServerTest
import de.sr.ipv64.model.Dns64Blocklist
import de.sr.ipv64.model.Dns64RuleAction
import de.sr.ipv64.model.Dns64SourceKind
import de.sr.ipv64.model.Dns64State
import de.sr.ipv64.model.UpdateDns64ClientRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import kotlin.time.Instant

class Dns64ApiTest : MockServerTest() {

    private fun body(text: String): JsonObject = Json.parseToJsonElement(text).jsonObject

    private val clientJson = """
        {"id":"d64_abc","name":"Home","client_id":"c1d2e3","state":"ready","blocking_enabled":true,
         "blocklists":["ads","future_list","threats"],
         "servers":[{"location":"Nuernberg","hostname":"dns1.ipv64.net","doh":"https://dns1.ipv64.net/c1d2e3",
           "dot":"c1d2e3.dns1.ipv64.net","doq":"quic://c1d2e3.dns1.ipv64.net","ipv6":"2001:db8::53",
           "ipv4_shared":"192.0.2.53","ipv6_shared":"2001:db8::54",
           "stamps":{"doh":"sdns://doh","dot":"sdns://dot","doq":"sdns://doq"}}],
         "queries_total":1200,"queries_blocked":300,"created_at":"2026-10-10T12:00:00+02:00",
         "rules":[{"id":1,"domain":"example.org","action":"block","state":"applying"}],
         "sources":[{"id":"12","kind":"ip","address":"203.0.113.9"},
           {"id":"rec_tok","kind":"record","address":"198.51.100.2","fqdn":"home.ipv64.net"}]}
    """.trimIndent()

    @Test
    fun `listClients decodes clients leniently`() = test {
        enqueueJson("""{"data":[$clientJson]}""")

        val clients = client().dns64.listClients()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/dns64/clients", request.apiTarget)
        val c = clients.single()
        assertEquals("c1d2e3", c.clientId)
        assertEquals(Dns64State.READY, c.state)
        assertEquals(listOf(Dns64Blocklist.ADS, Dns64Blocklist.THREATS), c.blocklists)
        assertEquals(1200L, c.queriesTotal)
        assertEquals(Instant.parse("2026-10-10T10:00:00Z"), c.createdAt)
        val server = c.servers.single()
        assertEquals("dns1.ipv64.net", server.hostname)
        assertEquals("192.0.2.53", server.ipv4Shared)
        assertEquals("sdns://doq", server.stamps?.doq)
        assertEquals(Dns64RuleAction.BLOCK, c.rules.single().action)
        assertEquals(Dns64State.APPLYING, c.rules.single().state)
        assertEquals(Dns64SourceKind.IP, c.sources[0].kind)
        assertEquals("home.ipv64.net", c.sources[1].fqdn)
    }

    @Test
    fun `createClient posts name`() = test {
        enqueueJson("""{"data":{"id":"d64_new","name":"Kids","state":"applying"}}""", code = 201)

        val c = client().dns64.createClient("Kids")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/dns64/clients", request.apiTarget)
        assertEquals("Kids", body(request.bodyText())["name"]?.jsonPrimitive?.content)
        assertEquals(Dns64State.APPLYING, c.state)
    }

    @Test
    fun `createClient without name sends empty object`() = test {
        enqueueJson("""{"data":{"id":"d64_new"}}""")

        client().dns64.createClient()

        assertEquals(0, body(takeRequest().bodyText()).size)
    }

    @Test
    fun `getClient requests by token`() = test {
        enqueueJson("""{"data":$clientJson}""")

        val c = client().dns64.getClient("d64_abc")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/dns64/clients/d64_abc", request.apiTarget)
        assertEquals(2, c.sources.size)
    }

    @Test
    fun `updateClient patches only set fields`() = test {
        enqueueJson("""{"data":$clientJson}""")

        client().dns64.updateClient(
            "d64_abc",
            UpdateDns64ClientRequest(blockingEnabled = false, blocklists = listOf(Dns64Blocklist.ADS)),
        )

        val request = takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/dns64/clients/d64_abc", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals(setOf("blocking_enabled", "blocklists"), sent.keys)
        assertFalse(sent["blocking_enabled"]!!.jsonPrimitive.boolean)
        assertEquals(listOf("ads"), sent["blocklists"]!!.jsonArray.map { it.jsonPrimitive.content })
    }

    @Test
    fun `deleteClient returns string id deletion`() = test {
        enqueueJson("""{"data":{"id":"d64_abc","state":"deleting"}}""")

        val deletion = client().dns64.deleteClient("d64_abc")

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/dns64/clients/d64_abc", request.apiTarget)
        assertEquals("d64_abc", deletion.id)
        assertEquals("deleting", deletion.state)
    }

    @Test
    fun `addRule posts domain and action`() = test {
        enqueueJson("""{"data":$clientJson}""", code = 201)

        val c = client().dns64.addRule("d64_abc", "ads.example.com", Dns64RuleAction.ALLOW)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/dns64/clients/d64_abc/rules", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals("ads.example.com", sent["domain"]?.jsonPrimitive?.content)
        assertEquals("allow", sent["action"]?.jsonPrimitive?.content)
        assertEquals(1, c.rules.size)
    }

    @Test
    fun `addRule omits action when null`() = test {
        enqueueJson("""{"data":$clientJson}""")

        client().dns64.addRule("d64_abc", "example.org")

        assertEquals(setOf("domain"), body(takeRequest().bodyText()).keys)
    }

    @Test
    fun `deleteRule deletes and accepts 204`() = test {
        enqueueNoContent()

        client().dns64.deleteRule("d64_abc", 1)

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/dns64/clients/d64_abc/rules/1", request.apiTarget)
    }

    @Test
    fun `addSource with ip omits record`() = test {
        enqueueJson("""{"data":$clientJson}""", code = 201)

        client().dns64.addSource("d64_abc", ip = "203.0.113.9")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/dns64/clients/d64_abc/sources", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals(setOf("ip"), sent.keys)
        assertEquals("203.0.113.9", sent["ip"]?.jsonPrimitive?.content)
    }

    @Test
    fun `addSource with record omits ip`() = test {
        enqueueJson("""{"data":$clientJson}""")

        client().dns64.addSource("d64_abc", record = "rec_tok")

        assertEquals(setOf("record"), body(takeRequest().bodyText()).keys)
    }

    @Test
    fun `deleteSource accepts record token and encodes segment`() = test {
        enqueueNoContent()
        enqueueNoContent()

        client().dns64.deleteSource("d64_abc", "12")
        client().dns64.deleteSource("d64_abc", "a/b c")

        val first = takeRequest()
        assertEquals("DELETE", first.method)
        assertEquals("/dns64/clients/d64_abc/sources/12", first.apiTarget)
        assertEquals("/dns64/clients/d64_abc/sources/a%2Fb%20c", takeRequest().apiTarget)
    }
}
