package de.sr.ipv64.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** Type of a healthcheck. */
@Serializable
public enum class HealthcheckType(override val value: String) : ApiValue {
    @SerialName("push") PUSH("push"),
    @SerialName("icmp") ICMP("icmp"),
    @SerialName("tcp") TCP("tcp"),
    @SerialName("http") HTTP("http"),
    @SerialName("keyword") KEYWORD("keyword"),
}

/** Status of a healthcheck. */
@Serializable
public enum class HealthcheckStatus(override val value: String) : ApiValue {
    @SerialName("new") NEW("new"),
    @SerialName("up") UP("up"),
    @SerialName("paused") PAUSED("paused"),
    @SerialName("warning") WARNING("warning"),
    @SerialName("down") DOWN("down"),
    @SerialName("stabilizing") STABILIZING("stabilizing"),
}

/** Time range of healthcheck statistics. */
public enum class StatsRange(override val value: String) : ApiValue {
    HOUR_1("1h"), HOURS_24("24h"), DAYS_7("7d"), DAYS_30("30d"),
}

/** A healthcheck event. */
@Serializable
public data class HealthcheckEvent(
    /** Event ID, also the cursor. */
    val id: Long? = null,
    /** Time. */
    val time: IsoInstant? = null,
    /** Status. */
    val status: HealthcheckStatus? = null,
    /** Message. */
    val message: String? = null,
    /** Response time in ms. */
    @SerialName("latency_ms") val latencyMs: Double? = null,
    /** Additional ping data. */
    val payload: JsonObject? = null,
)

/** Notification settings of a healthcheck. */
@Serializable
public data class HealthcheckNotify(
    /** Notify on DOWN. */
    @SerialName("on_down") val onDown: Boolean? = null,
    /** Notify on UP. */
    @SerialName("on_up") val onUp: Boolean? = null,
    /** Integration IDs, 0 = account e-mail. */
    val integrations: List<Long> = emptyList(),
)

/** Push settings of a healthcheck (type `push` only). */
@Serializable
public data class HealthcheckPush(
    /** URL your system pings. Contains the check token, treat as secret. */
    val url: String? = null,
    /** Expected ping interval in seconds. */
    @SerialName("period_seconds") val periodSeconds: Int? = null,
    /** Grace time in seconds. */
    @SerialName("grace_seconds") val graceSeconds: Int? = null,
    /** Latest time for the next ping. */
    @SerialName("expected_by") val expectedBy: IsoInstant? = null,
)

/** Settings of an active check (icmp, tcp, http, keyword). */
@Serializable
public data class HealthcheckCheck(
    /** Target for icmp and tcp. */
    val target: String? = null,
    /** Prefer IPv4. */
    @SerialName("prefer_ipv4") val preferIpv4: Boolean? = null,
    /** Port for tcp. */
    val port: Int? = null,
    /** URL for http and keyword. */
    val url: String? = null,
    /** Verify the TLS certificate. */
    @SerialName("verify_tls") val verifyTls: Boolean? = null,
    /** Search keyword for keyword checks. */
    val keyword: String? = null,
    /** Check interval in seconds. */
    @SerialName("interval_seconds") val intervalSeconds: Int? = null,
    /** Interval after a failure in seconds. */
    @SerialName("retry_interval_seconds") val retryIntervalSeconds: Int? = null,
    /** Failures until alarm. */
    @SerialName("failures_before_alarm") val failuresBeforeAlarm: Int? = null,
    /** Next check. */
    @SerialName("next_check_at") val nextCheckAt: IsoInstant? = null,
)

