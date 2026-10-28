package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IsoInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** How a certificate is issued. */
@Serializable
public enum class CertificateSource(override val value: String) : ApiValue {
    @SerialName("ipv64") IPV64("ipv64"),
    @SerialName("alias") ALIAS("alias"),
    @SerialName("http") HTTP("http"),
    @SerialName("dns") DNS("dns"),
    @SerialName("upload") UPLOAD("upload"),
    @SerialName("ca") CA("ca"),
    @SerialName("self") SELF("self"),
}

/** State of a certificate. */
@Serializable
public enum class CertificateStatus(override val value: String) : ApiValue {
    @SerialName("pending") PENDING("pending"),
    @SerialName("valid") VALID("valid"),
    @SerialName("error") ERROR("error"),
    @SerialName("expired") EXPIRED("expired"),
}

/** Key type of a certificate or CA (ECDSA P-256 or RSA 2048). */
@Serializable
public enum class CertificateKeyType(override val value: String) : ApiValue {
    @SerialName("ecdsa") ECDSA("ecdsa"),
    @SerialName("rsa") RSA("rsa"),
}

/** Download format of `GET /certs/{id}/bundle`. */
@Serializable
public enum class CertBundleFormat(override val value: String) : ApiValue {
    @SerialName("zip") ZIP("zip"),
    @SerialName("fullchain") FULLCHAIN("fullchain"),
    @SerialName("cert") CERT("cert"),
    @SerialName("chain") CHAIN("chain"),
    @SerialName("key") KEY("key"),
    @SerialName("pem") PEM("pem"),
}

/** Download format of `GET /cas/{id}/file`. */
@Serializable
public enum class CaFileFormat(override val value: String) : ApiValue {
    @SerialName("pem") PEM("pem"),
    @SerialName("crt") CRT("crt"),
    @SerialName("mobileconfig") MOBILECONFIG("mobileconfig"),
}

/** A certificate (without private key). */
@Serializable
public data class Certificate(
    /** Certificate ID. */
    val id: Long? = null,
    /** Names (domains / IPs). */
    val names: List<String> = emptyList(),
    /** Issuing source. */
    val source: CertificateSource? = null,
    /** Alias zone. */
    @SerialName("alias_zone") val aliasZone: String? = null,
    /** Alias prefix. */
    @SerialName("alias_prefix") val aliasPrefix: String? = null,
    /** Staging certificate. */
    val staging: Boolean? = null,
    /** Renew automatically. */
    @SerialName("auto_renew") val autoRenew: Boolean? = null,
    /** State. */
    val status: CertificateStatus? = null,
    /** Current step of the retrieval. */
    val step: String? = null,
    /** Issuer. */
    val issuer: String? = null,
    /** Valid from. */
    @SerialName("not_before") val notBefore: IsoInstant? = null,
    /** Valid until. */
    @SerialName("not_after") val notAfter: IsoInstant? = null,
    /** Last error. */
    @SerialName("last_error") val lastError: String? = null,
    /** Last attempt. */
    @SerialName("last_attempt") val lastAttempt: IsoInstant? = null,
    /** Users of the certificate. */
    @SerialName("used_by") val usedBy: List<String> = emptyList(),
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Last update. */
    @SerialName("updated_at") val updatedAt: IsoInstant? = null,
    /** DNS account (source `dns`). */
    @SerialName("dns_account") val dnsAccount: Long? = null,
    /** Own CA (source `ca`). */
    @SerialName("ca_id") val caId: Long? = null,
    /** Validity in days. */
    val days: Int? = null,
    /** ACME CA, 0 = Let's Encrypt. */
    @SerialName("acme_ca") val acmeCa: Long? = null,
    /** Key type. */
    @SerialName("key_type") val keyType: CertificateKeyType? = null,
)

/** Body of `POST /certs`. */
@Serializable
public data class CreateCertificateRequest(
    /** Names (domains / IPs; wildcards not with source `http`). */
    val names: List<String>,
    /** Source: `ipv64`, `alias`, `http`, `dns`, `ca`, `self`. */
    val source: CertificateSource,
    /** Alias zone (source `alias`). */
    @SerialName("alias_zone") val aliasZone: String? = null,
    /** Alias prefix (source `alias`). */
    @SerialName("alias_prefix") val aliasPrefix: String? = null,
    /** Use the staging environment. */
    val staging: Boolean? = null,
    /** Renew automatically. */
    @SerialName("auto_renew") val autoRenew: Boolean? = null,
    /** DNS account (source `dns`). */
    @SerialName("dns_account") val dnsAccount: Long? = null,
    /** Own CA (source `ca`). */
    @SerialName("ca_id") val caId: Long? = null,
    /** Validity in days (sources `ca`, `self`). */
    val days: Int? = null,
    /** Further ACME CA instead of Let's Encrypt. */
    @SerialName("acme_ca") val acmeCa: Long? = null,
    /** Key type (`ecdsa` or `rsa`). */
    @SerialName("key_type") val keyType: CertificateKeyType? = null,
)

