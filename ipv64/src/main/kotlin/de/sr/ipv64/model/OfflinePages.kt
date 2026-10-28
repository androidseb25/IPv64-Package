package de.sr.ipv64.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** An offline page shown while a failover rule points to it. */
@Serializable
public data class OfflinePage(
    /** Offline page ID (16 characters). */
    val id: String? = null,
    /** Heading. */
    val name: String? = null,
    /** Domain the page appears under. */
    val domain: String? = null,
    /** Contact e-mail. */
    val email: String? = null,
    /** Phone number. */
    val phone: String? = null,
    /** Alternative address. */
    @SerialName("alternative_url") val alternativeUrl: String? = null,
    /** Text as HTML. */
    val text: String? = null,
    /** Logo URL. */
    @SerialName("icon_url") val iconUrl: String? = null,
    /** Number of views. */
    val views: Long? = null,
    /** Failover policy using this page. */
    @SerialName("in_use_by_policy") val inUseByPolicy: String? = null,
    /** Currently being served. */
    val active: Boolean? = null,
    /** Preview URL. */
    @SerialName("preview_url") val previewUrl: String? = null,
)

/** Body of `POST /offline-pages`. */
@Serializable
public data class CreateOfflinePageRequest(
    /** Heading. */
    val name: String? = null,
    /** Domain the page applies to. */
    val domain: String? = null,
    /** Contact e-mail. */
    val email: String? = null,
    /** Phone number, digits with an optional leading `+`. */
    val phone: String? = null,
    /** Alternative address. */
    @SerialName("alternative_url") val alternativeUrl: String? = null,
    /** Text; line breaks are kept, HTML is escaped. */
    val text: String? = null,
)

/** Body of `PATCH /offline-pages/{id}`; `null` fields stay unchanged. */
@Serializable
public data class UpdateOfflinePageRequest(
    /** Heading. */
    val name: String? = null,
    /** Domain the page applies to. */
    val domain: String? = null,
    /** Contact e-mail. */
    val email: String? = null,
    /** Phone number, digits with an optional leading `+`. */
    val phone: String? = null,
    /** Alternative address. */
    @SerialName("alternative_url") val alternativeUrl: String? = null,
    /** Text; line breaks are kept, HTML is escaped. */
    val text: String? = null,
)
