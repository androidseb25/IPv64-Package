package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IsoInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** Kind of a monitored system. */
@Serializable
public enum class PVESystemKind(override val value: String) : ApiValue {
    @SerialName("pve") PVE("pve"),
    @SerialName("docker") DOCKER("docker"),
}

/** Filter of `GET /healthchecks/proxmox/systems`. */
public enum class PVESystemKindFilter(override val value: String) : ApiValue {
    PVE("pve"), DOCKER("docker"), ALL("all"),
}

/** State of a monitored system. */
@Serializable
public enum class PVESystemState(override val value: String) : ApiValue {
    @SerialName("ok") OK("ok"),
    @SerialName("warn") WARN("warn"),
    @SerialName("down") DOWN("down"),
    @SerialName("error") ERROR("error"),
    @SerialName("off") OFF("off"),
    @SerialName("pending") PENDING("pending"),
}

/** Type of a [PVEResource]. */
@Serializable
public enum class PVEResourceType(override val value: String) : ApiValue {
    @SerialName("node") NODE("node"),
    @SerialName("qemu") QEMU("qemu"),
    @SerialName("lxc") LXC("lxc"),
    @SerialName("docker") DOCKER("docker"),
}

/** State of a [PVEResource]. */
@Serializable
public enum class PVEResourceState(override val value: String) : ApiValue {
    @SerialName("running") RUNNING("running"),
    @SerialName("stopped") STOPPED("stopped"),
    @SerialName("paused") PAUSED("paused"),
    @SerialName("suspended") SUSPENDED("suspended"),
    @SerialName("backup") BACKUP("backup"),
    @SerialName("locked") LOCKED("locked"),
    @SerialName("online") ONLINE("online"),
    @SerialName("offline") OFFLINE("offline"),
    @SerialName("unknown") UNKNOWN("unknown"),
    @SerialName("error") ERROR("error"),
}

/** Docker healthcheck state of a container. */
@Serializable
public enum class DockerHealth(override val value: String) : ApiValue {
    @SerialName("healthy") HEALTHY("healthy"),
    @SerialName("unhealthy") UNHEALTHY("unhealthy"),
    @SerialName("starting") STARTING("starting"),
}

/** Cause of a Docker container in state error. */
@Serializable
public enum class DockerErrorDetail(override val value: String) : ApiValue {
    @SerialName("unhealthy") UNHEALTHY("unhealthy"),
    @SerialName("restarting") RESTARTING("restarting"),
    @SerialName("exited") EXITED("exited"),
    @SerialName("oom") OOM("oom"),
    @SerialName("dead") DEAD("dead"),
}

/** Time range of the series endpoints. */
public enum class SeriesRange(override val value: String) : ApiValue {
    HOUR_1("1h"), HOURS_24("24h"), DAYS_7("7d"), DAYS_30("30d"),
}

/** Proxmox VE system (single node or cluster) or Docker host with state. The token secret is never returned. */
@Serializable
public data class PVESystem(
    /** ID. */
    val id: Long? = null,
    /** `pve` or `docker`. */
    val kind: PVESystemKind? = null,
    /** Name. */
    val name: String? = null,
    /** Addresses (e.g. `https://pve1.lan:8006`). */
    val addresses: List<String> = emptyList(),
    /** API token ID (e.g. `node64@pve!monitor`). */
    @SerialName("token_id") val tokenId: String? = null,
    /** Pinned certificate fingerprint (64 hex); empty = system CA. */
    @SerialName("pin_sha256") val pinSha256: String? = null,
    /** Poll interval in seconds (15 to 300). */
    val interval: Int? = null,
    /** Monitoring enabled. */
    val enabled: Boolean? = null,
    /** Creation time. */
    val created: IsoInstant? = null,
    /** Cluster wide identifier (32 hex). */
    val uid: String? = null,
    /** Node that created the system; missing = this node. */
    val home: String? = null,
    /** A secret is stored and readable. */
    @SerialName("has_secret") val hasSecret: Boolean? = null,
    /** State. */
    val state: PVESystemState? = null,
    /** Error message of the last poll. */
    val error: String? = null,
    /** Version. */
    val version: String? = null,
    /** Cluster name. */
    val cluster: String? = null,
    /** Cluster has quorum. */
    val quorate: Boolean? = null,
    /** Address used for the last poll. */
    val address: String? = null,
    /** Time of the last poll. */
    @SerialName("last_poll") val lastPoll: IsoInstant? = null,
    /** Number of nodes. */
    val nodes: Int? = null,
    /** Nodes online. */
    @SerialName("nodes_online") val nodesOnline: Int? = null,
    /** Number of guests (Docker: containers). */
    val guests: Int? = null,
    /** Guests running. */
    @SerialName("guests_running") val guestsRunning: Int? = null,
    /** Guests in a bad state. */
    @SerialName("guests_bad") val guestsBad: Int? = null,
)

