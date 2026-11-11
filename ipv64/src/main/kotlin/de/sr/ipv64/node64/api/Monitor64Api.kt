package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.internal.query
import de.sr.ipv64.node64.model.Monitor64Agent
import de.sr.ipv64.node64.model.Monitor64MaintInput
import de.sr.ipv64.node64.model.Monitor64MaintList
import de.sr.ipv64.node64.model.Monitor64Uid
import de.sr.ipv64.node64.model.Monitor64NodeAddr
import de.sr.ipv64.node64.model.Monitor64Ok
import de.sr.ipv64.node64.model.Monitor64Overview
import de.sr.ipv64.node64.model.Monitor64ProblemAction
import de.sr.ipv64.node64.model.Monitor64ProblemDetail
import de.sr.ipv64.node64.model.Monitor64ProblemList
import de.sr.ipv64.node64.model.Monitor64ProblemStatus
import de.sr.ipv64.node64.model.Monitor64PushToken
import de.sr.ipv64.node64.model.Monitor64Range
import de.sr.ipv64.node64.model.Monitor64Series
import de.sr.ipv64.node64.model.Monitor64Settings
import de.sr.ipv64.node64.model.Monitor64SettingsInput
import de.sr.ipv64.node64.model.Monitor64Status
import de.sr.ipv64.node64.model.Monitor64TestInput
import de.sr.ipv64.node64.model.Monitor64TestResult
import de.sr.ipv64.node64.model.Monitor64TrapStatus

/** Tag *Monitor64*: monitoring of hosts (`/monitor64/…`) with overview, problems, maintenance, settings, traps and agents. A module that is off answers 404. */
public class Monitor64Api internal constructor(private val transport: HttpTransport) {

    /** Hosts and their accesses. */
    public val hosts: Monitor64HostsApi = Monitor64HostsApi(transport)

    /** Cluster view. */
    public val cluster: Monitor64ClusterApi = Monitor64ClusterApi(transport)

    /** Threshold rules. */
    public val rules: Monitor64RulesApi = Monitor64RulesApi(transport)

    /** Notification channels and overrides. */
    public val notify: Monitor64NotifyApi = Monitor64NotifyApi(transport)

    /** Technical state of the module. `GET /monitor64/status` */
    public suspend fun status(): Monitor64Status =
        transport.json(Method.GET, ApiPath("monitor64", "status"))

    /** Overview of all hosts, problems and recent events. `GET /monitor64/overview` */
    public suspend fun overview(): Monitor64Overview =
        transport.json(Method.GET, ApiPath("monitor64", "overview"))

    /**
     * Up to 48 averages of the last 24 hours. Without [host]: CPU load per host (key: host UID);
     * with [host]: the first measure with a threshold per service (key: service).
     *
     * @param host UID of a host.
     * `GET /monitor64/sparks`
     */
    public suspend fun sparks(host: String? = null): Map<String, List<Double>> =
        transport.json(Method.GET, ApiPath("monitor64", "sparks"), query("host" to host))

    /**
     * Series of a host for charts. Range: [range] (with [off] ranges back) or [from]/[to] (zoom, takes precedence); default last 24 hours.
     *
     * @param host UID of the host.
     * @param s series `service|measure` (1-16).
     * @param off number of ranges back (0-1000).
     * @param from start (Unix seconds, only with [to]).
     * @param to end (Unix seconds, after [from]).
     * @param points at most this many points per series (1-700, default 700).
     * `GET /monitor64/series`
     */
    public suspend fun series(
        host: String,
        s: List<String>,
        range: Monitor64Range? = null,
        off: Int? = null,
        from: Long? = null,
        to: Long? = null,
        points: Int? = null,
    ): Monitor64Series =
        transport.json(
            Method.GET,
            ApiPath("monitor64", "series"),
            query("host" to host, "s" to s, "range" to range, "off" to off, "from" to from, "to" to to, "points" to points),
        )

    /**
     * Address of this node as seen in the direction of a host.
     *
     * @param target address of the host.
     * `GET /monitor64/nodeaddr`
     */
    public suspend fun nodeAddr(target: String? = null): Monitor64NodeAddr =
        transport.json(Method.GET, ApiPath("monitor64", "nodeaddr"), query("target" to target))

    /** Tests a connection without saving. A failed test is a normal result with `ok=false`. `POST /monitor64/test` */
    public suspend fun test(request: Monitor64TestInput): Monitor64TestResult =
        transport.json(Method.POST, ApiPath("monitor64", "test"), body = transport.body(request))

