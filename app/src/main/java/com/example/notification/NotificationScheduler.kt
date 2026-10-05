package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.CountdownEntity

object NotificationScheduler {

    fun scheduleAlert(context: Context, timer: CountdownEntity) {
        if (!timer.notifyOnFinish) {
            cancelAlert(context, timer.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = System.currentTimeMillis()

        // 1. Target alarm at exact zero time
        if (timer.targetEpochMillis > now) {
            val intent = Intent(context, CountdownAlertReceiver::class.java).apply {
                action = "com.example.ACTION_COUNTDOWN_ALERT"
                putExtra(CountdownAlertReceiver.EXTRA_TIMER_ID, timer.id)
                putExtra(CountdownAlertReceiver.EXTRA_TIMER_TITLE, timer.title)
                putExtra(CountdownAlertReceiver.EXTRA_IS_ADVANCE, false)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                timer.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            setExactAlarm(alarmManager, timer.targetEpochMillis, pendingIntent)
        }

        // 2. Advance notice alarm if configured (e.g. 15m, 1h, 1d)
        if (timer.notifyAdvanceMinutes > 0) {
            val advanceTriggerTime = timer.targetEpochMillis - (timer.notifyAdvanceMinutes * 60 * 1000L)
            if (advanceTriggerTime > now) {
                val advanceIntent = Intent(context, CountdownAlertReceiver::class.java).apply {
                    action = "com.example.ACTION_COUNTDOWN_ALERT"
                    putExtra(CountdownAlertReceiver.EXTRA_TIMER_ID, timer.id)
                    putExtra(CountdownAlertReceiver.EXTRA_TIMER_TITLE, timer.title)
                    putExtra(CountdownAlertReceiver.EXTRA_IS_ADVANCE, true)
                    putExtra(CountdownAlertReceiver.EXTRA_ADVANCE_MINUTES, timer.notifyAdvanceMinutes)
                }

                val advancePendingIntent = PendingIntent.getBroadcast(
                    context,
                    (timer.id + 100000).toInt(),
                    advanceIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                setExactAlarm(alarmManager, advanceTriggerTime, advancePendingIntent)
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

        // Cancel target alarm
        val intent = Intent(context, CountdownAlertReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            timerId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }

        // Cancel advance alarm
        val advancePendingIntent = PendingIntent.getBroadcast(
            context,
            (timerId + 100000).toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (advancePendingIntent != null) {
            alarmManager.cancel(advancePendingIntent)
        }
    }
}
