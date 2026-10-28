package de.sr.ipv64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.data
import de.sr.ipv64.internal.list
import de.sr.ipv64.model.AccessListEntry
import de.sr.ipv64.model.AddAccessListEntryRequest
import de.sr.ipv64.model.AddCdnDomainRequest
import de.sr.ipv64.model.CdnDomain
import de.sr.ipv64.model.CdnDomainDetail
import de.sr.ipv64.model.CdnMapping
import de.sr.ipv64.model.CdnRecordMode
import de.sr.ipv64.model.CreateCdnMappingRequest
import de.sr.ipv64.model.EnableCdnRecordRequest
import de.sr.ipv64.model.Record
import de.sr.ipv64.model.UpdateCdnDomainRequest

/** Tag *CDN*: reverse proxy, port mapper and access list for domains running through the IPv64 CDN. */
public class CdnApi internal constructor(private val transport: HttpTransport) {

    /** Lists all CDN domains. `GET /cdn` */
    public suspend fun list(): List<CdnDomain> =
        transport.list(Method.GET, ApiPath("cdn"))

    /** Adds a domain to the CDN; a CDN server is assigned automatically afterwards. [domain] is the domain ID or name. `POST /cdn` */
    public suspend fun add(domain: String): CdnDomainDetail =
        transport.data(Method.POST, ApiPath("cdn"), body = transport.body(AddCdnDomainRequest(domain)))

    /** Returns a CDN domain including its mappings. `GET /cdn/{token}` */
    public suspend fun get(token: String): CdnDomainDetail =
        transport.data(Method.GET, ApiPath("cdn", token))

    /** Changes proxy settings; only sent fields change. HTTPS frontends trigger a Let's Encrypt certificate. `PATCH /cdn/{token}` */
    public suspend fun update(token: String, request: UpdateCdnDomainRequest): CdnDomainDetail =
        transport.data(Method.PATCH, ApiPath("cdn", token), body = transport.body(request))

    /** Removes a domain from the CDN (202 Accepted). Disable the CDN for all records first. `DELETE /cdn/{token}` */
    public suspend fun delete(token: String): CdnDomain =
        transport.data(Method.DELETE, ApiPath("cdn", token))

    /** Enables the reverse proxy (202 Accepted). At least one record must run through the CDN. `POST /cdn/{token}/proxy/enable` */
    public suspend fun enableProxy(token: String): CdnDomain =
        transport.data(Method.POST, ApiPath("cdn", token, "proxy", "enable"))

    /** Disables the reverse proxy (202 Accepted). `POST /cdn/{token}/proxy/disable` */
    public suspend fun disableProxy(token: String): CdnDomain =
        transport.data(Method.POST, ApiPath("cdn", token, "proxy", "disable"))

    /** Enables the port mapper (202 Accepted). At least one record must run through the CDN. `POST /cdn/{token}/portmapper/enable` */
    public suspend fun enablePortmapper(token: String): CdnDomain =
        transport.data(Method.POST, ApiPath("cdn", token, "portmapper", "enable"))

    /** Disables the port mapper (202 Accepted). `POST /cdn/{token}/portmapper/disable` */
    public suspend fun disablePortmapper(token: String): CdnDomain =
        transport.data(Method.POST, ApiPath("cdn", token, "portmapper", "disable"))

    /** Redeploys the configuration (202 Accepted). `POST /cdn/{token}/redeploy` */
    public suspend fun redeploy(token: String): CdnDomain =
        transport.data(Method.POST, ApiPath("cdn", token, "redeploy"))

    /** Resets the traffic counters of the domain. `POST /cdn/{token}/reset-traffic` */
    public suspend fun resetTraffic(token: String): CdnDomain =
        transport.data(Method.POST, ApiPath("cdn", token, "reset-traffic"))

    /** Moves the domain to another CDN server (202 Accepted). `POST /cdn/{token}/move` */
    public suspend fun move(token: String): CdnDomain =
        transport.data(Method.POST, ApiPath("cdn", token, "move"))

    /** Re-issues the certificate (202 Accepted). Only with active proxy and HTTPS frontend. `POST /cdn/{token}/renew-certificate` */
    public suspend fun renewCertificate(token: String): CdnDomain =
        transport.data(Method.POST, ApiPath("cdn", token, "renew-certificate"))

    /** Lists the A and AAAA records of the domain with their CDN state. `GET /cdn/{token}/records` */
    public suspend fun records(token: String): List<Record> =
        transport.list(Method.GET, ApiPath("cdn", token, "records"))

    /**
     * Routes a record through the CDN (202 Accepted).
     * `full`: the name points completely to the CDN. `other_family_only`: the record stays directly reachable and the CDN
     * serves only the other address family; only allowed if the record is the only one on its prefix.
     * @param record record ID or token
     * `POST /cdn/{token}/records/{record}/enable`
     */
    public suspend fun enableRecord(token: String, record: String, mode: CdnRecordMode? = null): Record =
        transport.data(
            Method.POST,
            ApiPath("cdn", token, "records", record, "enable"),
            body = transport.body(EnableCdnRecordRequest(mode)),
        )

    /** Delivers a record directly again (202 Accepted). `POST /cdn/{token}/records/{record}/disable` */
    public suspend fun disableRecord(token: String, record: String): Record =
        transport.data(Method.POST, ApiPath("cdn", token, "records", record, "disable"))

    /** Lists the port mappings. `GET /cdn/{token}/mappings` */
    public suspend fun mappings(token: String): List<CdnMapping> =
        transport.list(Method.GET, ApiPath("cdn", token, "mappings"))

    /**
     * Creates a port mapping forwarding a public CDN server port to a port at your server.
     * Without `listenPort` a free port between 50000 and 62000 is assigned.
     * `POST /cdn/{token}/mappings`
     */
    public suspend fun createMapping(token: String, request: CreateCdnMappingRequest): CdnMapping =
        transport.data(Method.POST, ApiPath("cdn", token, "mappings"), body = transport.body(request))

    /** Deletes a port mapping. `DELETE /cdn/{token}/mappings/{mapping}` */
    public suspend fun deleteMapping(token: String, mapping: String) {
        transport.unit(Method.DELETE, ApiPath("cdn", token, "mappings", mapping))
    }

    /** Lists the access list entries. `GET /cdn/{token}/access-list` */
    public suspend fun accessList(token: String): List<AccessListEntry> =
        transport.list(Method.GET, ApiPath("cdn", token, "access-list"))

    /** Adds an access list entry (IP or CIDR). Allow or deny is set by `proxy.access_list.mode`. `POST /cdn/{token}/access-list` */
    public suspend fun addAccessListEntry(token: String, address: String): AccessListEntry =
        transport.data(
            Method.POST,
            ApiPath("cdn", token, "access-list"),
            body = transport.body(AddAccessListEntryRequest(address)),
        )

    /** Removes an access list entry. `DELETE /cdn/{token}/access-list/{id}` */
    public suspend fun removeAccessListEntry(token: String, id: String) {
        transport.unit(Method.DELETE, ApiPath("cdn", token, "access-list", id))
    }
}
