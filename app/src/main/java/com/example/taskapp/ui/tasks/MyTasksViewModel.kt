package com.example.taskapp.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskapp.data.repository.TaskRepository
import com.example.taskapp.domain.model.ListType
import com.example.taskapp.domain.model.TaskList
import com.example.taskapp.notification.AlarmScheduler
import com.example.taskapp.util.ViewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyTasksViewModel(
    private val repo: TaskRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val tasks: StateFlow<List<TaskList>> = repo.getStandaloneTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createTask(title: String) {
        viewModelScope.launch {
            repo.createList(title.trim().ifEmpty { "New Task" }, ListType.TASK)
        }
    }

    fun toggleTask(task: TaskList) {
        viewModelScope.launch {
            repo.updateList(task.copy(isChecked = !task.isChecked))
        }
    }

    fun updateTaskTitle(task: TaskList, newTitle: String) {
        viewModelScope.launch {
            repo.updateList(task.copy(title = newTitle.trim().ifEmpty { task.title }))
        }
    }

    fun deleteTask(task: TaskList) {
        viewModelScope.launch {
            repo.moveToTrash(task)
            alarmScheduler.cancel(task.id)
        }
    }

    fun reorderTasks(reordered: List<TaskList>) {
        viewModelScope.launch {
            repo.reorderLists(reordered)
        }
    }

    companion object {
        fun Factory(repo: TaskRepository, alarmScheduler: AlarmScheduler): ViewModelProvider.Factory =
            ViewModelFactory { MyTasksViewModel(repo, alarmScheduler) }
    }
}
