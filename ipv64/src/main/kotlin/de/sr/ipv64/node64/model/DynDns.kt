package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IsoInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Kind of token of a DynDNS target. */
@Serializable
public enum class DynDnsTokenKind(override val value: String) : ApiValue {
    @SerialName("record") RECORD("record"),
    @SerialName("domain") DOMAIN("domain"),
}

/** Mode for the IPv4 address. */
@Serializable
public enum class DynDnsMode4(override val value: String) : ApiValue {
    @SerialName("auto") AUTO("auto"),
    @SerialName("fixed") FIXED("fixed"),
    @SerialName("off") OFF("off"),
}

/** Mode for the IPv6 address. */
@Serializable
public enum class DynDnsMode6(override val value: String) : ApiValue {
    @SerialName("host") HOST("host"),
    @SerialName("prefix") PREFIX("prefix"),
    @SerialName("suffix") SUFFIX("suffix"),
    @SerialName("iface") IFACE("iface"),
    @SerialName("fixed") FIXED("fixed"),
    @SerialName("off") OFF("off"),
}

/** Status of a DynDNS target. */
@Serializable
public enum class DynDnsTargetStatus(override val value: String) : ApiValue {
    @SerialName("new") NEW("new"),
    @SerialName("ok") OK("ok"),
    @SerialName("cdn") CDN("cdn"),
    @SerialName("pending") PENDING("pending"),
    @SerialName("dns") DNS("dns"),
    @SerialName("error") ERROR("error"),
    @SerialName("paused") PAUSED("paused"),
    @SerialName("off") OFF("off"),
}

/** Result of the comparison with the public DNS. */
@Serializable
public enum class DynDnsDnsState(override val value: String) : ApiValue {
    @SerialName("unknown") UNKNOWN("unknown"),
    @SerialName("ok") OK("ok"),
    @SerialName("differs") DIFFERS("differs"),
    @SerialName("redirected") REDIRECTED("redirected"),
}

/** DynDNS target (a host at ipv64 kept up to date). */
@Serializable
public data class DynDNSTarget(
    /** Target ID. */
    val id: Long? = null,
    /** Domain. */
    val domain: String? = null,
    /** Host. */
    val host: String? = null,
    /** Full name. */
    val fqdn: String? = null,
    /** Record ID at ipv64. */
    @SerialName("record_id") val recordId: Long? = null,
    /** Kind of token. */
    @SerialName("token_kind") val tokenKind: DynDnsTokenKind? = null,
    /** Hint of the token. */
    @SerialName("token_hint") val tokenHint: String? = null,
    /** IPv4 mode. */
    val mode4: DynDnsMode4? = null,
    /** Fixed IPv4 address. */
    val fixed4: String? = null,
    /** IPv6 mode. */
    val mode6: DynDnsMode6? = null,
    /** Fixed IPv6 address. */
    val fixed6: String? = null,
    /** IPv6 suffix. */
    val suffix6: String? = null,
    /** Network interface. */
    val iface: String? = null,
    /** Target is switched on. */
    val enabled: Boolean? = null,
    /** Reason of an automatic pause. */
    val paused: String? = null,
    /** Status. */
    val status: DynDnsTargetStatus? = null,
    /** Last reported IPv4 address. */
    @SerialName("last_ipv4") val lastIpv4: String? = null,
    /** Last reported IPv6 address. */
    @SerialName("last_ipv6") val lastIpv6: String? = null,
    /** Last reported IPv6 prefix. */
    @SerialName("last_prefix") val lastPrefix: String? = null,
    /** Last successful update. */
    @SerialName("last_ok_at") val lastOkAt: IsoInstant? = null,
    /** Last error. */
    @SerialName("last_error") val lastError: String? = null,
    /** Time of the last error. */
    @SerialName("last_error_at") val lastErrorAt: IsoInstant? = null,
    /** Comparison with the public DNS. */
    @SerialName("dns_state") val dnsState: DynDnsDnsState? = null,
)

