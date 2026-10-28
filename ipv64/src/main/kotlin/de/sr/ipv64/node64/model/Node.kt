package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IsoInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** Response of `GET /health`. */
@Serializable
public data class NodeHealth(
    /** `ok` when the node is available. */
    val status: String? = null,
    /** Node version. */
    val version: String? = null,
    /** Server time. */
    val time: IsoInstant? = null,
)

/** Response of `GET /version`. */
@Serializable
public data class NodeVersion(
    /** Node version. */
    val version: String? = null,
    /** Start time of the node. */
    @SerialName("started_at") val startedAt: IsoInstant? = null,
    /** Host name. */
    val hostname: String? = null,
    /** Server time. */
    val time: IsoInstant? = null,
)

/** State of a module of the node. */
@Serializable
public data class ModuleStatus(
    /** Module name. */
    val name: String? = null,
    /** Module version (own SemVer count per module). */
    val version: String? = null,
    /** Date of the newest version in the changelog. */
    val changed: String? = null,
    /** Module is available in this build. */
    val available: Boolean? = null,
    /** Module is switched on. */
    val enabled: Boolean? = null,
    /** Module is running. */
    val running: Boolean? = null,
    /** Path of the module in the web interface. */
    val path: String? = null,
    /** Last error of the module. */
    @SerialName("last_error") val lastError: String? = null,
    /** Changelog (Markdown); only present when requested with `changelog`. */
    val changelog: String? = null,
)

/** Response of `GET /status`. */
@Serializable
public data class NodeStatus(
    /** Information about the node. */
    val node: NodeStatusNode? = null,
    /** Link with the ipv64.net account. */
    val link: Link? = null,
    /** Modules with their state. */
    val modules: List<ModuleStatus> = emptyList(),
    /** Summary per module. */
    val boards: List<NodeStatusBoard> = emptyList(),
)

/** Node information of [NodeStatus]. */
@Serializable
public data class NodeStatusNode(
    /** Node ID. */
    val id: String? = null,
    /** Node name. */
    val name: String? = null,
    /** Location. */
    val location: String? = null,
    /** Time zone. */
    val timezone: String? = null,
    /** Node version. */
    val version: String? = null,
    /** Start time. */
    @SerialName("started_at") val startedAt: IsoInstant? = null,
    /** Uptime in seconds. */
    @SerialName("uptime_seconds") val uptimeSeconds: Long? = null,
    /** Size of the database in bytes. */
    @SerialName("database_bytes") val databaseBytes: Long? = null,
    /** Current log level. */
    @SerialName("log_level") val logLevel: String? = null,
)

/** Summary of a module in [NodeStatus]. */
@Serializable
public data class NodeStatusBoard(
    /** Module name. */
    val module: String? = null,
    /** Number of items that are fine. */
    val ok: Int? = null,
    /** Number of items that are down. */
    val down: Int? = null,
    /** Number of paused items. */
    val paused: Int? = null,
    /** Number of items in another state. */
    val other: Int? = null,
)

/** Entry of the audit log. */
@Serializable
public data class AuditEntry(
    /** Entry ID. */
    val id: Long? = null,
    /** Time of the action. */
    val time: IsoInstant? = null,
    /** Actor (user, API key or `X-Agent-Actor`). */
    val actor: String? = null,
    /** Action. */
    val action: String? = null,
    /** Target of the action. */
    val target: String? = null,
    /** Details. */
    val details: String? = null,
    /** IP address of the caller. */
    val ip: String? = null,
    /** Origin of the call. */
    val origin: String? = null,
)

/** Response of `GET /logs`. */
@Serializable
public data class NodeLogs(
    /** Current log level. */
    val level: String? = null,
    /** Last log lines (structure not documented). */
    val lines: List<JsonElement> = emptyList(),
)

/** Body of `POST /backup`. [password] is redacted in [toString]. */
@Serializable
public data class BackupRequest(
    /** Password the archive is encrypted with. */
    val password: String,
    /** Also include the DNS64 query log and PROXY64 statistics. */
    val stats: Boolean? = null,
) {
    override fun toString(): String = "BackupRequest(password=<redacted>, stats=$stats)"
}

/** Response of `POST /backup/restore`. */
@Serializable
public data class RestoreResult(
    /** The node restarts. */
    val restarting: Boolean? = null,
    /** Size of the uploaded file in bytes. */
    val bytes: Long? = null,
)

