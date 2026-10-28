package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.DynDNSTargetInput
import de.sr.ipv64.node64.model.DynDnsDnsState
import de.sr.ipv64.node64.model.DynDnsFamily
import de.sr.ipv64.node64.model.DynDnsFromAccountItem
import de.sr.ipv64.node64.model.DynDnsHistoryType
import de.sr.ipv64.node64.model.DynDnsMode4
import de.sr.ipv64.node64.model.DynDnsMode6
import de.sr.ipv64.node64.model.DynDnsNameKind
import de.sr.ipv64.node64.model.DynDnsTargetStatus
import de.sr.ipv64.node64.model.DynDnsTokenKind
import de.sr.ipv64.node64.model.DynDnsUpdateOutcome
import de.sr.ipv64.node64.model.UpdateDynDnsSettingsRequest
import de.sr.ipv64.node64.model.UpdateDynDnsTargetRequest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class DynDnsApiTest : Node64ApiTestBaseA() {

    private val targetJson = """{"id":3,"domain":"home.ipv64.net","host":"www","fqdn":"www.home.ipv64.net","record_id":9,
        "token_kind":"record","token_hint":"…abcd","mode4":"auto","fixed4":"","mode6":"prefix","fixed6":"","suffix6":"::1",
        "iface":"eth0","enabled":true,"paused":"","status":"ok","last_ipv4":"203.0.113.7","last_ipv6":"2001:db8::1",
        "last_prefix":"2001:db8::/64","last_ok_at":"2026-10-28T09:00:00Z","last_error":"","last_error_at":"2026-10-27T09:00:00Z",
        "dns_state":"redirected"}"""

    private fun assertTarget(t: de.sr.ipv64.node64.model.DynDNSTarget) {
        assertEquals(3L, t.id)
        assertEquals("www.home.ipv64.net", t.fqdn)
        assertEquals(9L, t.recordId)
        assertEquals(DynDnsTokenKind.RECORD, t.tokenKind)
        assertEquals("…abcd", t.tokenHint)
        assertEquals(DynDnsMode4.AUTO, t.mode4)
        assertEquals(DynDnsMode6.PREFIX, t.mode6)
        assertEquals("::1", t.suffix6)
        assertEquals("eth0", t.iface)
        assertEquals(DynDnsTargetStatus.OK, t.status)
        assertEquals("203.0.113.7", t.lastIpv4)
        assertEquals("2001:db8::/64", t.lastPrefix)
        assertEquals(Instant.parse("2026-10-28T09:00:00Z"), t.lastOkAt)
        assertEquals(Instant.parse("2026-10-27T09:00:00Z"), t.lastErrorAt)
        assertEquals(DynDnsDnsState.REDIRECTED, t.dnsState)
    }

    @Test
    fun `status returns undocumented json or null`() = test {
        enqueueJson("""{"running":true,"next":"2026-10-28T10:05:00Z"}""")
        enqueueEmpty()

        val status = node().dynDns.status()
        val none = node().dynDns.status()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/dyndns/status", request.target)
        assertEquals(JsonPrimitive(true), status!!.jsonObject["running"])
        assertNull(none)
    }

    @Test
    fun `check posts and tolerates empty body`() = test {
        enqueueEmpty()

        val result = node().dynDns.check()

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/dyndns/check", request.target)
        assertNull(result)
    }

    @Test
    fun `listTargets decodes targets`() = test {
        enqueueJson("[$targetJson]")

        val targets = node().dynDns.listTargets()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/dyndns/targets", request.target)
        assertTarget(targets.single())
    }

    @Test
    fun `createTarget posts input without nulls`() = test {
        enqueueJson(targetJson, code = 201)

        val target = node().dynDns.createTarget(
            DynDNSTargetInput(
                domain = "home.ipv64.net",
                token = "UPDATE-TOKEN",
                mode4 = DynDnsMode4.FIXED,
                mode6 = DynDnsMode6.OFF,
                fixed4 = "203.0.113.7",
                tokenKind = DynDnsTokenKind.DOMAIN,
            ),
        )

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/dyndns/targets", request.target)
        request.assertJsonBody(
            """{"domain":"home.ipv64.net","token":"UPDATE-TOKEN","mode4":"fixed","mode6":"off",
            "fixed4":"203.0.113.7","token_kind":"domain"}""",
        )
        assertTarget(target)
    }

    @Test
    fun `target input toString redacts token`() {
        val input = DynDNSTargetInput("d", "UPDATE-TOKEN", DynDnsMode4.AUTO, DynDnsMode6.HOST)

        assertFalse(input.toString().contains("UPDATE-TOKEN"))
        assertFalse(UpdateDynDnsTargetRequest(token = "UPDATE-TOKEN").toString().contains("UPDATE-TOKEN"))
    }

    @Test
    fun `getTarget fetches by id`() = test {
        enqueueJson(targetJson)

        assertTarget(node().dynDns.getTarget(3))

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/dyndns/targets/3", request.target)
    }

    @Test
    fun `updateTarget puts only given fields`() = test {
        enqueueJson(targetJson)

        node().dynDns.updateTarget(3, UpdateDynDnsTargetRequest(mode6 = DynDnsMode6.SUFFIX, suffix6 = "::2", enabled = false))

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/dyndns/targets/3", request.target)
        request.assertJsonBody("""{"mode6":"suffix","suffix6":"::2","enabled":false}""")
    }

    @Test
    fun `deleteTarget sends DELETE`() = test {
        enqueueNoContent()

        node().dynDns.deleteTarget(3)

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/dyndns/targets/3", request.target)
    }

    @Test
    fun `updateTargetNow posts and decodes outcome with target`() = test {
        enqueueJson("""{"result":"sent","target":$targetJson}""")

        val result = node().dynDns.updateTargetNow(3)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/dyndns/targets/3/update", request.target)
        assertEquals(DynDnsUpdateOutcome.SENT, result.result)
        assertTarget(result.target!!)
    }

    @Test
    fun `history without filters has no query`() = test {
        enqueueJson("""[{"time":"2026-10-28T09:00:00Z","ip":"203.0.113.7"}]""")

        val history = node().dynDns.history()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/dyndns/history", request.target)
        assertEquals(1, history!!.jsonArray.size)
    }

    @Test
    fun `history sends all filters`() = test {
        enqueueEmpty()

        val history = node().dynDns.history(DynDnsHistoryType.CHECKS, target = 3, family = DynDnsFamily.IPV6, days = 7)

        assertEquals("/api/dyndns/history?type=checks&target=3&family=6&days=7", takeRequest().target)
        assertNull(history)
    }

    @Test
    fun `settings decodes values`() = test {
        enqueueJson(
            """{"interval_minutes":5,"source4":"https://v4.example","source6":"https://v6.example","keep_checks_days":30,
            "keep_updates_days":90,"notify_ip":true,"notify_fail":false,"notify_limit":true}""",
        )

        val settings = node().dynDns.settings()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/dyndns/settings", request.target)
        assertEquals(5, settings.intervalMinutes)
        assertEquals("https://v4.example", settings.source4)
        assertEquals(30, settings.keepChecksDays)
        assertEquals(90, settings.keepUpdatesDays)
        assertEquals(true, settings.notifyIp)
        assertEquals(false, settings.notifyFail)
        assertEquals(true, settings.notifyLimit)
    }

    @Test
    fun `updateSettings puts only given fields`() = test {
        enqueueJson("""{"interval_minutes":10,"notify_fail":true}""")

        val settings = node().dynDns.updateSettings(UpdateDynDnsSettingsRequest(intervalMinutes = 10, notifyFail = true))

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/dyndns/settings", request.target)
        request.assertJsonBody("""{"interval_minutes":10,"notify_fail":true}""")
        assertEquals(10, settings.intervalMinutes)
    }

    @Test
    fun `accountNames decodes names with address lists`() = test {
        enqueueJson(
            """[{"key":"home.ipv64.net","domain":"home.ipv64.net","host":"","fqdn":"home.ipv64.net","kind":"dyndns",
            "a":["203.0.113.7"],"aaaa":["2001:db8::1","2001:db8::2"],"suffix_of":"","default_mode4":"auto",
            "default_mode6":"prefix","exists":true,"blocked":""},
            {"key":"own.example.com","kind":"own","exists":false,"blocked":"cdn"}]""",
        )

        val names = node().dynDns.accountNames()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/dyndns/account/names", request.target)
        assertEquals(2, names.size)
        assertEquals(DynDnsNameKind.DYNDNS, names[0].kind)
        assertEquals(listOf("203.0.113.7"), names[0].a)
        assertEquals(listOf("2001:db8::1", "2001:db8::2"), names[0].aaaa)
        assertEquals("prefix", names[0].defaultMode6)
        assertEquals(true, names[0].exists)
        assertEquals(DynDnsNameKind.OWN, names[1].kind)
        assertEquals("cdn", names[1].blocked)
        assertTrue(names[1].a.isEmpty())
    }

    @Test
    fun `createTargetsFromAccount posts a top level array`() = test {
        enqueueEmpty(201)

        val result = node().dynDns.createTargetsFromAccount(
            listOf(
                DynDnsFromAccountItem("home.ipv64.net", DynDnsMode4.AUTO, DynDnsMode6.HOST),
                DynDnsFromAccountItem("own.example.com"),
            ),
        )

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/dyndns/targets/from-account", request.target)
        request.assertJsonBody("""[{"key":"home.ipv64.net","mode4":"auto","mode6":"host"},{"key":"own.example.com"}]""")
        assertNull(result)
    }

    @Test
    fun `createTargetsFromAccount returns json when present`() = test {
        enqueueJson("""{"created":2}""", code = 201)

        val result = node().dynDns.createTargetsFromAccount(listOf(DynDnsFromAccountItem("a")))

        assertEquals(JsonPrimitive(2), result!!.jsonObject["created"])
    }

    @Test
    fun `refreshTargetToken posts to token endpoint`() = test {
        enqueueJson(targetJson)

        val target = node().dynDns.refreshTargetToken(3)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/dyndns/targets/3/token", request.target)
        assertTarget(target)
    }
}
