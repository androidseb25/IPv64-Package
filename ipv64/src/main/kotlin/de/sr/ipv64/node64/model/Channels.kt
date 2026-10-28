package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IsoInstant
import de.sr.ipv64.node64.internal.LenientInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

/** Type of a notification channel. */
@Serializable
public enum class ChannelType(override val value: String) : ApiValue {
    @SerialName("browser") BROWSER("browser"),
    @SerialName("smtp") SMTP("smtp"),
    @SerialName("telegram") TELEGRAM("telegram"),
    @SerialName("discord") DISCORD("discord"),
    @SerialName("slack") SLACK("slack"),
    @SerialName("teams") TEAMS("teams"),
    @SerialName("matrix") MATRIX("matrix"),
    @SerialName("secntfy") SECNTFY("secntfy"),
    @SerialName("ntfy") NTFY("ntfy"),
    @SerialName("gotify") GOTIFY("gotify"),
    @SerialName("pushover") PUSHOVER("pushover"),
    @SerialName("homeassistant") HOMEASSISTANT("homeassistant"),
    @SerialName("webhook") WEBHOOK("webhook"),
    @SerialName("ipv64_mail") IPV64_MAIL("ipv64_mail"),
    @SerialName("ipv64_sms") IPV64_SMS("ipv64_sms"),
    @SerialName("app") APP("app"),
}

/** Where a channel sends its messages from. */
@Serializable
public enum class ChannelExecution(override val value: String) : ApiValue {
    @SerialName("node") NODE("node"),
    @SerialName("server") SERVER("server"),
}

/** Notification channel. [config] contains tokens and is redacted in [toString]. */
@Serializable
public data class Channel(
    /** Channel ID. */
    val id: Long? = null,
    /** Channel type. */
    val type: ChannelType? = null,
    /** Name. */
    val name: String? = null,
    /** Type specific settings; secrets are masked unless requested with `includeSecrets`. */
    val config: JsonObject? = null,
    /** Channel is switched on. */
    val enabled: Boolean? = null,
    /** Preselected for new checks. */
    val default: Boolean? = null,
    /** Language of the messages. */
    val lang: String? = null,
    /** Notify when a check goes down. */
    @SerialName("notify_down") val notifyDown: Boolean? = null,
    /** Notify when a check is up again. */
    @SerialName("notify_up") val notifyUp: Boolean? = null,
    /** Where messages are sent from. */
    val execution: ChannelExecution? = null,
    /** Revision (sync). */
    val revision: Long? = null,
    /** Last use; ISO 8601, `null` while unset. */
    @Serializable(with = LenientInstantSerializer::class) @SerialName("last_used_at") val lastUsedAt: Instant? = null,
    /** Last error. */
    @SerialName("last_error") val lastError: String? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Last change. */
    @SerialName("updated_at") val updatedAt: IsoInstant? = null,
) {
    override fun toString(): String =
        "Channel(id=$id, type=$type, name=$name, config=<redacted>, enabled=$enabled, default=$default, lang=$lang, " +
            "notifyDown=$notifyDown, notifyUp=$notifyUp, execution=$execution, revision=$revision, " +
            "lastUsedAt=$lastUsedAt, lastError=$lastError, createdAt=$createdAt, updatedAt=$updatedAt)"
}

/**
 * Body of `POST /channels`, e.g. telegram: `config = {"bot_token": "...", "chat_id": "123"}`.
 * [config] contains secrets and is redacted in [toString].
 */
@Serializable
public data class CreateChannelRequest(
    /** Channel type. */
    val type: ChannelType,
    /** Name. */
    val name: String,
    /** Type specific settings (see `GET /channel-types`). */
    val config: JsonObject? = null,
    /** Channel is switched on. */
    val enabled: Boolean? = null,
    /** Preselected for new checks. */
    val default: Boolean? = null,
    /** Language of the messages. */
    val lang: String? = null,
    /** Notify when a check goes down. */
    @SerialName("notify_down") val notifyDown: Boolean? = null,
    /** Notify when a check is up again. */
    @SerialName("notify_up") val notifyUp: Boolean? = null,
    /** Where messages are sent from. */
    val execution: ChannelExecution? = null,
) {
    override fun toString(): String =
        "CreateChannelRequest(type=$type, name=$name, config=<redacted>, enabled=$enabled, default=$default, " +
            "lang=$lang, notifyDown=$notifyDown, notifyUp=$notifyUp, execution=$execution)"
}

/**
 * Body of `PUT /channels/{id}`. Empty secrets in [config] keep the stored value.
 * [config] is redacted in [toString].
 */
@Serializable
public data class UpdateChannelRequest(
    /** Name. */
    val name: String? = null,
    /** Type specific settings. */
    val config: JsonObject? = null,
    /** Channel is switched on. */
    val enabled: Boolean? = null,
    /** Preselected for new checks. */
    val default: Boolean? = null,
    /** Language of the messages. */
    val lang: String? = null,
    /** Notify when a check goes down. */
    @SerialName("notify_down") val notifyDown: Boolean? = null,
    /** Notify when a check is up again. */
    @SerialName("notify_up") val notifyUp: Boolean? = null,
    /** Where messages are sent from. */
    val execution: ChannelExecution? = null,
) {
    override fun toString(): String =
        "UpdateChannelRequest(name=$name, config=<redacted>, enabled=$enabled, default=$default, lang=$lang, " +
            "notifyDown=$notifyDown, notifyUp=$notifyUp, execution=$execution)"
}

/** Response of `POST /channels/{id}/test`: `{"ok": true}`. */
@Serializable
public data class ChannelTestResult(
    /** The test message was sent. */
    val ok: Boolean? = null,
)

/** Entry of the delivery log. */
@Serializable
public data class Delivery(
    /** Entry ID. */
    val id: Long? = null,
    /** Time of the delivery. */
    val time: IsoInstant? = null,
    /** Event ID. */
    @SerialName("event_id") val eventId: String? = null,
    /** Channel ID. */
    @SerialName("channel_id") val channelId: Long? = null,
    /** Channel name. */
    val channel: String? = null,
    /** Kind of message. */
    val kind: String? = null,
    /** Token of the check. */
    @SerialName("check_token") val checkToken: String? = null,
    /** Subject of the message. */
    val subject: String? = null,
    /** Delivery succeeded. */
    val ok: Boolean? = null,
    /** Error message. */
    val error: String? = null,
)
