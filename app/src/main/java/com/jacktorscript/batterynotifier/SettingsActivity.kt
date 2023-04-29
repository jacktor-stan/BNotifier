package com.jacktorscript.batterynotifier

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.jacktorscript.batterynotifier.core.FileUtils
import com.jacktorscript.batterynotifier.core.Prefs
import com.jacktorscript.batterynotifier.core.PrefsConfig
import com.jacktorscript.batterynotifier.ui.SettingsFragment
import java.io.DataOutputStream
import java.util.concurrent.Executors


class SettingsActivity : AppCompatActivity() {
    private var prefs: Prefs? = null
    private var prefsConfig: PrefsConfig? = null


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            //jika user klik izinkan penyimpanan/audio - pilih file
            showFileChooser()
        } else {
            //jika user klik tolak izin penyimpanan/audio
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                storageBlockedSB(R.string.storage_permission_not_granted_SB)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                storageBlockedSB(R.string.audio_permission_not_granted_SB)
            }
        }
    }

    private val selectFileActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val filePath = FileUtils.getPath(this, data?.data!!)!!.toUri()
            val fileName = FileUtils.getFileName(filePath)

            //Ac Sound Pref
            val acKey = "ac_connected_sound"
            val acTag = "ac_"
            if (prefs!!.getString("fileinfo", "") == acKey) {
                FileUtils.saveFile(this, filePath, acTag + fileName)

                // jangan hapus file jika nama file yang dipilih sama dengan sebelumnya
                if (prefs!!.getString(acKey, "") != acTag + fileName) {
                    FileUtils.removeFile(this, false, prefs!!.getString(acKey, "")!!)
                }

                prefs!!.setString(acKey, acTag + fileName)
            }

            //USB Sound Pref
            val usbKey = "usb_connected_sound"
            val usbTag = "usb_"
            if (prefs!!.getString("fileinfo", "") == usbKey) {
                FileUtils.saveFile(this, filePath, usbTag + fileName)

                // jangan hapus file jika nama file yang dipilih sama dengan sebelumnya
                if (prefs!!.getString(usbKey, "") != usbTag + fileName) {
                    FileUtils.removeFile(this, false, prefs!!.getString(usbKey, "")!!)
                }

                prefs!!.setString(usbKey, usbTag + fileName)

            }

            //Disconnected Sound Pref
            val dcKey = "disconnected_sound"
            val dcTag = "dc_"
            if (prefs!!.getString("fileinfo", "") == dcKey) {
                FileUtils.saveFile(this, filePath, dcTag + fileName)

                // jangan hapus file jika nama file yang dipilih sama dengan sebelumnya
                if (prefs!!.getString(dcKey, "") != dcTag + fileName) {
                    FileUtils.removeFile(this, false, prefs!!.getString(dcKey, "")!!)
                }

                prefs!!.setString(dcKey, dcTag + fileName)
            }

            Toast.makeText(this, R.string.changes_saved, Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //Init
        prefs = Prefs(this)
        prefsConfig = PrefsConfig(this)

        // Atur warna status bar untuk android lollipop
        if (Build.VERSION.SDK_INT <= 22) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        val topAppBar = findViewById<View>(R.id.topAppBar) as MaterialToolbar
        setSupportActionBar(topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Tombol back
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            }
        })

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()


        //terapkan perubahan tema dalam perubahan changeListener
        prefs!!.changeListener { prefs: SharedPreferences, _: String? ->
            when (prefs.getString("theme", "system")) {
                "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }




    /* MENU */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_settings, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.restore_pref -> {
                restoreSettings()
            }

            R.id.refresh_pref -> {
                recreate()
                Toast.makeText(this, getString(R.string.refresh_toast), Toast.LENGTH_SHORT).show()
            }

        }
        return false
    }

    private fun restoreSettings() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.restore_settings_dialog_title))
            .setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(getString(R.string.yes_continue)) { _, _ ->
                val preferences: SharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this)
                val editor = preferences.edit()
                editor.clear()
                editor.apply()
                recreate()
                Toast.makeText(this, getString(R.string.restore_settings_toast), Toast.LENGTH_SHORT)
                    .show()
            }
            .show()
    }


    fun askPermissionAndBrowseFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_MEDIA_AUDIO
                ) == PackageManager.PERMISSION_GRANTED -> {
                    //jika izin audio tersedia
                    showFileChooser()
                }

                shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_AUDIO) -> {
                    storageBlockedSB(R.string.audio_permission_not_granted_SB)
                }

                else -> {
                    // The registered ActivityResultCallback gets the result of this request
                    requestPermissionLauncher.launch(
                        Manifest.permission.READ_MEDIA_AUDIO
                    )
                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                when {
                    ContextCompat.checkSelfPermission(
                        this, Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        //jika izin peyimpanan tersedia
                        showFileChooser()
                    }

                    shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                        storageBlockedSB(R.string.storage_permission_not_granted_SB)
                    }

                    else -> {
                        // The registered ActivityResultCallback gets the result of this request
                        requestPermissionLauncher.launch(
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    }
                }
            } else {
                //Lewati izin untuk Android Lollipop
                showFileChooser()
            }
        }

    }

    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
        }
        selectFileActivityResult.launch(intent)
    }


    private fun storageBlockedSB(msg: Int) {
        Snackbar.make(
            findViewById(R.id.settings),
            getString(msg),
            Snackbar.LENGTH_LONG
        ).setAction(R.string.settings) {
            // Responds to click on the action
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
            .setDuration(5000)
            .show()
    }


    //SuperUser permission
    fun obtainPermission() {
        Executors.newCachedThreadPool().submit {
            val result: Int
            when (executeSuCommand(
                getString(
                    R.string.format_command,
                    packageName,
                    PERMISSION_BATTERY_STATS
                )
            )) {
                0 -> {
                    val hasPermission = hasPermission(this)
                    result =
                        if (hasPermission) RESULT_SUCCESS else RESULT_FAIL_PERMISSION
                }

                1 -> {
                    result = RESULT_FAIL_SU
                }

                255 -> {
                    result = RESULT_FAIL_PERMISSION
                }

                else -> {
                    result = RESULT_FAIL_UNKNOWN
                }
            }
            runOnUiThread {
                if (result == RESULT_SUCCESS) {
                    prefs!!.setBoolean("time_remaining", true)
                } else {
                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.dialog_time_remaining_title))
                        .setMessage(getString(R.string.dialog_time_remaining_content))
                        .setCancelable(false)
                        .setNeutralButton(getString(R.string.dialog_adb_title)) { _, _ ->

                            MaterialAlertDialogBuilder(this)
                                .setTitle(getString(R.string.dialog_adb_title))
                                .setMessage(
                                    """
                                                ${getString(R.string.dialog_adb_content)}
                                                
                                                
                                                """.trimIndent() +
                                            getString(
                                                R.string.format_command,
                                                packageName,
                                                PERMISSION_BATTERY_STATS
                                            )
                                )
                                .setCancelable(false)
                                .setNegativeButton(getString(R.string.dialog_button_tryagain)) { _, _ ->
                                    obtainPermission()
                                }
                                .setPositiveButton(getString(R.string.dialog_button_close)) { dialog2, _ ->
                                    dialog2.cancel()
                                    recreate()
                                }
                                .show()

                        }
                        .setNegativeButton(getString(R.string.dialog_button_tryagain)) { _, _ ->
                            obtainPermission()
                        }
                        .setPositiveButton(getString(R.string.dialog_button_close)) { dialog, _ ->
                            dialog.cancel()
                            recreate()
                        }
                        .show()


                    prefs!!.setBoolean("time_remaining", false)
                }
            }
        }
    }

    //System permission
    private fun hasPermission(context: Context): Boolean {
        return context.packageManager
            .checkPermission(
                PERMISSION_BATTERY_STATS,
                context.packageName
            ) == PackageManager.PERMISSION_GRANTED
    }


    //SU Command
    private fun executeSuCommand(command: String): Int {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes(command + "\n")
            os.writeBytes("exit\n")
            os.flush()
            os.close()
            process.waitFor()
            process.exitValue()
        } catch (e: Exception) {
            -1
        } finally {
            process?.destroy()
        }
    }


    companion object {
        private const val RESULT_SUCCESS = 1
        private const val RESULT_FAIL_SU = 2
        private const val RESULT_FAIL_PERMISSION = 3
        private const val RESULT_FAIL_UNKNOWN = 4
        private const val PERMISSION_BATTERY_STATS = "android.permission.BATTERY_STATS"
    }


}


