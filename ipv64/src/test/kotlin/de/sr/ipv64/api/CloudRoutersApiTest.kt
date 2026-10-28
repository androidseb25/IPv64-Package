package de.sr.ipv64.api

import de.sr.ipv64.MockServerTest
import de.sr.ipv64.model.CreateFirewallRuleRequest
import de.sr.ipv64.model.CreateRouteRequest
import de.sr.ipv64.model.FirewallAction
import de.sr.ipv64.model.FirewallChain
import de.sr.ipv64.model.IpFamily
import de.sr.ipv64.model.ResourceState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class CloudRoutersApiTest : MockServerTest() {

    private fun body(text: String): JsonObject = Json.parseToJsonElement(text).jsonObject

    private val routerJson = """
        {"id":"rtr_abc","name":"Home","vrf":"vrf1","traffic_limited":false,
         "gateways":[{"id":"gw_1","name":"Main","state":"ready","disabled":false,
           "server":{"name":"vpn1","hostname":"vpn1.ipv64.net","location":"Nuernberg"},
           "listen_port":51820,"ipv4":"10.0.0.1/24","ipv6":"fd00::1/64","public_key":"PUB=",
           "ospf":{"area_id":"0.0.0.0","ospf_version":3,"cost":10},
           "traffic_bytes":{"in":1024.0,"out":2048.0},
           "created_at":"2026-10-10T12:00:00+02:00"}],
         "public_ips":[{"id":7,"ip":"2001:db8::7","type":"ipv6","disabled":false,"state":"ready"}]}
    """.trimIndent()

    @Test
    fun `list returns routers with nested gateways and public ips`() = test {
        enqueueJson("""{"data":[$routerJson]}""")

        val routers = client().cloudRouters.list()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/cloud-routers", request.apiTarget)
        val router = routers.single()
        assertEquals("rtr_abc", router.id)
        assertEquals("Home", router.name)
        assertEquals(false, router.trafficLimited)
        val gw = router.gateways.single()
        assertEquals(ResourceState.READY, gw.state)
        assertEquals("vpn1.ipv64.net", gw.server?.hostname)
        assertEquals(51820, gw.listenPort)
        assertEquals(3, gw.ospf?.ospfVersion)
        assertEquals(2048.0, gw.trafficBytes?.outbound)
        assertEquals(Instant.parse("2026-10-10T10:00:00Z"), gw.createdAt)
        assertEquals(7L, router.publicIps.single().id)
        assertEquals("ipv6", router.publicIps.single().type)
    }

    @Test
    fun `create sends name`() = test {
        enqueueJson("""{"data":$routerJson}""", code = 201)

        val router = client().cloudRouters.create("Home")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/cloud-routers", request.apiTarget)
        assertEquals("Home", body(request.bodyText())["name"]?.jsonPrimitive?.content)
        assertEquals("rtr_abc", router.id)
    }

    @Test
    fun `create without name sends empty object`() = test {
        enqueueJson("""{"data":$routerJson}""")

        client().cloudRouters.create()

        assertEquals(0, body(takeRequest().bodyText()).size)
    }

    @Test
    fun `get requests router by token`() = test {
        enqueueJson("""{"data":$routerJson}""")

        val router = client().cloudRouters.get("rtr_abc")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/cloud-routers/rtr_abc", request.apiTarget)
        assertEquals("vrf1", router.vrf)
    }

    @Test
    fun `rename patches name`() = test {
        enqueueJson("""{"data":{"id":"rtr_abc","name":"Office"}}""")

        val router = client().cloudRouters.rename("rtr_abc", "Office")

        val request = takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/cloud-routers/rtr_abc", request.apiTarget)
        assertEquals(setOf("name"), body(request.bodyText()).keys)
        assertEquals("Office", router.name)
    }

    @Test
    fun `createGateway posts name`() = test {
        enqueueJson("""{"data":{"id":"gw_2","name":"Second","state":"creating"}}""", code = 201)

        val gateway = client().cloudRouters.createGateway("rtr_abc", "Second")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/cloud-routers/rtr_abc/gateways", request.apiTarget)
        assertEquals("Second", body(request.bodyText())["name"]?.jsonPrimitive?.content)
        assertEquals(ResourceState.CREATING, gateway.state)
    }

    @Test
    fun `listRoutes decodes routes`() = test {
        enqueueJson(
            """{"data":[{"id":3,"destination":"192.168.10.0/24","gateway":"10.0.0.2","distance":10,
                "peer":"peer_1","system":false,"disabled":true,"state":"applying"}]}""",
        )

        val routes = client().cloudRouters.listRoutes("rtr_abc")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/cloud-routers/rtr_abc/routes", request.apiTarget)
        val route = routes.single()
        assertEquals(3L, route.id)
        assertEquals("192.168.10.0/24", route.destination)
        assertEquals(10, route.distance)
        assertEquals(true, route.disabled)
        assertEquals(ResourceState.APPLYING, route.state)
    }

    @Test
    fun `createRoute sends request and omits null distance`() = test {
        enqueueJson("""{"data":{"id":4,"destination":"192.168.10.0/24","state":"creating"}}""", code = 201)

        val route = client().cloudRouters.createRoute(
            "rtr_abc",
            CreateRouteRequest(destination = "192.168.10.0/24", gateway = "10.0.0.2", peer = "peer_1"),
        )

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/cloud-routers/rtr_abc/routes", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals(setOf("destination", "gateway", "peer"), sent.keys)
        assertEquals("10.0.0.2", sent["gateway"]?.jsonPrimitive?.content)
        assertEquals(4L, route.id)
    }

    @Test
    fun `createRoute sends distance when set`() = test {
        enqueueJson("""{"data":{"id":4}}""")

        client().cloudRouters.createRoute("t", CreateRouteRequest("10.1.0.0/16", "10.0.0.2", "p", distance = 20))

        assertEquals(20, body(takeRequest().bodyText())["distance"]?.jsonPrimitive?.int)
    }

    @Test
    fun `setRouteDisabled patches only disabled`() = test {
        enqueueJson("""{"data":{"id":3,"disabled":true}}""")

        val route = client().cloudRouters.setRouteDisabled("rtr_abc", 3, true)

        val request = takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/cloud-routers/rtr_abc/routes/3", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals(setOf("disabled"), sent.keys)
        assertTrue(sent["disabled"]!!.jsonPrimitive.boolean)
        assertEquals(true, route.disabled)
    }

    @Test
    fun `deleteRoute returns deletion state`() = test {
        enqueueJson("""{"data":{"id":3,"state":"deleting"}}""", code = 202)

        val state = client().cloudRouters.deleteRoute("rtr_abc", 3)

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/cloud-routers/rtr_abc/routes/3", request.apiTarget)
        assertEquals(3L, state.id)
        assertEquals("deleting", state.state)
    }

    @Test
    fun `listFirewallRules decodes enums`() = test {
        enqueueJson(
            """{"data":[{"id":9,"chain":"forward","action":"drop","family":"ipv4",
                "source":"203.0.113.0/24","destination":"10.0.0.5","disabled":false,"state":"ready"}]}""",
        )

        val rules = client().cloudRouters.listFirewallRules("rtr_abc")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/cloud-routers/rtr_abc/firewall", request.apiTarget)
        val rule = rules.single()
        assertEquals(FirewallChain.FORWARD, rule.chain)
        assertEquals(FirewallAction.DROP, rule.action)
        assertEquals(IpFamily.IPV4, rule.family)
        assertEquals("203.0.113.0/24", rule.source)
        assertEquals(ResourceState.READY, rule.state)
    }

    @Test
    fun `createFirewallRule sends wire values and omits null destination`() = test {
        enqueueJson("""{"data":{"id":10,"chain":"input","action":"accept"}}""", code = 201)

        val rule = client().cloudRouters.createFirewallRule(
            "rtr_abc",
            CreateFirewallRuleRequest(FirewallChain.INPUT, FirewallAction.ACCEPT, source = "198.51.100.1"),
        )

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/cloud-routers/rtr_abc/firewall", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals(setOf("chain", "action", "source"), sent.keys)
        assertEquals("input", sent["chain"]?.jsonPrimitive?.content)
        assertEquals("accept", sent["action"]?.jsonPrimitive?.content)
        assertEquals(FirewallChain.INPUT, rule.chain)
    }

    @Test
    fun `setFirewallRuleDisabled patches disabled`() = test {
        enqueueJson("""{"data":{"id":9,"disabled":false}}""")

        val rule = client().cloudRouters.setFirewallRuleDisabled("rtr_abc", 9, false)

        val request = takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/cloud-routers/rtr_abc/firewall/9", request.apiTarget)
        assertFalse(body(request.bodyText())["disabled"]!!.jsonPrimitive.boolean)
        assertEquals(false, rule.disabled)
    }

    @Test
    fun `deleteFirewallRule uses delete`() = test {
        enqueueJson("""{"data":{"id":9,"state":"deleting"}}""")

        val state = client().cloudRouters.deleteFirewallRule("rtr_abc", 9)

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/cloud-routers/rtr_abc/firewall/9", request.apiTarget)
        assertEquals("deleting", state.state)
    }

    @Test
    fun `listPublicIps decodes created_at`() = test {
        enqueueJson(
            """{"data":[{"id":7,"ip":"2001:db8::7","type":"ipv6","disabled":false,"state":"ready",
                "created_at":"2026-10-11T08:30:00Z"}]}""",
        )

        val ips = client().cloudRouters.listPublicIps("rtr_abc")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/cloud-routers/rtr_abc/public-ips", request.apiTarget)
        assertEquals("2001:db8::7", ips.single().ip)
        assertEquals(Instant.parse("2026-10-11T08:30:00Z"), ips.single().createdAt)
    }

    @Test
    fun `createPublicIp posts empty object`() = test {
        enqueueJson("""{"data":{"id":8,"ip":"2001:db8::8","state":"creating"}}""", code = 201)

        val ip = client().cloudRouters.createPublicIp("rtr_abc")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/cloud-routers/rtr_abc/public-ips", request.apiTarget)
        assertEquals(0, body(request.bodyText()).size)
        assertEquals(8L, ip.id)
        assertEquals(ResourceState.CREATING, ip.state)
    }

    @Test
    fun `setPublicIpDisabled patches disabled`() = test {
        enqueueJson("""{"data":{"id":7,"disabled":true}}""")

        val ip = client().cloudRouters.setPublicIpDisabled("rtr_abc", 7, true)

        val request = takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/cloud-routers/rtr_abc/public-ips/7", request.apiTarget)
        assertEquals(setOf("disabled"), body(request.bodyText()).keys)
        assertEquals(true, ip.disabled)
    }

    @Test
    fun `deletePublicIp uses delete`() = test {
        enqueueJson("""{"data":{"id":7,"state":"deleting"}}""")

        val state = client().cloudRouters.deletePublicIp("rtr_abc", 7)

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/cloud-routers/rtr_abc/public-ips/7", request.apiTarget)
        assertEquals(7L, state.id)
    }
}
