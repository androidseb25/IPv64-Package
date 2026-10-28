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

/** Body of `POST /healthchecks/docker/systems`. Without [addresses] the local socket is used. */
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
)
