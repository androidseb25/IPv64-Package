package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IpFamily
import de.sr.ipv64.model.IsoInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** Kind of a proxy host. */
@Serializable
public enum class ProxyHostKind(override val value: String) : ApiValue {
    @SerialName("proxy") PROXY("proxy"),
    @SerialName("redirect") REDIRECT("redirect"),
    @SerialName("dead") DEAD("dead"),
    @SerialName("passthrough") PASSTHROUGH("passthrough"),
    @SerialName("status") STATUS("status"),
    @SerialName("stream") STREAM("stream"),
}

/** Scheme of a target. */
@Serializable
public enum class ProxyScheme(override val value: String) : ApiValue {
    @SerialName("http") HTTP("http"),
    @SerialName("https") HTTPS("https"),
}

/** Minimum TLS version. */
@Serializable
public enum class MinTlsVersion(override val value: String) : ApiValue {
    @SerialName("1.2") TLS_1_2("1.2"),
    @SerialName("1.3") TLS_1_3("1.3"),
}

/** Scheme of a redirect. */
@Serializable
public enum class RedirectScheme(override val value: String) : ApiValue {
    @SerialName("auto") AUTO("auto"),
    @SerialName("http") HTTP("http"),
    @SerialName("https") HTTPS("https"),
}

/** Country restriction: off, block these countries, or allow only these. */
@Serializable
public enum class ProxyGeoMode(override val value: String) : ApiValue {
    @SerialName("") OFF(""),
    @SerialName("block") BLOCK("block"),
    @SerialName("allow") ALLOW("allow"),
}

/** Action of a location: forward to the target, block, or answer with a fixed response. */
@Serializable
public enum class LocationAction(override val value: String) : ApiValue {
    @SerialName("") FORWARD(""),
    @SerialName("block") BLOCK("block"),
    @SerialName("respond") RESPOND("respond"),
}

/** Buffering of a location: as the host, on, or off. */
@Serializable
public enum class LocationBuffering(override val value: String) : ApiValue {
    @SerialName("") INHERIT(""),
    @SerialName("on") ON("on"),
    @SerialName("off") OFF("off"),
}

/** PROXY protocol sent to the target: off, v1 (text) or v2 (binary). */
@Serializable
public enum class ProxyProtocol(override val value: String) : ApiValue {
    @SerialName("") OFF(""),
    @SerialName("v1") V1("v1"),
    @SerialName("v2") V2("v2"),
}

/** Protocol to the target: default (host setting / automatic), auto, HTTP/1.1 only or HTTP/2 cleartext (gRPC). */
@Serializable
public enum class TargetHttp(override val value: String) : ApiValue {
    @SerialName("") DEFAULT(""),
    @SerialName("auto") AUTO("auto"),
    @SerialName("h1") H1("h1"),
    @SerialName("h2c") H2C("h2c"),
}

/** Style of the maintenance page. */
@Serializable
public enum class MaintenanceStyle(override val value: String) : ApiValue {
    @SerialName("matrix") MATRIX("matrix"),
    @SerialName("gear") GEAR("gear"),
    @SerialName("custom") CUSTOM("custom"),
}

/** Login service (forward auth) of an access list. */
@Serializable
public enum class AccessListAuth(override val value: String) : ApiValue {
    @SerialName("") USERS(""),
    @SerialName("authentik") AUTHENTIK("authentik"),
    @SerialName("authelia") AUTHELIA("authelia"),
    @SerialName("oauth2-proxy") OAUTH2_PROXY("oauth2-proxy"),
    @SerialName("custom") CUSTOM("custom"),
}

/** Action of an access rule. */
@Serializable
public enum class AccessRuleAction(override val value: String) : ApiValue {
    @SerialName("allow") ALLOW("allow"),
    @SerialName("deny") DENY("deny"),
}

/** Reachability of a target. */
@Serializable
public enum class HostReachState(override val value: String) : ApiValue {
    @SerialName("unknown") UNKNOWN("unknown"),
    @SerialName("up") UP("up"),
    @SerialName("slow") SLOW("slow"),
    @SerialName("down") DOWN("down"),
}

/** Certificate state of a host. */
@Serializable
public enum class HostCertificateState(override val value: String) : ApiValue {
    @SerialName("http") HTTP("http"),
    @SerialName("ok") OK("ok"),
    @SerialName("soon") SOON("soon"),
    @SerialName("expired") EXPIRED("expired"),
    @SerialName("missing") MISSING("missing"),
}

/** Result of creating a DNS record. */
@Serializable
public enum class HostDnsRecordResult(override val value: String) : ApiValue {
    @SerialName("created") CREATED("created"),
    @SerialName("exists") EXISTS("exists"),
    @SerialName("not_ipv64") NOT_IPV64("not_ipv64"),
    @SerialName("apex_cname") APEX_CNAME("apex_cname"),
    @SerialName("error") ERROR("error"),
}

/** Source when requesting a certificate along with a host. */
@Serializable
public enum class RequestCertificateSource(override val value: String) : ApiValue {
    @SerialName("auto") AUTO("auto"),
    @SerialName("ipv64") IPV64("ipv64"),
    @SerialName("alias") ALIAS("alias"),
    @SerialName("http") HTTP("http"),
    @SerialName("dns") DNS("dns"),
    @SerialName("ca") CA("ca"),
    @SerialName("self") SELF("self"),
}

/** DNS record type to create. */
@Serializable
public enum class HostDnsRecordType(override val value: String) : ApiValue {
    @SerialName("CNAME") CNAME("CNAME"),
    @SerialName("A") A("A"),
    @SerialName("AAAA") AAAA("AAAA"),
}

/** Edition of a blocklist. */
@Serializable
public enum class BlocklistEdition(override val value: String) : ApiValue {
    @SerialName("live") LIVE("live"),
    @SerialName("daily") DAILY("daily"),
}

/** Result of switching a name online. */
@Serializable
public enum class OnlineResultState(override val value: String) : ApiValue {
    @SerialName("on") ON("on"),
    @SerialName("skipped") SKIPPED("skipped"),
    @SerialName("error") ERROR("error"),
}

/** A HTTP header. */
@Serializable
public data class CDNHeader(
    /** Header name. */
    val name: String? = null,
    /** Header value. */
    val value: String? = null,
)

