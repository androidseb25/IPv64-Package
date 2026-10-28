package de.sr.ipv64.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** Remote management mode of a node. */
@Serializable
public enum class Hc2RemoteMgmt(override val value: String) : ApiValue {
    @SerialName("off") OFF("off"),
    @SerialName("read") READ("read"),
    @SerialName("full") FULL("full"),
}

/** A Node64 node linked to Healthchecks 2.0. */
@Serializable
public data class Hc2Node(
    /** Node identifier (32 characters). */
    @SerialName("node_id") val nodeId: String? = null,
    /** Name. */
    val name: String? = null,
    /** Location. */
    val location: String? = null,
    /** Node version. */
    val version: String? = null,
    /** Time zone. */
    val timezone: String? = null,
    /** Enabled modules. */
    val modules: List<String> = emptyList(),
    /** Remote management mode. */
    @SerialName("remote_mgmt") val remoteMgmt: Hc2RemoteMgmt? = null,
    /** Whether the node is online. */
    val online: Boolean? = null,
    /** Last contact. */
    @SerialName("last_seen") val lastSeen: IsoInstant? = null,
    /** Last synchronisation. */
    @SerialName("last_sync") val lastSync: IsoInstant? = null,
    /** Size of the outbox. */
    @SerialName("outbox_size") val outboxSize: Int? = null,
    /** Number of sync conflicts. */
    val conflicts: Int? = null,
    /** Hub the node is connected to. */
    val hub: String? = null,
    /** Time of linking. */
    @SerialName("linked_at") val linkedAt: IsoInstant? = null,
)

/** A node including its checks. */
@Serializable
public data class Hc2NodeDetail(
    /** Node identifier (32 characters). */
    @SerialName("node_id") val nodeId: String? = null,
    /** Name. */
    val name: String? = null,
    /** Location. */
    val location: String? = null,
    /** Node version. */
    val version: String? = null,
    /** Time zone. */
    val timezone: String? = null,
    /** Enabled modules. */
    val modules: List<String> = emptyList(),
    /** Remote management mode. */
    @SerialName("remote_mgmt") val remoteMgmt: Hc2RemoteMgmt? = null,
    /** Whether the node is online. */
    val online: Boolean? = null,
    /** Last contact. */
    @SerialName("last_seen") val lastSeen: IsoInstant? = null,
    /** Last synchronisation. */
    @SerialName("last_sync") val lastSync: IsoInstant? = null,
    /** Size of the outbox. */
    @SerialName("outbox_size") val outboxSize: Int? = null,
    /** Number of sync conflicts. */
    val conflicts: Int? = null,
    /** Hub the node is connected to. */
    val hub: String? = null,
    /** Time of linking. */
    @SerialName("linked_at") val linkedAt: IsoInstant? = null,
    /** Checks of this node. */
    val checks: List<Hc2Check> = emptyList(),
)

/** A command sent to a node. */
@Serializable
public data class Hc2Command(
    /** Command ID. */
    @SerialName("command_id") val commandId: Long? = null,
    /** Node identifier. */
    @SerialName("node_id") val nodeId: String? = null,
    /** Command name. */
    val name: String? = null,
    /** Command arguments. */
    val args: JsonObject? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Completion time. */
    @SerialName("done_at") val doneAt: IsoInstant? = null,
    /** Whether the command succeeded. */
    val ok: Boolean? = null,
    /** Result (free-form). */
    val output: JsonElement? = null,
)

/** Name of a node command. */
public enum class Hc2NodeCommandName(override val value: String) : ApiValue {
    RUN_CHECK("run_check"),
    REPAIR("repair"),
    PAUSE("pause"),
    RESUME("resume"),
    RESOLVE_CONFLICT("resolve_conflict"),
}

/** Conflict resolution side for `resolve_conflict` commands. */
public enum class Hc2ConflictKeep(override val value: String) : ApiValue {
    SERVER("server"),
    LOCAL("local"),
}

/** Arguments of a node command. */
@Serializable
public data class Hc2CommandArgs(
    /** Check token (`run_check`, `pause`, `resume`). */
    val token: String? = null,
    /** Conflict number (`resolve_conflict`). */
    val id: Long? = null,
    /** `server` or `local` (`resolve_conflict`). */
    val keep: String? = null,
)

/** Request body of `POST /hc2/nodes/{node_id}/commands`. */
@Serializable
public data class Hc2NodeCommandRequest(
    /** Command: run_check, repair, pause, resume, resolve_conflict. */
    val name: String,
    /** Command arguments. */
    val args: Hc2CommandArgs? = null,
)

/** Request body of `POST /hc2/nodes/link`. */
@Serializable
public data class LinkNodeRequest(
    /** Code (8 characters) shown by the node. */
    val code: String,
    /** Location (free-form). */
    val location: String? = null,
    /** Remote management: off, read, full (default full). */
    @SerialName("remote_mgmt") val remoteMgmt: Hc2RemoteMgmt? = null,
)

/** Result of `POST /hc2/nodes/link`. */
@Serializable
public data class LinkNodeResult(
    /** Whether the link request was accepted. */
    val accepted: Boolean? = null,
    /** Details. */
    val detail: String? = null,
)

/** Request body of `PATCH /hc2/nodes/{node_id}`; only set properties are changed. */
@Serializable
public data class UpdateNodeRequest(
    /** New name. */
    val name: String? = null,
    /** New location. */
    val location: String? = null,
    /** Remote management mode. */
    @SerialName("remote_mgmt") val remoteMgmt: Hc2RemoteMgmt? = null,
)

/** Request body of `DELETE /hc2/nodes/{node_id}`. */
@Serializable
public data class UnlinkNodeRequest(
    /** Continue checks with a public target as online checks (server default true). */
    @SerialName("keep_checks") val keepChecks: Boolean? = null,
)

/** Result of `DELETE /hc2/nodes/{node_id}`. */
@Serializable
public data class UnlinkNodeResult(
    /** Whether the node was unlinked. */
    val unlinked: Boolean? = null,
    /** Number of checks continued as online checks. */
    @SerialName("checks_kept_online") val checksKeptOnline: Int? = null,
    /** Number of deleted checks. */
    @SerialName("checks_deleted") val checksDeleted: Int? = null,
)

/** Optional features announced by a node in [NodeHelloRequest]. */
@Serializable
public data class NodeFeatures(
    @SerialName("hc_sync_linked") val hcSyncLinked: Boolean? = null,
    @SerialName("hub_node_id") val hubNodeId: String? = null,
    @SerialName("honeypot_sensor_id") val honeypotSensorId: String? = null,
    @SerialName("cdn64_tunnel") val cdn64Tunnel: String? = null,
    val dyndns: Boolean? = null,
)

/** Request body of `POST /node64/hello`. */
@Serializable
public data class NodeHelloRequest(
    /** Node identifier; must match the `X-Node64-Node` header. */
    @SerialName("node_id") val nodeId: String,
    /** Name. */
    val name: String? = null,
    /** Location. */
    val location: String? = null,
    /** Node64 version. */
    val version: String? = null,
    /** OS and architecture. */
    @SerialName("os_arch") val osArch: String? = null,
    /** Operating mode. */
    val mode: String? = null,
    /** Enabled modules. */
    val modules: List<String>? = null,
    /** Announced features. */
    val features: NodeFeatures? = null,
)
