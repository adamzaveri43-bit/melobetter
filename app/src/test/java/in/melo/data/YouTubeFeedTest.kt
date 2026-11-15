package `in`.melo.data

import `in`.melo.data.preferences.HomeLayout
import `in`.melo.data.preferences.HomeShelf
import `in`.melo.data.youtube.parseRadio
import `in`.melo.data.youtube.parseShelves
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The home feed's reading of YouTube Music pages, on responses shaped like music.youtube.com's (contracts with doubles). */
class YouTubeFeedTest {
    private fun runs(vararg texts: String) = JSONObject().put("runs", JSONArray().apply { texts.forEach { put(JSONObject().put("text", it)) } })
    private fun browse(id: String, type: String) = JSONObject().put("browseEndpoint", JSONObject().put("browseId", id)
        .put("browseEndpointContextSupportedConfigs", JSONObject().put("browseEndpointContextMusicConfig", JSONObject().put("pageType", type))))
    private fun thumb(url: String) = JSONObject().put("musicThumbnailRenderer", JSONObject().put("thumbnail",
        JSONObject().put("thumbnails", JSONArray().put(JSONObject().put("url", url)))))

    private fun twoRow(title: String, subtitle: Array<String>, endpoint: JSONObject) = JSONObject().put("musicTwoRowItemRenderer", JSONObject()
        .put("title", runs(title)).put("subtitle", runs(*subtitle)).put("navigationEndpoint", endpoint)
        .put("thumbnailRenderer", thumb("https://lh3.googleusercontent.com/x=w226-h226-l90-rj")))

    private fun carousel(title: String, vararg items: JSONObject) = JSONObject().put("musicCarouselShelfRenderer", JSONObject()
        .put("header", JSONObject().put("musicCarouselShelfBasicHeaderRenderer", JSONObject().put("title", runs(title))))
        .put("contents", JSONArray().apply { items.forEach { put(it) } }))

    private fun page(vararg sections: JSONObject) = JSONObject().put("contents", JSONObject().put("sectionListRenderer",
        JSONObject().put("contents", JSONArray().apply { sections.forEach { put(it) } })))

    private fun songRow(videoId: String, title: String, artist: String) = JSONObject().put("musicResponsiveListItemRenderer", JSONObject()
        .put("flexColumns", JSONArray()
            .put(JSONObject().put("musicResponsiveListItemFlexColumnRenderer", JSONObject().put("text", JSONObject().put("runs", JSONArray()
                .put(JSONObject().put("text", title).put("navigationEndpoint", JSONObject().put("watchEndpoint", JSONObject().put("videoId", videoId))))))))
            .put(JSONObject().put("musicResponsiveListItemFlexColumnRenderer", JSONObject().put("text", runs("Song", " • ", artist, " • ", "3:20"))))))

    @Test fun shelvesKeepMusicAndDropPodcasts() {
        val response = page(
            carousel("Podcasts to get you started", twoRow("A show", arrayOf("Podcast"), browse("MPSPx", "MUSIC_PAGE_TYPE_PODCAST_SHOW_DETAIL_PAGE"))),
            carousel("Genres",
                twoRow("Top 50 Rock", arrayOf("Chart", " • ", "YouTube Charts"), browse("VLPLrock", "MUSIC_PAGE_TYPE_PLAYLIST")),
                twoRow("Random Access Memories", arrayOf("Album", " • ", "Daft Punk", " • ", "2013"), browse("MPREb_ram", "MUSIC_PAGE_TYPE_ALBUM")),
                twoRow("A show", arrayOf("Podcast"), browse("MPSPy", "MUSIC_PAGE_TYPE_PODCAST_SHOW_DETAIL_PAGE"))),
            carousel("New music videos",
                twoRow("Song A", arrayOf("Artist A", " • ", "1.2M views"), JSONObject().put("watchEndpoint", JSONObject().put("videoId", "vidA")))),
            carousel("Trending", songRow("vidB", "Song B", "Artist B"), songRow("vidC", "Song C", "Artist C")),
        )
        val shelves = parseShelves(response, "explore")
        assertEquals(listOf("Genres", "New music videos", "Trending"), shelves.map { it.title })
        val genres = shelves[0].items
        assertEquals(listOf("ytm:playlist:PLrock", "ytm:album:MPREb_ram"), genres.map { it.id })
        assertEquals("YouTube Charts", genres[0].subtitle)
        assertEquals("Daft Punk", genres[1].subtitle)
        assertEquals("2013", genres[1].note)
        val video = shelves[1].items.single()
        assertEquals(MusicKind.Track, video.kind)
        assertEquals("Artist A", video.subtitle)
        // Cover cards stay a carousel; list rows of songs become quick picks.
        assertEquals(false, shelves[1].tracksOnly)
        assertTrue(shelves[2].tracksOnly)
        assertEquals(listOf("ytm:track:vidB", "ytm:track:vidC"), shelves[2].items.map { it.id })
        assertEquals(setOf("ytm_explore_0", "ytm_explore_1", "ytm_explore_2"), shelves.map { it.key }.toSet())
    }

    @Test fun radioReadsTheQueuePanel() {
        fun panel(videoId: String, title: String) = JSONObject().put("playlistPanelVideoRenderer", JSONObject()
            .put("videoId", videoId).put("title", runs(title))
            .put("longBylineText", runs("Rick Astley", " • ", "Whenever You Need Somebody", " • ", "1987"))
            .put("lengthText", runs("3:24")))
        val response = JSONObject().put("contents", JSONArray().put(panel("seed", "Seed")).put(panel("next1", "Together Forever")).put(panel("next1", "Together Forever")))
        val tracks = parseRadio(response)
        assertEquals(listOf("ytm:track:seed", "ytm:track:next1"), tracks.map { it.id })
        assertEquals("Rick Astley", tracks[1].subtitle)
        assertEquals("Whenever You Need Somebody", tracks[1].album)
        assertEquals(204_000, tracks[1].durationMs)
    }

    @Test fun earlierLayoutsLoseThePlaylistShelvesAndGainTheFeedAfterRecent() {
        val stored = """[{"shelf":"pins","visible":true,"style":"pills","count":6},{"shelf":"recent","visible":true,"style":"carousel","count":6},
            {"shelf":"playlists","visible":true,"style":"carousel","count":20},{"shelf":"albums","visible":false,"style":"grid","count":4}]"""
        val shelves = HomeLayout.decode(stored).map { it.shelf }
        assertEquals(listOf(HomeShelf.PINS, HomeShelf.RECENT, HomeShelf.FEED), shelves.take(3))
        assertEquals(HomeShelf.entries.toSet(), shelves.toSet())
        assertEquals(HomeShelf.entries.size, shelves.size)
        assertEquals(false, HomeLayout.decode(stored).first { it.shelf == HomeShelf.ALBUMS }.visible)
    }
}
