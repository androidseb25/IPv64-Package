package de.sr.ipv64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.data
import de.sr.ipv64.internal.list
import de.sr.ipv64.internal.page
import de.sr.ipv64.internal.query
import de.sr.ipv64.model.Hc2Account
import de.sr.ipv64.model.Hc2Channel
import de.sr.ipv64.model.Hc2ChannelInput
import de.sr.ipv64.model.Hc2ChannelTestResult
import de.sr.ipv64.model.Hc2ChannelType
import de.sr.ipv64.model.Hc2ChannelUpdate
import de.sr.ipv64.model.Hc2Check
import de.sr.ipv64.model.Hc2CheckDetail
import de.sr.ipv64.model.Hc2CheckInput
import de.sr.ipv64.model.Hc2CheckRunResult
import de.sr.ipv64.model.Hc2CheckStateFilter
import de.sr.ipv64.model.Hc2CheckUpdate
import de.sr.ipv64.model.Hc2Event
import de.sr.ipv64.model.Hc2Execution
import de.sr.ipv64.model.Hc2Incident
import de.sr.ipv64.model.Hc2Maintenance
import de.sr.ipv64.model.Hc2MaintenanceInput
import de.sr.ipv64.model.Hc2MaintenanceUpdate
import de.sr.ipv64.model.Hc2Measurement
import de.sr.ipv64.model.Hc2MoveCheckRequest
import de.sr.ipv64.model.Hc2SendMessageRequest
import de.sr.ipv64.model.Hc2SendMessageResult
import de.sr.ipv64.model.Hc2SettingsUpdate
import de.sr.ipv64.model.Hc2Stats
import de.sr.ipv64.model.Hc2StatsRange
import de.sr.ipv64.model.Hc2Testrun
import de.sr.ipv64.model.Hc2VerifyChannelRequest
import de.sr.ipv64.model.Page
import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

/**
 * Tag *Healthchecks 2.0*: account, settings, events, incidents and test runs, plus the sub APIs
 * [checks], [channels] and [maintenance]. Nodes live in [NodesApi].
 */
public class Hc2Api internal constructor(private val transport: HttpTransport) {

    /** Checks (create, change, pause, run, move, measurements, statistics). */
    public val checks: Hc2ChecksApi = Hc2ChecksApi(transport)

    /** Notification channels. */
    public val channels: Hc2ChannelsApi = Hc2ChannelsApi(transport)

    /** Maintenance windows. */
    public val maintenance: Hc2MaintenanceApi = Hc2MaintenanceApi(transport)

    /** Plan, limits and locations of the account. `GET /hc2/account` */
    public suspend fun account(): Hc2Account =
        transport.data(Method.GET, ApiPath("hc2", "account"))

    /** Notification settings (quiet hours, batching) as raw JSON object. `GET /hc2/settings` */
    public suspend fun settings(): JsonObject =
        transport.data(Method.GET, ApiPath("hc2", "settings"))

    /** Changes notification settings; only set properties are changed. `PATCH /hc2/settings` */
    public suspend fun updateSettings(request: Hc2SettingsUpdate): JsonObject =
        transport.data(Method.PATCH, ApiPath("hc2", "settings"), body = transport.body(request))

    /**
     * Events of all checks. [limit] is at most 100; [before] returns only IDs below (older), [after] only IDs
     * above (newer, ascending); [since] is a point in time. `GET /hc2/events`
     */
    public suspend fun events(
        limit: Int? = null,
        before: Long? = null,
        after: Long? = null,
        since: Instant? = null,
    ): Page<Hc2Event> =
        transport.page(
            Method.GET,
            ApiPath("hc2", "events"),
            query("limit" to limit, "before" to before, "after" to after, "since" to since),
        )

    /**
     * Incidents of the account. [limit] is at most 1000; [open] = true returns only open incidents;
     * [since] is a point in time. `GET /hc2/incidents`
     */
    public suspend fun incidents(limit: Int? = null, open: Boolean? = null, since: Instant? = null): List<Hc2Incident> =
        transport.list(Method.GET, ApiPath("hc2", "incidents"), query("limit" to limit, "open" to open, "since" to since))

    /** Status of a test run (poll every 2 s, at most 60 s). `GET /hc2/testruns/{id}` */
    public suspend fun testrun(id: Long): Hc2Testrun =
        transport.data(Method.GET, ApiPath("hc2", "testruns", id.toString()))

    /** Channel types with their configuration fields. `GET /hc2/channel-types` */
    public suspend fun channelTypes(): List<Hc2ChannelType> =
        transport.list(Method.GET, ApiPath("hc2", "channel-types"))
}

