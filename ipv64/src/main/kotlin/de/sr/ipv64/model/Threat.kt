package de.sr.ipv64.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Listing status of an IP. */
@Serializable
public enum class ThreatIpStatus(override val value: String) : ApiValue {
    @SerialName("listed") LISTED("listed"),
    @SerialName("not_listed") NOT_LISTED("not_listed"),
    @SerialName("released") RELEASED("released"),
    @SerialName("allowlisted") ALLOWLISTED("allowlisted"),
}

/** Status of a release request. */
@Serializable
public enum class ThreatReleaseStatus(override val value: String) : ApiValue {
    @SerialName("none") NONE("none"),
    @SerialName("in_review") IN_REVIEW("in_review"),
    @SerialName("released") RELEASED("released"),
    @SerialName("allowlisted") ALLOWLISTED("allowlisted"),
    @SerialName("rejected") REJECTED("rejected"),
}

/** Attacked port reported by at least two accounts. */
@Serializable
public data class ThreatIpPort(
    val service: String? = null,
    val port: Int? = null,
)

/** Release information of an IP. */
@Serializable
public data class ThreatIpRelease(
    /** Release status. */
    val status: ThreatReleaseStatus? = null,
    /** Release valid until (UTC); null = permanent or no release. */
    @SerialName("protected_until") val protectedUntil: IsoInstant? = null,
)

/** Information about an IP (`GET /threat/ip/{ip}`). */
@Serializable
public data class ThreatIp(
    /** Normalised address. */
    val ip: String? = null,
    /** Address family: 4 or 6. */
    val family: Int? = null,
    /** On the v64 blocklist (releases already excluded). */
    val listed: Boolean? = null,
    /** Status; `released` = temporary release from a request, `allowlisted` = allow list. */
    val status: ThreatIpStatus? = null,
    /** Trust score 0-100 (only when listed). */
    val score: Int? = null,
    /** Sub lists (ssh, web, mail, rdp, scanner, research). */
    val lists: List<String> = emptyList(),
    /** Kinds, e.g. `ssh:bruteforce`. */
    val kinds: List<String> = emptyList(),
    /** Attacked ports reported by at least two accounts (at most 10). */
    val ports: List<ThreatIpPort> = emptyList(),
    /** First sighting (day, UTC). */
    @SerialName("first_seen") val firstSeen: String? = null,
    /** Last sighting (day, UTC). */
    @SerialName("last_seen") val lastSeen: String? = null,
    /** Number of distinct reporting accounts. */
    val accounts: Long? = null,
    /** Country (ISO 3166-1) if known. */
    val country: String? = null,
    /** Autonomous system number if known. */
    val asn: Long? = null,
    /** Release information. */
    val release: ThreatIpRelease? = null,
    /** Page with explanation and release request. */
    val details: String? = null,
)

/** Ranking entry of the threat statistics. */
@Serializable
public data class ThreatStatsEntry(
    /** Value (user name, password, path, ...). */
    val value: String? = null,
    /** Sum of attempts (30 days). */
    val events: Long? = null,
    /** Distinct reporting accounts (30 days). */
    val accounts: Long? = null,
)

/** Thresholds of the statistics. */
@Serializable
public data class ThreatStatsThreshold(
    /** Minimum reporting accounts for rankings and lists. */
    val accounts: Long? = null,
    /** Accounts whose sensors reported within the last 7 days. */
    @SerialName("active_accounts_7d") val activeAccounts7d: Long? = null,
)

/** Sensor counters. */
@Serializable
public data class ThreatStatsSensors(
    @SerialName("active_7d") val active7d: Long? = null,
    /** Reported within the last 30 minutes. */
    val online: Long? = null,
)

/** Attacks in the last 24 hours. */
@Serializable
public data class ThreatStatsAttacks(
    val events: Long? = null,
    /** Distinct attacker IPs. */
    val sources: Long? = null,
)

/** Attacks per day (UTC). */
@Serializable
public data class ThreatStatsDay(
    /** Day (`yyyy-MM-dd`). */
    val day: String? = null,
    val events: Long? = null,
)

