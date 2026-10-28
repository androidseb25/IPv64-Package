package de.sr.ipv64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.data
import de.sr.ipv64.model.HealthStatus
import de.sr.ipv64.model.PublicIpInfo
import de.sr.ipv64.model.ServiceInfo
import kotlinx.serialization.json.JsonObject

/** Tag *Meta*: public endpoints that do not need an API key. */
public class MetaApi internal constructor(private val transport: HttpTransport) {

    /** Service information and server time. `GET /` */
    public suspend fun info(): ServiceInfo =
        transport.data(Method.GET, ApiPath())

    /** Availability of the API, including the database connection. Suitable for monitoring. `GET /health` */
    public suspend fun health(): HealthStatus =
        transport.data(Method.GET, ApiPath("health"))

    /** The public IP address the API is reached from. `GET /ip` */
    public suspend fun ip(): PublicIpInfo =
        transport.data(Method.GET, ApiPath("ip"))

    /** The OpenAPI document of the API (without `data` envelope). `GET /openapi.json` */
    public suspend fun openApi(): JsonObject =
        transport.decode(JsonObject.serializer(), transport.text(Method.GET, ApiPath("openapi.json")))
}
