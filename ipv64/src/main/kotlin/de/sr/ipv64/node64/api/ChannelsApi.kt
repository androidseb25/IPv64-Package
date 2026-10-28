package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.internal.query
import de.sr.ipv64.node64.model.Channel
import de.sr.ipv64.node64.model.ChannelTestResult
import de.sr.ipv64.node64.model.CreateChannelRequest
import de.sr.ipv64.node64.model.Delivery
import de.sr.ipv64.node64.model.UpdateChannelRequest
import kotlinx.serialization.json.JsonElement

/** Tag *Benachrichtigungen*: notification channels and deliveries. */
public class ChannelsApi internal constructor(private val transport: HttpTransport) {

    /**
     * Lists the channels.
     *
     * @param includeSecrets return secrets in [Channel.config] in full (spec type boolean, sent as `true`/`false`).
     *
     * `GET /channels`
     */
    public suspend fun list(includeSecrets: Boolean? = null): List<Channel> =
        transport.json(Method.GET, ApiPath("channels"), query("include_secrets" to includeSecrets))

    /** Creates a channel. `POST /channels` */
    public suspend fun create(request: CreateChannelRequest): Channel =
        transport.json(Method.POST, ApiPath("channels"), body = transport.body(request))

    /** A single channel. `GET /channels/{id}` */
    public suspend fun get(id: Long): Channel =
        transport.json(Method.GET, ApiPath("channels", id.toString()))

    /** Changes a channel; empty secrets keep the stored value. `PUT /channels/{id}` */
    public suspend fun update(id: Long, request: UpdateChannelRequest): Channel =
        transport.json(Method.PUT, ApiPath("channels", id.toString()), body = transport.body(request))

    /** Deletes a channel. `DELETE /channels/{id}` */
    public suspend fun delete(id: Long) {
        transport.unit(Method.DELETE, ApiPath("channels", id.toString()))
    }

    /** Sends a test message. `POST /channels/{id}/test` */
    public suspend fun test(id: Long): ChannelTestResult =
        transport.json(Method.POST, ApiPath("channels", id.toString(), "test"))

    /** Channel types with their fields. Response body is not documented by the API. `GET /channel-types` */
    public suspend fun channelTypes(): JsonElement? =
        transport.jsonOrNull(Method.GET, ApiPath("channel-types"))

    /**
     * Delivery log.
     *
     * @param limit maximum number of entries.
     * @param before only entries with an ID lower than this one (paging).
     * @param channel only deliveries of this channel ID.
     *
     * `GET /deliveries`
     */
    public suspend fun deliveries(limit: Int? = null, before: Long? = null, channel: Long? = null): List<Delivery> =
        transport.json(
            Method.GET,
            ApiPath("deliveries"),
            query("limit" to limit, "before" to before, "channel" to channel),
        )
}
