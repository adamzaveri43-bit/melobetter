package `in`.melo.presentation.components.player

import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import `in`.melo.R
import `in`.melo.data.model.Song
import `in`.melo.presentation.components.SmartImage
import `in`.melo.ui.theme.liquidGlass

private val Content = Color.White
private val Secondary = Color.White.copy(alpha = 0.62f)

/**
 * The full player as Apple Music lays it out, with Liquid Glass controls: the artwork blurred and
 * drifting behind everything, a large rounded cover that settles back when paused, the title on the
 * left with glass buttons on the right, a thin scrubber with the remaining time, plain transport
 * icons and a glass bar for lyrics, shuffle, repeat, output and the queue. Text is white over a
 * darkened backdrop, in both the light and dark themes.
 */
@Composable
internal fun AppleMusicPlayerLayout(
    song: Song,
    isPlayingProvider: () -> Boolean,
    positionProvider: () -> Long,
    durationProvider: () -> Long,
    isFavoriteProvider: () -> Boolean,
    isShuffleEnabled: Boolean,
    repeatMode: Int,
    onCollapse: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onFavorite: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onLyrics: () -> Unit,
    onCast: () -> Unit,
    onQueue: () -> Unit,
    onOptions: () -> Unit,
    onArtist: () -> Unit,
    onAlbum: () -> Unit,
) {
    val artwork = song.albumArtUriString?.takeIf { it.isNotBlank() }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AppleBackdrop(artwork)
        Column(
            Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // The grabber: a tap closes the player, as a drag down does.
            Box(
                Modifier.padding(top = 8.dp).size(width = 64.dp, height = 24.dp)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onCollapse),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(width = 38.dp, height = 5.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.45f)))
            }
            // The cover takes what the controls leave, as a square.
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                val side = minOf(maxWidth, maxHeight - 24.dp).coerceAtLeast(0.dp)
                AppleArtwork(artwork, isPlayingProvider(), Modifier.size(side), onAlbum, onNext, onPrevious)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(song.title, color = Content, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        song.displayArtist, color = Secondary, fontSize = 19.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onArtist),
                    )
                }
                GlassIconButton(if (isFavoriteProvider()) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    stringResource(R.string.glass_favorite), onFavorite)
                Spacer(Modifier.width(10.dp))
                GlassIconButton(Icons.Rounded.MoreHoriz, stringResource(R.string.player_cd_more_options), onOptions)
            }
            Spacer(Modifier.height(22.dp))
            AppleScrubber(positionProvider, durationProvider, onSeek)
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                TransportButton(Icons.Rounded.FastRewind, stringResource(R.string.glass_previous), 44.dp, onPrevious)
                TransportButton(if (isPlayingProvider()) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    stringResource(if (isPlayingProvider()) R.string.melo_pause else R.string.common_play), 64.dp, onPlayPause)
                TransportButton(Icons.Rounded.FastForward, stringResource(R.string.glass_next), 44.dp, onNext)
            }
            Spacer(Modifier.height(22.dp))
            // The glass bar of secondary actions.
            Row(
                Modifier.fillMaxWidth().height(56.dp).liquidGlass(RoundedCornerShape(28.dp), fillAlpha = 0.12f),
                horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically,
            ) {
                BarButton(painterResource(R.drawable.rounded_lyrics_24), stringResource(R.string.glass_lyrics), false, onLyrics)
                BarButton(Icons.Rounded.Shuffle, stringResource(R.string.glass_shuffle), isShuffleEnabled, onShuffle)
                BarButton(if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                    stringResource(R.string.glass_repeat), repeatMode != Player.REPEAT_MODE_OFF, onRepeat)
                BarButton(Icons.Rounded.Cast, stringResource(R.string.player_cd_cast), false, onCast)
                BarButton(Icons.AutoMirrored.Rounded.QueueMusic, stringResource(R.string.player_cd_open_queue), false, onQueue)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

/** The artwork, enlarged, blurred and drifting slowly, under a dark veil that keeps white text readable. */
@Composable
private fun AppleBackdrop(artwork: String?) {
    val blurs = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val transition = rememberInfiniteTransition(label = "appleBackdrop")
    val drift by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(20_000, easing = LinearEasing), RepeatMode.Reverse), label = "drift")
    if (artwork != null) SmartImage(
        model = artwork,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
            .graphicsLayer {
                val scale = 1.6f + 0.15f * drift
                scaleX = scale; scaleY = scale
                rotationZ = -8f + 16f * drift
                translationX = size.width * 0.06f * (drift - 0.5f)
            }
            .then(if (blurs) Modifier.blur(90.dp) else Modifier),
    )
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = if (blurs) 0.38f else 0.62f)))
}

