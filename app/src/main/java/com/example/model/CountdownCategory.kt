package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryDef(
    val name: String,
    val icon: ImageVector,
    val colorHex: Long,
    val iconName: String = name
)

object CountdownCategories {
    const val ALL = "All"
    const val CELEBRATION = "Celebration"
    const val MILESTONE = "Milestone"
    const val TRAVEL = "Travel"
    const val WORK = "Work"
    const val PERSONAL = "Personal"

    val DEFAULT_LIST = listOf(
        CategoryDef(CELEBRATION, Icons.Default.Celebration, 0xFFF59E0B, "Celebration"),
        CategoryDef(MILESTONE, Icons.Default.Flag, 0xFF8B5CF6, "Flag"),
        CategoryDef(TRAVEL, Icons.Default.Flight, 0xFF06B6D4, "Flight"),
        CategoryDef(WORK, Icons.Default.Work, 0xFF3B82F6, "Work"),
        CategoryDef(PERSONAL, Icons.Default.Favorite, 0xFFF43F5E, "Favorite")
    )

    // Palette of selectable icons with user-friendly names
    val AVAILABLE_ICONS: List<Pair<String, ImageVector>> = listOf(
        "Celebration" to Icons.Default.Celebration,
        "Flag" to Icons.Default.Flag,
        "Flight" to Icons.Default.Flight,
        "Work" to Icons.Default.Work,
        "Favorite" to Icons.Default.Favorite,
        "Fitness" to Icons.Default.FitnessCenter,
        "School" to Icons.Default.School,
        "Cake" to Icons.Default.Cake,
        "Gaming" to Icons.Default.SportsEsports,
        "Dining" to Icons.Default.LocalDining,
        "Movie" to Icons.Default.Movie,
        "Music" to Icons.Default.MusicNote,
        "Pets" to Icons.Default.Pets,
        "Star" to Icons.Default.Star,
        "Home" to Icons.Default.Home,
        "Shopping" to Icons.Default.ShoppingBag,
        "Car" to Icons.Default.DirectionsCar,
        "Vacation" to Icons.Default.BeachAccess,
        "Health" to Icons.Default.MedicalServices,
        "Savings" to Icons.Default.Savings,
        "Gift" to Icons.Default.CardGiftcard,
        "Tech" to Icons.Default.Computer,
        "Rocket" to Icons.Default.RocketLaunch,
        "Event" to Icons.Default.Event,
        "Bookmark" to Icons.Default.Bookmark
    )

    val PRESET_COLORS: List<Long> = listOf(
        0xFFF59E0B, // Amber Gold
        0xFF06B6D4, // Neon Cyan
        0xFF6366F1, // Electric Indigo
        0xFF8B5CF6, // Cosmic Purple
        0xFF10B981, // Emerald Green
        0xFFF43F5E, // Rose Coral
        0xFF3B82F6, // Sapphire Blue
        0xFFEC4899  // Vibrant Pink
    )

    // Thread-safe / fast lookup map for dynamic custom categories
    private val customCategoriesMap = mutableMapOf<String, Pair<ImageVector, Long>>()

    fun registerCustomCategory(name: String, iconName: String, colorHex: Long) {
        val icon = getIconByName(iconName)
        customCategoriesMap[name.lowercase()] = Pair(icon, colorHex)
    }

    fun getIconByName(iconName: String): ImageVector {
        return AVAILABLE_ICONS.find { it.first.equals(iconName, ignoreCase = true) }?.second
            ?: Icons.Default.Bookmark
    }

    fun getIconForCategory(category: String): ImageVector {
        val defaultMatch = DEFAULT_LIST.find { it.name.equals(category, ignoreCase = true) }
        if (defaultMatch != null) return defaultMatch.icon

        val customMatch = customCategoriesMap[category.lowercase()]
        if (customMatch != null) return customMatch.first

        return getIconByName(category)
    }

    fun getColorHexForCategory(category: String): Long {
        val defaultMatch = DEFAULT_LIST.find { it.name.equals(category, ignoreCase = true) }
        if (defaultMatch != null) return defaultMatch.colorHex

        val customMatch = customCategoriesMap[category.lowercase()]
        if (customMatch != null) return customMatch.second

        return 0xFF6366F1
    }
}
