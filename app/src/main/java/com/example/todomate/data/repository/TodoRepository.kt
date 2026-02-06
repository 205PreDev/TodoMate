package com.example.todomate.data.repository

import androidx.lifecycle.LiveData
import com.example.todomate.data.local.TodoDao
import com.example.todomate.data.local.TodoEntity

class TodoRepository(private val todoDao: TodoDao) {

    fun getAllTodos(): LiveData<List<TodoEntity>> = todoDao.getAllTodos()

    fun getAllTodosByPriority(): LiveData<List<TodoEntity>> = todoDao.getAllTodosByPriority()

    fun getAllTodosByDateAsc(): LiveData<List<TodoEntity>> = todoDao.getAllTodosByDateAsc()

    fun searchTodos(query: String): LiveData<List<TodoEntity>> = todoDao.searchTodos(query)

    suspend fun getTodoById(id: Long): TodoEntity? = todoDao.getTodoById(id)

    suspend fun insert(todo: TodoEntity) = todoDao.insert(todo)

    suspend fun update(todo: TodoEntity) = todoDao.update(todo)

    suspend fun delete(todo: TodoEntity) = todoDao.delete(todo)
}
