package de.sr.ipv64.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

// ---------------------------------------------------------------------------------------------
// Checks
// ---------------------------------------------------------------------------------------------

/** Type of a Healthchecks 2.0 check. */
@Serializable
public enum class Hc2CheckType(override val value: String) : ApiValue {
    @SerialName("http") HTTP("http"),
    @SerialName("keyword") KEYWORD("keyword"),
    @SerialName("json") JSON("json"),
    @SerialName("dns") DNS("dns"),
    @SerialName("icmp") ICMP("icmp"),
    @SerialName("tcp") TCP("tcp"),
    @SerialName("port") PORT("port"),
    @SerialName("cert") CERT("cert"),
    @SerialName("push") PUSH("push"),
    @SerialName("docker") DOCKER("docker"),
    @SerialName("whois") WHOIS("whois"),
}

/** IP mode of a check. */
@Serializable
public enum class Hc2IpMode(override val value: String) : ApiValue {
    @SerialName("auto") AUTO("auto"),
    @SerialName("ipv4") IPV4("ipv4"),
    @SerialName("ipv6") IPV6("ipv6"),
    @SerialName("both") BOTH("both"),
}

/** Where a check is executed: online (ipv64), local (node) or both (hybrid). */
@Serializable
public enum class Hc2Execution(override val value: String) : ApiValue {
    @SerialName("online") ONLINE("online"),
    @SerialName("local") LOCAL("local"),
    @SerialName("both") BOTH("both"),
}

/** Overall rule when a check has several measuring points. */
@Serializable
public enum class Hc2Policy(override val value: String) : ApiValue {
    @SerialName("all") ALL("all"),
    @SerialName("any") ANY("any"),
    @SerialName("majority") MAJORITY("majority"),
}

/** State filter of a check list. */
public enum class Hc2CheckStateFilter(override val value: String) : ApiValue {
    UP("up"),
    DOWN("down"),
    DEGRADED("degraded"),
    FLAPPING("flapping"),
    PAUSED("paused"),
    PENDING("pending"),
}

/** Time range of check statistics. */
public enum class Hc2StatsRange(override val value: String) : ApiValue {
    HOURS_24("24h"),
    DAYS_7("7d"),
    DAYS_30("30d"),
    YEAR_1("1y"),
}

/** Operator of a JSON check rule. */
@Serializable
public enum class Hc2JsonOp(override val value: String) : ApiValue {
    @SerialName("eq") EQ("eq"),
    @SerialName("ne") NE("ne"),
    @SerialName("lt") LT("lt"),
    @SerialName("le") LE("le"),
    @SerialName("gt") GT("gt"),
    @SerialName("ge") GE("ge"),
    @SerialName("contains") CONTAINS("contains"),
    @SerialName("not_contains") NOT_CONTAINS("not_contains"),
    @SerialName("regex") REGEX("regex"),
    @SerialName("exists") EXISTS("exists"),
    @SerialName("not_exists") NOT_EXISTS("not_exists"),
}

/** DNS record type of a DNS check. */
@Serializable
public enum class Hc2DnsRecordType(override val value: String) : ApiValue {
    @SerialName("A") A("A"),
    @SerialName("AAAA") AAAA("AAAA"),
    @SerialName("CNAME") CNAME("CNAME"),
    @SerialName("MX") MX("MX"),
    @SerialName("TXT") TXT("TXT"),
    @SerialName("NS") NS("NS"),
    @SerialName("SRV") SRV("SRV"),
    @SerialName("CAA") CAA("CAA"),
    @SerialName("PTR") PTR("PTR"),
    @SerialName("SOA") SOA("SOA"),
}

/** Resolver of a DNS check. */
@Serializable
public enum class Hc2DnsResolver(override val value: String) : ApiValue {
    @SerialName("system") SYSTEM("system"),
    @SerialName("server") SERVER("server"),
    @SerialName("authoritative") AUTHORITATIVE("authoritative"),
}

/** Condition on the JSON response of a JSON check. */
@Serializable
public data class Hc2JsonRule(
    /** Path, e.g. `data.items[0].status`. */
    val path: String,
    /** Comparison operator. */
    val op: Hc2JsonOp,
    /** Comparison value. */
    val value: String? = null,
)

/**
 * Type dependent parameters of a check. All properties are optional; which ones apply depends on the check type.
 * Used for requests and responses, so lists and maps are nullable (`null` = not sent / not present).
 */
