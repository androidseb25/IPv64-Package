package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.*
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.*
import org.junit.Test
import kotlin.time.Instant

class Proxy64StreamsApiTest : Node64ApiTestBaseB() {

    private val streams get() = node().proxy.streams

    private val streamJson = """{"id":4,"name":"ssh","enabled":true,"listen_port":2222,"tcp":true,"udp":false,"target_host":"10.0.0.5","target_port":22,
        "tls_cert_id":3,"created_at":"2026-10-20T10:00:00Z","updated_at":"2026-10-21T10:00:00Z","running":true,
        "reach":{"state":"up","ms":3},"listen_errors":{"tcp":"in use","udp":"x"},
        "stats":{"connections":1000000000000,"active":2,"bytes_in":5000000000,"bytes_out":6000000000},
        "cert_name":"a.example.com","geo_mode":"allow","geo_countries":["DE","AT"]}"""

    private fun assertStream(s: Stream) {
        assertEquals(4L, s.id)
        assertEquals("ssh", s.name)
        assertEquals(2222, s.listenPort)
        assertEquals(true, s.tcp)
        assertEquals(false, s.udp)
        assertEquals("10.0.0.5", s.targetHost)
        assertEquals(22, s.targetPort)
        assertEquals(3L, s.tlsCertId)
        assertEquals(Instant.parse("2026-10-20T10:00:00Z"), s.createdAt)
        assertEquals(Instant.parse("2026-10-21T10:00:00Z"), s.updatedAt)
        assertEquals(true, s.running)
        assertEquals("up", s.reach!!.jsonObject["state"]!!.jsonPrimitive.content)
        assertEquals(mapOf("tcp" to "in use", "udp" to "x"), s.listenErrors)
        assertEquals(1000000000000L, s.stats?.connections)
        assertEquals(5000000000L, s.stats?.bytesIn)
        assertEquals(6000000000L, s.stats?.bytesOut)
        assertEquals("a.example.com", s.certName)
        assertEquals(ProxyGeoMode.ALLOW, s.geoMode)
        assertEquals(listOf("DE", "AT"), s.geoCountries)
    }

    @Test
    fun `list`() = test<Unit> {
        enqueueJson("[$streamJson]")
        assertStream(streams.list().single())
        expectRequest("GET", "/api/cdn64/streams")
    }

    @Test
    fun `create posts input`() = test<Unit> {
        enqueueJson(streamJson)
        assertStream(
            streams.create(StreamInput("ssh", true, 2222, true, false, "10.0.0.5", 22, 3, ProxyGeoMode.ALLOW, listOf("DE"))),
        )
        assertEquals(
            js("""{"name":"ssh","enabled":true,"listen_port":2222,"tcp":true,"udp":false,"target_host":"10.0.0.5","target_port":22,
                "tls_cert_id":3,"geo_mode":"allow","geo_countries":["DE"]}"""),
            expectRequest("POST", "/api/cdn64/streams").jsonBody(),
        )
    }

    @Test
    fun `create omits null fields`() = test<Unit> {
        enqueueJson(streamJson)
        streams.create(StreamInput(listenPort = 53, udp = true))
        assertEquals(js("""{"listen_port":53,"udp":true}"""), expectRequest("POST", "/api/cdn64/streams").jsonBody())
    }

    @Test
    fun `get`() = test<Unit> {
        enqueueJson(streamJson)
        assertStream(streams.get(4))
        expectRequest("GET", "/api/cdn64/streams/4")
    }

    @Test
    fun `update`() = test<Unit> {
        enqueueJson(streamJson)
        assertStream(streams.update(4, StreamInput(geoMode = ProxyGeoMode.OFF, geoCountries = emptyList())))
        assertEquals(js("""{"geo_mode":"","geo_countries":[]}"""), expectRequest("PUT", "/api/cdn64/streams/4").jsonBody())
    }

    @Test
    fun `delete`() = test<Unit> {
        enqueueNoContent()
        streams.delete(4)
        expectRequest("DELETE", "/api/cdn64/streams/4")
    }

    @Test
    fun `enable`() = test<Unit> {
        enqueueJson(streamJson)
        assertStream(streams.enable(4))
        expectRequest("POST", "/api/cdn64/streams/4/enable")
    }

    @Test
    fun `disable`() = test<Unit> {
        enqueueJson(streamJson)
        assertStream(streams.disable(4))
        expectRequest("POST", "/api/cdn64/streams/4/disable")
    }

    @Test
    fun `createHealthcheck`() = test<Unit> {
        enqueueJson("""{"token":"zzz"}""")
        assertEquals("zzz", streams.createHealthcheck(4).token)
        expectRequest("POST", "/api/cdn64/streams/4/healthcheck")
    }
}
