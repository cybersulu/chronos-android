package com.example.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.model.CountdownBreakdown
import com.example.model.CountdownCategories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class CountdownAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (widgetId in appWidgetIds) {
            updateSingleWidgetAsync(context, appWidgetManager, widgetId)
        }
        scheduleNextMinuteTick(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateSingleWidgetAsync(context, appWidgetManager, appWidgetId)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (id in appWidgetIds) {
            WidgetPreferences.removeWidgetTimerId(context, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (action == ACTION_UPDATE_WIDGET_TICK ||
            action == AppWidgetManager.ACTION_APPWIDGET_UPDATE ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)

            // Refresh 4x1 widgets
            val ids4x1 = appWidgetManager.getAppWidgetIds(ComponentName(context, CountdownAppWidgetProvider::class.java))
            for (widgetId in ids4x1) {
                updateSingleWidgetAsync(context, appWidgetManager, widgetId)
            }

            // Refresh 3x1 compact widgets
            val ids3x1 = appWidgetManager.getAppWidgetIds(ComponentName(context, CountdownWidget3x1Provider::class.java))
            for (widgetId in ids3x1) {
                CountdownWidget3x1Provider.updateSingleWidgetAsync(context, appWidgetManager, widgetId)
            }

            // Refresh 5x1 wide widgets
            val ids5x1 = appWidgetManager.getAppWidgetIds(ComponentName(context, CountdownWidget5x1Provider::class.java))
            for (widgetId in ids5x1) {
                CountdownWidget5x1Provider.updateSingleWidgetAsync(context, appWidgetManager, widgetId)
            }

            // Schedule next tick to fire exactly on the next minute
            scheduleNextMinuteTick(context)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleNextMinuteTick(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val ids3x1 = appWidgetManager.getAppWidgetIds(ComponentName(context, CountdownWidget3x1Provider::class.java))
        val ids5x1 = appWidgetManager.getAppWidgetIds(ComponentName(context, CountdownWidget5x1Provider::class.java))
        if (ids3x1.isEmpty() && ids5x1.isEmpty()) {
            cancelMinuteTick(context)
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET_TICK = "com.example.ACTION_UPDATE_WIDGET_TICK"
        const val EXTRA_WIDGET_TIMER_ID = "extra_widget_timer_id"

        fun updateSingleWidgetAsync(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    // Check if this specific widget has a selected timer
                    val selectedTimerId = WidgetPreferences.getWidgetTimerId(context, widgetId)
                    val timer = if (selectedTimerId != null) {
                        db.countdownDao().getTimerByIdSync(selectedTimerId)
                    } else {
                        db.countdownDao().getFirstTimer()
                    }

                    val views = RemoteViews(context.packageName, R.layout.widget_countdown)

                    // Dynamically calculate and apply proportional typography and padding based on adjusted widget size
                    val dims = WidgetSizeHelper.getWidgetDimensions(
                        appWidgetManager = appWidgetManager,
                        widgetId = widgetId,
                        defaultWidthDp = 240,
                        defaultHeightDp = 40
                    )
                    val scale = WidgetSizeHelper.computeScale(context, dims)
                    WidgetSizeHelper.applyScaling(
                        views = views,
                        scale = scale,
                        rootId = R.id.widget_root,
                        titleId = R.id.widget_event_title,
                        dateId = R.id.widget_target_date,
                        arrivedId = R.id.widget_arrived_banner,
                        valIds = listOf(
                            R.id.widget_val_years,
                            R.id.widget_val_months,
                            R.id.widget_val_days,
                            R.id.widget_val_hours,
                            R.id.widget_val_minutes,
                            R.id.widget_val_seconds
                        ),
                        lblIds = listOf(
                            R.id.widget_lbl_years,
                            R.id.widget_lbl_months,
                            R.id.widget_lbl_days,
                            R.id.widget_lbl_hours,
                            R.id.widget_lbl_minutes,
                            R.id.widget_lbl_seconds
                        )
                    )

                    if (timer != null) {
                        val accentColorInt = CountdownCategories.getColorHexForCategory(timer.category).toInt()
                        val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
                        val breakdown = CountdownBreakdown.compute(timer.targetEpochMillis, timer.timeZoneId, is24Hour)

                        // Event Title
                        views.setTextViewText(R.id.widget_event_title, timer.title)

                        // Target date & time string
                        views.setTextViewText(
                            R.id.widget_target_date,
                            "${breakdown.formattedTargetDate} • ${breakdown.timeZoneDisplayName}"
                        )

                        if (breakdown.isPast) {
                            views.setViewVisibility(R.id.widget_grid_countdown, View.GONE)
                            views.setViewVisibility(R.id.widget_arrived_banner, View.VISIBLE)
                        } else {
                            views.setViewVisibility(R.id.widget_grid_countdown, View.VISIBLE)
                            views.setViewVisibility(R.id.widget_arrived_banner, View.GONE)

                            val years = breakdown.years
                            val months = breakdown.months
                            val days = breakdown.days
                            val hours = breakdown.hours
                            val minutes = breakdown.minutes
                            val seconds = breakdown.seconds

                            // Format numbers
                            views.setTextViewText(R.id.widget_val_years, String.format(Locale.US, "%02d", years))
                            views.setTextViewText(R.id.widget_val_months, String.format(Locale.US, "%02d", months))
                            views.setTextViewText(R.id.widget_val_days, String.format(Locale.US, "%02d", days))
                            views.setTextViewText(R.id.widget_val_hours, String.format(Locale.US, "%02d", hours))
                            views.setTextViewText(R.id.widget_val_minutes, String.format(Locale.US, "%02d", minutes))
                            views.setTextViewText(R.id.widget_val_seconds, String.format(Locale.US, "%02d", seconds))

                            // Apply the timer's chosen accent color across all digit blocks
                            views.setTextColor(R.id.widget_val_years, accentColorInt)
                            views.setTextColor(R.id.widget_val_months, accentColorInt)
                            views.setTextColor(R.id.widget_val_days, accentColorInt)
                            views.setTextColor(R.id.widget_val_hours, accentColorInt)
                            views.setTextColor(R.id.widget_val_minutes, accentColorInt)
                            views.setTextColor(R.id.widget_val_seconds, accentColorInt)

                            // Do not show the time interval if the time left is lower than that unit
                            views.setViewVisibility(
                                R.id.widget_block_years,
                                if (years > 0L) View.VISIBLE else View.GONE
                            )
                            views.setViewVisibility(
                                R.id.widget_block_months,
                                if (years > 0L || months > 0L) View.VISIBLE else View.GONE
                            )
                            views.setViewVisibility(
                                R.id.widget_block_days,
                                if (years > 0L || months > 0L || days > 0L) View.VISIBLE else View.GONE
                            )
                            views.setViewVisibility(
                                R.id.widget_block_hours,
                                if (years > 0L || months > 0L || days > 0L || hours > 0L) View.VISIBLE else View.GONE
                            )
                            views.setViewVisibility(
                                R.id.widget_block_minutes,
                                if (years > 0L || months > 0L || days > 0L || hours > 0L || minutes > 0L) View.VISIBLE else View.GONE
                            )

                            // Show seconds when time left is under 1 month
                            val showSeconds = (years == 0L && months == 0L)
                            views.setViewVisibility(
                                R.id.widget_block_seconds,
                                if (showSeconds) View.VISIBLE else View.GONE
                            )
                        }

                        // Launch app intent to open this specific timer
                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra(EXTRA_WIDGET_TIMER_ID, timer.id)
                        }
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            widgetId,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                    } else {
                        val defaultColor = 0xFFF59E0B.toInt()
                        views.setTextViewText(R.id.widget_event_title, "No timers created")

                        views.setTextViewText(R.id.widget_val_years, "--")
                        views.setTextViewText(R.id.widget_val_months, "--")
                        views.setTextViewText(R.id.widget_val_days, "--")
                        views.setTextViewText(R.id.widget_val_hours, "--")
                        views.setTextViewText(R.id.widget_val_minutes, "--")

                        views.setTextColor(R.id.widget_val_years, defaultColor)
                        views.setTextColor(R.id.widget_val_months, defaultColor)
                        views.setTextColor(R.id.widget_val_days, defaultColor)
                        views.setTextColor(R.id.widget_val_hours, defaultColor)
                        views.setTextColor(R.id.widget_val_minutes, defaultColor)

                        views.setTextViewText(R.id.widget_target_date, "Tap to add your first countdown")

                        val intent = Intent(context, MainActivity::class.java)
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            0,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                    }

                    appWidgetManager.updateAppWidget(widgetId, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun getAccentColorInt(colorIndex: Int): Int {
            return when (colorIndex) {
                0 -> 0xFFF59E0B.toInt() // AmberGold
                1 -> 0xFF06B6D4.toInt() // NeonCyan
                2 -> 0xFF6366F1.toInt() // ElectricIndigo
                3 -> 0xFF8B5CF6.toInt() // CosmicPurple
                4 -> 0xFF10B981.toInt() // EmeraldGreen
                5 -> 0xFFF43F5E.toInt() // RoseCoral
                6 -> 0xFF3B82F6.toInt() // SapphireBlue
                else -> 0xFFF59E0B.toInt()
            }
        }

        /**
         * Schedules an exact alarm to refresh widgets every minute, precisely on the minute boundary (XX:XX:00.000).
         */
        fun scheduleNextMinuteTick(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            // Compute exact milliseconds remaining until the next wall clock minute (seconds = 0, millis = 0)
            val nowWallTime = System.currentTimeMillis()
            var millisUntilNextMinute = 60_000L - (nowWallTime % 60_000L)
            // If we are within 250ms of the minute boundary, advance to the next full minute to avoid immediate duplicate trigger
            if (millisUntilNextMinute < 250L) {
                millisUntilNextMinute += 60_000L
            }

            val triggerAtElapsed = SystemClock.elapsedRealtime() + millisUntilNextMinute

            val intent = Intent(context, CountdownAppWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_WIDGET_TICK
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                8888,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.ELAPSED_REALTIME,
                            triggerAtElapsed,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setWindow(
                            AlarmManager.ELAPSED_REALTIME,
                            triggerAtElapsed,
                            1000L,
                            pendingIntent
                        )
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME,
                        triggerAtElapsed,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.ELAPSED_REALTIME,
                        triggerAtElapsed,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                alarmManager.set(
                    AlarmManager.ELAPSED_REALTIME,
                    triggerAtElapsed,
                    pendingIntent
                )
            }
        }

        fun cancelMinuteTick(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, CountdownAppWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_WIDGET_TICK
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                8888,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
        }

        fun triggerUpdate(context: Context) {
            val intent4x1 = Intent(context, CountdownAppWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            context.sendBroadcast(intent4x1)

            val intent3x1 = Intent(context, CountdownWidget3x1Provider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            context.sendBroadcast(intent3x1)

            val intent5x1 = Intent(context, CountdownWidget5x1Provider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            context.sendBroadcast(intent5x1)

            // Re-sync ticker schedule on any explicit update
            scheduleNextMinuteTick(context)
        }
    }
}
