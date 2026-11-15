package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.NodeGroupRequest
import de.sr.ipv64.node64.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test

class GroupsApiTest : Node64ApiTestBaseA() {

    private val groupJson = """{"uid":"g-1","name":"Ops","role":"admin","description":"d","ldap_groups":["cn=ops"],
        "oidc_groups":["ops"],"members":["a"],"ext_members":["b"],"created_at":"2026-09-01T10:00:00Z","updated_at":"2026-09-02T10:00:00Z"}"""

    @Test
    fun `list gets groups`() = test {
        enqueueJson("[$groupJson]")
        val g = node().groups.list().single()
        val r = takeRequest()
        assertEquals("GET", r.method)
        assertEquals("/api/groups", r.target)
        assertEquals("Ops", g.name)
        assertEquals(UserRole.ADMIN, g.role)
        assertEquals(listOf("cn=ops"), g.ldapGroups)
        assertEquals(listOf("b"), g.extMembers)
    }

    @Test
    fun `create posts body`() = test {
        enqueueJson(groupJson, 201)
        node().groups.create(NodeGroupRequest(name = "Ops", role = UserRole.READ, oidcGroups = listOf("ops")))
        val r = takeRequest()
        assertEquals("POST", r.method)
        assertEquals("/api/groups", r.target)
        r.assertJsonBody("""{"name":"Ops","role":"read","oidc_groups":["ops"]}""")
    }

    @Test
    fun `get gets one group`() = test {
        enqueueJson(groupJson)
        assertEquals("g-1", node().groups.get("g-1").uid)
        assertEquals("/api/groups/g-1", takeRequest().target)
    }

    @Test
    fun `update puts body`() = test {
        enqueueJson(groupJson)
        node().groups.update("g-1", NodeGroupRequest(description = "x"))
        val r = takeRequest()
        assertEquals("PUT", r.method)
        assertEquals("/api/groups/g-1", r.target)
        r.assertJsonBody("""{"description":"x"}""")
    }

    @Test
    fun `delete sends DELETE`() = test {
        enqueueEmpty(204)
        node().groups.delete("g-1")
        val r = takeRequest()
        assertEquals("DELETE", r.method)
        assertEquals("/api/groups/g-1", r.target)
    }
}
