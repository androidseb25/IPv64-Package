package de.sr.ipv64.internal

import de.sr.ipv64.IPv64ApiException
import de.sr.ipv64.IPv64Exception
import de.sr.ipv64.IPv64NetworkException
import de.sr.ipv64.IPv64SerializationException
import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.Page
import de.sr.ipv64.model.Problem
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** HTTP methods used by the IPv64 API. */
internal enum class Method { GET, POST, PUT, PATCH, DELETE }

/**
 * URL path relative to the API base URL. Every segment is percent-encoded on its own,
 * so user supplied values such as domain names or tokens can never break out of their segment.
 */
internal class ApiPath(vararg val segments: String)

/** Builds a query map and silently drops `null` values. */
internal fun query(vararg pairs: Pair<String, Any?>): Map<String, String> = buildMap {
    for ((key, value) in pairs) {
        when (value) {
            null -> Unit
            is ApiValue -> put(key, value.value)
            else -> put(key, value.toString())
        }
    }
}

/** Raw result of a finished HTTP exchange. */
internal class RawResponse(val code: Int, val bytes: ByteArray, val headers: okhttp3.Headers) {
    val body: String by lazy { bytes.decodeToString() }
}

@Serializable
internal class DataEnvelope<T>(val data: T)

/**
 * Shared HTTP layer of the [de.sr.ipv64.IPv64] and [de.sr.ipv64.node64.Node64] clients.
 *
 * @param defaultHeaders headers sent with every request (auth, user agent, …); per-call headers override them.
 * @param errorMapper turns a non-2xx response into the client specific exception.
 */
internal class HttpTransport(
    private val client: OkHttpClient,
    private val baseUrl: HttpUrl,
    private val defaultHeaders: Map<String, String>,
    private val errorMapper: (RawResponse) -> IPv64Exception,
) {
    internal val json = IPv64Json

    /** Performs a request and unwraps the `{"data": …}` envelope. */
    suspend fun <T> data(
        method: Method,
        path: ApiPath,
        deserializer: KSerializer<T>,
        query: Map<String, String> = emptyMap(),
        body: RequestBody? = null,
        headers: Map<String, String> = emptyMap(),
    ): T {
        val raw = execute(method, path, query, body, headers)
        return decode(DataEnvelope.serializer(deserializer), raw.body).data
    }

    /** Performs a request whose response is `{"data": [...], "meta": {...}}`. */
    suspend fun <T> page(
        method: Method,
        path: ApiPath,
        itemSerializer: KSerializer<T>,
        query: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
    ): Page<T> {
        val raw = execute(method, path, query, null, headers)
        return decode(Page.serializer(itemSerializer), raw.body)
    }

    /** Performs a request and ignores the response body (e.g. `204 No Content`). */
    suspend fun unit(
        method: Method,
        path: ApiPath,
        query: Map<String, String> = emptyMap(),
        body: RequestBody? = null,
        headers: Map<String, String> = emptyMap(),
    ) {
        execute(method, path, query, body, headers)
    }

    /** Performs a request and decodes the whole body as [T] (APIs without `data` envelope). */
    suspend fun <T> json(
        method: Method,
        path: ApiPath,
        deserializer: KSerializer<T>,
        query: Map<String, String> = emptyMap(),
        body: RequestBody? = null,
        headers: Map<String, String> = emptyMap(),
    ): T = decode(deserializer, execute(method, path, query, body, headers).body)

    /** Performs a request and returns the body as [JsonElement], or `null` if the body is empty. */
    suspend fun jsonOrNull(
        method: Method,
        path: ApiPath,
        query: Map<String, String> = emptyMap(),
        body: RequestBody? = null,
        headers: Map<String, String> = emptyMap(),
    ): JsonElement? {
        val text = execute(method, path, query, body, headers).body
        return if (text.isBlank()) null else decode(JsonElement.serializer(), text)
    }

    /** Performs a request and returns the raw response bytes (files, certificates, archives). */
    suspend fun bytes(
        method: Method,
        path: ApiPath,
        query: Map<String, String> = emptyMap(),
        body: RequestBody? = null,
        headers: Map<String, String> = emptyMap(),
    ): ByteArray = execute(method, path, query, body, headers).bytes

    /** Performs a request and returns the raw response body (e.g. `text/plain`). */
    suspend fun text(
        method: Method,
        path: ApiPath,
        query: Map<String, String> = emptyMap(),
        body: RequestBody? = null,
        headers: Map<String, String> = emptyMap(),
    ): String = execute(method, path, query, body, headers).body

    /** Performs a request and returns status, headers and body without any decoding. */
    suspend fun raw(
        method: Method,
        path: ApiPath,
        query: Map<String, String> = emptyMap(),
        body: RequestBody? = null,
        headers: Map<String, String> = emptyMap(),
    ): RawResponse = execute(method, path, query, body, headers)

    fun <T> jsonBody(serializer: SerializationStrategy<T>, value: T): RequestBody =
        json.encodeToString(serializer, value).toRequestBody(JSON_MEDIA_TYPE)

    fun <T> decode(deserializer: DeserializationStrategy<T>, body: String): T = try {
        json.decodeFromString(deserializer, body)
    } catch (e: SerializationException) {
        throw IPv64SerializationException("Unable to decode IPv64 response", e)
    } catch (e: IllegalArgumentException) {
        throw IPv64SerializationException("Unable to decode IPv64 response", e)
    }

    private suspend fun execute(
        method: Method,
        path: ApiPath,
        query: Map<String, String>,
        body: RequestBody?,
        headers: Map<String, String>,
    ): RawResponse {
        val url = baseUrl.newBuilder().apply {
            if (path.segments.isEmpty()) addPathSegment("") // API root needs the trailing slash
            path.segments.forEach { addPathSegment(it) }
            query.forEach { (key, value) -> addQueryParameter(key, value) }
        }.build()

        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json, application/problem+json, text/plain")
            .apply {
                defaultHeaders.forEach { (name, value) -> header(name, value) }
                headers.forEach { (name, value) -> header(name, value) }
                val requestBody = body ?: if (method.requiresBody) EMPTY_JSON_BODY else null
                method(method.name, requestBody)
            }
            .build()

        val raw = client.newCall(request).await()
        if (raw.code !in 200..299 && raw.code != 304) throw errorMapper(raw)
        return raw
    }

    private suspend fun Call.await(): RawResponse = suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { cancel() }
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(IPv64NetworkException("IPv64 request failed: ${e.message}", e))
            }

            override fun onResponse(call: Call, response: Response) {
                val result = runCatching {
                    response.use { RawResponse(it.code, it.body.bytes(), it.headers) }
                }
                result.fold(
                    onSuccess = { continuation.resume(it) },
                    onFailure = {
                        continuation.resumeWithException(
                            IPv64NetworkException("Unable to read IPv64 response: ${it.message}", it),
                        )
                    },
                )
            }
        })
    }

    private val Method.requiresBody: Boolean
        get() = this == Method.POST || this == Method.PUT || this == Method.PATCH

    internal companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private val EMPTY_JSON_BODY = "{}".toRequestBody(JSON_MEDIA_TYPE)
    }
}

