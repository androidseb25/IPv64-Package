package de.sr.ipv64.api

import de.sr.ipv64.model.CreateIntegrationRequest
import de.sr.ipv64.model.IntegrationStatus
import de.sr.ipv64.model.IntegrationType
import de.sr.ipv64.model.UpdateIntegrationRequest
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class IntegrationsApiTest : ApiSdkTestBase() {

    private val integrationJson = """{"id":5,"type":"telegram","name":"Phone","status":"active","default":true,
        "options":{"chat_id":"123","api_token":"SECRET-BOT-TOKEN"},
        "created_at":"2026-01-01T00:00:00Z","last_used_at":"2026-10-10T10:00:00Z","uses":17}"""

    @Test
    fun `list without include_secrets sends no query`() = test {
        enqueueJson("""{"data":[$integrationJson,
            {"id":0,"type":"account_email","name":"E-Mail","status":"pending_verification","default":false}]}""")

        val list = client().integrations.list()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/integrations", request.apiTarget)
        assertEquals(2, list.size)
        val first = list[0]
        assertEquals(5L, first.id)
        assertEquals(IntegrationType.TELEGRAM, first.type)
        assertEquals(IntegrationStatus.ACTIVE, first.status)
        assertEquals(true, first.default)
        assertEquals(JsonPrimitive("123"), first.options?.get("chat_id"))
        assertEquals(Instant.parse("2026-10-10T10:00:00Z"), first.lastUsedAt)
        assertEquals(17L, first.uses)
        assertEquals(IntegrationType.ACCOUNT_EMAIL, list[1].type)
        assertEquals(IntegrationStatus.PENDING_VERIFICATION, list[1].status)
    }

    @Test
    fun `list encodes include_secrets true and false`() = test {
        enqueueJson("""{"data":[]}""")
        enqueueJson("""{"data":[]}""")

        client().integrations.list(includeSecrets = true)
        client().integrations.list(includeSecrets = false)

        assertEquals("/integrations?include_secrets=true", takeRequest().apiTarget)
        assertEquals("/integrations?include_secrets=false", takeRequest().apiTarget)
    }

    @Test
    fun `create sends wire names and omits nulls`() = test {
        enqueueJson("""{"data":$integrationJson}""", code = 201)

        val created = client().integrations.create(
            CreateIntegrationRequest(
                type = IntegrationType.TELEGRAM,
                name = "Phone",
                default = true,
                chatId = "123",
                apiToken = "SECRET-BOT-TOKEN",
            ),
        )

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/integrations", request.apiTarget)
        val body = request.jsonBody()
        assertEquals(setOf("type", "name", "default", "chat_id", "api_token"), body.keys)
        assertEquals(JsonPrimitive("telegram"), body["type"])
        assertEquals(JsonPrimitive("SECRET-BOT-TOKEN"), body["api_token"])
        assertEquals(5L, created.id)
    }

    @Test
    fun `create webhook uses url_down and url_up`() = test {
        enqueueJson("""{"data":$integrationJson}""", code = 201)

        client().integrations.create(
            CreateIntegrationRequest(IntegrationType.WEBHOOK, "Hook", urlDown = "https://x/d", urlUp = "https://x/u"),
        )

        val body = takeRequest().jsonBody()
        assertEquals(JsonPrimitive("https://x/d"), body["url_down"])
        assertEquals(JsonPrimitive("https://x/u"), body["url_up"])
    }

    @Test
    fun `get sends include_secrets and decodes`() = test {
        enqueueJson("""{"data":$integrationJson}""")

        val integration = client().integrations.get("5", includeSecrets = true)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/integrations/5?include_secrets=true", request.apiTarget)
        assertEquals("Phone", integration.name)
    }

    @Test
    fun `get without include_secrets and with encoded id`() = test {
        enqueueJson("""{"data":$integrationJson}""")

        client().integrations.get("a b/c")

        assertEquals("/integrations/a%20b%2Fc", takeRequest().apiTarget)
    }

    @Test
    fun `update sends only given fields`() = test {
        enqueueJson("""{"data":$integrationJson}""")

        client().integrations.update("5", name = "New")

        val request = takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/integrations/5", request.apiTarget)
        val body = request.jsonBody()
        assertEquals(setOf("name"), body.keys)
        assertEquals(JsonPrimitive("New"), body["name"])
    }

    @Test
    fun `update from request object with default false is sent`() = test {
        enqueueJson("""{"data":$integrationJson}""")

        client().integrations.update("5", UpdateIntegrationRequest(default = false))

        val body = takeRequest().jsonBody()
        assertEquals(setOf("default"), body.keys)
        assertEquals(JsonPrimitive(false), body["default"])
    }

    @Test
    fun `delete requests DELETE and accepts 204`() = test {
        enqueueNoContent()

        client().integrations.delete("5")

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/integrations/5", request.apiTarget)
    }

    @Test
    fun `test posts to test endpoint`() = test {
        enqueueJson("""{"data":{"sent":true}}""")

        val result = client().integrations.test("0")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/integrations/0/test", request.apiTarget)
        assertEquals(true, result.sent)
    }

    @Test
    fun `toString redacts options and secret request fields`() = test {
        enqueueJson("""{"data":$integrationJson}""")

        val integration = client().integrations.get("5", includeSecrets = true)
        val request = CreateIntegrationRequest(
            type = IntegrationType.PUSHOVER,
            name = "P",
            appToken = "SECRET-APP",
            userToken = "SECRET-USER",
            webhookUrl = "https://discord/SECRET-HOOK",
            deviceToken = "SECRET-DEVICE",
        )

        val text = integration.toString()
        assertFalse(text, text.contains("SECRET-BOT-TOKEN"))
        assertTrue(text, text.contains("options=<redacted>"))
        assertTrue(text, text.contains("name=Phone"))
        val requestText = request.toString()
        for (secret in listOf("SECRET-APP", "SECRET-USER", "SECRET-HOOK", "SECRET-DEVICE")) {
            assertFalse(requestText, requestText.contains(secret))
        }
        assertTrue(requestText, requestText.contains("name=P"))
    }
}
