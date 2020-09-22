package net.guildem.publicip

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.core.widget.addTextChangedListener
import androidx.work.WorkManager
import kotlinx.android.synthetic.main.main_widget.*
import kotlinx.android.synthetic.main.main_widget_activity.*

class MainWidgetActivity : AppCompatActivity() {
    
    private var mDarkMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_widget_activity)

        widget.setBackgroundResource(R.drawable.config_background)

        widget.setOnClickListener { toggleDarkMode(!mDarkMode) }
        text_value.addTextChangedListener { applyWidgetValues() }
        color_value.addTextChangedListener { applyWidgetValues() }
        validate_button.setOnClickListener { validateAppWidget() }
        cancel_button.setOnClickListener { finish() }

        toggleDarkMode(true)
        applyWidgetValues()

        setResult(RESULT_CANCELED)
    }

    private fun getWidgetText(): String {
        val text = if (text_value.text.isNotEmpty()) text_value.text.toString() else getString(R.string.view_title)
        return if (TextUtils.isEmpty(text)) getString(R.string.view_title) else text
    }

    private fun getWidgetColor(): Int {
        return try {
            Color.parseColor(color_value.text!!.toString())
        } catch (e: Exception) {
            getColor(R.color.colorWidgetText)
        }
    }

    private fun toggleDarkMode(darkMode: Boolean) {
        mDarkMode = darkMode
        
        val color = if (darkMode) Color.BLACK else Color.WHITE
        widget.backgroundTintList = ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, 0xAF))
    }

    private fun applyWidgetValues() {
        title_text.visibility = View.INVISIBLE
        ip_text.visibility = View.INVISIBLE
        progress_bar.visibility = View.VISIBLE

        Handler().postDelayed({
            title_text.text = getWidgetText()
            ip_text.text = getString(R.string.view_text)

            val color = getWidgetColor()
            title_text.setTextColor(color)
            ip_text.setTextColor(color)

            title_text.visibility = View.VISIBLE
            ip_text.visibility = View.VISIBLE
            progress_bar.visibility = View.INVISIBLE

            widget.requestLayout()
        }, 300)
    }

    private fun validateAppWidget() {
        val extras = intent?.extras

        if (extras != null) {
            val appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val sharedPreferences = getSharedPreferences("widget_$appWidgetId", Context.MODE_PRIVATE)
                sharedPreferences.edit()
                    .putString("title", getWidgetText())
                    .putInt("color", getWidgetColor())
                    .apply()

                setResult(RESULT_OK, Intent().apply { putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId) })

                val manager = WorkManager.getInstance(this)
                manager.enqueue(Worker.getWorker())
            }
        }

        finish()
    }
}