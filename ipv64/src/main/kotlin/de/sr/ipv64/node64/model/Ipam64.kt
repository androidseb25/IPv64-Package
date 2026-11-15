package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IsoInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Status of a prefix. */
@Serializable
public enum class Ipam64PrefixStatus(override val value: String) : ApiValue {
    @SerialName("active") ACTIVE("active"),
    @SerialName("container") CONTAINER("container"),
    @SerialName("reserved") RESERVED("reserved"),
    @SerialName("deprecated") DEPRECATED("deprecated"),
}

/** Scan mode of a prefix. */
@Serializable
public enum class Ipam64ScanMode(override val value: String) : ApiValue {
    @SerialName("passive") PASSIVE("passive"),
    @SerialName("active") ACTIVE("active"),
    @SerialName("off") OFF("off"),
}

/** Status of a fixed address. */
@Serializable
public enum class Ipam64AddressStatus(override val value: String) : ApiValue {
    @SerialName("active") ACTIVE("active"),
    @SerialName("reserved") RESERVED("reserved"),
    @SerialName("deprecated") DEPRECATED("deprecated"),
}

/** Role of a fixed address; [NONE] is the empty string. */
@Serializable
public enum class Ipam64AddressRole(override val value: String) : ApiValue {
    @SerialName("") NONE(""),
    @SerialName("gateway") GATEWAY("gateway"),
    @SerialName("dns") DNS("dns"),
    @SerialName("vip") VIP("vip"),
    @SerialName("anycast") ANYCAST("anycast"),
}

/** Kind of an address range. */
@Serializable
public enum class Ipam64RangeKind(override val value: String) : ApiValue {
    @SerialName("fixed") FIXED("fixed"),
    @SerialName("reserved") RESERVED("reserved"),
}

/** Kind of an occupied address. */
@Serializable
public enum class Ipam64EntryKind(override val value: String) : ApiValue {
    @SerialName("static") STATIC("static"),
    @SerialName("fixed") FIXED("fixed"),
    @SerialName("dhcp") DHCP("dhcp"),
    @SerialName("conflict") CONFLICT("conflict"),
}

/** State of an occupied address. */
@Serializable
public enum class Ipam64EntryState(override val value: String) : ApiValue {
    @SerialName("online") ONLINE("online"),
    @SerialName("offline") OFFLINE("offline"),
    @SerialName("unknown") UNKNOWN("unknown"),
    @SerialName("conflict") CONFLICT("conflict"),
}

/** Kind of a conflict on an address (`dup`: two devices answer alternately, `mac`: foreign MAC, `lease`: lease against fixed address). */
@Serializable
public enum class Ipam64ConflictKind(override val value: String) : ApiValue {
    @SerialName("dup") DUP("dup"),
    @SerialName("mac") MAC("mac"),
    @SerialName("lease") LEASE("lease"),
}

/** Kind of a finding of the conflict view. */
@Serializable
public enum class Ipam64FindingKind(override val value: String) : ApiValue {
    @SerialName("dup") DUP("dup"),
    @SerialName("mac") MAC("mac"),
    @SerialName("lease") LEASE("lease"),
    @SerialName("double") DOUBLE("double"),
    @SerialName("pool") POOL("pool"),
    @SerialName("stale") STALE("stale"),
    @SerialName("foreign") FOREIGN("foreign"),
}

/** Severity of a finding. */
@Serializable
public enum class Ipam64Severity(override val value: String) : ApiValue {
    @SerialName("conflict") CONFLICT("conflict"),
    @SerialName("hint") HINT("hint"),
}