/**
 * Body for creating or changing a Proxmox system or Docker host (changes are partial). [secret] is required on
 * create; an empty secret on change keeps the stored one. [secret] is redacted in [toString].
 */
@Serializable
public data class PVESystemInput(
    /** Name. */
    val name: String? = null,
    /** Addresses. */
    val addresses: List<String>? = null,
    /** API token ID. */
    @SerialName("token_id") val tokenId: String? = null,
    /** API token secret. */
    val secret: String? = null,
    /** Pinned certificate fingerprint (64 hex). */
    @SerialName("pin_sha256") val pinSha256: String? = null,
    /** Poll interval in seconds (default 30). */
    val interval: Int? = null,
    /** Monitoring enabled (default true). */
    val enabled: Boolean? = null,
) {
    override fun toString(): String =
        "PVESystemInput(name=$name, addresses=$addresses, tokenId=$tokenId, secret=<redacted>, " +
            "pinSha256=$pinSha256, interval=$interval, enabled=$enabled)"
}

/**
 * Body of `POST /healthchecks/proxmox/probe`: like [PVESystemInput] plus [systemId]. With a [systemId] and without
 * [secret] the stored secret is used. [secret] is redacted in [toString].
 */
@Serializable
public data class PVEProbeRequest(
    /** Existing system whose stored secret should be used. */
    @SerialName("system_id") val systemId: Long? = null,
    /** Name. */
    val name: String? = null,
    /** Addresses. */
    val addresses: List<String>? = null,
    /** API token ID. */
    @SerialName("token_id") val tokenId: String? = null,
    /** API token secret. */
    val secret: String? = null,
    /** Pinned certificate fingerprint (64 hex). */
    @SerialName("pin_sha256") val pinSha256: String? = null,
    /** Poll interval in seconds. */
    val interval: Int? = null,
    /** Monitoring enabled. */
    val enabled: Boolean? = null,
) {
    override fun toString(): String =
        "PVEProbeRequest(systemId=$systemId, name=$name, addresses=$addresses, tokenId=$tokenId, " +
            "secret=<redacted>, pinSha256=$pinSha256, interval=$interval, enabled=$enabled)"
}

/** Result of testing the connection to a Proxmox system. */
@Serializable
public data class PVEProbe(
    /** Connection succeeded. */
    val ok: Boolean? = null,
    /** Error message. */
    val error: String? = null,
    /** Version. */
    val version: String? = null,
    /** Cluster name. */
    val cluster: String? = null,
    /** Node names. */
    val nodes: List<String> = emptyList(),
    /** Number of guests. */
    val guests: Int? = null,
    /** SHA-256 fingerprint of the certificate. */
    @SerialName("cert_sha256") val certSha256: String? = null,
    /** Certificate subject. */
    @SerialName("cert_subject") val certSubject: String? = null,
    /** Certificate is trusted. */
    @SerialName("cert_trusted") val certTrusted: Boolean? = null,
    /** Warning about missing permissions of the token. */
    @SerialName("perm_warning") val permWarning: String? = null,
)

