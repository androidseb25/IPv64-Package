package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.PVEProbeRequest
import de.sr.ipv64.node64.model.PVEResourceState
import de.sr.ipv64.node64.model.PVEResourceType
import de.sr.ipv64.node64.model.PVERuleSet
import de.sr.ipv64.node64.model.PVESettingsInput
import de.sr.ipv64.node64.model.PVESystemInput
import de.sr.ipv64.node64.model.PVESystemKind
import de.sr.ipv64.node64.model.PVESystemKindFilter
import de.sr.ipv64.node64.model.PVESystemState
import de.sr.ipv64.node64.model.PVEThreshold
import de.sr.ipv64.node64.model.SeriesRange
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import kotlin.time.Instant

class ProxmoxApiTest : Node64ApiTestBaseA() {

    private val systemJson = """{"id":1,"kind":"pve","name":"Cluster","addresses":["10.0.0.1:8006","10.0.0.2:8006"],
        "token_id":"root@pam!node64","pin_sha256":"AA:BB","interval":60,"enabled":true,"created":"2026-09-01T10:00:00Z",
        "has_secret":true,"state":"ok","error":"","version":"8.2.4","cluster":"homelab","quorate":true,"address":"10.0.0.1:8006",
        "last_poll":"2026-10-28T09:59:00Z","nodes":3,"nodes_online":2,"guests":20,"guests_running":15,"guests_bad":2}"""

    private fun assertSystem(s: de.sr.ipv64.node64.model.PVESystem) {
        assertEquals(1L, s.id)
        assertEquals(PVESystemKind.PVE, s.kind)
        assertEquals(listOf("10.0.0.1:8006", "10.0.0.2:8006"), s.addresses)
        assertEquals("root@pam!node64", s.tokenId)
        assertEquals("AA:BB", s.pinSha256)
        assertEquals(60, s.interval)
        assertEquals(Instant.parse("2026-09-01T10:00:00Z"), s.created)
        assertEquals(true, s.hasSecret)
        assertEquals(PVESystemState.OK, s.state)
        assertEquals("8.2.4", s.version)
        assertEquals("homelab", s.cluster)
        assertEquals(true, s.quorate)
        assertEquals(Instant.parse("2026-10-28T09:59:00Z"), s.lastPoll)
        assertEquals(3, s.nodes)
        assertEquals(2, s.nodesOnline)
        assertEquals(20, s.guests)
        assertEquals(15, s.guestsRunning)
        assertEquals(2, s.guestsBad)
    }

    @Test
    fun `systems without filter has no query`() = test {
        enqueueJson("[$systemJson]")

        val systems = node().proxmox.systems()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/healthchecks/proxmox/systems", request.target)
        assertSystem(systems.single())
    }

    @Test
    fun `systems sends kind filter`() = test {
        enqueueJson("[]")

        node().proxmox.systems(PVESystemKindFilter.ALL)

        assertEquals("/api/healthchecks/proxmox/systems?kind=all", takeRequest().target)
    }

    @Test
    fun `createSystem posts input without nulls and redacts secret in toString`() = test {
        enqueueJson(systemJson, code = 201)
        val input = PVESystemInput(
            name = "Cluster",
            addresses = listOf("10.0.0.1:8006"),
            tokenId = "root@pam!node64",
            secret = "PVE-SECRET",
            interval = 60,
        )

        assertSystem(node().proxmox.createSystem(input))

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/healthchecks/proxmox/systems", request.target)
        request.assertJsonBody(
            """{"name":"Cluster","addresses":["10.0.0.1:8006"],"token_id":"root@pam!node64","secret":"PVE-SECRET","interval":60}""",
        )
        assertFalse(input.toString().contains("PVE-SECRET"))
    }

