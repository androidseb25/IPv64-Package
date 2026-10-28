package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IsoInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** State of a group member. */
@Serializable
public enum class ClusterMemberState(override val value: String) : ApiValue {
    @SerialName("active") ACTIVE("active"),
    @SerialName("suspended") SUSPENDED("suspended"),
}

/** Response of `GET /group` and `POST /group/join`. [code] is redacted in [toString]. */
@Serializable
public data class ClusterGroup(
    /** Account ID. */
    @SerialName("account_id") val accountId: Long? = null,
    /** Node is linked with ipv64. */
    val linked: Boolean? = null,
    /** Node that forwards requests to ipv64. */
    val forwarder: String? = null,
    /** This node is the forwarder. */
    @SerialName("forwarder_self") val forwarderSelf: Boolean? = null,
    /** Open pairing code. */
    val code: String? = null,
    /** Expiry of the pairing code. */
    @SerialName("code_expires") val codeExpires: IsoInstant? = null,
    /** Members with their state. */
    val members: List<ClusterMember> = emptyList(),
    /** Sync switches per module. */
    @SerialName("sync_modules") val syncModules: List<ClusterSyncModule> = emptyList(),
    /** HTTPS port. */
    @SerialName("https_port") val httpsPort: Int? = null,
) {
    override fun toString(): String =
        "ClusterGroup(accountId=$accountId, linked=$linked, forwarder=$forwarder, forwarderSelf=$forwarderSelf, " +
            "code=<redacted>, codeExpires=$codeExpires, members=$members, syncModules=$syncModules, httpsPort=$httpsPort)"
}

/** Member of the group. */
@Serializable
public data class ClusterMember(
    /** Node ID. */
    @SerialName("node_id") val nodeId: String? = null,
    /** Name. */
    val name: String? = null,
    /** Address. */
    val url: String? = null,
    /** Member is online. */
    val online: Boolean? = null,
    /** Member is linked with ipv64. */
    val linked: Boolean? = null,
    /** Version. */
    val version: String? = null,
    /** Last contact. */
    @SerialName("last_seen") val lastSeen: IsoInstant? = null,
    /** Last error. */
    @SerialName("last_error") val lastError: String? = null,
    /** Added at. */
    @SerialName("added_at") val addedAt: IsoInstant? = null,
    /** State. */
    val state: ClusterMemberState? = null,
    /** Reason for suspension. */
    val reason: String? = null,
    /** Synced areas. */
    val sync: List<String> = emptyList(),
)

/** Sync switch of a module. */
@Serializable
public data class ClusterSyncModule(
    /** Module ID. */
    val id: String? = null,
    /** Translation key of the title. */
    @SerialName("title_key") val titleKey: String? = null,
    /** Module is available. */
    val available: Boolean? = null,
    /** Sync is switched on. */
    val enabled: Boolean? = null,
    /** Areas of the module. */
    val items: List<ClusterSyncItem> = emptyList(),
)

/** Sync switch of an area of a module. */
@Serializable
public data class ClusterSyncItem(
    /** Area ID. */
    val id: String? = null,
    /** Translation key of the title. */
    @SerialName("title_key") val titleKey: String? = null,
    /** Sync is switched on. */
    val enabled: Boolean? = null,
)

/** Response of `POST /group/code`. [code] is redacted in [toString]. */
@Serializable
public data class ClusterGroupCode(
    /** Pairing code (valid 10 minutes, once). */
    val code: String? = null,
    /** Expiry. */
    val expires: IsoInstant? = null,
) {
    override fun toString(): String = "ClusterGroupCode(code=<redacted>, expires=$expires)"
}

/** Body of `POST /group/join`. [code] is redacted in [toString]. */
@Serializable
public data class JoinGroupRequest(
    /** Address of a node in the group. */
    val addr: String,
    /** Pairing code of that node. */
    val code: String,
    /** Address under which the other nodes reach this node. */
    @SerialName("self_url") val selfUrl: String? = null,
) {
    override fun toString(): String = "JoinGroupRequest(addr=$addr, code=<redacted>, selfUrl=$selfUrl)"
}

/** Body of `PUT /group/members/{id}`. */
@Serializable
public data class UpdateGroupMemberRequest(
    /** New address of the member. */
    val url: String,
)

/** Body of `PUT /group/sync`. */
@Serializable
public data class SetGroupSyncRequest(
    /** Module ID. */
    val module: String,
    /** Area of the module; omit to switch the whole module. */
    val item: String? = null,
    /** Switch on or off. */
    val enabled: Boolean,
)

/** Response of `PUT /group/sync`. */
@Serializable
public data class ClusterSyncModules(
    /** Sync switches per module. */
    @SerialName("sync_modules") val syncModules: List<ClusterSyncModule> = emptyList(),
)
