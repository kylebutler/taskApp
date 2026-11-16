package com.example.taskapp.ui.detail

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskapp.domain.model.ListType
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
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val listState = rememberLazyListState()
    var showColorPicker by remember { mutableStateOf(false) }
    var showTrashConfirm by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    var noteText by remember(uiState.taskList?.textContent) { 
        mutableStateOf(uiState.taskList?.textContent ?: "") 
    }

    val textMatches = remember(noteText, searchQuery) {
        if (searchQuery.isEmpty()) emptyList()
        else {
            val indices = mutableListOf<Int>()
            var idx = noteText.indexOf(searchQuery, ignoreCase = true)
            while (idx >= 0) {
                indices.add(idx)
                idx = noteText.indexOf(searchQuery, idx + searchQuery.length, ignoreCase = true)
            }
            indices
        }
    }
    var textMatchIndex by remember(textMatches) { mutableStateOf(if (textMatches.isNotEmpty()) 0 else -1) }

    val presetColors = listOf(
        Color(0xFFB71C1C), // Deep Red
        Color(0xFFE65100), // Deep Orange
        Color(0xFFF57F17), // Deep Yellow/Gold
        Color(0xFF1B5E20), // Deep Green
        Color(0xFF0D47A1), // Deep Blue
        Color(0xFF4A148C), // Deep Purple
        Color(0xFF424242)  // Deep Gray
    )

    val backgroundColor = uiState.taskList?.colorArgb?.let { Color(it) } ?: MaterialTheme.colorScheme.surface
    val defaultSurfaceColor = MaterialTheme.colorScheme.surface

    // Determine content color based on background
    val contentColor = if (uiState.taskList?.colorArgb != null) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryContentColor = if (uiState.taskList?.colorArgb != null) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.secondary
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Sync local mirror from DB state only when not mid-drag
    LaunchedEffect(uiState.items) {
        if (!isDragging) {
            val sorted = uiState.items.sortedBy { it.isChecked }
            if (localItems.size == sorted.size) {
                sorted.forEachIndexed { index, item ->
                    if (localItems[index] != item) localItems[index] = item
                }
            } else {
                localItems.clear()
                localItems.addAll(sorted)
            }
        }
    }

    if (showTrashConfirm) {
        AlertDialog(
            onDismissRequest = { showTrashConfirm = false },
            title = { Text("Move to Trash") },
            text = { Text("Are you sure you want to move this list to trash?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.moveToTrash()
                        showTrashConfirm = false
                        onBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Move to Trash")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTrashConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = backgroundColor,
        contentColor = contentColor,
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        val searchFocusRequester = remember { FocusRequester() }
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(searchFocusRequester),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = contentColor),
                                cursorBrush = SolidColor(contentColor),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text("Search in list...", color = secondaryContentColor)
                                    }
                                    innerTextField()
                                }
                            )
                            if (uiState.taskList?.type == ListType.TEXT && textMatches.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        if (textMatchIndex > 0) textMatchIndex--
                                        else textMatchIndex = textMatches.size - 1
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous match", tint = contentColor)
                                }
                                IconButton(
                                    onClick = {
                                        if (textMatchIndex < textMatches.size - 1) textMatchIndex++
                                        else textMatchIndex = 0
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next match", tint = contentColor)
                                }
                            }
                        }
                        LaunchedEffect(Unit) {
                            searchFocusRequester.requestFocus()
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    if (isSearchActive) {
                        IconButton(onClick = { 
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close search",
                                tint = contentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { isSearchActive = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = contentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.undo() },
                            enabled = uiState.canUndo && uiState.taskList?.isLocked == false,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Undo",
                                tint = if (uiState.canUndo && uiState.taskList?.isLocked == false) contentColor else contentColor.copy(alpha = 0.38f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.redo() },
                            enabled = uiState.canRedo && uiState.taskList?.isLocked == false,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "Redo",
                                tint = if (uiState.canRedo && uiState.taskList?.isLocked == false) contentColor else contentColor.copy(alpha = 0.38f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { 
                                viewModel.toggleLock()
                                showColorPicker = false
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState.taskList?.isLocked == true) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = if (uiState.taskList?.isLocked == true) "Unlock" else "Lock",
                                tint = contentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { showTrashConfirm = true },
                            enabled = uiState.taskList?.isLocked == false,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Move to Trash",
                                tint = if (uiState.taskList?.isLocked == false) contentColor else contentColor.copy(alpha = 0.38f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { showColorPicker = !showColorPicker },
                            enabled = uiState.taskList?.isLocked == false,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Pick color",
                                tint = if (uiState.taskList?.isLocked == false) contentColor else contentColor.copy(alpha = 0.38f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onOpenNotificationSettings,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notification settings",
                                tint = contentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = contentColor,
                    navigationIconContentColor = contentColor,
                    actionIconContentColor = contentColor
                )
            )
        }
    ) { padding ->
        val list = uiState.taskList
        val isLocked = list?.isLocked == true
        Column(modifier = Modifier.padding(padding)) {
            // Title row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (uiState.isEditingTitle) {
                    var editText by remember { mutableStateOf(uiState.taskList?.title ?: "") }
                    BasicTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(color = contentColor),
                        cursorBrush = SolidColor(contentColor),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { 
                            if (editText.isNotBlank()) viewModel.saveTitle(editText)
                            viewModel.setTitleEditing(false)
                        })
                    )
                    DisposableEffect(Unit) {
                        onDispose { if (editText.isNotBlank()) viewModel.saveTitle(editText) }
                    }
                } else {
                    Text(
                        text = uiState.taskList?.title ?: "",
                        style = MaterialTheme.typography.headlineMedium,
                        color = contentColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isLocked) { viewModel.setTitleEditing(true) }
                    )
                }
            }

            if (showColorPicker && !isLocked) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        Surface(
                            onClick = { viewModel.saveColor(defaultSurfaceColor.toArgb()) },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.size(36.dp)
                        ) {}
                    }
                    items(presetColors) { color ->
                        Surface(
                            onClick = { viewModel.saveColor(color.toArgb()) },
                            shape = CircleShape,
                            color = color,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            modifier = Modifier.size(36.dp)
                        ) {}
                    }
                }
            }

            uiState.notificationDescription?.let { description ->
                Text(
                    text = "🔔 $description",
                    style = MaterialTheme.typography.labelMedium,
                    color = secondaryContentColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (list?.type == ListType.TEXT) {
                val scrollState = rememberScrollState()
                var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                
                LaunchedEffect(textMatchIndex, textLayoutResult) {
                    if (textMatchIndex in textMatches.indices && textLayoutResult != null) {
                        val start = textMatches[textMatchIndex]
                        val boundingBox = textLayoutResult!!.getBoundingBox(start)
                        val scrollToY = (boundingBox.top - 100f).coerceAtLeast(0f).toInt()
                        scrollState.animateScrollTo(scrollToY)
                    }
                }
                
                val searchVisualTransformation = remember(searchQuery, textMatches, textMatchIndex, contentColor) {
                    VisualTransformation { text ->
                        if (searchQuery.isEmpty() || textMatches.isEmpty()) {
                            TransformedText(text, OffsetMapping.Identity)
                        } else {
                            val annotated = buildAnnotatedString {
                                append(text.text)
                                textMatches.forEachIndexed { index, start ->
                                    val end = start + searchQuery.length
                                    val isActive = index == textMatchIndex
                                    val highlightColor = if (isActive) Color(0xFFFF9800).copy(alpha = 0.5f) else contentColor.copy(alpha = 0.2f)
                                    addStyle(
                                        style = SpanStyle(background = highlightColor),
                                        start = start,
                                        end = end
                                    )
                                }
                            }
                            TransformedText(annotated, OffsetMapping.Identity)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                ) {
                    BasicTextField(
                        value = noteText,
                        onValueChange = { if (!isLocked) noteText = it },
                        readOnly = isLocked,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = contentColor),
                        cursorBrush = SolidColor(contentColor),
                        visualTransformation = searchVisualTransformation,
                        onTextLayout = { textLayoutResult = it },
                        decorationBox = { innerTextField ->
                            if (noteText.isEmpty()) {
                                Text("Start typing your note...", color = secondaryContentColor)
                            }
                            innerTextField()
                        }
                    )
                }
                DisposableEffect(list.id) {
                    onDispose { if (!isLocked) viewModel.updateTextContent(noteText) }
                }
            } else {
                if (!isLocked) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.newItemText,
                            onValueChange = viewModel::setNewItemText,
                            placeholder = { Text("New task…", color = secondaryContentColor) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = viewModel::addItem) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add task",
                                tint = contentColor
                            )
                        }
                    }
                }

                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    val activeItems = localItems.filter { !it.isChecked && (searchQuery.isEmpty() || it.text.contains(searchQuery, ignoreCase = true)) }
                    val completedItems = localItems.filter { it.isChecked && (searchQuery.isEmpty() || it.text.contains(searchQuery, ignoreCase = true)) }
                    val isDragEnabled = !isLocked && searchQuery.isEmpty()

                    itemsIndexed(activeItems, key = { _, item -> item.id }) { index, item ->
                        val isDraggedItem = draggedIndex == index
                        val elevation by animateDpAsState(
                            targetValue = if (isDraggedItem) 8.dp else 0.dp,
                            label = "drag_elevation"
                        )
                        DraggableTaskRow(
                            item = item,
                            isDragged = isDraggedItem,
                            isLocked = isLocked,
                            isDragEnabled = isDragEnabled,
                            elevation = elevation,
                            dragOffsetY = if (isDraggedItem) dragOffsetY else 0f,
                            contentColor = contentColor,
                            onCheckedChange = { 
                                keyboardController?.hide()
                                focusManager.clearFocus(force = true)
                                viewModel.toggleItem(item) 
                            },
                            onTextChange = { viewModel.updateItemText(item, it) },
                            onDelete = { viewModel.deleteItem(item) },
                            onIndentChanged = { delta -> viewModel.changeItemIndent(item, delta) },
                            onDragStart = {
                                if (isDragEnabled) {
                                    isDragging = true
                                    draggedIndex = index
                                    dragOffsetY = 0f
                                }
                            },
                            onDrag = { delta ->
                                dragOffsetY += delta
                                val currentDragged = draggedIndex ?: return@DraggableTaskRow
                                val itemHeightPx = listState.layoutInfo.visibleItemsInfo
                                    .find { it.key == item.id }?.size?.toFloat() ?: 56f
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
                                color = secondaryContentColor
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
                                isLocked = isLocked,
                                isDragEnabled = isDragEnabled,
                                elevation = elevation,
                                dragOffsetY = if (isDraggedItem) dragOffsetY else 0f,
                                contentColor = contentColor,
                                onCheckedChange = { 
                                    keyboardController?.hide()
                                    focusManager.clearFocus(force = true)
                                    viewModel.toggleItem(item) 
                                },
                                onTextChange = { viewModel.updateItemText(item, it) },
                                onDelete = { viewModel.deleteItem(item) },
                                onIndentChanged = { delta -> viewModel.changeItemIndent(item, delta) },
                                onDragStart = {
                                    if (isDragEnabled) {
                                        isDragging = true
                                        draggedIndex = actualIndex
                                        dragOffsetY = 0f
                                    }
                                },
                                onDrag = { delta ->
                                    dragOffsetY += delta
                                    val currentDragged = draggedIndex ?: return@DraggableTaskRow
                                    val itemHeightPx = listState.layoutInfo.visibleItemsInfo
                                        .find { it.key == item.id }?.size?.toFloat() ?: 56f
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
}

@Composable
private fun DraggableTaskRow(
    item: TaskItem,
    isDragged: Boolean,
    isLocked: Boolean,
    isDragEnabled: Boolean,
    elevation: Dp,
    dragOffsetY: Float,
    contentColor: Color,
    onCheckedChange: () -> Unit,
    onTextChange: (String) -> Unit,
    onDelete: () -> Unit,
    onIndentChanged: (Int) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    var editText by remember(item.text) { mutableStateOf(item.text) }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnIndentChanged by rememberUpdatedState(onIndentChanged)

    var horizontalDragOffset by remember { mutableFloatStateOf(0f) }
    val indentStepPx = 40f // threshold to trigger indent/outdent

    // Force clear focus if this item becomes checked
    LaunchedEffect(item.isChecked) {
        if (item.isChecked) {
            focusManager.clearFocus(force = true)
        }
    }

    Surface(
        shadowElevation = elevation,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = if (isDragged) dragOffsetY else 0f }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .padding(start = (item.indentLevel * 24).dp)
                .pointerInput(item.id) {
                    if (isDragEnabled) {
                        detectHorizontalDragGestures(
                            onDragStart = { horizontalDragOffset = 0f },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                horizontalDragOffset += dragAmount
                                if (horizontalDragOffset > indentStepPx) {
                                    currentOnIndentChanged(1)
                                    horizontalDragOffset = 0f
                                } else if (horizontalDragOffset < -indentStepPx) {
                                    currentOnIndentChanged(-1)
                                    horizontalDragOffset = 0f
                                }
                            },
                            onDragEnd = { horizontalDragOffset = 0f },
                            onDragCancel = { horizontalDragOffset = 0f }
                        )
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isDragEnabled) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder",
                    tint = contentColor.copy(alpha = 0.6f),
                    modifier = Modifier
                        .pointerInput(item.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { currentOnDragStart() },
                                onDrag = { _, dragAmount -> currentOnDrag(dragAmount.y) },
                                onDragEnd = { currentOnDragEnd() },
                                onDragCancel = { currentOnDragEnd() }
                            )
                        }
                        .padding(8.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(40.dp))
            }
            
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { 
                    if (!isLocked) {
                        focusManager.clearFocus(force = true)
                        onCheckedChange() 
                    }
                },
                enabled = !isLocked
            )
            BasicTextField(
                value = editText,
                onValueChange = { if (!isLocked) editText = it },
                readOnly = isLocked || item.isChecked,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
                    .focusRequester(focusRequester)
                    .focusProperties { canFocus = !item.isChecked && !isLocked },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (item.isChecked) {
                        contentColor.copy(alpha = 0.6f)
                    } else {
                        contentColor
                    }
                ),
                cursorBrush = SolidColor(contentColor),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onTextChange(editText)
                        focusManager.clearFocus()
                    }
                )
            )
            // Trigger save on focus lost as well
            DisposableEffect(item.id) {
                onDispose {
                    if (!isLocked && editText != item.text) {
                        onTextChange(editText)
                    }
                }
            }
            if (!isLocked) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete task",
                        tint = if (contentColor == Color.White) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