    @Test
    fun `probe posts request and decodes result`() = test {
        enqueueJson(
            """{"ok":true,"error":"","version":"8.2.4","cluster":"homelab","nodes":["pve1","pve2"],"guests":20,
            "cert_sha256":"AA:BB","cert_subject":"CN=pve","cert_trusted":false,"perm_warning":"missing Sys.Audit"}""",
        )
        val probe = PVEProbeRequest(systemId = 1, addresses = listOf("10.0.0.1:8006"), tokenId = "t", secret = "PROBE-SECRET")

        val result = node().proxmox.probe(probe)

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/healthchecks/proxmox/probe", request.target)
        request.assertJsonBody("""{"system_id":1,"addresses":["10.0.0.1:8006"],"token_id":"t","secret":"PROBE-SECRET"}""")
        assertEquals(true, result.ok)
        assertEquals("homelab", result.cluster)
        assertEquals(listOf("pve1", "pve2"), result.nodes)
        assertEquals(20, result.guests)
        assertEquals("AA:BB", result.certSha256)
        assertEquals("CN=pve", result.certSubject)
        assertEquals(false, result.certTrusted)
        assertEquals("missing Sys.Audit", result.permWarning)
        assertFalse(probe.toString().contains("PROBE-SECRET"))
    }

    @Test
    fun `getSystem fetches by id`() = test {
        enqueueJson(systemJson)

        assertSystem(node().proxmox.getSystem(1))

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/healthchecks/proxmox/systems/1", request.target)
    }

    @Test
    fun `updateSystem puts input`() = test {
        enqueueJson(systemJson)

        assertSystem(node().proxmox.updateSystem(1, PVESystemInput(enabled = false, pinSha256 = "AA:BB")))

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/healthchecks/proxmox/systems/1", request.target)
        request.assertJsonBody("""{"pin_sha256":"AA:BB","enabled":false}""")
    }

    @Test
    fun `deleteSystem sends DELETE`() = test {
        enqueueNoContent()

        node().proxmox.deleteSystem(1)

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/healthchecks/proxmox/systems/1", request.target)
    }

    @Test
    fun `resources decodes nodes qemu and lxc guests`() = test {
        enqueueJson(
            """{"system":$systemJson,"resources":[
            {"key":"node/pve1","type":"node","node":"pve1","name":"pve1","state":"online","cpu":0.2,"max_cpu":8,"mem":1000,
            "max_mem":2000,"disk":10,"max_disk":20,"uptime":86400},
            {"key":"qemu/100","type":"qemu","node":"pve1","vmid":100,"name":"vm","state":"stopped","status":"stopped",
            "qmp":"stopped","lock":"backup","tags":["prod","db"],"ha_state":"started","template":false,"restarted":true,
            "bad":true,"excluded":false,"muted":true,"as_check":true,"check_token":"ck1"},
            {"key":"lxc/101","type":"lxc","vmid":101,"state":"suspended"}]}""",
        )

        val res = node().proxmox.resources(1)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/healthchecks/proxmox/systems/1/resources", request.target)
        assertSystem(res.system!!)
        assertEquals(3, res.resources.size)
        val node = res.resources[0]
        assertEquals(PVEResourceType.NODE, node.type)
        assertEquals(PVEResourceState.ONLINE, node.state)
        assertEquals(8.0, node.maxCpu!!, 0.0)
        assertEquals(2000L, node.maxMem)
        assertEquals(86400L, node.uptime)
        val vm = res.resources[1]
        assertEquals(PVEResourceType.QEMU, vm.type)
        assertEquals(100L, vm.vmid)
        assertEquals(PVEResourceState.STOPPED, vm.state)
        assertEquals("backup", vm.lock)
        assertEquals(listOf("prod", "db"), vm.tags)
        assertEquals("started", vm.haState)
        assertEquals(false, vm.template)
        assertEquals(true, vm.restarted)
        assertEquals(true, vm.bad)
        assertEquals("ck1", vm.checkToken)
        assertEquals(PVEResourceType.LXC, res.resources[2].type)
        assertEquals(PVEResourceState.SUSPENDED, res.resources[2].state)
    }

