package com.randomimage.ui.screens

import android.widget.Toast
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.randomimage.util.CloudSyncManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var webdavUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(true) }

    PredictiveBackHandler { backEvents ->
        backEvents.collect { }
        onBack()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("云同步") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("WebDAV 配置", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = webdavUrl,
                        onValueChange = { webdavUrl = it },
                        label = { Text("服务器地址") },
                        placeholder = { Text("https://example.com/webdav") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("用户名") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("密码") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = {
                            CloudSyncManager.configure(webdavUrl, username, password)
                            showConfigDialog = false
                            Toast.makeText(context, "配置已保存", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("保存配置")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("同步操作", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = {
                            scope.launch {
                                isExporting = true
                                val success = CloudSyncManager.exportFavorites(context)
                                isExporting = false
                                Toast.makeText(
                                    context,
                                    if (success) "导出成功" else "导出失败",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isExporting && !isImporting
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        }
                        Text("导出收藏到本地")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            scope.launch {
                                isImporting = true
                                val success = CloudSyncManager.importFavorites(context)
                                isImporting = false
                                Toast.makeText(
                                    context,
                                    if (success) "导入成功" else "导入失败",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isExporting && !isImporting
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                        } else {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        }
                        Text("从本地导入收藏")
                    }
                }
            }
        }
    }
}
