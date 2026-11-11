package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.model.Monitor64Notify
import de.sr.ipv64.node64.model.Monitor64NotifyConfig
import de.sr.ipv64.node64.model.Monitor64NotifyOverrideInput
import de.sr.ipv64.node64.model.Monitor64NotifyTestResult
import de.sr.ipv64.node64.model.Monitor64Ok
import de.sr.ipv64.node64.model.Monitor64OverrideMoveInput
import de.sr.ipv64.node64.model.Monitor64Uid

/** MONITOR64 notification: base setting and overrides per group/tag (`/monitor64/notify`). */
public class Monitor64NotifyApi internal constructor(private val transport: HttpTransport) {

    /** Base setting, channels of the node, overrides and quiet time. `GET /monitor64/notify` */
    public suspend fun get(): Monitor64Notify =
        transport.json(Method.GET, ApiPath("monitor64", "notify"))

    /** Saves the base setting. `PUT /monitor64/notify` */
    public suspend fun setConfig(config: Monitor64NotifyConfig): Monitor64Ok =
        transport.json(Method.PUT, ApiPath("monitor64", "notify"), body = transport.body(config))

    /** Sends a test message to all channels of the base setting. `POST /monitor64/notify/test` */
    public suspend fun test(): Monitor64NotifyTestResult =
        transport.json(Method.POST, ApiPath("monitor64", "notify", "test"))

    /** Creates an override (appended at the bottom). `POST /monitor64/notify/overrides` */
    public suspend fun createOverride(input: Monitor64NotifyOverrideInput): Monitor64Uid =
        transport.json(Method.POST, ApiPath("monitor64", "notify", "overrides"), body = transport.body(input))

    /** Changes an override; the position stays. `PUT /monitor64/notify/overrides/{uid}` */
    public suspend fun updateOverride(uid: String, input: Monitor64NotifyOverrideInput): Monitor64Uid =
        transport.json(Method.PUT, ApiPath("monitor64", "notify", "overrides", uid), body = transport.body(input))

    /** Removes an override. `DELETE /monitor64/notify/overrides/{uid}` */
    public suspend fun deleteOverride(uid: String): Monitor64Ok =
        transport.json(Method.DELETE, ApiPath("monitor64", "notify", "overrides", uid))

    /** Moves an override one place up ([dir] = -1) or down (1); top wins. `POST /monitor64/notify/overrides/{uid}/move` */
    public suspend fun moveOverride(uid: String, dir: Int): Monitor64Ok =
        transport.json(
            Method.POST,
            ApiPath("monitor64", "notify", "overrides", uid, "move"),
            body = transport.body(Monitor64OverrideMoveInput(dir)),
        )
}
