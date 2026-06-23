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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.randomimage.ui.theme.ColorMode
import com.randomimage.ui.theme.UiMode
import com.randomimage.util.ThemeManager
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeSettingsScreen(
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val colorScheme = MiuixTheme.colorScheme

    val colorMode by ThemeManager.colorModeFlow.collectAsState()
    val keyColor by ThemeManager.keyColorFlow.collectAsState()
    val amoled by ThemeManager.amoledFlow.collectAsState()
    val uiStyle by ThemeManager.uiStyleFlow.collectAsState()

    val selectedTabIndex = when (colorMode) {
        ColorMode.LIGHT, ColorMode.MONET_LIGHT -> 1
        ColorMode.DARK, ColorMode.DARK_AMOLED, ColorMode.MONET_DARK -> 2
        else -> 0
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // TopBar
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = colorScheme.onBackground
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 界面风格
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("界面风格", fontSize = 18.sp)
                        Text(
                            text = "切换后需要重启应用生效",
                            fontSize = 12.sp,
                            color = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StyleChip(
                                label = "Miuix",
                                selected = uiStyle == UiMode.Miuix.value,
                                onClick = {
                                    ThemeManager.setUiStyle(context, UiMode.Miuix.value)
                                    Toast.makeText(context, "请重启应用以应用 Miuix 风格", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            StyleChip(
                                label = "Material",
                                selected = uiStyle == UiMode.Material.value,
                                onClick = {
                                    ThemeManager.setUiStyle(context, UiMode.Material.value)
                                    Toast.makeText(context, "请重启应用以应用 Material 风格", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 主题模式
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("主题模式", fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        TabRow(selectedTabIndex = selectedTabIndex) {
                            Tab(
                                selected = selectedTabIndex == 0,
                                onClick = { ThemeManager.setColorMode(context, ColorMode.SYSTEM) },
                                text = { Text("跟随系统") }
                            )
                            Tab(
                                selected = selectedTabIndex == 1,
                                onClick = { ThemeManager.setColorMode(context, ColorMode.LIGHT) },
                                text = { Text("浅色") }
                            )
                            Tab(
                                selected = selectedTabIndex == 2,
                                onClick = { ThemeManager.setColorMode(context, ColorMode.DARK) },
                                text = { Text("深色") }
                            )
                        }
                    }
                }
            }

            // AMOLED 深色
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("AMOLED 深色", fontSize = 16.sp)
                            Text(
                                text = "纯黑色背景，省电护眼",
                                fontSize = 12.sp,
                                color = colorScheme.onSurface
                            )
                        }
                        Switch(
                            checked = amoled,
                            onCheckedChange = { ThemeManager.setAmoled(context, it) }
                        )
                    }
                }
            }

            // 主题色
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("主题色", fontSize = 18.sp)
                        Text(
                            text = "选择 0 表示使用系统动态颜色",
                            fontSize = 12.sp,
                            color = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 系统动态颜色选项
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.surfaceVariant)
                                    .border(
                                        if (keyColor == 0) 3.dp else 0.dp,
                                        colorScheme.onSurface,
                                        CircleShape
                                    )
                                    .clickable { ThemeManager.setKeyColor(context, 0) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (keyColor == 0) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "选中",
                                        tint = colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            ThemeManager.keyColorOptions.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(color))
                                        .border(
                                            if (keyColor == color) 3.dp else 0.dp,
                                            colorScheme.onSurface,
                                            CircleShape
                                        )
                                        .clickable { ThemeManager.setKeyColor(context, color) },
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

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StyleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MiuixTheme.colorScheme
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) colorScheme.primary
                else colorScheme.surfaceVariant
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) colorScheme.primary else colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) colorScheme.onPrimary
            else colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
