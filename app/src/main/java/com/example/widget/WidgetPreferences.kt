package com.example.widget

import android.content.Context

object WidgetPreferences {
    private const val PREFS_NAME = "com.example.widget.WidgetPreferences"
    private const val PREF_PREFIX_KEY = "widget_timer_id_"

    fun saveWidgetTimerId(context: Context, appWidgetId: Int, timerId: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(PREF_PREFIX_KEY + appWidgetId, timerId).apply()
    }

    fun getWidgetTimerId(context: Context, appWidgetId: Int): Long? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getLong(PREF_PREFIX_KEY + appWidgetId, -1L)
        return if (id != -1L) id else null
    }

    fun removeWidgetTimerId(context: Context, appWidgetId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(PREF_PREFIX_KEY + appWidgetId).apply()
    }
}
