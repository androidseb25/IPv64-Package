package de.sr.ipv64.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** A domain in the CDN. */
@Serializable
public data class CdnDomain(
    /** CDN ID (token). */
    val id: String? = null,
    /** Domain name. */
    val domain: String? = null,
    /** Domain ID. */
    @SerialName("domain_id") val domainId: Long? = null,
    /** CDN active. */
    val active: Boolean? = null,
    /** Open tasks of the CDN bot. */
    @Serializable(with = CdnPendingTaskListSerializer::class) val pending: List<CdnPendingTask> = emptyList(),
    /** At least one record runs through the CDN. */
    @SerialName("dns_records_enabled") val dnsRecordsEnabled: Boolean? = null,
    /** Assigned CDN server. */
    val server: CdnServer? = null,
    /** Reverse proxy settings. */
    val proxy: CdnProxy? = null,
    /** Portmapper settings. */
    val portmapper: CdnPortmapper? = null,
    /** Traffic in MB. */
    @SerialName("traffic_mb") val trafficMb: CdnTraffic? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
)

/** A domain in the CDN including its port mappings. */
@Serializable
public data class CdnDomainDetail(
    /** CDN ID (token). */
    val id: String? = null,
    /** Domain name. */
    val domain: String? = null,
    /** Domain ID. */
    @SerialName("domain_id") val domainId: Long? = null,
    /** CDN active. */
    val active: Boolean? = null,
    /** Open tasks of the CDN bot. */
    @Serializable(with = CdnPendingTaskListSerializer::class) val pending: List<CdnPendingTask> = emptyList(),
    /** At least one record runs through the CDN. */
    @SerialName("dns_records_enabled") val dnsRecordsEnabled: Boolean? = null,
    /** Assigned CDN server. */
    val server: CdnServer? = null,
    /** Reverse proxy settings. */
    val proxy: CdnProxy? = null,
    /** Portmapper settings. */
    val portmapper: CdnPortmapper? = null,
    /** Traffic in MB. */
    @SerialName("traffic_mb") val trafficMb: CdnTraffic? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Port mappings of the domain. */
    val mappings: List<CdnMapping> = emptyList(),
)

/** CDN server a domain is assigned to. */
@Serializable
public data class CdnServer(
    /** Name. */
    val name: String? = null,
    /** Hostname. */
    val hostname: String? = null,
    /** IPv4 address. */
    val ipv4: String? = null,
    /** IPv6 address. */
    val ipv6: String? = null,
)

/** Reverse proxy settings of a CDN domain. */
@Serializable
public data class CdnProxy(
    /** Reverse proxy state. */
    val state: CdnProxyState? = null,
    /** Frontend protocol handling. */
    val frontend: CdnFrontend? = null,
    /** Connection to the origin server. */
    val backend: CdnProxyBackend? = null,
    /** WebSocket support. */
    val websocket: Boolean? = null,
    /** Certificate in use. */
    val certificate: CdnCertificate? = null,
    /** Cache settings. */
    val caching: CdnProxyCaching? = null,
    /** Access list settings. */
    @SerialName("access_list") val accessList: CdnAccessListSettings? = null,
)

/** Origin server connection of the reverse proxy. */
@Serializable
public data class CdnProxyBackend(
    /** Protocol to the origin server. */
    val scheme: CdnBackendScheme? = null,
    /** Port of the origin server. */
    val port: Int? = null,
)

/** Cache settings of the reverse proxy. */
@Serializable
public data class CdnProxyCaching(
    /** Cache active. */
    val enabled: Boolean? = null,
    /** Cache duration in minutes. */
    @SerialName("time_minutes") val timeMinutes: Int? = null,
    /** Cache size in MB. */
    @SerialName("size_mb") val sizeMb: Int? = null,
)

/** Access list settings of the reverse proxy. */
@Serializable
public data class CdnAccessListSettings(
    /** Access list active. */
    val enabled: Boolean? = null,
    /** Whether entries are allowed or denied. */
    val mode: CdnAccessListMode? = null,
)

/** Portmapper settings of a CDN domain. */
@Serializable
public data class CdnPortmapper(
    /** Portmapper state. */
    val state: CdnPortmapperState? = null,
)

/** Traffic counters in MB. */
@Serializable
public data class CdnTraffic(
    /** Incoming. */
    @SerialName("in") val incoming: Double? = null,
    /** Outgoing. */
    @SerialName("out") val outgoing: Double? = null,
)

/** A port mapping forwarding a public CDN server port to a port at the user's server. */
@Serializable
public data class CdnMapping(
    /** Mapping ID. */
    val id: String? = null,
    /** Name. */
    val name: String? = null,
    /** Public port on the CDN server. */
    @SerialName("listen_port") val listenPort: Int? = null,
    /** Protocol. */
    val transport: CdnTransport? = null,
    /** Target port at the user's server. */
    @SerialName("target_port") val targetPort: Int? = null,
    /** Record token of the target. */
    val record: String? = null,
    /** Name of the target. */
    val fqdn: String? = null,
)

/** An entry of the CDN access list. */
@Serializable
public data class AccessListEntry(
    /** ID. */
    val id: Long? = null,
    /** IP or network (CIDR). */
    val address: String? = null,
)

/** Open task of the CDN bot. */
@Serializable
public enum class CdnPendingTask(override val value: String) : ApiValue {
    @SerialName("server_assignment") SERVER_ASSIGNMENT("server_assignment"),
    @SerialName("dns") DNS("dns"),
    @SerialName("config_deploy") CONFIG_DEPLOY("config_deploy"),
    @SerialName("certificate") CERTIFICATE("certificate"),
    @SerialName("server_move") SERVER_MOVE("server_move"),
    @SerialName("deletion") DELETION("deletion"),
}

