package net.guildem.publicip

import android.content.Context

class PublicIpData(context: Context) {

    private val prefs = context.getSharedPreferences("default", 0)

    fun loadIsRefreshing(): Boolean {
        return prefs.getBoolean("isRefreshing", false)
    }

    fun loadCurrentIp(): String? {
        return prefs.getString("currentIp", null)
    }

    fun update(isRefreshing: Boolean, currentIp: String? = null) {
        with(prefs.edit()) {
            putBoolean("isRefreshing", isRefreshing)
            if (!isRefreshing) {
                putString("currentIp", currentIp)
            }
            commit()
        }
    }

}