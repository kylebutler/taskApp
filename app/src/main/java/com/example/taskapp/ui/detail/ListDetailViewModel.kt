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
    val notificationDescription: String? = null
)

class ListDetailViewModel(
    application: Application,
    private val repo: TaskRepository,
    private val notifRepo: NotificationRepository,
    private val listId: Long
) : AndroidViewModel(application) {

    private val app get() = getApplication<TaskApp>()

    private val _uiState = MutableStateFlow(ListDetailUiState())
    val uiState: StateFlow<ListDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repo.getListById(listId),
                repo.getItemsForList(listId),
                notifRepo.getSettingForList(listId)
            ) { list, items, setting ->
                _uiState.value.copy(
                    taskList = list,
                    items = items,
                    notificationDescription = formatNotificationSchedule(setting)
                )
            }.collect { state ->
                _uiState.value = state
                // Refresh any active notification for this list whenever data changes
                if (state.taskList != null) {
                    NotificationHelper.updateIfActive(app, state.taskList, state.items)
                }
            }
        }
    }

    private fun formatNotificationSchedule(setting: NotificationSetting?): String? {
        if (setting == null || !setting.isEnabled) return null

        val timeStr = "%02d:%02d".format(setting.hour, setting.minute)

        return when (setting.frequency) {
            NotificationFrequency.DAILY -> "Daily at $timeStr"
            NotificationFrequency.WEEKLY -> {
                val days = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                val selectedDays = days.filterIndexed { index, _ -> (setting.weekDaysMask and (1 shl index)) != 0 }
                if (selectedDays.isEmpty()) "Weekly (no days selected)"
                else "Weekly on ${selectedDays.joinToString(", ")} at $timeStr"
            }
            NotificationFrequency.ONE_TIME -> {
                val date = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date(setting.oneTimeEpochMillis))
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

    fun setTitleEditing(editing: Boolean) = _uiState.update { it.copy(isEditingTitle = editing) }

    fun toggleLock() {
        val list = _uiState.value.taskList ?: return
        viewModelScope.launch {
            repo.updateList(list.copy(isLocked = !list.isLocked))
        }
    }

    fun moveToTrash() {
        val list = _uiState.value.taskList ?: return
        viewModelScope.launch {
            repo.moveToTrash(list)
        }
    }

    fun saveTitle(newTitle: String) {
        val list = _uiState.value.taskList ?: return
        viewModelScope.launch {
            repo.updateList(list.copy(title = newTitle.trim().ifEmpty { list.title }))
        }
        _uiState.update { it.copy(isEditingTitle = false) }
    }

    fun saveColor(colorArgb: Int) {
        val list = _uiState.value.taskList ?: return
        viewModelScope.launch {
            repo.updateList(list.copy(colorArgb = colorArgb))
        }
    }

    fun updateTextContent(text: String) {
        val list = _uiState.value.taskList ?: return
        if (list.textContent == text) return
        viewModelScope.launch {
            repo.updateList(list.copy(textContent = text))
        }
    }

    fun setNewItemText(text: String) = _uiState.update { it.copy(newItemText = text) }

    fun addItem() {
        val text = _uiState.value.newItemText.trim()
        if (text.isEmpty()) return
        val nextPos = _uiState.value.items.size
        viewModelScope.launch { repo.addItem(listId, text, nextPos) }
        _uiState.update { it.copy(newItemText = "") }
    }

    fun toggleItem(item: TaskItem) {
        viewModelScope.launch { repo.updateItem(item.copy(isChecked = !item.isChecked)) }
    }

    fun updateItemText(item: TaskItem, newText: String) {
        if (item.text == newText) return
        viewModelScope.launch { repo.updateItem(item.copy(text = newText)) }
    }

    fun deleteItem(item: TaskItem) {
        viewModelScope.launch { repo.deleteItem(item) }
    }

    fun reorderItems(reorderedItems: List<TaskItem>) {
        viewModelScope.launch { repo.reorderItems(reorderedItems) }
    }

    companion object {
        fun Factory(app: Application, repo: TaskRepository, notifRepo: NotificationRepository, listId: Long): ViewModelProvider.Factory =
            ViewModelFactory { ListDetailViewModel(app, repo, notifRepo, listId) }
    }
}
