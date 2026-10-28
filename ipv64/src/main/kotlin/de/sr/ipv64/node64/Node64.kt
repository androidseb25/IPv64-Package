package de.sr.ipv64.node64

import de.sr.ipv64.IPv64Exception
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.SharedOkHttpClient
import de.sr.ipv64.node64.api.CertificatesApi
import de.sr.ipv64.node64.api.ChannelsApi
import de.sr.ipv64.node64.api.ChecksApi
import de.sr.ipv64.node64.api.ClusterApi
import de.sr.ipv64.node64.api.Dns64Api
import de.sr.ipv64.node64.api.DockerApi
import de.sr.ipv64.node64.api.DynDnsApi
import de.sr.ipv64.node64.api.EdgeApi
import de.sr.ipv64.node64.api.HoneypotApi
import de.sr.ipv64.node64.api.LinkApi
import de.sr.ipv64.node64.api.MaintenanceApi
import de.sr.ipv64.node64.api.NodeApi
import de.sr.ipv64.node64.api.OnlineChecksApi
import de.sr.ipv64.node64.api.ProxmoxApi
import de.sr.ipv64.node64.api.Proxy64Api
import de.sr.ipv64.node64.api.StatusPagesApi
import de.sr.ipv64.node64.internal.node64ApiException
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient

/**
 * Client for the local JSON API of a **Node64** node in your own network.
 *
 * All calls are `suspend` functions, are main-safe and throw an [IPv64Exception]
 * (for HTTP errors a [de.sr.ipv64.Node64ApiException]) on failure.
 * Create one instance per node and reuse it – instances are thread safe and share the
 * underlying [OkHttpClient] connection pool with [de.sr.ipv64.IPv64].
 *
 * ```kotlin
 * val node = Node64(baseUrl = "http://node.example.home64.de/api", apiKey = "node64_…")
 * val status = node.node.status()
 * val checks = node.checks.list()
 * ```
 *
 * Nodes are usually reached via plain `http://` in the local network. Android blocks cleartext
 * traffic by default, so the app has to allow it for the node's host
 * (`network_security_config.xml`) – or use the node's HTTPS address.
 *
 * @param baseUrl Base URL of the node's API including `/api`, e.g. `http://192.168.1.10:8080/api`.
 * @param apiKey API key of the node (*Settings > API keys*, starts with `node64_`). May be `null`
 *   for public endpoints such as [NodeApi.health] and push URLs.
 * @param actor Optional name written to the node's audit log (header `X-Agent-Actor`). See [withActor].
 * @param okHttpClient Custom client, e.g. to add logging or trust a self-signed certificate.
 * @param userAgent Value of the `User-Agent` header.
 */
public class Node64(
    baseUrl: String,
    public val apiKey: String? = null,
    public val actor: String? = null,
    private val okHttpClient: OkHttpClient = SharedOkHttpClient.instance,
    private val userAgent: String = DEFAULT_USER_AGENT,
) {
    private val baseHttpUrl: HttpUrl = baseUrl.trimEnd('/').toHttpUrl()

    private val transport = HttpTransport(
        client = okHttpClient,
        baseUrl = baseHttpUrl,
        defaultHeaders = buildMap {
            put("User-Agent", userAgent)
            apiKey?.let { put("Authorization", "Bearer $it") }
            actor?.let { put("X-Agent-Actor", it) }
        },
        errorMapper = ::node64ApiException,
    )

    /** Base URL every request is sent to. */
    public val baseUrl: String get() = baseHttpUrl.toString()

    /** Node status, modules, metrics, settings, audit, logs, backups, API keys, import/export. */
    public val node: NodeApi = NodeApi(transport)

    /** Ports, port probe and the edge (reverse proxy entry point) configuration. */
    public val edge: EdgeApi = EdgeApi(transport)

    /** Certificates, DNS accounts for ACME, own CAs and ACME CAs. */
    public val certificates: CertificatesApi = CertificatesApi(transport)

    /** Cluster of several nodes (group, join code, members, sync). */
    public val cluster: ClusterApi = ClusterApi(transport)

    /** Link with an ipv64.net account. */
    public val link: LinkApi = LinkApi(transport)

    /** Healthchecks: checks, events, incidents, check types and push URLs. */
    public val checks: ChecksApi = ChecksApi(transport)

    /** Online checks executed from outside via ipv64.net. */
    public val onlineChecks: OnlineChecksApi = OnlineChecksApi(transport)

    /** Maintenance windows of the healthchecks. */
    public val maintenance: MaintenanceApi = MaintenanceApi(transport)

    /** Status pages including notices and logo. */
    public val statusPages: StatusPagesApi = StatusPagesApi(transport)

    /** Docker monitoring (containers and Docker systems). */
    public val docker: DockerApi = DockerApi(transport)

    /** Proxmox VE monitoring. */
    public val proxmox: ProxmoxApi = ProxmoxApi(transport)

    /** Notification channels and deliveries. */
    public val channels: ChannelsApi = ChannelsApi(transport)

    /** DynDNS updater of the node. */
    public val dynDns: DynDnsApi = DynDnsApi(transport)

    /** Local DNS64 resolver with filtering, records, groups, forwards, rewrites and zones. */
    public val dns64: Dns64Api = Dns64Api(transport)

    /** PROXY64 reverse proxy: hosts, streams, access lists, protection and online publishing. */
    public val proxy: Proxy64Api = Proxy64Api(transport)

    /** Honeypot64 sensor of the node. */
    public val honeypot: HoneypotApi = HoneypotApi(transport)

    /**
     * Returns a client that writes [actor] as the actor name into the node's audit log
     * (`X-Agent-Actor`). The returned client shares the connection pool with this one.
     */
    public fun withActor(actor: String?): Node64 = Node64(
        baseUrl = baseUrl,
        apiKey = apiKey,
        actor = actor,
        okHttpClient = okHttpClient,
        userAgent = userAgent,
    )

    override fun toString(): String = "Node64(baseUrl=$baseUrl, actor=$actor, apiKey=${if (apiKey == null) "none" else "***"})"

    public companion object {
        /** Default `User-Agent` header. */
        public const val DEFAULT_USER_AGENT: String = "IPv64-Android/1.0.0 (Node64)"
    }
}
