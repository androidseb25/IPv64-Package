package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IsoInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** Threshold: a number or the string `"off"` (explicitly no threshold). */
public typealias Monitor64Limit = JsonElement

/** Kind of a host (`""` = none). */
@Serializable
public enum class Monitor64HostKind(override val value: String) : ApiValue {
    @SerialName("linux-agent") LINUX_AGENT("linux-agent"),
    @SerialName("windows") WINDOWS("windows"),
    @SerialName("freebsd") FREEBSD("freebsd"),
    @SerialName("ssh") SSH("ssh"),
    @SerialName("pve") PVE("pve"),
    @SerialName("pbs") PBS("pbs"),
    @SerialName("docker") DOCKER("docker"),
    @SerialName("truenas") TRUENAS("truenas"),
    @SerialName("unraid") UNRAID("unraid"),
    @SerialName("server") SERVER("server"),
    @SerialName("ups") UPS("ups"),
    @SerialName("prom") PROM("prom"),
    @SerialName("snmp") SNMP("snmp"),
    @SerialName("nas") NAS("nas"),
    @SerialName("") NONE(""),
}

/** Category of a service on the host page. */
@Serializable
public enum class Monitor64HostServiceCategory(override val value: String) : ApiValue {
    @SerialName("system") SYSTEM("system"),
    @SerialName("fs") FS("fs"),
    @SerialName("storage") STORAGE("storage"),
    @SerialName("disks") DISKS("disks"),
    @SerialName("network") NETWORK("network"),
    @SerialName("devices") DEVICES("devices"),
    @SerialName("guests") GUESTS("guests"),
    @SerialName("docker") DOCKER("docker"),
    @SerialName("winsvc") WINSVC("winsvc"),
    @SerialName("local") LOCAL("local"),
    @SerialName("access") ACCESS("access"),
    @SerialName("other") OTHER("other"),
}

/** Direction of a numeric rule. */
@Serializable
public enum class Monitor64RuleDir(override val value: String) : ApiValue {
    /** Problem from the threshold upwards. */
    @SerialName("up") UP("up"),
    /** Problem below the threshold. */
    @SerialName("down") DOWN("down"),
}

/** Whether a rule uses percent used or GB free. */
@Serializable
public enum class Monitor64RuleMode(override val value: String) : ApiValue {
    @SerialName("used") USED("used"),
    @SerialName("free") FREE("free"),
}

/** Whether a rule notifies or only shows. */
@Serializable
public enum class Monitor64RuleReport(override val value: String) : ApiValue {
    @SerialName("notify") NOTIFY("notify"),
    @SerialName("show") SHOW("show"),
}

/** Severity of a state rule. */
@Serializable
public enum class Monitor64RuleSeverity(override val value: String) : ApiValue {
    @SerialName("crit") CRIT("crit"),
    @SerialName("warn") WARN("warn"),
    @SerialName("off") OFF("off"),
}

/** Target state of a guest or container. */
@Serializable
public enum class Monitor64ServiceSoll(override val value: String) : ApiValue {
    @SerialName("") NONE(""),
    @SerialName("run") RUN("run"),
    @SerialName("off") OFF("off"),
}

/** Reporting mode of a service (`show` = display only). */
@Serializable
public enum class Monitor64ServiceReport(override val value: String) : ApiValue {
    @SerialName("") NOTIFY(""),
    @SerialName("show") SHOW("show"),
}

/** Scope of a rule change on a service. */
@Serializable
public enum class Monitor64ServiceUpdateScope(override val value: String) : ApiValue {
    /** Only this service (default). */
    @SerialName("service") SERVICE("service"),
    /** All services of this host with this kind. */
    @SerialName("kind") KIND("kind"),
}

/** Access of a host without its secret. */
@Serializable
public data class Monitor64Access(
    val uid: String? = null,
    val type: Monitor64AccessType? = null,
    val config: Monitor64AccessConfig? = null,
    /** Remembered SSH host key (fingerprint). */
    val hostkey: String? = null,
    val enabled: Boolean? = null,
    /** A secret is stored (it is never returned). */
    @SerialName("secret_set") val secretSet: Boolean? = null,
)

/** Access on the host page with runtime state. */
@Serializable
public data class Monitor64AccessState(
    val uid: String? = null,
    val type: Monitor64AccessType? = null,
    val config: Monitor64AccessConfig? = null,
    val hostkey: String? = null,
    val enabled: Boolean? = null,
    @SerialName("secret_set") val secretSet: Boolean? = null,
    /** Display name of the access type. */
    val name: String? = null,
    /** Last query succeeded. */
    val ok: Boolean? = null,
    /** Consecutive failures. */
    val failures: Int? = null,
    val error: String? = null,
    /** Already queried. */
    val known: Boolean? = null,
    /** Only for agent accesses. */
    val agent: Monitor64AgentHost? = null,
)

