package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.internal.query
import de.sr.ipv64.node64.model.DockerContainers
import de.sr.ipv64.node64.model.DockerSystemRequest
import de.sr.ipv64.node64.model.PVESeries
import de.sr.ipv64.node64.model.PVESettingsInput
import de.sr.ipv64.node64.model.PVESettingsOverview
import de.sr.ipv64.node64.model.PVESettingsResult
import de.sr.ipv64.node64.model.PVESystem
import de.sr.ipv64.node64.model.PVESystemInput
import de.sr.ipv64.node64.model.PVEResources
import de.sr.ipv64.node64.model.SeriesRange

/** Tag *Healthchecks*: Docker monitoring (`/healthchecks/docker`). Docker hosts are modelled as [PVESystem]. */
public class DockerApi internal constructor(private val transport: HttpTransport) {

    /** All containers of the Docker host with monitoring values and Docker checks. `GET /healthchecks/docker/containers` */
    public suspend fun containers(): DockerContainers =
        transport.json(Method.GET, ApiPath("healthchecks", "docker", "containers"))

    /** All Docker hosts with state (`guests` = containers). `GET /healthchecks/docker/systems` */
    public suspend fun systems(): List<PVESystem> =
        transport.json(Method.GET, ApiPath("healthchecks", "docker", "systems"))

    /** Creates a Docker host. Without addresses the local socket is used. `POST /healthchecks/docker/systems` */
    public suspend fun createSystem(request: DockerSystemRequest = DockerSystemRequest()): PVESystem =
        transport.json(Method.POST, ApiPath("healthchecks", "docker", "systems"), body = transport.body(request))

    /** A Docker host. `GET /healthchecks/docker/systems/{id}` */
    public suspend fun getSystem(id: Long): PVESystem =
        transport.json(Method.GET, ApiPath("healthchecks", "docker", "systems", id.toString()))

    /** Partial change of a Docker host (name, interval, on/off). `PUT /healthchecks/docker/systems/{id}` */
    public suspend fun updateSystem(id: Long, input: PVESystemInput): PVESystem =
        transport.json(
            Method.PUT,
            ApiPath("healthchecks", "docker", "systems", id.toString()),
            body = transport.body(input),
        )

    /** Removes a Docker host with settings, history and "as check" checks. `DELETE /healthchecks/docker/systems/{id}` */
    public suspend fun deleteSystem(id: Long) {
        transport.unit(Method.DELETE, ApiPath("healthchecks", "docker", "systems", id.toString()))
    }

    /** Containers of a Docker host. `GET /healthchecks/docker/systems/{id}/resources` */
    public suspend fun resources(id: Long): PVEResources =
        transport.json(Method.GET, ApiPath("healthchecks", "docker", "systems", id.toString(), "resources"))

    /** History of a container; [key] is `docker/<name>`. `GET /healthchecks/docker/systems/{id}/series` */
    public suspend fun series(id: Long, key: String, range: SeriesRange? = null): PVESeries =
        transport.json(
            Method.GET,
            ApiPath("healthchecks", "docker", "systems", id.toString(), "series"),
            query("key" to key, "range" to range),
        )

    /** Settings and rules of a Docker host. `GET /healthchecks/docker/systems/{id}/settings` */
    public suspend fun settings(id: Long): PVESettingsOverview =
        transport.json(Method.GET, ApiPath("healthchecks", "docker", "systems", id.toString(), "settings"))

    /**
     * Sets the settings of one level; [key] empty = system, otherwise `docker/<name>`.
     * `asCheck` creates a Docker check.
     * `PUT /healthchecks/docker/systems/{id}/settings`
     */
    public suspend fun updateSettings(id: Long, input: PVESettingsInput, key: String? = null): PVESettingsResult =
        transport.json(
            Method.PUT,
            ApiPath("healthchecks", "docker", "systems", id.toString(), "settings"),
            query("key" to key),
            body = transport.body(input),
        )

    /** Resets a level to "inherit everything"; "as check" stays. `DELETE /healthchecks/docker/systems/{id}/settings` */
    public suspend fun resetSettings(id: Long, key: String? = null) {
        transport.unit(
            Method.DELETE,
            ApiPath("healthchecks", "docker", "systems", id.toString(), "settings"),
            query("key" to key),
        )
    }
}
