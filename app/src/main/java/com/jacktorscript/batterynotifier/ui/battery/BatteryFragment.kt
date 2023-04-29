package com.jacktorscript.batterynotifier.ui.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.jacktorscript.batterynotifier.MainActivity
import com.jacktorscript.batterynotifier.R
import com.jacktorscript.batterynotifier.databinding.FragmentBatteryBinding
import com.jacktorscript.batterynotifier.core.BatteryInfo
import com.jacktorscript.batterynotifier.core.CpuInfo
import com.jacktorscript.batterynotifier.widget.ArcProgress

class BatteryFragment : Fragment() {
    private var cpuMeter: ArcProgress? = null
    private var batteryMeter: ArcProgress? = null
    private var lastTime: TextView? = null
    private var batteryCurrent: TextView? = null
    private var batteryHealth: TextView? = null
    private var batteryStatus: TextView? = null
    private var batteryTechnology: TextView? = null
    private var batteryTemperature: TextView? = null
    private var batteryTemperatureF: TextView? = null
    private var batteryVoltage: TextView? = null
    private var batteryCapacity: TextView? = null
    private var pluggedType: TextView? = null

    private var pluggedTypeIcon: AppCompatImageView? = null
    private var stateIcon: AppCompatImageView? = null
    private var temperatureIcon: AppCompatImageView? = null
    private var wattageIcon: AppCompatImageView? = null

    private var _binding: FragmentBatteryBinding? = null


