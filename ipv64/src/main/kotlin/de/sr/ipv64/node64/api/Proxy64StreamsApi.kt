package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.model.ProxyHealthcheckRef
import de.sr.ipv64.node64.model.Stream
import de.sr.ipv64.node64.model.StreamInput

/** PROXY64 TCP/UDP streams (`/cdn64/streams`). */
public class Proxy64StreamsApi internal constructor(private val transport: HttpTransport) {

    /** Streams (TCP/UDP). `GET /cdn64/streams` */
    public suspend fun list(): List<Stream> =
        transport.json(Method.GET, ApiPath("cdn64", "streams"))

    /** Creates a stream. `POST /cdn64/streams` */
    public suspend fun create(request: StreamInput): Stream =
        transport.json(Method.POST, ApiPath("cdn64", "streams"), body = transport.body(request))

    /** A stream. `GET /cdn64/streams/{id}` */
    public suspend fun get(id: Long): Stream =
        transport.json(Method.GET, ApiPath("cdn64", "streams", id.toString()))

    /** Changes a stream (missing fields stay). `PUT /cdn64/streams/{id}` */
    public suspend fun update(id: Long, request: StreamInput): Stream =
        transport.json(Method.PUT, ApiPath("cdn64", "streams", id.toString()), body = transport.body(request))

    /** Deletes a stream. `DELETE /cdn64/streams/{id}` */
    public suspend fun delete(id: Long) {
        transport.unit(Method.DELETE, ApiPath("cdn64", "streams", id.toString()))
    }

    /** Switches a stream on. `POST /cdn64/streams/{id}/enable` */
    public suspend fun enable(id: Long): Stream =
        transport.json(Method.POST, ApiPath("cdn64", "streams", id.toString(), "enable"))

    /** Switches a stream off. `POST /cdn64/streams/{id}/disable` */
    public suspend fun disable(id: Long): Stream =
        transport.json(Method.POST, ApiPath("cdn64", "streams", id.toString(), "disable"))

    /** Creates a healthcheck for the target of this stream (module Healthchecks). `POST /cdn64/streams/{id}/healthcheck` */
    public suspend fun createHealthcheck(id: Long): ProxyHealthcheckRef =
        transport.json(Method.POST, ApiPath("cdn64", "streams", id.toString(), "healthcheck"))
}
