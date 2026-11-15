package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.model.LdapConfig
import de.sr.ipv64.node64.model.LdapSettings
import de.sr.ipv64.node64.model.LdapTestRequest
import de.sr.ipv64.node64.model.LdapTestResponse
import de.sr.ipv64.node64.model.OidcGroupsSettings
import de.sr.ipv64.node64.model.TwoFactorRequired

/** Tag *Benutzerverwaltung*: sign-in settings (mandatory 2FA, AD/LDAP, SSO group creation). */
public class AuthApi internal constructor(private val transport: HttpTransport) {

    /** Whether 2FA is mandatory for all users. `GET /auth/2fa-required` */
    public suspend fun twoFactorRequired(): TwoFactorRequired =
        transport.json(Method.GET, ApiPath("auth", "2fa-required"))

    /** Switches mandatory 2FA (cluster-wide). `PUT /auth/2fa-required` */
    public suspend fun setTwoFactorRequired(required: Boolean): TwoFactorRequired =
        transport.json(Method.PUT, ApiPath("auth", "2fa-required"), body = transport.body(TwoFactorRequired(required)))

    /** AD/LDAP connection (without bind password). `GET /auth/ldap` */
    public suspend fun ldap(): LdapSettings =
        transport.json(Method.GET, ApiPath("auth", "ldap"))

    /** Replaces the AD/LDAP connection; an empty bind password stays unchanged. `PUT /auth/ldap` */
    public suspend fun updateLdap(config: LdapConfig): LdapSettings =
        transport.json(Method.PUT, ApiPath("auth", "ldap"), body = transport.body(config))

    /** Tests the AD/LDAP connection and optionally looks up a user; saves nothing. `POST /auth/ldap/test` */
    public suspend fun testLdap(request: LdapTestRequest = LdapTestRequest()): LdapTestResponse =
        transport.json(Method.POST, ApiPath("auth", "ldap", "test"), body = transport.body(request))

    /** SSO: create users via the group claim. `GET /auth/oidc-groups` */
    public suspend fun oidcGroups(): OidcGroupsSettings =
        transport.json(Method.GET, ApiPath("auth", "oidc-groups"))

    /** Changes the SSO creation via groups. `PUT /auth/oidc-groups` */
    public suspend fun updateOidcGroups(settings: OidcGroupsSettings): OidcGroupsSettings =
        transport.json(Method.PUT, ApiPath("auth", "oidc-groups"), body = transport.body(settings))
}
