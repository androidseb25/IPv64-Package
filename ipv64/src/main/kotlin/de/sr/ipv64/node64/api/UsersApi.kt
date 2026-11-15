package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.model.CreateUserRequest
import de.sr.ipv64.node64.model.NodeUser
import de.sr.ipv64.node64.model.SetUserPasswordRequest
import de.sr.ipv64.node64.model.SetUserPasswordResult
import de.sr.ipv64.node64.model.UpdateMeRequest
import de.sr.ipv64.node64.model.UpdateUserRequest

/** Tag *Benutzerverwaltung*: users of this node. */
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

    /** Creates a local user; with `must_change` and no password the response carries a one-time password. `POST /users` */
    public suspend fun create(request: CreateUserRequest): NodeUser =
        transport.json(Method.POST, ApiPath("users"), body = transport.body(request))

    /** One user. `GET /users/{id}` */
    public suspend fun get(id: Long): NodeUser =
        transport.json(Method.GET, ApiPath("users", id.toString()))

    /** Changes role, groups, e-mail and lock; missing fields stay (never the last local admin or oneself: 409). `PUT /users/{id}` */
    public suspend fun update(id: Long, request: UpdateUserRequest): NodeUser =
        transport.json(Method.PUT, ApiPath("users", id.toString()), body = transport.body(request))

    /** Removes app 2FA and passkeys of a user on this node and ends their sessions. `POST /users/{id}/2fa-reset` */
    public suspend fun reset2fa(id: Long) {
        transport.unit(Method.POST, ApiPath("users", id.toString(), "2fa-reset"))
    }

    /**
     * Sets the password of a local user and ends their sessions; returns the generated one-time password
     * (`must_change` without `password`), otherwise `null` (204). `POST /users/{id}/password`
     */
    public suspend fun setPassword(id: Long, request: SetUserPasswordRequest): SetUserPasswordResult? =
        transport.jsonOrNull(Method.POST, ApiPath("users", id.toString(), "password"), body = transport.body(request))
            ?.let { transport.json.decodeFromJsonElement(SetUserPasswordResult.serializer(), it) }

    /** Ends all sessions of a user. `POST /users/{id}/sessions-end` */
    public suspend fun endSessions(id: Long) {
        transport.unit(Method.POST, ApiPath("users", id.toString(), "sessions-end"))
    }

    /** Deletes another user (never the own one: 409). `DELETE /users/{id}` */
    public suspend fun delete(id: Long) {
        transport.unit(Method.DELETE, ApiPath("users", id.toString()))
    }
}
