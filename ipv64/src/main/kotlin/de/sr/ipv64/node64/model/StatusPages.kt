package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IsoInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Layout of a status page. */
@Serializable
public enum class StatusPageLayout(override val value: String) : ApiValue {
    @SerialName("list") LIST("list"),
    @SerialName("tiles") TILES("tiles"),
    @SerialName("compact") COMPACT("compact"),
}

/** Color theme of a status page. */
@Serializable
public enum class StatusPageTheme(override val value: String) : ApiValue {
    @SerialName("auto") AUTO("auto"),
    @SerialName("light") LIGHT("light"),
    @SerialName("dark") DARK("dark"),
}

/** Level of a status notice. */
@Serializable
public enum class StatusNoticeLevel(override val value: String) : ApiValue {
    @SerialName("info") INFO("info"),
    @SerialName("warning") WARNING("warning"),
    @SerialName("danger") DANGER("danger"),
}

/** A status page. The password is never returned (see [hasPassword]). */
@Serializable
public data class StatusPage(
    /** ID. */
    val id: Long? = null,
    /** Slug, the page is served under `/status/<slug>`. */
    val slug: String? = null,
    /** Title. */
    val title: String? = null,
    /** Description (simple Markdown). */
    val description: String? = null,
    /** Footer (simple Markdown). */
    val footer: String? = null,
    /** Page is reachable. */
    val enabled: Boolean? = null,
    /** Published directly on the node port. */
    @SerialName("publish_node") val publishNode: Boolean? = null,
    /** Layout. */
    val layout: StatusPageLayout? = null,
    /** Theme. */
    val theme: StatusPageTheme? = null,
    /** Language (`de`, `en`, empty = browser language). */
    val lang: String? = null,
    /** Accent color `#rrggbb`. */
    val accent: String? = null,
    /** Days shown in the bars (30, 60, 90). */
    val days: Int? = null,
    /** Show uptime. */
    @SerialName("show_uptime") val showUptime: Boolean? = null,
    /** Show incidents and notices of the last 30 days. */
    @SerialName("show_history") val showHistory: Boolean? = null,
    /** Refresh interval in seconds (30 to 3600). */
    @SerialName("refresh_sec") val refreshSec: Int? = null,
    /** Page is password protected. */
    @SerialName("has_password") val hasPassword: Boolean? = null,
    /** Hide from search engines. */
    val noindex: Boolean? = null,
    /** Show "powered by Node64". */
    @SerialName("powered_by") val poweredBy: Boolean? = null,
    /** Groups with entries. */
    val groups: List<StatusGroup> = emptyList(),
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Last change. */
    @SerialName("updated_at") val updatedAt: IsoInstant? = null,
    /** Entries use the healthcheck groups. */
    @SerialName("hc_groups") val hcGroups: Boolean? = null,
    /** A custom logo is uploaded. */
    @SerialName("has_logo") val hasLogo: Boolean? = null,
    /** Last change of the logo. */
    @SerialName("logo_updated") val logoUpdated: IsoInstant? = null,
)

/** A group of a status page. */
@Serializable
public data class StatusGroup(
    /** ID. */
    val id: Long? = null,
    /** Heading; empty = none. */
    val name: String? = null,
    /** Entries. */
    val items: List<StatusItem> = emptyList(),
)

/** An entry of a status page group. */
@Serializable
public data class StatusItem(
    /** ID. */
    val id: Long? = null,
    /** Check token. */
    val token: String? = null,
    /** Display name; empty = name of the check. */
    val label: String? = null,
)

/** Group in a status page request. */
@Serializable
public data class StatusGroupRequest(
    /** Heading (max. 100 characters); empty = none. */
    val name: String? = null,
    /** Entries. */
    val items: List<StatusItemRequest> = emptyList(),
)

/** Entry in a status page request. */
@Serializable
public data class StatusItemRequest(
    /** Check token. */
    val token: String,
    /** Display name (max. 120 characters); empty = name of the check. */
    val label: String? = null,
)

/**
 * Body of `POST /status-pages`. Missing fields get defaults (layout list, theme auto, days 90, refresh 60 s;
 * publishNode, showUptime, showHistory, noindex and poweredBy on; enabled and hcGroups off).
 * [password] is redacted in [toString].
 */
