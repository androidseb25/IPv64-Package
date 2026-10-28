package de.sr.ipv64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.DataEnvelope
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.data
import de.sr.ipv64.internal.query
import de.sr.ipv64.model.AddThreatWatchRequest
import de.sr.ipv64.model.ThreatFeed
import de.sr.ipv64.model.ThreatFeedResult
import de.sr.ipv64.model.ThreatIp
import de.sr.ipv64.model.ThreatPasswordHash
import de.sr.ipv64.model.ThreatStats
import de.sr.ipv64.model.ThreatWatch
import de.sr.ipv64.model.ThreatWatchAlerts
import de.sr.ipv64.model.ThreatWatchNetwork
import java.security.MessageDigest

/** Tag *Threat*: threat intelligence from Honeypot64 (IP lookup, statistics, early warning, feed, password check). */
public class ThreatApi internal constructor(private val transport: HttpTransport) {

    /** Information about one public IPv4/IPv6 address: why is it listed? `GET /threat/ip/{ip}` */
    public suspend fun ip(ip: String): ThreatIp =
        transport.data(Method.GET, ApiPath("threat", "ip", ip))

    /** Public Honeypot64 statistics overview (cached up to 10 minutes). `GET /threat/stats` */
    public suspend fun stats(): ThreatStats =
        transport.data(Method.GET, ApiPath("threat", "stats"))

    /** Early warning: limits, watched networks and automatically watched DynDNS IPs. `GET /threat/watch` */
    public suspend fun watch(): ThreatWatch =
        transport.data(Method.GET, ApiPath("threat", "watch"))

    /**
     * Adds a public network (CIDR) or single IP to the early warning; host bits are removed.
     * `POST /threat/watch`
     */
    public suspend fun addWatch(request: AddThreatWatchRequest): ThreatWatchNetwork =
        transport.data(Method.POST, ApiPath("threat", "watch"), body = transport.body(request))

    /** Convenience overload of [addWatch]. `POST /threat/watch` */
    public suspend fun addWatch(cidr: String, label: String? = null): ThreatWatchNetwork =
        addWatch(AddThreatWatchRequest(cidr, label))

    /** Removes a watched network; earlier alerts are kept. `DELETE /threat/watch/{id}` */
    public suspend fun deleteWatch(id: Long) {
        transport.unit(Method.DELETE, ApiPath("threat", "watch", id.toString()))
    }

    /**
     * Early warning alerts, oldest first (at most 100 per call). Pass [ThreatWatchAlerts.next] unchanged as
     * [since] on the next call and discard duplicate IDs. `GET /threat/watch/alerts`
     */
    public suspend fun watchAlerts(since: String? = null): ThreatWatchAlerts =
        transport.data(Method.GET, ApiPath("threat", "watch", "alerts"), query("since" to since))

    /**
     * Change feed of the v64 blocklist since the cursor [since] (without it only the current cursor is returned).
     * Supports ETag: pass the ETag of the previous response with the same [since] as [ifNoneMatch]; if nothing
     * changed, [ThreatFeedResult.feed] is `null` (HTTP 304). A too old cursor yields HTTP 410.
     * `GET /threat/feed`
     */
    public suspend fun feed(since: String? = null, ifNoneMatch: String? = null): ThreatFeedResult {
        val raw = transport.raw(
            Method.GET,
            ApiPath("threat", "feed"),
            query("since" to since),
            headers = if (ifNoneMatch != null) mapOf("If-None-Match" to ifNoneMatch) else emptyMap(),
        )
        val etag = raw.headers["ETag"]
        if (raw.code == 304) return ThreatFeedResult(feed = null, etag = etag ?: ifNoneMatch)
        val feed = transport.decode(DataEnvelope.serializer(ThreatFeed.serializer()), raw.body).data
        return ThreatFeedResult(feed = feed, etag = etag)
    }

    /**
     * k-anonymity password check (like HIBP): returns the raw `text/plain` body with lines
     * `<35 hex chars>:<count>` for the SHA-1 prefix [prefix] (first 5 hex characters). Lines with count 0 are padding.
     * `GET /threat/passwords/range/{prefix}`
     */
    public suspend fun passwordRangeRaw(prefix: String): String =
        transport.text(Method.GET, ApiPath("threat", "passwords", "range", prefix))

    /** Like [passwordRangeRaw], but parsed into [ThreatPasswordHash] entries (padding lines included, count 0). */
    public suspend fun passwordRange(prefix: String): List<ThreatPasswordHash> =
        passwordRangeRaw(prefix).lineSequence().mapNotNull { line ->
            val trimmed = line.trim()
            val idx = trimmed.indexOf(':')
            if (idx <= 0) return@mapNotNull null
            val count = trimmed.substring(idx + 1).toLongOrNull() ?: return@mapNotNull null
            ThreatPasswordHash(trimmed.substring(0, idx).uppercase(), count)
        }.toList()

    /**
     * Checks a password locally via SHA-1: only the first 5 hex characters of the hash are sent. Returns how often
     * attackers tried it at Honeypot64 sensors (0 = not found).
     */
    public suspend fun passwordCount(password: String): Long {
        val digest = MessageDigest.getInstance("SHA-1").digest(password.toByteArray(Charsets.UTF_8))
        val hex = digest.joinToString("") { "%02X".format(it) }
        val suffix = hex.substring(5)
        return passwordRange(hex.substring(0, 5)).firstOrNull { it.suffix == suffix }?.count ?: 0L
    }
}