/** Reverse proxy state. */
@Serializable
public enum class CdnProxyState(override val value: String) : ApiValue {
    @SerialName("off") OFF("off"),
    @SerialName("on") ON("on"),
    @SerialName("disabling") DISABLING("disabling"),
}

/** Frontend protocol handling of the reverse proxy. */
@Serializable
public enum class CdnFrontend(override val value: String) : ApiValue {
    /** HTTP only. */
    @SerialName("http") HTTP("http"),
    /** HTTP is redirected to HTTPS. */
    @SerialName("https_redirect") HTTPS_REDIRECT("https_redirect"),
    /** HTTP and HTTPS. */
    @SerialName("http_and_https") HTTP_AND_HTTPS("http_and_https"),
}

/** Protocol used towards the origin server. */
@Serializable
public enum class CdnBackendScheme(override val value: String) : ApiValue {
    @SerialName("http") HTTP("http"),
    @SerialName("https") HTTPS("https"),
}

/** Certificate of the reverse proxy. */
@Serializable
public enum class CdnCertificate(override val value: String) : ApiValue {
    @SerialName("default") DEFAULT("default"),
    @SerialName("letsencrypt") LETSENCRYPT("letsencrypt"),
    @SerialName("issuing") ISSUING("issuing"),
}

/** Access list mode. */
@Serializable
public enum class CdnAccessListMode(override val value: String) : ApiValue {
    /** Only listed entries are allowed. */
    @SerialName("allowlist") ALLOWLIST("allowlist"),
    /** Listed entries are blocked. */
    @SerialName("denylist") DENYLIST("denylist"),
}

/** Portmapper state. */
@Serializable
public enum class CdnPortmapperState(override val value: String) : ApiValue {
    @SerialName("off") OFF("off"),
    @SerialName("on") ON("on"),
    @SerialName("disabling") DISABLING("disabling"),
    @SerialName("suspended_traffic_limit") SUSPENDED_TRAFFIC_LIMIT("suspended_traffic_limit"),
}

/** Transport protocol of a port mapping. */
@Serializable
public enum class CdnTransport(override val value: String) : ApiValue {
    @SerialName("tcp") TCP("tcp"),
    @SerialName("udp") UDP("udp"),
}

/** How a record is routed through the CDN. */
@Serializable
public enum class CdnRecordMode(override val value: String) : ApiValue {
    /** The name points completely to the CDN. */
    @SerialName("full") FULL("full"),
    /** The record stays directly reachable; the CDN serves only the other address family. */
    @SerialName("other_family_only") OTHER_FAMILY_ONLY("other_family_only"),
}

/** Body of `POST /cdn`. */
@Serializable
public data class AddCdnDomainRequest(
    /** Domain ID or name. */
    val domain: String,
)

/** Body of `PATCH /cdn/{token}`; `null` fields stay unchanged. */
@Serializable
public data class UpdateCdnDomainRequest(
    /** Frontend protocol handling. */
    val frontend: CdnFrontend? = null,
    /** Protocol to the origin server. */
    @SerialName("backend_scheme") val backendScheme: CdnBackendScheme? = null,
    /** Port of the origin server (1-65535). */
    @SerialName("backend_port") val backendPort: Int? = null,
    /** WebSocket support, only with active proxy. */
    val websocket: Boolean? = null,
    /** Cache settings. */
    val caching: UpdateCdnCaching? = null,
    /** Access list settings. */
    @SerialName("access_list") val accessList: UpdateCdnAccessList? = null,
)

/** Cache part of [UpdateCdnDomainRequest]. */
@Serializable
public data class UpdateCdnCaching(
    /** Cache active. */
    val enabled: Boolean? = null,
    /** Cache duration, 1 to 600 minutes. */
    @SerialName("time_minutes") val timeMinutes: Int? = null,
    /** Cache size, 1 MB up to the package limit. */
    @SerialName("size_mb") val sizeMb: Int? = null,
)

/** Access list part of [UpdateCdnDomainRequest]. */
@Serializable
public data class UpdateCdnAccessList(
    /** Access list active. */
    val enabled: Boolean? = null,
    /** Allowlist or denylist. */
    val mode: CdnAccessListMode? = null,
)

/** Body of `POST /cdn/{token}/records/{record}/enable`. */
@Serializable
public data class EnableCdnRecordRequest(
    /** Routing mode; the server default applies when `null`. */
    val mode: CdnRecordMode? = null,
)

/** Body of `POST /cdn/{token}/mappings`. */
@Serializable
public data class CreateCdnMappingRequest(
    /** Record ID or token of a record running through the CDN. */
    val record: String,
    /** Port at the user's server (1-65535). */
    @SerialName("target_port") val targetPort: Int,
    /** Protocol. */
    val transport: CdnTransport? = null,
    /** Name. */
    val name: String? = null,
    /** Desired public port (50000-62000); a free one is assigned if omitted. */
    @SerialName("listen_port") val listenPort: Int? = null,
)

/** Body of `POST /cdn/{token}/access-list`. */
@Serializable
public data class AddAccessListEntryRequest(
    /** IP or network in CIDR notation. */
    val address: String,
)

/**
 * Decodes `pending` leniently: values unknown to this SDK version are dropped instead of failing the whole response
 * (`coerceInputValues` only applies to properties, not to list elements).
 */
internal object CdnPendingTaskListSerializer : KSerializer<List<CdnPendingTask>> {
    private val delegate = ListSerializer(String.serializer())
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: List<CdnPendingTask>) {
        delegate.serialize(encoder, value.map { it.value })
    }

    override fun deserialize(decoder: Decoder): List<CdnPendingTask> =
        delegate.deserialize(decoder).mapNotNull { wire -> CdnPendingTask.entries.firstOrNull { it.value == wire } }
}
