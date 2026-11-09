package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IsoInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** User of this node (without password hash and secrets). */
@Serializable
public data class NodeUser(
    /** User ID. */
    val id: Long? = null,
    /** Identifier in the cluster. */
    val uid: String? = null,
    /** Username. */
    val username: String? = null,
    /** Language. */
    val lang: String? = null,
    /** Time zone (IANA). */
    val timezone: String? = null,
    /** Two-factor authentication is set up. */
    val totp: Boolean? = null,
    /** The signed-in user or owner of the API key. */
    @SerialName("self") val isSelf: Boolean? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Last login. */
    @SerialName("last_login_at") val lastLoginAt: IsoInstant? = null,
)

/** Language of a user. */
@Serializable
public enum class UserLang(override val value: String) : ApiValue {
    @SerialName("de") DE("de"),
    @SerialName("en") EN("en"),
}

/** Body of `PUT /users/me`; missing fields stay. */
@Serializable
public data class UpdateMeRequest(
    /** Language. */
    val lang: UserLang? = null,
    /** Time zone (IANA, e.g. `Europe/Berlin`); also applies to the node. */
    val timezone: String? = null,
)
