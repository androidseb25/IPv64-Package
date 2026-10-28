package de.sr.ipv64.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A DNS failover policy with its monitors and rules. */
@Serializable
public data class FailoverPolicy(
    /** Policy ID (16 characters). */
    val id: String? = null,
    /** Name. */
    val name: String? = null,
    /** Current state of the policy. */
    val status: FailoverStatus? = null,
    /** Whether the last switch was triggered manually. */
    val manual: Boolean? = null,
    /** Switching behaviour. */
    val mode: FailoverMode? = null,
    /** Fall back only once all monitors are UP again. */
    @SerialName("fallback_requires_all") val fallbackRequiresAll: Boolean? = null,
    /** How monitors are combined. */
    val linking: FailoverLinking? = null,
    /** Notifications enabled. */
    @SerialName("notifications_enabled") val notificationsEnabled: Boolean? = null,
    /** Integrations used for notifications (0 = account e-mail). */
    val integrations: List<Long> = emptyList(),
    /** Number of monitors currently counting as DOWN. */
    @SerialName("monitors_down") val monitorsDown: Int? = null,
    /** Time of the last check. */
    @SerialName("last_check_at") val lastCheckAt: IsoInstant? = null,
    /** Healthchecks that trigger the failover. */
    val monitors: List<FailoverMonitor> = emptyList(),
    /** Protected records and their replacement targets. */
    val rules: List<FailoverRule> = emptyList(),
)

/** A healthcheck monitoring a [FailoverPolicy]. */
@Serializable
public data class FailoverMonitor(
    /** Healthcheck token. */
    val healthcheck: String? = null,
    /** Name of the healthcheck. */
    val name: String? = null,
    /** Inverted evaluation (UP counts as DOWN). */
    val negate: Boolean? = null,
    /** Current status of the healthcheck. */
    @SerialName("healthcheck_status") val healthcheckStatus: String? = null,
)

/** A rule of a [FailoverPolicy] protecting an A or AAAA record. */
@Serializable
public data class FailoverRule(
    /** Token of the protected record. */
    val record: String? = null,
    /** Name of the record. */
    val fqdn: String? = null,
    /** `A` or `AAAA`. */
    @SerialName("record_type") val recordType: String? = null,
    /** State of the rule. */
    val state: FailoverRuleState? = null,
    /** Replacement target. */
    val target: FailoverRuleTarget? = null,
)

/** Replacement target of a [FailoverRule]. */
@Serializable
public data class FailoverRuleTarget(
    /** Kind of the target. */
    val kind: FailoverTargetKind? = null,
    /** Replacement IP (kind `ip`). */
    val ip: String? = null,
    /** Target record token (kind `record`). */
    val record: String? = null,
    /** `default` or offline page ID (kind `offline_page`). */
    @SerialName("offline_page") val offlinePage: String? = null,
)

/** State of a [FailoverPolicy]. */
@Serializable
public enum class FailoverStatus(override val value: String) : ApiValue {
    /** Being set up. */
    @SerialName("pending") PENDING("pending"),
    /** Monitored. */
    @SerialName("active") ACTIVE("active"),
    /** Replacement target active. */
    @SerialName("failed_over") FAILED_OVER("failed_over"),
    /** Paused. */
    @SerialName("paused") PAUSED("paused"),
    /** Switch to the replacement is in progress. */
    @SerialName("failover_requested") FAILOVER_REQUESTED("failover_requested"),
    /** Switch back is in progress. */
    @SerialName("fallback_requested") FALLBACK_REQUESTED("fallback_requested"),
}

/** Switching behaviour of a [FailoverPolicy]. */
@Serializable
public enum class FailoverMode(override val value: String) : ApiValue {
    /** Switch over automatically, switch back manually. */
    @SerialName("auto_failover") AUTO_FAILOVER("auto_failover"),
    /** Switch over and back automatically. */
    @SerialName("auto_failover_and_fallback") AUTO_FAILOVER_AND_FALLBACK("auto_failover_and_fallback"),
    /** Manual switching only. */
    @SerialName("manual") MANUAL("manual"),
}

/** How the monitors of a [FailoverPolicy] are combined. */
@Serializable
public enum class FailoverLinking(override val value: String) : ApiValue {
    /** All monitors must be DOWN. */
    @SerialName("and") AND("and"),
    /** One monitor being DOWN is enough. */
    @SerialName("or") OR("or"),
}

/** State of a [FailoverRule]. */
@Serializable
public enum class FailoverRuleState(override val value: String) : ApiValue {
    @SerialName("standby") STANDBY("standby"),
    @SerialName("failed_over") FAILED_OVER("failed_over"),
}

/** Kind of a [FailoverRuleTarget]. */
@Serializable
public enum class FailoverTargetKind(override val value: String) : ApiValue {
    @SerialName("ip") IP("ip"),
    @SerialName("record") RECORD("record"),
    @SerialName("offline_page") OFFLINE_PAGE("offline_page"),
}

/** Body of `POST /failover/policies`. */
@Serializable
public data class CreateFailoverPolicyRequest(
    /** Name. */
    val name: String? = null,
    /** Switching behaviour. */
    val mode: FailoverMode? = null,
    /** Fall back only once all monitors are UP again. */
    @SerialName("fallback_requires_all") val fallbackRequiresAll: Boolean? = null,
    /** How monitors are combined. */
    val linking: FailoverLinking? = null,
    /** Notifications enabled. */
    @SerialName("notifications_enabled") val notificationsEnabled: Boolean? = null,
    /** Integrations, 0 = account e-mail. */
    val integrations: List<Long>? = null,
)

/** Body of `PATCH /failover/policies/{id}`; `null` fields stay unchanged. */
@Serializable
public data class UpdateFailoverPolicyRequest(
    /** Name. */
    val name: String? = null,
    /** Switching behaviour. */
    val mode: FailoverMode? = null,
    /** Fall back only once all monitors are UP again. */
    @SerialName("fallback_requires_all") val fallbackRequiresAll: Boolean? = null,
    /** How monitors are combined (cannot be changed during an active failover). */
    val linking: FailoverLinking? = null,
    /** Notifications enabled. */
    @SerialName("notifications_enabled") val notificationsEnabled: Boolean? = null,
    /** Integrations, 0 = account e-mail. */
    val integrations: List<Long>? = null,
)

/** Body of `POST /failover/policies/{id}/monitors`. */
@Serializable
public data class AddFailoverMonitorRequest(
    /** Healthcheck token. */
    val healthcheck: String,
    /** Invert the evaluation. */
    val negate: Boolean? = null,
)

/**
 * Body of `POST /failover/policies/{id}/rules`.
 * Set exactly one of [targetIp], [targetRecord] or [offlinePage].
 */
@Serializable
public data class AddFailoverRuleRequest(
    /** Record ID or token of the protected record. */
    val record: String,
    /** Replacement IP of the same family. */
    @SerialName("target_ip") val targetIp: String? = null,
    /** Other own record whose IP is taken over. */
    @SerialName("target_record") val targetRecord: String? = null,
    /** `default` or offline page ID. */
    @SerialName("offline_page") val offlinePage: String? = null,
)
