package eu.kanade.tachiyomi.extension.en.manhuabox

import eu.kanade.tachiyomi.multisrc.madara.Madara

class ManhuaBox : Madara("ManhuaBox", "https://manhuabox.net", "en") {
    override val pageListParseSelector = "div.page-break, div.text-left p img"
}
