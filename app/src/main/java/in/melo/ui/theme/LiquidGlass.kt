package `in`.melo.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/** Which look the app uses: Material You, or Apple Music's player with Liquid Glass surfaces. */
object UiStyle {
    const val MATERIAL = "material"
    const val GLASS = "glass"
    val ALL = listOf(MATERIAL, GLASS)
}

/**
 * The screens' content, as a blur source for the glass surfaces drawn over it (navigation bar, mini
 * player). Null in the Material style.
 */
val LocalGlassHaze = staticCompositionLocalOf<HazeState?> { null }

/**
 * A Liquid Glass surface. With [haze], what is behind is blurred (Android 12 and later; earlier
 * versions get a denser translucent fill instead), tinted with [tint] at [fillAlpha], with
 * a fine grain. Without it, the fill alone is translucent: use that over content that is already
 * soft, such as the player's blurred artwork. Both get a specular sheen over the upper half and a
 * thin rim of light brighter at the top-left and bottom-right edges, as light catches the edges of
 * a pane of glass. Content drawn inside stays above the sheen.
 */
fun Modifier.liquidGlass(
    shape: Shape,
    tint: Color = Color.White,
    fillAlpha: Float = 0.16f,
    elevation: Dp = 0.dp,
    haze: HazeState? = null,
    blurRadius: Dp = 26.dp,
): Modifier = this
    .then(if (elevation > 0.dp) Modifier.shadow(elevation, shape, clip = false,
        ambientColor = Color.Black.copy(alpha = 0.25f), spotColor = Color.Black.copy(alpha = 0.35f)) else Modifier)
    .clip(shape)
    .then(
        if (haze != null) Modifier.hazeEffect(haze) {
            this.blurRadius = blurRadius
            backgroundColor = tint.copy(alpha = 1f)
            tints = listOf(HazeTint(tint.copy(alpha = fillAlpha)))
            noiseFactor = 0.06f
        } else Modifier.background(Brush.verticalGradient(listOf(
            tint.copy(alpha = (fillAlpha + 0.06f).coerceAtMost(1f)), tint.copy(alpha = fillAlpha))))
    )
    .drawWithContent {
        // The sheen under the content: the light the upper curve of the glass reflects.
        drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.16f), Color.Transparent), endY = size.height * 0.55f))
        drawContent()
    }
    .border(
        width = 0.8.dp,
        brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.6f), Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.3f))),
        shape = shape,
    )