/** Attacked port. */
@Serializable
public data class ThreatStatsPort(
    val service: String? = null,
    val port: Int? = null,
    val events: Long? = null,
)

/** Attack kind. */
@Serializable
public data class ThreatStatsKind(
    val kind: String? = null,
    val events: Long? = null,
)

/** Origin country of blocklist IPs. */
@Serializable
public data class ThreatStatsCountry(
    /** ISO 3166-1 alpha-2. */
    val country: String? = null,
    /** Listed IPs from this country. */
    val ips: Long? = null,
    /** Events of these IPs. */
    val events: Long? = null,
)

/** Top 10 rankings. */
@Serializable
public data class ThreatStatsTop(
    val usernames: List<ThreatStatsEntry> = emptyList(),
    val passwords: List<ThreatStatsEntry> = emptyList(),
    val paths: List<ThreatStatsEntry> = emptyList(),
    @SerialName("user_agents") val userAgents: List<ThreatStatsEntry> = emptyList(),
    /** Fingerprints (JA4/HASSH). */
    val fingerprints: List<ThreatStatsEntry> = emptyList(),
    /** Origin countries of the v64 blocklist (top 10). */
    val countries: List<ThreatStatsCountry> = emptyList(),
)

/** Public Honeypot64 statistics overview (`GET /threat/stats`). */
@Serializable
public data class ThreatStats(
    /** Time of the cache (UTC). */
    @SerialName("generated_at") val generatedAt: IsoInstant? = null,
    /** Ranking window in days. */
    @SerialName("window_days") val windowDays: Int? = null,
    /** Thresholds. */
    val threshold: ThreatStatsThreshold? = null,
    /** Sensors. */
    val sensors: ThreatStatsSensors? = null,
    /** Attacks in 24 h. */
    @SerialName("attacks_24h") val attacks24h: ThreatStatsAttacks? = null,
    /** Attacks per day (30 days). */
    val timeline: List<ThreatStatsDay> = emptyList(),
    /** Most attacked ports (7 days). */
    @SerialName("top_ports") val topPorts: List<ThreatStatsPort> = emptyList(),
    /** Most frequent kinds (7 days). */
    @SerialName("top_kinds") val topKinds: List<ThreatStatsKind> = emptyList(),
    /** List sizes (v64, honeypot_passwords, honeypot_usernames, ...). */
    val lists: Map<String, Long> = emptyMap(),
    /** Top 10 rankings. */
    val top: ThreatStatsTop? = null,
    /** Information text. */
    val info: String? = null,
    /** License. */
    val license: String? = null,
)

/** Limits of the early warning. */
@Serializable
public data class ThreatWatchLimits(
    /** Networks per account. */
    val max: Int? = null,
    /** Shortest allowed IPv4 prefix. */
    @SerialName("min_prefix_v4") val minPrefixV4: Int? = null,
    /** Shortest allowed IPv6 prefix. */
    @SerialName("min_prefix_v6") val minPrefixV6: Int? = null,
)

/** Automatically watched DynDNS IP. */
@Serializable
public data class ThreatWatchDynDns(
    val ip: String? = null,
    val domain: String? = null,
)

/** A watched network. */
@Serializable
public data class ThreatWatchNetwork(
    /** Network ID. */
    val id: Long? = null,
    /** Normalised network (no host bits). */
    val cidr: String? = null,
    /** Label. */
    val label: String? = null,
    /** Verified: every sighting reports, otherwise only listed IPs. */
    val verified: Boolean? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
)

/** Early warning overview (`GET /threat/watch`). */
@Serializable
public data class ThreatWatch(
    /** Limits. */
    val limits: ThreatWatchLimits? = null,
    /** Watched networks. */
    val networks: List<ThreatWatchNetwork> = emptyList(),
    /** Automatically watched DynDNS IPs. */
    val dyndns: List<ThreatWatchDynDns> = emptyList(),
)

/** Request body of `POST /threat/watch`. */
@Serializable
public data class AddThreatWatchRequest(
    /** Network in CIDR notation or a single IP (IPv4 up to /16, IPv6 up to /32). */
    val cidr: String,
    /** Optional label. */
    val label: String? = null,
)

