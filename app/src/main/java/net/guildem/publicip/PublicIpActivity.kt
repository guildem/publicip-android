package net.guildem.publicip

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.android.synthetic.main.public_ip_activity.*


class PublicIpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.public_ip_activity)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }

        view_layout.setOnClickListener { refreshData() }
    }

    override fun onResume() {
        super.onResume()

        refreshData()
    }

    private fun refreshData() {
        val manager = WorkManager.getInstance(this)
        val worker = PublicIpWorker.getWorker()

        manager.getWorkInfoByIdLiveData(worker.id)
            .observe(this, Observer<WorkInfo> { refreshView() })

        manager.enqueue(worker)
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
