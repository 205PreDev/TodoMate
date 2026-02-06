package com.example.todomate.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.todomate.data.local.TodoDatabase
import com.example.todomate.data.local.TodoEntity
import com.example.todomate.data.repository.TodoRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TodoRepository

    private val _sortType = MutableLiveData(SortType.DATE_DESC)
    private val _searchQuery = MutableLiveData("")

    val todos: LiveData<List<TodoEntity>>

    init {
        val dao = TodoDatabase.getInstance(application).todoDao()
        repository = TodoRepository(dao)

        val combined = MediatorLiveData<Pair<String, SortType>>().apply {
            addSource(_searchQuery) { query ->
                value = Pair(query, _sortType.value ?: SortType.DATE_DESC)
            }
            addSource(_sortType) { sort ->
                value = Pair(_searchQuery.value ?: "", sort)
            }
        }

        todos = combined.switchMap { (query, sortType) ->
            if (query.isBlank()) {
                when (sortType) {
                    SortType.DATE_DESC -> repository.getAllTodos()
                    SortType.DATE_ASC -> repository.getAllTodosByDateAsc()
                    SortType.PRIORITY -> repository.getAllTodosByPriority()
                }
            } else {
                repository.searchTodos(query)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortType(sortType: SortType) {
        _sortType.value = sortType
    }

    fun toggleComplete(todo: TodoEntity) {
        viewModelScope.launch {
            repository.update(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    fun deleteTodo(todo: TodoEntity) {
        viewModelScope.launch {
            repository.delete(todo)
        }
    }

    enum class SortType {
        DATE_DESC, DATE_ASC, PRIORITY
    }
}
