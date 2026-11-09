package de.sr.ipv64.node64.model

import de.sr.ipv64.model.ApiValue
import de.sr.ipv64.model.IsoInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Kind of a container in [DockerContainer]. */
@Serializable
public enum class DockerContainerKind(override val value: String) : ApiValue {
    @SerialName("running") RUNNING("running"),
    @SerialName("stopped") STOPPED("stopped"),
    @SerialName("paused") PAUSED("paused"),
    @SerialName("restarting") RESTARTING("restarting"),
    /** Check without container. */
    @SerialName("missing") MISSING("missing"),
    /** Other node or other Docker host. */
    @SerialName("remote") REMOTE("remote"),
    /** Socket not reachable. */
    @SerialName("unknown") UNKNOWN("unknown"),
}

/** Monitoring state of a container. */
@Serializable
public enum class DockerMonitorState(override val value: String) : ApiValue {
    @SerialName("running") RUNNING("running"),
    @SerialName("stopped") STOPPED("stopped"),
    @SerialName("paused") PAUSED("paused"),
    @SerialName("error") ERROR("error"),
    @SerialName("unknown") UNKNOWN("unknown"),
}

/** Response of `GET /healthchecks/docker/containers`. */
@Serializable
public data class DockerContainers(
    /** Docker host address. */
    val host: String? = null,
    /** Socket reachable. */
    val socket: Boolean? = null,
    /** Error message. */
    val error: String? = null,
    /** Containers and Docker checks. */
    val containers: List<DockerContainer> = emptyList(),
    /** ID of the Docker system. */
    @SerialName("system_id") val systemId: Long? = null,
    /** Monitoring is running. */
    val monitored: Boolean? = null,
)

/** A container of the Docker host with monitoring values and its Docker check. */
@Serializable
public data class DockerContainer(
    /** Name. */
    val name: String? = null,
    /** Kind. */
    val kind: DockerContainerKind? = null,
    /** Healthcheck of the container. */
    val health: DockerHealth? = null,
    /** Container ID. */
    val id: String? = null,
    /** Image. */
    val image: String? = null,
    /** State text of Docker. */
    val state: String? = null,
    /** Creation time. */
    val created: IsoInstant? = null,
    /** Ports. */
    val ports: List<String> = emptyList(),
    /** Docker check; null without check. */
    val check: DockerContainerCheck? = null,
    /** Key `docker/<name>`. */
    val key: String? = null,
    /** Monitoring state. */
    val monitor: DockerMonitorState? = null,
    /** Cause in state error. */
    val detail: DockerErrorDetail? = null,
    /** In a bad state. */
    val bad: Boolean? = null,
    /** Monitored as healthcheck. */
    @SerialName("as_check") val asCheck: Boolean? = null,
    /** Excluded from monitoring. */
    val excluded: Boolean? = null,
    /** Notifications muted. */
    val muted: Boolean? = null,
    /** CPU share. */
    val cpu: Double? = null,
    /** CPU count. */
    val cpus: Double? = null,
    /** Memory in bytes. */
    val mem: Long? = null,
    /** Memory limit in bytes. */
    @SerialName("max_mem") val maxMem: Long? = null,
    /** Network in, bytes/s. */
    @SerialName("net_in") val netIn: Double? = null,
    /** Network out, bytes/s. */
    @SerialName("net_out") val netOut: Double? = null,
    /** Disk read, bytes/s. */
    @SerialName("disk_read") val diskRead: Double? = null,
    /** Disk write, bytes/s. */
    @SerialName("disk_write") val diskWrite: Double? = null,
    /** Uptime in seconds. */
    val uptime: Long? = null,
    /** Restarts by Docker. */
    @SerialName("restart_count") val restartCount: Int? = null,
    /** Exit code. */
    @SerialName("exit_code") val exitCode: Int? = null,
)

/** The Docker check of a [DockerContainer]. */
@Serializable
public data class DockerContainerCheck(
    /** Check token. */
    val token: String? = null,
    /** Name. */
    val name: String? = null,
    /** State. */
    val state: String? = null,
    /** Message. */
    val message: String? = null,
)

/** Body of `POST /healthchecks/docker/systems`. Secrets are redacted in [toString]. Without [addresses] the local socket is used. */
@Serializable
public data class DockerSystemRequest(
    /** Name. */
    val name: String? = null,
    /** Addresses (default `NODE64_DOCKER_HOST` or `unix:///var/run/docker.sock`). */
    val addresses: List<String>? = null,
    /** Poll interval in seconds. */
    val interval: Int? = null,
    /** Monitoring enabled. */
    val enabled: Boolean? = null,
    /** API token ID. */
    @SerialName("token_id") val tokenId: String? = null,
    /** Secret: JSON `{key, cert, ca}` or a private SSH key (PEM). Redacted in [toString]. */
    val secret: String? = null,
    /** Pinned fingerprint (64 hex), https/ssh only. */
    @SerialName("pin_sha256") val pinSha256: String? = null,
    /** Private SSH or TLS client key (PEM); empty = unchanged. Redacted in [toString]. */
    @SerialName("private_key") val privateKey: String? = null,
    /** TLS client certificate (PEM); `""` removes certificate and key. */
    val cert: String? = null,
    /** CA of the Docker daemon (PEM); `""` removes it. */
    val ca: String? = null,
    /** SSH: generate and store a new ed25519 key (public part in `credentials.public_key`). */
    @SerialName("generate_key") val generateKey: Boolean? = null,
) {
    override fun toString(): String =
        "DockerSystemRequest(name=$name, addresses=$addresses, interval=$interval, enabled=$enabled, tokenId=$tokenId, " +
            "secret=<redacted>, pinSha256=$pinSha256, privateKey=<redacted>, cert=$cert, ca=$ca, generateKey=$generateKey)"
}

/** Host info of a [DockerProbe]. */
@Serializable
public data class DockerProbeInfo(
    /** Name. */
    val name: String? = null,
    /** Operating system. */
    val os: String? = null,
    /** Version. */
    val version: String? = null,
    /** CPU count. */
    val ncpu: Int? = null,
    /** Memory in bytes. */
    @SerialName("mem_total") val memTotal: Long? = null,
)

/** Response of `POST /healthchecks/docker/probe`. */
@Serializable
public data class DockerProbe(
    /** Connection succeeded. */
    val ok: Boolean? = null,
    /** Error message. */
    val error: String? = null,
    /** Seen host key (SSH) or certificate (TLS), 64 hex; usable as `pin_sha256`. */
    val fingerprint: String? = null,
    /** Fingerprint for comparing. */
    @SerialName("fingerprint_display") val fingerprintDisplay: String? = null,
    /** TLS: certificate confirmed by the CA. */
    val trusted: Boolean? = null,
    /** Only the fingerprint was read; test again with it as `pin_sha256`. */
    @SerialName("need_confirm") val needConfirm: Boolean? = null,
    /** Seen fingerprint does not match the given pin. */
    @SerialName("pin_differs") val pinDiffers: Boolean? = null,
    /** Host info. */
    val info: DockerProbeInfo? = null,
    /** Number of containers. */
    val containers: Int? = null,
    /** Number of running containers. */
    val running: Int? = null,
    /** Warning. */
    val warning: String? = null,
    /** SSH: public part of the used key. */
    @SerialName("public_key") val publicKey: String? = null,
)
