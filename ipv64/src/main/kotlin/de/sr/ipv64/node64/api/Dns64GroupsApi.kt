package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.model.*

/** Device groups of the DNS64 resolver. */
public class Dns64GroupsApi internal constructor(private val transport: HttpTransport) {

    /** Lists device groups with members. `GET /dns64/groups` */
    public suspend fun list(): List<DNS64Group> =
        transport.json(Method.GET, ApiPath("dns64", "groups"))

    /** Creates a group. `POST /dns64/groups` */
    public suspend fun create(name: String): DNS64Group =
        transport.json(Method.POST, ApiPath("dns64", "groups"), body = transport.body(DNS64GroupNameRequest(name)))

    /** Renames a group. `PUT /dns64/groups/{id}` */
    public suspend fun rename(id: Long, name: String) {
        transport.unit(Method.PUT, ApiPath("dns64", "groups", id.toString()), body = transport.body(DNS64GroupNameRequest(name)))
    }

    /** Deletes a group including its members. `DELETE /dns64/groups/{id}` */
    public suspend fun delete(id: Long) {
        transport.unit(Method.DELETE, ApiPath("dns64", "groups", id.toString()))
    }

    /** Moves a group up or down in the order (precedence). `POST /dns64/groups/{id}/move` */
    public suspend fun move(id: Long, up: Boolean) {
        transport.unit(Method.POST, ApiPath("dns64", "groups", id.toString(), "move"), body = transport.body(DNS64GroupMoveRequest(up)))
    }

    /** Assigns a device or network to a group (moves it out of another group). `POST /dns64/groups/{id}/members` */
    public suspend fun addMember(id: Long, request: DNS64GroupMemberRequest): DNS64GroupMember =
        transport.json(Method.POST, ApiPath("dns64", "groups", id.toString(), "members"), body = transport.body(request))

    /** Removes a member (the [id] is the member ID). `DELETE /dns64/groups/members/{id}` */
    public suspend fun removeMember(id: Long) {
        transport.unit(Method.DELETE, ApiPath("dns64", "groups", "members", id.toString()))
    }

    /** Sets the rules of a group: blocklists (global/custom) and categories. `PUT /dns64/groups/{id}/rules` */
    public suspend fun updateRules(id: Long, request: DNS64GroupRulesRequest) {
        transport.unit(Method.PUT, ApiPath("dns64", "groups", id.toString(), "rules"), body = transport.body(request))
    }

    /** Adds an allow or block rule for a domain (including subdomains) to a group. `POST /dns64/groups/{id}/domains` */
    public suspend fun addDomain(id: Long, request: DNS64GroupDomainRequest): DNS64GroupDomain =
        transport.json(Method.POST, ApiPath("dns64", "groups", id.toString(), "domains"), body = transport.body(request))

    /** Deletes a domain rule (the [id] is the domain rule ID). `DELETE /dns64/groups/domains/{id}` */
    public suspend fun removeDomain(id: Long) {
        transport.unit(Method.DELETE, ApiPath("dns64", "groups", "domains", id.toString()))
    }

    /**
     * Instant button: internet off (`block`), suspend schedules/categories (`free`) or release (empty mode).
     *
     * `POST /dns64/groups/{id}/hold`
     */
    public suspend fun hold(id: Long, request: DNS64GroupHoldRequest) {
        transport.unit(Method.POST, ApiPath("dns64", "groups", id.toString(), "hold"), body = transport.body(request))
    }

    /** Creates a schedule. `POST /dns64/groups/{id}/schedules` */
    public suspend fun addSchedule(id: Long, request: DNS64ScheduleRequest): DNS64Schedule =
        transport.json(Method.POST, ApiPath("dns64", "groups", id.toString(), "schedules"), body = transport.body(request))

    /** Enables a schedule (the [id] is the schedule ID). `POST /dns64/groups/schedules/{id}/enable` */
    public suspend fun enableSchedule(id: Long) {
        transport.unit(Method.POST, ApiPath("dns64", "groups", "schedules", id.toString(), "enable"))
    }

    /** Disables a schedule (the [id] is the schedule ID). `POST /dns64/groups/schedules/{id}/disable` */
    public suspend fun disableSchedule(id: Long) {
        transport.unit(Method.POST, ApiPath("dns64", "groups", "schedules", id.toString(), "disable"))
    }

    /** Deletes a schedule (the [id] is the schedule ID). `DELETE /dns64/groups/schedules/{id}` */
    public suspend fun deleteSchedule(id: Long) {
        transport.unit(Method.DELETE, ApiPath("dns64", "groups", "schedules", id.toString()))
    }
}
