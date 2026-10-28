package de.sr.ipv64.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** State of a Cloud Router sub-resource (gateway, peer, route, firewall rule, port forward, public IP). */
@Serializable
public enum class ResourceState(override val value: String) : ApiValue {
    @SerialName("ready") READY("ready"),
    @SerialName("creating") CREATING("creating"),
    @SerialName("applying") APPLYING("applying"),
    @SerialName("deleting") DELETING("deleting"),
}

/** Firewall chain. */
@Serializable
public enum class FirewallChain(override val value: String) : ApiValue {
    @SerialName("forward") FORWARD("forward"),
    @SerialName("input") INPUT("input"),
    @SerialName("output") OUTPUT("output"),
}

/** Firewall rule action. */
@Serializable
public enum class FirewallAction(override val value: String) : ApiValue {
    @SerialName("accept") ACCEPT("accept"),
    @SerialName("drop") DROP("drop"),
}

/** Protocol of a port forward. */
@Serializable
public enum class PortForwardProtocol(override val value: String) : ApiValue {
    @SerialName("tcp") TCP("tcp"),
    @SerialName("udp") UDP("udp"),
}

/** Public IP a port forward is exposed on. */
@Serializable
public enum class PortForwardPublicIp(override val value: String) : ApiValue {
    /** Address of the VPN server. */
    @SerialName("server") SERVER("server"),
    /** Public IP of the router (must be created beforehand). */
    @SerialName("router") ROUTER("router"),
}

/** Traffic counters in bytes. */
@Serializable
public data class VpnTrafficBytes(
    /** Incoming bytes. */
    @SerialName("in") val inbound: Double? = null,
    /** Outgoing bytes. */
    @SerialName("out") val outbound: Double? = null,
)

/** Cloud Router. */
@Serializable
public data class CloudRouter(
    /** Router ID. */
    val id: String? = null,
    /** Name. */
    val name: String? = null,
    /** Internal VRF name. */
    val vrf: String? = null,
    /** Blocked because of the traffic limit. */
    @SerialName("traffic_limited") val trafficLimited: Boolean? = null,
    /** Gateways of the router. */
    val gateways: List<Gateway> = emptyList(),
    /** Public IPs (detail view only). */
    @SerialName("public_ips") val publicIps: List<PublicIp> = emptyList(),
)

/** Gateway of a Cloud Router. */
@Serializable
public data class Gateway(
    /** Gateway ID. */
    val id: String? = null,
    /** Name. */
    val name: String? = null,
    /** State. */
    val state: ResourceState? = null,
    /** Disabled. */
    val disabled: Boolean? = null,
    /** Blocked because of the traffic limit. */
    @SerialName("traffic_limited") val trafficLimited: Boolean? = null,
    /** VPN server the gateway runs on. */
    val server: GatewayServer? = null,
    /** WireGuard port on the server. */
    @SerialName("listen_port") val listenPort: Int? = null,
    /** Gateway IPv4 address (CIDR). */
    val ipv4: String? = null,
    /** Gateway IPv6 address (CIDR). */
    val ipv6: String? = null,
    /** Public key. */
    @SerialName("public_key") val publicKey: String? = null,
    /** OSPF settings, `null` when OSPF is off. */
    val ospf: GatewayOspf? = null,
    /** Traffic counters. */
    @SerialName("traffic_bytes") val trafficBytes: VpnTrafficBytes? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Peers (detail view only). */
    val peers: List<Peer> = emptyList(),
)

/** VPN server of a [Gateway]. */
@Serializable
public data class GatewayServer(
    /** Name. */
    val name: String? = null,
    /** Hostname. */
    val hostname: String? = null,
    /** Location. */
    val location: String? = null,
)

/** OSPF settings of a [Gateway]. */
@Serializable
public data class GatewayOspf(
    /** OSPF area. */
    @SerialName("area_id") val areaId: String? = null,
    /** OSPF version (2 or 3). */
    @SerialName("ospf_version") val ospfVersion: Int? = null,
    /** Cost. */
    val cost: Int? = null,
)

