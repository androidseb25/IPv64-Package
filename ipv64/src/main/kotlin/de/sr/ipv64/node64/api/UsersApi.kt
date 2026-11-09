package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.model.NodeUser
import de.sr.ipv64.node64.model.UpdateMeRequest

/** Tag *Node*: users of this node. */
public class UsersApi internal constructor(private val transport: HttpTransport) {

    /** Lists the users of this node. `GET /users` */
    public suspend fun list(): List<NodeUser> =
        transport.json(Method.GET, ApiPath("users"))

    /** The signed-in user or owner of the API key. `GET /users/me` */
    public suspend fun me(): NodeUser =
        transport.json(Method.GET, ApiPath("users", "me"))

    /** Changes own language and time zone; missing fields stay. `PUT /users/me` */
    public suspend fun updateMe(request: UpdateMeRequest): NodeUser =
        transport.json(Method.PUT, ApiPath("users", "me"), body = transport.body(request))

    /** Deletes another user (never the own one: 409). `DELETE /users/{id}` */
    public suspend fun delete(id: Long) {
        transport.unit(Method.DELETE, ApiPath("users", id.toString()))
    }
}
