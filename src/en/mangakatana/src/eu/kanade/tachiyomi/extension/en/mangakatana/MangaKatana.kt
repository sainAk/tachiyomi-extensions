package eu.kanade.tachiyomi.extension.en.mangakatana

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.ListPreference
import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.text.SimpleDateFormat
import java.util.Locale

class MangaKatana : ConfigurableSource, ParsedHttpSource() {
    override val name = "MangaKatana"

    override val baseUrl = "https://mangakatana.com"

    override val lang = "en"

    override val supportsLatest = true

    private val preferences: SharedPreferences by lazy {
        Injekt.get<Application>().getSharedPreferences("source_$id", 0x0000)
    }
    private val serverPreference = "SERVER_PREFERENCE"

    override val client: OkHttpClient = network.cloudflareClient.newBuilder().addNetworkInterceptor { chain ->
        val originalResponse = chain.proceed(chain.request())
        if (originalResponse.headers("Content-Type").contains("application/octet-stream")) {
            val orgBody = originalResponse.body!!.bytes()
            val extension = chain.request().url.toString().substringAfterLast(".")
            val newBody = orgBody.toResponseBody("image/$extension".toMediaTypeOrNull())
            originalResponse.newBuilder()
                .body(newBody)
                .build()
        } else {
            originalResponse
        }
    }.build()

    override fun latestUpdatesSelector() = "div#book_list > div.item"

    override fun latestUpdatesRequest(page: Int) = GET("$baseUrl/page/$page", headers)

    override fun latestUpdatesFromElement(element: Element) = SManga.create().apply {
        setUrlWithoutDomain(element.select("div.text > h3 > a").attr("href"))
        title = element.select("div.text > h3 > a").text()
        thumbnail_url = element.select("img").attr("abs:src")
    }

    override fun latestUpdatesNextPageSelector() = ".next.page-numbers"

    override fun popularMangaSelector() = latestUpdatesSelector()

    override fun popularMangaRequest(page: Int) = GET("$baseUrl/manga/page/$page", headers)

    override fun popularMangaFromElement(element: Element) = latestUpdatesFromElement(element)

    override fun popularMangaNextPageSelector() = latestUpdatesNextPageSelector()

    override fun searchMangaSelector() = latestUpdatesSelector()

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList) = GET("$baseUrl/page/$page?search=$query&search_by=book_name", headers)

    override fun searchMangaFromElement(element: Element) = latestUpdatesFromElement(element)

    override fun searchMangaNextPageSelector() = latestUpdatesNextPageSelector()

    override fun searchMangaParse(response: Response): MangasPage {
        return if (response.request.url.toString().contains("/manga/")) {
            val document = response.asJsoup()
            val manga = SManga.create().apply {
                thumbnail_url = parseThumbnail(document)
                title = document.select("h1.heading").first().text()
            }
            manga.setUrlWithoutDomain(response.request.url.toString())
            MangasPage(listOf(manga), false)
        } else {
            super.searchMangaParse(response)
        }
    }

    override fun mangaDetailsParse(document: Document) = SManga.create().apply {
        author = document.select(".author").eachText().joinToString()
        description = document.select(".summary > p").text() +
            (document.select(".alt_name").text().takeIf { it.isNotBlank() }?.let { "\n\nAlt name(s): $it" } ?: "")
        status = parseStatus(document.select(".value.status").text())
        genre = document.select(".genres > a").joinToString { it.text() }
        thumbnail_url = parseThumbnail(document)
    }

    private fun parseThumbnail(document: Document) = document.select("div.media div.cover img").attr("abs:src")

    private fun parseStatus(status: String) = when {
        status.contains("Ongoing") -> SManga.ONGOING
        status.contains("Completed") -> SManga.COMPLETED
        else -> SManga.UNKNOWN
    }

    override fun chapterListSelector() = "tr:has(.chapter)"

    override fun chapterFromElement(element: Element) = SChapter.create().apply {
        setUrlWithoutDomain(element.select("a").attr("href"))
        name = element.select("a").text()
        date_upload = dateFormat.parse(element.select(".update_time").text())?.time ?: 0
    }

    companion object {
        val dateFormat by lazy {
            SimpleDateFormat("MMM-dd-yyyy", Locale.US)
        }
    }

    private val imageArrayRegex = Regex("""var ytaw=\[([^\[]*)]""")
    private val imageUrlRegex = Regex("""'([^']*)'""")

    override fun pageListRequest(chapter: SChapter): Request {
        val serverSuffix = preferences.getString(serverPreference, "")?.takeIf { it.isNotBlank() }?.let { "?sv=$it" } ?: ""
        return GET(baseUrl + chapter.url + serverSuffix, headers)
    }

    override fun pageListParse(document: Document): List<Page> {
        val imageArray = document.select("script:containsData(var ytaw)").firstOrNull()?.data()
            ?.let { imageArrayRegex.find(it)?.groupValues?.get(1) }
            ?: throw Exception("Image array not found")
        return imageUrlRegex.findAll(imageArray).asIterable().mapIndexed { i, mr ->
            Page(i, "", mr.groupValues[1])
        }
    }

    override fun imageUrlParse(document: Document): String = throw UnsupportedOperationException("Not Used")

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        val serverPref = ListPreference(screen.context).apply {
            key = "server_preference"
            title = "Server preference"
            entries = arrayOf("Server 1", "Server 2", "Server 3")
            entryValues = arrayOf("", "mk", "3")
            setDefaultValue("")
            summary = "%s"

            setOnPreferenceChangeListener { _, newValue ->
                val selected = newValue.toString()
                preferences.edit().putString(serverPreference, selected).commit()
            }
        }

        screen.addPreference(serverPref)
    }
}
