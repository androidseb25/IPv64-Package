package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IsoInstant
import de.sr.ipv64.node64.internal.LenientInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

/** Type of a check. */
@Serializable
public enum class CheckType(override val value: String) : ApiValue {
    @SerialName("http") HTTP("http"),
    @SerialName("keyword") KEYWORD("keyword"),
    @SerialName("json") JSON("json"),
    @SerialName("dns") DNS("dns"),
    @SerialName("icmp") ICMP("icmp"),
    @SerialName("tcp") TCP("tcp"),
    @SerialName("port") PORT("port"),
    @SerialName("cert") CERT("cert"),
    @SerialName("whois") WHOIS("whois"),
    @SerialName("push") PUSH("push"),
    @SerialName("docker") DOCKER("docker"),
}

/** IP version used for the measurement. */
@Serializable
public enum class CheckIpMode(override val value: String) : ApiValue {
    @SerialName("auto") AUTO("auto"),
    @SerialName("ipv4") IPV4("ipv4"),
    @SerialName("ipv6") IPV6("ipv6"),
    @SerialName("both") BOTH("both"),
}

/** Where a check is measured. */
@Serializable
public enum class CheckExecution(override val value: String) : ApiValue {
    /** On nodes (see `nodes`). */
    @SerialName("local") LOCAL("local"),
    /** Only from the ipv64 locations. */
    @SerialName("online") ONLINE("online"),
    /** Both. */
    @SerialName("both") BOTH("both"),
}

/** Overall state when a check is measured at several places. */
@Serializable
public enum class CheckPolicy(override val value: String) : ApiValue {
    /** Down only if all measuring points are down. */
    @SerialName("all") ALL("all"),
    /** Down if one measuring point is down. */
    @SerialName("any") ANY("any"),
    /** Down if the majority is down. */
    @SerialName("majority") MAJORITY("majority"),
}

/** Which node owns a check. */
@Serializable
public enum class CheckOwner(override val value: String) : ApiValue {
    /** This node owns the check. */
    @SerialName("self") SELF("self"),
    /** Another node of the account owns it (still editable here). */
    @SerialName("other") OTHER("other"),
    /** Exists only at ipv64. */
    @SerialName("online") ONLINE("online"),
}

/** State of a check. */
@Serializable
public enum class CheckStateValue(override val value: String) : ApiValue {
    @SerialName("pending") PENDING("pending"),
    @SerialName("up") UP("up"),
    @SerialName("degraded") DEGRADED("degraded"),
    @SerialName("down") DOWN("down"),
    @SerialName("stabilizing") STABILIZING("stabilizing"),
    @SerialName("flapping") FLAPPING("flapping"),
    @SerialName("dependent") DEPENDENT("dependent"),
    @SerialName("paused") PAUSED("paused"),
}

/** Result status of a single measurement. */
@Serializable
public enum class MeasurementStatus(override val value: String) : ApiValue {
    @SerialName("up") UP("up"),
    @SerialName("down") DOWN("down"),
}

/**
 * A check. [params] (may contain credentials and headers) and [pushUrl] (contains the push token) are
 * redacted in [toString]; for push checks the [token] is redacted too, because it is the push token.
 */
