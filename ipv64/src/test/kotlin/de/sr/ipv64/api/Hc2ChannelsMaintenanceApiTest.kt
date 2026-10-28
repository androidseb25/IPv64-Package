package de.sr.ipv64.api

import de.sr.ipv64.MockServerTest
import de.sr.ipv64.model.Hc2ChannelInput
import de.sr.ipv64.model.Hc2ChannelUpdate
import de.sr.ipv64.model.Hc2MaintenanceInput
import de.sr.ipv64.model.Hc2MaintenanceRepeat
import de.sr.ipv64.model.Hc2MaintenanceUpdate
import de.sr.ipv64.model.Hc2SendMessageRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class Hc2ChannelsMaintenanceApiTest : MockServerTest() {

    private fun body(text: String): JsonObject = Json.parseToJsonElement(text).jsonObject

    private val channelJson = """
        {"id":12,"type":"telegram","type_label":"Telegram","name":"Ops",
         "config":{"chat_id":"42","bot_token":"********"},"secrets_set":["bot_token"],
         "enabled":true,"default":false,"verified":true,"lang":"de","notify_down":true,"notify_up":false,
         "execution":"online","node_id":null,"last_used":"2026-10-10T12:00:00+02:00","last_error":null,
         "revision":3,"created_at":"2026-09-01T00:00:00Z","sendable_via_api":false}
    """.trimIndent()

    private fun assertChannel(c: de.sr.ipv64.model.Hc2Channel) {
        assertEquals(12L, c.id)
        assertEquals("telegram", c.type)
        assertEquals("Telegram", c.typeLabel)
        assertEquals("42", c.config?.get("chat_id")?.jsonPrimitive?.content)
        assertEquals(listOf("bot_token"), c.secretsSet)
        assertEquals(true, c.enabled)
        assertEquals(false, c.notifyUp)
        assertEquals(Instant.parse("2026-10-10T10:00:00Z"), c.lastUsed)
        assertNull(c.lastError)
        assertEquals(false, c.sendableViaApi)
    }

    // ---- channels ----

    @Test
    fun `channels list`() = test {
        enqueueJson("""{"data":[$channelJson]}""")

        val channels = client().hc2.channels.list()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/hc2/channels", request.apiTarget)
        assertChannel(channels.single())
    }

    @Test
    fun `channels create sends config and omits null fields`() = test {
        enqueueJson("""{"data":$channelJson}""", code = 201)

        val channel = client().hc2.channels.create(
            Hc2ChannelInput(
                name = "Ops",
                type = "telegram",
                config = buildJsonObject {
                    put("chat_id", JsonPrimitive("42"))
                    put("bot_token", JsonPrimitive("CFGSECRET"))
                },
                notifyDown = true,
            ),
        )

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/hc2/channels", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals(setOf("name", "type", "config", "notify_down"), sent.keys)
        assertEquals("CFGSECRET", sent["config"]!!.jsonObject["bot_token"]?.jsonPrimitive?.content)
        assertChannel(channel)
    }

    @Test
    fun `channels get`() = test {
        enqueueJson("""{"data":$channelJson}""")

        assertChannel(client().hc2.channels.get(12))

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/hc2/channels/12", request.apiTarget)
    }

    @Test
    fun `channels update patches only set properties`() = test {
        enqueueJson("""{"data":$channelJson}""")

        client().hc2.channels.update(12, Hc2ChannelUpdate(enabled = false, lang = "en"))

        val request = takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/hc2/channels/12", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals(setOf("enabled", "lang"), sent.keys)
        assertFalse(sent["enabled"]!!.jsonPrimitive.boolean)
    }

    @Test
    fun `channels delete accepts 204`() = test {
        enqueueNoContent()

        client().hc2.channels.delete(12)

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/hc2/channels/12", request.apiTarget)
    }

    @Test
    fun `channels test decodes result`() = test {
        enqueueJson("""{"data":{"sent":false,"queued_at_node":true}}""")

        val result = client().hc2.channels.test(12)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/hc2/channels/12/test", request.apiTarget)
        assertEquals(false, result.sent)
        assertEquals(true, result.queuedAtNode)
    }

    @Test
    fun `channels send posts message and decodes result`() = test {
        enqueueJson("""{"data":{"sent":true,"duplicate":false,"sms_left":9}}""")

        val result = client().hc2.channels.send(
            12,
            Hc2SendMessageRequest(subject = "Disk full", text = "95%", eventId = "evt-1"),
        )

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/hc2/channels/12/send", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals(setOf("subject", "text", "event_id"), sent.keys)
        assertEquals("evt-1", sent["event_id"]?.jsonPrimitive?.content)
        assertEquals(true, result.sent)
        assertEquals(false, result.duplicate)
        assertEquals(9, result.smsLeft)
    }

    @Test
    fun `channels sendVerificationCode posts and accepts 204`() = test {
        enqueueNoContent()

        client().hc2.channels.sendVerificationCode(12)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/hc2/channels/12/verify/send", request.apiTarget)
    }

    @Test
    fun `channels verify posts code and accepts empty 200`() = test {
        enqueueJson("")

        client().hc2.channels.verify(12, "123456")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/hc2/channels/12/verify", request.apiTarget)
        assertEquals("123456", body(request.bodyText())["code"]?.jsonPrimitive?.content)
    }

    @Test
    fun `channel config is redacted in toString`() {
        val config = buildJsonObject { put("bot_token", JsonPrimitive("CFGSECRET")) }
        assertFalse(Hc2ChannelInput(name = "x", config = config).toString().contains("CFGSECRET"))
        assertFalse(Hc2ChannelUpdate(config = config).toString().contains("CFGSECRET"))
    }

    // ---- maintenance ----

    private val maintenanceJson = """
        {"id":4,"name":"Patch night","start_at":"2026-11-01T22:00:00+01:00","end_at":"2026-11-02T02:00:00+01:00",
         "repeat":"weekly","repeat_until":"2027-01-01T00:00:00Z","tokens":["t1","t2"],"active":true,
         "node_id":null,"revision":1}
    """.trimIndent()

    private fun assertMaintenance(m: de.sr.ipv64.model.Hc2Maintenance) {
        assertEquals(4L, m.id)
        assertEquals("Patch night", m.name)
        assertEquals(Instant.parse("2026-11-01T21:00:00Z"), m.startAt)
        assertEquals(Instant.parse("2026-11-02T01:00:00Z"), m.endAt)
        assertEquals(Hc2MaintenanceRepeat.WEEKLY, m.repeat)
        assertEquals(Instant.parse("2027-01-01T00:00:00Z"), m.repeatUntil)
        assertEquals(listOf("t1", "t2"), m.tokens)
        assertEquals(true, m.active)
    }

    @Test
    fun `maintenance list`() = test {
        enqueueJson("""{"data":[$maintenanceJson]}""")

        val list = client().hc2.maintenance.list()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/hc2/maintenance", request.apiTarget)
        assertMaintenance(list.single())
    }

    @Test
    fun `maintenance create sends ISO instants`() = test {
        enqueueJson("""{"data":$maintenanceJson}""", code = 201)

        val m = client().hc2.maintenance.create(
            Hc2MaintenanceInput(
                name = "Patch night",
                startAt = Instant.parse("2026-11-01T21:00:00Z"),
                endAt = Instant.parse("2026-11-02T01:00:00Z"),
                repeat = Hc2MaintenanceRepeat.WEEKLY,
                tokens = listOf("t1", "t2"),
            ),
        )

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/hc2/maintenance", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals(setOf("name", "start_at", "end_at", "repeat", "tokens"), sent.keys)
        assertEquals("2026-11-01T21:00:00Z", sent["start_at"]?.jsonPrimitive?.content)
        assertEquals("weekly", sent["repeat"]?.jsonPrimitive?.content)
        assertEquals(2, sent["tokens"]!!.jsonArray.size)
        assertMaintenance(m)
    }

    @Test
    fun `maintenance get`() = test {
        enqueueJson("""{"data":$maintenanceJson}""")

        assertMaintenance(client().hc2.maintenance.get(4))

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/hc2/maintenance/4", request.apiTarget)
    }

    @Test
    fun `maintenance update patches only set properties`() = test {
        enqueueJson("""{"data":$maintenanceJson}""")

        client().hc2.maintenance.update(4, Hc2MaintenanceUpdate(active = false))

        val request = takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/hc2/maintenance/4", request.apiTarget)
        val sent = body(request.bodyText())
        assertEquals(setOf("active"), sent.keys)
        assertTrue(!sent["active"]!!.jsonPrimitive.boolean)
    }

    @Test
    fun `maintenance delete accepts 204`() = test {
        enqueueNoContent()

        client().hc2.maintenance.delete(4)

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/hc2/maintenance/4", request.apiTarget)
    }
}
