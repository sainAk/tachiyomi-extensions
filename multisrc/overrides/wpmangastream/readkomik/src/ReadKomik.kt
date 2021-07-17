package eu.kanade.tachiyomi.extension.en.readkomik

import eu.kanade.tachiyomi.multisrc.wpmangastream.WPMangaStream
import eu.kanade.tachiyomi.lib.ratelimit.RateLimitInterceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class ReadKomik : WPMangaStream("Readkomik", "https://readkomik.com", "en") {
    private val rateLimitInterceptor = RateLimitInterceptor(4)

    override val client: OkHttpClient = network.cloudflareClient.newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addNetworkInterceptor(rateLimitInterceptor)
        .build()

    override val hasProjectPage = true
}
