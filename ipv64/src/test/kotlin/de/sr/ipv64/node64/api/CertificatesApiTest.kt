package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.*
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mockwebserver3.MockResponse
import org.junit.Assert.*
import org.junit.Test
import kotlin.time.Instant

class CertificatesApiTest : Node64ApiTestBaseB() {

    private val certs get() = node().certificates

    private val certJson = """{"id":7,"names":["a.example.com","*.example.com"],"source":"dns","alias_zone":"ipv64.net","alias_prefix":"acme",
        "staging":false,"auto_renew":true,"status":"valid","step":"done","issuer":"R11",
        "not_before":"2026-09-01T00:00:00Z","not_after":"2026-11-30T00:00:00+01:00","last_error":"old","last_attempt":"2026-09-01T00:01:00Z",
        "used_by":["host:3"],"created_at":"2026-08-30T10:00:00Z","updated_at":"2026-09-01T00:02:00Z","dns_account":4,"ca_id":5,"days":90,
        "acme_ca":6,"key_type":"ecdsa"}"""

    private fun assertCert(c: Certificate) {
        assertEquals(7L, c.id)
        assertEquals(listOf("a.example.com", "*.example.com"), c.names)
        assertEquals(CertificateSource.DNS, c.source)
        assertEquals("ipv64.net", c.aliasZone)
        assertEquals("acme", c.aliasPrefix)
        assertEquals(false, c.staging)
        assertEquals(true, c.autoRenew)
        assertEquals(CertificateStatus.VALID, c.status)
        assertEquals("done", c.step)
        assertEquals("R11", c.issuer)
        assertEquals(Instant.parse("2026-09-01T00:00:00Z"), c.notBefore)
        assertEquals(Instant.parse("2026-11-29T23:00:00Z"), c.notAfter)
        assertEquals("old", c.lastError)
        assertEquals(Instant.parse("2026-09-01T00:01:00Z"), c.lastAttempt)
        assertEquals(listOf("host:3"), c.usedBy)
        assertEquals(Instant.parse("2026-08-30T10:00:00Z"), c.createdAt)
        assertEquals(Instant.parse("2026-09-01T00:02:00Z"), c.updatedAt)
        assertEquals(4L, c.dnsAccount)
        assertEquals(5L, c.caId)
        assertEquals(90, c.days)
        assertEquals(6L, c.acmeCa)
        assertEquals(CertificateKeyType.ECDSA, c.keyType)
    }

    // ---- certificates ----

    @Test
    fun `list`() = test<Unit> {
        enqueueJson("[$certJson,{\"id\":8,\"source\":\"upload\",\"status\":\"expired\",\"key_type\":\"rsa\"}]")
        val l = certs.list()
        expectRequest("GET", "/api/certs")
        assertCert(l[0])
        assertEquals(CertificateSource.UPLOAD, l[1].source)
        assertEquals(CertificateStatus.EXPIRED, l[1].status)
        assertEquals(CertificateKeyType.RSA, l[1].keyType)
        assertTrue(l[1].names.isEmpty())
    }

    @Test
    fun `create posts request with wire names`() = test<Unit> {
        enqueueJson(certJson)
        assertCert(
            certs.create(
                CreateCertificateRequest(
                    listOf("a.example.com"), CertificateSource.ALIAS, "ipv64.net", "acme", true, false, 4, 5, 90, 6, CertificateKeyType.RSA,
                ),
            ),
        )
        assertEquals(
            js(
                """{"names":["a.example.com"],"source":"alias","alias_zone":"ipv64.net","alias_prefix":"acme","staging":true,"auto_renew":false,
                "dns_account":4,"ca_id":5,"days":90,"acme_ca":6,"key_type":"rsa"}""",
            ),
            expectRequest("POST", "/api/certs").jsonBody(),
        )
    }

    @Test
    fun `create omits null optionals`() = test<Unit> {
        enqueueJson(certJson)
        certs.create(CreateCertificateRequest(listOf("a.example.com"), CertificateSource.HTTP))
        assertEquals(js("""{"names":["a.example.com"],"source":"http"}"""), expectRequest("POST", "/api/certs").jsonBody())
    }

