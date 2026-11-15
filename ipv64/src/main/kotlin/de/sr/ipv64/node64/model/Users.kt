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
    /** Effective role (highest of own role and groups). */
    val role: UserRole? = null,
    /** Own role. */
    @SerialName("own_role") val ownRole: UserRole? = null,
    /** Origin of the account. */
    val source: UserSource? = null,
    /** Locked: no sign-in; sessions and API keys are invalid. */
    val disabled: Boolean? = null,
    /** E-mail address. */
    val email: String? = null,
    /** Groups assigned by hand (uid). */
    val groups: List<String> = emptyList(),
    /** Groups determined via AD/LDAP or SSO (uid). */
    @SerialName("ext_groups") val extGroups: List<String> = emptyList(),
    /** Must set an own password at the next sign-in. */
    @SerialName("must_change") val mustChange: Boolean? = null,
    /** One-time password; only in the response of `POST /users` (secret, redacted in [toString]). */
    val password: String? = null,
) {
    override fun toString(): String =
        "NodeUser(id=$id, uid=$uid, username=$username, lang=$lang, timezone=$timezone, totp=$totp, isSelf=$isSelf, " +
            "createdAt=$createdAt, lastLoginAt=$lastLoginAt, role=$role, ownRole=$ownRole, source=$source, " +
            "disabled=$disabled, email=$email, groups=$groups, extGroups=$extGroups, mustChange=$mustChange, " +
            "password=${if (password == null) "null" else "<redacted>"})"
}

/** Role of a user or group. */
@Serializable
public enum class UserRole(override val value: String) : ApiValue {
    @SerialName("admin") ADMIN("admin"),
    @SerialName("read") READ("read"),
}

/** Origin of a user account. */
@Serializable
public enum class UserSource(override val value: String) : ApiValue {
    @SerialName("local") LOCAL("local"),
    @SerialName("ldap") LDAP("ldap"),
    @SerialName("oidc") OIDC("oidc"),
}

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

/** Body of `POST /users`. [password] is redacted in [toString]. */
@Serializable
public data class CreateUserRequest(
    /** User name (3-64 characters: letters, digits, dot, minus, underscore). */
    val username: String,
    /** Password (secret, at least 10 characters). */
    val password: String? = null,
    /** Must set an own password at the first sign-in; without [password] the node returns a one-time password. */
    @SerialName("must_change") val mustChange: Boolean? = null,
    /** Role; default `read`. */
    val role: UserRole? = null,
    /** E-mail address. */
    val email: String? = null,
    /** Groups (uid). */
    val groups: List<String>? = null,
) {
    override fun toString(): String =
        "CreateUserRequest(username=$username, password=<redacted>, mustChange=$mustChange, role=$role, email=$email, groups=$groups)"
}

/** Body of `PUT /users/{id}`; missing fields stay. */
@Serializable
public data class UpdateUserRequest(
    /** Role. */
    val role: UserRole? = null,
    /** Groups (uid). */
    val groups: List<String>? = null,
    /** E-mail address. */
    val email: String? = null,
    /** Lock or unlock the user. */
    val disabled: Boolean? = null,
)

/** Body of `POST /users/{id}/password`. [password] is redacted in [toString]. */
@Serializable
public data class SetUserPasswordRequest(
    /** New password (secret). */
    val password: String? = null,
    /** Must set an own password at the next sign-in; without [password] the node generates a one-time password. */
    @SerialName("must_change") val mustChange: Boolean? = null,
) {
    override fun toString(): String = "SetUserPasswordRequest(password=<redacted>, mustChange=$mustChange)"
}

/** Response of `POST /users/{id}/password` with a generated one-time password. [password] is redacted in [toString]. */
@Serializable
public data class SetUserPasswordResult(
    /** One-time password (secret). */
    val password: String? = null,
    /** Must set an own password at the next sign-in. */
    @SerialName("must_change") val mustChange: Boolean? = null,
) {
    override fun toString(): String = "SetUserPasswordResult(password=<redacted>, mustChange=$mustChange)"
}
