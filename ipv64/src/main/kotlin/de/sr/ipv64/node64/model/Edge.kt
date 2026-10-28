package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IsoInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** Transport protocol. */
@Serializable
public enum class NetProtocol(override val value: String) : ApiValue {
    @SerialName("tcp") TCP("tcp"),
    @SerialName("udp") UDP("udp"),
}

/** Answer for unknown names. */
@Serializable
public enum class DefaultPageMode(override val value: String) : ApiValue {
    @SerialName("404") NOT_FOUND("404"),
    @SerialName("close") CLOSE("close"),
    @SerialName("redirect") REDIRECT("redirect"),
    @SerialName("html") HTML("html"),
}

/** Name of an edge listener. */
@Serializable
public enum class EdgeListenerName(override val value: String) : ApiValue {
    @SerialName("http") HTTP("http"),
    @SerialName("https") HTTPS("https"),
    @SerialName("http3") HTTP3("http3"),
}

/** A port claimed by a part of Node64. */
@Serializable
public data class PortClaim(
    /** Owner such as `web`, `edge`, `dns64`, `cdn64`. */
    val owner: String? = null,
    /** Purpose of the port. */
    val purpose: String? = null,
    /** Protocol. */
    val proto: NetProtocol? = null,
    /** Port number. */
    val port: Int? = null,
    /** Bind address; missing means all addresses. */
    val bind: String? = null,
    /** Port is open / switched on. */
    val active: Boolean? = null,
    /** Conflicting claims. */
    val conflicts: List<String> = emptyList(),
)

/** Settings of the shared entry (edge). */
@Serializable
public data class EdgeConfig(
    /** Bind address, empty means all addresses. */
    val bind: String? = null,
    /** HTTP port, 0 = off, default 80. */
    @SerialName("http_port") val httpPort: Int? = null,
    /** HTTPS port, 0 = off, default 443. */
    @SerialName("https_port") val httpsPort: Int? = null,
    /** HTTPS port as seen by visitors (redirects, Alt-Svc), 0 = `https_port`. */
    @SerialName("public_port") val publicPort: Int? = null,
    /** HTTP/3 (QUIC) on `https_port`/UDP. */
    val http3: Boolean? = null,
    /** Answer for unknown names. */
    val default: DefaultPageMode? = null,
    /** Redirect target of the default answer. */
    @SerialName("default_url") val defaultUrl: String? = null,
    /** HTML of the default answer. */
    @SerialName("default_html") val defaultHtml: String? = null,
)

/** Body of `PUT /edge`. Missing fields stay unchanged. */
@Serializable
public data class EdgeConfigRequest(
    /** Bind address, empty means all addresses. */
    val bind: String? = null,
    /** HTTP port, 0 = off, default 80. */
    @SerialName("http_port") val httpPort: Int? = null,
    /** HTTPS port, 0 = off, default 443. */
    @SerialName("https_port") val httpsPort: Int? = null,
    /** HTTPS port as seen by visitors (redirects, Alt-Svc), 0 = `https_port`. */
    @SerialName("public_port") val publicPort: Int? = null,
    /** HTTP/3 (QUIC) on `https_port`/UDP. */
    val http3: Boolean? = null,
    /** Answer for unknown names. */
    val default: DefaultPageMode? = null,
    /** Redirect target of the default answer. */
    @SerialName("default_url") val defaultUrl: String? = null,
    /** HTML of the default answer. */
    @SerialName("default_html") val defaultHtml: String? = null,
)

/** A listener of the shared entry. */
@Serializable
public data class EdgeListener(
    /** Listener name. */
    val name: EdgeListenerName? = null,
    /** Protocol. */
    val proto: String? = null,
    /** Port. */
    val port: Int? = null,
    /** A module needs this port. */
    val needed: Boolean? = null,
    /** Listener is running. */
    val running: Boolean? = null,
    /** Listen address. */
    val addr: String? = null,
    /** Error opening the port. */
    val error: String? = null,
)

/** A name registered at the shared entry. */
@Serializable
public data class EdgeName(
    /** Name. */
    val name: String? = null,
    /** Path. */
    val path: String? = null,
    /** Owning part. */
    val owner: String? = null,
)

/** State of the shared entry: settings, open ports and registered names. */
@Serializable
public data class EdgeStatus(
    /** Settings. */
    val config: EdgeConfig? = null,
    /** Listeners. */
    val listeners: List<EdgeListener> = emptyList(),
    /** Registered names. */
    val names: List<EdgeName> = emptyList(),
)

/** Response of `GET /ports`. */
@Serializable
public data class PortsOverview(
    /** Claimed ports of all parts. */
    val ports: List<PortClaim> = emptyList(),
    /** State of the shared entry. */
    val edge: EdgeStatus? = null,
)

/** Response of `GET /ports/probe`. */
@Serializable
public data class PortProbeResult(
    /** Port is free. */
    val free: Boolean? = null,
    /** Who uses the port. */
    @SerialName("used_by") val usedBy: String? = null,
    /** Purpose of the use. */
    val purpose: String? = null,
    /** Error message. */
    val error: String? = null,
)

/** Node64 UI under its own name via the shared entry. */
@Serializable
public data class EdgeUiConfig(
    /** Name of the UI. */
    val name: String? = null,
    /** UI is publicly reachable. */
    @SerialName("public") val isPublic: Boolean? = null,
    /** Certificate. */
    val cert: String? = null,
    /** Certificate comes from the environment. */
    @SerialName("cert_from_env") val certFromEnv: Boolean? = null,
)

/** Body of `PUT /edge/ui`. */
@Serializable
public data class UpdateEdgeUiRequest(
    /** Name of the UI. */
    val name: String? = null,
    /** Make the UI publicly reachable. */
    @SerialName("public") val isPublic: Boolean? = null,
    /** Certificate. */
    val cert: String? = null,
)
