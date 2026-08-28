package com.shrikantbadwaik.landingkit

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import com.shrikantbadwaik.landingkit.feature.kits.notification.ArrivalReminderIntent
import com.shrikantbadwaik.travel.with.thikana.R

class ArrivalModeWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        updateWidget(context, appWidgetManager, appWidgetId, newOptions)
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        options: Bundle = appWidgetManager.getAppWidgetOptions(appWidgetId),
    ) {
        val widthDp = options.getInt(
            AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
            WIDE_LAYOUT_MIN_WIDTH_DP,
        )
        appWidgetManager.updateAppWidget(
            appWidgetId,
            arrivalModeRemoteViews(context, compact = widthDp < WIDE_LAYOUT_MIN_WIDTH_DP),
        )
    }

    private companion object {
        const val WIDE_LAYOUT_MIN_WIDTH_DP = 220
    }
}

internal fun arrivalModeRemoteViews(context: Context, compact: Boolean): RemoteViews {
    val launchIntent = Intent(context, MainActivity::class.java)
        .setAction(ArrivalReminderIntent.ACTION_OPEN_NEXT_ARRIVAL_MODE)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    val openArrivalMode = PendingIntent.getActivity(
        context,
        0,
        launchIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val layout = if (compact) {
        R.layout.arrival_mode_widget_compact
    } else {
        R.layout.arrival_mode_widget
    }
    return RemoteViews(context.packageName, layout).apply {
        setOnClickPendingIntent(R.id.arrival_widget_root, openArrivalMode)
        setOnClickPendingIntent(R.id.arrival_widget_open, openArrivalMode)
    }
}
