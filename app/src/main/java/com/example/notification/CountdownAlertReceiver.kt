package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CountdownAlertReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule active alarms on device boot
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val timers = db.countdownDao().getAllTimersList()
                val now = System.currentTimeMillis()
                for (timer in timers) {
                    if (timer.notifyOnFinish && timer.targetEpochMillis > now) {
                        NotificationScheduler.scheduleAlert(context, timer)
                    }
                }
            }
            return
        }

        val timerId = intent.getLongExtra(EXTRA_TIMER_ID, -1L)
        val timerTitle = intent.getStringExtra(EXTRA_TIMER_TITLE) ?: "Countdown Event"
        val isAdvanceNotice = intent.getBooleanExtra(EXTRA_IS_ADVANCE, false)
        val advanceMinutes = intent.getIntExtra(EXTRA_ADVANCE_MINUTES, 0)

        // Trigger Haptic Feedback
        triggerHapticAlert(context)

        // Display Notification
        showNotification(context, timerId, timerTitle, isAdvanceNotice, advanceMinutes)
    }

    private fun triggerHapticAlert(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let { v ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Expressive celebratory vibration pattern
                    val timings = longArrayOf(0, 300, 150, 300, 150, 500)
                    val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                    v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(longArrayOf(0, 300, 150, 300, 150, 500), -1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showNotification(
        context: Context,
        timerId: Long,
        title: String,
        isAdvanceNotice: Boolean,
        advanceMinutes: Int
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "chronos_countdown_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Countdown Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when your countdown timers finish or reach advance alerts"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300, 150, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_TIMER_ID, timerId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            timerId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (isAdvanceNotice) {
            val timeText = if (advanceMinutes >= 1440) {
                "${advanceMinutes / 1440} day(s)"
            } else if (advanceMinutes >= 60) {
                "${advanceMinutes / 60} hour(s)"
            } else {
                "$advanceMinutes minute(s)"
            }
            "Upcoming in $timeText! Prepare for $title."
        } else {
            "🎉 The countdown for '$title' has arrived! Tap to view."
        }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(if (isAdvanceNotice) "⏰ Countdown Reminder" else "⏳ Timer Complete!")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 300, 150, 300, 150, 500))
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(timerId.toInt(), notification)
    }

    companion object {
        const val EXTRA_TIMER_ID = "extra_timer_id"
        const val EXTRA_TIMER_TITLE = "extra_timer_title"
        const val EXTRA_IS_ADVANCE = "extra_is_advance"
        const val EXTRA_ADVANCE_MINUTES = "extra_advance_minutes"
    }
}