/** A location (path) of a host with its own target. */
@Serializable
public data class CDNLocation(
    /** Path. */
    val path: String? = null,
    /** Scheme of the target. */
    val scheme: ProxyScheme? = null,
    /** Target host. */
    @SerialName("target_host") val targetHost: String? = null,
    /** Target port. */
    @SerialName("target_port") val targetPort: Int? = null,
    /** Replaces the path at the target. */
    @SerialName("forward_path") val forwardPath: String? = null,
    /** WebSockets. */
    val websockets: Boolean? = null,
    /** Access list, 0 = as the host. */
    @SerialName("access_list_id") val accessListId: Long? = null,
    /** Action: `""` = to the target (default), `block`, `respond`; block and respond need no target. */
    val action: LocationAction? = null,
    /** block: 400-499 (default 403), respond: 200-599 (default 200). */
    val status: Int? = null,
    /** respond: content, at most 64 KB. */
    val body: String? = null,
    /** respond: default `text/plain; charset=utf-8`. */
    @SerialName("content_type") val contentType: String? = null,
    /** Own values of the location (action `""` only); unset ones come from the host. */
    val advanced: CDNLocationAdvanced? = null,
    /** On denied access (403) redirect with 302 to this relative path or http(s) URL, `""` = error page; not for login lists. */
    @SerialName("denied_redirect") val deniedRedirect: String? = null,
)

/** Own advanced values of a location. */
@Serializable
public data class CDNLocationAdvanced(
    /** Timeout in seconds until the target answers, 0 = as the host. */
    @SerialName("timeout_seconds") val timeoutSeconds: Int? = null,
    /** Maximum body size in MB, 0 = as the host, -1 = unlimited. */
    @SerialName("max_body_mb") val maxBodyMb: Int? = null,
    /** Buffering: `""` = as the host, `off` = pass on immediately (streaming). */
    val buffering: LocationBuffering? = null,
    /** Headers added to requests. */
    @SerialName("request_headers") val requestHeaders: List<CDNHeader> = emptyList(),
    /** Request headers to remove. */
    @SerialName("request_remove") val requestRemove: List<String> = emptyList(),
    /** Headers added to responses. */
    @SerialName("response_headers") val responseHeaders: List<CDNHeader> = emptyList(),
    /** Response headers to remove. */
    @SerialName("response_remove") val responseRemove: List<String> = emptyList(),
    /** Protocol to the target of this location: `""` = as the host, `auto`, `h1`, `h2c`. */
    @SerialName("target_http") val targetHttp: TargetHttp? = null,
)

/** Advanced settings of a host. */
@Serializable
public data class CDNAdvanced(
    /** Headers added to requests. */
    @SerialName("request_headers") val requestHeaders: List<CDNHeader> = emptyList(),
    /** Request headers to remove. */
    @SerialName("request_remove") val requestRemove: List<String> = emptyList(),
    /** Headers added to responses. */
    @SerialName("response_headers") val responseHeaders: List<CDNHeader> = emptyList(),
    /** Response headers to remove. */
    @SerialName("response_remove") val responseRemove: List<String> = emptyList(),
    /** Host header: empty = visitor name, `target` = target, otherwise a fixed value. */
    @SerialName("host_header") val hostHeader: String? = null,
    /** Timeout in seconds. */
    @SerialName("timeout_seconds") val timeoutSeconds: Int? = null,
    /** Maximum body size in MB. */
    @SerialName("max_body_mb") val maxBodyMb: Int? = null,
    /** Disable buffering. */
    @SerialName("no_buffering") val noBuffering: Boolean? = null,
    /** Compression. */
    val gzip: Boolean? = null,
    /** Trusted proxies. */
    @SerialName("trusted_proxies") val trustedProxies: List<String> = emptyList(),
    /** Write the access log. */
    @SerialName("access_log") val accessLog: Boolean? = null,
    /** Without request limit per IP and per target (connection limits and bans still apply). */
    @SerialName("no_limits") val noLimits: Boolean? = null,
    /** Country restriction mode. */
    @SerialName("geo_mode") val geoMode: ProxyGeoMode? = null,
    /** ISO country codes, e.g. `DE`, `AT`. */
    @SerialName("geo_countries") val geoCountries: List<String> = emptyList(),
    /** Reject AI bots (403) by user agent. */
    @SerialName("ai_bots") val aiBots: Boolean? = null,
    /** Still allow requests made on behalf of a user. */
    @SerialName("ai_bots_allow_user") val aiBotsAllowUser: Boolean? = null,
    /** Serve a `/robots.txt` disallowing all AI bots if the target has none. */
    @SerialName("ai_robots") val aiRobots: Boolean? = null,
    /** Maintenance mode (proxy hosts only): maintenance page (503) instead of the target. */
    val maintenance: Boolean? = null,
    /** Maintenance page style, default `matrix`. */
    @SerialName("maintenance_style") val maintenanceStyle: MaintenanceStyle? = null,
    /** Own HTML (`custom`), at most 64 KB. */
    @SerialName("maintenance_html") val maintenanceHtml: String? = null,
    /** Own error page (proxy hosts only) for 502/503/504; `{{STATUS}}` and `{{HOST}}` are replaced, empty = neutral page. */
    @SerialName("error_html") val errorHtml: String? = null,
    /** Also replace 5xx answers of the target by `error_html` (status code stays). */
    @SerialName("error_override") val errorOverride: Boolean? = null,
    /** Proxy hosts only: path put before every request, empty = unchanged. */
    @SerialName("target_path") val targetPath: String? = null,
    /** Proxy hosts only: answer only the locations, all other paths 404 (needs at least one location). */
    @SerialName("only_locations") val onlyLocations: Boolean? = null,
    /** Bandwidth to the visitor (download) in Mbit/s for all requests of the host, 0 = unlimited (0-100000). */
    @SerialName("bw_down_mbit") val bwDownMbit: Int? = null,
    /** Bandwidth from the visitor (upload) in Mbit/s, 0 = unlimited (0-100000). */
    @SerialName("bw_up_mbit") val bwUpMbit: Int? = null,
    /** Only kind `passthrough`: PROXY protocol to the target, `""` = off. */
    @SerialName("proxy_protocol") val proxyProtocol: ProxyProtocol? = null,
    /** Only proxy hosts: protocol to the target, `""`/`auto` = automatic, `h1` = HTTP/1.1 only, `h2c` = HTTP/2 (gRPC). */
    @SerialName("target_http") val targetHttp: TargetHttp? = null,
)

/** Optionally request a certificate for all domains along with the host (runs in the background). */
@Serializable
public data class ProxyHostRequestCertificate(
    /** Source. */
    val source: RequestCertificateSource? = null,
    /** Alias zone. */
    @SerialName("alias_zone") val aliasZone: String? = null,
    /** Alias prefix. */
    @SerialName("alias_prefix") val aliasPrefix: String? = null,
    /** Staging. */
    val staging: Boolean? = null,
    /** DNS account. */
    @SerialName("dns_account") val dnsAccount: Long? = null,
    /** Own CA (source `ca`). */
    @SerialName("ca_id") val caId: Long? = null,
    /** Accept the Let's Encrypt terms of service (ACME sources) if the account has not yet. */
    @SerialName("accept_tos") val acceptTos: Boolean? = null,
)

/** Optionally create a DNS record in the ipv64 account for each domain (existing ones stay). */
@Serializable
public data class ProxyHostCreateDnsRecord(
    /** Record type. */
    val type: HostDnsRecordType? = null,
    /** Record content. */
    val content: String? = null,
)

