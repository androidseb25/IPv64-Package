package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.*
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.*
import org.junit.Test

class Monitor64HostsApiTest : Node64ApiTestBaseB() {

    private val hosts get() = node().monitor64.hosts

    private val accessJson = """{"uid":"a1","type":"ssh","config":{"port":22},"hostkey":"SHA256:abc","enabled":true,"secret_set":true}"""

    private fun assertAccess(a: Monitor64Access) {
        assertEquals("a1", a.uid)
        assertEquals(Monitor64AccessType.SSH, a.type)
        assertEquals(true, a.secretSet)
        assertEquals("SHA256:abc", a.hostkey)
    }

    @Test
    fun `list`() = test<Unit> {
        enqueueJson("""[{"uid":"h1","name":"nas"}]""")
        assertEquals(1, hosts.list().size)
        expectRequest("GET", "/api/monitor64/hosts")
    }

    @Test
    fun `create posts host with accesses and redacts secrets`() = test<Unit> {
        enqueueJson("""{"uid":"h1"}""")
        val req = Monitor64HostCreate(
            "nas", "10.0.0.2", Monitor64HostKind.SSH,
            accesses = listOf(Monitor64AccessInput(type = Monitor64AccessType.SSH, secret = "s3cret")), adopt = listOf("k"),
        )
        assertEquals("h1", hosts.create(req).uid)
        assertEquals(
            js("""{"name":"nas","address":"10.0.0.2","kind":"ssh","accesses":[{"type":"ssh","secret":"s3cret"}],"adopt":["k"]}"""),
            expectRequest("POST", "/api/monitor64/hosts").jsonBody(),
        )
        assertFalse(req.toString().contains("s3cret"))
    }

    @Test
    fun `get decodes detail`() = test<Unit> {
        enqueueJson(
            """{"host":{"uid":"h1","name":"nas","kind":"ssh","tags":["a"],"created_at":"2026-10-20T10:00:00Z"},"state":"warn","view":"maintenance",
            "services":[{"key":"cpu","state":"ok","category":"system","metrics":[{"name":"util","value":12.5,"status":{"state":"warn","threshold":80}}],
            "levels":{"kind":"cpu","eff":{"warn":"off","origin":{"warn":"host"}}}}],
            "accesses":[$accessJson],"new":[{"uid":"s1","key":"k","adopted":0}],"running":true}""",
        )
        val d = hosts.get("h1")
        expectRequest("GET", "/api/monitor64/hosts/h1")
        assertEquals("nas", d.host!!.name)
        assertEquals(Monitor64HostKind.SSH, d.host!!.kind)
        assertEquals(Monitor64ServiceState.WARN, d.state)
        assertEquals(Monitor64HostView.MAINTENANCE, d.view)
        val s = d.services.single()
        assertEquals(Monitor64HostServiceCategory.SYSTEM, s.category)
        assertEquals(12.5, s.metrics[0].value!!, 0.0)
        assertEquals(JsonPrimitive(80), s.metrics[0].status!!.threshold)
        assertEquals(JsonPrimitive("off"), s.levels!!.eff!!.warn)
        assertEquals("host", s.levels!!.eff!!.origin["warn"])
        assertEquals(Monitor64AccessType.SSH, d.accesses.single().type)
        assertEquals("k", d.new.single().key)
        assertEquals(true, d.running)
    }

    @Test
    fun `update`() = test<Unit> {
        enqueueJson("""{"uid":"h1"}""")
        assertEquals("h1", hosts.update("h1", Monitor64HostInput("nas", "10.0.0.2", paused = true)).uid)
        assertEquals(
            js("""{"name":"nas","address":"10.0.0.2","paused":true}"""),
            expectRequest("PUT", "/api/monitor64/hosts/h1").jsonBody(),
        )
    }

    @Test
    fun `delete with and without history`() = test<Unit> {
        enqueueJson("""{"ok":true,"series_deleted":4}""")
        assertEquals(4, hosts.delete("h1", deleteHistory = true).seriesDeleted)
        expectRequest("DELETE", "/api/monitor64/hosts/h1?history=delete")
        enqueueJson("""{"ok":true}""")
        hosts.delete("h1")
        expectRequest("DELETE", "/api/monitor64/hosts/h1")
    }

    @Test
    fun `discover`() = test<Unit> {
        enqueueJson("""{"added":1,"new":2,"vanished":3}""")
        val r = hosts.discover("h1")
        expectRequest("POST", "/api/monitor64/hosts/h1/discover")
        assertEquals(listOf(1, 2, 3), listOf(r.added, r.new, r.vanished))
    }

