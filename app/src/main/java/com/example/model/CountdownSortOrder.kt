package com.example.model

import com.example.data.CountdownEntity

enum class CountdownSortOrder(
    val label: String,
    val shortLabel: String,
    val description: String
) {
    CUSTOM(
        label = "Custom Order",
        shortLabel = "Custom",
        description = "Arrange cards manually"
    ),
    CHRONOLOGICAL(
        label = "Forward Chronological",
        shortLabel = "Earliest",
        description = "Earliest date first"
    ),
    REVERSE_CHRONOLOGICAL(
        label = "Reverse Chronological",
        shortLabel = "Latest",
        description = "Latest date first"
    );

    companion object {
        fun sortTimers(
            timers: List<CountdownEntity>,
            sortOrder: CountdownSortOrder
        ): List<CountdownEntity> {
            return when (sortOrder) {
                CUSTOM -> timers
                CHRONOLOGICAL -> timers.sortedWith(
                    compareBy<CountdownEntity> { it.targetEpochMillis }
                        .thenBy { it.orderIndex }
                        .thenBy { it.id }
                )
                REVERSE_CHRONOLOGICAL -> timers.sortedWith(
                    compareByDescending<CountdownEntity> { it.targetEpochMillis }
                        .thenBy { it.orderIndex }
                        .thenBy { it.id }
                )
            }
        }

        fun fromStorageKey(key: String?): CountdownSortOrder {
            return entries.firstOrNull { it.name == key } ?: CUSTOM
        }
    }
}