/** Body of `POST /cdn64/hosts` and `PUT /cdn64/hosts/{id}`. Missing fields get defaults on create and stay unchanged on update. */
@Serializable
public data class ProxyHostInput(
    /** Domain names. */
    val names: List<String>? = null,
    /** Switched on. */
    val enabled: Boolean? = null,
    /** Scheme of the target. */
    val scheme: ProxyScheme? = null,
    /** Name or IP of the target. */
    @SerialName("target_host") val targetHost: String? = null,
    /** Target port. */
    @SerialName("target_port") val targetPort: Int? = null,
    /** WebSockets. */
    val websockets: Boolean? = null,
    /** Block exploits. */
    @SerialName("block_exploits") val blockExploits: Boolean? = null,
    /** Cache assets. */
    @SerialName("cache_assets") val cacheAssets: Boolean? = null,
    /** 0 = matching certificate by name, -1 = HTTP only, otherwise ID from `/certs`. */
    @SerialName("cert_id") val certId: Long? = null,
    /** Force HTTPS. */
    @SerialName("force_ssl") val forceSsl: Boolean? = null,
    /** HTTP/2. */
    val http2: Boolean? = null,
    /** HTTP/3. */
    val http3: Boolean? = null,
    /** HSTS. */
    val hsts: Boolean? = null,
    /** HSTS for subdomains. */
    @SerialName("hsts_subdomains") val hstsSubdomains: Boolean? = null,
    /** Minimum TLS version. */
    @SerialName("min_tls") val minTls: MinTlsVersion? = null,
    /** Verify the certificate of an https target (default off). */
    @SerialName("verify_target") val verifyTarget: Boolean? = null,
    /** Request a certificate right away. */
    @SerialName("request_certificate") val requestCertificate: ProxyHostRequestCertificate? = null,
    /** Kind; only on create, default `proxy`. */
    val kind: ProxyHostKind? = null,
    /** Redirect: `domain[:port][/path]`. */
    @SerialName("redirect_target") val redirectTarget: String? = null,
    /** Redirect scheme. */
    @SerialName("redirect_scheme") val redirectScheme: RedirectScheme? = null,
    /** Redirect code (300, 301, 302, 303, 307, 308). */
    @SerialName("redirect_code") val redirectCode: Int? = null,
    /** Preserve the path on redirect. */
    @SerialName("preserve_path") val preservePath: Boolean? = null,
    /** Access list, 0 = public. */
    @SerialName("access_list_id") val accessListId: Long? = null,
    /** Locations. */
    val locations: List<CDNLocation>? = null,
    /** Advanced settings. */
    val advanced: CDNAdvanced? = null,
    /** Create DNS records for the domains. */
    @SerialName("create_dns_record") val createDnsRecord: ProxyHostCreateDnsRecord? = null,
    /** Only for kind `status`: ID of the status page (required, > 0); otherwise 0. */
    @SerialName("status_page_id") val statusPageId: Long? = null,
)

/** Reachability of the target of a host. */
@Serializable
public data class ProxyHostReach(
    /** State. */
    val state: HostReachState? = null,
    /** Response time in ms. */
    val ms: Int? = null,
    /** Error. */
    val error: String? = null,
    /** Last check. */
    @SerialName("checked_at") val checkedAt: IsoInstant? = null,
    /** In this state since. */
    val since: IsoInstant? = null,
)

/** Counters of a host since the start of the module. */
@Serializable
public data class ProxyHostStats(
    /** Requests. */
    val requests: Long? = null,
    /** Errors. */
    val errors: Long? = null,
    /** Blocked requests. */
    val blocked: Long? = null,
)

/** Certificate state of a host. */
@Serializable
public data class ProxyHostCertificate(
    /** State. */
    val state: HostCertificateState? = null,
    /** Certificate name. */
    val name: String? = null,
    /** Remaining days. */
    @SerialName("days_left") val daysLeft: Int? = null,
    /** Missing names. */
    val missing: String? = null,
)

/** Result of creating a DNS record for a host name. */
@Serializable
public data class ProxyHostDnsRecord(
    /** Name. */
    val name: String? = null,
    /** Result. */
    val result: HostDnsRecordResult? = null,
    /** Error. */
    val error: String? = null,
)

/** A healthcheck watching exactly this target. */
@Serializable
public data class CDNHostWatch(
    /** Healthcheck token. */
    val token: String? = null,
    /** Name. */
    val name: String? = null,
    /** Type. */
    val type: String? = null,
    /** State (`up`, `down`, `new` …). */
    val state: String? = null,
    /** Paused. */
    val paused: Boolean? = null,
    /** Runs on this node. */
    val local: Boolean? = null,
    /** Ping: only the host matches. */
    @SerialName("host_only") val hostOnly: Boolean? = null,
    /** Last check. */
    @SerialName("last_check") val lastCheck: IsoInstant? = null,
)

/** A proxy host (proxy, redirect, 404, TLS passthrough or status page). */
@Serializable
public data class ProxyHost(
    /** ID. */
    val id: Long? = null,
    /** Kind. */
    val kind: ProxyHostKind? = null,
    /** Domain names. */
    val names: List<String> = emptyList(),
    /** Switched on. */
    val enabled: Boolean? = null,
    /** Scheme of the target. */
    val scheme: ProxyScheme? = null,
    /** Name or IP of the target. */
    @SerialName("target_host") val targetHost: String? = null,
    /** Target port. */
    @SerialName("target_port") val targetPort: Int? = null,
    /** WebSockets. */
    val websockets: Boolean? = null,
    /** Block exploits. */
    @SerialName("block_exploits") val blockExploits: Boolean? = null,
    /** Cache assets. */
    @SerialName("cache_assets") val cacheAssets: Boolean? = null,
    /** 0 = matching certificate by name, -1 = HTTP only, otherwise ID from `/certs`. */
    @SerialName("cert_id") val certId: Long? = null,
    /** Force HTTPS. */
    @SerialName("force_ssl") val forceSsl: Boolean? = null,
    /** HTTP/2. */
    val http2: Boolean? = null,
    /** HTTP/3. */
    val http3: Boolean? = null,
    /** HSTS. */
    val hsts: Boolean? = null,
    /** HSTS for subdomains. */
    @SerialName("hsts_subdomains") val hstsSubdomains: Boolean? = null,
    /** Minimum TLS version. */
    @SerialName("min_tls") val minTls: MinTlsVersion? = null,
    /** Verify the certificate of an https target. */
    @SerialName("verify_target") val verifyTarget: Boolean? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Last update. */
    @SerialName("updated_at") val updatedAt: IsoInstant? = null,
    /** Reachability. */
    val reach: ProxyHostReach? = null,
    /** Counters since the start of the module. */
    val stats: ProxyHostStats? = null,
    /** Certificate state. */
    val certificate: ProxyHostCertificate? = null,
    /** Name belongs to another part (e.g. DNS64). */
    @SerialName("route_error") val routeError: String? = null,
    /** Only after `request_certificate`, if requesting failed. */
    @SerialName("certificate_error") val certificateError: String? = null,
    /** Redirect: `domain[:port][/path]`. */
    @SerialName("redirect_target") val redirectTarget: String? = null,
    /** Redirect scheme. */
    @SerialName("redirect_scheme") val redirectScheme: RedirectScheme? = null,
    /** Redirect code. */
    @SerialName("redirect_code") val redirectCode: Int? = null,
    /** Preserve the path on redirect. */
    @SerialName("preserve_path") val preservePath: Boolean? = null,
    /** Access list, 0 = public. */
    @SerialName("access_list_id") val accessListId: Long? = null,
    /** Locations. */
    val locations: List<CDNLocation> = emptyList(),
    /** Advanced settings. */
    val advanced: CDNAdvanced? = null,
    /** Only after `create_dns_record`. */
    @SerialName("dns_records") val dnsRecords: List<ProxyHostDnsRecord> = emptyList(),
    /** DNS error. */
    @SerialName("dns_error") val dnsError: String? = null,
    /** Healthchecks on this target (only if the Healthchecks module runs). */
    val healthchecks: List<CDNHostWatch> = emptyList(),
    /** Only kind `status`: status page ID. */
    @SerialName("status_page_id") val statusPageId: Long? = null,
)

