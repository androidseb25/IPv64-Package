package de.sr.ipv64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.data
import de.sr.ipv64.internal.list
import de.sr.ipv64.model.CreateOfflinePageRequest
import de.sr.ipv64.model.OfflinePage
import de.sr.ipv64.model.UpdateOfflinePageRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/** Tag *Offline-Pages*: pages shown instead of an unreachable server during a failover. */
public class OfflinePagesApi internal constructor(private val transport: HttpTransport) {

    /** Lists all offline pages. `GET /offline-pages` */
    public suspend fun list(): List<OfflinePage> =
        transport.list(Method.GET, ApiPath("offline-pages"))

    /** Creates an offline page. `POST /offline-pages` */
    public suspend fun create(request: CreateOfflinePageRequest = CreateOfflinePageRequest()): OfflinePage =
        transport.data(Method.POST, ApiPath("offline-pages"), body = transport.body(request))

    /** Returns a single offline page. `GET /offline-pages/{id}` */
    public suspend fun get(id: String): OfflinePage =
        transport.data(Method.GET, ApiPath("offline-pages", id))

    /** Updates an offline page. `PATCH /offline-pages/{id}` */
    public suspend fun update(id: String, request: UpdateOfflinePageRequest): OfflinePage =
        transport.data(Method.PATCH, ApiPath("offline-pages", id), body = transport.body(request))

    /** Deletes an offline page. Not possible while a failover rule uses it. `DELETE /offline-pages/{id}` */
    public suspend fun delete(id: String) {
        transport.unit(Method.DELETE, ApiPath("offline-pages", id))
    }

    /**
     * Uploads the logo as multipart form field `icon`. JPEG, PNG or WebP, at most 1 MB and 1024 x 1024 pixels.
     * @param fileName file name sent in the multipart part
     * @param mediaType MIME type of the image, e.g. `image/png`
     * `POST /offline-pages/{id}/icon`
     */
    public suspend fun uploadIcon(
        id: String,
        icon: ByteArray,
        fileName: String,
        mediaType: String? = null,
    ): OfflinePage {
        val multipart = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("icon", fileName, icon.toRequestBody(mediaType?.toMediaTypeOrNull()))
            .build()
        return transport.data(Method.POST, ApiPath("offline-pages", id, "icon"), body = multipart)
    }

    /**
     * Uploads the logo from a [File]; the media type is derived from the extension (png, jpg/jpeg, webp).
     * `POST /offline-pages/{id}/icon`
     */
    public suspend fun uploadIcon(id: String, file: File, mediaType: String? = null): OfflinePage {
        val type = mediaType ?: when (file.extension.lowercase()) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "webp" -> "image/webp"
            else -> null
        }
        val multipart = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("icon", file.name, file.asRequestBody(type?.toMediaTypeOrNull()))
            .build()
        return transport.data(Method.POST, ApiPath("offline-pages", id, "icon"), body = multipart)
    }

    /** Deletes the logo. `DELETE /offline-pages/{id}/icon` */
    public suspend fun deleteIcon(id: String) {
        transport.unit(Method.DELETE, ApiPath("offline-pages", id, "icon"))
    }
}
