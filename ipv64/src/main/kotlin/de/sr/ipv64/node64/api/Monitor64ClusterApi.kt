package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.internal.query
import de.sr.ipv64.node64.model.Monitor64Cluster
import de.sr.ipv64.node64.model.Monitor64ClusterHostInfo
import de.sr.ipv64.node64.model.Monitor64ClusterHostSetting
import de.sr.ipv64.node64.model.Monitor64ClusterHostSettingInput
import de.sr.ipv64.node64.model.Monitor64ClusterKeyConfirmInput
import de.sr.ipv64.node64.model.Monitor64ClusterKeys
import de.sr.ipv64.node64.model.Monitor64ClusterOrphans
import de.sr.ipv64.node64.model.Monitor64ClusterReach
import de.sr.ipv64.node64.model.Monitor64ClusterViews
import de.sr.ipv64.node64.model.Monitor64OrphanInput
import de.sr.ipv64.node64.model.Monitor64OrphanResult

/** MONITOR64 cluster: responsibility per host, node keys, orphaned hosts (`/monitor64/cluster`). */
public class Monitor64ClusterApi internal constructor(private val transport: HttpTransport) {

    /** Nodes, responsibility per host and keys of the node. `GET /monitor64/cluster` */
    public suspend fun get(): Monitor64Cluster =
        transport.json(Method.GET, ApiPath("monitor64", "cluster"))

    /** Monitoring node per host; without [host] also the orphaned hosts. `GET /monitor64/cluster/views` */
    public suspend fun views(host: String? = null): Monitor64ClusterViews =
        transport.json(Method.GET, ApiPath("monitor64", "cluster", "views"), query("host" to host))

    /** Responsibility of a host. `GET /monitor64/cluster/hosts/{uid}` */
    public suspend fun host(uid: String): Monitor64ClusterHostInfo =
        transport.json(Method.GET, ApiPath("monitor64", "cluster", "hosts", uid))

    /** Sets preferred node and backup nodes of a host. `PUT /monitor64/cluster/hosts/{uid}` */
    public suspend fun setHost(uid: String, input: Monitor64ClusterHostSettingInput): Monitor64ClusterHostSetting =
        transport.json(Method.PUT, ApiPath("monitor64", "cluster", "hosts", uid), body = transport.body(input))

    /** Tests the accesses of a host from all reachable nodes. `POST /monitor64/cluster/hosts/{uid}/reach` */
    public suspend fun reach(uid: String): Monitor64ClusterReach =
        transport.json(Method.POST, ApiPath("monitor64", "cluster", "hosts", uid, "reach"))

    /** Keys of the node. `GET /monitor64/cluster/keys` */
    public suspend fun keys(): Monitor64ClusterKeys =
        transport.json(Method.GET, ApiPath("monitor64", "cluster", "keys"))

    /** Creates a new key for all nodes. `POST /monitor64/cluster/keys/rotate` */
    public suspend fun rotateKeys(): Monitor64ClusterKeys =
        transport.json(Method.POST, ApiPath("monitor64", "cluster", "keys", "rotate"))

    /** Retries the changeover of agents not yet switched. `POST /monitor64/cluster/keys/retry` */
    public suspend fun retryKeys(): Monitor64ClusterKeys =
        transport.json(Method.POST, ApiPath("monitor64", "cluster", "keys", "retry"))

    /** Confirms the manual changeover of an access (or discards the entry). `POST /monitor64/cluster/keys/confirm` */
    public suspend fun confirmKey(input: Monitor64ClusterKeyConfirmInput): Monitor64ClusterKeys =
        transport.json(Method.POST, ApiPath("monitor64", "cluster", "keys", "confirm"), body = transport.body(input))

    /** Discards old keys immediately. `POST /monitor64/cluster/keys/retire` */
    public suspend fun retireKeys(): Monitor64ClusterKeys =
        transport.json(Method.POST, ApiPath("monitor64", "cluster", "keys", "retire"))

    /** Hosts no node monitors any more. `GET /monitor64/cluster/orphans` */
    public suspend fun orphans(): Monitor64ClusterOrphans =
        transport.json(Method.GET, ApiPath("monitor64", "cluster", "orphans"))

    /** Assigns orphaned hosts to this node. `POST /monitor64/cluster/orphans/assign` */
    public suspend fun assignOrphans(input: Monitor64OrphanInput): Monitor64OrphanResult =
        transport.json(Method.POST, ApiPath("monitor64", "cluster", "orphans", "assign"), body = transport.body(input))

    /** Removes orphaned hosts. `POST /monitor64/cluster/orphans/remove` */
    public suspend fun removeOrphans(input: Monitor64OrphanInput): Monitor64OrphanResult =
        transport.json(Method.POST, ApiPath("monitor64", "cluster", "orphans", "remove"), body = transport.body(input))
}
