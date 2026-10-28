package de.sr.ipv64.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Sensor profile. */
@Serializable
public enum class HoneypotProfile(override val value: String) : ApiValue {
    @SerialName("minimal") MINIMAL("minimal"),
    @SerialName("standard") STANDARD("standard"),
    @SerialName("full") FULL("full"),
    @SerialName("custom") CUSTOM("custom"),
    @SerialName("proxy") PROXY("proxy"),
}

/** Sensor operating mode. */
@Serializable
public enum class HoneypotMode(override val value: String) : ApiValue {
    @SerialName("module") MODULE("module"),
    @SerialName("headless") HEADLESS("headless"),
}

/** Origin of a reported source. */
@Serializable
public enum class HoneypotSourceOrigin(override val value: String) : ApiValue {
    @SerialName("honeypot") HONEYPOT("honeypot"),
    @SerialName("log") LOG("log"),
    @SerialName("syslog") SYSLOG("syslog"),
}

/** Sensor description inside a [HoneypotReport]. */
@Serializable
public data class HoneypotReportSensor(
    /** Sensor UUID, fixed per installation (not the IP). */
    val id: String,
    /** Sensor version. */
    val version: String,
    /** Profile. */
    val profile: HoneypotProfile,
    /** Mode. */
    val mode: HoneypotMode,
    /** Configured location of the sensor (ISO 3166-1 alpha-2), only for the live map; missing = automatic. */
    val country: String? = null,
)

/** Reporting period (UTC, start inclusive, end exclusive; at most 7 days, end at most 5 min in the future). */
@Serializable
public data class HoneypotReportPeriod(
    /** Start (inclusive). */
    val start: IsoInstant,
    /** End (exclusive). */
    val end: IsoInstant,
)

/** An attacking source reported by a sensor. */
@Serializable
public data class HoneypotReportSource(
    /** Public IPv4/IPv6 address. */
    val ip: String,
    /** Kind from a fixed list, e.g. `ssh:bruteforce`, `http:exploit:sqli`, `generic:portscan`. */
    val kind: String,
    /** Service. */
    val service: String,
    /** Attacked port. */
    val port: Int,
    /** Number of attempts. */
    val count: Int,
    /** First sighting. */
    val first: IsoInstant,
    /** Last sighting. */
    val last: IsoInstant,
    /** Origin of the observation. */
    val source: HoneypotSourceOrigin,
    /** Rule of the log sensor. */
    val rule: String? = null,
    /** Details. */
    val detail: String? = null,
)

/** Credential sample. */
@Serializable
public data class HoneypotCredentialSample(
    val service: String,
    val user: String,
    val pass: String,
    val count: Int,
)

/** Counted key sample (path, user agent, fingerprint). */
@Serializable
public data class HoneypotKeySample(
    val key: String,
    val count: Int,
)

/** Payload sample. */
@Serializable
public data class HoneypotPayloadSample(
    /** SHA-256 of the payload. */
    val sha256: String,
    /** First bytes of the payload (at most 256 bytes). */
    val head: String,
    val service: String,
    val port: Int,
    val count: Int,
)

/** Samples of a report (honeypot only), stored as daily sums. */
@Serializable
public data class HoneypotReportSamples(
    /** Credentials tried. */
    val credentials: List<HoneypotCredentialSample>? = null,
    /** Requested paths. */
    val paths: List<HoneypotKeySample>? = null,
    /** User agents. */
    @SerialName("user_agents") val userAgents: List<HoneypotKeySample>? = null,
    /** Payloads. */
    val payloads: List<HoneypotPayloadSample>? = null,
    /** Tool fingerprints (optional); keys not matching the pattern are discarded. */
    val fingerprints: List<HoneypotKeySample>? = null,
)

/** Report of a Honeypot64 sensor (`POST /honeypot/reports`). */
@Serializable
public data class HoneypotReport(
    /** Report schema version. */
    val schema: Int,
    /** `<sensor.id>-<period.start unix>`. */
    @SerialName("report_id") val reportId: String,
    /** The reporting sensor. */
    val sensor: HoneypotReportSensor,
    /** Reporting period. */
    val period: HoneypotReportPeriod,
    /** Attacking sources. */
    val sources: List<HoneypotReportSource>,
    /** Samples. */
    val samples: HoneypotReportSamples? = null,
    /** Number of entries the sensor dropped because of limits. */
    val dropped: Int? = null,
)

