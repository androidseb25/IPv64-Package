package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.model.*

/** Conditional forwards of the DNS64 resolver. */
public class Dns64ForwardsApi internal constructor(private val transport: HttpTransport) {

    /** Lists forwards. `GET /dns64/forwards` */
    public suspend fun list(): List<DNS64Forward> =
        transport.json(Method.GET, ApiPath("dns64", "forwards"))

    /** Creates an entry. `POST /dns64/forwards` */
    public suspend fun create(request: DNS64ForwardRequest): DNS64Forward =
        transport.json(Method.POST, ApiPath("dns64", "forwards"), body = transport.body(request))

    /** Changes an entry (enabled state stays). `PUT /dns64/forwards/{id}` */
    public suspend fun update(id: Long, request: DNS64ForwardRequest): DNS64Forward =
        transport.json(Method.PUT, ApiPath("dns64", "forwards", id.toString()), body = transport.body(request))

    /** Deletes an entry. `DELETE /dns64/forwards/{id}` */
    public suspend fun delete(id: Long) {
        transport.unit(Method.DELETE, ApiPath("dns64", "forwards", id.toString()))
    }

    /** Enables an entry. `POST /dns64/forwards/{id}/enable` */
    public suspend fun enable(id: Long) {
        transport.unit(Method.POST, ApiPath("dns64", "forwards", id.toString(), "enable"))
    }

    /** Disables an entry. `POST /dns64/forwards/{id}/disable` */
    public suspend fun disable(id: Long) {
        transport.unit(Method.POST, ApiPath("dns64", "forwards", id.toString(), "disable"))
    }

    /** Tests a forward (SOA of the domain, also when disabled). `POST /dns64/forwards/{id}/test` */
    public suspend fun test(id: Long): DNS64ForwardTestResult =
        transport.json(Method.POST, ApiPath("dns64", "forwards", id.toString(), "test"))
}
