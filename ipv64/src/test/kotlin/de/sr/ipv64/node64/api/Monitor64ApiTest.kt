package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.Monitor64AccessConfig
import de.sr.ipv64.node64.model.Monitor64AccessInput
import de.sr.ipv64.node64.model.Monitor64AccessType
import de.sr.ipv64.node64.model.Monitor64ActorKind
import de.sr.ipv64.node64.model.Monitor64AgentHostState
import de.sr.ipv64.node64.model.Monitor64MaintInput
import de.sr.ipv64.node64.model.Monitor64MaintInputMode
import de.sr.ipv64.node64.model.Monitor64MaintenanceStatus
import de.sr.ipv64.node64.model.Monitor64ServiceState
import de.sr.ipv64.node64.model.Monitor64ProblemAction
import de.sr.ipv64.node64.model.Monitor64ProblemActionAction
import de.sr.ipv64.node64.model.Monitor64ProblemStatus
import de.sr.ipv64.node64.model.Monitor64Range
import de.sr.ipv64.node64.model.Monitor64SettingsInput
import de.sr.ipv64.node64.model.Monitor64TestInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Monitor64ApiTest : Node64ApiTestBaseA() {

    @Test
    fun `status decodes`() = test {
        enqueueJson("""{"version":"1.0","uptime":"3h","scheduler":{"jobs":4,"late":1},"history_mb":1.5,"goroutines":9}""")
        val s = node().monitor64.status()
        val r = takeRequest()
        assertEquals("GET", r.method)
        assertEquals("/api/monitor64/status", r.target)
        assertEquals("3h", s.uptime)
        assertEquals(4L, s.scheduler?.jobs)
        assertEquals(1.5, s.historyMb!!, 0.0)
    }

    @Test
    fun `overview decodes`() = test {
        enqueueJson(
            """{"counts":{"ok":3,"crit":1},"hosts":[{"uid":"h1","view":"maintenance","state":"warn","new":2,
            "maint":{"reason":"x","end":"2026-11-01T02:00:00Z"},"meters":[{"label":"cpu","value":12.5,"state":"ok"}]}],
            "problems":[{"host_uid":"h1","state":"crit","acked":true}],
            "recent":[{"kind":"maint_end","by":{"kind":"api","name":"k"}}],"open_unacked":1,"running":true}""",
        )
        val o = node().monitor64.overview()
        val r = takeRequest()
        assertEquals("GET", r.method)
        assertEquals("/api/monitor64/overview", r.target)
        assertEquals(3L, o.counts?.ok)
        assertEquals(2L, o.hosts[0].newServices)
        assertEquals(Monitor64ServiceState.WARN, o.hosts[0].state)
        assertEquals("cpu", o.hosts[0].meters[0].label?.value)
        assertTrue(o.problems[0].acked == true)
        assertEquals(Monitor64ActorKind.API, o.recent[0].by?.kind)
        assertEquals(1L, o.openUnacked)
    }

    @Test
    fun `sparks sends optional host`() = test {
        enqueueJson("""{"h1":[1.0,2.5]}""")
        val m = node().monitor64.sparks(host = "h1")
        val r = takeRequest()
        assertEquals("GET", r.method)
        assertEquals("/api/monitor64/sparks?host=h1", r.target)
        assertEquals(listOf(1.0, 2.5), m["h1"])
    }

    @Test
    fun `series sends repeated s in one request`() = test {
        enqueueJson("""{"from":1,"to":2,"series":[{"s":"cpu|usage","res":"raw","step":0,"points":[{"t":1,"min":1,"avg":2,"max":3,"s":60}]},{"s":"mem|usage","points":[]}],"cluster":[{"node":"n","ok":true}]}""")
        val s = node().monitor64.series("h1", listOf("cpu|usage", "mem|usage"), range = Monitor64Range.DAYS_7, off = 1, points = 100)
        val r = takeRequest()
        assertEquals("GET", r.method)
        assertEquals("/api/monitor64/series?host=h1&s=cpu%7Cusage&s=mem%7Cusage&range=7d&off=1&points=100", r.target)
        assertEquals(listOf("cpu|usage", "mem|usage"), s.series.map { it.s })
        assertEquals(3.0, s.series[0].points[0].max!!, 0.0)
        assertEquals(1, s.cluster.size)
    }

    @Test
    fun `nodeaddr sends target`() = test {
        enqueueJson("""{"addr":"10.0.0.1"}""")
        val a = node().monitor64.nodeAddr("10.0.0.2")
        val r = takeRequest()
        assertEquals("/api/monitor64/nodeaddr?target=10.0.0.2", r.target)
        assertEquals("10.0.0.1", a.addr)
    }

    @Test
    fun `test posts body and redacts secret`() = test {
        enqueueJson("""{"ok":false,"error":"x","ms":5,"need_confirm":true,"services":[{"key":"k","default":true}],"nodes":[{"name":"a","id":1,"local":true}]}""")
        val input = Monitor64TestInput(
            address = "h",
            access = Monitor64AccessInput(
                type = Monitor64AccessType.AGENT,
                secret = "topsecret",
                config = Monitor64AccessConfig(port = 1, token = "tok"),
            ),
        )
        val res = node().monitor64.test(input)
        val r = takeRequest()
        assertEquals("POST", r.method)
        assertEquals("/api/monitor64/test", r.target)
        r.assertJsonBody("""{"address":"h","access":{"type":"agent","config":{"port":1,"token":"tok"},"secret":"topsecret"}}""")
        assertFalse(input.toString().contains("topsecret"))
        assertFalse(input.toString().contains("tok\""))
        assertFalse(res.ok == true)
        assertTrue(res.services[0].isDefault == true)
        assertEquals(1L, res.nodes[0].id)
    }

    @Test
    fun `problems sends filters`() = test {
        enqueueJson(
            """{"problems":[{"uid":"p","state":"unreachable","how":"auto","host":"H","duration":5,"channels":["c"]}],
            "people":[{"kind":"user","name":"n"}],"counts":{"open":1,"closed":2}}""",
        )
        val l = node().monitor64.problems(Monitor64ProblemStatus.OPEN, Monitor64Range.HOURS_24, who = "u", q = "x", host = "h")
        val r = takeRequest()
        assertEquals("GET", r.method)
        assertEquals("/api/monitor64/problems?status=open&range=24h&who=u&q=x&host=h", r.target)
        assertEquals("H", l.problems[0].host)
        assertEquals(5L, l.problems[0].duration)
        assertEquals(2L, l.counts?.closed)
    }

    @Test
    fun `problems csv returns text`() = test {
        enqueueText("a,b\n1,2\n", "text/csv")
        val csv = node().monitor64.problemsCsv(status = Monitor64ProblemStatus.ALL)
        val r = takeRequest()
        assertEquals("GET", r.method)
        assertEquals("/api/monitor64/problems.csv?status=all", r.target)
        assertEquals("a,b\n1,2\n", csv)
    }

    @Test
    fun `problem detail decodes`() = test {
        enqueueJson("""{"problem":{"uid":"p1","ack":{"comment":"c"}},"events":[{"kind":"ack","problem":"p1"}],"channels":{"c1":"Mail"}}""")
        val d = node().monitor64.problem("p1")
        val r = takeRequest()
        assertEquals("/api/monitor64/problems/p1", r.target)
        assertEquals("c", d.problem?.ack?.comment)
        assertEquals("ack", d.events[0].kind)
        assertEquals("Mail", d.channels["c1"])
    }

    @Test
    fun `problem action posts body`() = test {
        enqueueJson("""{"ok":true}""")
        val ok = node().monitor64.problemAction(Monitor64ProblemAction("h", Monitor64ProblemActionAction.COMMENT, key = "k", comment = "hi"))
        val r = takeRequest()
        assertEquals("POST", r.method)
        assertEquals("/api/monitor64/problems/action", r.target)
        r.assertJsonBody("""{"host":"h","action":"comment","key":"k","comment":"hi"}""")
        assertTrue(ok.ok)
    }

    @Test
    fun `maintenance list decodes`() = test {
        enqueueJson(
            """{"items":[{"uid":"m1","status":"recurring","recur":{"days":[1],"from":"01:00","to":"02:00"},"start":"2026-11-01T02:00:00Z"}],
            "hosts":[{"uid":"h","name":"H"}],"targets":{"groups":["g"],"tags":["t"]},"tz":"Europe/Berlin","now_local":"2026-11-11T10:00"}""",
        )
        val l = node().monitor64.maintenance()
        val r = takeRequest()
        assertEquals("GET", r.method)
        assertEquals("/api/monitor64/maintenance", r.target)
        assertEquals(Monitor64MaintenanceStatus.RECURRING, l.items[0].status)
        assertEquals(listOf(1), l.items[0].recur?.days)
        assertEquals("H", l.hosts[0].name)
        assertEquals(listOf("g"), l.targets?.groups)
        assertEquals("Europe/Berlin", l.tz)
    }

    @Test
    fun `create maintenance posts body`() = test {
        enqueueJson("""{"uid":"m1"}""")
        val u = node().monitor64.createMaintenance(Monitor64MaintInput(Monitor64MaintInputMode.NOW, title = "t", hosts = listOf("h"), minutes = 30))
        val r = takeRequest()
        assertEquals("POST", r.method)
        assertEquals("/api/monitor64/maintenance", r.target)
        r.assertJsonBody("""{"mode":"now","title":"t","hosts":["h"],"minutes":30}""")
        assertEquals("m1", u.uid)
    }

    @Test
    fun `update maintenance puts body`() = test {
        enqueueJson("""{"uid":"m1"}""")
        node().monitor64.updateMaintenance("m1", Monitor64MaintInput(Monitor64MaintInputMode.PLAN, tags = listOf("t"), start = "2026-11-11T10:00", end = "2026-11-11T11:00"))
        val r = takeRequest()
        assertEquals("PUT", r.method)
        assertEquals("/api/monitor64/maintenance/m1", r.target)
        r.assertJsonBody("""{"mode":"plan","tags":["t"],"start":"2026-11-11T10:00","end":"2026-11-11T11:00"}""")
    }

    @Test
    fun `delete and end maintenance`() = test {
        enqueueJson("""{"ok":true}""")
        enqueueJson("""{"ok":true}""")
        node().monitor64.deleteMaintenance("m1")
        node().monitor64.endMaintenance("m1")
        val d = takeRequest()
        val e = takeRequest()
        assertEquals("DELETE", d.method)
        assertEquals("/api/monitor64/maintenance/m1", d.target)
        assertEquals("POST", e.method)
        assertEquals("/api/monitor64/maintenance/m1/end", e.target)
    }

    @Test
    fun `settings decodes and updates`() = test {
        enqueueJson("""{"interval":60,"workers":8,"retention_5m_days":30,"trap_port":162,"push_port":9,"traps":{"running":true,"stats":{"too_big":2}},"push":{"listening":[":9"],"stats":{"Tokens":3}}}""")
        enqueueJson("""{"ok":true}""")
        val s = node().monitor64.settings()
        node().monitor64.updateSettings(Monitor64SettingsInput(interval = 60, workers = 8, trapPort = 162))
        val g = takeRequest()
        val p = takeRequest()
        assertEquals("/api/monitor64/settings", g.target)
        assertEquals(30L, s.retention5mDays)
        assertEquals(2L, s.traps?.stats?.tooBig)
        assertEquals(3L, s.push?.stats?.tokens)
        assertEquals("PUT", p.method)
        p.assertJsonBody("""{"interval":60,"workers":8,"trap_port":162}""")
    }

    @Test
    fun `traps decodes and unknown is cleared`() = test {
        enqueueJson("""{"active":1,"running":true,"port":162,"unknown":[{"addr":"1.2.3.4","count":2,"trap_oid":"1.3"}]}""")
        enqueueJson("""{"ok":true}""")
        val t = node().monitor64.traps()
        node().monitor64.clearUnknownTraps()
        val g = takeRequest()
        val d = takeRequest()
        assertEquals("/api/monitor64/traps", g.target)
        assertEquals("1.3", t.unknown[0].trapOid)
        assertEquals("DELETE", d.method)
        assertEquals("/api/monitor64/traps/unknown", d.target)
    }

    @Test
    fun `push token decodes and redacts`() = test {
        enqueueJson("""{"token":"abc123","port":9000}""")
        val t = node().monitor64.pushToken()
        val r = takeRequest()
        assertEquals("GET", r.method)
        assertEquals("/api/monitor64/push/token", r.target)
        assertEquals("abc123", t.token)
        assertEquals(9000L, t.port)
        assertFalse(t.toString().contains("abc123"))
    }

    @Test
    fun `agent decodes and refresh posts`() = test {
        val body = """{"http_port":80,"running":true,"version":"1.0","files":[{"key":"linux-amd64","cached":true}],
            "hosts":[{"uid":"h","state":"pending","push":true}],"events":[{"kind":"agent_update","host":"H"}],"source":{"bases":["b"]}}"""
        enqueueJson(body)
        enqueueJson(body)
        val a = node().monitor64.agent()
        val b = node().monitor64.refreshAgent()
        val g = takeRequest()
        val p = takeRequest()
        assertEquals("GET", g.method)
        assertEquals("/api/monitor64/agent", g.target)
        assertEquals("POST", p.method)
        assertEquals("/api/monitor64/agent/refresh", p.target)
        assertEquals(Monitor64AgentHostState.PENDING, a.hosts[0].state)
        assertEquals("H", a.events[0].host)
        assertEquals(listOf("b"), a.source?.bases)
        assertEquals(a, b)
    }
}
