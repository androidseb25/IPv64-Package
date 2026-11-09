package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.internal.query
import de.sr.ipv64.node64.model.CDNOnlineTest
import de.sr.ipv64.node64.model.CDNServers
import de.sr.ipv64.node64.model.EnableHostOnlineRequest
import de.sr.ipv64.node64.model.HostLocalDnsRequest
import de.sr.ipv64.node64.model.HostLocalDnsResult
import de.sr.ipv64.node64.model.HostOnlineResult
import de.sr.ipv64.node64.model.HostProtection
import de.sr.ipv64.node64.model.HostProtectionRequest
import de.sr.ipv64.node64.model.PauseOnlineRequest
import de.sr.ipv64.node64.model.ProxyHealthcheckRef
import de.sr.ipv64.node64.model.ProxyHost
import de.sr.ipv64.node64.model.ProxyHostInput
import de.sr.ipv64.node64.model.ProxyHostKind
import de.sr.ipv64.node64.model.SetCDNServersRequest

/** PROXY64 hosts (`/cdn64/hosts`). */
public class Proxy64HostsApi internal constructor(private val transport: HttpTransport) {

    /** Hosts with reachability, counters and certificate, optionally filtered by [kind]. `GET /cdn64/hosts` */
    public suspend fun list(kind: ProxyHostKind? = null): List<ProxyHost> =
        transport.json(Method.GET, ApiPath("cdn64", "hosts"), query("kind" to kind))

    /** Creates a proxy host. `POST /cdn64/hosts` */
    public suspend fun create(request: ProxyHostInput): ProxyHost =
        transport.json(Method.POST, ApiPath("cdn64", "hosts"), body = transport.body(request))

    /** A proxy host. `GET /cdn64/hosts/{id}` */
    public suspend fun get(id: Long): ProxyHost =
        transport.json(Method.GET, ApiPath("cdn64", "hosts", id.toString()))

    /** Changes a proxy host (missing fields stay). `PUT /cdn64/hosts/{id}` */
    public suspend fun update(id: Long, request: ProxyHostInput): ProxyHost =
        transport.json(Method.PUT, ApiPath("cdn64", "hosts", id.toString()), body = transport.body(request))

    /** Deletes a proxy host. `DELETE /cdn64/hosts/{id}` */
    public suspend fun delete(id: Long) {
        transport.unit(Method.DELETE, ApiPath("cdn64", "hosts", id.toString()))
    }

    /** Switches a proxy host on. `POST /cdn64/hosts/{id}/enable` */
    public suspend fun enable(id: Long): ProxyHost =
        transport.json(Method.POST, ApiPath("cdn64", "hosts", id.toString(), "enable"))

    /** Switches a proxy host off. `POST /cdn64/hosts/{id}/disable` */
    public suspend fun disable(id: Long): ProxyHost =
        transport.json(Method.POST, ApiPath("cdn64", "hosts", id.toString(), "disable"))

    /** Creates a healthcheck for the target of this host (module Healthchecks). `POST /cdn64/hosts/{id}/healthcheck` */
    public suspend fun createHealthcheck(id: Long): ProxyHealthcheckRef =
        transport.json(Method.POST, ApiPath("cdn64", "hosts", id.toString(), "healthcheck"))

    /**
     * Switches the names of the host on at CDN64 (DNS points to srv07/srv08). Placeholder names are skipped,
     * each name has its own result. If all fail, the tunnel is removed again.
     * `POST /cdn64/hosts/{id}/online`
     */
    public suspend fun enableOnline(id: Long, request: EnableHostOnlineRequest? = null): HostOnlineResult =
        transport.json(
            Method.POST, ApiPath("cdn64", "hosts", id.toString(), "online"),
            body = request?.let { transport.body(it) },
        )

    /** Switches all names of the host off (DNS back); with the last name the tunnel goes. `DELETE /cdn64/hosts/{id}/online` */
    public suspend fun disableOnline(id: Long) {
        transport.unit(Method.DELETE, ApiPath("cdn64", "hosts", id.toString(), "online"))
    }

    /** Self test over the internet: every name over every server and address. `POST /cdn64/hosts/{id}/online/test` */
    public suspend fun testOnline(id: Long): List<CDNOnlineTest> =
        transport.json(Method.POST, ApiPath("cdn64", "hosts", id.toString(), "online", "test"))

    /** Pauses/resumes CDN64 for the names of the host (DNS straight to the server, settings stay). `POST /cdn64/hosts/{id}/online/pause` */
    public suspend fun pauseOnline(id: Long, request: PauseOnlineRequest) {
        transport.unit(Method.POST, ApiPath("cdn64", "hosts", id.toString(), "online", "pause"), body = transport.body(request))
    }

    /** CDN64 servers and connections per server of the names of the host. `GET /cdn64/hosts/{id}/servers` */
    public suspend fun servers(id: Long): CDNServers =
        transport.json(Method.GET, ApiPath("cdn64", "hosts", id.toString(), "servers"))

    /** Sets CDN64 servers and connections per server for all names of the host. `PUT /cdn64/hosts/{id}/servers` */
    public suspend fun setServers(id: Long, request: SetCDNServersRequest): CDNServers =
        transport.json(Method.PUT, ApiPath("cdn64", "hosts", id.toString(), "servers"), body = transport.body(request))

    /** Sets CDN64 protection (WAF, JA4) of the servers for all names of the host (missing fields stay). `PUT /cdn64/hosts/{id}/protection` */
    public suspend fun updateProtection(id: Long, request: HostProtectionRequest): HostProtection =
        transport.json(Method.PUT, ApiPath("cdn64", "hosts", id.toString(), "protection"), body = transport.body(request))

    /**
     * Adds the names of the host as own records to DNS64 (home network directly instead of over the internet).
     * IPv4 only A, IPv6 only AAAA; existing records stay, wildcards and main domains are skipped.
     * Without addresses PROXY64 takes the suggested ones.
     * `POST /cdn64/hosts/{id}/localdns`
     */
    public suspend fun addLocalDns(id: Long, request: HostLocalDnsRequest? = null): HostLocalDnsResult =
        transport.json(
            Method.POST, ApiPath("cdn64", "hosts", id.toString(), "localdns"),
            body = request?.let { transport.body(it) },
        )
}
