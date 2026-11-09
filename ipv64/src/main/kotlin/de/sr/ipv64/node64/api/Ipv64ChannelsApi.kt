package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.model.CreateIpv64ChannelRequest
import de.sr.ipv64.node64.model.Ipv64Channel
import de.sr.ipv64.node64.model.Ipv64ChannelTestResult
import de.sr.ipv64.node64.model.UpdateIpv64ChannelRequest
import de.sr.ipv64.node64.model.VerifyIpv64ChannelRequest

/** Tag *Benachrichtigungen*: e-mail, SMS and app channels at ipv64 (also unverified ones). */
public class Ipv64ChannelsApi internal constructor(private val transport: HttpTransport) {

    /** Lists the channels at ipv64. `GET /ipv64/channels` */
    public suspend fun list(): List<Ipv64Channel> =
        transport.json(Method.GET, ApiPath("ipv64", "channels"))

    /** Creates an e-mail or SMS channel; it starts unverified. `POST /ipv64/channels` */
    public suspend fun create(request: CreateIpv64ChannelRequest): Ipv64Channel =
        transport.json(Method.POST, ApiPath("ipv64", "channels"), body = transport.body(request))

    /** A single channel at ipv64. `GET /ipv64/channels/{id}` */
    public suspend fun get(id: Long): Ipv64Channel =
        transport.json(Method.GET, ApiPath("ipv64", "channels", id.toString()))

    /** Changes a channel at ipv64; missing fields stay. `PUT /ipv64/channels/{id}` */
    public suspend fun update(id: Long, request: UpdateIpv64ChannelRequest): Ipv64Channel =
        transport.json(Method.PUT, ApiPath("ipv64", "channels", id.toString()), body = transport.body(request))

    /** Deletes a channel at ipv64. `DELETE /ipv64/channels/{id}` */
    public suspend fun delete(id: Long) {
        transport.unit(Method.DELETE, ApiPath("ipv64", "channels", id.toString()))
    }

    /** Sends a test message (verified channels only). `POST /ipv64/channels/{id}/test` */
    public suspend fun test(id: Long): Ipv64ChannelTestResult =
        transport.json(Method.POST, ApiPath("ipv64", "channels", id.toString(), "test"))

    /** Sends the confirmation code to the address or number. `POST /ipv64/channels/{id}/verify/send` */
    public suspend fun sendVerification(id: Long) {
        transport.unit(Method.POST, ApiPath("ipv64", "channels", id.toString(), "verify", "send"))
    }

    /** Confirms the channel with the code. `POST /ipv64/channels/{id}/verify` */
    public suspend fun verify(id: Long, request: VerifyIpv64ChannelRequest): Ipv64Channel =
        transport.json(
            Method.POST,
            ApiPath("ipv64", "channels", id.toString(), "verify"),
            body = transport.body(request),
        )
}
