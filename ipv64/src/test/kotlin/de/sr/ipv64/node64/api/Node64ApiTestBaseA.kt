package de.sr.ipv64.node64.api

import de.sr.ipv64.MockServerTest
import de.sr.ipv64.node64.Node64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import mockwebserver3.MockResponse
import mockwebserver3.RecordedRequest
import okio.Buffer
import org.junit.Assert.assertEquals

/** Shared helpers for the Node64 per-API tests (group A). */
abstract class Node64ApiTestBaseA : MockServerTest() {

    protected fun node(): Node64 = Node64(baseUrl = server.url("/api").toString(), apiKey = "node64_test")

    protected fun parse(text: String): JsonElement = Json.parseToJsonElement(text)

    protected fun RecordedRequest.json(): JsonElement = parse(bodyText())

    protected fun RecordedRequest.bodyBytes(): ByteArray = body?.toByteArray() ?: ByteArray(0)

    /** Asserts that the request body is JSON equal (structurally) to [expected]. */
    protected fun RecordedRequest.assertJsonBody(expected: String) {
        assertEquals(parse(expected), json())
    }

    protected fun enqueueEmpty(code: Int = 200) {
        server.enqueue(MockResponse.Builder().code(code).build())
    }

    protected fun enqueueBytes(payload: ByteArray, contentType: String = "application/octet-stream") {
        server.enqueue(
            MockResponse.Builder().code(200).setHeader("Content-Type", contentType).body(Buffer().write(payload)).build(),
        )
    }

    protected fun enqueueText(body: String, contentType: String = "text/plain; version=0.0.4") {
        server.enqueue(MockResponse.Builder().code(200).setHeader("Content-Type", contentType).body(body).build())
    }
}
