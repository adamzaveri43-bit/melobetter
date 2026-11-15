package `in`.melo.data.youtube

import `in`.melo.data.Music
import `in`.melo.data.MusicKind
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Search of all of YouTube's videos, not only YouTube Music's catalogue: what neither YouTube Music
 * nor Spotify carries (game soundtracks uploaded by fans, live sets, covers, rare uploads) is found
 * here. Results are tracks with a `ytm:track:` ID, played like any YouTube Music track: the audio
 * stream of the video, through [YouTubeDataSource]. Every call blocks: run it on an IO thread.
 */
object YouTubeVideos {
    fun search(query: String): YouTubePage = page(JSONObject().put("query", query).put("params", VIDEOS_ONLY))

    fun more(token: String): YouTubePage = page(JSONObject().put("continuation", token))

    private fun page(body: JSONObject): YouTubePage {
        body.put("context", JSONObject().put("client", JSONObject()
            .put("clientName", "WEB").put("clientVersion", YouTubeClient.WEB.clientVersion).put("hl", "en").put("gl", "US")))
        val request = Request.Builder().url("https://www.youtube.com/youtubei/v1/search?prettyPrint=false")
            .header("User-Agent", YouTubeMusic.BROWSER_AGENT)
            .header("X-YouTube-Client-Name", "1").header("X-YouTube-Client-Version", YouTubeClient.WEB.clientVersion)
            .header("Origin", "https://www.youtube.com").header("Referer", "https://www.youtube.com/")
            .post(body.toString().toRequestBody(JSON))
            .build()
        YouTubeMusic.http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw YouTubeFailure("YouTube answered ${response.code}.", response.code.toString())
            val json = JSONObject(response.body?.string().orEmpty())
            return YouTubePage(parseVideoResults(json), findContinuation(json, preferShelf = false))
        }
    }

    /** "EgIQAQ==": the videos filter of YouTube's search. */
    private const val VIDEOS_ONLY = "EgIQAQ%3D%3D"
    private val JSON = "application/json".toMediaType()
}

/** The videos of a YouTube search page or continuation; live streams and shorts without a length are left out. */
internal fun parseVideoResults(response: JSONObject): List<Music> {
    val videos = ArrayList<Music>()
    walkJson(response) { node ->
        val video = node.optJSONObject("videoRenderer") ?: return@walkJson
        val videoId = video.optString("videoId").takeIf(String::isNotBlank) ?: return@walkJson
        val title = video.optJSONObject("title")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")?.takeIf(String::isNotBlank)
            ?: video.optJSONObject("title")?.optString("simpleText")?.takeIf(String::isNotBlank) ?: return@walkJson
        val length = video.optJSONObject("lengthText")?.optString("simpleText")?.takeIf(String::isNotBlank) ?: return@walkJson
        val channel = (video.optJSONObject("ownerText") ?: video.optJSONObject("longBylineText"))
            ?.optJSONArray("runs")?.optJSONObject(0)?.optString("text").orEmpty()
        videos += Music(
            id = YTM_TRACK_PREFIX + videoId, title = title, subtitle = channel, kind = MusicKind.Track,
            imageUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg", durationMs = durationMs(length),
        )
    }
    return videos.distinctBy(Music::id)
}
