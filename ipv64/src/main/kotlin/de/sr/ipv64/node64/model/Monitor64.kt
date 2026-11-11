package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IsoInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---- Common ---------------------------------------------------------------------------------

/** Body of the Monitor64 operations that answer `{"ok":true}`. */
@Serializable
public data class Monitor64Ok(
    /** Always `true`. */
    val ok: Boolean = true,
)

/** Kind of an [Monitor64Actor]. */
@Serializable
public enum class Monitor64ActorKind(override val value: String) : ApiValue {
    @SerialName("user") USER("user"),
    @SerialName("api") API("api"),
    @SerialName("system") SYSTEM("system"),
}

/** Who triggered a change or action. */
@Serializable
public data class Monitor64Actor(
    /** Person, API key or system. */
    val kind: Monitor64ActorKind? = null,
    /** Cluster-wide user UID (persons). */
    val uid: String? = null,
    /** Name at the time of the action (user or name of the API key). */
    val name: String? = null,
    /** Sign-in method of persons (currently always `local`). */
    val method: String? = null,
    /** Address of the caller. */
    val ip: String? = null,
)

/** Acknowledgement of a problem. */
@Serializable
public data class Monitor64Ack(
    val by: Monitor64Actor? = null,
    val at: IsoInstant? = null,
    val comment: String? = null,
)

/** Entry in the history of a problem. */
@Serializable
public data class Monitor64Event(
    val at: IsoInstant? = null,
    /** Kind, e.g. a state (`warn`, `crit`, ...), `ok`, `ack`, `unack`, `comment`, `resolved`, `bell`, `maint`, `maint_end`, `pause`, `resume`, `takeover`, `handover`, `agent_update`. */
    val kind: String? = null,
    val text: String? = null,
    val actor: Monitor64Actor? = null,
    @SerialName("host_uid") val hostUid: String? = null,
    val key: String? = null,
    /** UID of the problem. */
    val problem: String? = null,
)

/** Existing groups and tags of the hosts. */
@Serializable
public data class Monitor64Targets(
    val groups: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
)

// ---- Status / settings / traps / push -------------------------------------------------------

/** Scheduler part of [Monitor64Status]. */
@Serializable
public data class Monitor64SchedulerStatus(
    val jobs: Long? = null,
    val running: Long? = null,
    val workers: Long? = null,
    val runs: Long? = null,
    val late: Long? = null,
)

/** Technical state of the module. */
@Serializable
public data class Monitor64Status(
    /** Module version. */
    val version: String? = null,
    /** Uptime (Go duration, e.g. `3h2m1s`). */
    val uptime: String? = null,
    val scheduler: Monitor64SchedulerStatus? = null,
    /** Size of the history. */
    @SerialName("history_mb") val historyMb: Double? = null,
    @SerialName("loadgen_hosts") val loadgenHosts: Long? = null,
    @SerialName("loadgen_points_per_sec") val loadgenPointsPerSec: Double? = null,
    @SerialName("heap_mb") val heapMb: Double? = null,
    val goroutines: Long? = null,
)

/** Counters of the SNMP trap receiver. */
@Serializable
public data class Monitor64TrapStats(
    val received: Long? = null,
    val accepted: Long? = null,
    @SerialName("too_big") val tooBig: Long? = null,
    @SerialName("rate_limited") val rateLimited: Long? = null,
    @SerialName("bad_packet") val badPacket: Long? = null,
    val rejected: Long? = null,
    val unknown: Long? = null,
    @SerialName("queue_full") val queueFull: Long? = null,
)

/** Unknown trap sender. */
@Serializable
public data class Monitor64TrapUnknown(
    val addr: String? = null,
    val first: IsoInstant? = null,
    val last: IsoInstant? = null,
    val count: Long? = null,
    val version: String? = null,
    @SerialName("trap_oid") val trapOid: String? = null,
    @SerialName("trap_name") val trapName: String? = null,
    /** SNMPv3 user. */
    val user: String? = null,
)

/** State of the SNMP trap receiver. */
@Serializable
public data class Monitor64TrapStatus(
    /** Hosts with traps. */
    val active: Long? = null,
    val running: Boolean? = null,
    val port: Long? = null,
    @SerialName("engine_id") val engineId: String? = null,
    val error: String? = null,
    val stats: Monitor64TrapStats? = null,
    /** Unknown senders (newest first). */
    val unknown: List<Monitor64TrapUnknown> = emptyList(),
)

