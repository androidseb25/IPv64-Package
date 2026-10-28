package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.model.Check
import de.sr.ipv64.node64.model.CheckRequest
import de.sr.ipv64.node64.model.OnlineCheckAction

/**
 * Tag *Healthchecks*: online checks in API mode (node has an ipv64 API key but no healthchecks sync).
 * Otherwise the endpoints answer 409; with sync use [ChecksApi] (execution online).
 */
public class OnlineChecksApi internal constructor(private val transport: HttpTransport) {

    /** Online checks of the account. `GET /online-checks` */
    public suspend fun list(): List<Check> =
        transport.json(Method.GET, ApiPath("online-checks"))

    /** Creates an online check; same fields as [ChecksApi.create]. `POST /online-checks` */
    public suspend fun create(request: CheckRequest): Check =
        transport.json(Method.POST, ApiPath("online-checks"), body = transport.body(request))

    /** Reads an online check. `GET /online-checks/{token}` */
    public suspend fun get(token: String): Check =
        transport.json(Method.GET, ApiPath("online-checks", token))

    /** Changes an online check. `PUT /online-checks/{token}` */
    public suspend fun update(token: String, request: CheckRequest): Check =
        transport.json(Method.PUT, ApiPath("online-checks", token), body = transport.body(request))

    /** Deletes an online check. `DELETE /online-checks/{token}` */
    public suspend fun delete(token: String) {
        transport.unit(Method.DELETE, ApiPath("online-checks", token))
    }

    /** Pauses, resumes or runs an online check now (answer 202). `POST /online-checks/{token}/{action}` */
    public suspend fun action(token: String, action: OnlineCheckAction) {
        transport.unit(Method.POST, ApiPath("online-checks", token, action.value))
    }

    /** Pauses an online check. `POST /online-checks/{token}/{action}` */
    public suspend fun pause(token: String): Unit = action(token, OnlineCheckAction.PAUSE)

    /** Resumes an online check. `POST /online-checks/{token}/{action}` */
    public suspend fun resume(token: String): Unit = action(token, OnlineCheckAction.RESUME)

    /** Runs an online check now. `POST /online-checks/{token}/{action}` */
    public suspend fun run(token: String): Unit = action(token, OnlineCheckAction.RUN)
}
