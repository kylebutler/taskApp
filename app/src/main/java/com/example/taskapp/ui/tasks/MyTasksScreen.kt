package com.example.taskapp.ui.tasks

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskapp.domain.model.TaskList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTasksScreen(
    viewModel: MyTasksViewModel,
    onOpenDrawer: () -> Unit,
    onTaskClick: (Long) -> Unit
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val localTasks = remember { mutableStateListOf<TaskList>() }
    var isDragging by remember { mutableStateOf(false) }
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    val listState = rememberLazyListState()

    var showDialog by remember { mutableStateOf(false) }
    var dialogText by remember { mutableStateOf("") }
    var taskToEdit by remember { mutableStateOf<TaskList?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskList?>(null) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(showDialog) {
        if (showDialog) {
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(tasks) {
        if (!isDragging) {
            localTasks.clear()
            localTasks.addAll(tasks)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tasks") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                taskToEdit = null
                dialogText = ""
                showDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add task")
            }
        }
    ) { padding ->
        if (tasks.isEmpty() && localTasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No tasks yet. Tap + to add one.", style = MaterialTheme.typography.bodyLarge)
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
                itemsIndexed(localTasks, key = { _, task -> task.id }) { index, task ->
                    val isDraggedItem = draggedIndex == index
                    val elevation by animateDpAsState(
                        targetValue = if (isDraggedItem) 8.dp else 0.dp,
                        label = "drag_elevation"
                    )
                    DraggableStandaloneTaskCard(
                        task = task,
                        isDragged = isDraggedItem,
                        elevation = elevation,
                        dragOffsetY = if (isDraggedItem) dragOffsetY else 0f,
                        onClick = {
                            taskToEdit = task
                            dialogText = task.title
                            showDialog = true
                        },
                        onToggle = { viewModel.toggleTask(task) },
                        onDelete = { taskToDelete = task },
                        onReminderClick = { onTaskClick(task.id) },
                        onDragStart = {
                            isDragging = true
                            draggedIndex = index
                            dragOffsetY = 0f
                        },
                        onDrag = { delta ->
                            dragOffsetY += delta
                            val currentDragged = draggedIndex ?: return@DraggableStandaloneTaskCard
                            val itemHeightPx = listState.layoutInfo.visibleItemsInfo
                                .find { it.key == task.id }?.size?.toFloat() ?: 64f
                            val rawTarget = currentDragged + (dragOffsetY / itemHeightPx).toInt()
                            val targetIndex = rawTarget.coerceIn(0, localTasks.size - 1)
                            if (targetIndex != currentDragged) {
                                val moved = localTasks.removeAt(currentDragged)
                                localTasks.add(targetIndex, moved)
                                dragOffsetY -= (targetIndex - currentDragged) * itemHeightPx
                                draggedIndex = targetIndex
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                            draggedIndex = null
                            dragOffsetY = 0f
                            viewModel.reorderTasks(localTasks.toList())
                        }
                    )
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false; dialogText = "" },
                title = { Text(if (taskToEdit == null) "New Task" else "Edit Task") },
                text = {
                    OutlinedTextField(
                        value = dialogText,
                        onValueChange = { dialogText = it },
                        label = { Text("What needs to be done?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (dialogText.isNotBlank()) {
                            val edit = taskToEdit
                            if (edit == null) {
                                viewModel.createTask(dialogText)
                            } else {
                                viewModel.updateTaskTitle(edit, dialogText)
                            }
                            showDialog = false
                            dialogText = ""
                        }
                    }) { Text(if (taskToEdit == null) "Add" else "Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false; dialogText = "" }) {
                        Text("Cancel")
                    }
                }
            )
        }

        taskToDelete?.let { task ->
            AlertDialog(
                onDismissRequest = { taskToDelete = null },
                title = { Text("Move to Trash") },
                text = { Text("Are you sure you want to move \"${task.title}\" to trash?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteTask(task)
                            taskToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Move to Trash")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { taskToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun DraggableStandaloneTaskCard(
    task: TaskList,
    isDragged: Boolean,
    elevation: Dp,
    dragOffsetY: Float,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onReminderClick: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
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
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (task.isChecked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder",
                    modifier = Modifier
                        .pointerInput(task.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { currentOnDragStart() },
                                onDrag = { _, dragAmount -> currentOnDrag(dragAmount.y) },
                                onDragEnd = { currentOnDragEnd() },
                                onDragCancel = { currentOnDragEnd() }
                            )
                        }
                        .padding(8.dp)
                )

                Checkbox(
                    checked = task.isChecked,
                    onCheckedChange = { onToggle() }
                )

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    textDecoration = if (task.isChecked) TextDecoration.LineThrough else null,
                    color = if (task.isChecked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (task.isNotificationEnabled) {
                    IconButton(onClick = onReminderClick) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Edit reminder",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    IconButton(onClick = onReminderClick) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Set reminder",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete task",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
