package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.TypedValue
import android.widget.RemoteViews

object WidgetSizeHelper {

    data class WidgetDimensions(
        val widthDp: Int,
        val heightDp: Int
    )

    fun getWidgetDimensions(
        appWidgetManager: AppWidgetManager,
        widgetId: Int,
        defaultWidthDp: Int,
        defaultHeightDp: Int
    ): WidgetDimensions {
        val options = appWidgetManager.getAppWidgetOptions(widgetId)
        val minW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
        val minH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
        val w = if (minW > 0) minW else defaultWidthDp
        val h = if (minH > 0) minH else defaultHeightDp
        return WidgetDimensions(w, h)
    }

    data class ScaledTypography(
        val titleSp: Float,
        val dateSp: Float,
        val digitSp: Float,
        val labelSp: Float,
        val arrivedSp: Float,
        val digitPaddingV: Int,
        val rootPadding: Int
    )

    fun computeScale(context: Context, dims: WidgetDimensions): ScaledTypography {
        val density = context.resources.displayMetrics.density
        val w = dims.widthDp
        val h = dims.heightDp

        // Proportional Title font size (12.5sp to 24sp)
        val titleSp = when {
            h >= 140 -> 24f
            h >= 90 -> 20f
            h >= 65 -> 17f
            w >= 360 -> 18f
            w >= 260 -> 16f
            w >= 200 -> 14f
            else -> 12.5f
        }

        // Proportional Target date font size (9sp to 13.5sp)
        val dateSp = when {
            h >= 140 -> 13.5f
            h >= 90 -> 12f
            h >= 65 -> 11f
            w >= 260 -> 10.5f
            else -> 9f
        }

        // Proportional Countdown Digit numbers font size (12sp to 28sp)
        val digitSp = when {
            h >= 140 -> 28f
            h >= 90 -> 22f
            h >= 65 -> 17f
            w >= 360 -> 17f
            w >= 260 -> 15f
            w >= 200 -> 13.5f
            else -> 12f
        }

        // Proportional Unit label font size (7sp to 11.5sp)
        val labelSp = when {
            h >= 140 -> 11.5f
            h >= 90 -> 10f
            h >= 65 -> 8.5f
            w >= 260 -> 8f
            else -> 7f
        }

        // Celebration arrived banner font size (13sp to 20sp)
        val arrivedSp = when {
            h >= 100 -> 20f
            h >= 65 -> 16f
            else -> 13.5f
        }

        // Proportional vertical padding inside digit boxes (in pixels)
        val digitPaddingV = when {
            h >= 140 -> (12 * density).toInt()
            h >= 90 -> (8 * density).toInt()
            h >= 65 -> (5 * density).toInt()
            else -> (3 * density).toInt()
        }

        // Outer root padding
        val rootPadding = when {
            h >= 140 || w >= 360 -> (14 * density).toInt()
            h >= 70 -> (12 * density).toInt()
            else -> (8 * density).toInt()
        }

        return ScaledTypography(
            titleSp = titleSp,
            dateSp = dateSp,
            digitSp = digitSp,
            labelSp = labelSp,
            arrivedSp = arrivedSp,
            digitPaddingV = digitPaddingV,
            rootPadding = rootPadding
        )
    }

    fun applyScaling(
        views: RemoteViews,
        scale: ScaledTypography,
        rootId: Int,
        titleId: Int,
        dateId: Int,
        arrivedId: Int,
        valIds: List<Int>,
        lblIds: List<Int>
    ) {
        views.setViewPadding(rootId, scale.rootPadding, scale.rootPadding, scale.rootPadding, scale.rootPadding)
        views.setTextViewTextSize(titleId, TypedValue.COMPLEX_UNIT_SP, scale.titleSp)
        views.setTextViewTextSize(dateId, TypedValue.COMPLEX_UNIT_SP, scale.dateSp)
        views.setTextViewTextSize(arrivedId, TypedValue.COMPLEX_UNIT_SP, scale.arrivedSp)

        for (valId in valIds) {
            views.setTextViewTextSize(valId, TypedValue.COMPLEX_UNIT_SP, scale.digitSp)
            views.setViewPadding(valId, 0, scale.digitPaddingV, 0, scale.digitPaddingV)
        }

        for (lblId in lblIds) {
            views.setTextViewTextSize(lblId, TypedValue.COMPLEX_UNIT_SP, scale.labelSp)
        }
    }
}
