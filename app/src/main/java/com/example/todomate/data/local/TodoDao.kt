package com.example.todomate.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TodoDao {

    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    fun getAllTodos(): LiveData<List<TodoEntity>>

    @Query("SELECT * FROM todos ORDER BY priority DESC, createdAt DESC")
    fun getAllTodosByPriority(): LiveData<List<TodoEntity>>

    @Query("SELECT * FROM todos ORDER BY createdAt ASC")
    fun getAllTodosByDateAsc(): LiveData<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchTodos(query: String): LiveData<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: Long): TodoEntity?

    @Insert
    suspend fun insert(todo: TodoEntity)

    @Update
    suspend fun update(todo: TodoEntity)

    @Delete
    suspend fun delete(todo: TodoEntity)

    // 생활 영역별 통계
    @Query("""
        SELECT lifeAreaId, COUNT(*) as count
        FROM todos
        WHERE createdAt >= :startDate AND createdAt < :endDate AND lifeAreaId IS NOT NULL
        GROUP BY lifeAreaId
    """)
    fun getTodoCountByLifeArea(startDate: Long, endDate: Long): LiveData<List<LifeAreaCount>>

    @Query("""
        SELECT lifeAreaId, COUNT(*) as count
        FROM todos
        WHERE createdAt >= :startDate AND createdAt < :endDate AND isCompleted = 1 AND lifeAreaId IS NOT NULL
        GROUP BY lifeAreaId
    """)
    fun getCompletedCountByLifeArea(startDate: Long, endDate: Long): LiveData<List<LifeAreaCount>>
}

data class LifeAreaCount(
    val lifeAreaId: Long,
    val count: Int
)
