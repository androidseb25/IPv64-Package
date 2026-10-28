package de.sr.ipv64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.data
import de.sr.ipv64.internal.list
import de.sr.ipv64.internal.query
import de.sr.ipv64.model.CreateIntegrationRequest
import de.sr.ipv64.model.Integration
import de.sr.ipv64.model.NotificationTestResult
import de.sr.ipv64.model.UpdateIntegrationRequest

/** Tag *Benachrichtigungen*: notification integrations. `id` 0 is the account e-mail. */
public class IntegrationsApi internal constructor(private val transport: HttpTransport) {

    /**
     * All integrations. `GET /integrations`
     *
     * @param includeSecrets show tokens and URLs in full; only request when needed, the result contains secrets.
     */
    public suspend fun list(includeSecrets: Boolean? = null): List<Integration> =
        transport.list(Method.GET, ApiPath("integrations"), query("include_secrets" to includeSecrets))

    /** Creates an integration; required fields depend on the type, see [CreateIntegrationRequest]. `POST /integrations` */
    public suspend fun create(request: CreateIntegrationRequest): Integration =
        transport.data(Method.POST, ApiPath("integrations"), body = transport.body(request))

    /**
     * A single integration. `GET /integrations/{id}`
     *
     * @param includeSecrets show tokens and URLs in full; only request when needed, the result contains secrets.
     */
    public suspend fun get(id: String, includeSecrets: Boolean? = null): Integration =
        transport.data(Method.GET, ApiPath("integrations", id), query("include_secrets" to includeSecrets))

    /** Changes name or preselection; for other changes create a new integration. `PATCH /integrations/{id}` */
    public suspend fun update(id: String, name: String? = null, default: Boolean? = null): Integration =
        update(id, UpdateIntegrationRequest(name, default))

    /** Changes name or preselection from a request object. `PATCH /integrations/{id}` */
    public suspend fun update(id: String, request: UpdateIntegrationRequest): Integration =
        transport.data(Method.PATCH, ApiPath("integrations", id), body = transport.body(request))

    /** Deletes an integration. `DELETE /integrations/{id}` */
    public suspend fun delete(id: String) {
        transport.unit(Method.DELETE, ApiPath("integrations", id))
    }

    /** Sends a test message (ID 0 = account e-mail); at most one per 30 seconds and integration. `POST /integrations/{id}/test` */
    public suspend fun test(id: String): NotificationTestResult =
        transport.data(Method.POST, ApiPath("integrations", id, "test"))
}
