package com.cloq.loonki

import android.content.Context
import android.content.SharedPreferences

class MySharedPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("pref_name", 0)

    fun getPref(key: String, defValue: String): String {
        return prefs.getString(key, defValue).toString()
    }

    fun setPref(key: String, str: String) {
        prefs.edit().putString(key, str).apply()
    }
}