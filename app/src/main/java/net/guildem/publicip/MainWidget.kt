package net.guildem.publicip

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.content.ComponentName
import android.graphics.Color
import androidx.work.*

class MainWidget : AppWidgetProvider() {

    companion object {
        private const val ACTION_REFRESH = "net.guildem.publicip.action.ACTION_REFRESH"

        private fun updateAppWidget(context: Context, manager: AppWidgetManager, id: Int) {
            val intent = Intent(context, MainWidget::class.java).apply { action = ACTION_REFRESH }
            val tapIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

            val defaultTitle = context.getString(R.string.view_title)
            val data = State(context)
            val sharedPreferences = context.getSharedPreferences("widget_$id", Context.MODE_PRIVATE)
            val views = RemoteViews(context.packageName, R.layout.main_widget)

            if (data.loadIsRefreshing()) {
                views.setViewVisibility(R.id.title_text, View.INVISIBLE)
                views.setViewVisibility(R.id.ip_text, View.INVISIBLE)
                views.setViewVisibility(R.id.progress_bar, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.title_text, View.VISIBLE)
                views.setViewVisibility(R.id.ip_text, View.VISIBLE)
                views.setViewVisibility(R.id.progress_bar, View.INVISIBLE)
            }

            views.setTextViewText(R.id.title_text, sharedPreferences.getString("title", defaultTitle))
            views.setTextColor(R.id.title_text, sharedPreferences.getInt("color", Color.WHITE))
            views.setTextViewText(R.id.ip_text, data.loadCurrentIp() ?: context.getString(R.string.ip_not_found))
            views.setTextColor(R.id.ip_text, sharedPreferences.getInt("color", Color.WHITE))
            views.setOnClickPendingIntent(R.id.layout, tapIntent)

            manager.updateAppWidget(id, views)
        }

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, MainWidget::class.java))

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
            val manager = WorkManager.getInstance(context)
            manager.enqueue(Worker.getWorker())
        }
    }

    override fun onDeleted(context: Context?, ids: IntArray?) {
        ids?.let {
            for (id in ids) {
                val sharedPreferences = context?.getSharedPreferences("widget_$id", Context.MODE_PRIVATE)
                sharedPreferences?.edit()?.clear()?.apply()
            }
        }
    }

}