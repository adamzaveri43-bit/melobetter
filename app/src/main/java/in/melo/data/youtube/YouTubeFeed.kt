package `in`.melo.data.youtube

import `in`.melo.data.Music
import `in`.melo.data.MusicKind
import org.json.JSONArray
import org.json.JSONObject

/** One row of the home feed: a titled list of tracks, albums, artists or playlists. */
data class FeedShelf(val key: String, val title: String, val items: List<Music>, val tracksOnly: Boolean = false)

/**
 * The music the home screen shows, read from YouTube Music's own pages: its home (personalized
 * when a Google account is connected), then the explore, new releases and charts pages, which
 * answer without an account. Podcasts and episodes are left out. Every call blocks.
 */
object YouTubeFeed {
    /** YouTube Music's home shelves, with the account when there is one, following a few continuations. */
    fun home(): List<FeedShelf> {
        val anonymous: (JSONObject) -> JSONObject = { body -> YouTubeMusic.browseRequest("browse", body) }
        val withAccount: (JSONObject) -> JSONObject = { body -> YouTubeLibrary.browse(body) }
        val request = if (YouTubeAccount.signedIn) withAccount else anonymous
        val first = request(JSONObject().put("browseId", "FEmusic_home"))
        val shelves = ArrayList(parseShelves(first, "home"))
        val seen = HashSet<String>()
        var token = findContinuation(first, preferShelf = false)
        var pages = 0
        while (token != null && seen.add(token) && pages++ < MAX_HOME_PAGES) {
            val page = runCatching { request(JSONObject().put("continuation", token)) }.getOrNull() ?: break
            val more = parseShelves(page, "home$pages")
            if (more.isEmpty()) break
            shelves += more
            token = findContinuation(page, preferShelf = false)
        }
        return shelves
    }

    /**
     * Explore, new releases and charts: what anyone sees, account or not. A shelf the page leaves
     * untitled (the new releases grid) takes the page's name from [pageTitles], keyed by "explore", "new" and "charts".
     */
    fun discover(pageTitles: Map<String, String>): List<FeedShelf> = DISCOVER_PAGES.flatMap { (browseId, prefix) ->
        runCatching { parseShelves(YouTubeMusic.browseRequest("browse", JSONObject().put("browseId", browseId)), prefix) }
            .getOrDefault(emptyList())
            .map { if (it.title.isBlank()) it.copy(title = pageTitles[prefix].orEmpty()) else it }
    }

    /** The automatic radio YouTube Music builds after a track: similar songs, the seed excluded. */
    fun radio(videoId: String): List<Music> {
        val response = YouTubeMusic.browseRequest("next", JSONObject().put("videoId", videoId)
            .put("playlistId", "RDAMVM$videoId").put("isAudioOnly", true))
        return parseRadio(response).filterNot { it.id == YTM_TRACK_PREFIX + videoId }
    }

    /** The YouTube Music track that a Spotify or local track most likely is, to seed a radio. */
    fun videoIdFor(track: Music): String? = track.youTubeVideoId
        ?: YouTubeMusic.searchSongs("${track.title} ${track.subtitle}".trim()).firstOrNull()?.youTubeVideoId

    private const val MAX_HOME_PAGES = 3
    private val DISCOVER_PAGES = listOf(
        "FEmusic_explore" to "explore",
        "FEmusic_new_releases" to "new",
        "FEmusic_charts" to "charts",
    )
}

private val HIDDEN_PAGE_TYPES = setOf(
    "MUSIC_PAGE_TYPE_PODCAST_SHOW_DETAIL_PAGE", "MUSIC_PAGE_TYPE_NON_MUSIC_AUDIO_TRACK_PAGE", "MUSIC_PAGE_TYPE_USER_CHANNEL",
)
private val HIDDEN_SHELF = Regex("podcast|episode", RegexOption.IGNORE_CASE)
private val TYPE_WORDS = setOf("Song", "Video", "Album", "Single", "EP", "Playlist", "Chart", "Artist", "Mix", "Station")
private val YEAR = Regex("\\d{4}")
private val VIEWS = Regex("^[\\d.,]+[KMB]? (?:views|plays|subscribers|monthly audience)$", RegexOption.IGNORE_CASE)

