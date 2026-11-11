package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.Monitor64Rule
import de.sr.ipv64.node64.model.Monitor64RuleOverrideInput
import de.sr.ipv64.node64.model.Monitor64RulePreviewInput
import de.sr.ipv64.node64.model.Monitor64RulesStdInput
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class Monitor64RulesApiTest : Node64ApiTestBaseA() {

    private val rule: Monitor64Rule = Json { ignoreUnknownKeys = true }.decodeFromString("""{"report":"show"}""")

    @Test
    fun `get decodes rules`() = test {
        enqueueJson(
            """{"kinds":[{"kind":"cpu","group":"system","unit":"%","factor":1.0,"down":false}],
            "defaults":{"cpu":{"report":"notify"}},"std":{"cpu":{"report":"notify"}},
            "overrides":[{"uid":"o1","position":0,"target_type":"tag","target":"db","kind":"cpu","data":{"x":1},
            "rule":{"report":"show"},"effect":{"services":2,"hosts":["web"],"before":{"warn":80,"crit":90},"after":{"warn":70}}}],
            "own":[{"host_uid":"h1","host":"web","key":"","kind":"cpu","rule":{"report":"show"}}],
            "targets":{"groups":["g"],"tags":["db"]}}""",
        )

        val r = node().monitor64.rules.get()

        val req = takeRequest()
        assertEquals("GET", req.method)
        assertEquals("/api/monitor64/rules", req.target)
        assertEquals("cpu", r.kinds.single().kind)
        assertEquals(setOf("cpu"), r.defaults.keys)
        assertEquals(setOf("cpu"), r.std.keys)
        val o = r.overrides.single()
        assertEquals("o1", o.uid)
        assertEquals("tag", o.targetType)
        assertEquals(2, o.effect?.services)
        assertEquals(70, o.effect?.after?.warn)
        assertNotNull(o.rule)
        assertEquals("web", r.own.single().host)
        assertEquals(listOf("db"), r.targets?.tags)
    }

    @Test
    fun `setStd puts rules and decodes changed`() = test {
        enqueueJson("""{"ok":true,"changed":["cpu"]}""")

        val res = node().monitor64.rules.setStd(Monitor64RulesStdInput(mapOf("cpu" to rule)))

        val req = takeRequest()
        assertEquals("PUT", req.method)
        assertEquals("/api/monitor64/rules/std", req.target)
        assertEquals(setOf("cpu"), req.json().jsonObject["rules"]!!.jsonObject.keys)
        assertEquals(listOf("cpu"), res.changed)
    }

    @Test
    fun `preview posts input with uid`() = test {
        enqueueJson("""{"services":3,"hosts":["a"],"own":[],"shadow":{"0":["a"]},"before":{"warn":1,"crit":2},"after":{"warn":3,"crit":4}}""")

        val e = node().monitor64.rules.preview(Monitor64RulePreviewInput("group", "g", "cpu", rule, uid = "o1"))

        val req = takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/api/monitor64/rules/preview", req.target)
        val body: JsonObject = req.json().jsonObject
        assertEquals("o1", (body["uid"] as kotlinx.serialization.json.JsonPrimitive).content)
        assertEquals("group", (body["target_type"] as kotlinx.serialization.json.JsonPrimitive).content)
        assertEquals(3, e.services)
        assertEquals(listOf("a"), e.shadow.getValue("0"))
        assertEquals(4, e.after?.crit)
    }

    @Test
    fun `create and update override send body and return uid`() = test {
        enqueueJson("""{"uid":"n1"}""")
        enqueueJson("""{"uid":"n1"}""")
        val input = Monitor64RuleOverrideInput("tag", "db", "cpu", rule)

        val created = node().monitor64.rules.createOverride(input)
        val c = takeRequest()
        val updated = node().monitor64.rules.updateOverride("n1", input)
        val u = takeRequest()

        assertEquals("POST /api/monitor64/rules/overrides", "${c.method} ${c.target}")
        assertEquals("PUT /api/monitor64/rules/overrides/n1", "${u.method} ${u.target}")
        assertEquals("db", (c.json().jsonObject["target"] as kotlinx.serialization.json.JsonPrimitive).content)
        assertEquals("n1", created.uid)
        assertEquals("n1", updated.uid)
    }

    @Test
    fun `delete and move override`() = test {
        enqueueJson("""{"ok":true}""")
        enqueueJson("""{"ok":true}""")

        val d = node().monitor64.rules.deleteOverride("o1")
        val rd = takeRequest()
        val m = node().monitor64.rules.moveOverride("o1", -1)
        val rm = takeRequest()

        assertEquals("DELETE /api/monitor64/rules/overrides/o1", "${rd.method} ${rd.target}")
        assertEquals("POST /api/monitor64/rules/overrides/o1/move", "${rm.method} ${rm.target}")
        rm.assertJsonBody("""{"dir":-1}""")
        assertEquals(true, d.ok)
        assertEquals(true, m.ok)
    }
}