    @Test
    fun `upload sends json and redacts secrets in toString`() = test<Unit> {
        enqueueJson(certJson)
        val req = UploadCertificateRequest("-----BEGIN CERTIFICATE-----CERTBODY", "-----BEGIN PRIVATE KEY-----KEYBODY", "pw-secret", "P12BASE64SECRET")
        assertCert(certs.upload(req))
        val r = expectRequest("POST", "/api/certs/upload")
        assertTrue(r.headers["Content-Type"]!!.startsWith("application/json"))
        assertEquals(
            js("""{"cert":"-----BEGIN CERTIFICATE-----CERTBODY","key":"-----BEGIN PRIVATE KEY-----KEYBODY","password":"pw-secret","p12":"P12BASE64SECRET"}"""),
            r.jsonBody(),
        )
        val s = req.toString()
        assertFalse(s.contains("KEYBODY"))
        assertFalse(s.contains("pw-secret"))
        assertFalse(s.contains("P12BASE64SECRET"))
        assertTrue(s.contains("CERTBODY"))
    }

    @Test
    fun `upload omits null fields`() = test<Unit> {
        enqueueJson(certJson)
        certs.upload(UploadCertificateRequest(p12 = "AAAA", password = "x"))
        assertEquals(js("""{"password":"x","p12":"AAAA"}"""), expectRequest("POST", "/api/certs/upload").jsonBody())
    }

    @Test
    fun `uploadFiles sends multipart with field names file names and exact bytes`() = test<Unit> {
        enqueueJson(certJson)
        val cert = "CERT-PEM-ä".toByteArray() + byteArrayOf(0, -1, -2, 0x7F)
        val key = byteArrayOf(0x30, -128, 0x00, 0x01, -1)
        assertCert(certs.uploadFiles(cert, "my.pem", key, "my.key", "pass-word"))
        val r = expectRequest("POST", "/api/certs/upload")
        val ct = r.headers["Content-Type"]!!
        assertTrue(ct, ct.startsWith("multipart/form-data; boundary="))
        val raw = r.bodyBytes()
        val text = String(raw, Charsets.ISO_8859_1)
        assertTrue(text.contains("name=\"cert_file\"; filename=\"my.pem\""))
        assertTrue(text.contains("name=\"key_file\"; filename=\"my.key\""))
        assertTrue(text.contains("name=\"key_password\""))
        assertTrue(text.contains("Content-Type: application/octet-stream"))
        assertTrue(text.contains("\r\n\r\npass-word\r\n"))
        assertTrue(text.contains(String(cert, Charsets.ISO_8859_1)))
        assertTrue(text.contains(String(key, Charsets.ISO_8859_1)))
        // cert part must come before key part
        assertTrue(text.indexOf("cert_file") < text.indexOf("key_file"))
    }

    @Test
    fun `uploadFiles without key sends only cert part with defaults`() = test<Unit> {
        enqueueJson(certJson)
        val pfx = byteArrayOf(1, 2, 3, -4)
        certs.uploadFiles(pfx)
        val text = String(expectRequest("POST", "/api/certs/upload").bodyBytes(), Charsets.ISO_8859_1)
        assertTrue(text.contains("name=\"cert_file\"; filename=\"cert.pem\""))
        assertFalse(text.contains("key_file"))
        assertFalse(text.contains("key_password"))
        assertTrue(text.contains(String(pfx, Charsets.ISO_8859_1)))
    }

    @Test
    fun `uploadFiles wrong password yields validation error on password field`() = test<Unit> {
        server.enqueue(
            MockResponse.Builder().code(422).setHeader("Content-Type", "application/json")
                .body("""{"error":"wrong password","field":"password"}""").build(),
        )
        val e = runCatching { certs.uploadFiles(byteArrayOf(1), keyPassword = "bad") }.exceptionOrNull()
        e as de.sr.ipv64.Node64ApiException
        assertEquals("password", e.field)
        assertTrue(e.isValidationError)
    }

    @Test
    fun `account decodes`() = test<Unit> {
        enqueueJson("""{"email":"a@b.c","tos_accepted":true,"tos_at":"2026-08-01T10:00:00Z","tos_url":"https://le/tos"}""")
        val a = certs.account()
        expectRequest("GET", "/api/certs/account")
        assertEquals("a@b.c", a.email)
        assertEquals(true, a.tosAccepted)
        assertEquals(Instant.parse("2026-08-01T10:00:00Z"), a.tosAt)
        assertEquals("https://le/tos", a.tosUrl)
    }

