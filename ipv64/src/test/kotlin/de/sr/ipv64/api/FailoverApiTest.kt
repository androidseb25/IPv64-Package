package de.sr.ipv64.api

import de.sr.ipv64.model.AddFailoverRuleRequest
import de.sr.ipv64.model.CreateFailoverPolicyRequest
import de.sr.ipv64.model.FailoverLinking
import de.sr.ipv64.model.FailoverMode
import de.sr.ipv64.model.FailoverRuleState
import de.sr.ipv64.model.FailoverStatus
import de.sr.ipv64.model.FailoverTargetKind
import de.sr.ipv64.model.UpdateFailoverPolicyRequest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Instant

class FailoverApiTest : ApiSdkTestBase() {

    private val policyJson = """{"id":"pol1","name":"Web failover","status":"failed_over","manual":false,
        "mode":"auto_failover_and_fallback","fallback_requires_all":true,"linking":"or","notifications_enabled":true,
        "integrations":[0,5],"monitors_down":1,"last_check_at":"2026-10-10T10:00:00Z",
        "monitors":[{"healthcheck":"hc1","name":"Web","negate":false,"healthcheck_status":"down"}],
        "rules":[{"record":"rec1","fqdn":"www.example.com","record_type":"A","state":"failed_over",
        "target":{"kind":"offline_page","ip":null,"record":null,"offline_page":"off1"}},
        {"record":"rec2","fqdn":"api.example.com","record_type":"AAAA","state":"standby",
        "target":{"kind":"ip","ip":"2001:db8::2"}}]}"""

    private fun assertPolicy(policy: de.sr.ipv64.model.FailoverPolicy) {
        assertEquals("pol1", policy.id)
        assertEquals(FailoverStatus.FAILED_OVER, policy.status)
        assertEquals(FailoverMode.AUTO_FAILOVER_AND_FALLBACK, policy.mode)
        assertEquals(FailoverLinking.OR, policy.linking)
        assertEquals(true, policy.fallbackRequiresAll)
        assertEquals(listOf(0L, 5L), policy.integrations)
        assertEquals(1, policy.monitorsDown)
        assertEquals(Instant.parse("2026-10-10T10:00:00Z"), policy.lastCheckAt)
        val monitor = policy.monitors.single()
        assertEquals("hc1", monitor.healthcheck)
        assertEquals(false, monitor.negate)
        assertEquals("down", monitor.healthcheckStatus)
        assertEquals(2, policy.rules.size)
        assertEquals(FailoverRuleState.FAILED_OVER, policy.rules[0].state)
        assertEquals(FailoverTargetKind.OFFLINE_PAGE, policy.rules[0].target?.kind)
        assertEquals("off1", policy.rules[0].target?.offlinePage)
        assertEquals("AAAA", policy.rules[1].recordType)
        assertEquals(FailoverTargetKind.IP, policy.rules[1].target?.kind)
        assertEquals("2001:db8::2", policy.rules[1].target?.ip)
        assertNull(policy.rules[1].target?.record)
    }

    @Test
    fun `list requests GET failover policies`() = test {
        enqueueJson("""{"data":[$policyJson]}""")

        val policies = client().failover.list()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/failover/policies", request.apiTarget)
        assertPolicy(policies.single())
    }

    @Test
    fun `create default request sends empty object`() = test {
        enqueueJson("""{"data":$policyJson}""", code = 201)

        client().failover.create()

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/failover/policies", request.apiTarget)
        assertEquals(0, request.jsonBody().size)
    }

    @Test
    fun `create sends wire names`() = test {
        enqueueJson("""{"data":$policyJson}""", code = 201)

        val policy = client().failover.create(
            CreateFailoverPolicyRequest(
                name = "Web failover",
                mode = FailoverMode.MANUAL,
                fallbackRequiresAll = false,
                linking = FailoverLinking.AND,
                notificationsEnabled = true,
                integrations = listOf(5),
            ),
        )

        val body = takeRequest().jsonBody()
        assertEquals(JsonPrimitive("manual"), body["mode"])
        assertEquals(JsonPrimitive(false), body["fallback_requires_all"])
        assertEquals(JsonPrimitive("and"), body["linking"])
        assertEquals(JsonPrimitive(true), body["notifications_enabled"])
        assertEquals(JsonArray(listOf(JsonPrimitive(5))), body["integrations"])
        assertPolicy(policy)
    }

