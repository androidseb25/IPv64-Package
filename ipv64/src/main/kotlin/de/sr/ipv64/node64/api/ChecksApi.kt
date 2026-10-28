package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.internal.query
import de.sr.ipv64.node64.internal.jsonOptional
import de.sr.ipv64.node64.model.BulkCheckAction
import de.sr.ipv64.node64.model.BulkCheckRequest
import de.sr.ipv64.node64.model.BulkCheckResult
import de.sr.ipv64.node64.model.ChartRange
import de.sr.ipv64.node64.model.Check
import de.sr.ipv64.node64.model.CheckChart
import de.sr.ipv64.node64.model.CheckRequest
import de.sr.ipv64.node64.model.CheckStateValue
import de.sr.ipv64.node64.model.CheckType
import de.sr.ipv64.node64.model.Event
import de.sr.ipv64.node64.model.Incident
import de.sr.ipv64.node64.model.Measurement
import de.sr.ipv64.node64.model.OnlineTestRequest
import de.sr.ipv64.node64.model.OnlineTestResult
import de.sr.ipv64.node64.model.PushResult
import de.sr.ipv64.node64.model.PushStatus
import de.sr.ipv64.node64.model.Stats
import de.sr.ipv64.node64.model.StatsRange
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

/** Tag *Healthchecks*: checks, events, incidents, check types and push URLs. */
public class ChecksApi internal constructor(private val transport: HttpTransport) {

    /** All checks, optionally filtered by type, state, tag or search text. `GET /checks` */
    public suspend fun list(
        type: CheckType? = null,
        state: CheckStateValue? = null,
        tag: String? = null,
        q: String? = null,
    ): List<Check> =
        transport.json(Method.GET, ApiPath("checks"), query("type" to type, "state" to state, "tag" to tag, "q" to q))

    /**
     * Creates a check. Mandatory: name, type, target (except push). The creating node becomes the owner.
     * Response body is not documented by the API; the created check is returned if the node sends it.
     * `POST /checks`
     */
    public suspend fun create(request: CheckRequest): Check? =
        transport.jsonOptional(Method.POST, ApiPath("checks"), body = transport.body(request))

    /** A single check with its state. `GET /checks/{token}` */
    public suspend fun get(token: String): Check =
        transport.json(Method.GET, ApiPath("checks", token))

    /**
     * Changes a check. name, type and target are always taken over (send them); description is cleared when
     * omitted; other omitted values stay. Checks of other nodes can be changed too.
     * Response body is not documented by the API; the check is returned if the node sends it.
     * `PUT /checks/{token}`
     */
    public suspend fun update(token: String, request: CheckRequest): Check? =
        transport.jsonOptional(Method.PUT, ApiPath("checks", token), body = transport.body(request))

    /** Deletes the check with all measurements on all nodes and at ipv64. `DELETE /checks/{token}` */
    public suspend fun delete(token: String) {
        transport.unit(Method.DELETE, ApiPath("checks", token))
    }

    /** Pauses the check on all nodes. `POST /checks/{token}/pause` */
    public suspend fun pause(token: String): Check =
        transport.json(Method.POST, ApiPath("checks", token, "pause"))

    /** Resumes the check on all nodes (measures immediately). `POST /checks/{token}/resume` */
    public suspend fun resume(token: String): Check =
        transport.json(Method.POST, ApiPath("checks", token, "resume"))

    /** Measures now (answer 202 without content). Not possible for push checks. `POST /checks/{token}/run` */
    public suspend fun run(token: String) {
        transport.unit(Method.POST, ApiPath("checks", token, "run"))
    }

    /** Copies the check (name suffix "(copy)", paused). `POST /checks/{token}/clone` */
    public suspend fun clone(token: String): Check =
        transport.json(Method.POST, ApiPath("checks", token, "clone"))

    /**
     * Measurements, newest first.
     * @param before cursor: only measurements before this `seq`
     * @param from start time, @param to end time (as accepted by the node, e.g. ISO 8601)
     * `GET /checks/{token}/measurements`
     */
    public suspend fun measurements(
        token: String,
        limit: Int? = null,
        before: Long? = null,
        from: String? = null,
        to: String? = null,
    ): List<Measurement> =
        transport.json(
            Method.GET,
            ApiPath("checks", token, "measurements"),
            query("limit" to limit, "before" to before, "from" to from, "to" to to),
        )