/** Prefix with occupancy (from IPAM64 and/or a DHCP network from DNS64; without [uid]: DNS64 only). */
@Serializable
public data class Ipam64Net(
    /** IPv4 from /8 or IPv6 from /16, e.g. `192.168.1.0/24`. */
    val prefix: String? = null,
    /** Name (max 64). */
    val name: String? = null,
    /** Status. */
    val status: Ipam64PrefixStatus? = null,
    /** Role (max 32). */
    val role: String? = null,
    /** VLAN (0-4094). */
    val vlan: Int? = null,
    /** Tags (max 10). */
    val tags: List<String> = emptyList(),
    /** Description (max 200). */
    val description: String? = null,
    /** Counts as fully occupied. */
    @SerialName("mark_full") val markFull: Boolean? = null,
    /** DNS zone. */
    @SerialName("dns_zone") val dnsZone: String? = null,
    /** Scan mode. */
    val scan: Ipam64ScanMode? = null,
    /** UID; missing for prefixes known to DNS64 only. */
    val uid: String? = null,
    /** Address family (4 or 6). */
    val family: Int? = null,
    /** DHCP network. */
    val dhcp: Boolean? = null,
    /** Interface. */
    @SerialName("interface") val networkInterface: String? = null,
    /** Router. */
    val router: String? = null,
    /** Start of the DHCP pool. */
    @SerialName("pool_start") val poolStart: String? = null,
    /** End of the DHCP pool. */
    @SerialName("pool_end") val poolEnd: String? = null,
    /** Domain. */
    val domain: String? = null,
    /** Parent prefix. */
    val parent: String? = null,
    /** Depth in the tree. */
    val depth: Int? = null,
    /** Number of children. */
    val children: Int? = null,
    /** Attached to the node. */
    val attached: Boolean? = null,
    /** Usable addresses (IPv4). */
    val size: Long? = null,
    /** Occupied addresses. */
    val used: Long? = null,
    /** Free addresses. */
    val free: Long? = null,
    /** Free addresses in the pool. */
    @SerialName("pool_free") val poolFree: Long? = null,
    /** Number of conflicts. */
    val conflicts: Int? = null,
    /** Percent occupied. */
    @SerialName("used_pct") val usedPct: Double? = null,
    /** Dynamic prefix (provider IPv6 that changes). */
    val dynamic: Boolean? = null,
    /** Prefix ID below a dynamic prefix. */
    @SerialName("dyn_index") val dynIndex: String? = null,
)

/** Response of `GET /ipam64/prefixes`. */
@Serializable
public data class Ipam64PrefixList(
    /** Prefix tree (IPv4 before IPv6, parents before children). */
    val prefixes: List<Ipam64Net> = emptyList(),
)

/** Body of `POST /ipam64/prefixes` and `PUT /ipam64/prefixes/{uid}`. */
@Serializable
public data class Ipam64PrefixInput(
    /** IPv4 from /8 or IPv6 from /16, e.g. `192.168.1.0/24`. */
    val prefix: String,
    /** Name (max 64). */
    val name: String? = null,
    /** Status. */
    val status: Ipam64PrefixStatus? = null,
    /** Role (max 32). */
    val role: String? = null,
    /** VLAN (0-4094). */
    val vlan: Int? = null,
    /** Tags (max 10). */
    val tags: List<String>? = null,
    /** Description (max 200). */
    val description: String? = null,
    /** Counts as fully occupied. */
    @SerialName("mark_full") val markFull: Boolean? = null,
    /** DNS zone. */
    @SerialName("dns_zone") val dnsZone: String? = null,
    /** Scan mode. */
    val scan: Ipam64ScanMode? = null,
    /** Provider IPv6 that changes (global IPv6 prefixes only); when changing: missing = previous value. */
    val dynamic: Boolean? = null,
)

/** A prefix of IPAM64. */
@Serializable
public data class Ipam64Prefix(
    /** UID. */
    val uid: String? = null,
    /** Address family (4 or 6). */
    val family: Int? = null,
    /** Scanning node. */
    @SerialName("scan_node") val scanNode: String? = null,
    /** Last change. */
    val updated: IsoInstant? = null,
    /** Changed by. */
    @SerialName("updated_by") val updatedBy: String? = null,
    /** Prefix. */
    val prefix: String? = null,
    /** Name. */
    val name: String? = null,
    /** Status. */
    val status: Ipam64PrefixStatus? = null,
    /** Role. */
    val role: String? = null,
    /** VLAN. */
    val vlan: Int? = null,
    /** Tags. */
    val tags: List<String> = emptyList(),
    /** Description. */
    val description: String? = null,
    /** Counts as fully occupied. */
    @SerialName("mark_full") val markFull: Boolean? = null,
    /** DNS zone. */
    @SerialName("dns_zone") val dnsZone: String? = null,
    /** Scan mode. */
    val scan: Ipam64ScanMode? = null,
)

