package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.model.Monitor64Effect
import de.sr.ipv64.node64.model.Monitor64Ok
import de.sr.ipv64.node64.model.Monitor64OverrideMoveInput
import de.sr.ipv64.node64.model.Monitor64Uid
import de.sr.ipv64.node64.model.Monitor64RuleOverrideInput
import de.sr.ipv64.node64.model.Monitor64RulePreviewInput
import de.sr.ipv64.node64.model.Monitor64Rules
import de.sr.ipv64.node64.model.Monitor64RulesStdInput
import de.sr.ipv64.node64.model.Monitor64RulesStdResult

/** MONITOR64 thresholds: default table and overrides per group/tag (`/monitor64/rules`). */
public class Monitor64RulesApi internal constructor(private val transport: HttpTransport) {

    /** Rule kinds, defaults, overrides with effect and thresholds set at hosts. `GET /monitor64/rules` */
    public suspend fun get(): Monitor64Rules =
        transport.json(Method.GET, ApiPath("monitor64", "rules"))

    /** Replaces the changes to the built-in default table. `PUT /monitor64/rules/std` */
    public suspend fun setStd(input: Monitor64RulesStdInput): Monitor64RulesStdResult =
        transport.json(Method.PUT, ApiPath("monitor64", "rules", "std"), body = transport.body(input))

    /** Effect of a new or changed override; nothing is stored. `POST /monitor64/rules/preview` */
    public suspend fun preview(input: Monitor64RulePreviewInput): Monitor64Effect =
        transport.json(Method.POST, ApiPath("monitor64", "rules", "preview"), body = transport.body(input))

    /** Creates an override (appended at the bottom). `POST /monitor64/rules/overrides` */
    public suspend fun createOverride(input: Monitor64RuleOverrideInput): Monitor64Uid =
        transport.json(Method.POST, ApiPath("monitor64", "rules", "overrides"), body = transport.body(input))

    /** Changes an override; the position stays. `PUT /monitor64/rules/overrides/{uid}` */
    public suspend fun updateOverride(uid: String, input: Monitor64RuleOverrideInput): Monitor64Uid =
        transport.json(Method.PUT, ApiPath("monitor64", "rules", "overrides", uid), body = transport.body(input))

    /** Removes an override. `DELETE /monitor64/rules/overrides/{uid}` */
    public suspend fun deleteOverride(uid: String): Monitor64Ok =
        transport.json(Method.DELETE, ApiPath("monitor64", "rules", "overrides", uid))

    /** Moves an override one place up ([dir] = -1) or down (1); top wins. `POST /monitor64/rules/overrides/{uid}/move` */
    public suspend fun moveOverride(uid: String, dir: Int): Monitor64Ok =
        transport.json(
            Method.POST,
            ApiPath("monitor64", "rules", "overrides", uid, "move"),
            body = transport.body(Monitor64OverrideMoveInput(dir)),
        )
}
