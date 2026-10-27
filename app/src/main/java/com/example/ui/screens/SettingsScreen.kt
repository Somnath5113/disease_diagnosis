package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.data.settings.AppLanguage
import com.example.data.settings.AppSettings
import com.example.data.settings.EmergencyNumbers
import com.example.data.settings.TextSize
import com.example.data.settings.ThemeMode
import com.example.data.settings.dialNumber
import com.example.ui.theme.getUrgencyThemeColors

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
    onTextSizeSelected: (TextSize) -> Unit,
    onEmergencyNumberSelected: (String) -> Unit,
    onShowRemediesChanged: (Boolean) -> Unit,
    onSaveHistoryChanged: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val emergencyColors = getUrgencyThemeColors("EMERGENCY")
    var showClearDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Language
        item {
            SettingsCard(
                title = stringResource(R.string.settings_language),
                hint = stringResource(R.string.settings_language_hint),
                modifier = Modifier.testTag("settings_language_card")
            ) {
                AppLanguage.entries.forEach { language ->
                    val selected = language == settings.language
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected,
                                role = Role.RadioButton,
                                onClick = { if (!selected) onLanguageSelected(language) }
                            )
                            .padding(vertical = 2.dp)
                            .testTag("language_option_${language.tag}")
                    ) {
                        RadioButton(selected = selected, onClick = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                text = language.nativeName,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (language.englishName != language.nativeName) {
                                Text(
                                    text = language.englishName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                if (!settings.language.clinicalContent) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.settings_language_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                        modifier = Modifier.testTag("language_english_content_note")
                    )
                }
            }
        }

        // Emergency number
        item {
            SettingsCard(
                title = stringResource(R.string.settings_emergency_number),
                hint = stringResource(R.string.settings_emergency_number_hint),
                modifier = Modifier.testTag("settings_emergency_number_card")
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    EmergencyNumbers.PRIMARY_OPTIONS.forEach { number ->
                        FilterChip(
                            selected = number == settings.emergencyNumber,
                            onClick = { onEmergencyNumberSelected(number) },
                            label = {
                                Text(number, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            },
                            modifier = Modifier.testTag("emergency_number_chip_$number")
                        )
                    }
                }
            }
        }

        // Helplines
        item {
            SettingsCard(
                title = stringResource(R.string.settings_helplines),
                hint = stringResource(R.string.settings_helplines_hint),
                modifier = Modifier.testTag("settings_helplines_card")
            ) {
                EmergencyNumbers.HELPLINES.forEachIndexed { index, helpline ->
                    if (index > 0) Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = emergencyColors.container,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, emergencyColors.border)
                        ) {
                            Text(
                                text = helpline.number,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = emergencyColors.primary,
                                modifier = Modifier
                                    .width(64.dp)
                                    .padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(helpline.label),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(helpline.note),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { dialNumber(context, helpline.number) },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.2.dp, emergencyColors.primary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = emergencyColors.primary),
                            modifier = Modifier.testTag("helpline_call_${helpline.number}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = stringResource(R.string.call_number, helpline.number),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.settings_call), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Appearance
        item {
            SettingsCard(
                title = stringResource(R.string.settings_appearance),
                modifier = Modifier.testTag("settings_appearance_card")
            ) {
                Text(
                    text = stringResource(R.string.settings_theme),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = mode == settings.themeMode,
                            onClick = { onThemeSelected(mode) },
                            label = {
                                Text(
                                    when (mode) {
                                        ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                                        ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                                        ThemeMode.DARK -> stringResource(R.string.theme_dark)
                                    }
                                )
                            },
                            modifier = Modifier.testTag("theme_chip_${mode.name}")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.settings_text_size),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    TextSize.entries.forEach { size ->
                        FilterChip(
                            selected = size == settings.textSize,
                            onClick = { onTextSizeSelected(size) },
                            label = {
                                Text(
                                    when (size) {
                                        TextSize.NORMAL -> stringResource(R.string.text_normal)
                                        TextSize.LARGE -> stringResource(R.string.text_large)
                                        TextSize.EXTRA_LARGE -> stringResource(R.string.text_extra_large)
                                    }
                                )
                            },
                            modifier = Modifier.testTag("text_size_chip_${size.name}")
                        )
                    }
                }
            }
        }

        // Preferences
        item {
            SettingsCard(
                title = stringResource(R.string.settings_preferences),
                modifier = Modifier.testTag("settings_preferences_card")
            ) {
                SwitchRow(
                    title = stringResource(R.string.settings_remedies),
                    hint = stringResource(R.string.settings_remedies_hint),
                    checked = settings.showHomeRemedies,
                    onCheckedChange = onShowRemediesChanged,
                    tag = "switch_home_remedies"
                )
                Spacer(modifier = Modifier.height(10.dp))
                SwitchRow(
                    title = stringResource(R.string.settings_save_history),
                    hint = stringResource(R.string.settings_save_history_hint),
                    checked = settings.saveHistory,
                    onCheckedChange = onSaveHistoryChanged,
                    tag = "switch_save_history"
                )
            }
        }

        // Data
        item {
            SettingsCard(
                title = stringResource(R.string.settings_data),
                modifier = Modifier.testTag("settings_data_card")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showClearDialog = true }
                        .testTag("settings_clear_history_row")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Text(
                            text = stringResource(R.string.settings_clear_history),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = stringResource(R.string.settings_clear_history_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // About
        item {
            SettingsCard(
                title = stringResource(R.string.settings_about),
                modifier = Modifier.testTag("settings_about_card")
            ) {
                Text(
                    text = stringResource(R.string.settings_about_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.settings_disclaimer, settings.emergencyNumber),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    stringResource(R.string.history_clear_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    stringResource(R.string.history_clear_body),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.history_clear_all), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (hint != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    hint: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .testTag(tag)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = null)
    }
}