/** Log level. */
@Serializable
public enum class LogLevel(override val value: String) : ApiValue {
    @SerialName("debug") DEBUG("debug"),
    @SerialName("info") INFO("info"),
    @SerialName("warn") WARN("warn"),
    @SerialName("error") ERROR("error"),
}

/** Response of `GET /logging` and `PUT /logging`. */
@Serializable
public data class LoggingConfig(
    /** Global log level. */
    val level: LogLevel? = null,
    /** Log level per module (module name to level). */
    val modules: Map<String, String> = emptyMap(),
    /** Known modules (only in `GET /logging`). */
    @SerialName("known_modules") val knownModules: List<String> = emptyList(),
)

/** Body of `PUT /logging`. */
@Serializable
public data class UpdateLoggingRequest(
    /** Global log level. */
    val level: LogLevel? = null,
    /** Minutes after which the level is reset automatically. */
    @SerialName("reset_minutes") val resetMinutes: Int? = null,
    /** Log level per module (module name to level). */
    val modules: Map<String, String>? = null,
)

/** Response of `GET /retention` and `PUT /retention`. */
@Serializable
public data class RetentionSettings(
    /** Days raw measurements are kept. */
    @SerialName("raw_days") val rawDays: Int? = null,
    /** Days hourly aggregates are kept. */
    @SerialName("hourly_days") val hourlyDays: Int? = null,
    /** Resolution of charts. */
    @SerialName("chart_detail") val chartDetail: String? = null,
    /** Days audit log entries are kept. */
    @SerialName("audit_days") val auditDays: Int? = null,
    /** Selectable values (structure not documented). */
    val options: JsonObject? = null,
)

/** Body of `PUT /retention`; only sent fields change. */
@Serializable
public data class UpdateRetentionRequest(
    /** Days raw measurements are kept. */
    @SerialName("raw_days") val rawDays: Int? = null,
    /** Days hourly aggregates are kept. */
    @SerialName("hourly_days") val hourlyDays: Int? = null,
    /** Resolution of charts. */
    @SerialName("chart_detail") val chartDetail: String? = null,
    /** Days audit log entries are kept. */
    @SerialName("audit_days") val auditDays: Int? = null,
)

/** API key of the user (without the key itself). */
@Serializable
public data class APIKey(
    /** Key ID. */
    val id: Long? = null,
    /** Name. */
    val name: String? = null,
    /** Hint (beginning of the key). */
    val hint: String? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Last use. */
    @SerialName("last_used_at") val lastUsedAt: IsoInstant? = null,
)

/** Body of `POST /keys`. */
@Serializable
public data class CreateApiKeyRequest(
    /** Name of the key. */
    val name: String,
)

/** Newly created API key (response of `POST /keys`). [key] is redacted in [toString]. */
@Serializable
public data class CreatedApiKey(
    /** Key ID. */
    val id: Long? = null,
    /** Name. */
    val name: String? = null,
    /** Hint (beginning of the key). */
    val hint: String? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** The key itself – only returned once. */
    val key: String? = null,
) {
    override fun toString(): String =
        "CreatedApiKey(id=$id, name=$name, hint=$hint, createdAt=$createdAt, key=<redacted>)"
}

/** How secrets are treated by `POST /export`. */
@Serializable
public enum class ExportSecrets(override val value: String) : ApiValue {
    @SerialName("none") NONE("none"),
    @SerialName("password") PASSWORD("password"),
}

/** Body of `POST /export`. [password] is redacted in [toString]. */
@Serializable
public data class ExportRequest(
    /** Whether secrets are left out or encrypted with [password]. */
    val secrets: ExportSecrets? = null,
    /** Password for encrypted secrets (with [ExportSecrets.PASSWORD]). */
    val password: String? = null,
) {
    override fun toString(): String =
        "ExportRequest(secrets=$secrets, password=${if (password == null) "null" else "<redacted>"})"
}

/** Response of `POST /import`. */
@Serializable
public data class ImportResult(
    /** Number of imported items. */
    val imported: Int? = null,
    /** Problems that occurred. */
    val problems: List<String> = emptyList(),
)

/** Body of `POST /reset`. [password] is redacted in [toString]. */
@Serializable
public data class ResetRequest(
    /** Password of the user. */
    val password: String,
    /** Keep the healthchecks. */
    @SerialName("keep_checks") val keepChecks: Boolean? = null,
) {
    override fun toString(): String = "ResetRequest(password=<redacted>, keepChecks=$keepChecks)"
}

