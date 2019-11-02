package net.guildem.publicip

import android.content.Context
import org.json.JSONObject
import java.net.URL
import java.nio.charset.Charset

class IpFinder(context: Context) {

    private val data = PublicIpData(context)

    fun update(callback: () -> Unit) {
        data.update(true)

        val thread = Thread(Runnable {
            data.update(false, findIpFromIpify())
            callback()
        })

        thread.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, _ ->
            data.update(false, null)
            callback()
        }

        thread.start()
    }

    private fun findIpFromIpify(): String? {
        val url = "https://api.ipify.org?format=json"
        val json = URL(url).readText(Charset.defaultCharset())
        val obj = JSONObject(json)
        return obj.getString("ip")
    }

}