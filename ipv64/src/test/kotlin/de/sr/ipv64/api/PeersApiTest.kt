package de.sr.ipv64.api

import de.sr.ipv64.MockServerTest
import de.sr.ipv64.model.ResourceState
import de.sr.ipv64.model.SetPeerKeysRequest
import de.sr.ipv64.model.UpdatePeerRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mockwebserver3.MockResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PeersApiTest : MockServerTest() {

    private fun body(text: String): JsonObject = Json.parseToJsonElement(text).jsonObject

    private val peerJson = """
        {"id":"peer_1","name":"Laptop","state":"ready","disabled":false,"site_to_site":false,
         "ipv4":"10.0.0.2/32","ipv6":"fd00::2/128","dns_server":"10.0.0.1","keepalive":"00:00:25",
         "endpoint":"home.example.org","endpoint_port":51821,"public_key":"PK=","has_preshared_key":true,
         "allowed_ips":[{"id":5,"network":"192.168.1.0/24"},{"id":6,"network":"10.9.0.0/16"}]}
    """.trimIndent()

    @Test
    fun `get decodes peer with allowed ips`() = test {
        enqueueJson("""{"data":$peerJson}""")

        val peer = client().peers.get("peer_1")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/peers/peer_1", request.apiTarget)
        assertEquals("Laptop", peer.name)
        assertEquals(ResourceState.READY, peer.state)
        assertEquals("home.example.org", peer.endpoint)
        assertEquals(true, peer.hasPresharedKey)
        assertEquals(listOf(5L, 6L), peer.allowedIps.map { it.id })
        assertEquals("10.9.0.0/16", peer.allowedIps[1].network)
    }

    @Test
    fun `update patches only set fields`() = test {
        enqueueJson("""{"data":$peerJson}""")

        client().peers.update(
            "peer_1",
            UpdatePeerRequest(name = "Renamed", dnsServer = "", endpointPort = 0, siteToSite = true),
        )

        val request = takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/peers/peer_1", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals(setOf("name", "dns_server", "endpoint_port", "site_to_site"), sent.keys)
        assertEquals("", sent["dns_server"]?.jsonPrimitive?.content)
        assertEquals(0, sent["endpoint_port"]?.jsonPrimitive?.int)
        assertTrue(sent["site_to_site"]!!.jsonPrimitive.boolean)
    }

    @Test
    fun `delete uses delete method`() = test {
        enqueueJson("""{"data":{"id":"peer_1","state":"deleting"}}""")

        val peer = client().peers.delete("peer_1")

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/peers/peer_1", request.apiTarget)
        assertEquals(ResourceState.DELETING, peer.state)
    }

    @Test
    fun `config requests text plain and returns raw string`() = test {
        val conf = "[Interface]\nPrivateKey = SECRET=\nAddress = 10.0.0.2/32\n\n[Peer]\nPublicKey = PUB=\n"
        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .setHeader("Content-Type", "text/plain; charset=utf-8")
                .body(conf)
                .build(),
        )

        val text = client().peers.config("peer_1")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/peers/peer_1/config", request.apiTarget)
        assertEquals("text/plain", request.headers["Accept"])
        assertEquals(conf, text)
    }

    @Test
    fun `rotateKeys posts`() = test {
        enqueueJson("""{"data":{"id":"peer_1","public_key":"NEW="}}""")

        val peer = client().peers.rotateKeys("peer_1")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/peers/peer_1/rotate-keys", request.apiTarget)
        assertEquals("NEW=", peer.publicKey)
    }

    @Test
    fun `rotatePresharedKey posts`() = test {
        enqueueJson("""{"data":{"id":"peer_1","has_preshared_key":true}}""")

        val peer = client().peers.rotatePresharedKey("peer_1")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/peers/peer_1/rotate-preshared-key", request.apiTarget)
        assertEquals(true, peer.hasPresharedKey)
    }

    @Test
    fun `setKeys puts keys and omits missing private key`() = test {
        enqueueJson("""{"data":$peerJson}""")

        client().peers.setKeys("peer_1", SetPeerKeysRequest(publicKey = "PK=", presharedKey = "PSK="))

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/peers/peer_1/keys", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals(setOf("public_key", "preshared_key"), sent.keys)
        assertEquals("PK=", sent["public_key"]?.jsonPrimitive?.content)
    }

    @Test
    fun `setKeys sends private key when given`() = test {
        enqueueJson("""{"data":$peerJson}""")

        client().peers.setKeys("peer_1", SetPeerKeysRequest("PK=", privateKey = "PRIV="))

        val sent = body(takeRequest().bodyText())
        assertEquals("PRIV=", sent["private_key"]?.jsonPrimitive?.content)
        assertFalse(sent.containsKey("preshared_key"))
    }

    @Test
    fun `addAllowedIp posts network`() = test {
        enqueueJson("""{"data":$peerJson}""", code = 201)

        val peer = client().peers.addAllowedIp("peer_1", "172.16.0.0/12")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/peers/peer_1/allowed-ips", request.apiTarget)
        assertEquals(setOf("network"), body(request.bodyText()).keys)
        assertEquals("172.16.0.0/12", body(request.bodyText())["network"]?.jsonPrimitive?.content)
        assertEquals(2, peer.allowedIps.size)
    }

    @Test
    fun `removeAllowedIp deletes and accepts 204`() = test {
        enqueueNoContent()

        client().peers.removeAllowedIp("peer_1", 5)

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/peers/peer_1/allowed-ips/5", request.apiTarget)
    }

    @Test
    fun `toString of key request redacts secrets`() {
        val text = SetPeerKeysRequest("PUBLIC=", privateKey = "PRIVSECRET", presharedKey = "PSKSECRET").toString()
        assertFalse(text.contains("PRIVSECRET"))
        assertFalse(text.contains("PSKSECRET"))
        assertTrue(text.contains("PUBLIC="))
    }
}
