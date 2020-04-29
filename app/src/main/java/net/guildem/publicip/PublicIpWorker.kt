package net.guildem.publicip

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.nio.charset.Charset

class PublicIpWorker(context: Context, parameters: WorkerParameters)
    : CoroutineWorker(context, parameters) {

    companion object {
        const val Progress = "Progress"

        fun getWorker(forceConnected: Boolean = false): WorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(if (forceConnected) NetworkType.CONNECTED else NetworkType.NOT_REQUIRED)
                .build()

            val work = OneTimeWorkRequestBuilder<PublicIpWorker>()
                .setConstraints(constraints)
                .build()

            return work
        }
    }

    override suspend fun doWork(): Result {
        val data = PublicIpData(applicationContext)

        data.update(true)
        PublicIpWidget.updateAllWidgets(applicationContext)
        to(workDataOf(Progress to 0))

        try {
            data.update(false, findIpFromIpify())
        } catch (ex: Exception) {
            data.update(false, null)
            WorkManager.getInstance(applicationContext).enqueue(getWorker(true))
        } finally {
            PublicIpWidget.updateAllWidgets(applicationContext)
            to(workDataOf(Progress to 100))
        }

        return Result.success()
    }

    private fun findIpFromIpify(): String? {
        val url = "https://api.ipify.org?format=json"
        val json = URL(url).readText(Charset.defaultCharset())
        val obj = JSONObject(json)
        return obj.getString("ip")
    }
}