/** Sub API of [Hc2Api] for checks. */
public class Hc2ChecksApi internal constructor(private val transport: HttpTransport) {

    /**
     * Lists checks. Filters: [type] (e.g. `http`), [state], [execution], [nodeId], [tag], [group], [q] (search in
     * name/target). `GET /hc2/checks`
     */
    public suspend fun list(
        type: String? = null,
        state: Hc2CheckStateFilter? = null,
        execution: Hc2Execution? = null,
        nodeId: String? = null,
        tag: String? = null,
        group: String? = null,
        q: String? = null,
    ): List<Hc2Check> =
        transport.list(
            Method.GET,
            ApiPath("hc2", "checks"),
            query(
                "type" to type, "state" to state, "execution" to execution, "node_id" to nodeId,
                "tag" to tag, "group" to group, "q" to q,
            ),
        )

    /**
     * Creates a check. Online checks count against the limit per location and must not have private targets;
     * local checks need a linked node. `POST /hc2/checks`
     */
    public suspend fun create(request: Hc2CheckInput): Hc2CheckDetail =
        transport.data(Method.POST, ApiPath("hc2", "checks"), body = transport.body(request))

    /** Returns a check with measuring points and availability. [token] has 32 characters. `GET /hc2/checks/{token}` */
    public suspend fun get(token: String): Hc2CheckDetail =
        transport.data(Method.GET, ApiPath("hc2", "checks", token))

    /**
     * Changes a check; only the properties that are set are changed. Changing execution/node_id/locations is
     * rejected with 409 `move_requires_test`: use [move] instead (or set [Hc2CheckUpdate.forceMove]).
     * `PATCH /hc2/checks/{token}`
     */
    public suspend fun update(token: String, request: Hc2CheckUpdate): Hc2CheckDetail =
        transport.data(Method.PATCH, ApiPath("hc2", "checks", token), body = transport.body(request))

    /** Deletes a check. `DELETE /hc2/checks/{token}` */
    public suspend fun delete(token: String) {
        transport.unit(Method.DELETE, ApiPath("hc2", "checks", token))
    }

    /** Pauses a check. `POST /hc2/checks/{token}/pause` */
    public suspend fun pause(token: String): Hc2Check =
        transport.data(Method.POST, ApiPath("hc2", "checks", token, "pause"))

    /** Resumes a check. `POST /hc2/checks/{token}/resume` */
    public suspend fun resume(token: String): Hc2Check =
        transport.data(Method.POST, ApiPath("hc2", "checks", token, "resume"))

    /** Runs a check immediately (node checks: sends a command to the node). `POST /hc2/checks/{token}/run` */
    public suspend fun run(token: String): Hc2CheckRunResult =
        transport.data(Method.POST, ApiPath("hc2", "checks", token, "run"))

    /**
     * Changes the execution location with a test run. Poll [Hc2Api.testrun] until the status is `ok`; then the
     * change is applied and token and history are kept. `POST /hc2/checks/{token}/move`
     */
    public suspend fun move(token: String, request: Hc2MoveCheckRequest): Hc2Testrun =
        transport.data(Method.POST, ApiPath("hc2", "checks", token, "move"), body = transport.body(request))

    /**
     * Raw measurements, newest first (7 days). [limit] is at most 5000; [since]/[until] limit the time range;
     * [source] is `loc:nbg`, `loc:fsn` or `node:<id>`. `GET /hc2/checks/{token}/measurements`
     */
    public suspend fun measurements(
        token: String,
        limit: Int? = null,
        since: Instant? = null,
        until: Instant? = null,
        source: String? = null,
    ): List<Hc2Measurement> =
        transport.list(
            Method.GET,
            ApiPath("hc2", "checks", token, "measurements"),
            query("limit" to limit, "since" to since, "until" to until, "source" to source),
        )

    /**
     * Events of a check. [limit] is at most 100; [before]/[after] filter by event ID; [since] limits the start time.
     * `GET /hc2/checks/{token}/events`
     */
    public suspend fun events(
        token: String,
        limit: Int? = null,
        before: Long? = null,
        after: Long? = null,
        since: Instant? = null,
    ): Page<Hc2Event> =
        transport.page(
            Method.GET,
            ApiPath("hc2", "checks", token, "events"),
            query("limit" to limit, "before" to before, "after" to after, "since" to since),
        )

    /** Incidents of a check. [limit] is at most 500. `GET /hc2/checks/{token}/incidents` */
    public suspend fun incidents(token: String, limit: Int? = null): List<Hc2Incident> =
        transport.list(Method.GET, ApiPath("hc2", "checks", token, "incidents"), query("limit" to limit))