    @Test
    fun `updateAccount puts email and accept_tos`() = test<Unit> {
        enqueueJson("""{"email":"a@b.c","tos_accepted":true}""")
        assertEquals(true, certs.updateAccount(UpdateCertAccountRequest("a@b.c", true)).tosAccepted)
        assertEquals(js("""{"email":"a@b.c","accept_tos":true}"""), expectRequest("PUT", "/api/certs/account").jsonBody())
    }

    @Test
    fun `domains returns string list`() = test<Unit> {
        enqueueJson("""["example.ipv64.net","foo.ipv64.net"]""")
        assertEquals(listOf("example.ipv64.net", "foo.ipv64.net"), certs.domains())
        expectRequest("GET", "/api/certs/domains")
    }

    @Test
    fun `get`() = test<Unit> {
        enqueueJson(certJson)
        assertCert(certs.get(7))
        expectRequest("GET", "/api/certs/7")
    }

    @Test
    fun `setAutoRenew puts auto_renew`() = test<Unit> {
        enqueueJson(certJson)
        assertCert(certs.setAutoRenew(7, false))
        assertEquals(js("""{"auto_renew":false}"""), expectRequest("PUT", "/api/certs/7").jsonBody())
    }

    @Test
    fun `delete without force has no query`() = test<Unit> {
        enqueueNoContent()
        certs.delete(7)
        expectRequest("DELETE", "/api/certs/7")
    }

    @Test
    fun `delete with force sends force=1`() = test<Unit> {
        enqueueNoContent()
        certs.delete(7, force = true)
        expectRequest("DELETE", "/api/certs/7?force=1")
    }

    @Test
    fun `renew`() = test<Unit> {
        enqueueJson(certJson)
        assertCert(certs.renew(7))
        expectRequest("POST", "/api/certs/7/renew")
    }

    @Test
    fun `chain returns pem text and sends Accept`() = test<Unit> {
        val pem = "-----BEGIN CERTIFICATE-----\nMIIB\n-----END CERTIFICATE-----\n-----BEGIN CERTIFICATE-----\nMIIC\n-----END CERTIFICATE-----\n"
        enqueueText(pem, "application/pem-certificate-chain")
        val text = certs.chain(7)
        val r = expectRequest("GET", "/api/certs/7/chain")
        assertEquals("application/pem-certificate-chain", r.headers["Accept"])
        assertEquals(pem, text)
    }

    @Test
    fun `meta decodes`() = test<Unit> {
        enqueueJson(
            """{"id":7,"names":["a.example.com"],"issuer":"R11","serial":"03:AB","not_before":"2026-09-01T00:00:00Z","not_after":"2026-11-30T00:00:00Z",
            "days_left":33,"fingerprint_sha256":"AA:BB"}""",
        )
        val m = certs.meta(7)
        expectRequest("GET", "/api/certs/7/meta")
        assertEquals(7L, m.id)
        assertEquals("03:AB", m.serial)
        assertEquals(Instant.parse("2026-09-01T00:00:00Z"), m.notBefore)
        assertEquals(Instant.parse("2026-11-30T00:00:00Z"), m.notAfter)
        assertEquals(33, m.daysLeft)
        assertEquals("AA:BB", m.fingerprintSha256)
    }

    // ---- bundle ----

    @Test
    fun `bundle 200 returns exact bytes etag notAfter and sends Accept and format`() = test<Unit> {
        val zip = byteArrayOf(0x50, 0x4B, 0x03, 0x04, 0, -1, -128, 0x7F)
        enqueueBytes(zip, "application/zip", "ETag" to "\"fp-1\"", "X-Cert-Not-After" to "2026-11-30T00:00:00Z")
        val res = certs.bundle(7, CertBundleFormat.PEM)
        val r = expectRequest("GET", "/api/certs/7/bundle?format=pem")
        assertEquals("application/zip, application/x-pem-file, application/pem-certificate-chain, */*", r.headers["Accept"])
        assertNull(r.headers["If-None-Match"])
        assertArrayEquals(zip, res.content)
        assertEquals("\"fp-1\"", res.etag)
        assertEquals("2026-11-30T00:00:00Z", res.notAfter)
        assertFalse(res.notModified)
    }

    @Test
    fun `bundle without format has no query`() = test<Unit> {
        enqueueBytes(byteArrayOf(1))
        certs.bundle(7)
        expectRequest("GET", "/api/certs/7/bundle")
    }

