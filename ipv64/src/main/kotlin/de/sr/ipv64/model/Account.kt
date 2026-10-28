package de.sr.ipv64.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response of `GET /me`: account, plan, usage and limits, and the credentials in use. */
@Serializable
public data class Me(
    /** Account ID. */
    val id: Long? = null,
    /** E-mail address. */
    val email: String? = null,
    /** User name. */
    val username: String? = null,
    /** Registration time. */
    @SerialName("registered_at") val registeredAt: IsoInstant? = null,
    /** Last login on the website. */
    @SerialName("last_login_at") val lastLoginAt: IsoInstant? = null,
    /** Current plan. */
    val plan: MePlan? = null,
    /** Usage and limits. */
    val usage: MeUsage? = null,
    /** Number of days logs are retained. */
    @SerialName("log_retention_days") val logRetentionDays: Int? = null,
    /** Credentials used for this request. */
    val auth: MeAuth? = null,
)

/** Plan of an account. */
@Serializable
public data class MePlan(
    /** Plan ID. */
    val id: Long? = null,
    /** Plan name. */
    val name: String? = null,
    /** End of the term, `null` for the free plan. */
    @SerialName("valid_until") val validUntil: IsoInstant? = null,
)

/** Consumption and limit of one resource. */
@Serializable
public data class UsageCounter(
    /** Amount used. */
    val used: Double? = null,
    /** Plan limit, `null` means unlimited. */
    val limit: Int? = null,
)

/** Cloud router traffic usage. */
@Serializable
public data class VpnTrafficUsage(
    /** Amount used in GB. */
    val used: Double? = null,
    /** Limit in GB. */
    val limit: Int? = null,
    /** Whether the traffic limit has been reached. */
    val limited: Boolean? = null,
)

/** Usage and limits of an account. */
@Serializable
public data class MeUsage(
    /** DynDNS domains. */
    @SerialName("dyndns_domains") val dyndnsDomains: UsageCounter? = null,
    /** Own domains. */
    @SerialName("own_domains") val ownDomains: UsageCounter? = null,
    /** DynDNS updates of the last 24 hours. */
    @SerialName("dyndns_updates_24h") val dyndnsUpdates24h: UsageCounter? = null,
    /** Healthchecks. */
    val healthchecks: UsageCounter? = null,
    /** Healthcheck pings of the last 24 hours. */
    @SerialName("healthcheck_pings_24h") val healthcheckPings24h: UsageCounter? = null,
    /** SMS of the last 24 hours. */
    @SerialName("sms_24h") val sms24h: UsageCounter? = null,
    /** DNS failover policies. */
    @SerialName("failover_policies") val failoverPolicies: UsageCounter? = null,
    /** DNS failover monitors. */
    @SerialName("failover_monitors") val failoverMonitors: UsageCounter? = null,
    /** DNS failover rules. */
    @SerialName("failover_rules") val failoverRules: UsageCounter? = null,
    /** CDN domains. */
    @SerialName("cdn_domains") val cdnDomains: UsageCounter? = null,
    /** CDN traffic of the current month in GB. */
    @SerialName("cdn_traffic_gb") val cdnTrafficGb: UsageCounter? = null,
    /** Cloud router traffic in GB. */
    @SerialName("vpn_traffic_gb") val vpnTrafficGb: VpnTrafficUsage? = null,
    /** DNS64 clients. */
    @SerialName("dns64_clients") val dns64Clients: UsageCounter? = null,
)

/** Kind of credential used to authenticate. */
@Serializable
public enum class AuthType(override val value: String) : ApiValue {
    @SerialName("api_key") API_KEY("api_key"),
    @SerialName("account_key") ACCOUNT_KEY("account_key"),
}

/** Credentials used for the current request. */
@Serializable
public data class MeAuth(
    /** Kind of key. */
    val type: AuthType? = null,
    /** ID of the API key. */
    @SerialName("key_id") val keyId: Long? = null,
    /** Name of the API key. */
    @SerialName("key_name") val keyName: String? = null,
    /** With `X-IPv64-Act-As`: the account being acted for. */
    @SerialName("acting_for") val actingFor: Long? = null,
    /** With `X-IPv64-Act-As`: the own account. */
    @SerialName("acting_by") val actingBy: Long? = null,
)

