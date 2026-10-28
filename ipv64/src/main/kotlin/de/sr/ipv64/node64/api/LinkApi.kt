package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.model.ConflictResolution
import de.sr.ipv64.node64.model.Ipv64KeyStatus
import de.sr.ipv64.node64.model.Link
import de.sr.ipv64.node64.model.LinkConflict
import de.sr.ipv64.node64.model.LinkSettings
import de.sr.ipv64.node64.model.LinkWithApiKeyRequest
import de.sr.ipv64.node64.model.ResolveConflictRequest
import de.sr.ipv64.node64.model.SetIpv64KeyRequest
import de.sr.ipv64.node64.model.UnlinkRequest

/** Tag *Verknüpfung*: link of the node with an ipv64.net account. */
public class LinkApi internal constructor(private val transport: HttpTransport) {

    /** State of the healthcheck sync with ipv64. `GET /link` */
    public suspend fun get(): Link =
        transport.json(Method.GET, ApiPath("link"))

    /**
     * Links with one click using the central ipv64 API key (see [setIpv64Key]). Waits up to 10 s for the node token;
     * the node may answer with 202 while the link is still pending.
     * `POST /link/apikey`
     */
    public suspend fun linkWithApiKey(request: LinkWithApiKeyRequest = LinkWithApiKeyRequest()): Link =
        transport.json(Method.POST, ApiPath("link", "apikey"), body = transport.body(request))

    /** Removes the link. `POST /link/unlink` */
    public suspend fun unlink(keepChecks: Boolean? = null) {
        transport.unit(Method.POST, ApiPath("link", "unlink"), body = transport.body(UnlinkRequest(keepChecks)))
    }

    /** Open conflicts (local and server version). `GET /link/conflicts` */
    public suspend fun conflicts(): List<LinkConflict> =
        transport.json(Method.GET, ApiPath("link", "conflicts"))

    /** Resolves a conflict ([LinkConflict.id]) by keeping the server or the local version. `POST /link/conflicts/{id}` */
    public suspend fun resolveConflict(id: Long, keep: ConflictResolution) {
        transport.unit(
            Method.POST,
            ApiPath("link", "conflicts", id.toString()),
            body = transport.body(ResolveConflictRequest(keep)),
        )
    }

    /** Sync settings; effective immediately. `PUT /link/settings` */
    public suspend fun updateSettings(localNotifications: Boolean): LinkSettings =
        transport.json(
            Method.PUT,
            ApiPath("link", "settings"),
            body = transport.body(LinkSettings(localNotifications)),
        )

    /** Central ipv64 API key: account, plan, expiry. `GET /ipv64` */
    public suspend fun ipv64Key(): Ipv64KeyStatus =
        transport.json(Method.GET, ApiPath("ipv64"))

    /** Stores the ipv64 API key (verified with ipv64, stored encrypted). `PUT /ipv64/key` */
    public suspend fun setIpv64Key(key: String): Ipv64KeyStatus =
        transport.json(Method.PUT, ApiPath("ipv64", "key"), body = transport.body(SetIpv64KeyRequest(key)))

    /** Removes the ipv64 API key. `DELETE /ipv64/key` */
    public suspend fun deleteIpv64Key() {
        transport.unit(Method.DELETE, ApiPath("ipv64", "key"))
    }
}