@Serializable
public data class Check(
    /** 32 character primary key. */
    val token: String? = null,
    /** Name. */
    val name: String? = null,
    /** Check type. */
    val type: CheckType? = null,
    /** Target; depends on the type (URL, host, domain …), empty for push. */
    val target: String? = null,
    /** Type specific parameters (method, headers, port, keyword, …). */
    val params: JsonObject? = null,
    /** Check interval in seconds. */
    @SerialName("interval_seconds") val intervalSeconds: Int? = null,
    /** Interval for retries in seconds. */
    @SerialName("retry_interval_seconds") val retryIntervalSeconds: Int? = null,
    /** Number of retries (0 to 10). */
    val retries: Int? = null,
    /** Connect timeout in seconds. */
    @SerialName("connect_timeout_seconds") val connectTimeoutSeconds: Double? = null,
    /** Timeout in seconds. */
    @SerialName("timeout_seconds") val timeoutSeconds: Double? = null,
    /** Result is inverted. */
    val invert: Boolean? = null,
    /** Number of equal results needed to change state (1 to 20). */
    val stabilize: Int? = null,
    /** IP version. */
    @SerialName("ip_mode") val ipMode: CheckIpMode? = null,
    /** Tags. */
    val tags: List<String> = emptyList(),
    /** Description. */
    val description: String? = null,
    /** Check is paused. */
    val paused: Boolean? = null,
    /** Token of the parent check. */
    val parent: String? = null,
    /** Critical check. */
    val critical: Boolean? = null,
    /** Where the check is measured. */
    val execution: CheckExecution? = null,
    /** Overall state policy with several measuring points. */
    val policy: CheckPolicy? = null,
    /** Online locations (e.g. `nbg`, `fsn`). */
    val locations: List<String> = emptyList(),
    /** Also notify when partly degraded. */
    @SerialName("notify_degraded") val notifyDegraded: Boolean? = null,
    /** Notify on down. */
    @SerialName("notify_down") val notifyDown: Boolean? = null,
    /** Notify on up. */
    @SerialName("notify_up") val notifyUp: Boolean? = null,
    /** Notification channel IDs, empty = default channels. */
    val channels: List<Long> = emptyList(),
    /** Reminder interval in minutes during an outage (0 = off). */
    @SerialName("repeat_minutes") val repeatMinutes: Int? = null,
    /** Revision counter. */
    val revision: Long? = null,
    /** Creation time. */
    @Serializable(with = LenientInstantSerializer::class) @SerialName("created_at") val createdAt: Instant? = null,
    /** Last change. */
    @Serializable(with = LenientInstantSerializer::class) @SerialName("updated_at") val updatedAt: Instant? = null,
    /** Push URL (push checks). */
    @SerialName("push_url") val pushUrl: String? = null,
    /** Current state. */
    val state: State? = null,
    /** Free group name. */
    val group: String? = null,
    /** IDs of the measuring nodes. */
    val nodes: List<String> = emptyList(),
    /** Owner of the check. */
    val owner: CheckOwner? = null,
) {
    override fun toString(): String =
        "Check(token=${if (type == CheckType.PUSH && token != null) "<redacted>" else token}, name=$name, type=$type, target=$target, params=<redacted>, " +
            "intervalSeconds=$intervalSeconds, retries=$retries, tags=$tags, paused=$paused, " +
            "execution=$execution, policy=$policy, locations=$locations, channels=$channels, " +
            "pushUrl=<redacted>, state=$state, group=$group, nodes=$nodes, owner=$owner)"
}

/**
 * Body for creating (`POST /checks`) and changing (`PUT /checks/{token}`) a check.
 * On create, [name], [type] and [target] (except for push checks) are mandatory. On change,
 * [name], [type] and [target] are always taken over; an omitted [description] is cleared, other
 * omitted (null) values stay unchanged. [params] is redacted in [toString].
 */
