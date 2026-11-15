package `in`.melo.data.model

import androidx.compose.runtime.Immutable

@Immutable
enum class SearchFilterType {
    ALL,
    SONGS,
    ALBUMS,
    ARTISTS,
    PLAYLISTS,
    /** All of YouTube's videos, beyond YouTube Music's catalogue: a search of its own. */
    VIDEOS
}
