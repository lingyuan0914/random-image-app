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
import kotlinx.coroutines.launch

object ImageUtils {

    suspend fun downloadImage(context: Context, imageUrl: String): Boolean {
        return DownloadManager.downloadImage(context, imageUrl)
    }

    fun shareImage(context: Context, imageUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
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
            val loader = ImageLoader(context)
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
            false
        }
    }

    suspend fun getBitmap(context: Context, imageUrl: String): Bitmap? {
        return try {
            val loader = ImageLoader(context)
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
            null
        }
    }
}
