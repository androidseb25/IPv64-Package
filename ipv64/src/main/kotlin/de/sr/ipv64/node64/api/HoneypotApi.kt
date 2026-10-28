package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.internal.query
import de.sr.ipv64.node64.model.Honeypot64Catalog
import de.sr.ipv64.node64.model.Honeypot64Config
import de.sr.ipv64.node64.model.Honeypot64ConfigRequest
import de.sr.ipv64.node64.model.Honeypot64Event
import de.sr.ipv64.node64.model.Honeypot64Live
import de.sr.ipv64.node64.model.Honeypot64Stats
import de.sr.ipv64.node64.model.Honeypot64Status
import de.sr.ipv64.node64.model.HoneypotEventSource
import de.sr.ipv64.node64.model.HoneypotEventsDeleted

/** Tag *Honeypot64*: honeypot sensor of the node. */
public class HoneypotApi internal constructor(private val transport: HttpTransport) {

    /** State of the trap services, port hints and counters. `GET /honeypot/status` */
    public suspend fun status(): Honeypot64Status =
        transport.json(Method.GET, ApiPath("honeypot", "status"))

    /**
     * Statistics of a period: numbers, rankings and timeline.
     *
     * @param since `24h` (default), `7d`, `<n>d` or a duration such as `12h`; at most the retention.
     * @param top length of the rankings (default 10, at most 100).
     *
     * `GET /honeypot/stats`
     */
    public suspend fun stats(since: String? = null, top: Int? = null): Honeypot64Stats =
        transport.json(Method.GET, ApiPath("honeypot", "stats"), query("since" to since, "top" to top))

    /**
     * Latest events, newest first.
     *
     * @param limit number of events (default 50, at most 1000).
     * @param source only events of this sensor.
     * @param country only events from this country (ISO 3166 alpha-2, e.g. `DE`); without country data the list is empty.
     *
     * `GET /honeypot/events`
     */
    public suspend fun events(
        limit: Int? = null,
        source: HoneypotEventSource? = null,
        country: String? = null,
    ): List<Honeypot64Event> =
        transport.json(
            Method.GET,
            ApiPath("honeypot", "events"),
            query("limit" to limit, "source" to source, "country" to country),
        )

    /** Deletes all stored events of this node (settings stay; written to the audit log). `DELETE /honeypot/events` */
    public suspend fun clearEvents(): HoneypotEventsDeleted =
        transport.json(Method.DELETE, ApiPath("honeypot", "events"))

    /** Settings. `GET /honeypot/config` */
    public suspend fun config(): Honeypot64Config =
        transport.json(Method.GET, ApiPath("honeypot", "config"))

    /**
     * Changes the settings. Enabling `report` starts the reported period now (needs the central ipv64 API key).
     * The API does not document whether omitted fields keep their value – to be safe send the complete
     * configuration (e.g. based on [config]). Invalid values answer 400 with the error per field.
     * `PUT /honeypot/config`
     */
    public suspend fun updateConfig(request: Honeypot64ConfigRequest): Honeypot64Config =
        transport.json(Method.PUT, ApiPath("honeypot", "config"), body = transport.body(request))

    /** Service catalog with the selection and the state of each port. `GET /honeypot/catalog` */
    public suspend fun catalog(): Honeypot64Catalog =
        transport.json(Method.GET, ApiPath("honeypot", "catalog"))

    /** Live map: attacks of the last minutes with country and map point. `GET /honeypot/live` */
    public suspend fun live(minutes: Int? = null): Honeypot64Live =
        transport.json(Method.GET, ApiPath("honeypot", "live"), query("minutes" to minutes))
}
