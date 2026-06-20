package com.randomimage.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.randomimage.ui.components.StatsChart
import com.randomimage.ui.components.StatsItem
import com.randomimage.ui.theme.ColorMode
import com.randomimage.util.CacheManager
import com.randomimage.util.StatsManager
import com.randomimage.util.ThemeManager

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    val colorMode by ThemeManager.colorModeFlow.collectAsState()
    val keyColor by ThemeManager.keyColorFlow.collectAsState()
    var imageQuality by remember { mutableStateOf("中等") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("设置") },
            navigationIcon = { IconButton(onClick = { onBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } }
        )

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("使用统计", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("浏览图片"); Text("${StatsManager.getViewCount(context)} 张") }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("收藏图片"); Text("${StatsManager.getFavoriteCount(context)} 张") }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("下载图片"); Text("${StatsManager.getDownloadCount(context)} 张") }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("搜索次数"); Text("${StatsManager.getSearchCount(context)} 次") }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("使用天数"); Text("${StatsManager.getDaysSinceFirstOpen(context)} 天") }
                    Spacer(modifier = Modifier.height(12.dp))
                    StatsChart(items = listOf(
                        StatsItem("浏览", StatsManager.getViewCount(context), MaterialTheme.colorScheme.primary),
                        StatsItem("收藏", StatsManager.getFavoriteCount(context), MaterialTheme.colorScheme.error),
                        StatsItem("下载", StatsManager.getDownloadCount(context), MaterialTheme.colorScheme.tertiary),
                        StatsItem("搜索", StatsManager.getSearchCount(context), MaterialTheme.colorScheme.secondary)
                    ))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("外观设置", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth().clickable { onThemeSettings() }, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("主题设置")
                            Text("自定义更多主题选项", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.graphicsLayer { rotationZ = 180f })
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth().clickable { showQualityDialog = true }, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("图片质量")
                            Text(imageQuality, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("数据管理", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    val cacheStats = CacheManager.getCacheStats(context)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("图片缓存"); Text("${cacheStats.imageCount} 张 / ${CacheManager.formatSize(cacheStats.imageSize)}") }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth().clickable { onPreviewCache() }, verticalAlignment = Alignment.CenterVertically) { Column(modifier = Modifier.weight(1f)) { Text("预览缓存图片"); Text("查看和管理已缓存的图片", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth().clickable { showClearCacheDialog = true }, verticalAlignment = Alignment.CenterVertically) { Column(modifier = Modifier.weight(1f)) { Text("清除图片缓存"); Text("清除已缓存的图片，下次加载会重新下载", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth().clickable { showClearHistoryDialog = true }, verticalAlignment = Alignment.CenterVertically) { Column(modifier = Modifier.weight(1f)) { Text("清除浏览历史"); Text("清除所有浏览过的图片记录", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth().clickable { showClearSearchDialog = true }, verticalAlignment = Alignment.CenterVertically) { Column(modifier = Modifier.weight(1f)) { Text("清除搜索历史"); Text("清除所有搜索记录", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth().clickable { onCloudSync() }, verticalAlignment = Alignment.CenterVertically) { Column(modifier = Modifier.weight(1f)) { Text("云同步"); Text("备份和恢复收藏数据", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth().clickable { onManageApis() }, verticalAlignment = Alignment.CenterVertically) { Column(modifier = Modifier.weight(1f)) { Text("自定义API"); Text("添加和管理自定义图片源", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth().clickable { onLogs() }, verticalAlignment = Alignment.CenterVertically) { Column(modifier = Modifier.weight(1f)) { Text("应用日志"); Text("查看和分享应用运行日志", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("关于", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("版本"); Text(com.randomimage.BuildConfig.VERSION_NAME) }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("随机图片 - 二次元图片浏览应用")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("主题系统参考 SukiSU Ultra", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showClearCacheDialog) {
        AlertDialog(onDismissRequest = { showClearCacheDialog = false }, title = { Text("清除缓存") }, text = { Text("确定要清除所有图片缓存吗？") }, confirmButton = { TextButton(onClick = { onClearCache(); showClearCacheDialog = false; Toast.makeText(context, "缓存已清除", Toast.LENGTH_SHORT).show() }) { Text("确定") } }, dismissButton = { TextButton(onClick = { showClearCacheDialog = false }) { Text("取消") } })
    }
    if (showClearHistoryDialog) {
        AlertDialog(onDismissRequest = { showClearHistoryDialog = false }, title = { Text("清除历史") }, text = { Text("确定要清除所有浏览历史吗？") }, confirmButton = { TextButton(onClick = { onClearHistory(); showClearHistoryDialog = false; Toast.makeText(context, "历史已清除", Toast.LENGTH_SHORT).show() }) { Text("确定") } }, dismissButton = { TextButton(onClick = { showClearHistoryDialog = false }) { Text("取消") } })
    }
    if (showClearSearchDialog) {
        AlertDialog(onDismissRequest = { showClearSearchDialog = false }, title = { Text("清除搜索历史") }, text = { Text("确定要清除所有搜索历史吗？") }, confirmButton = { TextButton(onClick = { onClearSearchHistory(); showClearSearchDialog = false; Toast.makeText(context, "搜索历史已清除", Toast.LENGTH_SHORT).show() }) { Text("确定") } }, dismissButton = { TextButton(onClick = { showClearSearchDialog = false }) { Text("取消") } })
    }
    if (showQualityDialog) {
        AlertDialog(onDismissRequest = { showQualityDialog = false }, title = { Text("图片质量") }, text = {
            Column {
                listOf("缩略图", "中等", "原图").forEach { quality ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { imageQuality = quality; onQualityChanged(quality); showQualityDialog = false }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = imageQuality == quality, onClick = { imageQuality = quality; onQualityChanged(quality); showQualityDialog = false })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(quality)
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = { showQualityDialog = false }) { Text("取消") } })
    }
}