/** Rule of one level; missing fields inherit from the level below. */
@Serializable
public data class Monitor64Rule(
    val warn: Monitor64Limit? = null,
    val crit: Monitor64Limit? = null,
    /** Warning in GB free (mode `free`). */
    @SerialName("warn_free") val warnFree: Monitor64Limit? = null,
    /** Critical in GB free (mode `free`). */
    @SerialName("crit_free") val critFree: Monitor64Limit? = null,
    val dir: Monitor64RuleDir? = null,
    /** Duration in seconds (0..31622400). */
    @SerialName("for") val forSec: Int? = null,
    /** Hysteresis in units of the value. */
    val hyst: Double? = null,
    val mode: Monitor64RuleMode? = null,
    val report: Monitor64RuleReport? = null,
    val severity: Monitor64RuleSeverity? = null,
)

/** Effective threshold after resolving all levels. */
@Serializable
public data class Monitor64Effective(
    val kind: String? = null,
    /** Kind is in the default table. */
    val known: Boolean? = null,
    @SerialName("is_state") val isState: Boolean? = null,
    val warn: Monitor64Limit? = null,
    val crit: Monitor64Limit? = null,
    @SerialName("warn_free") val warnFree: Monitor64Limit? = null,
    @SerialName("crit_free") val critFree: Monitor64Limit? = null,
    val dir: Monitor64RuleDir? = null,
    @SerialName("for") val forSec: Int? = null,
    val hyst: Double? = null,
    val mode: Monitor64RuleMode? = null,
    val report: Monitor64RuleReport? = null,
    val severity: Monitor64RuleSeverity? = null,
    /** Field to the level the value comes from: `default`, `override`, `host` or `service`. */
    val origin: Map<String, String> = emptyMap(),
    /** Index of the effective override, -1 = none. */
    val override: Int? = null,
    @SerialName("override_id") val overrideId: String? = null,
)

/** Effective override of a [Monitor64Levels]. */
@Serializable
public data class Monitor64LevelsOverride(
    val index: Int? = null,
    val uid: String? = null,
    /** `group` or `tag`. */
    @SerialName("target_type") val targetType: String? = null,
    val target: String? = null,
    val rule: Monitor64Rule? = null,
)

/** Levels of a threshold on a service. */
@Serializable
public data class Monitor64Levels(
    val kind: String? = null,
    /** Metric (empty = state rule). */
    val metric: String? = null,
    val state: Boolean? = null,
    val std: Monitor64Rule? = null,
    val override: Monitor64LevelsOverride? = null,
    /** This host, all services of this kind. */
    @SerialName("host_kind") val hostKind: Monitor64Rule? = null,
    /** Only this service. */
    val service: Monitor64Rule? = null,
    val eff: Monitor64Effective? = null,
)

/** Evaluation of a metric (while a duration runs: `pending*`). */
@Serializable
public data class Monitor64MetricStatus(
    val state: Monitor64ServiceState? = null,
    /** Result of the thresholds without unknown. */
    val level: Monitor64ServiceState? = null,
    val since: IsoInstant? = null,
    /** Last measurement. */
    val at: IsoInstant? = null,
    /** Evaluated value (mode free: GB free). */
    val value: Double? = null,
    val threshold: Monitor64Limit? = null,
    val pending: Monitor64ServiceState? = null,
    @SerialName("pending_since") val pendingSince: IsoInstant? = null,
    @SerialName("pending_at") val pendingAt: IsoInstant? = null,
    @SerialName("pending_limit") val pendingLimit: Monitor64Limit? = null,
)

/** A metric of a service. */
@Serializable
public data class Monitor64Metric(
    /** E.g. util, used, in, out, temp. */
    val name: String? = null,
    val value: Double? = null,
    val unit: String? = null,
    /** Rule kind (empty = display only). */
    val rule: String? = null,
    /** E.g. size in bytes. */
    val max: Double? = null,
    val status: Monitor64MetricStatus? = null,
    @SerialName("rule_kind") val ruleKind: String? = null,
)

/** A service as reported by the monitor. */
@Serializable
public data class Monitor64Service(
    val key: String? = null,
    val name: String? = null,
    val kind: String? = null,
    val state: Monitor64ServiceState? = null,
    val text: String? = null,
    val cause: String? = null,
    val via: String? = null,
    /** 1 adopted, 0 new, -1 ignored. */
    val adopted: Int? = null,
    val vanished: Boolean? = null,
    @SerialName("show_only") val showOnly: Boolean? = null,
    val acked: Boolean? = null,
    val metrics: List<Monitor64Metric> = emptyList(),
)

