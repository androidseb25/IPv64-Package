package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.internal.query
import de.sr.ipv64.node64.model.*

/** Tag *DNS64*: local DNS resolver with filtering, records, groups, forwards, rewrites and zones. */
public class Dns64Api internal constructor(private val transport: HttpTransport) {

    /** Custom DNS records (including import/export). */
    public val records: Dns64RecordsApi = Dns64RecordsApi(transport)

    /** Device groups with members, rules, domains, hold and schedules. */
    public val groups: Dns64GroupsApi = Dns64GroupsApi(transport)

    /** Conditional forwards. */
    public val forwards: Dns64ForwardsApi = Dns64ForwardsApi(transport)

    /** Rewrites of names. */
    public val rewrites: Dns64RewritesApi = Dns64RewritesApi(transport)

    /** Local zones. */
    public val zones: Dns64ZonesApi = Dns64ZonesApi(transport)

    /** State of the DNS service. `GET /dns64/status` */
    public suspend fun status(): DNS64Status =
        transport.json(Method.GET, ApiPath("dns64", "status"))

    /** Key figures in the given range. `GET /dns64/summary` */
    public suspend fun summary(range: DNS64Range? = null): DNS64Summary =
        transport.json(Method.GET, ApiPath("dns64", "summary"), query("range" to range))

    /** Requests over time. `GET /dns64/timeline` */
    public suspend fun timeline(range: DNS64Range? = null): DNS64Timeline =
        transport.json(Method.GET, ApiPath("dns64", "timeline"), query("range" to range))

    /** Top lists (blocked domains, allowed domains or clients). `GET /dns64/top` */
    public suspend fun top(kind: DNS64TopKind, range: DNS64Range? = null, limit: Int? = null): List<DNS64TopItem> =
        transport.json(Method.GET, ApiPath("dns64", "top"), query("kind" to kind, "range" to range, "limit" to limit))

    /**
     * Query log, newest first.
     *
     * @param q part of a domain or client IP.
     * @param group only this group (empty string = without group).
     *
     * `GET /dns64/log`
     */
    public suspend fun log(
        status: DNS64LogStatus? = null,
        q: String? = null,
        limit: Int? = null,
        offset: Int? = null,
        group: String? = null,
    ): DNS64LogPage = transport.json(
        Method.GET, ApiPath("dns64", "log"),
        query("status" to status, "q" to q, "limit" to limit, "offset" to offset, "group" to group),
    )

    /** Blocklists with rule count, update time and error. `GET /dns64/lists` */
    public suspend fun lists(): List<DNS64ListStatus> =
        transport.json(Method.GET, ApiPath("dns64", "lists"))

    /** Adds a custom blocklist (switched on) from [url] with an optional display [name]. `POST /dns64/lists` */
    public suspend fun addList(url: String, name: String? = null): DNS64Settings =
        transport.json(Method.POST, ApiPath("dns64", "lists"), body = transport.body(DNS64AddListRequest(url, name)))

    /** Removes a custom blocklist identified by its [url] (query parameter). `DELETE /dns64/lists` */
    public suspend fun removeList(url: String): DNS64Settings =
        transport.json(Method.DELETE, ApiPath("dns64", "lists"), query("url" to url))

    /** Upstream servers with statistics and the router for local names. `GET /dns64/upstreams` */
    public suspend fun upstreams(): DNS64Upstreams =
        transport.json(Method.GET, ApiPath("dns64", "upstreams"))

    /** Adds a custom upstream server (`ips` = bootstrap addresses). `POST /dns64/upstreams` */
    public suspend fun addUpstream(request: DNS64AddUpstreamRequest): DNS64Settings =
        transport.json(Method.POST, ApiPath("dns64", "upstreams"), body = transport.body(request))

    /** Removes a custom upstream server identified by its [url] (query parameter). `DELETE /dns64/upstreams` */
    public suspend fun removeUpstream(url: String): DNS64Settings =
        transport.json(Method.DELETE, ApiPath("dns64", "upstreams"), query("url" to url))

