package `in`.melo.data

import `in`.melo.data.preferences.AppSettings
import `in`.melo.data.preferences.HomeLayout
import `in`.melo.data.preferences.HomeShelf
import `in`.melo.data.preferences.LyricsTextSize
import `in`.melo.data.preferences.MiniArtworkShape
import `in`.melo.data.preferences.PlayerBackground
import `in`.melo.data.preferences.ShelfStyle
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

/** Artist release lists and the player and home customization settings (contracts with doubles). */
class CustomizationTest {
    private fun album(id: String, title: String, year: String) = Music(id, title, "Tyler, The Creator", MusicKind.Album, note = year)

    @Test fun versionsOfOneReleaseShowOnce() {
        val releases = listOf(
            album("spotify:album:igor1", "IGOR", "2019"),
            album("spotify:album:igor2", "IGOR", "2019"),
            album("spotify:album:cmiygl", "CALL ME IF YOU GET LOST", "2021"),
            album("spotify:album:estate", "CALL ME IF YOU GET LOST: The Estate Sale", "2023"),
            album("spotify:album:flower", "Flower Boy", "2017"),
        )
        // The first version listed is kept; editions with their own title stay.
        assertEquals(listOf("spotify:album:igor1", "spotify:album:cmiygl", "spotify:album:estate", "spotify:album:flower"),
            releases.distinctReleases().map(Music::id))
        // A re-release in another year is another entry; an artist or a track is never merged by title.
        val reissue = album("spotify:album:igor2029", "IGOR", "2029")
        assertEquals(2, listOf(releases[0], reissue).distinctReleases().size)
        val tracks = listOf(Music("t1", "IGOR'S THEME", "x", MusicKind.Track), Music("t2", "IGOR'S THEME", "x", MusicKind.Track))
        assertEquals(2, tracks.distinctReleases().size)
    }

    @Test fun playerCustomizationRoundTripsAndRejectsUnknownKeys() {
        val chosen = AppSettings(
            playerBackground = PlayerBackground.ANIMATED, playerArtworkSize = "medium", showPlayerCastButton = false,
            showPlayerQueueButton = false, miniArtworkShape = MiniArtworkShape.SQUARE, miniShowPrevious = false,
            miniShowNext = false, miniShowProgress = true, lyricsBackground = PlayerBackground.BLUR,
            lyricsTextSize = LyricsTextSize.HUGE, lyricsBold = true,
        )
        assertEquals(chosen, AppSettings.fromJson(chosen.toJson()))
        val unknown = AppSettings.fromJson(JSONObject().put("playerBackground", "neon").put("miniArtworkShape", "star").put("lyricsTextSize", "tiny"))
        assertEquals(AppSettings().playerBackground, unknown.playerBackground)
        assertEquals(AppSettings().miniArtworkShape, unknown.miniArtworkShape)
        assertEquals(AppSettings().lyricsTextSize, unknown.lyricsTextSize)
        // Backups made before these settings keep the previous look.
        assertEquals(AppSettings(), AppSettings.fromJson(JSONObject()))
    }

    @Test fun recentlyPlayedOffersTheThreeNewLayouts() {
        val recent = HomeLayout.DEFAULT.first { it.shelf == HomeShelf.RECENT }
        assertEquals(true, recent.visible)
        listOf(ShelfStyle.YOUTUBE_MUSIC, ShelfStyle.SPOTIFY, ShelfStyle.COVER_FLOW).forEach { style ->
            val stored = """[{"shelf":"recent","visible":true,"style":"${style.key}","count":20}]"""
            assertEquals(style, HomeLayout.decode(stored).first { it.shelf == HomeShelf.RECENT }.style)
        }
    }
}
