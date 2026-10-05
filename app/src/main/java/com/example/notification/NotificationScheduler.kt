package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.CountdownEntity

object NotificationScheduler {

    private fun getRequestCode(timerId: Long, offsetMinutes: Int): Int {
        // Deterministic collision-resistant request code
        return ((timerId and 0xFFFL) shl 16 or (offsetMinutes.toLong() and 0xFFFFL)).toInt()
    }

    fun scheduleAlert(context: Context, timer: CountdownEntity) {
        // Cancel all existing scheduled alarms for this timer first
        cancelAlert(context, timer.id)

        if (!timer.notifyOnFinish) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = System.currentTimeMillis()
        val alertOffsets = timer.getAlertMinutes()

        for (offsetMinutes in alertOffsets) {
            val triggerTime = if (offsetMinutes == 0) {
                timer.targetEpochMillis
            } else {
                timer.targetEpochMillis - (offsetMinutes * 60 * 1000L)
            }

            if (triggerTime > now) {
                val intent = Intent(context, CountdownAlertReceiver::class.java).apply {
                    action = "com.example.ACTION_COUNTDOWN_ALERT"
                    putExtra(CountdownAlertReceiver.EXTRA_TIMER_ID, timer.id)
                    putExtra(CountdownAlertReceiver.EXTRA_TIMER_TITLE, timer.title)
                    putExtra(CountdownAlertReceiver.EXTRA_IS_ADVANCE, offsetMinutes > 0)
                    putExtra(CountdownAlertReceiver.EXTRA_ADVANCE_MINUTES, offsetMinutes)
                }

                val requestCode = getRequestCode(timer.id, offsetMinutes)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                setExactAlarm(alarmManager, triggerTime, pendingIntent)
            }
        }
    }

    private fun setExactAlarm(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelAlert(context: Context, timerId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, CountdownAlertReceiver::class.java)

        // Cancel across common preset & custom offset frequencies (up to 4 weeks out)
        val commonOffsets = listOf(
            0, 5, 10, 15, 20, 25, 30, 45, 60, 90, 120, 180, 240, 360, 480, 720,
            1440, 2880, 4320, 5760, 7200, 8640, 10080, 14400, 20160, 30240, 40320
        )
        for (offset in commonOffsets) {
            val requestCode = getRequestCode(timerId, offset)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
        }

        // Cancel legacy IDs
        val legacyZero = PendingIntent.getBroadcast(
            context,
            timerId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (legacyZero != null) alarmManager.cancel(legacyZero)

        val legacyAdvance = PendingIntent.getBroadcast(
            context,
            (timerId + 100000).toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (legacyAdvance != null) alarmManager.cancel(legacyAdvance)
    }
}
