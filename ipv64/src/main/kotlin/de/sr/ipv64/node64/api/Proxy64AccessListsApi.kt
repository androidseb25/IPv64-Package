package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.model.CDNAccessList
import de.sr.ipv64.node64.model.CDNAccessListInput
import de.sr.ipv64.node64.model.CDNAccessListUsage

/** PROXY64 access lists (`/cdn64/access-lists`). */
public class Proxy64AccessListsApi internal constructor(private val transport: HttpTransport) {

    /** Access lists (without passwords) with the hosts using them. `GET /cdn64/access-lists` */
    public suspend fun list(): List<CDNAccessListUsage> =
        transport.json(Method.GET, ApiPath("cdn64", "access-lists"))

    /** Creates an access list. `POST /cdn64/access-lists` */
    public suspend fun create(request: CDNAccessListInput): CDNAccessList =
        transport.json(Method.POST, ApiPath("cdn64", "access-lists"), body = transport.body(request))

    /** An access list. `GET /cdn64/access-lists/{id}` */
    public suspend fun get(id: Long): CDNAccessList =
        transport.json(Method.GET, ApiPath("cdn64", "access-lists", id.toString()))

    /** Changes an access list. `PUT /cdn64/access-lists/{id}` */
    public suspend fun update(id: Long, request: CDNAccessListInput): CDNAccessList =
        transport.json(Method.PUT, ApiPath("cdn64", "access-lists", id.toString()), body = transport.body(request))

    /** Deletes an access list. `DELETE /cdn64/access-lists/{id}` */
    public suspend fun delete(id: Long) {
        transport.unit(Method.DELETE, ApiPath("cdn64", "access-lists", id.toString()))
    }
}
