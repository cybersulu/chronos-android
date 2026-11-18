package com.example.model

import com.example.data.CountdownEntity

enum class CountdownSortOption(
    val shortLabel: String,
    val menuLabel: String,
    val subtitle: String
) {
    CUSTOM(
        shortLabel = "Custom",
        menuLabel = "Custom Order",
        subtitle = "Manual drag-and-drop order"
    ),
    CHRONOLOGICAL_ASC(
        shortLabel = "Earliest",
        menuLabel = "Chronological (Earliest first)",
        subtitle = "Soonest & past events first"
    ),
    CHRONOLOGICAL_DESC(
        shortLabel = "Latest",
        menuLabel = "Chronological (Latest first)",
        subtitle = "Farthest events first"
    ),
    ALPHABETICAL_ASC(
        shortLabel = "A–Z",
        menuLabel = "Alphabetical (A to Z)",
        subtitle = "Title ascending"
    ),
    ALPHABETICAL_DESC(
        shortLabel = "Z–A",
        menuLabel = "Alphabetical (Z to A)",
        subtitle = "Title descending"
    );

    fun sort(timers: List<CountdownEntity>): List<CountdownEntity> {
        return when (this) {
            CUSTOM -> timers
            CHRONOLOGICAL_ASC -> timers.sortedWith(
                compareBy<CountdownEntity> { it.targetEpochMillis }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title.trim() }
                    .thenBy { it.id }
            )
            CHRONOLOGICAL_DESC -> timers.sortedWith(
                compareByDescending<CountdownEntity> { it.targetEpochMillis }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title.trim() }
                    .thenBy { it.id }
            )
            ALPHABETICAL_ASC -> timers.sortedWith(
                compareBy<CountdownEntity, String>(String.CASE_INSENSITIVE_ORDER) { it.title.trim() }
                    .thenBy { it.targetEpochMillis }
                    .thenBy { it.id }
            )
            ALPHABETICAL_DESC -> timers.sortedWith(
                compareByDescending<CountdownEntity, String>(String.CASE_INSENSITIVE_ORDER) { it.title.trim() }
                    .thenBy { it.targetEpochMillis }
                    .thenBy { it.id }
            )
        }
    }
}
