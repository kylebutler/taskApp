package com.example.taskapp.ui.home

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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskapp.domain.model.ListType
import com.example.taskapp.domain.model.TaskList
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onListClick: (Long) -> Unit,
    onTrashClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val lists by viewModel.lists.collectAsStateWithLifecycle()
    val localLists = remember { mutableStateListOf<TaskList>() }
    var isDragging by remember { mutableStateOf(false) }
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    val listState = rememberLazyListState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newListTitle by remember { mutableStateOf("") }
    var newListType by remember { mutableStateOf(ListType.CHECKLIST) }

    var listToDelete by remember { mutableStateOf<TaskList?>(null) }
    var listToArchive by remember { mutableStateOf<TaskList?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(lists) {
        if (!isDragging) {
            localLists.clear()
            localLists.addAll(lists)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "TaskApp",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
                NavigationDrawerItem(
                    label = { Text("My Lists") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Menu, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Archive") },
                    selected = false,
                    onClick = { 
                        scope.launch { 
                            drawerState.close()
                            onArchiveClick()
                        }
                    },
                    icon = { Icon(Icons.Default.Archive, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Trash") },
                    selected = false,
                    onClick = { 
                        scope.launch { 
                            drawerState.close()
                            onTrashClick()
                        }
                    },
                    icon = { Icon(Icons.Default.DeleteForever, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onSettingsClick()
                        }
                    },
                    icon = { Icon(Icons.Default.Settings, null) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("My Lists") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "New list")
                }
            }
        ) { padding ->
            if (lists.isEmpty() && localLists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No lists yet. Tap + to create one.", style = MaterialTheme.typography.bodyLarge)
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
                    itemsIndexed(localLists, key = { _, list -> list.id }) { index, list ->
                        val isDraggedItem = draggedIndex == index
                        val elevation by animateDpAsState(
                            targetValue = if (isDraggedItem) 8.dp else 0.dp,
                            label = "drag_elevation"
                        )
                        DraggableListCard(
                            list = list,
                            isDragged = isDraggedItem,
                            elevation = elevation,
                            dragOffsetY = if (isDraggedItem) dragOffsetY else 0f,
                            onClick = { onListClick(list.id) },
                            onArchive = { listToArchive = list },
                            onDelete = { listToDelete = list },
                            onDragStart = {
                                isDragging = true
                                draggedIndex = index
                                dragOffsetY = 0f
                            },
                            onDrag = { delta ->
                                dragOffsetY += delta
                                val currentDragged = draggedIndex ?: return@DraggableListCard
                                val itemHeightPx = listState.layoutInfo.visibleItemsInfo
                                    .find { it.key == list.id }?.size?.toFloat() ?: 80f
                                val rawTarget = currentDragged + (dragOffsetY / itemHeightPx).toInt()
                                val targetIndex = rawTarget.coerceIn(0, localLists.size - 1)
                                if (targetIndex != currentDragged) {
                                    val moved = localLists.removeAt(currentDragged)
                                    localLists.add(targetIndex, moved)
                                    dragOffsetY -= (targetIndex - currentDragged) * itemHeightPx
                                    draggedIndex = targetIndex
                                }
                            },
                            onDragEnd = {
                                isDragging = false
                                draggedIndex = null
                                dragOffsetY = 0f
                                viewModel.reorderLists(localLists.toList())
                            }
                        )
                    }
                }
            }

            if (showCreateDialog) {
                AlertDialog(
                    onDismissRequest = { 
                        showCreateDialog = false
                        newListTitle = ""
                        newListType = ListType.CHECKLIST
                    },
                    title = { Text("New List") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = newListTitle,
                                onValueChange = { newListTitle = it },
                                label = { Text("List name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Column {
                                Text("Type", style = MaterialTheme.typography.labelLarge)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = newListType == ListType.CHECKLIST,
                                            onClick = { newListType = ListType.CHECKLIST }
                                        )
                                        Text("Checklist", modifier = Modifier.padding(start = 4.dp))
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = newListType == ListType.TEXT,
                                            onClick = { newListType = ListType.TEXT }
                                        )
                                        Text("Text", modifier = Modifier.padding(start = 4.dp))
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.createList(newListTitle, newListType)
                            showCreateDialog = false
                            newListTitle = ""
                            newListType = ListType.CHECKLIST
                        }) { Text("Create") }
                    },
                    dismissButton = {
                        TextButton(onClick = { 
                            showCreateDialog = false
                            newListTitle = ""
                            newListType = ListType.CHECKLIST
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            listToDelete?.let { list ->
                AlertDialog(
                    onDismissRequest = { listToDelete = null },
                    title = { Text("Move to Trash") },
                    text = { Text("Are you sure you want to move \"${list.title}\" to trash? It will be automatically deleted after 30 days.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.moveToTrash(list)
                                listToDelete = null
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Move to Trash")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { listToDelete = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            listToArchive?.let { list ->
                AlertDialog(
                    onDismissRequest = { listToArchive = null },
                    title = { Text("Archive List") },
                    text = { Text("Are you sure you want to move \"${list.title}\" to the archive?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.archiveList(list)
                                listToArchive = null
                            }
                        ) {
                            Text("Archive")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { listToArchive = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DraggableListCard(
    list: TaskList,
    isDragged: Boolean,
    elevation: Dp,
    dragOffsetY: Float,
    onClick: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val backgroundColor = list.colorArgb?.let { Color(it) } ?: MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (list.colorArgb != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

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
                containerColor = backgroundColor,
                contentColor = contentColor
            )
        ) {
            Row(
                modifier = Modifier.padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder",
                    tint = contentColor.copy(alpha = 0.6f),
                    modifier = Modifier
                        .pointerInput(list.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { currentOnDragStart() },
                                onDrag = { _, dragAmount -> currentOnDrag(dragAmount.y) },
                                onDragEnd = { currentOnDragEnd() },
                                onDragCancel = { currentOnDragEnd() }
                            )
                        }
                        .padding(8.dp)
                )

                Text(
                    text = list.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    color = contentColor
                )
                if (list.isNotificationEnabled) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notification enabled",
                        tint = contentColor.copy(alpha = 0.8f),
                        modifier = Modifier.padding(end = 8.dp).size(20.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (list.isLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = contentColor.copy(alpha = 0.6f),
                            modifier = Modifier.padding(end = 8.dp).size(20.dp)
                        )
                    }
                    IconButton(onClick = onArchive) {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = "Archive list",
                            tint = contentColor.copy(alpha = 0.8f)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete list",
                            tint = contentColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