/** Body of `POST /dyndns/targets`. [token] is redacted in [toString]. */
@Serializable
public data class DynDNSTargetInput(
    /** Domain. */
    val domain: String,
    /** Token of the record or domain at ipv64. */
    val token: String,
    /** IPv4 mode. */
    val mode4: DynDnsMode4,
    /** IPv6 mode. */
    val mode6: DynDnsMode6,
    /** Host (empty = domain itself). */
    val host: String? = null,
    /** Kind of token. */
    @SerialName("token_kind") val tokenKind: DynDnsTokenKind? = null,
    /** Record ID at ipv64. */
    @SerialName("record_id") val recordId: Long? = null,
    /** Fixed IPv4 address (mode `fixed`). */
    val fixed4: String? = null,
    /** Fixed IPv6 address (mode `fixed`). */
    val fixed6: String? = null,
    /** IPv6 suffix (mode `suffix`). */
    val suffix6: String? = null,
    /** Network interface (mode `iface`). */
    val iface: String? = null,
    /** Target is switched on. */
    val enabled: Boolean? = null,
) {
    override fun toString(): String =
        "DynDNSTargetInput(domain=$domain, token=<redacted>, mode4=$mode4, mode6=$mode6, host=$host, " +
            "tokenKind=$tokenKind, recordId=$recordId, fixed4=$fixed4, fixed6=$fixed6, suffix6=$suffix6, " +
            "iface=$iface, enabled=$enabled)"
}

/**
 * Body of `PUT /dyndns/targets/{id}`. Domain and host cannot be changed. An empty [token] keeps the
 * stored one. [token] is redacted in [toString].
 */
@Serializable
public data class UpdateDynDnsTargetRequest(
    /** New token; empty or `null` = unchanged. */
    val token: String? = null,
    /** IPv4 mode. */
    val mode4: DynDnsMode4? = null,
    /** IPv6 mode. */
    val mode6: DynDnsMode6? = null,
    /** Fixed IPv4 address. */
    val fixed4: String? = null,
    /** Fixed IPv6 address. */
    val fixed6: String? = null,
    /** IPv6 suffix. */
    val suffix6: String? = null,
    /** Network interface. */
    val iface: String? = null,
    /** Target is switched on. */
    val enabled: Boolean? = null,
) {
    override fun toString(): String =
        "UpdateDynDnsTargetRequest(token=${if (token == null) "null" else "<redacted>"}, mode4=$mode4, mode6=$mode6, " +
            "fixed4=$fixed4, fixed6=$fixed6, suffix6=$suffix6, iface=$iface, enabled=$enabled)"
}

/** Settings of the DynDNS module (response of `GET /dyndns/settings`). */
@Serializable
public data class DynDNSSettings(
    /** Check interval in minutes (1, 2, 5, 10, 15, 30, 60). */
    @SerialName("interval_minutes") val intervalMinutes: Int? = null,
    /** Source to determine the IPv4 address. */
    val source4: String? = null,
    /** Source to determine the IPv6 address. */
    val source6: String? = null,
    /** Days IP checks are kept (7, 30, 90, 365). */
    @SerialName("keep_checks_days") val keepChecksDays: Int? = null,
    /** Days updates are kept (90, 365, 0 = forever). */
    @SerialName("keep_updates_days") val keepUpdatesDays: Int? = null,
    /** Notify on an IP change. */
    @SerialName("notify_ip") val notifyIp: Boolean? = null,
    /** Notify on a failed update. */
    @SerialName("notify_fail") val notifyFail: Boolean? = null,
    /** Notify when the update limit is reached. */
    @SerialName("notify_limit") val notifyLimit: Boolean? = null,
)

