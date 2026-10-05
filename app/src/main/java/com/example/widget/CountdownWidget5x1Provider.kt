package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.model.CountdownBreakdown
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class CountdownWidget5x1Provider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (widgetId in appWidgetIds) {
            updateSingleWidgetAsync(context, appWidgetManager, widgetId)
        }
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

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        CountdownAppWidgetProvider.scheduleNextMinuteTick(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == CountdownAppWidgetProvider.ACTION_UPDATE_WIDGET_TICK ||
            intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, CountdownWidget5x1Provider::class.java)
            )
            for (widgetId in ids) {
                updateSingleWidgetAsync(context, appWidgetManager, widgetId)
            }
        }
    }

    companion object {
        fun updateSingleWidgetAsync(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val selectedTimerId = WidgetPreferences.getWidgetTimerId(context, widgetId)
                    val timer = if (selectedTimerId != null) {
                        db.countdownDao().getTimerByIdSync(selectedTimerId)
                    } else {
                        db.countdownDao().getPinnedWidgetTimer() ?: db.countdownDao().getFirstTimer()
                    }

                    val views = RemoteViews(context.packageName, R.layout.widget_countdown_5x1)

                    // Dynamically calculate and apply proportional typography and padding based on adjusted widget size
                    val dims = WidgetSizeHelper.getWidgetDimensions(
                        appWidgetManager = appWidgetManager,
                        widgetId = widgetId,
                        defaultWidthDp = 300,
                        defaultHeightDp = 40
                    )
                    val scale = WidgetSizeHelper.computeScale(context, dims)
                    WidgetSizeHelper.applyScaling(
                        views = views,
                        scale = scale,
                        rootId = R.id.widget_5x1_root,
                        titleId = R.id.widget_5x1_event_title,
                        dateId = R.id.widget_5x1_target_date,
                        arrivedId = R.id.widget_5x1_arrived_banner,
                        valIds = listOf(
                            R.id.widget_5x1_val_years,
                            R.id.widget_5x1_val_months,
                            R.id.widget_5x1_val_days,
                            R.id.widget_5x1_val_hours,
                            R.id.widget_5x1_val_minutes,
                            R.id.widget_5x1_val_seconds
                        ),
                        lblIds = listOf(
                            R.id.widget_5x1_lbl_years,
                            R.id.widget_5x1_lbl_months,
                            R.id.widget_5x1_lbl_days,
                            R.id.widget_5x1_lbl_hours,
                            R.id.widget_5x1_lbl_minutes,
                            R.id.widget_5x1_lbl_seconds
                        )
                    )

                    if (timer != null) {
                        val accentColorInt = getAccentColorInt(timer.colorIndex)
                        val breakdown = CountdownBreakdown.compute(timer.targetEpochMillis, timer.timeZoneId)

                        views.setTextViewText(R.id.widget_5x1_event_title, timer.title)
                        views.setTextViewText(
                            R.id.widget_5x1_target_date,
                            "${breakdown.formattedTargetDate} • ${breakdown.timeZoneDisplayName}"
                        )

                        if (breakdown.isPast) {
                            views.setViewVisibility(R.id.widget_5x1_grid_countdown, View.GONE)
                            views.setViewVisibility(R.id.widget_5x1_arrived_banner, View.VISIBLE)
                        } else {
                            views.setViewVisibility(R.id.widget_5x1_grid_countdown, View.VISIBLE)
                            views.setViewVisibility(R.id.widget_5x1_arrived_banner, View.GONE)

                            val years = breakdown.years
                            val months = breakdown.months
                            val days = breakdown.days
                            val hours = breakdown.hours
                            val minutes = breakdown.minutes
                            val seconds = breakdown.seconds

                            views.setTextViewText(R.id.widget_5x1_val_years, String.format(Locale.US, "%02d", years))
                            views.setTextViewText(R.id.widget_5x1_val_months, String.format(Locale.US, "%02d", months))
                            views.setTextViewText(R.id.widget_5x1_val_days, String.format(Locale.US, "%02d", days))
                            views.setTextViewText(R.id.widget_5x1_val_hours, String.format(Locale.US, "%02d", hours))
                            views.setTextViewText(R.id.widget_5x1_val_minutes, String.format(Locale.US, "%02d", minutes))
                            views.setTextViewText(R.id.widget_5x1_val_seconds, String.format(Locale.US, "%02d", seconds))

                            views.setTextColor(R.id.widget_5x1_val_years, accentColorInt)
                            views.setTextColor(R.id.widget_5x1_val_months, accentColorInt)
                            views.setTextColor(R.id.widget_5x1_val_days, accentColorInt)
                            views.setTextColor(R.id.widget_5x1_val_hours, accentColorInt)
                            views.setTextColor(R.id.widget_5x1_val_minutes, accentColorInt)
                            views.setTextColor(R.id.widget_5x1_val_seconds, accentColorInt)

                            // Don't show time interval if time left is lower than that unit
                            views.setViewVisibility(
                                R.id.widget_5x1_block_years,
                                if (years > 0L) View.VISIBLE else View.GONE
                            )
                            views.setViewVisibility(
                                R.id.widget_5x1_block_months,
                                if (years > 0L || months > 0L) View.VISIBLE else View.GONE
                            )
                            views.setViewVisibility(
                                R.id.widget_5x1_block_days,
                                if (years > 0L || months > 0L || days > 0L) View.VISIBLE else View.GONE
                            )
                            views.setViewVisibility(
                                R.id.widget_5x1_block_hours,
                                if (years > 0L || months > 0L || days > 0L || hours > 0L) View.VISIBLE else View.GONE
                            )
                            views.setViewVisibility(
                                R.id.widget_5x1_block_minutes,
                                if (years > 0L || months > 0L || days > 0L || hours > 0L || minutes > 0L) View.VISIBLE else View.GONE
                            )

                            val showSeconds = (years == 0L && months == 0L)
                            views.setViewVisibility(
                                R.id.widget_5x1_block_seconds,
                                if (showSeconds) View.VISIBLE else View.GONE
                            )
                        }

                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra(CountdownAppWidgetProvider.EXTRA_WIDGET_TIMER_ID, timer.id)
                        }
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            widgetId + 51000,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_5x1_root, pendingIntent)
                    } else {
                        val defaultColor = 0xFFF59E0B.toInt()
                        views.setTextViewText(R.id.widget_5x1_event_title, "No timers created")

                        views.setTextViewText(R.id.widget_5x1_val_years, "--")
                        views.setTextViewText(R.id.widget_5x1_val_months, "--")
                        views.setTextViewText(R.id.widget_5x1_val_days, "--")
                        views.setTextViewText(R.id.widget_5x1_val_hours, "--")
                        views.setTextViewText(R.id.widget_5x1_val_minutes, "--")

                        views.setTextColor(R.id.widget_5x1_val_years, defaultColor)
                        views.setTextColor(R.id.widget_5x1_val_months, defaultColor)
                        views.setTextColor(R.id.widget_5x1_val_days, defaultColor)
                        views.setTextColor(R.id.widget_5x1_val_hours, defaultColor)
                        views.setTextColor(R.id.widget_5x1_val_minutes, defaultColor)

                        views.setTextViewText(R.id.widget_5x1_target_date, "Tap to add your first countdown")

                        val intent = Intent(context, MainActivity::class.java)
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            5100,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_5x1_root, pendingIntent)
                    }

                    appWidgetManager.updateAppWidget(widgetId, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun getAccentColorInt(colorIndex: Int): Int {
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
    }
}
