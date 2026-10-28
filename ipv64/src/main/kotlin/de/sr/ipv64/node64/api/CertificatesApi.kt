package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.internal.query
import de.sr.ipv64.node64.model.ACMECA
import de.sr.ipv64.node64.model.CA
import de.sr.ipv64.node64.model.CaFileFormat
import de.sr.ipv64.node64.model.CertAccount
import de.sr.ipv64.node64.model.CertBundleFormat
import de.sr.ipv64.node64.model.CertBundleResult
import de.sr.ipv64.node64.model.CertMeta
import de.sr.ipv64.node64.model.Certificate
import de.sr.ipv64.node64.model.CreateAcmeCaRequest
import de.sr.ipv64.node64.model.CreateCaRequest
import de.sr.ipv64.node64.model.CreateCertificateRequest
import de.sr.ipv64.node64.model.CreateDnsAccountRequest
import de.sr.ipv64.node64.model.DNSAccount
import de.sr.ipv64.node64.model.DnsProvider
import de.sr.ipv64.node64.model.ImportCaRequest
import de.sr.ipv64.node64.model.UpdateCertAccountRequest
import de.sr.ipv64.node64.model.UpdateDnsAccountRequest
import de.sr.ipv64.node64.model.UploadCertificateRequest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** Tag *Ports & Zertifikate* (certificates part): certificates, DNS accounts, own CAs and ACME CAs. */
public class CertificatesApi internal constructor(private val transport: HttpTransport) {

    // ---- Certificates ----

    /** Certificates without keys, soon expiring first. `GET /certs` */
    public suspend fun list(): List<Certificate> =
        transport.json(Method.GET, ApiPath("certs"))

    /**
     * Requests a certificate or issues one with an own CA (runs in the background).
     * Accept the terms of service first (`PUT /certs/account`) for Let's Encrypt sources.
     * `POST /certs`
     */
    public suspend fun create(request: CreateCertificateRequest): Certificate =
        transport.json(Method.POST, ApiPath("certs"), body = transport.body(request))

    /** Uploads an own certificate as JSON (PEM `cert`/`key`/`password` or `p12`/`password`; max. 1 MB). `POST /certs/upload` */
    public suspend fun upload(request: UploadCertificateRequest): Certificate =
        transport.json(Method.POST, ApiPath("certs", "upload"), body = transport.body(request))

    /**
     * Uploads an own certificate as multipart form (PEM or .pfx/.p12 in [certFile], optional [keyFile] and [keyPassword]).
     * A wrong or missing password yields 422 with field `password`.
     * `POST /certs/upload`
     */
    public suspend fun uploadFiles(
        certFile: ByteArray,
        certFileName: String = "cert.pem",
        keyFile: ByteArray? = null,
        keyFileName: String = "key.pem",
        keyPassword: String? = null,
    ): Certificate {
        val octet = "application/octet-stream".toMediaType()
        val multipart = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("cert_file", certFileName, certFile.toRequestBody(octet))
            .apply {
                if (keyFile != null) addFormDataPart("key_file", keyFileName, keyFile.toRequestBody(octet))
                if (keyPassword != null) addFormDataPart("key_password", keyPassword)
            }
            .build()
        return transport.json(Method.POST, ApiPath("certs", "upload"), body = multipart)
    }

    /** ACME account: mail and terms of service. `GET /certs/account` */
    public suspend fun account(): CertAccount =
        transport.json(Method.GET, ApiPath("certs", "account"))

    /** Sets the mail and accepts the terms of service of Let's Encrypt. `PUT /certs/account` */
    public suspend fun updateAccount(request: UpdateCertAccountRequest): CertAccount =
        transport.json(Method.PUT, ApiPath("certs", "account"), body = transport.body(request))

    /** ipv64 domains of the account (for DNS-01 and as alias targets). `GET /certs/domains` */
    public suspend fun domains(): List<String> =
        transport.json(Method.GET, ApiPath("certs", "domains"))

