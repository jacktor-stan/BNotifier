package com.jacktorscript.batterynotifier.notification

import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toIcon
import com.jacktorscript.batterynotifier.MainActivity
import com.jacktorscript.batterynotifier.R
import com.jacktorscript.batterynotifier.core.BatteryInfo
import com.jacktorscript.batterynotifier.core.Bitmap
import com.jacktorscript.batterynotifier.core.PowerReceiver
import com.jacktorscript.batterynotifier.core.Prefs

class NotificationService : Service() {
    private lateinit var receiver: PowerReceiver
    private var prefs: Prefs? = null
    private var color = 0


    override fun onCreate() {
        super.onCreate()
        init(this)

        //Register receiver (PowerReceiver)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            receiver = PowerReceiver()

            IntentFilter(Intent.ACTION_POWER_CONNECTED).also {
                registerReceiver(receiver, it)
            }

            IntentFilter(Intent.ACTION_POWER_DISCONNECTED).also {
                registerReceiver(receiver, it)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }


    companion object {

        fun startService(context: Context) {
            val startIntent = Intent(context, NotificationService::class.java)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, NotificationService::class.java)
            context.stopService(stopIntent)
        }

        const val CHANNEL_ID = "notificationService"
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, FLAG_IMMUTABLE
        )


        val notificationHandler = Handler(Looper.getMainLooper())
        notificationHandler.post(object : Runnable {
            override fun run() {
                if (prefs!!.getBoolean("notification_service", true)) {
                    //Battery Value
                    val level = BatteryInfo.getBatteryLevel(applicationContext)
                    val temperature =
                        BatteryInfo.getTemperatureString(
                            applicationContext,
                            prefs!!.getBoolean("fahrenheit", false)
                        )
                    val voltage = BatteryInfo.getVoltageString(applicationContext)
                    val status = BatteryInfo.getStatusString(applicationContext)
                    //val health = BatteryInfo.getHealthString(applicationContext)
                    val statusInt = BatteryInfo.getStatus(applicationContext)
                    val pluggedInt = BatteryInfo.getPlugged(applicationContext)
                    val current = BatteryInfo.getCurrentNow(applicationContext).toString() + " mA"

                    val tr = if (prefs!!.getBoolean(
                            "time_remaining",
                            false
                        ) && BatteryInfo.getTimeRemaining(applicationContext) != ""
                    ) {
                        "\u2022 " + BatteryInfo.getTimeRemaining(applicationContext)
                    } else {
                        ""
                    }

                    val plugged = if (pluggedInt != 0) {
                        BatteryInfo.getPluggedString(applicationContext) + " -"
                    } else {
                        ""
                    }

                    //Notifikasi
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val text = BatteryInfo.getBatteryLevel(applicationContext).toString()

                        val smallIcon =
                            Bitmap.textAsBitmap(
                                text,
                                100f,
                                ContextCompat.getColor(applicationContext, R.color.white)
                            )
                                ?.toIcon() //Text to bitmap > To Icon

                        val bigStyle: Notification.Style = Notification.BigTextStyle()
                            .bigText("$current \u2022 $voltage \u2022 $plugged $status")
                        //.setSummaryText(Battery.getLastTime(context));

                        val notification = Notification.Builder(applicationContext, CHANNEL_ID)
                            .setContentTitle("$level% \u2022 $temperature $tr")
                            .setSmallIcon(smallIcon)
                            .setColor(getColor(applicationContext, statusInt, level))
                            .setShowWhen(false)
                            .setContentIntent(pendingIntent)
                            .setOngoing(true)
                            .setStyle(bigStyle)
                            .build()

                        val notificationManager: NotificationManager =
                            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(1, notification)

                        startForeground(1, notification)
                    } else {

                        //Android versi 7 kebawah
                        //convert drawable to bitmap
                        //val largeIcon = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)

                        val notification =
                            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                                .setContentTitle("$level% \u2022 $temperature $tr")
                                .setContentText("$current \u2022 $voltage \u2022 $plugged $status")
                                //.setLargeIcon(largeIcon)
                                .setSmallIcon(LegacyStatusBar.stat(level))
                                .setColor(getColor(applicationContext, statusInt, level))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setShowWhen(false)
                                .setContentIntent(pendingIntent)
                                .setOngoing(true)
                                .build()

                        val notificationManager: NotificationManager =
                            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(1, notification)

                        startForeground(1, notification)
                    }
                }

                if (prefs!!.getBoolean("notification_service", true)) {
                    notificationHandler.postDelayed(this, 2000)
                } else {
                    notificationHandler.removeCallbacks(this)
                }

            }
        })


        //stopSelf()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_service),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.setShowBadge(false)
            notificationChannel.enableLights(false)
            notificationChannel.enableVibration(false)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
            notificationChannel.description =
                getString(R.string.notification_channel_desc)
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager!!.createNotificationChannel(notificationChannel)
        }

    }

    //Color for notification accent - Android 8.0+
    private fun getColor(context: Context, state: Int, level: Int): Int {
        if (state == 2) {
            color = ContextCompat.getColor(context, R.color.blue)
        } else {
            //Standard
            if (level >= 21) {
                color = ContextCompat.getColor(context, R.color.green_dark)
            }
            //Low
            if (level <= 20) {
                color = ContextCompat.getColor(context, R.color.red)
            }
        }
        return color
    }

    private fun init(context: Context) {
        prefs = Prefs(context)
    }
}