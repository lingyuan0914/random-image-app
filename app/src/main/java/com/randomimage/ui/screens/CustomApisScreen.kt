package com.randomimage.ui.screens

import android.widget.Toast
import androidx.activity.compose.PredictiveBackHandler
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import com.randomimage.data.remote.ApiType
import com.randomimage.data.remote.CustomApiConfig
import com.randomimage.data.remote.CustomApiManager
import com.randomimage.data.remote.PresetApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomApisScreen(
    onBack: () -> Unit,
    onApisChanged: () -> Unit = {}
) {
    PredictiveBackHandler { backEvents ->
        backEvents.collect { }
        onBack()
    }
    val context = LocalContext.current
    var apis by remember { mutableStateOf(CustomApiManager.getCustomApis()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showPresets by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newUrl by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("自定义API") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "手动添加")
                }
                IconButton(onClick = { showPresets = true }) {
                    Icon(Icons.Default.Add, contentDescription = "推荐API")
                }
            }
        )

        if (apis.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("暂无自定义API", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "点击右上角 + 添加推荐API",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { showAddDialog = true }) {
                        Text("手动添加API")
                    }
                    TextButton(onClick = { showPresets = true }) {
                        Text("浏览推荐API")
                    }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(apis, key = { it.id }) { api ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(api.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    api.url,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                val typeLabel = try { ApiType.valueOf(api.apiType).label } catch (_: Exception) { "自动检测" }
                                Text(
                                    typeLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                val rateText = if (api.rateLimit <= 0) "无限制" else "${api.rateLimit} 次/${api.rateLimitWindow}秒"
                                Text(
                                    "速率限制: $rateText",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Slider(
                                    value = api.rateLimit.toFloat(),
                                    onValueChange = { newValue ->
                                        val newApis = apis.map { if (it.id == api.id) it.copy(rateLimit = newValue.toInt()) else it }
                                        apis = newApis
                                    },
                                    onValueChangeFinished = {
                                        CustomApiManager.updateRateLimit(api.id, api.rateLimit, api.rateLimitWindow)
                                        onApisChanged()
                                    },
                                    valueRange = 0f..50f,
                                    steps = 49,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Switch(
                                checked = api.enabled,
                                onCheckedChange = {
                                    CustomApiManager.toggleCustomApi(api.id)
                                    apis = CustomApiManager.getCustomApis()
                                    onApisChanged()
                                }
                            )
                            IconButton(onClick = {
                                CustomApiManager.removeCustomApi(api.id)
                                apis = CustomApiManager.getCustomApis()
                                onApisChanged()
                                Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "删除",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPresets) {
        AlertDialog(
            onDismissRequest = { showPresets = false },
            title = { Text("推荐API") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CustomApiManager.presetApis) { preset ->
                        val alreadyAdded = apis.any { it.url == preset.url }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!alreadyAdded) {
                                        CustomApiManager.addCustomApi(preset.name, preset.url, preset.apiType)
                                        apis = CustomApiManager.getCustomApis()
                                        onApisChanged()
                                        Toast.makeText(context, "已添加 ${preset.name}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        preset.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (alreadyAdded) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        preset.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        preset.url,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (alreadyAdded) {
                                    Text("已添加", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPresets = false }) { Text("关闭") }
            }
        )
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; newName = ""; newUrl = "" },
            title = { Text("添加自定义API") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("名称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newUrl,
                        onValueChange = { newUrl = it },
                        label = { Text("API地址") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank() && newUrl.isNotBlank()) {
                        CustomApiManager.addCustomApi(newName, newUrl)
                        apis = CustomApiManager.getCustomApis()
                        onApisChanged()
                        Toast.makeText(context, "已添加", Toast.LENGTH_SHORT).show()
                    }
                    showAddDialog = false
                    newName = ""
                    newUrl = ""
                }) { Text("添加") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; newName = ""; newUrl = "" }) { Text("取消") }
            }
        )
    }
}