@Serializable
public data class StatusPageRequest(
    /** Title (max. 120 characters). */
    val title: String,
    /** Slug (`^[a-z0-9][a-z0-9-]{1,62}$`); empty = derived from the title. */
    val slug: String? = null,
    /** Description (simple Markdown, max. 5000 characters). */
    val description: String? = null,
    /** Footer (simple Markdown, max. 2000 characters). */
    val footer: String? = null,
    /** Page is reachable. */
    val enabled: Boolean? = null,
    /** Publish directly on the node port. */
    @SerialName("publish_node") val publishNode: Boolean? = null,
    /** Layout. */
    val layout: StatusPageLayout? = null,
    /** Theme. */
    val theme: StatusPageTheme? = null,
    /** Language (`de`, `en` or empty). */
    val lang: String? = null,
    /** Accent color `#rrggbb`. */
    val accent: String? = null,
    /** Days shown (30, 60, 90). */
    val days: Int? = null,
    /** Show uptime. */
    @SerialName("show_uptime") val showUptime: Boolean? = null,
    /** Show incidents and notices of the last 30 days. */
    @SerialName("show_history") val showHistory: Boolean? = null,
    /** Refresh interval in seconds (30 to 3600). */
    @SerialName("refresh_sec") val refreshSec: Int? = null,
    /** Password in plain text (4 to 72 characters). */
    val password: String? = null,
    /** Hide from search engines. */
    val noindex: Boolean? = null,
    /** Show "powered by Node64". */
    @SerialName("powered_by") val poweredBy: Boolean? = null,
    /** Groups (max. 50 groups, 1000 entries). */
    val groups: List<StatusGroupRequest>? = null,
    /** Entries use the healthcheck groups. */
    @SerialName("hc_groups") val hcGroups: Boolean? = null,
) {
    override fun toString(): String =
        "StatusPageRequest(title=$title, slug=$slug, enabled=$enabled, layout=$layout, theme=$theme, " +
            "password=<redacted>, groups=$groups)"
}

/**
 * Body of `PUT /status-pages/{id}` (partial change: only sent fields change). `groups` replaces all groups and
 * entries. [password]: new password, `"-"` removes it, null/empty = unchanged; redacted in [toString].
 */
@Serializable
public data class StatusPageUpdateRequest(
    /** Title (max. 120 characters). */
    val title: String? = null,
    /** Slug. */
    val slug: String? = null,
    /** Description (simple Markdown). */
    val description: String? = null,
    /** Footer (simple Markdown). */
    val footer: String? = null,
    /** Page is reachable. */
    val enabled: Boolean? = null,
    /** Publish directly on the node port. */
    @SerialName("publish_node") val publishNode: Boolean? = null,
    /** Layout. */
    val layout: StatusPageLayout? = null,
    /** Theme. */
    val theme: StatusPageTheme? = null,
    /** Language (`de`, `en` or empty). */
    val lang: String? = null,
    /** Accent color `#rrggbb`. */
    val accent: String? = null,
    /** Days shown (30, 60, 90). */
    val days: Int? = null,
    /** Show uptime. */
    @SerialName("show_uptime") val showUptime: Boolean? = null,
    /** Show incidents and notices of the last 30 days. */
    @SerialName("show_history") val showHistory: Boolean? = null,
    /** Refresh interval in seconds (30 to 3600). */
    @SerialName("refresh_sec") val refreshSec: Int? = null,
    /** New password (4 to 72 characters), `"-"` removes it. */
    val password: String? = null,
    /** Hide from search engines. */
    val noindex: Boolean? = null,
    /** Show "powered by Node64". */
    @SerialName("powered_by") val poweredBy: Boolean? = null,
    /** Replaces all groups and entries (their IDs change). */
    val groups: List<StatusGroupRequest>? = null,
    /** Entries use the healthcheck groups. */
    @SerialName("hc_groups") val hcGroups: Boolean? = null,
) {
    override fun toString(): String =
        "StatusPageUpdateRequest(title=$title, slug=$slug, enabled=$enabled, layout=$layout, theme=$theme, " +
            "password=<redacted>, groups=$groups)"
}

/** An announcement on a status page. */
@Serializable
public data class StatusNotice(
    /** ID. */
    val id: Long? = null,
    /** Status page ID. */
    @SerialName("page_id") val pageId: Long? = null,
    /** Title. */
    val title: String? = null,
    /** Level. */
    val level: StatusNoticeLevel? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Last change. */
    @SerialName("updated_at") val updatedAt: IsoInstant? = null,
    /** Resolved at; missing while open. */
    @SerialName("resolved_at") val resolvedAt: IsoInstant? = null,
    /** Timeline updates. */
    val updates: List<StatusNoticeUpdate> = emptyList(),
)

/** A timeline entry of a [StatusNotice]. */
@Serializable
public data class StatusNoticeUpdate(
    /** ID. */
    val id: Long? = null,
    /** Time. */
    val time: IsoInstant? = null,
    /** Text (simple Markdown). */
    val text: String? = null,
)

/** Body of `POST /status-pages/{id}/notices`. */
@Serializable
public data class StatusNoticeInput(
    /** Title (max. 200 characters). */
    val title: String,
    /** Level (default info). */
    val level: StatusNoticeLevel? = null,
    /** First timeline entry (simple Markdown, max. 5000 characters). */
    val text: String? = null,
)

/** Body of `PUT /status-pages/{id}/notices/{nid}`. */
@Serializable
public data class StatusNoticeUpdateInput(
    /** New level; null = unchanged. */
    val level: StatusNoticeLevel? = null,
    /** Update text (max. 5000 characters); null = none. */
    val text: String? = null,
    /** Mark the notice as resolved. */
    val resolve: Boolean? = null,
)

/** Error body of the status page endpoints. */
@Serializable
public data class StatusError(
    /** Error message. */
    val error: String? = null,
    /** Affected field. */
    val field: String? = null,
    /** i18n key, e.g. `hc.status.err.slug`. */
    val key: String? = null,
)
