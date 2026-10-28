package de.sr.ipv64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.data
import de.sr.ipv64.model.AddPeerAllowedIpRequest
import de.sr.ipv64.model.Peer
import de.sr.ipv64.model.SetPeerKeysRequest
import de.sr.ipv64.model.UpdatePeerRequest

/** Tag *Peers*: WireGuard peers, their configuration, keys and allowed IPs. */
public class PeersApi internal constructor(private val transport: HttpTransport) {

    /** A peer with its allowed IPs. `GET /peers/{token}` */
    public suspend fun get(token: String): Peer =
        transport.data(Method.GET, ApiPath("peers", token))

    /**
     * Changes a peer. `name` and `dnsServer` apply immediately, all other fields are applied
     * asynchronously at the gateway. `PATCH /peers/{token}`
     */
    public suspend fun update(token: String, request: UpdatePeerRequest): Peer =
        transport.data(Method.PATCH, ApiPath("peers", token), body = transport.body(request))

    /** Deletes a peer. `DELETE /peers/{token}` */
    public suspend fun delete(token: String): Peer =
        transport.data(Method.DELETE, ApiPath("peers", token))

    /**
     * The WireGuard configuration as `.conf` text (requested with `Accept: text/plain`).
     * It contains the peer's private key. `GET /peers/{token}/config`
     */
    public suspend fun config(token: String): String =
        transport.text(
            Method.GET,
            ApiPath("peers", token, "config"),
            headers = mapOf("Accept" to "text/plain"),
        )

    /** Generates a new key pair. `POST /peers/{token}/rotate-keys` */
    public suspend fun rotateKeys(token: String): Peer =
        transport.data(Method.POST, ApiPath("peers", token, "rotate-keys"))

    /** Generates a new preshared key. `POST /peers/{token}/rotate-preshared-key` */
    public suspend fun rotatePresharedKey(token: String): Peer =
        transport.data(Method.POST, ApiPath("peers", token, "rotate-preshared-key"))

    /**
     * Sets own keys. Without a private key only the public key is stored and the config
     * contains no private key. `PUT /peers/{token}/keys`
     */
    public suspend fun setKeys(token: String, request: SetPeerKeysRequest): Peer =
        transport.data(Method.PUT, ApiPath("peers", token, "keys"), body = transport.body(request))

    /** Adds an additional network (CIDR) to the client config's allowed IPs. `POST /peers/{token}/allowed-ips` */
    public suspend fun addAllowedIp(token: String, network: String): Peer =
        transport.data(
            Method.POST,
            ApiPath("peers", token, "allowed-ips"),
            body = transport.body(AddPeerAllowedIpRequest(network)),
        )

    /** Removes an allowed IP. `DELETE /peers/{token}/allowed-ips/{id}` */
    public suspend fun removeAllowedIp(token: String, id: Long) {
        transport.unit(Method.DELETE, ApiPath("peers", token, "allowed-ips", id.toString()))
    }
}
