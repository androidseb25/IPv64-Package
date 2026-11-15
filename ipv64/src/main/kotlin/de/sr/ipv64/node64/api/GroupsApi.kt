package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.model.NodeGroup
import de.sr.ipv64.node64.model.NodeGroupRequest

/** Tag *Benutzerverwaltung*: user groups of this node (role and AD/LDAP/SSO mapping; not DNS64 groups). */
public class GroupsApi internal constructor(private val transport: HttpTransport) {

    /** Lists the user groups with members. `GET /groups` */
    public suspend fun list(): List<NodeGroup> =
        transport.json(Method.GET, ApiPath("groups"))

    /** Creates a user group. `POST /groups` */
    public suspend fun create(request: NodeGroupRequest): NodeGroup =
        transport.json(Method.POST, ApiPath("groups"), body = transport.body(request))

    /** One user group by uid. `GET /groups/{id}` */
    public suspend fun get(id: String): NodeGroup =
        transport.json(Method.GET, ApiPath("groups", id))

    /** Changes a user group; missing fields stay (never the last local admin or oneself: 409). `PUT /groups/{id}` */
    public suspend fun update(id: String, request: NodeGroupRequest): NodeGroup =
        transport.json(Method.PUT, ApiPath("groups", id), body = transport.body(request))

    /** Deletes a user group. `DELETE /groups/{id}` */
    public suspend fun delete(id: String) {
        transport.unit(Method.DELETE, ApiPath("groups", id))
    }
}
