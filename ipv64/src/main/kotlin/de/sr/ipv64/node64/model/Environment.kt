package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IsoInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Origin of an environment node. */
@Serializable
public enum class EnvironmentOrigin(override val value: String) : ApiValue {
    @SerialName("self") SELF("self"),
    @SerialName("cluster") CLUSTER("cluster"),
    @SerialName("added") ADDED("added"),
}

/** A node of the environment. */
@Serializable
public data class EnvironmentNode(
    /** ID. */
    val id: String? = null,
    /** Name. */
    val name: String? = null,
    /** Address. */
    val url: String? = null,
    /** Origin. */
    val origin: EnvironmentOrigin? = null,
    /** Reachable. */
    val online: Boolean? = null,
    /** Version. */
    val version: String? = null,
    /** Newer version available (added nodes with a key only). */
    val update: String? = null,
    /** Suspended. */
    val suspended: Boolean? = null,
    /** "Open" signs in with the own user (else login page). */
    val login: Boolean? = null,
    /** Address for the browser. */
    @SerialName("browser_url") val browserUrl: String? = null,
    /** Description. */
    val description: String? = null,
    /** An access key is stored. */
    @SerialName("has_key") val hasKey: Boolean? = null,
    /** Why the access key is not valid (e.g. revoked, user blocked). */
    @SerialName("key_error") val keyError: String? = null,
)

/** Response of `GET /environment`. */
@Serializable
public data class Environment(
    /** This node, cluster members and added nodes with status. */
    val nodes: List<EnvironmentNode> = emptyList(),
)

/** Body of `POST /environment/nodes` and `PUT /environment/nodes/{id}`. [key] is redacted in [toString]. */
@Serializable
public data class EnvironmentNodeInput(
    /** Address of the node (`https://host:port`; https is added without a scheme, no path). */
    val url: String,
    /** Access key of the other node (`n64env1.…`, secret); empty = link and status only, when changing: keep the previous one. */
    val key: String? = null,
    /** When changing: remove the key. */
    @SerialName("remove_key") val removeKey: Boolean? = null,
    /** Empty = name the node reports. */
    val name: String? = null,
    /** Description. */
    val description: String? = null,
    /** Own address for the browser (e.g. via PROXY64). */
    @SerialName("browser_url") val browserUrl: String? = null,
) {
    override fun toString(): String =
        "EnvironmentNodeInput(url=$url, key=<redacted>, removeKey=$removeKey, name=$name, description=$description, browserUrl=$browserUrl)"
}

/** An added node. */
@Serializable
public data class EnvironmentAddedNode(
    /** ID. */
    val id: String? = null,
    /** Name. */
    val name: String? = null,
    /** Address. */
    val url: String? = null,
    /** Address for the browser. */
    @SerialName("browser_url") val browserUrl: String? = null,
    /** Description. */
    val description: String? = null,
    /** ID of the node from the access key. */
    @SerialName("node_id") val nodeId: String? = null,
    /** An access key is stored. */
    @SerialName("has_key") val hasKey: Boolean? = null,
    /** User who entered the key (only that user is signed in via it). */
    @SerialName("owner_uid") val ownerUid: String? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Last update. */
    @SerialName("updated_at") val updatedAt: IsoInstant? = null,
)

/** An access of this node for other nodes (without key). */
@Serializable
public data class EnvironmentAccess(
    /** ID. */
    val id: Long? = null,
    /** Name. */
    val name: String? = null,
    /** User UID. */
    @SerialName("user_uid") val userUid: String? = null,
    /** User name; empty = user deleted (access has no effect). */
    val username: String? = null,
    /** Created by. */
    @SerialName("created_by") val createdBy: String? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Last use. */
    @SerialName("last_used_at") val lastUsedAt: IsoInstant? = null,
    /** Node of the last use. */
    @SerialName("last_from_node") val lastFromNode: String? = null,
    /** Name of the node of the last use. */
    @SerialName("last_from_name") val lastFromName: String? = null,
)

/** Body of `POST /environment/access`. */
@Serializable
public data class EnvironmentAccessInput(
    /** Name. */
    val name: String,
    /** User on this node; empty = yourself. */
    @SerialName("user_uid") val userUid: String? = null,
)

/** Response of `POST /environment/access`. [key] is only visible now and redacted in [toString]. */
@Serializable
public data class EnvironmentAccessCreated(
    /** The created access. */
    val access: EnvironmentAccess? = null,
    /** Access key `n64env1.…` (secret, shown once). */
    val key: String? = null,
) {
    override fun toString(): String = "EnvironmentAccessCreated(access=$access, key=<redacted>)"
}
