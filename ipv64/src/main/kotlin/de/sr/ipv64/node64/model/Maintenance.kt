package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IsoInstant
import de.sr.ipv64.node64.internal.LenientInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** Repetition of a maintenance window. */
@Serializable
public enum class MaintenanceRepeat(override val value: String) : ApiValue {
    @SerialName("none") NONE("none"),
    @SerialName("daily") DAILY("daily"),
    @SerialName("weekly") WEEKLY("weekly"),
    @SerialName("monthly") MONTHLY("monthly"),
}

/** A maintenance window. */
@Serializable
public data class Maintenance(
    /** ID. */
    val id: Long? = null,
    /** Name. */
    val name: String? = null,
    /** Start. */
    @Serializable(with = LenientInstantSerializer::class) @SerialName("start_at") val startAt: Instant? = null,
    /** End. */
    @Serializable(with = LenientInstantSerializer::class) @SerialName("end_at") val endAt: Instant? = null,
    /** Repetition. */
    val repeat: MaintenanceRepeat? = null,
    /** Repeat until. */
    @Serializable(with = LenientInstantSerializer::class) @SerialName("repeat_until") val repeatUntil: Instant? = null,
    /** Affected check tokens; empty = all. */
    val tokens: List<String> = emptyList(),
    /** Window is enabled. */
    val active: Boolean? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** ID at ipv64. */
    @SerialName("remote_id") val remoteId: Long? = null,
    /** Revision counter. */
    val revision: Long? = null,
)

/** Body of `POST /maintenance` and `PUT /maintenance/{id}` (the latter replaces the window completely). */
@Serializable
public data class MaintenanceInput(
    /** Name. */
    val name: String,
    /** Start. */
    @SerialName("start_at") val startAt: IsoInstant,
    /** End; after [startAt], at most 30 days later. */
    @SerialName("end_at") val endAt: IsoInstant,
    /** Repetition. */
    val repeat: MaintenanceRepeat? = null,
    /** Repeat until. */
    @SerialName("repeat_until") val repeatUntil: IsoInstant? = null,
    /** Affected check tokens; empty = all. */
    val tokens: List<String>? = null,
    /** Window is enabled (default true). */
    val active: Boolean? = null,
)