    @Test
    fun `createAccess`() = test<Unit> {
        enqueueJson(accessJson)
        assertAccess(hosts.createAccess("h1", Monitor64AccessInput(type = Monitor64AccessType.SSH, secret = "pw")))
        assertEquals(js("""{"type":"ssh","secret":"pw"}"""), expectRequest("POST", "/api/monitor64/hosts/h1/access").jsonBody())
    }

    @Test
    fun `adopt and remove services`() = test<Unit> {
        enqueueJson("""{"changed":2}""")
        assertEquals(2, hosts.adoptServices("h1", Monitor64HostsServiceKeys(listOf("a", "b"), -1)).changed)
        assertEquals(js("""{"keys":["a","b"],"state":-1}"""), expectRequest("POST", "/api/monitor64/hosts/h1/services/adopt").jsonBody())
        enqueueJson("""{"changed":1}""")
        assertEquals(1, hosts.removeServices("h1", Monitor64HostsServiceKeys(listOf("a"))).changed)
        assertEquals(js("""{"keys":["a"]}"""), expectRequest("POST", "/api/monitor64/hosts/h1/services/remove").jsonBody())
    }

    @Test
    fun `updateService`() = test<Unit> {
        enqueueJson("""{"ok":true}""")
        hosts.updateService(
            "h1",
            Monitor64ServiceUpdate(
                "cpu", name = "CPU", soll = Monitor64ServiceSoll.RUN,
                rules = mapOf("cpu" to Monitor64ServiceUpdateRule(Monitor64Rule(warn = JsonPrimitive(70), crit = JsonPrimitive("off"), forSec = 60), Monitor64ServiceUpdateScope.KIND)),
            ),
        )
        assertEquals(
            js("""{"key":"cpu","name":"CPU","soll":"run","rules":{"cpu":{"rule":{"warn":70,"crit":"off","for":60},"scope":"kind"}}}"""),
            expectRequest("PUT", "/api/monitor64/hosts/h1/service").jsonBody(),
        )
    }

    @Test
    fun `updateAccess deleteAccess testAccess resetHostKey`() = test<Unit> {
        enqueueJson(accessJson)
        assertAccess(hosts.updateAccess("a1", Monitor64AccessInput(enabled = false)))
        assertEquals(js("""{"enabled":false}"""), expectRequest("PUT", "/api/monitor64/access/a1").jsonBody())
        enqueueJson("""{"ok":true}""")
        hosts.deleteAccess("a1")
        expectRequest("DELETE", "/api/monitor64/access/a1")
        enqueueJson("""{"ok":true}""")
        hosts.testAccess("a1", Monitor64AccessInput(secret = "pw"))
        assertEquals(js("""{"secret":"pw"}"""), expectRequest("POST", "/api/monitor64/access/a1/test").jsonBody())
        enqueueJson("""{"ok":true}""")
        hosts.resetHostKey("a1")
        expectRequest("POST", "/api/monitor64/access/a1/hostkey-reset")
    }

    @Test
    fun `traps and reset`() = test<Unit> {
        enqueueJson(
            """{"list":[{"time":"2026-10-20T10:00:00Z","trap_oid":"1.2","trap_name":"linkDown","handled":true,"varbinds":[{"oid":"1.3","type":"int","value":"2"}]}],
            "pending":1,"total":5,"rejected":2,"reject_reason":"rate"}""",
        )
        val t = hosts.traps("h1")
        expectRequest("GET", "/api/monitor64/hosts/h1/traps")
        assertEquals("linkDown", t.list.single().trapName)
        assertEquals("2", t.list.single().varbinds.single().value)
        assertEquals(listOf(1, 5, 2), listOf(t.pending, t.total, t.rejected))
        enqueueJson("""{"ok":true}""")
        hosts.resetTraps("h1")
        expectRequest("POST", "/api/monitor64/hosts/h1/traps/reset")
    }

    @Test
    fun `agent autoupdate and update`() = test<Unit> {
        enqueueJson("""{"uid":"h1","autoupdate":true}""")
        assertEquals(true, hosts.setAgentAutoUpdate("h1", true).autoupdate)
        assertEquals(js("""{"autoupdate":true}"""), expectRequest("PUT", "/api/monitor64/hosts/h1/agent").jsonBody())
        enqueueJson("""{"ok":true,"version":"1.4.0"}""")
        assertEquals("1.4.0", hosts.updateAgent("h1").version)
        expectRequest("POST", "/api/monitor64/hosts/h1/agent/update")
    }
}