/** Every carousel and grid of a browse response, in order, each with its items. */
internal fun parseShelves(response: JSONObject, prefix: String): List<FeedShelf> {
    val shelves = ArrayList<FeedShelf>()
    walkJson(response) { node ->
        val shelf = node.optJSONObject("musicCarouselShelfRenderer") ?: node.optJSONObject("gridRenderer") ?: return@walkJson
        val header = shelf.optJSONObject("header")?.let { it.optJSONObject("musicCarouselShelfBasicHeaderRenderer") ?: it.optJSONObject("gridHeaderRenderer") }
        val title = header?.optJSONObject("title")?.optJSONArray("runs")?.let { runs ->
            (0 until runs.length()).joinToString("") { runs.optJSONObject(it)?.optString("text").orEmpty() }
        }?.trim().orEmpty()
        if (HIDDEN_SHELF.containsMatchIn(title)) return@walkJson
        val contents = shelf.optJSONArray("contents") ?: shelf.optJSONArray("items") ?: return@walkJson
        val items = (0 until contents.length()).mapNotNull { index ->
            val entry = contents.optJSONObject(index) ?: return@mapNotNull null
            entry.optJSONObject("musicTwoRowItemRenderer")?.let(::twoRowEntry)
                ?: entry.optJSONObject("musicResponsiveListItemRenderer")?.let(::listEntry)
        }.distinctBy(Music::id)
        if (items.isEmpty()) return@walkJson
        shelves += FeedShelf("ytm_${prefix}_${shelves.size}", title, items, tracksOnly = items.all { it.kind == MusicKind.Track } &&
            contents.optJSONObject(0)?.has("musicResponsiveListItemRenderer") == true)
    }
    return shelves
}

/** The tracks of a `next` response's queue panel. */
internal fun parseRadio(response: JSONObject): List<Music> {
    val tracks = ArrayList<Music>()
    walkJson(response) { node ->
        val panel = node.optJSONObject("playlistPanelVideoRenderer") ?: return@walkJson
        val videoId = panel.optString("videoId").takeIf(String::isNotBlank) ?: return@walkJson
        val title = panel.optJSONObject("title")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")?.takeIf(String::isNotBlank) ?: return@walkJson
        val byline = panel.optJSONObject("longBylineText")?.optJSONArray("runs")
        val parts = byline?.let(::partsOf).orEmpty()
        byline?.let { YouTubePages.remember(videoId, YouTubePages.relationsOf(it)) }
        val length = panel.optJSONObject("lengthText")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")
        val thumbnails = panel.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
        val image = thumbnails?.optJSONObject(thumbnails.length() - 1)?.optString("url")?.takeIf(String::isNotBlank)
        tracks += Music(YTM_TRACK_PREFIX + videoId, title, parts.firstOrNull().orEmpty(), MusicKind.Track,
            imageUrl = largestYouTubeImage(image?.takeIf { "googleusercontent" in it || "ggpht" in it }, videoId),
            durationMs = length?.let(::durationMs) ?: 0,
            album = parts.drop(1).firstOrNull { !VIEWS.matches(it) && !YEAR.matches(it) && !it.endsWith("likes") }.orEmpty())
    }
    return tracks.distinctBy(Music::id)
}

/** Texts of runs grouped between the " • " separators. */
private fun partsOf(runs: JSONArray): List<String> {
    val parts = ArrayList<String>()
    val current = StringBuilder()
    for (index in 0 until runs.length()) {
        val text = runs.optJSONObject(index)?.optString("text").orEmpty()
        if (text == " • ") { current.toString().trim().takeIf(String::isNotBlank)?.let(parts::add); current.clear() } else current.append(text)
    }
    current.toString().trim().takeIf(String::isNotBlank)?.let(parts::add)
    return parts
}

private fun pageTypeOf(browse: JSONObject): String? = browse.optJSONObject("browseEndpointContextSupportedConfigs")
    ?.optJSONObject("browseEndpointContextMusicConfig")?.optString("pageType")

