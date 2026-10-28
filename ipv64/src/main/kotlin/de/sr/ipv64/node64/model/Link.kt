package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IsoInstant
import de.sr.ipv64.node64.internal.LenientInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.time.Instant

/** Remote management right of ipv64 on this node. */
@Serializable
public enum class RemoteMgmt(override val value: String) : ApiValue {
    @SerialName("full") FULL("full"),
    @SerialName("read") READ("read"),
    @SerialName("off") OFF("off"),
}

/** State of the healthcheck sync with ipv64 (link). [pendingCode] grants linking and is redacted in [toString]. */
@Serializable
public data class Link(
    /** Node is linked with an account. */
    val linked: Boolean? = null,
    /** Connection to the hub is established. */
    val connected: Boolean? = null,
    /** Account ID. */
    @SerialName("account_id") val accountId: String? = null,
    /** Plan of the account. */
    val plan: String? = null,
    /** Node name. */
    @SerialName("node_name") val nodeName: String? = null,
    /** Location. */
    val location: String? = null,
    /** Remote management right. */
    @SerialName("remote_mgmt") val remoteMgmt: RemoteMgmt? = null,
    /** Hub address. */
    val hub: String? = null,
    /** Last sync; ISO 8601, `null` while unset. */
    @Serializable(with = LenientInstantSerializer::class) @SerialName("last_sync_at") val lastSyncAt: Instant? = null,
    /** Connected since; ISO 8601, `null` while unset. */
    @Serializable(with = LenientInstantSerializer::class) @SerialName("connected_at") val connectedAt: Instant? = null,
    /** Linked since; ISO 8601, `null` while unset. */
    @Serializable(with = LenientInstantSerializer::class) @SerialName("linked_at") val linkedAt: Instant? = null,
    /** Pending changes to send. */
    val outbox: Int? = null,
    /** Number of open conflicts. */
    val conflicts: Int? = null,
    /** Last error. */
    @SerialName("last_error") val lastError: String? = null,
    /** Pending link code. */
    @SerialName("pending_code") val pendingCode: String? = null,
    /** A link request is running. */
    val requesting: Boolean? = null,
    /** Limit of online checks. */
    @SerialName("online_limit") val onlineLimit: Int? = null,
    /** Checks executed today. */
    @SerialName("checks_today") val checksToday: Int? = null,
    /** Check limit per day. */
    @SerialName("checks_limit") val checksLimit: Int? = null,
    /** Locations per check. */
    @SerialName("locations_per_check") val locationsPerCheck: Int? = null,
    /** Notifications are sent locally. */
    @SerialName("local_notifications") val localNotifications: Boolean? = null,
) {
    override fun toString(): String =
        "Link(linked=$linked, connected=$connected, accountId=$accountId, plan=$plan, nodeName=$nodeName, " +
            "location=$location, remoteMgmt=$remoteMgmt, hub=$hub, lastSyncAt=$lastSyncAt, connectedAt=$connectedAt, " +
            "linkedAt=$linkedAt, outbox=$outbox, conflicts=$conflicts, lastError=$lastError, " +
            "pendingCode=${if (pendingCode == null) "null" else "<redacted>"}, requesting=$requesting, " +
            "onlineLimit=$onlineLimit, checksToday=$checksToday, checksLimit=$checksLimit, " +
            "locationsPerCheck=$locationsPerCheck, localNotifications=$localNotifications)"
}

/** Body of `POST /link/apikey`. */
@Serializable
public data class LinkWithApiKeyRequest(
    /** Location of the node. */
    val location: String? = null,
    /** Remote management right. */
    @SerialName("remote_mgmt") val remoteMgmt: RemoteMgmt? = null,
)

/** Body of `POST /link/unlink`. */
@Serializable
public data class UnlinkRequest(
    /** Keep the healthchecks locally. */
    @SerialName("keep_checks") val keepChecks: Boolean? = null,
)

/** Open sync conflict (local and server version). [local] and [server] may contain secrets (e.g. channel configs) and are redacted in [toString]. */
@Serializable
public data class LinkConflict(
    /** Conflict ID. */
    val id: Long? = null,
    /** Entity type. */
    val entity: String? = null,
    /** Entity ID. */
    @SerialName("entity_id") val entityId: String? = null,
    /** Name of the entity. */
    val name: String? = null,
    /** Local operation. */
    @SerialName("local_op") val localOp: String? = null,
    /** Local version. */
    val local: JsonElement? = null,
    /** Server version. */
    val server: JsonElement? = null,
    /** Revision of the server version. */
    @SerialName("server_revision") val serverRevision: Long? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
) {
    override fun toString(): String =
        "LinkConflict(id=$id, entity=$entity, entityId=$entityId, name=$name, localOp=$localOp, local=<redacted>, " +
            "server=<redacted>, serverRevision=$serverRevision, createdAt=$createdAt)"
}

/** Which version wins when resolving a conflict. */
@Serializable
public enum class ConflictResolution(override val value: String) : ApiValue {
    @SerialName("server") SERVER("server"),
    @SerialName("local") LOCAL("local"),
}

/** Body of `POST /link/conflicts/{id}`. */
@Serializable
public data class ResolveConflictRequest(
    /** Version to keep. */
    val keep: ConflictResolution,
)

/** Sync settings (request and response of `PUT /link/settings`). */
@Serializable
public data class LinkSettings(
    /** Send notifications from the node itself. */
    @SerialName("local_notifications") val localNotifications: Boolean? = null,
)

/** State of the central ipv64 API key. */
@Serializable
public data class Ipv64KeyStatus(
    /** A key is stored. */
    @SerialName("key_stored") val keyStored: Boolean? = null,
    /** Account name. */
    val account: String? = null,
    /** Account ID. */
    @SerialName("account_id") val accountId: Long? = null,
    /** Plan. */
    val plan: String? = null,
    /** Name of the key. */
    @SerialName("key_name") val keyName: String? = null,
    /** Last check. */
    @SerialName("checked_at") val checkedAt: IsoInstant? = null,
    /** Expiry of the key. */
    @SerialName("expires_at") val expiresAt: IsoInstant? = null,
    /** Days until expiry. */
    @SerialName("days_left") val daysLeft: Int? = null,
)

/** Body of `PUT /ipv64/key`. [key] is redacted in [toString]. */
@Serializable
public data class SetIpv64KeyRequest(
    /** ipv64 API key. */
    val key: String,
) {
    override fun toString(): String = "SetIpv64KeyRequest(key=<redacted>)"
}