@Serializable
public data class CheckRequest(
    /** Name. */
    val name: String,
    /** Check type. */
    val type: CheckType,
    /** Target; depends on the type, may be omitted for push checks. */
    val target: String? = null,
    /** Type specific parameters (method, headers, port, keyword, …). */
    val params: JsonObject? = null,
    /** Check interval in seconds (5 to 86400 local, 20 to 86400 online/hybrid; default 60). */
    @SerialName("interval_seconds") val intervalSeconds: Int? = null,
    /** Interval for retries in seconds (5 to interval; default 20). */
    @SerialName("retry_interval_seconds") val retryIntervalSeconds: Int? = null,
    /** Number of retries (0 to 10; default 1). */
    val retries: Int? = null,
    /** Connect timeout in seconds. */
    @SerialName("connect_timeout_seconds") val connectTimeoutSeconds: Double? = null,
    /** Timeout in seconds. */
    @SerialName("timeout_seconds") val timeoutSeconds: Double? = null,
    /** Invert the result. */
    val invert: Boolean? = null,
    /** Number of equal results needed to change state (1 to 20; default 3). */
    val stabilize: Int? = null,
    /** IP version. */
    @SerialName("ip_mode") val ipMode: CheckIpMode? = null,
    /** Tags. */
    val tags: List<String>? = null,
    /** Description. */
    val description: String? = null,
    /** Create/keep the check paused. */
    val paused: Boolean? = null,
    /** Token of the parent check. */
    val parent: String? = null,
    /** Critical check. */
    val critical: Boolean? = null,
    /** Where the check is measured (online/both need an ipv64 link; docker only local). */
    val execution: CheckExecution? = null,
    /** Overall state policy with several measuring points. */
    val policy: CheckPolicy? = null,
    /** Online locations (e.g. `nbg`, `fsn`), only with an ipv64 link. */
    val locations: List<String>? = null,
    /** Also notify when partly degraded. */
    @SerialName("notify_degraded") val notifyDegraded: Boolean? = null,
    /** Notify on down. */
    @SerialName("notify_down") val notifyDown: Boolean? = null,
    /** Notify on up. */
    @SerialName("notify_up") val notifyUp: Boolean? = null,
    /** Notification channel IDs, empty = default channels. */
    val channels: List<Long>? = null,
    /** Reminder interval in minutes during an outage (0 to 1440, 0 = off). */
    @SerialName("repeat_minutes") val repeatMinutes: Int? = null,
    /** Free group name (max. 100 characters). */
    val group: String? = null,
    /** IDs of the measuring nodes (local/both, only with an ipv64 link); empty = owner node. */
    val nodes: List<String>? = null,
) {
    override fun toString(): String =
        "CheckRequest(name=$name, type=$type, target=$target, params=<redacted>, intervalSeconds=$intervalSeconds, " +
            "tags=$tags, paused=$paused, execution=$execution, policy=$policy, locations=$locations, " +
            "channels=$channels, group=$group, nodes=$nodes)"
}

/** State of a check. */
@Serializable
public data class State(
    /** Current state. */
    val state: CheckStateValue? = null,
    /** In this state since. */
    @Serializable(with = LenientInstantSerializer::class) val since: Instant? = null,
    /** Last measurement. */
    @Serializable(with = LenientInstantSerializer::class) @SerialName("last_check_at") val lastCheckAt: Instant? = null,
    /** Next measurement. */
    @Serializable(with = LenientInstantSerializer::class) @SerialName("next_check_at") val nextCheckAt: Instant? = null,
    /** Last status (`up`/`down`). */
    @SerialName("last_status") val lastStatus: String? = null,
    /** Last latency in milliseconds. */
    @SerialName("last_latency_ms") val lastLatencyMs: Double? = null,
    /** Reason of the last result. */
    @SerialName("last_reason") val lastReason: String? = null,
    /** Message of the last result. */
    @SerialName("last_message") val lastMessage: String? = null,
    /** Consecutive failures. */
    @SerialName("consecutive_fail") val consecutiveFail: Int? = null,
    /** Consecutive successes. */
    @SerialName("consecutive_ok") val consecutiveOk: Int? = null,
    /** Last push received (push checks). */
    @Serializable(with = LenientInstantSerializer::class) @SerialName("last_push_at") val lastPushAt: Instant? = null,
    /** Down since. */
    @Serializable(with = LenientInstantSerializer::class) @SerialName("down_since") val downSince: Instant? = null,
    /** ID of the open incident. */
    @SerialName("incident_id") val incidentId: Long? = null,
    /** A maintenance window is active. */
    val maintenance: Boolean? = null,
)

