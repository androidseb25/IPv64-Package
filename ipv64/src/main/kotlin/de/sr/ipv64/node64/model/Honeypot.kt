package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IsoInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Profile of the trap services. */
@Serializable
public enum class HoneypotProfile(override val value: String) : ApiValue {
    @SerialName("minimal") MINIMAL("minimal"),
    @SerialName("standard") STANDARD("standard"),
    @SerialName("full") FULL("full"),
    @SerialName("custom") CUSTOM("custom"),
}

/** Fake web page shown by a trap service. */
@Serializable
public enum class HoneypotPage(override val value: String) : ApiValue {
    @SerialName("generic") GENERIC("generic"),
    @SerialName("wp-login") WP_LOGIN("wp-login"),
    @SerialName("phpmyadmin") PHPMYADMIN("phpmyadmin"),
    @SerialName("admin") ADMIN("admin"),
}

/** Sensor an event comes from. */
@Serializable
public enum class HoneypotEventSource(override val value: String) : ApiValue {
    @SerialName("honeypot") HONEYPOT("honeypot"),
    @SerialName("log") LOG("log"),
    @SerialName("syslog") SYSLOG("syslog"),
}

/** Transport protocol of a trap service. */
@Serializable
public enum class HoneypotProto(override val value: String) : ApiValue {
    @SerialName("tcp") TCP("tcp"),
    @SerialName("udp") UDP("udp"),
}

/** Group of a trap service in the catalog. */
@Serializable
public enum class HoneypotGroup(override val value: String) : ApiValue {
    @SerialName("remote") REMOTE("remote"),
    @SerialName("web") WEB("web"),
    @SerialName("mail") MAIL("mail"),
    @SerialName("db") DB("db"),
    @SerialName("iot") IOT("iot"),
    @SerialName("other") OTHER("other"),
}

/** State of a port in the catalog. */
@Serializable
public enum class HoneypotPortState(override val value: String) : ApiValue {
    @SerialName("free") FREE("free"),
    @SerialName("open") OPEN("open"),
    @SerialName("error") ERROR("error"),
    @SerialName("used") USED("used"),
    @SerialName("reserved") RESERVED("reserved"),
}

/** Format of a watched log file. */
@Serializable
public enum class HoneypotLogFormat(override val value: String) : ApiValue {
    @SerialName("") NONE(""),
    @SerialName("auto") AUTO("auto"),
    @SerialName("syslog") SYSLOG("syslog"),
    @SerialName("nginx") NGINX("nginx"),
    @SerialName("apache") APACHE("apache"),
    @SerialName("caddy") CADDY("caddy"),
    @SerialName("nextcloud") NEXTCLOUD("nextcloud"),
    @SerialName("vaultwarden") VAULTWARDEN("vaultwarden"),
    @SerialName("gitea") GITEA("gitea"),
    @SerialName("proxmox") PROXMOX("proxmox"),
    @SerialName("homeassistant") HOMEASSISTANT("homeassistant"),
}

/** Group of an event on the live map. */
@Serializable
public enum class HoneypotLiveGroup(override val value: String) : ApiValue {
    @SerialName("login") LOGIN("login"),
    @SerialName("exploit") EXPLOIT("exploit"),
    @SerialName("scan") SCAN("scan"),
    @SerialName("probe") PROBE("probe"),
    @SerialName("log") LOG("log"),
}

/** Trap service in the configuration. */
@Serializable
public data class Honeypot64ServiceConfig(
    /** Service name. */
    val service: String,
    /** Port. */
    val port: Int,
    /** Fake web page (web services). */
    val page: HoneypotPage? = null,
)

/** Watched log file. */
@Serializable
public data class Honeypot64LogFile(
    /** Path of the file. */
    val path: String,
    /** Format of the file. */
    val format: HoneypotLogFormat? = null,
)

/** Log sensor configuration. */
@Serializable
public data class Honeypot64LogConfig(
    /** Watched log files. */
    val files: List<Honeypot64LogFile> = emptyList(),
    /** Port of the syslog receiver. */
    @SerialName("syslog_port") val syslogPort: Int? = null,
    /** Sources allowed to send syslog messages. */
    @SerialName("syslog_allow") val syslogAllow: List<String> = emptyList(),
)

