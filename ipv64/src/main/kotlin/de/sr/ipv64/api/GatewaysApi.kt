package de.sr.ipv64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.data
import de.sr.ipv64.internal.list
import de.sr.ipv64.model.CreatePeerRequest
import de.sr.ipv64.model.CreatePortForwardRequest
import de.sr.ipv64.model.DeletionState
import de.sr.ipv64.model.Gateway
import de.sr.ipv64.model.Peer
import de.sr.ipv64.model.PortForward
import de.sr.ipv64.model.SetDisabledRequest
import de.sr.ipv64.model.SetOspfRequest
import de.sr.ipv64.model.UpdateGatewayRequest

/** Tag *Gateways*: gateways of a Cloud Router, OSPF, port forwards and their peers. */
public class GatewaysApi internal constructor(private val transport: HttpTransport) {

    /** A gateway with its peers. `GET /gateways/{token}` */
    public suspend fun get(token: String): Gateway =
        transport.data(Method.GET, ApiPath("gateways", token))

    /** Changes a gateway. Addresses are CIDR; an empty string removes an address. `PATCH /gateways/{token}` */
    public suspend fun update(token: String, request: UpdateGatewayRequest): Gateway =
        transport.data(Method.PATCH, ApiPath("gateways", token), body = transport.body(request))

    /** Deletes a gateway with all its peers. `DELETE /gateways/{token}` */
    public suspend fun delete(token: String): Gateway =
        transport.data(Method.DELETE, ApiPath("gateways", token))

    /** Generates a new key pair; afterwards all peers need the new configuration. `POST /gateways/{token}/rotate-keys` */
    public suspend fun rotateKeys(token: String): Gateway =
        transport.data(Method.POST, ApiPath("gateways", token, "rotate-keys"))

    /** Resets the traffic counters. `POST /gateways/{token}/reset-traffic` */
    public suspend fun resetTraffic(token: String): Gateway =
        transport.data(Method.POST, ApiPath("gateways", token, "reset-traffic"))

    /** Enables OSPF or changes its settings (area, version 2-3, cost 1-256). `PUT /gateways/{token}/ospf` */
    public suspend fun setOspf(token: String, request: SetOspfRequest = SetOspfRequest()): Gateway =
        transport.data(Method.PUT, ApiPath("gateways", token, "ospf"), body = transport.body(request))

    /** Disables OSPF. `DELETE /gateways/{token}/ospf` */
    public suspend fun disableOspf(token: String): Gateway =
        transport.data(Method.DELETE, ApiPath("gateways", token, "ospf"))

    /** Port forwards of the gateway. `GET /gateways/{token}/port-forwards` */
    public suspend fun listPortForwards(token: String): List<PortForward> =
        transport.list(Method.GET, ApiPath("gateways", token, "port-forwards"))

    /** Creates a port forward; the server assigns the public port (`publicPort`). `POST /gateways/{token}/port-forwards` */
    public suspend fun createPortForward(token: String, request: CreatePortForwardRequest): PortForward =
        transport.data(Method.POST, ApiPath("gateways", token, "port-forwards"), body = transport.body(request))

    /** Enables or disables a port forward. `PATCH /gateways/{token}/port-forwards/{id}` */
    public suspend fun setPortForwardDisabled(token: String, id: Long, disabled: Boolean): PortForward =
        transport.data(
            Method.PATCH,
            ApiPath("gateways", token, "port-forwards", id.toString()),
            body = transport.body(SetDisabledRequest(disabled)),
        )

    /** Deletes a port forward. `DELETE /gateways/{token}/port-forwards/{id}` */
    public suspend fun deletePortForward(token: String, id: Long): DeletionState =
        transport.data(Method.DELETE, ApiPath("gateways", token, "port-forwards", id.toString()))

    /** Peers of the gateway. `GET /gateways/{token}/peers` */
    public suspend fun listPeers(token: String): List<Peer> =
        transport.list(Method.GET, ApiPath("gateways", token, "peers"))

    /** Creates a peer; keys and addresses are assigned automatically. `POST /gateways/{token}/peers` */
    public suspend fun createPeer(token: String, name: String? = null): Peer =
        transport.data(
            Method.POST,
            ApiPath("gateways", token, "peers"),
            body = transport.body(CreatePeerRequest(name)),
        )
}