/** JSON body of `POST /certs/upload`: PEM `cert` + `key` (+ `password`) or `p12` + `password`. Key and passwords are redacted in [toString]. */
@Serializable
public data class UploadCertificateRequest(
    /** Certificate with chain (PEM). */
    val cert: String? = null,
    /** Private key (PEM, may be encrypted; secret). */
    val key: String? = null,
    /** Password of an encrypted key or the P12 file (secret). */
    val password: String? = null,
    /** Base64 encoded PKCS#12 file (secret). */
    val p12: String? = null,
) {
    override fun toString(): String =
        "UploadCertificateRequest(cert=$cert, key=<redacted>, password=<redacted>, p12=<redacted>)"
}

/** ACME account: mail and terms of service. */
@Serializable
public data class CertAccount(
    /** Account mail. */
    val email: String? = null,
    /** Terms of service accepted. */
    @SerialName("tos_accepted") val tosAccepted: Boolean? = null,
    /** Time of acceptance. */
    @SerialName("tos_at") val tosAt: IsoInstant? = null,
    /** URL of the terms of service. */
    @SerialName("tos_url") val tosUrl: String? = null,
)

/** Body of `PUT /certs/account`. */
@Serializable
public data class UpdateCertAccountRequest(
    /** Account mail. */
    val email: String? = null,
    /** Accept the terms of service of Let's Encrypt. */
    @SerialName("accept_tos") val acceptTos: Boolean? = null,
)

/** Details of an issued certificate (valid until, fingerprint). */
@Serializable
public data class CertMeta(
    /** Certificate ID. */
    val id: Long? = null,
    /** Names. */
    val names: List<String> = emptyList(),
    /** Issuer. */
    val issuer: String? = null,
    /** Serial number (hex). */
    val serial: String? = null,
    /** Valid from. */
    @SerialName("not_before") val notBefore: IsoInstant? = null,
    /** Valid until. */
    @SerialName("not_after") val notAfter: IsoInstant? = null,
    /** Remaining days. */
    @SerialName("days_left") val daysLeft: Int? = null,
    /** SHA-256 fingerprint (hex), equals the ETag of the bundle. */
    @SerialName("fingerprint_sha256") val fingerprintSha256: String? = null,
)

/** A field of a DNS provider. */
@Serializable
public data class DnsProviderField(
    /** Key. */
    val key: String? = null,
    /** Label. */
    val label: String? = null,
    /** Value is secret. */
    val secret: Boolean? = null,
    /** Field is optional. */
    val optional: Boolean? = null,
    /** Default value. */
    val default: String? = null,
)

/** A DNS provider with its fields. */
@Serializable
public data class DnsProvider(
    /** Provider ID. */
    val id: String? = null,
    /** Name. */
    val name: String? = null,
    /** Documentation. */
    val docs: String? = null,
    /** Fields to fill. */
    val fields: List<DnsProviderField> = emptyList(),
)

/** DNS account (without secret values). */
@Serializable
public data class DNSAccount(
    /** ID. */
    val id: Long? = null,
    /** Name. */
    val name: String? = null,
    /** Provider ID. */
    val provider: String? = null,
    /** Provider name. */
    @SerialName("provider_name") val providerName: String? = null,
    /** Non-secret fields; secret ones only as `set`. */
    val values: Map<String, String> = emptyMap(),
    /** Users. */
    @SerialName("used_by") val usedBy: List<String> = emptyList(),
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Last update. */
    @SerialName("updated_at") val updatedAt: IsoInstant? = null,
)

/** Body of `POST /certs/dns-accounts`. [values] contains provider credentials and is redacted in [toString]. */
@Serializable
public data class CreateDnsAccountRequest(
    /** Name. */
    val name: String,
    /** Provider ID (see `GET /certs/dns-providers`). */
    val provider: String,
    /** Provider fields (secret). */
    val values: Map<String, String> = emptyMap(),
) {
    override fun toString(): String =
        "CreateDnsAccountRequest(name=$name, provider=$provider, values=<redacted>)"
}

