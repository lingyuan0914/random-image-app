package com.randomimage

import android.app.Application
import android.util.Log
import coil.ImageLoader
import com.randomimage.data.local.AppDataStore
import com.randomimage.data.remote.ApiManager
import com.randomimage.util.CloudSyncManager
import com.randomimage.util.DownloadManager
import com.randomimage.util.ImageUtils
import com.randomimage.util.LogManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class RandomImageApp : Application() {

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var apiManager: ApiManager

    @Inject
    lateinit var dataStore: AppDataStore

    override fun onCreate() {
        super.onCreate()
        LogManager.init(this)
        Timber.plant(Timber.DebugTree())
        Timber.plant(LogManagerTree())
        ImageUtils.init(imageLoader)
        DownloadManager.init(imageLoader)
        apiManager.init(this)
        CloudSyncManager.init(dataStore)
        Timber.d("Application started")
    }

    private class LogManagerTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            val level = when (priority) {
                Log.VERBOSE -> "VERBOSE"
                Log.DEBUG -> "DEBUG"
                Log.INFO -> "INFO"
                Log.WARN -> "WARN"
                Log.ERROR -> "ERROR"
                Log.ASSERT -> "ASSERT"
                else -> "UNKNOWN"
            }
            LogManager.addLog(level, tag ?: "App", message)
        }
    }
}
