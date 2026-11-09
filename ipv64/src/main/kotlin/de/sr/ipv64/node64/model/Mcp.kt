package de.sr.ipv64.node64.model

import de.sr.ipv64.model.IsoInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Area of the MCP interface. */
@Serializable
public data class McpArea(
    val name: String? = null,
    val available: Boolean? = null,
)

/** MCP key (without the secret). */
@Serializable
public data class McpKey(
    /** Key ID. */
    val id: Long? = null,
    /** Name. */
    val name: String? = null,
    /** Short hint to recognize the key. */
    val hint: String? = null,
    /** Areas the key may read. */
    val areas: List<String> = emptyList(),
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Last use. */
    @SerialName("last_used_at") val lastUsedAt: IsoInstant? = null,
    /** IP of the last use. */
    @SerialName("last_ip") val lastIp: String? = null,
)

/** MCP switch, areas and own keys. `GET /mcp` */
@Serializable
public data class Mcp(
    val enabled: Boolean? = null,
    val path: String? = null,
    @SerialName("rate_per_minute") val ratePerMinute: Int? = null,
    val areas: List<McpArea> = emptyList(),
    val keys: List<McpKey> = emptyList(),
)

/** Created MCP key; [key] (`mcp64_...`) is only returned once and redacted in [toString]. */
@Serializable
public data class CreatedMcpKey(
    val id: Long? = null,
    val name: String? = null,
    val hint: String? = null,
    val areas: List<String> = emptyList(),
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    @SerialName("last_used_at") val lastUsedAt: IsoInstant? = null,
    @SerialName("last_ip") val lastIp: String? = null,
    /** The secret key. */
    val key: String? = null,
) {
    override fun toString(): String =
        "CreatedMcpKey(id=$id, name=$name, hint=$hint, areas=$areas, createdAt=$createdAt, " +
            "lastUsedAt=$lastUsedAt, lastIp=$lastIp, key=<redacted>)"
}

/** Body of `PUT /mcp`. */
@Serializable
public data class UpdateMcpRequest(val enabled: Boolean)

/** Body of `POST /mcp/keys`. */
@Serializable
public data class CreateMcpKeyRequest(
    /** Name, 1-100 characters. */
    val name: String,
    /** Areas from `GET /mcp`. */
    val areas: List<String>,
)
