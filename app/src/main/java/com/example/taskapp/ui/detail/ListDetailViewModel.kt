package com.example.taskapp.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskapp.TaskApp
import com.example.taskapp.data.repository.NotificationRepository
import com.example.taskapp.data.repository.TaskRepository
import com.example.taskapp.domain.model.IntervalUnit
import com.example.taskapp.domain.model.NotificationFrequency
import com.example.taskapp.domain.model.NotificationSetting
import com.example.taskapp.domain.model.TaskItem
import com.example.taskapp.domain.model.TaskList
import com.example.taskapp.notification.AlarmScheduler
import com.example.taskapp.notification.NotificationHelper
import com.example.taskapp.util.ViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ListDetailUiState(
    val taskList: TaskList? = null,
    val items: List<TaskItem> = emptyList(),
    val isEditingTitle: Boolean = false,
    val newItemText: String = "",
    val notificationDescription: String? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
)

private data class ListSnapshot(
    val title: String,
    val colorArgb: Int?,
    val textContent: String?,
    val items: List<TaskItem>
)

class ListDetailViewModel(
    application: Application,
    private val repo: TaskRepository,
    private val notifRepo: NotificationRepository,
    private val alarmScheduler: AlarmScheduler,
    private val listId: Long
) : AndroidViewModel(application) {

    private val app get() = getApplication<TaskApp>()

    private val _uiState = MutableStateFlow(ListDetailUiState())
    val uiState: StateFlow<ListDetailUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<ListSnapshot>()
    private val redoStack = mutableListOf<ListSnapshot>()
    private var isUndoRedoAction = false

    init {
        viewModelScope.launch {
            combine(
                repo.getListById(listId),
                repo.getItemsForList(listId),
                notifRepo.getSettingForList(listId)
            ) { list, items, setting ->
                DataSnapshot(list, items, setting)
            }.collect { snapshot ->
                _uiState.update { 
                    it.copy(
                        taskList = snapshot.list,
                        items = snapshot.items,
                        notificationDescription = formatNotificationSchedule(snapshot.setting),
                        canUndo = undoStack.isNotEmpty(),
                        canRedo = redoStack.isNotEmpty()
                    )
                }
                // Refresh any active notification for this list whenever data changes
                if (snapshot.list != null) {
                    NotificationHelper.updateIfActive(app, snapshot.list, snapshot.items)
                }
            }
        }
    }

    private data class DataSnapshot(
        val list: TaskList?,
        val items: List<TaskItem>,
        val setting: NotificationSetting?
    )

    private fun formatNotificationSchedule(setting: NotificationSetting?): String? {
        if (setting == null || !setting.isEnabled) return null

        val is24Hour = android.text.format.DateFormat.is24HourFormat(app)
        val timeStr = if (is24Hour) {
            "%02d:%02d".format(setting.hour, setting.minute)
        } else {
            val hour = if (setting.hour % 12 == 0) 12 else setting.hour % 12
            val amPm = if (setting.hour < 12) "AM" else "PM"
            "%d:%02d %s".format(hour, setting.minute, amPm)
        }

        return when (setting.frequency) {
            NotificationFrequency.INSTANT -> "Instant"
            NotificationFrequency.DAILY -> "Daily at $timeStr"
            NotificationFrequency.WEEKLY -> {
                val days = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                val selectedDays = days.filterIndexed { index, _ -> (setting.weekDaysMask and (1 shl index)) != 0 }
                if (selectedDays.isEmpty()) "Weekly (no days selected)"
                else "Weekly on ${selectedDays.joinToString(", ")} at $timeStr"
            }
            NotificationFrequency.ONE_TIME -> {
                val pattern = if (is24Hour) "MMM d, yyyy HH:mm" else "MMM d, yyyy h:mm a"
                val date = SimpleDateFormat(pattern, Locale.getDefault()).format(Date(setting.oneTimeEpochMillis))
                "One-time on $date"
            }
            NotificationFrequency.CUSTOM_INTERVAL -> {
                val unit = when (setting.intervalUnit) {
                    IntervalUnit.HOURS -> "hours"
                    IntervalUnit.DAYS -> "days"
                    IntervalUnit.WEEKS -> "weeks"
                }
                "Every ${setting.intervalValue} $unit"
            }
        }
    }

    private fun captureSnapshot(): ListSnapshot? {
        val list = _uiState.value.taskList ?: return null
        return ListSnapshot(
            title = list.title,
            colorArgb = list.colorArgb,
            textContent = list.textContent,
            items = _uiState.value.items.map { it.copy() }
        )
    }

    private fun saveHistory() {
        if (isUndoRedoAction) return
        captureSnapshot()?.let {
            undoStack.add(it)
            if (undoStack.size > 50) undoStack.removeAt(0)
            redoStack.clear()
            updateUndoRedoStates()
        }
    }

    private fun updateUndoRedoStates() {
        _uiState.update { it.copy(canUndo = undoStack.isNotEmpty(), canRedo = redoStack.isNotEmpty()) }
    }

    fun undo() {
        val current = captureSnapshot() ?: return
        if (undoStack.isEmpty()) return
        
        isUndoRedoAction = true
        val target = undoStack.removeAt(undoStack.size - 1)
        redoStack.add(current)
        
        applySnapshot(target)
    }

    fun redo() {
        val current = captureSnapshot() ?: return
        if (redoStack.isEmpty()) return
        
        isUndoRedoAction = true
        val target = redoStack.removeAt(redoStack.size - 1)
        undoStack.add(current)
        
        applySnapshot(target)
    }

    private fun applySnapshot(snapshot: ListSnapshot) {
        val list = _uiState.value.taskList ?: return
        viewModelScope.launch {
            val updatedList = list.copy(
                title = snapshot.title,
                colorArgb = snapshot.colorArgb,
                textContent = snapshot.textContent
            )
            repo.updateList(updatedList)
            repo.syncItems(listId, snapshot.items)
            
            // Refresh notification after undo/redo
            NotificationHelper.updateIfActive(app, updatedList, snapshot.items)

            isUndoRedoAction = false
            updateUndoRedoStates()
        }
    }

    fun setTitleEditing(editing: Boolean) = _uiState.update { it.copy(isEditingTitle = editing) }

    fun toggleLock() {
        val list = _uiState.value.taskList ?: return
        viewModelScope.launch {
            val updatedList = list.copy(isLocked = !list.isLocked)
            repo.updateList(updatedList)
            NotificationHelper.updateIfActive(app, updatedList, _uiState.value.items)
        }
    }

    fun moveToTrash() {
        val list = _uiState.value.taskList ?: return
        viewModelScope.launch {
            repo.moveToTrash(list)
            alarmScheduler.cancel(listId)
        }
    }

    fun saveTitle(newTitle: String) {
        val list = _uiState.value.taskList ?: return
        val trimmed = newTitle.trim()
        val finalTitle = trimmed.ifEmpty { list.title }
        if (list.title == finalTitle) return
        
        saveHistory()
        viewModelScope.launch {
            val updatedList = list.copy(title = finalTitle)
            repo.updateList(updatedList)
            // Explicitly refresh notification for immediate feedback on title change
            NotificationHelper.updateIfActive(app, updatedList, _uiState.value.items)
        }
        _uiState.update { it.copy(isEditingTitle = false) }
    }

    fun saveColor(colorArgb: Int) {
        val list = _uiState.value.taskList ?: return
        if (list.colorArgb == colorArgb) return
        saveHistory()
        viewModelScope.launch {
            val updatedList = list.copy(colorArgb = colorArgb)
            repo.updateList(updatedList)
            // Color change might affect notification visuals in the future
            NotificationHelper.updateIfActive(app, updatedList, _uiState.value.items)
        }
    }

    fun updateTextContent(text: String) {
        val list = _uiState.value.taskList ?: return
        if (list.textContent == text) return
        saveHistory()
        viewModelScope.launch {
            val updatedList = list.copy(textContent = text)
            repo.updateList(updatedList)
            NotificationHelper.updateIfActive(app, updatedList, _uiState.value.items)
        }
    }

    fun setNewItemText(text: String) = _uiState.update { it.copy(newItemText = text) }

    fun addItem() {
        val text = _uiState.value.newItemText.trim()
        if (text.isEmpty()) return
        saveHistory()
        val nextPos = _uiState.value.items.size
        viewModelScope.launch { repo.addItem(listId, text, nextPos) }
        _uiState.update { it.copy(newItemText = "") }
    }

    fun toggleItem(item: TaskItem) {
        saveHistory()
        viewModelScope.launch { repo.updateItem(item.copy(isChecked = !item.isChecked)) }
    }

    fun updateItemText(item: TaskItem, newText: String) {
        if (item.text == newText) return
        saveHistory()
        viewModelScope.launch { repo.updateItem(item.copy(text = newText)) }
    }

    fun deleteItem(item: TaskItem) {
        saveHistory()
        viewModelScope.launch { repo.deleteItem(item) }
    }

    fun changeItemIndent(item: TaskItem, delta: Int) {
        val currentItems = _uiState.value.items
        val index = currentItems.indexOfFirst { it.id == item.id }
        if (index == -1) return

        val newIndent = (item.indentLevel + delta).coerceIn(0, 4)
        if (newIndent == item.indentLevel) return

        // Optional: Check if it can be indented (must have a task above it to be a subtask)
        if (delta > 0 && index == 0) return 

        saveHistory()
        viewModelScope.launch {
            repo.updateItem(item.copy(indentLevel = newIndent))
        }
    }

    fun reorderItems(reorderedItems: List<TaskItem>) {
        if (reorderedItems == _uiState.value.items) return
        saveHistory()
        viewModelScope.launch { repo.reorderItems(reorderedItems) }
    }

    companion object {
        fun Factory(app: TaskApp, repo: TaskRepository, notifRepo: NotificationRepository, alarmScheduler: AlarmScheduler, listId: Long): ViewModelProvider.Factory =
            ViewModelFactory { ListDetailViewModel(app, repo, notifRepo, alarmScheduler, listId) }
    }
}
