package de.sr.ipv64.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

/** Implemented by every enum of the SDK; [value] is the exact wire representation. */
public interface ApiValue {
    public val value: String
}

/** ISO-8601 timestamp as returned by the API (e.g. `2026-10-10T12:00:00+02:00`). */
public typealias IsoInstant = @Serializable(with = IsoInstantSerializer::class) Instant

/** Serializes [Instant] as ISO-8601 string and accepts offsets as well as `yyyy-MM-dd HH:mm:ss` (UTC). */
public object IsoInstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("de.sr.ipv64.IsoInstant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        val text = decoder.decodeString().trim()
        val normalized = if (text.length == 19 && text[10] == ' ') text.replace(' ', 'T') + "Z" else text
        return Instant.parse(normalized)
    }
}

/** Error document according to RFC 9457 (`application/problem+json`). */
@Serializable
public data class Problem(
    /** Link to the error description. */
    val type: String? = null,
    /** HTTP status text. */
    val title: String? = null,
    /** HTTP status code. */
    val status: Int? = null,
    /** Human readable error message. */
    val detail: String? = null,
    /** Machine readable error code. */
    val code: String? = null,
    /** Affected input field, if any. */
    val field: String? = null,
    /** Seconds until the next attempt (429 and some 409 responses). */
    @SerialName("retry_after") val retryAfter: Int? = null,
)

/** Cursor based pagination info. */
@Serializable
public data class Hc2PageMeta(
    val limit: Int? = null,
    @SerialName("has_more") val hasMore: Boolean? = null,
    /** Pass as `before` to load the next older page. */
    @SerialName("next_before") val nextBefore: Long? = null,
    /** Pass as `after` to load the next newer page. */
    @SerialName("next_after") val nextAfter: Long? = null,
)

/** A page of results together with its pagination metadata. */
@Serializable
public data class Page<T>(
    val data: List<T>,
    val meta: Hc2PageMeta? = null,
)