/** Settings of the login-attack detection. */
@Serializable
public data class Honeypot64LoginConfig(
    /** Detection switched off. */
    val off: Boolean? = null,
    /** Number of failures that count as an attack. */
    val threshold: Int? = null,
    /** Time window in minutes. */
    @SerialName("window_min") val windowMin: Int? = null,
    /** Do not notify. */
    @SerialName("no_notify") val noNotify: Boolean? = null,
)

/** Settings of the honeypot (response of `GET /honeypot/config` and `PUT /honeypot/config`). */
@Serializable
public data class Honeypot64Config(
    /** Profile. */
    val profile: HoneypotProfile? = null,
    /** Services (with profile `custom`). */
    val services: List<Honeypot64ServiceConfig> = emptyList(),
    /** Detect port scans. */
    val scan: Boolean? = null,
    /** Maximum connections per IP. */
    @SerialName("max_conns_per_ip") val maxConnsPerIp: Int? = null,
    /** Maximum connections in total. */
    @SerialName("max_conns_total") val maxConnsTotal: Int? = null,
    /** Days events are kept. */
    @SerialName("retention_days") val retentionDays: Int? = null,
    /** Maximum number of stored events. */
    @SerialName("max_events") val maxEvents: Int? = null,
    /** Notify about new sources. */
    @SerialName("notify_new") val notifyNew: Boolean? = null,
    /** Report sources to ipv64. */
    val report: Boolean? = null,
    /** `true` = do not block detected attackers locally in PROXY64 (default `false`: blocked for all hosts and streams). */
    @SerialName("no_local_block") val noLocalBlock: Boolean? = null,
    /** Own country (ISO 3166 alpha-2). */
    val country: String? = null,
    /** Log sensors. */
    val log: Honeypot64LogConfig? = null,
    /** Certificate ID for TLS services. */
    @SerialName("cert_id") val certId: Long? = null,
    /** Ports served with the certificate. */
    @SerialName("cert_ports") val certPorts: List<Int> = emptyList(),
    /** Login-attack detection. */
    val login: Honeypot64LoginConfig? = null,
)

/**
 * Body of `PUT /honeypot/config`. Unset (`null`) fields are not sent.
 * Enabling [report] starts the reported period now.
 */
@Serializable
public data class Honeypot64ConfigRequest(
    /** Profile. */
    val profile: HoneypotProfile? = null,
    /** Services (with profile `custom`). */
    val services: List<Honeypot64ServiceConfig>? = null,
    /** Detect port scans. */
    val scan: Boolean? = null,
    /** Maximum connections per IP. */
    @SerialName("max_conns_per_ip") val maxConnsPerIp: Int? = null,
    /** Maximum connections in total. */
    @SerialName("max_conns_total") val maxConnsTotal: Int? = null,
    /** Days events are kept. */
    @SerialName("retention_days") val retentionDays: Int? = null,
    /** Maximum number of stored events. */
    @SerialName("max_events") val maxEvents: Int? = null,
    /** Notify about new sources. */
    @SerialName("notify_new") val notifyNew: Boolean? = null,
    /** Report sources to ipv64. */
    val report: Boolean? = null,
    /** `true` = do not block detected attackers locally in PROXY64 (default `false`: blocked for all hosts and streams). */
    @SerialName("no_local_block") val noLocalBlock: Boolean? = null,
    /** Own country (ISO 3166 alpha-2). */
    val country: String? = null,
    /** Log sensors. */
    val log: Honeypot64LogConfig? = null,
    /** Certificate ID for TLS services. */
    @SerialName("cert_id") val certId: Long? = null,
    /** Ports served with the certificate. */
    @SerialName("cert_ports") val certPorts: List<Int>? = null,
    /** Login-attack detection. */
    val login: Honeypot64LoginConfig? = null,
)

