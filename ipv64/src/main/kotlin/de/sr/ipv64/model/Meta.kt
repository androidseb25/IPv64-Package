package de.sr.ipv64.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response of `GET /`. */
@Serializable
public data class ServiceInfo(
    /** Name of the API. */
    val name: String? = null,
    /** API version. */
    val version: String? = null,
    /** Link to the documentation. */
    val documentation: String? = null,
    /** Server time. */
    val time: IsoInstant? = null,
)

/** Response of `GET /health`. */
@Serializable
public data class HealthStatus(
    /** `ok` when the API and its database are available. */
    val status: String? = null,
    /** API version. */
    val version: String? = null,
)

/** Response of `GET /ip`. */
@Serializable
public data class PublicIpInfo(
    /** IP address the API was reached from. */
    val ip: String? = null,
    /** Address family. */
    val family: IpFamily? = null,
)

/** IP address family. */
@Serializable
public enum class IpFamily(override val value: String) : ApiValue {
    @SerialName("ipv4") IPV4("ipv4"),
    @SerialName("ipv6") IPV6("ipv6"),
}
