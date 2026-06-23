package com.randomimage.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.randomimage.ui.components.StatsChart
import com.randomimage.ui.components.StatsItem
import com.randomimage.util.CacheManager
import com.randomimage.util.StatsManager
import com.randomimage.util.ThemeManager
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.extra.SuperArrow
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onClearCache: () -> Unit,
    onClearHistory: () -> Unit,
    onClearSearchHistory: () -> Unit,
    onPreviewCache: () -> Unit = {},
    onCloudSync: () -> Unit = {},
    onLogs: () -> Unit = {},
    onManageApis: () -> Unit = {},
    onThemeSettings: () -> Unit = {},
    onQualityChanged: (String) -> Unit = {}
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearSearchDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    val colorScheme = MiuixTheme.colorScheme
    var imageQuality by remember { mutableStateOf("中等") }

    Column(modifier = Modifier.fillMaxSize()) {
        // TopBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = colorScheme.onBackground
                )
            }
            Text("设置", fontSize = 20.sp)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 使用统计
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("使用统计", fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("浏览图片"); Text("${StatsManager.getViewCount(context)} 张")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("收藏图片"); Text("${StatsManager.getFavoriteCount(context)} 张")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("下载图片"); Text("${StatsManager.getDownloadCount(context)} 张")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("搜索次数"); Text("${StatsManager.getSearchCount(context)} 次")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("使用天数"); Text("${StatsManager.getDaysSinceFirstOpen(context)} 天")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        StatsChart(items = listOf(
                            StatsItem("浏览", StatsManager.getViewCount(context), colorScheme.primary),
                            StatsItem("收藏", StatsManager.getFavoriteCount(context), colorScheme.error),
                            StatsItem("下载", StatsManager.getDownloadCount(context), colorScheme.secondary),
                            StatsItem("搜索", StatsManager.getSearchCount(context), colorScheme.onSurface)
                        ))
                    }
                }
            }

            // 外观设置
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .fillMaxWidth()
                ) {
                    SuperArrow(
                        title = "主题设置",
                        summary = "自定义主题模式、主题色",
                        onClick = onThemeSettings
                    )
                    SuperArrow(
                        title = "图片质量",
                        summary = imageQuality,
                        onClick = { showQualityDialog = true }
                    )
                }
            }

            // 数据管理
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .fillMaxWidth()
                ) {
                    val cacheStats = CacheManager.getCacheStats(context)
                    SuperArrow(
                        title = "图片缓存",
                        summary = "${cacheStats.imageCount} 张 / ${CacheManager.formatSize(cacheStats.imageSize)}",
                        onClick = {}
                    )
                    SuperArrow(
                        title = "预览缓存图片",
                        summary = "查看和管理已缓存的图片",
                        onClick = onPreviewCache
                    )
                    SuperArrow(
                        title = "清除图片缓存",
                        summary = "清除已缓存的图片，下次加载会重新下载",
                        onClick = { showClearCacheDialog = true }
                    )
                    SuperArrow(
                        title = "清除浏览历史",
                        summary = "清除所有浏览过的图片记录",
                        onClick = { showClearHistoryDialog = true }
                    )
                    SuperArrow(
                        title = "清除搜索历史",
                        summary = "清除所有搜索记录",
                        onClick = { showClearSearchDialog = true }
                    )
                    SuperArrow(
                        title = "云同步",
                        summary = "备份和恢复收藏数据",
                        onClick = onCloudSync
                    )
                    SuperArrow(
                        title = "自定义API",
                        summary = "添加和管理自定义图片源",
                        onClick = onManageApis
                    )
                    SuperArrow(
                        title = "应用日志",
                        summary = "查看和分享应用运行日志",
                        onClick = onLogs
                    )
                }
            }

            // 关于
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("关于", fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("版本"); Text(com.randomimage.BuildConfig.VERSION_NAME)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("随机图片 - 二次元图片浏览应用")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // 清除缓存对话框
    if (showClearCacheDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("清除缓存") },
            text = { Text("确定要清除所有图片缓存吗？") },
            confirmButton = {
                Button(onClick = {
                    onClearCache()
                    showClearCacheDialog = false
                    Toast.makeText(context, "缓存已清除", Toast.LENGTH_SHORT).show()
                }) { Text("确定") }
            },
            dismissButton = {
                Button(onClick = { showClearCacheDialog = false }) { Text("取消") }
            }
        )
    }

    // 清除历史对话框
    if (showClearHistoryDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("清除历史") },
            text = { Text("确定要清除所有浏览历史吗？") },
            confirmButton = {
                Button(onClick = {
                    onClearHistory()
                    showClearHistoryDialog = false
                    Toast.makeText(context, "历史已清除", Toast.LENGTH_SHORT).show()
                }) { Text("确定") }
            },
            dismissButton = {
                Button(onClick = { showClearHistoryDialog = false }) { Text("取消") }
            }
        )
    }

    // 清除搜索历史对话框
    if (showClearSearchDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showClearSearchDialog = false },
            title = { Text("清除搜索历史") },
            text = { Text("确定要清除所有搜索历史吗？") },
            confirmButton = {
                Button(onClick = {
                    onClearSearchHistory()
                    showClearSearchDialog = false
                    Toast.makeText(context, "搜索历史已清除", Toast.LENGTH_SHORT).show()
                }) { Text("确定") }
            },
            dismissButton = {
                Button(onClick = { showClearSearchDialog = false }) { Text("取消") }
            }
        )
    }

    // 图片质量对话框
    if (showQualityDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            title = { Text("图片质量") },
            text = {
                Column {
                    listOf("缩略图", "中等", "原图").forEach { quality ->
                        SuperArrow(
                            title = quality,
                            summary = if (imageQuality == quality) "当前选择" else "",
                            onClick = {
                                imageQuality = quality
                                onQualityChanged(quality)
                                showQualityDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                Button(onClick = { showQualityDialog = false }) { Text("取消") }
            }
        )
    }
}
