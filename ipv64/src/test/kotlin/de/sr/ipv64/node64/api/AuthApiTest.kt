package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.LdapConfig
import de.sr.ipv64.node64.model.LdapProfile
import de.sr.ipv64.node64.model.LdapTestRequest
import de.sr.ipv64.node64.model.OidcGroupsSettings
import de.sr.ipv64.node64.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AuthApiTest : Node64ApiTestBaseA() {

    private val ldapJson = """{"enabled":true,"urls":["ldaps://h:636"],"profile":"ad","bind_dn":"cn=svc","base_dn":"dc=x",
        "timeout_sec":10,"bind_password_set":true,"default_filter":"(sAMAccountName=%s)"}"""

    @Test
    fun `twoFactorRequired gets and sets`() = test {
        enqueueJson("""{"required":false}""")
        assertEquals(false, node().auth.twoFactorRequired().required)
        val g = takeRequest()
        assertEquals("GET", g.method)
        assertEquals("/api/auth/2fa-required", g.target)
        enqueueJson("""{"required":true}""")
        assertEquals(true, node().auth.setTwoFactorRequired(true).required)
        val p = takeRequest()
        assertEquals("PUT", p.method)
        assertEquals("/api/auth/2fa-required", p.target)
        p.assertJsonBody("""{"required":true}""")
    }

    @Test
    fun `ldap gets settings`() = test {
        enqueueJson(ldapJson)
        val s = node().auth.ldap()
        assertEquals("/api/auth/ldap", takeRequest().target)
        assertEquals(LdapProfile.AD, s.profile)
        assertEquals(true, s.bindPasswordSet)
        assertEquals(listOf("ldaps://h:636"), s.urls)
    }

    @Test
    fun `updateLdap puts body and redacts bind password`() = test {
        enqueueJson(ldapJson)
        val cfg = LdapConfig(enabled = true, bindDn = "cn=svc", bindPassword = "pw-secret", timeoutSec = 10)
        node().auth.updateLdap(cfg)
        val r = takeRequest()
        assertEquals("PUT", r.method)
        assertEquals("/api/auth/ldap", r.target)
        r.assertJsonBody("""{"enabled":true,"bind_dn":"cn=svc","timeout_sec":10,"bind_password":"pw-secret"}""")
        assertFalse(cfg.toString().contains("pw-secret"))
    }

    @Test
    fun `testLdap posts and decodes`() = test {
        enqueueJson(
            """{"ok":true,"result":{"url":"ldaps://h","tls":true,"found":true,"dn":"cn=a","display_name":"A","groups":["g"]},
            "matching_groups":[{"uid":"g-1","name":"Ops","role":"read"}]}""",
        )
        val res = node().auth.testLdap(LdapTestRequest(username = "a"))
        val r = takeRequest()
        assertEquals("POST", r.method)
        assertEquals("/api/auth/ldap/test", r.target)
        r.assertJsonBody("""{"username":"a"}""")
        assertEquals(true, res.ok)
        assertEquals("A", res.result?.displayName)
        assertEquals(UserRole.READ, res.matchingGroups.single().role)
    }

    @Test
    fun `oidcGroups gets and puts`() = test {
        enqueueJson("""{"enabled":false,"claim":"groups"}""")
        assertEquals("groups", node().auth.oidcGroups().claim)
        assertEquals("/api/auth/oidc-groups", takeRequest().target)
        enqueueJson("""{"enabled":true,"claim":"roles"}""")
        node().auth.updateOidcGroups(OidcGroupsSettings(enabled = true, claim = "roles"))
        val r = takeRequest()
        assertEquals("PUT", r.method)
        assertEquals("/api/auth/oidc-groups", r.target)
        r.assertJsonBody("""{"enabled":true,"claim":"roles"}""")
    }
}
