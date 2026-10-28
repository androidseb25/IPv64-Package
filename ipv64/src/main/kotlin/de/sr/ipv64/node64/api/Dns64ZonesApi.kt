package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.model.*

/** Local zones of the DNS64 resolver. */
public class Dns64ZonesApi internal constructor(private val transport: HttpTransport) {

    /** Lists local zones. `GET /dns64/zones` */
    public suspend fun list(): List<DNS64Zone> =
        transport.json(Method.GET, ApiPath("dns64", "zones"))

    /** Creates an entry. `POST /dns64/zones` */
    public suspend fun create(request: DNS64ZoneRequest): DNS64Zone =
        transport.json(Method.POST, ApiPath("dns64", "zones"), body = transport.body(request))

    /** Changes an entry (enabled state stays). `PUT /dns64/zones/{id}` */
    public suspend fun update(id: Long, request: DNS64ZoneRequest): DNS64Zone =
        transport.json(Method.PUT, ApiPath("dns64", "zones", id.toString()), body = transport.body(request))

    /** Deletes an entry. `DELETE /dns64/zones/{id}` */
    public suspend fun delete(id: Long) {
        transport.unit(Method.DELETE, ApiPath("dns64", "zones", id.toString()))
    }

    /** Enables an entry. `POST /dns64/zones/{id}/enable` */
    public suspend fun enable(id: Long) {
        transport.unit(Method.POST, ApiPath("dns64", "zones", id.toString(), "enable"))
    }

    /** Disables an entry. `POST /dns64/zones/{id}/disable` */
    public suspend fun disable(id: Long) {
        transport.unit(Method.POST, ApiPath("dns64", "zones", id.toString(), "disable"))
    }

    /** Zone file of the zone in BIND format. `GET /dns64/zones/{id}/zonefile` */
    public suspend fun zonefile(id: Long): String =
        transport.text(Method.GET, ApiPath("dns64", "zones", id.toString(), "zonefile"), headers = mapOf("Accept" to "text/plain"))
}
