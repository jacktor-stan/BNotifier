package com.jacktorscript.batterynotifier.core

import android.content.Context
import android.content.SharedPreferences


class PrefsConfig(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("config", 0)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()


    fun setInt(key: String?, value: Int) {
        editor.putInt(key, value)
        editor.apply()
    }

    fun setLong(key: String?, value: Long) {
        editor.putLong(key, value)
        editor.apply()
    }

    //fun setString(key: String?, value: String?) {
    //    editor.putString(key, value)
    //    editor.apply()
    //}

    fun setBoolean(key: String?, value: Boolean) {
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun setPremium(value: Int) {
        editor.putInt("premium", value)
        editor.apply()
    }

    fun getBoolean(key: String?, def: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, def)
    }

    fun getInt(key: String?, def: Int): Int {
        return sharedPreferences.getInt(key, def)
    }

    fun getLong(key: String?, def: Long): Long {
        return sharedPreferences.getLong(key, def)
    }

    //fun getString(key: String?, def: String?): String? {
     //   return sharedPreferences.getString(key, def)
    //}

    fun getPremium(): Int {
        return sharedPreferences.getInt("premium", 0)
    }

}