/** A node or guest (Docker: container) from the last poll. */
@Serializable
public data class PVEResource(
    /** `node/<name>`, `qemu/<vmid>`, `lxc/<vmid>` or `docker/<name>`. */
    val key: String? = null,
    /** Type. */
    val type: PVEResourceType? = null,
    /** Node name. */
    val node: String? = null,
    /** VM ID. */
    val vmid: Long? = null,
    /** Name. */
    val name: String? = null,
    /** State. */
    val state: PVEResourceState? = null,
    /** Raw status. */
    val status: String? = null,
    /** QMP status. */
    val qmp: String? = null,
    /** Lock. */
    val lock: String? = null,
    /** Tags. */
    val tags: List<String> = emptyList(),
    /** HA state. */
    @SerialName("ha_state") val haState: String? = null,
    /** Is a template. */
    val template: Boolean? = null,
    /** CPU share 0..1. */
    val cpu: Double? = null,
    /** Max CPUs. */
    @SerialName("max_cpu") val maxCpu: Double? = null,
    /** Memory in bytes. */
    val mem: Long? = null,
    /** Max memory in bytes. */
    @SerialName("max_mem") val maxMem: Long? = null,
    /** Disk usage in bytes. */
    val disk: Long? = null,
    /** Max disk in bytes. */
    @SerialName("max_disk") val maxDisk: Long? = null,
    /** Uptime in seconds. */
    val uptime: Long? = null,
    /** Network in, bytes/s. */
    @SerialName("net_in") val netIn: Double? = null,
    /** Network out, bytes/s. */
    @SerialName("net_out") val netOut: Double? = null,
    /** Disk read, bytes/s. */
    @SerialName("disk_read") val diskRead: Double? = null,
    /** Disk write, bytes/s. */
    @SerialName("disk_write") val diskWrite: Double? = null,
    /** Restarted. */
    val restarted: Boolean? = null,
    /** In a bad state. */
    val bad: Boolean? = null,
    /** Excluded from monitoring (template, tag/label `nomonitor`). */
    val excluded: Boolean? = null,
    /** Notifications muted. */
    val muted: Boolean? = null,
    /** Monitored as healthcheck. */
    @SerialName("as_check") val asCheck: Boolean? = null,
    /** Token of the healthcheck. */
    @SerialName("check_token") val checkToken: String? = null,
    /** Docker only: container ID (12 characters). */
    @SerialName("container_id") val containerId: String? = null,
    /** Docker only: image. */
    val image: String? = null,
    /** Docker only: container healthcheck. */
    val health: DockerHealth? = null,
    /** Docker only: restarts by Docker. */
    @SerialName("restart_count") val restartCount: Int? = null,
    /** Docker only: exit code. */
    @SerialName("exit_code") val exitCode: Int? = null,
    /** Docker only: killed by OOM. */
    @SerialName("oom_killed") val oomKilled: Boolean? = null,
    /** Docker only: creation time (Unix). */
    val created: Long? = null,
    /** Docker only: ports. */
    val ports: List<String> = emptyList(),
    /** Docker only: labels. */
    val labels: Map<String, String> = emptyMap(),
    /** Docker only, state error: cause. */
    val detail: DockerErrorDetail? = null,
)

/** Response of the `resources` endpoints. */
@Serializable
public data class PVEResources(
    /** The system. */
    val system: PVESystem? = null,
    /** Nodes and guests of the last poll. */
    val resources: List<PVEResource> = emptyList(),
)

/** A data point of a series. */
@Serializable
public data class PVEPoint(
    /** Time. */
    val t: IsoInstant? = null,
    /** CPU in percent. */
    val cpu: Double? = null,
    /** Max CPU. */
    @SerialName("cpu_max") val cpuMax: Double? = null,
    /** Memory in percent. */
    val mem: Double? = null,
    /** Disk in percent; may be null. */
    val disk: Double? = null,
    /** Network in, bytes/s. */
    @SerialName("net_in") val netIn: Double? = null,
    /** Network out, bytes/s. */
    @SerialName("net_out") val netOut: Double? = null,
    /** Disk read, bytes/s. */
    @SerialName("disk_read") val diskRead: Double? = null,
    /** Disk write, bytes/s. */
    @SerialName("disk_write") val diskWrite: Double? = null,
)

