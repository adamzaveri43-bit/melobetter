package `in`.melo.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.melo.R
import `in`.melo.ui.theme.DotMatrixFamily
import kotlin.math.cos
import kotlin.math.sin

/** The record is decorative and static; it never competes with album artwork. */
@Composable
fun MeloRecord(modifier: Modifier = Modifier) {
    val ink = MaterialTheme.colorScheme.onSurface
    val grooves = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier.clearAndSetSemantics {}) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        for (ring in 4..12) {
            drawCircle(grooves, radius * ring / 12.5f, center, style = Stroke(1.dp.toPx()))
        }
        for (ring in 5..11) {
            val count = ring * 8
            for (dot in 0 until count) {
                val angle = dot * 2 * Math.PI / count
                val r = radius * ring / 12f
                drawCircle(ink.copy(alpha = if (dot % 8 < 4) 0.65f else 0.18f), radius * 0.009f,
                    Offset(center.x + (cos(angle) * r).toFloat(), center.y + (sin(angle) * r).toFloat()))
            }
        }
        drawCircle(ink, radius * 0.22f, center, style = Stroke(1.dp.toPx()))
        drawCircle(ink, radius * 0.035f, center)
    }
}

@Composable
fun MeloWordmark(modifier: Modifier = Modifier) {
    Text(stringResource(R.string.app_name).uppercase(), modifier,
        fontFamily = DotMatrixFamily, fontSize = 25.sp, letterSpacing = (-0.5).sp,
        color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ListeningHomeHeader() {
    Text(
        stringResource(R.string.melo_home_compact_title),
        Modifier.fillMaxWidth().padding(horizontal = `in`.melo.ui.theme.MeloLayout.PageGutter),
        style = MaterialTheme.typography.headlineSmall,
    )
}
