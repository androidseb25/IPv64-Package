package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.model.*

/** Rewrites of names of the DNS64 resolver. */
public class Dns64RewritesApi internal constructor(private val transport: HttpTransport) {

    /** Lists rewrites. `GET /dns64/rewrites` */
    public suspend fun list(): List<DNS64Rewrite> =
        transport.json(Method.GET, ApiPath("dns64", "rewrites"))

    /** Creates an entry. `POST /dns64/rewrites` */
    public suspend fun create(request: DNS64RewriteRequest): DNS64Rewrite =
        transport.json(Method.POST, ApiPath("dns64", "rewrites"), body = transport.body(request))

    /** Changes an entry (enabled state stays). `PUT /dns64/rewrites/{id}` */
    public suspend fun update(id: Long, request: DNS64RewriteRequest): DNS64Rewrite =
        transport.json(Method.PUT, ApiPath("dns64", "rewrites", id.toString()), body = transport.body(request))

    /** Deletes an entry. `DELETE /dns64/rewrites/{id}` */
    public suspend fun delete(id: Long) {
        transport.unit(Method.DELETE, ApiPath("dns64", "rewrites", id.toString()))
    }

    /** Enables an entry. `POST /dns64/rewrites/{id}/enable` */
    public suspend fun enable(id: Long) {
        transport.unit(Method.POST, ApiPath("dns64", "rewrites", id.toString(), "enable"))
    }

    /** Disables an entry. `POST /dns64/rewrites/{id}/disable` */
    public suspend fun disable(id: Long) {
        transport.unit(Method.POST, ApiPath("dns64", "rewrites", id.toString(), "disable"))
    }
}