/** Counters of the push listener. */
@Serializable
public data class Monitor64PushStats(
    @SerialName("Tokens") val tokens: Long? = null,
    @SerialName("Pending") val pending: Long? = null,
    @SerialName("Parked") val parked: Long? = null,
)

/** State of the listener for push mode. */
@Serializable
public data class Monitor64PushStatus(
    val listening: List<String> = emptyList(),
    val stats: Monitor64PushStats? = null,
)

/** Monitor64 settings. */
@Serializable
public data class Monitor64Settings(
    /** Default interval of new hosts (seconds). */
    val interval: Long? = null,
    /** Concurrent queries. */
    val workers: Long? = null,
    @SerialName("retention_raw_hours") val retentionRawHours: Long? = null,
    @SerialName("retention_5m_days") val retention5mDays: Long? = null,
    @SerialName("retention_1h_days") val retention1hDays: Long? = null,
    val hosts: Long? = null,
    val services: Long? = null,
    val status: Monitor64Status? = null,
    /** Port of the agent. */
    val port: Long? = null,
    /** Public key of the node (line for authorized_keys). */
    @SerialName("public_key") val publicKey: String? = null,
    val fingerprint: String? = null,
    @SerialName("trap_port") val trapPort: Long? = null,
    val traps: Monitor64TrapStatus? = null,
    /** Port for agents in push mode. */
    @SerialName("push_port") val pushPort: Long? = null,
    val push: Monitor64PushStatus? = null,
)

/** Body of `PUT /monitor64/settings`. */
@Serializable
public data class Monitor64SettingsInput(
    /** Default interval of new hosts in seconds (30-900). */
    val interval: Int,
    /** Concurrent queries (1-512); applies after the next start of the module. */
    val workers: Int,
    /** Trap receiver port (1-65535); missing = unchanged. */
    @SerialName("trap_port") val trapPort: Int? = null,
    /** Port for agents in push mode (1-65535); missing = unchanged. */
    @SerialName("push_port") val pushPort: Int? = null,
)

/** Response of `GET /monitor64/push/token`. [token] is redacted in [toString]. */
@Serializable
public data class Monitor64PushToken(
    /** New token for the install command (grants no access). */
    val token: String? = null,
    /** Push port of the node. */
    val port: Long? = null,
) {
    override fun toString(): String = "Monitor64PushToken(token=<redacted>, port=$port)"
}

/** Response of `GET /monitor64/nodeaddr`. */
@Serializable
public data class Monitor64NodeAddr(
    val addr: String? = null,
)

// ---- Overview -------------------------------------------------------------------------------

/** State of a meter, host, service or metric. */
@Serializable
public enum class Monitor64ServiceState(override val value: String) : ApiValue {
    @SerialName("ok") OK("ok"),
    @SerialName("warn") WARN("warn"),
    @SerialName("crit") CRIT("crit"),
    @SerialName("unknown") UNKNOWN("unknown"),
}

/** Label of a [Monitor64Meter]. */
@Serializable
public enum class Monitor64MeterLabel(override val value: String) : ApiValue {
    @SerialName("cpu") CPU("cpu"),
    @SerialName("ram") RAM("ram"),
    @SerialName("disk") DISK("disk"),
}

/** Small gauge of a host. */
@Serializable
public data class Monitor64Meter(
    val label: Monitor64MeterLabel? = null,
    /** Percent. */
    val value: Double? = null,
    val state: Monitor64ServiceState? = null,
)

/** Display state of a host. */
@Serializable
public enum class Monitor64HostView(override val value: String) : ApiValue {
    @SerialName("ok") OK("ok"),
    @SerialName("warn") WARN("warn"),
    @SerialName("crit") CRIT("crit"),
    @SerialName("unknown") UNKNOWN("unknown"),
    @SerialName("unreachable") UNREACHABLE("unreachable"),
    @SerialName("maintenance") MAINTENANCE("maintenance"),
}

/** Running maintenance of an [Monitor64OverviewHost]. */
@Serializable
public data class Monitor64OverviewHostMaint(
    val reason: String? = null,
    val end: IsoInstant? = null,
)

