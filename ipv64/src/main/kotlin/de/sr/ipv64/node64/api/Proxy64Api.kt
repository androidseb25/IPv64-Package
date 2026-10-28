package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.internal.query
import de.sr.ipv64.node64.model.CDNBlocklists
import de.sr.ipv64.node64.model.CDNHoneypotSettingsRequest
import de.sr.ipv64.node64.model.CDNHoneypotView
import de.sr.ipv64.node64.model.CDNLimitsRequest
import de.sr.ipv64.node64.model.CDNLimitsView
import de.sr.ipv64.node64.model.CDNOnlineStatus
import de.sr.ipv64.node64.model.ProxyCheckState
import de.sr.ipv64.node64.model.ProxyDefaultPage
import de.sr.ipv64.node64.model.ProxyLogEntry
import de.sr.ipv64.node64.model.ProxySettings
import de.sr.ipv64.node64.model.SetOnlineTunnelRequest
import de.sr.ipv64.node64.model.UpdateBlocklistsRequest
import de.sr.ipv64.node64.model.UpdateProxyDefaultPageRequest
import de.sr.ipv64.node64.model.UpdateProxySettingsRequest
import kotlinx.serialization.json.JsonObject

/** Tag *PROXY64*: reverse proxy (`/cdn64/…`) with hosts, streams, access lists, protection and online publishing. */
public class Proxy64Api internal constructor(private val transport: HttpTransport) {

    /** Proxy hosts (proxy, redirect, 404, TLS passthrough, status page) incl. enable/disable, healthchecks and CDN64 online. */
    public val hosts: Proxy64HostsApi = Proxy64HostsApi(transport)

    /** TCP/UDP streams. */
    public val streams: Proxy64StreamsApi = Proxy64StreamsApi(transport)

    /** Access lists (users and IP rules). */
    public val accessLists: Proxy64AccessListsApi = Proxy64AccessListsApi(transport)

    /** State: ports of the entry, number of hosts, cache. Response is not modelled by the API. `GET /cdn64/status` */
    public suspend fun status(): JsonObject =
        transport.json(Method.GET, ApiPath("cdn64", "status"))

    /** Checks the reachability of all targets now. `POST /cdn64/check` */
    public suspend fun check(): ProxyCheckState =
        transport.json(Method.POST, ApiPath("cdn64", "check"))

    /** Default page for unknown names. `GET /cdn64/default` */
    public suspend fun defaultPage(): ProxyDefaultPage =
        transport.json(Method.GET, ApiPath("cdn64", "default"))

    /** Changes the default page (missing fields stay). `PUT /cdn64/default` */
    public suspend fun updateDefaultPage(request: UpdateProxyDefaultPageRequest): ProxyDefaultPage =
        transport.json(Method.PUT, ApiPath("cdn64", "default"), body = transport.body(request))

    /**
     * Access log, newest first. Entries with `reason` were rejected.
     *
     * @param host host ID.
     * @param status status class `2`-`5`, or `r` for rejected only.
     * @param q search in path, client or user agent.
     * @param before pagination: entries with a smaller ID.
     * @param limit 1-500, default 100.
     * @param rejected only rejected entries (like `status=r`).
     * `GET /cdn64/log`
     */
    public suspend fun log(
        host: Long? = null,
        status: String? = null,
        q: String? = null,
        before: Long? = null,
        limit: Int? = null,
        rejected: Boolean? = null,
    ): List<ProxyLogEntry> =
        transport.json(
            Method.GET, ApiPath("cdn64", "log"),
            query(
                "host" to host, "status" to status, "q" to q, "before" to before, "limit" to limit,
                "rejected" to if (rejected == true) "1" else null,
            ),
        )

    /**
     * Requests per host and hourly timeline. Response is a free-form object.
     *
     * @param hours 1-2160, default 24.
     * @param host restrict to one host ID.
     * `GET /cdn64/stats`
     */
    public suspend fun stats(hours: Int? = null, host: Long? = null): JsonObject =
        transport.json(Method.GET, ApiPath("cdn64", "stats"), query("hours" to hours, "host" to host))

