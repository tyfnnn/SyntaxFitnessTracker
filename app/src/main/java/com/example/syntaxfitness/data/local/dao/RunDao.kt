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

    @Query("SELECT SUM(distance) FROM runs WHERE endLatitude != 0.0 AND endLongitude != 0.0")
    fun getTotalDistance(): Flow<Float?>

    @Query("SELECT COUNT(*) FROM runs WHERE endLatitude != 0.0 AND endLongitude != 0.0")
    fun getTotalRunCount(): Flow<Int>

    @Query("SELECT AVG(distance) FROM runs WHERE endLatitude != 0.0 AND endLongitude != 0.0")
    fun getAverageDistance(): Flow<Float?>

    @Query("SELECT * FROM runs WHERE date(startTime) = date(:date) AND endLatitude != 0.0 AND endLongitude != 0.0")
    fun getRunsByDate(date: String): Flow<List<RunEntity>>

    // Neue Queries für bessere Datenverwaltung
    @Query("SELECT * FROM runs WHERE endLatitude = 0.0 AND endLongitude = 0.0")
    suspend fun getIncompleteRuns(): List<RunEntity>

    @Query("DELETE FROM runs WHERE endLatitude = 0.0 AND endLongitude = 0.0")
    suspend fun deleteIncompleteRuns()

    @Query("SELECT * FROM runs WHERE endLatitude != 0.0 AND endLongitude != 0.0 ORDER BY distance DESC LIMIT :limit")
    fun getLongestRuns(limit: Int = 10): Flow<List<RunEntity>>

    @Query("SELECT * FROM runs WHERE endLatitude != 0.0 AND endLongitude != 0.0 ORDER BY duration DESC LIMIT :limit")
    fun getLongestDurationRuns(limit: Int = 10): Flow<List<RunEntity>>

    @Query("SELECT COUNT(*) FROM runs WHERE date(startTime) = date('now') AND endLatitude != 0.0 AND endLongitude != 0.0")
    fun getTodayRunCount(): Flow<Int>

    @Query("SELECT SUM(distance) FROM runs WHERE date(startTime) = date('now') AND endLatitude != 0.0 AND endLongitude != 0.0")
    fun getTodayTotalDistance(): Flow<Float?>
}