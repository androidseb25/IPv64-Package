package de.sr.ipv64.api

import de.sr.ipv64.MockServerTest
import de.sr.ipv64.model.CreatePortForwardRequest
import de.sr.ipv64.model.PortForwardProtocol
import de.sr.ipv64.model.PortForwardPublicIp
import de.sr.ipv64.model.ResourceState
import de.sr.ipv64.model.SetOspfRequest
import de.sr.ipv64.model.UpdateGatewayRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class GatewaysApiTest : MockServerTest() {

    private fun body(text: String): JsonObject = Json.parseToJsonElement(text).jsonObject

    private val gatewayJson = """
        {"id":"gw_1","name":"Main","state":"ready","disabled":false,"traffic_limited":false,
         "server":{"name":"vpn1","hostname":"vpn1.ipv64.net","location":"Nuernberg"},
         "listen_port":51820,"ipv4":"10.0.0.1/24","ipv6":"fd00::1/64","public_key":"PUB=",
         "ospf":null,"traffic_bytes":{"in":10.0,"out":20.0},"created_at":"2026-10-10 12:00:00",
         "peers":[{"id":"peer_1","name":"Laptop","state":"applying","site_to_site":true,
           "ipv4":"10.0.0.2/32","keepalive":"00:00:25","endpoint_port":51821,"has_preshared_key":true,
           "allowed_ips":[{"id":5,"network":"192.168.1.0/24"}]}]}
    """.trimIndent()

    @Test
    fun `get decodes gateway with peers and space separated timestamp`() = test {
        enqueueJson("""{"data":$gatewayJson}""")

        val gateway = client().gateways.get("gw_1")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/gateways/gw_1", request.apiTarget)
        assertEquals("Main", gateway.name)
        assertEquals(ResourceState.READY, gateway.state)
        assertNull(gateway.ospf)
        assertEquals(10.0, gateway.trafficBytes?.inbound)
        assertEquals(Instant.parse("2026-10-10T12:00:00Z"), gateway.createdAt)
        val peer = gateway.peers.single()
        assertEquals(ResourceState.APPLYING, peer.state)
        assertEquals(true, peer.siteToSite)
        assertEquals("00:00:25", peer.keepalive)
        assertEquals(51821, peer.endpointPort)
        assertEquals("192.168.1.0/24", peer.allowedIps.single().network)
    }

    @Test
    fun `update patches only set fields`() = test {
        enqueueJson("""{"data":$gatewayJson}""")

        client().gateways.update("gw_1", UpdateGatewayRequest(name = "Renamed", ipv6 = ""))

        val request = takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/gateways/gw_1", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals(setOf("name", "ipv6"), sent.keys)
        assertEquals("", sent["ipv6"]?.jsonPrimitive?.content)
    }

    @Test
    fun `delete uses delete method`() = test {
        enqueueJson("""{"data":{"id":"gw_1","state":"deleting"}}""")

        val gateway = client().gateways.delete("gw_1")

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/gateways/gw_1", request.apiTarget)
        assertEquals(ResourceState.DELETING, gateway.state)
    }

    @Test
    fun `rotateKeys posts to rotate-keys`() = test {
        enqueueJson("""{"data":{"id":"gw_1","public_key":"NEWPUB="}}""")

        val gateway = client().gateways.rotateKeys("gw_1")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/gateways/gw_1/rotate-keys", request.apiTarget)
        assertEquals("NEWPUB=", gateway.publicKey)
    }

    @Test
    fun `resetTraffic posts to reset-traffic`() = test {
        enqueueJson("""{"data":{"id":"gw_1","traffic_bytes":{"in":0.0,"out":0.0}}}""")

        val gateway = client().gateways.resetTraffic("gw_1")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/gateways/gw_1/reset-traffic", request.apiTarget)
        assertEquals(0.0, gateway.trafficBytes?.inbound)
    }

    @Test
    fun `setOspf puts settings with wire names`() = test {
        enqueueJson("""{"data":{"id":"gw_1","ospf":{"area_id":"0.0.0.1","ospf_version":2,"cost":50}}}""")

        val gateway = client().gateways.setOspf("gw_1", SetOspfRequest(areaId = "0.0.0.1", version = 2, cost = 50))

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/gateways/gw_1/ospf", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals("0.0.0.1", sent["area_id"]?.jsonPrimitive?.content)
        assertEquals(2, sent["version"]?.jsonPrimitive?.int)
        assertEquals(50, sent["cost"]?.jsonPrimitive?.int)
        assertEquals("0.0.0.1", gateway.ospf?.areaId)
        assertEquals(2, gateway.ospf?.ospfVersion)
        assertEquals(50, gateway.ospf?.cost)
    }

    @Test
    fun `setOspf default sends empty object`() = test {
        enqueueJson("""{"data":{"id":"gw_1"}}""")

        client().gateways.setOspf("gw_1")

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals(0, body(request.bodyText()).size)
    }

    @Test
    fun `disableOspf uses delete`() = test {
        enqueueJson("""{"data":{"id":"gw_1","ospf":null}}""")

        val gateway = client().gateways.disableOspf("gw_1")

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/gateways/gw_1/ospf", request.apiTarget)
        assertNull(gateway.ospf)
    }

    @Test
    fun `listPortForwards decodes enums`() = test {
        enqueueJson(
            """{"data":[{"id":1,"name":"ssh","destination":"10.0.0.2","target_port":22,"protocol":"tcp",
                "public_port":40022,"public_ip":"server","disabled":false,"state":"ready"}]}""",
        )

        val forwards = client().gateways.listPortForwards("gw_1")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/gateways/gw_1/port-forwards", request.apiTarget)
        val forward = forwards.single()
        assertEquals(PortForwardProtocol.TCP, forward.protocol)
        assertEquals(PortForwardPublicIp.SERVER, forward.publicIp)
        assertEquals(40022, forward.publicPort)
        assertEquals(22, forward.targetPort)
        assertEquals(ResourceState.READY, forward.state)
    }

    @Test
    fun `createPortForward sends wire values`() = test {
        enqueueJson("""{"data":{"id":2,"protocol":"udp","public_ip":"router","public_port":40100}}""", code = 201)

        val forward = client().gateways.createPortForward(
            "gw_1",
            CreatePortForwardRequest(
                destination = "10.0.0.2",
                targetPort = 51820,
                protocol = PortForwardProtocol.UDP,
                publicIp = PortForwardPublicIp.ROUTER,
            ),
        )

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/gateways/gw_1/port-forwards", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals(setOf("destination", "target_port", "protocol", "public_ip"), sent.keys)
        assertEquals("udp", sent["protocol"]?.jsonPrimitive?.content)
        assertEquals("router", sent["public_ip"]?.jsonPrimitive?.content)
        assertEquals(51820, sent["target_port"]?.jsonPrimitive?.int)
        assertEquals(40100, forward.publicPort)
        assertEquals(PortForwardPublicIp.ROUTER, forward.publicIp)
    }

    @Test
    fun `setPortForwardDisabled patches disabled`() = test {
        enqueueJson("""{"data":{"id":1,"disabled":true}}""")

        val forward = client().gateways.setPortForwardDisabled("gw_1", 1, true)

        val request = takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/gateways/gw_1/port-forwards/1", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals(setOf("disabled"), sent.keys)
        assertTrue(sent["disabled"]!!.jsonPrimitive.boolean)
        assertEquals(true, forward.disabled)
    }

    @Test
    fun `deletePortForward uses delete`() = test {
        enqueueJson("""{"data":{"id":1,"state":"deleting"}}""")

        val state = client().gateways.deletePortForward("gw_1", 1)

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/gateways/gw_1/port-forwards/1", request.apiTarget)
        assertEquals(1L, state.id)
        assertEquals("deleting", state.state)
    }

    @Test
    fun `listPeers decodes peers`() = test {
        enqueueJson(
            """{"data":[{"id":"peer_1","name":"Laptop","state":"ready","disabled":false,
                "ipv4":"10.0.0.2/32","ipv6":"fd00::2/128","dns_server":"10.0.0.1","public_key":"PK=",
                "last_handshake":"2026-10-10T12:00:00+02:00","current_endpoint":"198.51.100.4:51820",
                "traffic_bytes":{"in":5.0,"out":6.0}}]}""",
        )

        val peers = client().gateways.listPeers("gw_1")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/gateways/gw_1/peers", request.apiTarget)
        val peer = peers.single()
        assertEquals("peer_1", peer.id)
        assertEquals("10.0.0.1", peer.dnsServer)
        assertEquals("198.51.100.4:51820", peer.currentEndpoint)
        assertEquals(6.0, peer.trafficBytes?.outbound)
    }

    @Test
    fun `createPeer posts name`() = test {
        enqueueJson("""{"data":{"id":"peer_2","name":"Phone","state":"creating"}}""", code = 201)

        val peer = client().gateways.createPeer("gw_1", "Phone")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/gateways/gw_1/peers", request.apiTarget)
        assertEquals("Phone", body(request.bodyText())["name"]?.jsonPrimitive?.content)
        assertEquals(ResourceState.CREATING, peer.state)
    }

    @Test
    fun `createPeer without name sends empty object`() = test {
        enqueueJson("""{"data":{"id":"peer_3"}}""")

        client().gateways.createPeer("gw_1")

        assertEquals(0, body(takeRequest().bodyText()).size)
    }
}
