package `in`.melo.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import `in`.melo.ui.theme.GoogleSansRounded
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.melo.ui.theme.MeloLayout
import `in`.melo.data.Music
import `in`.melo.data.MusicKind
import `in`.melo.data.preferences.ShelfStyle
import `in`.melo.presentation.components.SmartImage
import `in`.melo.presentation.viewmodel.PlayerViewModel

@Composable
private fun Cover(item: Music, ui: PlayerViewModel, placeholder: Color, shape: androidx.compose.ui.graphics.Shape, modifier: Modifier) {
    Box(modifier.clip(shape).background(placeholder), contentAlignment = Alignment.Center) {
        if (item.id == ui.likedSongs.id) Icon(Icons.Rounded.Favorite, null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
        else if (item.imageUrl == null) Icon(Icons.Rounded.MusicNote, null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        else SmartImage(model = item.imageUrl, contentDescription = item.title, contentScale = ContentScale.Crop, shape = shape, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun CardTexts(item: Music, centered: Boolean, modifier: Modifier) {
    Column(modifier) {
        Text(
            item.title, Modifier.fillMaxWidth(),
            fontFamily = GoogleSansRounded, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 19.sp,
            color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium,
            maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = if (centered) TextAlign.Center else TextAlign.Start,
        )
        Text(
            item.subtitle.ifBlank { item.kind.label }, Modifier.fillMaxWidth().padding(top = 1.dp),
            fontFamily = GoogleSansRounded, fontWeight = FontWeight.Normal, fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall,
            maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = if (centered) TextAlign.Center else TextAlign.Start,
        )
    }
}

/** Artwork and metadata sit directly on the page, with no tinted card background. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ShelfCoverCard(item: Music, ui: PlayerViewModel, modifier: Modifier, onClick: () -> Unit, onLongClick: () -> Unit) {
    val round = item.kind == MusicKind.Artist
    val shape = MeloLayout.ArtworkShape
    Box(modifier.clip(shape).combinedClickable(onClick = onClick, onLongClick = onLongClick)) {
        Column {
            Cover(item, ui, MaterialTheme.colorScheme.surfaceContainerHighest, if (round) CircleShape else MeloLayout.ArtworkShape,
                Modifier.fillMaxWidth().aspectRatio(1f))
            CardTexts(item, round, Modifier.padding(top = 12.dp, bottom = 4.dp))
        }
    }
}

/** A quiet row with a small cover, title, and supporting metadata. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ShelfListRow(item: Music, ui: PlayerViewModel, onClick: () -> Unit, onLongClick: () -> Unit) {
    val shape = MeloLayout.ArtworkShape
    Box(Modifier.fillMaxWidth().padding(horizontal = MeloLayout.PageGutter).clip(shape).combinedClickable(onClick = onClick, onLongClick = onLongClick)) {
        Row(Modifier.padding(7.dp), verticalAlignment = Alignment.CenterVertically) {
            Cover(item, ui, MaterialTheme.colorScheme.surfaceContainerHighest,
                if (item.kind == MusicKind.Artist) CircleShape else MeloLayout.ArtworkShape, Modifier.size(58.dp))
            CardTexts(item, false, Modifier.weight(1f).padding(horizontal = 14.dp))
        }
    }
}

/**
 * Songs four to a column, in columns that scroll sideways, as YouTube Music lays out its quick
 * picks: a long list of suggestions that takes the height of four rows.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun QuickPicks(items: List<Music>, ui: PlayerViewModel, onClick: (Music) -> Unit, onLongClick: (Music) -> Unit) {
    val width = (androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp - MeloLayout.PageGutter * 2) * 0.9f
    LazyRow(contentPadding = PaddingValues(horizontal = MeloLayout.PageGutter), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items.chunked(QUICK_PICK_ROWS), key = { it.first().id }) { column ->
            Column(Modifier.width(width), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                column.forEach { item ->
                    val shape = MeloLayout.ArtworkShape
                    Row(
                        Modifier.fillMaxWidth().clip(shape).combinedClickable(onClick = { onClick(item) }, onLongClick = { onLongClick(item) }).padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Cover(item, ui, MaterialTheme.colorScheme.surfaceContainerHighest, shape, Modifier.size(52.dp))
                        CardTexts(item, false, Modifier.weight(1f).padding(horizontal = 12.dp))
                    }
                }
            }
        }
    }
}

private const val QUICK_PICK_ROWS = 4

/** A compact tile of Spotify's home grid: the cover flush with the start, the title beside it. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SpotifyTile(item: Music, ui: PlayerViewModel, modifier: Modifier, onClick: () -> Unit, onLongClick: () -> Unit) {
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier.height(56.dp).clip(shape).background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Cover(item, ui, MaterialTheme.colorScheme.surfaceContainerHighest, androidx.compose.ui.graphics.RectangleShape, Modifier.size(56.dp))
        Text(
            item.title, Modifier.weight(1f).padding(horizontal = 10.dp),
            fontFamily = GoogleSansRounded, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 16.sp,
            color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * One enlarged cover at a time: a swipe left or right moves to the next or previous item, whose
 * covers peek, smaller and dimmer, at the sides. A tap opens or plays the centred item.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CoverFlow(items: List<Music>, ui: PlayerViewModel, onClick: (Music) -> Unit, onLongClick: (Music) -> Unit) {
    val pager = androidx.compose.foundation.pager.rememberPagerState { items.size }
    val side = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp * 0.17f
    androidx.compose.foundation.pager.HorizontalPager(
        state = pager,
        contentPadding = PaddingValues(horizontal = side),
        pageSpacing = 8.dp,
        key = { items[it].id },
    ) { page ->
        val item = items[page]
        val distance = kotlin.math.abs((pager.currentPage - page) + pager.currentPageOffsetFraction).coerceIn(0f, 1f)
        Column(
            Modifier.fillMaxWidth().graphicsLayer {
                val scale = 1f - 0.14f * distance
                scaleX = scale; scaleY = scale; alpha = 1f - 0.45f * distance
            },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val shape = RoundedCornerShape(14.dp)
            Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(shape).combinedClickable(onClick = { onClick(item) }, onLongClick = { onLongClick(item) })) {
                Cover(item, ui, MaterialTheme.colorScheme.surfaceContainerHighest, shape, Modifier.fillMaxSize())
            }
            CardTexts(item, true, Modifier.padding(top = 14.dp))
        }
    }
}

/** Lays out a shelf's items in the chosen [style]. Pills are drawn by the caller. */
@Composable
internal fun ShelfItems(
    items: List<Music>,
    style: ShelfStyle,
    ui: PlayerViewModel,
    onClick: (Music) -> Unit,
    onLongClick: (Music) -> Unit,
    pill: @Composable (Music, Modifier) -> Unit,
) {
    when (style) {
        ShelfStyle.CAROUSEL, ShelfStyle.LARGE -> {
            val width: Dp = if (style == ShelfStyle.LARGE) 212.dp else 152.dp
            LazyRow(contentPadding = PaddingValues(horizontal = MeloLayout.PageGutter), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(items, key = Music::id, contentType = { it.kind }) { item ->
                    ShelfCoverCard(item, ui, Modifier.width(width), { onClick(item) }, { onLongClick(item) })
                }
            }
        }
        ShelfStyle.GRID -> Column(Modifier.padding(horizontal = MeloLayout.PageGutter), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    row.forEach { item -> ShelfCoverCard(item, ui, Modifier.weight(1f), { onClick(item) }, { onLongClick(item) }) }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        ShelfStyle.LIST -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { item -> ShelfListRow(item, ui, { onClick(item) }, { onLongClick(item) }) }
        }
        ShelfStyle.YOUTUBE_MUSIC -> LazyRow(contentPadding = PaddingValues(horizontal = MeloLayout.PageGutter), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(items.chunked(2), key = { it.first().id }) { column ->
                Column(Modifier.width(136.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    column.forEach { item -> ShelfCoverCard(item, ui, Modifier.fillMaxWidth(), { onClick(item) }, { onLongClick(item) }) }
                }
            }
        }
        ShelfStyle.SPOTIFY -> Column(Modifier.padding(horizontal = MeloLayout.PageGutter), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { item -> SpotifyTile(item, ui, Modifier.weight(1f), { onClick(item) }, { onLongClick(item) }) }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        ShelfStyle.COVER_FLOW -> CoverFlow(items, ui, onClick, onLongClick)
        else -> Column(Modifier.padding(horizontal = MeloLayout.PageGutter), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { item -> pill(item, Modifier.weight(1f)) }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
