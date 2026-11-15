package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.model.Environment
import de.sr.ipv64.node64.model.EnvironmentAccess
import de.sr.ipv64.node64.model.EnvironmentAccessCreated
import de.sr.ipv64.node64.model.EnvironmentAccessInput
import de.sr.ipv64.node64.model.EnvironmentAddedNode
import de.sr.ipv64.node64.model.EnvironmentNodeInput

/** Tag *Cluster*: this node, cluster members and added nodes (`/environment/…`); changes are administrator only. */
public class EnvironmentApi internal constructor(private val transport: HttpTransport) {

    /** This node, cluster members and added nodes with status; added nodes are queried in parallel (3 s each). `GET /environment` */
    public suspend fun get(): Environment =
        transport.json(Method.GET, ApiPath("environment"))

    /** Accesses of this node for other nodes, without keys (administrator only). `GET /environment/access` */
    public suspend fun accesses(): List<EnvironmentAccess> =
        transport.json(Method.GET, ApiPath("environment", "access"))

    /** Creates an access for another node (administrator only); the key is only in this response. `POST /environment/access` */
    public suspend fun createAccess(input: EnvironmentAccessInput): EnvironmentAccessCreated =
        transport.json(Method.POST, ApiPath("environment", "access"), body = transport.body(input))

    /** Revokes an access (administrator only). `DELETE /environment/access/{id}` */
    public suspend fun deleteAccess(id: Long) {
        transport.unit(Method.DELETE, ApiPath("environment", "access", id.toString()))
    }

    /** Adds a node (administrator only); with a key https only, the key belongs to the creating user. `POST /environment/nodes` */
    public suspend fun addNode(input: EnvironmentNodeInput): EnvironmentAddedNode =
        transport.json(Method.POST, ApiPath("environment", "nodes"), body = transport.body(input))

    /** Changes an added node (administrator only). `PUT /environment/nodes/{id}` */
    public suspend fun updateNode(id: String, input: EnvironmentNodeInput): EnvironmentAddedNode =
        transport.json(Method.PUT, ApiPath("environment", "nodes", id), body = transport.body(input))

    /** Removes an added node (administrator only). `DELETE /environment/nodes/{id}` */
    public suspend fun deleteNode(id: String) {
        transport.unit(Method.DELETE, ApiPath("environment", "nodes", id))
    }
}
