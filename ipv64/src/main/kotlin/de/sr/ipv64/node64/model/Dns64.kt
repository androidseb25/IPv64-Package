package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IsoInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Time range of statistics. */
@Serializable
public enum class DNS64Range(override val value: String) : ApiValue {
    @SerialName("1h") HOUR_1("1h"),
    @SerialName("24h") HOURS_24("24h"),
    @SerialName("7d") DAYS_7("7d"),
}

/** Kind of a top list. */
@Serializable
public enum class DNS64TopKind(override val value: String) : ApiValue {
    @SerialName("blocked") BLOCKED("blocked"),
    @SerialName("allowed") ALLOWED("allowed"),
    @SerialName("clients") CLIENTS("clients"),
}

/** Status filter of the query log. */
@Serializable
public enum class DNS64LogStatus(override val value: String) : ApiValue {
    @SerialName("allowed") ALLOWED("allowed"),
    @SerialName("cached") CACHED("cached"),
    @SerialName("blocked") BLOCKED("blocked"),
    @SerialName("local") LOCAL("local"),
    @SerialName("error") ERROR("error"),
}

/** Encrypted protocol of the generated configuration profile. */
@Serializable
public enum class DNS64MobileConfigProto(override val value: String) : ApiValue {
    @SerialName("doh") DOH("doh"),
    @SerialName("dot") DOT("dot"),
}

/** Export format of custom records. */
@Serializable
public enum class DNS64ExportFormat(override val value: String) : ApiValue {
    /** Hosts file (A/AAAA only). */
    @SerialName("hosts") HOSTS("hosts"),
    @SerialName("csv") CSV("csv"),
    @SerialName("json") JSON("json"),
}

/** Type of a custom DNS record. */
@Serializable
public enum class DNS64RecordType(override val value: String) : ApiValue {
    @SerialName("A") A("A"),
    @SerialName("AAAA") AAAA("AAAA"),
    @SerialName("CNAME") CNAME("CNAME"),
    @SerialName("TXT") TXT("TXT"),
    @SerialName("MX") MX("MX"),
    @SerialName("SRV") SRV("SRV"),
    @SerialName("PTR") PTR("PTR"),
    @SerialName("CAA") CAA("CAA"),
}

/** Upstream selection mode. */
@Serializable
public enum class DNS64UpstreamMode(override val value: String) : ApiValue {
    @SerialName("fastest") FASTEST("fastest"),
    @SerialName("parallel") PARALLEL("parallel"),
    @SerialName("ordered") ORDERED("ordered"),
}

/** Response mode for blocked domains. */
@Serializable
public enum class DNS64BlockMode(override val value: String) : ApiValue {
    @SerialName("zero") ZERO("zero"),
    @SerialName("nxdomain") NXDOMAIN("nxdomain"),
}

/** Anonymization of client addresses in the log. */
@Serializable
public enum class DNS64Anonymize(override val value: String) : ApiValue {
    @SerialName("off") OFF("off"),
    @SerialName("mask") MASK("mask"),
    @SerialName("hide") HIDE("hide"),
}

/** Certificate source for encrypted DNS. */
@Serializable
public enum class DNS64CertMode(override val value: String) : ApiValue {
    /** Own home network CA. */
    @SerialName("self") SELF("self"),
    /** Certificate of the web interface. */
    @SerialName("node64") NODE64("node64"),
    /** Let's Encrypt. */
    @SerialName("acme") ACME("acme"),
    /** Certificate from the certificate management. */
    @SerialName("central") CENTRAL("central"),
}

/** Upstream DNS server configuration. */
@Serializable
public data class DNS64Upstream(
    val name: String? = null,
    /** `https://…` (DoH), `tls://name[:853]` (DoT), `quic://name[:853]` (DoQ). */
    val url: String? = null,
    /** Bootstrap IP addresses. */
    val ips: List<String> = emptyList(),
    val enabled: Boolean? = null,
    /** User defined server. */
    val custom: Boolean? = null,
)

/** Blocklist configuration. */
@Serializable
public data class DNS64List(
    val name: String? = null,
    val url: String? = null,
    val enabled: Boolean? = null,
    /** User defined list. */
    val custom: Boolean? = null,
)