/** Host on the overview. */
@Serializable
public data class Monitor64OverviewHost(
    val uid: String? = null,
    val name: String? = null,
    val address: String? = null,
    val kind: String? = null,
    /** Bootstrap icon. */
    val icon: String? = null,
    val group: String? = null,
    val tags: List<String> = emptyList(),
    val state: Monitor64ServiceState? = null,
    /** View: maintenance > unreachable > worst service. */
    val view: Monitor64HostView? = null,
    val problems: Long? = null,
    val services: Long? = null,
    /** Newly discovered services. */
    @SerialName("new") val newServices: Long? = null,
    /** Vanished services. */
    val vanished: Long? = null,
    val paused: Boolean? = null,
    /** Running maintenance. */
    val maint: Monitor64OverviewHostMaint? = null,
    /** Error of the accesses if unreachable. */
    val unreach: String? = null,
    val meters: List<Monitor64Meter> = emptyList(),
    val text: String? = null,
    val cpu: Double? = null,
    val ram: Double? = null,
    @SerialName("has_cpu") val hasCpu: Boolean? = null,
    @SerialName("has_ram") val hasRam: Boolean? = null,
    /** Last round. */
    val checked: IsoInstant? = null,
)

/** Open problem on the overview. */
@Serializable
public data class Monitor64OverviewProblem(
    @SerialName("host_uid") val hostUid: String? = null,
    val host: String? = null,
    /** Service (empty = host unreachable). */
    val key: String? = null,
    val name: String? = null,
    val state: Monitor64ServiceState? = null,
    val detail: String? = null,
    val since: IsoInstant? = null,
    val acked: Boolean? = null,
    val maint: Boolean? = null,
    val unreach: Boolean? = null,
)

/** Services per state. */
@Serializable
public data class Monitor64OverviewCounts(
    val ok: Long? = null,
    val warn: Long? = null,
    val crit: Long? = null,
    val unknown: Long? = null,
)

/** Kind of a [Monitor64OverviewRecent]. */
@Serializable
public enum class Monitor64OverviewRecentKind(override val value: String) : ApiValue {
    @SerialName("ok") OK("ok"),
    @SerialName("resolved") RESOLVED("resolved"),
    @SerialName("maint") MAINT("maint"),
    @SerialName("maint_end") MAINT_END("maint_end"),
}

/** Recently resolved problem or maintenance. */
@Serializable
public data class Monitor64OverviewRecent(
    val kind: Monitor64OverviewRecentKind? = null,
    @SerialName("host_uid") val hostUid: String? = null,
    val host: String? = null,
    val name: String? = null,
    val text: String? = null,
    val by: Monitor64Actor? = null,
    val at: IsoInstant? = null,
)

/** Overview of all hosts. */
@Serializable
public data class Monitor64Overview(
    /** Services per state. */
    val counts: Monitor64OverviewCounts? = null,
    val hosts: List<Monitor64OverviewHost> = emptyList(),
    val problems: List<Monitor64OverviewProblem> = emptyList(),
    /** Recently resolved problems and maintenance (at most 8). */
    val recent: List<Monitor64OverviewRecent> = emptyList(),
    val groups: List<String> = emptyList(),
    val unreachable: Long? = null,
    val services: Long? = null,
    /** Open, not acknowledged problems. */
    @SerialName("open_unacked") val openUnacked: Long? = null,
    /** Module is running (scheduler active). */
    val running: Boolean? = null,
)

// ---- Series ---------------------------------------------------------------------------------

/** Range of `GET /monitor64/series` and `GET /monitor64/problems`. */
@Serializable
public enum class Monitor64Range(override val value: String) : ApiValue {
    @SerialName("6h") HOURS_6("6h"),
    @SerialName("24h") HOURS_24("24h"),
    @SerialName("7d") DAYS_7("7d"),
    @SerialName("30d") DAYS_30("30d"),
    @SerialName("1y") YEAR_1("1y"),
}

/** Data point of a [Monitor64SeriesEntry]. */
@Serializable
public data class Monitor64SeriesPoint(
    /** Unix seconds. */
    val t: Long? = null,
    val min: Double? = null,
    val avg: Double? = null,
    val max: Double? = null,
    /** Width of this point in seconds. */
    val s: Long? = null,
)

/** One series. */
@Serializable
public data class Monitor64SeriesEntry(
    /** `service|measure` as requested. */
    val s: String? = null,
    /** Resolution: `raw`, `5m`, `1h` (empty without data). */
    val res: String? = null,
    /** Width of the points in seconds (0 = single raw values). */
    val step: Long? = null,
    val points: List<Monitor64SeriesPoint> = emptyList(),
)

/** Piece of another node in a cluster. */
@Serializable
public data class Monitor64SeriesCluster(
    val node: String? = null,
    val name: String? = null,
    val ok: Boolean? = null,
    /** Not reachable: gap. */
    val missing: Boolean? = null,
)

