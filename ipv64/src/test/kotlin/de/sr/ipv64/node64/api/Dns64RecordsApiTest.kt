package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.time.Instant

class Dns64RecordsApiTest : Node64ApiTestBaseB() {

    private val records get() = node().dns64.records

    private val recordJson = """{"id":12,"name":"nas.home.lan","type":"A","value":"192.168.1.10","ttl":300,"enabled":true,
        "created_at":"2026-10-20T10:00:00Z","comment":"NAS","no_ptr":true,"local_only":false,"dynamic":true}"""

    private fun assertRecord(r: DNS64Record) {
        assertEquals(12L, r.id)
        assertEquals("nas.home.lan", r.name)
        assertEquals("A", r.type)
        assertEquals("192.168.1.10", r.value)
        assertEquals(300, r.ttl)
        assertEquals(true, r.enabled)
        assertEquals(Instant.parse("2026-10-20T10:00:00Z"), r.createdAt)
        assertEquals("NAS", r.comment)
        assertEquals(true, r.noPtr)
        assertEquals(false, r.localOnly)
        assertEquals(true, r.dynamic)
    }

    @Test
    fun `list decodes records`() = test<Unit> {
        enqueueJson("[$recordJson,{\"id\":13,\"name\":\"*.lab\",\"type\":\"MX\",\"value\":\"10 mail.home.lan\"}]")
        val list = records.list()
        expectRequest("GET", "/api/dns64/records")
        assertEquals(2, list.size)
        assertRecord(list[0])
        assertEquals("MX", list[1].type)
        assertEquals("10 mail.home.lan", list[1].value)
        assertNull(list[1].ttl)
    }

    @Test
    fun `create posts full request with enum wire name`() = test<Unit> {
        enqueueJson(recordJson)
        assertRecord(records.create(DNS64RecordRequest("nas.home.lan", DNS64RecordType.AAAA, "fd00::10", 60, "NAS", true, false)))
        val r = expectRequest("POST", "/api/dns64/records")
        assertEquals(
            js("""{"name":"nas.home.lan","type":"AAAA","value":"fd00::10","ttl":60,"comment":"NAS","no_ptr":true,"local_only":false}"""),
            r.jsonBody(),
        )
    }

    @Test
    fun `create omits null optionals`() = test<Unit> {
        enqueueJson(recordJson)
        records.create(DNS64RecordRequest("a.lan", DNS64RecordType.CNAME, "b.lan"))
        assertEquals(js("""{"name":"a.lan","type":"CNAME","value":"b.lan"}"""), expectRequest("POST", "/api/dns64/records").jsonBody())
    }

    @Test
    fun `update puts to id path`() = test<Unit> {
        enqueueJson(recordJson)
        assertRecord(records.update(12, DNS64RecordRequest("nas.home.lan", DNS64RecordType.TXT, "hello", ttl = 10)))
        val r = expectRequest("PUT", "/api/dns64/records/12")
        assertEquals(js("""{"name":"nas.home.lan","type":"TXT","value":"hello","ttl":10}"""), r.jsonBody())
    }

    @Test
    fun `delete`() = test<Unit> {
        enqueueNoContent()
        records.delete(12)
        assertEquals("", expectRequest("DELETE", "/api/dns64/records/12").bodyText())
    }

    @Test
    fun `enable`() = test<Unit> {
        enqueueNoContent()
        records.enable(12)
        expectRequest("POST", "/api/dns64/records/12/enable")
    }

    @Test
    fun `disable`() = test<Unit> {
        enqueueNoContent()
        records.disable(12)
        expectRequest("POST", "/api/dns64/records/12/disable")
    }

    @Test
    fun `import sends text plain bytes and zone query`() = test<Unit> {
        enqueueJson("""{"added":5,"skipped":2,"invalid":1}""")
        val content = "\$ORIGIN home.lan.\nnas IN A 192.168.1.10 ; umlaut ä\n"
        val res = records.import(content, "home.lan")
        val r = expectRequest("POST", "/api/dns64/records/import?zone=home.lan")
        assertEquals("text/plain; charset=utf-8", r.headers["Content-Type"])
        assertArrayEquals(content.toByteArray(Charsets.UTF_8), r.bodyBytes())
        assertEquals(5, res.added)
        assertEquals(2, res.skipped)
        assertEquals(1, res.invalid)
    }

    @Test
    fun `import without zone has no query`() = test<Unit> {
        enqueueJson("""{"added":1}""")
        records.import("192.168.1.10 nas")
        val r = expectRequest("POST", "/api/dns64/records/import")
        assertEquals("192.168.1.10 nas", r.bodyText())
    }

    @Test
    fun `export returns exact bytes with format and Accept`() = test<Unit> {
        val data = "a,b\n1,2\n".toByteArray() + byteArrayOf(0, -1)
        enqueueBytes(data, "text/csv")
        val bytes = records.export(DNS64ExportFormat.CSV)
        val r = expectRequest("GET", "/api/dns64/records/export?format=csv")
        assertEquals("*/*", r.headers["Accept"])
        assertArrayEquals(data, bytes)
    }

    @Test
    fun `export without format`() = test<Unit> {
        enqueueBytes(byteArrayOf(1, 2, 3))
        assertArrayEquals(byteArrayOf(1, 2, 3), records.export())
        expectRequest("GET", "/api/dns64/records/export")
    }
}
