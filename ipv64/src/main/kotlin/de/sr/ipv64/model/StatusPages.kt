package de.sr.ipv64.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A public status page. */
@Serializable
public data class StatusPage(
    /** ID. */
    val id: Long? = null,
    /** Title. */
    val name: String? = null,
    /** Address part behind `/status/`. */
    val slug: String? = null,
    /** Public URL. */
    val url: String? = null,
    /** Description. */
    val description: String? = null,
    /** Protected by a password. */
    @SerialName("password_protected") val passwordProtected: Boolean? = null,
    /** Design 1 to 4. */
    val design: Int? = null,
    /** Page reloads automatically. */
    val autorefresh: Boolean? = null,
    /** Tokens of the displayed healthchecks, in display order. */
    val healthchecks: List<String> = emptyList(),
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Time of the last view. */
    @SerialName("last_viewed_at") val lastViewedAt: IsoInstant? = null,
)

/** Body of `POST /status-pages`. */
@Serializable
public data class CreateStatusPageRequest(
    /** Title. */
    val name: String? = null,
    /** Description. */
    val description: String? = null,
    /** Address, 3 to 128 characters a-z, A-Z, 0-9; random if omitted. */
    val slug: String? = null,
    /** Password; empty = public. */
    val password: String? = null,
    /** Design 1 to 4. */
    val design: Int? = null,
    /** Reload automatically. */
    val autorefresh: Boolean? = null,
    /** Tokens of the displayed healthchecks. */
    val healthchecks: List<String>? = null,) {
    override fun toString(): String =
        "CreateStatusPageRequest(name=$name, description=$description, slug=$slug, password=${password?.let { "<redacted>" }}, " +
            "design=$design, autorefresh=$autorefresh, healthchecks=$healthchecks)"
}

/** Body of `PATCH /status-pages/{id}`; `null` fields stay unchanged. */
@Serializable
public data class UpdateStatusPageRequest(
    /** Title. */
    val name: String? = null,
    /** Description. */
    val description: String? = null,
    /** Address, 3 to 128 characters a-z, A-Z, 0-9. */
    val slug: String? = null,
    /** Password; empty = public. */
    val password: String? = null,
    /** Design 1 to 4. */
    val design: Int? = null,
    /** Reload automatically. */
    val autorefresh: Boolean? = null,
    /** Tokens of the displayed healthchecks. */
    val healthchecks: List<String>? = null,) {
    override fun toString(): String =
        "UpdateStatusPageRequest(name=$name, description=$description, slug=$slug, password=${password?.let { "<redacted>" }}, " +
            "design=$design, autorefresh=$autorefresh, healthchecks=$healthchecks)"
}
