package de.sr.ipv64.api

import de.sr.ipv64.MockServerTest
import de.sr.ipv64.model.Hc2CommandArgs
import de.sr.ipv64.model.Hc2ConflictKeep
import de.sr.ipv64.model.Hc2NodeCommandName
import de.sr.ipv64.model.Hc2NodeCommandRequest
import de.sr.ipv64.model.Hc2RemoteMgmt
import de.sr.ipv64.model.LinkNodeRequest
import de.sr.ipv64.model.NodeFeatures
import de.sr.ipv64.model.NodeHelloRequest
import de.sr.ipv64.model.UpdateNodeRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Instant

class NodesApiTest : MockServerTest() {

    private fun body(text: String): JsonObject = Json.parseToJsonElement(text).jsonObject

    private val nodeJson = """
        {"node_id":"node-1","name":"Raspi","location":"Home","version":"1.4.2","timezone":"Europe/Berlin",
         "modules":["healthchecks","honeypot"],"remote_mgmt":"read","online":true,
         "last_seen":"2026-10-10T12:00:00+02:00","last_sync":"2026-10-10T11:59:00Z","outbox_size":3,
         "conflicts":1,"hub":"https://hub.example","linked_at":"2026-09-01T00:00:00Z"}
    """.trimIndent()

    private fun assertNode(n: de.sr.ipv64.model.Hc2Node) {
        assertEquals("node-1", n.nodeId)
        assertEquals("Raspi", n.name)
        assertEquals(listOf("healthchecks", "honeypot"), n.modules)
        assertEquals(Hc2RemoteMgmt.READ, n.remoteMgmt)
        assertEquals(true, n.online)
        assertEquals(Instant.parse("2026-10-10T10:00:00Z"), n.lastSeen)
        assertEquals(3, n.outboxSize)
        assertEquals(1, n.conflicts)
    }

    @Test
    fun `hello posts body with Node64 header and accepts 204`() = test {
        enqueueNoContent()

        client().nodes.hello(
            NodeHelloRequest(
                nodeId = "node-1",
                name = "Raspi",
                version = "1.4.2",
                modules = listOf("healthchecks"),
                features = NodeFeatures(hcSyncLinked = true, dyndns = false),
            ),
        )

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/node64/hello", request.apiTarget)
        assertEquals("node-1", request.headers["X-Node64-Node"])
        val sent = body(request.bodyText())
        assertEquals(setOf("node_id", "name", "version", "modules", "features"), sent.keys)
        assertEquals("node-1", sent["node_id"]?.jsonPrimitive?.content)
        assertEquals(1, sent["modules"]!!.jsonArray.size)
        val features = sent["features"]!!.jsonObject
        assertEquals(setOf("hc_sync_linked", "dyndns"), features.keys)
        assertEquals(false, features["dyndns"]!!.jsonPrimitive.boolean)
    }

    @Test
    fun `overview returns raw json`() = test {
        enqueueJson("""{"data":[{"node_id":"node-1","online":true}]}""")

        val json = client().nodes.overview()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/node64/nodes", request.apiTarget)
        assertNotNull(json)
        assertEquals(
            "node-1",
            json!!.jsonObject["data"]!!.jsonArray[0].jsonObject["node_id"]?.jsonPrimitive?.content,
        )
    }

    @Test
    fun `overview returns null for empty response`() = test {
        enqueueNoContent()

        assertNull(client().nodes.overview())
    }

    @Test
    fun `list decodes nodes`() = test {
        enqueueJson("""{"data":[$nodeJson]}""")

        val nodes = client().nodes.list()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/hc2/nodes", request.apiTarget)
        assertNode(nodes.single())
    }

    @Test
    fun `link posts code and wire enum`() = test {
        enqueueJson("""{"data":{"accepted":true,"detail":"Node will fetch its token"}}""")

        val result = client().nodes.link(LinkNodeRequest(code = "ABCD-1234", remoteMgmt = Hc2RemoteMgmt.FULL))

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/hc2/nodes/link", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals(setOf("code", "remote_mgmt"), sent.keys)
        assertEquals("full", sent["remote_mgmt"]?.jsonPrimitive?.content)
        assertEquals(true, result.accepted)
        assertEquals("Node will fetch its token", result.detail)
    }

