package com.example.taskapp.ui.notifications

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskapp.domain.model.IntervalUnit
import com.example.taskapp.domain.model.NotificationFrequency
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: NotificationSettingsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val setting = uiState.setting ?: return
    val context = LocalContext.current
    val dayLabels = listOf("Su", "Mo", "Tu", "We", "Th", "Fri", "Sa")
    val is24Hour = android.text.format.DateFormat.is24HourFormat(context)

    val notificationsEnabled = remember {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    LaunchedEffect(Unit) {
        viewModel.saveFinished.collect { onBack() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.saveSetting() },
                text = { Text(if (uiState.isSaving) "Saving…" else "Save") },
                icon = { Icon(Icons.Default.Check, null) }
            )
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            if (!notificationsEnabled) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "Notifications are disabled in system settings. You won't receive any alerts until you enable them.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            item {
                ListItem(
                    headlineContent = { Text("Enable notifications") },
                    supportingContent = { Text(uiState.listTitle) },
                    trailingContent = {
                        Switch(
                            checked = setting.isEnabled,
                            onCheckedChange = viewModel::onEnabledChanged
                        )
                    }
                )
            }

            if (setting.isEnabled) {
                item { HorizontalDivider() }

                item {
                    Text(
                        "Frequency",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
                    )
                }

                item {
                    NotificationFrequency.entries.forEach { freq ->
                        ListItem(
                            headlineContent = {
                                Text(when (freq) {
                                    NotificationFrequency.DAILY -> "Daily"
                                    NotificationFrequency.WEEKLY -> "Weekly"
                                    NotificationFrequency.ONE_TIME -> "One-time"
                                    NotificationFrequency.CUSTOM_INTERVAL -> "Custom interval"
                                })
                            },
                            leadingContent = {
                                RadioButton(
                                    selected = setting.frequency == freq,
                                    onClick = { viewModel.onFrequencyChanged(freq) }
                                )
                            }
                        )
                    }
                }

                // Time picker — shown only for DAILY and WEEKLY
                if (setting.frequency == NotificationFrequency.DAILY || 
                    setting.frequency == NotificationFrequency.WEEKLY) {
                    item { HorizontalDivider() }
                    item {
                        var showTimePicker by remember { mutableStateOf(false) }
                        val timeDisplay = if (is24Hour) {
                            "%02d:%02d".format(setting.hour, setting.minute)
                        } else {
                            val hour = if (setting.hour % 12 == 0) 12 else setting.hour % 12
                            val amPm = if (setting.hour < 12) "AM" else "PM"
                            "%d:%02d %s".format(hour, setting.minute, amPm)
                        }
                        ListItem(
                            headlineContent = { Text("Time") },
                            supportingContent = {
                                Text(timeDisplay)
                            },
                            modifier = Modifier.clickable { showTimePicker = true }
                        )
                        if (showTimePicker) {
                            DisposableEffect(Unit) {
                                val dialog = TimePickerDialog(
                                    context,
                                    { _, h, m -> viewModel.onTimeChanged(h, m); showTimePicker = false },
                                    setting.hour, setting.minute, is24Hour
                                )
                                dialog.setOnDismissListener { showTimePicker = false }
                                dialog.show()
                                onDispose { if (dialog.isShowing) dialog.dismiss() }
                            }
                        }
                    }
                }

                // Weekly day chips
                if (setting.frequency == NotificationFrequency.WEEKLY) {
                    item { HorizontalDivider() }
                    item {
                        Text(
                            "Days",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            dayLabels.forEachIndexed { index, label ->
                                val bit = 1 shl index
                                FilterChip(
                                    selected = (setting.weekDaysMask and bit) != 0,
                                    onClick = { viewModel.onWeekDayToggled(bit) },
                                    label = { Text(label) }
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                // Date/time picker — ONE_TIME and CUSTOM_INTERVAL start time
                if (setting.frequency == NotificationFrequency.ONE_TIME ||
                    setting.frequency == NotificationFrequency.CUSTOM_INTERVAL
                ) {
                    item { HorizontalDivider() }
                    item {
                        val label = if (setting.frequency == NotificationFrequency.ONE_TIME)
                            "Date & Time" else "Start Date & Time"
                        val pattern = if (is24Hour) "MMM d, yyyy  HH:mm" else "MMM d, yyyy  h:mm a"
                        val dateDisplay = if (setting.oneTimeEpochMillis > 0)
                            SimpleDateFormat(pattern, Locale.getDefault())
                                .format(Date(setting.oneTimeEpochMillis))
                        else "Tap to set"

                        var showDatePicker by remember { mutableStateOf(false) }
                        ListItem(
                            headlineContent = { Text(label) },
                            supportingContent = { Text(dateDisplay) },
                            modifier = Modifier.clickable { showDatePicker = true }
                        )
                        if (showDatePicker) {
                            DisposableEffect(Unit) {
                                val cal = Calendar.getInstance().apply {
                                    if (setting.oneTimeEpochMillis > 0) timeInMillis = setting.oneTimeEpochMillis
                                }
                                val datePicker = DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        val timePicker = TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                val picked = Calendar.getInstance().apply {
                                                    set(year, month, day, hour, minute, 0)
                                                    set(Calendar.MILLISECOND, 0)
                                                }.timeInMillis
                                                viewModel.onOneTimeDateTimeChanged(picked)
                                                showDatePicker = false
                                            },
                                            cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), is24Hour
                                        )
                                        timePicker.setOnDismissListener { showDatePicker = false }
                                        timePicker.show()
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                )
                                datePicker.setOnDismissListener { showDatePicker = false }
                                datePicker.show()
                                onDispose { if (datePicker.isShowing) datePicker.dismiss() }
                            }
                        }
                    }
                }

                // Custom interval N + unit chips
                if (setting.frequency == NotificationFrequency.CUSTOM_INTERVAL) {
                    item { HorizontalDivider() }
                    item {
                        Text(
                            "Repeat every",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = setting.intervalValue.toString(),
                                onValueChange = { v ->
                                    v.toIntOrNull()?.let { viewModel.onIntervalValueChanged(it) }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(80.dp),
                                singleLine = true,
                                label = { Text("N") }
                            )
                            IntervalUnit.entries.forEach { unit ->
                                FilterChip(
                                    selected = setting.intervalUnit == unit,
                                    onClick = { viewModel.onIntervalUnitChanged(unit) },
                                    label = {
                                        Text(when (unit) {
                                            IntervalUnit.HOURS -> "Hours"
                                            IntervalUnit.DAYS -> "Days"
                                            IntervalUnit.WEEKS -> "Weeks"
                                        })
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}
