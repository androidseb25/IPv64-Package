package de.sr.ipv64.node64.model

import de.sr.ipv64.model.IsoInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** User group of this node (role and AD/LDAP/SSO mapping; not a DNS64 group). */
@Serializable
public data class NodeGroup(
    /** Group uid. */
    val uid: String? = null,
    /** Name. */
    val name: String? = null,
    /** Role of the members. */
    val role: UserRole? = null,
    /** Description. */
    val description: String? = null,
    /** Mapped AD/LDAP groups (DN). */
    @SerialName("ldap_groups") val ldapGroups: List<String> = emptyList(),
    /** Mapped groups from the SSO claim. */
    @SerialName("oidc_groups") val oidcGroups: List<String> = emptyList(),
    /** User names (by hand). */
    val members: List<String> = emptyList(),
    /** User names (via AD/LDAP or SSO). */
    @SerialName("ext_members") val extMembers: List<String> = emptyList(),
    /** Creation time. */
    @SerialName("created_at") val createdAt: IsoInstant? = null,
    /** Last update. */
    @SerialName("updated_at") val updatedAt: IsoInstant? = null,
)

/** Body of `POST /groups` and `PUT /groups/{id}`; on update missing fields stay. */
@Serializable
public data class NodeGroupRequest(
    /** Name. */
    val name: String? = null,
    /** Role; default `read`. */
    val role: UserRole? = null,
    /** Description. */
    val description: String? = null,
    /** AD/LDAP groups (DN). */
    @SerialName("ldap_groups") val ldapGroups: List<String>? = null,
    /** Groups from the SSO claim. */
    @SerialName("oidc_groups") val oidcGroups: List<String>? = null,
)
