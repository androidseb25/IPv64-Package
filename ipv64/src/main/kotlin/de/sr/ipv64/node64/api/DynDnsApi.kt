package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.internal.query
import de.sr.ipv64.node64.model.DynDNSAccountName
import de.sr.ipv64.node64.model.DynDNSSettings
import de.sr.ipv64.node64.model.DynDNSTarget
import de.sr.ipv64.node64.model.DynDNSTargetInput
import de.sr.ipv64.node64.model.DynDnsFamily
import de.sr.ipv64.node64.model.DynDnsFromAccountItem
import de.sr.ipv64.node64.model.DynDnsHistoryType
import de.sr.ipv64.node64.model.DynDnsUpdateNowResult
import de.sr.ipv64.node64.model.UpdateDynDnsSettingsRequest
import de.sr.ipv64.node64.model.UpdateDynDnsTargetRequest
import kotlinx.serialization.json.JsonElement

/** Tag *DynDNS*: DynDNS updater of the node. */
public class DynDnsApi internal constructor(private val transport: HttpTransport) {

    /**
     * State: public IPv4/IPv6 and prefix, connection hint (CGNAT/DS-Lite), targets by status, update limit.
     * Response body is not documented by the API. `GET /dyndns/status`
     */
    public suspend fun status(): JsonElement? =
        transport.jsonOrNull(Method.GET, ApiPath("dyndns", "status"))

    /**
     * Checks now: determines the addresses and compares the targets. Waits up to 45 s, then returns the state like
     * [status] (status 202 if the round takes longer). Response body is not documented by the API.
     * `POST /dyndns/check`
     */
    public suspend fun check(): JsonElement? =
        transport.jsonOrNull(Method.POST, ApiPath("dyndns", "check"))

    /** All targets. `GET /dyndns/targets` */
    public suspend fun listTargets(): List<DynDNSTarget> =
        transport.json(Method.GET, ApiPath("dyndns", "targets"))

    /** Creates a target. `POST /dyndns/targets` */
    public suspend fun createTarget(request: DynDNSTargetInput): DynDNSTarget =
        transport.json(Method.POST, ApiPath("dyndns", "targets"), body = transport.body(request))

    /** A single target. `GET /dyndns/targets/{id}` */
    public suspend fun getTarget(id: Long): DynDNSTarget =
        transport.json(Method.GET, ApiPath("dyndns", "targets", id.toString()))

    /** Changes a target (domain and host cannot be changed). `PUT /dyndns/targets/{id}` */
    public suspend fun updateTarget(id: Long, request: UpdateDynDnsTargetRequest): DynDNSTarget =
        transport.json(Method.PUT, ApiPath("dyndns", "targets", id.toString()), body = transport.body(request))

    /** Deletes a target (the record at ipv64 and the history stay). `DELETE /dyndns/targets/{id}` */
    public suspend fun deleteTarget(id: Long) {
        transport.unit(Method.DELETE, ApiPath("dyndns", "targets", id.toString()))
    }

    /**
     * Reports the address to ipv64 now, even without a change (costs update limit) and lifts an automatic pause.
     * [DynDnsUpdateNowResult.result] `nothing` means no current address is known for the modes. Fails with 409
     * while a round is running.
     * `POST /dyndns/targets/{id}/update`
     */
    public suspend fun updateTargetNow(id: Long): DynDnsUpdateNowResult =
        transport.json(Method.POST, ApiPath("dyndns", "targets", id.toString(), "update"))

    /**
     * History of updates at ipv64 or of IP checks (at most 300 entries, newest first).
     * Response body is not documented by the API.
     *
     * @param type `updates` or `checks`.
     * @param target only updates of this target.
     * @param family only for `type=checks`.
     * @param days 0 = everything.
     *
     * `GET /dyndns/history`
     */
    public suspend fun history(
        type: DynDnsHistoryType? = null,
        target: Long? = null,
        family: DynDnsFamily? = null,
        days: Int? = null,
    ): JsonElement? =
        transport.jsonOrNull(
            Method.GET,
            ApiPath("dyndns", "history"),
            query("type" to type, "target" to target, "family" to family, "days" to days),
        )

    /** Settings of the module. `GET /dyndns/settings` */
    public suspend fun settings(): DynDNSSettings =
        transport.json(Method.GET, ApiPath("dyndns", "settings"))

    /** Changes the settings; omitted fields keep their value. `PUT /dyndns/settings` */
    public suspend fun updateSettings(request: UpdateDynDnsSettingsRequest): DynDNSSettings =
        transport.json(Method.PUT, ApiPath("dyndns", "settings"), body = transport.body(request))

    /** Selectable names of the ipv64 account (needs the central ipv64 API key). `GET /dyndns/account/names` */
    public suspend fun accountNames(): List<DynDNSAccountName> =
        transport.json(Method.GET, ApiPath("dyndns", "account", "names"))

    /**
     * Creates targets from account names; the node fetches the record tokens itself. Empty modes use the suggestion.
     * The response (created targets and rejected names) is not documented by the API.
     * `POST /dyndns/targets/from-account`
     */
    public suspend fun createTargetsFromAccount(items: List<DynDnsFromAccountItem>): JsonElement? =
        transport.jsonOrNull(Method.POST, ApiPath("dyndns", "targets", "from-account"), body = transport.body(items))

    /** Fetches the record token again from ipv64 (lifts a pause because of badauth). `POST /dyndns/targets/{id}/token` */
    public suspend fun refreshTargetToken(id: Long): DynDNSTarget =
        transport.json(Method.POST, ApiPath("dyndns", "targets", id.toString(), "token"))
}
