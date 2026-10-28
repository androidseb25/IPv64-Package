package de.sr.ipv64.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Body of `POST /cloud-routers`. */
@Serializable
public data class CreateCloudRouterRequest(val name: String? = null)

/** Body of `PATCH /cloud-routers/{token}`. */
@Serializable
public data class UpdateCloudRouterRequest(val name: String? = null)

/** Body of `POST /cloud-routers/{token}/gateways`. */
@Serializable
public data class CreateGatewayRequest(val name: String? = null)

/** Body of `POST /cloud-routers/{token}/routes`. */
@Serializable
public data class CreateRouteRequest(
    /** Destination network in CIDR. */
    val destination: String,
    /** Next hop, same address family. */
    val gateway: String,
    /** Peer ID. */
    val peer: String,
    /** Distance (1-255). */
    val distance: Int? = null,
)

/** Body of `POST /cloud-routers/{token}/firewall`. At least one of source and destination is required. */
@Serializable
public data class CreateFirewallRuleRequest(
    /** Chain. */
    val chain: FirewallChain,
    /** Action. */
    val action: FirewallAction,
    /** Source as IP or CIDR. */
    val source: String? = null,
    /** Destination as IP or CIDR; same family as source. */
    val destination: String? = null,
)

/** Body of the enable/disable PATCH operations (routes, firewall, public IPs, port forwards). */
@Serializable
public data class SetDisabledRequest(val disabled: Boolean? = null)

/** Body of `PATCH /gateways/{token}`. An empty string removes an address. */
@Serializable
public data class UpdateGatewayRequest(
    /** Name. */
    val name: String? = null,
    /** Gateway IPv4 address (CIDR). */
    val ipv4: String? = null,
    /** Gateway IPv6 address (CIDR). */
    val ipv6: String? = null,
    /** Disable the gateway. */
    val disabled: Boolean? = null,
)

/** Body of `PUT /gateways/{token}/ospf`. */
@Serializable
public data class SetOspfRequest(
    /** OSPF area. */
    @SerialName("area_id") val areaId: String? = null,
    /** OSPF version (2-3). */
    val version: Int? = null,
    /** Cost (1-256). */
    val cost: Int? = null,
)

/** Body of `POST /gateways/{token}/port-forwards`. */
@Serializable
public data class CreatePortForwardRequest(
    /** Destination IP in the tunnel. */
    val destination: String,
    /** Target port (1-65499). */
    @SerialName("target_port") val targetPort: Int,
    /** Protocol. */
    val protocol: PortForwardProtocol,
    /** Name. */
    val name: String? = null,
    /** Public IP to use; `router` requires an existing public IP. */
    @SerialName("public_ip") val publicIp: PortForwardPublicIp? = null,
)

/** Body of `POST /gateways/{token}/peers`. */
@Serializable
public data class CreatePeerRequest(val name: String? = null)

/** Body of `PATCH /peers/{token}`. `name` and `dnsServer` apply immediately, other fields are applied asynchronously. */
@Serializable
public data class UpdatePeerRequest(
    /** Name. */
    val name: String? = null,
    /** DNS server for the client config; empty string = none. */
    @SerialName("dns_server") val dnsServer: String? = null,
    /** Keepalive as `hh:mm:ss`. */
    val keepalive: String? = null,
    /** Fixed endpoint (IP or hostname). */
    val endpoint: String? = null,
    /** Endpoint port (0-65535). */
    @SerialName("endpoint_port") val endpointPort: Int? = null,
    /** Tunnel IPv4 in CIDR. */
    val ipv4: String? = null,
    /** Tunnel IPv6 in CIDR. */
    val ipv6: String? = null,
    /** Disabled. */
    val disabled: Boolean? = null,
    /** Site-to-site. */
    @SerialName("site_to_site") val siteToSite: Boolean? = null,
)

/** Body of `PUT /peers/{token}/keys`. [privateKey] and [presharedKey] are redacted in [toString]. */
@Serializable
public data class SetPeerKeysRequest(
    /** Public key (Base64). */
    @SerialName("public_key") val publicKey: String,
    /** Private key; must match the public key. Without it the config contains no private key. */
    @SerialName("private_key") val privateKey: String? = null,
    /** Preshared key (Base64). */
    @SerialName("preshared_key") val presharedKey: String? = null,
) {
    override fun toString(): String =
        "SetPeerKeysRequest(publicKey=$publicKey, privateKey=<redacted>, presharedKey=<redacted>)"
}

/** Body of `POST /peers/{token}/allowed-ips`. */
@Serializable
public data class AddPeerAllowedIpRequest(
    /** IP or network in CIDR. */
    val network: String,
)

/** Body of `POST /dns64/clients`. */
@Serializable
public data class CreateDns64ClientRequest(val name: String? = null)

/** Body of `PATCH /dns64/clients/{token}`. */
@Serializable
public data class UpdateDns64ClientRequest(
    /** Name. */
    val name: String? = null,
    /** Filter active. */
    @SerialName("blocking_enabled") val blockingEnabled: Boolean? = null,
    /** Active blocklists (`adult` and `threats` are not available in the free plan). */
    val blocklists: List<Dns64Blocklist>? = null,
)

/** Body of `POST /dns64/clients/{token}/rules`. */
@Serializable
public data class AddDns64RuleRequest(
    /** Domain. */
    val domain: String,
    /** Action. */
    val action: Dns64RuleAction? = null,
)

/** Body of `POST /dns64/clients/{token}/sources`: either a fixed [ip] or a DynDNS [record]. */
@Serializable
public data class AddDns64SourceRequest(
    /** Fixed IP. */
    val ip: String? = null,
    /** Record ID or token whose DynDNS address is tracked. */
    val record: String? = null,
)
