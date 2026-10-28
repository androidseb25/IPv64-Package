package de.sr.ipv64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.data
import de.sr.ipv64.internal.list
import de.sr.ipv64.internal.query
import de.sr.ipv64.model.Hc2Command
import de.sr.ipv64.model.Hc2CommandArgs
import de.sr.ipv64.model.Hc2Node
import de.sr.ipv64.model.Hc2NodeCommandRequest
import de.sr.ipv64.model.Hc2NodeDetail
import de.sr.ipv64.model.LinkNodeRequest
import de.sr.ipv64.model.LinkNodeResult
import de.sr.ipv64.model.NodeHelloRequest
import de.sr.ipv64.model.UnlinkNodeRequest
import de.sr.ipv64.model.UnlinkNodeResult
import de.sr.ipv64.model.UpdateNodeRequest
import kotlinx.serialization.json.JsonElement

/** Tag *Nodes*: Node64 containers and their link to Healthchecks 2.0. */
public class NodesApi internal constructor(private val transport: HttpTransport) {

    /**
     * Announces a Node64 container (called by the container on start, after changes and every 15 minutes).
     * The `X-Node64-Node` header is set to [NodeHelloRequest.nodeId] as required by the API. `POST /node64/hello`
     */
    public suspend fun hello(request: NodeHelloRequest) {
        transport.unit(
            Method.POST,
            ApiPath("node64", "hello"),
            body = transport.body(request),
            headers = mapOf("X-Node64-Node" to request.nodeId),
        )
    }

    /**
     * All Node64 containers of the account (node overview). The spec documents no response body,
     * so the raw JSON is returned (`null` if the response is empty). `GET /node64/nodes`
     */
    public suspend fun overview(): JsonElement? {
        val text = transport.text(Method.GET, ApiPath("node64", "nodes"))
        return if (text.isBlank()) null else transport.decode(JsonElement.serializer(), text)
    }

    /** Lists the linked nodes. `GET /hc2/nodes` */
    public suspend fun list(): List<Hc2Node> =
        transport.list(Method.GET, ApiPath("hc2", "nodes"))

    /**
     * Links a node using the code it shows under "ipv64 link"; the node then fetches its token itself.
     * `POST /hc2/nodes/link`
     */
    public suspend fun link(request: LinkNodeRequest): LinkNodeResult =
        transport.data(Method.POST, ApiPath("hc2", "nodes", "link"), body = transport.body(request))

    /** Returns a node including its checks. `GET /hc2/nodes/{node_id}` */
    public suspend fun get(nodeId: String): Hc2NodeDetail =
        transport.data(Method.GET, ApiPath("hc2", "nodes", nodeId))

    /** Renames a node or changes its location / remote management. `PATCH /hc2/nodes/{node_id}` */
    public suspend fun update(nodeId: String, request: UpdateNodeRequest): Hc2Node =
        transport.data(Method.PATCH, ApiPath("hc2", "nodes", nodeId), body = transport.body(request))

    /**
     * Unlinks a node and revokes its token; the node keeps running standalone.
     * [keepChecks] continues checks with a public target as online checks (server default true).
     * `DELETE /hc2/nodes/{node_id}`
     */
    public suspend fun unlink(nodeId: String, keepChecks: Boolean? = null): UnlinkNodeResult =
        transport.data(
            Method.DELETE,
            ApiPath("hc2", "nodes", nodeId),
            body = transport.body(UnlinkNodeRequest(keepChecks)),
        )

    /** Latest commands of a node. [limit] is at most 200. `GET /hc2/nodes/{node_id}/commands` */
    public suspend fun commands(nodeId: String, limit: Int? = null): List<Hc2Command> =
        transport.list(Method.GET, ApiPath("hc2", "nodes", nodeId, "commands"), query("limit" to limit))

    /**
     * Sends a command to a node (`run_check`, `repair`, `pause`, `resume`, `resolve_conflict`);
     * the node only accepts commands if its remote management allows it. `POST /hc2/nodes/{node_id}/commands`
     */
    public suspend fun sendCommand(nodeId: String, request: Hc2NodeCommandRequest): Hc2Command =
        transport.data(Method.POST, ApiPath("hc2", "nodes", nodeId, "commands"), body = transport.body(request))

    /** Convenience overload of [sendCommand] taking the command [name] and optional [args]. */
    public suspend fun sendCommand(nodeId: String, name: String, args: Hc2CommandArgs? = null): Hc2Command =
        sendCommand(nodeId, Hc2NodeCommandRequest(name, args))
}