/** Body of `POST /cdn64/streams` and `PUT /cdn64/streams/{id}` (missing fields stay unchanged on update). */
@Serializable
public data class StreamInput(
    /** Name. */
    val name: String? = null,
    /** Switched on. */
    val enabled: Boolean? = null,
    /** Listen port. */
    @SerialName("listen_port") val listenPort: Int? = null,
    /** TCP. */
    val tcp: Boolean? = null,
    /** UDP. */
    val udp: Boolean? = null,
    /** Target host. */
    @SerialName("target_host") val targetHost: String? = null,
    /** Target port. */
    @SerialName("target_port") val targetPort: Int? = null,
    /** TCP: terminate TLS here with this certificate, 0 = raw. */
    @SerialName("tls_cert_id") val tlsCertId: Long? = null,
    /** Country restriction (changing it does not restart the stream). */
    @SerialName("geo_mode") val geoMode: ProxyGeoMode? = null,
    /** ISO country codes. */
    @SerialName("geo_countries") val geoCountries: List<String>? = null,
    /** Bandwidth to the visitor in Mbit/s for all connections of the stream, 0 = unlimited. */
    @SerialName("bw_down_mbit") val bwDownMbit: Int? = null,
    /** Bandwidth from the visitor in Mbit/s, 0 = unlimited. */
    @SerialName("bw_up_mbit") val bwUpMbit: Int? = null,
    /** Port mapper: expose the port via CDN64 under this name, empty = local only. */
    @SerialName("online_name") val onlineName: String? = null,
    /** TCP only: PROXY protocol to the target, `""` = off (changing it does not restart the stream). */
    @SerialName("proxy_protocol") val proxyProtocol: ProxyProtocol? = null,
)

/** Counters of a stream. */
@Serializable
public data class StreamStats(
    /** Connections. */
    val connections: Long? = null,
    /** Active connections. */
    val active: Long? = null,
    /** Bytes in. */
    @SerialName("bytes_in") val bytesIn: Long? = null,
    /** Bytes out. */
    @SerialName("bytes_out") val bytesOut: Long? = null,
)

/** A TCP/UDP stream. */
@Serializable
public data class Stream(
    /** ID. */
    val id: Long? = null,
    /** Name. */
    val name: String? = null,
    /** Switched on. */
    val enabled: Boolean? = null,
    /** Listen port. */
    @SerialName("listen_port") val listenPort: Int? = null,
    /** TCP. */
    val tcp: Boolean? = null,
    /** UDP. */
    val udp: Boolean? = null,
    /** Target host. */
    @SerialName("target_host") val targetHost: String? = null,
    /** Target port. */
    @SerialName("target_port") val targetPort: Int? = null,
    /** TCP: terminate TLS here with this certificate, 0 = raw. */
    @SerialName("tls_cert_id") val tlsCertId: Long? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Last update. */
    @SerialName("updated_at") val updatedAt: IsoInstant? = null,
    /** Running. */
    val running: Boolean? = null,
    /** Reachability (not documented in detail). */
    val reach: JsonElement? = null,
    /** Why the tcp/udp port is not open. */
    @SerialName("listen_errors") val listenErrors: Map<String, String> = emptyMap(),
    /** Counters. */
    val stats: StreamStats? = null,
    /** Certificate name. */
    @SerialName("cert_name") val certName: String? = null,
    /** Country restriction. */
    @SerialName("geo_mode") val geoMode: ProxyGeoMode? = null,
    /** ISO country codes. */
    @SerialName("geo_countries") val geoCountries: List<String> = emptyList(),
    /** Bandwidth to the visitor in Mbit/s for all connections of the stream, 0 = unlimited. */
    @SerialName("bw_down_mbit") val bwDownMbit: Int? = null,
    /** Bandwidth from the visitor in Mbit/s, 0 = unlimited. */
    @SerialName("bw_up_mbit") val bwUpMbit: Int? = null,
    /** TCP only: PROXY protocol to the target, `""` = off. */
    @SerialName("proxy_protocol") val proxyProtocol: ProxyProtocol? = null,
)

/** An IP rule of an access list. */
@Serializable
public data class CDNAccessListRule(
    /** Allow or deny. */
    val action: AccessRuleAction? = null,
    /** Address or network in CIDR. */
    val cidr: String? = null,
)

/** A user of an access list (response, without password). */
@Serializable
public data class CDNAccessListUser(
    /** User name. */
    val username: String? = null,
)

/** An access list (without passwords). */
@Serializable
public data class CDNAccessList(
    /** ID. */
    val id: Long? = null,
    /** Name. */
    val name: String? = null,
    /** Any condition suffices. */
    @SerialName("satisfy_any") val satisfyAny: Boolean? = null,
    /** Pass the authorization to the target. */
    @SerialName("pass_auth") val passAuth: Boolean? = null,
    /** Users. */
    val users: List<CDNAccessListUser> = emptyList(),
    /** IP rules. */
    val rules: List<CDNAccessListRule> = emptyList(),
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Last update. */
    @SerialName("updated_at") val updatedAt: IsoInstant? = null,
)

/** A user of an access list (request). [password] is redacted in [toString]. */
@Serializable
public data class CDNAccessListInputUser(
    /** User name. */
    val username: String,
    /** Password (secret); `null`/empty keeps the current password of an existing user. */
    val password: String? = null,
) {
    override fun toString(): String =
        "CDNAccessListInputUser(username=$username, password=<redacted>)"
}

