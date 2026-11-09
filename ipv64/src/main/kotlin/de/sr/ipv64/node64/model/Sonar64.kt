package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.node64.internal.LenientInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

/** Sonar64 measurement budget. */
@Serializable
public enum class Sonar64Budget(override val value: String) : ApiValue {
    @SerialName("sparsam") SPARSAM("sparsam"),
    @SerialName("normal") NORMAL("normal"),
    @SerialName("grosszuegig") GROSSZUEGIG("grosszuegig"),
}

/** Connection type of the node. */
@Serializable
public enum class Sonar64Access(override val value: String) : ApiValue {
    @SerialName("fiber") FIBER("fiber"),
    @SerialName("dsl") DSL("dsl"),
    @SerialName("cable") CABLE("cable"),
    @SerialName("mobile") MOBILE("mobile"),
    @SerialName("satellite") SATELLITE("satellite"),
    @SerialName("datacenter") DATACENTER("datacenter"),
    @SerialName("other") OTHER("other"),
}

/** Device type of the node. */
@Serializable
public enum class Sonar64Device(override val value: String) : ApiValue {
    @SerialName("node64") NODE64("node64"),
    @SerialName("pi") PI("pi"),
    @SerialName("mikrotik") MIKROTIK("mikrotik"),
    @SerialName("linux") LINUX("linux"),
    @SerialName("other") OTHER("other"),
}

/** State of the local Sonar64 node (core). */
@Serializable
public data class Sonar64NodeState(
    @SerialName("node_id") val nodeId: String? = null,
    val registered: Boolean? = null,
    val mode: String? = null,
    val measured: Boolean? = null,
    val budget: String? = null,
    val responder: Boolean? = null,
    @Serializable(with = LenientInstantSerializer::class) @SerialName("plan_issued") val planIssued: Instant? = null,
    @Serializable(with = LenientInstantSerializer::class) @SerialName("plan_expires") val planExpires: Instant? = null,
    @SerialName("anchors_only") val anchorsOnly: Boolean? = null,
    /** Pause reason: `uplink`, `quiet`, `no_plan` or `budget`. */
    val paused: String? = null,
    @SerialName("sent_today") val sentToday: Long? = null,
    @Serializable(with = LenientInstantSerializer::class) @SerialName("last_report") val lastReport: Instant? = null,
    val buffered: Long? = null,
    @SerialName("last_error") val lastError: String? = null,
)

/** Trust level of the node. */
@Serializable
public data class Sonar64Trust(
    /** `new`, `stable`, `trusted`, `pillar`, `anchor` or `quarantine`. */
    val level: String? = null,
    @SerialName("class") val trustClass: String? = null,
    val next: String? = null,
    val pct: Long? = null,
    val days: Long? = null,
    @SerialName("need_days") val needDays: Long? = null,
    val uptime: Double? = null,
    @SerialName("need_uptime") val needUptime: Double? = null,
    @SerialName("need_measured") val needMeasured: Boolean? = null,
)

/** Response of `GET /sonar64/status`. */
@Serializable
public data class Sonar64Status(
    val version: String? = null,
    val running: Boolean? = null,
    @Serializable(with = LenientInstantSerializer::class) @SerialName("started_at") val startedAt: Instant? = null,
    /** Central ipv64 API key is set. */
    @SerialName("key_set") val keySet: Boolean? = null,
    val participate: Boolean? = null,
    val node: Sonar64NodeState? = null,
    val error: String? = null,
    /** UDP 6464 is in use: outbound only. */
    @SerialName("port_error") val portError: String? = null,
    /** Own node at ipv64 (`GET /sonar/me`), free-form. */
    val me: JsonObject? = null,
    @SerialName("me_error") val meError: String? = null,
    val trust: Sonar64Trust? = null,
    /** Public node page on ipv64.net. */
    @SerialName("public_url") val publicUrl: String? = null,
)

/** Response of `GET /sonar64/settings` and `PUT /sonar64/settings`. */
@Serializable
public data class Sonar64Settings(
    val participate: Boolean? = null,
    /** Also be measured (UDP 6464). */
    val measured: Boolean? = null,
    /** See [Sonar64Budget]. */
    val budget: String? = null,
    /** Quiet hours, e.g. `23-6`; empty = none. */
    val quiet: String? = null,
    val name: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    /** See [Sonar64Access]; may be empty. */
    val access: String? = null,
    /** See [Sonar64Device]; may be empty. */
    val device: String? = null,
)

/** Body of `PUT /sonar64/settings`; missing fields stay unchanged. */
@Serializable
public data class Sonar64SettingsRequest(
    val participate: Boolean? = null,
    val measured: Boolean? = null,
    val budget: Sonar64Budget? = null,
    val quiet: String? = null,
    /** 3-32 characters of `A-Za-z0-9 ._-`. */
    val name: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    /** Deletes the location. */
    @SerialName("clear_location") val clearLocation: Boolean? = null,
    val access: Sonar64Access? = null,
    val device: Sonar64Device? = null,
)

/** Own measurement from [Sonar64Recent]. */
@Serializable
public data class Sonar64Result(
    @Serializable(with = LenientInstantSerializer::class) val at: Instant? = null,
    /** Node ID of the target. */
    val target: String? = null,
    /** `icmp`, `udp` or `trace`. */
    val method: String? = null,
    val fam: Int? = null,
    val sent: Long? = null,
    val recv: Long? = null,
    val min: Double? = null,
    val avg: Double? = null,
    val max: Double? = null,
    /** Loss in percent. */
    val loss: Double? = null,
    val err: String? = null,
)

/** Probe answered live on UDP 6464 from [Sonar64Recent]. */
@Serializable
public data class Sonar64InboundProbe(
    /** Short ID of the measuring node (8 hex). */
    val from: String? = null,
    val name: String? = null,
    val fam: Int? = null,
    val count: Long? = null,
    @Serializable(with = LenientInstantSerializer::class) val at: Instant? = null,
)

/** Response of `GET /sonar64/recent`. */
@Serializable
public data class Sonar64Recent(
    val results: List<Sonar64Result> = emptyList(),
    val inbound: List<Sonar64InboundProbe> = emptyList(),
    val responder: Boolean? = null,
)

/** Row of [Sonar64Inbound]: who measured this node. */
@Serializable
public data class Sonar64InboundRow(
    val from: String? = null,
    val name: String? = null,
    val asname: String? = null,
    val place: String? = null,
    /** `new`, `stable`, `trusted`, `pillar`, `anchor` or `quarantine`. */
    val trust: String? = null,
    val fam: Int? = null,
    val method: String? = null,
    val avg: Double? = null,
    /** Share 0..1. */
    val loss: Double? = null,
    @Serializable(with = LenientInstantSerializer::class) val at: Instant? = null,
)

/** Response of `GET /sonar64/inbound`. */
@Serializable
public data class Sonar64Inbound(
    /** False while the node is not registered. */
    val ready: Boolean? = null,
    val rows: List<Sonar64InboundRow> = emptyList(),
)
