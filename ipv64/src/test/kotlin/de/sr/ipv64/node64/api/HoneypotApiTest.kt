package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.Honeypot64Config
import de.sr.ipv64.node64.model.Honeypot64ConfigRequest
import de.sr.ipv64.node64.model.Honeypot64LogConfig
import de.sr.ipv64.node64.model.Honeypot64LogFile
import de.sr.ipv64.node64.model.Honeypot64LoginConfig
import de.sr.ipv64.node64.model.Honeypot64ServiceConfig
import de.sr.ipv64.node64.model.HoneypotEventSource
import de.sr.ipv64.node64.model.HoneypotGroup
import de.sr.ipv64.node64.model.HoneypotLiveGroup
import de.sr.ipv64.node64.model.HoneypotLogFormat
import de.sr.ipv64.node64.model.HoneypotPage
import de.sr.ipv64.node64.model.HoneypotPortState
import de.sr.ipv64.node64.model.HoneypotProfile
import de.sr.ipv64.node64.model.HoneypotProto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Instant

class HoneypotApiTest : Node64ApiTestBaseA() {

    @Test
    fun `status decodes counters services report and logs`() = test {
        enqueueJson(
            """{"version":"1.2.0","running":true,"started_at":"2026-10-20T00:00:00Z","profile":"standard","scan":true,
            "services":[{"service":"ssh","port":2222,"listen":true,"conns":15},{"service":"ftp","port":21,"listen":false,"error":"in use"}],
            "warnings":[{"service":"http","port":80,"owner":"nginx","purpose":"web","skipped":true}],
            "counters":{"connections":100,"logins":40,"events":150,"dropped":2},"buffered":3,"dropped":1,
            "report":{"enabled":true,"key_set":true,"active":true,"sensor_id":"s-1","last_success":"2026-10-28T09:00:00Z",
            "last_end":"2026-10-28T09:00:05Z","last_error":"","last_error_at":"2026-10-27T09:00:00Z",
            "next_attempt":"2026-10-28T10:00:00Z","errors":1,"sources_today":5,"sources_total":50,"last_rejected":2,
            "last_rejected_reasons":{"private":2}},
            "logs":{"files":[{"path":"/var/log/auth.log","format":"syslog","ok":true,"lines":99,"last":"2026-10-28T09:59:00Z"}],
            "syslog":{"port":5514,"udp":true,"tcp":false,"messages":7,"denied":1},"lines":99,"hits":4,"events":4}}""",
        )

        val status = node().honeypot.status()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/honeypot/status", request.target)
        assertEquals(true, status.running)
        assertEquals(Instant.parse("2026-10-20T00:00:00Z"), status.startedAt)
        assertEquals(2, status.services.size)
        assertEquals(2222, status.services[0].port)
        assertEquals(15L, status.services[0].conns)
        assertEquals("in use", status.services[1].error)
        assertEquals(true, status.warnings.single().skipped)
        assertEquals(150L, status.counters!!.events)
        val report = status.report!!
        assertEquals("s-1", report.sensorId)
        assertEquals(Instant.parse("2026-10-28T09:00:00Z"), report.lastSuccess)
        assertEquals(Instant.parse("2026-10-28T10:00:00Z"), report.nextAttempt)
        assertEquals(mapOf("private" to 2), report.lastRejectedReasons)
        val logs = status.logs!!
        assertEquals("/var/log/auth.log", logs.files.single().path)
        assertEquals(Instant.parse("2026-10-28T09:59:00Z"), logs.files.single().last)
        assertEquals(5514, logs.syslog!!.port)
        assertEquals(false, logs.syslog!!.tcp)
    }

    @Test
    fun `stats without params has no query and decodes counts`() = test {
        enqueueJson(
            """{"since":"2026-10-21T00:00:00Z","events":500,"sources":40,"logins":120,
            "by_service":[{"key":"ssh","count":300,"last":"2026-10-28T09:00:00Z"}],
            "by_port":[{"key":"22","count":10}],"top_sources":[{"key":"198.51.100.4","count":99,"country":"DE"}],
            "top_users":[{"key":"root","count":50}],"top_passwords":[{"key":"123456","count":20}],
            "timeline":[{"time":"2026-10-28T08:00:00Z","events":12}],
            "by_country":[{"key":"DE","count":30,"sources":5,"last":"2026-10-28T09:00:00Z"}],"country_unknown":3,
            "by_decoy":[{"key":"wp-login","count":4}]}""",
        )

        val stats = node().honeypot.stats()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/honeypot/stats", request.target)
        assertEquals(Instant.parse("2026-10-21T00:00:00Z"), stats.since)
        assertEquals(500L, stats.events)
        assertEquals("ssh", stats.byService.single().key)
        assertEquals(Instant.parse("2026-10-28T09:00:00Z"), stats.byService.single().last)
        assertEquals("DE", stats.topSources.single().country)
        assertEquals("123456", stats.topPasswords.single().key)
        assertEquals(12L, stats.timeline.single().events)
        assertEquals(5L, stats.byCountry.single().sources)
        assertEquals(3L, stats.countryUnknown)
        assertEquals("wp-login", stats.byDecoy.single().key)
    }