/** A service on the host page. */
@Serializable
public data class Monitor64HostService(
    val key: String? = null,
    val name: String? = null,
    val kind: String? = null,
    val state: Monitor64ServiceState? = null,
    val text: String? = null,
    val cause: String? = null,
    val via: String? = null,
    val adopted: Int? = null,
    val vanished: Boolean? = null,
    @SerialName("show_only") val showOnly: Boolean? = null,
    val acked: Boolean? = null,
    val metrics: List<Monitor64Metric> = emptyList(),
    val category: Monitor64HostServiceCategory? = null,
    /** Problem open since. */
    val since: IsoInstant? = null,
    val ack: Monitor64Ack? = null,
    @SerialName("custom_name") val customName: String? = null,
    val soll: String? = null,
    val report: String? = null,
    /** Guest or container with a target state. */
    val guest: Boolean? = null,
    /** Index of the first metric with a threshold (-1 = none). */
    val primary: Int? = null,
    val levels: Monitor64Levels? = null,
    /** All rule kinds of the service. */
    val rules: List<Monitor64Levels> = emptyList(),
)

/** A stored service. */
@Serializable
public data class Monitor64ServiceRow(
    val uid: String? = null,
    @SerialName("host_uid") val hostUid: String? = null,
    val key: String? = null,
    /** Custom display name. */
    val name: String? = null,
    val kind: String? = null,
    /** 1 adopted, 0 new, -1 ignored. */
    val adopted: Int? = null,
    @SerialName("vanished_at") val vanishedAt: IsoInstant? = null,
    val via: String? = null,
    /** Thresholds "only this service" (JSON). */
    @SerialName("host_rule") val hostRule: String? = null,
    val soll: Monitor64ServiceSoll? = null,
    val report: Monitor64ServiceReport? = null,
)

/** An effective maintenance window. */
@Serializable
public data class Monitor64Window(
    val id: String? = null,
    val hosts: List<String> = emptyList(),
    val groups: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val start: IsoInstant? = null,
    val end: IsoInstant? = null,
    val recurring: Monitor64Recurrence? = null,
    val reason: String? = null,
)

/** A host. */
@Serializable
public data class Monitor64Host(
    val uid: String? = null,
    val name: String? = null,
    /** Hostname, IPv4 or IPv6 (without brackets). */
    val address: String? = null,
    val kind: Monitor64HostKind? = null,
    val group: String? = null,
    val tags: List<String> = emptyList(),
    @SerialName("interval_sec") val intervalSec: Int? = null,
    /** Cluster: responsible node. */
    @SerialName("owner_node") val ownerNode: String? = null,
    /** Cluster: preferred node. */
    @SerialName("preferred_node") val preferredNode: String? = null,
    /** Cluster: backup nodes. */
    @SerialName("backup_nodes") val backupNodes: List<String> = emptyList(),
    @SerialName("agent_autoupdate") val agentAutoupdate: Boolean? = null,
    val paused: Boolean? = null,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    @SerialName("updated_at") val updatedAt: IsoInstant? = null,
)

/** A series of a host chart. */
@Serializable
public data class Monitor64HostChartSeries(
    val label: String? = null,
    val svc: String? = null,
    val metric: String? = null,
)

/** A chart of the host page (`cpu`, `ram`, `net`, `disk`). */
@Serializable
public data class Monitor64HostChart(
    val id: String? = null,
    val unit: String? = null,
    val max: Double? = null,
    val series: List<Monitor64HostChartSeries> = emptyList(),
    val warn: Double? = null,
    val crit: Double? = null,
)

/** A host with services, accesses (without secrets), new and ignored services. */
@Serializable
public data class Monitor64HostDetail(
    val host: Monitor64Host? = null,
    val icon: String? = null,
    val state: Monitor64ServiceState? = null,
    val view: Monitor64HostView? = null,
    val checked: IsoInstant? = null,
    val maint: Monitor64Window? = null,
    val unreach: String? = null,
    val services: List<Monitor64HostService> = emptyList(),
    val accesses: List<Monitor64AccessState> = emptyList(),
    /** Newly discovered, not yet adopted services. */
    val new: List<Monitor64ServiceRow> = emptyList(),
    val ignored: List<Monitor64ServiceRow> = emptyList(),
    val charts: List<Monitor64HostChart> = emptyList(),
    val running: Boolean? = null,
)

/** Fields of a host. `PUT` replaces all fields; only kind, interval and autoupdate stay when empty/0/null. */
@Serializable
public data class Monitor64HostInput(
    /** Required; at most 100 characters. */
    val name: String,
    /** Required; hostname, IPv4 or IPv6. */
    val address: String,
    /** Empty = none (create) or unchanged (update). */
    val kind: Monitor64HostKind? = null,
    val group: String? = null,
    val tags: List<String>? = null,
    /** Seconds (30..900); 0 = default (create) or unchanged (update). */
    @SerialName("interval_sec") val intervalSec: Int? = null,
    val notes: String? = null,
    val paused: Boolean? = null,
    /** Missing/`null` = unchanged (new: true). */
    @SerialName("agent_autoupdate") val agentAutoupdate: Boolean? = null,
)

