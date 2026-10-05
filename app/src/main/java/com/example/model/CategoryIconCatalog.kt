package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

data class IconOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

object CategoryIconCatalog {

    val OPTIONS: List<IconOption> = listOf(
        IconOption("Celebration", "Celebration", Icons.Default.Celebration),
        IconOption("Milestone", "Milestone", Icons.Default.Flag),
        IconOption("Travel", "Travel", Icons.Default.Flight),
        IconOption("Work", "Work", Icons.Default.Work),
        IconOption("Personal", "Personal", Icons.Default.Favorite),
        IconOption("Birthday", "Birthday", Icons.Default.Cake),
        IconOption("Event", "Event", Icons.Default.Event),
        IconOption("Fitness", "Fitness", Icons.Default.FitnessCenter),
        IconOption("Education", "School", Icons.Default.School),
        IconOption("Music", "Music", Icons.Default.MusicNote),
        IconOption("Movie", "Cinema", Icons.Default.Movie),
        IconOption("Gaming", "Gaming", Icons.Default.SportsEsports),
        IconOption("Beach", "Vacation", Icons.Default.BeachAccess),
        IconOption("Tech", "Tech", Icons.Default.Computer),
        IconOption("Pets", "Pets", Icons.Default.Pets),
        IconOption("Car", "Road Trip", Icons.Default.DirectionsCar),
        IconOption("Home", "Home", Icons.Default.Home),
        IconOption("Star", "Special", Icons.Default.Star),
        IconOption("Rocket", "Launch", Icons.Default.RocketLaunch),
        IconOption("Dining", "Food", Icons.Default.Restaurant),
        IconOption("Coffee", "Coffee", Icons.Default.LocalCafe),
        IconOption("Shopping", "Shopping", Icons.Default.ShoppingCart),
        IconOption("Books", "Reading", Icons.Default.MenuBook),
        IconOption("Finance", "Finance", Icons.Default.AttachMoney),
        IconOption("Nature", "Nature", Icons.Default.Forest),
        IconOption("Family", "Family", Icons.Default.ChildCare),
        IconOption("Health", "Health", Icons.Default.LocalHospital),
        IconOption("Art", "Art", Icons.Default.Brush),
        IconOption("Photography", "Photo", Icons.Default.CameraAlt),
        IconOption("Science", "Science", Icons.Default.Science),
        IconOption("Bookmark", "Other", Icons.Default.Bookmark)
    )

    private val iconMap: Map<String, ImageVector> = OPTIONS.associate { it.key.lowercase() to it.icon }

    fun getIcon(key: String?): ImageVector {
        if (key.isNullOrBlank()) return Icons.Default.Bookmark
        return iconMap[key.lowercase()]
            ?: when (key.lowercase()) {
                "flag" -> Icons.Default.Flag
                "flight" -> Icons.Default.Flight
                "cake" -> Icons.Default.Cake
                "heart", "love" -> Icons.Default.Favorite
                "star" -> Icons.Default.Star
                "work", "job" -> Icons.Default.Work
                else -> Icons.Default.Bookmark
            }
    }
}
