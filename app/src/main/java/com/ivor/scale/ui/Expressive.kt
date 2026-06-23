package com.ivor.scale.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import com.ivor.scale.ui.theme.rememberRobotoFlexAxis
import kotlin.math.roundToInt

/**
 * Springy press feedback: the element squishes slightly when pressed and bounces
 * back. Pairs with [MotionScheme.expressive] for a consistent tactile feel where
 * alpha16 doesn't yet expose `Button(shapes = …)` shape-morphing.
 */
fun Modifier.expressivePress(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.94f,
): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 650f),
        label = "expressivePressScale",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * The hero result number. Uses Roboto Flex's weight & width variation axes to
 * give a subtle expressive "morph" each time the value changes — the digits
 * settle into place with a spring instead of snapping. Kept gentle so it stays
 * pleasant during live typing.
 */
@Composable
fun HeroNumeral(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val morph = remember { Animatable(1f) }
    LaunchedEffect(text) {
        morph.snapTo(0f)
        morph.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = 320f),
        )
    }
    val p = morph.value.coerceIn(0f, 1f)
    // From a touch lighter & wider, settling into heavy & normal-width.
    val weightAxis = ((840 + 60 * p).roundToInt() / 10) * 10
    val widthAxis = 112f - 12f * p
    val family = rememberRobotoFlexAxis(weightAxis, widthAxis)

    Text(
        text = text,
        style = style,
        color = color,
        fontFamily = family,
        modifier = modifier,
    )
}
