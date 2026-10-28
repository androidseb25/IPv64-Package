package de.sr.ipv64.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Kind of a domain. */
@Serializable
public enum class DomainKind(override val value: String) : ApiValue {
    /** DynDNS domain below an IPv64 zone. */
    @SerialName("dyndns") DYNDNS("dyndns"),
    /** Own domain. */
    @SerialName("own") OWN("own"),
}

/** Address family ignored on DynDNS updates. */
@Serializable
public enum class IgnoreFamily(override val value: String) : ApiValue {
    @SerialName("none") NONE("none"),
    @SerialName("ipv4") IPV4("ipv4"),
    @SerialName("ipv6") IPV6("ipv6"),
}

/** A domain. [updateToken] is a secret and is redacted in [toString]. */
@Serializable
public data class Domain(
    /** Domain ID. */
    val id: Long? = null,
    /** Full name. */
    val name: String? = null,
    /** `dyndns` or `own`. */
    val kind: DomainKind? = null,
    /** Zone. */
    val zone: String? = null,
    /** Whether wildcard records `*.name` are active. */
    val wildcard: Boolean? = null,
    /** Address family ignored on DynDNS updates. */
    val ignore: IgnoreFamily? = null,
    /** Fixed IPv6 prefix for interface IDs. */
    @SerialName("ipv6_prefix") val ipv6Prefix: String? = null,
    /** Whether the domain is active. */
    val active: Boolean? = null,
    /** Domain update token for `update.php` (secret). */
    @SerialName("update_token") val updateToken: String? = null,
    /** Number of DynDNS updates. */
    @SerialName("update_count") val updateCount: Long? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Last update. */
    @SerialName("updated_at") val updatedAt: IsoInstant? = null,
) {
    override fun toString(): String =
        "Domain(id=$id, name=$name, kind=$kind, zone=$zone, wildcard=$wildcard, ignore=$ignore, " +
            "ipv6Prefix=$ipv6Prefix, active=$active, updateToken=<redacted>, updateCount=$updateCount, " +
            "createdAt=$createdAt, updatedAt=$updatedAt)"
}

/** A domain including all its records. [updateToken] is a secret and is redacted in [toString]. */
@Serializable
public data class DomainDetail(
    /** Domain ID. */
    val id: Long? = null,
    /** Full name. */
    val name: String? = null,
    /** `dyndns` or `own`. */
    val kind: DomainKind? = null,
    /** Zone. */
    val zone: String? = null,
    /** Whether wildcard records `*.name` are active. */
    val wildcard: Boolean? = null,
    /** Address family ignored on DynDNS updates. */
    val ignore: IgnoreFamily? = null,
    /** Fixed IPv6 prefix for interface IDs. */
    @SerialName("ipv6_prefix") val ipv6Prefix: String? = null,
    /** Whether the domain is active. */
    val active: Boolean? = null,
    /** Domain update token for `update.php` (secret). */
    @SerialName("update_token") val updateToken: String? = null,
    /** Number of DynDNS updates. */
    @SerialName("update_count") val updateCount: Long? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Last update. */
    @SerialName("updated_at") val updatedAt: IsoInstant? = null,
    /** All records of the domain. */
    val records: List<Record> = emptyList(),
) {
    override fun toString(): String =
        "DomainDetail(id=$id, name=$name, kind=$kind, zone=$zone, wildcard=$wildcard, ignore=$ignore, " +
            "ipv6Prefix=$ipv6Prefix, active=$active, updateToken=<redacted>, updateCount=$updateCount, " +
            "createdAt=$createdAt, updatedAt=$updatedAt, records=$records)"
}

/** Request body of `POST /domains`. */
@Serializable
public data class CreateDomainRequest(
    /** Domain name; for DynDNS `<name>.<zone>`. */
    val name: String,
    /** `true` for an own domain instead of DynDNS. */
    val own: Boolean? = null,
)

/** Request body of `PATCH /domains/{domain}`; `null` fields stay unchanged. */
@Serializable
public data class UpdateDomainRequest(
    /** Enable or disable wildcard `*.name`. */
    val wildcard: Boolean? = null,
    /** Address family to ignore on DynDNS updates. */
    val ignore: IgnoreFamily? = null,
    /** Fixed IPv6 prefix such as `2001:db8:1234:5600::/56`, empty string = off. */
    @SerialName("ipv6_prefix") val ipv6Prefix: String? = null,
)

/** Request body of `POST /domains/{domain}/dyndns`. */
@Serializable
public data class DynDnsUpdateRequest(
    /** IPv4 address. */
    val ipv4: String? = null,
    /** IPv6 address. */
    val ipv6: String? = null,
    /** Prefix, empty = the domain itself. */
    val prefix: String? = null,
)

/** Outcome of a DynDNS update per IP. */
@Serializable
public enum class DynDnsOutcome(override val value: String) : ApiValue {
    @SerialName("created") CREATED("created"),
    @SerialName("updated") UPDATED("updated"),
    @SerialName("unchanged") UNCHANGED("unchanged"),
    @SerialName("ignored") IGNORED("ignored"),
    @SerialName("invalid_ip") INVALID_IP("invalid_ip"),
    @SerialName("cname_conflict") CNAME_CONFLICT("cname_conflict"),
    @SerialName("cdn_managed") CDN_MANAGED("cdn_managed"),
}

/** Result for a single IP of a DynDNS update. */
@Serializable
public data class DynDnsIpResult(
    /** IP address. */
    val ip: String? = null,
    /** `A` or `AAAA`. */
    val type: String? = null,
    /** Outcome for this IP. */
    val result: DynDnsOutcome? = null,
)

/** Response of `POST /domains/{domain}/dyndns`. */
@Serializable
public data class DynDnsResult(
    /** Domain. */
    val domain: String? = null,
    /** Prefix. */
    val prefix: String? = null,
    /** Whether at least one record changed. */
    val changed: Boolean? = null,
    /** Result per IP. */
    val results: List<DynDnsIpResult> = emptyList(),
)
