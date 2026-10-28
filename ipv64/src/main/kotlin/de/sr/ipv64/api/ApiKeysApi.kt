package de.sr.ipv64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.data
import de.sr.ipv64.internal.list
import de.sr.ipv64.model.ApiKey
import de.sr.ipv64.model.ApiKeyCreated
import de.sr.ipv64.model.CreateApiKeyRequest

/** Tag *API-Keys*. */
public class ApiKeysApi internal constructor(private val transport: HttpTransport) {

    /** All API keys; the keys themselves are never shown again, only their start and end. `GET /api-keys` */
    public suspend fun list(): List<ApiKey> =
        transport.list(Method.GET, ApiPath("api-keys"))

    /**
     * Creates an API key. The full key is only contained in this response. `POST /api-keys`
     *
     * @param name name of the key.
     * @param expiresIn validity in seconds, 0 = unlimited (max. 315360000).
     */
    public suspend fun create(name: String? = null, expiresIn: Int? = null): ApiKeyCreated =
        transport.data(
            Method.POST,
            ApiPath("api-keys"),
            body = transport.body(CreateApiKeyRequest(name = name, expiresIn = expiresIn)),
        )

    /** Deletes an API key. The key currently in use cannot delete itself. `DELETE /api-keys/{id}` */
    public suspend fun delete(id: String) {
        transport.unit(Method.DELETE, ApiPath("api-keys", id))
    }
}
