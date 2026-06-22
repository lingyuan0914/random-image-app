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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.randomimage.ui.theme.ColorMode
import com.randomimage.util.ThemeManager

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ThemeSettingsScreen(
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    val colorMode by ThemeManager.colorModeFlow.collectAsState()
    val keyColor by ThemeManager.keyColorFlow.collectAsState()
    val dynamicColor by ThemeManager.dynamicColorFlow.collectAsState()
    val amoled by ThemeManager.amoledFlow.collectAsState()
    val uiStyle by ThemeManager.uiStyleFlow.collectAsState()

    val selectedTabIndex = when (colorMode) {
        ColorMode.LIGHT, ColorMode.MONET_LIGHT -> 1
        ColorMode.DARK, ColorMode.DARK_AMOLED, ColorMode.MONET_DARK -> 2
        else -> 0
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("主题设置") },
            navigationIcon = { IconButton(onClick = { onBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 主题模式选择
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("主题模式", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    TabRow(selectedTabIndex = selectedTabIndex) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = {
                                val mode = if (dynamicColor) ColorMode.MONET_SYSTEM else ColorMode.SYSTEM
                                ThemeManager.setColorMode(context, mode)
                            },
                            text = { Text("跟随系统") }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = {
                                val mode = if (dynamicColor) ColorMode.MONET_LIGHT else ColorMode.LIGHT
                                ThemeManager.setColorMode(context, mode)
                            },
                            text = { Text("浅色") }
                        )
                        Tab(
                            selected = selectedTabIndex == 2,
                            onClick = {
                                val mode = if (amoled) ColorMode.DARK_AMOLED else if (dynamicColor) ColorMode.MONET_DARK else ColorMode.DARK
                                ThemeManager.setColorMode(context, mode)
                            },
                            text = { Text("深色") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 界面风格选择
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("界面风格", style = MaterialTheme.typography.titleMedium)
                    Text("切换后需要重启应用生效", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StyleChip(
                            label = "Miuix",
                            selected = uiStyle == "miuix",
                            onClick = {
                                ThemeManager.setUiStyle(context, "miuix")
                                Toast.makeText(context, "请重启应用以应用 Miuix 风格", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        StyleChip(
                            label = "Material",
                            selected = uiStyle == "material",
                            onClick = {
                                ThemeManager.setUiStyle(context, "material")
                                Toast.makeText(context, "请重启应用以应用 Material 风格", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 主题色选择
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("主题色", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeManager.keyColorOptions.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(color))
                                    .border(
                                        if (keyColor == color) 3.dp else 0.dp,
                                        MaterialTheme.colorScheme.onSurface,
                                        CircleShape
                                    )
                                    .clickable {
                                        ThemeManager.setKeyColor(context, color)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (keyColor == color) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "选中",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 高级设置
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("高级设置", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        SettingToggleItem(
                            title = "动态取色",
                            subtitle = "使用系统壁纸颜色",
                            checked = dynamicColor,
                            onCheckedChange = { enabled ->
                                ThemeManager.setDynamicColor(context, enabled)
                                // 更新颜色模式以反映动态取色状态
                                val currentMode = ThemeManager.getColorMode(context)
                                if (enabled && !currentMode.isMonet) {
                                    ThemeManager.setColorMode(context, ColorMode.fromValue(currentMode.toMonetMode()))
                                } else if (!enabled && currentMode.isMonet) {
                                    ThemeManager.setColorMode(context, ColorMode.fromValue(currentMode.toNonMonetMode()))
                                }
                            }
                        )
                    }

                    SettingToggleItem(
                        title = "AMOLED 深色",
                        subtitle = "纯黑色背景",
                        checked = amoled,
                        onCheckedChange = { enabled ->
                            ThemeManager.setAmoled(context, enabled)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
private fun StyleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
