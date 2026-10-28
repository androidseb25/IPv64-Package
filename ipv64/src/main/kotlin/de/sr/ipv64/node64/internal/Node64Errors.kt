package de.sr.ipv64.node64.internal

import de.sr.ipv64.IPv64Exception
import de.sr.ipv64.Node64ApiException
import de.sr.ipv64.internal.IPv64Json
import de.sr.ipv64.internal.RawResponse
import kotlinx.serialization.Serializable

/** Error document of Node64 (`{"error": "…", "field": "…"}`), schema `Error`. */
@Serializable
internal class Node64Error(val error: String? = null, val field: String? = null)

/** Maps a Node64 error response to [Node64ApiException]. */
internal fun node64ApiException(raw: RawResponse): IPv64Exception {
    val parsed = runCatching { IPv64Json.decodeFromString(Node64Error.serializer(), raw.body) }.getOrNull()
    return Node64ApiException(
        status = raw.code,
        error = parsed?.error ?: raw.body.takeIf { it.isNotBlank() && it.length <= 200 }?.trim(),
        field = parsed?.field,
        retryAfterSeconds = raw.headers["Retry-After"]?.toIntOrNull(),
        rawBody = raw.body,
    )
}