/** Body of `POST /cdn64/access-lists` and `PUT /cdn64/access-lists/{id}`. Passwords are redacted in [toString]. */
@Serializable
public data class CDNAccessListInput(
    /** Name. */
    val name: String? = null,
    /** Any condition suffices. */
    @SerialName("satisfy_any") val satisfyAny: Boolean? = null,
    /** Pass the authorization to the target. */
    @SerialName("pass_auth") val passAuth: Boolean? = null,
    /** Replaces the users; missing = unchanged. */
    val users: List<CDNAccessListInputUser>? = null,
    /** IP rules. */
    val rules: List<CDNAccessListRule>? = null,
    /** Sign in via a login page instead of the browser dialog. */
    @SerialName("login_page") val loginPage: Boolean? = null,
    /** Login service (forward auth); `""` = users of the list. */
    val auth: AccessListAuth? = null,
    /** Settings of the login service. */
    val forward: CDNAccessListForward? = null,
    /** Pass Remote-User, -Email, -Groups, -Name to the target. */
    @SerialName("pass_user") val passUser: Boolean? = null,
    /** Name of a new access key; the key appears once in the response. */
    @SerialName("add_key") val addKey: String? = null,
    /** Access keys to delete (start/hint). */
    @SerialName("remove_keys") val removeKeys: List<String>? = null,
)

/** Settings of the login service of an access list. */
@Serializable
public data class CDNAccessListForward(
    /** Check address, e.g. `http://authentik-server:9000/outpost.goauthentik.io/auth/traefik`. */
    val address: String? = null,
    /** Response headers passed to the target. */
    val headers: List<String>? = null,
    /** Path of the return route on the host. */
    val callback: String? = null,
)

/** An access list with the hosts using it. */
@Serializable
public data class CDNAccessListUsage(
    /** The access list. */
    @SerialName("access_list") val accessList: CDNAccessList? = null,
    /** Names of the hosts using it. */
    @SerialName("used_by") val usedBy: List<String> = emptyList(),
)

/** Response of creating a healthcheck for a host or stream. */
@Serializable
public data class ProxyHealthcheckRef(
    /** Token of the new healthcheck. */
    val token: String? = null,
)

/** Response of `POST /cdn64/check`. */
@Serializable
public data class ProxyCheckState(
    /** The check is running. */
    val checking: Boolean? = null,
)

/** Default page for unknown names. */
@Serializable
public data class ProxyDefaultPage(
    /** Answer. */
    val default: DefaultPageMode? = null,
    /** Redirect URL. */
    @SerialName("default_url") val defaultUrl: String? = null,
    /** HTML. */
    @SerialName("default_html") val defaultHtml: String? = null,
)

/** Body of `PUT /cdn64/default` (missing fields stay unchanged). */
@Serializable
public data class UpdateProxyDefaultPageRequest(
    /** Answer. */
    val default: DefaultPageMode? = null,
    /** Redirect URL. */
    @SerialName("default_url") val defaultUrl: String? = null,
    /** HTML. */
    @SerialName("default_html") val defaultHtml: String? = null,
)

/** An entry of the access log. */
@Serializable
public data class ProxyLogEntry(
    /** ID. */
    val id: Long? = null,
    /** Time. */
    val time: IsoInstant? = null,
    /** Host ID, 0 = connection dropped before the first request. */
    @SerialName("host_id") val hostId: Long? = null,
    /** Client. */
    val client: String? = null,
    /** Method. */
    val method: String? = null,
    /** Host. */
    val host: String? = null,
    /** Path. */
    val path: String? = null,
    /** Status, 0 = dropped. */
    val status: Int? = null,
    /** Bytes. */
    val bytes: Long? = null,
    /** Duration in ms. */
    val ms: Long? = null,
    /** User agent. */
    @SerialName("user_agent") val userAgent: String? = null,
    /** Reject reason (blocklist, ban, conns, conn_rate, max_conns, req_rate, target, exploit, acl); not in the schema. */
    val reason: String? = null,
)

/** Retention settings of the access log. */
@Serializable
public data class ProxySettings(
    /** Retention of the access log in days. */
    @SerialName("log_days") val logDays: Int? = null,
    /** Anonymize the log. */
    val anonymize: Boolean? = null,
)

/** Body of `PUT /cdn64/settings`. */
@Serializable
public data class UpdateProxySettingsRequest(
    /** Retention of the access log in days. */
    @SerialName("log_days") val logDays: Int? = null,
    /** Anonymize the log. */
    val anonymize: Boolean? = null,
)

/** A blocklist. */
@Serializable
public data class CDNBlocklist(
    /** ID. */
    val id: String? = null,
    /** Name. */
    val name: String? = null,
    /** Category. */
    val category: String? = null,
    /** Description. */
    val text: String? = null,
    /** License. */
    val license: String? = null,
    /** Source. */
    val source: String? = null,
    /** Switched on. */
    val enabled: Boolean? = null,
    /** Number of entries. */
    val entries: Long? = null,
    /** Last update. */
    val updated: IsoInstant? = null,
    /** Last change. */
    val changed: IsoInstant? = null,
    /** Error. */
    val error: String? = null,
    /** Edition. */
    val edition: BlocklistEdition? = null,
    /** Live edition denied. */
    @SerialName("live_denied") val liveDenied: Boolean? = null,
    /** Live edition error. */
    @SerialName("live_error") val liveError: String? = null,
)

/** A country list of the country restriction. */
@Serializable
public data class CDNBlocklistCountry(
    /** Country code. */
    val code: String? = null,
    /** Entries. */
    val entries: Long? = null,
    /** Last update. */
    val updated: IsoInstant? = null,
    /** Error. */
    val error: String? = null,
)

/** State of the AI bot user agent list. */
@Serializable
public data class CDNBlocklistAiBots(
    /** Needed by a host. */
    val needed: Boolean? = null,
    /** Entries. */
    val entries: Long? = null,
    /** User entries. */
    val users: Long? = null,
    /** Last update. */
    val updated: IsoInstant? = null,
    /** Last change. */
    val changed: IsoInstant? = null,
    /** Error. */
    val error: String? = null,
    /** Blocked since start. */
    val blocked: Long? = null,
)

/** State of the ipv64 blocklists. */
@Serializable
public data class CDNBlocklists(
    /** Lists. */
    val lists: List<CDNBlocklist> = emptyList(),
    /** Exceptions: IP addresses or networks. */
    val allow: List<String> = emptyList(),
    /** Rejected since start. */
    val blocked: Long? = null,
    /** At least one list is loaded and on. */
    val active: Boolean? = null,
    /** Country lists of the country restriction. */
    val countries: List<CDNBlocklistCountry> = emptyList(),
    /** Rejected by the country restriction since start. */
    @SerialName("geo_blocked") val geoBlocked: Long? = null,
    /** A list token for the live edition is stored (never returned). */
    @SerialName("token_set") val tokenSet: Boolean? = null,
    /** AI bot user agent list. */
    @SerialName("ai_bots") val aiBots: CDNBlocklistAiBots? = null,
    /** Own blocklist: IP addresses or networks rejected for all hosts and streams (exceptions win). */
    val deny: List<String> = emptyList(),
    /** Rejected by the own blocklist since start. */
    val denied: Long? = null,
)

