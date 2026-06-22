package com.randomimage.ui.screens

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.randomimage.util.ThemeManager

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ThemeSettingsScreen(
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    val themeMode by ThemeManager.themeModeFlow.collectAsState()
    val keyColor by ThemeManager.keyColorFlow.collectAsState()
    val amoled by ThemeManager.amoledFlow.collectAsState()

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
            // 主题模式
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("主题模式", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    TabRow(selectedTabIndex = themeMode) {
                        Tab(
                            selected = themeMode == ThemeManager.MODE_SYSTEM,
                            onClick = { ThemeManager.setThemeMode(context, ThemeManager.MODE_SYSTEM) },
                            text = { Text("跟随系统") }
                        )
                        Tab(
                            selected = themeMode == ThemeManager.MODE_LIGHT,
                            onClick = { ThemeManager.setThemeMode(context, ThemeManager.MODE_LIGHT) },
                            text = { Text("浅色") }
                        )
                        Tab(
                            selected = themeMode == ThemeManager.MODE_DARK,
                            onClick = { ThemeManager.setThemeMode(context, ThemeManager.MODE_DARK) },
                            text = { Text("深色") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AMOLED 深色
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("AMOLED 深色", style = MaterialTheme.typography.bodyLarge)
                        Text("纯黑色背景，省电护眼", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = amoled,
                        onCheckedChange = { ThemeManager.setAmoled(context, it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 主题色
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

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