/** Observation behind an alert. */
@Serializable
public data class ThreatWatchAlertSeen(
    /** Origin: honeypot, log or syslog. */
    val origin: String? = null,
    val service: String? = null,
    val port: Int? = null,
    val kind: String? = null,
    /** Rule of the log sensor. */
    val rule: String? = null,
    val count: Long? = null,
    val sensors: Long? = null,
    val last: IsoInstant? = null,
)

/** Why an IP was flagged (last 14 days). */
@Serializable
public data class ThreatWatchAlertDetails(
    /** Reporting accounts. */
    val accounts: Long? = null,
    val sensors: Long? = null,
    val events: Long? = null,
    val lists: List<String> = emptyList(),
    /** Reason, e.g. threshold, customer_ip, allowlist. */
    val reason: String? = null,
    /** Most frequent observations (at most 8). */
    val seen: List<ThreatWatchAlertSeen> = emptyList(),
)

/** An early warning alert. */
@Serializable
public data class ThreatWatchAlert(
    /** Alert ID. */
    val id: Long? = null,
    /** Attacker IP. */
    val ip: String? = null,
    /** Network, or the IP as /32 or /128 for DynDNS. */
    val cidr: String? = null,
    /** Network ID, null for DynDNS. */
    @SerialName("network_id") val networkId: Long? = null,
    /** Source: `network` or `dyndns`. */
    val source: String? = null,
    /** Label of the network or domain. */
    val label: String? = null,
    /** On the v64 blocklist. */
    val listed: Boolean? = null,
    /** Trust score. */
    val score: Int? = null,
    val kinds: List<String> = emptyList(),
    /** First sighting (day, UTC). */
    @SerialName("first_seen") val firstSeen: String? = null,
    /** Last sighting (day, UTC). */
    @SerialName("last_seen") val lastSeen: String? = null,
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Details. */
    val details: ThreatWatchAlertDetails? = null,
)

/** Alerts plus cursor (`GET /threat/watch/alerts`). */
@Serializable
public data class ThreatWatchAlerts(
    /** Alerts, oldest first (at most 100). */
    val alerts: List<ThreatWatchAlert> = emptyList(),
    /** Cursor for the next call; pass unchanged as `since`. */
    val next: String? = null,
)

/** Action of a feed change. */
@Serializable
public enum class ThreatFeedAction(override val value: String) : ApiValue {
    @SerialName("add") ADD("add"),
    @SerialName("remove") REMOVE("remove"),
    @SerialName("update") UPDATE("update"),
}

/** A change of the v64 blocklist. */
@Serializable
public data class ThreatFeedChange(
    val ip: String? = null,
    /** `add` = newly listed, `remove` = no longer listed, `update` = other sub lists. */
    val action: ThreatFeedAction? = null,
    /** Lists afterwards; empty for remove. */
    val lists: List<String> = emptyList(),
    /** Lists before; empty for add. */
    @SerialName("previous_lists") val previousLists: List<String> = emptyList(),
    /** Time of the evaluation (UTC). */
    val at: IsoInstant? = null,
)

/** Change feed of the v64 blocklist (`GET /threat/feed`). */
@Serializable
public data class ThreatFeed(
    /** Changes, oldest first (at most 10 000). */
    val changes: List<ThreatFeedChange> = emptyList(),
    /** Opaque cursor; pass unchanged as `since`. */
    val next: String? = null,
    /** More changes are ready: call again immediately with [next]. */
    val more: Boolean? = null,
)

/** Result of `GET /threat/feed` with ETag support. */
public data class ThreatFeedResult(
    /** The feed, or `null` if the server answered `304 Not Modified`. */
    val feed: ThreatFeed?,
    /** ETag of the response; pass it as `ifNoneMatch` together with the same `since` on the next call. */
    val etag: String?,
) {
    /** True if nothing changed since the given ETag (HTTP 304). */
    val notModified: Boolean get() = feed == null
}

/** One line of a password range response: the remaining 35 hex characters of a SHA-1 hash and a count. */
public data class ThreatPasswordHash(
    /** Remaining 35 uppercase hex characters of the SHA-1 hash. */
    val suffix: String,
    /** Number of times seen; 0 marks a padding line. */
    val count: Long,
)
