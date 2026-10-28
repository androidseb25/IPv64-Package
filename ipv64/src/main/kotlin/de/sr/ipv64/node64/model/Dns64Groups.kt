package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IsoInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Kind of a group member. */
@Serializable
public enum class DNS64MemberKind(override val value: String) : ApiValue {
    @SerialName("mac") MAC("mac"),
    @SerialName("ip") IP("ip"),
    @SerialName("net") NET("net"),
}

/** Blocklist mode of a group. */
@Serializable
public enum class DNS64ListsMode(override val value: String) : ApiValue {
    @SerialName("global") GLOBAL("global"),
    @SerialName("custom") CUSTOM("custom"),
}

/** Blocking category. */
@Serializable
public enum class DNS64Category(override val value: String) : ApiValue {
    @SerialName("social") SOCIAL("social"),
    @SerialName("games") GAMES("games"),
    @SerialName("streaming") STREAMING("streaming"),
    @SerialName("gambling") GAMBLING("gambling"),
    @SerialName("adult") ADULT("adult"),
}

/** YouTube restriction level. */
@Serializable
public enum class DNS64YoutubeMode(override val value: String) : ApiValue {
    @SerialName("strict") STRICT("strict"),
    @SerialName("moderate") MODERATE("moderate"),
    @SerialName("off") OFF("off"),
}

/** Action of a group domain rule. */
@Serializable
public enum class DNS64DomainAction(override val value: String) : ApiValue {
    @SerialName("allow") ALLOW("allow"),
    @SerialName("block") BLOCK("block"),
}

/** Action of a schedule. */
@Serializable
public enum class DNS64ScheduleAction(override val value: String) : ApiValue {
    /** Internet off. */
    @SerialName("off") OFF("off"),
    /** Block categories. */
    @SerialName("cats") CATS("cats"),
}

/** Mode of the group hold button. */
@Serializable
public enum class DNS64HoldMode(override val value: String) : ApiValue {
    /** Internet off. */
    @SerialName("block") BLOCK("block"),
    /** Suspend schedules and categories. */
    @SerialName("free") FREE("free"),
    /** Release the hold (empty mode). */
    @SerialName("") NONE(""),
}

/** Member (device or network) of a group. */
@Serializable
public data class DNS64GroupMember(
    val id: Long? = null,
    @SerialName("group_id") val groupId: Long? = null,
    /** mac, ip or net. */
    val kind: String? = null,
    val value: String? = null,
    val label: String? = null,
)

/** Request body of `POST /dns64/groups/{id}/members`. */
@Serializable
public data class DNS64GroupMemberRequest(
    val kind: DNS64MemberKind,
    /** MAC address, IP address or network. */
    val value: String,
    val label: String? = null,
)

/** Domain rule (allow/block) of a group, including subdomains. */
@Serializable
public data class DNS64GroupDomain(
    val id: Long? = null,
    @SerialName("group_id") val groupId: Long? = null,
    val domain: String? = null,
    /** allow or block. */
    val action: String? = null,
)

/** Request body of `POST /dns64/groups/{id}/domains`. */
@Serializable
public data class DNS64GroupDomainRequest(
    val domain: String,
    val action: DNS64DomainAction,
)

/** Time schedule of a group. */
@Serializable
public data class DNS64Schedule(
    val id: Long? = null,
    @SerialName("group_id") val groupId: Long? = null,
    val label: String? = null,
    /** Days of the week, 1 = Monday. */
    val days: List<Int> = emptyList(),
    /** Start time, e.g. `21:00`. */
    val start: String? = null,
    /** End time, e.g. `07:00`. */
    val end: String? = null,
    /** off or cats. */
    val action: String? = null,
    val categories: List<String> = emptyList(),
    val enabled: Boolean? = null,
)

/** Request body of `POST /dns64/groups/{id}/schedules`. */
@Serializable
public data class DNS64ScheduleRequest(
    /** Days of the week, 1 = Monday. */
    val days: List<Int>,
    val start: String,
    val end: String,
    val action: DNS64ScheduleAction,
    val label: String? = null,
    val categories: List<DNS64Category>? = null,
    val enabled: Boolean? = null,
)

/** Current state of a group (GET only). */
@Serializable
public data class DNS64GroupState(
    @SerialName("internet_off") val internetOff: Boolean? = null,
    val free: Boolean? = null,
    @SerialName("by_button") val byButton: Boolean? = null,
    @SerialName("until_released") val untilReleased: Boolean? = null,
    val until: IsoInstant? = null,
    val schedule: String? = null,
)

/** Device group. */
@Serializable
public data class DNS64Group(
    val id: Long? = null,
    val name: String? = null,
    /** Order (precedence). */
    val position: Int? = null,
    val members: List<DNS64GroupMember> = emptyList(),
    /** global or custom. */
    @SerialName("lists_mode") val listsMode: String? = null,
    /** Blocklist URLs for `custom`. */
    val lists: List<String> = emptyList(),
    /** Blocked categories (social, games, streaming, gambling, adult). */
    val categories: List<String> = emptyList(),
    val domains: List<DNS64GroupDomain> = emptyList(),
    val safesearch: Boolean? = null,
    /** strict, moderate or off. */
    val youtube: String? = null,
    val bypass: Boolean? = null,
    /** Hold: empty, block or free. */
    val hold: String? = null,
    /** RFC 3339; empty = until released. */
    @SerialName("hold_until") val holdUntil: String? = null,
    val schedules: List<DNS64Schedule> = emptyList(),
    val state: DNS64GroupState? = null,
    /** Cluster-wide identifier. */
    val uid: String? = null,
)

/** Request body of `PUT /dns64/groups/{id}/rules`. */
@Serializable
public data class DNS64GroupRulesRequest(
    @SerialName("lists_mode") val listsMode: DNS64ListsMode? = null,
    /** Blocklist URLs (for `custom`). */
    val lists: List<String>? = null,
    val categories: List<DNS64Category>? = null,
    val safesearch: Boolean? = null,
    val youtube: DNS64YoutubeMode? = null,
    val bypass: Boolean? = null,
)

/** Request body of `POST /dns64/groups/{id}/hold`. */
@Serializable
public data class DNS64GroupHoldRequest(
    val mode: DNS64HoldMode,
    val minutes: Int? = null,
    @SerialName("until_morning") val untilMorning: Boolean? = null,
)

/** Request body of `POST /dns64/groups` and `PUT /dns64/groups/{id}`. */
@Serializable
public data class DNS64GroupNameRequest(val name: String)

/** Request body of `POST /dns64/groups/{id}/move`. */
@Serializable
public data class DNS64GroupMoveRequest(val up: Boolean)