/** An occupied address, merged from all sources. */
@Serializable
public data class Ipam64Entry(
    /** Prefix. */
    val prefix: String? = null,
    /** IP address. */
    val ip: String? = null,
    /** Name. */
    val name: String? = null,
    /** Note. */
    val note: String? = null,
    /** MAC. */
    val mac: String? = null,
    /** All known MACs. */
    val macs: List<String> = emptyList(),
    /** Kind. */
    val kind: Ipam64EntryKind? = null,
    /** Sources: `ipam`, `node`, `dhcp`, `dns`, `healthchecks`, `monitor64`, `proxy`, `scan` (strings, so new sources never break decoding). */
    val sources: List<String> = emptyList(),
    /** Last seen. */
    @SerialName("last_seen") val lastSeen: IsoInstant? = null,
    /** State. */
    val state: Ipam64EntryState? = null,
    /** UID of the fixed address. */
    val uid: String? = null,
    /** Status. */
    val status: String? = null,
    /** Role. */
    val role: String? = null,
    /** Description. */
    val description: String? = null,
    /** Tags. */
    val tags: List<String> = emptyList(),
    /** First seen. */
    @SerialName("first_seen") val firstSeen: IsoInstant? = null,
    /** Kind of the conflict, if any. */
    val conflict: Ipam64ConflictKind? = null,
    /** Vendor from the MAC; `random` = random MAC. */
    val vendor: String? = null,
)

/** Page of occupied addresses. */
@Serializable
public data class Ipam64AddressPage(
    /** Addresses of this page. */
    val addresses: List<Ipam64Entry> = emptyList(),
    /** Total number. */
    val total: Int? = null,
    /** Offset of this page. */
    val offset: Int? = null,
    /** Page size. */
    val limit: Int? = null,
)

/** Body of `POST /ipam64/addresses` and `PUT /ipam64/addresses/{uid}`. */
@Serializable
public data class Ipam64AddressInput(
    /** IP address. */
    val address: String,
    /** Status. */
    val status: Ipam64AddressStatus? = null,
    /** Role. */
    val role: Ipam64AddressRole? = null,
    /** Host name (one DNS label). */
    val name: String? = null,
    /** MAC. */
    val mac: String? = null,
    /** Description (max 200). */
    val description: String? = null,
    /** Tags (max 10). */
    val tags: List<String>? = null,
    /** Local A/AAAA record in DNS64; when changing: missing = previous value. */
    val dns: Boolean? = null,
    /** Zone; empty = zone of the prefix. */
    @SerialName("dns_zone") val dnsZone: String? = null,
    /** Reverse resolution from the entry. */
    val ptr: Boolean? = null,
    /** Reservation in the DHCP server of DNS64 (IPv4 only, needs a MAC). */
    val dhcp: Boolean? = null,
)

/** A fixed address. */
@Serializable
public data class Ipam64Address(
    /** UID. */
    val uid: String? = null,
    /** Handed over to DNS64: IPAM64 writes nothing for the address to DNS64 until dns or dhcp is switched on again. */
    val released: Boolean? = null,
    /** Local A/AAAA record. */
    val dns: Boolean? = null,
    /** DNS zone. */
    @SerialName("dns_zone") val dnsZone: String? = null,
    /** Reverse resolution. */
    val ptr: Boolean? = null,
    /** DHCP reservation. */
    val dhcp: Boolean? = null,
    /** Last change. */
    val updated: IsoInstant? = null,
    /** Changed by. */
    @SerialName("updated_by") val updatedBy: String? = null,
    /** IP address. */
    val address: String? = null,
    /** Status. */
    val status: Ipam64AddressStatus? = null,
    /** Role. */
    val role: Ipam64AddressRole? = null,
    /** Host name. */
    val name: String? = null,
    /** MAC. */
    val mac: String? = null,
    /** Description. */
    val description: String? = null,
    /** Tags. */
    val tags: List<String> = emptyList(),
)

/** Response of `GET /ipam64/available`. */
@Serializable
public data class Ipam64Available(
    /** Prefix. */
    val prefix: String? = null,
    /** Free addresses. */
    val available: List<String> = emptyList(),
)

