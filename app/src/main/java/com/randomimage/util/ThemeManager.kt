package com.randomimage.util

import android.content.Context
import android.content.SharedPreferences

object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_PREDICTIVE_BACK = "predictive_back"

    const val THEME_LIGHT = 0
    const val THEME_DARK = 1
    const val THEME_SYSTEM = 2

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getThemeMode(context: Context): Int {
        return getPrefs(context).getInt(KEY_THEME_MODE, THEME_SYSTEM)
    }

    fun setThemeMode(context: Context, mode: Int) {
        getPrefs(context).edit().putInt(KEY_THEME_MODE, mode).apply()
    }

    fun getPredictiveBack(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_PREDICTIVE_BACK, true)
    }

    fun setPredictiveBack(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_PREDICTIVE_BACK, enabled).apply()
    }
}
