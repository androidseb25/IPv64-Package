package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.internal.query
import de.sr.ipv64.node64.model.APIKey
import de.sr.ipv64.node64.model.AuditEntry
import de.sr.ipv64.node64.model.BackupRequest
import de.sr.ipv64.node64.model.CreateApiKeyRequest
import de.sr.ipv64.node64.model.CreatedApiKey
import de.sr.ipv64.node64.model.DashboardLayout
import de.sr.ipv64.node64.model.ExportRequest
import de.sr.ipv64.node64.model.ImportResult
import de.sr.ipv64.node64.model.LoggingConfig
import de.sr.ipv64.node64.model.ModuleStatus
import de.sr.ipv64.node64.model.NodeHealth
import de.sr.ipv64.node64.model.NodeLogs
import de.sr.ipv64.node64.model.NodeOverview
import de.sr.ipv64.node64.model.NodeStatus
import de.sr.ipv64.node64.model.NodeVersion
import de.sr.ipv64.node64.model.ResetRequest
import de.sr.ipv64.node64.model.ResetResult
import de.sr.ipv64.node64.model.RestoreResult
import de.sr.ipv64.node64.model.RetentionSettings
import de.sr.ipv64.node64.model.UpdateLoggingRequest
import de.sr.ipv64.node64.model.UpdateRetentionRequest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** Tag *Node*: status, modules, metrics, settings, audit, logs, backups, API keys, import/export. */
public class NodeApi internal constructor(private val transport: HttpTransport) {

    /** Availability of the node (no login needed, e.g. for a Docker HEALTHCHECK). `GET /health` */
    public suspend fun health(): NodeHealth =
        transport.json(Method.GET, ApiPath("health"))

    /** Version and start time. `GET /version` */
    public suspend fun version(): NodeVersion =
        transport.json(Method.GET, ApiPath("version"))

    /** Dashboard data: node, modules, summary per module, link. `GET /status` */
    public suspend fun status(): NodeStatus =
        transport.json(Method.GET, ApiPath("status"))

    /**
     * Modules with their state.
     *
     * @param changelog also return the changelog (Markdown) of each module.
     *
     * `GET /modules`
     */
    public suspend fun modules(changelog: Boolean = false): List<ModuleStatus> =
        transport.json(Method.GET, ApiPath("modules"), query("changelog" to if (changelog) "1" else null))

    /**
     * Enables a module (runs immediately, no restart). Response body is not documented by the API.
     * `POST /modules/{name}/enable`
     */
    public suspend fun enableModule(name: String): JsonElement? =
        transport.jsonOrNull(Method.POST, ApiPath("modules", name, "enable"))

    /** Disables a module. Response body is not documented by the API. `POST /modules/{name}/disable` */
    public suspend fun disableModule(name: String): JsonElement? =
        transport.jsonOrNull(Method.POST, ApiPath("modules", name, "disable"))

    /**
     * Metrics in Prometheus text format. Create a dedicated API key for Prometheus and use it as Bearer token.
     * `GET /metrics`
     */
    public suspend fun metrics(): String =
        transport.text(Method.GET, ApiPath("metrics"), headers = mapOf("Accept" to "text/plain"))

    /**
     * Settings as key/value pairs (`node.name`, `node.location`, `node.timezone`, `node.base_url`,
     * `notify.quiet_enabled`, `notify.quiet_from`, `notify.quiet_to`, `notify.quiet_days`, `notify.quiet_tz`,
     * `notify.rate_per_hour`, `notify.batch_threshold`, `notify.batch_window_seconds`, `updates.check`).
     * `GET /settings`
     */
    public suspend fun settings(): Map<String, String> =
        transport.json(Method.GET, ApiPath("settings"))

    /**
     * Changes settings; only the sent keys change. Renaming (`node.name` 1-64 characters, `node.location`)
     * is propagated to ipv64 and the other nodes. `PUT /settings`
     */
    public suspend fun updateSettings(settings: Map<String, String>): Map<String, String> =
        transport.json(Method.PUT, ApiPath("settings"), body = transport.body(settings))

    /**
     * Audit log, newest first.
     *
     * @param limit maximum number of entries.
     * @param before only entries with an ID lower than this one (paging).
     *
     * `GET /audit`
     */
    public suspend fun audit(limit: Int? = null, before: Long? = null): List<AuditEntry> =
        transport.json(Method.GET, ApiPath("audit"), query("limit" to limit, "before" to before))

    /** Last log lines. `GET /logs` */
    public suspend fun logs(lines: Int? = null): NodeLogs =
        transport.json(Method.GET, ApiPath("logs"), query("lines" to lines))

    /**
     * Database only (SQLite file, without the node's keys). For a complete backup use [backup].
     * `GET /backup`
     */
    public suspend fun downloadDatabase(): ByteArray =
        transport.bytes(Method.GET, ApiPath("backup"), headers = mapOf("Accept" to "application/vnd.sqlite3"))