    /**
     * Problems.
     *
     * @param status open, closed or all.
     * @param range time range.
     * @param who filter by involved person.
     * @param q search text.
     * @param host host UID.
     * `GET /monitor64/problems`
     */
    public suspend fun problems(
        status: Monitor64ProblemStatus? = null,
        range: Monitor64Range? = null,
        who: String? = null,
        q: String? = null,
        host: String? = null,
    ): Monitor64ProblemList =
        transport.json(
            Method.GET,
            ApiPath("monitor64", "problems"),
            query("status" to status, "range" to range, "who" to who, "q" to q, "host" to host),
        )

    /** Problems as CSV text (same filters as [problems]). `GET /monitor64/problems.csv` */
    public suspend fun problemsCsv(
        status: Monitor64ProblemStatus? = null,
        range: Monitor64Range? = null,
        who: String? = null,
        q: String? = null,
        host: String? = null,
    ): String =
        transport.text(
            Method.GET,
            ApiPath("monitor64", "problems.csv"),
            query("status" to status, "range" to range, "who" to who, "q" to q, "host" to host),
            headers = mapOf("Accept" to "text/csv"),
        )

    /** A problem with its history. `GET /monitor64/problems/{uid}` */
    public suspend fun problem(uid: String): Monitor64ProblemDetail =
        transport.json(Method.GET, ApiPath("monitor64", "problems", uid))

    /** Acknowledges, comments or resolves a problem. `POST /monitor64/problems/action` */
    public suspend fun problemAction(request: Monitor64ProblemAction): Monitor64Ok =
        transport.json(Method.POST, ApiPath("monitor64", "problems", "action"), body = transport.body(request))

    /** Maintenance windows with hosts, targets and time zone. `GET /monitor64/maintenance` */
    public suspend fun maintenance(): Monitor64MaintList =
        transport.json(Method.GET, ApiPath("monitor64", "maintenance"))

    /** Creates a maintenance window. `POST /monitor64/maintenance` */
    public suspend fun createMaintenance(request: Monitor64MaintInput): Monitor64Uid =
        transport.json(Method.POST, ApiPath("monitor64", "maintenance"), body = transport.body(request))

    /** Changes a maintenance window. `PUT /monitor64/maintenance/{uid}` */
    public suspend fun updateMaintenance(uid: String, request: Monitor64MaintInput): Monitor64Uid =
        transport.json(Method.PUT, ApiPath("monitor64", "maintenance", uid), body = transport.body(request))

    /** Removes a maintenance window. `DELETE /monitor64/maintenance/{uid}` */
    public suspend fun deleteMaintenance(uid: String): Monitor64Ok =
        transport.json(Method.DELETE, ApiPath("monitor64", "maintenance", uid))

    /** Ends a maintenance window now. `POST /monitor64/maintenance/{uid}/end` */
    public suspend fun endMaintenance(uid: String): Monitor64Ok =
        transport.json(Method.POST, ApiPath("monitor64", "maintenance", uid, "end"))

    /** Settings. `GET /monitor64/settings` */
    public suspend fun settings(): Monitor64Settings =
        transport.json(Method.GET, ApiPath("monitor64", "settings"))

    /** Saves the settings. `PUT /monitor64/settings` */
    public suspend fun updateSettings(request: Monitor64SettingsInput): Monitor64Ok =
        transport.json(Method.PUT, ApiPath("monitor64", "settings"), body = transport.body(request))

    /** State of the SNMP trap receiver. `GET /monitor64/traps` */
    public suspend fun traps(): Monitor64TrapStatus =
        transport.json(Method.GET, ApiPath("monitor64", "traps"))

    /** Clears the list of unknown trap senders. `DELETE /monitor64/traps/unknown` */
    public suspend fun clearUnknownTraps(): Monitor64Ok =
        transport.json(Method.DELETE, ApiPath("monitor64", "traps", "unknown"))

    /** New push token for the install command (stored only with the access; grants no access) and the push port. `GET /monitor64/push/token` */
    public suspend fun pushToken(): Monitor64PushToken =
        transport.json(Method.GET, ApiPath("monitor64", "push", "token"))

    /** State of the agent distribution. `GET /monitor64/agent` */
    public suspend fun agent(): Monitor64Agent =
        transport.json(Method.GET, ApiPath("monitor64", "agent"))

    /** Fetches the agent directory again now. `POST /monitor64/agent/refresh` */
    public suspend fun refreshAgent(): Monitor64Agent =
        transport.json(Method.POST, ApiPath("monitor64", "agent", "refresh"))
}
