package de.sr.ipv64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.data
import de.sr.ipv64.internal.list
import de.sr.ipv64.model.AddDns64RuleRequest
import de.sr.ipv64.model.AddDns64SourceRequest
import de.sr.ipv64.model.CreateDns64ClientRequest
import de.sr.ipv64.model.Dns64Client
import de.sr.ipv64.model.Dns64ClientDeletion
import de.sr.ipv64.model.Dns64RuleAction
import de.sr.ipv64.model.UpdateDns64ClientRequest

/** Tag *DNS64*: DNS64 resolver clients with filter rules and source addresses. */
public class Dns64Api internal constructor(private val transport: HttpTransport) {

    /** All DNS64 clients. `GET /dns64/clients` */
    public suspend fun listClients(): List<Dns64Client> =
        transport.list(Method.GET, ApiPath("dns64", "clients"))

    /** Creates a client; resolver addresses and `clientId` are available once the state is `ready`. `POST /dns64/clients` */
    public suspend fun createClient(name: String? = null): Dns64Client =
        transport.data(
            Method.POST,
            ApiPath("dns64", "clients"),
            body = transport.body(CreateDns64ClientRequest(name)),
        )

    /** A client with rules, sources and DNS stamps. `GET /dns64/clients/{token}` */
    public suspend fun getClient(token: String): Dns64Client =
        transport.data(Method.GET, ApiPath("dns64", "clients", token))

    /** Changes a client. The blocklists `adult` and `threats` are not available in the free plan. `PATCH /dns64/clients/{token}` */
    public suspend fun updateClient(token: String, request: UpdateDns64ClientRequest): Dns64Client =
        transport.data(Method.PATCH, ApiPath("dns64", "clients", token), body = transport.body(request))

    /** Deletes a client. `DELETE /dns64/clients/{token}` */
    public suspend fun deleteClient(token: String): Dns64ClientDeletion =
        transport.data(Method.DELETE, ApiPath("dns64", "clients", token))

    /** Allows or blocks a domain (action defaults to the server's default if omitted). `POST /dns64/clients/{token}/rules` */
    public suspend fun addRule(token: String, domain: String, action: Dns64RuleAction? = null): Dns64Client =
        transport.data(
            Method.POST,
            ApiPath("dns64", "clients", token, "rules"),
            body = transport.body(AddDns64RuleRequest(domain, action)),
        )

    /** Removes a rule. `DELETE /dns64/clients/{token}/rules/{id}` */
    public suspend fun deleteRule(token: String, id: Long) {
        transport.unit(Method.DELETE, ApiPath("dns64", "clients", token, "rules", id.toString()))
    }

    /**
     * Adds a source address so the shared resolver IPs can attribute queries to the client:
     * either a fixed [ip] or a DynDNS [record] (ID or token). At most 5 per kind.
     * `POST /dns64/clients/{token}/sources`
     */
    public suspend fun addSource(token: String, ip: String? = null, record: String? = null): Dns64Client =
        transport.data(
            Method.POST,
            ApiPath("dns64", "clients", token, "sources"),
            body = transport.body(AddDns64SourceRequest(ip, record)),
        )

    /**
     * Removes a source address; [id] is the numeric ID for fixed IPs or the record token for records.
     * `DELETE /dns64/clients/{token}/sources/{id}`
     */
    public suspend fun deleteSource(token: String, id: String) {
        transport.unit(Method.DELETE, ApiPath("dns64", "clients", token, "sources", id))
    }
}
