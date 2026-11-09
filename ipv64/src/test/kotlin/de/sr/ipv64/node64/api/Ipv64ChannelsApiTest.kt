package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.CreateIpv64ChannelRequest
import de.sr.ipv64.node64.model.Ipv64ChannelType
import de.sr.ipv64.node64.model.UpdateIpv64ChannelRequest
import de.sr.ipv64.node64.model.VerifyIpv64ChannelRequest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Ipv64ChannelsApiTest : Node64ApiTestBaseA() {

    private val channelJson = """{"id":7,"type":"ipv64_mail","name":"Mail","enabled":true,"default":false,
        "verified":false,"lang":"de","notify_down":true,"notify_up":false,"config":{"email":"a@b.de"},"last_error":""}"""

    @Test
    fun `list gets channels`() = test {
        enqueueJson("[$channelJson]")
        val c = node().ipv64Channels.list().single()
        val r = takeRequest()
        assertEquals("GET", r.method)
        assertEquals("/api/ipv64/channels", r.target)
        assertEquals(7L, c.id)
        assertEquals(Ipv64ChannelType.IPV64_MAIL, c.type)
        assertEquals(false, c.verified)
        assertEquals(JsonPrimitive("a@b.de"), c.config!!["email"])
    }

    @Test
    fun `create posts body`() = test {
        enqueueJson(channelJson)
        node().ipv64Channels.create(
            CreateIpv64ChannelRequest(
                Ipv64ChannelType.IPV64_MAIL, "Mail",
                config = buildJsonObject { put("email", JsonPrimitive("a@b.de")) },
                notifyDown = true,
            ),
        )
        val r = takeRequest()
        assertEquals("POST", r.method)
        assertEquals("/api/ipv64/channels", r.target)
        r.assertJsonBody("""{"type":"ipv64_mail","name":"Mail","config":{"email":"a@b.de"},"notify_down":true}""")
    }

    @Test
    fun `get update delete`() = test {
        enqueueJson(channelJson)
        enqueueJson(channelJson)
        enqueueEmpty(204)

        assertEquals(7L, node().ipv64Channels.get(7).id)
        node().ipv64Channels.update(7, UpdateIpv64ChannelRequest(enabled = false))
        node().ipv64Channels.delete(7)

        val get = takeRequest()
        assertEquals("GET", get.method)
        assertEquals("/api/ipv64/channels/7", get.target)
        val put = takeRequest()
        assertEquals("PUT", put.method)
        assertEquals("/api/ipv64/channels/7", put.target)
        put.assertJsonBody("""{"enabled":false}""")
        val del = takeRequest()
        assertEquals("DELETE", del.method)
        assertEquals("/api/ipv64/channels/7", del.target)
    }

    @Test
    fun `test posts and decodes ok`() = test {
        enqueueJson("""{"ok":true}""")
        assertEquals(true, node().ipv64Channels.test(7).ok)
        val r = takeRequest()
        assertEquals("POST", r.method)
        assertEquals("/api/ipv64/channels/7/test", r.target)
    }

    @Test
    fun `sendVerification posts`() = test {
        enqueueEmpty(202)
        node().ipv64Channels.sendVerification(7)
        val r = takeRequest()
        assertEquals("POST", r.method)
        assertEquals("/api/ipv64/channels/7/verify/send", r.target)
    }

    @Test
    fun `verify posts code and redacts it`() = test {
        enqueueJson(channelJson)
        val request = VerifyIpv64ChannelRequest("123456")
        node().ipv64Channels.verify(7, request)
        val r = takeRequest()
        assertEquals("POST", r.method)
        assertEquals("/api/ipv64/channels/7/verify", r.target)
        r.assertJsonBody("""{"code":"123456"}""")
        assertFalse(request.toString().contains("123456"))
        assertTrue(request.toString().contains("<redacted>"))
    }
}