    /**
     * Complete backup as encrypted archive (`.n64backup`): database, node keys, DNS64 shares and certificate files.
     * `POST /backup`
     */
    public suspend fun backup(request: BackupRequest): ByteArray =
        transport.bytes(
            Method.POST,
            ApiPath("backup"),
            body = transport.body(request),
            headers = mapOf("Accept" to "application/octet-stream"),
        )

    /**
     * Restores a backup (the node restarts). Sends the archive or database raw as `application/octet-stream`.
     * Maximum 1 GB.
     *
     * @param data the `.n64backup` archive or database file.
     * @param password password of the archive (header `X-Backup-Password`).
     *
     * `POST /backup/restore`
     */
    public suspend fun restore(data: ByteArray, password: String? = null): RestoreResult =
        transport.json(
            Method.POST,
            ApiPath("backup", "restore"),
            body = data.toRequestBody("application/octet-stream".toMediaType()),
            headers = if (password == null) emptyMap() else mapOf("X-Backup-Password" to password),
        )

    /**
     * Restores a backup like [restore], but as `multipart/form-data` (fields `file` and `password`).
     * `POST /backup/restore`
     */
    public suspend fun restoreMultipart(
        data: ByteArray,
        fileName: String = "backup.n64backup",
        password: String? = null,
    ): RestoreResult {
        val multipart = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", fileName, data.toRequestBody("application/octet-stream".toMediaType()))
            .apply { if (password != null) addFormDataPart("password", password) }
            .build()
        return transport.json(Method.POST, ApiPath("backup", "restore"), body = multipart)
    }

    /** The OpenAPI description of the node. `GET /openapi.json` */
    public suspend fun openApi(): JsonObject =
        transport.json(Method.GET, ApiPath("openapi.json"))

    /** Log level (global and per module). `GET /logging` */
    public suspend fun logging(): LoggingConfig =
        transport.json(Method.GET, ApiPath("logging"))

    /** Changes the log level. `PUT /logging` */
    public suspend fun updateLogging(request: UpdateLoggingRequest): LoggingConfig =
        transport.json(Method.PUT, ApiPath("logging"), body = transport.body(request))

    /** Retention of measurements and chart resolution. `GET /retention` */
    public suspend fun retention(): RetentionSettings =
        transport.json(Method.GET, ApiPath("retention"))

    /** Changes the retention; only sent fields change. `PUT /retention` */
    public suspend fun updateRetention(request: UpdateRetentionRequest): RetentionSettings =
        transport.json(Method.PUT, ApiPath("retention"), body = transport.body(request))

    /** API keys of the user (without the key itself). `GET /keys` */
    public suspend fun listApiKeys(): List<APIKey> =
        transport.json(Method.GET, ApiPath("keys"))

    /** Creates an API key. The key is only contained in this response. `POST /keys` */
    public suspend fun createApiKey(name: String): CreatedApiKey =
        transport.json(Method.POST, ApiPath("keys"), body = transport.body(CreateApiKeyRequest(name)))

    /** Revokes an API key. `DELETE /keys/{id}` */
    public suspend fun deleteApiKey(id: Long) {
        transport.unit(Method.DELETE, ApiPath("keys", id.toString()))
    }

    /**
     * Exports the configuration (settings, channels, modules) as a JSON document that can be passed to [importConfig].
     * `POST /export`
     */
    public suspend fun exportConfig(request: ExportRequest = ExportRequest()): JsonObject =
        transport.json(Method.POST, ApiPath("export"), body = transport.body(request))

    /**
     * Imports a configuration created by [exportConfig]. With a wrong password nothing is imported.
     *
     * @param config the document returned by [exportConfig].
     * @param password password for encrypted secrets (header `X-Config-Password`).
     *
     * `POST /import`
     */
    public suspend fun importConfig(config: JsonObject, password: String? = null): ImportResult =
        transport.json(
            Method.POST,
            ApiPath("import"),
            body = transport.body(config),
            headers = if (password == null) emptyMap() else mapOf("X-Config-Password" to password),
        )

    /**
     * Factory reset (the node restarts and clears its data directory). Needs the user's password.
     * `POST /reset`
     */
    public suspend fun reset(request: ResetRequest): ResetResult =
        transport.json(Method.POST, ApiPath("reset"), body = transport.body(request))

    /** Dashboard figures: node, modules, traffic, healthchecks, ipv64 account, update. `GET /overview` */
    public suspend fun overview(): NodeOverview =
        transport.json(Method.GET, ApiPath("overview"))

    /** Dashboard layout of the user. `GET /dashboard/layout` */
    public suspend fun dashboardLayout(): DashboardLayout =
        transport.json(Method.GET, ApiPath("dashboard", "layout"))

    /** Saves the dashboard layout (per user). `PUT /dashboard/layout` */
    public suspend fun updateDashboardLayout(layout: DashboardLayout): DashboardLayout =
        transport.json(Method.PUT, ApiPath("dashboard", "layout"), body = transport.body(layout))

    /** Resets the dashboard to the default. `DELETE /dashboard/layout` */
    public suspend fun resetDashboardLayout() {
        transport.unit(Method.DELETE, ApiPath("dashboard", "layout"))
    }
}
