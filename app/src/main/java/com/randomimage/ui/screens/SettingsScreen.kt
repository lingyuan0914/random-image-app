package com.randomimage.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.randomimage.util.StatsManager
import com.randomimage.util.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onClearCache: () -> Unit,
    onClearHistory: () -> Unit,
    onClearSearchHistory: () -> Unit
) {
    val context = LocalContext.current
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearSearchDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val themeMode = remember { mutableStateOf(ThemeManager.getThemeMode(context)) }
    val autoRefresh by remember { mutableStateOf(false) }
    var refreshInterval by remember { mutableFloatStateOf(30f) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("设置") }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 使用统计
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "使用统计",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "浏览图片")
                        Text(text = "${StatsManager.getViewCount(context)} 张")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "收藏图片")
                        Text(text = "${StatsManager.getFavoriteCount(context)} 张")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "下载图片")
                        Text(text = "${StatsManager.getDownloadCount(context)} 张")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "搜索次数")
                        Text(text = "${StatsManager.getSearchCount(context)} 次")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "使用天数")
                        Text(text = "${StatsManager.getDaysSinceFirstOpen(context)} 天")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 主题设置
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "外观设置",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showThemeDialog = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "主题模式")
                            Text(
                                text = when (themeMode.value) {
                                    ThemeManager.THEME_LIGHT -> "浅色模式"
                                    ThemeManager.THEME_DARK -> "深色模式"
                                    else -> "跟随系统"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "自动刷新")
                            Text(
                                text = "定时自动更换图片",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoRefresh,
                            onCheckedChange = { }
                        )
                    }
                    if (autoRefresh) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "刷新间隔: ${refreshInterval.toInt()} 秒")
                        Slider(
                            value = refreshInterval,
                            onValueChange = { refreshInterval = it },
                            valueRange = 10f..120f,
                            steps = 10
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 图片设置
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "图片设置",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "高清图片")
                            Text(
                                text = "加载原图（消耗更多流量）",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = false,
                            onCheckedChange = { }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "预加载")
                            Text(
                                text = "提前加载下一张图片",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = true,
                            onCheckedChange = { }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 数据管理
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "数据管理",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showClearCacheDialog = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "清除图片缓存")
                            Text(
                                text = "清除已缓存的图片，下次加载会重新下载",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showClearHistoryDialog = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "清除浏览历史")
                            Text(
                                text = "清除所有浏览过的图片记录",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showClearSearchDialog = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "清除搜索历史")
                            Text(
                                text = "清除所有搜索记录",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 关于
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "关于",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "版本")
                        Text(text = "1.0.0")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "随机图片 - 二次元图片浏览应用")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "支持API: Lolicon, 萌图, 色图API, Kori图库, 随机美图, 二次元风景",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // 主题选择对话框
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("主题模式") },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                themeMode.value = ThemeManager.THEME_SYSTEM
                                ThemeManager.setThemeMode(context, ThemeManager.THEME_SYSTEM)
                                showThemeDialog = false
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = themeMode.value == ThemeManager.THEME_SYSTEM,
                            onClick = {
                                themeMode.value = ThemeManager.THEME_SYSTEM
                                ThemeManager.setThemeMode(context, ThemeManager.THEME_SYSTEM)
                                showThemeDialog = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("跟随系统")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                themeMode.value = ThemeManager.THEME_LIGHT
                                ThemeManager.setThemeMode(context, ThemeManager.THEME_LIGHT)
                                showThemeDialog = false
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = themeMode.value == ThemeManager.THEME_LIGHT,
                            onClick = {
                                themeMode.value = ThemeManager.THEME_LIGHT
                                ThemeManager.setThemeMode(context, ThemeManager.THEME_LIGHT)
                                showThemeDialog = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("浅色模式")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                themeMode.value = ThemeManager.THEME_DARK
                                ThemeManager.setThemeMode(context, ThemeManager.THEME_DARK)
                                showThemeDialog = false
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = themeMode.value == ThemeManager.THEME_DARK,
                            onClick = {
                                themeMode.value = ThemeManager.THEME_DARK
                                ThemeManager.setThemeMode(context, ThemeManager.THEME_DARK)
                                showThemeDialog = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("深色模式")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 清除缓存对话框
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("清除缓存") },
            text = { Text("确定要清除所有图片缓存吗？") },
            confirmButton = {
                TextButton(onClick = {
                    onClearCache()
                    showClearCacheDialog = false
                    Toast.makeText(context, "缓存已清除", Toast.LENGTH_SHORT).show()
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 清除历史对话框
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("清除历史") },
            text = { Text("确定要清除所有浏览历史吗？") },
            confirmButton = {
                TextButton(onClick = {
                    onClearHistory()
                    showClearHistoryDialog = false
                    Toast.makeText(context, "历史已清除", Toast.LENGTH_SHORT).show()
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 清除搜索历史对话框
    if (showClearSearchDialog) {
        AlertDialog(
            onDismissRequest = { showClearSearchDialog = false },
            title = { Text("清除搜索历史") },
            text = { Text("确定要清除所有搜索历史吗？") },
            confirmButton = {
                TextButton(onClick = {
                    onClearSearchHistory()
                    showClearSearchDialog = false
                    Toast.makeText(context, "搜索历史已清除", Toast.LENGTH_SHORT).show()
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearSearchDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}


