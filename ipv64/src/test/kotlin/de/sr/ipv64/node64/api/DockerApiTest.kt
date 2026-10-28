package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.DockerContainerKind
import de.sr.ipv64.node64.model.DockerErrorDetail
import de.sr.ipv64.node64.model.DockerHealth
import de.sr.ipv64.node64.model.DockerMonitorState
import de.sr.ipv64.node64.model.DockerSystemRequest
import de.sr.ipv64.node64.model.PVEResourceState
import de.sr.ipv64.node64.model.PVEResourceType
import de.sr.ipv64.node64.model.PVERuleSet
import de.sr.ipv64.node64.model.PVESettingsInput
import de.sr.ipv64.node64.model.PVESystemInput
import de.sr.ipv64.node64.model.PVESystemKind
import de.sr.ipv64.node64.model.PVESystemState
import de.sr.ipv64.node64.model.PVEThreshold
import de.sr.ipv64.node64.model.SeriesRange
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import kotlin.time.Instant

class DockerApiTest : Node64ApiTestBaseA() {

    private val systemJson = """{"id":6,"kind":"docker","name":"Host","addresses":["tcp://10.0.0.5:2375"],"interval":30,
        "enabled":true,"created":"2026-09-01T10:00:00Z","uid":"u-1","home":"h","has_secret":false,"state":"warn",
        "error":"","version":"27.0","last_poll":"2026-10-28T09:59:00Z","guests":10,"guests_running":8,"guests_bad":1}"""

    private fun assertSystem(s: de.sr.ipv64.node64.model.PVESystem) {
        assertEquals(6L, s.id)
        assertEquals(PVESystemKind.DOCKER, s.kind)
        assertEquals(listOf("tcp://10.0.0.5:2375"), s.addresses)
        assertEquals(30, s.interval)
        assertEquals(Instant.parse("2026-09-01T10:00:00Z"), s.created)
        assertEquals(false, s.hasSecret)
        assertEquals(PVESystemState.WARN, s.state)
        assertEquals("27.0", s.version)
        assertEquals(Instant.parse("2026-10-28T09:59:00Z"), s.lastPoll)
        assertEquals(10, s.guests)
        assertEquals(8, s.guestsRunning)
        assertEquals(1, s.guestsBad)
    }

    @Test
    fun `containers decodes containers with metrics and enums`() = test {
        enqueueJson(
            """{"host":"unix:///var/run/docker.sock","socket":true,"error":"","system_id":6,"monitored":true,
            "containers":[{"name":"web","kind":"running","health":"healthy","id":"abc123","image":"nginx:1","state":"running",
            "created":"2026-10-01T10:00:00Z","ports":["80/tcp","443/tcp"],
            "check":{"token":"ck1","name":"web check","state":"up","message":"ok"},"key":"abc123","monitor":"running",
            "detail":"oom","bad":true,"as_check":true,"excluded":false,"muted":true,"cpu":1.5,"cpus":4,"mem":1048576,
            "max_mem":2097152,"net_in":10.5,"net_out":20.5,"disk_read":1.0,"disk_write":2.0,"uptime":3600,
            "restart_count":3,"exit_code":137}]}""",
        )

        val result = node().docker.containers()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/healthchecks/docker/containers", request.target)
        assertEquals("unix:///var/run/docker.sock", result.host)
        assertEquals(true, result.socket)
        assertEquals(6L, result.systemId)
        assertEquals(true, result.monitored)
        val c = result.containers.single()
        assertEquals("web", c.name)
        assertEquals(DockerContainerKind.RUNNING, c.kind)
        assertEquals(DockerHealth.HEALTHY, c.health)
        assertEquals(Instant.parse("2026-10-01T10:00:00Z"), c.created)
        assertEquals(listOf("80/tcp", "443/tcp"), c.ports)
        assertEquals("ck1", c.check!!.token)
        assertEquals(DockerMonitorState.RUNNING, c.monitor)
        assertEquals(DockerErrorDetail.OOM, c.detail)
        assertEquals(true, c.bad)
        assertEquals(true, c.asCheck)
        assertEquals(true, c.muted)
        assertEquals(1.5, c.cpu!!, 0.0)
        assertEquals(1048576L, c.mem)
        assertEquals(2097152L, c.maxMem)
        assertEquals(10.5, c.netIn!!, 0.0)
        assertEquals(3600L, c.uptime)
        assertEquals(3, c.restartCount)
        assertEquals(137, c.exitCode)
    }

