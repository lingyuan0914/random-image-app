package com.randomimage.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileOutputStream

object DownloadManager {

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState

    sealed class DownloadState {
        object Idle : DownloadState()
        object Downloading : DownloadState()
        data class Success(val filePath: String) : DownloadState()
        data class Error(val message: String) : DownloadState()
    }

    private var imageLoader: ImageLoader? = null

    fun init(loader: ImageLoader) {
        imageLoader = loader
    }

    suspend fun downloadImage(context: Context, imageUrl: String): Boolean {
        return try {
            _downloadState.value = DownloadState.Downloading

            val loader = imageLoader ?: return false
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()

            val result = loader.execute(request)
            if (result is SuccessResult) {
                val bitmap = (result.drawable as BitmapDrawable).bitmap
                val filePath = saveBitmapToGallery(context, bitmap)
                StatsManager.incrementDownloadCount(context)
                _downloadState.value = DownloadState.Success(filePath)
                true
            } else {
                _downloadState.value = DownloadState.Error("下载失败")
                false
            }
        } catch (e: Exception) {
            _downloadState.value = DownloadState.Error(e.message ?: "下载失败")
            false
        }
    }

    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap): String {
        val filename = "random_image_${System.currentTimeMillis()}.jpg"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }
                it.toString()
            } ?: ""
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            )
            val imageFile = File(imagesDir, filename)
            FileOutputStream(imageFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
            imageFile.absolutePath
        }
    }

    fun resetState() {
        _downloadState.value = DownloadState.Idle
    }
}
