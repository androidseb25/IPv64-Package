package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.internal.query
import de.sr.ipv64.node64.model.PVEProbe
import de.sr.ipv64.node64.model.PVEProbeRequest
import de.sr.ipv64.node64.model.PVEResources
import de.sr.ipv64.node64.model.PVESeries
import de.sr.ipv64.node64.model.PVESettingsInput
import de.sr.ipv64.node64.model.PVESettingsOverview
import de.sr.ipv64.node64.model.PVESettingsResult
import de.sr.ipv64.node64.model.PVESystem
import de.sr.ipv64.node64.model.PVESystemInput
import de.sr.ipv64.node64.model.PVESystemKindFilter
import de.sr.ipv64.node64.model.SeriesRange

/** Tag *Healthchecks*: Proxmox VE monitoring (`/healthchecks/proxmox`). */
public class ProxmoxApi internal constructor(private val transport: HttpTransport) {

    /** All Proxmox systems with state (without secret); Docker hosts only with `kind` docker/all. `GET /healthchecks/proxmox/systems` */
    public suspend fun systems(kind: PVESystemKindFilter? = null): List<PVESystem> =
        transport.json(Method.GET, ApiPath("healthchecks", "proxmox", "systems"), query("kind" to kind))

    /** Creates a Proxmox system and starts polling; the secret is stored encrypted. `POST /healthchecks/proxmox/systems` */
    public suspend fun createSystem(input: PVESystemInput): PVESystem =
        transport.json(Method.POST, ApiPath("healthchecks", "proxmox", "systems"), body = transport.body(input))

    /** Tests the connection: fetches certificate fingerprint, version, cluster, nodes and guests. `POST /healthchecks/proxmox/probe` */
    public suspend fun probe(request: PVEProbeRequest): PVEProbe =
        transport.json(Method.POST, ApiPath("healthchecks", "proxmox", "probe"), body = transport.body(request))

    /** A Proxmox system. `GET /healthchecks/proxmox/systems/{id}` */
    public suspend fun getSystem(id: Long): PVESystem =
        transport.json(Method.GET, ApiPath("healthchecks", "proxmox", "systems", id.toString()))

    /** Partial change; an empty secret keeps the stored one. `PUT /healthchecks/proxmox/systems/{id}` */
    public suspend fun updateSystem(id: Long, input: PVESystemInput): PVESystem =
        transport.json(
            Method.PUT,
            ApiPath("healthchecks", "proxmox", "systems", id.toString()),
            body = transport.body(input),
        )

    /** Deletes a system with settings, history and "as check" checks. `DELETE /healthchecks/proxmox/systems/{id}` */
    public suspend fun deleteSystem(id: Long) {
        transport.unit(Method.DELETE, ApiPath("healthchecks", "proxmox", "systems", id.toString()))
    }

    /** Nodes and guests of the last poll. `GET /healthchecks/proxmox/systems/{id}/resources` */
    public suspend fun resources(id: Long): PVEResources =
        transport.json(Method.GET, ApiPath("healthchecks", "proxmox", "systems", id.toString(), "resources"))

    /** History of a node or guest; [key] is `node/<name>`, `qemu/<vmid>` or `lxc/<vmid>`. `GET /healthchecks/proxmox/systems/{id}/series` */
    public suspend fun series(id: Long, key: String, range: SeriesRange? = null): PVESeries =
        transport.json(
            Method.GET,
            ApiPath("healthchecks", "proxmox", "systems", id.toString(), "series"),
            query("key" to key, "range" to range),
        )

    /** Settings and effective rules. `GET /healthchecks/proxmox/systems/{id}/settings` */
    public suspend fun settings(id: Long): PVESettingsOverview =
        transport.json(Method.GET, ApiPath("healthchecks", "proxmox", "systems", id.toString(), "settings"))

    /** Sets the settings of one level; [key] empty = system, otherwise `node/<name>`, `qemu/<vmid>`, `lxc/<vmid>`. `PUT /healthchecks/proxmox/systems/{id}/settings` */
    public suspend fun updateSettings(id: Long, input: PVESettingsInput, key: String? = null): PVESettingsResult =
        transport.json(
            Method.PUT,
            ApiPath("healthchecks", "proxmox", "systems", id.toString(), "settings"),
            query("key" to key),
            body = transport.body(input),
        )

    /** Resets a level to "inherit everything"; "as check" stays. `DELETE /healthchecks/proxmox/systems/{id}/settings` */
    public suspend fun resetSettings(id: Long, key: String? = null) {
        transport.unit(
            Method.DELETE,
            ApiPath("healthchecks", "proxmox", "systems", id.toString(), "settings"),
            query("key" to key),
        )
    }
}
