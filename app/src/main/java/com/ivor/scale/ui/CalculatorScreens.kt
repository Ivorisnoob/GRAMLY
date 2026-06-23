package com.ivor.scale.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivor.scale.domain.CalcResult

/** Weight tab: money in, weight out. */
@Composable
fun WeightTab(
    vm: ScaleViewModel,
    animationsEnabled: Boolean,
    onCopy: (String) -> Unit,
    onImeNext: () -> Unit,
) {
    val strings = LocalStrings.current
    val result = vm.weightResult
    CalculatorScaffold(
        result = result,
        resultLabel = strings.resultWeight,
        emptyHint = strings.hintWeight,
        onClear = { vm.clearWeightTab() },
        animationsEnabled = animationsEnabled,
        onCopy = onCopy,
    ) {
        NumberField(
            value = vm.pricePerKg,
            onValueChange = vm::onPriceChange,
            label = strings.fieldPrice,
            prefix = "₹",
            isError = result is CalcResult.Error,
        )
        NumberField(
            value = vm.amount,
            onValueChange = vm::onAmountChange,
            label = strings.fieldAmount,
            prefix = "₹",
            imeAction = ImeAction.Next,
            onImeAction = onImeNext,
        )
    }
}

/** Rate tab: weight in, money out. */
@Composable
fun RateTab(
    vm: ScaleViewModel,
    animationsEnabled: Boolean,
    onCopy: (String) -> Unit,
    onImeNext: () -> Unit,
) {
    val strings = LocalStrings.current
    val result = vm.rateResult
    CalculatorScaffold(
        result = result,
        resultLabel = strings.resultAmount,
        emptyHint = strings.hintAmount,
        onClear = { vm.clearRateTab() },
        animationsEnabled = animationsEnabled,
        onCopy = onCopy,
    ) {
        NumberField(
            value = vm.pricePerKg,
            onValueChange = vm::onPriceChange,
            label = strings.fieldPrice,
            prefix = "₹",
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField(
                value = vm.rateKilos,
                onValueChange = vm::onRateKilosChange,
                label = strings.fieldKilo,
                suffix = "kg",
                modifier = Modifier.weight(1f),
            )
            NumberField(
                value = vm.rateGrams,
                onValueChange = vm::onRateGramsChange,
                label = strings.fieldGram,
                suffix = "g",
                imeAction = ImeAction.Next,
                onImeAction = onImeNext,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Shared layout for both tabs: scrollable column with input fields, the result
 * hero card, and a clear action.
 */
@Composable
private fun CalculatorScaffold(
    result: CalcResult,
    resultLabel: String,
    emptyHint: String,
    onClear: () -> Unit,
    animationsEnabled: Boolean,
    onCopy: (String) -> Unit,
    inputs: @Composable () -> Unit,
) {
    val strings = LocalStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        inputs()

        ResultHeroCard(
            label = resultLabel,
            result = result,
            emptyHint = emptyHint,
            animationsEnabled = animationsEnabled,
            onCopy = onCopy,
        )

        val clearSource = remember { MutableInteractionSource() }
        OutlinedButton(
            onClick = onClear,
            interactionSource = clearSource,
            modifier = Modifier
                .fillMaxWidth()
                .expressivePress(clearSource),
        ) {
            Text(strings.clear)
        }

        Text(
            text = strings.tipSharedPrice,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )
    }
}