@Serializable
public data class Hc2CheckParams(
    /** http/keyword: method (GET, HEAD, POST, ...). */
    val method: String? = null,
    /** http/keyword: accepted status codes, e.g. `200-299,301`. */
    @SerialName("accept_status") val acceptStatus: String? = null,
    /** http/keyword: follow redirects. */
    @SerialName("follow_redirects") val followRedirects: Boolean? = null,
    /** http/keyword: maximum number of redirects. */
    @SerialName("max_redirects") val maxRedirects: Int? = null,
    /** keyword: text to search for. */
    val keyword: String? = null,
    /** keyword: the text must NOT occur. */
    @SerialName("keyword_absent") val keywordAbsent: Boolean? = null,
    /** http/keyword: basic auth user. */
    @SerialName("basic_user") val basicUser: String? = null,
    /** http/keyword: basic auth password. */
    @SerialName("basic_password") val basicPassword: String? = null,
    /** http/keyword: additional request headers. */
    val headers: Map<String, String>? = null,
    /** http/keyword: request body. */
    val body: String? = null,
    /** http/keyword: ignore certificate errors. */
    @SerialName("insecure_tls") val insecureTls: Boolean? = null,
    /** http/keyword (HTTPS): fail if the certificate expires in fewer days (0 = off). */
    @SerialName("cert_min_days") val certMinDays: Int? = null,
    /** icmp: number of packets (1-20). */
    val count: Int? = null,
    /** icmp: maximum packet loss in percent. */
    @SerialName("max_loss_percent") val maxLossPercent: Int? = null,
    /** icmp: maximum latency in ms, 0 = off. */
    @SerialName("max_latency_ms") val maxLatencyMs: Int? = null,
    /** tcp/port/cert: port (alternatively `host:port` in the target). */
    val port: Int? = null,
    /** tcp: data to send. */
    val send: String? = null,
    /** tcp: expected response start; port: `open`/`closed`; dns: any, contains, equals, regex, nxdomain, public_ip (local only). */
    val expect: String? = null,
    /** tcp: use TLS after connecting. */
    val tls: Boolean? = null,
    /** cert: minimum remaining validity in days. */
    @SerialName("min_days") val minDays: Int? = null,
    /** cert: SNI server name. */
    @SerialName("server_name") val serverName: String? = null,
    /** cert: skip chain validation. */
    @SerialName("skip_chain") val skipChain: Boolean? = null,
    /** cert: skip hostname validation. */
    @SerialName("skip_hostname") val skipHostname: Boolean? = null,
    /** push: expected interval in seconds. */
    @SerialName("period_seconds") val periodSeconds: Int? = null,
    /** push: grace period in seconds. */
    @SerialName("grace_seconds") val graceSeconds: Int? = null,
    /** json: conditions on the JSON response (at most 10). */
    @SerialName("json_rules") val jsonRules: List<Hc2JsonRule>? = null,
    /** json: one satisfied condition is enough (otherwise all). */
    @SerialName("json_any") val jsonAny: Boolean? = null,
    /** dns: record type (default A). */
    @SerialName("record_type") val recordType: Hc2DnsRecordType? = null,
    /** dns: resolver (default system). */
    val resolver: Hc2DnsResolver? = null,
    /** dns with resolver=server: IP, optionally with port. */
    val server: String? = null,
    /** dns: expected values (contains/equals) or a pattern (regex). */
    val values: List<String>? = null,
    /** dns: the answer must be DNSSEC validated. */
    val dnssec: Boolean? = null,
    /** dns: minimum TTL in seconds (0 = any). */
    @SerialName("min_ttl") val minTtl: Int? = null,
) {
    /** [basicPassword] and the values of [headers] (may carry credentials) are redacted. */
    override fun toString(): String =
        "Hc2CheckParams(method=$method, acceptStatus=$acceptStatus, followRedirects=$followRedirects, " +
            "maxRedirects=$maxRedirects, keyword=$keyword, keywordAbsent=$keywordAbsent, basicUser=$basicUser, " +
            "basicPassword=<redacted>, headers=${headers?.keys?.associateWith { "<redacted>" }}, body=$body, " +
            "insecureTls=$insecureTls, certMinDays=$certMinDays, count=$count, maxLossPercent=$maxLossPercent, " +
            "maxLatencyMs=$maxLatencyMs, port=$port, send=$send, expect=$expect, tls=$tls, minDays=$minDays, " +
            "serverName=$serverName, skipChain=$skipChain, skipHostname=$skipHostname, " +
            "periodSeconds=$periodSeconds, graceSeconds=$graceSeconds, jsonRules=$jsonRules, jsonAny=$jsonAny, " +
            "recordType=$recordType, resolver=$resolver, server=$server, values=$values, dnssec=$dnssec, " +
            "minTtl=$minTtl)"
}

/** Current state of a check. */
@Serializable
public data class Hc2CheckState(
    /** State: up, down, degraded, flapping, paused or pending. */
    val state: String? = null,
    /** In this state since. */
    val since: IsoInstant? = null,
    /** Time of the last measurement. */
    @SerialName("last_check_at") val lastCheckAt: IsoInstant? = null,
    /** Last status: `up` or `down`. */
    @SerialName("last_status") val lastStatus: String? = null,
    /** Last latency in ms. */
    @SerialName("last_latency_ms") val lastLatencyMs: Double? = null,
    /** Reason of the last result. */
    @SerialName("last_reason") val lastReason: String? = null,
    /** Message of the last result. */
    @SerialName("last_message") val lastMessage: String? = null,
    /** Down since. */
    @SerialName("down_since") val downSince: IsoInstant? = null,
    /** Time of the last push. */
    @SerialName("last_push_at") val lastPushAt: IsoInstant? = null,
)

/** Push URLs of a push check. */
@Serializable
public data class Hc2CheckPush(
    /** Push URL at ipv64. */
    val url: String? = null,
    /** Push URL at the hub. */
    @SerialName("hub_url") val hubUrl: String? = null,
    /** Path at the node (`http://<node>/api/push/<token>`). */
    @SerialName("node_path") val nodePath: String? = null,
)

