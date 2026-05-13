package com.example.taskapp.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskapp.data.repository.TaskRepository
import com.example.taskapp.domain.model.TaskList
import com.example.taskapp.util.ViewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArchiveViewModel(private val repo: TaskRepository) : ViewModel() {

    val lists: StateFlow<List<TaskList>> = repo.getArchivedLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun restoreList(list: TaskList) {
        viewModelScope.launch { repo.restoreFromArchive(list) }
    }

    fun deleteList(list: TaskList) {
        viewModelScope.launch { repo.moveToTrash(list) }
    }

    companion object {
        fun Factory(repo: TaskRepository): ViewModelProvider.Factory =
            ViewModelFactory { ArchiveViewModel(repo) }
    }
}