/** Body of `PUT /certs/dns-accounts/{id}`. [values] is redacted in [toString]. */
@Serializable
public data class UpdateDnsAccountRequest(
    /** Name. */
    val name: String? = null,
    /** Provider ID. */
    val provider: String? = null,
    /** Provider fields (secret). */
    val values: Map<String, String>? = null,
) {
    override fun toString(): String =
        "UpdateDnsAccountRequest(name=$name, provider=$provider, values=<redacted>)"
}

/** An own certificate authority. */
@Serializable
public data class CA(
    /** ID. */
    val id: Long? = null,
    /** Name. */
    val name: String? = null,
    /** Subject. */
    val subject: String? = null,
    /** Valid until. */
    @SerialName("not_after") val notAfter: IsoInstant? = null,
    /** Imported CA. */
    val imported: Boolean? = null,
    /** Self-signed, otherwise an intermediate CA. */
    val root: Boolean? = null,
    /** Permitted DNS names. */
    @SerialName("permitted_dns") val permittedDns: List<String> = emptyList(),
    /** Permitted IP ranges. */
    @SerialName("permitted_ip") val permittedIp: List<String> = emptyList(),
    /** SHA-256 fingerprint. */
    @SerialName("fingerprint_sha256") val fingerprintSha256: String? = null,
    /** Number of certificates issued with it. */
    val certificates: Int? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
)

/** Body of `POST /cas` (valid for 10 years). */
@Serializable
public data class CreateCaRequest(
    /** Name. */
    val name: String,
    /** Key type. */
    @SerialName("key_type") val keyType: CertificateKeyType? = null,
    /** Restrict the CA to `domains`. */
    val constrained: Boolean? = null,
    /** Permitted domains. */
    val domains: List<String>? = null,
)

/** Body of `POST /cas/import`: PEM `cert` + `key` or `p12`. Key and passwords are redacted in [toString]. */
@Serializable
public data class ImportCaRequest(
    /** Name; `null`/empty = common name of the certificate. */
    val name: String? = null,
    /** Certificate (PEM, intermediate CA with chain). */
    val cert: String? = null,
    /** Private key (secret). */
    val key: String? = null,
    /** Password (secret). */
    val password: String? = null,
    /** Base64 encoded PKCS#12 file (secret). */
    val p12: String? = null,
) {
    override fun toString(): String =
        "ImportCaRequest(name=$name, cert=$cert, key=<redacted>, password=<redacted>, p12=<redacted>)"
}

/** A further ACME CA besides Let's Encrypt. */
@Serializable
public data class ACMECA(
    /** ID. */
    val id: Long? = null,
    /** Name. */
    val name: String? = null,
    /** Directory URL. */
    val directory: String? = null,
    /** External account binding in use. */
    val eab: Boolean? = null,
    /** EAB key ID. */
    @SerialName("eab_kid") val eabKid: String? = null,
    /** Custom root certificate. */
    @SerialName("custom_root") val customRoot: Boolean? = null,
    /** Number of certificates. */
    val certificates: Int? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
)

/** Body of `POST /acme-cas`. [eabHmac] is redacted in [toString]. */
@Serializable
public data class CreateAcmeCaRequest(
    /** Name. */
    val name: String,
    /** Directory URL. */
    val directory: String,
    /** Accept the terms of service of the CA (required by the API). */
    @SerialName("accept_tos") val acceptTos: Boolean,
    /** EAB key ID (required if the CA demands EAB). */
    @SerialName("eab_kid") val eabKid: String? = null,
    /** EAB HMAC key (secret). */
    @SerialName("eab_hmac") val eabHmac: String? = null,
    /** Custom root certificate (PEM), e.g. for step-ca. */
    @SerialName("root_pem") val rootPem: String? = null,
) {
    override fun toString(): String =
        "CreateAcmeCaRequest(name=$name, directory=$directory, acceptTos=$acceptTos, eabKid=$eabKid, eabHmac=<redacted>, rootPem=$rootPem)"
}

/**
 * Result of `GET /certs/{id}/bundle` with ETag support.
 *
 * Not a data class: [content] may contain the private key, so [toString] only shows its size.
 */
public class CertBundleResult(
    /** The file content, or `null` if the server answered `304 Not Modified`. */
    public val content: ByteArray?,
    /** ETag of the response (= SHA-256 fingerprint); pass it as `ifNoneMatch` on the next call. */
    public val etag: String?,
    /** Value of the `X-Cert-Not-After` header (expiry of the certificate), as sent by the server. */
    public val notAfter: String?,
) {
    /** True if the certificate did not change since the given ETag (HTTP 304). */
    public val notModified: Boolean get() = content == null

    override fun toString(): String =
        "CertBundleResult(content=${content?.let { "${it.size} bytes" }}, etag=$etag, notAfter=$notAfter)"
}
