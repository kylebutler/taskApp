package com.example.taskapp.ui.detail

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskapp.domain.model.TaskItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    viewModel: ListDetailViewModel,
    onBack: () -> Unit,
    onOpenNotificationSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val localItems = remember { mutableStateListOf<TaskItem>() }
    var isDragging by remember { mutableStateOf(false) }
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    val listState = rememberLazyListState()

    // Sync local mirror from DB state only when not mid-drag
    LaunchedEffect(uiState.items) {
        if (!isDragging) {
            localItems.clear()
            localItems.addAll(uiState.items.sortedBy { it.isChecked })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (uiState.isEditingTitle) {
                        var editText by remember { mutableStateOf(uiState.taskList?.title ?: "") }
                        OutlinedTextField(
                            value = editText,
                            onValueChange = { editText = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        DisposableEffect(Unit) {
                            onDispose { if (editText.isNotBlank()) viewModel.saveTitle(editText) }
                        }
                    } else {
                        TextButton(onClick = { viewModel.setTitleEditing(true) }) {
                            Text(
                                text = uiState.taskList?.title ?: "",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenNotificationSettings) {
                        Icon(Icons.Default.Notifications, "Notification settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.newItemText,
                    onValueChange = viewModel::setNewItemText,
                    placeholder = { Text("New task…") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = viewModel::addItem) {
                    Icon(Icons.Default.Add, "Add task")
                }
            }

            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                val activeItems = localItems.filter { !it.isChecked }
                val completedItems = localItems.filter { it.isChecked }

                itemsIndexed(activeItems, key = { _, item -> item.id }) { index, item ->
                    val isDraggedItem = draggedIndex == index
                    val elevation by animateDpAsState(
                        targetValue = if (isDraggedItem) 8.dp else 0.dp,
                        label = "drag_elevation"
                    )
                    DraggableTaskRow(
                        item = item,
                        isDragged = isDraggedItem,
                        elevation = elevation,
                        dragOffsetY = if (isDraggedItem) dragOffsetY else 0f,
                        onCheckedChange = { viewModel.toggleItem(item) },
                        onDelete = { viewModel.deleteItem(item) },
                        onDragStart = {
                            isDragging = true
                            draggedIndex = index
                            dragOffsetY = 0f
                        },
                        onDrag = { delta ->
                            dragOffsetY += delta
                            val currentDragged = draggedIndex ?: return@DraggableTaskRow
                            val itemHeightPx = listState.layoutInfo.visibleItemsInfo
                                .firstOrNull()?.size?.toFloat() ?: 56f
                            val rawTarget = currentDragged + (dragOffsetY / itemHeightPx).toInt()
                            val targetIndex = rawTarget.coerceIn(0, activeItems.size - 1)
                            if (targetIndex != currentDragged) {
                                val moved = localItems.removeAt(currentDragged)
                                localItems.add(targetIndex, moved)
                                dragOffsetY -= (targetIndex - currentDragged) * itemHeightPx
                                draggedIndex = targetIndex
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                            draggedIndex = null
                            dragOffsetY = 0f
                            viewModel.reorderItems(localItems.toList())
                        }
                    )
                }

                if (completedItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    itemsIndexed(completedItems, key = { _, item -> item.id }) { index, item ->
                        val actualIndex = index + activeItems.size
                        val isDraggedItem = draggedIndex == actualIndex
                        val elevation by animateDpAsState(
                            targetValue = if (isDraggedItem) 8.dp else 0.dp,
                            label = "drag_elevation"
                        )
                        DraggableTaskRow(
                            item = item,
                            isDragged = isDraggedItem,
                            elevation = elevation,
                            dragOffsetY = if (isDraggedItem) dragOffsetY else 0f,
                            onCheckedChange = { viewModel.toggleItem(item) },
                            onDelete = { viewModel.deleteItem(item) },
                            onDragStart = {
                                isDragging = true
                                draggedIndex = actualIndex
                                dragOffsetY = 0f
                            },
                            onDrag = { delta ->
                                dragOffsetY += delta
                                val currentDragged = draggedIndex ?: return@DraggableTaskRow
                                val itemHeightPx = listState.layoutInfo.visibleItemsInfo
                                    .firstOrNull()?.size?.toFloat() ?: 56f
                                val rawTarget = currentDragged + (dragOffsetY / itemHeightPx).toInt()
                                val targetIndex = rawTarget.coerceIn(activeItems.size, localItems.size - 1)
                                if (targetIndex != currentDragged) {
                                    val moved = localItems.removeAt(currentDragged)
                                    localItems.add(targetIndex, moved)
                                    dragOffsetY -= (targetIndex - currentDragged) * itemHeightPx
                                    draggedIndex = targetIndex
                                }
                            },
                            onDragEnd = {
                                isDragging = false
                                draggedIndex = null
                                dragOffsetY = 0f
                                viewModel.reorderItems(localItems.toList())
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DraggableTaskRow(
    item: TaskItem,
    isDragged: Boolean,
    elevation: Dp,
    dragOffsetY: Float,
    onCheckedChange: () -> Unit,
    onDelete: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    Surface(
        shadowElevation = elevation,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = if (isDragged) dragOffsetY else 0f }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Drag to reorder",
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { onDragStart() },
                            onDrag = { _, dragAmount -> onDrag(dragAmount.y) },
                            onDragEnd = onDragEnd,
                            onDragCancel = onDragEnd
                        )
                    }
                    .padding(8.dp)
            )
            Checkbox(checked = item.isChecked, onCheckedChange = { onCheckedChange() })
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                ),
                color = if (item.isChecked) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete task", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
