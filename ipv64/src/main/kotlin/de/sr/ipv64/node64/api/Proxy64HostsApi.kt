package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.internal.query
import de.sr.ipv64.node64.model.CDNOnlineTest
import de.sr.ipv64.node64.model.EnableHostOnlineRequest
import de.sr.ipv64.node64.model.HostOnlineResult
import de.sr.ipv64.node64.model.ProxyHealthcheckRef
import de.sr.ipv64.node64.model.ProxyHost
import de.sr.ipv64.node64.model.ProxyHostInput
import de.sr.ipv64.node64.model.ProxyHostKind

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
}