    @Test
    fun `systems decodes list`() = test {
        enqueueJson("[$systemJson]")

        val systems = node().docker.systems()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/healthchecks/docker/systems", request.target)
        assertSystem(systems.single())
    }

    @Test
    fun `createSystem with defaults posts empty object`() = test {
        enqueueJson(systemJson, code = 201)

        assertSystem(node().docker.createSystem())

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/healthchecks/docker/systems", request.target)
        request.assertJsonBody("{}")
    }

    @Test
    fun `createSystem sends given fields`() = test {
        enqueueJson(systemJson, code = 201)

        node().docker.createSystem(DockerSystemRequest("Host", listOf("tcp://10.0.0.5:2375"), 30, true))

        takeRequest().assertJsonBody("""{"name":"Host","addresses":["tcp://10.0.0.5:2375"],"interval":30,"enabled":true}""")
    }

    @Test
    fun `getSystem fetches by id`() = test {
        enqueueJson(systemJson)

        assertSystem(node().docker.getSystem(6))

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/healthchecks/docker/systems/6", request.target)
    }

    @Test
    fun `updateSystem puts input and never leaks secret in toString`() = test {
        enqueueJson(systemJson)
        val input = PVESystemInput(name = "Host2", secret = "TOKEN-SECRET", interval = 60)

        assertSystem(node().docker.updateSystem(6, input))

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/healthchecks/docker/systems/6", request.target)
        request.assertJsonBody("""{"name":"Host2","secret":"TOKEN-SECRET","interval":60}""")
        assertFalse(input.toString().contains("TOKEN-SECRET"))
    }

    @Test
    fun `deleteSystem sends DELETE`() = test {
        enqueueNoContent()

        node().docker.deleteSystem(6)

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/healthchecks/docker/systems/6", request.target)
    }

    @Test
    fun `resources decodes system and docker resources`() = test {
        enqueueJson(
            """{"system":$systemJson,"resources":[{"key":"docker/abc","type":"docker","node":"host","name":"web",
            "state":"running","status":"Up 2 days","tags":["a"],"cpu":0.5,"max_cpu":2,"mem":100,"max_mem":200,"disk":10,
            "max_disk":20,"uptime":99,"net_in":1.5,"net_out":2.5,"disk_read":3.5,"disk_write":4.5,"restarted":false,
            "bad":false,"excluded":false,"muted":false,"as_check":true,"check_token":"ck1","container_id":"abc","image":"nginx",
            "health":"unhealthy","restart_count":2,"exit_code":1,"oom_killed":true,"created":1790000000,
            "ports":["80/tcp"],"labels":{"com.docker.compose.project":"shop"},"detail":"unhealthy"}]}""",
        )

        val res = node().docker.resources(6)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/healthchecks/docker/systems/6/resources", request.target)
        assertSystem(res.system!!)
        val r = res.resources.single()
        assertEquals(PVEResourceType.DOCKER, r.type)
        assertEquals(PVEResourceState.RUNNING, r.state)
        assertEquals(DockerHealth.UNHEALTHY, r.health)
        assertEquals(DockerErrorDetail.UNHEALTHY, r.detail)
        assertEquals(2.0, r.maxCpu!!, 0.0)
        assertEquals(200L, r.maxMem)
        assertEquals("ck1", r.checkToken)
        assertEquals("abc", r.containerId)
        assertEquals(true, r.oomKilled)
        assertEquals(1790000000L, r.created)
        assertEquals(mapOf("com.docker.compose.project" to "shop"), r.labels)
        assertEquals(listOf("a"), r.tags)
    }

