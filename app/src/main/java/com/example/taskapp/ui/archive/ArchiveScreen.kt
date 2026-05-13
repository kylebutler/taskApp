package com.example.taskapp.ui.archive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskapp.domain.model.TaskList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    viewModel: ArchiveViewModel,
    onBack: () -> Unit,
    onListClick: (Long) -> Unit
) {
    val lists by viewModel.lists.collectAsStateWithLifecycle()
    var listToDelete by remember { mutableStateOf<TaskList?>(null) }

    if (listToDelete != null) {
        val list = listToDelete!!
        AlertDialog(
            onDismissRequest = { listToDelete = null },
            title = { Text("Move to Trash") },
            text = { Text("Are you sure you want to move \"${list.title}\" to the trash?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteList(list)
                        listToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Trash")
                }
            },
            dismissButton = {
                TextButton(onClick = { listToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Archive") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (lists.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Archive is empty.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(lists, key = { it.id }) { list ->
                    ArchiveListCard(
                        list = list,
                        onClick = { onListClick(list.id) },
                        onRestore = { viewModel.restoreList(list) },
                        onDelete = { listToDelete = list }
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchiveListCard(
    list: TaskList,
    onClick: () -> Unit,
    onRestore: () -> Unit,
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
                IconButton(onClick = onRestore) {
                    Icon(
                        imageVector = Icons.Default.Unarchive,
                        contentDescription = "Restore from archive",
                        tint = contentColor.copy(alpha = 0.8f)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Move to trash",
                        tint = contentColor.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
