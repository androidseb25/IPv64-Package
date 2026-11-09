package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.model.UpdateApplyResult
import de.sr.ipv64.node64.model.UpdateResetResult
import de.sr.ipv64.node64.model.UpdateSettingsRequest
import kotlinx.serialization.json.JsonObject

/** Tag *Node*: self-update. */
public class UpdateApi internal constructor(private val transport: HttpTransport) {

    /** Running version, origin, settings (Mode, Channel, Chosen, Window, Modes, Channels) and last check (Latest). `GET /update` */
    public suspend fun get(): JsonObject =
        transport.json(Method.GET, ApiPath("update"))

    /** Checks for updates now. `POST /update/check` */
    public suspend fun check(): JsonObject =
        transport.json(Method.POST, ApiPath("update", "check"))

    /** Installs the latest version of the channel in the background; the node restarts. `POST /update/apply` */
    public suspend fun apply(): UpdateApplyResult =
        transport.json(Method.POST, ApiPath("update", "apply"))

    /** Goes back to the version from the image; the node restarts. `POST /update/reset` */
    public suspend fun reset(): UpdateResetResult =
        transport.json(Method.POST, ApiPath("update", "reset"))

    /** Changes mode, channel and window; missing fields stay. `PUT /update/settings` */
    public suspend fun updateSettings(request: UpdateSettingsRequest): JsonObject =
        transport.json(Method.PUT, ApiPath("update", "settings"), body = transport.body(request))
}
