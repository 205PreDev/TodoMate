package com.example.todomate.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.todomate.ai.EncouragementResult
import com.example.todomate.ai.GeminiService
import com.example.todomate.data.local.TodoDatabase
import com.example.todomate.data.local.TodoEntity
import com.example.todomate.data.repository.TodoRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TodoRepository
    private val geminiService = GeminiService()

    private val _sortType = MutableLiveData(SortType.DATE_DESC)
    private val _searchQuery = MutableLiveData("")

    val todos: LiveData<List<TodoEntity>>

    // AI 격려 메시지
    private val _encouragementMessage = MutableLiveData<AiMessageState>()
    val encouragementMessage: LiveData<AiMessageState> = _encouragementMessage

    sealed class AiMessageState {
        object Loading : AiMessageState()
        data class Success(val message: String) : AiMessageState()
        data class Error(val message: String) : AiMessageState()
    }

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

    /**
     * AI 격려 메시지 요청
     */
    fun requestEncouragement() {
        val todoList = todos.value ?: emptyList()

        _encouragementMessage.value = AiMessageState.Loading

        viewModelScope.launch {
            val totalCount = todoList.size
            val completedCount = todoList.count { it.isCompleted }
            val pendingCount = totalCount - completedCount
            val highPriorityPending = todoList.count { !it.isCompleted && it.priority == 2 }

            // 간단한 통계를 AI에 전달
            val stats = mapOf(
                "전체 할 일" to totalCount,
                "완료" to completedCount,
                "미완료" to pendingCount,
                "긴급(미완료)" to highPriorityPending
            )

            when (val result = geminiService.generateEncouragementForTodos(stats)) {
                is EncouragementResult.Success -> {
                    _encouragementMessage.value = AiMessageState.Success(result.message)
                }
                is EncouragementResult.Error -> {
                    _encouragementMessage.value = AiMessageState.Success(getDefaultEncouragement())
                }
            }
        }
    }

    /**
     * 기본 격려 메시지
     */
    private fun getDefaultEncouragement(): String {
        val todoList = todos.value ?: emptyList()
        val totalCount = todoList.size
        val completedCount = todoList.count { it.isCompleted }

        return when {
            totalCount == 0 -> "📝 할 일을 추가하고 하루를 시작해보세요!"
            completedCount == totalCount -> "🎉 모든 할 일을 완료했어요! 대단해요!"
            completedCount == 0 -> "💪 오늘도 화이팅! 하나씩 해나가봐요."
            completedCount > totalCount / 2 -> "👍 잘 진행하고 있어요! 조금만 더!"
            else -> "🌱 천천히 하나씩, 할 수 있어요!"
        }
    }

    enum class SortType {
        DATE_DESC, DATE_ASC, PRIORITY
    }
}
