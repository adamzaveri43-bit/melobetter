package `in`.melo.data.preferences

import org.json.JSONArray
import org.json.JSONObject

/** The shelves the home screen can show. The user's playlists are not among them: they live in the library. */
enum class HomeShelf(val key: String, val styles: List<ShelfStyle>, val counts: Boolean = true) {
    PINS("pins", listOf(ShelfStyle.COLLAGE, ShelfStyle.PILLS, ShelfStyle.CAROUSEL, ShelfStyle.GRID, ShelfStyle.LIST)),
    RECENT("recent", listOf(ShelfStyle.CAROUSEL, ShelfStyle.LARGE, ShelfStyle.GRID, ShelfStyle.LIST, ShelfStyle.PILLS)),
    /**
     * Suggested music, as YouTube Music and Spotify open on: one row per suggestion shelf. Track
     * shelves always use the quick picks layout; the style applies to the cover shelves.
     */
    FEED("feed", listOf(ShelfStyle.CAROUSEL, ShelfStyle.LARGE)),
    DOWNLOADS("downloads", listOf(ShelfStyle.CAROUSEL, ShelfStyle.LARGE, ShelfStyle.GRID, ShelfStyle.LIST, ShelfStyle.PILLS)),
    LIKED("liked", listOf(ShelfStyle.CARD), counts = false),
    ALBUMS("albums", listOf(ShelfStyle.CAROUSEL, ShelfStyle.LARGE, ShelfStyle.GRID, ShelfStyle.LIST, ShelfStyle.PILLS)),
    ARTISTS("artists", listOf(ShelfStyle.CAROUSEL, ShelfStyle.LARGE, ShelfStyle.GRID, ShelfStyle.LIST, ShelfStyle.PILLS)),
    LOCAL_FILES("local_files", listOf(ShelfStyle.LIST, ShelfStyle.CAROUSEL, ShelfStyle.GRID, ShelfStyle.PILLS)),
}

/** How a shelf lays out its items. */
enum class ShelfStyle(val key: String) {
    /** Covers in a horizontal row, title under each cover. */
    CAROUSEL("carousel"),
    /** The same row with larger covers. */
    LARGE("large"),
    /** Two columns of covers. */
    GRID("grid"),
    /** Rows with a small cover, one item per line. */
    LIST("list"),
    /** Two columns of rounded pills tinted from each cover. */
    PILLS("pills"),
    /** Pins only: the artwork collage, then the pills. */
    COLLAGE("collage"),
    /** Liked songs only: the card with its play button. */
    CARD("card"),
}

data class ShelfConfig(val shelf: HomeShelf, val visible: Boolean, val style: ShelfStyle, val count: Int, val showTitle: Boolean = true)

/**
 * Home screen layout: shelves in display order. Unknown shelves (the playlist shelves of earlier
 * versions) are dropped; missing ones get their defaults, after the shelf they follow by default.
 */
object HomeLayout {
    val COUNTS = listOf(4, 6, 10, 20, 50)

    val DEFAULT: List<ShelfConfig> = listOf(
        ShelfConfig(HomeShelf.PINS, true, ShelfStyle.PILLS, 6),
        ShelfConfig(HomeShelf.RECENT, true, ShelfStyle.CAROUSEL, 6),
        ShelfConfig(HomeShelf.FEED, true, ShelfStyle.CAROUSEL, 20),
        ShelfConfig(HomeShelf.DOWNLOADS, true, ShelfStyle.CAROUSEL, 6),
        ShelfConfig(HomeShelf.LIKED, true, ShelfStyle.CARD, 1),
        ShelfConfig(HomeShelf.ALBUMS, true, ShelfStyle.CAROUSEL, 20),
        ShelfConfig(HomeShelf.ARTISTS, true, ShelfStyle.CAROUSEL, 20),
        ShelfConfig(HomeShelf.LOCAL_FILES, false, ShelfStyle.LIST, 6),
    )

    fun decode(encoded: String): List<ShelfConfig> {
        if (encoded.isBlank()) return DEFAULT
        val read = try {
            val array = JSONArray(encoded)
            (0 until array.length()).mapNotNull { index ->
                val entry = array.optJSONObject(index) ?: return@mapNotNull null
                val shelf = HomeShelf.entries.firstOrNull { it.key == entry.optString("shelf") } ?: return@mapNotNull null
                val fallback = DEFAULT.first { it.shelf == shelf }
                val style = ShelfStyle.entries.firstOrNull { it.key == entry.optString("style") }?.takeIf { it in shelf.styles } ?: fallback.style
                ShelfConfig(shelf, entry.optBoolean("visible", fallback.visible), style,
                    entry.optInt("count", fallback.count).coerceIn(1, 50), entry.optBoolean("title", true))
            }.distinctBy { it.shelf }
        } catch (_: org.json.JSONException) { return DEFAULT }
        val result = read.toMutableList()
        DEFAULT.forEachIndexed { index, default ->
            if (result.any { it.shelf == default.shelf }) return@forEachIndexed
            val after = DEFAULT.take(index).lastOrNull { previous -> result.any { it.shelf == previous.shelf } }
            result.add(after?.let { previous -> result.indexOfFirst { it.shelf == previous.shelf } + 1 } ?: 0, default)
        }
        return result
    }

    fun encode(shelves: List<ShelfConfig>): String = JSONArray().apply {
        shelves.forEach { put(JSONObject().put("shelf", it.shelf.key).put("visible", it.visible).put("style", it.style.key).put("count", it.count).put("title", it.showTitle)) }
    }.toString()
}
