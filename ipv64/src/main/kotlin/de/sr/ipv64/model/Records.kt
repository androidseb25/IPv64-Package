package de.sr.ipv64.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** DNS record type. */
@Serializable
public enum class RecordType(override val value: String) : ApiValue {
    A("A"), AAAA("AAAA"), CNAME("CNAME"), MX("MX"), TXT("TXT"), NS("NS"), SRV("SRV"), TLSA("TLSA"), CAA("CAA"),
}

/** State of a DNS record. */
@Serializable
public enum class RecordState(override val value: String) : ApiValue {
    @SerialName("active") ACTIVE("active"),
    @SerialName("inactive") INACTIVE("inactive"),
    /** Managed by the CDN. */
    @SerialName("cdn_record") CDN_RECORD("cdn_record"),
    /** Runs through the CDN. */
    @SerialName("cdn_proxied") CDN_PROXIED("cdn_proxied"),
    /** Partly runs through the CDN. */
    @SerialName("cdn_partial") CDN_PARTIAL("cdn_partial"),
    /** Temporary replacement created by DNS failover. */
    @SerialName("failover_record") FAILOVER_RECORD("failover_record"),
    /** Replaced by DNS failover. */
    @SerialName("failover_replaced") FAILOVER_REPLACED("failover_replaced"),
}

/** A DNS record. [token] is a secret that can be used instead of the ID. */
@Serializable
public data class Record(
    /** Record ID. */
    val id: Long? = null,
    /** Domain ID. */
    @SerialName("domain_id") val domainId: Long? = null,
    /** Domain. */
    val domain: String? = null,
    /** Prefix, empty = the domain itself. */
    val prefix: String? = null,
    /** Fully qualified name. */
    val fqdn: String? = null,
    /** Record type. */
    val type: RecordType? = null,
    /** Content. */
    val content: String? = null,
    /** TTL in seconds. */
    val ttl: Int? = null,
    /** State. */
    val state: RecordState? = null,
    /** Whether a change is still being propagated to the name servers. */
    @SerialName("pending_dns") val pendingDns: Boolean? = null,
    /** Record token; stays the same on IP changes and can replace the ID. */
    val token: String? = null,
    /** Last change. */
    @SerialName("updated_at") val updatedAt: IsoInstant? = null,
)

/** Request body of `POST /domains/{domain}/records`. */
@Serializable
public data class CreateRecordRequest(
    /** Record type. */
    val type: RecordType,
    /** Content; format per type as in the web UI, e.g. MX `10 mail.example.com`. */
    val content: String,
    /** Prefix, empty or `@` = the domain itself. */
    val prefix: String? = null,
)

/** Request body of `PATCH /records/{record}`; `null` fields stay unchanged. */
@Serializable
public data class UpdateRecordRequest(
    /** Record type. */
    val type: RecordType? = null,
    /** Prefix. */
    val prefix: String? = null,
    /** Content. */
    val content: String? = null,
)