    @Test
    fun `bundle 304 sends If-None-Match and reports not modified`() = test<Unit> {
        server.enqueue(MockResponse.Builder().code(304).setHeader("ETag", "\"fp-1\"").setHeader("X-Cert-Not-After", "2026-11-30T00:00:00Z").build())
        val res = certs.bundle(7, CertBundleFormat.ZIP, "\"fp-1\"")
        val r = expectRequest("GET", "/api/certs/7/bundle?format=zip")
        assertEquals("\"fp-1\"", r.headers["If-None-Match"])
        assertTrue(res.notModified)
        assertNull(res.content)
        assertEquals("\"fp-1\"", res.etag)
        assertEquals("2026-11-30T00:00:00Z", res.notAfter)
    }

    @Test
    fun `bundle 304 without etag header falls back to the sent one`() = test<Unit> {
        server.enqueue(MockResponse.Builder().code(304).build())
        val res = certs.bundle(7, ifNoneMatch = "\"old\"")
        takeRequest()
        assertTrue(res.notModified)
        assertEquals("\"old\"", res.etag)
        assertNull(res.notAfter)
    }

    @Test
    fun `CertBundleResult toString does not leak content`() {
        val res = CertBundleResult(content = "PRIVATE-KEY-MATERIAL".toByteArray(), etag = "\"e\"", notAfter = "2026-01-01")
        val s = res.toString()
        assertFalse(s.contains("PRIVATE"))
        assertFalse(s.contains("["))
        assertTrue(s.contains("20 bytes"))
        assertTrue(s.contains("\"e\""))
        assertTrue(CertBundleResult(null, "e", null).toString().contains("content=null"))
    }

    @Test
    fun `p12 posts password and legacy flag and returns exact bytes`() = test<Unit> {
        val p12 = byteArrayOf(0x30, -126, 0x0A, 0x00, -1, -2)
        enqueueBytes(p12, "application/x-pkcs12")
        val bytes = certs.p12(7, "p12-secret", legacy = true)
        val r = expectRequest("POST", "/api/certs/7/p12")
        assertEquals("application/x-pkcs12", r.headers["Accept"])
        assertEquals(js("""{"password":"p12-secret","legacy":true}"""), r.jsonBody())
        assertArrayEquals(p12, bytes)
    }

    @Test
    fun `p12 omits legacy when null`() = test<Unit> {
        enqueueBytes(byteArrayOf(1))
        certs.p12(7, "pw")
        assertEquals(js("""{"password":"pw"}"""), expectRequest("POST", "/api/certs/7/p12").jsonBody())
    }

    // ---- DNS accounts ----

    @Test
    fun `dnsProviders decodes fields`() = test<Unit> {
        enqueueJson(
            """[{"id":"cloudflare","name":"Cloudflare","docs":"https://docs","fields":[{"key":"token","label":"API Token","secret":true,"optional":false},
            {"key":"ttl","label":"TTL","secret":false,"optional":true,"default":"120"}]}]""",
        )
        val p = certs.dnsProviders().single()
        expectRequest("GET", "/api/certs/dns-providers")
        assertEquals("cloudflare", p.id)
        assertEquals("https://docs", p.docs)
        assertEquals(true, p.fields[0].secret)
        assertEquals(false, p.fields[0].optional)
        assertEquals("120", p.fields[1].default)
        assertEquals(true, p.fields[1].optional)
    }

    private val accountJson = """{"id":4,"name":"CF","provider":"cloudflare","provider_name":"Cloudflare","values":{"ttl":"120","token":"***"},
        "used_by":["cert:7"],"created_at":"2026-08-01T10:00:00Z","updated_at":"2026-08-02T10:00:00Z"}"""

    @Test
    fun `dnsAccounts decodes values map`() = test<Unit> {
        enqueueJson("[$accountJson]")
        val a = certs.dnsAccounts().single()
        expectRequest("GET", "/api/certs/dns-accounts")
        assertEquals(4L, a.id)
        assertEquals("Cloudflare", a.providerName)
        assertEquals(mapOf("ttl" to "120", "token" to "***"), a.values)
        assertEquals(listOf("cert:7"), a.usedBy)
        assertEquals(Instant.parse("2026-08-01T10:00:00Z"), a.createdAt)
        assertEquals(Instant.parse("2026-08-02T10:00:00Z"), a.updatedAt)
    }

