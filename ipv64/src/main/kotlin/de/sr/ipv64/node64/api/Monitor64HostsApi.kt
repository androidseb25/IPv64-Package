package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.internal.query
import de.sr.ipv64.node64.model.*

/** MONITOR64 hosts, their services and accesses (`/monitor64/hosts…`, `/monitor64/access/…`). */
public class Monitor64HostsApi internal constructor(private val transport: HttpTransport) {

    /** Hosts with their status. `GET /monitor64/hosts` */
    public suspend fun list(): List<Monitor64OverviewHost> =
        transport.json(Method.GET, ApiPath("monitor64", "hosts"))

    /** Creates a host (optionally with accesses and discovered services). `POST /monitor64/hosts` */
    public suspend fun create(request: Monitor64HostCreate): Monitor64Uid =
        transport.json(Method.POST, ApiPath("monitor64", "hosts"), body = transport.body(request))

    /** A host with its services, accesses, new and ignored services. `GET /monitor64/hosts/{uid}` */
    public suspend fun get(uid: String): Monitor64HostDetail =
        transport.json(Method.GET, ApiPath("monitor64", "hosts", uid))

    /** Replaces the fields of a host. `PUT /monitor64/hosts/{uid}` */
    public suspend fun update(uid: String, request: Monitor64HostInput): Monitor64Uid =
        transport.json(Method.PUT, ApiPath("monitor64", "hosts", uid), body = transport.body(request))

    /** Removes a host; with [deleteHistory] its recorded series are deleted too. `DELETE /monitor64/hosts/{uid}` */
    public suspend fun delete(uid: String, deleteHistory: Boolean = false): Monitor64HostsDeleteResult =
        transport.json(
            Method.DELETE,
            ApiPath("monitor64", "hosts", uid),
            query("history" to if (deleteHistory) "delete" else null),
        )

    /** Discovers the services of a host now. `POST /monitor64/hosts/{uid}/discover` */
    public suspend fun discover(uid: String): Monitor64HostsDiscoverResult =
        transport.json(Method.POST, ApiPath("monitor64", "hosts", uid, "discover"))

    /** Creates an access for a host. `POST /monitor64/hosts/{uid}/access` */
    public suspend fun createAccess(uid: String, request: Monitor64AccessInput): Monitor64Access =
        transport.json(Method.POST, ApiPath("monitor64", "hosts", uid, "access"), body = transport.body(request))

    /** Adopts, ignores or re-offers services (see [Monitor64HostsServiceKeys.state]). `POST /monitor64/hosts/{uid}/services/adopt` */
    public suspend fun adoptServices(uid: String, request: Monitor64HostsServiceKeys): Monitor64HostsChanged =
        transport.json(Method.POST, ApiPath("monitor64", "hosts", uid, "services", "adopt"), body = transport.body(request))

    /** Removes services. `POST /monitor64/hosts/{uid}/services/remove` */
    public suspend fun removeServices(uid: String, request: Monitor64HostsServiceKeys): Monitor64HostsChanged =
        transport.json(Method.POST, ApiPath("monitor64", "hosts", uid, "services", "remove"), body = transport.body(request))

    /** Changes a service (name, target state, thresholds). `PUT /monitor64/hosts/{uid}/service` */
    public suspend fun updateService(uid: String, request: Monitor64ServiceUpdate): Monitor64Ok =
        transport.json(Method.PUT, ApiPath("monitor64", "hosts", uid, "service"), body = transport.body(request))

    /** Changes an access. `PUT /monitor64/access/{uid}` */
    public suspend fun updateAccess(uid: String, request: Monitor64AccessInput): Monitor64Access =
        transport.json(Method.PUT, ApiPath("monitor64", "access", uid), body = transport.body(request))

    /** Removes an access. `DELETE /monitor64/access/{uid}` */
    public suspend fun deleteAccess(uid: String): Monitor64Ok =
        transport.json(Method.DELETE, ApiPath("monitor64", "access", uid))

    /** Tests an access (stored values apply where the request leaves them out). `POST /monitor64/access/{uid}/test` */
    public suspend fun testAccess(uid: String, request: Monitor64AccessInput = Monitor64AccessInput()): Monitor64TestResult =
        transport.json(Method.POST, ApiPath("monitor64", "access", uid, "test"), body = transport.body(request))

    /** Discards the remembered SSH host key of an access. `POST /monitor64/access/{uid}/hostkey-reset` */
    public suspend fun resetHostKey(uid: String): Monitor64Ok =
        transport.json(Method.POST, ApiPath("monitor64", "access", uid, "hostkey-reset"))

    /** Traps of a host. `GET /monitor64/hosts/{uid}/traps` */
    public suspend fun traps(uid: String): Monitor64HostTraps =
        transport.json(Method.GET, ApiPath("monitor64", "hosts", uid, "traps"))

    /** Resets the open traps of a host. `POST /monitor64/hosts/{uid}/traps/reset` */
    public suspend fun resetTraps(uid: String): Monitor64Ok =
        transport.json(Method.POST, ApiPath("monitor64", "hosts", uid, "traps", "reset"))

    /** Switches automatic agent updates of a host. `PUT /monitor64/hosts/{uid}/agent` */
    public suspend fun setAgentAutoUpdate(uid: String, autoUpdate: Boolean): Monitor64HostsAgentAutoUpdate =
        transport.json(
            Method.PUT,
            ApiPath("monitor64", "hosts", uid, "agent"),
            body = transport.body(Monitor64HostsAgentAutoUpdateRequest(autoUpdate)),
        )

    /** Updates the agent of a host now (accepted, runs asynchronously). `POST /monitor64/hosts/{uid}/agent/update` */
    public suspend fun updateAgent(uid: String): Monitor64HostsAgentUpdate =
        transport.json(Method.POST, ApiPath("monitor64", "hosts", uid, "agent", "update"))
}