/** Body of `PUT /cdn64/blocklists`. [token] is redacted in [toString]. */
@Serializable
public data class UpdateBlocklistsRequest(
    /** IDs of the enabled lists. */
    val enabled: List<String>? = null,
    /** Exceptions: IP addresses or networks. */
    val allow: List<String>? = null,
    /** List token for the live edition (secret). */
    val token: String? = null,
    /** Own blocklist: IP addresses or networks. */
    val deny: List<String>? = null,
) {
    override fun toString(): String =
        "UpdateBlocklistsRequest(enabled=$enabled, allow=$allow, deny=$deny, token=<redacted>)"
}

/** Overload protection per IPv4 address or IPv6 /64; 0 = limit off. */
@Serializable
public data class CDNLimits(
    /** Protection on. */
    val enabled: Boolean? = null,
    /** Simultaneous connections per IP (default 200). */
    @SerialName("conns_per_ip") val connsPerIp: Int? = null,
    /** New connections per IP and second (default 20). */
    @SerialName("conn_rate") val connRate: Int? = null,
    /** Requests per IP and second, above that 429 (default 50). */
    @SerialName("req_rate") val reqRate: Int? = null,
    /** Requests in a row (default 500). */
    @SerialName("req_burst") val reqBurst: Int? = null,
    /** Open connections in total (default 10000). */
    @SerialName("max_conns") val maxConns: Int? = null,
    /** Simultaneous requests per proxy host to its target. */
    @SerialName("target_conns") val targetConns: Int? = null,
    /** Rejections per minute before a ban (default 300). */
    @SerialName("ban_after") val banAfter: Int? = null,
    /** Ban duration (default 10). */
    @SerialName("ban_minutes") val banMinutes: Int? = null,
    /** Never limit private addresses (default true). */
    @SerialName("exempt_lan") val exemptLan: Boolean? = null,
)

/** Body of `PUT /cdn64/limits` (missing fields stay unchanged). */
@Serializable
public data class CDNLimitsRequest(
    /** Protection on. */
    val enabled: Boolean? = null,
    /** Simultaneous connections per IP (default 200). */
    @SerialName("conns_per_ip") val connsPerIp: Int? = null,
    /** New connections per IP and second (default 20). */
    @SerialName("conn_rate") val connRate: Int? = null,
    /** Requests per IP and second, above that 429 (default 50). */
    @SerialName("req_rate") val reqRate: Int? = null,
    /** Requests in a row (default 500). */
    @SerialName("req_burst") val reqBurst: Int? = null,
    /** Open connections in total (default 10000). */
    @SerialName("max_conns") val maxConns: Int? = null,
    /** Simultaneous requests per proxy host to its target. */
    @SerialName("target_conns") val targetConns: Int? = null,
    /** Rejections per minute before a ban (default 300). */
    @SerialName("ban_after") val banAfter: Int? = null,
    /** Ban duration (default 10). */
    @SerialName("ban_minutes") val banMinutes: Int? = null,
    /** Never limit private addresses (default true). */
    @SerialName("exempt_lan") val exemptLan: Boolean? = null,
)

/** A banned address. */
@Serializable
public data class CDNLimitBan(
    /** Address. */
    val ip: String? = null,
    /** Banned until. */
    val until: IsoInstant? = null,
)

/** Overload protection: settings, bans, counters. */
@Serializable
public data class CDNLimitsView(
    /** Current settings. */
    val limits: CDNLimits? = null,
    /** Default settings. */
    val defaults: CDNLimits? = null,
    /** Bans. */
    val bans: List<CDNLimitBan> = emptyList(),
    /** Rejected since start. */
    val rejected: Long? = null,
    /** Open connections. */
    val conns: Long? = null,
)

/** Which rejections are routed to the honeypot. */
@Serializable
public data class CDNHoneypotRoute(
    /** Blocklist rejections. */
    val blocklist: Boolean? = null,
    /** Honeypot rejections. */
    val honeypot: Boolean? = null,
    /** Exploit rejections. */
    val exploit: Boolean? = null,
    /** AI bot rejections. */
    @SerialName("ai_bot") val aiBot: Boolean? = null,
    /** Country restriction rejections. */
    val geo: Boolean? = null,
)

/** Honeypot64 settings. */
@Serializable
public data class CDNHoneypotSettings(
    /** Forward requests without matching host to the Honeypot64 web decoy. */
    val decoy: Boolean? = null,
    /** Forward accesses (metadata only) to the Honeypot64 log sensor. */
    val access: Boolean? = null,
    /** Block IPs reported by the honeypot locally right away. */
    val block: Boolean? = null,
    /** Ban duration in hours (default 24). */
    @SerialName("block_hours") val blockHours: Int? = null,
    /** Which rejections go to the honeypot; missing = default (everything except geo). */
    val route: CDNHoneypotRoute? = null,
)

/** Body of `PUT /cdn64/honeypot` (missing fields stay unchanged). */
@Serializable
public data class CDNHoneypotSettingsRequest(
    /** Forward requests without matching host to the Honeypot64 web decoy. */
    val decoy: Boolean? = null,
    /** Forward accesses (metadata only) to the Honeypot64 log sensor. */
    val access: Boolean? = null,
    /** Block IPs reported by the honeypot locally right away. */
    val block: Boolean? = null,
    /** Ban duration in hours (default 24). */
    @SerialName("block_hours") val blockHours: Int? = null,
    /** Which rejections go to the honeypot; missing = default (everything except geo). */
    val route: CDNHoneypotRoute? = null,
)

/** An IP blocked by the honeypot. */
@Serializable
public data class CDNHoneypotBlock(
    /** Address. */
    val ip: String? = null,
    /** Reason. */
    val reason: String? = null,
    /** Since. */
    val since: IsoInstant? = null,
    /** Until. */
    val until: IsoInstant? = null,
)

/** Honeypot64: settings, blocked IPs, counters. */
@Serializable
public data class CDNHoneypotView(
    /** Settings. */
    val settings: CDNHoneypotSettings? = null,
    /** A web decoy is currently offered. */
    @SerialName("decoy_available") val decoyAvailable: Boolean? = null,
    /** Accesses are currently evaluated. */
    @SerialName("access_available") val accessAvailable: Boolean? = null,
    /** Blocked IPs. */
    val blocks: List<CDNHoneypotBlock> = emptyList(),
    /** Rejected since start. */
    val rejected: Long? = null,
    /** Handed to the decoy since start. */
    val decoyed: Long? = null,
    /** Handed to the log sensor since start. */
    val reported: Long? = null,
)

/** Error body of the CDN64 online endpoints. */
@Serializable
public data class CDNOnlineError(
    /** Error. */
    val error: String? = null,
    /** Code, e.g. `no_api_key`, `api_key_rejected`, `ipv64_unreachable`, `plan_not_allowed`, `limit_reached`. */
    val code: String? = null,
    /** Error text with remedy. */
    val text: String? = null,
)