/** DNS64 settings (response of `GET /dns64/settings`). */
@Serializable
public data class DNS64Settings(
    val port: Int? = null,
    val bind: String? = null,
    /** Router answering local names. */
    val router: String? = null,
    @SerialName("retention_days") val retentionDays: Int? = null,
    /** `fastest`, `parallel` or `ordered`. */
    @SerialName("upstream_mode") val upstreamMode: String? = null,
    val upstreams: List<DNS64Upstream> = emptyList(),
    val lists: List<DNS64List> = emptyList(),
    @SerialName("list_hours") val listHours: Int? = null,
    /** `zero` or `nxdomain`. */
    @SerialName("block_mode") val blockMode: String? = null,
    @SerialName("cache_size") val cacheSize: Int? = null,
    /** `off`, `mask` or `hide`. */
    val anonymize: String? = null,
    @SerialName("allow_nets") val allowNets: List<String> = emptyList(),
    val doh: Boolean? = null,
    val dot: Boolean? = null,
    val doq: Boolean? = null,
    @SerialName("doh_port") val dohPort: Int? = null,
    @SerialName("dot_port") val dotPort: Int? = null,
    /** Names for devices and certificate. */
    val names: List<String> = emptyList(),
    /** `self`, `node64`, `acme` or `central`. */
    @SerialName("cert_mode") val certMode: String? = null,
    @SerialName("notify_upstream") val notifyUpstream: Boolean? = null,
    @SerialName("notify_lists") val notifyLists: Boolean? = null,
    @SerialName("acme_domain") val acmeDomain: String? = null,
    @SerialName("acme_prefix") val acmePrefix: String? = null,
    @SerialName("acme_email") val acmeEmail: String? = null,
    @SerialName("acme_staging") val acmeStaging: Boolean? = null,
    /** `fastest`: number of upstreams asked simultaneously (1-3). */
    @SerialName("upstream_count") val upstreamCount: Int? = null,
    /** `central`: ID of the certificate in the certificate management. */
    @SerialName("cert_id") val certId: Int? = null,
    /** Answer `use-application-dns.net` with NXDOMAIN (default on). */
    @SerialName("firefox_canary") val firefoxCanary: Boolean? = null,
)

/**
 * Request body of `PUT /dns64/settings`: only sent fields change; `upstreams` and `lists` are replaced completely.
 */
@Serializable
public data class DNS64SettingsRequest(
    val port: Int? = null,
    val bind: String? = null,
    val router: String? = null,
    @SerialName("retention_days") val retentionDays: Int? = null,
    @SerialName("upstream_mode") val upstreamMode: DNS64UpstreamMode? = null,
    val upstreams: List<DNS64Upstream>? = null,
    val lists: List<DNS64List>? = null,
    @SerialName("list_hours") val listHours: Int? = null,
    @SerialName("block_mode") val blockMode: DNS64BlockMode? = null,
    @SerialName("cache_size") val cacheSize: Int? = null,
    val anonymize: DNS64Anonymize? = null,
    @SerialName("allow_nets") val allowNets: List<String>? = null,
    val doh: Boolean? = null,
    val dot: Boolean? = null,
    val doq: Boolean? = null,
    @SerialName("doh_port") val dohPort: Int? = null,
    @SerialName("dot_port") val dotPort: Int? = null,
    val names: List<String>? = null,
    @SerialName("cert_mode") val certMode: DNS64CertMode? = null,
    @SerialName("notify_upstream") val notifyUpstream: Boolean? = null,
    @SerialName("notify_lists") val notifyLists: Boolean? = null,
    @SerialName("acme_domain") val acmeDomain: String? = null,
    @SerialName("acme_prefix") val acmePrefix: String? = null,
    @SerialName("acme_email") val acmeEmail: String? = null,
    @SerialName("acme_staging") val acmeStaging: Boolean? = null,
    @SerialName("upstream_count") val upstreamCount: Int? = null,
    @SerialName("cert_id") val certId: Int? = null,
    @SerialName("firefox_canary") val firefoxCanary: Boolean? = null,
)

