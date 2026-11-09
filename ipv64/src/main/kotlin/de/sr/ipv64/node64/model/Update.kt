package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Self-update mode. */
@Serializable
public enum class UpdateMode(override val value: String) : ApiValue {
    @SerialName("off") OFF("off"),
    @SerialName("notify") NOTIFY("notify"),
    @SerialName("auto") AUTO("auto"),
}

/** Self-update channel; [AUTO] withdraws the own choice. */
@Serializable
public enum class UpdateChannel(override val value: String) : ApiValue {
    @SerialName("stable") STABLE("stable"),
    @SerialName("preview") PREVIEW("preview"),
    @SerialName("auto") AUTO("auto"),
}

/** Body of `PUT /update/settings`; missing fields stay unchanged. */
@Serializable
public data class UpdateSettingsRequest(
    val mode: UpdateMode? = null,
    val channel: UpdateChannel? = null,
    /** Time window for automatic updates, e.g. `03:00-05:00`. */
    val window: String? = null,
)

/** Response of `POST /update/apply` (202). */
@Serializable
public data class UpdateApplyResult(
    /** `restarting`. */
    val status: String? = null,
    val version: String? = null,
)

/** Response of `POST /update/reset` (202). */
@Serializable
public data class UpdateResetResult(
    /** `restarting`. */
    val status: String? = null,
)
