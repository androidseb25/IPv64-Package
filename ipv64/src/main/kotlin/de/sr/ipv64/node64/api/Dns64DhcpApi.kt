package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.model.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** DHCP server of the DNS64 resolver: networks, fixed addresses, leases and IPv6 router advertisement. */
public class Dns64DhcpApi internal constructor(private val transport: HttpTransport) {

    /** Switch and state of the DHCP server. Response body is not documented by the API. `GET /dns64/dhcp` */
    public suspend fun get(): JsonElement? =
        transport.jsonOrNull(Method.GET, ApiPath("dns64", "dhcp"))

    /** Switches the DHCP server (per node, never in a cluster). `PUT /dns64/dhcp/mode` */
    public suspend fun setMode(mode: DNS64DhcpMode) {
        transport.unit(
            Method.PUT, ApiPath("dns64", "dhcp", "mode"),
            body = transport.body(buildJsonObject { put("mode", mode.value) }),
        )
    }

    /** Sets the role of this node in the cluster (per node). `PUT /dns64/dhcp/role` */
    public suspend fun setRole(role: DNS64DhcpRole) {
        transport.unit(
            Method.PUT, ApiPath("dns64", "dhcp", "role"),
            body = transport.body(buildJsonObject { put("role", role.value) }),
        )
    }

    /** Lists DHCP networks. `GET /dns64/dhcp/scopes` */
    public suspend fun listScopes(): List<DNS64DHCPScope> =
        transport.json(Method.GET, ApiPath("dns64", "dhcp", "scopes"))

    /** Creates a DHCP network. `POST /dns64/dhcp/scopes` */
    public suspend fun createScope(request: DNS64DHCPScope): DNS64DHCPScope =
        transport.json(Method.POST, ApiPath("dns64", "dhcp", "scopes"), body = transport.body(request))

    /** Changes a DHCP network (enabled state stays, the network itself cannot change). `PUT /dns64/dhcp/scopes/{id}` */
    public suspend fun updateScope(id: Long, request: DNS64DHCPScope): DNS64DHCPScope =
        transport.json(Method.PUT, ApiPath("dns64", "dhcp", "scopes", id.toString()), body = transport.body(request))

    /** Deletes a DHCP network with its fixed addresses and leases. `DELETE /dns64/dhcp/scopes/{id}` */
    public suspend fun deleteScope(id: Long) {
        transport.unit(Method.DELETE, ApiPath("dns64", "dhcp", "scopes", id.toString()))
    }

    /** Enables a DHCP network. `POST /dns64/dhcp/scopes/{id}/enable` */
    public suspend fun enableScope(id: Long) {
        transport.unit(Method.POST, ApiPath("dns64", "dhcp", "scopes", id.toString(), "enable"))
    }

    /** Disables a DHCP network. `POST /dns64/dhcp/scopes/{id}/disable` */
    public suspend fun disableScope(id: Long) {
        transport.unit(Method.POST, ApiPath("dns64", "dhcp", "scopes", id.toString(), "disable"))
    }

    /** Lists fixed DHCP addresses. `GET /dns64/dhcp/reservations` */
    public suspend fun listReservations(): List<DNS64DHCPReservation> =
        transport.json(Method.GET, ApiPath("dns64", "dhcp", "reservations"))

    /** Creates a fixed DHCP address. `POST /dns64/dhcp/reservations` */
    public suspend fun createReservation(request: DNS64DHCPReservation): DNS64DHCPReservation =
        transport.json(Method.POST, ApiPath("dns64", "dhcp", "reservations"), body = transport.body(request))

    /** Changes a fixed DHCP address. `PUT /dns64/dhcp/reservations/{id}` */
    public suspend fun updateReservation(id: Long, request: DNS64DHCPReservation): DNS64DHCPReservation =
        transport.json(Method.PUT, ApiPath("dns64", "dhcp", "reservations", id.toString()), body = transport.body(request))

    /** Deletes a fixed DHCP address. `DELETE /dns64/dhcp/reservations/{id}` */
    public suspend fun deleteReservation(id: Long) {
        transport.unit(Method.DELETE, ApiPath("dns64", "dhcp", "reservations", id.toString()))
    }

    /** Releases a leased address (the path segments are URL-encoded). `DELETE /dns64/dhcp/leases/{scope}/{mac}` */
    public suspend fun releaseLease(scope: Long, mac: String) {
        transport.unit(Method.DELETE, ApiPath("dns64", "dhcp", "leases", scope.toString(), mac))
    }

    /** Returns settings and state of IPv6 DNS via router advertisement. `GET /dns64/dhcp/ipv6` */
    public suspend fun getIpv6(): DNS64DhcpIpv6 =
        transport.json(Method.GET, ApiPath("dns64", "dhcp", "ipv6"))

    /** Sets IPv6 DNS via router advertisement. `PUT /dns64/dhcp/ipv6` */
    public suspend fun setIpv6(config: DNS64DhcpIpv6Config) {
        transport.unit(Method.PUT, ApiPath("dns64", "dhcp", "ipv6"), body = transport.body(config))
    }

    /** Asks routers in the network (router solicitation, 3 s). `POST /dns64/dhcp/ipv6/probe` */
    public suspend fun probeIpv6(): DNS64DhcpIpv6Probe =
        transport.json(Method.POST, ApiPath("dns64", "dhcp", "ipv6", "probe"))
}
