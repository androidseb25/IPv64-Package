package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.UpdateMeRequest
import de.sr.ipv64.node64.model.UserLang
import org.junit.Assert.assertEquals
import org.junit.Test

class UsersApiTest : Node64ApiTestBaseA() {

    private val userJson = """{"id":1,"uid":"u-1","username":"admin","lang":"de","timezone":"Europe/Berlin",
        "totp":true,"self":true,"created_at":"2026-09-01T10:00:00Z","last_login_at":"2026-10-01T10:00:00Z"}"""

    @Test
    fun `list gets users`() = test {
        enqueueJson("[$userJson]")
        val u = node().users.list().single()
        val r = takeRequest()
        assertEquals("GET", r.method)
        assertEquals("/api/users", r.target)
        assertEquals("admin", u.username)
        assertEquals(true, u.isSelf)
        assertEquals(true, u.totp)
    }

    @Test
    fun `me gets current user`() = test {
        enqueueJson(userJson)
        assertEquals("u-1", node().users.me().uid)
        assertEquals("/api/users/me", takeRequest().target)
    }

    @Test
    fun `updateMe puts body`() = test {
        enqueueJson(userJson)
        node().users.updateMe(UpdateMeRequest(lang = UserLang.EN, timezone = "Europe/Berlin"))
        val r = takeRequest()
        assertEquals("PUT", r.method)
        assertEquals("/api/users/me", r.target)
        r.assertJsonBody("""{"lang":"en","timezone":"Europe/Berlin"}""")
    }

    @Test
    fun `delete sends DELETE`() = test {
        enqueueEmpty(204)
        node().users.delete(3)
        val r = takeRequest()
        assertEquals("DELETE", r.method)
        assertEquals("/api/users/3", r.target)
    }
}
