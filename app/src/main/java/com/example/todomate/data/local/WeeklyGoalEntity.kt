package com.example.todomate.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weekly_goals",
    foreignKeys = [
        ForeignKey(
            entity = LifeAreaEntity::class,
            parentColumns = ["id"],
            childColumns = ["lifeAreaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("lifeAreaId"), Index("weekStartDate")]
)
data class WeeklyGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val lifeAreaId: Long,
    val weekStartDate: Long, // 주의 시작일 (월요일) timestamp
    val targetPercentage: Int, // 목표 비율 (0-100)
    val createdAt: Long = System.currentTimeMillis()
)
