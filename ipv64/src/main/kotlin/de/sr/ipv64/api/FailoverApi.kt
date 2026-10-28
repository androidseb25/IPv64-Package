package de.sr.ipv64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.data
import de.sr.ipv64.internal.list
import de.sr.ipv64.model.AddFailoverMonitorRequest
import de.sr.ipv64.model.AddFailoverRuleRequest
import de.sr.ipv64.model.CreateFailoverPolicyRequest
import de.sr.ipv64.model.FailoverPolicy
import de.sr.ipv64.model.UpdateFailoverPolicyRequest

/** Tag *DNS-Failover*: policies that switch DNS records to replacement targets. */
public class FailoverApi internal constructor(private val transport: HttpTransport) {

    /** Lists all failover policies. `GET /failover/policies` */
    public suspend fun list(): List<FailoverPolicy> =
        transport.list(Method.GET, ApiPath("failover", "policies"))

    /** Creates a failover policy. `POST /failover/policies` */
    public suspend fun create(request: CreateFailoverPolicyRequest = CreateFailoverPolicyRequest()): FailoverPolicy =
        transport.data(Method.POST, ApiPath("failover", "policies"), body = transport.body(request))

    /** Returns a single policy. `GET /failover/policies/{id}` */
    public suspend fun get(id: String): FailoverPolicy =
        transport.data(Method.GET, ApiPath("failover", "policies", id))

    /** Updates a policy. `linking` cannot be changed during an active failover. `PATCH /failover/policies/{id}` */
    public suspend fun update(id: String, request: UpdateFailoverPolicyRequest): FailoverPolicy =
        transport.data(Method.PATCH, ApiPath("failover", "policies", id), body = transport.body(request))

    /** Deletes a policy. Not possible while the failover is active. `DELETE /failover/policies/{id}` */
    public suspend fun delete(id: String) {
        transport.unit(Method.DELETE, ApiPath("failover", "policies", id))
    }

    /** Pauses a policy. `POST /failover/policies/{id}/pause` */
    public suspend fun pause(id: String): FailoverPolicy =
        transport.data(Method.POST, ApiPath("failover", "policies", id, "pause"))

    /** Resumes a paused policy. `POST /failover/policies/{id}/resume` */
    public suspend fun resume(id: String): FailoverPolicy =
        transport.data(Method.POST, ApiPath("failover", "policies", id, "resume"))

    /** Switches manually to the replacement targets (202 Accepted). Only in status `active`. `POST /failover/policies/{id}/failover` */
    public suspend fun failover(id: String): FailoverPolicy =
        transport.data(Method.POST, ApiPath("failover", "policies", id, "failover"))

    /** Switches manually back to the original targets (202 Accepted). Only in status `failed_over`. `POST /failover/policies/{id}/fallback` */
    public suspend fun fallback(id: String): FailoverPolicy =
        transport.data(Method.POST, ApiPath("failover", "policies", id, "fallback"))

    /**
     * Adds a monitor (a healthcheck that triggers the failover).
     * @param healthcheck healthcheck token
     * @param negate evaluate inverted
     * `POST /failover/policies/{id}/monitors`
     */
    public suspend fun addMonitor(id: String, healthcheck: String, negate: Boolean? = null): FailoverPolicy =
        transport.data(
            Method.POST,
            ApiPath("failover", "policies", id, "monitors"),
            body = transport.body(AddFailoverMonitorRequest(healthcheck, negate)),
        )

    /** Removes a monitor by healthcheck token. `DELETE /failover/policies/{id}/monitors/{token}` */
    public suspend fun removeMonitor(id: String, token: String) {
        transport.unit(Method.DELETE, ApiPath("failover", "policies", id, "monitors", token))
    }

    /**
     * Adds a rule protecting an A or AAAA record. Set exactly one target in [request].
     * `POST /failover/policies/{id}/rules`
     */
    public suspend fun addRule(id: String, request: AddFailoverRuleRequest): FailoverPolicy =
        transport.data(Method.POST, ApiPath("failover", "policies", id, "rules"), body = transport.body(request))

    /** Removes a rule by record ID or token. `DELETE /failover/policies/{id}/rules/{record}` */
    public suspend fun removeRule(id: String, record: String) {
        transport.unit(Method.DELETE, ApiPath("failover", "policies", id, "rules", record))
    }
}
