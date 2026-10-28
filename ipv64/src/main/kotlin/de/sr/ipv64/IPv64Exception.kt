package de.sr.ipv64

import de.sr.ipv64.model.Problem

/** Base type of every error thrown by the [IPv64] client. */
public sealed class IPv64Exception(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * The API answered with a non-2xx status code.
 *
 * The body is an RFC 9457 problem document ([problem]) when the server sends one.
 */
public class IPv64ApiException(
    /** HTTP status code. */
    public val status: Int,
    /** Parsed `application/problem+json` body, if available. */
    public val problem: Problem?,
    /** Seconds until the next attempt is allowed (429 and some 409 responses). */
    public val retryAfterSeconds: Int?,
    /** Unparsed response body. */
    public val rawBody: String,
) : IPv64Exception(
    buildString {
        append("IPv64 API error ").append(status)
        problem?.code?.let { append(" (").append(it).append(')') }
        problem?.detail?.let { append(": ").append(it) }
    },
) {
    /** Machine readable error code such as `domain_not_found`. */
    public val code: String? get() = problem?.code

    /** Human readable error message. */
    public val detail: String? get() = problem?.detail

    /** Input field the error refers to, if any. */
    public val field: String? get() = problem?.field

    public val isUnauthorized: Boolean get() = status == 401
    public val isForbidden: Boolean get() = status == 403
    public val isNotFound: Boolean get() = status == 404
    public val isConflict: Boolean get() = status == 409
    public val isValidationError: Boolean get() = status == 422
    public val isRateLimited: Boolean get() = status == 429
}

/** The request could not be sent or the response could not be read (no connection, timeout, …). */
public class IPv64NetworkException(message: String, cause: Throwable) : IPv64Exception(message, cause)

/** The response could not be decoded into the expected model. */
public class IPv64SerializationException(message: String, cause: Throwable) : IPv64Exception(message, cause)

/**
 * A [de.sr.ipv64.node64.Node64] node answered with a non-2xx status code.
 *
 * Node64 sends errors as `{"error": "…", "field": "…"}`. A 404 is also returned when the
 * module (e.g. Healthchecks, DynDNS) is switched off on the node.
 */
public class Node64ApiException(
    /** HTTP status code. */
    public val status: Int,
    /** Error message of the node (e.g. `invalid api key`), if available. */
    public val error: String?,
    /** Affected input field, if any. */
    public val field: String?,
    /** Seconds until the next attempt is allowed (header `Retry-After`, sent with 429). */
    public val retryAfterSeconds: Int?,
    /** Unparsed response body. */
    public val rawBody: String,
) : IPv64Exception(
    buildString {
        append("Node64 API error ").append(status)
        error?.let { append(": ").append(it) }
        field?.let { append(" (field ").append(it).append(')') }
    },
) {
    public val isUnauthorized: Boolean get() = status == 401
    public val isForbidden: Boolean get() = status == 403
    public val isNotFound: Boolean get() = status == 404
    public val isConflict: Boolean get() = status == 409
    public val isValidationError: Boolean get() = status == 422
    public val isRateLimited: Boolean get() = status == 429
}
