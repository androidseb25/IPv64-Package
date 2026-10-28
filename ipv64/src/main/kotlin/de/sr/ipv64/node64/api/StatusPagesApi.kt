package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.model.StatusNotice
import de.sr.ipv64.node64.model.StatusNoticeInput
import de.sr.ipv64.node64.model.StatusNoticeUpdateInput
import de.sr.ipv64.node64.model.StatusPage
import de.sr.ipv64.node64.model.StatusPageRequest
import de.sr.ipv64.node64.model.StatusPageUpdateRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

/** Tag *Healthchecks*: status pages including notices and logo. */
public class StatusPagesApi internal constructor(private val transport: HttpTransport) {

    /** All status pages with groups and entries (the password is never returned). `GET /status-pages` */
    public suspend fun list(): List<StatusPage> =
        transport.json(Method.GET, ApiPath("status-pages"))

    /** Creates a status page; missing fields get defaults. `POST /status-pages` */
    public suspend fun create(request: StatusPageRequest): StatusPage =
        transport.json(Method.POST, ApiPath("status-pages"), body = transport.body(request))

    /** A status page. `GET /status-pages/{id}` */
    public suspend fun get(id: Long): StatusPage =
        transport.json(Method.GET, ApiPath("status-pages", id.toString()))

    /** Partial change: only sent fields change; `groups` replaces all groups and entries. `PUT /status-pages/{id}` */
    public suspend fun update(id: Long, request: StatusPageUpdateRequest): StatusPage =
        transport.json(Method.PUT, ApiPath("status-pages", id.toString()), body = transport.body(request))

    /** Deletes the page with groups, entries and notices. `DELETE /status-pages/{id}` */
    public suspend fun delete(id: Long) {
        transport.unit(Method.DELETE, ApiPath("status-pages", id.toString()))
    }

    /** Open and resolved notices (newest first, at most 100) with updates. `GET /status-pages/{id}/notices` */
    public suspend fun notices(id: Long): List<StatusNotice> =
        transport.json(Method.GET, ApiPath("status-pages", id.toString(), "notices"))

    /** Creates a notice. `POST /status-pages/{id}/notices` */
    public suspend fun createNotice(id: Long, input: StatusNoticeInput): StatusNotice =
        transport.json(Method.POST, ApiPath("status-pages", id.toString(), "notices"), body = transport.body(input))

    /** Appends an update to a notice, optionally changes the level and marks it resolved. `PUT /status-pages/{id}/notices/{nid}` */
    public suspend fun updateNotice(id: Long, noticeId: Long, input: StatusNoticeUpdateInput): StatusNotice =
        transport.json(
            Method.PUT,
            ApiPath("status-pages", id.toString(), "notices", noticeId.toString()),
            body = transport.body(input),
        )

    /** Deletes a notice. `DELETE /status-pages/{id}/notices/{nid}` */
    public suspend fun deleteNotice(id: Long, noticeId: Long) {
        transport.unit(Method.DELETE, ApiPath("status-pages", id.toString(), "notices", noticeId.toString()))
    }

    /**
     * Sets the logo. [image] is the raw image file (PNG, JPEG, WebP or SVG, at most 256 KiB); the format is
     * detected from the content. [contentType] is optional (an image media type must match the content).
     * `PUT /status-pages/{id}/logo`
     */
    public suspend fun setLogo(id: Long, image: ByteArray, contentType: String = "application/octet-stream"): StatusPage =
        transport.json(
            Method.PUT,
            ApiPath("status-pages", id.toString(), "logo"),
            body = image.toRequestBody(contentType.toMediaType()),
        )

    /** Removes the logo. `DELETE /status-pages/{id}/logo` */
    public suspend fun deleteLogo(id: Long) {
        transport.unit(Method.DELETE, ApiPath("status-pages", id.toString(), "logo"))
    }
}