    @Test
    fun `get decodes node with checks`() = test {
        enqueueJson(
            """{"data":${nodeJson.dropLast(1)},"checks":[{"name":"Router","type":"icmp","token":"tok",
                "execution":"local","node_id":"node-1"}]}}""",
        )

        val node = client().nodes.get("node-1")

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/hc2/nodes/node-1", request.apiTarget)
        assertEquals("Raspi", node.name)
        assertEquals("Router", node.checks.single().name)
        assertEquals("node-1", node.checks.single().nodeId)
    }

    @Test
    fun `update patches only set fields`() = test {
        enqueueJson("""{"data":$nodeJson}""")

        val node = client().nodes.update("node-1", UpdateNodeRequest(remoteMgmt = Hc2RemoteMgmt.OFF))

        val request = takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/hc2/nodes/node-1", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals(setOf("remote_mgmt"), sent.keys)
        assertEquals("off", sent["remote_mgmt"]?.jsonPrimitive?.content)
        assertNode(node)
    }

    @Test
    fun `unlink sends keep_checks in delete body and decodes result`() = test {
        enqueueJson("""{"data":{"unlinked":true,"checks_kept_online":2,"checks_deleted":1}}""")

        val result = client().nodes.unlink("node-1", keepChecks = false)

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/hc2/nodes/node-1", request.apiTarget)
        assertFalse(body(request.bodyText())["keep_checks"]!!.jsonPrimitive.boolean)
        assertEquals(true, result.unlinked)
        assertEquals(2, result.checksKeptOnline)
        assertEquals(1, result.checksDeleted)
    }

    @Test
    fun `unlink without keepChecks omits it`() = test {
        enqueueJson("""{"data":{"unlinked":true}}""")

        client().nodes.unlink("node-1")

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals(0, body(request.bodyText()).size)
    }

    @Test
    fun `commands encodes limit and decodes commands`() = test {
        enqueueJson(
            """{"data":[{"command_id":5,"node_id":"node-1","name":"run_check","args":{"token":"tok"},
                "created_at":"2026-10-10T12:00:00+02:00","done_at":"2026-10-10T12:00:05Z","ok":true,
                "output":{"latency":12}}]}""",
        )

        val commands = client().nodes.commands("node-1", limit = 20)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/hc2/nodes/node-1/commands?limit=20", request.apiTarget)
        val cmd = commands.single()
        assertEquals(5L, cmd.commandId)
        assertEquals("run_check", cmd.name)
        assertEquals("tok", cmd.args?.get("token")?.jsonPrimitive?.content)
        assertEquals(Instant.parse("2026-10-10T10:00:00Z"), cmd.createdAt)
        assertEquals(true, cmd.ok)
        assertEquals(12, cmd.output?.jsonObject?.get("latency")?.jsonPrimitive?.int)
    }

    @Test
    fun `commands without limit has no query`() = test {
        enqueueJson("""{"data":[]}""")

        client().nodes.commands("node-1")

        assertEquals("/hc2/nodes/node-1/commands", takeRequest().apiTarget)
    }

    @Test
    fun `sendCommand posts request object`() = test {
        enqueueJson("""{"data":{"command_id":6,"node_id":"node-1","name":"resolve_conflict"}}""", code = 201)

        val cmd = client().nodes.sendCommand(
            "node-1",
            Hc2NodeCommandRequest(
                Hc2NodeCommandName.RESOLVE_CONFLICT.value,
                Hc2CommandArgs(id = 3, keep = Hc2ConflictKeep.SERVER.value),
            ),
        )

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/hc2/nodes/node-1/commands", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals("resolve_conflict", sent["name"]?.jsonPrimitive?.content)
        val args = sent["args"]!!.jsonObject
        assertEquals(setOf("id", "keep"), args.keys)
        assertEquals("server", args["keep"]?.jsonPrimitive?.content)
        assertEquals(6L, cmd.commandId)
    }

    @Test
    fun `sendCommand convenience overload omits null args`() = test {
        enqueueJson("""{"data":{"command_id":7,"name":"pause"}}""")

        client().nodes.sendCommand("node-1", "pause")

        val sent = body(takeRequest().bodyText())
        assertEquals(setOf("name"), sent.keys)
        assertEquals("pause", sent["name"]?.jsonPrimitive?.content)
    }
}
