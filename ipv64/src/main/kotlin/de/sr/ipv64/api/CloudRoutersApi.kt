package de.sr.ipv64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.data
import de.sr.ipv64.internal.list
import de.sr.ipv64.model.CloudRouter
import de.sr.ipv64.model.CreateCloudRouterRequest
import de.sr.ipv64.model.CreateFirewallRuleRequest
import de.sr.ipv64.model.CreateGatewayRequest
import de.sr.ipv64.model.CreateRouteRequest
import de.sr.ipv64.model.DeletionState
import de.sr.ipv64.model.FirewallRule
import de.sr.ipv64.model.Gateway
import de.sr.ipv64.model.PublicIp
import de.sr.ipv64.model.Route
import de.sr.ipv64.model.SetDisabledRequest
import de.sr.ipv64.model.UpdateCloudRouterRequest

/** Tag *Cloud Router*: routers, their gateways, routes, firewall rules and public IPs. */
public class CloudRoutersApi internal constructor(private val transport: HttpTransport) {

    /** All Cloud Routers. `GET /cloud-routers` */
    public suspend fun list(): List<CloudRouter> =
        transport.list(Method.GET, ApiPath("cloud-routers"))

    /** Creates a router together with its first gateway. `POST /cloud-routers` */
    public suspend fun create(name: String? = null): CloudRouter =
        transport.data(Method.POST, ApiPath("cloud-routers"), body = transport.body(CreateCloudRouterRequest(name)))

    /** A router with gateways, peers and IPs. `GET /cloud-routers/{token}` */
    public suspend fun get(token: String): CloudRouter =
        transport.data(Method.GET, ApiPath("cloud-routers", token))

    /** Renames a router. `PATCH /cloud-routers/{token}` */
    public suspend fun rename(token: String, name: String): CloudRouter =
        transport.data(
            Method.PATCH,
            ApiPath("cloud-routers", token),
            body = transport.body(UpdateCloudRouterRequest(name)),
        )

    /** Adds another gateway to the router. `POST /cloud-routers/{token}/gateways` */
    public suspend fun createGateway(token: String, name: String? = null): Gateway =
        transport.data(
            Method.POST,
            ApiPath("cloud-routers", token, "gateways"),
            body = transport.body(CreateGatewayRequest(name)),
        )

    /** Static routes of the router. `GET /cloud-routers/{token}/routes` */
    public suspend fun listRoutes(token: String): List<Route> =
        transport.list(Method.GET, ApiPath("cloud-routers", token, "routes"))

    /** Creates a static route into a network behind a peer. `POST /cloud-routers/{token}/routes` */
    public suspend fun createRoute(token: String, request: CreateRouteRequest): Route =
        transport.data(Method.POST, ApiPath("cloud-routers", token, "routes"), body = transport.body(request))

    /** Enables or disables a route. `PATCH /cloud-routers/{token}/routes/{id}` */
    public suspend fun setRouteDisabled(token: String, id: Long, disabled: Boolean): Route =
        transport.data(
            Method.PATCH,
            ApiPath("cloud-routers", token, "routes", id.toString()),
            body = transport.body(SetDisabledRequest(disabled)),
        )

    /** Deletes a route; system routes cannot be deleted. `DELETE /cloud-routers/{token}/routes/{id}` */
    public suspend fun deleteRoute(token: String, id: Long): DeletionState =
        transport.data(Method.DELETE, ApiPath("cloud-routers", token, "routes", id.toString()))

    /** Firewall rules of the router. `GET /cloud-routers/{token}/firewall` */
    public suspend fun listFirewallRules(token: String): List<FirewallRule> =
        transport.list(Method.GET, ApiPath("cloud-routers", token, "firewall"))

    /**
     * Creates a firewall rule. Source and destination must belong to the same family;
     * at least one of them is required. `POST /cloud-routers/{token}/firewall`
     */
    public suspend fun createFirewallRule(token: String, request: CreateFirewallRuleRequest): FirewallRule =
        transport.data(Method.POST, ApiPath("cloud-routers", token, "firewall"), body = transport.body(request))

    /** Enables or disables a firewall rule. `PATCH /cloud-routers/{token}/firewall/{id}` */
    public suspend fun setFirewallRuleDisabled(token: String, id: Long, disabled: Boolean): FirewallRule =
        transport.data(
            Method.PATCH,
            ApiPath("cloud-routers", token, "firewall", id.toString()),
            body = transport.body(SetDisabledRequest(disabled)),
        )

    /** Deletes a firewall rule. `DELETE /cloud-routers/{token}/firewall/{id}` */
    public suspend fun deleteFirewallRule(token: String, id: Long): DeletionState =
        transport.data(Method.DELETE, ApiPath("cloud-routers", token, "firewall", id.toString()))

    /** Public IPs of the router. `GET /cloud-routers/{token}/public-ips` */
    public suspend fun listPublicIps(token: String): List<PublicIp> =
        transport.list(Method.GET, ApiPath("cloud-routers", token, "public-ips"))

    /**
     * Creates a public IPv6 (at most one per router); the address is assigned randomly from the
     * VPN server's network. `POST /cloud-routers/{token}/public-ips`
     */
    public suspend fun createPublicIp(token: String): PublicIp =
        transport.data(Method.POST, ApiPath("cloud-routers", token, "public-ips"))

    /** Enables or disables a public IP. `PATCH /cloud-routers/{token}/public-ips/{id}` */
    public suspend fun setPublicIpDisabled(token: String, id: Long, disabled: Boolean): PublicIp =
        transport.data(
            Method.PATCH,
            ApiPath("cloud-routers", token, "public-ips", id.toString()),
            body = transport.body(SetDisabledRequest(disabled)),
        )

    /** Deletes a public IP; port forwards on it are re-established. `DELETE /cloud-routers/{token}/public-ips/{id}` */
    public suspend fun deletePublicIp(token: String, id: Long): DeletionState =
        transport.data(Method.DELETE, ApiPath("cloud-routers", token, "public-ips", id.toString()))
}
