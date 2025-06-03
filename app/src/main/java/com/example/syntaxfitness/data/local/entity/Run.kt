package com.example.syntaxfitness.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "runs")
data class RunEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startLatitude: Double,
    val startLongitude: Double,
    val endLatitude: Double,
    val endLongitude: Double,
    val distance: Float,
    val startTime: Date,
    val endTime: Date,
    val duration: Long // in milliseconds
)