/** A single measurement of a check. */
@Serializable
public data class Measurement(
    /** Sequence number (cursor for `before`). */
    val seq: Long? = null,
    /** Time of the measurement. */
    @Serializable(with = LenientInstantSerializer::class) val time: Instant? = null,
    /** Result. */
    val status: MeasurementStatus? = null,
    /** Latency in milliseconds. */
    @SerialName("latency_ms") val latencyMs: Double? = null,
    /** Reason code. */
    val reason: String? = null,
    /** Message. */
    val message: String? = null,
    /** IP family used. */
    val family: String? = null,
    /** Type specific details. */
    val details: JsonObject? = null,
    /** Measured during maintenance. */
    val maintenance: Boolean? = null,
    /** Result suppressed because the parent check is down. */
    val dependent: Boolean? = null,
)

/** A state change of a check. */
@Serializable
public data class Event(
    /** Event number (cursor for `before`). */
    val id: Long? = null,
    /** Event ID (string). */
    @SerialName("event_id") val eventId: String? = null,
    /** Check token. */
    val token: String? = null,
    /** Kind of event. */
    val kind: String? = null,
    /** Time. */
    @Serializable(with = LenientInstantSerializer::class) val time: Instant? = null,
    /** Previous state. */
    val from: String? = null,
    /** New state. */
    val to: String? = null,
    /** Reason code. */
    val reason: String? = null,
    /** Message. */
    val message: String? = null,
    /** Duration of the previous state in seconds. */
    @SerialName("duration_seconds") val durationSeconds: Long? = null,
    /** Number of state flaps. */
    val flaps: Int? = null,
    /** Happened during maintenance. */
    val maintenance: Boolean? = null,
    /** No notification was sent. */
    val silent: Boolean? = null,
    /** Related incident. */
    @SerialName("incident_id") val incidentId: Long? = null,
)

/** An outage of a check. */
@Serializable
public data class Incident(
    /** Incident ID. */
    val id: Long? = null,
    /** Check token. */
    val token: String? = null,
    /** Start. */
    @Serializable(with = LenientInstantSerializer::class) @SerialName("started_at") val startedAt: Instant? = null,
    /** End; missing while open. */
    @Serializable(with = LenientInstantSerializer::class) @SerialName("ended_at") val endedAt: Instant? = null,
    /** Still open. */
    val open: Boolean? = null,
    /** Reason code. */
    val reason: String? = null,
    /** Message. */
    val message: String? = null,
)

/** Availability and latency of a check over a range. */
@Serializable
public data class Stats(
    /** Requested range. */
    val range: String? = null,
    /** Start of the range. */
    @Serializable(with = LenientInstantSerializer::class) val from: Instant? = null,
    /** End of the range. */
    @Serializable(with = LenientInstantSerializer::class) val to: Instant? = null,
    /** Number of measurements. */
    val count: Long? = null,
    /** Number of successful measurements. */
    @SerialName("up_count") val upCount: Long? = null,
    /** Availability in percent. */
    @SerialName("up_percent") val upPercent: Double? = null,
    /** Average latency in ms. */
    @SerialName("avg_ms") val avgMs: Double? = null,
    /** Minimum latency in ms. */
    @SerialName("min_ms") val minMs: Double? = null,
    /** Maximum latency in ms. */
    @SerialName("max_ms") val maxMs: Double? = null,
    /** Width of a bucket in seconds. */
    @SerialName("bucket_seconds") val bucketSeconds: Long? = null,
    /** Time series. */
    val series: List<StatsPoint> = emptyList(),
)

