package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IsoInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** Node of the Monitor64 cluster. */
@Serializable
public data class Monitor64ClusterNode(
    val id: String? = null,
    val name: String? = null,
    @SerialName("self") val isSelf: Boolean? = null,
    val online: Boolean? = null,
    /** Reachable for 5 minutes (may become responsible). */
    val stable: Boolean? = null,
)

/** Role of this node for a host. */
@Serializable
public enum class Monitor64ClusterHostRole(override val value: String) : ApiValue {
    @SerialName("report") REPORT("report"),
    @SerialName("measure") MEASURE("measure"),
    @SerialName("idle") IDLE("idle"),
}

/** Responsibility for a host (view of this node). */
@Serializable
public data class Monitor64ClusterHost(
    val uid: String? = null,
    val name: String? = null,
    /** Responsible node. */
    val owner: String? = null,
    @SerialName("owner_name") val ownerName: String? = null,
    val preferred: String? = null,
    val backups: List<String> = emptyList(),
    /** Role of this node; `null` for values this SDK does not know. */
    val role: Monitor64ClusterHostRole? = null,
    /** Responsible node reachable. */
    val reachable: Boolean? = null,
)

/** Pending key change of an access. */
@Serializable
public data class Monitor64ClusterKeyPending(
    @SerialName("access_uid") val accessUid: String? = null,
    @SerialName("host_uid") val hostUid: String? = null,
    @SerialName("host_name") val hostName: String? = null,
    val type: String? = null,
    val tries: Int? = null,
    @SerialName("last_try") val lastTry: IsoInstant? = null,
    val error: String? = null,
    /** SSH: enter the line by hand, then confirm. */
    val manual: Boolean? = null,
)

/** Keys of the node (no private key). [authorizedKey] is treated as secret material and redacted in [toString]. */
@Serializable
public data class Monitor64ClusterKeys(
    val fingerprint: String? = null,
    /** Line for `authorized_keys` / install command (public key). */
    @SerialName("authorized_key") val authorizedKey: String? = null,
    /** Shared cluster key. */
    val cluster: Boolean? = null,
    val at: IsoInstant? = null,
    /** Fingerprints of old keys still offered. */
    @SerialName("old_keys") val oldKeys: List<String> = emptyList(),
    val pending: List<Monitor64ClusterKeyPending> = emptyList(),
    /** Change runs in the background (query again). */
    val running: Boolean? = null,
) {
    override fun toString(): String =
        "Monitor64ClusterKeys(fingerprint=$fingerprint, authorizedKey=<redacted>, cluster=$cluster, at=$at, " +
            "oldKeys=$oldKeys, pending=$pending, running=$running)"
}

/** `GET /monitor64/cluster`. */
@Serializable
public data class Monitor64Cluster(
    val active: Boolean? = null,
    @SerialName("self") val selfId: String? = null,
    val majority: Boolean? = null,
    val total: Int? = null,
    val connected: Int? = null,
    val nodes: List<Monitor64ClusterNode> = emptyList(),
    val hosts: List<Monitor64ClusterHost> = emptyList(),
    val key: Monitor64ClusterKeys? = null,
)

/** Which node monitors a host. */
@Serializable
public data class Monitor64ClusterRef(
    val node: String? = null,
    val name: String? = null,
    /** This node polls. */
    val here: Boolean? = null,
    /** The monitoring node reports. */
    val reporting: Boolean? = null,
    val reachable: Boolean? = null,
    /** `owner_unreachable`, `no_data`, `measure_only`. */
    val note: String? = null,
    val at: IsoInstant? = null,
)

/** Orphaned host (no node monitors it). */
@Serializable
public data class Monitor64Orphan(
    val uid: String? = null,
    val name: String? = null,
    val address: String? = null,
    /** `node_gone` or `no_candidate`. */
    val reason: String? = null,
    /** Previously responsible node. */
    val node: String? = null,
    @SerialName("node_name") val nodeName: String? = null,
)

