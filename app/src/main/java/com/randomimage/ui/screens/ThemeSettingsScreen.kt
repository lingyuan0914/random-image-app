package com.randomimage.ui.screens

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.ui.unit.sp
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
    val predictiveBack by remember { mutableStateOf(ThemeManager.getPredictiveBack(context)) }
    var uiScale by remember { mutableFloatStateOf(1f) }

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

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            ThemePreviewCard(keyColor = keyColor, colorMode = colorMode)

            Spacer(modifier = Modifier.height(16.dp))

            ThemeModeTabs(
                selectedIndex = selectedTabIndex,
                onSelect = { index ->
                    val mode = when (index) {
                        1 -> if (dynamicColor) ColorMode.MONET_LIGHT else ColorMode.LIGHT
                        2 -> if (amoled) ColorMode.DARK_AMOLED else if (dynamicColor) ColorMode.MONET_DARK else ColorMode.DARK
                        else -> if (dynamicColor) ColorMode.MONET_SYSTEM else ColorMode.SYSTEM
                    }
                    ThemeManager.setColorMode(context, mode)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ColorPickerSection(
                keyColor = keyColor,
                onColorSelected = { ThemeManager.setKeyColor(context, it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        SettingToggleItem(
                            title = "启用 Monet 颜色",
                            subtitle = "使用系统壁纸动态取色",
                            checked = dynamicColor,
                            onCheckedChange = { ThemeManager.setDynamicColor(context, it) }
                        )
                    }

                    SettingToggleItem(
                        title = "AMOLED 深色",
                        subtitle = "纯黑色背景，省电护眼",
                        checked = amoled,
                        onCheckedChange = { ThemeManager.setAmoled(context, it) }
                    )

                    SettingToggleItem(
                        title = "预测性返回手势",
                        subtitle = "启用对预测性返回手势的支持",
                        checked = predictiveBack,
                        onCheckedChange = { ThemeManager.setPredictiveBack(context, it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("界面缩放", style = MaterialTheme.typography.bodyLarge)
                    Text("调整全局显示比例", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("${(uiScale * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
                        Slider(
                            value = uiScale,
                            onValueChange = { uiScale = it },
                            valueRange = 0.8f..1.2f,
                            steps = 3,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ThemePreviewCard(keyColor: Int, colorMode: ColorMode) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("预览卡片", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary))
                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary))
                }
            }
        }
    }
}

@Composable
private fun ThemeModeTabs(selectedIndex: Int, onSelect: (Int) -> Unit) {
    val tabs = listOf("跟随系统", "浅色", "深色")
    TabRow(selectedTabIndex = selectedIndex) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onSelect(index) },
                text = { Text(title) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorPickerSection(keyColor: Int, onColorSelected: (Int) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("主题色", style = MaterialTheme.typography.bodyLarge)
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
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                            .clickable { onColorSelected(color) },
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
