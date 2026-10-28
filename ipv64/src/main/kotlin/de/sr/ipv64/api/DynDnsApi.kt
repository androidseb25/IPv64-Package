package de.sr.ipv64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.data
import de.sr.ipv64.model.DynDnsResult
import de.sr.ipv64.model.DynDnsUpdateRequest

/** Tag *DynDNS*. */
public class DynDnsApi internal constructor(private val transport: HttpTransport) {

    /**
     * Updates the A and AAAA records of a domain or prefix. Without `ipv4` and `ipv6` the sender address is
     * used; only public addresses are allowed; missing records are created. `POST /domains/{domain}/dyndns`
     *
     * @param domain domain ID or full name.
     * @param prefix prefix, empty = the domain itself.
     */
    public suspend fun update(
        domain: String,
        ipv4: String? = null,
        ipv6: String? = null,
        prefix: String? = null,
    ): DynDnsResult = update(domain, DynDnsUpdateRequest(ipv4, ipv6, prefix))

    /** Updates the IPs from a request object. `POST /domains/{domain}/dyndns` */
    public suspend fun update(domain: String, request: DynDnsUpdateRequest): DynDnsResult =
        transport.data(Method.POST, ApiPath("domains", domain, "dyndns"), body = transport.body(request))
}
