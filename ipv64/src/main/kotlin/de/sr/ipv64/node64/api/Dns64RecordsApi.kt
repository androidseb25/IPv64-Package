package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.internal.query
import de.sr.ipv64.node64.model.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

/** Custom DNS records of the DNS64 resolver. */
public class Dns64RecordsApi internal constructor(private val transport: HttpTransport) {

    /** Lists custom DNS records. `GET /dns64/records` */
    public suspend fun list(): List<DNS64Record> =
        transport.json(Method.GET, ApiPath("dns64", "records"))

    /**
     * Creates a custom record. Applies immediately; `*.name` = all subnames; a CNAME is alone per name;
     * A/AAAA also get a PTR.
     *
     * `POST /dns64/records`
     */
    public suspend fun create(request: DNS64RecordRequest): DNS64Record =
        transport.json(Method.POST, ApiPath("dns64", "records"), body = transport.body(request))

    /** Changes a custom record (enabled state stays). `PUT /dns64/records/{id}` */
    public suspend fun update(id: Long, request: DNS64RecordRequest): DNS64Record =
        transport.json(Method.PUT, ApiPath("dns64", "records", id.toString()), body = transport.body(request))

    /** Deletes a custom record. `DELETE /dns64/records/{id}` */
    public suspend fun delete(id: Long) {
        transport.unit(Method.DELETE, ApiPath("dns64", "records", id.toString()))
    }

    /** Enables a custom record. `POST /dns64/records/{id}/enable` */
    public suspend fun enable(id: Long) {
        transport.unit(Method.POST, ApiPath("dns64", "records", id.toString(), "enable"))
    }

    /** Disables a custom record. `POST /dns64/records/{id}/disable` */
    public suspend fun disable(id: Long) {
        transport.unit(Method.POST, ApiPath("dns64", "records", id.toString(), "disable"))
    }

    /**
     * Imports custom records from a hosts file, CSV, JSON or BIND zone file (existing records stay).
     *
     * @param content the file content as text.
     * @param zone zone for relative names of a zone file without `$ORIGIN`.
     *
     * `POST /dns64/records/import`
     */
    public suspend fun import(content: String, zone: String? = null): DNS64RecordImportResult =
        transport.json(
            Method.POST, ApiPath("dns64", "records", "import"), query("zone" to zone),
            body = content.toRequestBody("text/plain; charset=utf-8".toMediaType()),
        )

    /** Exports the custom records as file content (hosts: only A/AAAA). `GET /dns64/records/export` */
    public suspend fun export(format: DNS64ExportFormat? = null): ByteArray =
        transport.bytes(Method.GET, ApiPath("dns64", "records", "export"), query("format" to format), headers = mapOf("Accept" to "*/*"))
}
