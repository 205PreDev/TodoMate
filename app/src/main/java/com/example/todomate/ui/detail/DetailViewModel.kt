package com.example.todomate.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.todomate.data.local.LifeAreaEntity
import com.example.todomate.data.local.TodoDatabase
import com.example.todomate.data.local.TodoEntity
import com.example.todomate.data.repository.DashboardRepository
import com.example.todomate.data.repository.TodoRepository
import kotlinx.coroutines.launch

class DetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TodoRepository
    private val dashboardRepository: DashboardRepository

    val lifeAreas: LiveData<List<LifeAreaEntity>>

    init {
        val database = TodoDatabase.getInstance(application)
        repository = TodoRepository(database.todoDao())
        dashboardRepository = DashboardRepository(
            database.lifeAreaDao(),
            database.weeklyGoalDao()
        )
        lifeAreas = dashboardRepository.getAllLifeAreas()
    }

    fun getTodoById(id: Long): LiveData<TodoEntity?> {
        val result = MutableLiveData<TodoEntity?>()
        viewModelScope.launch {
            result.postValue(repository.getTodoById(id))
        }
        return result
    }

    fun insert(todo: TodoEntity) {
        viewModelScope.launch {
            repository.insert(todo)
        }
    }

    fun update(id: Long, title: String, description: String, priority: Int, category: String, lifeAreaId: Long?) {
        viewModelScope.launch {
            val existing = repository.getTodoById(id)
            existing?.let {
                repository.update(
                    it.copy(
                        title = title,
                        description = description,
                        priority = priority,
                        category = category,
                        lifeAreaId = lifeAreaId
                    )
                )
            }
        }
    }
}
