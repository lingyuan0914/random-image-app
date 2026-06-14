package com.randomimage.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.app.WallpaperManager
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

object ImageUtils {
    private var imageLoader: ImageLoader? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun init(loader: ImageLoader) {
        imageLoader = loader
    }

    suspend fun downloadImage(context: Context, imageUrl: String): Boolean {
        return DownloadManager.downloadImage(context, imageUrl)
    }

    fun shareImage(context: Context, imageUrl: String) {
        scope.launch {
            ShareUtils.shareImage(context, imageUrl)
        }
    }

    fun shareToWechat(context: Context, imageUrl: String) {
        ShareUtils.shareToWechat(context, imageUrl)
    }

    fun shareToQQ(context: Context, imageUrl: String) {
        ShareUtils.shareToQQ(context, imageUrl)
    }

    suspend fun setWallpaper(context: Context, imageUrl: String): Boolean {
        return try {
            val loader = imageLoader ?: return false
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()

            val result = loader.execute(request)
            if (result is SuccessResult) {
                val bitmap = (result.drawable as BitmapDrawable).bitmap
                val wallpaperManager = WallpaperManager.getInstance(context)
                wallpaperManager.setBitmap(bitmap)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to set wallpaper")
            false
        }
    }

    suspend fun getBitmap(context: Context, imageUrl: String): Bitmap? {
        return try {
            val loader = imageLoader ?: return null
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()

            val result = loader.execute(request)
            if (result is SuccessResult) {
                (result.drawable as BitmapDrawable).bitmap
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get bitmap")
            null
        }
    }
}
