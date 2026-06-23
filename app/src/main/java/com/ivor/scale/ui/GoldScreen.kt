package com.ivor.scale.ui

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ivor.scale.domain.GoldResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoldScreen(
    vm: ScaleViewModel,
    onBack: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gold Calculator",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wapas")
                    }
                },
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

            KaratDropdown(
                selected = vm.goldKarat,
                onSelected = vm::onGoldKaratChange,
            )

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

            OutlinedButton(
                onClick = vm::clearGold,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Saaf karein")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KaratDropdown(
    selected: Int,
    onSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = "${selected}K",
            onValueChange = {},
            readOnly = true,
            label = { Text("Gold Carat") },
            textStyle = MaterialTheme.typography.headlineSmall,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            com.ivor.scale.domain.GoldCalculator.KARATS.forEach { karat ->
                DropdownMenuItem(
                    text = { Text("${karat}K") },
                    onClick = {
                        onSelected(karat)
                        expanded = false
                    },
                )
            }
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
                            Text(
                                text = row.label,
                                style = MaterialTheme.typography.bodyLarge,
                            )
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
                        Text(
                            text = result.finalAmount,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
            }
        }
    }
}