/** Response of `POST /reset`. */
@Serializable
public data class ResetResult(
    /** The node restarts. */
    val restarting: Boolean? = null,
    /** Problems during the online clean-up (the reset runs anyway). */
    @SerialName("cleanup_problems") val cleanupProblems: List<String> = emptyList(),
)

/** Response of `GET /overview`. */
@Serializable
public data class NodeOverview(
    /** Node information. */
    val node: NodeOverviewNode? = null,
    /** Modules with their state. */
    val modules: List<NodeOverviewModule> = emptyList(),
    /** Network traffic. */
    val traffic: NodeOverviewTraffic? = null,
    /** Healthcheck figures. */
    val healthchecks: NodeOverviewHealthchecks? = null,
    /** ipv64 account. */
    val account: NodeOverviewAccount? = null,
    /** Update information (structure not documented). */
    val update: JsonObject? = null,
)

/** Node part of [NodeOverview]. */
@Serializable
public data class NodeOverviewNode(
    /** Node ID. */
    val id: String? = null,
    /** Node name. */
    val name: String? = null,
    /** Location. */
    val location: String? = null,
    /** Node version. */
    val version: String? = null,
    /** Start time. */
    @SerialName("started_at") val startedAt: IsoInstant? = null,
    /** Size of the database in bytes. */
    @SerialName("db_bytes") val dbBytes: Long? = null,
)

/** Module part of [NodeOverview]. */
@Serializable
public data class NodeOverviewModule(
    /** Module name. */
    val name: String? = null,
    /** Module version. */
    val version: String? = null,
    /** Module is switched on. */
    val enabled: Boolean? = null,
    /** Module is running. */
    val running: Boolean? = null,
    /** Last error. */
    @SerialName("last_error") val lastError: String? = null,
)

/** Traffic part of [NodeOverview]. */
@Serializable
public data class NodeOverviewTraffic(
    /** Traffic figures are available. */
    val available: Boolean? = null,
    /** Received bytes since start. */
    @SerialName("rx_since_start") val rxSinceStart: Long? = null,
    /** Sent bytes since start. */
    @SerialName("tx_since_start") val txSinceStart: Long? = null,
    /** Received bytes in total. */
    @SerialName("rx_total") val rxTotal: Long? = null,
    /** Sent bytes in total. */
    @SerialName("tx_total") val txTotal: Long? = null,
    /** Start of the total counters. */
    val since: IsoInstant? = null,
)

/** Healthcheck part of [NodeOverview]. */
@Serializable
public data class NodeOverviewHealthchecks(
    /** Number of checks per location. */
    @SerialName("checks_by_location") val checksByLocation: Map<String, Int> = emptyMap(),
    /** Number of nodes. */
    val nodes: Int? = null,
    /** Number of nodes that are online. */
    @SerialName("nodes_online") val nodesOnline: Int? = null,
)

/** Account part of [NodeOverview]. */
@Serializable
public data class NodeOverviewAccount(
    /** Account name. */
    val account: String? = null,
    /** Plan. */
    val plan: String? = null,
    /** Plan valid until. */
    @SerialName("plan_until") val planUntil: IsoInstant? = null,
    /** Last check of the account. */
    @SerialName("checked_at") val checkedAt: IsoInstant? = null,
    /** Last error. */
    val error: String? = null,
    /** Plan limits. */
    val limits: List<NodeOverviewLimit> = emptyList(),
)

/** Limit of the ipv64 plan. */
@Serializable
public data class NodeOverviewLimit(
    /** Limit key. */
    val key: String? = null,
    /** Used. */
    val used: Double? = null,
    /** Maximum. */
    val max: Double? = null,
    /** No limit. */
    val unlimited: Boolean? = null,
    /** Percentage used. */
    val pct: Int? = null,
)

/** Width of a dashboard tile. */
@Serializable
public enum class DashboardTileSize(override val value: String) : ApiValue {
    @SerialName("s") S("s"),
    @SerialName("m") M("m"),
    @SerialName("l") L("l"),
    @SerialName("xl") XL("xl"),
}

/** Tile of the dashboard. */
@Serializable
public data class DashboardTile(
    /** Tile ID. */
    val id: String? = null,
    /** Width. */
    val size: DashboardTileSize? = null,
    /** Tile is hidden. */
    val hidden: Boolean? = null,
)

/** Dashboard layout of the user (request and response of `/dashboard/layout`). */
@Serializable
public data class DashboardLayout(
    /** Tiles in display order. */
    val items: List<DashboardTile> = emptyList(),
)