    @Test
    fun `get requests policy by id and encodes the segment`() = test {
        enqueueJson("""{"data":$policyJson}""")
        enqueueJson("""{"data":$policyJson}""")

        assertPolicy(client().failover.get("pol1"))
        client().failover.get("a b/c")

        val first = takeRequest()
        assertEquals("GET", first.method)
        assertEquals("/failover/policies/pol1", first.apiTarget)
        assertEquals("/failover/policies/a%20b%2Fc", takeRequest().apiTarget)
    }

    @Test
    fun `update sends only non-null fields`() = test {
        enqueueJson("""{"data":$policyJson}""")

        client().failover.update("pol1", UpdateFailoverPolicyRequest(linking = FailoverLinking.AND))

        val request = takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/failover/policies/pol1", request.apiTarget)
        assertEquals(setOf("linking"), request.jsonBody().keys)
    }

    @Test
    fun `delete requests DELETE and accepts 204`() = test {
        enqueueNoContent()

        client().failover.delete("pol1")

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/failover/policies/pol1", request.apiTarget)
    }

    @Test
    fun `pause resume failover and fallback post to their endpoints`() = test {
        repeat(4) { enqueueJson("""{"data":$policyJson}""") }
        val api = client().failover

        api.pause("pol1")
        api.resume("pol1")
        api.failover("pol1")
        api.fallback("pol1")

        for (action in listOf("pause", "resume", "failover", "fallback")) {
            val request = takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/failover/policies/pol1/$action", request.apiTarget)
        }
    }

    @Test
    fun `failover returns the decoded policy`() = test {
        enqueueJson("""{"data":$policyJson}""", code = 202)

        assertPolicy(client().failover.failover("pol1"))
    }

    @Test
    fun `addMonitor sends healthcheck and negate`() = test {
        enqueueJson("""{"data":$policyJson}""", code = 201)

        val policy = client().failover.addMonitor("pol1", "hc1", negate = true)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/failover/policies/pol1/monitors", request.apiTarget)
        val body = request.jsonBody()
        assertEquals(JsonPrimitive("hc1"), body["healthcheck"])
        assertEquals(JsonPrimitive(true), body["negate"])
        assertPolicy(policy)
    }

    @Test
    fun `addMonitor without negate omits it`() = test {
        enqueueJson("""{"data":$policyJson}""", code = 201)

        client().failover.addMonitor("pol1", "hc1")

        assertEquals(setOf("healthcheck"), takeRequest().jsonBody().keys)
    }

    @Test
    fun `removeMonitor requests DELETE with token segment`() = test {
        enqueueNoContent()

        client().failover.removeMonitor("pol1", "hc 1")

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/failover/policies/pol1/monitors/hc%201", request.apiTarget)
    }

    @Test
    fun `addRule sends record and target ip only`() = test {
        enqueueJson("""{"data":$policyJson}""", code = 201)

        val policy = client().failover.addRule("pol1", AddFailoverRuleRequest(record = "rec1", targetIp = "203.0.113.9"))

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/failover/policies/pol1/rules", request.apiTarget)
        val body = request.jsonBody()
        assertEquals(setOf("record", "target_ip"), body.keys)
        assertEquals(JsonPrimitive("203.0.113.9"), body["target_ip"])
        assertPolicy(policy)
    }

    @Test
    fun `addRule with offline page uses offline_page field`() = test {
        enqueueJson("""{"data":$policyJson}""", code = 201)

        client().failover.addRule("pol1", AddFailoverRuleRequest(record = "rec1", offlinePage = "off1"))

        assertEquals(JsonPrimitive("off1"), takeRequest().jsonBody()["offline_page"])
    }

    @Test
    fun `removeRule requests DELETE with record segment`() = test {
        enqueueNoContent()

        client().failover.removeRule("pol1", "rec1")

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/failover/policies/pol1/rules/rec1", request.apiTarget)
    }
}
