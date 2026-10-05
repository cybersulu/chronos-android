package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "countdown_timers")
data class CountdownEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetEpochMillis: Long,
    val timeZoneId: String,
    val category: String = "Milestone",
    val colorIndex: Int = 0,
    val iconName: String = "Celebration",
    val orderIndex: Int = 0,
    val notifyOnFinish: Boolean = true,
    val notifyAdvanceMinutes: Int = 0, // 0 = at exact time, 15 = 15m before, 60 = 1h before, 1440 = 1d before
    val isPinnedToWidget: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
