package de.sr.ipv64

import de.sr.ipv64.api.AccountApi
import de.sr.ipv64.api.ApiKeysApi
import de.sr.ipv64.api.CdnApi
import de.sr.ipv64.api.CloudRoutersApi
import de.sr.ipv64.api.Dns64Api
import de.sr.ipv64.api.DomainsApi
import de.sr.ipv64.api.DynDnsApi
import de.sr.ipv64.api.FailoverApi
import de.sr.ipv64.api.GatewaysApi
import de.sr.ipv64.api.Hc2Api
import de.sr.ipv64.api.HealthchecksApi
import de.sr.ipv64.api.HoneypotApi
import de.sr.ipv64.api.IntegrationsApi
import de.sr.ipv64.api.MetaApi
import de.sr.ipv64.api.NodesApi
import de.sr.ipv64.api.OfflinePagesApi
import de.sr.ipv64.api.PeersApi
import de.sr.ipv64.api.RecordsApi
import de.sr.ipv64.api.StatusPagesApi
import de.sr.ipv64.api.ThreatApi
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.SharedOkHttpClient
import de.sr.ipv64.internal.ipv64ApiException
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient

/**
 * Client for the [IPv64.net API v2](https://ipv64.net/api/v2/docs).
 *
 * All calls are `suspend` functions, are main-safe and throw an [IPv64Exception] on failure.
 * Create one instance per API key and reuse it – instances are thread safe and share the
 * underlying [OkHttpClient] connection pool.
 *
 * ```kotlin
 * val ipv64 = IPv64(apiKey = "…")
 * val domains = ipv64.domains.list()
 * ipv64.dynDns.update("meinname.ipv64.de")
 * ```
 *
 * @param apiKey API key from *Account > API*. May be `null` for the public endpoints
 *   (e.g. [MetaApi] and parts of [ThreatApi]).
 * @param actAs Account ID or e-mail of a shared account (header `X-IPv64-Act-As`).
 *   Every request is then executed inside that account. See [withActAs].
 * @param authScheme How the key is transmitted, `Authorization: Bearer` by default.
 * @param baseUrl Base URL of the API, mainly useful for tests.
 * @param okHttpClient Custom client, e.g. to add logging or certificate pinning.
 * @param userAgent Value of the `User-Agent` header.
 */
public class IPv64(
    public val apiKey: String? = null,
    public val actAs: String? = null,
    public val authScheme: IPv64AuthScheme = IPv64AuthScheme.Bearer,
    baseUrl: String = DEFAULT_BASE_URL,
    private val okHttpClient: OkHttpClient = SharedOkHttpClient.instance,
    private val userAgent: String = DEFAULT_USER_AGENT,
) {
    private val baseHttpUrl: HttpUrl = baseUrl.trimEnd('/').toHttpUrl()

    private val transport = HttpTransport(
        client = okHttpClient,
        baseUrl = baseHttpUrl,
        defaultHeaders = buildMap {
            put("User-Agent", userAgent)
            apiKey?.let {
                when (authScheme) {
                    IPv64AuthScheme.Bearer -> put("Authorization", "Bearer $it")
                    IPv64AuthScheme.ApiKeyHeader -> put("X-API-Key", it)
                }
            }
            actAs?.let { put("X-IPv64-Act-As", it) }
        },
        errorMapper = ::ipv64ApiException,
    )

    /** Base URL every request is sent to. */
    public val baseUrl: String get() = baseHttpUrl.toString()

    /** Service information, health, own IP and the OpenAPI document. */
    public val meta: MetaApi = MetaApi(transport)

    /** Account data, logs and account links. */
    public val account: AccountApi = AccountApi(transport)

    /** Management of API keys. */
    public val apiKeys: ApiKeysApi = ApiKeysApi(transport)

    /** DynDNS and own domains. */
    public val domains: DomainsApi = DomainsApi(transport)

    /** DynDNS updates. */
    public val dynDns: DynDnsApi = DynDnsApi(transport)

    /** DNS records. */
    public val records: RecordsApi = RecordsApi(transport)

    /** Healthchecks (classic). */
    public val healthchecks: HealthchecksApi = HealthchecksApi(transport)

    /** Notification integrations. */
    public val integrations: IntegrationsApi = IntegrationsApi(transport)

    /** DNS failover policies. */
    public val failover: FailoverApi = FailoverApi(transport)

    /** Status pages. */
    public val statusPages: StatusPagesApi = StatusPagesApi(transport)

    /** Offline pages. */
    public val offlinePages: OfflinePagesApi = OfflinePagesApi(transport)

    /** CDN domains, mappings and access lists. */
    public val cdn: CdnApi = CdnApi(transport)

    /** Cloud routers, routes, firewall and public IPs. */
    public val cloudRouters: CloudRoutersApi = CloudRoutersApi(transport)

    /** Cloud router gateways. */
    public val gateways: GatewaysApi = GatewaysApi(transport)

    /** WireGuard peers. */
    public val peers: PeersApi = PeersApi(transport)

    /** DNS64 clients. */
    public val dns64: Dns64Api = Dns64Api(transport)

    /** Healthchecks 2.0. */
    public val hc2: Hc2Api = Hc2Api(transport)

    /** Node64 nodes. */
    public val nodes: NodesApi = NodesApi(transport)

    /** Honeypot64 sensors and reports. */
    public val honeypot: HoneypotApi = HoneypotApi(transport)

    /** Threat intelligence. */
    public val threat: ThreatApi = ThreatApi(transport)

    /**
     * Returns a client that runs every request inside the shared account [account]
     * (`X-IPv64-Act-As`). Pass `null` to act as the owner of the API key again.
     * The returned client shares the connection pool with this one.
     */
    public fun withActAs(account: String?): IPv64 = IPv64(
        apiKey = apiKey,
        actAs = account,
        authScheme = authScheme,
        baseUrl = baseUrl,
        okHttpClient = okHttpClient,
        userAgent = userAgent,
    )

    override fun toString(): String = "IPv64(baseUrl=$baseUrl, actAs=$actAs, apiKey=${if (apiKey == null) "none" else "***"})"

    public companion object {
        /** Production endpoint. */
        public const val DEFAULT_BASE_URL: String = "https://ipv64.net/api/v2"

        /** Default `User-Agent` header. */
        public const val DEFAULT_USER_AGENT: String = "IPv64-Android/1.0.0"
    }
}