    /** Cache counters since start. `GET /dns64/cache` */
    public suspend fun cache(): DNS64Cache =
        transport.json(Method.GET, ApiPath("dns64", "cache"))

    /** Empties the cache. `POST /dns64/cache/flush` */
    public suspend fun flushCache(): DNS64FlushResult =
        transport.json(Method.POST, ApiPath("dns64", "cache", "flush"))

    /** Allowed domains (including subdomains). `GET /dns64/allowlist` */
    public suspend fun allowlist(): List<DNS64AllowedDomain> =
        transport.json(Method.GET, ApiPath("dns64", "allowlist"))

    /** Allows a domain. `POST /dns64/allowlist` */
    public suspend fun allow(domain: String): DNS64AllowedDomain =
        transport.json(Method.POST, ApiPath("dns64", "allowlist"), body = transport.body(DNS64AllowDomainRequest(domain)))

    /** Removes an allowed domain (query parameter). `DELETE /dns64/allowlist` */
    public suspend fun removeAllowed(domain: String) {
        transport.unit(Method.DELETE, ApiPath("dns64", "allowlist"), query("domain" to domain))
    }

    /**
     * Imports many allowed domains at once (comments `#` and timestamps after the domain are ignored,
     * existing entries stay).
     *
     * `POST /dns64/allowlist/import`
     */
    public suspend fun importAllowlist(domains: List<String>): DNS64AllowlistImportResult =
        transport.json(Method.POST, ApiPath("dns64", "allowlist", "import"), body = transport.body(DNS64AllowlistImportRequest(domains)))

    /** Current settings. `GET /dns64/settings` */
    public suspend fun settings(): DNS64Settings =
        transport.json(Method.GET, ApiPath("dns64", "settings"))

    /**
     * Changes settings; only sent fields change (`upstreams`/`lists` are replaced completely).
     * The DNS service restarts afterwards.
     *
     * `PUT /dns64/settings`
     */
    public suspend fun updateSettings(request: DNS64SettingsRequest): DNS64Settings =
        transport.json(Method.PUT, ApiPath("dns64", "settings"), body = transport.body(request))

    /**
     * Pauses blocking. The DNS service keeps running, only the blocklists do not apply.
     *
     * @param minutes 1-1440; 0 = until resumed.
     *
     * `POST /dns64/pause`
     */
    public suspend fun pause(minutes: Int): DNS64Blocking =
        transport.json(Method.POST, ApiPath("dns64", "pause"), body = transport.body(DNS64PauseRequest(minutes)))

    /** Resumes blocking. `POST /dns64/resume` */
    public suspend fun resume(): DNS64Blocking =
        transport.json(Method.POST, ApiPath("dns64", "resume"))

    /** Devices of the last 24 hours (name from the router, MAC from the neighbor table, group). `GET /dns64/devices` */
    public suspend fun devices(): List<DNS64Device> =
        transport.json(Method.GET, ApiPath("dns64", "devices"))

    /** Device names of the zones with their current addresses. `GET /dns64/device-names` */
    public suspend fun deviceNames(): List<DNS64DeviceName> =
        transport.json(Method.GET, ApiPath("dns64", "device-names"))

    /** Home network CA certificate (DER encoded, `application/x-x509-ca-cert`). `GET /dns64/ca.crt` */
    public suspend fun caCertificate(): ByteArray =
        transport.bytes(Method.GET, ApiPath("dns64", "ca.crt"), headers = mapOf("Accept" to "application/x-x509-ca-cert"))

    /**
     * Configuration profile for iOS/macOS (`application/x-apple-aspen-config`).
     *
     * @param host name or IP for the devices.
     *
     * `GET /dns64/mobileconfig`
     */
    public suspend fun mobileConfig(proto: DNS64MobileConfigProto? = null, host: String? = null): ByteArray =
        transport.bytes(
            Method.GET, ApiPath("dns64", "mobileconfig"), query("proto" to proto, "host" to host),
            headers = mapOf("Accept" to "application/x-apple-aspen-config"),
        )
}
