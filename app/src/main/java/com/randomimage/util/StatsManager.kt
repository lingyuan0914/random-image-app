package com.randomimage.util

import android.content.Context
import android.content.SharedPreferences

object StatsManager {
    private const val PREFS_NAME = "stats_prefs"
    private const val KEY_VIEW_COUNT = "view_count"
    private const val KEY_FAVORITE_COUNT = "favorite_count"
    private const val KEY_DOWNLOAD_COUNT = "download_count"
    private const val KEY_SEARCH_COUNT = "search_count"
    private const val KEY_LAST_OPEN_TIME = "last_open_time"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun incrementViewCount(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit().putInt(KEY_VIEW_COUNT, prefs.getInt(KEY_VIEW_COUNT, 0) + 1).apply()
    }

    fun incrementFavoriteCount(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit().putInt(KEY_FAVORITE_COUNT, prefs.getInt(KEY_FAVORITE_COUNT, 0) + 1).apply()
    }

    fun incrementDownloadCount(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit().putInt(KEY_DOWNLOAD_COUNT, prefs.getInt(KEY_DOWNLOAD_COUNT, 0) + 1).apply()
    }

    fun incrementSearchCount(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit().putInt(KEY_SEARCH_COUNT, prefs.getInt(KEY_SEARCH_COUNT, 0) + 1).apply()
    }

    fun updateLastOpenTime(context: Context) {
        getPrefs(context).edit().putLong(KEY_LAST_OPEN_TIME, System.currentTimeMillis()).apply()
    }

    fun getViewCount(context: Context): Int = getPrefs(context).getInt(KEY_VIEW_COUNT, 0)
    fun getFavoriteCount(context: Context): Int = getPrefs(context).getInt(KEY_FAVORITE_COUNT, 0)
    fun getDownloadCount(context: Context): Int = getPrefs(context).getInt(KEY_DOWNLOAD_COUNT, 0)
    fun getSearchCount(context: Context): Int = getPrefs(context).getInt(KEY_SEARCH_COUNT, 0)
    fun getLastOpenTime(context: Context): Long = getPrefs(context).getLong(KEY_LAST_OPEN_TIME, 0)

    fun getDaysSinceFirstOpen(context: Context): Int {
        val lastOpen = getLastOpenTime(context)
        if (lastOpen == 0L) return 0
        val diff = System.currentTimeMillis() - lastOpen
        return (diff / (24 * 60 * 60 * 1000)).toInt() + 1
    }
}
