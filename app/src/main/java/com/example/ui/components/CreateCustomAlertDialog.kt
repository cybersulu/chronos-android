package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlarmAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CountdownEntity

enum class AlertTimeUnit(val label: String, val multiplierMinutes: Int) {
    MINUTES("Minutes", 1),
    HOURS("Hours", 60),
    DAYS("Days", 1440),
    WEEKS("Weeks", 10080)
}

@Composable
fun CreateCustomAlertDialog(
    onDismissRequest: () -> Unit,
    onAlertCreated: (minutes: Int) -> Unit
) {
    var amountText by remember { mutableStateOf("30") }
    var selectedUnit by remember { mutableStateOf(AlertTimeUnit.MINUTES) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val amountInt = amountText.toIntOrNull()
    val totalMinutes = if (amountInt != null && amountInt > 0) amountInt * selectedUnit.multiplierMinutes else 0

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.testTag("dialog_custom_alert"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AlarmAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "New Alert Notification",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Specify how long before the event countdown you would like to be alerted:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Numeric Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter { char -> char.isDigit() }
                        errorMessage = null
                    },
                    label = { Text("Amount") },
                    placeholder = { Text("e.g. 30") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_custom_alert_amount")
                )

                // Time Unit Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Time Unit",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AlertTimeUnit.entries.forEach { unit ->
                            val isSelected = selectedUnit == unit
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedUnit = unit },
                                label = { Text(unit.label, fontSize = 12.sp) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_unit_${unit.name.lowercase()}")
                            )
                        }
                    }
                }

                // Summary / Preview Box
                if (totalMinutes > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Preview Alert Notice",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = CountdownEntity.formatAlertOffsetLabel(totalMinutes),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amountInt == null || amountInt <= 0) {
                        errorMessage = "Please enter a valid number greater than 0"
                        return@Button
                    }
                    if (totalMinutes > 525600) { // More than 1 year
                        errorMessage = "Alert time must be within 1 year"
                        return@Button
                    }
                    onAlertCreated(totalMinutes)
                    onDismissRequest()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("button_confirm_add_custom_alert")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Alert", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.testTag("button_cancel_custom_alert")
            ) {
                Text("Cancel")
            }
        }
    )
}
