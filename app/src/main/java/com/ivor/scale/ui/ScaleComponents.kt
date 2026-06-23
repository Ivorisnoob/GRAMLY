package com.ivor.scale.ui

import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ivor.scale.domain.CalcResult

/** True unless the user has disabled animations system-wide (accessibility). */
@Composable
fun rememberAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) != 0f
    }
}

/**
 * A large, number-only input with a unit prefix (e.g. "₹") and a clear button.
 * Generous height and a rounded shape keep it tappable and on-brand.
 */
@Composable
fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    prefix: String? = null,
    suffix: String? = null,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    isError: Boolean = false,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        singleLine = true,
        isError = isError,
        textStyle = MaterialTheme.typography.headlineSmall,
        prefix = prefix?.let { { Text(it, style = MaterialTheme.typography.titleLarge) } },
        suffix = suffix?.let { { Text(it, style = MaterialTheme.typography.titleMedium) } },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() },
        ),
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Saaf karein")
                }
            }
        },
        shape = MaterialTheme.shapes.large,
    )
}

/**
 * The hero of each screen: a big card that shows the live answer. It morphs
 * between three states — hint (empty), error, and the result — with an
 * animated value swap so a changing number feels tactile.
 */
@Composable
fun ResultHeroCard(
    label: String,
    result: CalcResult,
    emptyHint: String,
    animationsEnabled: Boolean,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val containerColor = when (result) {
        is CalcResult.Success -> MaterialTheme.colorScheme.primaryContainer
        CalcResult.Error -> MaterialTheme.colorScheme.errorContainer
        CalcResult.Empty -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = when (result) {
        is CalcResult.Success -> MaterialTheme.colorScheme.onPrimaryContainer
        CalcResult.Error -> MaterialTheme.colorScheme.onErrorContainer
        CalcResult.Empty -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        onClick = {
            if (result is CalcResult.Success) onCopy(result.plain)
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = contentColor.copy(alpha = 0.7f),
            )

            when (result) {
                is CalcResult.Success ->
                    // Giant weight-morphing hero numeral.
                    HeroNumeral(
                        text = result.display,
                        style = MaterialTheme.typography.displayMedium,
                        color = contentColor,
                    )

                else -> {
                    val target = if (result is CalcResult.Error) strings.errorPriceZero else emptyHint
                    AnimatedContent(
                        targetState = target,
                        transitionSpec = {
                            if (animationsEnabled) {
                                (slideInVertically { it / 2 } + fadeIn())
                                    .togetherWith(slideOutVertically { -it / 2 } + fadeOut())
                            } else {
                                EnterTransition.None togetherWith ExitTransition.None
                            }
                        },
                        label = "result",
                    ) { text ->
                        Text(
                            text = text,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            if (result is CalcResult.Success) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = strings.tapToCopy,
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalContentColor.current.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}
