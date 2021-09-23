package net.guildem.publicip

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.work.WorkManager
import net.guildem.publicip.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(R.layout.main_activity)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT

        binding.viewLayout.setOnClickListener { refreshData() }
        binding.shareButton.setOnClickListener { shareData() }
    }

    override fun onResume() {
        super.onResume()

        refreshData()
    }

    private fun refreshData() {
        val manager = WorkManager.getInstance(this)
        val worker = Worker.getWorker()

        manager.getWorkInfoByIdLiveData(worker.id).observe(this) { refreshView() }
        manager.enqueue(worker)
    }

    private fun shareData() {
        val data = State(this)
        val shareText = data.loadCurrentIp() ?: getString(R.string.ip_not_found)
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_my_ip)))
    }

    private fun refreshView() {
        val data = State(this)
        runOnUiThread {
            if (data.loadIsRefreshing()) {
                binding.progressBar.visibility = View.VISIBLE
                binding.ipText.visibility = View.INVISIBLE
            } else {
                binding.progressBar.visibility = View.INVISIBLE
                binding.ipText.visibility = View.VISIBLE
            }
            binding.ipText.text = data.loadCurrentIp() ?: getString(R.string.ip_not_found)
        }
    }

}
