package com.randomimage.util

import android.content.Context
import timber.log.Timber
import java.io.File

data class CacheStats(
    val totalSize: Long,
    val imageSize: Long,
    val metadataSize: Long,
    val imageCount: Int
)

object CacheManager {
    fun getCacheStats(context: Context): CacheStats {
        var totalSize = 0L
        var imageSize = 0L
        var metadataSize = 0L
        var imageCount = 0

        // Coil disk cache
        val coilCacheDir = File(context.cacheDir, "image_cache")
        if (coilCacheDir.exists()) {
            coilCacheDir.walkTopDown()
                .filter { it.isFile && it.name != "journal" }
                .forEach { file ->
                    val size = file.length()
                    totalSize += size
                    if (file.name.endsWith(".1") && size > 1000) {
                        imageSize += size
                        imageCount++
                    } else {
                        metadataSize += size
                    }
                }
        }

        // Custom API images
        val customDir = File(context.cacheDir, "custom_api_images")
        if (customDir.exists()) {
            customDir.walkTopDown()
                .filter { it.isFile }
                .forEach { file ->
                    val size = file.length()
                    totalSize += size
                    imageSize += size
                    imageCount++
                }
        }

        return CacheStats(totalSize, imageSize, metadataSize, imageCount)
    }

    fun getCacheSize(context: Context): Long {
        return getCacheStats(context).totalSize
    }

    fun clearDiskCache(context: Context) {
        val cacheDir = File(context.cacheDir, "image_cache")
        if (cacheDir.exists()) {
            cacheDir.deleteRecursively()
            Timber.d("Disk cache cleared")
        }
        val customDir = File(context.cacheDir, "custom_api_images")
        if (customDir.exists()) {
            customDir.deleteRecursively()
            Timber.d("Custom API images cleared")
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
