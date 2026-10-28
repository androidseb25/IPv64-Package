package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.BackupRequest
import de.sr.ipv64.node64.model.DashboardLayout
import de.sr.ipv64.node64.model.DashboardTile
import de.sr.ipv64.node64.model.DashboardTileSize
import de.sr.ipv64.node64.model.ExportRequest
import de.sr.ipv64.node64.model.ExportSecrets
import de.sr.ipv64.node64.model.LogLevel
import de.sr.ipv64.node64.model.ResetRequest
import de.sr.ipv64.node64.model.UpdateLoggingRequest
import de.sr.ipv64.node64.model.UpdateRetentionRequest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okio.ByteString.Companion.toByteString
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class NodeApiTest : Node64ApiTestBaseA() {

    @Test
    fun `health decodes status version and time`() = test {
        enqueueJson("""{"status":"ok","version":"1.4.2","time":"2026-10-28T10:00:00Z"}""")

        val health = node().node.health()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/health", request.target)
        assertEquals("Bearer node64_test", request.headers["Authorization"])
        assertEquals("ok", health.status)
        assertEquals("1.4.2", health.version)
        assertEquals(Instant.parse("2026-10-28T10:00:00Z"), health.time)
    }

    @Test
    fun `version decodes start time and host`() = test {
        enqueueJson("""{"version":"1.4.2","started_at":"2026-10-01T08:00:00+02:00","hostname":"node1","time":"2026-10-28 10:00:00"}""")

        val version = node().node.version()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/version", request.target)
        assertEquals(Instant.parse("2026-10-01T06:00:00Z"), version.startedAt)
        assertEquals("node1", version.hostname)
        assertEquals(Instant.parse("2026-10-28T10:00:00Z"), version.time)
    }

    @Test
    fun `status decodes node link modules and boards`() = test {
        enqueueJson(
            """{"node":{"id":"n-1","name":"Home","location":"Berlin","timezone":"Europe/Berlin","version":"1.4.2",
            "started_at":"2026-10-01T06:00:00Z","uptime_seconds":2332800,"database_bytes":1048576,"log_level":"info"},
            "link":{"linked":true,"connected":true,"remote_mgmt":"read","last_sync_at":"","linked_at":"2026-09-01T00:00:00Z"},
            "modules":[{"name":"checks","version":"2.1.0","enabled":true,"running":true,"available":true}],
            "boards":[{"module":"checks","ok":10,"down":1,"paused":2,"other":0}]}""",
        )

        val status = node().node.status()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/status", request.target)
        val n = status.node!!
        assertEquals("n-1", n.id)
        assertEquals(2332800L, n.uptimeSeconds)
        assertEquals(1048576L, n.databaseBytes)
        assertEquals(Instant.parse("2026-10-01T06:00:00Z"), n.startedAt)
        assertEquals(true, status.link!!.connected)
        assertNull(status.link!!.lastSyncAt)
        assertEquals(Instant.parse("2026-09-01T00:00:00Z"), status.link!!.linkedAt)
        assertEquals("checks", status.modules.single().name)
        val board = status.boards.single()
        assertEquals(10, board.ok)
        assertEquals(1, board.down)
        assertEquals(2, board.paused)
    }

    @Test
    fun `modules without changelog sends no query`() = test {
        enqueueJson(
            """[{"name":"checks","version":"2.1.0","changed":"2026-10-01","available":true,"enabled":true,
            "running":false,"path":"/checks","last_error":"boom"},{"name":"dns64","enabled":false}]""",
        )

        val modules = node().node.modules()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/modules", request.target)
        assertEquals(2, modules.size)
        assertEquals("/checks", modules[0].path)
        assertEquals("boom", modules[0].lastError)
        assertEquals(false, modules[0].running)
        assertEquals(false, modules[1].enabled)
    }

    @Test
    fun `modules with changelog sends changelog=1`() = test {
        enqueueJson("""[{"name":"checks","changelog":"# 2.1.0\n- fix"}]""")

        val modules = node().node.modules(changelog = true)

        assertEquals("/api/modules?changelog=1", takeRequest().target)
        assertEquals("# 2.1.0\n- fix", modules.single().changelog)
    }

    @Test
    fun `enableModule posts and returns null for empty body`() = test {
        enqueueEmpty()

        val result = node().node.enableModule("honeypot")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/modules/honeypot/enable", request.target)
        assertNull(result)
    }

    @Test
    fun `enableModule returns json when body present`() = test {
        enqueueJson("""{"ok":true}""")

        val result = node().node.enableModule("honeypot")

        assertEquals(JsonPrimitive(true), result!!.jsonObject["ok"])
    }

    @Test
    fun `disableModule posts and returns null for empty body`() = test {
        enqueueEmpty(204)

        val result = node().node.disableModule("honeypot")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/modules/honeypot/disable", request.target)
        assertNull(result)
    }

    @Test
    fun `metrics returns prometheus text and asks for text plain`() = test {
        val text = "# HELP node64_up Node up\n# TYPE node64_up gauge\nnode64_up 1\nnode64_checks{state=\"up\"} 12\n"
        enqueueText(text)

        val metrics = node().node.metrics()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/metrics", request.target)
        assertEquals("text/plain", request.headers["Accept"])
        assertEquals(text, metrics)
    }

    @Test
    fun `settings decodes key value map`() = test {
        enqueueJson("""{"node.name":"Home","node.location":"Berlin","notify.quiet_enabled":"1","updates.check":"0"}""")

        val settings = node().node.settings()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/settings", request.target)
        assertEquals(
            mapOf("node.name" to "Home", "node.location" to "Berlin", "notify.quiet_enabled" to "1", "updates.check" to "0"),
            settings,
        )
    }

    @Test
    fun `updateSettings puts the map and decodes the result`() = test {
        enqueueJson("""{"node.name":"Office","notify.rate_per_hour":"30"}""")

        val result = node().node.updateSettings(mapOf("node.name" to "Office", "notify.rate_per_hour" to "30"))

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/settings", request.target)
        request.assertJsonBody("""{"node.name":"Office","notify.rate_per_hour":"30"}""")
        assertEquals("Office", result["node.name"])
    }

    @Test
    fun `audit without paging has no query and decodes entries`() = test {
        enqueueJson(
            """[{"id":42,"time":"2026-10-28T09:00:00Z","actor":"android","action":"check.create","target":"abc",
            "details":"name=x","ip":"203.0.113.9","origin":"api"}]""",
        )

        val entries = node().node.audit()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/audit", request.target)
        val entry = entries.single()
        assertEquals(42L, entry.id)
        assertEquals(Instant.parse("2026-10-28T09:00:00Z"), entry.time)
        assertEquals("check.create", entry.action)
        assertEquals("203.0.113.9", entry.ip)
        assertEquals("api", entry.origin)
    }

    @Test
    fun `audit sends limit and before`() = test {
        enqueueJson("[]")

        node().node.audit(limit = 25, before = 100)

        assertEquals("/api/audit?limit=25&before=100", takeRequest().target)
    }

    @Test
    fun `logs sends lines and decodes level and lines`() = test {
        enqueueJson("""{"level":"info","lines":["plain line",{"msg":"started","lvl":"info"}]}""")

        val logs = node().node.logs(lines = 200)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/logs?lines=200", request.target)
        assertEquals("info", logs.level)
        assertEquals(2, logs.lines.size)
        assertEquals("plain line", logs.lines[0].jsonPrimitive.content)
        assertEquals("started", logs.lines[1].jsonObject["msg"]!!.jsonPrimitive.content)
    }

    @Test
    fun `logs without lines has no query`() = test {
        enqueueJson("""{"level":"debug","lines":[]}""")

        node().node.logs()

        assertEquals("/api/logs", takeRequest().target)
    }

    @Test
    fun `downloadDatabase returns exact bytes and asks for sqlite`() = test {
        val payload = byteArrayOf(0x53, 0x51, 0x4C, 0x69, 0x74, 0x65, 0x00, -1, -128, 0x7F)
        enqueueBytes(payload, "application/vnd.sqlite3")

        val bytes = node().node.downloadDatabase()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/backup", request.target)
        assertEquals("application/vnd.sqlite3", request.headers["Accept"])
        assertArrayEquals(payload, bytes)
    }

    @Test
    fun `backup posts json and returns exact archive bytes`() = test {
        val payload = ByteArray(300) { (it * 7).toByte() }
        enqueueBytes(payload)

        val bytes = node().node.backup(BackupRequest(password = "s3cret", stats = true))

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/backup", request.target)
        assertEquals("application/octet-stream", request.headers["Accept"])
        assertTrue(request.headers["Content-Type"]!!.startsWith("application/json"))
        request.assertJsonBody("""{"password":"s3cret","stats":true}""")
        assertArrayEquals(payload, bytes)
    }

    @Test
    fun `backup omits stats when null`() = test {
        enqueueBytes(byteArrayOf(1))

        node().node.backup(BackupRequest(password = "pw"))

        takeRequest().assertJsonBody("""{"password":"pw"}""")
    }

    @Test
    fun `restore sends raw octet stream with password header`() = test {
        enqueueJson("""{"restarting":true,"bytes":2048}""")
        val data = byteArrayOf(0, 1, 2, -1, -2, 0x4E, 0x36)

        val result = node().node.restore(data, password = "pw")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/backup/restore", request.target)
        assertEquals("application/octet-stream", request.headers["Content-Type"])
        assertEquals("pw", request.headers["X-Backup-Password"])
        assertArrayEquals(data, request.bodyBytes())
        assertEquals(true, result.restarting)
        assertEquals(2048L, result.bytes)
    }

    @Test
    fun `restore without password sends no password header`() = test {
        enqueueJson("""{"restarting":false}""")

        val result = node().node.restore(byteArrayOf(9))

        val request = takeRequest()
        assertNull(request.headers["X-Backup-Password"])
        assertEquals(false, result.restarting)
        assertNull(result.bytes)
    }

    @Test
    fun `restoreMultipart sends file and password fields`() = test {
        enqueueJson("""{"restarting":true,"bytes":5}""")
        val data = byteArrayOf(0x10, 0x20, -5, -6, 0x30)

        val result = node().node.restoreMultipart(data, fileName = "my.n64backup", password = "pw")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/backup/restore", request.target)
        assertTrue(request.headers["Content-Type"]!!.startsWith("multipart/form-data; boundary="))
        val body = request.body!!
        val text = body.utf8()
        assertTrue(text, text.contains("""Content-Disposition: form-data; name="file"; filename="my.n64backup""""))
        assertTrue(text.contains("Content-Type: application/octet-stream"))
        assertTrue(text.contains("""Content-Disposition: form-data; name="password""""))
        assertTrue(text.contains("\r\n\r\npw\r\n"))
        assertTrue(body.indexOf(data.toByteString()) >= 0)
        assertNull(request.headers["X-Backup-Password"])
        assertEquals(true, result.restarting)
    }

    @Test
    fun `restoreMultipart defaults file name and omits password`() = test {
        enqueueJson("""{"restarting":true}""")

        node().node.restoreMultipart(byteArrayOf(1, 2))

        val text = takeRequest().bodyText()
        assertTrue(text.contains("""filename="backup.n64backup""""))
        assertFalse(text.contains("""name="password""""))
    }

    @Test
    fun `openApi returns the document as json object`() = test {
        enqueueJson("""{"openapi":"3.0.3","info":{"title":"Node64","version":"1.4.2"},"paths":{"/health":{}}}""")

        val doc = node().node.openApi()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/openapi.json", request.target)
        assertEquals("3.0.3", doc["openapi"]!!.jsonPrimitive.content)
        assertEquals("Node64", doc["info"]!!.jsonObject["title"]!!.jsonPrimitive.content)
    }

    @Test
    fun `logging decodes level modules and known modules`() = test {
        enqueueJson("""{"level":"warn","modules":{"checks":"debug"},"known_modules":["checks","dns64","proxy"]}""")

        val config = node().node.logging()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/logging", request.target)
        assertEquals(LogLevel.WARN, config.level)
        assertEquals(mapOf("checks" to "debug"), config.modules)
        assertEquals(listOf("checks", "dns64", "proxy"), config.knownModules)
    }

    @Test
    fun `updateLogging puts the request and omits null fields`() = test {
        enqueueJson("""{"level":"debug","modules":{},"known_modules":[]}""")

        val config = node().node.updateLogging(UpdateLoggingRequest(level = LogLevel.DEBUG, resetMinutes = 30))

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/logging", request.target)
        request.assertJsonBody("""{"level":"debug","reset_minutes":30}""")
        assertEquals(LogLevel.DEBUG, config.level)
    }

    @Test
    fun `updateLogging sends module overrides`() = test {
        enqueueJson("""{"level":"info"}""")

        node().node.updateLogging(UpdateLoggingRequest(modules = mapOf("checks" to "error")))

        takeRequest().assertJsonBody("""{"modules":{"checks":"error"}}""")
    }

    @Test
    fun `retention decodes days detail and options`() = test {
        enqueueJson("""{"raw_days":14,"hourly_days":90,"chart_detail":"high","audit_days":365,"options":{"raw_days":[7,14,30]}}""")

        val retention = node().node.retention()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/retention", request.target)
        assertEquals(14, retention.rawDays)
        assertEquals(90, retention.hourlyDays)
        assertEquals("high", retention.chartDetail)
        assertEquals(365, retention.auditDays)
        assertNotNull(retention.options!!["raw_days"])
    }

    @Test
    fun `updateRetention sends only given fields`() = test {
        enqueueJson("""{"raw_days":30,"hourly_days":90}""")

        val retention = node().node.updateRetention(UpdateRetentionRequest(rawDays = 30, chartDetail = "low"))

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/retention", request.target)
        request.assertJsonBody("""{"raw_days":30,"chart_detail":"low"}""")
        assertEquals(30, retention.rawDays)
    }

    @Test
    fun `listApiKeys decodes keys without secret`() = test {
        enqueueJson(
            """[{"id":1,"name":"prometheus","hint":"…a1b2","created_at":"2026-09-01T10:00:00Z","last_used_at":"2026-10-28T09:59:00Z"},
            {"id":2,"name":"unused","hint":"…c3d4","created_at":"2026-09-02T10:00:00Z","last_used_at":null}]""",
        )

        val keys = node().node.listApiKeys()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/keys", request.target)
        assertEquals(2, keys.size)
        assertEquals(1L, keys[0].id)
        assertEquals("prometheus", keys[0].name)
        assertEquals("…a1b2", keys[0].hint)
        assertEquals(Instant.parse("2026-10-28T09:59:00Z"), keys[0].lastUsedAt)
        assertNull(keys[1].lastUsedAt)
    }

    @Test
    fun `createApiKey posts name and returns key once`() = test {
        enqueueJson(
            """{"id":3,"name":"android","hint":"…zz99","created_at":"2026-10-28T10:00:00Z","key":"node64_SUPERSECRETKEY"}""",
            code = 201,
        )

        val created = node().node.createApiKey("android")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/keys", request.target)
        request.assertJsonBody("""{"name":"android"}""")
        assertEquals(3L, created.id)
        assertEquals("node64_SUPERSECRETKEY", created.key)
        assertEquals(Instant.parse("2026-10-28T10:00:00Z"), created.createdAt)
        assertFalse(created.toString().contains("SUPERSECRETKEY"))
        assertTrue(created.toString().contains("<redacted>"))
    }

    @Test
    fun `deleteApiKey sends DELETE`() = test {
        enqueueNoContent()

        node().node.deleteApiKey(7)

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/keys/7", request.target)
    }

    @Test
    fun `exportConfig with defaults posts empty object`() = test {
        enqueueJson("""{"version":1,"settings":{"node.name":"Home"},"channels":[],"modules":["checks"]}""")

        val doc = node().node.exportConfig()

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/export", request.target)
        request.assertJsonBody("{}")
        assertEquals("1", doc["version"]!!.jsonPrimitive.content)
        assertEquals("Home", doc["settings"]!!.jsonObject["node.name"]!!.jsonPrimitive.content)
    }

    @Test
    fun `exportConfig with password sends secrets mode and password`() = test {
        enqueueJson("""{"version":1}""")

        node().node.exportConfig(ExportRequest(ExportSecrets.PASSWORD, "pw123"))

        takeRequest().assertJsonBody("""{"secrets":"password","password":"pw123"}""")
    }

    @Test
    fun `importConfig sends document and config password header`() = test {
        enqueueJson("""{"imported":7,"problems":["channel 3: invalid type"]}""")
        val config = buildJsonObject {
            put("version", JsonPrimitive(1))
            put("settings", buildJsonObject { put("node.name", JsonPrimitive("Home")) })
        }

        val result = node().node.importConfig(config, password = "pw123")

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/import", request.target)
        assertEquals("pw123", request.headers["X-Config-Password"])
        request.assertJsonBody("""{"version":1,"settings":{"node.name":"Home"}}""")
        assertEquals(7, result.imported)
        assertEquals(listOf("channel 3: invalid type"), result.problems)
    }

    @Test
    fun `importConfig without password sends no config password header`() = test {
        enqueueJson("""{"imported":0}""")

        val result = node().node.importConfig(JsonObject(emptyMap()))

        val request = takeRequest()
        assertNull(request.headers["X-Config-Password"])
        request.assertJsonBody("{}")
        assertEquals(0, result.imported)
        assertTrue(result.problems.isEmpty())
    }

    @Test
    fun `reset posts password and keep_checks`() = test {
        enqueueJson("""{"restarting":true,"cleanup_problems":["could not delete cert"]}""")

        val result = node().node.reset(ResetRequest(password = "pw", keepChecks = true))

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/reset", request.target)
        request.assertJsonBody("""{"password":"pw","keep_checks":true}""")
        assertEquals(true, result.restarting)
        assertEquals(listOf("could not delete cert"), result.cleanupProblems)
    }

    @Test
    fun `reset omits keep_checks when null`() = test {
        enqueueJson("""{"restarting":true}""")

        node().node.reset(ResetRequest(password = "pw"))

        takeRequest().assertJsonBody("""{"password":"pw"}""")
    }

    @Test
    fun `overview decodes all sections`() = test {
        enqueueJson(
            """{"node":{"id":"n-1","name":"Home","location":"Berlin","version":"1.4.2",
            "started_at":"2026-10-01T06:00:00Z","db_bytes":4096},
            "modules":[{"name":"checks","version":"2.1.0","enabled":true,"running":true,"last_error":""}],
            "traffic":{"available":true,"rx_since_start":100,"tx_since_start":200,"rx_total":300,"tx_total":400,
            "since":"2026-09-01T00:00:00Z"},
            "healthchecks":{"checks_by_location":{"node":5,"fra":2},"nodes":3,"nodes_online":2},
            "account":{"account":"user@example.com","plan":"pro","plan_until":"2027-01-01T00:00:00Z",
            "checked_at":"2026-10-28T09:00:00Z","error":"",
            "limits":[{"key":"checks","used":5.0,"max":50.0,"unlimited":false,"pct":10}]},
            "update":{"available":true,"version":"1.5.0"}}""",
        )

        val overview = node().node.overview()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/overview", request.target)
        assertEquals(4096L, overview.node!!.dbBytes)
        assertEquals(Instant.parse("2026-10-01T06:00:00Z"), overview.node!!.startedAt)
        assertEquals(true, overview.modules.single().running)
        val traffic = overview.traffic!!
        assertEquals(100L, traffic.rxSinceStart)
        assertEquals(400L, traffic.txTotal)
        assertEquals(Instant.parse("2026-09-01T00:00:00Z"), traffic.since)
        assertEquals(mapOf("node" to 5, "fra" to 2), overview.healthchecks!!.checksByLocation)
        assertEquals(2, overview.healthchecks!!.nodesOnline)
        val account = overview.account!!
        assertEquals("pro", account.plan)
        assertEquals(Instant.parse("2027-01-01T00:00:00Z"), account.planUntil)
        val limit = account.limits.single()
        assertEquals("checks", limit.key)
        assertEquals(50.0, limit.max!!, 0.0)
        assertEquals(10, limit.pct)
        assertEquals(JsonPrimitive("1.5.0"), overview.update!!["version"])
    }

    @Test
    fun `dashboardLayout decodes tiles with sizes`() = test {
        enqueueJson("""{"items":[{"id":"checks","size":"xl","hidden":false},{"id":"traffic","size":"s","hidden":true}]}""")

        val layout = node().node.dashboardLayout()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/dashboard/layout", request.target)
        assertEquals(2, layout.items.size)
        assertEquals(DashboardTileSize.XL, layout.items[0].size)
        assertEquals(DashboardTileSize.S, layout.items[1].size)
        assertEquals(true, layout.items[1].hidden)
    }

    @Test
    fun `updateDashboardLayout puts tiles`() = test {
        enqueueJson("""{"items":[{"id":"checks","size":"m"}]}""")

        val layout = node().node.updateDashboardLayout(
            DashboardLayout(listOf(DashboardTile("checks", DashboardTileSize.M, null), DashboardTile("traffic", null, true))),
        )

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/dashboard/layout", request.target)
        request.assertJsonBody("""{"items":[{"id":"checks","size":"m"},{"id":"traffic","hidden":true}]}""")
        assertEquals(DashboardTileSize.M, layout.items.single().size)
    }

    @Test
    fun `resetDashboardLayout sends DELETE`() = test {
        enqueueNoContent()

        node().node.resetDashboardLayout()

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/dashboard/layout", request.target)
    }

    @Test
    fun `secret bearing request classes redact toString`() {
        assertFalse(BackupRequest("topsecret", true).toString().contains("topsecret"))
        assertFalse(ResetRequest("topsecret", true).toString().contains("topsecret"))
        assertFalse(ExportRequest(ExportSecrets.PASSWORD, "topsecret").toString().contains("topsecret"))
    }
}