/** Body of `PUT /dyndns/settings`; omitted fields keep their value. */
@Serializable
public data class UpdateDynDnsSettingsRequest(
    /** Check interval in minutes (1, 2, 5, 10, 15, 30, 60). */
    @SerialName("interval_minutes") val intervalMinutes: Int? = null,
    /** Source to determine the IPv4 address. */
    val source4: String? = null,
    /** Source to determine the IPv6 address. */
    val source6: String? = null,
    /** Days IP checks are kept (7, 30, 90, 365). */
    @SerialName("keep_checks_days") val keepChecksDays: Int? = null,
    /** Days updates are kept (90, 365, 0 = forever). */
    @SerialName("keep_updates_days") val keepUpdatesDays: Int? = null,
    /** Notify on an IP change. */
    @SerialName("notify_ip") val notifyIp: Boolean? = null,
    /** Notify on a failed update. */
    @SerialName("notify_fail") val notifyFail: Boolean? = null,
    /** Notify when the update limit is reached. */
    @SerialName("notify_limit") val notifyLimit: Boolean? = null,
)

/** Kind of a name in the ipv64 account. */
@Serializable
public enum class DynDnsNameKind(override val value: String) : ApiValue {
    @SerialName("dyndns") DYNDNS("dyndns"),
    @SerialName("own") OWN("own"),
}

/** Selectable name of the ipv64 account (without tokens). */
@Serializable
public data class DynDNSAccountName(
    /** Key to pass to `POST /dyndns/targets/from-account`. */
    val key: String? = null,
    /** Domain. */
    val domain: String? = null,
    /** Host. */
    val host: String? = null,
    /** Full name. */
    val fqdn: String? = null,
    /** Kind of the domain. */
    val kind: DynDnsNameKind? = null,
    /** Existing A records. */
    val a: List<String> = emptyList(),
    /** Existing AAAA records. */
    val aaaa: List<String> = emptyList(),
    /** Interface ID taken from the existing AAAA record (for `mode6 = suffix`). */
    @SerialName("suffix_of") val suffixOf: String? = null,
    /** Suggested IPv4 mode. */
    @SerialName("default_mode4") val defaultMode4: String? = null,
    /** Suggested IPv6 mode. */
    @SerialName("default_mode6") val defaultMode6: String? = null,
    /** A target for this name exists. */
    val exists: Boolean? = null,
    /** Reason why the name cannot be used. */
    val blocked: String? = null,
)

/** Outcome of `POST /dyndns/targets/{id}/update`. */
@Serializable
public enum class DynDnsUpdateOutcome(override val value: String) : ApiValue {
    /** The address was reported to ipv64. */
    @SerialName("sent") SENT("sent"),
    /** No current address is known for the modes of the target. */
    @SerialName("nothing") NOTHING("nothing"),
}

/** Response of `POST /dyndns/targets/{id}/update`: `{"result": "sent" | "nothing", "target": ...}`. */
@Serializable
public data class DynDnsUpdateNowResult(
    /** Whether an update was sent. */
    val result: DynDnsUpdateOutcome? = null,
    /** The target after the update. */
    val target: DynDNSTarget? = null,
)

/** Entry of the body of `POST /dyndns/targets/from-account`. */
@Serializable
public data class DynDnsFromAccountItem(
    /** Key of the name ([DynDNSAccountName.key]). */
    val key: String,
    /** IPv4 mode; `null` = suggestion. */
    val mode4: DynDnsMode4? = null,
    /** IPv6 mode; `null` = suggestion. */
    val mode6: DynDnsMode6? = null,
)

/** Type of the history. */
@Serializable
public enum class DynDnsHistoryType(override val value: String) : ApiValue {
    @SerialName("updates") UPDATES("updates"),
    @SerialName("checks") CHECKS("checks"),
}

/** Address family filter of the history. */
@Serializable
public enum class DynDnsFamily(override val value: String) : ApiValue {
    @SerialName("4") IPV4("4"),
    @SerialName("6") IPV6("6"),
}
