package `in`.melo.presentation.components.player

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import `in`.melo.data.preferences.PlayerBackground
import `in`.melo.presentation.components.SmartImage
import kotlin.math.cos
import kotlin.math.sin

/**
 * What is drawn behind the full player and the lyrics screen, as chosen in Settings → Player.
 * [PlayerBackground.SOLID] draws nothing: the sheet's own colour shows. Every other style keeps
 * the colour scheme's container colour underneath and stays dim enough for its content colours.
 */
@Composable
fun PlayerBackdrop(style: String, artworkUrl: String?, scheme: ColorScheme, modifier: Modifier = Modifier) {
    when (style) {
        PlayerBackground.GRADIENT -> Box(
            modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(scheme.primary.copy(alpha = 0.55f).compositeOn(scheme.primaryContainer), scheme.primaryContainer, scheme.surfaceContainerLowest))
            )
        )
        PlayerBackground.BLUR -> Box(modifier.fillMaxSize().background(scheme.primaryContainer)) {
            val blurs = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            if (artworkUrl != null) SmartImage(
                model = artworkUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
                    .graphicsLayer { scaleX = 1.3f; scaleY = 1.3f }
                    .then(if (blurs) Modifier.blur(72.dp) else Modifier),
            )
            // Without blur (before Android 12) the artwork shows as it is: a heavier veil keeps text readable.
            Box(Modifier.fillMaxSize().background(scheme.primaryContainer.copy(alpha = if (blurs) 0.55f else 0.78f)))
        }
        PlayerBackground.ANIMATED -> AnimatedBackdrop(scheme, modifier)
        else -> Unit
    }
}

/** Three soft colour fields from the artwork's palette drifting slowly, as Apple Music's lyrics background moves. */
@Composable
private fun AnimatedBackdrop(scheme: ColorScheme, modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "playerBackdrop")
    val phase by transition.animateFloat(
        initialValue = 0f, targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(durationMillis = 24_000, easing = LinearEasing), RepeatMode.Restart),
        label = "phase",
    )
    val base = scheme.primaryContainer
    val fields = listOf(scheme.primary, scheme.tertiary, scheme.secondary)
    Canvas(modifier.fillMaxSize().background(base)) {
        val radius = maxOf(size.width, size.height) * 0.75f
        fields.forEachIndexed { index, color ->
            val angle = phase + index * 2.1f
            val center = Offset(
                size.width * (0.5f + 0.35f * cos(angle * (1f + index * 0.15f))),
                size.height * (0.5f + 0.3f * sin(angle * (0.8f + index * 0.2f))),
            )
            drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.42f), Color.Transparent), center, radius), radius, center)
        }
        // A veil of the base colour keeps the scheme's content colours readable over the fields.
        drawRect(base.copy(alpha = 0.28f))
    }
}

private fun Color.compositeOn(background: Color): Color {
    val a = alpha
    return Color(red * a + background.red * (1 - a), green * a + background.green * (1 - a), blue * a + background.blue * (1 - a), 1f)
}
