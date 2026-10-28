package de.sr.ipv64.node64.internal

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import okhttp3.RequestBody
import kotlin.time.Instant

/** Decodes the body as [T], or returns `null` when the (undocumented) success body is empty. */
internal suspend inline fun <reified T : Any> HttpTransport.jsonOptional(
    method: Method,
    path: ApiPath,
    query: Map<String, String> = emptyMap(),
    body: RequestBody? = null,
    headers: Map<String, String> = emptyMap(),
): T? {
    val text = text(method, path, query, body, headers)
    return if (text.isBlank()) null else decode(serializer<T>(), text)
}

/**
 * ISO 8601 timestamp for Node64 fields the spec only types as `string` (no `format: date-time`).
 * Such fields are typically sent as `""` while unset, so blank or unparseable values decode to `null`
 * instead of failing the whole response. Accepts the same formats as [de.sr.ipv64.model.IsoInstantSerializer].
 */
@OptIn(ExperimentalSerializationApi::class)
internal object LenientInstantSerializer : KSerializer<Instant?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("de.sr.ipv64.node64.LenientInstant", PrimitiveKind.STRING).nullable

    override fun serialize(encoder: Encoder, value: Instant?) {
        if (value == null) encoder.encodeNull() else encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant? {
        if (!decoder.decodeNotNullMark()) return decoder.decodeNull()
        val text = decoder.decodeString().trim()
        if (text.isEmpty()) return null
        val normalized = if (text.length == 19 && text[10] == ' ') text.replace(' ', 'T') + "Z" else text
        return runCatching { Instant.parse(normalized) }.getOrNull()
    }
}
