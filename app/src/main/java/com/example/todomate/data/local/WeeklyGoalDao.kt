package com.example.todomate.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface WeeklyGoalDao {

    @Query("SELECT * FROM weekly_goals WHERE weekStartDate = :weekStartDate")
    fun getGoalsForWeek(weekStartDate: Long): LiveData<List<WeeklyGoalEntity>>

    @Query("SELECT * FROM weekly_goals WHERE weekStartDate = :weekStartDate")
    suspend fun getGoalsForWeekSync(weekStartDate: Long): List<WeeklyGoalEntity>

    @Query("SELECT * FROM weekly_goals WHERE weekStartDate = :weekStartDate AND lifeAreaId = :lifeAreaId")
    suspend fun getGoalForWeekAndArea(weekStartDate: Long, lifeAreaId: Long): WeeklyGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: WeeklyGoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<WeeklyGoalEntity>)

    @Update
    suspend fun update(goal: WeeklyGoalEntity)

    @Delete
    suspend fun delete(goal: WeeklyGoalEntity)

    @Query("DELETE FROM weekly_goals WHERE weekStartDate = :weekStartDate")
    suspend fun deleteGoalsForWeek(weekStartDate: Long)
}
