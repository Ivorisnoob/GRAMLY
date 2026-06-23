@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ivor.scale.domain.GoldCalculator
import com.ivor.scale.domain.GoldResult

@Composable
fun GoldScreen(
    vm: ScaleViewModel,
    onBack: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text("Gold Calculator", fontWeight = FontWeight.Black) },
                subtitle = { Text("Carat, labour aur GST") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wapas")
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            NumberField(
                value = vm.goldPrice24,
                onValueChange = vm::onGoldPriceChange,
                label = "Gold price 24 Carat (₹/gram)",
                prefix = "₹",
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Gold Carat",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CaratButtonGroup(
                    options = GoldCalculator.KARATS,
                    selected = vm.goldKarat,
                    onSelect = vm::onGoldKaratChange,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField(
                    value = vm.goldGrams,
                    onValueChange = vm::onGoldGramsChange,
                    label = "Vajan (gram)",
                    suffix = "g",
                    modifier = Modifier.weight(1f),
                )
                NumberField(
                    value = vm.goldMilligrams,
                    onValueChange = vm::onGoldMilligramsChange,
                    label = "Vajan (milligram)",
                    suffix = "mg",
                    modifier = Modifier.weight(1f),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField(
                    value = vm.goldLabourPct,
                    onValueChange = vm::onGoldLabourChange,
                    label = "Labour charge %",
                    suffix = "%",
                    modifier = Modifier.weight(1f),
                )
                NumberField(
                    value = vm.goldGstPct,
                    onValueChange = vm::onGoldGstChange,
                    label = "GST %",
                    suffix = "%",
                    imeAction = ImeAction.Done,
                    modifier = Modifier.weight(1f),
                )
            }

            GoldResultCard(result = vm.goldResult)

            val clearSource = remember { MutableInteractionSource() }
            OutlinedButton(
                onClick = vm::clearGold,
                interactionSource = clearSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .expressivePress(clearSource),
            ) {
                Text("Saaf karein")
            }
        }
    }
}

/**
 * Standard (non-connected) ButtonGroup for single-select carat purity. Each
 * button uses the default shape-morphing ToggleButton shapes, and the group's
 * `animateWidth` makes the pressed button expand while its neighbours compress —
 * the signature expressive ButtonGroup interaction.
 */
@Composable
private fun CaratButtonGroup(
    options: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    val sources = remember(options.size) { List(options.size) { MutableInteractionSource() } }
    ButtonGroup(
        overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        options.forEachIndexed { index, karat ->
            customItem(
                buttonGroupContent = {
                    ToggleButton(
                        checked = selected == karat,
                        onCheckedChange = { onSelect(karat) },
                        interactionSource = sources[index],
                        modifier = Modifier.animateWidth(sources[index]),
                    ) {
                        Text("${karat}K")
                    }
                },
                menuContent = {
                    DropdownMenuItem(
                        text = { Text("${karat}K") },
                        onClick = { onSelect(karat) },
                    )
                },
            )
        }
    }
}

/** Clean breakdown card: line items, a divider, and the final payable amount. */
@Composable
private fun GoldResultCard(result: GoldResult) {
    val isSuccess = result is GoldResult.Success
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = if (isSuccess) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
            contentColor = if (isSuccess) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (result) {
                GoldResult.Empty -> {
                    Text(
                        text = "RESULT",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                    Text(
                        text = "Bhaav aur vajan bharein",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                is GoldResult.Success -> {
                    result.rows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = row.label, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = row.value,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.25f),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Final amount",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        HeroNumeral(
                            text = result.finalAmount,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }
        }
    }
}