    @Test
    fun `series sends key and range`() = test {
        enqueueJson(
            """{"key":"docker/abc","range":"24h","step_seconds":300,"from":"2026-10-27T10:00:00Z","to":"2026-10-28T10:00:00Z",
            "points":[{"t":"2026-10-27T10:00:00Z","cpu":0.1,"cpu_max":0.4,"mem":100.0,"disk":5.0,"net_in":1.0,"net_out":2.0,
            "disk_read":3.0,"disk_write":4.0}]}""",
        )

        val series = node().docker.series(6, "docker/abc", SeriesRange.HOURS_24)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/healthchecks/docker/systems/6/series?key=docker%2Fabc&range=24h", request.target)
        assertEquals("docker/abc", series.key)
        assertEquals(300, series.stepSeconds)
        assertEquals(Instant.parse("2026-10-27T10:00:00Z"), series.from)
        val p = series.points.single()
        assertEquals(Instant.parse("2026-10-27T10:00:00Z"), p.t)
        assertEquals(0.4, p.cpuMax!!, 0.0)
        assertEquals(4.0, p.diskWrite!!, 0.0)
    }

    @Test
    fun `series without range sends only key`() = test {
        enqueueJson("{}")

        node().docker.series(6, "k")

        assertEquals("/api/healthchecks/docker/systems/6/series?key=k", takeRequest().target)
    }

    private val settingsOverviewJson = """{"system_id":6,"items":{"docker/abc":{"system_id":6,"key":"docker/abc","muted":false,
        "as_check":true,"check_token":"ck1","expect_running":true,"seen_running":true,
        "rules":{"crashed":true,"stopped":false,"cpu":{"percent":90,"minutes":5},"delay":2,"channels":[1,2]}}},
        "effective":{"crashed":true},"defaults":{"crashed":false}}"""

    @Test
    fun `settings decodes overview with items`() = test {
        enqueueJson(settingsOverviewJson)

        val overview = node().docker.settings(6)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/healthchecks/docker/systems/6/settings", request.target)
        assertEquals(6L, overview.systemId)
        val item = overview.items.getValue("docker/abc")
        assertEquals("ck1", item.checkToken)
        assertEquals(true, item.expectRunning)
        assertEquals(true, item.rules!!.crashed)
        assertEquals(false, item.rules!!.stopped)
        assertEquals(90.0, item.rules!!.cpu!!.percent!!, 0.0)
        assertEquals(5, item.rules!!.cpu!!.minutes)
        assertEquals(listOf(1L, 2L), item.rules!!.channels)
        assertEquals(JsonPrimitive(true), overview.effective!!["crashed"])
        assertEquals(JsonPrimitive(false), overview.defaults!!["crashed"])
    }

    @Test
    fun `updateSettings puts input with key query`() = test {
        enqueueJson("""{"settings":{"system_id":6,"key":"docker/abc","muted":true},"effective":{"crashed":true}}""")

        val result = node().docker.updateSettings(
            6,
            PVESettingsInput(muted = true, expectRunning = JsonPrimitive(true), rules = PVERuleSet(crashed = true, cpu = PVEThreshold(80.0, 3))),
            key = "docker/abc",
        )

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/healthchecks/docker/systems/6/settings?key=docker%2Fabc", request.target)
        request.assertJsonBody("""{"muted":true,"expect_running":true,"rules":{"crashed":true,"cpu":{"percent":80.0,"minutes":3}}}""")
        assertEquals(true, result.settings!!.muted)
        assertEquals("docker/abc", result.settings!!.key)
        assertEquals(JsonPrimitive(true), result.effective!!["crashed"])
    }

    @Test
    fun `updateSettings without key has no query`() = test {
        enqueueJson("{}")

        node().docker.updateSettings(6, PVESettingsInput(asCheck = false))

        val request = takeRequest()
        assertEquals("/api/healthchecks/docker/systems/6/settings", request.target)
        request.assertJsonBody("""{"as_check":false}""")
    }

    @Test
    fun `resetSettings sends DELETE with key`() = test {
        enqueueNoContent()
        enqueueNoContent()

        node().docker.resetSettings(6, "docker/abc")
        node().docker.resetSettings(6)

        val first = takeRequest()
        assertEquals("DELETE", first.method)
        assertEquals("/api/healthchecks/docker/systems/6/settings?key=docker%2Fabc", first.target)
        assertEquals("/api/healthchecks/docker/systems/6/settings", takeRequest().target)
    }
}