/** Entry of the service catalog. */
@Serializable
public data class Honeypot64CatalogItem(
    /** Service name. */
    val service: String? = null,
    /** Selected port. */
    val port: Int? = null,
    /** Default port. */
    @SerialName("default_port") val defaultPort: Int? = null,
    /** Fake web page. */
    val page: String? = null,
    /** Protocol. */
    val proto: HoneypotProto? = null,
    /** Group. */
    val group: HoneypotGroup? = null,
    /** Description. */
    val description: String? = null,
    /** Profiles containing the service. */
    val profiles: List<String> = emptyList(),
    /** Service is selected. */
    val on: Boolean? = null,
    /** State of the port. */
    val state: HoneypotPortState? = null,
    /** Part of the node that uses the port. */
    val owner: String? = null,
    /** Purpose of the port. */
    val purpose: String? = null,
    /** Error message. */
    val error: String? = null,
    /** Connections since start. */
    val conns: Long? = null,
)

/** Response of `GET /honeypot/catalog`. */
@Serializable
public data class Honeypot64Catalog(
    /** Saved profile. */
    val profile: String? = null,
    /** The sensor is running. */
    val running: Boolean? = null,
    /** Catalog entries. */
    val entries: List<Honeypot64CatalogItem> = emptyList(),
    /** Selected services that are not in the catalog. */
    val extra: List<Honeypot64CatalogItem> = emptyList(),
    /** Available service names. */
    val services: List<String> = emptyList(),
    /** Available fake pages. */
    val pages: List<String> = emptyList(),
    /** Available groups. */
    val groups: List<String> = emptyList(),
    /** Services per preset profile. */
    val presets: Map<String, List<Honeypot64ServiceConfig>> = emptyMap(),
)

/** Trap service in [Honeypot64Status]. */
@Serializable
public data class Honeypot64Service(
    /** Service name. */
    val service: String? = null,
    /** Port. */
    val port: Int? = null,
    /** The port is open. */
    val listen: Boolean? = null,
    /** Error message. */
    val error: String? = null,
    /** Connections since start. */
    val conns: Long? = null,
)

/** Hint about a port in [Honeypot64Status]. */
@Serializable
public data class Honeypot64Warning(
    /** Service name. */
    val service: String? = null,
    /** Port. */
    val port: Int? = null,
    /** Part of the node that uses the port. */
    val owner: String? = null,
    /** Purpose of the port. */
    val purpose: String? = null,
    /** The profile left the port out. */
    val skipped: Boolean? = null,
)

/** Counters of the sensor. */
@Serializable
public data class Honeypot64Counters(
    /** Connections. */
    val connections: Long? = null,
    /** Login attempts. */
    val logins: Long? = null,
    /** Events. */
    val events: Long? = null,
    /** Dropped events. */
    val dropped: Long? = null,
)

/** Status of reporting to ipv64. */
@Serializable
public data class Honeypot64ReportStatus(
    /** Reporting is switched on. */
    val enabled: Boolean? = null,
    /** A key is set. */
    @SerialName("key_set") val keySet: Boolean? = null,
    /** Reporting is active. */
    val active: Boolean? = null,
    /** Sensor ID. */
    @SerialName("sensor_id") val sensorId: String? = null,
    /** Last successful report. */
    @SerialName("last_success") val lastSuccess: IsoInstant? = null,
    /** End of the last reported period. */
    @SerialName("last_end") val lastEnd: IsoInstant? = null,
    /** Last error. */
    @SerialName("last_error") val lastError: String? = null,
    /** Time of the last error. */
    @SerialName("last_error_at") val lastErrorAt: IsoInstant? = null,
    /** Next attempt. */
    @SerialName("next_attempt") val nextAttempt: IsoInstant? = null,
    /** Number of errors. */
    val errors: Int? = null,
    /** Sources reported today. */
    @SerialName("sources_today") val sourcesToday: Int? = null,
    /** Sources reported in total. */
    @SerialName("sources_total") val sourcesTotal: Int? = null,
    /** Sources rejected in the last report. */
    @SerialName("last_rejected") val lastRejected: Int? = null,
    /** Reasons for the rejections with counts. */
    @SerialName("last_rejected_reasons") val lastRejectedReasons: Map<String, Int> = emptyMap(),
)

/** State of a watched log file. */
@Serializable
public data class Honeypot64LogFileStatus(
    /** Path. */
    val path: String? = null,
    /** Format. */
    val format: String? = null,
    /** File can be read. */
    val ok: Boolean? = null,
    /** Error message. */
    val error: String? = null,
    /** Lines read. */
    val lines: Long? = null,
    /** Last line read. */
    val last: IsoInstant? = null,
)

