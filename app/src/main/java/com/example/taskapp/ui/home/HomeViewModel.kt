package com.example.taskapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskapp.data.repository.TaskRepository
import com.example.taskapp.domain.model.ListType
import com.example.taskapp.domain.model.TaskList
import com.example.taskapp.util.ViewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: TaskRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            repo.cleanOldTrash()
        }
    }

    val lists: StateFlow<List<TaskList>> = repo.getAllLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createList(title: String, type: ListType = ListType.CHECKLIST) {
        viewModelScope.launch { repo.createList(title.trim().ifEmpty { "New List" }, type) }
    }

    fun moveToTrash(list: TaskList) {
        viewModelScope.launch { repo.moveToTrash(list) }
    }

    fun archiveList(list: TaskList) {
        viewModelScope.launch { repo.moveToArchive(list) }
    }

    fun toggleLock(list: TaskList) {
        viewModelScope.launch {
            repo.updateList(list.copy(isLocked = !list.isLocked))
        }
    }

    fun deleteList(list: TaskList) {
        moveToTrash(list)
    }

    companion object {
        fun Factory(repo: TaskRepository): ViewModelProvider.Factory =
            ViewModelFactory { HomeViewModel(repo) }
    }
}
