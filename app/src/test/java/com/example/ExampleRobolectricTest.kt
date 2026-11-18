package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CountdownBreakdown
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.ZonedDateTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Chronos", appName)
  }

  @Test
  fun `test countdown breakdown calculation`() {
    val now = ZonedDateTime.now()
    val future = now.plusYears(1).plusMonths(2).plusDays(5).plusHours(4).plusMinutes(30)
    val breakdown = CountdownBreakdown.compute(future.toInstant().toEpochMilli(), "UTC")
    assertFalse(breakdown.isPast)
    assertTrue(breakdown.totalSeconds > 0)

    val topThree = CountdownBreakdown.getTopThreeIntervals(breakdown)
    assertEquals("YEARS", topThree.label1)
    assertEquals("MONTHS", topThree.label2)
    assertEquals("DAYS", topThree.label3)
  }

  @Test
  fun `test top three intervals without years`() {
    val now = ZonedDateTime.now()
    val future = now.plusMonths(4).plusDays(10).plusHours(6)
    val breakdown = CountdownBreakdown.compute(future.toInstant().toEpochMilli(), "UTC")
    val topThree = CountdownBreakdown.getTopThreeIntervals(breakdown)
    assertEquals("MONTHS", topThree.label1)
    assertEquals("DAYS", topThree.label2)
    assertEquals("HOURS", topThree.label3)
  }

  @Test
  fun `test default category accent colors`() {
    val celebrationColor = com.example.model.CountdownCategories.getColorHexForCategory("Celebration")
    assertEquals(0xFFF59E0BL, celebrationColor)

    val travelColor = com.example.model.CountdownCategories.getColorHexForCategory("Travel")
    assertEquals(0xFF06B6D4L, travelColor)
  }

  @Test
  fun `test alert notification formatting and multiple alerts parsing`() {
    assertEquals("At event time", com.example.data.CountdownEntity.formatAlertOffsetLabel(0))
    assertEquals("15m before", com.example.data.CountdownEntity.formatAlertOffsetLabel(15))
    assertEquals("1h before", com.example.data.CountdownEntity.formatAlertOffsetLabel(60))
    assertEquals("1d before", com.example.data.CountdownEntity.formatAlertOffsetLabel(1440))

    val timer = com.example.data.CountdownEntity(
      title = "Test Event",
      targetEpochMillis = System.currentTimeMillis() + 100000,
      timeZoneId = "UTC",
      category = "Travel",
      alertMinutesList = "0,15,60,1440"
    )
    val alerts = timer.getAlertMinutes()
    assertEquals(listOf(0, 15, 60, 1440), alerts)
  }

  @Test
  fun `test chronological and alphabetical forward and reverse sorting`() {
    val timers = listOf(
      com.example.data.CountdownEntity(id = 1, title = "charlie", targetEpochMillis = 3000L, timeZoneId = "UTC", orderIndex = 0),
      com.example.data.CountdownEntity(id = 2, title = "Alpha", targetEpochMillis = 1000L, timeZoneId = "UTC", orderIndex = 1),
      com.example.data.CountdownEntity(id = 3, title = "Bravo", targetEpochMillis = 2000L, timeZoneId = "UTC", orderIndex = 2)
    )

    assertEquals(
      listOf(1L, 2L, 3L),
      com.example.model.CountdownSortOption.CUSTOM.sort(timers).map { it.id }
    )
    assertEquals(
      listOf(2L, 3L, 1L),
      com.example.model.CountdownSortOption.CHRONOLOGICAL_ASC.sort(timers).map { it.id }
    )
    assertEquals(
      listOf(1L, 3L, 2L),
      com.example.model.CountdownSortOption.CHRONOLOGICAL_DESC.sort(timers).map { it.id }
    )
    assertEquals(
      listOf(2L, 3L, 1L),
      com.example.model.CountdownSortOption.ALPHABETICAL_ASC.sort(timers).map { it.id }
    )
    assertEquals(
      listOf(1L, 3L, 2L),
      com.example.model.CountdownSortOption.ALPHABETICAL_DESC.sort(timers).map { it.id }
    )
  }
}
