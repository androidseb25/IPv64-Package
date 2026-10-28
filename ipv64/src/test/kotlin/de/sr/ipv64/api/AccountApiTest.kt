package de.sr.ipv64.api

import de.sr.ipv64.model.AccountLinkDirection
import de.sr.ipv64.model.AccountLinkStatus
import de.sr.ipv64.model.AuthType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Instant

class AccountApiTest : ApiSdkTestBase() {

    @Test
    fun `me requests GET me and decodes plan usage and auth`() = test {
        enqueueJson(
            """{"data":{"id":42,"email":"max@example.com","username":"max",
            "registered_at":"2024-01-02T03:04:05+01:00","last_login_at":"2026-10-10 12:00:00",
            "plan":{"id":3,"name":"Pro","valid_until":null},
            "usage":{"dyndns_domains":{"used":2,"limit":5},"own_domains":{"used":1.0,"limit":null},
              "healthchecks":{"used":4,"limit":20},"vpn_traffic_gb":{"used":1.5,"limit":100,"limited":false}},
            "log_retention_days":30,
            "auth":{"type":"api_key","key_id":7,"key_name":"cli","acting_for":9,"acting_by":42}}}""",
        )

        val me = client().account.me()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/me", request.apiTarget)
        assertEquals(42L, me.id)
        assertEquals("max@example.com", me.email)
        assertEquals(Instant.parse("2024-01-02T03:04:05+01:00"), me.registeredAt)
        assertEquals(Instant.parse("2026-10-10T12:00:00Z"), me.lastLoginAt)
        assertEquals("Pro", me.plan?.name)
        assertNull(me.plan?.validUntil)
        assertEquals(2.0, me.usage?.dyndnsDomains?.used)
        assertEquals(5, me.usage?.dyndnsDomains?.limit)
        assertNull(me.usage?.ownDomains?.limit)
        assertEquals(false, me.usage?.vpnTrafficGb?.limited)
        assertEquals(100, me.usage?.vpnTrafficGb?.limit)
        assertEquals(30, me.logRetentionDays)
        assertEquals(AuthType.API_KEY, me.auth?.type)
        assertEquals(7L, me.auth?.keyId)
        assertEquals(9L, me.auth?.actingFor)
    }

    @Test
    fun `logs without parameters sends no query`() = test {
        enqueueJson("""{"data":[],"meta":{"limit":50,"next_before":null,"retention_days":30}}""")

        val page = client().account.logs()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/me/logs", request.apiTarget)
        assertEquals(emptyList<Any>(), page.data)
        assertEquals(50, page.meta?.limit)
        assertNull(page.meta?.nextBefore)
    }

    @Test
    fun `logs sends limit before and since and decodes entries`() = test {
        enqueueJson(
            """{"data":[{"id":120,"domain_id":5,"title":"DynDNS","message":"Update ok","time":"2026-10-10T10:30:00Z"},
            {"id":119,"domain_id":null,"title":"Login","message":"Web login","time":"2026-10-10 09:00:00"}],
            "meta":{"limit":2,"next_before":119,"retention_days":7}}""",
        )

        val page = client().account.logs(limit = 2, before = 121L, since = Instant.parse("2026-10-10T00:00:00Z"))

        val request = takeRequest()
        assertEquals("/me/logs?limit=2&before=121&since=2026-10-10T00%3A00%3A00Z", request.apiTarget)
        assertEquals("2026-10-10T00:00:00Z", request.url.queryParameter("since"))
        assertEquals(2, page.data.size)
        assertEquals(120L, page.data[0].id)
        assertEquals(5L, page.data[0].domainId)
        assertEquals("DynDNS", page.data[0].title)
        assertEquals(Instant.parse("2026-10-10T10:30:00Z"), page.data[0].time)
        assertNull(page.data[1].domainId)
        assertEquals(Instant.parse("2026-10-10T09:00:00Z"), page.data[1].time)
        assertEquals(119L, page.meta?.nextBefore)
        assertEquals(7, page.meta?.retentionDays)
    }

    @Test
    fun `accountLinks requests GET me account-links and decodes both directions`() = test {
        enqueueJson(
            """{"data":{"shared_with":[{"id":1,"direction":"shared_with","email":"a@example.com","status":"pending",
            "created_at":"2026-01-01T00:00:00Z","last_used_at":null}],
            "managed":[{"id":2,"direction":"managed","email":"b@example.com","status":"active",
            "created_at":"2026-02-01T00:00:00Z","last_used_at":"2026-03-01T00:00:00Z","account_id":77}]}}""",
        )

        val links = client().account.accountLinks()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/me/account-links", request.apiTarget)
        assertEquals(1, links.sharedWith.size)
        assertEquals(AccountLinkDirection.SHARED_WITH, links.sharedWith[0].direction)
        assertEquals(AccountLinkStatus.PENDING, links.sharedWith[0].status)
        assertNull(links.sharedWith[0].lastUsedAt)
        val managed = links.managed.single()
        assertEquals(AccountLinkDirection.MANAGED, managed.direction)
        assertEquals(AccountLinkStatus.ACTIVE, managed.status)
        assertEquals(77L, managed.accountId)
        assertEquals(Instant.parse("2026-03-01T00:00:00Z"), managed.lastUsedAt)
    }
}
