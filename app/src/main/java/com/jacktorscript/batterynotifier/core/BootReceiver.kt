package com.jacktorscript.batterynotifier.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jacktorscript.batterynotifier.notification.NotificationService

class BootReceiver : BroadcastReceiver() {
    private var prefs: Prefs? = null

    override fun onReceive(context: Context, intent: Intent) {
        init(context)
        //val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (Intent.ACTION_BOOT_COMPLETED == intent.action && prefs!!.getBoolean(
                "start_on_boot", true
            )
        ) {

            //Foreground Service
            if (prefs!!.getBoolean("notification_service", true)) {
                NotificationService.startService(context)
            }
        }
    }

    private fun init(context: Context) {
        prefs = Prefs(context)
    }
}