/** A healthcheck. */
@Serializable
public data class Healthcheck(
    /** Healthcheck token, identifies the check. */
    val token: String? = null,
    /** Name. */
    val name: String? = null,
    /** Type. */
    val type: HealthcheckType? = null,
    /** Status. */
    val status: HealthcheckStatus? = null,
    /** Notification settings. */
    val notify: HealthcheckNotify? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Last ping or check. */
    @SerialName("last_seen_at") val lastSeenAt: IsoInstant? = null,
    /** Last alarm. */
    @SerialName("last_alarm_at") val lastAlarmAt: IsoInstant? = null,
    /** Number of pings. */
    @SerialName("pings_total") val pingsTotal: Long? = null,
    /** Consecutive failures. */
    @SerialName("failed_in_row") val failedInRow: Int? = null,
    /** Push settings, only for type push. */
    val push: HealthcheckPush? = null,
    /** Active check settings, `null` for push or if not configured yet. */
    val check: HealthcheckCheck? = null,
)

/** A healthcheck including its most recent events. */
@Serializable
public data class HealthcheckDetail(
    /** Healthcheck token, identifies the check. */
    val token: String? = null,
    /** Name. */
    val name: String? = null,
    /** Type. */
    val type: HealthcheckType? = null,
    /** Status. */
    val status: HealthcheckStatus? = null,
    /** Notification settings. */
    val notify: HealthcheckNotify? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Last ping or check. */
    @SerialName("last_seen_at") val lastSeenAt: IsoInstant? = null,
    /** Last alarm. */
    @SerialName("last_alarm_at") val lastAlarmAt: IsoInstant? = null,
    /** Number of pings. */
    @SerialName("pings_total") val pingsTotal: Long? = null,
    /** Consecutive failures. */
    @SerialName("failed_in_row") val failedInRow: Int? = null,
    /** Push settings, only for type push. */
    val push: HealthcheckPush? = null,
    /** Active check settings, `null` for push or if not configured yet. */
    val check: HealthcheckCheck? = null,
    /** The last 20 events. */
    val recent: List<HealthcheckEvent> = emptyList(),
)

/** Status of an incident. */
@Serializable
public enum class IncidentStatus(override val value: String) : ApiValue {
    @SerialName("open") OPEN("open"),
    @SerialName("closed") CLOSED("closed"),
}

/** An entry of an incident. */
@Serializable
public data class IncidentEvent(
    /** Time. */
    val time: IsoInstant? = null,
    /** Kind. */
    val kind: String? = null,
    /** Message. */
    val message: String? = null,
)

/** A healthcheck incident. */
@Serializable
public data class Incident(
    /** ID. */
    val id: Long? = null,
    /** Status. */
    val status: IncidentStatus? = null,
    /** Start. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** End. */
    @SerialName("closed_at") val closedAt: IsoInstant? = null,
    /** Events of the incident. */
    val events: List<IncidentEvent> = emptyList(),
)

/** Latency summary in ms. */
@Serializable
public data class HealthcheckLatency(
    /** Average. */
    val avg: Double? = null,
    /** Minimum. */
    val min: Double? = null,
    /** Maximum. */
    val max: Double? = null,
)

/** One point of the statistics time series. */
@Serializable
public data class HealthcheckSeriesPoint(
    /** Start of the interval. */
    val time: IsoInstant? = null,
    /** Measurements. */
    val measurements: Int? = null,
    /** Availability in percent. */
    @SerialName("availability_percent") val availabilityPercent: Double? = null,
    /** Average latency in ms. */
    @SerialName("latency_avg_ms") val latencyAvgMs: Double? = null,
    /** Maximum latency in ms. */
    @SerialName("latency_max_ms") val latencyMaxMs: Double? = null,
)

/** Availability and latency of a healthcheck. */
@Serializable
public data class HealthcheckStats(
    /** Time range. */
    val range: String? = null,
    /** Start. */
    val from: IsoInstant? = null,
    /** End. */
    val to: IsoInstant? = null,
    /** Number of measurements. */
    val measurements: Int? = null,
    /** Availability in percent. */
    @SerialName("availability_percent") val availabilityPercent: Double? = null,
    /** Latency summary. */
    @SerialName("latency_ms") val latencyMs: HealthcheckLatency? = null,
    /** Width of a time series point in seconds. */
    @SerialName("bucket_seconds") val bucketSeconds: Int? = null,
    /** Time series. */
    val series: List<HealthcheckSeriesPoint> = emptyList(),
)

