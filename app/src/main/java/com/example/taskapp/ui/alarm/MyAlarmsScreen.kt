package com.example.taskapp.ui.alarm

import android.app.TimePickerDialog
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskapp.domain.model.Alarm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAlarmsScreen(
    viewModel: MyAlarmsViewModel,
    onOpenDrawer: () -> Unit
) {
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val localAlarms = remember { mutableStateListOf<Alarm>() }
    var isDragging by remember { mutableStateOf(false) }
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    val listState = rememberLazyListState()

    var showDialog by remember { mutableStateOf(false) }
    var alarmToEdit by remember { mutableStateOf<Alarm?>(null) }
    var alarmToDelete by remember { mutableStateOf<Alarm?>(null) }

    LaunchedEffect(alarms) {
        if (!isDragging) {
            localAlarms.clear()
            localAlarms.addAll(alarms)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Alarms") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                alarmToEdit = Alarm(label = "", hour = 8, minute = 0)
                showDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add alarm")
            }
        }
    ) { padding ->
        if (alarms.isEmpty() && localAlarms.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No alarms set.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(localAlarms, key = { _, alarm -> alarm.id }) { index, alarm ->
                    val isDraggedItem = draggedIndex == index
                    val elevation by animateDpAsState(
                        targetValue = if (isDraggedItem) 8.dp else 0.dp,
                        label = "drag_elevation"
                    )
                    DraggableAlarmCard(
                        alarm = alarm,
                        isDragged = isDraggedItem,
                        elevation = elevation,
                        dragOffsetY = if (isDraggedItem) dragOffsetY else 0f,
                        onToggle = { viewModel.toggleAlarm(alarm) },
                        onDelete = { alarmToDelete = alarm },
                        onClick = {
                            alarmToEdit = alarm
                            showDialog = true
                        },
                        onDragStart = {
                            isDragging = true
                            draggedIndex = index
                            dragOffsetY = 0f
                        },
                        onDrag = { delta ->
                            dragOffsetY += delta
                            val currentDragged = draggedIndex ?: return@DraggableAlarmCard
                            val itemHeightPx = listState.layoutInfo.visibleItemsInfo
                                .find { it.key == alarm.id }?.size?.toFloat() ?: 100f
                            val rawTarget = currentDragged + (dragOffsetY / itemHeightPx).toInt()
                            val targetIndex = rawTarget.coerceIn(0, localAlarms.size - 1)
                            if (targetIndex != currentDragged) {
                                val moved = localAlarms.removeAt(currentDragged)
                                localAlarms.add(targetIndex, moved)
                                dragOffsetY -= (targetIndex - currentDragged) * itemHeightPx
                                draggedIndex = targetIndex
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                            draggedIndex = null
                            dragOffsetY = 0f
                            viewModel.reorderAlarms(localAlarms.toList())
                        }
                    )
                }
            }
        }

        if (showDialog && alarmToEdit != null) {
            AlarmEditDialog(
                alarm = alarmToEdit!!,
                onDismiss = { showDialog = false },
                onConfirm = { editedAlarm ->
                    viewModel.upsertAlarm(editedAlarm)
                    showDialog = false
                }
            )
        }

        alarmToDelete?.let { alarm ->
            AlertDialog(
                onDismissRequest = { alarmToDelete = null },
                title = { Text("Delete Alarm") },
                text = { Text("Are you sure you want to permanently delete this alarm? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteAlarm(alarm)
                            alarmToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { alarmToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun DraggableAlarmCard(
    alarm: Alarm,
    isDragged: Boolean,
    elevation: Dp,
    dragOffsetY: Float,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val dayLabels = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
    val selectedDays = dayLabels.filterIndexed { index, _ -> (alarm.daysOfWeek and (1 shl index)) != 0 }
    
    val recurrenceText = if (alarm.isOneTime) "One-time"
    else if (alarm.daysOfWeek == 0b1111111) "Daily"
    else if (selectedDays.isEmpty()) "Never (select days)"
    else selectedDays.joinToString(", ")

    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)

    Surface(
        shadowElevation = elevation,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = if (isDragged) dragOffsetY else 0f }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable { onClick() }
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder",
                    modifier = Modifier
                        .pointerInput(alarm.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { currentOnDragStart() },
                                onDrag = { _, dragAmount -> currentOnDrag(dragAmount.y) },
                                onDragEnd = { currentOnDragEnd() },
                                onDragCancel = { currentOnDragEnd() }
                            )
                        }
                        .padding(8.dp)
                )

                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text(
                        text = "%02d:%02d".format(alarm.hour, alarm.minute),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    if (alarm.label.isNotBlank()) {
                        Text(text = alarm.label, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = recurrenceText + if (alarm.isSilent) " (Silent)" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (alarm.daysOfWeek == 0 && !alarm.isOneTime) MaterialTheme.colorScheme.error else Color.Unspecified
                    )
                }
                Switch(checked = alarm.isEnabled, onCheckedChange = { onToggle() })
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete alarm")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditDialog(
    alarm: Alarm,
    onDismiss: () -> Unit,
    onConfirm: (Alarm) -> Unit
) {
    var label by remember { mutableStateOf(alarm.label) }
    var hour by remember { mutableIntStateOf(alarm.hour) }
    var minute by remember { mutableIntStateOf(alarm.minute) }
    var isOneTime by remember { mutableStateOf(alarm.isOneTime) }
    var isSilent by remember { mutableStateOf(alarm.isSilent) }
    
    // Default to Daily if it's a new alarm or already set to all days
    var isDaily by remember { mutableStateOf(alarm.daysOfWeek == 0b1111111 || alarm.daysOfWeek == 0) }
    var daysOfWeek by remember { mutableIntStateOf(if (alarm.daysOfWeek == 0) 0b1111111 else alarm.daysOfWeek) }
    
    val context = LocalContext.current
    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (alarm.id == 0L) "New Alarm" else "Edit Alarm") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = {
                        TimePickerDialog(context, { _, h, m ->
                            hour = h
                            minute = m
                        }, hour, minute, true).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Time: %02d:%02d".format(hour, minute))
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "One-time",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOneTime) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = !isOneTime,
                        onCheckedChange = { isOneTime = !it }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Recurring",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (!isOneTime) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!isOneTime) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Daily",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDaily) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = !isDaily,
                            onCheckedChange = { isDaily = !it }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Custom",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (!isDaily) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!isDaily) {
                        Column {
                            Text("Repeat on:", style = MaterialTheme.typography.labelLarge)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                dayLabels.forEachIndexed { index, day ->
                                    val bit = 1 shl index
                                    val isSelected = (daysOfWeek and bit) != 0
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { daysOfWeek = daysOfWeek xor bit },
                                        label = { 
                                            Text(
                                                text = day,
                                                modifier = Modifier.fillMaxWidth(),
                                                style = MaterialTheme.typography.labelSmall,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            ) 
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                                }
                            }
                            if (daysOfWeek == 0) {
                                Text(
                                    "Select at least one day",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Checkbox(
                        checked = isSilent,
                        onCheckedChange = { isSilent = it }
                    )
                    Text(
                        text = "Vibrate only",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = isOneTime || isDaily || daysOfWeek != 0,
                onClick = {
                    onConfirm(alarm.copy(
                        label = label,
                        hour = hour,
                        minute = minute,
                        isOneTime = isOneTime,
                        isEnabled = true,
                        isSilent = isSilent,
                        daysOfWeek = when {
                            isOneTime -> 0
                            isDaily -> 0b1111111
                            else -> daysOfWeek
                        }
                    ))
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
