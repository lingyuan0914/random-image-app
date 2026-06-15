package com.randomimage.util

import android.content.Context
import timber.log.Timber
import java.io.File

object CacheManager {
    fun getCacheSize(context: Context): Long {
        var size = 0L
        val diskCacheDir = File(context.cacheDir, "image_cache")
        if (diskCacheDir.exists()) {
            size += diskCacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        }
        return size
    }

    fun clearDiskCache(context: Context) {
        val diskCacheDir = File(context.cacheDir, "image_cache")
        if (diskCacheDir.exists()) {
            diskCacheDir.deleteRecursively()
            Timber.d("Disk cache cleared")
        }
    }

    fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${"%.2f".format(bytes / (1024.0 * 1024))} MB"
        }
    }
}
