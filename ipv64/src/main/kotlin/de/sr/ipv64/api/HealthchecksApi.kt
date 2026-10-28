package de.sr.ipv64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.data
import de.sr.ipv64.internal.list
import de.sr.ipv64.internal.page
import de.sr.ipv64.internal.query
import de.sr.ipv64.model.CreateHealthcheckRequest
import de.sr.ipv64.model.Healthcheck
import de.sr.ipv64.model.HealthcheckDetail
import de.sr.ipv64.model.HealthcheckEvent
import de.sr.ipv64.model.HealthcheckStats
import de.sr.ipv64.model.HealthcheckStatus
import de.sr.ipv64.model.HealthcheckType
import de.sr.ipv64.model.Incident
import de.sr.ipv64.model.NotificationTestResult
import de.sr.ipv64.model.Page
import de.sr.ipv64.model.StatsRange
import de.sr.ipv64.model.UpdateHealthcheckRequest
import kotlin.time.Instant

/** Tag *Healthchecks* (classic). `token` is the 32 character healthcheck token or ID. */
public class HealthchecksApi internal constructor(private val transport: HttpTransport) {

    /** All healthchecks, optionally filtered by type and status. `GET /healthchecks` */
    public suspend fun list(type: HealthcheckType? = null, status: HealthcheckStatus? = null): List<Healthcheck> =
        transport.list(Method.GET, ApiPath("healthchecks"), query("type" to type, "status" to status))

    /**
     * Creates a healthcheck. Push check: afterwards call `push.url` of the result regularly.
     * Active checks require `target` or `url`. `POST /healthchecks`
     */
    public suspend fun create(request: CreateHealthcheckRequest): HealthcheckDetail =
        transport.data(Method.POST, ApiPath("healthchecks"), body = transport.body(request))

    /** A healthcheck with its latest events. `GET /healthchecks/{token}` */
    public suspend fun get(token: String): HealthcheckDetail =
        transport.data(Method.GET, ApiPath("healthchecks", token))

    /** Changes a healthcheck; only non-null fields are sent. Changing the type resets the check settings. `PATCH /healthchecks/{token}` */
    public suspend fun update(token: String, request: UpdateHealthcheckRequest): HealthcheckDetail =
        transport.data(Method.PATCH, ApiPath("healthchecks", token), body = transport.body(request))

    /** Deletes a healthcheck. Not possible while it controls an active DNS failover. `DELETE /healthchecks/{token}` */
    public suspend fun delete(token: String) {
        transport.unit(Method.DELETE, ApiPath("healthchecks", token))
    }

    /** Pauses a healthcheck. `POST /healthchecks/{token}/pause` */
    public suspend fun pause(token: String): Healthcheck =
        transport.data(Method.POST, ApiPath("healthchecks", token, "pause"))

    /** Resumes a paused healthcheck. `POST /healthchecks/{token}/resume` */
    public suspend fun resume(token: String): Healthcheck =
        transport.data(Method.POST, ApiPath("healthchecks", token, "resume"))

    /**
     * Creates a new token; the push URL changes. Events, incidents and failover monitors are kept.
     * `POST /healthchecks/{token}/rotate-token`
     */
    public suspend fun rotateToken(token: String): Healthcheck =
        transport.data(Method.POST, ApiPath("healthchecks", token, "rotate-token"))

    /**
     * Events. Without cursor newest first; with [after] oldest first, ideal for polling new events.
     * `GET /healthchecks/{token}/events`
     *
     * @param limit number of events (1 to 1000, server default 100).
     * @param before only events with a smaller ID.
     * @param after only events with a greater ID, ascending.
     * @param since only events from this point in time.
     */
    public suspend fun events(
        token: String,
        limit: Int? = null,
        before: Long? = null,
        after: Long? = null,
        since: Instant? = null,
    ): Page<HealthcheckEvent> =
        transport.page(
            Method.GET,
            ApiPath("healthchecks", token, "events"),
            query("limit" to limit, "before" to before, "after" to after, "since" to since),
        )

    /** Incidents. `GET /healthchecks/{token}/incidents` ([limit] 1 to 500, server default 50) */
    public suspend fun incidents(token: String, limit: Int? = null): List<Incident> =
        transport.list(Method.GET, ApiPath("healthchecks", token, "incidents"), query("limit" to limit))

    /** Availability and latency computed from the stored events, with time series. `GET /healthchecks/{token}/stats` ([range] server default 24h) */
    public suspend fun stats(token: String, range: StatsRange? = null): HealthcheckStats =
        transport.data(Method.GET, ApiPath("healthchecks", token, "stats"), query("range" to range))

    /** Sends a test message to all integrations of the check; at most one per 30 seconds. `POST /healthchecks/{token}/test-notification` */
    public suspend fun testNotification(token: String): NotificationTestResult =
        transport.data(Method.POST, ApiPath("healthchecks", token, "test-notification"))
}
