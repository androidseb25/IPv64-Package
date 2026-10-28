package de.sr.ipv64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.data
import de.sr.ipv64.internal.list
import de.sr.ipv64.internal.query
import de.sr.ipv64.model.CreateDomainRequest
import de.sr.ipv64.model.Domain
import de.sr.ipv64.model.DomainDetail
import de.sr.ipv64.model.DomainKind
import de.sr.ipv64.model.IgnoreFamily
import de.sr.ipv64.model.UpdateDomainRequest

/** Tag *Domains*. In all methods `domain` is the domain ID or the full name, e.g. `example.ipv64.de`. */
public class DomainsApi internal constructor(private val transport: HttpTransport) {

    /** All domains, optionally only DynDNS or only own ones. `GET /domains` */
    public suspend fun list(kind: DomainKind? = null): List<Domain> =
        transport.list(Method.GET, ApiPath("domains"), query("kind" to kind))

    /**
     * Creates a domain. DynDNS: `name` as `<name>.<zone>`; own domain: set `own` to true and point the
     * name servers ns1.ipv64.net and ns2.ipv64.net to IPv64. Plan limits apply. `POST /domains`
     */
    public suspend fun create(name: String, own: Boolean? = null): DomainDetail =
        create(CreateDomainRequest(name = name, own = own))

    /** Creates a domain from a request object. `POST /domains` */
    public suspend fun create(request: CreateDomainRequest): DomainDetail =
        transport.data(Method.POST, ApiPath("domains"), body = transport.body(request))

    /** A domain with its records. `GET /domains/{domain}` */
    public suspend fun get(domain: String): DomainDetail =
        transport.data(Method.GET, ApiPath("domains", domain))

    /** Changes domain settings; only non-null fields are sent. `PATCH /domains/{domain}` */
    public suspend fun update(
        domain: String,
        wildcard: Boolean? = null,
        ignore: IgnoreFamily? = null,
        ipv6Prefix: String? = null,
    ): DomainDetail = update(domain, UpdateDomainRequest(wildcard, ignore, ipv6Prefix))

    /** Changes domain settings from a request object. `PATCH /domains/{domain}` */
    public suspend fun update(domain: String, request: UpdateDomainRequest): DomainDetail =
        transport.data(Method.PATCH, ApiPath("domains", domain), body = transport.body(request))

    /** Deletes the domain with all records. Not possible while DNS failover is active. `DELETE /domains/{domain}` */
    public suspend fun delete(domain: String) {
        transport.unit(Method.DELETE, ApiPath("domains", domain))
    }

    /** Creates a new domain update token; the old token for update.php is invalid immediately. `POST /domains/{domain}/rotate-token` */
    public suspend fun rotateToken(domain: String): Domain =
        transport.data(Method.POST, ApiPath("domains", domain, "rotate-token"))

    /** Resets the domain: deletes all active records and sets up the zone anew. `POST /domains/{domain}/reset` */
    public suspend fun reset(domain: String): DomainDetail =
        transport.data(Method.POST, ApiPath("domains", domain, "reset"))
}