/** Body of `POST /ipam64/available`: finds and registers the next free address in one step. */
@Serializable
public data class Ipam64AllocateInput(
    /** Prefix. */
    val prefix: String,
    /** UID of an address range (optional). */
    val range: String? = null,
    /** Status. */
    val status: Ipam64AddressStatus? = null,
    /** Role. */
    val role: Ipam64AddressRole? = null,
    /** Host name (one DNS label). */
    val name: String? = null,
    /** MAC. */
    val mac: String? = null,
    /** Description (max 200). */
    val description: String? = null,
    /** Tags (max 10). */
    val tags: List<String>? = null,
    /** Local A/AAAA record in DNS64. */
    val dns: Boolean? = null,
    /** Zone; empty = zone of the prefix. */
    @SerialName("dns_zone") val dnsZone: String? = null,
    /** Reverse resolution. */
    val ptr: Boolean? = null,
    /** Reservation in the DHCP server of DNS64 (IPv4 only, needs a MAC). */
    val dhcp: Boolean? = null,
)

/** An address range. */
@Serializable
public data class Ipam64Range(
    /** UID. */
    val uid: String? = null,
    /** Last change. */
    val updated: IsoInstant? = null,
    /** Changed by. */
    @SerialName("updated_by") val updatedBy: String? = null,
    /** First address. */
    val start: String? = null,
    /** Last address. */
    val end: String? = null,
    /** Kind. */
    val kind: Ipam64RangeKind? = null,
    /** Name (max 64). */
    val name: String? = null,
    /** Description (max 200). */
    val description: String? = null,
)

/** Response of `GET /ipam64/ranges`. */
@Serializable
public data class Ipam64RangeList(
    /** Ranges. */
    val ranges: List<Ipam64Range> = emptyList(),
)

/** Body of `POST /ipam64/ranges` and `PUT /ipam64/ranges/{uid}`. */
@Serializable
public data class Ipam64RangeInput(
    /** First address. */
    val start: String,
    /** Last address. */
    val end: String,
    /** Kind. */
    val kind: Ipam64RangeKind? = null,
    /** Name (max 64). */
    val name: String? = null,
    /** Description (max 200). */
    val description: String? = null,
)

/** State of the network scan. */
@Serializable
public data class Ipam64Scan(
    /** A scan is running. */
    val running: Boolean? = null,
    /** Scanned prefix. */
    val prefix: String? = null,
    /** Start. */
    val started: IsoInstant? = null,
    /** End. */
    val finished: IsoInstant? = null,
    /** Devices found. */
    val found: Int? = null,
    /** Error. */
    val error: String? = null,
)

/** Body of `POST /ipam64/scan`. */
@Serializable
public data class Ipam64ScanInput(
    /** Private IPv4 prefix up to /22 known to IPAM64. */
    val prefix: String,
)

/** A conflict or hint. */
@Serializable
public data class Ipam64Finding(
    /** Kind. */
    val kind: Ipam64FindingKind? = null,
    /** Severity. */
    val severity: Ipam64Severity? = null,
    /** IP address. */
    val ip: String? = null,
    /** Prefix. */
    val prefix: String? = null,
    /** Name. */
    val name: String? = null,
    /** MACs involved. */
    val macs: List<String> = emptyList(),
)

/** A device only the neighbour table knows. */
@Serializable
public data class Ipam64UnknownDevice(
    /** IP address. */
    val ip: String? = null,
    /** Prefix. */
    val prefix: String? = null,
    /** MAC. */
    val mac: String? = null,
    /** Vendor. */
    val vendor: String? = null,
    /** First seen. */
    @SerialName("first_seen") val firstSeen: IsoInstant? = null,
    /** Last seen. */
    @SerialName("last_seen") val lastSeen: IsoInstant? = null,
)

/** Response of `GET /ipam64/conflicts`. */
@Serializable
public data class Ipam64Conflicts(
    /** Conflicts and hints. */
    val findings: List<Ipam64Finding> = emptyList(),
    /** Unknown devices. */
    val unknown: List<Ipam64UnknownDevice> = emptyList(),
)