/** One bucket of [Stats.series]. */
@Serializable
public data class StatsPoint(
    /** Start of the bucket. */
    @Serializable(with = LenientInstantSerializer::class) val time: Instant? = null,
    /** Number of measurements. */
    val count: Long? = null,
    /** Number of successful measurements. */
    @SerialName("up_count") val upCount: Long? = null,
    /** Average latency in ms. */
    @SerialName("avg_ms") val avgMs: Double? = null,
    /** Minimum latency in ms. */
    @SerialName("min_ms") val minMs: Double? = null,
    /** Maximum latency in ms. */
    @SerialName("max_ms") val maxMs: Double? = null,
    /** Availability in percent. */
    @SerialName("up_percent") val upPercent: Double? = null,
)

/** Range of `GET /checks/{token}/stats`. */
public enum class StatsRange(override val value: String) : ApiValue {
    HOUR_1("1h"), HOURS_24("24h"), DAYS_7("7d"), DAYS_30("30d"), YEAR_1("1y"),
}

/** Range of `GET /checks/{token}/chart`. */
public enum class ChartRange(override val value: String) : ApiValue {
    HOUR_1("1h"), HOURS_6("6h"), HOURS_24("24h"), DAYS_7("7d"), DAYS_30("30d"), YEAR_1("1y"),
}

/** Resolution of a chart. */
@Serializable
public enum class ChartBucket(override val value: String) : ApiValue {
    /** Single measurements. */
    @SerialName("raw") RAW("raw"),
    /** Hourly. */
    @SerialName("hour") HOUR("hour"),
    /** Daily. */
    @SerialName("day") DAY("day"),
}

/** Response of `GET /checks/{token}/chart`. */
@Serializable
public data class CheckChart(
    /** Requested range. */
    val range: String? = null,
    /** Resolution. */
    val bucket: ChartBucket? = null,
    /** One series per measuring point. */
    val series: List<CheckChartSeries> = emptyList(),
)

/** Latency series of one measuring point. */
@Serializable
public data class CheckChartSeries(
    /** `self`, `node:<id>` or `loc:<code>`. */
    val source: String? = null,
    /** Display name. */
    val label: String? = null,
    /** Data points. */
    val points: List<CheckChartPoint> = emptyList(),
)

/** A point of [CheckChartSeries]. */
@Serializable
public data class CheckChartPoint(
    /** Time. */
    val t: IsoInstant? = null,
    /** Latency in ms; null = outage or no measurement. */
    val ms: Double? = null,
    /** Availability in percent (only hourly/daily). */
    val up: Double? = null,
)

/** Action of `POST /checks/bulk`. */
@Serializable
public enum class BulkCheckAction(override val value: String) : ApiValue {
    @SerialName("pause") PAUSE("pause"),
    @SerialName("resume") RESUME("resume"),
    @SerialName("run") RUN("run"),
    @SerialName("delete") DELETE("delete"),
    @SerialName("tag") TAG("tag"),
    @SerialName("untag") UNTAG("untag"),
}

/** Body of `POST /checks/bulk`. */
@Serializable
public data class BulkCheckRequest(
    /** Action to execute. */
    val action: BulkCheckAction,
    /** Check tokens (1 to 1000). */
    val tokens: List<String>,
    /** Tag for the actions `tag` and `untag`. */
    val tag: String? = null,
)

/** Response of `POST /checks/bulk`. */
@Serializable
public data class BulkCheckResult(
    /** Number of successfully processed checks. */
    val done: Int? = null,
    /** Checks that failed. */
    val failed: List<BulkCheckFailure> = emptyList(),
)

/** A check the bulk action failed for. */
@Serializable
public data class BulkCheckFailure(
    /** Check token. */
    val token: String? = null,
    /** Error message. */
    val error: String? = null,
)

/**
 * Body of `POST /checks/online-test`: the same fields as [CheckRequest] plus an optional existing [token].
 * Nothing is saved. [token] and [params] are redacted in [toString].
 */
