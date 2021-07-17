package eu.kanade.tachiyomi.multisrc.guya

import generator.ThemeSourceData.SingleLang
import generator.ThemeSourceData.MultiLang
import generator.ThemeSourceGenerator

class GuyaGenerator : ThemeSourceGenerator {

    override val themePkg = "guya"

    override val themeClass = "Guya"

    override val baseVersionCode: Int = 2

    override val sources = listOf(
        SingleLang("Guya", "https://guya.moe", "en", overrideVersionCode = 18),
        SingleLang("Danke fürs Lesen", "https://danke.moe", "en", className = "DankeFursLesen"),
        SingleLang("Colored Council", "https://coloredcouncil.moe", "en"),
        SingleLang("Hachirumi", "https://hachirumi.com", "en", isNsfw = true),
        MultiLang("Magical Translators", "https://mahoushoujobu.com", listOf("en", "pl"), className = "MagicalTranslatorsFactory"),
    )
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            GuyaGenerator().createAll()
        }
    }
}
