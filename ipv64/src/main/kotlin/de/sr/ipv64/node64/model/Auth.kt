package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Body and response of `GET`/`PUT /auth/2fa-required`. */
@Serializable
public data class TwoFactorRequired(
    /** 2FA is mandatory for everyone (SSO users excepted). */
    val required: Boolean? = null,
)

/** Type of the directory. */
@Serializable
public enum class LdapProfile(override val value: String) : ApiValue {
    @SerialName("ad") AD("ad"),
    @SerialName("ldap") LDAP("ldap"),
}

/** AD/LDAP connection (response; the bind password is never returned). */
@Serializable
public data class LdapSettings(
    /** AD/LDAP sign-in is on. */
    val enabled: Boolean? = null,
    /** Server URLs (at most 10); the first reachable one is used. */
    val urls: List<String> = emptyList(),
    /** Require StartTLS for `ldap://`. */
    val starttls: Boolean? = null,
    /** Allow `ldap://` without TLS (legacy devices only). */
    @SerialName("allow_plain") val allowPlain: Boolean? = null,
    /** Do not verify the certificate (insecure). */
    @SerialName("skip_verify") val skipVerify: Boolean? = null,
    /** Active Directory or LDAP. */
    val profile: LdapProfile? = null,
    /** Service account; empty = anonymous search. */
    @SerialName("bind_dn") val bindDn: String? = null,
    /** Base DN. */
    @SerialName("base_dn") val baseDn: String? = null,
    /** User filter; empty = profile default; `%s` = user name. */
    @SerialName("user_filter") val userFilter: String? = null,
    /** AD: nested groups. */
    val nested: Boolean? = null,
    /** Timeout in seconds (0-60; 0 = 10 s). */
    @SerialName("timeout_sec") val timeoutSec: Int? = null,
    /** A bind password is stored. */
    @SerialName("bind_password_set") val bindPasswordSet: Boolean? = null,
    /** Default filter of the chosen profile. */
    @SerialName("default_filter") val defaultFilter: String? = null,
)

/** AD/LDAP connection (request body of `PUT /auth/ldap`, `config` of the test). [bindPassword] is redacted in [toString]. */
@Serializable
public data class LdapConfig(
    /** AD/LDAP sign-in is on. */
    val enabled: Boolean? = null,
    /** Server URLs (at most 10). */
    val urls: List<String>? = null,
    /** Require StartTLS for `ldap://`. */
    val starttls: Boolean? = null,
    /** Allow `ldap://` without TLS. */
    @SerialName("allow_plain") val allowPlain: Boolean? = null,
    /** Do not verify the certificate. */
    @SerialName("skip_verify") val skipVerify: Boolean? = null,
    /** Active Directory or LDAP. */
    val profile: LdapProfile? = null,
    /** Service account; empty = anonymous search. */
    @SerialName("bind_dn") val bindDn: String? = null,
    /** Base DN. */
    @SerialName("base_dn") val baseDn: String? = null,
    /** User filter. */
    @SerialName("user_filter") val userFilter: String? = null,
    /** AD: nested groups. */
    val nested: Boolean? = null,
    /** Timeout in seconds (0-60). */
    @SerialName("timeout_sec") val timeoutSec: Int? = null,
    /** Bind password (secret, write-only); empty or missing = unchanged. */
    @SerialName("bind_password") val bindPassword: String? = null,
) {
    override fun toString(): String =
        "LdapConfig(enabled=$enabled, urls=$urls, starttls=$starttls, allowPlain=$allowPlain, skipVerify=$skipVerify, " +
            "profile=$profile, bindDn=$bindDn, baseDn=$baseDn, userFilter=$userFilter, nested=$nested, " +
            "timeoutSec=$timeoutSec, bindPassword=<redacted>)"
}

/** Body of `POST /auth/ldap/test`. */
@Serializable
public data class LdapTestRequest(
    /** Test user name (optional). */
    val username: String? = null,
    /** Settings to test; without it the stored ones are used. Nothing is saved. */
    val config: LdapConfig? = null,
)

/** Looked-up directory entry of the LDAP test. */
@Serializable
public data class LdapTestResult(
    /** Connected server. */
    val url: String? = null,
    /** TLS in use. */
    val tls: Boolean? = null,
    /** Search probe. */
    val probe: String? = null,
    /** User found. */
    val found: Boolean? = null,
    /** DN. */
    val dn: String? = null,
    /** User name. */
    val username: String? = null,
    /** Display name. */
    @SerialName("display_name") val displayName: String? = null,
    /** E-mail address. */
    val email: String? = null,
    /** objectGUID (hex), entryUUID or DN. */
    @SerialName("stable_key") val stableKey: String? = null,
    /** Disabled in the directory. */
    val disabled: Boolean? = null,
    /** Groups of the user. */
    val groups: List<String> = emptyList(),
)

/** Node64 group matching a tested LDAP user. */
@Serializable
public data class LdapMatchingGroup(
    /** Group uid. */
    val uid: String? = null,
    /** Name. */
    val name: String? = null,
    /** Role. */
    val role: UserRole? = null,
)

/** Response of `POST /auth/ldap/test`. */
@Serializable
public data class LdapTestResponse(
    /** Connection and service account work. */
    val ok: Boolean? = null,
    /** Error of the connection or service account (without passwords). */
    val error: String? = null,
    /** Looked-up user. */
    val result: LdapTestResult? = null,
    /** Matching Node64 groups. */
    @SerialName("matching_groups") val matchingGroups: List<LdapMatchingGroup> = emptyList(),
)

/** Body and response of `GET`/`PUT /auth/oidc-groups`. */
@Serializable
public data class OidcGroupsSettings(
    /** SSO creates users via the group claim. */
    val enabled: Boolean? = null,
    /** Name of the group claim in the ID token (empty = `groups`). */
    val claim: String? = null,
)
