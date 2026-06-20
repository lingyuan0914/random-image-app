package com.randomimage.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import java.io.File
import java.io.FileOutputStream

object ShareUtils {

    private var sharedImageLoader: ImageLoader? = null

    fun init(imageLoader: ImageLoader) {
        sharedImageLoader = imageLoader
    }

    private fun getLoader(context: Context): ImageLoader {
        return sharedImageLoader ?: ImageLoader(context)
    }

    suspend fun shareImage(context: Context, imageUrl: String, appName: String? = null) {
        try {
            val loader = getLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()

            val result = loader.execute(request)
            if (result is SuccessResult) {
                val bitmap = (result.drawable as BitmapDrawable).bitmap
                val uri = saveBitmapToCache(context, bitmap)

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooserIntent = Intent.createChooser(shareIntent, appName ?: "分享图片")
                context.startActivity(chooserIntent)
            }
        } catch (e: Exception) {
            // 分享失败
        }
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
        val cacheDir = File(context.cacheDir, "shared_images")
        cacheDir.mkdirs()

        val file = File(cacheDir, "share_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    suspend fun shareImageToWechat(context: Context, imageUrl: String) {
        try {
            val loader = getLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()

            val result = loader.execute(request)
            if (result is SuccessResult) {
                val bitmap = (result.drawable as BitmapDrawable).bitmap
                val uri = saveBitmapToCache(context, bitmap)

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    setPackage("com.tencent.mm")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            // 微信未安装或分享失败
        }
    }

    suspend fun shareImageToQQ(context: Context, imageUrl: String) {
        try {
            val loader = getLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()

            val result = loader.execute(request)
            if (result is SuccessResult) {
                val bitmap = (result.drawable as BitmapDrawable).bitmap
                val uri = saveBitmapToCache(context, bitmap)

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    setPackage("com.tencent.mobileqq")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            // QQ未安装或分享失败
        }
    }

    fun shareToWechat(context: Context, imageUrl: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.tencent.mm")
            putExtra(Intent.EXTRA_TEXT, imageUrl)
        }
        context.startActivity(intent)
    }

    fun shareToQQ(context: Context, imageUrl: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.tencent.mobileqq")
            putExtra(Intent.EXTRA_TEXT, imageUrl)
        }
        context.startActivity(intent)
    }
}