/** A Healthchecks 2.0 check. */
@Serializable
public data class Hc2Check(
    /** Name of the check. */
    val name: String? = null,
    /** Check type. */
    val type: Hc2CheckType? = null,
    /** Target: URL (http/keyword/json), name (dns, for PTR also an IP), host/IP (icmp), `host` or `host:port` (tcp/port/cert); empty for push. */
    val target: String? = null,
    /** Type specific parameters. */
    val params: Hc2CheckParams? = null,
    /** Interval in seconds (minimum 20). */
    @SerialName("interval_seconds") val intervalSeconds: Int? = null,
    /** Retry interval after a failure in seconds (minimum 5). */
    @SerialName("retry_interval_seconds") val retryIntervalSeconds: Int? = null,
    /** Additional attempts before an alarm is raised (0-10). */
    val retries: Int? = null,
    /** Connect timeout in seconds. */
    @SerialName("connect_timeout_seconds") val connectTimeoutSeconds: Double? = null,
    /** Total timeout in seconds. */
    @SerialName("timeout_seconds") val timeoutSeconds: Double? = null,
    /** Whether the result is inverted. */
    val invert: Boolean? = null,
    /** Number of measurements until a state change (1-20). */
    val stabilize: Int? = null,
    /** IP mode. */
    @SerialName("ip_mode") val ipMode: Hc2IpMode? = null,
    /** Tags. */
    val tags: List<String> = emptyList(),
    /** Group (free-form name). */
    val group: String? = null,
    /** Description. */
    val description: String? = null,
    /** Whether the check is paused. */
    val paused: Boolean? = null,
    /** Token of the parent check (dependency) or empty. */
    val parent: String? = null,
    /** Critical checks ignore quiet hours. */
    val critical: Boolean? = null,
    /** Where the check is executed. */
    val execution: Hc2Execution? = null,
    /** Owner node for local/both executions. */
    @SerialName("node_id") val nodeId: String? = null,
    /** Measuring nodes; the server returns objects of the form `{node_id, name}`, so the items are kept as raw JSON. */
    val nodes: List<JsonElement> = emptyList(),
    /** Locations for online/both executions. */
    val locations: List<String> = emptyList(),
    /** Overall rule when several measuring points exist. */
    val policy: Hc2Policy? = null,
    /** Notify also on partial failure. */
    @SerialName("notify_degraded") val notifyDegraded: Boolean? = null,
    /** Notify on failure. */
    @SerialName("notify_down") val notifyDown: Boolean? = null,
    /** Notify on recovery. */
    @SerialName("notify_up") val notifyUp: Boolean? = null,
    /** IDs of the notification channels; empty means default channels. */
    val channels: List<Long> = emptyList(),
    /** Reminder interval in minutes while a failure persists, 0 = off. */
    @SerialName("repeat_minutes") val repeatMinutes: Int? = null,
    /** Token of the check (32 characters). */
    val token: String? = null,
    /** Name of the node. */
    @SerialName("node_name") val nodeName: String? = null,
    /** Revision counter. */
    val revision: Int? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Last modification time. */
    @SerialName("updated_at") val updatedAt: IsoInstant? = null,
    /** Current state. */
    val state: Hc2CheckState? = null,
    /** Push details, only for type push. */
    val push: Hc2CheckPush? = null,
)

/** State of one measuring point of a check. */
@Serializable
public data class Hc2CheckSource(
    /** `loc:nbg`, `loc:fsn` or `node:<id>`. */
    val source: String? = null,
    /** Readable name. */
    val label: String? = null,
    /** State of this measuring point. */
    val state: String? = null,
    /** In this state since. */
    val since: IsoInstant? = null,
    /** Time of the last measurement. */
    @SerialName("last_check_at") val lastCheckAt: IsoInstant? = null,
    /** Last status. */
    @SerialName("last_status") val lastStatus: String? = null,
    /** Last latency in ms. */
    @SerialName("last_latency_ms") val lastLatencyMs: Double? = null,
    /** Reason of the last result. */
    @SerialName("last_reason") val lastReason: String? = null,
    /** Message of the last result. */
    @SerialName("last_message") val lastMessage: String? = null,
    /** Consecutive failures. */
    @SerialName("consecutive_fail") val consecutiveFail: Long? = null,
    /** Consecutive successes. */
    @SerialName("consecutive_ok") val consecutiveOk: Long? = null,
)

/** Availability of a check in percent per period. */
@Serializable
public data class Hc2CheckAvailability(
    /** Last 24 hours. */
    @SerialName("24h") val last24h: Double? = null,
    /** Last 30 days. */
    @SerialName("30d") val last30d: Double? = null,
    /** Last year. */
    @SerialName("1y") val last1y: Double? = null,
)

