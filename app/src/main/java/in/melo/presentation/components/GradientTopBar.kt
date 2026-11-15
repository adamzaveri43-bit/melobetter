package `in`.melo.presentation.components

import dev.chrisbanes.haze.hazeEffect
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.material.icons.rounded.History

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.melo.R
import `in`.melo.ui.theme.GoogleSansRounded
import `in`.melo.ui.theme.MeloStatusBarStyle
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreGradientTopBar(
    title: String,
    startColor: Color,
    endColor: Color,
    contentColor: Color,
    scrollBehavior: TopAppBarScrollBehavior,
    onNavigationIconClick: () -> Unit,
) {
    val gradientBrush = remember(startColor, endColor) {
        Brush.verticalGradient(colors = listOf(startColor, endColor))
    }

    MeloStatusBarStyle(color = startColor)

    LargeTopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                modifier = Modifier.padding(start = 6.dp),
                text = title,
                color = contentColor,
                fontFamily = GoogleSansRounded
            )
        },
        expandedHeight = 160.dp,
        modifier = Modifier.background(brush = gradientBrush),
        navigationIcon = {
            IconButton(
                modifier = Modifier.padding(start = 10.dp),
                onClick = onNavigationIconClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = contentColor
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                    tint = startColor
                )
            }
        },
        colors = topAppBarColors(
            containerColor = Color.Transparent, // Background is handled by the gradient brush
            scrolledContainerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer, // Or a color that contrasts well with your typical gradient
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer // Same as title
        )
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeGradientTopBar(
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    isScrolled: Boolean = false,
    /** Liquid Glass: the list scrolling under the bar is blurred through it. */
    glassHaze: dev.chrisbanes.haze.HazeState? = null,
) {
    val colors = MaterialTheme.colorScheme
    MeloStatusBarStyle(color = colors.background)
    TopAppBar(
        modifier = if (glassHaze == null) Modifier else Modifier.liquidGlassBar(glassHaze, colors.background),
        title = { MeloWordmark(Modifier.padding(start = 8.dp)) },
        actions = {
            IconButton(onClick = onHistoryClick, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Rounded.History, stringResource(R.string.history_title))
            }
            IconButton(onClick = onSettingsClick, modifier = Modifier.padding(end = 12.dp).size(48.dp)) {
                Icon(painterResource(R.drawable.rounded_settings_24), stringResource(R.string.common_settings))
            }
        },
        colors = topAppBarColors(
            containerColor = if (glassHaze == null) colors.background else androidx.compose.ui.graphics.Color.Transparent,
            scrolledContainerColor = if (glassHaze == null) colors.background else androidx.compose.ui.graphics.Color.Transparent,
        ),
    )
}

/** A full-width glass bar: blur of what scrolls under it, a light tint, and a hairline of light at its lower edge. */
private fun Modifier.liquidGlassBar(haze: dev.chrisbanes.haze.HazeState, tint: androidx.compose.ui.graphics.Color): Modifier = this
    .hazeEffect(haze) {
        blurRadius = 26.dp
        backgroundColor = tint
        tints = listOf(dev.chrisbanes.haze.HazeTint(tint.copy(alpha = 0.5f)))
        noiseFactor = 0.06f
    }
    .drawWithContent {
        drawContent()
        drawLine(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.18f),
            androidx.compose.ui.geometry.Offset(0f, size.height), androidx.compose.ui.geometry.Offset(size.width, size.height), 1.dp.toPx())
    }