/** Request body of `POST /healthchecks`. */
@Serializable
public data class CreateHealthcheckRequest(
    /** Name. */
    val name: String,
    /** Type; the web UI names `health`, `ping`, `web` are also accepted by the server. */
    val type: HealthcheckType? = null,
    /** Integration IDs for notifications, 0 = account e-mail. */
    val integrations: List<Long>? = null,
    /** Notify on DOWN. */
    @SerialName("notify_on_down") val notifyOnDown: Boolean? = null,
    /** Notify on UP. */
    @SerialName("notify_on_up") val notifyOnUp: Boolean? = null,
    /** push: expected ping interval (min. 60, whole minutes, hours or days). */
    @SerialName("period_seconds") val periodSeconds: Int? = null,
    /** push: grace time (min. 60, whole minutes, hours or days). */
    @SerialName("grace_seconds") val graceSeconds: Int? = null,
    /** icmp and tcp: host name or IP. */
    val target: String? = null,
    /** icmp and tcp: prefer IPv4. */
    @SerialName("prefer_ipv4") val preferIpv4: Boolean? = null,
    /** tcp: port (1 to 65535). */
    val port: Int? = null,
    /** http and keyword: URL. */
    val url: String? = null,
    /** http and keyword: verify the certificate. */
    @SerialName("verify_tls") val verifyTls: Boolean? = null,
    /** keyword: text that must appear on the page. */
    val keyword: String? = null,
    /** Active checks: interval (1 to 86400, free plan min. 120). */
    @SerialName("interval_seconds") val intervalSeconds: Int? = null,
    /** Active checks: interval after a failure (10 to 86400). */
    @SerialName("retry_interval_seconds") val retryIntervalSeconds: Int? = null,
    /** Active checks: failures until alarm (1 to 60). */
    @SerialName("failures_before_alarm") val failuresBeforeAlarm: Int? = null,
)

/** Request body of `PATCH /healthchecks/{token}`; `null` fields stay unchanged. Changing [type] resets the check settings. */
@Serializable
public data class UpdateHealthcheckRequest(
    /** Name. */
    val name: String? = null,
    /** Type. */
    val type: HealthcheckType? = null,
    /** Integration IDs for notifications, 0 = account e-mail. */
    val integrations: List<Long>? = null,
    /** Notify on DOWN. */
    @SerialName("notify_on_down") val notifyOnDown: Boolean? = null,
    /** Notify on UP. */
    @SerialName("notify_on_up") val notifyOnUp: Boolean? = null,
    /** push: expected ping interval (min. 60). */
    @SerialName("period_seconds") val periodSeconds: Int? = null,
    /** push: grace time (min. 60). */
    @SerialName("grace_seconds") val graceSeconds: Int? = null,
    /** icmp and tcp: host name or IP. */
    val target: String? = null,
    /** icmp and tcp: prefer IPv4. */
    @SerialName("prefer_ipv4") val preferIpv4: Boolean? = null,
    /** tcp: port (1 to 65535). */
    val port: Int? = null,
    /** http and keyword: URL. */
    val url: String? = null,
    /** http and keyword: verify the certificate. */
    @SerialName("verify_tls") val verifyTls: Boolean? = null,
    /** keyword: text that must appear on the page. */
    val keyword: String? = null,
    /** Active checks: interval (1 to 86400). */
    @SerialName("interval_seconds") val intervalSeconds: Int? = null,
    /** Active checks: interval after a failure (10 to 86400). */
    @SerialName("retry_interval_seconds") val retryIntervalSeconds: Int? = null,
    /** Active checks: failures until alarm (1 to 60). */
    @SerialName("failures_before_alarm") val failuresBeforeAlarm: Int? = null,
)

/** Response of a test notification. */
@Serializable
public data class NotificationTestResult(
    /** Whether the message was sent. */
    val sent: Boolean? = null,
)