    /** Availability and latency over [range] (default server side). `GET /hc2/checks/{token}/stats` */
    public suspend fun stats(token: String, range: Hc2StatsRange? = null): Hc2Stats =
        transport.data(Method.GET, ApiPath("hc2", "checks", token, "stats"), query("range" to range))
}

/** Sub API of [Hc2Api] for notification channels. */
public class Hc2ChannelsApi internal constructor(private val transport: HttpTransport) {

    /** Lists channels. `GET /hc2/channels` */
    public suspend fun list(): List<Hc2Channel> =
        transport.list(Method.GET, ApiPath("hc2", "channels"))

    /**
     * Creates a channel. Mail and SMS channels must then be confirmed by code ([sendVerificationCode], [verify]);
     * nothing is sent before. `POST /hc2/channels`
     */
    public suspend fun create(request: Hc2ChannelInput): Hc2Channel =
        transport.data(Method.POST, ApiPath("hc2", "channels"), body = transport.body(request))

    /** Returns a channel. `GET /hc2/channels/{id}` */
    public suspend fun get(id: Long): Hc2Channel =
        transport.data(Method.GET, ApiPath("hc2", "channels", id.toString()))

    /**
     * Changes a channel; only the properties that are set are changed, omit secret config fields to keep them.
     * `PATCH /hc2/channels/{id}`
     */
    public suspend fun update(id: Long, request: Hc2ChannelUpdate): Hc2Channel =
        transport.data(Method.PATCH, ApiPath("hc2", "channels", id.toString()), body = transport.body(request))

    /** Deletes a channel. `DELETE /hc2/channels/{id}` */
    public suspend fun delete(id: Long) {
        transport.unit(Method.DELETE, ApiPath("hc2", "channels", id.toString()))
    }

    /** Sends a test message. `POST /hc2/channels/{id}/test` */
    public suspend fun test(id: Long): Hc2ChannelTestResult =
        transport.data(Method.POST, ApiPath("hc2", "channels", id.toString(), "test"))

    /**
     * Sends a message directly through a verified, enabled ipv64 channel (ipv64_mail, ipv64_sms, app), e.g. for
     * node module notices. Rate limit: 30 at once, then 30 per hour, at most 200 per day; an `event_id` already
     * sent within 24 h is not sent again (`duplicate`). `POST /hc2/channels/{id}/send`
     */
    public suspend fun send(id: Long, request: Hc2SendMessageRequest): Hc2SendMessageResult =
        transport.data(Method.POST, ApiPath("hc2", "channels", id.toString(), "send"), body = transport.body(request))

    /**
     * Sends a 6-digit confirmation code to the channel's e-mail address or phone number (valid 15 minutes, at most
     * 5 per hour). Already confirmed channels: nothing is sent. `POST /hc2/channels/{id}/verify/send`
     */
    public suspend fun sendVerificationCode(id: Long) {
        transport.unit(Method.POST, ApiPath("hc2", "channels", id.toString(), "verify", "send"))
    }

    /** Confirms the channel with the [code] from [sendVerificationCode]. `POST /hc2/channels/{id}/verify` */
    public suspend fun verify(id: Long, code: String) {
        transport.unit(
            Method.POST,
            ApiPath("hc2", "channels", id.toString(), "verify"),
            body = transport.body(Hc2VerifyChannelRequest(code)),
        )
    }
}

/** Sub API of [Hc2Api] for maintenance windows. */
public class Hc2MaintenanceApi internal constructor(private val transport: HttpTransport) {

    /** Lists maintenance windows. `GET /hc2/maintenance` */
    public suspend fun list(): List<Hc2Maintenance> =
        transport.list(Method.GET, ApiPath("hc2", "maintenance"))

    /** Creates a maintenance window. `POST /hc2/maintenance` */
    public suspend fun create(request: Hc2MaintenanceInput): Hc2Maintenance =
        transport.data(Method.POST, ApiPath("hc2", "maintenance"), body = transport.body(request))

    /** Returns a maintenance window. `GET /hc2/maintenance/{id}` */
    public suspend fun get(id: Long): Hc2Maintenance =
        transport.data(Method.GET, ApiPath("hc2", "maintenance", id.toString()))

    /** Changes a maintenance window; only the properties that are set are changed. `PATCH /hc2/maintenance/{id}` */
    public suspend fun update(id: Long, request: Hc2MaintenanceUpdate): Hc2Maintenance =
        transport.data(Method.PATCH, ApiPath("hc2", "maintenance", id.toString()), body = transport.body(request))

    /** Deletes a maintenance window. `DELETE /hc2/maintenance/{id}` */
    public suspend fun delete(id: Long) {
        transport.unit(Method.DELETE, ApiPath("hc2", "maintenance", id.toString()))
    }
}
