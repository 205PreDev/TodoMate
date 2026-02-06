package com.example.todomate.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "life_areas")
data class LifeAreaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String = "",
    val color: Int = 0,
    val isDefault: Boolean = false, // 기본 제공 영역인지 여부
    val orderIndex: Int = 0
) {
    companion object {
        // 기본 생활 영역
        val DEFAULT_AREAS = listOf(
            LifeAreaEntity(id = 1, name = "커리어", icon = "work", isDefault = true, orderIndex = 0),
            LifeAreaEntity(id = 2, name = "건강", icon = "health", isDefault = true, orderIndex = 1),
            LifeAreaEntity(id = 3, name = "학습", icon = "study", isDefault = true, orderIndex = 2),
            LifeAreaEntity(id = 4, name = "관계", icon = "relationship", isDefault = true, orderIndex = 3),
            LifeAreaEntity(id = 5, name = "재정", icon = "finance", isDefault = true, orderIndex = 4)
        )
    }
}