/** Response of the `series` endpoints. */
@Serializable
public data class PVESeries(
    /** Key of the node/guest/container. */
    val key: String? = null,
    /** Range. */
    val range: String? = null,
    /** Resolution in seconds. */
    @SerialName("step_seconds") val stepSeconds: Int? = null,
    /** Start. */
    val from: IsoInstant? = null,
    /** End. */
    val to: IsoInstant? = null,
    /** Data points. */
    val points: List<PVEPoint> = emptyList(),
)

/** Threshold rule: alert above [percent] for [minutes]. */
@Serializable
public data class PVEThreshold(
    /** Percent; 0 = off. */
    val percent: Double? = null,
    /** Duration in minutes. */
    val minutes: Int? = null,
)

/** Rules of one level; missing fields are inherited (system, node, guest). */
@Serializable
public data class PVERuleSet(
    /** Report crashed guests. */
    val crashed: Boolean? = null,
    /** Report stopped guests. */
    val stopped: Boolean? = null,
    /** Report paused guests. */
    val paused: Boolean? = null,
    /** Report suspended guests. */
    val suspended: Boolean? = null,
    /** Report offline nodes. */
    val offline: Boolean? = null,
    /** Report unreachable systems. */
    val unreachable: Boolean? = null,
    /** Report restarts. */
    val restarted: Boolean? = null,
    /** CPU threshold. */
    val cpu: PVEThreshold? = null,
    /** Memory threshold. */
    val mem: PVEThreshold? = null,
    /** Disk threshold. */
    val disk: PVEThreshold? = null,
    /** Report only after N polls (1 to 60). */
    val delay: Int? = null,
    /** Channel IDs: null = inherit, empty = default channels, otherwise only these. */
    val channels: List<Long>? = null,
)

/** Settings of one level (system, node, guest or container). */
@Serializable
public data class PVESettings(
    /** System ID. */
    @SerialName("system_id") val systemId: Long? = null,
    /** Key of the level; empty = system. */
    val key: String? = null,
    /** Notifications muted. */
    val muted: Boolean? = null,
    /** Monitored as healthcheck. */
    @SerialName("as_check") val asCheck: Boolean? = null,
    /** Token of the healthcheck. */
    @SerialName("check_token") val checkToken: String? = null,
    /** Expected running (null = automatic). */
    @SerialName("expect_running") val expectRunning: Boolean? = null,
    /** Has been seen running. */
    @SerialName("seen_running") val seenRunning: Boolean? = null,
    /** Rules. */
    val rules: PVERuleSet? = null,
)

/**
 * Body for setting the settings of one level (partial change). [asCheck] creates or deletes the healthcheck.
 * [expectRunning]: `JsonPrimitive(true/false)`, or `kotlinx.serialization.json.JsonNull` for automatic
 * (a Kotlin `null` means "not sent").
 */
@Serializable
public data class PVESettingsInput(
    /** Notifications muted. */
    val muted: Boolean? = null,
    /** Create/delete the healthcheck. */
    @SerialName("as_check") val asCheck: Boolean? = null,
    /** Expected running; see class doc for automatic. */
    @SerialName("expect_running") val expectRunning: JsonElement? = null,
    /** Rules. */
    val rules: PVERuleSet? = null,
)

/** Response of the `GET …/settings` endpoints. */
@Serializable
public data class PVESettingsOverview(
    /** System ID. */
    @SerialName("system_id") val systemId: Long? = null,
    /** Settings per key (`''` = system). */
    val items: Map<String, PVESettings> = emptyMap(),
    /** Effective rules per key. */
    val effective: JsonObject? = null,
    /** Default rules. */
    val defaults: JsonObject? = null,
)

/** Response of `PUT …/settings`. */
@Serializable
public data class PVESettingsResult(
    /** Saved settings. */
    val settings: PVESettings? = null,
    /** Effective rules (channels = effective channel IDs, empty = default channels). */
    val effective: JsonObject? = null,
)
