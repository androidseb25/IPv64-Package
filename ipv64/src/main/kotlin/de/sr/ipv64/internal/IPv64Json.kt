package de.sr.ipv64.internal

import kotlinx.serialization.json.Json

/**
 * JSON configuration shared by the whole SDK.
 *
 * - unknown keys are ignored, so new server fields never break older SDK versions
 * - `null` values are not sent, so optional fields in PATCH requests mean "leave unchanged"
 * - unknown enum values fall back to the property's default (`null`)
 */
internal val IPv64Json: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
    encodeDefaults = false
}