    /** State changes of a check. `GET /checks/{token}/events` */
    public suspend fun events(token: String, limit: Int? = null, before: Long? = null): List<Event> =
        transport.json(Method.GET, ApiPath("checks", token, "events"), query("limit" to limit, "before" to before))

    /** Outages of a check. `GET /checks/{token}/incidents` */
    public suspend fun incidents(token: String, limit: Int? = null): List<Incident> =
        transport.json(Method.GET, ApiPath("checks", token, "incidents"), query("limit" to limit))

    /** Availability and latency as a time series. `GET /checks/{token}/stats` */
    public suspend fun stats(token: String, range: StatsRange? = null): Stats =
        transport.json(Method.GET, ApiPath("checks", token, "stats"), query("range" to range))

    /**
     * Latency per measuring point (this node, other nodes, ipv64 locations). 1h–24h single measurements,
     * 7d/30d hourly, 1y daily.
     * `GET /checks/{token}/chart`
     */
    public suspend fun chart(token: String, range: ChartRange): CheckChart =
        transport.json(Method.GET, ApiPath("checks", token, "chart"), query("range" to range))

    /** State changes of all checks. `GET /events` */
    public suspend fun allEvents(limit: Int? = null, before: Long? = null): List<Event> =
        transport.json(Method.GET, ApiPath("events"), query("limit" to limit, "before" to before))

    /** Outages of all checks. `GET /incidents` */
    public suspend fun allIncidents(limit: Int? = null): List<Incident> =
        transport.json(Method.GET, ApiPath("incidents"), query("limit" to limit))

    /**
     * Available check types (http, keyword, icmp, tcp, port, cert, push, docker, …).
     * Response body is not documented by the API.
     * `GET /check-types`
     */
    public suspend fun types(): JsonElement? =
        transport.jsonOrNull(Method.GET, ApiPath("check-types"))

    /** Bulk action for 1 to 1000 checks. `POST /checks/bulk` */
    public suspend fun bulk(request: BulkCheckRequest): BulkCheckResult =
        transport.json(Method.POST, ApiPath("checks", "bulk"), body = transport.body(request))

    /** Bulk action for 1 to 1000 checks. `POST /checks/bulk` */
    public suspend fun bulk(action: BulkCheckAction, tokens: List<String>, tag: String? = null): BulkCheckResult =
        bulk(BulkCheckRequest(action, tokens, tag))

    /**
     * Test run from the ipv64 locations (takes up to ~70 s); nothing is saved. Needs an ipv64 link.
     * `POST /checks/online-test`
     */
    public suspend fun onlineTest(request: OnlineTestRequest): OnlineTestResult =
        transport.json(Method.POST, ApiPath("checks", "online-test"), body = transport.body(request))

    /**
     * Heartbeat of a push check (public, no API key needed). `status` other than `down` counts as up; `msg` at most
     * 200 characters. Rate limit: 1 per second, 60 per minute (HTTP 429).
     * `GET /push/{token}`
     */
    public suspend fun push(token: String, status: PushStatus? = null, msg: String? = null): PushResult =
        transport.json(Method.GET, ApiPath("push", token), query("status" to status, "msg" to msg))

    /**
     * Heartbeat of a push check via POST with an optional plain text [message] (up to 4 KB, stored as
     * `details.payload`). Public, no API key needed.
     * `POST /push/{token}`
     */
    public suspend fun pushPayload(
        token: String,
        message: String? = null,
        status: PushStatus? = null,
        msg: String? = null,
    ): PushResult =
        transport.json(
            Method.POST,
            ApiPath("push", token),
            query("status" to status, "msg" to msg),
            body = (message ?: "").toRequestBody(TEXT_PLAIN),
        )

    /**
     * Heartbeat of a push check via POST with a JSON object payload (up to 4 KB, stored as `details.payload`).
     * `POST /push/{token}`
     */
    public suspend fun pushPayload(
        token: String,
        payload: JsonObject,
        status: PushStatus? = null,
        msg: String? = null,
    ): PushResult =
        transport.json(
            Method.POST,
            ApiPath("push", token),
            query("status" to status, "msg" to msg),
            body = payload.toString().toRequestBody(JSON),
        )

    private companion object {
        val TEXT_PLAIN = "text/plain; charset=utf-8".toMediaType()
        val JSON = "application/json; charset=utf-8".toMediaType()
    }
}