/** Result of `POST /honeypot/reports` (HTTP 202, or 200 with [duplicate] for a known report). */
@Serializable
public data class HoneypotReportAccepted(
    /** Number of stored sources. */
    val accepted: Long? = null,
    /** Number of discarded sources/samples. */
    val rejected: Long? = null,
    /** Discard reasons with counts (not_public, own_ip, unknown_kind, ...). */
    @SerialName("rejected_reasons") val rejectedReasons: Map<String, Long> = emptyMap(),
    /** Report ID. */
    @SerialName("report_id") val reportId: String? = null,
    /** True if the report ID was already known (counts as success). */
    val duplicate: Boolean? = null,
)

/** A sensor of the account. The IP of a sensor is never returned. */
@Serializable
public data class HoneypotSensor(
    /** Sensor UUID. */
    val id: String? = null,
    /** Version. */
    val version: String? = null,
    /** Profile. */
    val profile: String? = null,
    /** Mode. */
    val mode: String? = null,
    /** First report. */
    @SerialName("first_seen") val firstSeen: IsoInstant? = null,
    /** Last report. */
    @SerialName("last_seen") val lastSeen: IsoInstant? = null,
    /** Reported within the last 30 minutes. */
    val online: Boolean? = null,
    /** Number of reports. */
    val reports: Long? = null,
    /** Whether the sensor is disabled. */
    val disabled: Boolean? = null,
    /** Distinct source IPs in 24 h. */
    @SerialName("sources_24h") val sources24h: Long? = null,
    /** Distinct source IPs in 7 d. */
    @SerialName("sources_7d") val sources7d: Long? = null,
    /** Events in 24 h. */
    @SerialName("events_24h") val events24h: Long? = null,
    /** Events in 7 d. */
    @SerialName("events_7d") val events7d: Long? = null,
)

/** Top attack kind of the own contribution. */
@Serializable
public data class HoneypotKindStat(
    val kind: String? = null,
    val events: Long? = null,
    val sources: Long? = null,
)

/** Top attacked port of the own contribution. */
@Serializable
public data class HoneypotPortStat(
    val service: String? = null,
    val port: Int? = null,
    val events: Long? = null,
    val sources: Long? = null,
)

/** Events per day (UTC). */
@Serializable
public data class HoneypotDayStat(
    /** Day (`yyyy-MM-dd`). */
    val day: String? = null,
    val events: Long? = null,
)

/** Own honeypot contribution (`GET /honeypot/stats`). */
@Serializable
public data class HoneypotStats(
    /** Number of sensors. */
    val sensors: Long? = null,
    /** Number of online sensors. */
    @SerialName("sensors_online") val sensorsOnline: Long? = null,
    /** At least one sensor reported within the last 7 days. */
    val contributor: Boolean? = null,
    /** Time of the last report. */
    @SerialName("last_report") val lastReport: IsoInstant? = null,
    /** Events in 24 h. */
    @SerialName("events_24h") val events24h: Long? = null,
    /** Events in 7 d. */
    @SerialName("events_7d") val events7d: Long? = null,
    /** Distinct source IPs in 24 h. */
    @SerialName("sources_24h") val sources24h: Long? = null,
    /** Distinct source IPs in 7 d. */
    @SerialName("sources_7d") val sources7d: Long? = null,
    /** Top attack kinds (7 days). */
    @SerialName("top_kinds") val topKinds: List<HoneypotKindStat> = emptyList(),
    /** Top ports (7 days). */
    @SerialName("top_ports") val topPorts: List<HoneypotPortStat> = emptyList(),
    /** Events per day (UTC, 7 days). */
    val timeline: List<HoneypotDayStat> = emptyList(),
)

/** Result of `GET /honeypot/list-token`. [token] and [base] (which contains the token) are redacted in [toString]. */
@Serializable
public data class HoneypotListToken(
    /** Personal list token; keep it secret. */
    val token: String? = null,
    /** Whether live access is currently permitted (otherwise the daily state is served). */
    val live: Boolean? = null,
    /** Base URL containing the token (`https://ipv64.net/blocklists/live/<token>/`); append the file name, e.g. `ipv64_blocklist_v64.txt`. */
    val base: String? = null,
) {
    override fun toString(): String = "HoneypotListToken(token=<redacted>, live=$live, base=<redacted>)"
}
