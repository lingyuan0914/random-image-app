package com.randomimage.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.randomimage.util.CacheManager
import com.randomimage.util.CacheStats
import timber.log.Timber
import java.io.File

data class CachedImage(
    val file: File,
    val name: String,
    val size: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CachePreviewScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var cachedImages by remember { mutableStateOf<List<CachedImage>>(emptyList()) }
    var cacheStats by remember { mutableStateOf(CacheStats(0, 0, 0, 0)) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf<CachedImage?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        cachedImages = loadCachedImages(context)
        cacheStats = CacheManager.getCacheStats(context)
    }

    BackHandler { onBack() }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("缓存预览") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                IconButton(onClick = { showClearAllDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "清除全部")
                }
            }
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "共 ${cacheStats.imageCount} 张图片",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "总: ${CacheManager.formatSize(cacheStats.totalSize)}  图片: ${CacheManager.formatSize(cacheStats.imageSize)}  元数据: ${CacheManager.formatSize(cacheStats.metadataSize)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (cachedImages.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无缓存图片",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(cachedImages) { cachedImage ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clickable {
                                selectedImage = cachedImage
                                showDeleteDialog = true
                            },
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        val bitmap = remember(cachedImage.file) {
                            try {
                                BitmapFactory.decodeFile(cachedImage.file.absolutePath)
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = cachedImage.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && selectedImage != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                selectedImage = null
            },
            title = { Text("删除缓存") },
            text = { Text("确定要删除这张缓存图片吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedImage?.file?.delete()
                        cachedImages = cachedImages.filter { it.file != selectedImage?.file }
                        cacheStats = CacheManager.getCacheStats(context)
                        showDeleteDialog = false
                        selectedImage = null
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    selectedImage = null
                }) {
                    Text("取消")
                }
            }
        )
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("清除全部缓存") },
            text = { Text("确定要清除所有缓存图片吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        CacheManager.clearDiskCache(context)
                        cachedImages = emptyList()
                        cacheStats = CacheStats(0, 0, 0, 0)
                        showClearAllDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

private fun loadCachedImages(context: Context): List<CachedImage> {
    val cacheDir = File(context.cacheDir, "image_cache")
    Timber.d("Cache dir: ${cacheDir.absolutePath}, exists: ${cacheDir.exists()}")

    if (!cacheDir.exists()) {
        cacheDir.mkdirs()
        return emptyList()
    }

    return cacheDir.walkTopDown()
        .filter { file ->
            file.isFile &&
            file.name != "journal" &&
            file.name.endsWith(".1") &&
            file.length() > 1000
        }
        .map { file ->
            CachedImage(
                file = file,
                name = file.name,
                size = file.length()
            )
        }
        .sortedByDescending { it.file.lastModified() }
        .toList()
}