/** WireGuard peer of a [Gateway]. */
@Serializable
public data class Peer(
    /** Peer ID. */
    val id: String? = null,
    /** Name. */
    val name: String? = null,
    /** State. */
    val state: ResourceState? = null,
    /** Disabled. */
    val disabled: Boolean? = null,
    /** Marked as site-to-site. */
    @SerialName("site_to_site") val siteToSite: Boolean? = null,
    /** Tunnel IPv4 (CIDR). */
    val ipv4: String? = null,
    /** Tunnel IPv6 (CIDR). */
    val ipv6: String? = null,
    /** DNS server in the client config. */
    @SerialName("dns_server") val dnsServer: String? = null,
    /** Fixed endpoint of the peer. */
    val endpoint: String? = null,
    /** Endpoint port. */
    @SerialName("endpoint_port") val endpointPort: Int? = null,
    /** Keepalive as `hh:mm:ss`. */
    val keepalive: String? = null,
    /** Public key. */
    @SerialName("public_key") val publicKey: String? = null,
    /** Whether a preshared key is set. */
    @SerialName("has_preshared_key") val hasPresharedKey: Boolean? = null,
    /** Last seen address. */
    @SerialName("current_endpoint") val currentEndpoint: String? = null,
    /** Last handshake. */
    @SerialName("last_handshake") val lastHandshake: String? = null,
    /** Traffic counters. */
    @SerialName("traffic_bytes") val trafficBytes: VpnTrafficBytes? = null,
    /** Additional allowed IPs (detail view only). */
    @SerialName("allowed_ips") val allowedIps: List<PeerAllowedIp> = emptyList(),
)

/** Additional allowed IP of a [Peer]. */
@Serializable
public data class PeerAllowedIp(
    /** ID. */
    val id: Long? = null,
    /** Network. */
    val network: String? = null,
)

/** Public IP of a Cloud Router. */
@Serializable
public data class PublicIp(
    /** ID. */
    val id: Long? = null,
    /** Address. */
    val ip: String? = null,
    /** Address family. */
    val type: String? = null,
    /** Disabled. */
    val disabled: Boolean? = null,
    /** State. */
    val state: ResourceState? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
)

/** Port forward of a [Gateway]. */
@Serializable
public data class PortForward(
    /** ID. */
    val id: Long? = null,
    /** Name. */
    val name: String? = null,
    /** Destination IP in the tunnel. */
    val destination: String? = null,
    /** Target port. */
    @SerialName("target_port") val targetPort: Int? = null,
    /** Protocol. */
    val protocol: PortForwardProtocol? = null,
    /** Public port assigned by the server. */
    @SerialName("public_port") val publicPort: Int? = null,
    /** Public IP the forward is exposed on. */
    @SerialName("public_ip") val publicIp: PortForwardPublicIp? = null,
    /** Disabled. */
    val disabled: Boolean? = null,
    /** State. */
    val state: ResourceState? = null,
)

/** Static route of a Cloud Router. */
@Serializable
public data class Route(
    /** ID. */
    val id: Long? = null,
    /** Destination network. */
    val destination: String? = null,
    /** Next hop. */
    val gateway: String? = null,
    /** Distance. */
    val distance: Int? = null,
    /** Peer ID. */
    val peer: String? = null,
    /** Created by the system, cannot be deleted. */
    val system: Boolean? = null,
    /** Disabled. */
    val disabled: Boolean? = null,
    /** State. */
    val state: ResourceState? = null,
)

/** Firewall rule of a Cloud Router. */
@Serializable
public data class FirewallRule(
    /** ID. */
    val id: Long? = null,
    /** Chain. */
    val chain: FirewallChain? = null,
    /** Action. */
    val action: FirewallAction? = null,
    /** Address family. */
    val family: IpFamily? = null,
    /** Source. */
    val source: String? = null,
    /** Destination. */
    val destination: String? = null,
    /** Disabled. */
    val disabled: Boolean? = null,
    /** State. */
    val state: ResourceState? = null,
)

/**
 * Response of deleting a route, firewall rule, public IP or port forward (integer id).
 * The DNS64 client deletion has a string id and uses [Dns64ClientDeletion].
 */
@Serializable
public data class DeletionState(
    /** ID of the resource. */
    val id: Long? = null,
    /** State (normally `deleting`). */
    val state: String? = null,
)
