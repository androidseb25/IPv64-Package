package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.*
import org.junit.Assert.*
import org.junit.Test

class EdgeApiTest : Node64ApiTestBaseB() {

    private val edge get() = node().edge

    private val edgeJson = """{"config":{"bind":"0.0.0.0","http_port":80,"https_port":443,"public_port":8443,"http3":true,"default":"html",
        "default_url":"https://x","default_html":"<p>x</p>"},
        "listeners":[{"name":"http3","proto":"udp","port":443,"needed":true,"running":false,"addr":"[::]:443","error":"in use"}],
        "names":[{"name":"a.example.com","path":"/","owner":"cdn64"}]}"""

    private fun assertEdge(e: EdgeStatus) {
        val c = e.config!!
        assertEquals("0.0.0.0", c.bind)
        assertEquals(80, c.httpPort)
        assertEquals(443, c.httpsPort)
        assertEquals(8443, c.publicPort)
        assertEquals(true, c.http3)
        assertEquals(DefaultPageMode.HTML, c.default)
        assertEquals("https://x", c.defaultUrl)
        assertEquals("<p>x</p>", c.defaultHtml)
        val l = e.listeners.single()
        assertEquals(EdgeListenerName.HTTP3, l.name)
        assertEquals("udp", l.proto)
        assertEquals(443, l.port)
        assertEquals(true, l.needed)
        assertEquals(false, l.running)
        assertEquals("[::]:443", l.addr)
        assertEquals("in use", l.error)
        assertEquals("cdn64", e.names.single().owner)
        assertEquals("/", e.names.single().path)
    }

    @Test
    fun `ports decodes claims and nested edge`() = test<Unit> {
        enqueueJson(
            """{"ports":[{"owner":"dns64","purpose":"DoT","proto":"tcp","port":853,"bind":"0.0.0.0","active":true,"conflicts":["cdn64"]},
            {"owner":"cdn64","proto":"udp","port":443}],"edge":$edgeJson}""",
        )
        val p = edge.ports()
        expectRequest("GET", "/api/ports")
        assertEquals(2, p.ports.size)
        assertEquals(NetProtocol.TCP, p.ports[0].proto)
        assertEquals(853, p.ports[0].port)
        assertEquals(true, p.ports[0].active)
        assertEquals(listOf("cdn64"), p.ports[0].conflicts)
        assertEquals(NetProtocol.UDP, p.ports[1].proto)
        assertTrue(p.ports[1].conflicts.isEmpty())
        assertEdge(p.edge!!)
    }

    @Test
    fun `probePort sends proto port and bind`() = test<Unit> {
        enqueueJson("""{"free":false,"used_by":"dns64","purpose":"DNS","error":""}""")
        val r = edge.probePort(NetProtocol.UDP, 53, "127.0.0.1")
        expectRequest("GET", "/api/ports/probe?proto=udp&port=53&bind=127.0.0.1")
        assertEquals(false, r.free)
        assertEquals("dns64", r.usedBy)
        assertEquals("DNS", r.purpose)
    }

    @Test
    fun `probePort without bind`() = test<Unit> {
        enqueueJson("""{"free":true}""")
        assertEquals(true, edge.probePort(NetProtocol.TCP, 8080).free)
        expectRequest("GET", "/api/ports/probe?proto=tcp&port=8080")
    }

    @Test
    fun `get decodes`() = test<Unit> {
        enqueueJson(edgeJson)
        assertEdge(edge.get())
        expectRequest("GET", "/api/edge")
    }

    @Test
    fun `update puts config`() = test<Unit> {
        enqueueJson(edgeJson)
        assertEdge(edge.update(EdgeConfigRequest(httpPort = 8080, http3 = false, default = DefaultPageMode.NOT_FOUND, defaultUrl = "https://x")))
        assertEquals(
            js("""{"http_port":8080,"http3":false,"default":"404","default_url":"https://x"}"""),
            expectRequest("PUT", "/api/edge").jsonBody(),
        )
    }

    @Test
    fun `ui decodes reserved word public`() = test<Unit> {
        enqueueJson("""{"name":"node.example.com","public":true,"cert":"3","cert_from_env":true}""")
        val u = edge.ui()
        expectRequest("GET", "/api/edge/ui")
        assertEquals("node.example.com", u.name)
        assertEquals(true, u.isPublic)
        assertEquals("3", u.cert)
        assertEquals(true, u.certFromEnv)
    }

    @Test
    fun `updateUi puts public field`() = test<Unit> {
        enqueueJson("""{"name":"n","public":false}""")
        val u = edge.updateUi(UpdateEdgeUiRequest(name = "n", isPublic = false, cert = "auto"))
        assertEquals(js("""{"name":"n","public":false,"cert":"auto"}"""), expectRequest("PUT", "/api/edge/ui").jsonBody())
        assertEquals(false, u.isPublic)
    }
}
