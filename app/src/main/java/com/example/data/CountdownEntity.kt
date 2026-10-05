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
    val notifyAdvanceMinutes: Int = 0, // legacy single offset
    val alertMinutesList: String = "", // comma-separated offsets: e.g. "0,15,60,1440"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getAlertMinutes(): List<Int> {
        if (alertMinutesList.isNotBlank()) {
            return alertMinutesList.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .distinct()
                .sorted()
        }
        return if (notifyOnFinish) listOf(notifyAdvanceMinutes) else emptyList()
    }

    companion object {
        fun formatAlertOffsetLabel(minutes: Int): String {
            return when {
                minutes == 0 -> "At event time"
                minutes < 60 -> "${minutes}m before"
                minutes < 1440 && minutes % 60 == 0 -> "${minutes / 60}h before"
                minutes < 10080 && minutes % 1440 == 0 -> "${minutes / 1440}d before"
                minutes % 10080 == 0 -> "${minutes / 10080}w before"
                minutes < 1440 -> "${minutes / 60}h ${minutes % 60}m before"
                else -> "${minutes / 1440}d ${(minutes % 1440) / 60}h before"
            }
        }
    }
}
