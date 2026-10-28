package de.sr.ipv64

import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import org.junit.After
import org.junit.Before
import java.util.concurrent.TimeUnit

/** Base class for tests that run the [IPv64] client against a local [MockWebServer]. */
abstract class MockServerTest {
    protected lateinit var server: MockWebServer

    @Before
    fun startServer() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun stopServer() {
        server.close()
    }

    protected fun client(
        apiKey: String? = "test-key",
        actAs: String? = null,
        authScheme: IPv64AuthScheme = IPv64AuthScheme.Bearer,
    ): IPv64 = IPv64(
        apiKey = apiKey,
        actAs = actAs,
        authScheme = authScheme,
        baseUrl = server.url("/api/v2").toString(),
    )

    protected fun enqueueJson(body: String, code: Int = 200) {
        server.enqueue(
            MockResponse.Builder()
                .code(code)
                .setHeader("Content-Type", "application/json")
                .body(body)
                .build(),
        )
    }

    protected fun enqueueProblem(status: Int, code: String, detail: String, extra: String = "") {
        server.enqueue(
            MockResponse.Builder()
                .code(status)
                .setHeader("Content-Type", "application/problem+json")
                .body(
                    """{"type":"https://ipv64.net/api/v2/errors#$code","title":"Error","status":$status,""" +
                        """"detail":"$detail","code":"$code"$extra}""",
                )
                .build(),
        )
    }

    protected fun enqueueNoContent() {
        server.enqueue(MockResponse.Builder().code(204).build())
    }

    protected fun takeRequest(): RecordedRequest =
        requireNotNull(server.takeRequest(5, TimeUnit.SECONDS)) { "no request received" }

    /** Path of the request relative to `/api/v2`, including the query string. */
    protected val RecordedRequest.apiTarget: String
        get() = target.removePrefix("/api/v2")

    protected fun RecordedRequest.bodyText(): String = body?.utf8() ?: ""

    protected fun <T> test(block: suspend () -> T): T = runBlocking { block() }
}
