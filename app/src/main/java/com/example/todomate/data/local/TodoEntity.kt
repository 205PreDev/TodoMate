package com.example.todomate.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: Int = 1, // 0=LOW, 1=MEDIUM, 2=HIGH
    val category: String = "개인",
    val createdAt: Long = System.currentTimeMillis()
)
