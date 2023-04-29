package com.jacktorscript.batterynotifier.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jacktorscript.batterynotifier.R
import com.jacktorscript.batterynotifier.SettingsActivity
import com.jacktorscript.batterynotifier.core.FileUtils
import com.jacktorscript.batterynotifier.core.Prefs
import com.jacktorscript.batterynotifier.core.PrefsConfig
import com.jacktorscript.batterynotifier.notification.NotificationService
import com.scottyab.rootbeer.RootBeer




class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        val context = preferenceManager.context
        val getActivity = activity as SettingsActivity

        //Preferences
        val prefs = Prefs(context)
        val prefsConfig = PrefsConfig(context)

        //Material 3 untuk Preferences
        //val pref: SwitchPreference? =
        //    findPreference<Preference>("notification_service") as SwitchPreference?
        //pref!!.widgetLayoutResource = R.layout.preferences_widget_switch


        /* Fitur PRO */
        //Theme - PRO
        if (prefsConfig.getPremium() == 1) {
            findPreference<Preference>("theme")!!.isEnabled = true
        }


        //Change Listener
        prefs.changeListener { prefs1: SharedPreferences, _: String? ->
            //set preferences
            setPreferences(this, prefs, prefsConfig)

            //set summary
            if (isAdded) {
                setSummary(requireContext(), this, prefs1)
            }
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    if (isAdded) {
                        setSummary(requireContext(), this, prefs1)
                    }
                },
                1000
            )
        }

        //set preferences
        setPreferences(this, prefs, prefsConfig)
        //set summary
        if (isAdded) {
            setSummary(requireContext(), this, prefs.preferenceManager())
        }


        //Tampilkan ini di tombol layanan notifikasi jika diatas android 8
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notification = findPreference<Preference>("notification_service")
            //notification?.isEnabled = false
            notification?.setSummary(R.string.pref_notification_service_smry2)
        } else {
            findPreference<Preference>("show_notification")?.setSummary(R.string.pref_notification_service_smry)
        }


        //Time Remaining (ClickListener)
        val timeRemaining = findPreference<Preference>("time_remaining")
        timeRemaining?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                getActivity.obtainPermission()
                true
            }

        //Ac connected sound (ClickListener)
        val acConnectedSound = findPreference<Preference>("ac_connected_sound")
        acConnectedSound?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val type = "ac_connected_sound"
                selectSoundDialog(context, getActivity, type)
                true
            }

        //USB connected sound (ClickListener)
        val usbConnectedSound = findPreference<Preference>("usb_connected_sound")
        usbConnectedSound?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val type = "usb_connected_sound"
                selectSoundDialog(context, getActivity, type)
                true
            }

        //Disconnected sound (ClickListener)
        val disconnectedSound = findPreference<Preference>("disconnected_sound")
        disconnectedSound?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val type = "disconnected_sound"
                selectSoundDialog(context, getActivity, type)
                true
            }

        //Manage Notification (ClickListener)
        findPreference<Preference>("manage_notification")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val intent: Intent
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                    intent.putExtra(
                        Settings.EXTRA_CHANNEL_ID,
                        NotificationService.CHANNEL_ID
                    )
                    intent.putExtra(
                        Settings.EXTRA_APP_PACKAGE,
                        getActivity.packageName
                    )
                    startActivity(intent)
                } else {
                    val intent1 = Intent()
                    intent1.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    intent1.putExtra("app_package", getActivity.packageName)
                    intent1.putExtra("app_uid", getActivity.applicationInfo.uid)
                    startActivity(intent1)
                }
                true
            }
    }


    //companion object {
