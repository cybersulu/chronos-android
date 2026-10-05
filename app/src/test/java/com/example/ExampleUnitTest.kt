package com.example

import com.example.model.CountdownBreakdown
import org.junit.Assert.*
import org.junit.Test
import java.time.ZonedDateTime

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun `test 12 hour vs 24 hour breakdown formatting`() {
    val zdt = ZonedDateTime.of(2026, 12, 31, 15, 30, 0, 0, java.time.ZoneId.of("UTC"))
    val epoch = zdt.toInstant().toEpochMilli()

    val breakdown12 = CountdownBreakdown.compute(epoch, "UTC", is24Hour = false)
    val breakdown24 = CountdownBreakdown.compute(epoch, "UTC", is24Hour = true)

    assertTrue(breakdown12.formattedTargetTime.contains("PM") || breakdown12.formattedTargetTime.contains("pm"))
    assertEquals("15:30", breakdown24.formattedTargetTime)
  }
}
