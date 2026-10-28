package de.sr.ipv64.api

import de.sr.ipv64.internal.ApiPath
import de.sr.ipv64.internal.HttpTransport
import de.sr.ipv64.internal.Method
import de.sr.ipv64.internal.data
import de.sr.ipv64.internal.query
import de.sr.ipv64.model.AccountLinks
import de.sr.ipv64.model.LogPage
import de.sr.ipv64.model.Me
import kotlin.time.Instant

/** Tag *Account*: plan, usage, log and account links. */
public class AccountApi internal constructor(private val transport: HttpTransport) {

    /** Shows plan, usage and limits as well as the credentials in use. `GET /me` */
    public suspend fun me(): Me =
        transport.data(Method.GET, ApiPath("me"))

    /**
     * Account log, newest entries first. Only entries within the plan's retention time are returned. `GET /me/logs`
     *
     * @param limit number of entries (1 to 500, server default 50).
     * @param before only entries with a smaller ID (cursor, use `meta.nextBefore`).
     * @param since only entries from this point in time.
     */
    public suspend fun logs(limit: Int? = null, before: Long? = null, since: Instant? = null): LogPage =
        transport.decode(
            LogPage.serializer(),
            transport.text(
                Method.GET,
                ApiPath("me", "logs"),
                query("limit" to limit, "before" to before, "since" to since),
            ),
        )

    /** Who may manage your account (`sharedWith`) and which accounts you may manage (`managed`). `GET /me/account-links` */
    public suspend fun accountLinks(): AccountLinks =
        transport.data(Method.GET, ApiPath("me", "account-links"))
}
