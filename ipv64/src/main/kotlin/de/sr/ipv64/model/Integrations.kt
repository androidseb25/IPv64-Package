package de.sr.ipv64.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** Type of a notification integration. */
@Serializable
public enum class IntegrationType(override val value: String) : ApiValue {
    @SerialName("account_email") ACCOUNT_EMAIL("account_email"),
    @SerialName("telegram") TELEGRAM("telegram"),
    @SerialName("gotify") GOTIFY("gotify"),
    @SerialName("ntfy") NTFY("ntfy"),
    @SerialName("pushover") PUSHOVER("pushover"),
    @SerialName("discord") DISCORD("discord"),
    @SerialName("webhook") WEBHOOK("webhook"),
    @SerialName("email") EMAIL("email"),
    @SerialName("sms") SMS("sms"),
    @SerialName("app") APP("app"),
}

/** Verification status of an integration. */
@Serializable
public enum class IntegrationStatus(override val value: String) : ApiValue {
    @SerialName("active") ACTIVE("active"),
    /** E-mail and SMS must be confirmed first. */
    @SerialName("pending_verification") PENDING_VERIFICATION("pending_verification"),
}

/** A notification integration. */
@Serializable
public data class Integration(
    /** ID, 0 = account e-mail. */
    val id: Long? = null,
    /** Type. */
    val type: IntegrationType? = null,
    /** Name. */
    val name: String? = null,
    /** Status. */
    val status: IntegrationStatus? = null,
    /** Preselected for new healthchecks. */
    val default: Boolean? = null,
    /** Type specific settings. Secrets are shortened unless requested with `includeSecrets`; redacted in [toString]. */
    val options: JsonObject? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Last use. */
    @SerialName("last_used_at") val lastUsedAt: IsoInstant? = null,
    /** Number of messages sent. */
    val uses: Long? = null,
) {
    /** [options] may contain tokens and URLs in full (`includeSecrets`), so they are redacted here. */
    override fun toString(): String =
        "Integration(id=$id, type=$type, name=$name, status=$status, default=$default, options=<redacted>, " +
            "createdAt=$createdAt, lastUsedAt=$lastUsedAt, uses=$uses)"
}

/**
 * Request body of `POST /integrations`. Required fields depend on [type]:
 * telegram: chatId, apiToken; gotify: serverUrl, appToken, priority (0-10);
 * ntfy: serverUrl incl. topic, priorityDown/priorityUp (1-5);
 * pushover: userToken, appToken, device, priority (-2 to 1);
 * discord: webhookUrl, pingUser, pingGroup; webhook: urlDown and/or urlUp;
 * email: email (confirmation link is sent); sms: number, countryCode `0049`; app: deviceToken.
 * Secret fields are redacted in [toString].
 */
@Serializable
public data class CreateIntegrationRequest(
    /** Type. */
    val type: IntegrationType,
    /** Name. */
    val name: String,
    /** Preselect for new healthchecks. */
    val default: Boolean? = null,
    /** telegram: chat ID. */
    @SerialName("chat_id") val chatId: String? = null,
    /** telegram: bot token (secret). */
    @SerialName("api_token") val apiToken: String? = null,
    /** gotify, ntfy: server URL. */
    @SerialName("server_url") val serverUrl: String? = null,
    /** gotify, pushover: app token (secret). */
    @SerialName("app_token") val appToken: String? = null,
    /** pushover: user token (secret). */
    @SerialName("user_token") val userToken: String? = null,
    /** pushover: device. */
    val device: String? = null,
    /** gotify (0 to 10), pushover (-2 to 1): priority. */
    val priority: Int? = null,
    /** ntfy: priority on DOWN (1 to 5). */
    @SerialName("priority_down") val priorityDown: Int? = null,
    /** ntfy: priority on UP (1 to 5). */
    @SerialName("priority_up") val priorityUp: Int? = null,
    /** discord: webhook URL (secret). */
    @SerialName("webhook_url") val webhookUrl: String? = null,
    /** discord: user ID to ping. */
    @SerialName("ping_user") val pingUser: String? = null,
    /** discord: role ID to ping. */
    @SerialName("ping_group") val pingGroup: String? = null,
    /** webhook: URL called on DOWN. */
    @SerialName("url_down") val urlDown: String? = null,
    /** webhook: URL called on UP. */
    @SerialName("url_up") val urlUp: String? = null,
    /** email: address. */
    val email: String? = null,
    /** sms: German mobile number. */
    val number: String? = null,
    /** sms: country code, `0049`. */
    @SerialName("country_code") val countryCode: String? = null,
    /** app: device token of the IPv64 app (secret). */
    @SerialName("device_token") val deviceToken: String? = null,
) {
    override fun toString(): String =
        "CreateIntegrationRequest(type=$type, name=$name, default=$default, secrets=<redacted>)"
}

/** Request body of `PATCH /integrations/{id}`; `null` fields stay unchanged. */
@Serializable
public data class UpdateIntegrationRequest(
    /** Name. */
    val name: String? = null,
    /** Preselect for new healthchecks. */
    val default: Boolean? = null,
)