/** A CDN64 server (edge) of the tunnel. */
@Serializable
public data class CDNOnlineEdge(
    /** Name. */
    val name: String? = null,
    /** Host. */
    val host: String? = null,
    /** Connected. */
    val connected: Boolean? = null,
    /** Transport (`quic`, `tcp` or empty). */
    val transport: String? = null,
    /** Total. */
    val total: Int? = null,
    /** Round trip time to the server in ms. */
    @SerialName("rtt_ms") val rttMs: Double? = null,
    /** Packet loss of the node to this server in the last 30 s in percent; -1 = unknown. */
    @SerialName("loss_pct") val lossPct: Double? = null,
)

/** A name switched on at CDN64. */
@Serializable
public data class CDNOnlineName(
    /** Name. */
    val name: String? = null,
    /** Host ID. */
    @SerialName("host_id") val hostId: Long? = null,
    /** ID at ipv64. */
    @SerialName("remote_id") val remoteId: Long? = null,
    /** Mode. */
    val mode: Int? = null,
    /** Check. */
    val check: String? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
)

/** Domain quota. */
@Serializable
public data class CDNOnlineQuotaDomains(
    /** Used. */
    val used: Int? = null,
    /** Used by the old CDN. */
    @SerialName("old_cdn") val oldCdn: Int? = null,
    /** Limit. */
    val limit: Int? = null,
)

/** Traffic quota in MB. */
@Serializable
public data class CDNOnlineQuotaTraffic(
    /** Used. */
    val used: Double? = null,
    /** Used by the old CDN. */
    @SerialName("old_cdn") val oldCdn: Double? = null,
    /** Limit. */
    val limit: Long? = null,
)

/** Quota at ipv64 (old and new CDN combined). */
@Serializable
public data class CDNOnlineQuota(
    /** Domains. */
    val domains: CDNOnlineQuotaDomains? = null,
    /** Traffic. */
    @SerialName("traffic_mb") val trafficMb: CDNOnlineQuotaTraffic? = null,
    /** Bandwidth in Mbit. */
    @SerialName("bandwidth_mbit") val bandwidthMbit: Int? = null,
)

/** CDN64: tunnel, servers, enabled names, quota. */
@Serializable
public data class CDNOnlineStatus(
    /** Tunnel set up. */
    val configured: Boolean? = null,
    /** Tunnel ID at ipv64 (`tn_...`). */
    val tunnel: String? = null,
    /** Servers. */
    val edges: List<CDNOnlineEdge> = emptyList(),
    /** ipv64 account connected. */
    @SerialName("api_ready") val apiReady: Boolean? = null,
    /** Enabled names. */
    val names: List<CDNOnlineName> = emptyList(),
    /** Quota. */
    val quota: CDNOnlineQuota? = null,
    /** Last sync with ipv64. */
    val synced: IsoInstant? = null,
    /** Sync error. */
    @SerialName("sync_error") val syncError: String? = null,
    /** `net.core.rmem_max` of the host. */
    @SerialName("udp_buffer") val udpBuffer: Long? = null,
    /** Too small for full QUIC speed. */
    @SerialName("udp_buffer_low") val udpBufferLow: Boolean? = null,
)

/** A server entry for `PUT /cdn64/online`. */
@Serializable
public data class CDNOnlineEdgeInput(
    /** Name. */
    val name: String? = null,
    /** Host. */
    val host: String? = null,
    /** Port. */
    val port: Int? = null,
    /** Addresses. */
    val ips: List<String>? = null,
)

/** Body of `PUT /cdn64/online` (set the tunnel manually, for tests). [token] is redacted in [toString]. */
@Serializable
public data class SetOnlineTunnelRequest(
    /** Tunnel ID. */
    val tunnel: String? = null,
    /** Tunnel token (secret). */
    val token: String? = null,
    /** Servers. */
    val edges: List<CDNOnlineEdgeInput>? = null,
) {
    override fun toString(): String =
        "SetOnlineTunnelRequest(tunnel=$tunnel, token=<redacted>, edges=$edges)"
}

/** Body of `POST /cdn64/hosts/{id}/online`. */
@Serializable
public data class EnableHostOnlineRequest(
    /** Mode (1). */
    val mode: Int? = null,
    /** Create a healthcheck. */
    val healthcheck: Boolean? = null,
)

/** Result for one name when switching online. */
@Serializable
public data class CDNOnlineResult(
    /** Name. */
    val name: String? = null,
    /** Result. */
    val result: OnlineResultState? = null,
    /** Error code of ipv64 (e.g. `domain_not_in_account`, `cname_exists`, `limit_reached`). */
    val code: String? = null,
    /** Error. */
    val error: String? = null,
    /** Text. */
    val text: String? = null,
)

/** Response of `POST /cdn64/hosts/{id}/online`. */
@Serializable
public data class HostOnlineResult(
    /** Result per name. */
    val results: List<CDNOnlineResult> = emptyList(),
    /** Check. */
    val check: String? = null,
    /** Check error. */
    @SerialName("check_error") val checkError: String? = null,
)

/** One probe of the online self test. */
@Serializable
public data class CDNOnlineProbe(
    /** Server. */
    val edge: String? = null,
    /** Address. */
    val addr: String? = null,
    /** Address family. */
    val family: IpFamily? = null,
    /** Succeeded. */
    val ok: Boolean? = null,
    /** Skipped. */
    val skipped: Boolean? = null,
    /** Duration in ms. */
    val ms: Long? = null,
    /** Certificate trusted. */
    val trusted: Boolean? = null,
    /** Error. */
    val error: String? = null,
)

/** Self test of one name over every server and address. */
@Serializable
public data class CDNOnlineTest(
    /** Name. */
    val name: String? = null,
    /** TLS. */
    val tls: Boolean? = null,
    /** Probes. */
    val probes: List<CDNOnlineProbe> = emptyList(),
    /** Answer of a public resolver. */
    val dns: List<String> = emptyList(),
    /** DNS answer OK. */
    @SerialName("dns_ok") val dnsOk: Boolean? = null,
)

/** Source of an automatic block for `POST /cdn64/release`. */
@Serializable
public enum class ReleaseSource(override val value: String) : ApiValue {
    @SerialName("") ALL(""),
    @SerialName("honeypot") HONEYPOT("honeypot"),
    @SerialName("ban") BAN("ban"),
}

/** Mode of the JA4 attack tool detection. */
@Serializable
public enum class Ja4Mode(override val value: String) : ApiValue {
    @SerialName("off") OFF("off"),
    @SerialName("count") COUNT("count"),
    @SerialName("block") BLOCK("block"),
}