/** Series of a host. */
@Serializable
public data class Monitor64Series(
    /** Unix seconds. */
    val from: Long? = null,
    /** Unix seconds. */
    val to: Long? = null,
    val series: List<Monitor64SeriesEntry> = emptyList(),
    /** Cluster: pieces of other nodes. */
    val cluster: List<Monitor64SeriesCluster> = emptyList(),
)

// ---- Connection test ------------------------------------------------------------------------

/** Type of an access. */
@Serializable
public enum class Monitor64AccessType(override val value: String) : ApiValue {
    @SerialName("agent") AGENT("agent"),
    @SerialName("ssh") SSH("ssh"),
    @SerialName("pve") PVE("pve"),
    @SerialName("pbs") PBS("pbs"),
    @SerialName("docker") DOCKER("docker"),
    @SerialName("prom") PROM("prom"),
    @SerialName("nut") NUT("nut"),
    @SerialName("truenas") TRUENAS("truenas"),
    @SerialName("unraid") UNRAID("unraid"),
    @SerialName("redfish") REDFISH("redfish"),
    @SerialName("snmp") SNMP("snmp"),
}

/** NUT TLS mode. */
@Serializable
public enum class Monitor64AccessConfigTls(override val value: String) : ApiValue {
    /** STARTTLS if offered. */
    @SerialName("") DEFAULT(""),
    @SerialName("off") OFF("off"),
    @SerialName("require") REQUIRE("require"),
}

/** TrueNAS API flavour. */
@Serializable
public enum class Monitor64AccessConfigApi(override val value: String) : ApiValue {
    /** Automatic. */
    @SerialName("") AUTO(""),
    @SerialName("jsonrpc") JSONRPC("jsonrpc"),
    @SerialName("rest") REST("rest"),
}

/** Agent mode. */
@Serializable
public enum class Monitor64AccessConfigMode(override val value: String) : ApiValue {
    /** The node queries the agent. */
    @SerialName("") PULL(""),
    /** The agent connects to the node. */
    @SerialName("push") PUSH("push"),
}

/** SNMP version. */
@Serializable
public enum class Monitor64AccessConfigSnmpVersion(override val value: String) : ApiValue {
    @SerialName("1") V1("1"),
    @SerialName("2") V2("2"),
    @SerialName("2c") V2C("2c"),
    @SerialName("3") V3("3"),
}

/** SNMPv3 security level. */
@Serializable
public enum class Monitor64AccessConfigLevel(override val value: String) : ApiValue {
    @SerialName("noAuthNoPriv") NO_AUTH_NO_PRIV("noAuthNoPriv"),
    @SerialName("authNoPriv") AUTH_NO_PRIV("authNoPriv"),
    @SerialName("authPriv") AUTH_PRIV("authPriv"),
}

/** SNMPv3 authentication protocol. */
@Serializable
public enum class Monitor64AccessConfigAuthProto(override val value: String) : ApiValue {
    @SerialName("SHA") SHA("SHA"),
    @SerialName("SHA224") SHA224("SHA224"),
    @SerialName("SHA256") SHA256("SHA256"),
    @SerialName("SHA384") SHA384("SHA384"),
    @SerialName("SHA512") SHA512("SHA512"),
    @SerialName("MD5") MD5("MD5"),
}

/** SNMPv3 encryption protocol. */
@Serializable
public enum class Monitor64AccessConfigPrivProto(override val value: String) : ApiValue {
    @SerialName("AES") AES("AES"),
    @SerialName("AES192") AES192("AES192"),
    @SerialName("AES256") AES256("AES256"),
    @SerialName("AES192C") AES192C("AES192C"),
    @SerialName("AES256C") AES256C("AES256C"),
    @SerialName("DES") DES("DES"),
}

/** Custom Prometheus metric (`metrics` of [Monitor64AccessConfig]). */
@Serializable
public data class Monitor64AccessConfigMetric(
    /** Metric name, e.g. `nginx_connections_active`. */
    val name: String,
    /** Must match exactly (`""` = label missing or empty). */
    val labels: Map<String, String>? = null,
    /** Display name (empty = name). */
    val title: String? = null,
    /** E.g. `%`, `B`, `B/s`, `°C`, `ms`. */
    val unit: String? = null,
    /** Value x factor (0 = 1). */
    val factor: Double? = null,
    /** Counter: change per second. */
    val rate: Boolean? = null,
)

/** Custom SNMP OID (`custom` of [Monitor64AccessConfig]). */
@Serializable
public data class Monitor64AccessConfigOid(
    val oid: String,
    val name: String,
    val unit: String? = null,
    /** 0 = 1. */
    val factor: Double? = null,
    /** Counter: rate per second (times factor). */
    val counter: Boolean? = null,
    /** Rule kind for thresholds, e.g. `temp.system`. */
    val rule: String? = null,
)

