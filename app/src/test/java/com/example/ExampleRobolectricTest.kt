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
}