    @Test
    fun `createDnsAccount posts values and redacts them`() = test<Unit> {
        enqueueJson(accountJson)
        val req = CreateDnsAccountRequest("CF", "cloudflare", mapOf("token" to "cf-secret-token"))
        assertEquals("CF", certs.createDnsAccount(req).name)
        assertEquals(
            js("""{"name":"CF","provider":"cloudflare","values":{"token":"cf-secret-token"}}"""),
            expectRequest("POST", "/api/certs/dns-accounts").jsonBody(),
        )
        assertFalse(req.toString().contains("cf-secret-token"))
        assertTrue(req.toString().contains("<redacted>"))
        assertTrue(req.toString().contains("cloudflare"))
    }

    @Test
    fun `updateDnsAccount puts partial and redacts values`() = test<Unit> {
        enqueueJson(accountJson)
        val req = UpdateDnsAccountRequest(values = mapOf("token" to "new-secret"))
        certs.updateDnsAccount(4, req)
        assertEquals(js("""{"values":{"token":"new-secret"}}"""), expectRequest("PUT", "/api/certs/dns-accounts/4").jsonBody())
        assertFalse(req.toString().contains("new-secret"))
        assertTrue(req.toString().contains("<redacted>"))
    }

    @Test
    fun `deleteDnsAccount`() = test<Unit> {
        enqueueNoContent()
        certs.deleteDnsAccount(4)
        expectRequest("DELETE", "/api/certs/dns-accounts/4")
    }

    // ---- CAs ----

    private val caJson = """{"id":2,"name":"Home CA","subject":"CN=Home CA","not_after":"2036-08-01T00:00:00Z","imported":false,"root":true,
        "permitted_dns":["home.lan"],"permitted_ip":["192.168.0.0/16"],"fingerprint_sha256":"AA:BB","certificates":3,"created_at":"2026-08-01T00:00:00Z"}"""

    private fun assertCa(c: CA) {
        assertEquals(2L, c.id)
        assertEquals("Home CA", c.name)
        assertEquals("CN=Home CA", c.subject)
        assertEquals(Instant.parse("2036-08-01T00:00:00Z"), c.notAfter)
        assertEquals(false, c.imported)
        assertEquals(true, c.root)
        assertEquals(listOf("home.lan"), c.permittedDns)
        assertEquals(listOf("192.168.0.0/16"), c.permittedIp)
        assertEquals("AA:BB", c.fingerprintSha256)
        assertEquals(3, c.certificates)
        assertEquals(Instant.parse("2026-08-01T00:00:00Z"), c.createdAt)
    }

    @Test
    fun `cas`() = test<Unit> {
        enqueueJson("[$caJson]")
        assertCa(certs.cas().single())
        expectRequest("GET", "/api/cas")
    }

    @Test
    fun `createCa posts request`() = test<Unit> {
        enqueueJson(caJson)
        assertCa(certs.createCa(CreateCaRequest("Home CA", CertificateKeyType.ECDSA, true, listOf("home.lan"))))
        assertEquals(
            js("""{"name":"Home CA","key_type":"ecdsa","constrained":true,"domains":["home.lan"]}"""),
            expectRequest("POST", "/api/cas").jsonBody(),
        )
    }

    @Test
    fun `createCa omits null optionals`() = test<Unit> {
        enqueueJson(caJson)
        certs.createCa(CreateCaRequest("X"))
        assertEquals(js("""{"name":"X"}"""), expectRequest("POST", "/api/cas").jsonBody())
    }

    @Test
    fun `importCa posts request and redacts secrets`() = test<Unit> {
        enqueueJson(caJson)
        val req = ImportCaRequest("Imp", "CA-CERT-PEM", "CA-KEY-SECRET", "ca-pass", "CA-P12-SECRET")
        assertCa(certs.importCa(req))
        assertEquals(
            js("""{"name":"Imp","cert":"CA-CERT-PEM","key":"CA-KEY-SECRET","password":"ca-pass","p12":"CA-P12-SECRET"}"""),
            expectRequest("POST", "/api/cas/import").jsonBody(),
        )
        val s = req.toString()
        assertFalse(s.contains("CA-KEY-SECRET"))
        assertFalse(s.contains("ca-pass"))
        assertFalse(s.contains("CA-P12-SECRET"))
        assertTrue(s.contains("CA-CERT-PEM"))
    }

    @Test
    fun `getCa`() = test<Unit> {
        enqueueJson(caJson)
        assertCa(certs.getCa(2))
        expectRequest("GET", "/api/cas/2")
    }

