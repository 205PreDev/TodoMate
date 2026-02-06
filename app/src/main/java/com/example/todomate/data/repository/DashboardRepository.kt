package com.example.todomate.data.repository

import androidx.lifecycle.LiveData
import com.example.todomate.data.local.LifeAreaDao
import com.example.todomate.data.local.LifeAreaEntity
import com.example.todomate.data.local.WeeklyGoalDao
import com.example.todomate.data.local.WeeklyGoalEntity

class DashboardRepository(
    private val lifeAreaDao: LifeAreaDao,
    private val weeklyGoalDao: WeeklyGoalDao
) {
    // Life Area
    fun getAllLifeAreas(): LiveData<List<LifeAreaEntity>> = lifeAreaDao.getAllLifeAreas()

    suspend fun getAllLifeAreasSync(): List<LifeAreaEntity> = lifeAreaDao.getAllLifeAreasSync()

    suspend fun getLifeAreaById(id: Long): LifeAreaEntity? = lifeAreaDao.getLifeAreaById(id)

    suspend fun insertLifeArea(lifeArea: LifeAreaEntity) = lifeAreaDao.insert(lifeArea)

    suspend fun insertLifeAreas(lifeAreas: List<LifeAreaEntity>) = lifeAreaDao.insertAll(lifeAreas)

    suspend fun insertLifeAreasReplace(lifeAreas: List<LifeAreaEntity>) = lifeAreaDao.insertAllReplace(lifeAreas)

    suspend fun updateLifeArea(lifeArea: LifeAreaEntity) = lifeAreaDao.update(lifeArea)

    suspend fun deleteLifeArea(lifeArea: LifeAreaEntity) = lifeAreaDao.delete(lifeArea)

    suspend fun deleteAllLifeAreas() = lifeAreaDao.deleteAll()

    // Weekly Goal
    fun getGoalsForWeek(weekStartDate: Long): LiveData<List<WeeklyGoalEntity>> =
        weeklyGoalDao.getGoalsForWeek(weekStartDate)

    suspend fun getGoalsForWeekSync(weekStartDate: Long): List<WeeklyGoalEntity> =
        weeklyGoalDao.getGoalsForWeekSync(weekStartDate)

    suspend fun saveGoal(goal: WeeklyGoalEntity) = weeklyGoalDao.insert(goal)

    suspend fun saveGoals(goals: List<WeeklyGoalEntity>) = weeklyGoalDao.insertAll(goals)

    suspend fun deleteGoalsForWeek(weekStartDate: Long) = weeklyGoalDao.deleteGoalsForWeek(weekStartDate)
}
