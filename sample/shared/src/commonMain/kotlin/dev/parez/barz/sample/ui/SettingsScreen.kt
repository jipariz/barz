package dev.parez.barz.sample.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Tonality
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.parez.barz.sample.ThemeMode
import dev.parez.barz.sample.UnitSystem
import dev.parez.barz.sample.theme.isDarkScheme

@Composable
fun SettingsScreen(
    unit: UnitSystem,
    onUnitChange: (UnitSystem) -> Unit,
    twentyFourHourTime: Boolean,
    onTwentyFourHourTimeChange: (Boolean) -> Unit,
    mode: ThemeMode,
    onModeChange: (ThemeMode) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var showAbout by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(16.dp)
    ) {
        // The design only ever shows this at phone width. Left to stretch on a desktop window the
        // card grows to ~1000dp and each control ends up a screen away from the label it belongs
        // to, so it is capped rather than filled.
        Surface(
            modifier = Modifier.widthIn(max = 560.dp),
            shape = CardShape,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column {
                SettingsRow(icon = Icons.Filled.Straighten, label = "Unit") {
                    UnitToggle(unit = unit, onChange = onUnitChange)
                }
                RowDivider()
                SettingsRow(icon = Icons.Filled.Schedule, label = "24-Hour Time") {
                    Switch(
                        checked = twentyFourHourTime,
                        onCheckedChange = onTwentyFourHourTimeChange,
                        // Spelled out in full. The M3 defaults draw the thumb and border in
                        // `outline` over a `surfaceContainerHighest` track, which on this greyscale
                        // palette is close enough to invisible.
                        colors =
                            SwitchDefaults.colors(
                                checkedTrackColor = MaterialTheme.colorScheme.onSurface,
                                checkedThumbColor = MaterialTheme.colorScheme.surface,
                                checkedBorderColor = Color.Transparent,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainer,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedBorderColor = Color.Transparent,
                            ),
                    )
                }
                RowDivider()
                SettingsRow(icon = Icons.Filled.Tonality, label = "Mode") {
                    ModePicker(mode = mode, onChange = onModeChange)
                }
                RowDivider()
                SettingsRow(
                    icon = Icons.Filled.Info,
                    label = "About",
                    onClick = { showAbout = true },
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                // A floor, not a fixed height: the Unit row is taller than the rest because of its
                // segmented control, and the others would otherwise look cramped beside it.
                .defaultMinSize(minHeight = 60.dp)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        trailing()
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

/**
 * The design's inline segmented control: one pill with the selected half raised out of it.
 *
 * "Raised" has to flip with the scheme. In light the track is darker than the card and the selected
 * chip lighter; in dark that ordering inverts, and reusing the light roles made the selected chip
 * *recede* into the track instead of standing out of it.
 */
@Composable
private fun UnitToggle(unit: UnitSystem, onChange: (UnitSystem) -> Unit) {
    val dark = isDarkScheme()
    val track =
        if (dark) MaterialTheme.colorScheme.background
        else MaterialTheme.colorScheme.surfaceContainer
    val raised =
        if (dark) MaterialTheme.colorScheme.surfaceContainer
        else MaterialTheme.colorScheme.surfaceContainerLowest

    Surface(shape = CircleShape, color = track) {
        Row(Modifier.padding(3.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            UnitSystem.entries.forEach { option ->
                val selected = option == unit
                Surface(
                    onClick = { onChange(option) },
                    shape = CircleShape,
                    color = if (selected) raised else Color.Transparent,
                ) {
                    Text(
                        text = option.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            if (selected) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ModePicker(mode: ThemeMode, onChange: (ThemeMode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            // Clip before clickable: unclipped, the press/hover indication paints a hard-edged
            // rectangle that runs to the card's edge.
            modifier =
                Modifier.clip(CircleShape)
                    .clickable { expanded = true }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = mode.label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(
                Icons.Filled.ExpandMore,
                contentDescription = "Change mode",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ThemeMode.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    leadingIcon = {
                        if (option == mode) {
                            Icon(Icons.Filled.Check, contentDescription = null)
                        } else {
                            Spacer(Modifier.size(24.dp))
                        }
                    },
                    onClick = {
                        onChange(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "About",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PokeballMark(Modifier.size(72.dp))
                Spacer(Modifier.height(12.dp))
                Text(
                    "Pokemon v1.0",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

private val ThemeMode.label: String
    get() = name.lowercase().replaceFirstChar { it.uppercase() }