/** `GET /monitor64/cluster/views`. */
@Serializable
public data class Monitor64ClusterViews(
    val active: Boolean? = null,
    @SerialName("self") val selfId: String? = null,
    @SerialName("self_name") val selfName: String? = null,
    /** Host UID to monitoring node. */
    val refs: Map<String, Monitor64ClusterRef> = emptyMap(),
    /** Only without `host`. */
    val orphans: List<Monitor64Orphan> = emptyList(),
)

/** `GET /monitor64/cluster/hosts/{uid}`. */
@Serializable
public data class Monitor64ClusterHostInfo(
    val active: Boolean? = null,
    @SerialName("self") val selfId: String? = null,
    val majority: Boolean? = null,
    val nodes: List<Monitor64ClusterNode> = emptyList(),
    val host: Monitor64ClusterHost? = null,
    val ref: Monitor64ClusterRef? = null,
    /** Latest takeovers and handovers (at most 20). */
    val events: List<Monitor64Event> = emptyList(),
)

/** Body of `PUT /monitor64/cluster/hosts/{uid}`. */
@Serializable
public data class Monitor64ClusterHostSettingInput(
    /** ID of the node (empty = this node). */
    @SerialName("preferred_node") val preferredNode: String? = null,
    /** IDs of backup nodes (at most 8). */
    @SerialName("backup_nodes") val backupNodes: List<String>? = null,
)

/** Response of `PUT /monitor64/cluster/hosts/{uid}`. */
@Serializable
public data class Monitor64ClusterHostSetting(
    val uid: String? = null,
    @SerialName("preferred_node") val preferredNode: String? = null,
    @SerialName("backup_nodes") val backupNodes: List<String> = emptyList(),
)

/** One tested access of [Monitor64ClusterReachNode]. */
@Serializable
public data class Monitor64ClusterReachAccess(
    val access: String? = null,
    val type: String? = null,
    val ok: Boolean? = null,
    val error: String? = null,
    val ms: Int? = null,
    /** Push mode: only the own node of the agent. */
    val skip: Boolean? = null,
)

/** Reachability result of one node. */
@Serializable
public data class Monitor64ClusterReachNode(
    val node: String? = null,
    val name: String? = null,
    @SerialName("self") val isSelf: Boolean? = null,
    /** All tested accesses answer. */
    val ok: Boolean? = null,
    val error: String? = null,
    val accesses: List<Monitor64ClusterReachAccess> = emptyList(),
)

/** `POST /monitor64/cluster/hosts/{uid}/reach`; this node first. */
@Serializable
public data class Monitor64ClusterReach(
    val nodes: List<Monitor64ClusterReachNode> = emptyList(),
)

/** Body of `POST /monitor64/cluster/keys/confirm`. */
@Serializable
public data class Monitor64ClusterKeyConfirmInput(
    /** UID of the access. */
    @SerialName("access_uid") val accessUid: String,
)

/** `GET /monitor64/cluster/orphans`. */
@Serializable
public data class Monitor64ClusterOrphans(
    val active: Boolean? = null,
    @SerialName("self") val selfId: String? = null,
    val orphans: List<Monitor64Orphan> = emptyList(),
)

/** Body of the orphan assign/remove calls; `uids` or `all` is required. */
@Serializable
public data class Monitor64OrphanInput(
    val uids: List<String>? = null,
    /** All orphaned hosts. */
    val all: Boolean? = null,
)

/** Result of the orphan assign/remove calls. */
@Serializable
public data class Monitor64OrphanResult(
    /** Handled hosts. */
    val done: Int? = null,
    /** Remaining orphaned hosts. */
    val orphans: List<Monitor64Orphan> = emptyList(),
)

// ---- rules ----

/** Rule kind. */
@Serializable
public data class Monitor64RuleKind(
    val kind: String? = null,
    /** system, storage, disks, network, devices, fixed. */
    val group: String? = null,
    val unit: String? = null,
    /** Display = value / factor. */
    val factor: Double? = null,
    /** Fill level: also "GB free". */
    val store: Boolean? = null,
    /** State rule. */
    val state: Boolean? = null,
    /** Threshold downwards. */
    val down: Boolean? = null,
    /** Without duration. */
    val nofor: Boolean? = null,
)

