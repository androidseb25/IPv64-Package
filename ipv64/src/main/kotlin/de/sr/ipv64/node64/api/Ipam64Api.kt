package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.internal.query
import de.sr.ipv64.node64.model.Ipam64Address
import de.sr.ipv64.node64.model.Ipam64AddressInput
import de.sr.ipv64.node64.model.Ipam64AddressPage
import de.sr.ipv64.node64.model.Ipam64AllocateInput
import de.sr.ipv64.node64.model.Ipam64Available
import de.sr.ipv64.node64.model.Ipam64Conflicts
import de.sr.ipv64.node64.model.Ipam64Prefix
import de.sr.ipv64.node64.model.Ipam64PrefixInput
import de.sr.ipv64.node64.model.Ipam64PrefixList
import de.sr.ipv64.node64.model.Ipam64Range
import de.sr.ipv64.node64.model.Ipam64RangeInput
import de.sr.ipv64.node64.model.Ipam64RangeList
import de.sr.ipv64.node64.model.Ipam64Scan
import de.sr.ipv64.node64.model.Ipam64ScanInput

/** Tag *IPAM64*: IP address management (`/ipam64/…`) with prefixes, fixed addresses, ranges, free addresses, scan and conflicts. */
public class Ipam64Api internal constructor(private val transport: HttpTransport) {

    /** All prefixes as a tree with occupancy; DHCP networks from DNS64 appear without an IPAM64 entry too. `GET /ipam64/prefixes` */
    public suspend fun prefixes(): Ipam64PrefixList =
        transport.json(Method.GET, ApiPath("ipam64", "prefixes"))

    /** Creates a prefix. `POST /ipam64/prefixes` */
    public suspend fun createPrefix(input: Ipam64PrefixInput): Ipam64Prefix =
        transport.json(Method.POST, ApiPath("ipam64", "prefixes"), body = transport.body(input))

    /** Changes a prefix. `PUT /ipam64/prefixes/{uid}` */
    public suspend fun updatePrefix(uid: String, input: Ipam64PrefixInput): Ipam64Prefix =
        transport.json(Method.PUT, ApiPath("ipam64", "prefixes", uid), body = transport.body(input))

    /** Deletes a prefix; addresses and ranges in it remain. `DELETE /ipam64/prefixes/{uid}` */
    public suspend fun deletePrefix(uid: String) {
        transport.unit(Method.DELETE, ApiPath("ipam64", "prefixes", uid))
    }

    /**
     * Occupied addresses, merged from all sources, page by page.
     *
     * @param prefix restricts to a prefix.
     * @param q search text.
     * @param limit page size (default 1000, max 5000).
     * @param offset offset (default 0).
     * `GET /ipam64/addresses`
     */
    public suspend fun addresses(
        prefix: String? = null,
        q: String? = null,
        limit: Int? = null,
        offset: Int? = null,
    ): Ipam64AddressPage =
        transport.json(
            Method.GET, ApiPath("ipam64", "addresses"),
            query("prefix" to prefix, "q" to q, "limit" to limit, "offset" to offset),
        )

    /** Registers a fixed address (and, depending on dns/dhcp, writes record, reservation or exception to DNS64; 409 `dns64: <code>` if DNS64 refuses). `POST /ipam64/addresses` */
    public suspend fun createAddress(input: Ipam64AddressInput): Ipam64Address =
        transport.json(Method.POST, ApiPath("ipam64", "addresses"), body = transport.body(input))

    /** Changes a fixed address. `PUT /ipam64/addresses/{uid}` */
    public suspend fun updateAddress(uid: String, input: Ipam64AddressInput): Ipam64Address =
        transport.json(Method.PUT, ApiPath("ipam64", "addresses", uid), body = transport.body(input))

    /** Removes a fixed address. `DELETE /ipam64/addresses/{uid}` */
    public suspend fun deleteAddress(uid: String) {
        transport.unit(Method.DELETE, ApiPath("ipam64", "addresses", uid))
    }

    /**
     * Free addresses of a prefix.
     *
     * @param prefix the prefix.
     * @param range UID of an address range.
     * @param count number of addresses (default 10, max 256).
     * `GET /ipam64/available`
     */
    public suspend fun available(prefix: String, range: String? = null, count: Int? = null): Ipam64Available =
        transport.json(
            Method.GET, ApiPath("ipam64", "available"),
            query("prefix" to prefix, "range" to range, "count" to count),
        )

    /** Finds and registers the next free address in one step; in a cluster allocate on one node only. `POST /ipam64/available` */
    public suspend fun allocate(input: Ipam64AllocateInput): Ipam64Address =
        transport.json(Method.POST, ApiPath("ipam64", "available"), body = transport.body(input))

    /** Conflicts, hints and devices only the neighbour table knows. `GET /ipam64/conflicts` */
    public suspend fun conflicts(): Ipam64Conflicts =
        transport.json(Method.GET, ApiPath("ipam64", "conflicts"))

    /** All address ranges. `GET /ipam64/ranges` */
    public suspend fun ranges(): Ipam64RangeList =
        transport.json(Method.GET, ApiPath("ipam64", "ranges"))

    /** Creates an address range; it must lie completely within one prefix and not overlap others. `POST /ipam64/ranges` */
    public suspend fun createRange(input: Ipam64RangeInput): Ipam64Range =
        transport.json(Method.POST, ApiPath("ipam64", "ranges"), body = transport.body(input))

    /** Changes an address range. `PUT /ipam64/ranges/{uid}` */
    public suspend fun updateRange(uid: String, input: Ipam64RangeInput): Ipam64Range =
        transport.json(Method.PUT, ApiPath("ipam64", "ranges", uid), body = transport.body(input))

    /** Deletes an address range. `DELETE /ipam64/ranges/{uid}` */
    public suspend fun deleteRange(uid: String) {
        transport.unit(Method.DELETE, ApiPath("ipam64", "ranges", uid))
    }

    /** State of the network scan. `GET /ipam64/scan` */
    public suspend fun scan(): Ipam64Scan =
        transport.json(Method.GET, ApiPath("ipam64", "scan"))

    /** Starts a network scan in the background (private IPv4 up to /22; one scan at a time, else 409). `POST /ipam64/scan` */
    public suspend fun startScan(input: Ipam64ScanInput): Ipam64Scan =
        transport.json(Method.POST, ApiPath("ipam64", "scan"), body = transport.body(input))
}
