package de.sr.ipv64.api

import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class ApiKeysApiTest : ApiSdkTestBase() {

    @Test
    fun `list requests GET api-keys and decodes keys`() = test {
        enqueueJson(
            """{"data":[{"id":1,"name":"cli","token_hint":"abcd…wxyz","created_at":"2026-01-01T00:00:00Z",
            "expires_at":null,"expired":false,"uses":12},
            {"id":2,"name":"old","token_hint":"1234…6789","created_at":"2025-01-01T00:00:00Z",
            "expires_at":"2025-06-01T00:00:00Z","expired":true,"uses":3}]}""",
        )

        val keys = client().apiKeys.list()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api-keys", request.apiTarget)
        assertEquals(2, keys.size)
        assertEquals("cli", keys[0].name)
        assertEquals("abcd…wxyz", keys[0].tokenHint)
        assertNull(keys[0].expiresAt)
        assertEquals(12L, keys[0].uses)
        assertTrue(keys[1].expired == true)
        assertEquals(Instant.parse("2025-06-01T00:00:00Z"), keys[1].expiresAt)
    }

    @Test
    fun `create sends name and expires_in and decodes the secret token`() = test {
        enqueueJson(
            """{"data":{"id":3,"name":"ci","token_hint":"abcd…wxyz","created_at":"2026-10-10T10:00:00Z",
            "expires_at":"2027-10-10T10:00:00Z","expired":false,"uses":0,"token":"SECRET-FULL-KEY"}}""",
            code = 201,
        )

        val created = client().apiKeys.create(name = "ci", expiresIn = 31536000)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api-keys", request.apiTarget)
        val body = request.jsonBody()
        assertEquals(JsonPrimitive("ci"), body["name"])
        assertEquals(JsonPrimitive(31536000), body["expires_in"])
        assertEquals(2, body.size)
        assertEquals(3L, created.id)
        assertEquals("SECRET-FULL-KEY", created.token)
        assertEquals(Instant.parse("2027-10-10T10:00:00Z"), created.expiresAt)
        assertFalse(created.expired == true)
    }

    @Test
    fun `create without arguments sends no null fields`() = test {
        enqueueJson("""{"data":{"id":4,"token":"K"}}""", code = 201)

        client().apiKeys.create()

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals(0, request.jsonBody().size)
    }

    @Test
    fun `delete requests DELETE api-keys id and accepts 204`() = test {
        enqueueNoContent()

        client().apiKeys.delete("5")

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api-keys/5", request.apiTarget)
    }

    @Test
    fun `delete encodes special characters in the path segment`() = test {
        enqueueNoContent()

        client().apiKeys.delete("a b/c")

        assertEquals("/api-keys/a%20b%2Fc", takeRequest().apiTarget)
    }

    @Test
    fun `ApiKeyCreated toString redacts the token`() = test {
        enqueueJson("""{"data":{"id":3,"name":"ci","token":"SECRET-FULL-KEY"}}""", code = 201)

        val text = client().apiKeys.create(name = "ci").toString()

        assertFalse(text.contains("SECRET-FULL-KEY"))
        assertTrue(text.contains("<redacted>"))
        assertTrue(text.contains("name=ci"))
    }
}