/**
 * Configuration of an access without secrets. Checked strictly by the server: only fields of the access type.
 * [token] is redacted in [toString].
 */
@Serializable
public data class Monitor64AccessConfig(
    /** Port (0 = default of the access type). */
    val port: Int? = null,
    val user: String? = null,
    /** SSH: SMART via sudo smartctl. */
    val smart: Boolean? = null,
    /** Address of the interface (at most 512 characters). */
    val url: String? = null,
    /** Token ID (e.g. Proxmox `user@realm!token`). */
    @SerialName("token_id") val tokenId: String? = null,
    /** Node name (Proxmox VE). */
    val node: String? = null,
    /** Fingerprint of the certificate or SSH host key. */
    val pin: String? = null,
    /** NUT: UPS names, comma list (empty = all). */
    val ups: String? = null,
    val tls: Monitor64AccessConfigTls? = null,
    val api: Monitor64AccessConfigApi? = null,
    /** Redfish: session instead of basic auth. */
    val session: Boolean? = null,
    val mode: Monitor64AccessConfigMode? = null,
    /** Agent in push mode: pointer to the access (at most 64 characters). Grants no access, but treat as a secret. */
    val token: String? = null,
    /** Prometheus only: custom metrics (at most 50). */
    val metrics: List<Monitor64AccessConfigMetric>? = null,
    @SerialName("snmp_version") val snmpVersion: Monitor64AccessConfigSnmpVersion? = null,
    val level: Monitor64AccessConfigLevel? = null,
    @SerialName("auth_proto") val authProto: Monitor64AccessConfigAuthProto? = null,
    @SerialName("priv_proto") val privProto: Monitor64AccessConfigPrivProto? = null,
    /** SNMPv3 context (usually empty). */
    val context: String? = null,
    /** SNMP: custom OIDs. */
    val custom: List<Monitor64AccessConfigOid>? = null,
    /** SNMP: fixed profiles (at most 16; empty = automatic). */
    val profiles: List<String>? = null,
    /** SNMP: accept traps of this host. */
    val traps: Boolean? = null,
    /** SNMPv3: engine ID of the trap sender (hex). */
    @SerialName("trap_engine_id") val trapEngineId: String? = null,
) {
    override fun toString(): String =
        "Monitor64AccessConfig(port=$port, user=$user, url=$url, node=$node, mode=$mode, token=<redacted>, " +
            "metrics=$metrics, snmpVersion=$snmpVersion, custom=$custom, profiles=$profiles, traps=$traps)"
}

/**
 * Create or change an access. [secret] is write-only and redacted in [toString].
 */
@Serializable
public data class Monitor64AccessInput(
    /** Access type; required when creating, empty on PUT = unchanged. */
    val type: Monitor64AccessType? = null,
    /** On PUT: missing/null = existing configuration (checked again). */
    val config: Monitor64AccessConfig? = null,
    /** Secret (token, password, key material; at most 4096 bytes, Docker 64 KiB). Empty = unchanged. */
    val secret: String? = null,
    /** Remove the stored secret. */
    @SerialName("clear_secret") val clearSecret: Boolean? = null,
    /** Agent in push mode: create a new token. */
    @SerialName("new_token") val newToken: Boolean? = null,
    /** Confirmed fingerprint of the SSH host key (`SHA256:...`); on PUT `reset` discards the stored key. */
    val hostkey: String? = null,
    /** Access on/off; missing = unchanged (new: true). */
    val enabled: Boolean? = null,
) {
    override fun toString(): String =
        "Monitor64AccessInput(type=$type, config=$config, secret=<redacted>, clearSecret=$clearSecret, " +
            "newToken=$newToken, hostkey=$hostkey, enabled=$enabled)"
}

/** Body of `POST /monitor64/test`. */
@Serializable
public data class Monitor64TestInput(
    /** Address of the host. */
    val address: String,
    val access: Monitor64AccessInput? = null,
    /** Stored access: must belong to the host with this address; empty fields in [access] (also the secret) count as stored. */
    @SerialName("access_uid") val accessUid: String? = null,
)

/** Discovered service (result of the connection test). */
@Serializable
public data class Monitor64DiscoveredService(
    /** Key of the service (at most 200 characters). */
    val key: String,
    /** Display name. */
    val name: String? = null,
    /** Kind, e.g. cpu, mem, fs, disk, if, vm, ctr. */
    val kind: String? = null,
    /** Offered by default. */
    @SerialName("default") val isDefault: Boolean? = null,
)