/** Custom DNS record. */
@Serializable
public data class DNS64Record(
    val id: Long? = null,
    /** Record name; for PTR also the IP address. */
    val name: String? = null,
    /** Record type (A, AAAA, CNAME, TXT, MX, SRV, PTR, CAA). */
    val type: String? = null,
    /** IP, target name, text or structured value (e.g. `10 mail.home.lan` for MX). */
    val value: String? = null,
    val ttl: Int? = null,
    val enabled: Boolean? = null,
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    val comment: String? = null,
    /** A/AAAA: no reverse lookup from this record. */
    @SerialName("no_ptr") val noPtr: Boolean? = null,
    /** Only this node (never synchronized in a cluster). */
    @SerialName("local_only") val localOnly: Boolean? = null,
    /** Created by a dynamic update. */
    val dynamic: Boolean? = null,
)

/** Request body to create or change a custom DNS record. */
@Serializable
public data class DNS64RecordRequest(
    val name: String,
    val type: DNS64RecordType,
    val value: String,
    val ttl: Int? = null,
    val comment: String? = null,
    @SerialName("no_ptr") val noPtr: Boolean? = null,
    @SerialName("local_only") val localOnly: Boolean? = null,
)

/** Counters of the DNS summary. */
@Serializable
public data class DNS64SummaryStats(
    val total: Long? = null,
    val blocked: Long? = null,
    val cached: Long? = null,
    val clients: Long? = null,
    @SerialName("avgUpstreamMs") val avgUpstreamMs: Double? = null,
    @SerialName("droppedLogEntries") val droppedLogEntries: Long? = null,
)

/** Key figures in a time range. */
@Serializable
public data class DNS64Summary(
    val summary: DNS64SummaryStats? = null,
    /** Size of the blocklists. */
    @SerialName("blocklistSize") val blocklistSize: Long? = null,
    @SerialName("listsLoadedAt") val listsLoadedAt: IsoInstant? = null,
    val cache: DNS64Cache? = null,
    @SerialName("uptimeSeconds") val uptimeSeconds: Long? = null,
    val version: String? = null,
)

/** Cache counters since start. */
@Serializable
public data class DNS64Cache(
    val entries: Long? = null,
    val max: Long? = null,
    @SerialName("staleEntries") val staleEntries: Long? = null,
    val hits: Long? = null,
    val misses: Long? = null,
    @SerialName("staleServed") val staleServed: Long? = null,
    val refreshed: Long? = null,
    @SerialName("refreshFailures") val refreshFailures: Long? = null,
)

/** A row of the query log. */
@Serializable
public data class DNS64LogRow(
    /** Unix time in milliseconds. */
    val t: Long? = null,
    val client: String? = null,
    @SerialName("clientName") val clientName: String? = null,
    val domain: String? = null,
    val qtype: String? = null,
    /** allowed, cached, blocked, local, error or custom. */
    val status: String? = null,
    /** Blocklist or hint (e.g. `released:<list>`, `cat:<category>`, `forward:<domain>`). */
    val reason: String? = null,
    val rcode: String? = null,
    val upstream: String? = null,
    val ms: Double? = null,
    /** udp, tcp, doh, dot or doq. */
    val proto: String? = null,
    /** Device group. */
    val group: String? = null,
)

/** Status of a blocklist. */
@Serializable
public data class DNS64ListStatus(
    val name: String? = null,
    val url: String? = null,
    /** Number of rules. */
    val rules: Long? = null,
    @SerialName("updatedAt") val updatedAt: IsoInstant? = null,
    val error: String? = null,
    val failures: Int? = null,
)

/** Upstream server with statistics. */
@Serializable
public data class DNS64UpstreamStats(
    val name: String? = null,
    val url: String? = null,
    /** doh, dot or doq. */
    val proto: String? = null,
    val ips: List<String> = emptyList(),
    val requests: Long? = null,
    val wins: Long? = null,
    val errors: Long? = null,
    @SerialName("avgMs") val avgMs: Double? = null,
    val healthy: Boolean? = null,
    @SerialName("lastError") val lastError: String? = null,
    @SerialName("lastErrorAt") val lastErrorAt: IsoInstant? = null,
    /** `fastest`: currently among the servers asked first. */
    val active: Boolean? = null,
)

/** Blocking state (pause). */
@Serializable
public data class DNS64Blocking(
    val paused: Boolean? = null,
    @SerialName("until_resumed") val untilResumed: Boolean? = null,
    @SerialName("paused_until") val pausedUntil: IsoInstant? = null,
)