/** A check including state per measuring point and availability. */
@Serializable
public data class Hc2CheckDetail(
    /** Name of the check. */
    val name: String? = null,
    /** Check type. */
    val type: Hc2CheckType? = null,
    /** Target: URL (http/keyword/json), name (dns, for PTR also an IP), host/IP (icmp), `host` or `host:port` (tcp/port/cert); empty for push. */
    val target: String? = null,
    /** Type specific parameters. */
    val params: Hc2CheckParams? = null,
    /** Interval in seconds (minimum 20). */
    @SerialName("interval_seconds") val intervalSeconds: Int? = null,
    /** Retry interval after a failure in seconds (minimum 5). */
    @SerialName("retry_interval_seconds") val retryIntervalSeconds: Int? = null,
    /** Additional attempts before an alarm is raised (0-10). */
    val retries: Int? = null,
    /** Connect timeout in seconds. */
    @SerialName("connect_timeout_seconds") val connectTimeoutSeconds: Double? = null,
    /** Total timeout in seconds. */
    @SerialName("timeout_seconds") val timeoutSeconds: Double? = null,
    /** Whether the result is inverted. */
    val invert: Boolean? = null,
    /** Number of measurements until a state change (1-20). */
    val stabilize: Int? = null,
    /** IP mode. */
    @SerialName("ip_mode") val ipMode: Hc2IpMode? = null,
    /** Tags. */
    val tags: List<String> = emptyList(),
    /** Group (free-form name). */
    val group: String? = null,
    /** Description. */
    val description: String? = null,
    /** Whether the check is paused. */
    val paused: Boolean? = null,
    /** Token of the parent check (dependency) or empty. */
    val parent: String? = null,
    /** Critical checks ignore quiet hours. */
    val critical: Boolean? = null,
    /** Where the check is executed. */
    val execution: Hc2Execution? = null,
    /** Owner node for local/both executions. */
    @SerialName("node_id") val nodeId: String? = null,
    /** Measuring nodes; the server returns objects of the form `{node_id, name}`, so the items are kept as raw JSON. */
    val nodes: List<JsonElement> = emptyList(),
    /** Locations for online/both executions. */
    val locations: List<String> = emptyList(),
    /** Overall rule when several measuring points exist. */
    val policy: Hc2Policy? = null,
    /** Notify also on partial failure. */
    @SerialName("notify_degraded") val notifyDegraded: Boolean? = null,
    /** Notify on failure. */
    @SerialName("notify_down") val notifyDown: Boolean? = null,
    /** Notify on recovery. */
    @SerialName("notify_up") val notifyUp: Boolean? = null,
    /** IDs of the notification channels; empty means default channels. */
    val channels: List<Long> = emptyList(),
    /** Reminder interval in minutes while a failure persists, 0 = off. */
    @SerialName("repeat_minutes") val repeatMinutes: Int? = null,
    /** Token of the check (32 characters). */
    val token: String? = null,
    /** Name of the node. */
    @SerialName("node_name") val nodeName: String? = null,
    /** Revision counter. */
    val revision: Int? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Last modification time. */
    @SerialName("updated_at") val updatedAt: IsoInstant? = null,
    /** Current state. */
    val state: Hc2CheckState? = null,
    /** Push details, only for type push. */
    val push: Hc2CheckPush? = null,
    /** State per measuring point. */
    val sources: List<Hc2CheckSource> = emptyList(),
    /** Availability in percent for `24h`, `30d` and `1y`. */
    @SerialName("availability_percent") val availabilityPercent: Hc2CheckAvailability? = null,
)

/**
 * Request body to create a check (`POST /hc2/checks`). To change a check use [Hc2CheckUpdate].
 */
@Serializable
public data class Hc2CheckInput(
    /** Name of the check. */
    val name: String,
    /** Check type. */
    val type: Hc2CheckType,
    /** Target: URL (http/keyword/json), name (dns), host/IP (icmp), `host` or `host:port` (tcp/port/cert); empty for push. */
    val target: String? = null,
    /** Type specific parameters. */
    val params: Hc2CheckParams? = null,
    /** Interval in seconds (minimum 20). */
    @SerialName("interval_seconds") val intervalSeconds: Int? = null,
    /** Retry interval after a failure in seconds (minimum 5). */
    @SerialName("retry_interval_seconds") val retryIntervalSeconds: Int? = null,
    /** Additional attempts before an alarm is raised (0-10). */
    val retries: Int? = null,
    /** Connect timeout in seconds. */
    @SerialName("connect_timeout_seconds") val connectTimeoutSeconds: Double? = null,
    /** Total timeout in seconds. */
    @SerialName("timeout_seconds") val timeoutSeconds: Double? = null,
    /** Invert the result. */
    val invert: Boolean? = null,
    /** Measurements until a state change (1-20). */
    val stabilize: Int? = null,
    /** IP mode. */
    @SerialName("ip_mode") val ipMode: Hc2IpMode? = null,
    /** Tags. */
    val tags: List<String>? = null,
    /** Group (free-form name). */
    val group: String? = null,
    /** Description. */
    val description: String? = null,
    /** Create/keep the check paused. */
    val paused: Boolean? = null,
    /** Token of the parent check (dependency) or empty. */
    val parent: String? = null,
    /** Critical checks ignore quiet hours. */
    val critical: Boolean? = null,
    /** Where the check is executed. */
    val execution: Hc2Execution? = null,
    /** Owner node for local/both; on its own it means a single measuring node. */
    @SerialName("node_id") val nodeId: String? = null,
    /** Measuring node IDs for local/both (docker/push exactly one). */
    val nodes: List<String>? = null,
    /** Locations for online/both (`nbg`, `fsn`). */
    val locations: List<String>? = null,
    /** Overall rule when several measuring points exist. */
    val policy: Hc2Policy? = null,
    /** Notify also on partial failure. */
    @SerialName("notify_degraded") val notifyDegraded: Boolean? = null,
    /** Notify on failure. */
    @SerialName("notify_down") val notifyDown: Boolean? = null,
    /** Notify on recovery. */
    @SerialName("notify_up") val notifyUp: Boolean? = null,
    /** Channel IDs; empty means default channels. */
    val channels: List<Long>? = null,
    /** Reminder interval in minutes while a failure persists, 0 = off. */
    @SerialName("repeat_minutes") val repeatMinutes: Int? = null,
)

