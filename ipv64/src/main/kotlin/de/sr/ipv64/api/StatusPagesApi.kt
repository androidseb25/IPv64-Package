package de.sr.ipv64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.data
import de.sr.ipv64.internal.list
import de.sr.ipv64.model.CreateStatusPageRequest
import de.sr.ipv64.model.StatusPage
import de.sr.ipv64.model.UpdateStatusPageRequest

/** Tag *Statusseiten*: public status pages showing healthchecks. */
public class StatusPagesApi internal constructor(private val transport: HttpTransport) {

    /** Lists all status pages. `GET /status-pages` */
    public suspend fun list(): List<StatusPage> =
        transport.list(Method.GET, ApiPath("status-pages"))

    /** Creates a status page. Without a slug a random address is assigned. `POST /status-pages` */
    public suspend fun create(request: CreateStatusPageRequest = CreateStatusPageRequest()): StatusPage =
        transport.data(Method.POST, ApiPath("status-pages"), body = transport.body(request))

    /** Returns a single status page. `GET /status-pages/{id}` */
    public suspend fun get(id: String): StatusPage =
        transport.data(Method.GET, ApiPath("status-pages", id))

    /** Updates a status page. `PATCH /status-pages/{id}` */
    public suspend fun update(id: String, request: UpdateStatusPageRequest): StatusPage =
        transport.data(Method.PATCH, ApiPath("status-pages", id), body = transport.body(request))

    /** Deletes a status page. `DELETE /status-pages/{id}` */
    public suspend fun delete(id: String) {
        transport.unit(Method.DELETE, ApiPath("status-pages", id))
    }
}