/** State of the syslog receiver. */
@Serializable
public data class Honeypot64SyslogStatus(
    /** Port. */
    val port: Int? = null,
    /** UDP is open. */
    val udp: Boolean? = null,
    /** TCP is open. */
    val tcp: Boolean? = null,
    /** Error message. */
    val error: String? = null,
    /** Messages received. */
    val messages: Long? = null,
    /** Messages denied. */
    val denied: Long? = null,
)

/** State of the log sensors. */
@Serializable
public data class Honeypot64LogStatus(
    /** Watched files. */
    val files: List<Honeypot64LogFileStatus> = emptyList(),
    /** Syslog receiver. */
    val syslog: Honeypot64SyslogStatus? = null,
    /** Lines read. */
    val lines: Long? = null,
    /** Lines that matched. */
    val hits: Long? = null,
    /** Events created. */
    val events: Long? = null,
)

/** Response of `GET /honeypot/status`. */
@Serializable
public data class Honeypot64Status(
    /** Version. */
    val version: String? = null,
    /** The sensor is running. */
    val running: Boolean? = null,
    /** Start time. */
    @SerialName("started_at") val startedAt: IsoInstant? = null,
    /** Profile. */
    val profile: String? = null,
    /** Port scan detection is on. */
    val scan: Boolean? = null,
    /** Trap services. */
    val services: List<Honeypot64Service> = emptyList(),
    /** Hints about ports. */
    val warnings: List<Honeypot64Warning> = emptyList(),
    /** Counters of the sensor. */
    val counters: Honeypot64Counters? = null,
    /** Events in the buffer. */
    val buffered: Long? = null,
    /** Dropped events. */
    val dropped: Long? = null,
    /** Reporting to ipv64. */
    val report: Honeypot64ReportStatus? = null,
    /** Log sensors. */
    val logs: Honeypot64LogStatus? = null,
)

/** Ranking entry. */
@Serializable
public data class Honeypot64Count(
    /** Value (service, port, source, user name, …). */
    val key: String? = null,
    /** Count. */
    val count: Long? = null,
    /** Last occurrence. */
    val last: IsoInstant? = null,
    /** Country. */
    val country: String? = null,
)

/** Ranking entry per country. */
@Serializable
public data class Honeypot64CountryCount(
    /** Country code. */
    val key: String? = null,
    /** Events. */
    val count: Long? = null,
    /** Distinct sources. */
    val sources: Long? = null,
    /** Last occurrence. */
    val last: IsoInstant? = null,
)

/** Point of the timeline. */
@Serializable
public data class Honeypot64TimelinePoint(
    /** Start of the interval. */
    val time: IsoInstant? = null,
    /** Events in the interval. */
    val events: Long? = null,
)

/** Response of `GET /honeypot/stats`. */
@Serializable
public data class Honeypot64Stats(
    /** Start of the period. */
    val since: IsoInstant? = null,
    /** Events. */
    val events: Long? = null,
    /** Distinct sources. */
    val sources: Long? = null,
    /** Login attempts. */
    val logins: Long? = null,
    /** Events per service. */
    @SerialName("by_service") val byService: List<Honeypot64Count> = emptyList(),
    /** Events per port. */
    @SerialName("by_port") val byPort: List<Honeypot64Count> = emptyList(),
    /** Events per kind. */
    @SerialName("by_kind") val byKind: List<Honeypot64Count> = emptyList(),
    /** Top sources. */
    @SerialName("top_sources") val topSources: List<Honeypot64Count> = emptyList(),
    /** Top user names. */
    @SerialName("top_users") val topUsers: List<Honeypot64Count> = emptyList(),
    /** Top passwords. */
    @SerialName("top_passwords") val topPasswords: List<Honeypot64Count> = emptyList(),
    /** Top credential pairs. */
    @SerialName("top_credentials") val topCredentials: List<Honeypot64Count> = emptyList(),
    /** Top requested paths. */
    @SerialName("top_paths") val topPaths: List<Honeypot64Count> = emptyList(),
    /** Top user agents. */
    @SerialName("top_user_agents") val topUserAgents: List<Honeypot64Count> = emptyList(),
    /** Top fingerprints. */
    @SerialName("top_fingerprints") val topFingerprints: List<Honeypot64Count> = emptyList(),
    /** Timeline (per hour up to 48 hours, otherwise per day). */
    val timeline: List<Honeypot64TimelinePoint> = emptyList(),
    /** Events per source. */
    @SerialName("by_source") val bySource: List<Honeypot64Count> = emptyList(),
    /** Events per country. */
    @SerialName("by_country") val byCountry: List<Honeypot64CountryCount> = emptyList(),
    /** Events with unknown country. */
    @SerialName("country_unknown") val countryUnknown: Long? = null,
    /** Events per decoy. */
    @SerialName("by_decoy") val byDecoy: List<Honeypot64Count> = emptyList(),
)