/**
 * Request body of `PATCH /hc2/checks/{token}` (schema `Hc2CheckInput`, but every property optional):
 * only the properties that are set (non-`null`) are changed.
 * Changing [execution], [nodeId] or [locations] is rejected with 409 `move_requires_test` unless
 * [forceMove] is `true`; prefer `POST /hc2/checks/{token}/move` with a test run.
 */
@Serializable
public data class Hc2CheckUpdate(
    /** Name of the check. */
    val name: String? = null,
    /** Check type. */
    val type: Hc2CheckType? = null,
    /** Target: URL (http/keyword/json), name (dns), host/IP (icmp), `host` or `host:port` (tcp/port/cert); empty for push. */
    val target: String? = null,
    /** Type specific parameters. */
    val params: Hc2CheckParams? = null,
    /** Interval in seconds (minimum 20). */
    @SerialName("interval_seconds") val intervalSeconds: Int? = null,
    /** Retry interval after a failure in seconds (minimum 5). */
    @SerialName("retry_interval_seconds") val retryIntervalSeconds: Int? = null,
    /** Additional attempts before an alarm is raised (0-10). */
    val retries: Int? = null,
    /** Connect timeout in seconds. */
    @SerialName("connect_timeout_seconds") val connectTimeoutSeconds: Double? = null,
    /** Total timeout in seconds. */
    @SerialName("timeout_seconds") val timeoutSeconds: Double? = null,
    /** Invert the result. */
    val invert: Boolean? = null,
    /** Measurements until a state change (1-20). */
    val stabilize: Int? = null,
    /** IP mode. */
    @SerialName("ip_mode") val ipMode: Hc2IpMode? = null,
    /** Tags (replaces the existing tags). */
    val tags: List<String>? = null,
    /** Group (free-form name). */
    val group: String? = null,
    /** Description. */
    val description: String? = null,
    /** Pause or resume the check. */
    val paused: Boolean? = null,
    /** Token of the parent check (dependency) or empty. */
    val parent: String? = null,
    /** Critical checks ignore quiet hours. */
    val critical: Boolean? = null,
    /** Where the check is executed (see [forceMove]). */
    val execution: Hc2Execution? = null,
    /** Owner node for local/both (see [forceMove]). */
    @SerialName("node_id") val nodeId: String? = null,
    /** Measuring node IDs for local/both (docker/push exactly one). */
    val nodes: List<String>? = null,
    /** Locations for online/both (`nbg`, `fsn`) (see [forceMove]). */
    val locations: List<String>? = null,
    /** Overall rule when several measuring points exist. */
    val policy: Hc2Policy? = null,
    /** Notify also on partial failure. */
    @SerialName("notify_degraded") val notifyDegraded: Boolean? = null,
    /** Notify on failure. */
    @SerialName("notify_down") val notifyDown: Boolean? = null,
    /** Notify on recovery. */
    @SerialName("notify_up") val notifyUp: Boolean? = null,
    /** Channel IDs; empty means default channels. */
    val channels: List<Long>? = null,
    /** Reminder interval in minutes while a failure persists, 0 = off. */
    @SerialName("repeat_minutes") val repeatMinutes: Int? = null,
    /**
     * Apply a change of execution/node/locations without a test run. Only mentioned in the operation
     * description (`force_move=true`), not part of the published schema.
     */
    @SerialName("force_move") val forceMove: Boolean? = null,
)

/**
 * Request body of `POST /hc2/checks/{token}/move`: change the execution location with a test run.
 */
@Serializable
public data class Hc2MoveCheckRequest(
    /** Target execution location. */
    val execution: Hc2Execution,
    /** Owner node for local/both. */
    @SerialName("node_id") val nodeId: String? = null,
    /** Measuring node IDs for local/both. */
    val nodes: List<String>? = null,
    /** Locations for online/both. */
    val locations: List<String>? = null,
)

/** Result of `POST /hc2/checks/{token}/run`. */
@Serializable
public data class Hc2CheckRunResult(
    /** Whether the run was queued. */
    val queued: Boolean? = null,
    /** Command ID, set for node checks (HTTP 202). */
    @SerialName("command_id") val commandId: Long? = null,
    /** Node the command was sent to. */
    @SerialName("node_id") val nodeId: String? = null,
    /** Details. */
    val detail: String? = null,
)

/** Result of a single measuring point in a [Hc2Testrun]. */
@Serializable
public data class Hc2TestrunResult(
    /** Measuring point. */
    val source: String? = null,
    /** Whether the measurement succeeded. */
    val ok: Boolean? = null,
    /** Message. */
    val message: String? = null,
)

/** Status of a test run (see `POST /hc2/checks/{token}/move`). */
@Serializable
public data class Hc2Testrun(
    /** Test run ID. */
    val id: Long? = null,
    /** Status: pending, running, ok, failed or expired. */
    val status: String? = null,
    /** Target execution location. */
    val target: String? = null,
    /** Whether the change was applied. */
    val applied: Boolean? = null,
    /** Result per measuring point. */
    val results: List<Hc2TestrunResult> = emptyList(),
    /** Error message. */
    val error: String? = null,
)

/** A raw measurement of a check. */
@Serializable
public data class Hc2Measurement(
    /** Time of the measurement. */
    val time: IsoInstant? = null,
    /** Measuring point (`loc:nbg`, `node:<id>`, ...). */
    val source: String? = null,
    /** Readable name of the measuring point. */
    @SerialName("source_label") val sourceLabel: String? = null,
    /** `up` or `down`. */
    val status: String? = null,
    /** Latency in ms. */
    @SerialName("latency_ms") val latencyMs: Double? = null,
    /** Reason. */
    val reason: String? = null,
    /** Message. */
    val message: String? = null,
    /** `ipv4` or `ipv6`. */
    val family: String? = null,
    /** Raw details. */
    val details: JsonObject? = null,
)