    /** Retention of the access log. `GET /cdn64/settings` */
    public suspend fun settings(): ProxySettings =
        transport.json(Method.GET, ApiPath("cdn64", "settings"))

    /** Changes the retention of the access log. `PUT /cdn64/settings` */
    public suspend fun updateSettings(request: UpdateProxySettingsRequest): ProxySettings =
        transport.json(Method.PUT, ApiPath("cdn64", "settings"), body = transport.body(request))

    // ---- Blocklists ----

    /** State of the ipv64 blocklists. `GET /cdn64/blocklists` */
    public suspend fun blocklists(): CDNBlocklists =
        transport.json(Method.GET, ApiPath("cdn64", "blocklists"))

    /** Enables/disables blocklists and sets exceptions. `PUT /cdn64/blocklists` */
    public suspend fun updateBlocklists(request: UpdateBlocklistsRequest): CDNBlocklists =
        transport.json(Method.PUT, ApiPath("cdn64", "blocklists"), body = transport.body(request))

    /** Reloads the blocklists now. `POST /cdn64/blocklists/refresh` */
    public suspend fun refreshBlocklists(): CDNBlocklists =
        transport.json(Method.POST, ApiPath("cdn64", "blocklists", "refresh"))

    // ---- Honeypot64 ----

    /** Honeypot64: settings, blocked IPs, counters. `GET /cdn64/honeypot` */
    public suspend fun honeypot(): CDNHoneypotView =
        transport.json(Method.GET, ApiPath("cdn64", "honeypot"))

    /** Configures Honeypot64 (missing fields stay; disabling the block clears the list). `PUT /cdn64/honeypot` */
    public suspend fun updateHoneypot(request: CDNHoneypotSettingsRequest): CDNHoneypotView =
        transport.json(Method.PUT, ApiPath("cdn64", "honeypot"), body = transport.body(request))

    /** Lifts a honeypot block for [ip] (IPv4 or IPv6 as in `blocks`). `DELETE /cdn64/honeypot/blocks/{ip}` */
    public suspend fun removeHoneypotBlock(ip: String) {
        transport.unit(Method.DELETE, ApiPath("cdn64", "honeypot", "blocks", ip))
    }

    // ---- Overload protection ----

    /** Overload protection: settings, bans, counters. `GET /cdn64/limits` */
    public suspend fun limits(): CDNLimitsView =
        transport.json(Method.GET, ApiPath("cdn64", "limits"))

    /** Configures the overload protection (missing fields stay). `PUT /cdn64/limits` */
    public suspend fun updateLimits(request: CDNLimitsRequest): CDNLimitsView =
        transport.json(Method.PUT, ApiPath("cdn64", "limits"), body = transport.body(request))

    /** Releases a banned address (IPv4 or IPv6 /64 as in `bans`). `DELETE /cdn64/limits/bans/{ip}` */
    public suspend fun removeLimitBan(ip: String) {
        transport.unit(Method.DELETE, ApiPath("cdn64", "limits", "bans", ip))
    }

    // ---- CDN64 online ----

    /** CDN64: tunnel, servers, enabled names, quota. `GET /cdn64/online` */
    public suspend fun online(): CDNOnlineStatus =
        transport.json(Method.GET, ApiPath("cdn64", "online"))

    /** Sets the tunnel manually (tests; normally done by one click). `PUT /cdn64/online` */
    public suspend fun setOnlineTunnel(request: SetOnlineTunnelRequest): CDNOnlineStatus =
        transport.json(Method.PUT, ApiPath("cdn64", "online"), body = transport.body(request))

    /** Forgets the tunnel locally (it stays at ipv64). `DELETE /cdn64/online` */
    public suspend fun removeOnline() {
        transport.unit(Method.DELETE, ApiPath("cdn64", "online"))
    }

    /**
     * Registers the tunnel at ipv64 or checks it and waits up to 25 s for the connection.
     * Creates the tunnel if none exists and takes over the server list from ipv64.
     * `POST /cdn64/online/tunnel`
     */
    public suspend fun registerOnlineTunnel(): CDNOnlineStatus =
        transport.json(Method.POST, ApiPath("cdn64", "online", "tunnel"))
}
