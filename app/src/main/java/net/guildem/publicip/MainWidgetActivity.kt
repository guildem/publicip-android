package net.guildem.publicip

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.widget.addTextChangedListener
import androidx.work.WorkManager
import net.guildem.publicip.databinding.MainWidgetActivityBinding

class MainWidgetActivity : AppCompatActivity() {
    private lateinit var binding: MainWidgetActivityBinding
    private var mDarkMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainWidgetActivityBinding.inflate(layoutInflater)
        setContentView(R.layout.main_widget_activity)

        binding.widget.layout.setBackgroundResource(R.drawable.config_background)
        binding.widget.layout.setOnClickListener { toggleDarkMode(!mDarkMode) }

        binding.textValue.addTextChangedListener { applyWidgetValues() }
        binding.colorValue.addTextChangedListener { applyWidgetValues() }
        binding.validateButton.setOnClickListener { validateAppWidget() }
        binding.cancelButton.setOnClickListener { finish() }

        toggleDarkMode(true)
        applyWidgetValues()

        setResult(RESULT_CANCELED)
    }

    private fun getWidgetText(): String {
        val text = if (binding.textValue.text.isNotEmpty()) binding.textValue.text.toString() else getString(R.string.view_title)
        return if (TextUtils.isEmpty(text)) getString(R.string.view_title) else text
    }

    private fun getWidgetColor(): Int {
        return try {
            Color.parseColor(binding.colorValue.text!!.toString())
        } catch (e: Exception) {
            ContextCompat.getColor(baseContext, R.color.colorWidgetText)
        }
    }

    private fun toggleDarkMode(darkMode: Boolean) {
        mDarkMode = darkMode
        
        val color = if (darkMode) Color.BLACK else Color.WHITE
        binding.widget.layout.backgroundTintList = ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, 0xAF))
    }

    private fun applyWidgetValues() {
        binding.widget.titleText.visibility = View.INVISIBLE
        binding.widget.ipText.visibility = View.INVISIBLE
        binding.widget.progressBar.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            binding.widget.titleText.text = getWidgetText()
            binding.widget.ipText.text = getString(R.string.view_text)

            val color = getWidgetColor()
            binding.widget.titleText.setTextColor(color)
            binding.widget.ipText.setTextColor(color)

            binding.widget.titleText.visibility = View.VISIBLE
            binding.widget.ipText.visibility = View.VISIBLE
            binding.widget.progressBar.visibility = View.INVISIBLE

            binding.widget.layout.requestLayout()
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