/** Effect of an override on the current state (without duration and hysteresis). */
@Serializable
public data class Monitor64EffectLimits(
    val warn: Int? = null,
    val crit: Int? = null,
)

/** Effect of an override on the current state. */
@Serializable
public data class Monitor64Effect(
    /** Affected services. */
    val services: Int? = null,
    /** Names of affected hosts. */
    val hosts: List<String> = emptyList(),
    /** Hosts where the threshold is set directly. */
    val own: List<String> = emptyList(),
    /** Index of an override further up to hosts where it wins. */
    val shadow: Map<String, List<String>> = emptyMap(),
    val before: Monitor64EffectLimits? = null,
    val after: Monitor64EffectLimits? = null,
)

/** Row of an override list (rules and notify). */
@Serializable
public data class Monitor64OverrideRow(
    val uid: String? = null,
    /** Order (top wins). */
    val position: Int? = null,
    /** `group` or `tag`. */
    @SerialName("target_type") val targetType: String? = null,
    val target: String? = null,
    /** Rule kind (thresholds only). */
    val kind: String? = null,
    /** Stored rule or notification (JSON). */
    val data: JsonObject? = null,
    val by: Monitor64Actor? = null,
    val at: IsoInstant? = null,
)

/** Threshold override of a group or tag (allOf OverrideRow + rule + effect). */
@Serializable
public data class Monitor64RuleOverride(
    val uid: String? = null,
    val position: Int? = null,
    @SerialName("target_type") val targetType: String? = null,
    val target: String? = null,
    val kind: String? = null,
    val data: JsonObject? = null,
    val by: Monitor64Actor? = null,
    val at: IsoInstant? = null,
    val rule: Monitor64Rule? = null,
    val effect: Monitor64Effect? = null,
)

/** Rule set directly at a host. */
@Serializable
public data class Monitor64RulesOwn(
    @SerialName("host_uid") val hostUid: String? = null,
    val host: String? = null,
    /** Empty = all services of the host with this kind. */
    val key: String? = null,
    val name: String? = null,
    val kind: String? = null,
    val rule: Monitor64Rule? = null,
)

/** `GET /monitor64/rules`. */
@Serializable
public data class Monitor64Rules(
    val kinds: List<Monitor64RuleKind> = emptyList(),
    /** Built-in default per kind. */
    val defaults: Map<String, Monitor64Rule> = emptyMap(),
    /** Effective default table (built-in plus changes). */
    val std: Map<String, Monitor64Rule> = emptyMap(),
    val overrides: List<Monitor64RuleOverride> = emptyList(),
    /** Set at the host. */
    val own: List<Monitor64RulesOwn> = emptyList(),
    val targets: Monitor64Targets? = null,
)

/** Body of `PUT /monitor64/rules/std`. */
@Serializable
public data class Monitor64RulesStdInput(
    /** Kind to rule (fields override the built-in default). */
    val rules: Map<String, Monitor64Rule>? = null,
)

/** Response of `PUT /monitor64/rules/std`. */
@Serializable
public data class Monitor64RulesStdResult(
    val ok: Boolean? = null,
    /** Stored kinds. */
    val changed: List<String> = emptyList(),
)

/** Body of the threshold override create/update calls. */
@Serializable
public data class Monitor64RuleOverrideInput(
    /** `group` or `tag`. */
    @SerialName("target_type") val targetType: String,
    /** Group or tag (at most 64 characters; tags lower case). */
    val target: String,
    /** Rule kind from the default table. */
    val kind: String,
    /** At least one field (else 400 `rule_empty`). */
    val rule: Monitor64Rule,
)

/** Body of `POST /monitor64/rules/preview`; [uid] set = replaces this override. */
@Serializable
public data class Monitor64RulePreviewInput(
    @SerialName("target_type") val targetType: String,
    val target: String,
    val kind: String,
    val rule: Monitor64Rule,
    /** Existing override that is replaced. */
    val uid: String? = null,
)

