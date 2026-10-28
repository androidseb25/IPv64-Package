package de.sr.ipv64

/** How the API key is sent to the server. */
public enum class IPv64AuthScheme {
    /** `Authorization: Bearer <API-Key>` (default). */
    Bearer,

    /** `X-API-Key: <API-Key>`. */
    ApiKeyHeader,
}
