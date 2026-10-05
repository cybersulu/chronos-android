package com.example.model

import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

data class TimeZoneItem(
    val id: String,
    val cityOrRegion: String,
    val offsetString: String,
    val isLocal: Boolean = false
)

object TimeZoneHelper {
    private val POPULAR_ZONES = listOf(
        "UTC",
        "America/New_York",
        "America/Chicago",
        "America/Denver",
        "America/Los_Angeles",
        "America/Toronto",
        "America/Sao_Paulo",
        "Europe/London",
        "Europe/Paris",
        "Europe/Berlin",
        "Europe/Rome",
        "Europe/Madrid",
        "Asia/Dubai",
        "Asia/Kolkata",
        "Asia/Bangkok",
        "Asia/Singapore",
        "Asia/Tokyo",
        "Asia/Seoul",
        "Asia/Shanghai",
        "Australia/Sydney",
        "Pacific/Auckland",
        "Pacific/Honolulu"
    )

    fun getLocalZoneId(): String = ZoneId.systemDefault().id

    fun getAllTimeZones(): List<TimeZoneItem> {
        val now = Instant.now()
        val localId = getLocalZoneId()

        val allIds = (listOf(localId) + POPULAR_ZONES + ZoneId.getAvailableZoneIds().toList()).distinct()

        return allIds.mapNotNull { id ->
            try {
                val zone = ZoneId.of(id)
                val offset = zone.rules.getOffset(now)
                val cleanCity = id.substringAfterLast('/').replace('_', ' ')
                val isLocal = id == localId
                TimeZoneItem(
                    id = id,
                    cityOrRegion = if (isLocal) "$cleanCity (Your Device Time)" else cleanCity,
                    offsetString = "UTC$offset",
                    isLocal = isLocal
                )
            } catch (e: Exception) {
                null
            }
        }.sortedWith(
            compareByDescending<TimeZoneItem> { it.isLocal }
                .thenBy { it.cityOrRegion }
        )
    }

    fun formatZoneSummary(zoneIdString: String): String {
        return try {
            val zone = ZoneId.of(zoneIdString)
            val offset = zone.rules.getOffset(Instant.now())
            val name = zone.id.substringAfterLast('/').replace('_', ' ')
            "$name (UTC$offset)"
        } catch (e: Exception) {
            zoneIdString
        }
    }
}
