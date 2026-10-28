package de.sr.ipv64

import de.sr.ipv64.internal.IPv64Json
import de.sr.ipv64.model.Dns64Blocklist
import de.sr.ipv64.model.Dns64Client
import de.sr.ipv64.model.Hc2ChannelUpdate
import de.sr.ipv64.model.Hc2CheckParams
import de.sr.ipv64.model.Hc2CheckUpdate
import de.sr.ipv64.model.Hc2MaintenanceUpdate
import de.sr.ipv64.model.HoneypotListToken
import de.sr.ipv64.model.SetPeerKeysRequest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class VpnHc2ModelTest {

    @Test
    fun `unknown dns64 blocklist does not break decoding`() {
        val client = IPv64Json.decodeFromString(
            Dns64Client.serializer(),
            """{"id":"x","blocklists":["ads","future_list","threats"]}""",
        )
        assertEquals(listOf(Dns64Blocklist.ADS, Dns64Blocklist.THREATS), client.blocklists)
    }

    @Test
    fun `hc2 partial updates only send the set properties`() {
        assertEquals(
            """{"paused":true}""",
            IPv64Json.encodeToString(Hc2CheckUpdate.serializer(), Hc2CheckUpdate(paused = true)),
        )
        assertEquals(
            """{"enabled":false}""",
            IPv64Json.encodeToString(Hc2ChannelUpdate.serializer(), Hc2ChannelUpdate(enabled = false)),
        )
        assertEquals(
            """{"active":false}""",
            IPv64Json.encodeToString(Hc2MaintenanceUpdate.serializer(), Hc2MaintenanceUpdate(active = false)),
        )
    }

    @Test
    fun `secrets are not exposed by toString`() {
        val texts = listOf(
            SetPeerKeysRequest(publicKey = "pub", privateKey = "PRIVSECRET", presharedKey = "PSKSECRET").toString(),
            HoneypotListToken(token = "TOKSECRET", live = true, base = "https://x/TOKSECRET/").toString(),
            Hc2CheckParams(basicPassword = "PWSECRET", headers = mapOf("Authorization" to "HDRSECRET")).toString(),
            Hc2ChannelUpdate(config = buildJsonObject { put("bot_token", JsonPrimitive("CFGSECRET")) }).toString(),
        )
        for (text in texts) {
            assertFalse(text, Regex("[A-Z]+SECRET").containsMatchIn(text))
        }
    }
}
