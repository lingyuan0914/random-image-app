package com.randomimage.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import com.randomimage.util.CacheManager
import com.randomimage.util.CacheStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

data class CachedImage(val file: File, val name: String, val size: Long)

@Composable
fun CachePreviewScreen(
    onBack: () -> Unit,
    onImageClick: (com.randomimage.domain.model.ImageModel) -> Unit = {}
) {
    val context = LocalContext.current
    var cachedImages by remember { mutableStateOf<List<CachedImage>>(emptyList()) }
    var cacheStats by remember { mutableStateOf(CacheStats(0, 0, 0, 0)) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf<CachedImage?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val colorScheme = MiuixTheme.colorScheme

    LaunchedEffect(Unit) {
        isLoading = true
        cachedImages = withContext(Dispatchers.IO) { loadCachedImages(context) }
        cacheStats = withContext(Dispatchers.IO) { CacheManager.getCacheStats(context) }
        isLoading = false
    }

    BackHandler { onBack() }

    Column(modifier = Modifier.fillMaxSize()) {
        // TopBar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = colorScheme.onBackground
                )
            }
            Text(
                text = "缓存预览",
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.Center)
            )
            IconButton(
                onClick = { showClearAllDialog = true },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "清除全部",
                    tint = colorScheme.onBackground
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("共 ${cacheStats.imageCount} 张图片", fontSize = 14.sp, color = colorScheme.onSurface)
            Text(
                text = "总: ${CacheManager.formatSize(cacheStats.totalSize)}  图片: ${CacheManager.formatSize(cacheStats.imageSize)}  元数据: ${CacheManager.formatSize(cacheStats.metadataSize)}",
                fontSize = 12.sp,
                color = colorScheme.onSurface
            )
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            cachedImages.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无缓存图片", fontSize = 16.sp, color = colorScheme.onSurface)
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(cachedImages, key = { it.file.absolutePath }) { cachedImage ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clickable {
                                    selectedImage = cachedImage
                                    showDeleteDialog = true
                                }
                        ) {
                            var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
                            LaunchedEffect(cachedImage.file) {
                                bitmap = withContext(Dispatchers.IO) {
                                    try { BitmapFactory.decodeFile(cachedImage.file.absolutePath) } catch (_: Exception) { null }
                                }
                            }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap!!.asImageBitmap(),
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
    }

    // 图片预览对话框
    if (showDeleteDialog && selectedImage != null) {
        var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
        LaunchedEffect(selectedImage?.file) {
            bitmap = withContext(Dispatchers.IO) {
                selectedImage?.file?.let { try { BitmapFactory.decodeFile(it.absolutePath) } catch (_: Exception) { null } }
            }
        }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = false; selectedImage = null },
            title = { Text("图片预览") },
            text = {
                Column {
                    bitmap?.let { bmp ->
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "预览",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(selectedImage?.name ?: "", fontSize = 12.sp, color = colorScheme.onSurface)
                    Text(CacheManager.formatSize(selectedImage?.size ?: 0), fontSize = 12.sp, color = colorScheme.onSurface)
                }
            },
            confirmButton = {
                Button(onClick = {
                    selectedImage?.file?.delete()
                    cachedImages = cachedImages.filter { it.file != selectedImage?.file }
                    cacheStats = CacheManager.getCacheStats(context)
                    showDeleteDialog = false; selectedImage = null
                }) { Text("删除") }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false; selectedImage = null }) { Text("关闭") }
            }
        )
    }

    // 清除全部对话框
    if (showClearAllDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("清除全部缓存") },
            text = { Text("确定要清除所有缓存图片吗？") },
            confirmButton = {
                Button(onClick = {
                    CacheManager.clearDiskCache(context)
                    cachedImages = emptyList()
                    cacheStats = CacheStats(0, 0, 0, 0)
                    showClearAllDialog = false
                }) { Text("确定") }
            },
            dismissButton = {
                Button(onClick = { showClearAllDialog = false }) { Text("取消") }
            }
        )
    }
}

private fun loadCachedImages(context: Context): List<CachedImage> {
    val images = mutableListOf<CachedImage>()
    val coilCacheDir = File(context.cacheDir, "image_cache")
    if (coilCacheDir.exists()) {
        coilCacheDir.walkTopDown()
            .filter { it.isFile && it.name != "journal" && it.name.endsWith(".1") && it.length() > 1000 }
            .forEach { file -> images.add(CachedImage(file = file, name = file.name, size = file.length())) }
    }
    val customDir = File(context.cacheDir, "custom_api_images")
    if (customDir.exists()) {
        customDir.walkTopDown()
            .filter { it.isFile && it.length() > 1000 }
            .forEach { file -> images.add(CachedImage(file = file, name = file.name, size = file.length())) }
    }
    return images.sortedByDescending { it.file.lastModified() }
}
