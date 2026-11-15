package `in`.melo.data.preferences

/** What the full player and the lyrics screen draw behind their content. */
object PlayerBackground {
    /** The sheet's own colour, from the artwork or the app theme. */
    const val SOLID = "solid"
    /** The artwork's colours, from the top down. */
    const val GRADIENT = "gradient"
    /** The artwork itself, enlarged and blurred. */
    const val BLUR = "blur"
    /** Colour fields from the artwork drifting slowly. */
    const val ANIMATED = "animated"
    val ALL = listOf(SOLID, GRADIENT, BLUR, ANIMATED)
}

/** How much of the player's width the artwork takes. */
object PlayerArtworkSize {
    const val SMALL = "small"
    const val MEDIUM = "medium"
    const val LARGE = "large"
    val ALL = listOf(SMALL, MEDIUM, LARGE)
    fun fraction(key: String): Float = when (key) { SMALL -> 0.72f; MEDIUM -> 0.86f; else -> 1f }
}

/** The cover's shape in the mini player. */
object MiniArtworkShape {
    const val CIRCLE = "circle"
    const val ROUNDED = "rounded"
    const val SQUARE = "square"
    val ALL = listOf(CIRCLE, ROUNDED, SQUARE)
}

/** Size of the lyrics text, as a factor of the player's title style. */
object LyricsTextSize {
    const val SMALL = "small"
    const val MEDIUM = "medium"
    const val LARGE = "large"
    const val HUGE = "huge"
    val ALL = listOf(SMALL, MEDIUM, LARGE, HUGE)
    fun scale(key: String): Float = when (key) { SMALL -> 0.85f; LARGE -> 1.25f; HUGE -> 1.5f; else -> 1f }
}

/**
 * Which services the app reads from: search, the library and the home screen show only what they
 * allow. Device files and Melo's own playlists are always shown. A Spotify track found while only
 * YouTube Music is allowed is never shown, so it never needs to be played through YouTube Music.
 */
object MusicSources {
    const val BOTH = "both"
    const val SPOTIFY = "spotify"
    const val YOUTUBE = "youtube"
    val ALL = listOf(BOTH, SPOTIFY, YOUTUBE)

    fun allowsSpotify(mode: String) = mode != YOUTUBE
    fun allowsYouTube(mode: String) = mode != SPOTIFY

    /** Whether an item of any kind (track, album, artist, playlist) comes from a service [mode] allows. */
    fun allows(mode: String, id: String): Boolean = when {
        mode == BOTH -> true
        id.startsWith("ytm:") -> allowsYouTube(mode)
        id.startsWith("local:") || id.startsWith(`in`.melo.data.LOCAL_PLAYLIST_PREFIX) -> true
        else -> allowsSpotify(mode)
    }
}
