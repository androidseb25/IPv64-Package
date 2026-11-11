package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IsoInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Operating mode of the DHCP server of a node. */
@Serializable
public enum class DNS64DhcpMode(override val value: String) : ApiValue {
    @SerialName("off") OFF("off"),
    @SerialName("observe") OBSERVE("observe"),
    @SerialName("on") ON("on"),
}

/** Role of a node in the cluster for DHCP. */
@Serializable
public enum class DNS64DhcpRole(override val value: String) : ApiValue {
    @SerialName("primary") PRIMARY("primary"),
    @SerialName("reserve") RESERVE("reserve"),
}

/** Kind of an error while sending router advertisements. */
@Serializable
public enum class DNS64DhcpIpv6ErrorKind(override val value: String) : ApiValue {
    @SerialName("no_iface") NO_IFACE("no_iface"),
    @SerialName("no_addr") NO_ADDR("no_addr"),
    @SerialName("send") SEND("send"),
}

/** DHCP network (one per interface); also the request body to create or change one. New networks are enabled. */
@Serializable
public data class DNS64DHCPScope(
    val id: Long? = null,
    val name: String? = null,
    /** Host interface, also VLAN (`eth0.20`). */
    @SerialName("interface") val interfaceName: String? = null,
    /** For example `192.168.1.0/24`. */
    val subnet: String? = null,
    @SerialName("range_start") val rangeStart: String? = null,
    @SerialName("range_end") val rangeEnd: String? = null,
    /** Empty = none. */
    val router: String? = null,
    /** Empty = this node (recommended), at most 4. */
    val dns: List<String> = emptyList(),
    val domain: String? = null,
    /** 1-8760, 0 = 24. */
    @SerialName("lease_hours") val leaseHours: Int? = null,
    val ntp: List<String> = emptyList(),
    /** Unfitting requests get a NAK (switching over from a router). */
    val authoritative: Boolean? = null,
    /** PXE: TFTP server (option 66). */
    @SerialName("boot_server") val bootServer: String? = null,
    @SerialName("boot_bios") val bootBios: String? = null,
    @SerialName("boot_uefi") val bootUefi: String? = null,
    /** Boot file if the client already is iPXE. */
    @SerialName("boot_ipxe") val bootIpxe: String? = null,
    /** Network behind a DHCP relay. */
    val relay: Boolean? = null,
    /** Read only. */
    val enabled: Boolean? = null,
    /** Read only. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
)

/** Fixed address (MAC and IP unique per network); also the request body to create or change one. */
@Serializable
public data class DNS64DHCPReservation(
    val id: Long? = null,
    @SerialName("scope_id") val scopeId: Long,
    /** For example `aa:bb:cc:dd:ee:ff`. */
    val mac: String,
    val ip: String,
    val name: String? = null,
    val comment: String? = null,
    /** Only when creating: also add the MAC to this device group. */
    @SerialName("group_id") val groupId: Long? = null,
)

/** Settings of IPv6 DNS via router advertisement. */
@Serializable
public data class DNS64DhcpIpv6Config(
    val enabled: Boolean? = null,
    val interfaces: List<String> = emptyList(),
    /** Search domain (DNSSL), empty = none. */
    val domain: String? = null,
)

/** Sending state of one interface. */
@Serializable
public data class DNS64DhcpIpv6Status(
    @SerialName("interface") val interfaceName: String? = null,
    val dns: List<String> = emptyList(),
    @SerialName("last_sent") val lastSent: IsoInstant? = null,
    val error: String? = null,
    @SerialName("error_kind") val errorKind: DNS64DhcpIpv6ErrorKind? = null,
)

/** Prefix announced by a router. */
@Serializable
public data class DNS64DhcpIpv6Prefix(
    val prefix: String? = null,
    val slaac: Boolean? = null,
    @SerialName("on_link") val onLink: Boolean? = null,
    @SerialName("valid_lifetime") val validLifetime: Long? = null,
)

/** Router seen in the network. */
@Serializable
public data class DNS64DhcpIpv6Router(
    @SerialName("interface") val interfaceName: String? = null,
    /** For example `fe80::1`. */
    val source: String? = null,
    /** Seconds, 0 = no default router. */
    @SerialName("router_lifetime") val routerLifetime: Long? = null,
    /** M flag: addresses via DHCPv6. */
    val managed: Boolean? = null,
    /** O flag: further settings via DHCPv6. */
    val other: Boolean? = null,
    val prefixes: List<DNS64DhcpIpv6Prefix> = emptyList(),
    val dns: List<String> = emptyList(),
    val domains: List<String> = emptyList(),
    @SerialName("last_seen") val lastSeen: IsoInstant? = null,
)

/** Settings and state of IPv6 DNS via router advertisement. */
@Serializable
public data class DNS64DhcpIpv6(
    val config: DNS64DhcpIpv6Config? = null,
    val running: Boolean? = null,
    /** Socket error (for example without `cap_net_raw`). */
    val error: String? = null,
    val status: List<DNS64DhcpIpv6Status> = emptyList(),
    val routers: List<DNS64DhcpIpv6Router> = emptyList(),
)

/** Result of a router probe (router solicitation, 3 s). */
@Serializable
public data class DNS64DhcpIpv6Probe(
    val routers: List<DNS64DhcpIpv6Router> = emptyList(),
    val error: String? = null,
)