//Set Preferences
    private fun setPreferences(
        fm: PreferenceFragmentCompat,
        prefs: Prefs,
        prefsConfig: PrefsConfig
    ) {


        //Time Remaining
        //val rootBeer = RootBeer(context)
        //fm.findPreference<Preference>("time_remaining")!!.isEnabled = rootBeer.isRooted

        //atur vibrate_duration dari custom_vibrate_duration
        if (prefs.getString("vibrate_duration", "450") != "0") {
            if (prefsConfig.getPremium() == 1) {
                fm.findPreference<Preference>("custom_vibrate_duration")!!.isEnabled = false
            } else {
                prefs.setString("vibrate_duration", "450")
            }
            prefs.setString(
                "custom_vibrate_duration",
                prefs.getString("vibrate_duration", "450")
            )
        } else {
            if (prefsConfig.getPremium() == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ) {
                    if (prefs.getBoolean(
                            "notification_service",
                            true
                        )
                    ) {
                        fm.findPreference<Preference>("custom_vibrate_duration")!!.isEnabled =
                            true
                    }
                } else {
                    fm.findPreference<Preference>("custom_vibrate_duration")!!.isEnabled = true
                }
            }
        }

        if (prefs.getBoolean("notification_service", true)) {
            fm.findPreference<Preference>("fahrenheit")?.isEnabled = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                fm.findPreference<Preference>("ac_connected_sound")?.isEnabled = true
                fm.findPreference<Preference>("usb_connected_sound")?.isEnabled = true
                fm.findPreference<Preference>("disconnected_sound")?.isEnabled = true
                fm.findPreference<Preference>("sound_delay")?.isEnabled = true
                fm.findPreference<Preference>("enable_vibration")?.isEnabled = true
                fm.findPreference<Preference>("vibrate_duration")?.isEnabled = true
                fm.findPreference<Preference>("vibrate_mode")?.isEnabled = true
                fm.findPreference<Preference>("enable_toast")?.isEnabled = true
                fm.findPreference<Preference>("legacy_statusbar_icon")?.isEnabled = true
            }
        } else {
            fm.findPreference<Preference>("fahrenheit")?.isEnabled = false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                fm.findPreference<Preference>("ac_connected_sound")?.isEnabled = false
                fm.findPreference<Preference>("usb_connected_sound")?.isEnabled = false
                fm.findPreference<Preference>("disconnected_sound")?.isEnabled = false
                fm.findPreference<Preference>("sound_delay")?.isEnabled = false
                fm.findPreference<Preference>("enable_vibration")?.isEnabled = false
                fm.findPreference<Preference>("vibrate_duration")?.isEnabled = false
                fm.findPreference<Preference>("vibrate_mode")?.isEnabled = false
                fm.findPreference<Preference>("custom_vibrate_duration")?.isEnabled = false
                fm.findPreference<Preference>("enable_toast")?.isEnabled = false
            }
        }

    }


    //Dialog pilih suara - dari ClickListener
    private fun selectSoundDialog(context: Context, activity: SettingsActivity, type: String) {


        val items = arrayOf<CharSequence>(
            context.getString(R.string.select_file), context.getString(R.string.reset)
        )
        val dialog = MaterialAlertDialogBuilder(
            context
        )
        dialog.setItems(items) { _: DialogInterface?, item: Int ->
            when (item) {
                0 -> selectSound(context, activity, type)
                1 -> resetSound(context, type)
            }
        }
        val alert = dialog.create()
        alert.show()
    }

    private fun selectSound(context: Context, activity: SettingsActivity, type: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(
            context
        )
        val editor = prefs.edit()
        editor.putString("fileinfo", type)
        editor.apply()

        //Minta izin penyimpanan dan pilih file
        activity.askPermissionAndBrowseFile()
    }


    //Reset suara - dari dialog pilih suara
    private fun resetSound(context: Context, type: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(
            context
        )

        val filename = prefs.getString(type, "")
        if (filename != "") {
            FileUtils.removeFile(context, true, filename!!)
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.audio_has_been_reset),
                Toast.LENGTH_SHORT
            ).show()
        }

        val editor = prefs.edit()
        editor.putString(type, "")
        editor.apply()
    }


    private fun setSummary(
        context: Context,
        fm: PreferenceFragmentCompat,
        prefs: SharedPreferences
    ) {
        //Theme summary
        val key = "theme"
        val theme = fm.findPreference<Preference>(key)
        val themePref = prefs.getString(key, "light")

        if (themePref == "system") {
            theme?.summary = context.getString(R.string.theme_follow_by_system)
        }
        if (themePref == "dark") {
            theme?.summary = context.getString(R.string.theme_dark)
        }
        if (themePref == "light") {
            theme?.summary = context.getString(R.string.theme_light)
        }

        //Ac connected sound summary
        val key1 = "ac_connected_sound"
        val file = prefs.getString(key1, "")
        val acConnectedSound = fm.findPreference<Preference>(key1)

        if (file != "") {
            acConnectedSound?.summary = file?.replace("ac_", "")
        } else {
            acConnectedSound?.setSummary(R.string.audio_not_selected)
        }

        //USB connected sound summary
        val key2 = "usb_connected_sound"
        val file2 = prefs.getString(key2, "")
        val usbConnectedSound = fm.findPreference<Preference>(key2)

        if (file2 != "") {
            usbConnectedSound?.summary = file2?.replace("usb_", "")
        } else {
            usbConnectedSound?.setSummary(R.string.audio_not_selected)
        }

        //Disconnected sound summary
        val key3 = "disconnected_sound"
        val file3 = prefs.getString(key3, "")
        val disconnectedSound = fm.findPreference<Preference>(key3)

        if (file3 != "") {
            disconnectedSound?.summary = file3?.replace("dc_", "")
        } else {
            disconnectedSound?.setSummary(R.string.audio_not_selected)
        }

        //Sound delay summary
        val key4 = "sound_delay"
        val soundDelay = fm.findPreference<Preference>(key4)
        soundDelay?.summary = prefs.getString(key4, "550")

        //Vibrate duration
        val key5 = "vibrate_duration"
        val vibrateDuration = fm.findPreference<Preference>(key5)

        when (prefs.getString(key5, "450")) {
            "150" -> {
                vibrateDuration?.summary = context.getString(R.string.pref_vibration_short)
            }
            "250" -> {
                vibrateDuration?.summary = context.getString(R.string.pref_vibration_medium)
            }
            "450" -> {
                vibrateDuration?.summary = context.getString(R.string.pref_vibration_long)
            }
            "0" -> {
                vibrateDuration?.summary = context.getString(R.string.pref_vibration_custom)
                //fm.findPreference<Preference>("custom_vibrate_duration")!!.isEnabled = true
            }
        }

        //Custom vibrate duration
        val key6 = "custom_vibrate_duration"
        val customVibDuration = fm.findPreference<Preference>(key6)
        val customVibDurPref = prefs.getString(key6, "450")
        customVibDuration?.summary = customVibDurPref

        //Vibrate mode
        val key7 = "vibrate_mode"
        val vibrateMode = fm.findPreference<Preference>(key7)
        val vibrateModePref = prefs.getString(key7, "connected")

        if (vibrateModePref == "both") {
            vibrateMode?.summary = context.getString(R.string.pref_both)
        }
        if (vibrateModePref == "connected") {
            vibrateMode?.summary = context.getString(R.string.pref_connected)
        }
        if (vibrateModePref == "disconnected") {
            vibrateMode?.summary = context.getString(R.string.pref_disconnected)
        }

    }
}