/** An event of a check. */
@Serializable
public data class Hc2Event(
    /** Event ID (cursor for `before`/`after`). */
    val id: Long? = null,
    /** Event identifier. */
    @SerialName("event_id") val eventId: String? = null,
    /** Check token. */
    val token: String? = null,
    /** Measuring point; empty = overall state. */
    val source: String? = null,
    /** Readable name of the measuring point. */
    @SerialName("source_label") val sourceLabel: String? = null,
    /** Kind: down, up, degraded, degraded_end, flapping_start, flapping_end, reminder. */
    val kind: String? = null,
    /** Event time. */
    val time: IsoInstant? = null,
    /** Previous state. */
    @SerialName("from_state") val fromState: String? = null,
    /** New state. */
    @SerialName("to_state") val toState: String? = null,
    /** Reason. */
    val reason: String? = null,
    /** Message. */
    val message: String? = null,
    /** Duration of the previous state in seconds. */
    @SerialName("duration_seconds") val durationSeconds: Long? = null,
    /** Number of flaps. */
    val flaps: Int? = null,
    /** Event without alarm. */
    val silent: Boolean? = null,
    /** Related incident. */
    @SerialName("incident_id") val incidentId: Long? = null,
)

/** An incident (outage) of a check. */
@Serializable
public data class Hc2Incident(
    /** Incident ID. */
    val id: Long? = null,
    /** Check token. */
    val token: String? = null,
    /** Check name. */
    val name: String? = null,
    /** `open` or `closed`. */
    val status: String? = null,
    /** Start of the incident. */
    @SerialName("started_at") val startedAt: IsoInstant? = null,
    /** End of the incident. */
    @SerialName("ended_at") val endedAt: IsoInstant? = null,
    /** Duration in seconds. */
    @SerialName("duration_seconds") val durationSeconds: Long? = null,
    /** Reason. */
    val reason: String? = null,
    /** Message. */
    val message: String? = null,
    /** Affected measuring points. */
    val sources: List<String> = emptyList(),
)

/** Latency statistics in milliseconds. */
@Serializable
public data class Hc2LatencyStats(
    val min: Double? = null,
    val avg: Double? = null,
    val max: Double? = null,
)

/** Statistics of one measuring point. */
@Serializable
public data class Hc2StatsSource(
    /** Measuring point. */
    val source: String? = null,
    /** Readable name. */
    val label: String? = null,
    /** Number of measurements. */
    val measurements: Long? = null,
    /** Availability in percent. */
    @SerialName("availability_percent") val availabilityPercent: Double? = null,
    /** Latency statistics. */
    @SerialName("latency_ms") val latencyMs: Hc2LatencyStats? = null,
)

/** One point of the statistics time series. */
@Serializable
public data class Hc2StatsPoint(
    /** Start of the bucket. */
    val time: IsoInstant? = null,
    /** Measuring point. */
    val source: String? = null,
    /** Number of measurements. */
    val measurements: Long? = null,
    /** Availability in percent. */
    @SerialName("availability_percent") val availabilityPercent: Double? = null,
    /** Average latency in ms. */
    @SerialName("latency_avg_ms") val latencyAvgMs: Double? = null,
    /** Maximum latency in ms. */
    @SerialName("latency_max_ms") val latencyMaxMs: Double? = null,
)

/** Availability and latency statistics of a check. */
@Serializable
public data class Hc2Stats(
    /** Requested range. */
    val range: String? = null,
    /** Bucket size: `hour` or `day`. */
    val bucket: String? = null,
    /** Total number of measurements. */
    val measurements: Long? = null,
    /** Availability in percent. */
    @SerialName("availability_percent") val availabilityPercent: Double? = null,
    /** Statistics per measuring point. */
    val sources: List<Hc2StatsSource> = emptyList(),
    /** Time series. */
    val series: List<Hc2StatsPoint> = emptyList(),
)

// ---------------------------------------------------------------------------------------------
// Channels
// ---------------------------------------------------------------------------------------------

/** A notification channel. */
@Serializable
public data class Hc2Channel(
    /** Channel ID. */
    val id: Long? = null,
    /** Channel type: ipv64_mail, ipv64_sms, app, telegram, discord, slack, teams, matrix, ntfy, gotify, pushover, secntfy, webhook, smtp. */
    val type: String? = null,
    /** Readable type name. */
    @SerialName("type_label") val typeLabel: String? = null,
    /** Name. */
    val name: String? = null,
    /** Configuration without secrets. */
    val config: JsonObject? = null,
    /** Names of the secret fields that are set. */
    @SerialName("secrets_set") val secretsSet: List<String> = emptyList(),
    /** Whether the channel is enabled. */
    val enabled: Boolean? = null,
    /** Whether this is a default channel. */
    val default: Boolean? = null,
    /** Mail/SMS: confirmed by code. */
    val verified: Boolean? = null,
    /** Language: `de` or `en`. */
    val lang: String? = null,
    /** Notify on failure. */
    @SerialName("notify_down") val notifyDown: Boolean? = null,
    /** Notify on recovery. */
    @SerialName("notify_up") val notifyUp: Boolean? = null,
    /** Execution: `server` or `node`. */
    val execution: String? = null,
    /** Node ID for node channels. */
    @SerialName("node_id") val nodeId: String? = null,
    /** Last use. */
    @SerialName("last_used") val lastUsed: IsoInstant? = null,
    /** Last error. */
    @SerialName("last_error") val lastError: String? = null,
    /** Revision counter. */
    val revision: Int? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Usable via `POST /hc2/channels/{id}/send` (ipv64 channel, verified, enabled). */
    @SerialName("sendable_via_api") val sendableViaApi: Boolean? = null,
)

