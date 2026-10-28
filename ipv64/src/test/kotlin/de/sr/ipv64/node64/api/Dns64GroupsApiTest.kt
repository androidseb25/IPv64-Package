package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.time.Instant

class Dns64GroupsApiTest : Node64ApiTestBaseB() {

    private val groups get() = node().dns64.groups

    private val groupJson = """{"id":4,"name":"Kids","position":2,
        "members":[{"id":40,"group_id":4,"kind":"mac","value":"aa:bb:cc:dd:ee:ff","label":"tablet"},
                   {"id":41,"group_id":4,"kind":"net","value":"192.168.50.0/24"}],
        "lists_mode":"custom","lists":["https://example.org/l.txt"],"categories":["social","adult"],
        "domains":[{"id":7,"group_id":4,"domain":"school.org","action":"allow"}],
        "safesearch":true,"youtube":"strict","bypass":false,"hold":"block","hold_until":"2026-10-28T20:00:00Z",
        "schedules":[{"id":9,"group_id":4,"label":"Night","days":[1,2,3,4,5],"start":"21:00","end":"07:00","action":"cats",
                      "categories":["games","streaming"],"enabled":true}],
        "state":{"internet_off":true,"free":false,"by_button":true,"until_released":false,"until":"2026-10-28T20:00:00Z","schedule":"Night"},
        "uid":"grp-abc"}"""

    @Test
    fun `list decodes nested groups`() = test<Unit> {
        enqueueJson("[$groupJson]")
        val g = groups.list().single()
        expectRequest("GET", "/api/dns64/groups")
        assertEquals(4L, g.id)
        assertEquals("Kids", g.name)
        assertEquals(2, g.position)
        assertEquals(2, g.members.size)
        assertEquals(40L, g.members[0].id)
        assertEquals(4L, g.members[0].groupId)
        assertEquals("mac", g.members[0].kind)
        assertEquals("tablet", g.members[0].label)
        assertNull(g.members[1].label)
        assertEquals("custom", g.listsMode)
        assertEquals(listOf("https://example.org/l.txt"), g.lists)
        assertEquals(listOf("social", "adult"), g.categories)
        assertEquals("allow", g.domains.single().action)
        assertEquals("school.org", g.domains.single().domain)
        assertEquals(true, g.safesearch)
        assertEquals("strict", g.youtube)
        assertEquals(false, g.bypass)
        assertEquals("block", g.hold)
        assertEquals("2026-10-28T20:00:00Z", g.holdUntil)
        val s = g.schedules.single()
        assertEquals(listOf(1, 2, 3, 4, 5), s.days)
        assertEquals("21:00", s.start)
        assertEquals("07:00", s.end)
        assertEquals("cats", s.action)
        assertEquals(listOf("games", "streaming"), s.categories)
        assertEquals(true, s.enabled)
        assertEquals(true, g.state?.internetOff)
        assertEquals(true, g.state?.byButton)
        assertEquals(Instant.parse("2026-10-28T20:00:00Z"), g.state?.until)
        assertEquals("Night", g.state?.schedule)
        assertEquals("grp-abc", g.uid)
    }

    @Test
    fun `create posts name`() = test<Unit> {
        enqueueJson(groupJson)
        assertEquals("Kids", groups.create("Kids").name)
        assertEquals(js("""{"name":"Kids"}"""), expectRequest("POST", "/api/dns64/groups").jsonBody())
    }

    @Test
    fun `rename puts name`() = test<Unit> {
        enqueueNoContent()
        groups.rename(4, "Teens")
        assertEquals(js("""{"name":"Teens"}"""), expectRequest("PUT", "/api/dns64/groups/4").jsonBody())
    }

    @Test
    fun `delete`() = test<Unit> {
        enqueueNoContent()
        groups.delete(4)
        expectRequest("DELETE", "/api/dns64/groups/4")
    }

    @Test
    fun `move posts direction`() = test<Unit> {
        enqueueNoContent()
        groups.move(4, up = false)
        assertEquals(js("""{"up":false}"""), expectRequest("POST", "/api/dns64/groups/4/move").jsonBody())
    }

    @Test
    fun `addMember posts kind value and label`() = test<Unit> {
        enqueueJson("""{"id":40,"group_id":4,"kind":"mac","value":"aa:bb:cc:dd:ee:ff","label":"tablet"}""")
        val m = groups.addMember(4, DNS64GroupMemberRequest(DNS64MemberKind.MAC, "aa:bb:cc:dd:ee:ff", "tablet"))
        val r = expectRequest("POST", "/api/dns64/groups/4/members")
        assertEquals(js("""{"kind":"mac","value":"aa:bb:cc:dd:ee:ff","label":"tablet"}"""), r.jsonBody())
        assertEquals(40L, m.id)
        assertEquals("tablet", m.label)
    }

    @Test
    fun `addMember omits null label`() = test<Unit> {
        enqueueJson("""{"id":1}""")
        groups.addMember(4, DNS64GroupMemberRequest(DNS64MemberKind.NET, "10.0.0.0/8"))
        assertEquals(js("""{"kind":"net","value":"10.0.0.0/8"}"""), expectRequest("POST", "/api/dns64/groups/4/members").jsonBody())
    }

