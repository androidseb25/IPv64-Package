package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.CreateUserRequest
import de.sr.ipv64.node64.model.SetUserPasswordRequest
import de.sr.ipv64.node64.model.UpdateMeRequest
import de.sr.ipv64.node64.model.UpdateUserRequest
import de.sr.ipv64.node64.model.UserRole
import de.sr.ipv64.node64.model.UserSource
import de.sr.ipv64.node64.model.UserLang
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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

    private val fullUserJson = """{"id":2,"uid":"u-2","username":"bob","role":"admin","own_role":"read","source":"ldap",
        "disabled":false,"email":"b@x.de","groups":["g1"],"ext_groups":["g2"],"must_change":true,"password":"s3cret-pw"}"""

    @Test
    fun `create posts body and decodes one-time password`() = test {
        enqueueJson(fullUserJson, 201)
        val req = CreateUserRequest(username = "bob", mustChange = true, role = UserRole.ADMIN, groups = listOf("g1"))
        val u = node().users.create(req)
        val r = takeRequest()
        assertEquals("POST", r.method)
        assertEquals("/api/users", r.target)
        r.assertJsonBody("""{"username":"bob","must_change":true,"role":"admin","groups":["g1"]}""")
        assertEquals(UserRole.ADMIN, u.role)
        assertEquals(UserRole.READ, u.ownRole)
        assertEquals(UserSource.LDAP, u.source)
        assertEquals(listOf("g2"), u.extGroups)
        assertEquals(true, u.mustChange)
        assertEquals("s3cret-pw", u.password)
        assertFalse(u.toString().contains("s3cret-pw"))
        assertFalse(CreateUserRequest("bob", password = "topsecret").toString().contains("topsecret"))
    }

    @Test
    fun `get gets one user`() = test {
        enqueueJson(fullUserJson)
        assertEquals("bob", node().users.get(2).username)
        val r = takeRequest()
        assertEquals("GET", r.method)
        assertEquals("/api/users/2", r.target)
    }

    @Test
    fun `update puts body`() = test {
        enqueueJson(fullUserJson)
        node().users.update(2, UpdateUserRequest(disabled = true, email = "b@x.de"))
        val r = takeRequest()
        assertEquals("PUT", r.method)
        assertEquals("/api/users/2", r.target)
        r.assertJsonBody("""{"email":"b@x.de","disabled":true}""")
    }

    @Test
    fun `reset2fa and endSessions post`() = test {
        enqueueEmpty(204)
        node().users.reset2fa(4)
        assertEquals("/api/users/4/2fa-reset", takeRequest().also { assertEquals("POST", it.method) }.target)
        enqueueEmpty(204)
        node().users.endSessions(4)
        assertEquals("/api/users/4/sessions-end", takeRequest().also { assertEquals("POST", it.method) }.target)
    }

    @Test
    fun `setPassword returns generated password or null`() = test {
        enqueueJson("""{"password":"one-time","must_change":true}""")
        val res = node().users.setPassword(4, SetUserPasswordRequest(mustChange = true))
        val r = takeRequest()
        assertEquals("POST", r.method)
        assertEquals("/api/users/4/password", r.target)
        r.assertJsonBody("""{"must_change":true}""")
        assertEquals("one-time", res?.password)
        assertFalse(res.toString().contains("one-time"))
        enqueueEmpty(204)
        assertNull(node().users.setPassword(4, SetUserPasswordRequest(password = "x")))
    }
}