/** Proxmox cluster node found by the test. */
@Serializable
public data class Monitor64TestNode(
    val name: String? = null,
    val ip: String? = null,
    val id: Long? = null,
    val online: Boolean? = null,
    /** Node behind the queried address. */
    val local: Boolean? = null,
)

/** SNMP profile found by the test. */
@Serializable
public data class Monitor64TestProfile(
    val id: String? = null,
    val name: String? = null,
    /** Verified on a real device. */
    val verified: Boolean? = null,
)

/** Result of the connection test (nothing is stored). A failed test is 200 with `ok=false`. */
@Serializable
public data class Monitor64TestResult(
    val ok: Boolean? = null,
    /** Error text (also partial errors when ok). */
    val error: String? = null,
    /** Duration in milliseconds. */
    val ms: Long? = null,
    /** Fingerprint of certificate or SSH host key. */
    val fingerprint: String? = null,
    val services: List<Monitor64DiscoveredService> = emptyList(),
    /** Confirm the fingerprint first (save as `pin`/`hostkey`), then test again. */
    @SerialName("need_confirm") val needConfirm: Boolean? = null,
    /** Certificate from a known CA or pin matches. */
    val trusted: Boolean? = null,
    @SerialName("cert_subject") val certSubject: String? = null,
    val version: String? = null,
    val node: String? = null,
    val cluster: String? = null,
    /** Proxmox: nodes of the cluster. */
    val nodes: List<Monitor64TestNode> = emptyList(),
    val summary: String? = null,
    val warning: String? = null,
    /** SNMP: nas, ups, printer, switch, router, ap, server, device. */
    val device: String? = null,
    /** SNMP: detected profiles. */
    val profiles: List<Monitor64TestProfile> = emptyList(),
    /** SNMP: what is queried. */
    val queries: List<String> = emptyList(),
)

// ---- Problems -------------------------------------------------------------------------------

/** Filter `status` of the problem list. */
@Serializable
public enum class Monitor64ProblemStatus(override val value: String) : ApiValue {
    @SerialName("open") OPEN("open"),
    @SerialName("closed") CLOSED("closed"),
    @SerialName("all") ALL("all"),
}

/** State of a problem. */
@Serializable
public enum class Monitor64ProblemRowState(override val value: String) : ApiValue {
    @SerialName("ok") OK("ok"),
    @SerialName("warn") WARN("warn"),
    @SerialName("crit") CRIT("crit"),
    @SerialName("unknown") UNKNOWN("unknown"),
    @SerialName("unreachable") UNREACHABLE("unreachable"),
}

/** How a problem was closed. */
@Serializable
public enum class Monitor64ProblemRowHow(override val value: String) : ApiValue {
    @SerialName("auto") AUTO("auto"),
    @SerialName("maint") MAINT("maint"),
    @SerialName("user") USER("user"),
    @SerialName("api") API("api"),
    @SerialName("removed") REMOVED("removed"),
}

/** Problem (row). */
@Serializable
public data class Monitor64ProblemRow(
    val uid: String? = null,
    @SerialName("host_uid") val hostUid: String? = null,
    /** Service (empty = host unreachable). */
    val key: String? = null,
    val state: Monitor64ProblemRowState? = null,
    /** Worst state. */
    val worst: String? = null,
    val opened: IsoInstant? = null,
    val closed: IsoInstant? = null,
    val how: Monitor64ProblemRowHow? = null,
    @SerialName("closed_by") val closedBy: Monitor64Actor? = null,
    val ack: Monitor64Ack? = null,
    /** IDs of the channels notified (`default` = default channels). */
    val notified: List<String> = emptyList(),
)

/** Problem with names (row plus host/service names). */
@Serializable
public data class Monitor64Problem(
    val uid: String? = null,
    @SerialName("host_uid") val hostUid: String? = null,
    val key: String? = null,
    val state: Monitor64ProblemRowState? = null,
    val worst: String? = null,
    val opened: IsoInstant? = null,
    val closed: IsoInstant? = null,
    val how: Monitor64ProblemRowHow? = null,
    @SerialName("closed_by") val closedBy: Monitor64Actor? = null,
    val ack: Monitor64Ack? = null,
    val notified: List<String> = emptyList(),
    /** Name of the host (removed: UID). */
    val host: String? = null,
    /** Host removed. */
    @SerialName("host_gone") val hostGone: Boolean? = null,
    /** Name of the service. */
    val name: String? = null,
    /** Seconds (open: until now). */
    val duration: Long? = null,
    /** Names of the channels. */
    val channels: List<String> = emptyList(),
)

