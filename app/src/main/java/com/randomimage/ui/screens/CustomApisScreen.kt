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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.randomimage.data.remote.ApiType
import com.randomimage.ui.viewmodel.CustomApisViewModel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.SuperArrow
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@Composable
fun CustomApisScreen(
    onBack: () -> Unit,
    onApisChanged: () -> Unit = {},
    viewModel: CustomApisViewModel = hiltViewModel()
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showPresets by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newUrl by remember { mutableStateOf("") }
    val colorScheme = MiuixTheme.colorScheme

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
            Text("自定义API", fontSize = 20.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = { showAddDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "手动添加",
                    tint = colorScheme.onBackground
                )
            }
            IconButton(onClick = { showPresets = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "推荐API",
                    tint = colorScheme.onBackground
                )
            }
        }

        if (uiState.apis.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("暂无自定义API", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("点击右上角 + 添加推荐API", fontSize = 14.sp, color = colorScheme.onSurface)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { showAddDialog = true }) { Text("手动添加API") }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { showPresets = true }) { Text("浏览推荐API") }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.apis, key = { it.id }) { api ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(api.name ?: "未命名", fontSize = 16.sp)
                                Text(
                                    text = api.url ?: "未知地址",
                                    fontSize = 12.sp,
                                    color = colorScheme.onSurface,
                                    maxLines = 1
                                )
                                val typeLabel = try {
                                    ApiType.valueOf(api.apiType ?: "").label
                                } catch (_: Exception) {
                                    "自动检测"
                                }
                                Text(typeLabel, fontSize = 11.sp, color = colorScheme.primary)
                                val rateText = if (api.rateLimit <= 0) "无限制" else "${api.rateLimit} 次/${api.rateLimitWindow}秒"
                                Text("速率限制: $rateText", fontSize = 11.sp, color = colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(4.dp))
                                Slider(
                                    value = api.rateLimit.toFloat(),
                                    onValueChange = { newValue ->
                                        viewModel.updateRateLimit(api.id, newValue.toInt(), api.rateLimitWindow)
                                        onApisChanged()
                                    },
                                    valueRange = 0f..50f
                                )
                            }
                            Switch(
                                checked = api.enabled,
                                onCheckedChange = {
                                    viewModel.toggleCustomApi(api.id)
                                    onApisChanged()
                                }
                            )
                            IconButton(onClick = {
                                viewModel.removeCustomApi(api.id)
                                onApisChanged()
                                Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "删除",
                                    tint = colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 推荐API对话框
    if (showPresets) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPresets = false },
            title = { Text("推荐API") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.presetApis) { preset ->
                        val alreadyAdded = uiState.apis.any { it.url == preset.url }
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (!alreadyAdded) {
                                    viewModel.addCustomApi(preset.name, preset.url, preset.apiType)
                                    onApisChanged()
                                    Toast.makeText(context, "已添加 ${preset.name}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.name ?: "未命名",
                                        fontSize = 16.sp,
                                        color = if (alreadyAdded) colorScheme.onSurface else colorScheme.onSurface
                                    )
                                    Text(preset.description ?: "", fontSize = 12.sp, color = colorScheme.onSurface)
                                    Text(preset.url ?: "", fontSize = 11.sp, color = colorScheme.onSurface)
                                }
                                if (alreadyAdded) {
                                    Text("已添加", fontSize = 12.sp, color = colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showPresets = false }) { Text("关闭") }
            },
            dismissButton = {}
        )
    }

    // 手动添加对话框
    if (showAddDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddDialog = false; newName = ""; newUrl = "" },
            title = { Text("添加自定义API") },
            text = {
                Column {
                    TextField(value = newName, onValueChange = { newName = it }, label = "名称")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(value = newUrl, onValueChange = { newUrl = it }, label = "API地址")
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotBlank() && newUrl.isNotBlank()) {
                        viewModel.addCustomApi(newName, newUrl)
                        onApisChanged()
                        Toast.makeText(context, "已添加", Toast.LENGTH_SHORT).show()
                    }
                    showAddDialog = false; newName = ""; newUrl = ""
                }) { Text("添加") }
            },
            dismissButton = {
                Button(onClick = { showAddDialog = false; newName = ""; newUrl = "" }) { Text("取消") }
            }
        )
    }
}
