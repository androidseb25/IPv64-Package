package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.ConflictResolution
import de.sr.ipv64.node64.model.LinkWithApiKeyRequest
import de.sr.ipv64.node64.model.RemoteMgmt
import de.sr.ipv64.node64.model.SetIpv64KeyRequest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class LinkApiTest : Node64ApiTestBaseA() {

    private val linkJson = """{"linked":true,"connected":true,"account_id":"acc-1","plan":"pro","node_name":"Home",
        "location":"Berlin","remote_mgmt":"full","hub":"hub.ipv64.net","last_sync_at":"2026-10-28T09:00:00Z",
        "connected_at":"","linked_at":"2026-09-01 12:00:00","outbox":2,"conflicts":1,"last_error":"",
        "pending_code":"PENDING-SECRET","requesting":false,"online_limit":5,"checks_today":3,"checks_limit":100,
        "locations_per_check":4,"local_notifications":true}"""

    @Test
    fun `get decodes link with lenient timestamps`() = test {
        enqueueJson(linkJson)

        val link = node().link.get()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/link", request.target)
        assertEquals(true, link.linked)
        assertEquals("acc-1", link.accountId)
        assertEquals(RemoteMgmt.FULL, link.remoteMgmt)
        assertEquals(Instant.parse("2026-10-28T09:00:00Z"), link.lastSyncAt)
        assertNull(link.connectedAt)
        assertEquals(Instant.parse("2026-09-01T12:00:00Z"), link.linkedAt)
        assertEquals(2, link.outbox)
        assertEquals(1, link.conflicts)
        assertEquals(5, link.onlineLimit)
        assertEquals(100, link.checksLimit)
        assertEquals(4, link.locationsPerCheck)
        assertEquals(true, link.localNotifications)
        assertEquals("PENDING-SECRET", link.pendingCode)
    }

    @Test
    fun `link toString redacts the pending code`() = test {
        enqueueJson(linkJson)

        val link = node().link.get()

        assertFalse(link.toString().contains("PENDING-SECRET"))
        assertTrue(link.toString().contains("pendingCode=<redacted>"))
    }

    @Test
    fun `linkWithApiKey with defaults posts empty object`() = test {
        enqueueJson(linkJson)

        val link = node().link.linkWithApiKey()

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/link/apikey", request.target)
        request.assertJsonBody("{}")
        assertEquals("hub.ipv64.net", link.hub)
    }

    @Test
    fun `linkWithApiKey sends location and remote_mgmt and accepts 202`() = test {
        enqueueJson("""{"linked":false,"requesting":true,"pending_code":"X"}""", code = 202)

        val link = node().link.linkWithApiKey(LinkWithApiKeyRequest("Berlin", RemoteMgmt.READ))

        takeRequest().assertJsonBody("""{"location":"Berlin","remote_mgmt":"read"}""")
        assertEquals(true, link.requesting)
    }

    @Test
    fun `unlink without keepChecks posts empty object`() = test {
        enqueueNoContent()

        node().link.unlink()

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/link/unlink", request.target)
        request.assertJsonBody("{}")
    }

    @Test
    fun `unlink sends keep_checks`() = test {
        enqueueNoContent()

        node().link.unlink(keepChecks = false)

        takeRequest().assertJsonBody("""{"keep_checks":false}""")
    }

    @Test
    fun `conflicts decodes local and server documents and redacts toString`() = test {
        enqueueJson(
            """[{"id":9,"entity":"check","entity_id":"tok1","name":"Web","local_op":"update",
            "local":{"name":"LOCAL-SECRET"},"server":{"name":"SERVER-SECRET"},"server_revision":12,
            "created_at":"2026-10-28T08:00:00Z"}]""",
        )

        val conflicts = node().link.conflicts()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/link/conflicts", request.target)
        val conflict = conflicts.single()
        assertEquals(9L, conflict.id)
        assertEquals("check", conflict.entity)
        assertEquals("tok1", conflict.entityId)
        assertEquals("update", conflict.localOp)
        assertEquals(JsonPrimitive("LOCAL-SECRET"), conflict.local!!.jsonObject["name"])
        assertEquals(JsonPrimitive("SERVER-SECRET"), conflict.server!!.jsonObject["name"])
        assertEquals(12L, conflict.serverRevision)
        assertEquals(Instant.parse("2026-10-28T08:00:00Z"), conflict.createdAt)
        assertFalse(conflict.toString().contains("LOCAL-SECRET"))
        assertFalse(conflict.toString().contains("SERVER-SECRET"))
    }

    @Test
    fun `resolveConflict posts keep choice`() = test {
        enqueueNoContent()

        node().link.resolveConflict(9, ConflictResolution.SERVER)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/link/conflicts/9", request.target)
        request.assertJsonBody("""{"keep":"server"}""")
    }

    @Test
    fun `resolveConflict can keep local`() = test {
        enqueueNoContent()

        node().link.resolveConflict(10, ConflictResolution.LOCAL)

        takeRequest().assertJsonBody("""{"keep":"local"}""")
    }

    @Test
    fun `updateSettings puts local_notifications`() = test {
        enqueueJson("""{"local_notifications":false}""")

        val settings = node().link.updateSettings(false)

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/link/settings", request.target)
        request.assertJsonBody("""{"local_notifications":false}""")
        assertEquals(false, settings.localNotifications)
    }

    @Test
    fun `ipv64Key decodes key status`() = test {
        enqueueJson(
            """{"key_stored":true,"account":"user@example.com","account_id":4711,"plan":"pro","key_name":"node",
            "checked_at":"2026-10-28T09:00:00Z","expires_at":"2027-01-01T00:00:00Z","days_left":65}""",
        )

        val status = node().link.ipv64Key()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/ipv64", request.target)
        assertEquals(true, status.keyStored)
        assertEquals(4711L, status.accountId)
        assertEquals("node", status.keyName)
        assertEquals(Instant.parse("2027-01-01T00:00:00Z"), status.expiresAt)
        assertEquals(65, status.daysLeft)
    }

    @Test
    fun `setIpv64Key puts key and redacts request toString`() = test {
        enqueueJson("""{"key_stored":true,"account":"user@example.com"}""")

        val status = node().link.setIpv64Key("IPV64-SECRET-KEY")

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/ipv64/key", request.target)
        request.assertJsonBody("""{"key":"IPV64-SECRET-KEY"}""")
        assertEquals("user@example.com", status.account)
        assertFalse(SetIpv64KeyRequest("IPV64-SECRET-KEY").toString().contains("IPV64-SECRET-KEY"))
    }

    @Test
    fun `deleteIpv64Key sends DELETE`() = test {
        enqueueNoContent()

        node().link.deleteIpv64Key()

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/ipv64/key", request.target)
    }
}