    @Test
    fun `series sends key and range and decodes points`() = test {
        enqueueJson(
            """{"key":"qemu/100","range":"7d","step_seconds":1800,"from":"2026-10-21T10:00:00Z","to":"2026-10-28T10:00:00Z",
            "points":[{"t":"2026-10-21T10:00:00Z","cpu":0.2,"cpu_max":0.9,"mem":512.0,"disk":4.0,"net_in":1.0,"net_out":2.0,
            "disk_read":3.0,"disk_write":4.0},{"t":"2026-10-21T10:30:00Z","cpu":0.3}]}""",
        )

        val series = node().proxmox.series(1, "qemu/100", SeriesRange.DAYS_7)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/healthchecks/proxmox/systems/1/series?key=qemu%2F100&range=7d", request.target)
        assertEquals("7d", series.range)
        assertEquals(1800, series.stepSeconds)
        assertEquals(Instant.parse("2026-10-28T10:00:00Z"), series.to)
        assertEquals(2, series.points.size)
        assertEquals(Instant.parse("2026-10-21T10:30:00Z"), series.points[1].t)
        assertEquals(0.9, series.points[0].cpuMax!!, 0.0)
        assertEquals(512.0, series.points[0].mem!!, 0.0)
    }

    @Test
    fun `series without range sends only key`() = test {
        enqueueJson("{}")

        node().proxmox.series(1, "k")

        assertEquals("/api/healthchecks/proxmox/systems/1/series?key=k", takeRequest().target)
    }

    @Test
    fun `settings decodes overview`() = test {
        enqueueJson(
            """{"system_id":1,"items":{"qemu/100":{"system_id":1,"key":"qemu/100","muted":true,"as_check":false,
            "expect_running":false,"seen_running":true,"rules":{"offline":true,"cpu":{"percent":95,"minutes":10},
            "mem":{"percent":90,"minutes":5},"delay":1,"channels":[3]}}},"effective":{"offline":true},"defaults":{}}""",
        )

        val overview = node().proxmox.settings(1)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/healthchecks/proxmox/systems/1/settings", request.target)
        assertEquals(1L, overview.systemId)
        val item = overview.items.getValue("qemu/100")
        assertEquals(true, item.muted)
        assertEquals(false, item.expectRunning)
        assertEquals(true, item.seenRunning)
        assertEquals(true, item.rules!!.offline)
        assertEquals(95.0, item.rules!!.cpu!!.percent!!, 0.0)
        assertEquals(10, item.rules!!.cpu!!.minutes)
        assertEquals(90.0, item.rules!!.mem!!.percent!!, 0.0)
        assertEquals(listOf(3L), item.rules!!.channels)
        assertEquals(JsonPrimitive(true), overview.effective!!["offline"])
    }

    @Test
    fun `updateSettings puts input with key`() = test {
        enqueueJson("""{"settings":{"system_id":1,"key":"qemu/100","expect_running":true},"effective":{"offline":true}}""")

        val result = node().proxmox.updateSettings(
            1,
            PVESettingsInput(
                muted = false,
                expectRunning = JsonPrimitive("auto"),
                rules = PVERuleSet(offline = true, mem = PVEThreshold(85.0, 5), channels = listOf(3L)),
            ),
            key = "qemu/100",
        )

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/healthchecks/proxmox/systems/1/settings?key=qemu%2F100", request.target)
        request.assertJsonBody(
            """{"muted":false,"expect_running":"auto","rules":{"offline":true,"mem":{"percent":85.0,"minutes":5},"channels":[3]}}""",
        )
        assertEquals("qemu/100", result.settings!!.key)
        assertEquals(true, result.settings!!.expectRunning)
    }

    @Test
    fun `updateSettings without key has no query`() = test {
        enqueueJson("{}")

        node().proxmox.updateSettings(1, PVESettingsInput(asCheck = true))

        val request = takeRequest()
        assertEquals("/api/healthchecks/proxmox/systems/1/settings", request.target)
        request.assertJsonBody("""{"as_check":true}""")
    }

    @Test
    fun `resetSettings sends DELETE with optional key`() = test {
        enqueueNoContent()
        enqueueNoContent()

        node().proxmox.resetSettings(1, "qemu/100")
        node().proxmox.resetSettings(1)

        val first = takeRequest()
        assertEquals("DELETE", first.method)
        assertEquals("/api/healthchecks/proxmox/systems/1/settings?key=qemu%2F100", first.target)
        assertEquals("/api/healthchecks/proxmox/systems/1/settings", takeRequest().target)
    }
}
