package de.sr.ipv64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.data
import de.sr.ipv64.internal.list
import de.sr.ipv64.model.HoneypotListToken
import de.sr.ipv64.model.HoneypotReport
import de.sr.ipv64.model.HoneypotReportAccepted
import de.sr.ipv64.model.HoneypotSensor
import de.sr.ipv64.model.HoneypotStats
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

/** Tag *Honeypot64*: sensors, reports and the personal blocklist token. */
public class HoneypotApi internal constructor(private val transport: HttpTransport) {

    /**
     * Submits a sensor report (Node64 honeypot module only). At most 2 MiB uncompressed; one report per
     * sensor every 2 minutes. With [gzip] the JSON body is compressed and sent with `Content-Encoding: gzip`.
     * A known `report_id` yields [HoneypotReportAccepted.duplicate] = true. `POST /honeypot/reports`
     */
    public suspend fun submitReport(report: HoneypotReport, gzip: Boolean = false): HoneypotReportAccepted {
        val json = transport.json.encodeToString(HoneypotReport.serializer(), report)
        val bytes = json.toByteArray(Charsets.UTF_8)
        return if (gzip) {
            val compressed = ByteArrayOutputStream().also { out ->
                GZIPOutputStream(out).use { it.write(bytes) }
            }.toByteArray()
            transport.data(
                Method.POST,
                ApiPath("honeypot", "reports"),
                body = compressed.toRequestBody(HttpTransport.JSON_MEDIA_TYPE),
                headers = mapOf("Content-Encoding" to "gzip"),
            )
        } else {
            transport.data(
                Method.POST,
                ApiPath("honeypot", "reports"),
                body = bytes.toRequestBody(HttpTransport.JSON_MEDIA_TYPE),
            )
        }
    }

    /** Own sensors with their contribution in 24 h / 7 d. The sensor IP is never returned. `GET /honeypot/sensors` */
    public suspend fun sensors(): List<HoneypotSensor> =
        transport.list(Method.GET, ApiPath("honeypot", "sensors"))

    /**
     * Personal list token for the live v64 blocklist; created if missing, an existing one is kept.
     * Keep the token secret. `GET /honeypot/list-token`
     */
    public suspend fun listToken(): HoneypotListToken =
        transport.data(Method.GET, ApiPath("honeypot", "list-token"))

    /** Own contribution: events and sources 24 h / 7 d, top kinds and ports, timeline. `GET /honeypot/stats` */
    public suspend fun stats(): HoneypotStats =
        transport.data(Method.GET, ApiPath("honeypot", "stats"))
}