    /** A certificate. `GET /certs/{id}` */
    public suspend fun get(id: Long): Certificate =
        transport.json(Method.GET, ApiPath("certs", id.toString()))

    /** Switches automatic renewal. `PUT /certs/{id}` */
    public suspend fun setAutoRenew(id: Long, autoRenew: Boolean): Certificate =
        transport.json(Method.PUT, ApiPath("certs", id.toString()), body = transport.body(AutoRenewBody(autoRenew)))

    /** Deletes a certificate; [force] also deletes it while in use. `DELETE /certs/{id}` */
    public suspend fun delete(id: Long, force: Boolean = false) {
        transport.unit(Method.DELETE, ApiPath("certs", id.toString()), query("force" to if (force) "1" else null))
    }

    /** Renews the certificate now (Let's Encrypt). `POST /certs/{id}/renew` */
    public suspend fun renew(id: Long): Certificate =
        transport.json(Method.POST, ApiPath("certs", id.toString(), "renew"))

    /** Certificate with chain as PEM (without key). `GET /certs/{id}/chain` */
    public suspend fun chain(id: Long): String =
        transport.text(
            Method.GET, ApiPath("certs", id.toString(), "chain"),
            headers = mapOf("Accept" to "application/pem-certificate-chain"),
        )

    /** Details of the issued certificate (valid until, fingerprint). `GET /certs/{id}/meta` */
    public suspend fun meta(id: Long): CertMeta =
        transport.json(Method.GET, ApiPath("certs", id.toString(), "meta"))

    /**
     * Downloads the certificate as file (e.g. by cron on a NAS/router). [format] `zip` (server default) returns a ZIP
     * archive (fullchain.pem, cert.pem, chain.pem, privkey.pem), `pem` fullchain and key in one file.
     * Formats with the private key (`key`, `pem`, `zip`) appear in the audit log.
     * Supports ETag (= fingerprint): pass the [CertBundleResult.etag] of an earlier call as [ifNoneMatch]; as long as
     * the certificate did not change the server answers 304 and [CertBundleResult.content] is `null`.
     * `GET /certs/{id}/bundle`
     */
    public suspend fun bundle(id: Long, format: CertBundleFormat? = null, ifNoneMatch: String? = null): CertBundleResult {
        val raw = transport.raw(
            Method.GET, ApiPath("certs", id.toString(), "bundle"), query("format" to format),
            headers = buildMap {
                put("Accept", "application/zip, application/x-pem-file, application/pem-certificate-chain, */*")
                if (ifNoneMatch != null) put("If-None-Match", ifNoneMatch)
            },
        )
        val etag = raw.headers["ETag"]
        val notAfter = raw.headers["X-Cert-Not-After"]
        if (raw.code == 304) return CertBundleResult(content = null, etag = etag ?: ifNoneMatch, notAfter = notAfter)
        return CertBundleResult(content = raw.bytes, etag = etag, notAfter = notAfter)
    }

    /**
     * PKCS#12 file (.p12) with password; [legacy] uses old 3DES encryption for older devices, otherwise AES-256.
     * `POST /certs/{id}/p12`
     */
    public suspend fun p12(id: Long, password: String, legacy: Boolean? = null): ByteArray =
        transport.bytes(
            Method.POST, ApiPath("certs", id.toString(), "p12"),
            body = transport.body(P12Body(password, legacy)),
            headers = mapOf("Accept" to "application/x-pkcs12"),
        )

    // ---- DNS accounts ----

    /** DNS providers with their fields. `GET /certs/dns-providers` */
    public suspend fun dnsProviders(): List<DnsProvider> =
        transport.json(Method.GET, ApiPath("certs", "dns-providers"))

    /** DNS accounts without secret values. `GET /certs/dns-accounts` */
    public suspend fun dnsAccounts(): List<DNSAccount> =
        transport.json(Method.GET, ApiPath("certs", "dns-accounts"))