/** Settings of the Docker sync. */
@Serializable
public data class ProxyDockerSettings(
    /** Proxy hosts from Docker labels (`node64.proxy.*`). */
    val enabled: Boolean? = null,
    /** Also read `traefik.*` labels. */
    val traefik: Boolean? = null,
    /** Docker API of this node (`unix://` or `tcp://`); empty = default. */
    @SerialName("docker_host") val dockerHost: String? = null,
    /** Certificate for new Docker hosts: empty, `auto`, `ipv64`, `http`, `dns:<access>`. */
    val certificate: String? = null,
)

/** Docker sync: state, containers and settings. */
@Serializable
public data class ProxyDockerView(
    /** Sync enabled. */
    val enabled: Boolean? = null,
    /** Docker API reachable. */
    val connected: Boolean? = null,
    /** Docker API as used. */
    val host: String? = null,
    /** Error. */
    val error: String? = null,
    /** Last sync. */
    val synced: IsoInstant? = null,
    /** Containers with `node64.proxy.*` or `traefik.*` labels (free-form). */
    val containers: List<JsonObject> = emptyList(),
    /** Settings. */
    val settings: ProxyDockerSettings? = null,
)

/** Body of `PUT /cdn64/docker` (missing fields stay). */
@Serializable
public data class UpdateProxyDockerRequest(
    /** Proxy hosts from Docker labels. */
    val enabled: Boolean? = null,
    /** Also read `traefik.*` labels. */
    val traefik: Boolean? = null,
    /** Docker API of this node. */
    @SerialName("docker_host") val dockerHost: String? = null,
    /** Certificate for new Docker hosts. */
    val certificate: String? = null,
    /** Accept the Let's Encrypt terms (needed with [certificate] until the account accepted them). */
    @SerialName("accept_tos") val acceptTos: Boolean? = null,
)

/** One JA4 fingerprint with its hits. */
@Serializable
public data class CDNJa4Hit(
    /** JA4 fingerprint. */
    val ja4: String? = null,
    /** Hits. */
    val hits: Long? = null,
)

/** Protection of one name in [CDNProtectionStats]. */
@Serializable
public data class CDNProtectionHost(
    /** Name ID at ipv64. */
    val id: Long? = null,
    /** Name. */
    val fqdn: String? = null,
    /** Mode. */
    val mode: Int? = null,
    /** Host ID of this node (missing = none). */
    @SerialName("host_id") val hostId: Long? = null,
    /** Blocked requests. */
    val blocked: Long? = null,
    /** Overload. */
    val overload: Long? = null,
    /** WAF on. */
    val waf: Boolean? = null,
    /** JA4 mode: empty, `off`, `count`, `block`. */
    val ja4: String? = null,
)

/** CDN64 statistics: what the CDN64 servers blocked for the names of the account. */
@Serializable
public data class CDNProtectionStats(
    /** Days. */
    val days: Int? = null,
    /** Global JA4 mode (`count` = start phase, `block`). */
    @SerialName("ja4_global") val ja4Global: String? = null,
    /** Totals (`blocked_list`, `blocked_acl`, `blocked_flood`, `blocked_limit`, `blocked_waf`, `blocked_ja4`, `ja4_hits`, `blocked_ips`, `conns`, `conns_v4`). */
    val totals: JsonObject? = null,
    /** Daily values (free-form). */
    val daily: List<JsonObject> = emptyList(),
    /** Top JA4 fingerprints. */
    @SerialName("ja4_top") val ja4Top: List<CDNJa4Hit> = emptyList(),
    /** Names. */
    val hosts: List<CDNProtectionHost> = emptyList(),
)

/** Body of `POST /cdn64/release`. */
@Serializable
public data class ReleaseBlockRequest(
    /** IP or /64. */
    val ip: String,
    /** Block of the honeypot, the DoS protection, or both (default). */
    val from: ReleaseSource? = null,
)

/** CDN64 servers and connections per server. */
@Serializable
public data class CDNServers(
    /** Assigned servers. */
    val count: Int? = null,
    /** Chosen (0 = maximum or all, also future). */
    val choice: Int? = null,
    /** Maximum. */
    val max: Int? = null,
    /** All servers. */
    val all: Boolean? = null,
    /** Total. */
    val total: Int? = null,
    /** Number selectable (class). */
    val selectable: Boolean? = null,
    /** Assigned servers. */
    val assigned: List<String> = emptyList(),
    /** Currently in DNS. */
    val active: List<String> = emptyList(),
    /** Connections per server. */
    val sessions: Int? = null,
    /** Maximum connections per server. */
    @SerialName("sessions_max") val sessionsMax: Int? = null,
    /** Failover: failed to replacement. */
    val replaced: Map<String, String> = emptyMap(),
    /** Selectable counts (0 = all); empty = fixed. */
    val options: List<Int> = emptyList(),
    /** Selectable connections per server; empty = fixed. */
    @SerialName("session_options") val sessionOptions: List<Int> = emptyList(),
)

/** Body of `PUT /cdn64/online/servers` and `PUT /cdn64/hosts/{id}/servers`. */
@Serializable
public data class SetCDNServersRequest(
    /** Servers (0 = all, also future; only classes without limit). */
    val count: Int,
    /** Connections per server (min. 1); missing = unchanged. */
    val sessions: Int? = null,
)

/** Body of the pause endpoints. */
@Serializable
public data class PauseOnlineRequest(
    /** Pause (true) or resume (false). */
    val paused: Boolean? = null,
)

/** Body of `POST /cdn64/hosts/{id}/localdns`; without addresses PROXY64 uses the suggested ones. */
@Serializable
public data class HostLocalDnsRequest(
    /** IPv4 (A records). */
    val ipv4: String? = null,
    /** IPv6 (AAAA records). */
    val ipv6: String? = null,
)

/** Response of `POST /cdn64/hosts/{id}/localdns`. */
@Serializable
public data class HostLocalDnsResult(
    /** Created records. */
    val added: Int? = null,
    /** `"name type"` already present. */
    val skipped: List<String> = emptyList(),
    /** Excluded names (wildcards, main domains). */
    val excluded: List<String> = emptyList(),
    /** IPv4 used. */
    val ipv4: String? = null,
    /** IPv6 used. */
    val ipv6: String? = null,
)

/** Body of `PUT /cdn64/hosts/{id}/protection` (missing fields stay). */
@Serializable
public data class HostProtectionRequest(
    /** Pattern WAF (mode 2/3). */
    val waf: Boolean? = null,
    /** Attack tools (JA4). */
    val ja4: Ja4Mode? = null,
)

/** CDN64 protection of one name. */
@Serializable
public data class HostProtectionName(
    /** Name ID at ipv64. */
    val id: Long? = null,
    /** Name. */
    val fqdn: String? = null,
    /** WAF on. */
    val waf: Boolean? = null,
    /** JA4 mode. */
    val ja4: String? = null,
)

/** CDN64 protection (WAF, JA4) of all names of a host. */
@Serializable
public data class HostProtection(
    /** WAF on. */
    val waf: Boolean? = null,
    /** JA4 mode. */
    val ja4: String? = null,
    /** Names. */
    val names: List<HostProtectionName> = emptyList(),
)
