package eu.kanade.tachiyomi.multisrc.mangabox

import generator.ThemeSourceData.SingleLang
import generator.ThemeSourceGenerator

class MangaBoxGenerator : ThemeSourceGenerator {

    override val themePkg = "mangabox"

    override val themeClass = "MangaBox"

    override val baseVersionCode: Int = 3

    override val sources = listOf(
        SingleLang("Mangakakalot", "https://mangakakalot.com", "en", overrideVersionCode = 3),
        SingleLang("Manganato", "https://manganato.com", "en", overrideVersionCode = 2, pkgName = "manganelo"),
        SingleLang("Mangabat", "https://m.mangabat.com", "en", overrideVersionCode = 4),
        SingleLang("Mangakakalots (unoriginal)", "https://mangakakalots.com", "en", overrideVersionCode = 1, className = "Mangakakalots", pkgName = "mangakakalots"),
        SingleLang("Mangairo", "https://h.mangairo.com", "en", overrideVersionCode = 3),
    )

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            MangaBoxGenerator().createAll()
        }
    }
}
