package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.time.Instant

class Proxy64AccessListsApiTest : Node64ApiTestBaseB() {

    private val lists get() = node().proxy.accessLists

    private val listJson = """{"id":2,"name":"Office","satisfy_any":true,"pass_auth":false,"users":[{"username":"alice"},{"username":"bob"}],
        "rules":[{"action":"allow","cidr":"10.0.0.0/8"},{"action":"deny","cidr":"0.0.0.0/0"}],
        "created_at":"2026-10-20T10:00:00Z","updated_at":"2026-10-21T10:00:00Z"}"""

    private fun assertList(l: CDNAccessList) {
        assertEquals(2L, l.id)
        assertEquals("Office", l.name)
        assertEquals(true, l.satisfyAny)
        assertEquals(false, l.passAuth)
        assertEquals(listOf("alice", "bob"), l.users.map { it.username })
        assertEquals(listOf(AccessRuleAction.ALLOW, AccessRuleAction.DENY), l.rules.map { it.action })
        assertEquals("10.0.0.0/8", l.rules[0].cidr)
        assertEquals(Instant.parse("2026-10-20T10:00:00Z"), l.createdAt)
        assertEquals(Instant.parse("2026-10-21T10:00:00Z"), l.updatedAt)
    }

    @Test
    fun `list decodes usage`() = test<Unit> {
        enqueueJson("""[{"access_list":$listJson,"used_by":["a.example.com","b.example.com"]}]""")
        val u = lists.list().single()
        expectRequest("GET", "/api/cdn64/access-lists")
        assertList(u.accessList!!)
        assertEquals(listOf("a.example.com", "b.example.com"), u.usedBy)
    }

    @Test
    fun `create posts users with passwords and redacts toString`() = test<Unit> {
        enqueueJson(listJson)
        val input = CDNAccessListInput(
            "Office", true, false, listOf(CDNAccessListInputUser("alice", "hunter2"), CDNAccessListInputUser("bob")),
            listOf(CDNAccessListRule(AccessRuleAction.ALLOW, "10.0.0.0/8")),
        )
        assertList(lists.create(input))
        assertEquals(
            js(
                """{"name":"Office","satisfy_any":true,"pass_auth":false,
                "users":[{"username":"alice","password":"hunter2"},{"username":"bob"}],"rules":[{"action":"allow","cidr":"10.0.0.0/8"}]}""",
            ),
            expectRequest("POST", "/api/cdn64/access-lists").jsonBody(),
        )
        assertFalse(input.toString().contains("hunter2"))
        assertFalse(input.users!![0].toString().contains("hunter2"))
        assertTrue(input.users!![0].toString().contains("alice"))
        assertTrue(input.users!![0].toString().contains("<redacted>"))
    }

    @Test
    fun `get`() = test<Unit> {
        enqueueJson(listJson)
        assertList(lists.get(2))
        expectRequest("GET", "/api/cdn64/access-lists/2")
    }

    @Test
    fun `update sends only provided fields`() = test<Unit> {
        enqueueJson(listJson)
        assertList(lists.update(2, CDNAccessListInput(name = "Renamed")))
        assertEquals(js("""{"name":"Renamed"}"""), expectRequest("PUT", "/api/cdn64/access-lists/2").jsonBody())
    }

    @Test
    fun `delete`() = test<Unit> {
        enqueueNoContent()
        lists.delete(2)
        expectRequest("DELETE", "/api/cdn64/access-lists/2")
    }
}
