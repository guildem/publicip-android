package net.guildem.publicip

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import kotlinx.android.synthetic.main.public_ip_activity.*

class PublicIpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.public_ip_activity)

        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }

        view_layout.setOnClickListener { refreshData() }
    }

    override fun onResume() {
        super.onResume()

        refreshData()
    }

    private fun refreshData() {
        IpFinder(this).update {
            PublicIpWidget.updateAllWidgets(baseContext)
            refreshView()
        }
        PublicIpWidget.updateAllWidgets(baseContext)
        refreshView()
    }

    private fun refreshView() {
        val data = PublicIpData(this)
        runOnUiThread {
            if (data.loadIsRefreshing()) {
                progress_bar?.visibility = View.VISIBLE
                ip_text.visibility = View.INVISIBLE
            } else {
                progress_bar?.visibility = View.INVISIBLE
                ip_text.visibility = View.VISIBLE
            }
            ip_text.text = data.loadCurrentIp() ?: getString(R.string.ip_not_found)
        }
    }

}