/** Maps an IPv64 error response (RFC 9457 problem document) to [IPv64ApiException]. */
internal fun ipv64ApiException(raw: RawResponse): IPv64Exception {
    val problem = runCatching { IPv64Json.decodeFromString(Problem.serializer(), raw.body) }.getOrNull()
    return IPv64ApiException(
        status = raw.code,
        problem = problem,
        retryAfterSeconds = problem?.retryAfter ?: raw.headers["Retry-After"]?.toIntOrNull(),
        rawBody = raw.body,
    )
}

/** Decodes the whole body as [T] for a reified type (APIs without `data` envelope). */
internal suspend inline fun <reified T> HttpTransport.json(
    method: Method,
    path: ApiPath,
    query: Map<String, String> = emptyMap(),
    body: RequestBody? = null,
    headers: Map<String, String> = emptyMap(),
): T = json(method, path, serializer<T>(), query, body, headers)

/** Unwraps `{"data": T}` for a reified type. */
internal suspend inline fun <reified T> HttpTransport.data(
    method: Method,
    path: ApiPath,
    query: Map<String, String> = emptyMap(),
    body: RequestBody? = null,
    headers: Map<String, String> = emptyMap(),
): T = data(method, path, serializer<T>(), query, body, headers)

/** Unwraps `{"data": [T]}` for a reified element type. */
internal suspend inline fun <reified T> HttpTransport.list(
    method: Method,
    path: ApiPath,
    query: Map<String, String> = emptyMap(),
    body: RequestBody? = null,
    headers: Map<String, String> = emptyMap(),
): List<T> = data(method, path, ListSerializer(serializer<T>()), query, body, headers)

/** Unwraps `{"data": [T], "meta": {...}}` for a reified element type. */
internal suspend inline fun <reified T> HttpTransport.page(
    method: Method,
    path: ApiPath,
    query: Map<String, String> = emptyMap(),
    headers: Map<String, String> = emptyMap(),
): Page<T> = page(method, path, serializer<T>(), query, headers)

/** Encodes a request object as JSON body. */
internal inline fun <reified T> HttpTransport.body(value: T): RequestBody = jsonBody(serializer<T>(), value)