/** A large rounded cover with a soft shadow; it settles back while paused. A horizontal swipe skips. */
@Composable
private fun AppleArtwork(
    artwork: String?, playing: Boolean, modifier: Modifier,
    onTap: () -> Unit, onNext: () -> Unit, onPrevious: () -> Unit,
) {
    val scale by animateFloatAsState(if (playing) 1f else 0.82f, spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow), label = "coverScale")
    var drag by remember { mutableFloatStateOf(0f) }
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale; translationX = drag * 0.35f }
            .shadow(if (playing) 28.dp else 12.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(shape)
            .background(Color.White.copy(alpha = 0.08f))
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val threshold = size.width * 0.22f
                        if (drag < -threshold) onNext() else if (drag > threshold) onPrevious()
                        drag = 0f
                    },
                    onDragCancel = { drag = 0f },
                ) { change, amount -> change.consume(); drag += amount }
            }
            .pointerInput(Unit) { detectTapGestures(onTap = { onTap() }) },
    ) {
        if (artwork != null) SmartImage(model = artwork, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
    }
}

/**
 * A thin scrubber that thickens under the finger, elapsed time under its start and remaining time
 * under its end. Reading the position here keeps the rest of the player from recomposing each tick.
 */
@Composable
private fun AppleScrubber(positionProvider: () -> Long, durationProvider: () -> Long, onSeek: (Long) -> Unit) {
    var dragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    val duration = durationProvider().coerceAtLeast(0L)
    val played = if (dragging) dragFraction else if (duration > 0) (positionProvider().toFloat() / duration).coerceIn(0f, 1f) else 0f
    val thickness by animateDpAsState(if (dragging) 12.dp else 7.dp, tween(160, easing = FastOutSlowInEasing), label = "scrubber")
    Column(Modifier.fillMaxWidth()) {
        Box(
            Modifier.fillMaxWidth().height(24.dp)
                .pointerInput(duration) {
                    detectTapGestures { offset -> if (duration > 0) onSeek((offset.x / size.width).coerceIn(0f, 1f).times(duration).toLong()) }
                }
                .pointerInput(duration) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset -> dragging = true; dragFraction = (offset.x / size.width).coerceIn(0f, 1f) },
                        onDragEnd = { if (duration > 0) onSeek((dragFraction * duration).toLong()); dragging = false },
                        onDragCancel = { dragging = false },
                    ) { change, amount -> change.consume(); dragFraction = (dragFraction + amount / size.width).coerceIn(0f, 1f) }
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(Modifier.fillMaxWidth().height(thickness).clip(CircleShape).background(Color.White.copy(alpha = 0.22f))) {
                Box(Modifier.fillMaxWidth(played).fillMaxSize().background(Color.White.copy(alpha = if (dragging) 1f else 0.82f)))
            }
        }
        val elapsed = (played * duration).toLong()
        Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Text(clock(elapsed), color = Secondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("-" + clock((duration - elapsed).coerceAtLeast(0)), color = Secondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun clock(ms: Long): String {
    val seconds = ms / 1000
    return if (seconds >= 3600) "%d:%02d:%02d".format(seconds / 3600, seconds / 60 % 60, seconds % 60) else "%d:%02d".format(seconds / 60, seconds % 60)
}

/** A plain white transport icon that shrinks and gains a faint glass halo while pressed. */
@Composable
private fun TransportButton(icon: ImageVector, description: String, size: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.86f else 1f, spring(dampingRatio = 0.5f), label = "transport")
    Box(
        Modifier.size(size + 28.dp).graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(Color.White.copy(alpha = if (pressed) 0.12f else 0f))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, description, Modifier.size(size), tint = Content)
    }
}

@Composable
private fun GlassIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        Modifier.size(38.dp).liquidGlass(CircleShape, fillAlpha = 0.14f).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, description, Modifier.size(22.dp), tint = Content)
    }
}

@Composable
private fun BarButton(icon: ImageVector, description: String, active: Boolean, onClick: () -> Unit) =
    BarButtonFrame(active, onClick) { Icon(icon, description, Modifier.size(24.dp), tint = if (active) Color.Black else Content) }

@Composable
private fun BarButton(icon: androidx.compose.ui.graphics.painter.Painter, description: String, active: Boolean, onClick: () -> Unit) =
    BarButtonFrame(active, onClick) { Icon(icon, description, Modifier.size(24.dp), tint = if (active) Color.Black else Content) }

/** An active toggle (shuffle, repeat) is a white disc, as Apple Music marks it. */
@Composable
private fun BarButtonFrame(active: Boolean, onClick: () -> Unit, icon: @Composable () -> Unit) {
    Box(
        Modifier.size(42.dp).clip(CircleShape).background(if (active) Color.White.copy(alpha = 0.9f) else Color.Transparent).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { icon() }
}