/** Event recorded by the honeypot (credentials of attackers in plain text). */
@Serializable
public data class Honeypot64Event(
    /** Time. */
    val time: IsoInstant? = null,
    /** Service. */
    val service: String? = null,
    /** Target port. */
    val port: Int? = null,
    /** Kind of the event. */
    val kind: String? = null,
    /** Source IP. */
    val src: String? = null,
    /** Source port. */
    @SerialName("src_port") val srcPort: Int? = null,
    /** Country of the source. */
    val country: String? = null,
    /** User name tried. */
    val username: String? = null,
    /** Password tried. */
    val password: String? = null,
    /** HTTP method. */
    val method: String? = null,
    /** Requested path. */
    val path: String? = null,
    /** Host header. */
    val host: String? = null,
    /** User agent. */
    @SerialName("user_agent") val userAgent: String? = null,
    /** Client software. */
    val client: String? = null,
    /** Fake page that was shown. */
    val page: String? = null,
    /** Details. */
    val detail: String? = null,
    /** Payload as hex. */
    @SerialName("payload_hex") val payloadHex: String? = null,
    /** Payload as escaped ASCII text. */
    @SerialName("payload_text") val payloadText: String? = null,
    /** Sensor. */
    val source: HoneypotEventSource? = null,
    /** Matching rule (log sensors). */
    val rule: String? = null,
    /** Fingerprint. */
    val fingerprint: String? = null,
    /** TLS server name. */
    val sni: String? = null,
)

/** Response of `DELETE /honeypot/events`. */
@Serializable
public data class HoneypotEventsDeleted(
    /** Number of deleted events. */
    val deleted: Long? = null,
)

/** Response of `GET /honeypot/live`. */
@Serializable
public data class Honeypot64Live(
    /** Period in minutes. */
    val minutes: Int? = null,
    /** Current Unix time. */
    val now: Long? = null,
    /** Target of the arcs (country of the node's public IPv4). */
    val target: Honeypot64LiveTarget? = null,
    /** Events with known country, newest first (at most 500). */
    val events: List<Honeypot64LiveEvent> = emptyList(),
)

/** Target of the arcs on the live map. x/y are coordinates of the country map (viewBox 0 0 1000 436). */
@Serializable
public data class Honeypot64LiveTarget(
    /** Country code. */
    val cc: String? = null,
    /** Flag emoji. */
    val flag: String? = null,
    /** Country name. */
    val name: String? = null,
    /** X coordinate. */
    val x: Double? = null,
    /** Y coordinate. */
    val y: Double? = null,
    /** Country is known. */
    val known: Boolean? = null,
    /** Country was set manually. */
    val manual: Boolean? = null,
)

/** Event on the live map. x/y are coordinates of the country map (viewBox 0 0 1000 436). */
@Serializable
public data class Honeypot64LiveEvent(
    /** Unix time. */
    val t: Long? = null,
    /** Country code. */
    val cc: String? = null,
    /** Flag emoji. */
    val flag: String? = null,
    /** Country name. */
    val name: String? = null,
    /** X coordinate. */
    val x: Double? = null,
    /** Y coordinate. */
    val y: Double? = null,
    /** Source IP. */
    val ip: String? = null,
    /** Kind of the event. */
    val kind: String? = null,
    /** Group. */
    val group: HoneypotLiveGroup? = null,
    /** Service. */
    val service: String? = null,
    /** Port. */
    val port: Int? = null,
    /** Sensor. */
    val source: String? = null,
)
