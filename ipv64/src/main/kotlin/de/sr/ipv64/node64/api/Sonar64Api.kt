package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.internal.query
import de.sr.ipv64.node64.model.Sonar64Inbound
import de.sr.ipv64.node64.model.Sonar64Recent
import de.sr.ipv64.node64.model.Sonar64Settings
import de.sr.ipv64.node64.model.Sonar64SettingsRequest
import de.sr.ipv64.node64.model.Sonar64Status
import kotlin.time.Instant

/** Tag *Sonar64*: latency measurements between participating nodes. */
public class Sonar64Api internal constructor(private val transport: HttpTransport) {

    /** State of the node, own node at ipv64 and trust level. `GET /sonar64/status` */
    public suspend fun status(): Sonar64Status =
        transport.json(Method.GET, ApiPath("sonar64", "status"))

    /** Local setting and profile at ipv64. `GET /sonar64/settings` */
    public suspend fun settings(): Sonar64Settings =
        transport.json(Method.GET, ApiPath("sonar64", "settings"))

    /** Changes participation, role, budget, quiet hours and profile; missing fields stay. `PUT /sonar64/settings` */
    public suspend fun updateSettings(request: Sonar64SettingsRequest): Sonar64Settings =
        transport.json(Method.PUT, ApiPath("sonar64", "settings"), body = transport.body(request))

    /** Who measured this node (up to 5 minutes delayed, max. 500 rows); [since] at most 24 h back. `GET /sonar64/inbound` */
    public suspend fun inbound(since: Instant? = null): Sonar64Inbound =
        transport.json(Method.GET, ApiPath("sonar64", "inbound"), query("since" to since))

    /** Latest own measurements and live probes, newest first; [n] 1-200 (default 50). `GET /sonar64/recent` */
    public suspend fun recent(n: Int? = null): Sonar64Recent =
        transport.json(Method.GET, ApiPath("sonar64", "recent"), query("n" to n))
}