/** Body of `POST /monitor64/hosts`: a host with accesses (at most 8, secrets redacted by [Monitor64AccessInput]), discovered services and services to adopt. */
@Serializable
public data class Monitor64HostCreate(
    val name: String,
    val address: String,
    val kind: Monitor64HostKind? = null,
    val group: String? = null,
    val tags: List<String>? = null,
    @SerialName("interval_sec") val intervalSec: Int? = null,
    val notes: String? = null,
    val paused: Boolean? = null,
    @SerialName("agent_autoupdate") val agentAutoupdate: Boolean? = null,
    val accesses: List<Monitor64AccessInput>? = null,
    /** Discovered services from the connection test (added as new). */
    val services: List<Monitor64DiscoveredService>? = null,
    /** Keys of the services to adopt right away. */
    val adopt: List<String>? = null,
)

/** Rule change for one kind in [Monitor64ServiceUpdate.rules]. */
@Serializable
public data class Monitor64ServiceUpdateRule(
    /** An empty rule removes the setting. */
    val rule: Monitor64Rule? = null,
    val scope: Monitor64ServiceUpdateScope? = null,
    /** Remove both settings of this host for the kind. */
    val reset: Boolean? = null,
)

/** Body of `PUT /monitor64/hosts/{uid}/service`; missing fields stay unchanged. */
@Serializable
public data class Monitor64ServiceUpdate(
    /** Key of the service. */
    val key: String,
    /** Display name (empty = detected name; at most 200 characters). */
    val name: String? = null,
    val soll: Monitor64ServiceSoll? = null,
    val report: Monitor64ServiceReport? = null,
    /** Rule kind to change of the level "this host" (at most 32). */
    val rules: Map<String, Monitor64ServiceUpdateRule>? = null,
    /** Remove all "this host" thresholds of the kinds of this service. */
    val reset: Boolean? = null,
)

/** Body of the `services/adopt` and `services/remove` requests. */
@Serializable
public data class Monitor64HostsServiceKeys(
    /** Keys of the services (1..5000). */
    val keys: List<String>,
    /** 1 adopt, -1 ignore, 0 offer as new again. */
    val state: Int? = null,
)

/** Response of `DELETE /monitor64/hosts/{uid}`. */
@Serializable
public data class Monitor64HostsDeleteResult(
    val ok: Boolean? = null,
    /** Deleted series. */
    @SerialName("series_deleted") val seriesDeleted: Int? = null,
)

/** Response of `POST /monitor64/hosts/{uid}/discover`. */
@Serializable
public data class Monitor64HostsDiscoverResult(
    /** Newly added services. */
    val added: Int? = null,
    /** Not yet adopted new services in total. */
    val new: Int? = null,
    /** Vanished services. */
    val vanished: Int? = null,
)

/** Response of adopt/remove. */
@Serializable
public data class Monitor64HostsChanged(val changed: Int? = null)

/** Body of `PUT /monitor64/hosts/{uid}/agent`. */
@Serializable
internal data class Monitor64HostsAgentAutoUpdateRequest(val autoupdate: Boolean)

/** Response of `PUT /monitor64/hosts/{uid}/agent`. */
@Serializable
public data class Monitor64HostsAgentAutoUpdate(
    val uid: String? = null,
    val autoupdate: Boolean? = null,
)

/** Response of `POST /monitor64/hosts/{uid}/agent/update`. */
@Serializable
public data class Monitor64HostsAgentUpdate(
    val ok: Boolean? = null,
    /** Target version. */
    val version: String? = null,
)

/** A variable binding of a trap. */
@Serializable
public data class Monitor64HostTrapVarbind(
    val oid: String? = null,
    val type: String? = null,
    val value: String? = null,
)

/** A received trap. */
@Serializable
public data class Monitor64HostTrap(
    val time: IsoInstant? = null,
    val version: String? = null,
    val inform: Boolean? = null,
    val user: String? = null,
    @SerialName("trap_oid") val trapOid: String? = null,
    @SerialName("trap_name") val trapName: String? = null,
    val kind: String? = null,
    /** Known trap: immediate query only. */
    val handled: Boolean? = null,
    val varbinds: List<Monitor64HostTrapVarbind> = emptyList(),
    val truncated: Boolean? = null,
)

/** Trap state of a host. */
@Serializable
public data class Monitor64HostTraps(
    /** Newest first. */
    val list: List<Monitor64HostTrap> = emptyList(),
    /** Open traps. */
    val pending: Int? = null,
    val total: Int? = null,
    val rejected: Int? = null,
    @SerialName("reject_reason") val rejectReason: String? = null,
    @SerialName("rejected_at") val rejectedAt: IsoInstant? = null,
)