    /** Creates a DNS account. `POST /certs/dns-accounts` */
    public suspend fun createDnsAccount(request: CreateDnsAccountRequest): DNSAccount =
        transport.json(Method.POST, ApiPath("certs", "dns-accounts"), body = transport.body(request))

    /** Changes a DNS account. `PUT /certs/dns-accounts/{id}` */
    public suspend fun updateDnsAccount(id: Long, request: UpdateDnsAccountRequest): DNSAccount =
        transport.json(Method.PUT, ApiPath("certs", "dns-accounts", id.toString()), body = transport.body(request))

    /** Deletes a DNS account. `DELETE /certs/dns-accounts/{id}` */
    public suspend fun deleteDnsAccount(id: Long) {
        transport.unit(Method.DELETE, ApiPath("certs", "dns-accounts", id.toString()))
    }

    // ---- Own CAs ----

    /** Own CAs. `GET /cas` */
    public suspend fun cas(): List<CA> =
        transport.json(Method.GET, ApiPath("cas"))

    /** Creates an own CA (valid for 10 years). `POST /cas` */
    public suspend fun createCa(request: CreateCaRequest): CA =
        transport.json(Method.POST, ApiPath("cas"), body = transport.body(request))

    /** Imports an existing CA (also an intermediate CA with chain). `POST /cas/import` */
    public suspend fun importCa(request: ImportCaRequest): CA =
        transport.json(Method.POST, ApiPath("cas", "import"), body = transport.body(request))

    /** A CA. `GET /cas/{id}` */
    public suspend fun getCa(id: Long): CA =
        transport.json(Method.GET, ApiPath("cas", id.toString()))

    /** Renames a CA. `PUT /cas/{id}` */
    public suspend fun renameCa(id: Long, name: String): CA =
        transport.json(Method.PUT, ApiPath("cas", id.toString()), body = transport.body(NameBody(name)))

    /** Deletes a CA (only without issued certificates). `DELETE /cas/{id}` */
    public suspend fun deleteCa(id: Long) {
        transport.unit(Method.DELETE, ApiPath("cas", id.toString()))
    }

    /**
     * CA certificate to install on devices. `crt` is DER (Windows/Android), `mobileconfig` a profile for iOS/macOS.
     * `GET /cas/{id}/file`
     */
    public suspend fun caFile(id: Long, format: CaFileFormat? = null): ByteArray =
        transport.bytes(
            Method.GET, ApiPath("cas", id.toString(), "file"), query("format" to format),
            headers = mapOf("Accept" to "*/*"),
        )

    // ---- ACME CAs ----

    /** Further ACME CAs besides Let's Encrypt. `GET /acme-cas` */
    public suspend fun acmeCas(): List<ACMECA> =
        transport.json(Method.GET, ApiPath("acme-cas"))

    /**
     * Adds an ACME CA (ZeroSSL, Google, SSL.com, step-ca …). The directory is queried; if the CA requires
     * external account binding, `eabKid` and `eabHmac` are mandatory.
     * `POST /acme-cas`
     */
    public suspend fun createAcmeCa(request: CreateAcmeCaRequest): ACMECA =
        transport.json(Method.POST, ApiPath("acme-cas"), body = transport.body(request))

    /** Templates of known ACME CAs. Response items are not documented by the API. `GET /acme-cas/presets` */
    public suspend fun acmeCaPresets(): List<JsonObject> =
        transport.json(Method.GET, ApiPath("acme-cas", "presets"))

    /** Removes an ACME CA (only without certificates). `DELETE /acme-cas/{id}` */
    public suspend fun deleteAcmeCa(id: Long) {
        transport.unit(Method.DELETE, ApiPath("acme-cas", id.toString()))
    }
}

@Serializable
private class AutoRenewBody(val auto_renew: Boolean)

@Serializable
private class NameBody(val name: String)

@Serializable
private class P12Body(val password: String, val legacy: Boolean? = null) {
    override fun toString(): String = "P12Body(password=<redacted>, legacy=$legacy)"
}