/** Counts of a [Monitor64ProblemList] (in the range, without the other filters). */
@Serializable
public data class Monitor64ProblemCounts(
    val open: Long? = null,
    val closed: Long? = null,
)

/** Response of `GET /monitor64/problems`. */
@Serializable
public data class Monitor64ProblemList(
    val problems: List<Monitor64Problem> = emptyList(),
    /** Involved people (close, acknowledge) for the `who` filter. */
    val people: List<Monitor64Actor> = emptyList(),
    val counts: Monitor64ProblemCounts? = null,
)

/** Response of `GET /monitor64/problems/{uid}`. */
@Serializable
public data class Monitor64ProblemDetail(
    val problem: Monitor64ProblemRow? = null,
    val events: List<Monitor64Event> = emptyList(),
    /** Channel ID to name. */
    val channels: Map<String, String> = emptyMap(),
)

/** Action on a problem. */
@Serializable
public enum class Monitor64ProblemActionAction(override val value: String) : ApiValue {
    @SerialName("ack") ACK("ack"),
    @SerialName("unack") UNACK("unack"),
    @SerialName("comment") COMMENT("comment"),
    @SerialName("resolve") RESOLVE("resolve"),
}

/** Body of `POST /monitor64/problems/action`. */
@Serializable
public data class Monitor64ProblemAction(
    /** UID of the host. */
    val host: String,
    val action: Monitor64ProblemActionAction,
    /** Service (empty = problem "unreachable" of the host). */
    val key: String? = null,
    /** At most 500 characters; required for `comment`. */
    val comment: String? = null,
)

// ---- Maintenance ----------------------------------------------------------------------------

/** Mode of a maintenance window. */
@Serializable
public enum class Monitor64MaintInputMode(override val value: String) : ApiValue {
    /** From now on. */
    @SerialName("now") NOW("now"),
    /** Start to end. */
    @SerialName("plan") PLAN("plan"),
    /** Recurring. */
    @SerialName("recur") RECUR("recur"),
}

/** Status of a maintenance window. */
@Serializable
public enum class Monitor64MaintenanceStatus(override val value: String) : ApiValue {
    @SerialName("running") RUNNING("running"),
    @SerialName("planned") PLANNED("planned"),
    @SerialName("recurring") RECURRING("recurring"),
    @SerialName("ended") ENDED("ended"),
}

/** Body of `POST /monitor64/maintenance` and `PUT /monitor64/maintenance/{uid}`; at least one host, group or tag. */
@Serializable
public data class Monitor64MaintInput(
    val mode: Monitor64MaintInputMode,
    /** Title (at most 200 characters). */
    val title: String? = null,
    /** UIDs of existing hosts. */
    val hosts: List<String>? = null,
    val groups: List<String>? = null,
    val tags: List<String>? = null,
    /** now: duration in minutes (0 = until ended; max 43200). */
    val minutes: Int? = null,
    /** plan: start `YYYY-MM-DDTHH:MM` in the time zone of the node. */
    val start: String? = null,
    /** plan: end `YYYY-MM-DDTHH:MM` (after the start). */
    val end: String? = null,
    /** recur: weekdays, 0 = Sunday; empty = every day. */
    val days: List<Int>? = null,
    /** recur: `HH:MM`. */
    val from: String? = null,
    /** recur: `HH:MM` (up to 24:00). */
    val to: String? = null,
)

/** Recurrence of a maintenance window. */
@Serializable
public data class Monitor64Recurrence(
    /** 0 = Sunday; empty = every day. */
    val days: List<Int> = emptyList(),
    /** `HH:MM`. */
    val from: String? = null,
    /** `HH:MM`. */
    val to: String? = null,
)

/** Maintenance window. */
@Serializable
public data class Monitor64Maintenance(
    val uid: String? = null,
    val title: String? = null,
    val hosts: List<String> = emptyList(),
    val groups: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val start: IsoInstant? = null,
    val end: IsoInstant? = null,
    val recur: Monitor64Recurrence? = null,
    /** Ended by hand. */
    val ended: IsoInstant? = null,
    val by: Monitor64Actor? = null,
    val at: IsoInstant? = null,
    @SerialName("changed_by") val changedBy: Monitor64Actor? = null,
    val status: Monitor64MaintenanceStatus? = null,
    /** Currently active. */
    val running: Boolean? = null,
    /** Start in the time zone of the node (`YYYY-MM-DDTHH:MM`). */
    @SerialName("start_local") val startLocal: String? = null,
    @SerialName("end_local") val endLocal: String? = null,
    @SerialName("ended_local") val endedLocal: String? = null,
)

