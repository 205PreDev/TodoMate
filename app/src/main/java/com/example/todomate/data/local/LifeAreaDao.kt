package com.example.todomate.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface LifeAreaDao {

    @Query("SELECT * FROM life_areas ORDER BY orderIndex ASC")
    fun getAllLifeAreas(): LiveData<List<LifeAreaEntity>>

    @Query("SELECT * FROM life_areas ORDER BY orderIndex ASC")
    suspend fun getAllLifeAreasSync(): List<LifeAreaEntity>

    @Query("SELECT * FROM life_areas WHERE id = :id")
    suspend fun getLifeAreaById(id: Long): LifeAreaEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(lifeArea: LifeAreaEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(lifeAreas: List<LifeAreaEntity>)

    @Update
    suspend fun update(lifeArea: LifeAreaEntity)

    @Delete
    suspend fun delete(lifeArea: LifeAreaEntity)

    @Query("DELETE FROM life_areas")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReplace(lifeAreas: List<LifeAreaEntity>)

    @Query("SELECT COUNT(*) FROM life_areas")
    suspend fun getCount(): Int
}
