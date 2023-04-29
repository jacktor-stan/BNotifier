package com.jacktorscript.batterynotifier.core

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager


class Prefs(context: Context) {
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()


    fun setInt(key: String?, value: Int) {
        editor.putInt(key, value)
        editor.apply()
    }

    fun setLong(key: String?, value: Long) {
        editor.putLong(key, value)
        editor.apply()
    }

    fun setString(key: String?, value: String?) {
        editor.putString(key, value)
        editor.apply()
    }

    fun setBoolean(key: String?, value: Boolean) {
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String?, def: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, def)
    }

    fun getInt(key: String?, def: Int): Int {
        return sharedPreferences.getInt(key, def)
    }

    fun getString(key: String?, def: String?): String? {
        return sharedPreferences.getString(key, def)
    }


    fun changeListener(prefChange: SharedPreferences.OnSharedPreferenceChangeListener) {
        return sharedPreferences.registerOnSharedPreferenceChangeListener(prefChange)
    }

    fun preferenceManager(): SharedPreferences {
        return sharedPreferences
    }
}