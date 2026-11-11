package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.Monitor64ClusterHostRole
import de.sr.ipv64.node64.model.Monitor64ClusterHostSettingInput
import de.sr.ipv64.node64.model.Monitor64ClusterKeyConfirmInput
import de.sr.ipv64.node64.model.Monitor64ClusterKeys
import de.sr.ipv64.node64.model.Monitor64OrphanInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Monitor64ClusterApiTest : Node64ApiTestBaseA() {

    private val keysJson = """{"fingerprint":"SHA256:abc","authorized_key":"ssh-ed25519 AAAA secret","cluster":true,
        "at":"2026-11-01T02:00:00Z","old_keys":["SHA256:old"],"running":true,
        "pending":[{"access_uid":"a1","host_uid":"h1","host_name":"web","type":"ssh","tries":2,"manual":true}]}"""
    private val orphanJson = """{"uid":"h9","name":"old","address":"10.0.0.9","reason":"node_gone","node":"n2","node_name":"Two"}"""

    @Test
    fun `get decodes cluster`() = test {
        enqueueJson(
            """{"active":true,"self":"n1","majority":true,"total":2,"connected":2,
            "nodes":[{"id":"n1","name":"One","self":true,"online":true,"stable":true}],
            "hosts":[{"uid":"h1","name":"web","owner":"n1","backups":["n2"],"role":"report","reachable":true}],
            "key":$keysJson}""",
        )

        val c = node().monitor64.cluster.get()

        val r = takeRequest()
        assertEquals("GET", r.method)
        assertEquals("/api/monitor64/cluster", r.target)
        assertEquals("n1", c.selfId)
        assertTrue(c.nodes.single().isSelf == true)
        assertEquals(Monitor64ClusterHostRole.REPORT, c.hosts.single().role)
        assertEquals(listOf("n2"), c.hosts.single().backups)
        assertEquals("a1", c.key?.pending?.single()?.accessUid)
    }

    @Test
    fun `views passes host query`() = test {
        enqueueJson("""{"active":true,"self":"n1","refs":{"h1":{"node":"n2","here":false,"note":"no_data"}},"orphans":[$orphanJson]}""")

        val v = node().monitor64.cluster.views(host = "h1")

        assertEquals("/api/monitor64/cluster/views?host=h1", takeRequest().target)
        assertEquals("no_data", v.refs.getValue("h1").note)
        assertEquals("node_gone", v.orphans.single().reason)
    }

    @Test
    fun `host decodes info with events`() = test {
        enqueueJson("""{"active":true,"host":{"uid":"h1"},"ref":{"node":"n1","here":true},"events":[{"kind":"takeover","text":"x"}]}""")

        val i = node().monitor64.cluster.host("h1")

        assertEquals("/api/monitor64/cluster/hosts/h1", takeRequest().target)
        assertEquals("h1", i.host?.uid)
        assertEquals("takeover", i.events.single().kind)
    }

    @Test
    fun `setHost puts preferred and backups`() = test {
        enqueueJson("""{"uid":"h1","preferred_node":"n2","backup_nodes":["n3"]}""")

        val s = node().monitor64.cluster.setHost("h1", Monitor64ClusterHostSettingInput("n2", listOf("n3")))

        val r = takeRequest()
        assertEquals("PUT", r.method)
        assertEquals("/api/monitor64/cluster/hosts/h1", r.target)
        r.assertJsonBody("""{"preferred_node":"n2","backup_nodes":["n3"]}""")
        assertEquals("n2", s.preferredNode)
        assertEquals(listOf("n3"), s.backupNodes)
    }

    @Test
    fun `reach posts and decodes`() = test {
        enqueueJson("""{"nodes":[{"node":"n1","self":true,"ok":false,"accesses":[{"access":"a","type":"ssh","ok":false,"error":"x","ms":5,"skip":false}]}]}""")

        val reach = node().monitor64.cluster.reach("h1")

        val r = takeRequest()
        assertEquals("POST", r.method)
        assertEquals("/api/monitor64/cluster/hosts/h1/reach", r.target)
        assertEquals(5, reach.nodes.single().accesses.single().ms)
    }

    @Test
    fun `keys calls hit their paths and redact the key`() = test {
        val c = node().monitor64.cluster
        val calls = listOf<Pair<String, suspend () -> Monitor64ClusterKeys>>(
            "GET /api/monitor64/cluster/keys" to { c.keys() },
            "POST /api/monitor64/cluster/keys/rotate" to { c.rotateKeys() },
            "POST /api/monitor64/cluster/keys/retry" to { c.retryKeys() },
            "POST /api/monitor64/cluster/keys/retire" to { c.retireKeys() },
        )
        for ((expected, call) in calls) {
            enqueueJson(keysJson)
            val keys = call()
            val r = takeRequest()
            assertEquals(expected, "${r.method} ${r.target}")
            assertEquals("ssh-ed25519 AAAA secret", keys.authorizedKey)
            assertEquals(listOf("SHA256:old"), keys.oldKeys)
            assertTrue(keys.running == true)
            assertFalse(keys.toString().contains("AAAA"))
        }
    }

    @Test
    fun `confirmKey posts access uid`() = test {
        enqueueJson(keysJson)

        node().monitor64.cluster.confirmKey(Monitor64ClusterKeyConfirmInput("a1"))

        val r = takeRequest()
        assertEquals("POST", r.method)
        assertEquals("/api/monitor64/cluster/keys/confirm", r.target)
        r.assertJsonBody("""{"access_uid":"a1"}""")
    }

    @Test
    fun `orphans lists`() = test {
        enqueueJson("""{"active":true,"self":"n1","orphans":[$orphanJson]}""")

        val o = node().monitor64.cluster.orphans()

        assertEquals("/api/monitor64/cluster/orphans", takeRequest().target)
        assertEquals("h9", o.orphans.single().uid)
        assertEquals("Two", o.orphans.single().nodeName)
    }

    @Test
    fun `assign and remove post input`() = test {
        enqueueJson("""{"done":1,"orphans":[]}""")
        enqueueJson("""{"done":2,"orphans":[$orphanJson]}""")

        val a = node().monitor64.cluster.assignOrphans(Monitor64OrphanInput(uids = listOf("h9")))
        val ra = takeRequest()
        val b = node().monitor64.cluster.removeOrphans(Monitor64OrphanInput(all = true))
        val rb = takeRequest()

        assertEquals("POST /api/monitor64/cluster/orphans/assign", "${ra.method} ${ra.target}")
        ra.assertJsonBody("""{"uids":["h9"]}""")
        assertEquals("POST /api/monitor64/cluster/orphans/remove", "${rb.method} ${rb.target}")
        rb.assertJsonBody("""{"all":true}""")
        assertEquals(1, a.done)
        assertEquals(2, b.done)
        assertEquals("h9", b.orphans.single().uid)
    }
}
