package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** Type of a channel at ipv64. */
@Serializable
public enum class Ipv64ChannelType(override val value: String) : ApiValue {
    @SerialName("ipv64_mail") IPV64_MAIL("ipv64_mail"),
    @SerialName("ipv64_sms") IPV64_SMS("ipv64_sms"),
    @SerialName("app") APP("app"),
}

/** Channel at ipv64 (e-mail, SMS or app); may be unverified. */
@Serializable
public data class Ipv64Channel(
    /** Channel ID. */
    val id: Long? = null,
    /** Channel type. */
    val type: Ipv64ChannelType? = null,
    /** Name. */
    val name: String? = null,
    /** Channel is switched on. */
    val enabled: Boolean? = null,
    /** Preselected for new checks. */
    val default: Boolean? = null,
    /** Confirmed by code. */
    val verified: Boolean? = null,
    /** Language of the messages. */
    val lang: String? = null,
    /** Notify when a check goes down. */
    @SerialName("notify_down") val notifyDown: Boolean? = null,
    /** Notify when a check is up again. */
    @SerialName("notify_up") val notifyUp: Boolean? = null,
    /** Non-secret values (e-mail address or number). */
    val config: JsonObject? = null,
    /** Last error. */
    @SerialName("last_error") val lastError: String? = null,
)

/** Body of `POST /ipv64/channels`; [config] is e.g. `{"email": "..."}` or `{"number": "+49 ..."}`. */
@Serializable
public data class CreateIpv64ChannelRequest(
    /** Channel type (`ipv64_mail` or `ipv64_sms`). */
    val type: Ipv64ChannelType,
    /** Name, 1-100 characters. */
    val name: String,
    /** Language of the messages. */
    val lang: String? = null,
    /** Fields of the type; unknown fields are rejected with 422. */
    val config: JsonObject? = null,
    /** Channel is switched on. */
    val enabled: Boolean? = null,
    /** Preselected for new checks. */
    val default: Boolean? = null,
    /** Notify when a check goes down. */
    @SerialName("notify_down") val notifyDown: Boolean? = null,
    /** Notify when a check is up again. */
    @SerialName("notify_up") val notifyUp: Boolean? = null,
)

/** Body of `PUT /ipv64/channels/{id}`; missing fields stay. A new address or number makes the channel unverified. */
@Serializable
public data class UpdateIpv64ChannelRequest(
    /** Name, 1-100 characters. */
    val name: String? = null,
    /** Language of the messages. */
    val lang: String? = null,
    /** Fields of the type. */
    val config: JsonObject? = null,
    /** Channel is switched on. */
    val enabled: Boolean? = null,
    /** Preselected for new checks. */
    val default: Boolean? = null,
    /** Notify when a check goes down. */
    @SerialName("notify_down") val notifyDown: Boolean? = null,
    /** Notify when a check is up again. */
    @SerialName("notify_up") val notifyUp: Boolean? = null,
)

/** Body of `POST /ipv64/channels/{id}/verify`; [code] is redacted in [toString]. */
@Serializable
public data class VerifyIpv64ChannelRequest(
    /** Confirmation code. */
    val code: String,
) {
    override fun toString(): String = "VerifyIpv64ChannelRequest(code=<redacted>)"
}

/** Result of `POST /ipv64/channels/{id}/test`. */
@Serializable
public data class Ipv64ChannelTestResult(
    val ok: Boolean? = null,
)