    @Test
    fun `stats sends since and top`() = test {
        enqueueJson("{}")

        node().honeypot.stats(since = "24h", top = 5)

        assertEquals("/api/honeypot/stats?since=24h&top=5", takeRequest().target)
    }

    @Test
    fun `events without filters has no query and decodes events`() = test {
        enqueueJson(
            """[{"time":"2026-10-28T09:00:00Z","service":"ssh","port":2222,"kind":"login","src":"198.51.100.4",
            "src_port":51000,"country":"DE","username":"root","password":"toor","method":"","path":"","host":"",
            "user_agent":"","client":"SSH-2.0-libssh","page":"","detail":"","payload_hex":"00ff","payload_text":"hi",
            "source":"honeypot","rule":"r1","fingerprint":"fp","sni":"example.com"}]""",
        )

        val events = node().honeypot.events()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/honeypot/events", request.target)
        val event = events.single()
        assertEquals(Instant.parse("2026-10-28T09:00:00Z"), event.time)
        assertEquals(51000, event.srcPort)
        assertEquals("root", event.username)
        assertEquals("toor", event.password)
        assertEquals("00ff", event.payloadHex)
        assertEquals(HoneypotEventSource.HONEYPOT, event.source)
        assertEquals("example.com", event.sni)
    }

    @Test
    fun `events sends limit source and country`() = test {
        enqueueJson("[]")

        node().honeypot.events(limit = 10, source = HoneypotEventSource.SYSLOG, country = "DE")

        assertEquals("/api/honeypot/events?limit=10&source=syslog&country=DE", takeRequest().target)
    }

    @Test
    fun `clearEvents sends DELETE and decodes count`() = test {
        enqueueJson("""{"deleted":123}""")

        val result = node().honeypot.clearEvents()

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/honeypot/events", request.target)
        assertEquals(123L, result.deleted)
    }

    private val configJson = """{"profile":"custom","services":[{"service":"http","port":8080,"page":"wp-login"},{"service":"ssh","port":2222}],
        "scan":true,"max_conns_per_ip":5,"max_conns_total":200,"retention_days":30,"max_events":10000,"notify_new":true,
        "report":false,"no_local_block":true,"country":"DE","log":{"files":[{"path":"/var/log/nginx/access.log","format":"nginx"}],
        "syslog_port":5514,"syslog_allow":["10.0.0.0/8"]},"cert_id":4,"cert_ports":[443,8443],
        "login":{"off":false,"threshold":3,"window_min":10,"no_notify":true}}"""

    private fun assertConfig(c: Honeypot64Config) {
        assertEquals(HoneypotProfile.CUSTOM, c.profile)
        assertEquals(2, c.services.size)
        assertEquals(HoneypotPage.WP_LOGIN, c.services[0].page)
        assertNull(c.services[1].page)
        assertEquals(5, c.maxConnsPerIp)
        assertEquals(200, c.maxConnsTotal)
        assertEquals(30, c.retentionDays)
        assertEquals(10000, c.maxEvents)
        assertEquals(true, c.noLocalBlock)
        assertEquals("DE", c.country)
        assertEquals(HoneypotLogFormat.NGINX, c.log!!.files.single().format)
        assertEquals(5514, c.log!!.syslogPort)
        assertEquals(listOf("10.0.0.0/8"), c.log!!.syslogAllow)
        assertEquals(4L, c.certId)
        assertEquals(listOf(443, 8443), c.certPorts)
        assertEquals(3, c.login!!.threshold)
        assertEquals(10, c.login!!.windowMin)
        assertEquals(true, c.login!!.noNotify)
    }

    @Test
    fun `config decodes full configuration`() = test {
        enqueueJson(configJson)

        assertConfig(node().honeypot.config())

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/honeypot/config", request.target)
    }