/**
 * Request body to create a channel (`POST /hc2/channels`). To change a channel use [Hc2ChannelUpdate].
 * [config] may contain secrets and is redacted in [toString].
 */
@Serializable
public data class Hc2ChannelInput(
    /** Name of the channel. */
    val name: String,
    /** Channel type (only when creating). */
    val type: String? = null,
    /** Fields as returned by `GET /hc2/channel-types`. */
    val config: JsonObject? = null,
    /** Whether the channel is enabled. */
    val enabled: Boolean? = null,
    /** Whether this is a default channel. */
    val default: Boolean? = null,
    /** Language: `de` or `en`. */
    val lang: String? = null,
    /** Notify on failure. */
    @SerialName("notify_down") val notifyDown: Boolean? = null,
    /** Notify on recovery. */
    @SerialName("notify_up") val notifyUp: Boolean? = null,
) {
    override fun toString(): String =
        "Hc2ChannelInput(name=$name, type=$type, config=<redacted>, enabled=$enabled, default=$default, " +
            "lang=$lang, notifyDown=$notifyDown, notifyUp=$notifyUp)"
}

/**
 * Request body of `PATCH /hc2/channels/{id}` (schema `Hc2ChannelInput`, but every property optional):
 * only the properties that are set (non-`null`) are changed. The channel type cannot be changed.
 * [config] may contain secrets and is redacted in [toString].
 */
@Serializable
public data class Hc2ChannelUpdate(
    /** Name of the channel. */
    val name: String? = null,
    /** Fields as returned by `GET /hc2/channel-types`; omit secret fields to keep them unchanged. */
    val config: JsonObject? = null,
    /** Whether the channel is enabled. */
    val enabled: Boolean? = null,
    /** Whether this is a default channel. */
    val default: Boolean? = null,
    /** Language: `de` or `en`. */
    val lang: String? = null,
    /** Notify on failure. */
    @SerialName("notify_down") val notifyDown: Boolean? = null,
    /** Notify on recovery. */
    @SerialName("notify_up") val notifyUp: Boolean? = null,
) {
    override fun toString(): String =
        "Hc2ChannelUpdate(name=$name, config=<redacted>, enabled=$enabled, default=$default, lang=$lang, " +
            "notifyDown=$notifyDown, notifyUp=$notifyUp)"
}

/** Configuration field of a channel type. */
@Serializable
public data class Hc2ChannelTypeField(
    /** Field key. */
    val key: String? = null,
    /** Label. */
    val label: String? = null,
    /** Kind: text, password, url, email, number, select, textarea, bool. */
    val kind: String? = null,
    /** Whether the field is required. */
    val required: Boolean? = null,
    /** Whether the field is secret. */
    val secret: Boolean? = null,
    /** Options for select fields. */
    val options: List<String> = emptyList(),
    /** Default value. */
    val default: String? = null,
)

/** A channel type with its configuration fields. */
@Serializable
public data class Hc2ChannelType(
    /** Type identifier. */
    val type: String? = null,
    /** Readable name. */
    val label: String? = null,
    /** Whether the channel must be verified by code. */
    @SerialName("needs_verification") val needsVerification: Boolean? = null,
    /** Configuration fields. */
    val fields: List<Hc2ChannelTypeField> = emptyList(),
)

/** Result of `POST /hc2/channels/{id}/test`. */
@Serializable
public data class Hc2ChannelTestResult(
    /** Whether the message was sent. */
    val sent: Boolean? = null,
    /** Whether the message was queued at the node. */
    @SerialName("queued_at_node") val queuedAtNode: Boolean? = null,
)

/** Request body of `POST /hc2/channels/{id}/send`. */
@Serializable
public data class Hc2SendMessageRequest(
    /** Subject. */
    val subject: String,
    /** Text; lines of the form `Label: value` are shown as a table in mails. */
    val text: String,
    /** Kind of message, e.g. `dyndns_ip`, `dyndns_fail` (controls color and status of the mail). */
    val kind: String? = null,
    /** Identifier to detect duplicates (24 h). */
    @SerialName("event_id") val eventId: String? = null,
    /** Source of the message. */
    val source: String? = null,
)

/** Result of `POST /hc2/channels/{id}/send`. */
@Serializable
public data class Hc2SendMessageResult(
    /** Whether the message was sent. */
    val sent: Boolean? = null,
    /** True if the `event_id` was already sent within 24 h and nothing was sent again. */
    val duplicate: Boolean? = null,
    /** Remaining SMS of the day, `null` = unlimited or not applicable. */
    @SerialName("sms_left") val smsLeft: Int? = null,
)

/** Request body of `POST /hc2/channels/{id}/verify`. */
@Serializable
public data class Hc2VerifyChannelRequest(
    /** The code received via `POST /hc2/channels/{id}/verify/send`. */
    val code: String,
)

// ---------------------------------------------------------------------------------------------
// Maintenance
// ---------------------------------------------------------------------------------------------

