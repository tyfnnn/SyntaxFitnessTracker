package com.example.syntaxfitness.data.local.dao

import androidx.room.*
import com.example.syntaxfitness.data.local.entity.RunEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {
    @Query("SELECT * FROM runs ORDER BY startTime DESC")
    fun getAllRuns(): Flow<List<RunEntity>>

    @Query("SELECT * FROM runs WHERE id = :id")
    suspend fun getRunById(id: Long): RunEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: RunEntity): Long

    @Update
    suspend fun updateRun(run: RunEntity)

    @Delete
    suspend fun deleteRun(run: RunEntity)

    @Query("DELETE FROM runs")
    suspend fun deleteAllRuns()

    @Query("SELECT * FROM runs ORDER BY startTime DESC LIMIT 1")
    suspend fun getLastRun(): RunEntity?

    @Query("SELECT SUM(distance) FROM runs")
    fun getTotalDistance(): Flow<Float?>

    @Query("SELECT COUNT(*) FROM runs")
    fun getTotalRunCount(): Flow<Int>

    @Query("SELECT AVG(distance) FROM runs")
    fun getAverageDistance(): Flow<Float?>

    @Query("SELECT * FROM runs WHERE date(startTime) = date(:date)")
    fun getRunsByDate(date: String): Flow<List<RunEntity>>
}