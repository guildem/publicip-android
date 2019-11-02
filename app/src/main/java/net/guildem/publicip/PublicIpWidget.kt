package net.guildem.publicip

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.content.ComponentName

private const val ACTION_REFRESH = "net.guildem.publicip.action.ACTION_REFRESH"

class PublicIpWidget : AppWidgetProvider() {

    companion object {
        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, PublicIpWidget::class.java))

            for (id in ids) {
                updateAppWidget(context, manager, id)
            }
        }
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            updateAppWidget(context, manager, id)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if (context !== null && intent?.action === ACTION_REFRESH) {
            IpFinder(context).update {
                updateAllWidgets(context)
            }
            updateAllWidgets(context)
        }
    }

}

internal fun updateAppWidget(context: Context, manager: AppWidgetManager, id: Int) {
    val intent = Intent(context, PublicIpWidget::class.java).apply { action = ACTION_REFRESH }
    val tapIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

    val data = PublicIpData(context)
    val views = RemoteViews(context.packageName, R.layout.public_ip_widget)

    if (data.loadIsRefreshing()) {
        views.setViewVisibility(R.id.title_text, View.INVISIBLE)
        views.setViewVisibility(R.id.ip_text, View.INVISIBLE)
        views.setViewVisibility(R.id.progress_bar, View.VISIBLE)
    } else {
        views.setViewVisibility(R.id.title_text, View.VISIBLE)
        views.setViewVisibility(R.id.ip_text, View.VISIBLE)
        views.setViewVisibility(R.id.progress_bar, View.INVISIBLE)
    }
    views.setTextViewText(R.id.ip_text, data.loadCurrentIp() ?: context.getString(R.string.ip_not_found))
    views.setOnClickPendingIntent(R.id.layout, tapIntent)

    manager.updateAppWidget(id, views)
}