/** One entry of the account log. */
@Serializable
public data class LogEntry(
    /** Log ID, also the cursor for `before`. */
    val id: Long? = null,
    /** Affected domain. */
    @SerialName("domain_id") val domainId: Long? = null,
    /** Category. */
    val title: String? = null,
    /** Message text. */
    val message: String? = null,
    /** Time of the entry. */
    val time: IsoInstant? = null,
)

/** Pagination info of the account log. */
@Serializable
public data class LogPageMeta(
    /** Page size. */
    val limit: Int? = null,
    /** Pass as `before` for the next page, `null` at the end. */
    @SerialName("next_before") val nextBefore: Long? = null,
    /** Retention in days. */
    @SerialName("retention_days") val retentionDays: Int? = null,
)

/** A page of account log entries. */
@Serializable
public data class LogPage(
    /** Log entries, newest first. */
    val data: List<LogEntry> = emptyList(),
    /** Pagination info. */
    val meta: LogPageMeta? = null,
)

/** Direction of an account link. */
@Serializable
public enum class AccountLinkDirection(override val value: String) : ApiValue {
    /** You shared your account. */
    @SerialName("shared_with") SHARED_WITH("shared_with"),
    /** You manage the other account. */
    @SerialName("managed") MANAGED("managed"),
}

/** Status of an account link. */
@Serializable
public enum class AccountLinkStatus(override val value: String) : ApiValue {
    @SerialName("active") ACTIVE("active"),
    @SerialName("pending") PENDING("pending"),
}

/** Account share (management permission). */
@Serializable
public data class AccountLink(
    /** ID of the share. */
    val id: Long? = null,
    /** Direction of the share. */
    val direction: AccountLinkDirection? = null,
    /** E-mail of the other account. */
    val email: String? = null,
    /** Status. */
    val status: AccountLinkStatus? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Last use. */
    @SerialName("last_used_at") val lastUsedAt: IsoInstant? = null,
    /** Only for `managed`: ID to use with `X-IPv64-Act-As`. */
    @SerialName("account_id") val accountId: Long? = null,
)

/** Response of `GET /me/account-links`. */
@Serializable
public data class AccountLinks(
    /** Accounts that may manage your account. */
    @SerialName("shared_with") val sharedWith: List<AccountLink> = emptyList(),
    /** Accounts you may manage. */
    val managed: List<AccountLink> = emptyList(),
)

/** An API key; the key itself is never shown again, only its start and end. */
@Serializable
public data class ApiKey(
    /** ID. */
    val id: Long? = null,
    /** Name. */
    val name: String? = null,
    /** Start and end of the key. */
    @SerialName("token_hint") val tokenHint: String? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Expiry, `null` means never. */
    @SerialName("expires_at") val expiresAt: IsoInstant? = null,
    /** Whether the key has expired. */
    val expired: Boolean? = null,
    /** Number of calls made with the key. */
    val uses: Long? = null,
)

/** A freshly created API key including its full secret [token]. */
@Serializable
public data class ApiKeyCreated(
    /** ID. */
    val id: Long? = null,
    /** Name. */
    val name: String? = null,
    /** Start and end of the key. */
    @SerialName("token_hint") val tokenHint: String? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Expiry, `null` means never. */
    @SerialName("expires_at") val expiresAt: IsoInstant? = null,
    /** Whether the key has expired. */
    val expired: Boolean? = null,
    /** Number of calls made with the key. */
    val uses: Long? = null,
    /** The complete key. Shown only once, store it securely. */
    val token: String? = null,
) {
    override fun toString(): String =
        "ApiKeyCreated(id=$id, name=$name, tokenHint=$tokenHint, createdAt=$createdAt, expiresAt=$expiresAt, " +
            "expired=$expired, uses=$uses, token=<redacted>)"
}

/** Request body of `POST /api-keys`. */
@Serializable
public data class CreateApiKeyRequest(
    /** Name of the key. */
    val name: String? = null,
    /** Validity in seconds, 0 = unlimited (max. 315360000). */
    @SerialName("expires_in") val expiresIn: Int? = null,
)
