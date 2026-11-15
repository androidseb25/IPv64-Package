package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.EnvironmentAccessInput
import de.sr.ipv64.node64.model.EnvironmentNodeInput
import de.sr.ipv64.node64.model.EnvironmentOrigin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnvironmentApiTest : Node64ApiTestBaseB() {

    private val nodeJson = """{"id":"n1","name":"Node","url":"https://n:1","browser_url":"https://b","description":"d",
        "node_id":"x","has_key":true,"owner_uid":"u1","created_at":"2026-11-15T10:00:00Z","updated_at":"2026-11-15T11:00:00Z"}"""
    private val accessJson = """{"id":7,"name":"a","user_uid":"u1","username":"admin","created_by":"admin",
        "created_at":"2026-11-15T10:00:00Z","last_used_at":"2026-11-15T11:00:00Z","last_from_node":"n","last_from_name":"N"}"""

    @Test
    fun `get decodes nodes`() = test<Unit> {
        enqueueJson("""{"nodes":[{"id":"self","name":"Me","url":"https://me","origin":"self","online":true,"version":"1.0",
            "has_key":false,"key_error":"revoked","login":true}]}""")
        val env = node().environment.get()
        expectRequest("GET", "/api/environment")
        val n = env.nodes.single()
        assertEquals(EnvironmentOrigin.SELF, n.origin)
        assertEquals("revoked", n.keyError)
        assertEquals(true, n.online)
    }

    @Test
    fun `access list create and delete`() = test<Unit> {
        enqueueJson("[$accessJson]")
        val list = node().environment.accesses()
        expectRequest("GET", "/api/environment/access")
        assertEquals(7L, list.single().id)
        assertEquals("N", list.single().lastFromName)

        enqueueJson("""{"access":$accessJson,"key":"n64env1.SECRET"}""", 201)
        val created = node().environment.createAccess(EnvironmentAccessInput("a", userUid = "u1"))
        val post = expectRequest("POST", "/api/environment/access")
        assertEquals(js("""{"name":"a","user_uid":"u1"}"""), post.jsonBody())
        assertEquals("n64env1.SECRET", created.key)
        assertEquals("admin", created.access?.username)
        assertFalse(created.toString().contains("SECRET"))

        enqueueNoContent()
        node().environment.deleteAccess(7)
        expectRequest("DELETE", "/api/environment/access/7")
    }

    @Test
    fun `nodes add update delete`() = test<Unit> {
        enqueueJson(nodeJson, 201)
        val input = EnvironmentNodeInput("https://n:1", key = "n64env1.SECRET", name = "Node")
        assertFalse(input.toString().contains("SECRET"))
        assertTrue(input.toString().contains("<redacted>"))
        val added = node().environment.addNode(input)
        val post = expectRequest("POST", "/api/environment/nodes")
        assertEquals(js("""{"url":"https://n:1","key":"n64env1.SECRET","name":"Node"}"""), post.jsonBody())
        assertEquals("n1", added.id)
        assertEquals("u1", added.ownerUid)

        enqueueJson(nodeJson)
        node().environment.updateNode("n1", EnvironmentNodeInput("https://n:1", removeKey = true))
        val put = expectRequest("PUT", "/api/environment/nodes/n1")
        assertEquals(js("""{"url":"https://n:1","remove_key":true}"""), put.jsonBody())

        enqueueNoContent()
        node().environment.deleteNode("n1")
        expectRequest("DELETE", "/api/environment/nodes/n1")
    }
}
