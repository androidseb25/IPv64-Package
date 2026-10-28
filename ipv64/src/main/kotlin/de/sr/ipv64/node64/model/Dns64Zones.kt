package de.sr.ipv64.node64.model

import de.sr.ipv64.model.IsoInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Per-server statistics of a forward. */
@Serializable
public data class DNS64ForwardServerState(
    val label: String? = null,
    val url: String? = null,
    val requests: Long? = null,
    val errors: Long? = null,
    @SerialName("avg_ms") val avgMs: Double? = null,
    val healthy: Boolean? = null,
    @SerialName("last_error") val lastError: String? = null,
    @SerialName("last_error_at") val lastErrorAt: IsoInstant? = null,
)

/** Conditional forward of a domain to other DNS servers. */
@Serializable
public data class DNS64Forward(
    val id: Long? = null,
    /** Applies to all subnames; a network like `192.168.0.0/16` becomes a reverse zone. */
    val domain: String? = null,
    /** 1-8 servers (IP, IP:port, tls://, https://…/dns-query, quic://), asked in order. */
    val servers: List<String> = emptyList(),
    val comment: String? = null,
    val enabled: Boolean? = null,
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Statistics per server (GET only). */
    val state: List<DNS64ForwardServerState> = emptyList(),
    /** Only this node (never synchronized in a cluster). */
    @SerialName("local_only") val localOnly: Boolean? = null,
)

/** Request body to create or change a forward. */
@Serializable
public data class DNS64ForwardRequest(
    val domain: String,
    val servers: List<String>,
    val comment: String? = null,
    @SerialName("local_only") val localOnly: Boolean? = null,
)

/** Response of `POST /dns64/forwards/{id}/test`. */
@Serializable
public data class DNS64ForwardTestResult(
    val ok: Boolean? = null,
    val rcode: String? = null,
    val server: String? = null,
    val ms: Double? = null,
    val error: String? = null,
)

/** Rewrite of a name to another target. */
@Serializable
public data class DNS64Rewrite(
    val id: Long? = null,
    /** Name; also `*.domain`. */
    val name: String? = null,
    val target: String? = null,
    val comment: String? = null,
    val enabled: Boolean? = null,
    @SerialName("created_at") val createdAt: IsoInstant? = null,
)

/** Request body to create or change a rewrite. */
@Serializable
public data class DNS64RewriteRequest(
    val name: String,
    val target: String,
    val comment: String? = null,
)

/** Local DNS zone. */
@Serializable
public data class DNS64Zone(
    val id: Long? = null,
    /** Zone name or a network like `192.168.1.0/24` (reverse zone). */
    val name: String? = null,
    /** Unknown names yield NXDOMAIN (otherwise forwarded). */
    val authoritative: Boolean? = null,
    /** Devices of the groups with a label as `<name>.<zone>`. */
    val devices: Boolean? = null,
    val comment: String? = null,
    val enabled: Boolean? = null,
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Default TTL of new records. */
    val ttl: Int? = null,
    val refresh: Int? = null,
    val retry: Int? = null,
    val expire: Int? = null,
    /** TTL for "not existing". */
    val minimum: Int? = null,
    /** Mail address in the SOA (empty = `hostmaster.<zone>`). */
    val hostmaster: String? = null,
    /** Dynamic updates (RFC 2136) enabled. */
    val dynamic: Boolean? = null,
    @SerialName("tsig_name") val tsigName: String? = null,
)

/** Request body to create or change a local zone. */
@Serializable
public data class DNS64ZoneRequest(
    val name: String,
    val authoritative: Boolean? = null,
    val devices: Boolean? = null,
    val comment: String? = null,
    val ttl: Int? = null,
    val refresh: Int? = null,
    val retry: Int? = null,
    val expire: Int? = null,
    val minimum: Int? = null,
    val hostmaster: String? = null,
)
