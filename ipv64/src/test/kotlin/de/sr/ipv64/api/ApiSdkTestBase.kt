package de.sr.ipv64.api

import de.sr.ipv64.MockServerTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import mockwebserver3.RecordedRequest

/** Shared helpers for the per-API tests. */
abstract class ApiSdkTestBase : MockServerTest() {
    /** Request body parsed as JSON object. */
    protected fun RecordedRequest.jsonBody(): JsonObject = Json.parseToJsonElement(bodyText()).jsonObject
}
