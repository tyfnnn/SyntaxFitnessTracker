package com.example.syntaxfitness.data.local.repository

import com.example.syntaxfitness.data.local.dao.RunDao
import com.example.syntaxfitness.data.local.entity.RunEntity
import kotlinx.coroutines.flow.Flow

interface RunRepository {
    fun getAllRuns(): Flow<List<RunEntity>>
    suspend fun getRunById(id: Long): RunEntity?
    suspend fun insertRun(run: RunEntity): Long
    suspend fun updateRun(run: RunEntity)
    suspend fun deleteRun(run: RunEntity)
    suspend fun deleteAllRuns()
    suspend fun getLastRun(): RunEntity?
    fun getTotalDistance(): Flow<Float?>
    fun getTotalRunCount(): Flow<Int>
    fun getAverageDistance(): Flow<Float?>
    fun getRunsByDate(date: String): Flow<List<RunEntity>>
}

class RunRepositoryImpl(
    private val runDao: RunDao
) : RunRepository {
    override fun getAllRuns(): Flow<List<RunEntity>> = runDao.getAllRuns()

    override suspend fun getRunById(id: Long): RunEntity? = runDao.getRunById(id)

    override suspend fun insertRun(run: RunEntity): Long = runDao.insertRun(run)

    override suspend fun updateRun(run: RunEntity) = runDao.updateRun(run)

    override suspend fun deleteRun(run: RunEntity) = runDao.deleteRun(run)

    override suspend fun deleteAllRuns() = runDao.deleteAllRuns()

    override suspend fun getLastRun(): RunEntity? = runDao.getLastRun()

    override fun getTotalDistance(): Flow<Float?> = runDao.getTotalDistance()

    override fun getTotalRunCount(): Flow<Int> = runDao.getTotalRunCount()

    override fun getAverageDistance(): Flow<Float?> = runDao.getAverageDistance()

    override fun getRunsByDate(date: String): Flow<List<RunEntity>> = runDao.getRunsByDate(date)
}