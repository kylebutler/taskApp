package com.example.taskapp.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onArchiveClick: () -> Unit
) {
    val lists by viewModel.lists.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newListTitle by remember { mutableStateOf("") }
    var newListType by remember { mutableStateOf(ListType.CHECKLIST) }

    var listToDelete by remember { mutableStateOf<TaskList?>(null) }
    var listToArchive by remember { mutableStateOf<TaskList?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
            if (lists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No lists yet. Tap + to create one.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp,
                        top = padding.calculateTopPadding() + 8.dp,
                        bottom = padding.calculateBottomPadding() + 80.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(lists, key = { it.id }) { list ->
                        TaskListCard(
                            list = list,
                            onClick = { onListClick(list.id) },
                            onArchive = { listToArchive = list },
                            onDelete = { listToDelete = list }
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
private fun TaskListCard(
    list: TaskList,
    onClick: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val backgroundColor = list.colorArgb?.let { Color(it) } ?: MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (list.colorArgb != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            Row {
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
