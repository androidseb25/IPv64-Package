package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.Monitor64ChannelRoute
import de.sr.ipv64.node64.model.Monitor64NotifyConfig
import de.sr.ipv64.node64.model.Monitor64NotifyOverrideInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Monitor64NotifyApiTest : Node64ApiTestBaseA() {

    @Test
    fun `get decodes notify`() = test {
        enqueueJson(
            """{"config":{"channels":[{"id":"default","warn":true,"crit":true}],"remind_warn":0,"remind_crit":86400,"crit_in_quiet":true},
            "channels":[{"id":3,"type":"mail","name":"Ops","enabled":true,"default":true}],
            "overrides":[{"uid":"o1","position":1,"target_type":"group","target":"g",
            "notify":{"channels":[{"id":"3","crit":true}],"replace":true,"only_crit":true,"remind_crit":3600}}],
            "quiet":{"enabled":true,"from":"22:00","to":"06:00","days":"1-7"},"targets":{"groups":["g"],"tags":[]}}""",
        )

        val n = node().monitor64.notify.get()

        val r = takeRequest()
        assertEquals("GET", r.method)
        assertEquals("/api/monitor64/notify", r.target)
        assertEquals("default", n.config?.channels?.single()?.id)
        assertEquals(86400, n.config?.remindCrit)
        assertTrue(n.channels.single().isDefault == true)
        assertEquals(3, n.channels.single().id)
        val o = n.overrides.single()
        assertEquals("g", o.target)
        assertEquals(true, o.notify?.replace)
        assertEquals(3600, o.notify?.remindCrit)
        assertEquals("22:00", n.quiet?.from)
        assertEquals(listOf("g"), n.targets?.groups)
    }

    @Test
    fun `setConfig puts config`() = test {
        enqueueJson("""{"ok":true}""")

        val ok = node().monitor64.notify.setConfig(
            Monitor64NotifyConfig(listOf(Monitor64ChannelRoute("default", warn = true, crit = true)), remindWarn = 0, critInQuiet = true),
        )

        val r = takeRequest()
        assertEquals("PUT", r.method)
        assertEquals("/api/monitor64/notify", r.target)
        r.assertJsonBody("""{"channels":[{"id":"default","warn":true,"crit":true}],"remind_warn":0,"crit_in_quiet":true}""")
        assertEquals(true, ok.ok)
    }

    @Test
    fun `test posts and decodes channel count`() = test {
        enqueueJson("""{"ok":true,"channels":2}""")

        val res = node().monitor64.notify.test()

        val r = takeRequest()
        assertEquals("POST", r.method)
        assertEquals("/api/monitor64/notify/test", r.target)
        assertEquals(2, res.channels)
    }

    @Test
    fun `create and update override`() = test {
        enqueueJson("""{"uid":"n1"}""")
        enqueueJson("""{"uid":"n1"}""")
        val input = Monitor64NotifyOverrideInput("tag", "db", replace = false, onlyCrit = true, remindCrit = 7200)

        val created = node().monitor64.notify.createOverride(input)
        val c = takeRequest()
        val updated = node().monitor64.notify.updateOverride("n1", input)
        val u = takeRequest()

        assertEquals("POST /api/monitor64/notify/overrides", "${c.method} ${c.target}")
        assertEquals("PUT /api/monitor64/notify/overrides/n1", "${u.method} ${u.target}")
        c.assertJsonBody("""{"target_type":"tag","target":"db","replace":false,"only_crit":true,"remind_crit":7200}""")
        assertEquals("n1", created.uid)
        assertEquals("n1", updated.uid)
    }

    @Test
    fun `delete and move override`() = test {
        enqueueJson("""{"ok":true}""")
        enqueueJson("""{"ok":true}""")

        node().monitor64.notify.deleteOverride("o1")
        val rd = takeRequest()
        node().monitor64.notify.moveOverride("o1", 1)
        val rm = takeRequest()

        assertEquals("DELETE /api/monitor64/notify/overrides/o1", "${rd.method} ${rd.target}")
        assertEquals("POST /api/monitor64/notify/overrides/o1/move", "${rm.method} ${rm.target}")
        rm.assertJsonBody("""{"dir":1}""")
    }
}
