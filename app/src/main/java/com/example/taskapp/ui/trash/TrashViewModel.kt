package com.example.taskapp.ui.trash

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

class TrashViewModel(private val repo: TaskRepository) : ViewModel() {

    val lists: StateFlow<List<TaskList>> = repo.getTrashLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun restoreList(list: TaskList) {
        viewModelScope.launch { repo.restoreList(list) }
    }

    fun permanentlyDeleteList(list: TaskList) {
        viewModelScope.launch { repo.permanentlyDeleteList(list) }
    }

    companion object {
        fun Factory(repo: TaskRepository): ViewModelProvider.Factory =
            ViewModelFactory { TrashViewModel(repo) }
    }
}