    @Test
    fun `removeMember uses members collection path`() = test<Unit> {
        enqueueNoContent()
        groups.removeMember(40)
        expectRequest("DELETE", "/api/dns64/groups/members/40")
    }

    @Test
    fun `updateRules puts rules with enum wire names`() = test<Unit> {
        enqueueNoContent()
        groups.updateRules(
            4,
            DNS64GroupRulesRequest(
                DNS64ListsMode.CUSTOM, listOf("https://example.org/l.txt"), listOf(DNS64Category.SOCIAL, DNS64Category.GAMBLING),
                safesearch = true, youtube = DNS64YoutubeMode.MODERATE, bypass = false,
            ),
        )
        val r = expectRequest("PUT", "/api/dns64/groups/4/rules")
        assertEquals(
            js("""{"lists_mode":"custom","lists":["https://example.org/l.txt"],"categories":["social","gambling"],
                "safesearch":true,"youtube":"moderate","bypass":false}"""),
            r.jsonBody(),
        )
    }

    @Test
    fun `updateRules omits unset fields`() = test<Unit> {
        enqueueNoContent()
        groups.updateRules(4, DNS64GroupRulesRequest(listsMode = DNS64ListsMode.GLOBAL))
        assertEquals(js("""{"lists_mode":"global"}"""), expectRequest("PUT", "/api/dns64/groups/4/rules").jsonBody())
    }

    @Test
    fun `addDomain posts domain and action`() = test<Unit> {
        enqueueJson("""{"id":7,"group_id":4,"domain":"school.org","action":"allow"}""")
        val d = groups.addDomain(4, DNS64GroupDomainRequest("school.org", DNS64DomainAction.ALLOW))
        assertEquals(js("""{"domain":"school.org","action":"allow"}"""), expectRequest("POST", "/api/dns64/groups/4/domains").jsonBody())
        assertEquals(7L, d.id)
        assertEquals(4L, d.groupId)
        assertEquals("allow", d.action)
    }

    @Test
    fun `removeDomain uses domains collection path`() = test<Unit> {
        enqueueNoContent()
        groups.removeDomain(7)
        expectRequest("DELETE", "/api/dns64/groups/domains/7")
    }

    @Test
    fun `hold posts block with minutes`() = test<Unit> {
        enqueueNoContent()
        groups.hold(4, DNS64GroupHoldRequest(DNS64HoldMode.BLOCK, minutes = 60))
        assertEquals(js("""{"mode":"block","minutes":60}"""), expectRequest("POST", "/api/dns64/groups/4/hold").jsonBody())
    }

    @Test
    fun `hold free until morning`() = test<Unit> {
        enqueueNoContent()
        groups.hold(4, DNS64GroupHoldRequest(DNS64HoldMode.FREE, untilMorning = true))
        assertEquals(js("""{"mode":"free","until_morning":true}"""), expectRequest("POST", "/api/dns64/groups/4/hold").jsonBody())
    }

    @Test
    fun `hold release sends empty mode`() = test<Unit> {
        enqueueNoContent()
        groups.hold(4, DNS64GroupHoldRequest(DNS64HoldMode.NONE))
        assertEquals(js("""{"mode":""}"""), expectRequest("POST", "/api/dns64/groups/4/hold").jsonBody())
    }

    @Test
    fun `addSchedule posts schedule and decodes`() = test<Unit> {
        enqueueJson("""{"id":9,"group_id":4,"label":"Night","days":[6,7],"start":"22:00","end":"08:00","action":"off","categories":[],"enabled":false}""")
        val s = groups.addSchedule(
            4,
            DNS64ScheduleRequest(listOf(6, 7), "22:00", "08:00", DNS64ScheduleAction.OFF, "Night", listOf(DNS64Category.ADULT), enabled = false),
        )
        val r = expectRequest("POST", "/api/dns64/groups/4/schedules")
        assertEquals(
            js("""{"days":[6,7],"start":"22:00","end":"08:00","action":"off","label":"Night","categories":["adult"],"enabled":false}"""),
            r.jsonBody(),
        )
        assertEquals(9L, s.id)
        assertEquals(listOf(6, 7), s.days)
        assertEquals("off", s.action)
        assertEquals(false, s.enabled)
        assertTrue(s.categories.isEmpty())
    }

    @Test
    fun `addSchedule omits optionals`() = test<Unit> {
        enqueueJson("""{"id":9}""")
        groups.addSchedule(4, DNS64ScheduleRequest(listOf(1), "01:00", "02:00", DNS64ScheduleAction.CATS))
        assertEquals(
            js("""{"days":[1],"start":"01:00","end":"02:00","action":"cats"}"""),
            expectRequest("POST", "/api/dns64/groups/4/schedules").jsonBody(),
        )
    }

    @Test
    fun `enableSchedule`() = test<Unit> {
        enqueueNoContent()
        groups.enableSchedule(9)
        expectRequest("POST", "/api/dns64/groups/schedules/9/enable")
    }

    @Test
    fun `disableSchedule`() = test<Unit> {
        enqueueNoContent()
        groups.disableSchedule(9)
        expectRequest("POST", "/api/dns64/groups/schedules/9/disable")
    }

    @Test
    fun `deleteSchedule`() = test<Unit> {
        enqueueNoContent()
        groups.deleteSchedule(9)
        expectRequest("DELETE", "/api/dns64/groups/schedules/9")
    }
}
