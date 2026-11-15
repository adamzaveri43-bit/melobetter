package `in`.melo.data.preferences

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject

/** Every user-facing preference of the app. Never holds account material. */
data class AppSettings(
    val themeMode: String = THEME_DARK,
    val language: String = LANGUAGE_SYSTEM,
    val useSmoothCorners: Boolean = true,
    val disableBlur: Boolean = true,
    val showScrollbar: Boolean = true,
    val playerTheme: String = ThemePreference.GLOBAL,
    val palette: String = AlbumArtPaletteStyle.default.storageKey,
    val carouselStyle: String = CarouselStyle.NO_PEEK,
    val showPlayerFileInfo: Boolean = true,
    val showQualityInLists: Boolean = false,
    val collagePattern: String = CollagePattern.default.storageKey,
    val collageAutoRotate: Boolean = false,
    val navBarStyle: String = NavBarStyle.FULL_WIDTH,
    val navBarCompact: Boolean = false,
    val navBarCornerRadius: Int = 0,
    val immersiveLyrics: Boolean = false,
    val immersiveLyricsTimeoutMs: Long = 4000L,
    val lyricsFallback: Boolean = true,
    val defaultTab: String = TAB_HOME,
    val keepPlayingInBackground: Boolean = true,
    val pauseOnVolumeZero: Boolean = false,
    val resumeOnHeadphones: Boolean = false,
    val persistentShuffle: Boolean = false,
    val showQueueHistory: Boolean = true,
    val audioCacheLimitMb: Int = 4096,
    val preloadTracks: Int = 2,
    val preloadEntireQueue: Boolean = false,
    val preloadOnMobile: Boolean = false,
    val cachePlayedTracks: Boolean = true,
    val preloadUpcomingTracks: Boolean = true,
    val offlineMode: Boolean = false,
    val tapBackgroundClosesPlayer: Boolean = false,
    val hapticsEnabled: Boolean = true,
    /** Source badge on each track of a multi-service playlist. */
    val showSourceBadges: Boolean = true,
    /** Smaller rows and spacing in track and library lists. */
    val compactLists: Boolean = false,
    /** Home shelves: order, visibility, layout and size, as [HomeLayout] encodes them. Empty: defaults. */
    val homeShelves: String = "",
    /** Album and playlist page buttons: order and which ones are in the bar, as [DetailActions] encodes them. */
    val albumActions: String = "",
    val playlistActions: String = "",
    /** What swiping a track row does, as [`in`.melo.presentation.components.TrackSwipeAction] keys. */
    val swipeRight: String = "queue",
    val swipeLeft: String = "none",
    /** [`in`.melo.data.AudioQuality] keys for streaming on Wi-Fi, on mobile data, and for downloads. */
    val streamingQualityWifi: String = "auto",
    val streamingQualityMobile: String = "auto",
    val downloadQuality: String = "very_high",
    /** Loudness normalization of Spotify and YouTube Music tracks from the loudness each service publishes. */
    val audioNormalization: Boolean = false,
    /** The ⋮ button at the top right of the full player. */
    val showPlayerMenuButton: Boolean = true,
    /** Horizontal swipe on the mini player: [MINI_SWIPE_TRACKS], [MINI_SWIPE_DISMISS] or [MINI_SWIPE_OFF]. */
    val miniPlayerSwipe: String = MINI_SWIPE_TRACKS,
    /** The Spotify / YouTube / Device choice above search results. Hidden: every search reads all sources. */
    val showSearchSources: Boolean = false,
    /** Firebase usage analytics is enabled by default and can be disabled in Privacy settings. */
    val analyticsEnabled: Boolean = true,
    /** Full player: [PlayerBackground] key, [PlayerArtworkSize] key, and which top bar buttons show. */
    val playerBackground: String = PlayerBackground.SOLID,
    val playerArtworkSize: String = PlayerArtworkSize.LARGE,
    val showPlayerCastButton: Boolean = true,
    val showPlayerQueueButton: Boolean = true,
    /** Mini player: [MiniArtworkShape] key, its skip buttons, and a progress line along its bottom edge. */
    val miniArtworkShape: String = MiniArtworkShape.CIRCLE,
    val miniShowPrevious: Boolean = true,
    val miniShowNext: Boolean = true,
    val miniShowProgress: Boolean = false,
    /** Lyrics screen: [PlayerBackground] key, [LyricsTextSize] key, and bold text. */
    val lyricsBackground: String = PlayerBackground.SOLID,
    val lyricsTextSize: String = LyricsTextSize.MEDIUM,
    val lyricsBold: Boolean = false,
    /** [`in`.melo.ui.theme.UiStyle]: Material You, or Apple Music's player with Liquid Glass surfaces. */
    val uiStyle: String = `in`.melo.ui.theme.UiStyle.MATERIAL,
    /** [MusicSources]: the services search, the library and the home screen read from. */
    val musicSources: String = MusicSources.BOTH,
) {
    fun actions(page: DetailPage): List<ActionSlot> =
        DetailActions.decode(page, if (page == DetailPage.ALBUM) albumActions else playlistActions)
    val homeLayout: List<ShelfConfig> get() = HomeLayout.decode(homeShelves)
    fun toJson(): JSONObject = JSONObject()
        .put("themeMode", themeMode).put("language", language).put("useSmoothCorners", useSmoothCorners)
        .put("disableBlur", disableBlur).put("showScrollbar", showScrollbar).put("playerTheme", playerTheme)
        .put("palette", palette).put("carouselStyle", carouselStyle).put("showPlayerFileInfo", showPlayerFileInfo)
        .put("showQualityInLists", showQualityInLists).put("collagePattern", collagePattern)
        .put("collageAutoRotate", collageAutoRotate).put("navBarStyle", navBarStyle).put("navBarCompact", navBarCompact)
        .put("navBarCornerRadius", navBarCornerRadius).put("immersiveLyrics", immersiveLyrics)
        .put("immersiveLyricsTimeoutMs", immersiveLyricsTimeoutMs).put("lyricsFallback", lyricsFallback)
        .put("defaultTab", defaultTab).put("keepPlayingInBackground", keepPlayingInBackground)
        .put("pauseOnVolumeZero", pauseOnVolumeZero).put("resumeOnHeadphones", resumeOnHeadphones)
        .put("persistentShuffle", persistentShuffle).put("showQueueHistory", showQueueHistory)
        .put("audioCacheLimitMb", audioCacheLimitMb).put("preloadTracks", preloadTracks)
        .put("preloadEntireQueue", preloadEntireQueue).put("preloadOnMobile", preloadOnMobile)
        .put("cachePlayedTracks", cachePlayedTracks).put("preloadUpcomingTracks", preloadUpcomingTracks)
        .put("offlineMode", offlineMode)
        .put("tapBackgroundClosesPlayer", tapBackgroundClosesPlayer).put("hapticsEnabled", hapticsEnabled)
        .put("showSourceBadges", showSourceBadges).put("compactLists", compactLists).put("homeShelves", homeShelves)
        .put("albumActions", albumActions).put("playlistActions", playlistActions)
        .put("swipeRight", swipeRight).put("swipeLeft", swipeLeft)
        .put("streamingQualityWifi", streamingQualityWifi).put("streamingQualityMobile", streamingQualityMobile)
        .put("downloadQuality", downloadQuality).put("audioNormalization", audioNormalization)
        .put("showPlayerMenuButton", showPlayerMenuButton).put("miniPlayerSwipe", miniPlayerSwipe)
        .put("showSearchSources", showSearchSources).put("analyticsEnabled", analyticsEnabled)
        .put("playerBackground", playerBackground).put("playerArtworkSize", playerArtworkSize)
        .put("showPlayerCastButton", showPlayerCastButton).put("showPlayerQueueButton", showPlayerQueueButton)
        .put("miniArtworkShape", miniArtworkShape).put("miniShowPrevious", miniShowPrevious).put("miniShowNext", miniShowNext)
        .put("miniShowProgress", miniShowProgress).put("lyricsBackground", lyricsBackground)
        .put("lyricsTextSize", lyricsTextSize).put("lyricsBold", lyricsBold).put("uiStyle", uiStyle).put("musicSources", musicSources)

    companion object {
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val LANGUAGE_SYSTEM = "system"
        const val TAB_HOME = "home"
        const val TAB_SEARCH = "search"
        const val TAB_LIBRARY = "library"
        const val MINI_SWIPE_TRACKS = "tracks"
        const val MINI_SWIPE_DISMISS = "dismiss"
        const val MINI_SWIPE_OFF = "off"

        /** Unknown or missing fields keep their default, so old backups stay valid. */
        fun fromJson(json: JSONObject): AppSettings {
            val d = AppSettings()
            val savedPreloadTracks = if (json.optBoolean("preloadEntireQueue", false)) 10
                else json.optInt("preloadTracks", d.preloadTracks)
            return AppSettings(
                themeMode = json.optString("themeMode", d.themeMode),
                language = json.optString("language", d.language),
                useSmoothCorners = json.optBoolean("useSmoothCorners", d.useSmoothCorners),
                disableBlur = json.optBoolean("disableBlur", d.disableBlur),
                showScrollbar = json.optBoolean("showScrollbar", d.showScrollbar),
                playerTheme = json.optString("playerTheme", d.playerTheme),
                palette = json.optString("palette", d.palette),
                carouselStyle = json.optString("carouselStyle", d.carouselStyle),
                showPlayerFileInfo = json.optBoolean("showPlayerFileInfo", d.showPlayerFileInfo),
                showQualityInLists = json.optBoolean("showQualityInLists", d.showQualityInLists),
                collagePattern = json.optString("collagePattern", d.collagePattern),
                collageAutoRotate = json.optBoolean("collageAutoRotate", d.collageAutoRotate),
                navBarStyle = json.optString("navBarStyle", d.navBarStyle),
                navBarCompact = json.optBoolean("navBarCompact", d.navBarCompact),
                navBarCornerRadius = sanitizeNavBarCornerRadius(json.optInt("navBarCornerRadius", d.navBarCornerRadius)),
                immersiveLyrics = json.optBoolean("immersiveLyrics", d.immersiveLyrics),
                immersiveLyricsTimeoutMs = json.optLong("immersiveLyricsTimeoutMs", d.immersiveLyricsTimeoutMs),
                lyricsFallback = json.optBoolean("lyricsFallback", d.lyricsFallback),
                defaultTab = json.optString("defaultTab", d.defaultTab),
                keepPlayingInBackground = json.optBoolean("keepPlayingInBackground", d.keepPlayingInBackground),
                pauseOnVolumeZero = json.optBoolean("pauseOnVolumeZero", d.pauseOnVolumeZero),
                resumeOnHeadphones = json.optBoolean("resumeOnHeadphones", d.resumeOnHeadphones),
                persistentShuffle = json.optBoolean("persistentShuffle", d.persistentShuffle),
                showQueueHistory = json.optBoolean("showQueueHistory", d.showQueueHistory),
                audioCacheLimitMb = json.optInt("audioCacheLimitMb", d.audioCacheLimitMb).coerceIn(512, 32768),
                preloadTracks = savedPreloadTracks.coerceIn(1, 20),
                preloadEntireQueue = false,
                preloadOnMobile = json.optBoolean("preloadOnMobile", d.preloadOnMobile),
                cachePlayedTracks = json.optBoolean("cachePlayedTracks", d.cachePlayedTracks),
                preloadUpcomingTracks = json.optBoolean("preloadUpcomingTracks", savedPreloadTracks != 0),
                offlineMode = json.optBoolean("offlineMode", d.offlineMode),
                tapBackgroundClosesPlayer = json.optBoolean("tapBackgroundClosesPlayer", d.tapBackgroundClosesPlayer),
                hapticsEnabled = json.optBoolean("hapticsEnabled", d.hapticsEnabled),
                showSourceBadges = json.optBoolean("showSourceBadges", d.showSourceBadges),
                compactLists = json.optBoolean("compactLists", d.compactLists),
                homeShelves = json.optString("homeShelves", d.homeShelves),
                albumActions = json.optString("albumActions", d.albumActions),
                playlistActions = json.optString("playlistActions", d.playlistActions),
                swipeRight = json.optString("swipeRight", d.swipeRight),
                swipeLeft = json.optString("swipeLeft", d.swipeLeft),
                streamingQualityWifi = json.optString("streamingQualityWifi", d.streamingQualityWifi),
                streamingQualityMobile = json.optString("streamingQualityMobile", d.streamingQualityMobile),
                downloadQuality = json.optString("downloadQuality", d.downloadQuality),
                audioNormalization = json.optBoolean("audioNormalization", d.audioNormalization),
                showPlayerMenuButton = json.optBoolean("showPlayerMenuButton", d.showPlayerMenuButton),
                miniPlayerSwipe = json.optString("miniPlayerSwipe", d.miniPlayerSwipe)
                    .takeIf { it in setOf(MINI_SWIPE_TRACKS, MINI_SWIPE_DISMISS, MINI_SWIPE_OFF) } ?: d.miniPlayerSwipe,
                showSearchSources = json.optBoolean("showSearchSources", d.showSearchSources),
                analyticsEnabled = json.optBoolean("analyticsEnabled", d.analyticsEnabled),
                playerBackground = json.optString("playerBackground", d.playerBackground).takeIf { it in PlayerBackground.ALL } ?: d.playerBackground,
                playerArtworkSize = json.optString("playerArtworkSize", d.playerArtworkSize).takeIf { it in PlayerArtworkSize.ALL } ?: d.playerArtworkSize,
                showPlayerCastButton = json.optBoolean("showPlayerCastButton", d.showPlayerCastButton),
                showPlayerQueueButton = json.optBoolean("showPlayerQueueButton", d.showPlayerQueueButton),
                miniArtworkShape = json.optString("miniArtworkShape", d.miniArtworkShape).takeIf { it in MiniArtworkShape.ALL } ?: d.miniArtworkShape,
                miniShowPrevious = json.optBoolean("miniShowPrevious", d.miniShowPrevious),
                miniShowNext = json.optBoolean("miniShowNext", d.miniShowNext),
                miniShowProgress = json.optBoolean("miniShowProgress", d.miniShowProgress),
                lyricsBackground = json.optString("lyricsBackground", d.lyricsBackground).takeIf { it in PlayerBackground.ALL } ?: d.lyricsBackground,
                lyricsTextSize = json.optString("lyricsTextSize", d.lyricsTextSize).takeIf { it in LyricsTextSize.ALL } ?: d.lyricsTextSize,
                lyricsBold = json.optBoolean("lyricsBold", d.lyricsBold),
                uiStyle = json.optString("uiStyle", d.uiStyle).takeIf { it in `in`.melo.ui.theme.UiStyle.ALL } ?: d.uiStyle,
                musicSources = json.optString("musicSources", d.musicSources).takeIf { it in MusicSources.ALL } ?: d.musicSources,
            )
        }
    }
}

class SettingsStore private constructor(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    private val mutable = MutableStateFlow(read())
    val settings: StateFlow<AppSettings> = mutable.asStateFlow()

    private fun read(): AppSettings = preferences.getString(KEY, null)?.let { saved ->
        try { AppSettings.fromJson(JSONObject(saved)) } catch (_: org.json.JSONException) { null }
    } ?: AppSettings()

    fun update(change: (AppSettings) -> AppSettings) {
        mutable.update(change)
        preferences.edit { putString(KEY, mutable.value.toJson().toString()) }
    }

    companion object {
        private const val KEY = "settings"
        @Volatile private var instance: SettingsStore? = null
        /** Shared with the playback service, which runs in the same process. */
        fun get(context: Context): SettingsStore = instance ?: synchronized(this) {
            instance ?: SettingsStore(context).also { instance = it }
        }
    }
}
