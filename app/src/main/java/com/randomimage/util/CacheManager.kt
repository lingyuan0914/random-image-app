package com.randomimage.util

import android.content.Context
import timber.log.Timber
import java.io.File

data class CacheInfo(
    val totalSize: Long,
    val imageSize: Long,
    val metaDataSize: Long
)

object CacheManager {
    fun getCacheInfo(context: Context): CacheInfo {
        val diskCacheDir = File(context.cacheDir, "image_cache")
        if (!diskCacheDir.exists()) return CacheInfo(0, 0, 0)

        var totalSize = 0L
        var imageSize = 0L
        var metaDataSize = 0L

        diskCacheDir.walkTopDown().filter { it.isFile }.forEach { file ->
            val size = file.length()
            totalSize += size
            when {
                file.name == "journal" -> metaDataSize += size
                file.name.endsWith(".0") -> metaDataSize += size
                file.name.endsWith(".1") && size > 1024 -> imageSize += size
                else -> metaDataSize += size
            }
        }

        return CacheInfo(totalSize, imageSize, metaDataSize)
    }

    fun getCacheSize(context: Context): Long {
        return getCacheInfo(context).totalSize
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
            else -> "${"%.1f".format(bytes / (1024.0 * 1024))} MB"
        }
    }
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
    }

    fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${"%.2f".format(bytes / (1024.0 * 1024))} MB"
        }
    }
}
