package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.ChannelExecution
import de.sr.ipv64.node64.model.ChannelType
import de.sr.ipv64.node64.model.CreateChannelRequest
import de.sr.ipv64.node64.model.UpdateChannelRequest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class ChannelsApiTest : Node64ApiTestBaseA() {

    private val channelJson = """{"id":5,"type":"telegram","name":"Alerts","config":{"token":"BOT-SECRET","chat_id":"123"},
        "enabled":true,"default":false,"lang":"de","notify_down":true,"notify_up":false,"execution":"node","revision":3,
        "last_used_at":"","last_error":"","created_at":"2026-09-01T10:00:00Z","updated_at":"2026-10-01T10:00:00+02:00"}"""

    @Test
    fun `list without include_secrets has no query`() = test {
        enqueueJson("[$channelJson]")

        val channels = node().channels.list()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/channels", request.target)
        val channel = channels.single()
        assertEquals(5L, channel.id)
        assertEquals(ChannelType.TELEGRAM, channel.type)
        assertEquals("Alerts", channel.name)
        assertEquals(JsonPrimitive("123"), channel.config!!["chat_id"])
        assertEquals(true, channel.enabled)
        assertEquals(false, channel.default)
        assertEquals("de", channel.lang)
        assertEquals(true, channel.notifyDown)
        assertEquals(false, channel.notifyUp)
        assertEquals(ChannelExecution.NODE, channel.execution)
        assertEquals(3L, channel.revision)
        assertNull(channel.lastUsedAt)
        assertEquals(Instant.parse("2026-09-01T10:00:00Z"), channel.createdAt)
        assertEquals(Instant.parse("2026-10-01T08:00:00Z"), channel.updatedAt)
    }

    @Test
    fun `list sends include_secrets as true and false`() = test {
        enqueueJson("[]")
        enqueueJson("[]")

        node().channels.list(includeSecrets = true)
        node().channels.list(includeSecrets = false)

        assertEquals("/api/channels?include_secrets=true", takeRequest().target)
        assertEquals("/api/channels?include_secrets=false", takeRequest().target)
    }

    @Test
    fun `channel toString redacts config`() = test {
        enqueueJson("[$channelJson]")

        val channel = node().channels.list().single()

        assertFalse(channel.toString().contains("BOT-SECRET"))
        assertTrue(channel.toString().contains("config=<redacted>"))
    }

    @Test
    fun `create posts typed request and omits nulls`() = test {
        enqueueJson(channelJson, code = 201)
        val config = buildJsonObject {
            put("token", JsonPrimitive("BOT-SECRET"))
            put("chat_id", JsonPrimitive("123"))
        }

        val channel = node().channels.create(
            CreateChannelRequest(
                type = ChannelType.TELEGRAM,
                name = "Alerts",
                config = config,
                notifyDown = true,
                execution = ChannelExecution.SERVER,
            ),
        )

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/channels", request.target)
        request.assertJsonBody(
            """{"type":"telegram","name":"Alerts","config":{"token":"BOT-SECRET","chat_id":"123"},
            "notify_down":true,"execution":"server"}""",
        )
        assertEquals(5L, channel.id)
    }

    @Test
    fun `create request toString redacts config`() {
        val request = CreateChannelRequest(
            ChannelType.WEBHOOK,
            "hook",
            buildJsonObject { put("url", JsonPrimitive("https://secret.example/hook")) },
        )

        assertFalse(request.toString().contains("secret.example"))
    }

    @Test
    fun `get fetches a single channel`() = test {
        enqueueJson(channelJson)

        val channel = node().channels.get(5)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/channels/5", request.target)
        assertEquals("Alerts", channel.name)
    }

    @Test
    fun `update puts only given fields`() = test {
        enqueueJson(channelJson)

        val channel = node().channels.update(5, UpdateChannelRequest(name = "Renamed", enabled = false, default = true))

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/channels/5", request.target)
        request.assertJsonBody("""{"name":"Renamed","enabled":false,"default":true}""")
        assertEquals(5L, channel.id)
    }

    @Test
    fun `update request toString redacts config`() {
        val request = UpdateChannelRequest(config = buildJsonObject { put("password", JsonPrimitive("hunter2")) })

        assertFalse(request.toString().contains("hunter2"))
    }

    @Test
    fun `delete sends DELETE`() = test {
        enqueueNoContent()

        node().channels.delete(5)

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/channels/5", request.target)
    }

    @Test
    fun `test posts to test endpoint and decodes ok`() = test {
        enqueueJson("""{"ok":true}""")

        val result = node().channels.test(5)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/channels/5/test", request.target)
        assertEquals(true, result.ok)
    }

    @Test
    fun `channelTypes returns undocumented json and null for empty body`() = test {
        enqueueJson("""[{"type":"smtp","fields":[{"name":"host","secret":false}]}]""")
        enqueueEmpty()

        val types = node().channels.channelTypes()
        val none = node().channels.channelTypes()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/channel-types", request.target)
        assertEquals("smtp", types!!.jsonArray[0].jsonObject["type"]!!.let { (it as JsonPrimitive).content })
        assertNull(none)
    }

    @Test
    fun `deliveries without filters has no query and decodes log`() = test {
        enqueueJson(
            """[{"id":100,"time":"2026-10-28T09:30:00Z","event_id":"ev-1","channel_id":5,"channel":"Alerts",
            "kind":"down","check_token":"tok1","subject":"Web is down","ok":false,"error":"timeout"}]""",
        )

        val deliveries = node().channels.deliveries()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/deliveries", request.target)
        val delivery = deliveries.single()
        assertEquals(100L, delivery.id)
        assertEquals(Instant.parse("2026-10-28T09:30:00Z"), delivery.time)
        assertEquals("ev-1", delivery.eventId)
        assertEquals(5L, delivery.channelId)
        assertEquals("down", delivery.kind)
        assertEquals("tok1", delivery.checkToken)
        assertEquals(false, delivery.ok)
        assertEquals("timeout", delivery.error)
    }

    @Test
    fun `deliveries sends limit before and channel`() = test {
        enqueueJson("[]")

        node().channels.deliveries(limit = 50, before = 200, channel = 5)

        assertEquals("/api/deliveries?limit=50&before=200&channel=5", takeRequest().target)
    }
}