private fun thumbnailUrl(holder: JSONObject?): String? {
    val list = holder?.optJSONObject("musicThumbnailRenderer")?.optJSONObject("thumbnail")?.optJSONArray("thumbnails") ?: return null
    return list.optJSONObject(list.length() - 1)?.optString("url")?.takeIf(String::isNotBlank)
}

/** A cover card: a song or video (watch link), or an album, artist or playlist page. */
internal fun twoRowEntry(item: JSONObject): Music? {
    val title = item.optJSONObject("title")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")?.takeIf(String::isNotBlank) ?: return null
    val subtitleRuns = item.optJSONObject("subtitle")?.optJSONArray("runs")
    val parts = subtitleRuns?.let(::partsOf).orEmpty()
    val label = parts.firstOrNull()?.takeIf { it in TYPE_WORDS }
    val details = if (label != null) parts.drop(1) else parts
    val image = thumbnailUrl(item.optJSONObject("thumbnailRenderer"))
    val endpoint = item.optJSONObject("navigationEndpoint")
    endpoint?.optJSONObject("watchEndpoint")?.optString("videoId")?.takeIf(String::isNotBlank)?.let { videoId ->
        subtitleRuns?.let { YouTubePages.remember(videoId, YouTubePages.relationsOf(it)) }
        return Music(YTM_TRACK_PREFIX + videoId, title, details.firstOrNull { !VIEWS.matches(it) }.orEmpty(), MusicKind.Track,
            imageUrl = largestYouTubeImage(image?.takeIf { "googleusercontent" in it || "ggpht" in it }, videoId))
    }
    val browse = endpoint?.optJSONObject("browseEndpoint") ?: return null
    return browseEntity(browse, title, details, image?.let { largestYouTubeImage(it, "") })
}

/** A list row: a track, or an artist, album or playlist (as the charts list top artists). */
internal fun listEntry(row: JSONObject): Music? {
    val browse = row.optJSONObject("navigationEndpoint")?.optJSONObject("browseEndpoint") ?: return parseTrackRow(row)
    val flex = row.optJSONArray("flexColumns") ?: return null
    fun column(index: Int) = flex.optJSONObject(index)?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")?.optJSONObject("text")?.optJSONArray("runs")
    val title = column(0)?.optJSONObject(0)?.optString("text")?.takeIf(String::isNotBlank) ?: return null
    val parts = column(1)?.let(::partsOf).orEmpty()
    val details = if (parts.firstOrNull()?.let { it in TYPE_WORDS } == true) parts.drop(1) else parts
    return browseEntity(browse, title, details, thumbnailUrl(row.optJSONObject("thumbnail"))?.let { largestYouTubeImage(it, "") })
}

private fun browseEntity(browse: JSONObject, title: String, details: List<String>, image: String?): Music? {
    val id = browse.optString("browseId").takeIf(String::isNotBlank) ?: return null
    val type = pageTypeOf(browse)
    if (type in HIDDEN_PAGE_TYPES) return null
    val subtitle = details.firstOrNull { !VIEWS.matches(it) }.orEmpty()
    return when {
        type == "MUSIC_PAGE_TYPE_ALBUM" -> Music(YTM_ALBUM_PREFIX + id, title, subtitle.takeUnless { YEAR.matches(it) }.orEmpty(), MusicKind.Album,
            imageUrl = image, note = details.lastOrNull { YEAR.matches(it) }.orEmpty())
        type == "MUSIC_PAGE_TYPE_ARTIST" || (type == null && id.startsWith("UC")) ->
            Music(YTM_ARTIST_PREFIX + id, title, "Artist", MusicKind.Artist, imageUrl = image)
        type == "MUSIC_PAGE_TYPE_PLAYLIST" || id.startsWith("VL") ->
            Music(YTM_PLAYLIST_PREFIX + id.removePrefix("VL"), title, subtitle, MusicKind.Playlist, imageUrl = image, ownerUsername = subtitle.ifBlank { null })
        else -> null
    }
}