/** Host entry of a [Monitor64MaintList]. */
@Serializable
public data class Monitor64MaintListHost(
    val uid: String? = null,
    val name: String? = null,
    val group: String? = null,
    val kind: String? = null,
)

/** Response of `GET /monitor64/maintenance`. */
@Serializable
public data class Monitor64MaintList(
    val items: List<Monitor64Maintenance> = emptyList(),
    val hosts: List<Monitor64MaintListHost> = emptyList(),
    val targets: Monitor64Targets? = null,
    /** Time zone of the node. */
    val tz: String? = null,
    /** Now in the time zone of the node. */
    @SerialName("now_local") val nowLocal: String? = null,
)

/** Response with the UID of a created or changed object. */
@Serializable
public data class Monitor64Uid(val uid: String? = null)

// ---- Agent ----------------------------------------------------------------------------------

/** Update state of an agent. */
@Serializable
public enum class Monitor64AgentHostState(override val value: String) : ApiValue {
    @SerialName("current") CURRENT("current"),
    @SerialName("pending") PENDING("pending"),
    @SerialName("unknown") UNKNOWN("unknown"),
    @SerialName("nofile") NOFILE("nofile"),
    @SerialName("elsewhere") ELSEWHERE("elsewhere"),
    @SerialName("off") OFF("off"),
}

/** State of the agent of a host. */
@Serializable
public data class Monitor64AgentHost(
    /** Host. */
    val uid: String? = null,
    val name: String? = null,
    /** UID of the agent access. */
    val access: String? = null,
    /** Push mode. */
    val push: Boolean? = null,
    val version: String? = null,
    /** E.g. `linux-amd64`. */
    val platform: String? = null,
    /** Automatic updates. */
    val auto: Boolean? = null,
    val state: Monitor64AgentHostState? = null,
    /** Update running or waiting for a free slot. */
    val busy: Boolean? = null,
    /** Error of the last attempt. */
    val error: String? = null,
    val tried: IsoInstant? = null,
    val next: IsoInstant? = null,
    /** For `elsewhere`: responsible node. */
    val node: String? = null,
    /** Available version. */
    val latest: String? = null,
    /** Access turned off. */
    val disabled: Boolean? = null,
)

/** Agent file of the distribution. */
@Serializable
public data class Monitor64AgentFile(
    val name: String? = null,
    val os: String? = null,
    val arch: String? = null,
    val size: Long? = null,
    val sha256: String? = null,
    /** E.g. `linux-amd64`. */
    val key: String? = null,
    /** Verified copy on the node. */
    val cached: Boolean? = null,
)

/** Script of the agent distribution. */
@Serializable
public data class Monitor64AgentScript(
    val name: String? = null,
    val size: Long? = null,
    val sha256: String? = null,
)

/** Source of the signed directory. */
@Serializable
public data class Monitor64AgentSource(
    val bases: List<String> = emptyList(),
    val source: String? = null,
    val checked: IsoInstant? = null,
    val ok: IsoInstant? = null,
    val error: String? = null,
)

/** Event of the agent distribution ([Monitor64Event] plus host name). */
@Serializable
public data class Monitor64AgentEvent(
    val at: IsoInstant? = null,
    val kind: String? = null,
    val text: String? = null,
    val actor: Monitor64Actor? = null,
    @SerialName("host_uid") val hostUid: String? = null,
    val key: String? = null,
    val problem: String? = null,
    val host: String? = null,
)

/** State of the agent distribution. */
@Serializable
public data class Monitor64Agent(
    /** Folder of the agent files on the internet. */
    val internet: String? = null,
    /** HTTP port of the node (0 = off). */
    @SerialName("http_port") val httpPort: Long? = null,
    /** Port of the agent. */
    val port: Long? = null,
    val running: Boolean? = null,
    /** Current version. */
    val version: String? = null,
    val date: String? = null,
    /** Source of the signed directory. */
    val source: Monitor64AgentSource? = null,
    /** The directory should be fetched again. */
    val due: Boolean? = null,
    val files: List<Monitor64AgentFile> = emptyList(),
    val scripts: List<Monitor64AgentScript> = emptyList(),
    val hosts: List<Monitor64AgentHost> = emptyList(),
    val events: List<Monitor64AgentEvent> = emptyList(),
    @SerialName("public_key") val publicKey: String? = null,
    val fingerprint: String? = null,
)