/** Repeat mode of a maintenance window. */
@Serializable
public enum class Hc2MaintenanceRepeat(override val value: String) : ApiValue {
    @SerialName("none") NONE("none"),
    @SerialName("daily") DAILY("daily"),
    @SerialName("weekly") WEEKLY("weekly"),
    @SerialName("monthly") MONTHLY("monthly"),
}

/** A maintenance window. */
@Serializable
public data class Hc2Maintenance(
    /** ID. */
    val id: Long? = null,
    /** Name. */
    val name: String? = null,
    /** Start. */
    @SerialName("start_at") val startAt: IsoInstant? = null,
    /** End. */
    @SerialName("end_at") val endAt: IsoInstant? = null,
    /** Repeat mode. */
    val repeat: Hc2MaintenanceRepeat? = null,
    /** Repeat until. */
    @SerialName("repeat_until") val repeatUntil: IsoInstant? = null,
    /** Affected check tokens; empty = all checks. */
    val tokens: List<String> = emptyList(),
    /** Whether the window is active. */
    val active: Boolean? = null,
    /** Node ID. */
    @SerialName("node_id") val nodeId: String? = null,
    /** Revision counter. */
    val revision: Int? = null,
)

/** Request body to create a maintenance window (`POST /hc2/maintenance`). To change one use [Hc2MaintenanceUpdate]. */
@Serializable
public data class Hc2MaintenanceInput(
    /** Name. */
    val name: String,
    /** Start. */
    @SerialName("start_at") val startAt: IsoInstant,
    /** End. */
    @SerialName("end_at") val endAt: IsoInstant,
    /** Repeat mode. */
    val repeat: Hc2MaintenanceRepeat? = null,
    /** Repeat until. */
    @SerialName("repeat_until") val repeatUntil: IsoInstant? = null,
    /** Affected check tokens; empty = all checks. */
    val tokens: List<String>? = null,
    /** Whether the window is active. */
    val active: Boolean? = null,
)

/**
 * Request body of `PATCH /hc2/maintenance/{id}` (schema `Hc2MaintenanceInput`, but every property optional):
 * only the properties that are set (non-`null`) are changed.
 */
@Serializable
public data class Hc2MaintenanceUpdate(
    /** Name. */
    val name: String? = null,
    /** Start. */
    @SerialName("start_at") val startAt: IsoInstant? = null,
    /** End. */
    @SerialName("end_at") val endAt: IsoInstant? = null,
    /** Repeat mode. */
    val repeat: Hc2MaintenanceRepeat? = null,
    /** Repeat until. */
    @SerialName("repeat_until") val repeatUntil: IsoInstant? = null,
    /** Affected check tokens (replaces the existing list); empty = all checks. */
    val tokens: List<String>? = null,
    /** Whether the window is active. */
    val active: Boolean? = null,
)

// ---------------------------------------------------------------------------------------------
// Account and settings
// ---------------------------------------------------------------------------------------------

/** A used/limit pair; a `null` limit means unlimited. */
@Serializable
public data class Hc2UsageLimit(
    /** Current usage. */
    val used: Long? = null,
    /** Limit, `null` = unlimited. */
    val limit: Long? = null,
)

/** An online measuring location. */
@Serializable
public data class Hc2Location(
    /** Location code (`nbg`, `fsn`). */
    val code: String? = null,
    /** Name. */
    val name: String? = null,
)

/** Node counters of the account. */
@Serializable
public data class Hc2AccountNodes(
    /** Total nodes. */
    val total: Int? = null,
    /** Online nodes. */
    val online: Int? = null,
)

/** Healthchecks 2.0 plan, limits and locations of the account. */
@Serializable
public data class Hc2Account(
    /** Plan. */
    val plan: String? = null,
    /** Online measuring points used / limit. */
    @SerialName("online_measuring_points") val onlineMeasuringPoints: Hc2UsageLimit? = null,
    /** Daily quota of online measurements per location plus pushes to the online address (day in Europe/Berlin). */
    @SerialName("checks_per_day") val checksPerDay: Hc2UsageLimit? = null,
    /** Maximum locations per check. */
    @SerialName("locations_per_check") val locationsPerCheck: Int? = null,
    /** SMS per day. */
    @SerialName("sms_per_day") val smsPerDay: Int? = null,
    /** Available locations. */
    val locations: List<Hc2Location> = emptyList(),
    /** Node counters. */
    val nodes: Hc2AccountNodes? = null,
    /** Number of checks. */
    val checks: Int? = null,
)

/** Request body of `PATCH /hc2/settings`; only set properties are changed. */
@Serializable
public data class Hc2SettingsUpdate(
    /** Enable quiet hours. */
    @SerialName("notify.quiet_enabled") val quietEnabled: Boolean? = null,
    /** Quiet hours start (time of day). */
    @SerialName("notify.quiet_from") val quietFrom: String? = null,
    /** Quiet hours end (time of day). */
    @SerialName("notify.quiet_to") val quietTo: String? = null,
    /** Days on which quiet hours apply. */
    @SerialName("notify.quiet_days") val quietDays: String? = null,
    /** Maximum notifications per hour. */
    @SerialName("notify.rate_per_hour") val ratePerHour: Int? = null,
    /** Number of notifications from which they are batched. */
    @SerialName("notify.batch_threshold") val batchThreshold: Int? = null,
    /** Batching window in seconds. */
    @SerialName("notify.batch_window_seconds") val batchWindowSeconds: Int? = null,
)
