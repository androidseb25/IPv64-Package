package de.sr.ipv64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.data
import de.sr.ipv64.internal.list
import de.sr.ipv64.internal.query
import de.sr.ipv64.model.CreateRecordRequest
import de.sr.ipv64.model.Record
import de.sr.ipv64.model.RecordType
import de.sr.ipv64.model.UpdateRecordRequest

/** Tag *DNS-Records*. `domain` is a domain ID or full name; `record` is a record ID or record token. */
public class RecordsApi internal constructor(private val transport: HttpTransport) {

    /** Records of a domain, optionally filtered by type and prefix. `GET /domains/{domain}/records` */
    public suspend fun list(domain: String, type: RecordType? = null, prefix: String? = null): List<Record> =
        transport.list(Method.GET, ApiPath("domains", domain, "records"), query("type" to type, "prefix" to prefix))

    /**
     * Creates a record. TTL is fixed at 60 seconds. Content formats as in the web UI, e.g. MX `10 mail.example.com`.
     * `POST /domains/{domain}/records`
     *
     * @param prefix prefix, empty or `@` = the domain itself.
     */
    public suspend fun create(domain: String, type: RecordType, content: String, prefix: String? = null): Record =
        create(domain, CreateRecordRequest(type = type, content = content, prefix = prefix))

    /** Creates a record from a request object. `POST /domains/{domain}/records` */
    public suspend fun create(domain: String, request: CreateRecordRequest): Record =
        transport.data(Method.POST, ApiPath("domains", domain, "records"), body = transport.body(request))

    /** Deletes the record with exactly this type, prefix and content (handy for ACME clients). `DELETE /domains/{domain}/records` */
    public suspend fun deleteByContent(domain: String, type: RecordType, content: String, prefix: String? = null) {
        transport.unit(
            Method.DELETE,
            ApiPath("domains", domain, "records"),
            query("type" to type, "content" to content, "prefix" to prefix),
        )
    }

    /** A single record. `GET /records/{record}` */
    public suspend fun get(record: String): Record =
        transport.data(Method.GET, ApiPath("records", record))

    /**
     * Changes a record. If only the IP of an A/AAAA record changes, ID and token are kept; otherwise the record is
     * replaced with a new ID and token. Records managed by CDN or DNS failover are locked. `PATCH /records/{record}`
     */
    public suspend fun update(
        record: String,
        type: RecordType? = null,
        prefix: String? = null,
        content: String? = null,
    ): Record = update(record, UpdateRecordRequest(type, prefix, content))

    /** Changes a record from a request object. `PATCH /records/{record}` */
    public suspend fun update(record: String, request: UpdateRecordRequest): Record =
        transport.data(Method.PATCH, ApiPath("records", record), body = transport.body(request))

    /** Deletes a record. `DELETE /records/{record}` */
    public suspend fun delete(record: String) {
        transport.unit(Method.DELETE, ApiPath("records", record))
    }
}
