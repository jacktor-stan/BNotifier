@file:Suppress("DEPRECATION")

package com.jacktorscript.batterynotifier.core

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.Parcel
import android.os.SystemClock
import android.text.format.Time
import com.jacktorscript.batterynotifier.R
import java.util.*
import java.lang.reflect.Field
import kotlin.math.abs


object BatteryInfo {
    //Level
    fun getBatteryLevel(context: Context): Int {
        return receiver(context)!!.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
    }

    //Temperature
    fun getTemperature(context: Context): Int {
        return receiver(context)!!.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) * 10
    }

    fun getTemperatureString(context: Context, useFahrenheit: Boolean): String {
        var temperature = getTemperature(context)
        if (useFahrenheit) temperature = temperature / 5 * 9 + 3200
        return (temperature / 100).toString() + "." + abs(temperature % 100) +
                "\u00B0" + if (useFahrenheit) "F" else "C"
    }


    //Voltage
    fun getVoltage(context: Context): Double {
        return (receiver(context)!!
            .getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) / 1000.0)
    }

    fun getVoltageString(context: Context): String {
        return getVoltage(context).toString() + "V"
    }


    fun getCurrentNow(context: Context): Int {
        val manager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW).div(1000)
    }

    //State
    fun getStatus(context: Context): Int {
        return receiver(context)!!.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    }

    fun getStatusString(context: Context): String {
        var status = context.getString(R.string.unknown)
        when (getStatus(context)) {
            1 -> {
                status = context.getString(R.string.unknown)
            }
            2 -> {
                status = context.getString(R.string.charging)
            }
            3 -> {
                status = context.getString(R.string.discharging)
            }
            4 -> {
                status = context.getString(R.string.notcharging)
            }
            5 -> {
                status = context.getString(R.string.full)
            }
        }
        return status
    }


    //Status
    fun getPlugged(context: Context): Int {
        return receiver(context)!!.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
    }

    fun getPluggedString(context: Context): String {
        var type = context.getString(R.string.unknown)
        when (getPlugged(context)) {
            0 -> {
                type = context.getString(R.string.not_plugged)
            }
            1 -> {
                type = context.getString(R.string.ac)
            }
            2 -> {
                type = context.getString(R.string.usb)
            }
            3 -> {
                type = context.getString(R.string.wireless)
            }
        }
        return type
    }


    //Health
    fun getHealth(context: Context): Int {
        return receiver(context)!!.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
    }

    fun getHealthString(context: Context): String {
        var health = context.getString(R.string.unknown)
        when (getHealth(context)) {
            1 -> {
                health = context.getString(R.string.unknown)
            }
            2 -> {
                health = context.getString(R.string.good)
            }
            3 -> {
                health = context.getString(R.string.overheat)
            }
            4 -> {
                health = context.getString(R.string.dead)
            }
            5 -> {
                health = context.getString(R.string.overvolt)
            }
            6 -> {
                health = context.getString(R.string.failed)
            }
            7 -> {
                health = context.getString(R.string.cold)
            }
        }
        return health
    }


    fun getTechnology(context: Context): String? {
        val tech = receiver(context)!!.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
        val techStr = if (tech == "") {
            "N/A"
        } else {
            tech
        }

        return techStr
    }

    //designed battery capacity
    @SuppressLint("PrivateApi")
    fun getBatteryDesignCapacity(context: Context?): Int {
        val mPowerProfile: Any
        var batteryCapacity = 0.0
        val powerProfileClass = "com.android.internal.os.PowerProfile"
        try {
            mPowerProfile = Class.forName(powerProfileClass)
                .getConstructor(Context::class.java)
                .newInstance(context)
            batteryCapacity = Class
                .forName(powerProfileClass)
                .getMethod("getBatteryCapacity")
                .invoke(mPowerProfile) as Double
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return batteryCapacity.toInt()
    }

    fun getBatteryDesignCapacityString(context: Context?): String {
        return getBatteryDesignCapacity(context).toString() + " mAh"
    }


    private fun receiver(context: Context): Intent? {
        return context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
    }


    fun getLastTime(context: Context): String {
        var lastTime = ""
        val prefsConfig = PrefsConfig(context)

        //config awal
        if (prefsConfig.getBoolean("first_config", true)) {
            prefsConfig.setBoolean("first_config", false)
            prefsConfig.setInt(
                "last_state_change_battery_level",
                getBatteryLevel(context)
            )
            prefsConfig.setLong("last_state_change_time", Date().time)
        }

        val t = prefsConfig.getLong("last_state_change_time", 0)
        if (t != 0L) {
            val time = Time()
            time.set(t)
            time.format("%b")
            val time2 = Date().time - t
            val t2 = time2 / 1000 / 60 / 60 % 24

            lastTime = context.getString(
                R.string.battery_info, getStatusString(context),
                prefsConfig.getInt("last_state_change_battery_level", 0),
                (time2 / 1000 / 24 / 60 / 60), t2,
                (time2 / 1000 / 60 % 60)
            )
        }
        return lastTime
    }


    @SuppressLint("PrivateApi", "SoonBlockedPrivateApi")
    fun getTimeRemaining(context: Context): String {
        if (Build.VERSION.SDK_INT >= 28) {
            try {
                //Somehow Android made things easier instead of harder
                val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val fieldBatteryStats: Field =
                    BatteryManager::class.java.getDeclaredField("mBatteryStats")
                fieldBatteryStats.isAccessible = true
                val batteryStats: Any = fieldBatteryStats.get(bm) as Any
                val batteryTimeRemaining = batteryStats
                    .javaClass
                    .getMethod("computeBatteryTimeRemaining")
                    .invoke(batteryStats) as Long
                val chargeTimeRemaining = batteryStats
                    .javaClass
                    .getMethod("computeChargeTimeRemaining")
                    .invoke(batteryStats) as Long
                if (receiver(context)!!.getIntExtra(
                        BatteryManager.EXTRA_STATUS,
                        -1
                    ) == BatteryManager.BATTERY_STATUS_DISCHARGING
                ) {
                    return computeTimeString(context, (batteryTimeRemaining / 1000).toInt())
                } else if (receiver(context)!!.getIntExtra(
                        BatteryManager.EXTRA_STATUS,
                        -1
                    ) == BatteryManager.BATTERY_STATUS_CHARGING
                ) {
                    return computeTimeString(context, (chargeTimeRemaining / 1000).toInt())
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        } else
            try {
                //IBatteryStats iBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats");
                val batteryStatsIBinder = Class.forName("android.os.ServiceManager")
                    .getMethod("getService", String::class.java)
                    .invoke(null, "batterystats") as IBinder
                val iBatteryStats = Class
                    .forName("com.android.internal.app.IBatteryStats\$Stub")
                    .getMethod("asInterface", IBinder::class.java)
                    .invoke(null, batteryStatsIBinder)
                if (iBatteryStats != null) {
                    //byte[] data = iBatteryStats.getStatistics();
                    val data = iBatteryStats
                        .javaClass
                        .getMethod("getStatistics")
                        .invoke(iBatteryStats) as ByteArray
                    val parcel = Parcel.obtain()
                    parcel.unmarshall(data, 0, data.size)
                    parcel.setDataPosition(0)
                    val creator = Class
                        .forName("com.android.internal.os.BatteryStatsImpl")
                        .getField("CREATOR")[null]
                    if (creator != null) {
                        //BatteryStats batteryStats = BatteryStatsImpl.CREATOR.createFromParcel(parcel);
                        val batteryStats = creator
                            .javaClass
                            .getMethod("createFromParcel", Parcel::class.java)
                            .invoke(creator, parcel)
                        parcel.recycle()
                        if (batteryStats != null) {
                            val now = SystemClock.elapsedRealtime() * 1000
                            val batteryTimeRemaining = batteryStats
                                .javaClass
                                .getMethod(
                                    "computeBatteryTimeRemaining",
                                    Long::class.javaPrimitiveType
                                )
                                .invoke(batteryStats, now) as Long
                            val chargeTimeRemaining = batteryStats
                                .javaClass
                                .getMethod(
                                    "computeChargeTimeRemaining",
                                    Long::class.javaPrimitiveType
                                )
                                .invoke(batteryStats, now) as Long
                            if (receiver(context)!!.getIntExtra(
                                    BatteryManager.EXTRA_STATUS,
                                    -1
                                ) == BatteryManager.BATTERY_STATUS_DISCHARGING
                            ) {
                                return computeTimeString(
                                    context,
                                    (batteryTimeRemaining / 1000000).toInt()
                                )
                            } else if (receiver(context)!!.getIntExtra(
                                    BatteryManager.EXTRA_STATUS,
                                    -1
                                ) == BatteryManager.BATTERY_STATUS_CHARGING
                            ) {
                                return computeTimeString(
                                    context,
                                    (chargeTimeRemaining / 1000000).toInt()
                                )
                            }
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        return ""
    }


    private fun computeTimeString(context: Context, secsRemaining: Int): String {
        if (secsRemaining < 1) {
            return ""
        }
        val min = secsRemaining / 60 % 60
        val hour = secsRemaining / (60 * 60) % 24
        val day = secsRemaining / (60 * 60 * 24)
        return if (day > 0) {
            context.getString(R.string.format_time_remaining_days, day, hour, min)
        } else if (hour > 0) {
            context.getString(R.string.format_time_remaining_hours, hour, min)
        } else {
            context.getString(R.string.format_time_remaining_mins, min)
        }
    }


}