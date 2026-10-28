package de.sr.ipv64.node64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.body
import de.sr.ipv64.internal.json
import de.sr.ipv64.internal.query
import de.sr.ipv64.node64.model.EdgeConfigRequest
import de.sr.ipv64.node64.model.EdgeStatus
import de.sr.ipv64.node64.model.EdgeUiConfig
import de.sr.ipv64.node64.model.NetProtocol
import de.sr.ipv64.node64.model.PortProbeResult
import de.sr.ipv64.node64.model.PortsOverview
import de.sr.ipv64.node64.model.UpdateEdgeUiRequest

/** Ports of all parts, port probe and the shared entry (edge). */
public class EdgeApi internal constructor(private val transport: HttpTransport) {

    /** Ports claimed by all parts including overlaps, plus the state of the shared entry. `GET /ports` */
    public suspend fun ports(): PortsOverview =
        transport.json(Method.GET, ApiPath("ports"))

    /**
     * Checks whether a port is free on the host.
     *
     * @param bind optional bind address.
     * `GET /ports/probe`
     */
    public suspend fun probePort(proto: NetProtocol, port: Int, bind: String? = null): PortProbeResult =
        transport.json(Method.GET, ApiPath("ports", "probe"), query("proto" to proto, "port" to port, "bind" to bind))

    /** Shared entry: settings, open ports and registered names. `GET /edge` */
    public suspend fun get(): EdgeStatus =
        transport.json(Method.GET, ApiPath("edge"))

    /**
     * Changes the settings of the shared entry (missing fields stay). Open ports are briefly closed and reopened.
     * `PUT /edge`
     */
    public suspend fun update(request: EdgeConfigRequest): EdgeStatus =
        transport.json(Method.PUT, ApiPath("edge"), body = transport.body(request))

    /** Node64 UI under its own name via the shared entry. `GET /edge/ui` */
    public suspend fun ui(): EdgeUiConfig =
        transport.json(Method.GET, ApiPath("edge", "ui"))

    /** Changes name, release and certificate of the UI (no restart). `PUT /edge/ui` */
    public suspend fun updateUi(request: UpdateEdgeUiRequest): EdgeUiConfig =
        transport.json(Method.PUT, ApiPath("edge", "ui"), body = transport.body(request))
}