    @Test
    fun `renameCa puts name`() = test<Unit> {
        enqueueJson(caJson)
        assertCa(certs.renameCa(2, "New"))
        assertEquals(js("""{"name":"New"}"""), expectRequest("PUT", "/api/cas/2").jsonBody())
    }

    @Test
    fun `deleteCa`() = test<Unit> {
        enqueueNoContent()
        certs.deleteCa(2)
        expectRequest("DELETE", "/api/cas/2")
    }

    @Test
    fun `caFile returns exact bytes with format query and Accept`() = test<Unit> {
        val der = byteArrayOf(0x30, -126, 0x02, 0x00, -1, 0)
        enqueueBytes(der, "application/x-x509-ca-cert")
        val bytes = certs.caFile(2, CaFileFormat.CRT)
        val r = expectRequest("GET", "/api/cas/2/file?format=crt")
        assertEquals("*/*", r.headers["Accept"])
        assertArrayEquals(der, bytes)
    }

    @Test
    fun `caFile mobileconfig and default format`() = test<Unit> {
        val plist = "<plist>ü</plist>".toByteArray()
        enqueueBytes(plist, "application/x-apple-aspen-config")
        assertArrayEquals(plist, certs.caFile(2, CaFileFormat.MOBILECONFIG))
        expectRequest("GET", "/api/cas/2/file?format=mobileconfig")
        enqueueBytes(byteArrayOf(9))
        certs.caFile(2)
        expectRequest("GET", "/api/cas/2/file")
    }

    // ---- ACME CAs ----

    @Test
    fun `acmeCas decodes`() = test<Unit> {
        enqueueJson(
            """[{"id":6,"name":"ZeroSSL","directory":"https://acme.zerossl.com/v2/DV90","eab":true,"eab_kid":"kid1","custom_root":false,"certificates":2,
            "created_at":"2026-08-01T00:00:00Z"}]""",
        )
        val a = certs.acmeCas().single()
        expectRequest("GET", "/api/acme-cas")
        assertEquals(6L, a.id)
        assertEquals("https://acme.zerossl.com/v2/DV90", a.directory)
        assertEquals(true, a.eab)
        assertEquals("kid1", a.eabKid)
        assertEquals(false, a.customRoot)
        assertEquals(2, a.certificates)
        assertEquals(Instant.parse("2026-08-01T00:00:00Z"), a.createdAt)
    }

    @Test
    fun `createAcmeCa posts request and redacts hmac`() = test<Unit> {
        enqueueJson("""{"id":6,"name":"ZeroSSL","eab":true}""")
        val req = CreateAcmeCaRequest("ZeroSSL", "https://acme.zerossl.com/v2/DV90", true, "kid1", "HMAC-SECRET", "ROOT-PEM")
        assertEquals(6L, certs.createAcmeCa(req).id)
        assertEquals(
            js("""{"name":"ZeroSSL","directory":"https://acme.zerossl.com/v2/DV90","accept_tos":true,"eab_kid":"kid1","eab_hmac":"HMAC-SECRET","root_pem":"ROOT-PEM"}"""),
            expectRequest("POST", "/api/acme-cas").jsonBody(),
        )
        assertFalse(req.toString().contains("HMAC-SECRET"))
        assertTrue(req.toString().contains("<redacted>"))
        assertTrue(req.toString().contains("kid1"))
    }

    @Test
    fun `createAcmeCa sends accept_tos false and omits null optionals`() = test<Unit> {
        enqueueJson("""{"id":1}""")
        certs.createAcmeCa(CreateAcmeCaRequest("n", "https://d", false))
        assertEquals(js("""{"name":"n","directory":"https://d","accept_tos":false}"""), expectRequest("POST", "/api/acme-cas").jsonBody())
    }

    @Test
    fun `acmeCaPresets returns raw objects`() = test<Unit> {
        enqueueJson("""[{"id":"zerossl","name":"ZeroSSL","eab":true},{"id":"google"}]""")
        val p = certs.acmeCaPresets()
        expectRequest("GET", "/api/acme-cas/presets")
        assertEquals(2, p.size)
        assertEquals("zerossl", p[0]["id"]!!.jsonPrimitive.content)
        assertEquals("true", p[0].jsonObject["eab"]!!.jsonPrimitive.content)
    }

    @Test
    fun `deleteAcmeCa`() = test<Unit> {
        enqueueNoContent()
        certs.deleteAcmeCa(6)
        expectRequest("DELETE", "/api/acme-cas/6")
    }
}