/** Entry of a top list. */
@Serializable
public data class DNS64TopItem(
    val name: String? = null,
    val hostname: String? = null,
    val count: Long? = null,
    val blocked: Long? = null,
)

/** Device seen in the last 24 hours. */
@Serializable
public data class DNS64Device(
    val ip: String? = null,
    val name: String? = null,
    val mac: String? = null,
    val group: String? = null,
    @SerialName("group_id") val groupId: Long? = null,
    val queries: Long? = null,
    @SerialName("last_seen") val lastSeen: IsoInstant? = null,
)

/** Response of `GET /dns64/status`. */
@Serializable
public data class DNS64Status(
    val version: String? = null,
    /** Listen address (address:port). */
    val listen: String? = null,
    @SerialName("startedAt") val startedAt: IsoInstant? = null,
    /** Router for local names (empty = none). */
    val router: String? = null,
    /** `config` or `auto`. */
    @SerialName("routerSource") val routerSource: String? = null,
    @SerialName("retentionDays") val retentionDays: Int? = null,
    val blocking: DNS64Blocking? = null,
)

/** Bucket of the request timeline. */
@Serializable
public data class DNS64TimelineBucket(
    /** Unix time in milliseconds. */
    val t: Long? = null,
    val total: Long? = null,
    val blocked: Long? = null,
)

/** Response of `GET /dns64/timeline`. */
@Serializable
public data class DNS64Timeline(
    /** Bar width in milliseconds. */
    @SerialName("stepMs") val stepMs: Long? = null,
    val buckets: List<DNS64TimelineBucket> = emptyList(),
)

/** Response of `GET /dns64/log`. */
@Serializable
public data class DNS64LogPage(
    val rows: List<DNS64LogRow> = emptyList(),
    val total: Long? = null,
)

/** Response of `GET /dns64/upstreams`. */
@Serializable
public data class DNS64Upstreams(
    /** `parallel`, `ordered` or `fastest`. */
    val mode: String? = null,
    val servers: List<DNS64UpstreamStats> = emptyList(),
    @SerialName("localDNS") val localDns: String? = null,
    @SerialName("localSource") val localSource: String? = null,
    val count: Int? = null,
)

/** Response of `POST /dns64/cache/flush`. */
@Serializable
public data class DNS64FlushResult(val flushed: Long? = null)

/** Allowed domain. */
@Serializable
public data class DNS64AllowedDomain(
    val domain: String? = null,
    @SerialName("addedAt") val addedAt: IsoInstant? = null,
)

/** Response of `POST /dns64/allowlist/import`. */
@Serializable
public data class DNS64AllowlistImportResult(
    val imported: Int? = null,
    val invalid: Int? = null,
)

/** Response of `POST /dns64/records/import`. */
@Serializable
public data class DNS64RecordImportResult(
    val added: Int? = null,
    val skipped: Int? = null,
    val invalid: Int? = null,
)

/** Device name of the zones with its current addresses. */
@Serializable
public data class DNS64DeviceName(
    val name: String? = null,
    val ips: List<String> = emptyList(),
)

/** Request body of `POST /dns64/lists`. */
@Serializable
public data class DNS64AddListRequest(
    /** URL of the blocklist. */
    val url: String,
    /** Display name (optional). */
    val name: String? = null,
)

/** Request body of `POST /dns64/upstreams`. */
@Serializable
public data class DNS64AddUpstreamRequest(
    /** `https://…` (DoH), `tls://name[:853]` (DoT) or `quic://name[:853]` (DoQ). */
    val url: String,
    /** Display name (optional). */
    val name: String? = null,
    /** Bootstrap addresses of the server. */
    val ips: List<String>? = null,
)

/** Request body of `POST /dns64/allowlist`. */
@Serializable
public data class DNS64AllowDomainRequest(val domain: String)

/** Request body of `POST /dns64/allowlist/import`. */
@Serializable
public data class DNS64AllowlistImportRequest(val domains: List<String>)

/** Request body of `POST /dns64/pause`. */
@Serializable
public data class DNS64PauseRequest(
    /** 1-1440; 0 = until resumed. */
    val minutes: Int,
)
