package eu.kanade.tachiyomi.extension.all.nhentaicom

import eu.kanade.tachiyomi.annotations.Nsfw
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

@Nsfw
class NHentaiComFactory : SourceFactory {
    override fun createSources(): List<Source> = languages.map { NHentaiCom(it) }
}

private val languages = listOf(
    "all", "en", "zh", "ja", "other", "eo", "cz", "ar", "sk"
)