@Serializable
public data class OnlineTestRequest(
    /** Name. */
    val name: String,
    /** Check type. */
    val type: CheckType,
    /** Target. */
    val target: String? = null,
    /** Existing check token (e.g. to reuse stored credentials). */
    val token: String? = null,
    /** Type specific parameters. */
    val params: JsonObject? = null,
    /** Online locations (e.g. `nbg`, `fsn`). */
    val locations: List<String>? = null,
    /** Timeout in seconds. */
    @SerialName("timeout_seconds") val timeoutSeconds: Double? = null,
    /** Connect timeout in seconds. */
    @SerialName("connect_timeout_seconds") val connectTimeoutSeconds: Double? = null,
    /** IP version. */
    @SerialName("ip_mode") val ipMode: CheckIpMode? = null,
    /** Invert the result. */
    val invert: Boolean? = null,
    /** Check interval in seconds (20 to 86400 for online/hybrid). */
    @SerialName("interval_seconds") val intervalSeconds: Int? = null,
    /** Interval for retries in seconds. */
    @SerialName("retry_interval_seconds") val retryIntervalSeconds: Int? = null,
    /** Number of retries (0 to 10). */
    val retries: Int? = null,
    /** Number of equal results needed to change state (1 to 20). */
    val stabilize: Int? = null,
    /** Tags. */
    val tags: List<String>? = null,
    /** Description. */
    val description: String? = null,
    /** Paused. */
    val paused: Boolean? = null,
    /** Token of the parent check. */
    val parent: String? = null,
    /** Critical check. */
    val critical: Boolean? = null,
    /** Where the check is measured. */
    val execution: CheckExecution? = null,
    /** Overall state policy with several measuring points. */
    val policy: CheckPolicy? = null,
    /** Also notify when partly degraded. */
    @SerialName("notify_degraded") val notifyDegraded: Boolean? = null,
    /** Notify on down. */
    @SerialName("notify_down") val notifyDown: Boolean? = null,
    /** Notify on up. */
    @SerialName("notify_up") val notifyUp: Boolean? = null,
    /** Notification channel IDs. */
    val channels: List<Long>? = null,
    /** Reminder interval in minutes during an outage (0 to 1440, 0 = off). */
    @SerialName("repeat_minutes") val repeatMinutes: Int? = null,
    /** Free group name (max. 100 characters). */
    val group: String? = null,
    /** IDs of the measuring nodes. */
    val nodes: List<String>? = null,
) {
    override fun toString(): String =
        "OnlineTestRequest(name=$name, type=$type, target=$target, token=<redacted>, params=<redacted>, locations=$locations)"
}

/** Overall result of an online test. */
@Serializable
public enum class OnlineTestStatus(override val value: String) : ApiValue {
    @SerialName("ok") OK("ok"),
    @SerialName("failed") FAILED("failed"),
    @SerialName("expired") EXPIRED("expired"),
}

/** Response of `POST /checks/online-test`. */
@Serializable
public data class OnlineTestResult(
    /** Overall result. */
    val status: OnlineTestStatus? = null,
    /** Error message. */
    val error: String? = null,
    /** Result per location. */
    val results: List<OnlineTestLocationResult> = emptyList(),
)

/** Result of one location of an online test. */
@Serializable
public data class OnlineTestLocationResult(
    /** Location code. */
    val location: String? = null,
    /** Measurement succeeded. */
    val ok: Boolean? = null,
    /** Message. */
    val message: String? = null,
)

/** Status reported by a push heartbeat. */
public enum class PushStatus(override val value: String) : ApiValue {
    UP("up"), DOWN("down"),
}

/** Response of `GET`/`POST /push/{token}` (`{"status": "ok"}`). */
@Serializable
public data class PushResult(
    /** `ok` on success. */
    val status: String? = null,
    /** Error message (e.g. not connected to ipv64). */
    val error: String? = null,
)

/** Action of `POST /online-checks/{token}/{action}`. */
public enum class OnlineCheckAction(override val value: String) : ApiValue {
    PAUSE("pause"), RESUME("resume"), RUN("run"),
}
