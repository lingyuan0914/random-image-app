package com.randomimage.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.randomimage.ui.components.StatsChart
import com.randomimage.ui.components.StatsItem
import com.randomimage.util.CacheManager
import com.randomimage.util.StatsManager
import com.randomimage.util.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
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
    onQualityChanged: (String) -> Unit = {}
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearSearchDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    val themeMode = remember { mutableStateOf(ThemeManager.getThemeMode(context)) }
    var imageQuality by remember { mutableStateOf("\u4e2d\u7b49") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("\u8bbe\u7f6e") }, navigationIcon = { IconButton(onClick = { onBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "\u8fd4\u56de") } })

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("\u4f7f\u7528\u7edf\u8ba1", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("\u6d4f\u89c8\u56fe\u7247"); Text("${StatsManager.getViewCount(context)} \u5f20") }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("\u6536\u85cf\u56fe\u7247"); Text("${StatsManager.getFavoriteCount(context)} \u5f20") }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("\u4e0b\u8f7d\u56fe\u7247"); Text("${StatsManager.getDownloadCount(context)} \u5f20") }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("\u641c\u7d22\u6b21\u6570"); Text("${StatsManager.getSearchCount(context)} \u6b21") }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("\u4f7f\u7528\u5929\u6570"); Text("${StatsManager.getDaysSinceFirstOpen(context)} \u5929") }
                    Spacer(modifier = Modifier.height(12.dp))
                    StatsChart(items = listOf(StatsItem("\u6d4f\u89c8", StatsManager.getViewCount(context), MaterialTheme.colorScheme.primary), StatsItem("\u6536\u85cf", StatsManager.getFavoriteCount(context), MaterialTheme.colorScheme.error), StatsItem("\u4e0b\u8f7d", StatsManager.getDownloadCount(context), MaterialTheme.colorScheme.tertiary), StatsItem("\u641c\u7d22", StatsManager.getSearchCount(context), MaterialTheme.colorScheme.secondary)))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("\u5916\u89c2\u8bbe\u7f6e", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth().clickable { showThemeDialog = true }, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("\u4e3b\u9898\u6a21\u5f0f")
                            Text(when (themeMode.value) { ThemeManager.THEME_LIGHT -> "\u6d45\u8272\u6a21\u5f0f"; ThemeManager.THEME_DARK -> "\u6df1\u8272\u6a21\u5f0f"; else -> "\u8ddf\u968f\u7cfb\u7edf" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth().clickable { showQualityDialog = true }, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("\u56fe\u7247\u8d28\u91cf")
                            Text(imageQuality, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("\u6570\u636e\u7ba1\u7406", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    val cacheStats = CacheManager.getCacheStats(context)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("\u56fe\u7247\u7f13\u5b58"); Text("${cacheStats.imageCount} \u5f20 / ${CacheManager.formatSize(cacheStats.imageSize)}") }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth().clickable { onPreviewCache() }, verticalAlignment = Alignment.CenterVertically) { Column(modifier = Modifier.weight(1f)) { Text("\u9884\u89c8\u7f13\u5b58\u56fe\u7247"); Text("\u67e5\u770b\u548c\u7ba1\u7406\u5df2\u7f13\u5b58\u7684\u56fe\u7247", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth().clickable { showClearCacheDialog = true }, verticalAlignment = Alignment.CenterVertically) { Column(modifier = Modifier.weight(1f)) { Text("\u6e05\u9664\u56fe\u7247\u7f13\u5b58"); Text("\u6e05\u9664\u5df2\u7f13\u5b58\u7684\u56fe\u7247\uff0c\u4e0b\u6b21\u52a0\u8f7d\u4f1a\u91cd\u65b0\u4e0b\u8f7d", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth().clickable { showClearHistoryDialog = true }, verticalAlignment = Alignment.CenterVertically) { Column(modifier = Modifier.weight(1f)) { Text("\u6e05\u9664\u6d4f\u89c8\u5386\u53f2"); Text("\u6e05\u9664\u6240\u6709\u6d4f\u89c8\u8fc7\u7684\u56fe\u7247\u8bb0\u5f55", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth().clickable { showClearSearchDialog = true }, verticalAlignment = Alignment.CenterVertically) { Column(modifier = Modifier.weight(1f)) { Text("\u6e05\u9664\u641c\u7d22\u5386\u53f2"); Text("\u6e05\u9664\u6240\u6709\u641c\u7d22\u8bb0\u5f55", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth().clickable { onCloudSync() }, verticalAlignment = Alignment.CenterVertically) { Column(modifier = Modifier.weight(1f)) { Text("\u4e91\u540c\u6b65"); Text("\u5907\u4efd\u548c\u6062\u590d\u6536\u85cf\u6570\u636e", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth().clickable { onManageApis() }, verticalAlignment = Alignment.CenterVertically) { Column(modifier = Modifier.weight(1f)) { Text("\u81ea\u5b9a\u4e49API"); Text("\u6dfb\u52a0\u548c\u7ba1\u7406\u81ea\u5b9a\u4e49\u56fe\u7247\u6e90", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth().clickable { onLogs() }, verticalAlignment = Alignment.CenterVertically) { Column(modifier = Modifier.weight(1f)) { Text("\u5e94\u7528\u65e5\u5fd7"); Text("\u67e5\u770b\u548c\u5206\u4eab\u5e94\u7528\u8fd0\u884c\u65e5\u5fd7", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("\u5173\u4e8e", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("\u7248\u672c"); Text(com.randomimage.BuildConfig.VERSION_NAME) }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("\u968f\u673a\u56fe\u7247 - \u4e8c\u6b21\u5143\u56fe\u7247\u6d4f\u89c8\u5e94\u7528")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("\u652f\u6301API: Lolicon, \u840c\u56fe, \u8272\u56feAPI, Kori\u56fe\u5e93, \u968f\u673a\u7f8e\u56fe, \u4e8c\u6b21\u5143\u98ce\u666f + \u81ea\u5b9a\u4e49API", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showThemeDialog) {
        AlertDialog(onDismissRequest = { showThemeDialog = false }, title = { Text("\u4e3b\u9898\u6a21\u5f0f") }, text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth().clickable { themeMode.value = ThemeManager.THEME_SYSTEM; ThemeManager.setThemeMode(context, ThemeManager.THEME_SYSTEM); showThemeDialog = false }, verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = themeMode.value == ThemeManager.THEME_SYSTEM, onClick = { themeMode.value = ThemeManager.THEME_SYSTEM; ThemeManager.setThemeMode(context, ThemeManager.THEME_SYSTEM); showThemeDialog = false }); Spacer(modifier = Modifier.width(8.dp)); Text("\u8ddf\u968f\u7cfb\u7edf")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth().clickable { themeMode.value = ThemeManager.THEME_LIGHT; ThemeManager.setThemeMode(context, ThemeManager.THEME_LIGHT); showThemeDialog = false }, verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = themeMode.value == ThemeManager.THEME_LIGHT, onClick = { themeMode.value = ThemeManager.THEME_LIGHT; ThemeManager.setThemeMode(context, ThemeManager.THEME_LIGHT); showThemeDialog = false }); Spacer(modifier = Modifier.width(8.dp)); Text("\u6d45\u8272\u6a21\u5f0f")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth().clickable { themeMode.value = ThemeManager.THEME_DARK; ThemeManager.setThemeMode(context, ThemeManager.THEME_DARK); showThemeDialog = false }, verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = themeMode.value == ThemeManager.THEME_DARK, onClick = { themeMode.value = ThemeManager.THEME_DARK; ThemeManager.setThemeMode(context, ThemeManager.THEME_DARK); showThemeDialog = false }); Spacer(modifier = Modifier.width(8.dp)); Text("\u6df1\u8272\u6a21\u5f0f")
                }
            }
        }, confirmButton = { TextButton(onClick = { showThemeDialog = false }) { Text("\u53d6\u6d88") } })
    }

    if (showClearCacheDialog) {
        AlertDialog(onDismissRequest = { showClearCacheDialog = false }, title = { Text("\u6e05\u9664\u7f13\u5b58") }, text = { Text("\u786e\u5b9a\u8981\u6e05\u9664\u6240\u6709\u56fe\u7247\u7f13\u5b58\u5417\uff1f") }, confirmButton = { TextButton(onClick = { onClearCache(); showClearCacheDialog = false; Toast.makeText(context, "\u7f13\u5b58\u5df2\u6e05\u9664", Toast.LENGTH_SHORT).show() }) { Text("\u786e\u5b9a") } }, dismissButton = { TextButton(onClick = { showClearCacheDialog = false }) { Text("\u53d6\u6d88") } })
    }
    if (showClearHistoryDialog) {
        AlertDialog(onDismissRequest = { showClearHistoryDialog = false }, title = { Text("\u6e05\u9664\u5386\u53f2") }, text = { Text("\u786e\u5b9a\u8981\u6e05\u9664\u6240\u6709\u6d4f\u89c8\u5386\u53f2\u5417\uff1f") }, confirmButton = { TextButton(onClick = { onClearHistory(); showClearHistoryDialog = false; Toast.makeText(context, "\u5386\u53f2\u5df2\u6e05\u9664", Toast.LENGTH_SHORT).show() }) { Text("\u786e\u5b9a") } }, dismissButton = { TextButton(onClick = { showClearHistoryDialog = false }) { Text("\u53d6\u6d88") } })
    }
    if (showClearSearchDialog) {
        AlertDialog(onDismissRequest = { showClearSearchDialog = false }, title = { Text("\u6e05\u9664\u641c\u7d22\u5386\u53f2") }, text = { Text("\u786e\u5b9a\u8981\u6e05\u9664\u6240\u6709\u641c\u7d22\u5386\u53f2\u5417\uff1f") }, confirmButton = { TextButton(onClick = { onClearSearchHistory(); showClearSearchDialog = false; Toast.makeText(context, "\u641c\u7d22\u5386\u53f2\u5df2\u6e05\u9664", Toast.LENGTH_SHORT).show() }) { Text("\u786e\u5b9a") } }, dismissButton = { TextButton(onClick = { showClearSearchDialog = false }) { Text("\u53d6\u6d88") } })
    }
    if (showQualityDialog) {
        AlertDialog(onDismissRequest = { showQualityDialog = false }, title = { Text("\u56fe\u7247\u8d28\u91cf") }, text = {
            Column {
                listOf("\u7f29\u7565\u56fe", "\u4e2d\u7b49", "\u539f\u56fe").forEach { quality ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { imageQuality = quality; onQualityChanged(quality); showQualityDialog = false }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = imageQuality == quality, onClick = { imageQuality = quality; onQualityChanged(quality); showQualityDialog = false }); Spacer(modifier = Modifier.width(8.dp)); Text(quality)
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = { showQualityDialog = false }) { Text("\u53d6\u6d88") } })
    }
}
