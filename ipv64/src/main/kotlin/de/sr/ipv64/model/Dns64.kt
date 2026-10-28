package de.sr.ipv64.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** Blocklist of a DNS64 client. */
@Serializable
public enum class Dns64Blocklist(override val value: String) : ApiValue {
    @SerialName("ads") ADS("ads"),
    /** Not available in the free plan. */
    @SerialName("adult") ADULT("adult"),
    /** Not available in the free plan. */
    @SerialName("threats") THREATS("threats"),
}

/** Action of a DNS64 rule. */
@Serializable
public enum class Dns64RuleAction(override val value: String) : ApiValue {
    @SerialName("allow") ALLOW("allow"),
    @SerialName("block") BLOCK("block"),
}

/** Kind of a DNS64 source address. */
@Serializable
public enum class Dns64SourceKind(override val value: String) : ApiValue {
    @SerialName("ip") IP("ip"),
    @SerialName("record") RECORD("record"),
}

/** State of a DNS64 client or rule. */
@Serializable
public enum class Dns64State(override val value: String) : ApiValue {
    @SerialName("ready") READY("ready"),
    @SerialName("applying") APPLYING("applying"),
}

/** DNS64 client. */
@Serializable
public data class Dns64Client(
    /** Client ID. */
    val id: String? = null,
    /** Name. */
    val name: String? = null,
    /** Short identifier used in the resolver addresses. */
    @SerialName("client_id") val clientId: String? = null,
    /** State. */
    val state: Dns64State? = null,
    /** Filter active. */
    @SerialName("blocking_enabled") val blockingEnabled: Boolean? = null,
    /** Active blocklists (values unknown to this SDK version are dropped). */
    @Serializable(with = Dns64BlocklistListSerializer::class) val blocklists: List<Dns64Blocklist> = emptyList(),
    /** Resolver servers. */
    val servers: List<Dns64Server> = emptyList(),
    /** Total queries. */
    @SerialName("queries_total") val queriesTotal: Long? = null,
    /** Blocked queries. */
    @SerialName("queries_blocked") val queriesBlocked: Long? = null,
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Own allow/block rules (detail view only). */
    val rules: List<Dns64Rule> = emptyList(),
    /** Source addresses for the shared resolver IPs (detail view only). */
    val sources: List<Dns64Source> = emptyList(),
)

/** Resolver server of a [Dns64Client]. */
@Serializable
public data class Dns64Server(
    /** Location. */
    val location: String? = null,
    /** Hostname. */
    val hostname: String? = null,
    /** DNS over HTTPS. */
    val doh: String? = null,
    /** DNS over TLS. */
    val dot: String? = null,
    /** DNS over QUIC. */
    val doq: String? = null,
    /** Personal IPv6 of the resolver. */
    val ipv6: String? = null,
    /** Shared IPv4 (assignment via source addresses). */
    @SerialName("ipv4_shared") val ipv4Shared: String? = null,
    /** Shared IPv6. */
    @SerialName("ipv6_shared") val ipv6Shared: String? = null,
    /** DNS stamps (detail view only). */
    val stamps: Dns64Stamps? = null,
)

/** DNS stamps of a [Dns64Server]. */
@Serializable
public data class Dns64Stamps(
    val doh: String? = null,
    val dot: String? = null,
    val doq: String? = null,
)

/** Allow/block rule of a [Dns64Client]. */
@Serializable
public data class Dns64Rule(
    /** ID. */
    val id: Long? = null,
    /** Domain. */
    val domain: String? = null,
    /** Action. */
    val action: Dns64RuleAction? = null,
    /** State. */
    val state: Dns64State? = null,
)

/** Source address of a [Dns64Client]. */
@Serializable
public data class Dns64Source(
    /** Numeric ID for kind `ip`, record token for kind `record`. */
    val id: String? = null,
    /** Kind. */
    val kind: Dns64SourceKind? = null,
    /** Address. */
    val address: String? = null,
    /** Only for kind `record`. */
    val fqdn: String? = null,
)

/** Response of deleting a DNS64 client (string id, unlike [DeletionState]). */
@Serializable
public data class Dns64ClientDeletion(
    /** Client ID. */
    val id: String? = null,
    /** State (normally `deleting`). */
    val state: String? = null,
)

/**
 * Decodes `blocklists` leniently: values unknown to this SDK version are dropped instead of failing the whole
 * response (`coerceInputValues` only applies to properties, not to list elements).
 */
internal object Dns64BlocklistListSerializer : KSerializer<List<Dns64Blocklist>> {
    private val delegate = ListSerializer(String.serializer())
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: List<Dns64Blocklist>) {
        delegate.serialize(encoder, value.map { it.value })
    }

    override fun deserialize(decoder: Decoder): List<Dns64Blocklist> =
        delegate.deserialize(decoder).mapNotNull { wire -> Dns64Blocklist.entries.firstOrNull { it.value == wire } }
}
