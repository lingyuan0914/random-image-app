package com.randomimage

import android.app.Application
import coil.ImageLoader
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

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        LogManager.init(this)
        ImageUtils.init(imageLoader)
        DownloadManager.init(imageLoader)
        Timber.d("Application started")
    }
}