    @Test
    fun `updateConfig puts nested request without nulls`() = test {
        enqueueJson(configJson)

        val config = node().honeypot.updateConfig(
            Honeypot64ConfigRequest(
                profile = HoneypotProfile.CUSTOM,
                services = listOf(Honeypot64ServiceConfig("http", 8080, HoneypotPage.WP_LOGIN), Honeypot64ServiceConfig("ssh", 2222)),
                maxConnsPerIp = 5,
                log = Honeypot64LogConfig(listOf(Honeypot64LogFile("/var/log/x.log", HoneypotLogFormat.AUTO)), syslogPort = 5514),
                certPorts = listOf(443),
                login = Honeypot64LoginConfig(threshold = 3),
            ),
        )

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/honeypot/config", request.target)
        request.assertJsonBody(
            """{"profile":"custom","services":[{"service":"http","port":8080,"page":"wp-login"},{"service":"ssh","port":2222}],
            "max_conns_per_ip":5,"log":{"files":[{"path":"/var/log/x.log","format":"auto"}],"syslog_port":5514},
            "cert_ports":[443],"login":{"threshold":3}}""",
        )
        assertConfig(config)
    }

    @Test
    fun `catalog decodes entries presets and enums`() = test {
        enqueueJson(
            """{"profile":"standard","running":true,
            "entries":[{"service":"ssh","port":2222,"default_port":22,"page":"","proto":"tcp","group":"remote",
            "description":"SSH","profiles":["standard","full"],"on":true,"state":"open","owner":"","purpose":"","error":"","conns":4},
            {"service":"snmp","port":161,"default_port":161,"proto":"udp","group":"iot","on":false,"state":"used","owner":"snmpd"}],
            "extra":[{"service":"custom","port":9999,"state":"free"}],
            "services":["ssh","ftp"],"pages":["generic","admin"],"groups":["remote","web"],
            "presets":{"minimal":[{"service":"ssh","port":2222}]}}""",
        )

        val catalog = node().honeypot.catalog()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/honeypot/catalog", request.target)
        assertEquals(true, catalog.running)
        assertEquals(2, catalog.entries.size)
        val ssh = catalog.entries[0]
        assertEquals(HoneypotProto.TCP, ssh.proto)
        assertEquals(HoneypotGroup.REMOTE, ssh.group)
        assertEquals(HoneypotPortState.OPEN, ssh.state)
        assertEquals(22, ssh.defaultPort)
        assertEquals(listOf("standard", "full"), ssh.profiles)
        assertEquals(4L, ssh.conns)
        assertEquals(HoneypotProto.UDP, catalog.entries[1].proto)
        assertEquals(HoneypotGroup.IOT, catalog.entries[1].group)
        assertEquals(HoneypotPortState.USED, catalog.entries[1].state)
        assertEquals("snmpd", catalog.entries[1].owner)
        assertEquals(HoneypotPortState.FREE, catalog.extra.single().state)
        assertEquals(listOf("ssh", "ftp"), catalog.services)
        assertEquals(listOf("generic", "admin"), catalog.pages)
        assertEquals(2222, catalog.presets["minimal"]!!.single().port)
    }

    @Test
    fun `live without minutes has no query and decodes target and events`() = test {
        enqueueJson(
            """{"minutes":15,"now":1793000000,"target":{"cc":"DE","flag":"DE","name":"Germany","x":0.52,"y":0.31,"known":true,"manual":false},
            "events":[{"t":1792999990,"cc":"US","flag":"US","name":"United States","x":0.2,"y":0.4,"ip":"198.51.100.4",
            "kind":"login","group":"login","service":"ssh","port":22,"source":"honeypot"}]}""",
        )

        val live = node().honeypot.live()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/honeypot/live", request.target)
        assertEquals(15, live.minutes)
        assertEquals(1793000000L, live.now)
        assertEquals("Germany", live.target!!.name)
        assertEquals(0.52, live.target!!.x!!, 0.0)
        assertEquals(true, live.target!!.known)
        val event = live.events.single()
        assertEquals(1792999990L, event.t)
        assertEquals(HoneypotLiveGroup.LOGIN, event.group)
        assertEquals("198.51.100.4", event.ip)
        assertEquals(22, event.port)
    }

    @Test
    fun `live sends minutes`() = test {
        enqueueJson("{}")

        node().honeypot.live(minutes = 60)

        assertEquals("/api/honeypot/live?minutes=60", takeRequest().target)
    }
}
