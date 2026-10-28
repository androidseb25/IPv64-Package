package de.sr.ipv64.node64.api

import de.sr.ipv64.MockServerTest
import de.sr.ipv64.node64.Node64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import mockwebserver3.MockResponse
import mockwebserver3.RecordedRequest
import okio.Buffer
import org.junit.Assert.assertEquals

/** Shared helpers for the Node64 API tests (set B). Node64 responses carry no data envelope. */
abstract class Node64ApiTestBaseB : MockServerTest() {

    protected fun node(): Node64 = Node64(baseUrl = server.url("/api").toString(), apiKey = "node64_test")

    /** Parses a JSON literal for comparison with request bodies. */
    protected fun js(text: String): JsonElement = Json.parseToJsonElement(text)

    protected fun RecordedRequest.jsonBody(): JsonElement = Json.parseToJsonElement(bodyText())

    protected fun RecordedRequest.bodyBytes(): ByteArray = body?.toByteArray() ?: ByteArray(0)

    /** Asserts method and exact target (including the query) and returns the request. */
    protected fun expectRequest(method: String, target: String): RecordedRequest {
        val request = takeRequest()
        assertEquals(method, request.method)
        assertEquals(target, request.target)
        return request
    }

    protected fun enqueueBytes(bytes: ByteArray, contentType: String = "application/octet-stream", vararg headers: Pair<String, String>) {
        server.enqueue(
            MockResponse.Builder().code(200).setHeader("Content-Type", contentType)
                .apply { headers.forEach { (k, v) -> setHeader(k, v) } }
                .body(Buffer().write(bytes)).build(),
        )
    }

    protected fun enqueueText(body: String, contentType: String = "text/plain") {
        server.enqueue(MockResponse.Builder().code(200).setHeader("Content-Type", contentType).body(body).build())
    }
}