    private val batteryReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "android.intent.action.BATTERY_CHANGED") {
                //activity?.intent = intent
                if (isAdded) {
                    status
                }
            }
        }
    }


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBatteryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Ignore Battery Optimization
        //askIgnoreOptimization()

        //Battery receiver
        val intent = IntentFilter("android.intent.action.BATTERY_CHANGED")
        activity?.registerReceiver(batteryReceiver, intent)

        cpuMeter = view.findViewById(R.id.cpu_view_usage)
        batteryMeter = view.findViewById(R.id.battery_view)
        lastTime = view.findViewById(R.id.lastStateChangeTime)
        batteryCurrent = view.findViewById(R.id.txt_battery_current)
        batteryVoltage = view.findViewById(R.id.txt_battery_voltage)
        batteryTemperature = view.findViewById(R.id.txt_battery_temp)
        batteryTemperatureF = view.findViewById(R.id.txt_battery_temp_f)
        batteryTechnology = view.findViewById(R.id.txt_battery_tech)
        batteryStatus = view.findViewById(R.id.txt_battery_status)
        batteryCapacity = view.findViewById(R.id.txt_battery_capacity)
        batteryHealth = view.findViewById(R.id.txt_battery_health)
        pluggedType = view.findViewById(R.id.txt_plugged_type)

        pluggedTypeIcon = view.findViewById(R.id.ic_plugged_type)
        stateIcon = view.findViewById(R.id.ic_state)
        temperatureIcon = view.findViewById(R.id.ic_temperature)
        wattageIcon = view.findViewById(R.id.ic_wattage)

        //CPU Usage
        val cpuHandler = Handler(Looper.getMainLooper())
        cpuHandler.post(object : Runnable {
            override fun run() {
                if (isAdded) {
                    cpuInfoUpdate(requireContext())
                }
                cpuHandler.postDelayed(this, 5000)
            }
        })

        //Battery Ampere Info
        val batteryAmpHandler = Handler(Looper.getMainLooper())
        batteryAmpHandler.post(object : Runnable {
            override fun run() {
                if (isAdded) {
                    batteryAmpereUpdate(requireContext())
                }
                batteryAmpHandler.postDelayed(this, 2000)
            }
        })

        //Battery Info
        val batteryHandler = Handler(Looper.getMainLooper())
        batteryHandler.post(object : Runnable {
            override fun run() {
                if (isAdded) {
                    batteryInfoUpdate(requireContext())
                }
                batteryHandler.postDelayed(this, 10000)
            }
        })
    }

    fun cpuInfoUpdate(context: Context) {
        val cpuUsage = CpuInfo.getCpuUsageFromFreq()
        cpuMeter!!.setProgress(cpuUsage.toFloat())

        if (cpuUsage == 100) {
            cpuMeter!!.setFinishedStrokeColor(ContextCompat.getColor(context, R.color.red))
        } else {
            cpuMeter!!.setFinishedStrokeColor(ContextCompat.getColor(context, R.color.white))
        }
    }

    fun batteryInfoUpdate(context: Context) {
        //Battery Meter
        val level = BatteryInfo.getBatteryLevel(context)
        if (level <= 20) {
            batteryMeter!!.setFinishedStrokeColor(ContextCompat.getColor(context, R.color.red))
        } else {
            batteryMeter!!.setFinishedStrokeColor(
                ContextCompat.getColor(
                    context,
                    R.color.white
                )
            )
        }
        batteryMeter!!.setProgress(level.toFloat())
    }

    fun batteryAmpereUpdate(context: Context) {
        val current = BatteryInfo.getCurrentNow(context).toString() + " mA"

        //Current
        batteryCurrent!!.text = current
    }


    val status:
            Unit
        get() {
            val mainActivity = activity as MainActivity
            val context = requireContext()

            //val level = BatteryInfo.getBatteryLevel(context)
            val health = BatteryInfo.getHealth(context)
            val status = BatteryInfo.getStatus(context)
            val temperature = BatteryInfo.getTemperature(context)
            val plugged = BatteryInfo.getPlugged(context)
            val voltage = BatteryInfo.getVoltage(context)


            //Last time
            lastTime!!.text = BatteryInfo.getLastTime(context)
            val t: Thread = object : Thread() {
                override fun run() {
                    sleep(1000)
                    activity?.runOnUiThread {
                        lastTime!!.text = BatteryInfo.getLastTime(context)
                    }
                }
            }
            t.start()


            //Voltage
            batteryVoltage!!.text = BatteryInfo.getVoltageString(context)

            //Technology
            batteryTechnology!!.text = BatteryInfo.getTechnology(context)

            //Health
            batteryHealth!!.text = BatteryInfo.getHealthString(context)

            //Temperature
            batteryTemperature!!.text = BatteryInfo.getTemperatureString(context, false)
            batteryTemperatureF!!.text = BatteryInfo.getTemperatureString(context, true)

            //Capacity
            batteryCapacity!!.text = BatteryInfo.getBatteryDesignCapacityString(context)

            //Status
            batteryStatus!!.text = BatteryInfo.getStatusString(context)

            //Plugged
            pluggedType!!.text = BatteryInfo.getPluggedString(context)


            //battery state icon
            if (health != 4 || health != 5 || health != 6) {
                when (status) {
                    1 -> {
                        stateIcon?.setColorFilter(
                            ContextCompat.getColor(
                                context,
                                R.color.battery_unknown
                            )
                        )
                    }

                    2 -> {
                        stateIcon?.setColorFilter(
                            ContextCompat.getColor(
                                context,
                                R.color.battery_charge
                            )
                        )
                    }

                    3 -> {
                        stateIcon?.setColorFilter(
                            ContextCompat.getColor(
                                context,
                                R.color.battery_discharge
                            )
                        )
                    }
                }
            } else {
                stateIcon?.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.battery_warning
                    )
                )
            }


            //plugged icon
            when (plugged) {
                0 -> {
                    when (mainActivity.prefsConfig?.getInt("last_plugged", 1)) {
                        1 -> pluggedTypeIcon?.setImageResource(R.drawable.ic_ac_unplugged_24)
                        2 -> pluggedTypeIcon?.setImageResource(R.drawable.ic_usb_unplugged_24)
                        3 -> pluggedTypeIcon?.setImageResource(R.drawable.ic_ac_unplugged_24)
                    }

                    pluggedTypeIcon?.alpha = 0.66f
                    pluggedTypeIcon?.setColorFilter(
                        ContextCompat.getColor(
                            context,
                            R.color.battery_off
                        )
                    )
                }
                1 -> {
                    pluggedTypeIcon?.setImageResource(R.drawable.ic_ac_plugged_24)
                    pluggedTypeIcon?.alpha = 1.0f
                    pluggedTypeIcon?.setColorFilter(
                        ContextCompat.getColor(
                            context,
                            R.color.battery_charge
                        )
                    )
                    mainActivity.prefsConfig?.setInt("last_plugged", 1)
                }
                2 -> {
                    pluggedTypeIcon?.setImageResource(R.drawable.ic_usb_plugged_24)
                    pluggedTypeIcon?.alpha = 1.0f
                    pluggedTypeIcon?.setColorFilter(
                        ContextCompat.getColor(
                            context,
                            R.color.battery_charge
                        )
                    )
                    mainActivity.prefsConfig?.setInt("last_plugged", 2)
                }
                3 -> {
                    pluggedTypeIcon?.setImageResource(R.drawable.ic_wireless_charging_24)
                    pluggedTypeIcon?.alpha = 1.0f
                    pluggedTypeIcon?.setColorFilter(
                        ContextCompat.getColor(
                            context,
                            R.color.battery_charge
                        )
                    )
                    mainActivity.prefsConfig?.setInt("last_plugged", 3)
                }
            }

            //Toast.makeText(context, temperature.toString(), Toast.LENGTH_LONG).show()

            //temp icon
            if (temperature <= 1900) {
                temperatureIcon?.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.battery_temp_cold
                    )
                )
            }
            if (temperature >= 2000) {
                temperatureIcon?.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.battery_temp_normal
                    )
                )
            }
            if (temperature >= 3000) {
                temperatureIcon?.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.battery_temp_warm
                    )
                )
            }
            if (temperature >= 4000) {
                temperatureIcon?.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.battery_temp_hot
                    )
                )
            }
            if (temperature >= 5000) {
                temperatureIcon?.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.battery_temp_very_hot
                    )
                )
            }


            //wattage icon
            if (voltage.toInt() <= 6) {
                wattageIcon?.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.battery_power
                    )
                )
            }

            if (voltage.toInt() >= 7) {
                wattageIcon?.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.battery_alert
                    )
                )
            }

        }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}