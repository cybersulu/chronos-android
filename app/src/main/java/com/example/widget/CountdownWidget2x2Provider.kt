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
import com.example.model.CountdownCategories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class CountdownWidget2x2Provider : AppWidgetProvider() {

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
                ComponentName(context, CountdownWidget2x2Provider::class.java)
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
                        db.countdownDao().getFirstTimer()
                    }

                    val views = RemoteViews(context.packageName, R.layout.widget_countdown_2x2)

                    // Dynamically calculate and apply proportional typography and padding
                    val dims = WidgetSizeHelper.getWidgetDimensions(
                        appWidgetManager = appWidgetManager,
                        widgetId = widgetId,
                        defaultWidthDp = 140,
                        defaultHeightDp = 140
                    )
                    val scale = WidgetSizeHelper.computeScale(context, dims)
                    WidgetSizeHelper.applyScaling(
                        views = views,
                        scale = scale,
                        rootId = R.id.widget_2x2_root,
                        titleId = R.id.widget_2x2_event_title,
                        dateId = R.id.widget_2x2_target_date,
                        arrivedId = R.id.widget_2x2_arrived_banner,
                        valIds = listOf(
                            R.id.widget_2x2_val_1,
                            R.id.widget_2x2_val_2,
                            R.id.widget_2x2_val_3,
                            R.id.widget_2x2_val_4
                        ),
                        lblIds = listOf(
                            R.id.widget_2x2_lbl_1,
                            R.id.widget_2x2_lbl_2,
                            R.id.widget_2x2_lbl_3,
                            R.id.widget_2x2_lbl_4
                        )
                    )

                    if (timer != null) {
                        val accentColorInt = CountdownCategories.getColorHexForCategory(timer.category).toInt()
                        val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
                        val breakdown = CountdownBreakdown.compute(timer.targetEpochMillis, timer.timeZoneId, is24Hour)

                        // Event Title
                        views.setTextViewText(R.id.widget_2x2_event_title, timer.title)

                        // Target date & time string
                        views.setTextViewText(
                            R.id.widget_2x2_target_date,
                            "${breakdown.formattedTargetDate} • ${breakdown.timeZoneDisplayName}"
                        )

                        if (breakdown.isPast) {
                            views.setViewVisibility(R.id.widget_2x2_grid_countdown, View.GONE)
                            views.setViewVisibility(R.id.widget_2x2_arrived_banner, View.VISIBLE)
                        } else {
                            views.setViewVisibility(R.id.widget_2x2_grid_countdown, View.VISIBLE)
                            views.setViewVisibility(R.id.widget_2x2_arrived_banner, View.GONE)

                            val years = breakdown.years
                            val months = breakdown.months
                            val days = breakdown.days
                            val hours = breakdown.hours
                            val minutes = breakdown.minutes
                            val seconds = breakdown.seconds

                            // 4 prominent units adapted to countdown magnitude
                            val (u1Val, u1Lbl, u2Val, u2Lbl, u3Val, u3Lbl, u4Val, u4Lbl) = when {
                                years > 0L -> Tuple4(
                                    years, "YEARS",
                                    months, "MONTHS",
                                    days, "DAYS",
                                    hours, "HOURS"
                                )
                                months > 0L -> Tuple4(
                                    months, "MONTHS",
                                    days, "DAYS",
                                    hours, "HOURS",
                                    minutes, "MINS"
                                )
                                else -> Tuple4(
                                    days, "DAYS",
                                    hours, "HOURS",
                                    minutes, "MINS",
                                    seconds, "SECS"
                                )
                            }

                            views.setTextViewText(R.id.widget_2x2_val_1, String.format(Locale.US, "%02d", u1Val))
                            views.setTextViewText(R.id.widget_2x2_lbl_1, u1Lbl)
                            views.setTextColor(R.id.widget_2x2_val_1, accentColorInt)

                            views.setTextViewText(R.id.widget_2x2_val_2, String.format(Locale.US, "%02d", u2Val))
                            views.setTextViewText(R.id.widget_2x2_lbl_2, u2Lbl)
                            views.setTextColor(R.id.widget_2x2_val_2, accentColorInt)

                            views.setTextViewText(R.id.widget_2x2_val_3, String.format(Locale.US, "%02d", u3Val))
                            views.setTextViewText(R.id.widget_2x2_lbl_3, u3Lbl)
                            views.setTextColor(R.id.widget_2x2_val_3, accentColorInt)

                            views.setTextViewText(R.id.widget_2x2_val_4, String.format(Locale.US, "%02d", u4Val))
                            views.setTextViewText(R.id.widget_2x2_lbl_4, u4Lbl)
                            views.setTextColor(R.id.widget_2x2_val_4, accentColorInt)
                        }

                        // Launch app intent to open this specific timer
                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra(CountdownAppWidgetProvider.EXTRA_WIDGET_TIMER_ID, timer.id)
                        }
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            widgetId + 30000,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_2x2_root, pendingIntent)
                    } else {
                        // Empty State when no timer exists
                        views.setTextViewText(R.id.widget_2x2_event_title, "No Countdowns")
                        views.setTextViewText(R.id.widget_2x2_target_date, "Tap to create one")
                        views.setViewVisibility(R.id.widget_2x2_grid_countdown, View.GONE)
                        views.setViewVisibility(R.id.widget_2x2_arrived_banner, View.VISIBLE)
                        views.setTextViewText(R.id.widget_2x2_arrived_banner, "TAP TO ADD EVENT")

                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            widgetId + 30000,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_2x2_root, pendingIntent)
                    }

                    appWidgetManager.updateAppWidget(widgetId, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private data class Tuple4(
            val v1: Long, val l1: String,
            val v2: Long, val l2: String,
            val v3: Long, val l3: String,
            val v4: Long, val l4: String
        )
    }
}
