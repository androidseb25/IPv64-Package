package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.ClusterGroupCode
import de.sr.ipv64.node64.model.ClusterMemberState
import de.sr.ipv64.node64.model.JoinGroupRequest
import de.sr.ipv64.node64.model.SetGroupSyncRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class ClusterApiTest : Node64ApiTestBaseA() {

    private val groupJson = """{"account_id":77,"linked":true,"forwarder":"n-1","forwarder_self":true,
        "code":"PAIR-SECRET-123","code_expires":"2026-10-28T10:10:00Z","https_port":8443,
        "members":[
          {"node_id":"n-1","name":"Home","url":"https://home.example","online":true,"linked":true,"version":"1.4.2",
           "last_seen":"2026-10-28T10:00:00Z","added_at":"2026-09-01T00:00:00Z","state":"active","sync":["checks","dns64"]},
          {"node_id":"n-2","name":"Office","url":"https://office.example","online":false,"last_error":"timeout",
           "state":"suspended","reason":"version mismatch","sync":[]}],
        "sync_modules":[{"id":"checks","title_key":"mod.checks","available":true,"enabled":true,
           "items":[{"id":"channels","title_key":"item.channels","enabled":false}]}]}"""

    @Test
    fun `group decodes members sync modules and instants`() = test {
        enqueueJson(groupJson)

        val group = node().cluster.group()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/group", request.target)
        assertEquals(77L, group.accountId)
        assertEquals(true, group.forwarderSelf)
        assertEquals("n-1", group.forwarder)
        assertEquals(8443, group.httpsPort)
        assertEquals(Instant.parse("2026-10-28T10:10:00Z"), group.codeExpires)
        assertEquals(2, group.members.size)
        val home = group.members[0]
        assertEquals("n-1", home.nodeId)
        assertEquals(ClusterMemberState.ACTIVE, home.state)
        assertEquals(Instant.parse("2026-10-28T10:00:00Z"), home.lastSeen)
        assertEquals(listOf("checks", "dns64"), home.sync)
        val office = group.members[1]
        assertEquals(ClusterMemberState.SUSPENDED, office.state)
        assertEquals("version mismatch", office.reason)
        assertEquals("timeout", office.lastError)
        assertNull(office.addedAt)
        val module = group.syncModules.single()
        assertEquals("mod.checks", module.titleKey)
        assertEquals(false, module.items.single().enabled)
    }

    @Test
    fun `group toString hides the pairing code`() = test {
        enqueueJson(groupJson)

        val group = node().cluster.group()

        assertFalse(group.toString().contains("PAIR-SECRET-123"))
    }

    @Test
    fun `createCode posts without body and redacts toString`() = test {
        enqueueJson("""{"code":"ABCD-EFGH","expires":"2026-10-28T10:10:00Z"}""", code = 201)

        val code: ClusterGroupCode = node().cluster.createCode()

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/group/code", request.target)
        assertEquals("ABCD-EFGH", code.code)
        assertEquals(Instant.parse("2026-10-28T10:10:00Z"), code.expires)
        assertFalse(code.toString().contains("ABCD-EFGH"))
    }

    @Test
    fun `discardCode sends DELETE`() = test {
        enqueueNoContent()

        node().cluster.discardCode()

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/group/code", request.target)
    }

    @Test
    fun `join posts request and decodes the group`() = test {
        enqueueJson(groupJson)

        val group = node().cluster.join(JoinGroupRequest(addr = "https://other.example", code = "CODE-1", selfUrl = "https://me.example"))

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/group/join", request.target)
        request.assertJsonBody("""{"addr":"https://other.example","code":"CODE-1","self_url":"https://me.example"}""")
        assertEquals(2, group.members.size)
    }

    @Test
    fun `join omits self_url when null and redacts code in toString`() = test {
        enqueueJson("""{"linked":false}""")
        val join = JoinGroupRequest("https://other.example", "CODE-SECRET")

        node().cluster.join(join)

        takeRequest().assertJsonBody("""{"addr":"https://other.example","code":"CODE-SECRET"}""")
        assertFalse(join.toString().contains("CODE-SECRET"))
        assertTrue(join.toString().contains("https://other.example"))
    }

    @Test
    fun `leave posts`() = test {
        enqueueNoContent()

        node().cluster.leave()

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/group/leave", request.target)
    }

    @Test
    fun `updateMember puts url`() = test {
        enqueueNoContent()

        node().cluster.updateMember("n-2", "https://new.example")

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/group/members/n-2", request.target)
        request.assertJsonBody("""{"url":"https://new.example"}""")
    }

    @Test
    fun `removeMember sends DELETE`() = test {
        enqueueNoContent()

        node().cluster.removeMember("n-2")

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/group/members/n-2", request.target)
    }

    @Test
    fun `resumeMember posts to resume`() = test {
        enqueueNoContent()

        node().cluster.resumeMember("n-2")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/group/members/n-2/resume", request.target)
    }

    @Test
    fun `member id segment is percent encoded`() = test {
        enqueueNoContent()

        node().cluster.removeMember("a/b c")

        assertEquals("/api/group/members/a%2Fb%20c", takeRequest().target)
    }

    @Test
    fun `setSync puts module item and enabled`() = test {
        enqueueJson(
            """{"sync_modules":[{"id":"checks","title_key":"mod.checks","available":true,"enabled":true,
            "items":[{"id":"channels","title_key":"item.channels","enabled":true}]}]}""",
        )

        val result = node().cluster.setSync(SetGroupSyncRequest(module = "checks", item = "channels", enabled = true))

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/group/sync", request.target)
        request.assertJsonBody("""{"module":"checks","item":"channels","enabled":true}""")
        assertEquals(true, result.syncModules.single().items.single().enabled)
    }

    @Test
    fun `setSync without item omits it but keeps enabled=false`() = test {
        enqueueJson("""{"sync_modules":[]}""")

        node().cluster.setSync(SetGroupSyncRequest(module = "checks", enabled = false))

        takeRequest().assertJsonBody("""{"module":"checks","enabled":false}""")
    }
}
