package com.randomimage.util

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.atomic.AtomicInteger

object StatsManager {
    private const val PREFS_NAME = "stats_prefs"
    private const val KEY_VIEW_COUNT = "view_count"
    private const val KEY_FAVORITE_COUNT = "favorite_count"
    private const val KEY_DOWNLOAD_COUNT = "download_count"
    private const val KEY_SEARCH_COUNT = "search_count"
    private const val KEY_FIRST_OPEN_TIME = "first_open_time"
    private const val KEY_LAST_OPEN_TIME = "last_open_time"

    private val viewCount = AtomicInteger(0)
    private val favoriteCount = AtomicInteger(0)
    private val downloadCount = AtomicInteger(0)
    private val searchCount = AtomicInteger(0)

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun init(context: Context) {
        val prefs = getPrefs(context)
        viewCount.set(prefs.getInt(KEY_VIEW_COUNT, 0))
        favoriteCount.set(prefs.getInt(KEY_FAVORITE_COUNT, 0))
        downloadCount.set(prefs.getInt(KEY_DOWNLOAD_COUNT, 0))
        searchCount.set(prefs.getInt(KEY_SEARCH_COUNT, 0))
    }

    fun updateFirstOpenTime(context: Context) {
        val prefs = getPrefs(context)
        if (prefs.getLong(KEY_FIRST_OPEN_TIME, 0) == 0L) {
            prefs.edit().putLong(KEY_FIRST_OPEN_TIME, System.currentTimeMillis()).apply()
        }
        prefs.edit().putLong(KEY_LAST_OPEN_TIME, System.currentTimeMillis()).apply()
    }

    fun incrementViewCount(context: Context) {
        val newCount = viewCount.incrementAndGet()
        getPrefs(context).edit().putInt(KEY_VIEW_COUNT, newCount).apply()
    }

    fun incrementFavoriteCount(context: Context) {
        val newCount = favoriteCount.incrementAndGet()
        getPrefs(context).edit().putInt(KEY_FAVORITE_COUNT, newCount).apply()
    }

    fun incrementDownloadCount(context: Context) {
        val newCount = downloadCount.incrementAndGet()
        getPrefs(context).edit().putInt(KEY_DOWNLOAD_COUNT, newCount).apply()
    }

    fun incrementSearchCount(context: Context) {
        val newCount = searchCount.incrementAndGet()
        getPrefs(context).edit().putInt(KEY_SEARCH_COUNT, newCount).apply()
    }

    fun getViewCount(): Int = viewCount.get()
    fun getFavoriteCount(): Int = favoriteCount.get()
    fun getDownloadCount(): Int = downloadCount.get()
    fun getSearchCount(): Int = searchCount.get()
    fun getFirstOpenTime(context: Context): Long = getPrefs(context).getLong(KEY_FIRST_OPEN_TIME, 0)

    fun getDaysSinceFirstOpen(context: Context): Int {
        val firstOpen = getPrefs(context).getLong(KEY_FIRST_OPEN_TIME, 0)
        if (firstOpen == 0L) return 0
        val diff = System.currentTimeMillis() - firstOpen
        return (diff / (24 * 60 * 60 * 1000)).toInt() + 1
    }
}
