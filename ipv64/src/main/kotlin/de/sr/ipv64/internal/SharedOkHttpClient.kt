package de.sr.ipv64.internal

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/** Default [OkHttpClient] shared by all clients of the SDK, so they share one connection pool. */
internal object SharedOkHttpClient {
    val instance: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
