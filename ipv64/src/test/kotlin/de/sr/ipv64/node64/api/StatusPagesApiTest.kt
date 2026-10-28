package de.sr.ipv64.node64.api

import de.sr.ipv64.node64.model.StatusGroupRequest
import de.sr.ipv64.node64.model.StatusItemRequest
import de.sr.ipv64.node64.model.StatusNoticeInput
import de.sr.ipv64.node64.model.StatusNoticeLevel
import de.sr.ipv64.node64.model.StatusNoticeUpdateInput
import de.sr.ipv64.node64.model.StatusPage
import de.sr.ipv64.node64.model.StatusPageLayout
import de.sr.ipv64.node64.model.StatusPageRequest
import de.sr.ipv64.node64.model.StatusPageTheme
import de.sr.ipv64.node64.model.StatusPageUpdateRequest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import kotlin.time.Instant

class StatusPagesApiTest : Node64ApiTestBaseA() {

    private val pageJson = """{"id":2,"slug":"status","title":"Status","description":"d","footer":"f","enabled":true,
        "publish_node":false,"layout":"tiles","theme":"dark","lang":"de","accent":"#ff8800","days":90,"show_uptime":true,
        "show_history":false,"refresh_sec":60,"has_password":true,"noindex":true,"powered_by":false,
        "groups":[{"id":1,"name":"Web","items":[{"id":10,"token":"tok1","label":"Site"}]}],
        "created_at":"2026-09-01T10:00:00Z","updated_at":"2026-10-01T10:00:00Z","hc_groups":true,"has_logo":true,
        "logo_updated":"2026-10-02T10:00:00Z"}"""

    private fun assertPage(p: StatusPage) {
        assertEquals(2L, p.id)
        assertEquals("status", p.slug)
        assertEquals(StatusPageLayout.TILES, p.layout)
        assertEquals(StatusPageTheme.DARK, p.theme)
        assertEquals("#ff8800", p.accent)
        assertEquals(90, p.days)
        assertEquals(false, p.showHistory)
        assertEquals(60, p.refreshSec)
        assertEquals(true, p.hasPassword)
        assertEquals(false, p.poweredBy)
        assertEquals(false, p.publishNode)
        val group = p.groups.single()
        assertEquals("Web", group.name)
        assertEquals("tok1", group.items.single().token)
        assertEquals("Site", group.items.single().label)
        assertEquals(Instant.parse("2026-09-01T10:00:00Z"), p.createdAt)
        assertEquals(true, p.hcGroups)
        assertEquals(true, p.hasLogo)
        assertEquals(Instant.parse("2026-10-02T10:00:00Z"), p.logoUpdated)
    }

    @Test
    fun `list decodes pages`() = test {
        enqueueJson("[$pageJson]")

        val pages = node().statusPages.list()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/status-pages", request.target)
        assertPage(pages.single())
    }

    @Test
    fun `create posts nested request and omits nulls`() = test {
        enqueueJson(pageJson, code = 201)

        val page = node().statusPages.create(
            StatusPageRequest(
                title = "Status",
                slug = "status",
                layout = StatusPageLayout.TILES,
                theme = StatusPageTheme.DARK,
                password = "pagepw",
                groups = listOf(StatusGroupRequest("Web", listOf(StatusItemRequest("tok1", "Site"), StatusItemRequest("tok2")))),
            ),
        )

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/status-pages", request.target)
        request.assertJsonBody(
            """{"title":"Status","slug":"status","layout":"tiles","theme":"dark","password":"pagepw",
            "groups":[{"name":"Web","items":[{"token":"tok1","label":"Site"},{"token":"tok2"}]}]}""",
        )
        assertPage(page)
    }

    @Test
    fun `get fetches by id`() = test {
        enqueueJson(pageJson)

        assertPage(node().statusPages.get(2))

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/status-pages/2", request.target)
    }

    @Test
    fun `update puts only given fields`() = test {
        enqueueJson(pageJson)

        assertPage(node().statusPages.update(2, StatusPageUpdateRequest(enabled = false, refreshSec = 30, hcGroups = true)))

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/status-pages/2", request.target)
        request.assertJsonBody("""{"enabled":false,"refresh_sec":30,"hc_groups":true}""")
    }

    @Test
    fun `request toString redacts password`() {
        assertFalse(StatusPageRequest(title = "t", password = "pagepw").toString().contains("pagepw"))
        assertFalse(StatusPageUpdateRequest(password = "pagepw").toString().contains("pagepw"))
    }

    @Test
    fun `delete sends DELETE`() = test {
        enqueueNoContent()

        node().statusPages.delete(2)

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/status-pages/2", request.target)
    }

    private val noticeJson = """{"id":8,"page_id":2,"title":"Outage","level":"danger","created_at":"2026-10-28T08:00:00Z",
        "updated_at":"2026-10-28T09:00:00Z","resolved_at":"2026-10-28T09:30:00Z",
        "updates":[{"id":1,"time":"2026-10-28T08:30:00Z","text":"investigating"},{"id":2,"time":"2026-10-28T09:00:00Z","text":"fixed"}]}"""

    @Test
    fun `notices decodes notices with updates`() = test {
        enqueueJson("[$noticeJson]")

        val notices = node().statusPages.notices(2)

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/status-pages/2/notices", request.target)
        val n = notices.single()
        assertEquals(8L, n.id)
        assertEquals(2L, n.pageId)
        assertEquals(StatusNoticeLevel.DANGER, n.level)
        assertEquals(Instant.parse("2026-10-28T09:30:00Z"), n.resolvedAt)
        assertEquals(2, n.updates.size)
        assertEquals("investigating", n.updates[0].text)
        assertEquals(Instant.parse("2026-10-28T09:00:00Z"), n.updates[1].time)
    }

    @Test
    fun `createNotice posts input`() = test {
        enqueueJson(noticeJson, code = 201)

        val notice = node().statusPages.createNotice(2, StatusNoticeInput("Outage", StatusNoticeLevel.WARNING, "we are on it"))

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/status-pages/2/notices", request.target)
        request.assertJsonBody("""{"title":"Outage","level":"warning","text":"we are on it"}""")
        assertEquals(8L, notice.id)
    }

    @Test
    fun `updateNotice puts input to notice path`() = test {
        enqueueJson(noticeJson)

        node().statusPages.updateNotice(2, 8, StatusNoticeUpdateInput(text = "fixed", resolve = true))

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/status-pages/2/notices/8", request.target)
        request.assertJsonBody("""{"text":"fixed","resolve":true}""")
    }

    @Test
    fun `deleteNotice sends DELETE`() = test {
        enqueueNoContent()

        node().statusPages.deleteNotice(2, 8)

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/status-pages/2/notices/8", request.target)
    }

    @Test
    fun `setLogo puts raw image bytes with content type`() = test {
        enqueueJson(pageJson)
        val png = byteArrayOf(-119, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, -1)

        val page = node().statusPages.setLogo(2, png, "image/png")

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/status-pages/2/logo", request.target)
        assertEquals("image/png", request.headers["Content-Type"])
        assertArrayEquals(png, request.bodyBytes())
        assertPage(page)
    }

    @Test
    fun `setLogo defaults to octet stream`() = test {
        enqueueJson(pageJson)

        node().statusPages.setLogo(2, byteArrayOf(1, 2, 3))

        assertEquals("application/octet-stream", takeRequest().headers["Content-Type"])
    }

    @Test
    fun `deleteLogo sends DELETE`() = test {
        enqueueNoContent()

        node().statusPages.deleteLogo(2)

        val request = takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/status-pages/2/logo", request.target)
    }
}