/** Body of the override move calls. */
@Serializable
public data class Monitor64OverrideMoveInput(
    /** -1 up, 1 down. */
    val dir: Int,
)

// ---- notify ----

/** Notification channel of the node (no configuration, no secrets). */
@Serializable
public data class Monitor64Channel(
    val id: Int? = null,
    val type: String? = null,
    val name: String? = null,
    val enabled: Boolean? = null,
    /** Default channel. */
    @SerialName("default") val isDefault: Boolean? = null,
)

/** Channel with severity; entries without warn and crit are dropped. */
@Serializable
public data class Monitor64ChannelRoute(
    /** `default` = default channels of Node64, else channel ID as text. */
    val id: String,
    val warn: Boolean? = null,
    val crit: Boolean? = null,
)

/** Base notification setting (body of `PUT /monitor64/notify`). */
@Serializable
public data class Monitor64NotifyConfig(
    val channels: List<Monitor64ChannelRoute>? = null,
    /** Reminder on warning in seconds (0 = never, else at least 3600). */
    @SerialName("remind_warn") val remindWarn: Int? = null,
    /** Reminder on critical in seconds (0 = never, else at least 3600). */
    @SerialName("remind_crit") val remindCrit: Int? = null,
    /** Critical immediately even in quiet time. */
    @SerialName("crit_in_quiet") val critInQuiet: Boolean? = null,
)

/** Notification override data; missing fields inherit. */
@Serializable
public data class Monitor64NotifyOverrideData(
    val channels: List<Monitor64ChannelRoute> = emptyList(),
    /** true: channels instead, false: additionally. */
    val replace: Boolean? = null,
    @SerialName("only_crit") val onlyCrit: Boolean? = null,
    @SerialName("crit_in_quiet") val critInQuiet: Boolean? = null,
    @SerialName("remind_warn") val remindWarn: Int? = null,
    @SerialName("remind_crit") val remindCrit: Int? = null,
)

/** Notification override of a group or tag (allOf OverrideRow + notify). */
@Serializable
public data class Monitor64NotifyOverride(
    val uid: String? = null,
    val position: Int? = null,
    @SerialName("target_type") val targetType: String? = null,
    val target: String? = null,
    val kind: String? = null,
    val data: JsonObject? = null,
    val by: Monitor64Actor? = null,
    val at: IsoInstant? = null,
    val notify: Monitor64NotifyOverrideData? = null,
)

/** Quiet time of the node (read only). */
@Serializable
public data class Monitor64NotifyQuiet(
    val enabled: Boolean? = null,
    val from: String? = null,
    val to: String? = null,
    val days: String? = null,
)

/** `GET /monitor64/notify`. */
@Serializable
public data class Monitor64Notify(
    val config: Monitor64NotifyConfig? = null,
    val channels: List<Monitor64Channel> = emptyList(),
    val overrides: List<Monitor64NotifyOverride> = emptyList(),
    val quiet: Monitor64NotifyQuiet? = null,
    val targets: Monitor64Targets? = null,
)

/** Body of the notification override create/update calls; null fields inherit. */
@Serializable
public data class Monitor64NotifyOverrideInput(
    /** `group` or `tag`. */
    @SerialName("target_type") val targetType: String,
    /** Group or tag (at most 64 characters; tags lower case). */
    val target: String,
    val channels: List<Monitor64ChannelRoute>? = null,
    /** true: channels instead of the base setting, false: additionally. */
    val replace: Boolean? = null,
    @SerialName("only_crit") val onlyCrit: Boolean? = null,
    @SerialName("crit_in_quiet") val critInQuiet: Boolean? = null,
    @SerialName("remind_warn") val remindWarn: Int? = null,
    @SerialName("remind_crit") val remindCrit: Int? = null,
)

/** Response of `POST /monitor64/notify/test`. */
@Serializable
public data class Monitor64NotifyTestResult(
    val ok: Boolean? = null,
    /** Number of channels. */
    val channels: Int? = null,
)
