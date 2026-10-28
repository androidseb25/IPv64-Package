package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.model.ClusterGroup
import de.sr.ipv64.node64.model.ClusterGroupCode
import de.sr.ipv64.node64.model.ClusterSyncModules
import de.sr.ipv64.node64.model.JoinGroupRequest
import de.sr.ipv64.node64.model.SetGroupSyncRequest
import de.sr.ipv64.node64.model.UpdateGroupMemberRequest

/** Tag *Cluster*: group of several nodes. */
public class ClusterApi internal constructor(private val transport: HttpTransport) {

    /** Members with state, forwarding node, open code and sync switches. `GET /group` */
    public suspend fun group(): ClusterGroup =
        transport.json(Method.GET, ApiPath("group"))

    /** Creates a pairing code for another node (valid 10 minutes, once). `POST /group/code` */
    public suspend fun createCode(): ClusterGroupCode =
        transport.json(Method.POST, ApiPath("group", "code"))

    /** Discards the open code. `DELETE /group/code` */
    public suspend fun discardCode() {
        transport.unit(Method.DELETE, ApiPath("group", "code"))
    }

    /** Joins a group. `POST /group/join` */
    public suspend fun join(request: JoinGroupRequest): ClusterGroup =
        transport.json(Method.POST, ApiPath("group", "join"), body = transport.body(request))

    /** Leaves the group (the others are informed). `POST /group/leave` */
    public suspend fun leave() {
        transport.unit(Method.POST, ApiPath("group", "leave"))
    }

    /** Changes the address of a member. `PUT /group/members/{id}` */
    public suspend fun updateMember(id: String, url: String) {
        transport.unit(
            Method.PUT,
            ApiPath("group", "members", id),
            body = transport.body(UpdateGroupMemberRequest(url)),
        )
    }

    /** Removes a member from the group. `DELETE /group/members/{id}` */
    public suspend fun removeMember(id: String) {
        transport.unit(Method.DELETE, ApiPath("group", "members", id))
    }

    /** Resumes a suspended member (checks again; if it does not fit, it stays suspended). `POST /group/members/{id}/resume` */
    public suspend fun resumeMember(id: String) {
        transport.unit(Method.POST, ApiPath("group", "members", id, "resume"))
    }

    /**
     * Switches the sync of a module or area on or off (only effective when on for both nodes).
     * `PUT /group/sync`
     */
    public suspend fun setSync(request: SetGroupSyncRequest): ClusterSyncModules =
        transport.json(Method.PUT, ApiPath("group", "sync"), body = transport.body(request))
}
