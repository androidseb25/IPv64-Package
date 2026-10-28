package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.internal.jsonOptional
import de.sr.ipv64.node64.model.Maintenance
import de.sr.ipv64.node64.model.MaintenanceInput

/** Tag *Healthchecks*: maintenance windows. */
public class MaintenanceApi internal constructor(private val transport: HttpTransport) {

    /** All maintenance windows. `GET /maintenance` */
    public suspend fun list(): List<Maintenance> =
        transport.json(Method.GET, ApiPath("maintenance"))

    /**
     * Creates a maintenance window. Response body is not documented by the API; the window is returned
     * if the node sends it.
     * `POST /maintenance`
     */
    public suspend fun create(input: MaintenanceInput): Maintenance? =
        transport.jsonOptional(Method.POST, ApiPath("maintenance"), body = transport.body(input))

    /**
     * Replaces a maintenance window completely (send all fields). Windows of other nodes and from the portal can
     * be changed too. Response body is not documented by the API; the window is returned if the node sends it.
     * `PUT /maintenance/{id}`
     */
    public suspend fun update(id: Long, input: MaintenanceInput): Maintenance? =
        transport.jsonOptional(Method.PUT, ApiPath("maintenance", id.toString()), body = transport.body(input))

    /** Deletes a window everywhere; also answers 204 for unknown IDs. `DELETE /maintenance/{id}` */
    public suspend fun delete(id: Long) {
        transport.unit(Method.DELETE, ApiPath("maintenance", id.toString()))
    }
}
