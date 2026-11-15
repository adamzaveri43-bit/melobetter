package `in`.melo.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Which look the app uses: Material You, or Apple Music's player with Liquid Glass surfaces. */
object UiStyle {
    const val MATERIAL = "material"
    const val GLASS = "glass"
    val ALL = listOf(MATERIAL, GLASS)
}

/**
 * A Liquid Glass surface: a translucent fill of [tint], lighter at the top, a specular sheen over its
 * upper half, and a thin rim of light brighter at the top-left and bottom-right edges, as light
 * catches the edges of a pane of glass. What is behind shows through the fill; content drawn inside
 * stays above the sheen.
 *
 * Compose cannot sample and blur what is behind an arbitrary surface on every Android version, so
 * the glass does not blur by itself: over the player it sits on the blurred artwork, elsewhere its
 * fill is opaque enough ([fillAlpha]) for lists that scroll underneath to stay legible.
 */
fun Modifier.liquidGlass(
    shape: Shape,
    tint: Color = Color.White,
    fillAlpha: Float = 0.16f,
    elevation: Dp = 0.dp,
): Modifier = this
    .then(if (elevation > 0.dp) Modifier.shadow(elevation, shape, clip = false,
        ambientColor = Color.Black.copy(alpha = 0.25f), spotColor = Color.Black.copy(alpha = 0.35f)) else Modifier)
    .clip(shape)
    .background(Brush.verticalGradient(listOf(tint.copy(alpha = (fillAlpha + 0.06f).coerceAtMost(1f)), tint.copy(alpha = fillAlpha))))
    .drawWithContent {
        // The sheen under the content: the light the upper curve of the glass reflects.
        drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.14f), Color.Transparent), endY = size.height * 0.55f))
        drawContent()
    }
    .border(
        width = 0.8.dp,
        brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.28f))),
        shape = shape,
    )
