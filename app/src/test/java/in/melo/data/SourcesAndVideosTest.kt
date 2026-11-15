package `in`.melo.data

import `in`.melo.data.preferences.MusicSources
import `in`.melo.data.youtube.parseVideoResults
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Music source modes and YouTube's video search results (contracts with doubles). */
class SourcesAndVideosTest {
    @Test fun sourceModesKeepDeviceFilesAndMelosPlaylists() {
        val spotify = "spotify:track:abc"
        val youTube = listOf("ytm:track:xyz", "ytm:playlist:PL1", "ytm:album:MPREb", "ytm:artist:UC1")
        val device = listOf("local:track:9", "local:playlist:mix")
        assertTrue((youTube + spotify + device).all { MusicSources.allows(MusicSources.BOTH, it) })
        assertTrue(MusicSources.allows(MusicSources.SPOTIFY, spotify))
        assertFalse(youTube.any { MusicSources.allows(MusicSources.SPOTIFY, it) })
        assertTrue(youTube.all { MusicSources.allows(MusicSources.YOUTUBE, it) })
        assertFalse(MusicSources.allows(MusicSources.YOUTUBE, spotify))
        assertTrue(device.all { MusicSources.allows(MusicSources.SPOTIFY, it) && MusicSources.allows(MusicSources.YOUTUBE, it) })
    }

    @Test fun videoResultsBecomePlayableTracksWithoutLiveStreams() {
        fun runs(text: String) = JSONObject().put("runs", JSONArray().put(JSONObject().put("text", text)))
        fun video(id: String, title: String, channel: String, length: String?) = JSONObject().put("videoRenderer", JSONObject()
            .put("videoId", id).put("title", runs(title)).put("ownerText", runs(channel))
            .apply { if (length != null) put("lengthText", JSONObject().put("simpleText", length)) })
        val response = JSONObject().put("contents", JSONArray()
            .put(video("wbO3p7_Mf30", "087. Hopes And Dreams (UNDERTALE Soundtrack)", "Toby Fox", "3:05"))
            .put(video("live1", "24/7 lofi radio", "Lofi Girl", null))
            .put(video("lNQ0pQpNfLs", "Undertale - Hopes and Dreams Mashup", "Taspell", "1:08:11")))
        val tracks = parseVideoResults(response)
        assertEquals(listOf("ytm:track:wbO3p7_Mf30", "ytm:track:lNQ0pQpNfLs"), tracks.map(Music::id))
        assertEquals("Toby Fox", tracks[0].subtitle)
        assertEquals(185_000, tracks[0].durationMs)
        assertEquals(4_091_000, tracks[1].durationMs)
        assertEquals(MusicKind.Track, tracks[0].kind)
    }
}
