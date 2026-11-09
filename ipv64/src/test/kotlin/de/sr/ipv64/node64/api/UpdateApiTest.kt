package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.UpdateChannel
import de.sr.ipv64.node64.model.UpdateMode
import de.sr.ipv64.node64.model.UpdateSettingsRequest
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateApiTest : Node64ApiTestBaseA() {

    @Test
    fun `get returns free-form object`() = test {
        enqueueJson("""{"Version":"1.0"}""")

        val r = node().update.get()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/update", request.target)
        assertEquals("1.0", r["Version"]?.jsonPrimitive?.content)
    }

    @Test
    fun `check posts`() = test {
        enqueueJson("""{"Latest":{}}""")

        val r = node().update.check()

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/update/check", request.target)
        assertEquals(true, r["Latest"]?.jsonObject != null)
    }

    @Test
    fun `apply decodes result`() = test {
        enqueueJson("""{"status":"restarting","version":"1.1"}""", code = 202)

        val r = node().update.apply()

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/update/apply", request.target)
        assertEquals("restarting", r.status)
        assertEquals("1.1", r.version)
    }

    @Test
    fun `reset decodes result`() = test {
        enqueueJson("""{"status":"restarting"}""", code = 202)

        val r = node().update.reset()

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/update/reset", request.target)
        assertEquals("restarting", r.status)
    }

    @Test
    fun `updateSettings puts request`() = test {
        enqueueJson("""{"Mode":"auto"}""")

        node().update.updateSettings(UpdateSettingsRequest(mode = UpdateMode.AUTO, channel = UpdateChannel.AUTO, window = "03:00-05:00"))

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/update/settings", request.target)
        request.assertJsonBody("""{"mode":"auto","channel":"auto","window":"03:00-05:00"}""")
    }
}
