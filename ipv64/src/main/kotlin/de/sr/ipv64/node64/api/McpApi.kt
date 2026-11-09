package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.node64.model.CreateMcpKeyRequest
import de.sr.ipv64.node64.model.CreatedMcpKey
import de.sr.ipv64.node64.model.Mcp
import de.sr.ipv64.node64.model.UpdateMcpRequest

/** Tag *Node*: MCP (AI interface) switch, areas and own keys. */
public class McpApi internal constructor(private val transport: HttpTransport) {

    /** MCP switch, areas and own keys. `GET /mcp` */
    public suspend fun get(): Mcp =
        transport.json(Method.GET, ApiPath("mcp"))

    /** Switches MCP on or off; returns the new state. `PUT /mcp` */
    public suspend fun update(request: UpdateMcpRequest): Mcp =
        transport.json(Method.PUT, ApiPath("mcp"), body = transport.body(request))

    /** Creates a read-only MCP key; the secret is only in this response. `POST /mcp/keys` */
    public suspend fun createKey(request: CreateMcpKeyRequest): CreatedMcpKey =
        transport.json(Method.POST, ApiPath("mcp", "keys"), body = transport.body(request))

    /** Revokes an own MCP key. `DELETE /mcp/keys/{id}` */
    public suspend fun deleteKey(id: Long) {
        transport.unit(Method.DELETE, ApiPath("mcp", "keys", id.toString()))
    }
}
