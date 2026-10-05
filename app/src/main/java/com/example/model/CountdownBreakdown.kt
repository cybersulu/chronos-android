package com.example.model

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class CountdownBreakdown(
    val years: Long,
    val months: Long,
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val totalSeconds: Long,
    val isPast: Boolean,
    val formattedTargetDate: String,
    val formattedTargetTime: String,
    val timeZoneDisplayName: String
) {
    companion object {
        fun compute(targetEpochMillis: Long, timeZoneId: String): CountdownBreakdown {
            val zone = try {
                ZoneId.of(timeZoneId)
            } catch (e: Exception) {
                ZoneId.systemDefault()
            }

            val now = ZonedDateTime.now(zone)
            val target = Instant.ofEpochMilli(targetEpochMillis).atZone(zone)
            val isPast = now.isAfter(target)

            val (start, end) = if (isPast) target to now else now to target

            var current = start
            var years = 0L
            while (!current.plusYears(1).isAfter(end)) {
                current = current.plusYears(1)
                years++
            }

            var months = 0L
            while (!current.plusMonths(1).isAfter(end)) {
                current = current.plusMonths(1)
                months++
            }

            var days = 0L
            while (!current.plusDays(1).isAfter(end)) {
                current = current.plusDays(1)
                days++
            }

            val remainingDuration = Duration.between(current, end)
            val hours = remainingDuration.toHours()
            val minutes = remainingDuration.toMinutes() % 60
            val seconds = remainingDuration.seconds % 60
            val totalSeconds = Math.abs(Duration.between(now, target).seconds)

            val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.getDefault())
            val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

            val offset = zone.rules.getOffset(target.toInstant())
            val cleanName = zone.id.substringAfterLast('/').replace('_', ' ')
            val tzDisplay = "$cleanName (UTC$offset)"

            return CountdownBreakdown(
                years = years,
                months = months,
                days = days,
                hours = hours,
                minutes = minutes,
                seconds = seconds,
                totalSeconds = totalSeconds,
                isPast = isPast,
                formattedTargetDate = target.format(dateFormatter),
                formattedTargetTime = target.format(timeFormatter),
                timeZoneDisplayName = tzDisplay
            )
        }

        fun getWidgetDisplayString(breakdown: CountdownBreakdown): String {
            return if (breakdown.isPast) {
                "Event Arrived!"
            } else if (breakdown.years > 0) {
                "${breakdown.years}y ${breakdown.months}m ${breakdown.days}d"
            } else if (breakdown.months > 0) {
                "${breakdown.months}m ${breakdown.days}d ${String.format(Locale.US, "%02d:%02d", breakdown.hours, breakdown.minutes)}"
            } else if (breakdown.days > 0) {
                "${breakdown.days}d ${String.format(Locale.US, "%02d:%02d", breakdown.hours, breakdown.minutes)}"
            } else {
                String.format(Locale.US, "%02d:%02d:%02d", breakdown.hours, breakdown.minutes, breakdown.seconds)
            }
        }

        fun getTopThreeIntervals(breakdown: CountdownBreakdown): TopThreeIntervals {
            return when {
                breakdown.years > 0 -> {
                    TopThreeIntervals(
                        val1 = String.format(Locale.US, "%02d", breakdown.years), label1 = "YEARS",
                        val2 = String.format(Locale.US, "%02d", breakdown.months), label2 = "MONTHS",
                        val3 = String.format(Locale.US, "%02d", breakdown.days), label3 = "DAYS"
                    )
                }
                breakdown.months > 0 -> {
                    TopThreeIntervals(
                        val1 = String.format(Locale.US, "%02d", breakdown.months), label1 = "MONTHS",
                        val2 = String.format(Locale.US, "%02d", breakdown.days), label2 = "DAYS",
                        val3 = String.format(Locale.US, "%02d", breakdown.hours), label3 = "HOURS"
                    )
                }
                breakdown.days > 0 -> {
                    TopThreeIntervals(
                        val1 = String.format(Locale.US, "%02d", breakdown.days), label1 = "DAYS",
                        val2 = String.format(Locale.US, "%02d", breakdown.hours), label2 = "HOURS",
                        val3 = String.format(Locale.US, "%02d", breakdown.minutes), label3 = "MINS"
                    )
                }
                else -> {
                    TopThreeIntervals(
                        val1 = String.format(Locale.US, "%02d", breakdown.hours), label1 = "HOURS",
                        val2 = String.format(Locale.US, "%02d", breakdown.minutes), label2 = "MINS",
                        val3 = String.format(Locale.US, "%02d", breakdown.seconds), label3 = "SECS"
                    )
                }
            }
        }
    }
}

data class TopThreeIntervals(
    val val1: String,
    val label1: String,
    val val2: String,
    val label2: String,
    val val3: String,
    val label3: String
)

